package org.schema.schine.network.common;


import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.schema.common.SerializationInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.objects.NetworkObject;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public interface NetworkProcessor {
	
	public void startPinger();
	
	public void closeSocket() throws IOException;

	public void enqueuePacket(OutputPacket packet) throws IOException;


	public StateInterface getState();

	public boolean isConnected();

	public void disconnect();
	
	public OutputPacket getNewOutputPacket();
	
	public void freeOuptutPacket(OutputPacket packet);

	public boolean isStopTransmit();


	public boolean isDisconnectAfterSent();

	public NetworkSettings getNetworkSettings();

	public boolean isOnServer();
	
	public int getPing();
	
	public void setPing(int l);

	public void receivedPing() throws IOException;
	
	public void receivedPong() throws IOException;
	
	public void onSocketClosedManually();
	
	public boolean wasSocketClosedManually();
	
	public boolean isOtherSideExpectedToDisconnect();
	
	public void setOtherSideExpectedToDisconnect(boolean b);

	public void attachDebugIfNecessary(Command command, SerializationInterface target, OutputPacket p);

	public DataProcessor getDataProcessor();

	public void flushOut() throws IOException;

	public void sendPacket(OutputPacket s) throws IOException;
	
	public ThreadPoolExecutor getThreadPool();

	public boolean isFullyFinishedDisconnect();

	public ObjectArrayList<NetworkObject> getLastReceived();


}
