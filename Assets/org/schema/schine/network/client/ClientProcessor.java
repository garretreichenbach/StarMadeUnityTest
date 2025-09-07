/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ClientProcessor</H2>
 * <H3>org.schema.schine.network</H3>
 * ClientProcessor.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.network.client;

import api.DebugFile;
import api.ModPlayground;
import api.network.Packet;
import api.network.PacketReadBuffer;
import api.utils.StarRunnable;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.SerializationInterface;
import org.schema.common.util.StringTools;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.*;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.objects.NetworkObject;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The Class ClientProcessor.
 */
public class ClientProcessor implements NetworkProcessor, Runnable {

	public static final boolean DEBUG_BIG_CHUNKS = false;
	private int ping = Integer.MAX_VALUE;
	private CommandSocketInterface receive;
	private ClientStateInterface state;
	private ClientToServerConnection clientToServerConnection;
	private DataInputStream dataInputStream;
	private Pinger pinger;
	private Thread thread;
	private SendingQueueThread sendingQueueThread;

	private boolean stopTransmitting;
	private boolean disconnectAfterSend;
	private final PacketReceiver packetReceiver;
	private boolean sockedClosedManually;
	private boolean expectedToDisconnect;
	private Socket socket;
	private boolean receiverFinished;
	private ObjectArrayList<NetworkObject> lastReceived = new ObjectArrayList<>();
	//INSERTED CODE
	private final ConcurrentLinkedQueue<Packet> modPacketQueue = new ConcurrentLinkedQueue<api.network.Packet>();
	public ConcurrentLinkedQueue<api.network.Packet> getModPacketQueue() {
		return modPacketQueue;
	}
	///
	public ClientProcessor(ClientToServerConnection ctsc,
	                       ClientStateInterface state, Socket socket) throws IOException {

		this.state = state;
		this.clientToServerConnection = ctsc;
		this.socket = socket;
		receive = ctsc.getConnection();

		sendingQueueThread = new SendingQueueThread(this);
		packetReceiver = new PacketReceiver(this, state);
	}


	public void freeOutputPacket(OutputPacket b) {
		state.getNetworkManager().getPacketPool().freeOutputPacket(b);
	}

	@Override
	public void closeSocket() throws IOException {
		sendLogout();
		System.err.println("[CLIENT] CLOSING SOCKET");
		if (!clientToServerConnection.getConnection().isClosed()) {
			System.err.println("[CLIENT] CLOSING SOCKET");
			clientToServerConnection.disconnect();
		}

	}

	private void sendLogout() throws IOException {
		state.getNetworkManager().sendLogout(this);
	}


	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public boolean isConnected() {
		return clientToServerConnection.isConnected();
	}


	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
		thread.setName("ClientProcessor["+clientToServerConnection.getHost()+"]");
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {


		int size = 0;
		state.getConnectionThreadPool().execute(sendingQueueThread);


		try {
			assert(socket == receive.getSocket());
			dataInputStream = new DataInputStream(new FastBufferedInputStream(receive.getInputStream(), getNetworkSettings().getSocketReceiveBufferSize().getInt()));
//			while (isConnected() && !receive.isClosed()) {
			while (true) {
				thread.setName("client Processor: " + state);
				size = dataInputStream.readInt();

				//INSERTED CODE @337
				if(size == -2){
					//SPECIAL PACKET ID received
					short packetId = this.dataInputStream.readShort();
					//Construct packet
					final api.network.Packet packet = api.network.Packet.newPacket(packetId);
					//Fill with data
					try {
						packet.readPacketData(new PacketReadBuffer(dataInputStream));
					}catch (IOException e){
						e.printStackTrace();
						DebugFile.logError(e, null);
					}
					//Move packet to a queue to be executed on the main loop
					new StarRunnable(){
						@Override
						public void run() {
							packet.processPacketOnClient();
						}
					}.runLater(ModPlayground.inst, 0);
					continue;
				}
				///

				if(size <= 0) {
					System.err.println("ERROR CRITICAL: Size Read Was 0 "+state);
				}
				assert (size > 0) : " Empty update!";

				InputPacket packet = state.getNetworkManager().getPacketPool().getNewInputPacket();

				if (size > 128 * 1024) {
					System.err.println("[CLIENT] WARNING: received large (>128kb) NT package: " + StringTools.formatBytes(size));
				}

				//read all the data
				packet.readFully(dataInputStream, size);

				packetReceiver.received(packet);
			}

		} catch ( SocketException e) {
			//on shutdown
//			if(!wasSocketClosedManually()) {
//				if(state.getDebugMode() == ConnectionDebugMode.ALL_EXCEPTIONS) {
//					e.printStackTrace();
//				}
//			}
		}catch ( EOFException e) {
//			if(!wasSocketClosedManually()) {
//				System.err.println("[CLIENT] Connection Ended "+state+", "+this+" (last packet size: " + size + ";) ClientToServerConnection.isConnected(): "+clientToServerConnection.isConnected()+"; socket.isClosed(): "+clientToServerConnection.getConnection().isClosed());
//				// client will produce a throw of this at end when it closes the
//				// socket
//				e.printStackTrace();
//			}
//			System.err.println("[CLIENTPROCESSOR] disconnected "+state);
		} catch (IOException e) {
//			if(!wasSocketClosedManually()) {
//				// client will produce a throw of this at end when it closes the
//				// socket
//				e.printStackTrace();
//			}
//			System.err.println("[CLIENTPROCESSOR] disconnected "+state);
		} finally{
			sendingQueueThread.stop();
			synchronized(sendingQueueThread) {
				sendingQueueThread.notify();
			}
			long t = System.currentTimeMillis();
			while(!sendingQueueThread.isSenderFinished()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized(sendingQueueThread) {
					sendingQueueThread.notify();
				}
				if(System.currentTimeMillis() - t > 20000) {
					System.err.println("Waiting for sender shutdown");
				}
			}
			receiverFinished = true;
			onDisconnect();
		}
//		System.out.println("[ClientProcessor] EXIT: Input Stream closed. Terminated Client Processor");
	}


	public void onDisconnect() {
		sendingQueueThread.onDisconnect();
		packetReceiver.onDisconnect();
		if(pinger != null) {
			pinger.onDisconnect();
		}
		clientToServerConnection.setDisconneted();
		clientToServerConnection.onDisconnect();

	}



	@Override
	public void enqueuePacket(OutputPacket packet) throws IOException {
		sendingQueueThread.enqueue(packet);
	}



	@Override
	public void disconnect() {
		clientToServerConnection.disconnect();
	}


	@Override
	public OutputPacket getNewOutputPacket() {
		return state.getNetworkManager().getPacketPool().getNewOutputPacket();
	}

	@Override
	public void freeOuptutPacket(OutputPacket packet) {
		state.getNetworkManager().getPacketPool().freeOutputPacket(packet);
	}

	@Override
	public boolean isStopTransmit() {
		return stopTransmitting;
	}

	@Override
	public boolean isDisconnectAfterSent() {
		return disconnectAfterSend;
	}

	@Override
	public NetworkSettings getNetworkSettings() {
		return state.getNetworkManager().getNetworkSettings();
	}


	@Override
	public boolean isOnServer() {
		return false;
	}


	@Override
	public int getPing() {
		return ping;
	}


	@Override
	public void setPing(int l) {
		ping = l;
	}


	public void startPinger() {
		assert(pinger == null);
		pinger = new Pinger(this);
		state.getConnectionThreadPool().execute(pinger);
	}


	@Override
	public void receivedPing() throws IOException {
		if(pinger != null) {
			pinger.handlePingReceived();
		}
	}


	@Override
	public void receivedPong() throws IOException {
		if(pinger != null) {
			pinger.handlePongReceived();
		}
	}


	public boolean wasSocketClosedManually() {
		return sockedClosedManually;
	}
	public void onSocketClosedManually() {
		this.sockedClosedManually = true;
	}
	@Override
	public boolean isOtherSideExpectedToDisconnect() {
		return expectedToDisconnect;
	}

	@Override
	public void setOtherSideExpectedToDisconnect(boolean b) {
		expectedToDisconnect = b;
	}


	@Override
	public void attachDebugIfNecessary(Command command, SerializationInterface target, OutputPacket p) {
	}


	@Override
	public DataProcessor getDataProcessor() {
		throw new RuntimeException("data processor only for server");
	}


	@Override
	public void flushOut() throws IOException {
		clientToServerConnection.getOutput().flush();
	}


	@Override
	public void sendPacket(OutputPacket s) throws IOException {
		s.writeTo(clientToServerConnection.getOutput());
	}


	@Override
	public ThreadPoolExecutor getThreadPool() {
		return state.getConnectionThreadPool();
	}


	@Override
	public boolean isFullyFinishedDisconnect() {
		return receiverFinished && sendingQueueThread.isSenderFinished();
	}


	@Override
	public ObjectArrayList<NetworkObject> getLastReceived() {
		return lastReceived;
	}


}
