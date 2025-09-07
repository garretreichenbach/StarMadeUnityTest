package org.schema.schine.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;


public class SystemInListener implements Runnable {

	private BufferedReader in;

	private ServerState state;
	
	@Override
	public void run() {
		in = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String l = in.readLine();
				if (l != null) {
					try{
						if(state != null){
							ObjectArrayFIFOQueue<String> systemInQueue = ((ServerController) state.getController()).getSystemInQueue();
							synchronized (systemInQueue) {
								systemInQueue.enqueue(l);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[SERVER][EXCEPTION-INFO] Exception successfully cought: The system.in is not available (probably due to 'nohup'). This will not affect the game.");
		}
	}

	public ServerState getState() {
		return state;
	}

	public void setState(ServerState state) {
		this.state = state;
	}


}
