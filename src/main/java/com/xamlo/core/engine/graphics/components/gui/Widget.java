package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IFont;
import com.xamlo.core.engine.graphics.api.gui.IWidget;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

public class Widget implements IWidget {
	
	protected WidgetGeometry geometry;
	protected IWidget parent;
	protected List<IWidget> childWidgets;
	protected boolean visible;
	protected boolean isEnable;
	protected boolean focusable;
	protected AbstractTexture backgroundImage;
	protected IColor backgroundColor;
	protected IFont font;
	protected String toolTipText;
	protected Border border;
	protected String widgetName;

	public Widget() {
		
		this.geometry = new WidgetGeometry(0, 0, 0, 0);
		this.parent = null;
		this.childWidgets = new ArrayList<IWidget>();
		this.visible = true;
		this.isEnable = true;
		this.focusable = false;
		this.backgroundColor = new Color(255, 255, 255);
		this.font = new Font("Default", 12, false, false);
		this.toolTipText = "";
		this.border = new Border(4, new Color(128, 128, 128));
		
	}
	
	public Widget(Widget parent) {
		this.parent = parent;
		this.parent.addChild(this);
		this.geometry = new WidgetGeometry(0, 0, 0, 0);
		this.parent = null;
		this.childWidgets = new ArrayList<IWidget>();
		this.visible = true;
		this.isEnable = true;
		this.focusable = false;
		this.backgroundColor = new Color(255, 255, 255);
		this.font = new Font("Default", 12, false, false);
		this.toolTipText = "";
		this.border = new Border(4, new Color(128, 128, 128));
		
	}
	
	@Override
	public void setParent(IWidget parent) {
		this.parent = parent;
		if (geometry != null) {
			this.resize(this.geometry);
		}
	}

	@Override
	public boolean hasParent() {
		return this.parent != null;
	}


	@Override
	public IWidget getParent() {
		return this.parent;
	}
	
	@Override
	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	@Override
	public String getWidgetName() {
		return this.widgetName;
	}
	
	@Override
	public WidgetGeometry getWidgetGeometry() {
		return geometry;
	}

	@Override
	public WidgetSize getWidSize() {
		return geometry;
	}

	@Override
	public void resize(WidgetGeometry geometry) {

		if (hasParent()) {
			geometry.xCoord += parent.getWidgetGeometry().xCoord;
			geometry.yCoord += parent.getWidgetGeometry().yCoord;

		}
		
		this.geometry = geometry;
		
	}

	@Override
	public void resize(WidgetSize size) {

		this.geometry.width = geometry.width;		
		this.geometry.height = geometry.height;		
	}

	@Override
	public void hide() {
		this.visible = false;
	}

	@Override
	public void show() {
		this.visible = true;
	}

	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			show();
		} else {
			hide();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addChild(IWidget child) {
		this.childWidgets.add(child);
		//if (!child.hasParent()) {
			child.setParent(this);
			//Тут апдейт геометрии из-за того, что при добавлении парента, координаты становятся относительными
			//Но я думаю что это должно как-то не тут вообще обновляться
			this.resize(this.geometry);
		//}

	}
	
	@Override
	public List<IWidget> getChildElements() {
		return this.childWidgets;
	}

	@Override
	public boolean hasBackgroundImage() {
		return this.backgroundImage != null;
	}
	
	@Override
	public void setBackgroundImage(AbstractTexture image) {
		this.backgroundImage = image;
	}

	@Override
	public AbstractTexture getBackgroundImage() {
		return this.backgroundImage;
	}

	@Override
	public void setPosition(int x, int y) {
		this.geometry = new WidgetGeometry(x, y, this.geometry.width, this.geometry.height);
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isEnable = enabled;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnable;
	}

	@Override
	public void setBackgroundColor(IColor color) {
		this.backgroundColor = color;
	}

	@Override
	public IColor getBackgroundColor() {
		return this.backgroundColor;
	}

	@Override
	public void setFont(IFont font) {
		this.font = font;
	}

	@Override
	public IFont getFont() {
		return this.font;
	}

	@Override
	public void setToolTipText(String tooltip) {
		this.toolTipText = tooltip;
	}

	@Override
	public String getToolTipText() {
		return this.toolTipText;
	}

	@Override
	public void setBorder(Border border) {
		this.border = border;
	}
	
	@Override
	public void setBorder(int borderSize) {
		this.border = new Border(borderSize, this.border.getColor());
	}

	@Override
	public Border getBorder() {
		return this.border;
	}

	@Override
	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}

	@Override
	public boolean isFocusable() {
		return this.focusable;
	}

}
