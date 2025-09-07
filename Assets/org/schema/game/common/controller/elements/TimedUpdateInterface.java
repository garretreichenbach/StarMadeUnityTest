package org.schema.game.common.controller.elements;

import java.io.IOException;

import org.schema.schine.graphicsengine.core.Timer;

public interface TimedUpdateInterface {
	public long getLastExecution();

	public long getNextExecution();

	public long getTimeStep();

	public void update(Timer timer) throws IOException, InterruptedException;
}
