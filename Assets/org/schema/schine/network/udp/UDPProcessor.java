package org.schema.schine.network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.schema.schine.network.IdGen;

public class UDPProcessor {

	// The nature of UDP means that even through a firewall,
	// a user would have to have a unique address+port since UDP
	// can't really be NAT'ed.
	private Map<SocketAddress, UDPEndpoint> socketEndpoints = new ConcurrentHashMap<SocketAddress, UDPEndpoint>();
	private InetSocketAddress address;
	private HostThread thread;
	private ExecutorService writer;

	public UDPProcessor(InetAddress host, int port) {
		this(new InetSocketAddress(host, port));
	}

	public UDPProcessor(InetSocketAddress address) {
		this.address = address;
	}

	public UDPProcessor(int port) throws IOException {
		this(new InetSocketAddress(port));
	}

	public static void main(String[] a) {
		new Thread(() -> {
			try {
				UDPProcessor p = new UDPProcessor(4242);
				p.initialize();
				while (true) {
					System.err.println("Broadcasting");
					p.broadcast(new byte[]{10}, 0, 1);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			try {
				UDPProcessor p = new UDPProcessor(4244);
				p.initialize();
				UDPEndpoint endpoint = p.getEndpoint(new InetSocketAddress(4242), true);
				endpoint.send(new byte[13], 0, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Dispatches the data to all endpoints managed by the
	 * kernel.  'routing' is currently ignored.
	 */
	public void broadcast(byte[] data, int offset, int length) {

		//        if( copy ) {
		//            // Copy the data just once
		//            byte[] temp = new byte[data.remaining()];
		//            System.arraycopy(data.array(), data.position(), temp, 0, data.remaining());
		//            data = ByteBuffer.wrap(temp);
		//        }

		// Hand it to all of the endpoints that match our routing
		for (UDPEndpoint p : socketEndpoints.values()) {
			// Send the data
			System.err.println("SEDING TO ENDPOINT");
			p.send(data, offset, length);
		}
	}

	/**
	 * Called by the endpoints when they need to be closed.
	 */
	protected void closeEndpoint(UDPEndpoint p) throws IOException {
		// Just book-keeping to do here.
		if (socketEndpoints.remove(p.getRemoteAddress()) == null) {
			return;
		}

		//        log.log( Level.INFO, "Closing endpoint:{0}.", p );
		//        log.log( Level.FINE, "Socket endpoints size:{0}", socketEndpoints.size() );

		//        addEvent( EndpointEvent.createRemove( this, p ) );

		// If there are no pending messages then add one so that the
		// kernel-user knows to wake up if it is only listening for
		// envelopes.
		//        if( !hasEnvelopes() ) {
		//            // Note: this is not really a race condition.  At worst, our
		//            // event has already been handled by now and it does no harm
		//            // to check again.
		//            addEnvelope( EVENTS_PENDING );
		//        }
	}

	protected HostThread createHostThread() {
		return new HostThread();
	}

	public void enqueueWrite(UDPEndpoint endpoint, DatagramPacket packet) throws IOException {
		writer.execute(new MessageWriter(endpoint, packet));

	}

	protected UDPEndpoint getEndpoint(SocketAddress address, boolean create) {
		UDPEndpoint p = socketEndpoints.get(address);
		if (p == null && create) {
			p = new UDPEndpoint(this, IdGen.getIndependentId(), address, thread.getSocket());
			socketEndpoints.put(address, p);

			// Add an event for it.
			//            addEvent( EndpointEvent.createAdd( this, p ) );
		}
		return p;
	}

	public void initialize() {
		if (thread != null)
			throw new IllegalStateException("Kernel already initialized.");

		writer = Executors.newFixedThreadPool(2, new NamedThreadFactory(toString() + "-writer"));

		thread = createHostThread();

		try {
			thread.connect();
			thread.start();
		} catch (IOException e) {
			throw new UDPException("Error hosting:" + address, e);
		}
	}

	protected void newData(DatagramPacket packet) {
		// So the tricky part here is figuring out the endpoint and
		// whether it's new or not.  In these UDP schemes, firewalls have
		// to be ported back to a specific machine so we will consider
		// the address + port (ie: SocketAddress) the defacto unique
		// ID.
		UDPEndpoint p = getEndpoint(packet.getSocketAddress(), true);

		System.err.println("RECEIVED " + Arrays.toString(packet.getData()));
		// We'll copy the data to trim it.
		//        byte[] data = new byte[packet.getLength()];
		//        System.arraycopy(packet.getData(), 0, data, 0, data.length);

		//        Envelope env = new Envelope( p, data, false );
		//        addEnvelope( env );
	}

	public void terminate() throws InterruptedException {
		if (thread == null)
			throw new IllegalStateException("Kernel not initialized.");

		try {
			thread.close();
			writer.shutdown();
			thread = null;
		} catch (IOException e) {
			throw new UDPException("Error closing host connection:" + address, e);
		}
	}

	protected class HostThread extends Thread {
		private DatagramSocket socket;
		private boolean running = true;

		private byte[] buffer = new byte[65535]; // slightly bigger than needed.

		public HostThread() {
			setName("UDP Host@" + address);
			setDaemon(true);
		}

		public void close() throws IOException, InterruptedException {
			// Set the thread to stop
			running = false;

			// Make sure the channel is closed
			socket.close();

			// And wait for it
			join();
		}

		public void connect() throws IOException {
			socket = new DatagramSocket(address);
			//            log.log( Level.INFO, "Hosting UDP connection:{0}.", address );
		}

		protected DatagramSocket getSocket() {
			return socket;
		}

		private void reportError(IOException e) {

		}

		@Override
		public void run() {
			//            log.log( Level.INFO, "Kernel started for connection:{0}.", address );

			// An atomic is safest and costs almost nothing
			while (running) {
				try {
					// Could reuse the packet but I don't see the
					// point and it may lead to subtle bugs if not properly
					// reset.
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);

					newData(packet);
				} catch (IOException e) {
					if (!running) {
						return;
					}

					reportError(e);
				}
			}
		}
	}

	protected class MessageWriter implements Runnable {
		private UDPEndpoint endpoint;
		private DatagramPacket packet;

		public MessageWriter(UDPEndpoint endpoint, DatagramPacket packet) {
			this.endpoint = endpoint;
			this.packet = packet;
		}

		@Override
		public void run() {
			// Not guaranteed to always work but an extra datagram
			// to a dead connection isn't so big of a deal.
			if (!endpoint.isConnected()) {
				return;
			}

			try {
				thread.getSocket().send(packet);
			} catch (Exception e) {
				UDPException exc = new UDPException("Error sending datagram to:" + address, e);
				exc.fillInStackTrace();
				e.printStackTrace();
				//                reportError(exc);
			}
		}
	}
}
