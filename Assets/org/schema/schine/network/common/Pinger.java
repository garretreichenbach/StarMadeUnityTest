package org.schema.schine.network.common;

import java.io.IOException;



public class Pinger implements Runnable{
	

	private static final int heartbeatTimeOut = 10000; 
	private static final int MAX_PING_RETRYS = 0;
	
	private int pingRetrys = MAX_PING_RETRYS;
	private long pingTime;
	
	private static final long TIME_BETWEEN_PINGS_MS = 3000;
	private long lastPingSend;
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	private long firstTry = 0;
	private boolean waitingForPong;
	private long heartBeatTimeStamp;
	private long heartBeatTimeStampTimeoutHelper;
	
	public static int PING_THREADS_RUNNING;
	private final NetworkProcessor p;
	
	
	
	public Pinger(NetworkProcessor p) {
		
		this.p = p;
		heartBeatTimeStamp = System.currentTimeMillis();
		heartBeatTimeStampTimeoutHelper = System.currentTimeMillis();
	}

	private void execute() throws IOException {
		long updateTime = System.currentTimeMillis();
		if (firstTry == 0) {
			firstTry = System.currentTimeMillis();
		}


		if (!waitingForPong && updateTime - lastPingSend > TIME_BETWEEN_PINGS_MS) {
			// Send hearbeat to client
			sendPing();
			if (pingRetrys < MAX_PING_RETRYS) {
				System.err.println("[SERVER] Std Ping; retries: " + pingRetrys + " to " + p + ";");
			} else {
			}
			heartBeatTimeStamp = updateTime;
			heartBeatTimeStampTimeoutHelper = heartBeatTimeStamp;
			lastPingSend = heartBeatTimeStamp;
			waitingForPong = true;

		} else {
			if (pingRetrys >= 0
					&& waitingForPong
					&& updateTime > heartBeatTimeStampTimeoutHelper
					+ heartbeatTimeOut) {
//				System.err
//						.println("[SERVERPROCESSOR][WARNING] PING timeout warning. "
//								+ p + " Retries left: " + pingRetrys + "; ");
				heartBeatTimeStampTimeoutHelper = System
						.currentTimeMillis();
				pingRetrys--;
				
			} else {
				if (pingRetrys < MAX_PING_RETRYS) {
//					System.err.println("RETRY STATUS: Retries: " + pingRetrys + "; waiting for pong " + waitingForPong + " (" + System.currentTimeMillis() + "/" + (heartBeatTimeStampTimeoutHelper
//							+ heartbeatTimeOut) + ")" + "; Processor: " + p);
				}
			}
			if (pingRetrys < 0) {
//				System.out
//						.println("[SERVERPROCESSOR][ERROR] ping timeout ("
//								+ (System
//								.currentTimeMillis() - heartBeatTimeStamp)
//								+ ") from client -> DISCONNECT "
//								+p);
				
				p.disconnect();
				
//				System.err
//						.println("[SERVER] PING TIMEOUT logged out client "
//								+ p + "; ");

			}
		}

	}
	/**
	 * @return the pingTime in mass
	 */
	public long getPingTime() {
		return pingTime;
	}

	/**
	 * @param pingTime the pingTime to set
	 */
	public void setPingTime(long pingTime) {
		this.pingTime = pingTime;
	}

	private void sendPing() throws IOException {
		p.getState().getNetworkManager().sendPing(p);
	}		
	
	public void handlePingReceived() throws IOException {
		sendPong();
	}
	public void handlePongReceived() {
		pingTime = System.currentTimeMillis() - heartBeatTimeStamp;
		waitingForPong = false;
		if (pingRetrys != MAX_PING_RETRYS) {
			System.err.println("[SERVER][WARNING] Recovered Ping for " + p + "; Retries left: " + pingRetrys + "; retries resetting");
		}
		pingRetrys = MAX_PING_RETRYS;
	}
	@Override
	public void run() {
		PING_THREADS_RUNNING++;
		try {
			while (p.isConnected()) {
				try {
					execute();
					if (!p.isConnected()) {
						return;
					}
					Thread.sleep(TIME_BETWEEN_PINGS_MS);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println("[SERVER] ping processor disconnected : " + e1.getMessage());
				}
			}
		} finally {
			PING_THREADS_RUNNING--;
		}

	}

	public void sendPong() throws IOException {
		p.getState().getNetworkManager().sendPong(p);
	}
	
	




	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
}
