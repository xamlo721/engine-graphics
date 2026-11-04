package com.xamlo.core.engine.graphics.threads;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class RenderTaskQueueTest {

    @Test
    void failingTaskDoesNotAbortFollowingTasks() {
        RenderTaskQueue queue = new RenderTaskQueue();
        AtomicInteger completed = new AtomicInteger();

        queue.submit(() -> {
            throw new IllegalStateException("boom");
        });
        queue.submit(() -> completed.incrementAndGet());

        assertDoesNotThrow(queue::drain);
        assertEquals(1, completed.get());
    }

    @Test
    void drainRunsAllTasksInSubmissionOrder() {
        RenderTaskQueue queue = new RenderTaskQueue();
        List<Integer> order = new ArrayList<>();

        queue.submit(() -> order.add(1));
        queue.submit(() -> order.add(2));
        queue.submit(() -> order.add(3));

        queue.drain();

        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void errorsAreNotSwallowed() {
        RenderTaskQueue queue = new RenderTaskQueue();
        queue.submit(() -> {
            throw new AssertionError("fatal");
        });

        assertThrows(AssertionError.class, queue::drain);
    }
}
