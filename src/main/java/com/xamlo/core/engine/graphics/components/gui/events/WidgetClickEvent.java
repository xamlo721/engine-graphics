package com.xamlo.core.engine.graphics.components.gui.events;

import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;

import net.lenni0451.asmevents.event.IEvent;

public class WidgetClickEvent implements IEvent {
	
	private final IWidget widget;

	public WidgetClickEvent(final IWidget widget) {
		this.widget = widget;
	}

	public IWidget getWidget() {
		return widget;
	}
	
}
