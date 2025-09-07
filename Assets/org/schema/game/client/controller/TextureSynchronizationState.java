package org.schema.game.client.controller;

import org.schema.game.common.data.player.PlayerState;

public class TextureSynchronizationState {

	private final PlayerState player;
	public boolean requested;
	private long clientTimeStamp = -1;
	private long serverTimeStamp = -1;
	private long clientSize = -1;
	private long serverSize = -1;
	private boolean timeStampRequested = false;
	private boolean received;

	public TextureSynchronizationState(PlayerState player) {
		this.player = player;

		clientTimeStamp = player.getSkinManager().getTimeStamp();
	}

	/**
	 * @return the clientSize
	 */
	public long getClientSize() {
		return clientSize;
	}

	/**
	 * @param clientSize the clientSize to set
	 */
	public void setClientSize(long clientSize) {
		this.clientSize = clientSize;
	}

	/**
	 * @return the clientTimeStamp
	 */
	public long getClientTimeStamp() {
		return clientTimeStamp;
	}

	/**
	 * @param clientTimeStamp the clientTimeStamp to set
	 */
	public void setClientTimeStamp(long clientTimeStamp) {
		this.clientTimeStamp = clientTimeStamp;
	}

	/**
	 * @return the player
	 */
	public PlayerState getPlayer() {
		return player;
	}

	/**
	 * @return the serverSize
	 */
	public long getServerSize() {
		return serverSize;
	}

	/**
	 * @param serverSize the serverSize to set
	 */
	public void setServerSize(long serverSize) {
		this.serverSize = serverSize;
	}

	/**
	 * @return the serverTimeStamp
	 */
	public long getServerTimeStamp() {
		return serverTimeStamp;
	}

	/**
	 * @param serverTimeStamp the serverTimeStamp to set
	 */
	public void setServerTimeStamp(long serverTimeStamp) {
		this.serverTimeStamp = serverTimeStamp;
	}

	/**
	 * @return the received
	 */
	public boolean isReceived() {
		return received;
	}

	/**
	 * @param received the received to set
	 */
	public void setReceived(boolean received) {
		this.received = received;
	}

	/**
	 * @return the timeStampRequested
	 */
	public boolean isTimeStampRequested() {
		return timeStampRequested;
	}

	/**
	 * @param timeStampRequested the timeStampRequested to set
	 */
	public void setTimeStampRequested(boolean timeStampRequested) {
		this.timeStampRequested = timeStampRequested;
	}
}
