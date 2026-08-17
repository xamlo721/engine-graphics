package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xamlo.engine.api.devices.EnumKeyboardButtons;

/**
 * Режим пароля и горизонтальная прокрутка TextField. Метрики подменены на
 * детерминированные (10 px за символ), чтобы не зависеть от загруженного атласа.
 */
public class TextFieldPasswordAndHScrollTest {

    /** Поле с фиксированными метриками: каждый символ ровно charW пикселей. */
    private static final class TestField extends TextField {
        float charW = 10f;

        @Override
        protected boolean caretBlinkOn() {
            return true;
        }

        @Override
        protected float measureWidth(String text) {
            return text == null || text.isEmpty() ? 0f : text.length() * charW;
        }

        int caretLocalX() {
            return caret.getGeometry().getXCoord();
        }
    }

    private static TestField field(int width, int height) {
        TestField f = new TestField();
        f.resize(new UIElementGeometry(50, 60, width, height));
        f.setFocused(true);
        return f;
    }

    // --- режим пароли ------------------------------------------------------

    @Test
    public void passwordModeMasksRenderingButKeepsRealText() {
        TestField f = field(200, 32);
        f.setText("secret");
        assertFalse(f.isPasswordMode());
        assertEquals("secret", f.getDisplayText());

        f.setPasswordMode(true);
        assertTrue(f.isPasswordMode());
        assertEquals("secret", f.getText(), "реальный текст доступен без маски");
        assertEquals("••••••", f.getDisplayText());

        f.setPasswordMode(false);
        assertEquals("secret", f.getDisplayText());
    }

    @Test
    public void maskFollowsLengthAfterTypingAndDeletion() {
        TestField f = field(200, 32);
        f.setPasswordMode(true);
        f.onCharTyped('a');
        f.onCharTyped('b');
        assertEquals("ab", f.getText());
        assertEquals("••", f.getDisplayText());

        f.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertEquals("a", f.getText());
        assertEquals("•", f.getDisplayText());

        f.onKeyPressed(EnumKeyboardButtons.KEY_BACKSPACE, false, false);
        assertTrue(f.getDisplayText().isEmpty(), "пустое поле не рисует маску");
    }

    @Test
    public void customMaskCharacterIsUsed() {
        TestField f = field(200, 32);
        f.setMaskCharacter('#');
        f.setText("hi");
        f.setPasswordMode(true);
        assertEquals("##", f.getDisplayText());
        assertEquals('#', f.getMaskCharacter());
    }

    @Test
    public void selectionYieldsRealTextInPasswordMode() {
        TestField f = field(200, 32);
        f.setText("hunter2");
        f.setPasswordMode(true);
        f.setSelection(1, 4);
        assertEquals("unt", f.getSelectedText(), "выделение отдаёт реальные символы");
        assertEquals("•••••••", f.getDisplayText());
    }

    // --- горизонтальная прокрутка ------------------------------------------

    /** Поле 200px с padding'ом 8: видимая часть строки = 184 px. */
    private static final int VIEW_W = 184;

    @Test
    public void shortLineDoesNotScroll() {
        TestField f = field(200, 32);
        f.setText("abcde"); // 50 px < 184
        setCaretToEnd(f, 5);

        assertEquals(0f, f.maxHorizontalScroll(), 1e-6f);
        assertEquals(0f, f.getHorizontalScroll(), 1e-6f);
    }

    @Test
    public void longLineAutoFollowsCaretToTheEnd() {
        TestField f = field(200, 32);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append('a');
        }
        f.setText(sb.toString()); // ширина 400 → maxS = 400 − 184 = 216
        setCaretToEnd(f, 40);

        assertEquals(216f, f.getHorizontalScroll(), 1e-6f, "окно ушло до конца строки");
        int caretX = f.caretLocalX();
        assertTrue(caretX >= 0 && caretX <= 200, "caret остался внутри рамки поля: x=" + caretX);
    }

    @Test
    public void movingBackwardRestoresLeftEdge() {
        TestField f = field(200, 32);
        f.setText("aaaaaaaaaa"); // 100 px — влезает без скролла
        f.setMaxLength(50);
        for (char c : "bbbbbbbbb".toCharArray()) {
            f.onCharTyped(c);
        }
        f.setCaretPosition(f.getText().length());
        float scrolled = f.getHorizontalScroll();
        assertTrue(scrolled > 0f, "строка прокрутилась за caret'ом");

        f.setCaretPosition(0);
        assertEquals(0f, f.getHorizontalScroll(), 1e-6f, "возврат к началу сбросил смещение");
    }

    @Test
    public void setHorizontalScrollClampsToBothBounds() {
        TestField f = field(200, 32);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append('a');
        }
        f.setText(sb.toString());

        f.setHorizontalScroll(-50f);
        assertEquals(0f, f.getHorizontalScroll(), 1e-6f);

        f.setHorizontalScroll(9999f);
        assertEquals(216f, f.getHorizontalScroll(), 1e-6f, "кламп до максимальной ширины");
    }

    @Test
    public void manualScrollStaysWhenCaretStillInView() {
        TestField f = field(200, 32);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            sb.append('a');
        }
        f.setText(sb.toString()); // maxS = 300 − 184 = 116
        f.setCaretPosition(15);
        f.setHorizontalScroll(50f);
        float before = f.getHorizontalScroll();

        f.onKeyPressed(EnumKeyboardButtons.KEY_LEFT, false, false); // caret всё ещё в окне [50..234]

        assertEquals(before, f.getHorizontalScroll(), 1e-6f, "ручная прокрутка не сбрасывается внутри окна");
    }

    private static void setCaretToEnd(TestField f, int length) {
        f.setCaretPosition(length);
    }

}
