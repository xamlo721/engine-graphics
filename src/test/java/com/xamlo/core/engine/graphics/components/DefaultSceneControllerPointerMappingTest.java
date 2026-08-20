package com.xamlo.core.engine.graphics.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.xamlo.engine.device.events.WindowResizeEvent;
import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.TextField;
import com.xamlo.core.engine.graphics.viewport.ViewportSettings;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.events.MouseClickEvent;

/**
 * E9: с заданной логической сеткой курсорные события приходят физическими пикселями и
 * переводятся в пространство сетки ДО хит-теста — иначе после ресайза окна кнопки и их
 * точки нажатия рассинхронизируются. Без сетки маппинг identity (legacy-поведение).
 */
public class DefaultSceneControllerPointerMappingTest {

	@AfterEach
	void resetGlobalState() {
		ViewportSettings.setActive(null);
	}

	private static class TestScene implements IScene {
		private final List<IUIElement> elements = new ArrayList<>();

		void add(IUIElement element) {
			this.elements.add(element);
		}

		@Override
		public void load() {
		}

		@Override
		public List<AbstractSceneElement> getRenderableObject() {
			return new ArrayList<>();
		}

		@Override
		public IUIElement findElementAt(float xCoord, float yCoord) {
			for (int i = this.elements.size() - 1; i >= 0; i--) {
				if (this.elements.get(i).containsPoint(xCoord, yCoord)) {
					return this.elements.get(i);
				}
			}
			return null;
		}

		@Override
		public List<IUIElement> getGuiElements() {
			return this.elements;
		}

		@Override
		public Matrix4f getProjectionMatrix() {
			return new Matrix4f();
		}

		@Override
		public void setProjectionMatrix(Matrix4f transformMatrix) {
		}

		@Override
		public void unload() {
		}
	}

	private static class StubCamera implements ICamera {
		final List<Vector3f> moves = new ArrayList<>();

		@Override public Matrix4f getViewMatrix() { return new Matrix4f(); }
		@Override public void rotate(Vector3f vector) {}
		@Override public void move(Vector3f vector) { this.moves.add(vector); }
		@Override public Vector3f getPosition() { return new Vector3f(); }
		@Override public Vector3f getRotation() { return new Vector3f(); }
		@Override public float getFov() { return 70f; }
		@Override public void setFov(float fov) {}
		@Override public float getAspectRatio() { return 16f / 9f; }
		@Override public void setAspectRatio(int width, int height) {}
		@Override public float getNearDistance() { return 0.2f; }
		@Override public void setNearDistance(float far) {}
		@Override public float getFarDistance() { return 1000f; }
		@Override public void setFarDistance(float near) {}
	}

	private DefaultSceneController newController(TestScene testScene) {
		DefaultSceneController controller = new DefaultSceneController();
		controller.setScene(testScene);
		controller.setCamera(new StubCamera());
		return controller;
	}

	@Test
	public void physicalCursorMapsIntoActiveGridBeforeHitTest() {
		TestScene scene = new TestScene();
		// Логическое поле: [100..300] × [100..132]. Сетка 1920×1080 при физике 960×540 масштабирует вдвое.
		TextField field = new TextField();
		field.resize(new UIElementGeometry(100, 100, 200, 32));
		scene.add(field);
		DefaultSceneController controller = newController(scene);
		controller.setLogicalViewport(new com.xamlo.core.engine.graphics.viewport.LogicalViewport(1920, 1080));
		controller.onWindowResizeEvent(new WindowResizeEvent(960, 540));

		// Физический центр поля (100, 57) → логические (200, 114) — внутри [100..300]×[100..132].
		controller.onMouseClienEvent(new MouseClickEvent(100f, 57f, EnumMouseButtons.MOUSE_BUTTON_1));
		assertSame(field, controller.getFocusedElement(), "клик в физических координатах попал через маппинг сетки");
		assertTrue(field.isFocused());
	}

	@Test
	public void legacyModeKeepsRawCoordinates() {
		TestScene scene = new TestScene();
		TextField field = new TextField();
		field.resize(new UIElementGeometry(100, 100, 200, 32));
		scene.add(field);
		DefaultSceneController controller = newController(scene);
		assertFalse(controller.getLogicalViewport() != null, "сетка не задана");

		// Без сетки те же сырые координаты identity: точка (100, 57) выше поля и мимо.
		controller.onMouseClienEvent(new MouseClickEvent(100f, 57f, EnumMouseButtons.MOUSE_BUTTON_1));
		assertNull(controller.getFocusedElement(), "legacy-режим не трогает координаты курсора");
		assertFalse(field.isFocused());
	}
}
