package com.xamlo.core.engine.graphics.devices;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.xamlo.core.engine.graphics.api.devices.IInputFrameProvider;
import com.xamlo.core.engine.graphics.api.devices.InputFrame;

/**
 * Очередь снимков ввода между потоком LWJGL и диспетчерским потоком.
 * publish() вызывает поток рендера после каждого кадра (снимок строится из
 * живых устройств до их update()), takeNext() — диспетчерский поток. При
 * переполнении глубины отбрасывается самый старый кадр: непрерывные состояния
 * (holding) в свежем кадре всё равно актуальны, потерять можно только edge'и
 * за время полной остановки потребителя, что является аномалией.
 */
public final class VolatileInputFrameHolder implements IInputFrameProvider {

	private static final int MAX_QUEUED_FRAMES = 64;

	private final ConcurrentLinkedQueue<InputFrame> frames = new ConcurrentLinkedQueue<>();

	public void publish(InputFrame frame) {
		frames.offer(frame);
		while (frames.size() > MAX_QUEUED_FRAMES) {
			frames.poll();
		}
	}

	@Override
	public InputFrame takeNext() {
		return frames.poll();
	}
}
