package com.xamlo.core.engine.graphics.renderers;

import static org.lwjgl.opengl.GL11.glClearColor;

import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneRenderer;
import com.xamlo.core.engine.graphics.api.gui.AbstractSceneElement;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.ZOrder;
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
	
	@Override
	public void renderScene(IScene scene) {
        
		for (AbstractSceneElement obj : scene.getRenderableObject()) {
			elementRenderer.draw(obj, scene);
		}
		
		// Отрисовка в z-порядке: элементы с большим индексом слоя поверх остальных,
		// при равных — в порядке дерева.
		for (IUIElement element : ZOrder.sorted(scene.getGuiElements())) {
			uiRenderer.draw(element, scene);
			textRenderer.draw(element, scene);
		}

	}
	

	@Override
	public void cleanup() {

        uiRenderer.release();
        textRenderer.release();
        elementRenderer.release();
        	
	}

	
}
