package com.xamlo.core.engine.graphics.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.EventTarget;

import com.xamlo.engine.device.events.WindowResizeEvent;
import org.lwjgl.glfw.GLFWImage;

/**
 * E9: AbstractWindow.applyNewSize шлёт WindowResizeEvent ровно один раз на реально
 * изменившийся размер и не дёргает поток событий при повторном вызове с тем же размером.
 */
public class AbstractWindowResizeEmissionTest {

	/** Заглушка окна без GLFW: только чистая логика applyNewSize. */
	private static final class StubWindow extends AbstractWindow {
		@Override public void create(int width, int height) {}
		@Override public void setWindowTitle(String title) {}
		@Override public void setWindowIcon(GLFWImage icon) {}
		@Override public void swapBuffers() {}
		@Override public boolean isCloseRequested() { return false; }
		@Override public void resize(int x, int y) { this.applyNewSize(x, y); }
		@Override public void close() {}
		@Override public void update() {}
	}

	public static final class Probe {
		final List<WindowResizeEvent> events = new ArrayList<>();

		@EventTarget(noParamEvents = WindowResizeEvent.class)
		public void onResize(WindowResizeEvent event) {
			this.events.add(event);
		}
	}

	private final Probe probe = new Probe();

	@AfterEach
	void cleanup() {
		EventManager.unregister(this.probe);
	}

	@Test
	public void emitsOncePerRealChangeAndIgnoresRepeatedSameSize() {
		EventManager.register(this.probe);
		StubWindow window = new StubWindow();

		assertTrue(window.applyNewSize(640, 360), "первый реальный размер принимается");
		assertFalse(window.applyNewSize(640, 360), "повторный вызов с тем же размером ничего не меняет");
		assertTrue(window.applyNewSize(800, 500), "новый размер принимается повторно");
		assertEquals(2, this.probe.events.size(), "событий ровно по числу реально изменившихся размеров: " + this.probe.events);

		WindowResizeEvent first = this.probe.events.get(0);
		assertEquals(640, first.getWidth());
		assertEquals(360, first.getHeight());
		WindowResizeEvent second = this.probe.events.get(1);
		assertEquals(800, second.getWidth());
		assertEquals(500, second.getHeight());
		assertEquals(800, window.getWidth());
		assertEquals(500, window.getHeight());
	}
}
