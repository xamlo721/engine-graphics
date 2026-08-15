package com.xamlo.core.engine.graphics;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.xamlo.core.engine.graphics.api.components.ICamera;
import com.xamlo.core.engine.graphics.api.components.IRenderEngine;
import com.xamlo.core.engine.graphics.api.components.IScene;
import com.xamlo.core.engine.graphics.api.components.ISceneController;
import com.xamlo.core.engine.graphics.api.components.ISceneRenderer;
import com.xamlo.core.engine.graphics.devices.AbstractKeyboard;
import com.xamlo.core.engine.graphics.devices.AbstractMouse;
import com.xamlo.core.engine.graphics.devices.AbstractWindow;
import com.xamlo.core.engine.graphics.devices.LJWGLKeyboard;
import com.xamlo.core.engine.graphics.devices.LJWGLMouse;
import com.xamlo.core.engine.graphics.devices.LJWGLWindow;
import com.xamlo.core.engine.graphics.opengl.cull.EnumOpenGLCullMode;
import com.xamlo.core.engine.graphics.opengl.cull.EnumOpenGLCullOrder;
import com.xamlo.core.engine.graphics.opengl.cull.OpenGlCull;
import com.xamlo.engine.api.devices.EnumKeyboardButtons;
import com.xamlo.engine.api.devices.EnumMouseButtons;
import com.xamlo.engine.device.events.KeyboardClickEvent;
import com.xamlo.engine.device.events.MouseClickEvent;
import com.xamlo.engine.device.events.MouseHoverEvent;

import net.lenni0451.asmevents.EventManager;

public class RenderEngine implements IRenderEngine {
	
	@SuppressWarnings("unused")
	private GLFWErrorCallback errorCallback;
	
	private IScene scene;
	private ISceneRenderer renderer;
	private ISceneController sceneController;
	
	private boolean isRendering;
	private boolean isCloseRequest;

	private ICamera camera;
    private Matrix4f projectionMatrix;
    
	private AbstractWindow window;
    private AbstractKeyboard keyboard;
    private AbstractMouse mouse;
	
	public RenderEngine(ISceneRenderer renderer) {
		this.isCloseRequest = false;
		this.renderer = renderer;
	}
	
	public void setCamera(ICamera cam) {
		this.camera = cam;
	}
	
	public void setScene(IScene scene, ISceneController controller) {
		this.scene = scene;
		this.sceneController = controller;

		sceneController.setCamera(camera);
		sceneController.setScene(scene);
	}
	
	@Override
	public boolean isRendering() {
		return this.isRendering && !this.window.isCloseRequested();
	}

	@Override
	public void init() {
		
		window = LJWGLWindow.getInstance();
        //camera = Camera.getInstance();

		if(glfwInit() == false) {
			//Исключение, если мы не можем инициализироваться
		}

		//Может вызываться перед инициализацией
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        //camera.setAspectRatio(480, 480);
        camera.setFov((float) Math.toRadians(60.0f));
        //camera.setPosition(new Vec3f(0.f, 0f, 0f));

        projectionMatrix = new Matrix4f().perspective(
       		    camera.getFov(), 
           		camera.getAspectRatio(),
           	    camera.getNearDistance(), 
           	    camera.getFarDistance()
        );
        
        projectionMatrix = projectionMatrix.mul(camera.getViewMatrix());

	}
	
	@Override
	public void createWindow(int width, int height) {

		window.create(width, height);
		window.setWindowTitle("Game window");
		
//		ByteBuffer bufferedImage = ImageLoader.loadImageToByteBuffer("./res/logo/logo_lwjgl_icon32.png");
//		GLFWImage image = GLFWImage.malloc();
//		image.set(32, 32, bufferedImage);
//		window.setWindowIcon(image);
				
		OpenGlCull.enable();
		OpenGlCull.setCullOrder(EnumOpenGLCullOrder.CCW);
		OpenGlCull.setCullMode(EnumOpenGLCullMode.BACK);
		
		//Настройка OpenGL для того, чтобы она могла работать с текстурами
		//Но, если верить туториалам эта настройка не обязательна, если мы рендерим с помощью шейдеров glsl
		glEnable(GL_TEXTURE_2D);
		
		//TODO: Вынести гамма коррекцию в отдельный раздел
		//Гамма корректция должна использоваться только для некоторых отдельных объектов или сцен
		//glEnable(GL_FRAMEBUFFER_SRGB);
		
		getDeviceProperties();
	}
	
	@Override
	public void loadInputDevices() {
		keyboard = new LJWGLKeyboard();
		mouse = new LJWGLMouse();

		sceneController.setMouse(mouse);
		sceneController.setKeyboard(keyboard);
	}

	@Override
	public void stop() {
		
		if(!isRendering) {
			return;
		}
		
		isRendering = false;


		EventManager.unregister(KeyboardClickEvent.class, sceneController);
		EventManager.unregister(MouseClickEvent.class, sceneController);
		EventManager.unregister(MouseHoverEvent.class, sceneController);
	}
	@Override
	public void loadScene() {
	    this.scene.load();	
	}

	@Override
	public void transformScene() {
		
		
        projectionMatrix = new Matrix4f().perspective(
	            camera.getFov(), 
	            camera.getAspectRatio(),
	            camera.getNearDistance(), 
	            camera.getFarDistance()
	    );
        
	    scene.setProjectionMatrix(projectionMatrix.mul(camera.getViewMatrix()));
		
	}

	@Override
	public void updateInputDevices() {

		for (EnumKeyboardButtons holdButton : keyboard.getKeysHolding()) {
			EventManager.call(new KeyboardClickEvent(holdButton));
		}
		
		if (mouse.getCursorPositionDiff().x != 0 &&  mouse.getCursorPositionDiff().y != 0) {
			float dy = mouse.getCursorPositionDiff().x;
			float dx = mouse.getCursorPositionDiff().y;
			EventManager.call(new MouseHoverEvent(mouse.getCursorPosition().x, mouse.getCursorPosition().y, dx, dy));
		}

		for (EnumMouseButtons holdButton : mouse.getButtonsHolding()) {
			EventManager.call(new MouseClickEvent(mouse.getCursorPosition().x, mouse.getCursorPosition().y, holdButton));
		}

		window.update();
		keyboard.update();
		mouse.update();
	}
	
	
	@Override
	public void renderFrame() {	
        // Set the clear color
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

        //Я думаю это не нужно в каждом цикле
        glViewport(0, 0, window.getWidth(), window.getHeight());


        
		// Вся логическая сцена
        renderer.renderScene(scene);
		

		//camera.setFov(camera.getFov() + 0.01f);
		//camera.move(new Vector3f(0.0f, 0.01f, 0.01f));
		//camera.rotate(new Vector3f(0.0f, 0.1f, 0.3f));

		// draw into OpenGL window
		this.window.swapBuffers();

	}

	@Override
	public void start() {
		if(isRendering) {
			return;
		}

		EventManager.register(KeyboardClickEvent.class, sceneController);
		EventManager.register(MouseClickEvent.class, sceneController);
		EventManager.register(MouseHoverEvent.class, sceneController);
	    this.isRendering = true;

	    this.renderer.init();
	}


	@Override
	public void release() {
		
		scene.unload();
		
		window.close();
		
		glfwTerminate();
		
		this.isCloseRequest = true;		
		
	}


	private void getDeviceProperties() {
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION) + " bytes");
		System.out.println("Max Geometry Uniform Blocks: " + GL31.GL_MAX_GEOMETRY_UNIFORM_BLOCKS+ " bytes");
		System.out.println("Max Geometry Shader Invocations: " + GL40.GL_MAX_GEOMETRY_SHADER_INVOCATIONS + " bytes");
		System.out.println("Max Uniform Buffer Bindings: " + GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS + " bytes");
		System.out.println("Max Uniform Block Size: " + GL31.GL_MAX_UNIFORM_BLOCK_SIZE + " bytes");
		System.out.println("Max SSBO Block Size: " + GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE + " bytes");

		int[] work_grp_cnt = new int[3];

		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, work_grp_cnt);
		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, work_grp_cnt);
		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, work_grp_cnt);

		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_COUNT 0 " + work_grp_cnt[0]);
		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_COUNT 1 " + work_grp_cnt[1]);
		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_COUNT 2 " + work_grp_cnt[2]);

		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, work_grp_cnt);
		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 1, work_grp_cnt);
		GL31.glGetIntegeri_v(GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 2, work_grp_cnt);
		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE 0 " + work_grp_cnt[0]);
		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE 1 " + work_grp_cnt[1]);
		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE 2 " + work_grp_cnt[2]);


		System.out.println("GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS " + GL11.glGetInteger(GL43.GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS));
	}
}