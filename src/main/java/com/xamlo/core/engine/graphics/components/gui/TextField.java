package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IFrameTickable;
import com.xamlo.core.engine.graphics.api.gui.IKeyboardHandler;
import com.xamlo.core.engine.graphics.api.gui.ITextFieldChangeListener;
import com.xamlo.core.engine.graphics.api.gui.IPointerListener;
import com.xamlo.core.engine.graphics.api.gui.ITextSelectionHandler;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.elements.ITextField;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;
import com.xamlo.core.engine.graphics.font.ApplicationFont;
import com.xamlo.core.engine.graphics.font.UnicodeGlyphFont;
import com.xamlo.core.engine.graphics.fontsystem.FontSystem;
import com.xamlo.core.engine.graphics.devices.Clipboard;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;

/**
 * Однострочное текстовое поле: ввод символов, caret с миганием, выделение,
 * placeholder, ограничение длины, слушатель изменения.
 *
 * Ввод приходит через IKeyboardHandler/onCharTyped (маршрутизация — у
 * DefaultSceneController, элемент должен быть сфокусирован). Клик по полю
 * ставит caret по X-координате (IPointerListener). Буфер обмена и
 * горизонтальная прокрутка в v1 не поддерживаются.
 */
public class TextField extends Label implements ITextField, IKeyboardHandler, IFrameTickable, ITextSelectionHandler {

    /** Период мигания caret'а (наносекунды). */
    private static final long BLINK_PERIOD_NS = 533_000_000L;

    protected int maxLength;
    protected String placeholder;
    protected int caretIndex;
    /** Якорь выделения (-1 — выделения нет); выделение = диапазон [anchor, caret). */
    protected int selectionAnchor;
    protected ITextFieldChangeListener changeListener;

    protected NonInteractiveQuad caret;
    protected NonInteractiveQuad selectionHighlight;
    protected Label placeholderLabel;

    /** Label, неучаствующий в hit-test'е (placeholder не перехватывает клики). */
    private static class NonInteractiveLabel extends Label {
        private NonInteractiveLabel() {
            super();
        }

        @Override
        public boolean containsPoint(float x, float y) {
            return false;
        }
    }

    public TextField() {
        this("");
    }

    public TextField(String text) {
        super(text == null ? "" : text);
        this.maxLength = DEFAULT_MAX_LENGTH;
        this.placeholder = "";
        this.caretIndex = getText().length();
        this.selectionAnchor = -1;
        this.focusable = true;
        this.setAlignment(EnumAlignment.LEFT);
        this.setPadding(8);
        this.setBackgroundColor(new Color(20, 22, 28, 220));
        this.setTextColor(new Color(235, 235, 235));
        buildIndicators();
        setPointerListener(new IPointerListener() {
            @Override
            public void onPointer(IUIElement element, float x, float y) {
                placeCaretAtScreenX(x);
            }
        });
    }

    private void buildIndicators() {
        this.caret = new NonInteractiveQuad();
        addChild(caret);
        caret.setBackgroundColor(new Color(220, 220, 220));
        caret.setVisible(false);

        this.selectionHighlight = new NonInteractiveQuad();
        addChild(selectionHighlight);
        selectionHighlight.setBackgroundColor(new Color(72, 140, 220, 128));
        selectionHighlight.setVisible(false);

        this.placeholderLabel = new NonInteractiveLabel();
        addChild(placeholderLabel);
        placeholderLabel.setTextColor(new Color(140, 140, 140));
        placeholderLabel.setAlignment(EnumAlignment.LEFT);
        placeholderLabel.setPadding(getPadding());
        placeholderLabel.setFont(getFont());
        placeholderLabel.setVisible(false);
    }

    @Override
    public void setFont(IFont font) {
        super.setFont(font);
        if (placeholderLabel != null) {
            placeholderLabel.setFont(font);
        }
        updateIndicators();
    }

    @Override
    public void setPadding(int padding) {
        super.setPadding(padding);
        if (placeholderLabel != null) {
            placeholderLabel.setPadding(padding);
        }
    }

    @Override
    public void resize(UIElementGeometry geometry) {
        super.resize(geometry);
        if (placeholderLabel != null) {
            placeholderLabel.resize(new UIElementGeometry(0, 0, geometry.getWidth(), geometry.getHeight()));
        }
        updateIndicators();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        updateIndicators();
    }

    @Override
    public void setText(String text) {
        super.setText(text == null ? "" : text);
        caretIndex = Math.min(caretIndex, getText().length());
        selectionAnchor = -1;
        updateIndicators();
    }

    // --- ITextField -------------------------------------------------------

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        updateIndicators();
    }

    @Override
    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public int getCaretPosition() {
        return caretIndex;
    }

    @Override
    public void setCaretPosition(int position) {
        caretIndex = Math.max(0, Math.min(position, getText().length()));
        selectionAnchor = -1;
        updateIndicators();
    }

    @Override
    public int getSelectionStart() {
        if (!hasSelection()) {
            return -1;
        }
        return Math.min(selectionAnchor, caretIndex);
    }

    @Override
    public int getSelectionEnd() {
        if (!hasSelection()) {
            return -1;
        }
        return Math.max(selectionAnchor, caretIndex);
    }

    @Override
    public void setSelection(int start, int end) {
        int len = getText().length();
        int s = Math.max(0, Math.min(start, len));
        int e = Math.max(0, Math.min(end, len));
        selectionAnchor = s;
        caretIndex = e;
        updateIndicators();
    }

    public void clearSelection() {
        selectionAnchor = -1;
    }

    @Override
    public void selectAll() {
        setSelection(0, getText().length());
    }

    @Override
    public String getSelectedText() {
        if (!hasSelection()) {
            return "";
        }
        int from = Math.min(selectionAnchor, caretIndex);
        int to = Math.max(selectionAnchor, caretIndex);
        return getText().substring(from, to);
    }

    @Override
    public void setChangeListener(ITextFieldChangeListener listener) {
        this.changeListener = listener;
    }

    // --- ввод ------------------------------------------------------------

    @Override
    public void onCharTyped(char c) {
        if (!isFocused()) {
            return;
        }
        if (c == '\n' || c == '\r' || c == '\t') {
            return;
        }
        deleteSelection();
        String text = getText();
        if (text.length() >= maxLength) {
            return;
        }
        String newText = text.substring(0, caretIndex) + c + text.substring(caretIndex);
        applyText(newText);
        caretIndex++;
    }

    @Override
    public void onKeyPressed(EnumKeyboardButtons key, boolean ctrl, boolean shift) {
        if (!isFocused()) {
            return;
        }
        if (ctrl) {
            handleCtrlCombo(key);
            return;
        }
        switch (key) {
            case KEY_BACKSPACE:
                if (hasSelection()) {
                    deleteSelection();
                } else if (caretIndex > 0) {
                    int newCaret = caretIndex - 1;
                    String before = getText();
                    applyText(before.substring(0, newCaret) + before.substring(caretIndex));
                    // setText() уже стянул caret до длины нового текста — возвращаем нужный
                    caretIndex = newCaret;
                }
                break;
            case KEY_DELETE:
                if (hasSelection()) {
                    deleteSelection();
                } else if (caretIndex < getText().length()) {
                    String t = getText();
                    applyText(t.substring(0, caretIndex) + t.substring(caretIndex + 1));
                }
                break;
            case KEY_HOME:
                if (shift) {
                    extendSelectionTo(0);
                } else {
                    setCaretPosition(0);
                }
                break;
            case KEY_END:
                if (shift) {
                    extendSelectionTo(getText().length());
                } else {
                    setCaretPosition(getText().length());
                }
                break;
            case KEY_LEFT:
                if (shift) {
                    extendSelectionTo(Math.max(0, caretIndex - 1));
                } else {
                    setCaretPosition(caretIndex - 1);
                }
                break;
            case KEY_RIGHT:
                if (shift) {
                    extendSelectionTo(Math.min(getText().length(), caretIndex + 1));
                } else {
                    setCaretPosition(caretIndex + 1);
                }
                break;
            default:
                break;
        }
        updateIndicators();
    }

    /** Ctrl-комбинации: копирование/вырезание/вставка/выделить всё. */
    private void handleCtrlCombo(EnumKeyboardButtons key) {
        switch (key) {
            case KEY_C:
                if (hasSelection()) {
                    Clipboard.setString(getSelectedText());
                }
                break;
            case KEY_X:
                if (hasSelection()) {
                    Clipboard.setString(getSelectedText());
                    deleteSelection();
                }
                break;
            case KEY_V:
                pasteFromClipboard();
                break;
            case KEY_A:
                selectAll();
                break;
            default:
                break;
        }
        updateIndicators();
    }

    /** Вставляет содержимое буфера обмена в позицию caret (удаляя выделение). */
    private void pasteFromClipboard() {
        String pasted = Clipboard.getString();
        if (pasted == null || pasted.isEmpty()) {
            return;
        }
        // Вставляем только печатаемые символы (без управляющих, кроме перевода строки не поддерживается)
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < pasted.length(); i++) {
            char c = pasted.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || c < 32) {
                continue;
            }
            cleaned.append(c);
        }
        String toInsert = cleaned.toString();
        if (toInsert.isEmpty()) {
            return;
        }
        deleteSelection();
        String text = getText();
        int room = maxLength - text.length();
        if (room <= 0) {
            return;
        }
        if (toInsert.length() > room) {
            toInsert = toInsert.substring(0, room);
        }
        String newText = text.substring(0, caretIndex) + toInsert + text.substring(caretIndex);
        applyText(newText);
        caretIndex += toInsert.length();
    }

    @Override
    public void onFrame() {
        updateIndicators();
    }

    /** Пользовательское изменение текста: применяет и уведомляет слушателя. */
    private void applyText(String newText) {
        setText(newText);
        if (changeListener != null) {
            changeListener.onTextChanged(this, newText);
        }
    }

    /**
     * Shift+стрелка: двигает caret к позиции, а якорь выделения закрепляет
     * (первое нажатие закрепляет в текущей позиции caret'а, дальше выделение
     * растёт/сжимается от якоря, при пересечении — разворачивается).
     */
    private void extendSelectionTo(int position) {
        if (selectionAnchor < 0) {
            selectionAnchor = caretIndex;
        }
        caretIndex = Math.max(0, Math.min(position, getText().length()));
        updateIndicators();
    }

    private boolean hasSelection() {
        return selectionAnchor >= 0 && selectionAnchor != caretIndex;
    }

    private void deleteSelection() {
        if (!hasSelection()) {
            return;
        }
        int from = Math.min(selectionAnchor, caretIndex);
        int to = Math.max(selectionAnchor, caretIndex);
        String text = getText();
        applyText(text.substring(0, from) + text.substring(to));
        caretIndex = from;
        selectionAnchor = -1;
    }

    // --- геометрия caret/выделения ----------------------------------------

    /**
     * Ширина подстроки текста в пикселях (метрики того же шрифта, которым
     * рендерится текст поля), 0 если шрифт не резолвится.
     */
    public float getTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }
        UnicodeGlyphFont font = resolveGlyphFont();
        return font == null ? 0f : font.getStringWidth(text);
    }

    private UnicodeGlyphFont resolveGlyphFont() {
        IFont f = getFont();
        ApplicationFont key = (f == null || f.getFontFamily() == null)
                ? new ApplicationFont("Default", 12, false, false)
                : new ApplicationFont(f.getFontFamily(), f.getFontSize(), f.isBold(), f.isItalic());
        return FontSystem.getInstance().ensureFont(key);
    }

    /** Ширина первых n символов текущего текста (для геометрии caret/выделения). */
    private float textWidthUpTo(int index) {
        String text = getText();
        int n = Math.max(0, Math.min(index, text.length()));
        if (n == 0) {
            return 0f;
        }
        return getTextWidth(text.substring(0, n));
    }

    private int textPad() {
        return Math.max(getPadding(), 6);
    }

    /** Мигание caret'а; переопределяется в тестах. */
    protected boolean caretBlinkOn() {
        return (System.nanoTime() / BLINK_PERIOD_NS) % 2L == 0L;
    }

    private void updateIndicators() {
        int pad = textPad();
        int height = getGeometry().getHeight();
        int width = getGeometry().getWidth();
        int indicatorHeight = Math.max(8, height - 2 * pad);
        int y = pad;

        float caretX = pad + textWidthUpTo(caretIndex);
        caret.resize(new UIElementGeometry((int) caretX, y, 1, indicatorHeight));
        caret.setVisible(isFocused() && (hasSelection() || caretBlinkOn()));

        if (hasSelection()) {
            int from = Math.min(selectionAnchor, caretIndex);
            int to = Math.max(selectionAnchor, caretIndex);
            float x1 = pad + textWidthUpTo(from);
            float x2 = pad + textWidthUpTo(to);
            selectionHighlight.resize(new UIElementGeometry((int) x1, y, Math.max(1, (int) (x2 - x1)), indicatorHeight));
            selectionHighlight.setVisible(true);
        } else {
            selectionHighlight.setVisible(false);
        }

        placeholderLabel.setText(placeholder);
        placeholderLabel.setVisible(getText().isEmpty());
    }

    /** Ставит caret по X-координате клика в экранных координатах. */
    private void placeCaretAtScreenX(float screenX) {
        setCaretPosition(charIndexAtScreenX(screenX));
    }

    /**
     * Индекс символа под X-координатой (экранные координаты): первое положение,
     * где накопленная ширина текста не меньше смещения. Вне текста — начало/конец.
     */
    private int charIndexAtScreenX(float screenX) {
        int pad = textPad();
        float localX = screenX - getAbsX() - pad;
        String text = getText();
        UnicodeGlyphFont font = resolveGlyphFont();
        if (font == null) {
            return Math.max(0, Math.min(text.length(), localX < 0 ? 0 : text.length()));
        }
        if (localX <= 0) {
            return 0;
        }
        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (font.getStringWidth(text.substring(0, mid)) < localX) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    // --- выделение мышью (ITextSelectionHandler) ---------------------------

    @Override
    public void onSelectionStart(float x, float y) {
        int pos = charIndexAtScreenX(x);
        selectionAnchor = pos;
        caretIndex = pos;
        updateIndicators();
    }

    @Override
    public void onSelectionDrag(float x, float y) {
        caretIndex = charIndexAtScreenX(x);
        updateIndicators();
    }

    @Override
    public void onSelectionEnd(float x, float y) {
        // Жест завершён — выделение (и caret) остаются как есть.
    }

}
