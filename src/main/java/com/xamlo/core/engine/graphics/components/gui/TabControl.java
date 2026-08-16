package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.IClickListener;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;
import com.xamlo.core.engine.graphics.api.gui.elements.ITabControl;

/**
 * Контейнер вкладок: ряд кнопок сверху и переключаемые страницы под ними.
 * Клик по вкладке показывает её страницу и скрывает остальные; первый добавленный
 * таб выбран автоматически. Дополнительный обработчик (перегрузка addTab с IClickListener)
 * вызывается после переключения состояний.
 */
public class TabControl extends Widget implements ITabControl {

    private static final int TAB_HEIGHT = 36;
    private static final Color SELECTED_COLOR = new Color(58, 110, 165, 230);
    private static final Color UNSELECTED_COLOR = new Color(40, 42, 52, 200);
    private static final Color PAGE_BACKGROUND = new Color(10, 10, 14, 120);

    protected List<String> tabTitles;
    protected List<IWidget> pages;
    protected List<PushButton> tabs;
    protected List<IClickListener> extraHandlers;
    protected int selectedIndex;

    public TabControl() {
        super();
        this.tabTitles = new ArrayList<>();
        this.pages = new ArrayList<>();
        this.tabs = new ArrayList<>();
        this.extraHandlers = new ArrayList<>();
        this.selectedIndex = -1;
        relayout();
    }

    @Override
    public void addTab(String title, IWidget page) {
        addTab(title, page, null);
    }

    /**
     * Добавляет вкладку. afterAction вызывается после переключения на эту вкладку.
     */
    public void addTab(String title, IWidget page, IClickListener afterAction) {
        Objects.requireNonNull(page, "page must not be null");

        if (!page.hasBackgroundImage()) {
            // Страница без собственного фона получает нейтральную подложку один раз при добавлении.
            page.setBackgroundColor(PAGE_BACKGROUND);
        }
        tabTitles.add(title == null ? "" : title);
        pages.add(page);
        addChild(page);
        extraHandlers.add(afterAction);

        PushButton button = createTabButton(tabTitles.size() - 1);
        tabs.add(button);
        addChild(button);

        relayout();
        if (selectedIndex < 0) {
            setSelected(0);
        } else {
            refreshPageVisibility();
            refreshTabStyles();
        }
    }

    private PushButton createTabButton(int index) {
        final int capturedIndex = index;
        PushButton button = new PushButton(tabTitles.get(capturedIndex));
        button.setBackgroundColor(UNSELECTED_COLOR);
        button.setHoverColor(new Color(255, 255, 255, 96));
        button.setAlignment(EnumAlignment.CENTER);
        button.setPadding(4);
        button.setClickListener(new IClickListener() {
            @Override
            public void onClicked(IClickable element) {
                TabControl.this.setSelected(capturedIndex);
                IClickListener afterAction = TabControl.this.extraHandlers.get(capturedIndex);
                if (afterAction != null) {
                    afterAction.onClicked(element);
                }
            }
        });
        return button;
    }

    /** Переключает видимую страницу по индексу вкладок. */
    @Override
    public void setSelected(int index) {
        if (index < 0 || index >= pages.size()) {
            return;
        }
        this.selectedIndex = index;
        refreshPageVisibility();
        refreshTabStyles();
    }

    private void refreshPageVisibility() {
        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).setVisible(i == selectedIndex);
        }
    }

    private void refreshTabStyles() {
        for (int i = 0; i < tabs.size(); i++) {
            Color color = (i == selectedIndex) ? SELECTED_COLOR : UNSELECTED_COLOR;
            tabs.get(i).setBackgroundColor(color);
        }
    }

    @Override
    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    @Override
    public int getTabCount() {
        return tabTitles.size();
    }

    /** Вкладки делят ширину контейнера поровну; страницы занимают область ниже полосы вкладок. */
    protected void relayout() {
        int w = getGeometry().getWidth();
        int h = getGeometry().getHeight();
        int count = tabs.size();
        if (count > 0 && w > 0) {
            int tabWidth = Math.max(TAB_HEIGHT, w / count);
            for (int i = 0; i < count; i++) {
                tabs.get(i).resize(new UIElementGeometry(i * tabWidth, 0, tabWidth, TAB_HEIGHT));
            }
        }
        int pageHeight = Math.max(0, h - TAB_HEIGHT);
        for (IWidget page : pages) {
            page.resize(new UIElementGeometry(0, TAB_HEIGHT, w, pageHeight));
        }
    }

    @Override
    public void resize(UIElementGeometry geometry) {
        super.resize(geometry);
        relayout();
    }

}
