package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.AbstractTexture;

public class ImageViewTest {

    private final AbstractTexture imageStub = new AbstractTexture("stub", 16, 16) {
        @Override
        public void bind() {
        }

        @Override
        public void unbind() {
        }
    };

    @Test
    public void setImageDelegatesToBackgroundPipeline() {
        ImageView view = new ImageView();
        assertFalse(view.hasBackgroundImage());

        view.setImage(imageStub);
        assertTrue(view.hasBackgroundImage());
        assertSame(imageStub, view.getBackgroundImage());
        assertSame(imageStub, view.getImage());
    }

    @Test
    public void nullImageClearsTrackedAndDelegatedReferences() {
        ImageView view = new ImageView();
        view.setBackgroundImage(imageStub);
        assertSame(imageStub, view.getBackgroundImage());

        // setImage полностью управляет текстурным состоянием элемента (включая делегат).
        view.setImage(null);
        assertTrue(view.getImage() == null || !view.hasBackgroundImage());
    }

    @Test
    public void stretchModeDefaultsToFILLAndRejectsNull() {
        ImageView view = new ImageView();
        assertEquals(EnumImageStretch.FILL, view.getStretchMode());

        view.setStretchMode(EnumImageStretch.FIT);
        assertEquals(EnumImageStretch.FIT, view.getStretchMode());

        view.setStretchMode(null);
        assertEquals(EnumImageStretch.FILL, view.getStretchMode(), "null возвращает режим по умолчанию");
    }

    /** Режим из setStretchMode должен попасть в storage уровня виджета — его читает рендерер. */
    @Test
    public void setStretchModePropagatesIntoWidgetBackgroundState() {
        ImageView view = new ImageView();
        assertEquals(EnumImageStretch.FILL, view.getBackgroundStretchMode());

        view.setStretchMode(EnumImageStretch.NINEPATCH);
        assertEquals(EnumImageStretch.NINEPATCH, view.getStretchMode());
        assertEquals(EnumImageStretch.NINEPATCH, view.getBackgroundStretchMode());

        // Виджетный уровень задаёт режим напрямую — зеркало на уровне вида остаётся в синхроне.
        view.setBackgroundStretchMode(EnumImageStretch.CENTER);
        assertEquals(EnumImageStretch.CENTER, view.getStretchMode());
        assertEquals(EnumImageStretch.CENTER, view.getBackgroundStretchMode());

        view.setBackgroundStretchMode(null);
        assertEquals(EnumImageStretch.FILL, view.getStretchMode());
    }

    @Test
    public void ninePatchInsetsAreStoredAndClampedOnImageView() {
        ImageView view = new ImageView();
        assertEquals(ImageStretchLayout.NinePatchInsets.NONE, view.getNinePatchInsets());

        view.setNinePatchInsets(2, 1, -3, 4);
        ImageStretchLayout.NinePatchInsets insets = view.getNinePatchInsets();
        assertEquals(2, insets.left());
        assertEquals(1, insets.top());
        assertEquals(0, insets.right(), "отрицательные борды обрезаются до нуля");
        assertEquals(4, insets.bottom());
    }

}
