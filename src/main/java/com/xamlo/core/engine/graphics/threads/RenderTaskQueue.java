package com.xamlo.core.engine.graphics.threads;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Очередь задач, выполняемых на потоке рендера (он владеет GLFW-окном и
 * GL-контекстом). Другие потоки (например, диспетчерский) кладут сюда задачи,
 * которые обязаны выполниться в контексте рендер-потока — например, обращения
 * к GLFW-клипборду, который не тред-сейф'ов.
 *
 * Thread-safety: submit() безопасен из любого потока, drain() — только на
 * рендер-потоке.
 */
public final class RenderTaskQueue {

    public interface Task {
        void run();
    }

    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();

    public void submit(Task task) {
        tasks.add(task);
    }

    /** Выполняет все накопленные задачи в порядке очереди. Вызывать на рендер-потоке. */
    public void drain() {
        Task task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }

}
