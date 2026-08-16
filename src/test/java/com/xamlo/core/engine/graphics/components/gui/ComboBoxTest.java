package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IUIElement;

public class ComboBoxTest {

    @Test
    public void itemsAreAddedSequentiallyAndListIsUnmodifiable() {
        ComboBox combo = new ComboBox("pick");
        assertEquals(-1, combo.getSelectedIndex());

        int first = combo.addItem("Classic");
        int second = combo.addItem("Relaxed");
        assertEquals(0, first);
        assertEquals(1, second);
        assertEquals(2, combo.getItems().size());

        assertThrows(UnsupportedOperationException.class, () -> combo.getItems().add("X"), "внешний список неизменяем");
    }

    @Test
    public void programmaticSelectionUpdatesHeaderWithoutEvent() {
        final ComboBox combo = new ComboBox();
        combo.addItem("A");
        combo.addItem("B");
        AtomicInteger events = new AtomicInteger();
        combo.setSelectionListener((c, i) -> events.incrementAndGet());

        combo.setSelectedIndex(1);
        assertEquals("B", combo.getText(), "заголовок показывает выбранный вариант");
        assertEquals(1, combo.getSelectedIndex());
        assertEquals(0, events.get(), "программный выбор не генерирует события выбора");

        combo.setSelectedIndex(7);
        assertEquals(1, combo.getSelectedIndex(), "индекс вне списка игнорируется");
    }

    @Test
    public void clickTogglesPanelAndRowClickSelectsClosesNotifies() {
        final ComboBox combo = new ComboBox("");
        combo.addItem("Alpha");
        combo.addItem("Beta");
        final AtomicInteger selectedIndexes = new AtomicInteger(-1);
        combo.setSelectionListener((c, index) -> selectedIndexes.set(index));

        // Клик по заголовку открывает панель.
        combo.getClickListener().onClicked(combo);
        assertTrue(combo.isOpen());

        // Клик по второй строке выбирает её и закрывает панель.
        PushButton secondRow = findRowByLabel(combo, "Beta");
        assertNotNull(secondRow);
        secondRow.getClickListener().onClicked(secondRow);

        assertFalse(combo.isOpen());
        assertEquals(1, combo.getSelectedIndex());
        assertEquals("Beta", combo.getText());
        assertEquals(1, selectedIndexes.get());

        // Повторное раскрытие после закрытия снова показывает строки (регрессия потери видимости).
        combo.getClickListener().onClicked(combo);
        assertTrue(combo.isOpen());
        assertTrue(findRowByLabel(combo, "Alpha").isVisible(), "после повторного открытия строки видны");
    }

    @Test
    public void clickOnEmptyComboDoesNothing() {
        ComboBox empty = new ComboBox("");
        empty.getClickListener().onClicked(empty);
        assertFalse(empty.isOpen(), "без вариантов панель не раскрывается");
    }

    @Test
    public void rowsAddedWhilePanelClosedStayHidden() {
        ComboBox combo = new ComboBox("");
        combo.addItem("Alpha");
        combo.addItem("Beta");
        combo.addItem("Gamma");

        assertFalse(combo.isOpen());
        PushButton alpha = findRowByLabel(combo, "Alpha");
        PushButton beta = findRowByLabel(combo, "Beta");
        PushButton gamma = findRowByLabel(combo, "Gamma");
        assertNotNull(alpha);
        assertFalse(alpha.isVisible(), "первая строка скрыта, пока панель закрыта");
        assertFalse(beta.isVisible(), "строки, добавленные при закрытой панели, не видны");
        assertFalse(gamma.isVisible());

        combo.getClickListener().onClicked(combo);
        assertTrue(alpha.isVisible());
        assertTrue(beta.isVisible(), "после раскрытия все строки видны");
        assertTrue(gamma.isVisible());
    }

    private static PushButton findRowByLabel(ComboBox combo, String label) {
        for (IUIElement child : combo.getChildElements()) {
            if (!(child instanceof Widget)) {
                continue;
            }
            Widget panel = (Widget) child;
            for (IUIElement rowChild : panel.getChildElements()) {
                if (rowChild instanceof PushButton && label.equals(((PushButton) rowChild).getText())) {
                    return (PushButton) rowChild;
                }
            }
        }
        return null;
    }

}
