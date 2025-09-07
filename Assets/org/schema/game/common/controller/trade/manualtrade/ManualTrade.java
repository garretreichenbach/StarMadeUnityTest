package org.schema.game.common.controller.trade.manualtrade;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.remote.RemoteManualTrade;
import org.schema.game.network.objects.remote.RemoteManualTradeItem;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;

public class ManualTrade extends GUIObservable implements SerializationInterface{

	public AbstractOwnerState[] a;
	StateInterface state;
	
	int id = -1;
	
	private boolean[] joined;
	private boolean[] accept;
	
	
	public List<ManualTradeItem> aItems[];
	private boolean canceled;
	private short itemIdGen;
	public boolean executed;
	private int[] receivedTradeInital;
	public ManualTrade(){
		
	}
	public ManualTrade(AbstractOwnerState a, AbstractOwnerState b){
		this.a = new AbstractOwnerState[]{a,b};
		this.joined = new boolean[]{true, false};
		this.accept = new boolean[]{false, false};
	}
	
	private boolean isOnServer() {
		return state instanceof ServerStateInterface;
	}
	public byte getSide(AbstractOwnerState who){
		for(int i = 0; i < a.length; i++){
			if(a[i] == who){
				return (byte)i;
			}
		}
		throw new IllegalArgumentException();
	}
	public AbstractOwnerState getPlayer(byte who){
		return a[who];
	}
	public void clientMod(PlayerState who, short type, int metaId, int amount){
		assert(!isOnServer());
		ManualTradeItem t = new ManualTradeItem(state, id,
				getSide(who), type, amount, metaId, (short)-1);
		
		who.getClientChannel().getNetworkObject().manualTradeItemBuffer.add(new RemoteManualTradeItem(t, isOnServer()));
	}
	public void clientAccept(PlayerState who, boolean accept){
		assert(!isOnServer());
		ManualTradeItem t = new ManualTradeItem(state, id, getSide(who));
		t.acc = accept;
		t.mode = Mode.ACCEPT;
		who.getClientChannel().getNetworkObject().manualTradeItemBuffer.add(new RemoteManualTradeItem(t, isOnServer()));
	}
	public void clientJoin (PlayerState who, boolean join){
		assert(!isOnServer());
		ManualTradeItem t = new ManualTradeItem(state, id, getSide(who));
		t.join = join;
		t.mode = Mode.JOIN;
		who.getClientChannel().getNetworkObject().manualTradeItemBuffer.add(new RemoteManualTradeItem(t, isOnServer()));
	}
	public void clientCancel(PlayerState who){
		assert(!isOnServer());
		ManualTradeItem t = new ManualTradeItem(state, id, getSide(who));
		t.mode = Mode.CANCEL;
		who.getClientChannel().getNetworkObject().manualTradeItemBuffer.add(new RemoteManualTradeItem(t, isOnServer()));
	}
	
	public void received(ManualTradeItem item){
		item.state = state;
		AbstractOwnerState st = getPlayer(item.side);
		switch(item.mode) {
			case ITEM -> {
				modItem(item);
				Arrays.fill(accept, false);
			}
			case ACCEPT -> accept[item.side] = item.acc;
			case JOIN -> joined[item.side] = item.join;
			case CANCEL -> this.canceled = true;
			default -> throw new IllegalArgumentException(item.mode.toString());
		}
		
		if(!isOnServer()){
			notifyObservers();
		}
	}
	
	private void modItem(ManualTradeItem mod) {
		if(isOnServer()){
			List<ManualTradeItem> l = aItems[mod.side];
			
			for(int i = 0; i < l.size(); i++){
				ManualTradeItem item = l.get(i);
				if(item.type == mod.type && item.metaId == mod.metaId){
					item.amount += mod.amount;
					
					if(item.amount <= 0){
						l.remove(i);
					}
					sendAll(item);
					return;
				}
			}
			
			if(mod.amount > 0){
				mod.itemId = itemIdGen++;
				l.add(mod);
				sendAll(mod);
			}
		}else{
			List<ManualTradeItem> l = aItems[mod.side];
			
			for(int i = 0; i < l.size(); i++){
				ManualTradeItem item = l.get(i);
				if(item.itemId == mod.itemId){
					item.amount += mod.amount;
					
					if(item.amount <= 0){
						l.remove(i);
					}
					
					return;
				}
			}
		}
	}

	private void sendAll(ManualTradeItem mod) {
		assert(isOnServer());
		for(AbstractOwnerState o : a){
			if(o instanceof PlayerState){
				PlayerState p = (PlayerState)o;
				
				ClientChannel clientChannel = p.getClientChannel();
				
				clientChannel.getNetworkObject().manualTradeItemBuffer.add(new RemoteManualTradeItem(mod, isOnServer()));
			}
		}
	}
	public void sendSelfToClients() {
		assert(isOnServer());
		for(AbstractOwnerState o : a){
			if(o instanceof PlayerState){
				PlayerState p = (PlayerState)o;
				
				ClientChannel clientChannel = p.getClientChannel();
				
				clientChannel.getNetworkObject().manualTradeBuffer.add(new RemoteManualTrade(this, isOnServer()));
			}
		}
		
	}
	public enum Mode{
		ITEM,
		JOIN,
		ACCEPT,
		CANCEL,
	}
	
	
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeShort(a.length);
		for(AbstractOwnerState s : a){
			b.writeInt(s.getId());
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		int size = b.readShort();
		this.receivedTradeInital = new int[size];
		for(int i = 0; i < receivedTradeInital.length; i++){
			receivedTradeInital[i] = b.readInt();
		}
	}

	public static String getUID(AbstractOwnerState ... a){
		String[] s = new String[a.length];
		int cap = 0;
		for(int i = 0; i < a.length; i++){
			s[i] = a[i].getName().toLowerCase(Locale.ENGLISH);
			cap+=s[i].length();
		}
		StringBuffer sb = new StringBuffer(cap);
		Arrays.sort(s);
		for(int i = 0; i < s.length; i++){
			sb.append(s[i]);
		}
		return sb.toString();
	}
	
	public String getUID(){
		return getUID(a);
	}
	public boolean isCanceled() {
		return canceled;
	}
	public boolean isReadyToExecute() {
		for(boolean n : accept){
			if(!n){
				return false;
			}
		}
		return true;
	}
	public void execute() {
		assert(isOnServer());
	}
	public void setFromPlayers() throws PlayerNotFountException {
		assert(receivedTradeInital != null);
		try{
			a = new AbstractOwnerState[receivedTradeInital.length];
			for(int i = 0; i < a.length; i++){
				System.err.println("RETRIEVING PLAYER "+receivedTradeInital[i]);
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(receivedTradeInital[i]);
				System.err.println("RETRIVED PLAYER: "+sendable);
				a[i] = (AbstractOwnerState)sendable ;
				if(a[i] == null){
					throw new PlayerNotFountException("PLAYER ID: "+receivedTradeInital[i]);
				}
			}
			
			this.joined = new boolean[]{true, false};
			this.accept = new boolean[]{false, false};
			for(AbstractOwnerState o : a){
				o.activeManualTrades.add(this);
			}
		}catch(RuntimeException e){
			e.printStackTrace();
			throw new PlayerNotFountException(e.getMessage());
		}
		
		
	}
	public void updateFromPlayerClient(PlayerState p) {
		int side = getSide(p);
		int otherSide = side == 0 ? 1 : 0;
		if(!joined[side]){
			((GameClientState)state).getController()
			.popupAlertTextMessage(Lng.str("Incoming trade request from:\n%s\nPress %s to accept, %s to cancel", a[otherSide].getName(), KeyboardMappings.PLAYER_TRADE_ACCEPT, KeyboardMappings.PLAYER_TRADE_CANCEL), 0);
		}
		
	}
	public boolean isReady(AbstractOwnerState p) {
		return accept[getSide(p)];
	}
	
	
	
}
