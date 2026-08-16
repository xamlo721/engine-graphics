package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.ITextFieldChangeListener;
import com.xamlo.core.engine.graphics.api.gui.elements.ITextField;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;

public class TextFieldTest {

    private static class SteadyCaretField extends TextField {
        SteadyCaretField() {
            super();
        }

        @Override
        protected boolean caretBlinkOn() {
            return true;
        }
    }

    @Test
    public void defaultsMatchMinecraftBehavior() {
        TextField field = new TextField();
        assertEquals(ITextField.DEFAULT_MAX_LENGTH, field.getMaxLength());
        assertTrue(field.isFocusable(), "поле фокусируемо по умолчанию");
        assertEquals(0, field.getCaretPosition());
        assertEquals("", field.getPlaceholder());
        assertEquals(-1, field.getSelectionStart());
        assertEquals(-1, field.getSelectionEnd());
    }

    @Test
    public void typedCharactersAreInsertedAtCaret() {
        TextField field = new TextField("ab");
        field.setFocused(true);
        field.setCaretPosition(1);

        field.onCharTyped('X');
        assertEquals("aXb", field.getText());
        assertEquals(2, field.getCaretPosition());

        field.onCharTyped('Y');
        assertEquals("aXYb", field.getText());
        assertEquals(3, field.getCaretPosition());
    }

    @Test
    public void maxLengthIsRespected() {
        TextField field = new TextField();
        field.setMaxLength(3);
        field.setFocused(true);

        field.onCharTyped('a');
        field.onCharTyped('b');
        field.onCharTyped('c');
        field.onCharTyped('d');
        assertEquals("abc", field.getText(), "символ сверх maxLength не добавляется");
        assertEquals(3, field.getCaretPosition());
    }

    @Test
    public void typingReplacesSelection() {
        TextField field = new TextField("hello");
        field.setFocused(true);
        field.setSelection(1, 4);
        assertEquals("ell", field.getSelectedText());

        field.onCharTyped('X');
        assertEquals("hXo", field.getText());
        assertEquals(2, field.getCaretPosition());
        assertEquals(-1, field.getSelectionStart());
    }

    @Test
    public void backspaceAndDeleteEditAtCaret() {
        TextField field = new TextField("abcd");
        field.setFocused(true);
        field.setCaretPosition(2);

        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals("acd", field.getText());
        assertEquals(1, field.getCaretPosition());

        field.onKeyPressed(EnumKeyboardButtons.KEY_DELETE, false, false);
        assertEquals("ad", field.getText());
        assertEquals(1, field.getCaretPosition());
    }

    @Test
    public void backspacePastStartDoesNotCorruptCaret() {
        TextField field = new TextField("ab");
        field.setFocused(true);
        field.setCaretPosition(2);

        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals("", field.getText());
        // Лишние Backspace'и за начало строки не должны уводить caret в минус
        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals(0, field.getCaretPosition());

        field.onCharTyped('x');
        assertEquals("x", field.getText(), "ввод после лишних Backspace'ей работает");
    }

    @Test
    public void backspaceDeletesSelection() {
        TextField field = new TextField("hello");
        field.setFocused(true);
        field.selectAll();

        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals("", field.getText());
        assertEquals(0, field.getCaretPosition());
    }

    @Test
    public void arrowKeysMoveCaret() {
        TextField field = new TextField("abcdef");
        field.setFocused(true);
        field.setCaretPosition(2);

        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        assertEquals(1, field.getCaretPosition());

        field.onKeyPressed(EnumKeyboardButtons.KEY_RIGHT, false, false);
        assertEquals(2, field.getCaretPosition());

        field.onKeyPressed(EnumKeyboardButtons.KEY_HOME, false, false);
        assertEquals(0, field.getCaretPosition());

        field.onKeyPressed(EnumKeyboardButtons.KEY_END, false, false);
        assertEquals(6, field.getCaretPosition());

        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        field.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false);
        assertEquals(0, field.getCaretPosition(), "caret не уходит за начало");
    }

    @Test
    public void shiftArrowsExtendSelection() {
        TextField field = new TextField("abcdef");
        field.setFocused(true);
        field.setCaretPosition(2);

        field.onKeyPressed(EnumKeyboardButtons.KEY_RIGHT, false, true);
        assertEquals(2, field.getSelectionStart());
        assertEquals(3, field.getSelectionEnd());

        field.onKeyPressed(EnumKeyboardButtons.KEY_RIGHT, false, true);
        assertEquals(2, field.getSelectionStart());
        assertEquals(4, field.getSelectionEnd());
        assertEquals("cd", field.getSelectedText());
    }

    @Test
    public void selectAllCoversWholeText() {
        TextField field = new TextField("hello");
        field.setFocused(true);
        field.selectAll();
        assertEquals(0, field.getSelectionStart());
        assertEquals(5, field.getSelectionEnd());
        assertEquals("hello", field.getSelectedText());
    }

    @Test
    public void setTextClampsCaretAndClearsSelection() {
        TextField field = new TextField("hello world");
        field.setFocused(true);
        field.selectAll();

        field.setText("hi");
        assertEquals(2, field.getCaretPosition(), "caret не выходит за новый текст");
        assertEquals(-1, field.getSelectionStart());
    }

    @Test
    public void changeListenerFiresOnlyOnUserEdits() {
        TextField field = new TextField("ab");
        field.setFocused(true);
        AtomicReference<String> last = new AtomicReference<>("(none)");
        field.setChangeListener(new ITextFieldChangeListener() {
            @Override
            public void onTextChanged(ITextField f, String newText) {
                last.set(newText);
            }
        });

        field.setText("xyz");
        assertEquals("(none)", last.get(), "программный setText не шлёт событие");

        field.setCaretPosition(3);
        field.onCharTyped('!');
        assertEquals("xyz!", last.get());
    }

    @Test
    public void unfocusedFieldIgnoresInput() {
        TextField field = new TextField("ab");
        field.setFocused(false);

        field.onCharTyped('X');
        field.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals("ab", field.getText());
    }

    @Test
    public void placeholderVisibleOnlyWhenEmpty() {
        TextField field = new TextField();
        field.setPlaceholder("name");
        field.resize(new com.xamlo.core.engine.graphics.components.gui.UIElementGeometry(0, 0, 200, 32));

        assertTrue(field.placeholderLabel.isVisible(), "пустое поле показывает placeholder");

        field.setFocused(true);
        field.onCharTyped('n');
        assertFalse(field.placeholderLabel.isVisible(), "введённый текст скрывает placeholder");
    }

    @Test
    public void caretIsVisibleWhenFocusedAndHiddenWhenNot() {
        SteadyCaretField field = new SteadyCaretField();
        field.resize(new com.xamlo.core.engine.graphics.components.gui.UIElementGeometry(0, 0, 200, 32));

        field.setFocused(true);
        assertTrue(field.caret.isVisible(), "сфокусированное поле показывает caret");

        field.setFocused(false);
        assertFalse(field.caret.isVisible());
    }

    @Test
    public void caretGeometryFollowsCaretPosition() {
        SteadyCaretField field = new SteadyCaretField();
        field.setText("abc");
        field.resize(new com.xamlo.core.engine.graphics.components.gui.UIElementGeometry(0, 0, 200, 32));
        field.setFocused(true);

        field.setCaretPosition(0);
        int pad = Math.max(field.getPadding(), 6);
        assertEquals(pad, field.caret.getGeometry().getXCoord(), "caret у начала текста");

        field.setCaretPosition(2);
        float expected = pad + field.getTextWidth("ab");
        assertEquals((int) expected, field.caret.getGeometry().getXCoord());
    }

    @Test
    public void clickPlacesCaretByXCoordinate() {
        TextField field = new TextField("Hello");
        field.resize(new com.xamlo.core.engine.graphics.components.gui.UIElementGeometry(100, 50, 200, 32));
        field.setFocused(true);

        int pad = Math.max(field.getPadding(), 6);
        float clickX = field.getAbsX() + pad + field.getTextWidth("He");
        field.getPointerListener().onPointer(field, clickX, 60);
        assertEquals(2, field.getCaretPosition(), "клик после 'He' ставит caret на позицию 2");

        field.getPointerListener().onPointer(field, field.getAbsX() + pad - 5, 60);
        assertEquals(0, field.getCaretPosition(), "клик слева от текста ставит caret в начало");

        field.getPointerListener().onPointer(field, field.getAbsX() + 500, 60);
        assertEquals(5, field.getCaretPosition(), "клик справа от текста ставит caret в конец");
    }

    @Test
    public void selectionHighlightGeometrySpansSelectedRange() {
        TextField field = new TextField("abcdef");
        field.resize(new com.xamlo.core.engine.graphics.components.gui.UIElementGeometry(0, 0, 200, 32));
        field.setFocused(true);
        field.setSelection(1, 3);

        int pad = Math.max(field.getPadding(), 6);
        assertTrue(field.selectionHighlight.isVisible());
        assertEquals(pad + (int) field.getTextWidth("a"), field.selectionHighlight.getGeometry().getXCoord());
        assertEquals((int) (field.getTextWidth("abc") - field.getTextWidth("a")),
                field.selectionHighlight.getGeometry().getWidth());
    }

}
