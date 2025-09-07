package org.schema.schine.network.common;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.commands.GameRequestAnswerCommandPackage;
import org.schema.schine.network.commands.gamerequests.GameAnswerInterface;
import org.schema.schine.network.commands.gamerequests.GameRequestAnswerFactory;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;
import org.schema.schine.network.common.commands.UnknownCommandException;
import org.schema.schine.network.server.ServerProcessorInterface;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class NetworkManager implements Updatable{
	
	private final NetworkSettings settings;
	private final PacketPool pool;
	
	private Set<PacketReceiver> changed = new ObjectOpenHashSet<>(); 
	private List<PacketReceiver> processinig = new ObjectArrayList<>();
	
	private Object2ObjectOpenHashMap<String, ServerProcessorInterface> ip2clientConnections = new Object2ObjectOpenHashMap<>();
	private final StateInterface state; 
	
	
	public void registerConnection(ServerProcessorInterface serverProcessor) {
		registerConnectionOnServer(serverProcessor);		
	}
	
	public NetworkManager(StateInterface state) {
		this.settings = state.getSettings();
		assert(settings != null);
		this.pool = new PacketPool(settings.getDelaySetting());
		this.state = state;
		
	}
	public boolean isOnServer() {
		return state.isOnServer();
	}
	public PacketPool getPacketPool() {
		return pool;
	}

	public void terminateAllServerConnections() {
		synchronized(ip2clientConnections) {
			for(ServerProcessorInterface p : ip2clientConnections.values()) {
				p.disconnect();
			}
			ip2clientConnections.clear();
		}
	}
	public void unregisterConnectionOnServer(ServerProcessorInterface p) {
		synchronized(ip2clientConnections) {
			ip2clientConnections.remove(p.getIp());
		}
	}
	public void registerConnectionOnServer(ServerProcessorInterface p) {
		synchronized(ip2clientConnections) {
			ip2clientConnections.put(p.getIp(), p);
		}
	}
	public NetworkSettings getNetworkSettings() {
		return settings;
	}
	protected void receivedPong(NetworkProcessor proc) throws IOException {
		proc.receivedPong();
	}

	protected void receivedPing(NetworkProcessor proc) throws IOException {
		proc.receivedPing();
	}
	@Override
	public void update() throws IOException {
		//update the queue to move from synch to asynch to have as little synch need as possible
		synchronized(this) {
			for(PacketReceiver r : changed) {
				r.updateQueue();
				processinig.add(r);
			}
			changed.clear();
		}
		
		//process the receivers
		for(PacketReceiver r : processinig) {
			r.process();
		}
		processinig.clear();
	}


	void notifyReceived(InputPacket p, PacketReceiver packetReceiver) {
		//a packet was received. The receiver is added to be updated next update
		synchronized(this) {
			packetReceiver.getQueue().add(p);
			changed.add(packetReceiver);
		}
		state.notifyUpdateNeeded();
	}
	
	/**
	 * 
	 * ++++++++++++ WARNING +++++++++++++
	 * PACKAGE IS POOLED AND WILL BE FREED AFTER THIS METHOD RETURNS
	 * 
	 * 
	 * @param recipient who received this package (one possible for client (yourself), multiple for server (one per client))
	 * @param commandPackage received package (do not store this, as it will be reused)
	 * @throws IOException 
	 */
	public void processReceivedPackage(NetworkProcessor recipient, StateInterface state, CommandPackage commandPackage) throws IOException {
		commandPackage.getType().getHandler().handleGeneric(recipient, state, commandPackage);
	}

	public abstract void sendLogout(NetworkProcessor proc) throws IOException;

	public abstract void sendPing(NetworkProcessor proc) throws IOException;

	public abstract void sendPong(NetworkProcessor proc) throws IOException;

	/**
	 * for commands that have no package attached 
	 * @param proc
	 * @param state
	 * @param commandable
	 * @throws IOException 
	 */
	public abstract void processReceivedCommandWithoutTarget(NetworkProcessor proc, StateInterface state, Commandable commandable) throws IOException;
	private Byte2ObjectOpenHashMap<GameAnswerInterface> requested = new Byte2ObjectOpenHashMap<GameAnswerInterface>();
	public void notifyPackageArrivedForWaiting(GameRequestAnswerCommandPackage pack, UpdateSynch synch) {
		if(pack.answer.getFactory().isBlocking()) {
			synchronized(requested) {
				requested.put(pack.answer.getFactory().getGameRequestid(), pack.answer);
			}
			synch.notfifyUpdateNeeded();
		}
	}
	
	public GameAnswerInterface waitForRequestAnswer(GameRequestAnswerFactory waitingFor, UpdateSynch synch) throws IOException, UnknownCommandException {
		synchronized(requested) {
			while(!requested.containsKey(waitingFor.getGameRequestid())) {
				//execute only package receiving
				synch.updateLock(this);
			}
			return requested.remove(waitingFor.getGameRequestid());
		}
			
	}

	
}
