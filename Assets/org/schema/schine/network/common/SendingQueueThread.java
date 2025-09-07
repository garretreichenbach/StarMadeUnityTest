package org.schema.schine.network.common;

import java.io.IOException;

import org.schema.schine.network.client.ClientProcessor;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class SendingQueueThread implements ConnectionRunnable {

	private final ObjectArrayFIFOQueue<OutputPacket> sendingQueue = new ObjectArrayFIFOQueue<OutputPacket>();
	private final NetworkProcessor p;
	private int totalPackagesQueued;
	public static int SENDING_THREADS_RUNNING;
	private Thread thread;
	private boolean senderFinished;
	private boolean stopped; 
	public SendingQueueThread(NetworkProcessor p) throws IOException {
		
		this.p = p;
	}


	public void sendPacket(OutputPacket s) throws IOException {
		p.sendPacket(s);
	}
	public void freePacket(OutputPacket s) {
		p.freeOuptutPacket(s);
	}
	@Override
	public void run() {
		try {
			SENDING_THREADS_RUNNING++;
			while (!stopped && isConnected()) {
				if(isStopTransmit()) {
					Thread.sleep(50);
					continue;
				}
				
				if (sendingQueue.isEmpty() && isConnected()) {
					p.flushOut();
				}
				try {
					Thread.sleep(2L);
				} catch (InterruptedException e) {
				}
				
				OutputPacket next = null;
				synchronized (sendingQueue) {
					boolean empty = false;
					while (sendingQueue.isEmpty() && isConnected()) {

						sendingQueue.wait(5000);
						if (sendingQueue.isEmpty() && isConnected()) {
							//do a cycle without packet
							empty = true;
							break;
						}

						if (!isConnected()) {
							break;
						}
						if(isStopTransmit()) {
							continue;
						}
						if (sendingQueue.size() > getSettings().getSendingQueueSizeSetting().getInt()) {
							disconnect();
						}
					}
					if (!empty) {
						if (!isConnected()) {
							break;
						}
						if(isStopTransmit()) {
							continue;
						}
						next = sendingQueue.dequeue();

						totalPackagesQueued--;
					}
				}

				if (next != null){
					if (getSettings().getDelaySetting().getInt() > 0) {
						long t = System.currentTimeMillis();
						long tt = t - next.time;
						if (tt < t) {
							long d = (long)getSettings().getDelaySetting().getInt() - tt;
							if(d > 0){
								Thread.sleep(d);
							}
						}
					}
					sendPacket(next);
					freePacket(next);
					if (sendingQueue.isEmpty() && isDisconnectAfterSent()) {
						System.err.println("[NETWORK] "+this+" SCHEDULED DISCONNECT EXECUTING");
						p.getState().getNetworkManager().sendLogout(p);
						p.flushOut();
						p.disconnect();
					}
				}
			}
		} catch (IOException | RuntimeException | InterruptedException e) {
			if(!(p instanceof ClientProcessor)) {
				System.err.println("[NETWORK] SENDING THREAD ENDED WITH Exception: " + p+"; State: "+p.getState());
				e.printStackTrace();
			}
		} finally {
			disconnect();
			synchronized (sendingQueue) {
				while (sendingQueue.size() > 0) {
					freePacket(sendingQueue.dequeue());
					totalPackagesQueued--;
				}
				sendingQueue.clear();
			}
			SENDING_THREADS_RUNNING--;
			senderFinished = true;
		}

	}
	private void disconnect() {
		p.disconnect();
	}


	private boolean isDisconnectAfterSent() {
		return p.isDisconnectAfterSent();
	}


	public NetworkSettings getSettings() {
		return p.getNetworkSettings();
	}


	public boolean isStopTransmit() {
		return p.isStopTransmit();
	}


	public boolean isConnected() {
		return p.isConnected();
	}
	public void enqueue(OutputPacket packet) {
		assert (packet.size() > 0);
	
		synchronized (sendingQueue) {
			if (isConnected()) {
				sendingQueue.enqueue(packet);
				totalPackagesQueued++;
				sendingQueue.notify();
			}
		}
	}


	public void onDisconnect() {
		synchronized (sendingQueue) {
			sendingQueue.notify();
		}
	}


	public int getTotalPackagesQueued() {
		return totalPackagesQueued;
	}


	@Override
	public Thread getThread() {
		return thread;
	}


	@Override
	public void setThread(Thread thread) {
		this.thread = thread;		
		this.thread.setName("SendingQueueThread["+p+"]");
	}


	public boolean isSenderFinished() {
		return senderFinished;
	}


	public void stop() {
		stopped = true;
	}





}
