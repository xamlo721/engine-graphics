package com.xamlo.core.engine.graphics.api.gui;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Реестр состояния жеста drag-and-drop на элементах сцены (E4). Пакетно-приватный:
 * внешним коду доступны только default-методы {@link IDraggable}. Записи хранятся в
 * слабых картах и исчезают вместе со сборкой элементов; ключи сравниваются по ссылке
 * (каталогные виджеты equals не переопределяют), вызовы ожидаются из потока диспетчера
 * событий ввода как и остальное состояние UI.
 */
final class DragStateRegistry {

	private static final Map<Object, DragPayload> PAYLOADS = Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<Object, Boolean> GHOST_PREVIEWS = Collections.synchronizedMap(new WeakHashMap<>());

	private DragStateRegistry() {
	}

	static void storePayload(Object element, DragPayload payload) {
		if (payload == null) {
			PAYLOADS.remove(element);
		} else {
			PAYLOADS.put(element, payload);
		}
	}

	static DragPayload payloadOf(Object element) {
		return PAYLOADS.get(element);
	}

	static boolean ghostPreviewEnabled(Object element) {
		Boolean enabled = GHOST_PREVIEWS.get(element);
		return enabled != null && enabled;
	}

	static void storeGhostPreview(Object element, boolean enabled) {
		if (enabled) {
			GHOST_PREVIEWS.put(element, Boolean.TRUE);
		} else {
			GHOST_PREVIEWS.remove(element);
		}
	}

}
