package api.listener.events.state;

import api.listener.events.Event;

/**
 * Event for when the server shutdown counter is called.
 */
public class ServerShutdownCounterEvent extends Event {

	private int countDown;

	public ServerShutdownCounterEvent(int countDown) {
		this.countDown = countDown;
	}

	public int getCountDown() {
		return countDown;
	}

	public void setCountDown(int countDown) {
		this.countDown = countDown;
	}
}
