package org.schema.game.client.controller;

public interface ThreadCallback {
	/**
	 * between 0 and 1
	 *
	 * @return percent
	 */
	public float getPercent();

	public boolean isFinished();

	public void onFinished();
}
