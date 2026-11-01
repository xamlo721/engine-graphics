package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthMask;

import org.joml.Matrix4f;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneRenderer;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.ZOrder;
import com.xamlo.core.engine.graphics.components.ChunkMeshElement;
import com.xamlo.core.engine.graphics.components.Frustum;
import com.xamlo.core.engine.graphics.opengl.blend.EnumOpenglBlendMode;
import com.xamlo.core.engine.graphics.opengl.blend.OpenGLBlend;
import com.xamlo.core.engine.graphics.opengl.depth.EnumOpenGLDepthMode;
import com.xamlo.core.engine.graphics.opengl.depth.OpenGLDepth;
import com.xamlo.engine.api.resources.IResourceLoader;

public class DefaultSceneRenderer implements ISceneRenderer {

	
	private UIElementRenderer uiRenderer;
	private TextElementRenderer textRenderer;
	private SceneElementRenderer elementRenderer;

	/**
	 * Diagnostic counters for the opaque-world pass, refreshed once per rendered frame: how many
	 * {@link ChunkMeshElement} instances were drawn versus skipped by view-volume culling on that
	 * exact frame (-1 until the first world pass has run). Read-only from any thread after init.
	 */
	public volatile int lastOpaqueWorldDrawn = -1;
	public volatile int lastOpaqueWorldCulled = -1;
	
	public DefaultSceneRenderer(IResourceLoader<String> resourceLoader) {
		this.uiRenderer = new UIElementRenderer();
		this.textRenderer = new TextElementRenderer();
		this.elementRenderer = new SceneElementRenderer();
	}

	public void setDebugWidgetNames(boolean enabled) {
		textRenderer.setDebugWidgetNames(enabled);
	}

	public boolean isDebugWidgetNames() {
		return textRenderer.isDebugWidgetNames();
	}

	@Override
	public void init() {
        //Рисовать рамку или заливать цветом - закомментировать, если хотим цвет
		//GL13.glPolygonMode(GL13.GL_FRONT_AND_BACK, GL13.GL_LINE);
		
		OpenGLBlend.enable();
		OpenGLBlend.setMode(EnumOpenglBlendMode.SRC_ALPHA, EnumOpenglBlendMode.ONE_MINUS_SRC_ALPHA);
        
		
		OpenGLDepth.enable();
        OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.ALWAYS);
        
        uiRenderer.init();
        textRenderer.init();
        elementRenderer.init();

        // clear the framebuffer
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

	}

	@Override
	public void loadScene(IScene scene) {
		
		//TODO: Подгрузка в кеш текстур и моделей сцены
		
		scene.load();
		
	}
	
	/**
	 * Кадр сцены = упорядоченный список проходов, каждый из которых сам декларирует свои
	 * требования к состоянию GPU и обязан вернуть его точно до начала следующего прохода
	 * (см. {@link #init()}: blend SRC_ALPHA/ONE_MINUS_SRC_ALPHA, depth-test включён, func ALWAYS):
	 * <ol>
	 *   <li>{@code opaque-world} — объекты сцены, включая {@link ChunkMeshElement}, под LESS + mask(true);
	 *       элементы с зарегистрированным секционным ящиком проходят cull-volume ({@link #chunkInView});</li>
	 *   <li>{@code gui} — z-циклы по виджетам/тексту в стейди-стейте init(), который world-pass восстанавливает.</li>
	 * </ol>
	 * При нуле чанков GL-состояние за кадр всё равно побайтово совпадает со старым поведением метода
	 * (culling не добавляет GPU-вызовов). Визуальная приёмка pass-plan'а на реальных данных чанков
	 * остаётся за тестовой фазой; здесь culling уже активен для всех элементов с hasChunkBounds.
	 */
	@Override
	public void renderScene(IScene scene) {

		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			if (!(obj instanceof ChunkMeshElement)) {
				elementRenderer.draw(obj, scene);
			}
		}

		// Проход "opaque-world": реальный depth-test только вокруг 3D-контента.
		OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.LESS);
		glDepthMask(true);
		Frustum cullVolume = scene.getViewFrustum();
		int worldDrawn = 0;
		int worldCulled = 0;
		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			if (obj instanceof ChunkMeshElement chunk) {
				if (chunkInView(chunk, cullVolume)) {
					chunk.draw(scene);
					worldDrawn++;
				} else {
					worldCulled++;
				}
			}
		}
		lastOpaqueWorldDrawn = worldDrawn;
		lastOpaqueWorldCulled = worldCulled;
		// Точный возврат стейди-стейта init() до отрисовки GUI.
		OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.ALWAYS);
		glDepthMask(true);

		// Отрисовка в z-порядке: элементы с большим индексом слоя поверх остальных,
		// при равных — в порядке дерева.
		for (IUIElement element : ZOrder.sorted(scene.getGuiElements())) {
			uiRenderer.draw(element, scene);
			textRenderer.draw(element, scene);
		}

	}

	/**
	 * Culling секционного AABB чанка: {@code registerChunkBox} хранит corner-based min-угол
	 * плюс axis-exact протяжённости SX/SY/SZ, тестируемый ящик [min, min+S] по каждой оси через
	 * {@link Frustum#cubeIntoFrustum(float, float, float, float, float, float)} — пересечённый
	 * тест: рисуем, пока хотя бы одна из восьми граней не оказалась целиком за любой плоскостью.
	 * Элемент без зарегистрированного ящика или кадр без сохранённого world→clip преобразования
	 * рисуются как раньше — culling деградирует в off (backward-compatible).
	 */
	private static boolean chunkInView(ChunkMeshElement chunk, Frustum frustum) {
		if (frustum == null || !chunk.hasChunkBounds()) {
			return true;
		}
		int minX = chunk.getChunkMinX(), minY = chunk.getChunkMinY(), minZ = chunk.getChunkMinZ();
		return frustum.cubeIntoFrustum(minX, minY, minZ,
				minX + chunk.getChunkSX(), minY + chunk.getChunkSY(), minZ + chunk.getChunkSZ());
	}


	@Override
	public void cleanup() {

        uiRenderer.release();
        textRenderer.release();
        elementRenderer.release();
        	
	}

	
}
