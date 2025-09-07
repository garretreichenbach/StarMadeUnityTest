package org.schema.schine.network;

public class IdGen {
	public static final int SERVER_ID = 0;
	private static int independentIdPool;
	private static int networkId = 100;
	private static Short packetIdC = Short.MIN_VALUE + 1;
	/**
	 * The NETWOR k_ i d_ creator.
	 */
	private static int NETWORK_ID_CREATOR = 1; //ids begin at 1, because 0 is the server id

	

	public static synchronized int getFreeStateId() {
		return NETWORK_ID_CREATOR++;
	}

	public static synchronized int getIndependentId() {
		return independentIdPool++;
	}

	public synchronized static short getNewPacketId() {
		synchronized (packetIdC) {
			if (packetIdC == Short.MIN_VALUE) {
				packetIdC++;
			}
			short pid = packetIdC;
			packetIdC++;
			return pid;
		}
	}

	public synchronized static int getNextId() {
		return networkId++;
	}
}
