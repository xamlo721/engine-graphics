package com.xamlo.core.engine.graphics.api.gui.elements;

/**
 * Интерфейс для контейнера с рамкой и заголовком, группирующего дочерние элементы.
 */
public interface IGroupBox extends IWidget {

    void setTitle(String title);

    String getTitle();

}
