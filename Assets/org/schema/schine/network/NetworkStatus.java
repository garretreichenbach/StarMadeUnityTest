package org.schema.schine.network;

public class NetworkStatus {

	private long totalBytesSent;
	private int bytesSent;
	private int bytesSentPerSecond;

	private long totalBytesReceived;
	private int bytesReceived;
	private int bytesReceivedPerSecond;

	private long lastSecondSent;
	private long lastSecondReceived;

	public void addBytesReceived(int bytesReceived) {
		totalBytesReceived += bytesReceived;
		this.bytesReceived += bytesReceived;
		if (lastSecondReceived + 1000 < System.currentTimeMillis()) {
			bytesReceivedPerSecond = this.bytesReceived;
			this.bytesReceived = 0;
			lastSecondReceived = System.currentTimeMillis();
			//			System.err.println("updated bytes received "+bytesReceivedPerSecond+" -- "+bytesReceived);
		}

	}

	public void addBytesSent(int bytesSent) {

		totalBytesSent += bytesSent;
		this.bytesSent += bytesSent;
		if (lastSecondSent + 1000 < System.currentTimeMillis()) {

			bytesSentPerSecond = this.bytesSent;
			this.bytesSent = 0;
			lastSecondSent = System.currentTimeMillis();
			//			System.err.println("updated bytes sent "+bytesSentPerSecond+" -- "+bytesSent);
		}

	}

	/**
	 * @return the bytesReceivedPerSecond
	 */
	public int getBytesReceivedPerSecond() {
		addBytesReceived(0);
		return bytesReceivedPerSecond;
	}

	/**
	 * @return the bytesSentPerSecond
	 */
	public int getBytesSentPerSecond() {
		addBytesSent(0);
		return bytesSentPerSecond;
	}

	/**
	 * @return the totalBytesReceived
	 */
	public long getTotalBytesReceived() {
		return totalBytesReceived;
	}

	/**
	 * @return the totalBytesSent
	 */
	public long getTotalBytesSent() {
		return totalBytesSent;
	}

}
