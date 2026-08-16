package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IClickListener;
import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.elements.ICheckbox;

/**
 * Элемент с бинарным состоянием: квадрат-индикатор слева + подпись справа.
 * Клик переключает состояние и затем прокидывает событие во внешний слушатель.
 * Индикаторы не участвуют в hit-test'е — клик по любой части элемента работает.
 */
public class CheckBox extends Label implements ICheckbox, IClickable {

    protected static final Color ACCENT = new Color(72, 140, 220);

    private static final int BOX_SIZE = 20;
    private static final int TICK_INSET = 5;
    private static final int BOX_X_PAD = 8;

    protected boolean checked;
    protected boolean pressed;
    protected NonInteractiveQuad boxIndicator;
    protected NonInteractiveQuad tickMark;
    protected IClickListener userClickHandler;

    public CheckBox() {
        this("");
    }

    public CheckBox(String text) {
        super(text);
        this.backgroundColor = new Color(0, 0, 0, 96);
        this.checked = false;

        this.boxIndicator = new NonInteractiveQuad();
        this.boxIndicator.setBackgroundColor(new Color(70, 70, 70, 200));
        this.addChild(this.boxIndicator);

        this.tickMark = new NonInteractiveQuad();
        this.tickMark.setBackgroundColor(new Color(235, 235, 235, 230));
        this.tickMark.setVisible(false);
        this.addChild(this.tickMark);

        updateVisualState();
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked == checked) {
            return;
        }
        this.checked = checked;
        updateVisualState();
    }

    @Override
    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public boolean isPressed() {
        return this.pressed;
    }

    /** Индикаторы позиционируются относительно текущей геометрии элемента. */
    private void relayoutIndicators() {
        int h = getGeometry().getHeight();
        int boxY = Math.max(4, (h - BOX_SIZE) / 2);
        this.boxIndicator.resize(new UIElementGeometry(BOX_X_PAD, boxY, BOX_SIZE, BOX_SIZE));
        int tickSize = BOX_SIZE - TICK_INSET * 2;
        int tickX = BOX_X_PAD + TICK_INSET;
        int tickY = boxY + TICK_INSET;
        this.tickMark.resize(new UIElementGeometry(tickX, tickY, tickSize, tickSize));
    }

    @Override
    public void resize(UIElementGeometry geometry) {
        super.resize(geometry);
        relayoutIndicators();
    }

    @Override
    public void hide() {
        super.hide();
        syncChildVisibility();
    }

    @Override
    public void show() {
        super.show();
        syncChildVisibility();
    }

    private void syncChildVisibility() {
        boolean visible = isVisible();
        this.boxIndicator.setVisible(visible);
        this.tickMark.setVisible(visible && checked);
    }

    /** Обновляет цвета индикаторов и видимость галочки по текущему состоянию. */
    protected void updateVisualState() {
        if (this.checked) {
            this.boxIndicator.setBackgroundColor(ACCENT);
        } else {
            this.boxIndicator.setBackgroundColor(new Color(70, 70, 70, 200));
        }
        syncChildVisibility();
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
                CheckBox.this.setChecked(!CheckBox.this.isChecked());
                if (CheckBox.this.userClickHandler != null) {
                    CheckBox.this.userClickHandler.onClicked(button);
                }
            }
        };
    }

}
