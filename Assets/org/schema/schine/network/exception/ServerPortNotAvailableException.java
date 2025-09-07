package org.schema.schine.network.exception;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPortNotAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private boolean instanceRunning;

	public ServerPortNotAvailableException(String string) {
		super(string);
	}

	public static void main(String[] sad) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(4242);
		
			while (true) {
				try {
					Socket accept = serverSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
		}finally {
			if(serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * @return the instanceRunning
	 */
	public boolean isInstanceRunning() {
		return instanceRunning;
	}

	/**
	 * @param instanceRunning the instanceRunning to set
	 */
	public void setInstanceRunning(boolean instanceRunning) {
		this.instanceRunning = instanceRunning;
	}

}
