package org.schema.game.client.view.gui;

import org.schema.schine.graphicsengine.core.Timer;

public interface GUIPopupInterface {
	/**
	 * @param index the index to set
	 */
	public void setIndex(float index);

	public void draw();

	public void update(Timer timer);

	public boolean isAlive();

	public String getMessage();

	public void setMessage(String message);

	public void restartPopupMessage();

	public void timeOut();

	public void setFlashing(boolean b);

	public void startPopupMessage(float timeDelayInSecs);

	public Object getId();

	public float getHeight();

	public void setCurrentHeight(int height);
}
