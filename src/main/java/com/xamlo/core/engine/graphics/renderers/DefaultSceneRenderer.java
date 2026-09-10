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
	 * Кадр сцены = упорядоченный список проходов, каждый из которых сам декларирует свои
	 * требования к состоянию GPU и обязан вернуть его точно до начала следующего прохода
	 * (см. {@link #init()}: blend SRC_ALPHA/ONE_MINUS_SRC_ALPHA, depth-test включён, func ALWAYS):
	 * <ol>
	 *   <li>{@code opaque-world} — объекты сцены, включая {@link ChunkMeshElement}, под LESS + mask(true);</li>
	 *   <li>{@code gui} — z-циклы по виджетам/тексту в стейди-стейте init(), который world-pass восстанавливает.</li>
	 * </ol>
	 * При нуле чанков GL-состояние за кадр побайтово совпадает со старым поведением метода.
	 * TODO: frustum-culling чанков по AABB добавить, когда IScene будет отдавать полный MVP
	 *       (сейчас доступен только getProjectionMatrix()); визуальная приёмка pass-plan'а на
	 *       реальных данных чанков — после появления сетевого подключения.
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
		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			if (obj instanceof ChunkMeshElement chunk) {
				chunk.draw(scene);
			}
		}
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
