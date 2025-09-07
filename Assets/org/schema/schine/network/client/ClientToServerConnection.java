/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>ClientToServerConnection</H2>
 * <H3>org.schema.schine.network</H3>
 * ClientToServerConnection.java
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.schema.schine.network.Recipient;
import org.schema.schine.network.common.CommandSocket;
import org.schema.schine.network.common.CommandSocketInterface;

public class ClientToServerConnection implements Recipient {

	private String host;

	private String name;

	private CommandSocketInterface connection;

	private int port;

	private ClientStateInterface state;

	private DataOutputStream output;

	private final ClientSettingsInterface settings;

	private ClientProcessor clientProcessor;

	private boolean connected;


	/**
	 * Instantiates a new client to server connection.
	 *
	 * @param state the state
	 */
	public ClientToServerConnection(ClientStateInterface state, ClientSettingsInterface settings) {
		this.state = state;
		this.settings = settings;
	}
	/**
	 * Connect.
	 *
	 * @param host the host
	 * @param port the port
	 * @return 
	 * @return 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ClientProcessor connect(String host, int port, int timeoutInSecs) throws IOException {
		this.host = host;
		this.port = port;
		Socket socket = null;
		if (connection == null) {
			InetSocketAddress address = new InetSocketAddress(host, port);
			
				
			try {
				socket = new Socket();
				socket.setSoTimeout(0/*timeoutInSecs * 1000*/); //client from java is definite timeout
				socket.setTcpNoDelay(true);
				socket.setTrafficClass(24);
				socket.setReceiveBufferSize(settings.getSocketReceiveBufferSize().getInt());
				socket.setSendBufferSize(settings.getSocketSendBufferSize().getInt());
				socket.setSoTimeout(0);
				socket.connect(address);
				
				connection = new CommandSocket(socket);
				
				
			} catch (Exception e) {
				if (connection != null && !connection.isClosed()) {
					try {
						connection.close();
					} catch (IOException ee) {
						ee.printStackTrace();
					}
				}
				throw new IOException("[CLIENT] TCP connection failed: " + host+":"+port, e);
			}
		}
		
		
		assert(connection != null);
		
		while (!connection.isConnected() || !connection.isBound() || connection.isInputShutdown() || connection.isOutputShutdown()) {
			System.err.println("[CLIENT] WAITING FOR CONNECTION! Connected: "+ connection.isConnected()+"; Bound: "+ connection.isBound()+"; inputshutdown: "+ connection.isInputShutdown()+"; outputshutdown: "+ connection.isOutputShutdown());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("[CLIENT] SOCKET CONNECTED TO SERVER");
		
		output = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream(), settings.getSocketReceiveBufferSize().getInt()));
		clientProcessor = new ClientProcessor(this, state, socket);
		Thread clientProcessorThread = new Thread(clientProcessor);
		clientProcessorThread.setDaemon(true);
		clientProcessorThread.setName("ClientProcessor["+state+"]");
		clientProcessor.setThread(clientProcessorThread);
		
		connected = true;
		socket.getOutputStream();
		socket.getInputStream();
		clientProcessorThread.start();

		return clientProcessor;
		
		
	}
	
	public ClientProcessor getClientProcessor() {
		return clientProcessor;
	}

	
	public void disconnect() {
		clientProcessor.onSocketClosedManually();
		connected = false;
		try {
			synchronized(connection) {
				if(!connection.isInputShutdown()) {
					connection.shutdownInput();
				}
				if(!connection.isOutputShutdown()) {
					connection.shutdownOutput();
				}
				if(!connection.isClosed()) {
					connection.close();
				}
			}
//			throw new IOException("[CLIENT][EXPECTED] Client Socket connection has been closed");
		} catch (IOException e) {
			
			e.printStackTrace();
		} finally {
			setDisconneted();
		}

	}

	public CommandSocketInterface getConnection() {
		return connection;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataOutputStream getOutput() {
		return output;
	}

	public void setOutput(DataOutputStream output) {
		this.output = output;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public void setDisconneted(){
		connected = false;
	}
	public boolean isConnected() {
		return connected;
	}

	public ClientSettingsInterface getSettings() {
		return settings;
	}

	public void onDisconnect() {
		state.onClientDisconnected();
	}


}
