package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xamlo.core.engine.graphics.api.gui.IClickListener;
import com.xamlo.core.engine.graphics.api.gui.IClickable;
import com.xamlo.core.engine.graphics.api.gui.elements.IRadioButton;

/**
 * Элемент выбора одного варианта из группы: квадрат-индикатор слева + подпись справа.
 * Клик выбирает элемент и снимает отметку со всех остальных в той же группе.
 */
public class RadioButton extends Label implements IRadioButton, IClickable {

    protected static final Color ACCENT = new Color(72, 140, 220);

    private static final int BOX_SIZE = 20;
    private static final int TICK_INSET = 5;
    private static final int BOX_X_PAD = 8;

    /** Реестр групп: имя группы → элементы её участники (единый диспетчерский поток). */
    private static final Map<String, List<IRadioButton>> GROUPS = new HashMap<>();

    protected boolean checked;
    protected boolean pressed;
    protected String group;
    protected NonInteractiveQuad boxIndicator;
    protected NonInteractiveQuad tickMark;
    protected IClickListener userClickHandler;

    public RadioButton() {
        this("", null);
    }

    public RadioButton(String text) {
        this(text, null);
    }

    public RadioButton(String text, String group) {
        super(text);
        this.backgroundColor = new Color(0, 0, 0, 96);
        this.checked = false;
        setGroup(group);

        this.boxIndicator = new NonInteractiveQuad();
        this.boxIndicator.setBackgroundColor(new Color(70, 70, 70, 200));
        this.addChild(this.boxIndicator);

        this.tickMark = new NonInteractiveQuad();
        this.tickMark.setBackgroundColor(new Color(235, 235, 235, 230));
        this.tickMark.setVisible(false);
        this.addChild(this.tickMark);

        updateVisualState();
    }

    /** Выбор элемента снимает отметку со всех остальных в группе; программный сброс её не ставит обратно. */
    @Override
    public void setChecked(boolean value) {
        boolean previous = this.checked;
        if (previous == value) {
            return;
        }
        this.checked = value;
        if (value && group != null) {
            uncheckOthersInGroup();
        }
        updateVisualState();
    }

    /** Снятие отметки со остальных в группе не вызывает встречного каскада. */
    private void uncheckOthersInGroup() {
        List<IRadioButton> snapshot;
        synchronized (GROUPS) {
            List<IRadioButton> members = GROUPS.get(group);
            snapshot = members == null ? new ArrayList<>() : new ArrayList<>(members);
        }
        for (IRadioButton member : snapshot) {
            if (member != this && member.isChecked()) {
                member.setChecked(false);
            }
        }
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

    @Override
    public void setGroup(String group) {
        String oldGroup = this.group;
        if (!java.util.Objects.equals(oldGroup, group)) {
            removeFromRegistry(oldGroup);
            registerInRegistry(group);
            this.group = group;
        }
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    private void removeFromRegistry(String name) {
        synchronized (GROUPS) {
            List<IRadioButton> members = GROUPS.get(name);
            if (members == null) {
                return;
            }
            members.remove(this);
            if (members.isEmpty()) {
                GROUPS.remove(name);
            }
        }
    }

    private void registerInRegistry(String name) {
        if (name == null) {
            return;
        }
        synchronized (GROUPS) {
            GROUPS.computeIfAbsent(name, k -> new ArrayList<>()).add(this);
        }
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

    /** Обновляет цвета индикаторов и видимость отметки по текущему состоянию. */
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
                RadioButton.this.setChecked(true);
                if (RadioButton.this.userClickHandler != null) {
                    RadioButton.this.userClickHandler.onClicked(button);
                }
            }
        };
    }

    @Override
    public void free() {
        removeFromRegistry(this.group);
        super.free();
    }

}
