package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IStretchedBackground;
import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

/**
 * Виджетный уровень растяжения фона (E3): значения по умолчанию, null-safe'ность,
 * перегрузка setBackgroundImage с режимом и совместимость legacy-реализаций IBackgroundSupport.
 */
public class WidgetStretchedBackgroundTest {

    private final AbstractTexture imageStub = new AbstractTexture("stub", 64, 32) {
        @Override
        public void bind() {
        }

        @Override
        public void unbind() {
        }
    };

    /**
     * Legacy-реализация, объявленная ДО E3 (только старый интерфейс): компилируется без единой
     * правки — новые члены ушли в default-методы расширенного интерфейса.
     */
    private abstract static class PlainBackgroundElement implements com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport {
        protected AbstractTexture image;

        @Override
        public boolean hasBackgroundImage() {
            return image != null;
        }

        @Override
        public AbstractTexture getBackgroundImage() {
            return image;
        }

        @Override
        public void setBackgroundImage(AbstractTexture image) {
            this.image = image;
        }

        @Override
        public void setBackgroundColor(IColor color) {
        }

        @Override
        public IColor getBackgroundColor() {
            return null;
        }
    }

    /** Та же legacy-копия, добавившая новый интерфейс: ничего не переопределяя, получает FILL-поведение. */
    private static final class AdoptedDefaultElement extends PlainBackgroundElement implements IStretchedBackground {
    }

    @Test
    public void widgetDefaultsKeepHistoricalFillBehavior() {
        Widget widget = new PushButton();

        assertEquals(EnumImageStretch.FILL, widget.getBackgroundStretchMode());
        assertEquals(ImageStretchLayout.NinePatchInsets.NONE, widget.getNinePatchInsets());
        assertFalse(widget.hasBackgroundImage());
    }

    @Test
    public void setBackgroundStretchModeIsNullSafeAndStored() {
        Widget widget = new PushButton();

        widget.setBackgroundStretchMode(null);
        assertEquals(EnumImageStretch.FILL, widget.getBackgroundStretchMode(), "null возвращает режим по умолчанию");

        widget.setBackgroundStretchMode(EnumImageStretch.FIT);
        assertEquals(EnumImageStretch.FIT, widget.getBackgroundStretchMode());
    }

    @Test
    public void backgroundImageOverloadSetsImageAndModeTogether() {
        Widget widget = new CheckBox();

        widget.setBackgroundImage(imageStub, EnumImageStretch.CENTER);
        assertTrue(widget.hasBackgroundImage());
        assertSame(imageStub, widget.getBackgroundImage());
        assertEquals(EnumImageStretch.CENTER, widget.getBackgroundStretchMode());

        // Перегрузка с null-режимом не сбивает уже выбранный.
        widget.setBackgroundImage(imageStub, null);
        assertSame(imageStub, widget.getBackgroundImage());
        assertEquals(EnumImageStretch.CENTER, widget.getBackgroundStretchMode());
    }

    @Test
    public void ninePatchInsetsClampNegativeSidesToZero() {
        Widget widget = new Slider();

        widget.setNinePatchInsets(-5, 2, -1, 3);
        ImageStretchLayout.NinePatchInsets insets = widget.getNinePatchInsets();
        assertEquals(0, insets.left());
        assertEquals(2, insets.top());
        assertEquals(0, insets.right());
        assertEquals(3, insets.bottom());
    }

    @Test
    public void legacyElementsAdoptNewInterfaceWithoutAnyOverridesAndStayOnFill() {
        AdoptedDefaultElement element = new AdoptedDefaultElement();

        // Без единого переопределения default-методы держат историческое поведение:
        // режим остаётся FILL, борды нулевые — рендерер пройдёт легаси-путь.
        assertEquals(EnumImageStretch.FILL, element.getBackgroundStretchMode());
        assertEquals(ImageStretchLayout.NinePatchInsets.NONE, element.getNinePatchInsets());
        assertTrue(element instanceof com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport);
    }

}
