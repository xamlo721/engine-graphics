package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IClickListener;
import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.IItemSelectionListener;
import com.xamlo.core.engine.graphics.api.gui.elements.IComboBox;

/**
 * Выпадающий список: кнопка-заголовок показывает выбранный вариант, по клику под ней
 * раскрывается панель со списком пунктов. Выбор пункта закрывает панель и уведомляет
 * IItemSelectionListener; программный setSelectedIndex событие не генерирует.
 */
public class ComboBox extends PushButton implements IComboBox {

    private static final int ROW_HEIGHT = 32;

    /** Индекс слоя открытой панели: выше всех обычных элементов сцены. */
    public static final int POPUP_Z_INDEX = 1000;

    protected List<String> items;
    protected int selectedIndex;
    protected boolean open;
    protected Widget popupPanel;
    protected List<PushButton> itemButtons;
    protected IItemSelectionListener selectionListener;
    protected IClickListener userClickHandler;

    public ComboBox() {
        this("");
    }

    /** @param placeholderText подпись кнопки до первого выбора варианта */
    public ComboBox(String placeholderText) {
        super(placeholderText == null ? "" : placeholderText);
        this.items = new ArrayList<>();
        this.selectedIndex = -1;
        this.open = false;
        this.itemButtons = new ArrayList<>();
        this.backgroundColor = new Color(28, 30, 38, 220);
        setHoverColor(new Color(255, 255, 255, 96));
        setAlignment(EnumAlignment.LEFT);
        setPadding(8);
    }

    @Override
    public int addItem(String label) {
        String safeLabel = (label == null) ? "" : label;
        ensureCapacity(items.size() + 1);
        items.add(safeLabel);
        itemButtons.get(itemButtons.size() - 1).setText(safeLabel);
        // Видимость строк — чистая функция состояния: обновляем всегда, иначе строки,
        // добавленные при закрытой панели, остаются видимыми по умолчанию.
        layoutPopup();
        applyPanelVisibility();
        return items.size() - 1;
    }

    @Override
    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Устанавливает выбранный вариант без раскрытия панели и без события выбора. */
    @Override
    public void setSelectedIndex(int index) {
        if (index < -1 || index >= items.size()) {
            return;
        }
        this.selectedIndex = index;
        setText(index < 0 ? "" : items.get(index));
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    public void setSelectionListener(IItemSelectionListener listener) {
        this.selectionListener = listener;
    }

    /** Клик по кнопке раскрывает/скрывает панель, затем прокидывается во внешний слушатель. */
    private void toggleOpenState() {
        if (items.isEmpty()) {
            return;
        }
        ensureCapacity(items.size());
        this.open = !this.open;
        applyPanelVisibility();
    }

    private void closePanel() {
        this.open = false;
        applyPanelVisibility();
    }

    private void selectRow(int index) {
        closePanel();
        this.selectedIndex = index;
        setText(items.get(index));
        if (selectionListener != null) {
            selectionListener.onItemSelected(this, index);
        }
    }

    /** Геометрия панели: под заголовком, ширина не уже кнопки, высота — по числу вариантов. */
    protected void layoutPopup() {
        if (popupPanel == null) {
            return;
        }
        int panelWidth = Math.max(getGeometry().getWidth(), 120);
        popupPanel.resize(new UIElementGeometry(0, getGeometry().getHeight(), panelWidth, items.size() * ROW_HEIGHT));
        for (int i = 0; i < itemButtons.size(); i++) {
            itemButtons.get(i).resize(new UIElementGeometry(0, i * ROW_HEIGHT, panelWidth, ROW_HEIGHT));
        }
    }

    /** Видимость панели и строк полностью определяется состоянием open/числом вариантов. */
    private void applyPanelVisibility() {
        if (popupPanel == null) {
            return;
        }
        popupPanel.setVisible(open);
        // Открытая панель рисуется поверх остальных элементов сцены; строки
        // поднимает эффективный z-индекс (наследуются от панели).
        popupPanel.setZIndex(open ? POPUP_Z_INDEX : 0);
        for (int i = 0; i < itemButtons.size(); i++) {
            itemButtons.get(i).setVisible(open && i < items.size());
        }
    }

    /** Создаёт панель-попап и кнопки пунктов по мере необходимости. */
    private void ensureCapacity(int required) {
        if (required <= itemButtons.size()) {
            return;
        }
        if (popupPanel == null) {
            this.popupPanel = new Widget();
            this.popupPanel.setBackgroundColor(new Color(24, 26, 34, 245));
            addChild(this.popupPanel);
            layoutPopup();
            applyPanelVisibility();
        }
        while (itemButtons.size() < required) {
            final int index = itemButtons.size();
            PushButton row = new PushButton("");
            row.setBackgroundColor(new Color(0, 0, 0, 0));
            row.setHoverColor(new Color(72, 140, 220, 96));
            row.setAlignment(EnumAlignment.LEFT);
            row.setPadding(8);
            row.setClickListener(new IClickListener() {
                @Override
                public void onClicked(IClickable element) {
                    ComboBox.this.selectRow(index);
                }
            });
            popupPanel.addChild(row);
            itemButtons.add(row);
        }
    }

    @Override
    public void setClickListener(IClickListener listener) {
        this.userClickHandler = listener;
    }

    @Override
    public IClickListener getClickListener() {
        return new IClickListener() {
            @Override
            public void onClicked(IClickable button) {
                ComboBox.this.toggleOpenState();
                if (ComboBox.this.userClickHandler != null) {
                    ComboBox.this.userClickHandler.onClicked(button);
                }
            }
        };
    }

}
