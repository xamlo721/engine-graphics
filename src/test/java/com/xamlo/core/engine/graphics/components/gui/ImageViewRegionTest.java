package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.TextureRegion;
import org.junit.jupiter.api.Test;

/**
 * Состояние подрегина на уровне виджета по умолчанию и после установки
 * (без GL: только storage-поля элемента).
 */
public class ImageViewRegionTest {

    @Test
    public void regionDefaultsToFullTexture() {
        var view = new ImageView();
        assertEquals(TextureRegion.FULL, view.getRegion());
    }

    @Test
    public void regionRoundTripsThroughWidgetStorage() {
        var view = new ImageView();
        view.setRegion(50, 10, 75, 40);
        var r = view.getRegion();
        assertEquals(new TextureRegion(50, 10, 75, 40), r.normalized());

        // «до края» по осям: нулевые размеры разрешаются против источника при раскладке
        view.setRegion(60, 20, -1, 40);
        assertEquals(-1, view.getRegion().width());
        assertEquals(40, view.getRegion().height());
    }
}
