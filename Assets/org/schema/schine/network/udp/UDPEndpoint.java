package org.schema.schine.network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class UDPEndpoint {

	private int id;
	private SocketAddress address;
	private DatagramSocket socket;
	private boolean connected = true; // it's connectionless but we track logical state
	private UDPProcessor processor;

	public UDPEndpoint(UDPProcessor processor, int id, SocketAddress address, DatagramSocket socket) {
		this.id = id;
		this.address = address;
		this.socket = socket;
		this.processor = processor;
	}

	public void close() {
		close(false);
	}

	public void close(boolean flush) {
		// No real reason to flush UDP traffic yet... especially
		// when considering that the outbound UDP isn't even
		// queued.

		try {
			processor.closeEndpoint(this);
			connected = false;
		} catch (IOException e) {
			throw new UDPException("Error closing endpoint for socket:" + socket, e);
		}
	}

	public String getAddress() {
		return String.valueOf(address);
	}

	public long getId() {
		return id;
	}

	protected SocketAddress getRemoteAddress() {
		return address;
	}

	public boolean isConnected() {
		// The socket is always unconnected anyway so we track our
		// own logical state for the kernel's benefit.
		return connected;
	}

	public void send(byte[] data, int offset, int length) {
		if (!connected) {
			throw new UDPException("Endpoint is not connected:" + this);
		}

		try {
			DatagramPacket p = new DatagramPacket(data, offset,
					length, address);

			// Just queue it up for the kernel threads to write
			// out
			processor.enqueueWrite(this, p);

			//socket.send(p);
		} catch (IOException e) {
			throw new UDPException("Error sending datagram to:" + address, e);
		}
	}

	@Override
	public String toString() {
		return "UdpEndpoint[" + id + ", " + address + "]";
	}

}
