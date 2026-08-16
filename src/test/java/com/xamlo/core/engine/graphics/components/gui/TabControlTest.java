package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IUIElement;

public class TabControlTest {

    @Test
    public void firstAddedTabIsSelectedAutomatically() {
        TabControl tabs = new TabControl();
        Label pageA = new Label("a");
        Label pageB = new Label("b");

        tabs.addTab("One", pageA);
        assertEquals(0, tabs.getSelectedIndex());
        assertTrue(pageA.isVisible());

        tabs.addTab("Two", pageB);
        assertTrue(pageA.isVisible(), "новая вкладка не сбрасывает текущий выбор");
        assertFalse(pageB.isVisible());
        assertEquals(2, tabs.getTabCount());
    }

    @Test
    public void selectingShowsOnlyTheChosenPage() {
        TabControl tabs = new TabControl();
        Label pageA = new Label("a");
        Label pageB = new Label("b");
        Label pageC = new Label("c");
        tabs.addTab("A", pageA);
        tabs.addTab("B", pageB);
        tabs.addTab("C", pageC);

        tabs.setSelected(1);
        assertTrue(pageB.isVisible());
        assertFalse(pageA.isVisible());
        assertFalse(pageC.isVisible());
        assertEquals(1, tabs.getSelectedIndex());

        tabs.setSelected(5);
        assertEquals(1, tabs.getSelectedIndex(), "индекс вне диапазона игнорируется");
    }

    @Test
    public void clickOnTabButtonSwitchesSelectionAndFiresAfterAction() {
        final TabControl tabs = new TabControl();
        final AtomicInteger afterCalls = new AtomicInteger();
        Label pageA = new Label("a");
        Label pageB = new Label("b");
        tabs.addTab("A", pageA);
        tabs.addTab("B", pageB, button -> afterCalls.incrementAndGet());

        PushButton secondButton = findTabButton(tabs, "B");
        assertNotNull(secondButton, "кнопка вкладки B найдена среди дочерних элементов");

        secondButton.getClickListener().onClicked(secondButton);

        assertEquals(1, tabs.getSelectedIndex());
        assertTrue(pageB.isVisible());
        assertFalse(pageA.isVisible());
        assertEquals(1, afterCalls.get(), "доп. обработчик вызван один раз после переключения");
    }

    private static PushButton findTabButton(TabControl tabs, String title) {
        for (IUIElement child : tabs.getChildElements()) {
            if (child instanceof PushButton && title.equals(((Label) child).getText())) {
                return (PushButton) child;
            }
        }
        return null;
    }

    @Test
    public void relayoutDistributesTabWidthsEvenlyAndPlacesPagesBelowStrip() {
        TabControl tabs = new TabControl();
        Label pageOne = new Label("a");
        tabs.addTab("One", pageOne);
        tabs.addTab("Two", new Label("b"));
        tabs.addTab("Three", new Label("c"));
        tabs.resize(new UIElementGeometry(0, 0, 360, 240));

        int tabCount = 0;
        for (IUIElement child : tabs.getChildElements()) {
            if (!(child instanceof PushButton)) {
                continue;
            }
            tabCount++;
            assertEquals(36, ((PushButton) child).getGeometry().getHeight());
            assertEquals(120, ((PushButton) child).getGeometry().getWidth(), "ширина делится поровну: 360/3=120");
        }
        assertEquals(3, tabCount);

        assertEquals(36, pageOne.getGeometry().getYCoord(), "страницы начинаются под полосой вкладок");
        assertEquals(360, pageOne.getGeometry().getWidth());
        assertEquals(204, pageOne.getGeometry().getHeight(), "высота страницы — остаток контрола после полосы");
    }

}
