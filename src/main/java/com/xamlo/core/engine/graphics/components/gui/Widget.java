package com.xamlo.core.engine.graphics.components.gui;

import java.util.ArrayList;
import java.util.List;

import com.xamlo.core.engine.graphics.api.gui.IColor;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;
import com.xamlo.core.engine.graphics.api.gui.font.IFont;
import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.font.ApplicationFont;

import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.IBorderSupport;
import com.xamlo.core.engine.graphics.api.gui.IPointer;
import com.xamlo.core.engine.graphics.api.gui.IPointerListener;

public class Widget implements IWidget, IDropTarget, IBorderSupport, IPointer {

	private boolean dropActive;
	private IColor savedDropBackground;
	
	protected UIElementGeometry geometry;
	protected IUIElement parent;
	protected List<IUIElement> childWidgets;
	protected boolean visible;
	protected boolean isEnable;
	protected boolean focusable;
	protected boolean focused;
	protected AbstractTexture backgroundImage;
	protected IColor backgroundColor;
	protected IFont font;
	protected String toolTipText;
	protected Border border;
	protected String widgetName;
	protected int margin;
	protected int padding;
	protected EnumAlignment alignment;
	protected int zIndex;
	protected IPointerListener pointerListener;

	public Widget() {
		
		this.geometry = new UIElementGeometry(0, 0, 0, 0);
		this.parent = null;
		this.childWidgets = new ArrayList<IUIElement>();
		this.visible = true;
		this.isEnable = true;
		this.focusable = false;
		this.focused = false;
		this.backgroundColor = new Color(255, 255, 255);
		this.font = new ApplicationFont("Default", 12, false, false);
		this.toolTipText = "";
		this.border = new Border(4, new Color(128, 128, 128));
		this.zIndex = 0;
		
	}
	
	public Widget(IWidget parent) {
		this();
		this.parent = parent;
		parent.addChild(this);
	}

	@Override
	public void setParent(IUIElement parent) {
		this.parent = parent;
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
	public int getZIndex() {
		return zIndex;
	}

	@Override
	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;
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

	/**
	 * Хранит локальную координату элемента относительно родителя и копирует
	 * переданную геометрию (аргумент не мутируется, повторный вызов идемпотентен).
	 */
	@Override
	public void resize(UIElementGeometry geometry) {
		this.geometry = new UIElementGeometry(
				geometry.getXCoord(), geometry.getYCoord(),
				geometry.getWidth(), geometry.getHeight());
	}

	/**
	 * Изменяет размер, сохраняя текущую позицию.
	 */
	@Override
	public void resize(ElementSize size) {
		this.geometry = new UIElementGeometry(
				this.geometry.getXCoord(), this.geometry.getYCoord(),
				size.getWidth(), size.getHeight());
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
		child.setParent(this);
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

		// Точка за границей клипающего предка (прокрученное «за край» содержимое)
		// не может быть целью — то же правило, что и у скисора рендера.
		int[] clip = ClipContexts.effectiveClippedRect(this);
		if (clip != null &&
				(x < clip[0] || x > clip[0] + clip[2] || y < clip[1] || y > clip[1] + clip[3])) {
			return false;
		}

		float absX = this.getAbsX();
		float absY = this.getAbsY();

		return x >= absX &&
		       x <= absX + geometry.getWidth() &&
		       y >= absY &&
		       y <= absY + geometry.getHeight();
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
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return this.focused;
	}

	@Override
	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}

	@Override
	public boolean isFocusable() {
		return this.focusable;
	}

	@Override
	public void setPointerListener(IPointerListener listener) {
		this.pointerListener = listener;
	}

	@Override
	public IPointerListener getPointerListener() {
		return this.pointerListener;
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

	/** Подсветка состояния «готов принять перетаскиваемый элемент». */
	@Override
	public void setDropActive(boolean active) {
		if (this.dropActive == active) return;
		this.dropActive = active;
		if (active) {
			this.savedDropBackground = this.backgroundColor;
			this.setBackgroundColor(new Color(80, 220, 120));
		} else if (savedDropBackground != null) {
			this.setBackgroundColor(savedDropBackground);
			this.savedDropBackground = null;
		}
	}

	@Override
	public boolean isDropActive() {
		return dropActive;
	}

	protected int cornerRadius;

	/** Задаёт толщину рамки, сохраняя текущий цвет (рендеринг рамки — за счётчиком задач). */
	@Override
	public void setBorderSize(int size) {
		this.border = new Border(size, this.border.getColor());
	}

	@Override
	public int getBorderSize() {
		return border.getThickness();
	}

	@Override
	public void setBorderColor(IColor color) {
		this.border = new Border(border.getThickness(), color);
	}

	@Override
	public IColor getBorderColor() {
		return border.getColor();
	}

	@Override
	public void setCornerRadius(int radius) {
		this.cornerRadius = radius;
	}

	@Override
	public int getCornerRadius() {
		return cornerRadius;
	}

	@Override
	public void setBorder(Border border) {
		this.border = border;
	}

	@Override
	public Border getBorder() {
		return border;
	}

}
