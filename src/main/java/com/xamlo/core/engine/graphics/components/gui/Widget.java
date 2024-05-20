package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IFont;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;
import com.xamlo.core.engine.graphics.components.AbstractTexture;

public class Widget implements IWidget {
	
	protected UIElementGeometry geometry;
	protected IUIElement parent;
	protected List<IUIElement> childWidgets;
	protected boolean visible;
	protected boolean isEnable;
	protected boolean focusable;
	protected AbstractTexture backgroundImage;
	protected IColor backgroundColor;
	protected IFont font;
	protected String toolTipText;
	protected Border border;
	protected String widgetName;
	protected int margin;
	protected int padding;
	protected EnumAlignment alignment;

	public Widget() {
		
		this.geometry = new UIElementGeometry(0, 0, 0, 0);
		this.parent = null;
		this.childWidgets = new ArrayList<IUIElement>();
		this.visible = true;
		this.isEnable = true;
		this.focusable = false;
		this.backgroundColor = new Color(255, 255, 255);
		this.font = new Font("Default", 12, false, false);
		this.toolTipText = "";
		this.border = new Border(4, new Color(128, 128, 128));
		
	}
	
	public Widget(IWidget parent) {
		this.parent = parent;
		this.parent.addChild(this);
		this.geometry = new UIElementGeometry(0, 0, 0, 0);
		this.parent = null;
		this.childWidgets = new ArrayList<IUIElement>();
		this.visible = true;
		this.isEnable = true;
		this.focusable = false;
		this.backgroundColor = new Color(255, 255, 255);
		this.font = new Font("Default", 12, false, false);
		this.toolTipText = "";
		this.border = new Border(4, new Color(128, 128, 128));
		
	}
	
	@Override
	public void setParent(IUIElement parent) {
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
	public IUIElement getParent() {
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
	public UIElementGeometry getGeometry() {
		return geometry;
	}

	@Override
	public ElementSize getWidSize() {
		return geometry;
	}

	@Override
	public void resize(UIElementGeometry geometry) {

		if (hasParent()) {
			geometry.xCoord += ((IWidget)parent).getGeometry().xCoord;
			geometry.yCoord += ((IWidget)parent).getGeometry().yCoord;

		}
		
		this.geometry = geometry;
		
	}

	@Override
	public void resize(ElementSize size) {

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
		this.visible = visible;
		if(visible) {
			show();
		} else {
			hide();
		}
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addChild(IUIElement child) {
		this.childWidgets.add(child);
		//if (!child.hasParent()) {
			child.setParent(this);
			//Тут апдейт геометрии из-за того, что при добавлении парента, координаты становятся относительными
			//Но я думаю что это должно как-то не тут вообще обновляться
			this.resize(this.geometry);
		//}

	}
	
	@Override
	public List<IUIElement> getChildElements() {
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
		this.geometry = new UIElementGeometry(x, y, this.geometry.width, this.geometry.height);
	}

	@Override
	public boolean containsPoint(float x, float y) {
		
		boolean result = x >= geometry.getXCoord() && 
		           x <= geometry.getXCoord() + geometry.getWidth() && 
		           y >= geometry.getYCoord() && 
		           y <= geometry.getYCoord() + geometry.getHeight();
		
//		           if (result) {
//		        	   System.out.println("containsPoint with result " + result);
//		        	   System.out.println(this.toString());
//		           }
		           
        return result;
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
	public void setFocused(boolean focusable) {
		this.focusable = focusable;
	}

	@Override
	public boolean isFocused() {
		return this.focusable;
	}

	@Override
	public void setAlignment(EnumAlignment alignment) {
		this.alignment = alignment;
	}

	@Override
	public EnumAlignment getAlignment() {
		return this.alignment;
	}

	@Override
	public void setPadding(int padding) {
		this.padding = padding;
	}

	@Override
	public int getPadding() {
		return this.padding;
	}

	@Override
	public void setMargin(int margin) {
		this.margin = margin;
	}

	@Override
	public int getMargin() {
		return this.margin;
	}

}
