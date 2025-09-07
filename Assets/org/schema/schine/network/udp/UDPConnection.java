package org.schema.schine.network.udp;

public class UDPConnection {

	short sequenceNumber = Short.MIN_VALUE;

	public static boolean sequenceMoreRecent(int s1, int s2, int maxSequence) {
		return (s1 > s2) && (s1 - s2 <= maxSequence / 2) ||
				(s2 > s1) && (s2 - s1 > maxSequence / 2);
	}

	public void receive() {

	}

	public void send(byte[] data) {

	}
}
