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
	 * Кадр сцены: мир (включая {@link ChunkMeshElement}) рисуется под реальным depth-test'ом
	 * LESS с записью глубины; после обоих GUI-циклов состояние возвращается ровно к тому,
	 * что оставляет init() сегодня (тест включён, func ALWAYS, mask true). Frustum строится
	 * один раз из матрицы сцены (RenderEngine.transformScene кладёт туда projection*view),
	 * и чанки без зарегистрированного AABB либо вышедшие за него пропускаются целиком.
	 */
	@Override
	public void renderScene(IScene scene) {

		OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.LESS);
		glDepthMask(true);

		Frustum frustum = null;
		Matrix4f mvp = scene == null ? null : scene.getProjectionMatrix();
		if (mvp != null) {
			frustum = Frustum.getFrustum();
			frustum.calculateFrustum(mvp);
		}

		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			if (obj instanceof ChunkMeshElement chunk) {
				if (!chunkInView(chunk, frustum)) {
					continue;
				}
				chunk.draw(scene);
			} else {
				elementRenderer.draw(obj, scene);
			}
		}

		// Отрисовка в z-порядке: элементы с большим индексом слоя поверх остальных,
		// при равных — в порядке дерева.
		for (IUIElement element : ZOrder.sorted(scene.getGuiElements())) {
			uiRenderer.draw(element, scene);
			textRenderer.draw(element, scene);
		}

		// Точно то состояние глубины, которое init() задаёт на старте кадра сегодня
		OpenGLDepth.setDepthMode(EnumOpenGLDepthMode.ALWAYS);
		glDepthMask(true);

	}

	/**
	 * Culling чанка по его AABB: {@link ChunkMeshElement#registerChunkBox(int, int, int, int)}
	 * хранит CORNER-BASED координату нижнего блока + размер, Frustum.cubeInFrustum ждёт
	 * центр+half-size — переводим перед вызовом. Без MVP или без зарегистрированного ящика
	 * каллинг не применяется и элемент рисуется как раньше. Для нечётного размера тестовый
	 * ящик ужимается максимум на блок по оси; типовые 16-блочные чанки точны.
	 */
	private static boolean chunkInView(ChunkMeshElement chunk, Frustum frustum) {
		if (frustum == null || !chunk.hasChunkBounds()) {
			return true;
		}
		int size = chunk.getChunkSize();
		return frustum.cubeInFrustum(
				chunk.getChunkMinX() + size / 2,
				chunk.getChunkMinY() + size / 2,
				chunk.getChunkMinZ() + size / 2,
				size / 2);
	}


	@Override
	public void cleanup() {

        uiRenderer.release();
        textRenderer.release();
        elementRenderer.release();
        	
	}

	
}
