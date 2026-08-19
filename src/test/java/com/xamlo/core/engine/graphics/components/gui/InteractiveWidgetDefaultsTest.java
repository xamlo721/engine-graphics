package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Контракт интерактивных элементов (E1/E2): стандартные кнопки фокусируются по
 * умолчанию, выключенный элемент не активируется, disabled-color хранится и отдаётся.
 */
public class InteractiveWidgetDefaultsTest {

    @Test
    public void interactiveWidgetsAreFocusableByDefault() {
        assertTrue(new PushButton("x").isFocusable(), "PushButton");
        assertTrue(new CheckBox().isFocusable(), "CheckBox");
        assertTrue(new RadioButton().isFocusable(), "RadioButton");
        assertTrue(new ComboBox().isFocusable(), "ComboBox");
    }

    @Test
    public void noArgPushButtonStartsEnabledLikeItsTextedCounterpart() {
        // Конструктор без аргумента раньше оставлял enabled=false, хотя кнопка оставалась кликабельной.
        PushButton plain = new PushButton();
        assertTrue(plain.isEnabled(), "кнопка из конструктора без аргументов включена");
        assertEquals("", plain.getText());
    }

    @Test
    public void plainWidgetsStayNonFocusableByDefault() {
        assertFalse(new Widget().isFocusable());
        assertFalse(new Label("t").isFocusable());
        assertFalse(new NonInteractiveQuad().isFocusable());
    }

    @Test
    public void activationHonorsDisabledStateAtTheWidgetLevelToo() {
        AtomicInteger clicks = new AtomicInteger();
        PushButton button = new PushButton("off");
        button.setClickListener(element -> clicks.incrementAndGet());
        button.setEnabled(false);

        button.activate();
        assertEquals(0, clicks.get(), "выключенная кнопка игнорирует прямую активацию");

        button.setEnabled(true);
        button.activate();
        assertEquals(1, clicks.get(), "включённая кнопка активируется");
    }

    @Test
    public void disabledColorRoundsTripThroughLabelAndPushButton() {
        Color dim = new Color(128, 128, 128);

        Label label = new Label("dimmed");
        assertTrue(label.isEnabled());
        assertNullRoundtrip(label);
        label.setDisabledColor(dim);
        assertSame(dim, label.getDisabledColor());
        label.setEnabled(false);
        assertFalse(label.isEnabled());

        PushButton button = new PushButton("disabled");
        button.setDisabledColor(dim);
        assertSame(dim, button.getDisabledColor());
        button.setEnabled(false);
        assertFalse(button.isEnabled());
        assertEquals(128, button.getDisabledColor().getRed());
    }

    private static void assertNullRoundtrip(Label label) {
        org.junit.jupiter.api.Assertions.assertNull(label.getDisabledColor(), "без явной установки цвета нет");
    }

}
