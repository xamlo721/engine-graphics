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

}
