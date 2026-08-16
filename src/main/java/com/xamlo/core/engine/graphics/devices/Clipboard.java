package com.xamlo.core.engine.graphics.devices;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.lwjgl.glfw.GLFW;

import com.xamlo.core.engine.graphics.threads.RenderTaskQueue;

/**
 * Обёртка над GLFW-клипбордом. GLFW не тред-сейф'ов, а диспетчерский поток
 * (где живут обработчики UI) не владеет окном — поэтому сами GLFW-вызовы
 * выполняются на рендер-потоке через очередь задач. Запись асинхронна,
 * чтение блокирует вызывающий поток до выполнения на рендер-потоке.
 */
public final class Clipboard {

    /** Максимум, сколько диспетчерский поток ждёт чтения клипборда. */
    private static final long READ_TIMEOUT_MS = 500L;

    private static volatile RenderTaskQueue taskQueue;

    private Clipboard() {
    }

    /** Подключает очередь задач рендер-потока. Вызывается RenderEngine при старте. */
    public static void init(RenderTaskQueue queue) {
        taskQueue = queue;
    }

    public static boolean isReady() {
        return taskQueue != null;
    }

    /** Кладёт строку в системный буфер обмена (выполняется на рендер-потоке). */
    public static void setString(String text) {
        RenderTaskQueue q = taskQueue;
        if (q == null) {
            return;
        }
        final String s = text == null ? "" : text;
        q.submit(() -> GLFW.glfwSetClipboardString(LJWGLWindow.getInstance().getWindow(), s));
    }

    /**
     * Читает системный буфер обмена. Блокирует вызывающий поток, пока задача не
     * выполнится на рендер-потоке; при сбое/таймауте возвращает пустую строку.
     */
    public static String getString() {
        RenderTaskQueue q = taskQueue;
        if (q == null) {
            return "";
        }
        CompletableFuture<String> result = new CompletableFuture<>();
        q.submit(() -> {
            String s = GLFW.glfwGetClipboardString(LJWGLWindow.getInstance().getWindow());
            result.complete(s == null ? "" : s);
        });
        try {
            return result.get(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "";
        }
    }

}
