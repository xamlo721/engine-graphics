package com.xamlo.core.engine.graphics.components.gui;

/**
 * Разделитель интерфейса: тонкая цветная полоса-квад для визуального разделения зон.
 * Горизонтальный — широкая и низкая, вертикальный — узкий и высокий; геометрию можно
 * переопределить обычным resize() как у любого виджета.
 */
public class Separator extends Widget {

    private static final Color DEFAULT_COLOR = new Color(150, 150, 150, 96);

    public Separator() {
        this(true);
    }

    /** @param horizontal true — горизонтальная линия, false — вертикальная */
    public Separator(boolean horizontal) {
        super();
        this.backgroundColor = DEFAULT_COLOR;
        if (horizontal) {
            this.resize(new ElementSize(240, 2));
        } else {
            this.resize(new ElementSize(2, 180));
        }
    }

}
