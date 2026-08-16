package com.xamlo.core.engine.graphics.api.gui.elements;

/**
 * Интерфейс для контейнера вкладок: ряд кнопок сверху + переключаемые страницы под ними.
 */
public interface ITabControl extends IWidget {

    void addTab(String title, IWidget page);

    int getSelectedIndex();

    void setSelected(int index);

    int getTabCount();

}
