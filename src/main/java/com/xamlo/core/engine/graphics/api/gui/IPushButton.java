package com.xamlo.core.engine.graphics.api.gui;

import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.ButtonAlignment;
import com.xamlo.core.engine.graphics.components.gui.ButtonIconPosition;

public interface IPushButton extends ILabel {

    void setButtonColor(IColor color);

    IColor getButtonColor();

    void setButtonHoverColor(IColor color);

    IColor getButtonHoverColor();

    void setButtonPressedColor(IColor color);

    IColor getButtonPressedColor();

    void setButtonDisabledColor(IColor color);

    IColor getButtonDisabledColor();

    void setButtonBorderSize(int size);

    int getButtonBorderSize();

    void setButtonBorderColor(IColor color);

    IColor getButtonBorderColor();

    void setButtonCornerRadius(int radius);

    int getButtonCornerRadius();

    void setButtonIcon(AbstractTexture icon);

    AbstractTexture getButtonIcon();

    void setButtonIconPosition(ButtonIconPosition position);

    ButtonIconPosition getButtonIconPosition();

    void setButtonIconSpacing(int spacing);

    int getButtonIconSpacing();

    void setButtonAlignment(ButtonAlignment alignment);

    ButtonAlignment getButtonAlignment();

    void setButtonPadding(int padding);

    int getButtonPadding();

    void setButtonMargin(int margin);

    int getButtonMargin();

    void setButtonEnabled(boolean enabled);

    boolean isButtonEnabled();

    void setButtonVisible(boolean visible);

    boolean isButtonVisible();

    void setButtonPressed(boolean pressed);

    boolean isButtonPressed();

    void setButtonHovered(boolean hovered);

    boolean isButtonHovered();

    void setButtonFocused(boolean focused);

    boolean isButtonFocused();

    void setButtonClickListener(ButtonClickListener listener);

    ButtonClickListener getButtonClickListener();

}