package com.xamlo.core.engine.graphics.api.gui.elements;

import com.xamlo.core.engine.graphics.api.gui.IBackgroundSupport;
import com.xamlo.core.engine.graphics.api.gui.IFocusable;
import com.xamlo.core.engine.graphics.api.gui.IFontSupport;
import com.xamlo.core.engine.graphics.api.gui.IResizable;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.IVisible;

public interface IWidget extends IUIElement,
								 IVisible, 
								 IResizable,
								 IFocusable, 
								 IFontSupport,
								 IBackgroundSupport {

}
