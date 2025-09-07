/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ServerProcessor</H2>
 * <H3>org.schema.schine.network</H3>
 * ServerProcessor.java
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
package org.schema.schine.network.server;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.SerializationInterface;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.UnregisteredClient;
import org.schema.schine.network.common.*;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.objects.NetworkObject;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;




/**
 * The communication between server and client. Each client has one instance of this located in RegisteredClientOnServer.
 * They have input and output streams to send and receive data
 *
 * @author schema
 *
 */
public class ServerProcessor implements NetworkProcessor, Runnable, ConnectionRunnable, ServerProcessorInterface {





	public static int totalPackagesQueued;




	private ServerStateInterface state;
	private RegisteredClientInterface client;
	private long connectionStartTime;
	private boolean connected = false;



	private Thread thread;
	private boolean stopTransmit;
	private SendingQueueThread sendingQueueThread;
	private boolean disconnectAfterSent;
	private PacketReceiver packetReceiver;
	private Pinger pinger;
	private int ping = Integer.MAX_VALUE;


	private boolean sockedClosedManually;


	private boolean expectedToDisconnect;




	private DataProcessor dataPipe;




	private boolean receiverFinished;




	private ObjectArrayList<NetworkObject> lastReceived = new ObjectArrayList<NetworkObject>();

	public ServerProcessor(DataProcessor dataPipe, ServerStateInterface state) throws SocketException {
		this.dataPipe = dataPipe;
		this.state = state;

		connectionStartTime = System.currentTimeMillis();

		setClient(new UnregisteredClient(state, dataPipe));
	}





	@Override
	public void closeSocket() throws IOException {
		dataPipe.close(this);
	}

	@Override
	public void enqueuePacket(OutputPacket packet) throws IOException {
		sendingQueueThread.enqueue(packet);
	}


	@Override
	public ServerStateInterface getState() {
		return state;
	}



	public void disconnectAfterSent() {
		disconnectAfterSent = true;
	}



	public RegisteredClientInterface getClient() {
		return client;
	}

	public void setClient(RegisteredClientInterface client) {
		this.client = client;
		if(thread != null) {
			thread.setName(this.toString());
		}
	}

	public void startPinger() {
		assert(pinger == null);
		if(pinger == null) {
			pinger = new Pinger(this);
			state.getConnectionThreadPool().execute(pinger);
		}
	}
	public String getClientIp() {
		return dataPipe.getRemoteIp();
	}

	/**
	 * @return time client is connected in mass
	 */
	public long getConnectionTime() {
		return System.currentTimeMillis() - connectionStartTime;
	}

	public String getIp() {
		if (dataPipe.isConnected()) {
			return dataPipe.getRemoteIp();
		} else {
			return "n/a";
		}
	}


	public int getPort() {
		return dataPipe.getLocalPort();
	}

	public String toString() {
		return "ServerProcessor["+(client != null ? client.getId() : "NO_CLIENT_OBJECT")+"]["+dataPipe.getInetAddress()+"]";
	}
	public long getId() {
		return client != null ? client.getId() : Long.MIN_VALUE;
	}

	public boolean isConnectionAlive() {
		return dataPipe != null && dataPipe.isConnected();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */

	@Override
	public void run() {
		try {
			state.getConnectionStats().PROCESSOR_THREADS_RUNNING++;
			state.getConnectionStats().CONNECTIONS++;
			while (!dataPipe.isConnected()) {
				try {
					System.err.println("[SERVER] waiting for socket to connect: " + dataPipe);
					Thread.sleep(100);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			try {

				connected = true;

				setup();

//				System.out.println("[SERVER][PROCESSOR] setup completed for Connection: " + this + ". listening for input");

				while (connected) {
					if(dataPipe.hasData()) { //FIXME This method was never supposed to be in this interface, but it would throw an EoF every time without it. ~ Ithirahad
						//get an unused input packet from the pool (or create new if necessary)
						InputPacket packet = getNewInputPacket();
						try {
							//read all the data into the InputPacket
							int size = dataPipe.readPackage(packet);

							if (size > 1024 * 1024 * 100) {
//						Log.warning("[SERVER] Exception: Unusual big update from client " + getClient() );
							}
							packetReceiver.received(packet);
							//lastRecievedData = System.currentTimeMillis();
						} catch (EOFException eof){
							//FIXME This should never be necessary, but the server was randomly EoFing when reading its data and it was preventing clients logging in.
							// with the catch in place, things seem to work... But the error should never happen in the first place.
							// ~ Ithirahad
							System.err.println("EoF on server:");
							eof.printStackTrace();
							//presumably nothing recieved at the moment
						}
					}
					//if(System.currentTimeMillis() - lastRecievedData > TIMEOUT) do disconnect stuff
				}
			} catch (IOException e) {
				e.printStackTrace();
				if(wasSocketClosedManually()) {
					//normal disconnect
				}else {

					if(!expectedToDisconnect && !disconnectAfterSent){
//						System.err.println("[SERVER] DISCONNECTED in "+getState()+"; client "+client+": "+e.getClass().getName()+"; "+e.getMessage());
//						e.printStackTrace();
					}
				}
				connected = false;

			} finally {
				connected = false;
				onDisconnect();
			}

//			Log.fine("[DISCONNECT] Client '" + client + "' IP(" + getIp() + ") HAS BEEN DISCONNECTED . ProcessorID: " + this);

//			System.err.println("[SERVER] SERVER PROCESSOR STOPPED FOR " + this);

			state.getConnectionStats().PROCESSOR_THREADS_RUNNING--;
		} finally {
			connected = false;

			try {
				cleanUp();
			} catch (IOException e) {
				e.printStackTrace();
			}
			receiverFinished = true;
		}


	}
	public InputPacket getNewInputPacket() {
		return state.getNetworkManager().getPacketPool().getNewInputPacket();
	}





	public void onDisconnect() {
		if(sendingQueueThread != null) {
			sendingQueueThread.onDisconnect();
		}
		if(packetReceiver != null) {
			packetReceiver.onDisconnect();
		}
		if(pinger != null) {
			pinger.onDisconnect();
		}
	}
	/**
	 * @return the stopTransmit
	 */
	public boolean isStopTransmit() {
		return stopTransmit;
	}

	/**
	 * @param stopTransmit the stopTransmit to set
	 */
	public void setStopTransmit(boolean stopTransmit) {
		this.stopTransmit = stopTransmit;
	}

	private void cleanUp() throws IOException {

		if (client != null) {
			state.onClientDisconnected(client);
		}else {
			System.err.println("CLIENT WAS NULL");
		}

		state.getNetworkManager().unregisterConnectionOnServer(this);

		if (dataPipe.isConnected()) {
			try {
				dataPipe.close(this);
			}catch(IOException e) {
			}
		}

		state.getConnectionStats().CONNECTIONS--;
		if (client == null) {
			return;
		}
//		System.err.println("[SERVER] Client <" + getClient().getId()+ "> logged out from server. connections count: " + getState().getConnectionStats().CONNECTIONS);
		if (dataPipe.isConnected()) {
			System.err.println("[SERVER][CLEANUP] ERROR: socket still open!");
		}
	}




	public void setThread(Thread serverThread) {
		this.thread = serverThread;
		serverThread.setName("ServerProcessor["+dataPipe.getInetAddress()+"]");
	}

	private void setup() throws IOException {
		while (!dataPipe.isConnected()) {
			System.err.println("Waiting for command socket! ");
		}

		sendingQueueThread = new SendingQueueThread(this);
		state.getConnectionThreadPool().execute(sendingQueueThread);

		packetReceiver = new PacketReceiver(this, state);


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
	public boolean isConnected() {
		return connected;
	}



	@Override
	public void disconnect() {
		onSocketClosedManually();
		connected = false;
		sendingQueueThread.onDisconnect();
		if (dataPipe.isConnected()) {
			try {
				dataPipe.close(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public NetworkSettings getNetworkSettings() {
		return state.getNetworkManager().getNetworkSettings();
	}



	@Override
	public boolean isDisconnectAfterSent() {
		return disconnectAfterSent;
	}





	@Override
	public boolean isOnServer() {
		return true;
	}

	@Override
	public int getPing() {
		return ping;
	}


	@Override
	public void setPing(int l) {
		ping = l;
	}





	@Override
	public void receivedPing() throws IOException {
		if(pinger == null) {
			//first ping from client. Start own server ping
			startPinger();
		}
		pinger.handlePingReceived();
	}





	@Override
	public void receivedPong() throws IOException {
		if(pinger != null) {
			pinger.handlePongReceived();
		}else {
//			Log.warning("[SERVER] received pong from "+this+" but no ping sent yet (pinger not started)");
		}
	}





	@Override
	public Thread getThread() {
		return thread;
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
		if(client != null) {
//			client.attachDebugIfNecessary(command, target, p);
		}
	}





	@Override
	public DataProcessor getDataProcessor() {
		return dataPipe;
	}





	@Override
	public void flushOut() throws IOException {
		dataPipe.flushOut();
	}





	@Override
	public void sendPacket(OutputPacket s) throws IOException {
		dataPipe.sendPacket(s);
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
