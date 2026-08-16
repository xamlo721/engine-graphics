package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IVisible;
import com.xamlo.core.engine.graphics.api.gui.elements.IGroupBox;

/**
 * Контейнер с тёмным полупрозрачным фоном, рамкой и заголовком в левом верхнем углу.
 * Заголовок — отдельный дочерний Label: он автоматически перемещается при resize()
 * родителя и скрывается вместе с ним.
 */
public class GroupBox extends Label implements IGroupBox {

    private static final int TITLE_X_PAD = 8;
    private static final int TITLE_HEIGHT = 24;

    protected String title;
    protected Label titleLabel;

    public GroupBox() {
        super("");
        this.title = "";
        this.backgroundColor = new Color(0, 0, 5, 128);
        setBorderSize(2);
    }

    @Override
    public void setTitle(String title) {
        this.title = title == null ? "" : title;
        if (this.title.isEmpty()) {
            hideTitleLabel();
            return;
        }
        ensureTitleLabel().setText(this.title);
        relayoutTitle();
        syncChildVisibility();
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    private Label ensureTitleLabel() {
        if (titleLabel == null) {
            titleLabel = new Label();
            titleLabel.setBackgroundColor(new Color(0, 0, 0, 0));
            titleLabel.setAlignment(EnumAlignment.LEFT);
            titleLabel.setPadding(TITLE_X_PAD / 2);
            addChild(titleLabel);
        }
        return titleLabel;
    }

    /** Заголовок позиционируется по верхнему краю и растягивается на ширину контейнера. */
    private void relayoutTitle() {
        if (titleLabel == null) {
            return;
        }
        int w = Math.max(getGeometry().getWidth(), TITLE_HEIGHT * 2);
        titleLabel.resize(new UIElementGeometry(TITLE_X_PAD, getBorderSize(), w - TITLE_X_PAD * 2, TITLE_HEIGHT));
    }

    @Override
    public void resize(UIElementGeometry geometry) {
        super.resize(geometry);
        relayoutTitle();
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
        if (titleLabel != null && !title.isEmpty()) {
            titleLabel.setVisible(visible);
        }
    }

    private void hideTitleLabel() {
        if (titleLabel != null) {
            titleLabel.setVisible(false);
        }
    }

}
