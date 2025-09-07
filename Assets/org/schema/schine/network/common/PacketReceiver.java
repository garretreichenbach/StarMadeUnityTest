package org.schema.schine.network.common;

import java.io.IOException;
import java.util.List;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.common.commands.UnknownCommandException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PacketReceiver {

	
	
	private final List<InputPacket> queue = new ObjectArrayList<>();
	private final List<InputPacket> processing = new ObjectArrayList<>();
	
	private final NetworkProcessor receiver; 
	private final StateInterface state;
	private boolean disconnected;
	
	public PacketReceiver(NetworkProcessor receiver, StateInterface state) {
		this.receiver = receiver;
		this.state = state;
	}
	List<InputPacket> getQueue(){
		return queue;
	}
	/**
	 * called from main thread via NetworkManager
	 * 
	 * this handles all packages received on this end.
	 * 
	 * @throws IOException 
	 * @throws UnknownCommandException 
	 */
	void updateQueue() throws IOException, UnknownCommandException {
		if(disconnected) {
			for(InputPacket p : queue) {
				state.getNetworkManager().getPacketPool().freeInputPacket(p);
			}
			return;
		}
		processing.addAll(queue);
		queue.clear();
		
	}
	void process()  throws IOException, UnknownCommandException {
		for(InputPacket p : processing) {
			final int commandByte = p.payload.readByte() & 0xFF; //convert to unsinged byte
			Command com = Command.get(commandByte);
//			System.err.println("RECEIVED COMMAND "+com);
			if(com == null) continue;
			assert(com != null):commandByte;
			com.receiverProcess(receiver, p.payload, state);
			state.getNetworkManager().getPacketPool().freeInputPacket(p);
		}
		processing.clear();
	}
	public void received(InputPacket p) {
		state.getNetworkManager().notifyReceived(p, this);
	}

	public void onDisconnect() {
		this.disconnected = true;
	}
	public boolean isDisconnected() {
		return disconnected;
	}
	public void disconnect() {
		receiver.disconnect();
	}
	

}
