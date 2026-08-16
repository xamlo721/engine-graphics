package com.xamlo.core.engine.graphics.components.gui;

/**
 * Квад-виджет, неучастный в hit-test'е: точки внутри него «пропадают» и
 * разрешаются родительским элементом. Используется для внутренних индикаторов
 * (коробка чекбокса, бегунок слайдера и т.п.), чтобы клики доставались самому элементу.
 */
public class NonInteractiveQuad extends Widget {

    @Override
    public boolean containsPoint(float x, float y) {
        return false;
    }

}
