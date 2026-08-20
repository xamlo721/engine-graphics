package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.components.gui.ClipContexts;
import com.xamlo.core.engine.graphics.viewport.LogicalViewport;
import com.xamlo.core.engine.graphics.viewport.ViewportSettings;
import com.xamlo.engine.device.events.WindowResizeEvent;

/**
 * E9: контроллер уровня — legacy-режим без сетки не зовёт хук re-layout, заданная
 * сетка синхронизируется в ViewportSettings и каждое реально изменившееся физическое
 * окно даёт ровно один вызов onViewportChanged.
 */
public class DefaultSceneControllerViewportTest {

	@AfterEach
	void resetGlobalState() {
		ViewportSettings.setActive(null);
	}

	private static final class RecordingController extends DefaultSceneController {
		final List<int[]> hookCalls = new ArrayList<>();

		@Override
		protected void onViewportChanged(int logicalWidth, int logicalHeight, int physicalWidth, int physicalHeight) {
			this.hookCalls.add(new int[] {logicalWidth, logicalHeight, physicalWidth, physicalHeight});
		}
	}

	@Test
	public void legacyModeTracksSizeButNeverFiresRelayoutHook() {
		RecordingController controller = new RecordingController();
		controller.setScene(null);

		assertFalse(ViewportSettings.isActive(), "сетка не задана");
		controller.onWindowResizeEvent(new WindowResizeEvent(1280, 720));
		controller.onWindowResizeEvent(new WindowResizeEvent(1280, 720));
		assertEquals(0, controller.hookCalls.size(), "legacy-режим: хук re-layout не зовётся");
		assertNull(controller.getLogicalViewport());
		assertTrue(Float.isFinite(controller.toDesignX(5f)), "identity-маппинг без сетки работает");
	}

	@Test
	public void activeGridSyncsGlobalStateAndFiresOncePerRealChange() {
		RecordingController controller = new RecordingController();
		LogicalViewport grid = new LogicalViewport(640, 360);

		controller.setLogicalViewport(grid);
		assertSame(grid, ViewportSettings.getActive(), "сетка продублирована в глобальном состоянии рендеров");
		assertEquals(640, ViewportSettings.effectiveLogicalWidth());
		assertEquals(360, ViewportSettings.effectiveLogicalHeight());
		assertEquals(1, controller.hookCalls.size(), "установка сетки даёт один первичный вызов хука");
		int[] setup = controller.hookCalls.get(0);
		assertEquals(640, setup[0]);
		assertEquals(360, setup[1]);

		// Первое событие с размером, отличным от legacy-констант, — реальный resize.
		controller.onWindowResizeEvent(new WindowResizeEvent(1280, 720));
		assertEquals(2, controller.hookCalls.size(), "реально изменившееся окно дало второй вызов");
		int[] resized = controller.hookCalls.get(1);
		assertEquals(640, resized[0], "логическая ширина не меняется при ресайзе окна");
		assertEquals(720, resized[3], "физический размер обновлён до фактического");

		// Повтор того же размера — тишина; новый — ещё один вызов.
		controller.onWindowResizeEvent(new WindowResizeEvent(1280, 720));
		assertEquals(2, controller.hookCalls.size(), "повторное событие тем же размером игнорируется");
		controller.onWindowResizeEvent(new WindowResizeEvent(960, 540));
		assertEquals(3, controller.hookCalls.size());

		// Курсорная маппинг использует последний физический размер активной сетки.
		// Последнее окно в тесте — 960×540: его физические центры маппятся в середины сетки.
		assertEquals(320f, controller.toDesignX(480f), 1e-4f, "центр X окна 960×540 → середина логической ширины");
		assertEquals(180f, controller.toDesignY(270f), 1e-4f, "центр Y окна 960×540 → середина логической высоты");
	}

	@Test
	public void clearingGridRestoresLegacyConstantsAndStopsHooks() {
		RecordingController controller = new RecordingController();
		controller.setLogicalViewport(new LogicalViewport(854, 480));
		int afterSet = controller.hookCalls.size();

		controller.setLogicalViewport(null);
		assertNull(ViewportSettings.getActive(), "сброс снимает глобальную сетку");
		assertEquals(ClipContexts.WINDOW_WIDTH, ViewportSettings.effectiveLogicalWidth());
		assertEquals(ClipContexts.WINDOW_HEIGHT, ViewportSettings.effectiveLogicalHeight());
		assertTrue(controller.hookCalls.size() >= afterSet + 1, "сам сброс тоже сообщает о возврате к legacy-размерам");
	}
}
