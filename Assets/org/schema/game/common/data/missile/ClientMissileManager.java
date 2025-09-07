package org.schema.game.common.data.missile;

import java.util.ArrayList;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.updates.MissileTargetPositionUpdate;
import org.schema.game.common.data.missile.updates.MissileUpdate;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

public class ClientMissileManager implements MissileManagerInterface{

	private GameClientState state;

	private final Short2ObjectOpenHashMap<Missile> missiles = new Short2ObjectOpenHashMap<Missile>();
	private final ShortOpenHashSet requestedMissiles = new ShortOpenHashSet();

	private ArrayList<MissileUpdate> receivedUpdates = new ArrayList<MissileUpdate>();

	public MissileTargetManager targetManager = new MissileTargetManager();

	public ShortSet stalling = new ShortOpenHashSet();

	private long lastStallingMsg;
	public ClientMissileManager(GameClientState state) {
		this.state = state;
	}

	public void addUpdate(MissileUpdate u) {
		receivedUpdates.add(u);
	}

	public void fromNetwork(NetworkClientChannel c) {
		for (int i = 0; i < c.missileUpdateBuffer.getReceiveBuffer().size(); i++) {
			MissileUpdate missileUpdate = c.missileUpdateBuffer.getReceiveBuffer().get(i).get();
			//			System.err.println("[CLIENT] Received missile update: "+missileUpdate);
			receivedUpdates.add(missileUpdate);
		}
	}

	private void handleUpdates(ClientChannel channel) {
		for (MissileUpdate u : receivedUpdates) {
			u.handleClientUpdate(state, missiles, channel);
		}
		receivedUpdates.clear();
	}

	public void onMissingMissile(short id, ClientChannel channel) {
//		if (!requestedMissiles.contains(id)) {
//			requestedMissiles.add(id);
//			channel.getNetworkObject().missileMissingRequestBuffer.add(new RemoteShort(id, false));
//		}
	}

	public void updateClient(Timer timer, ClientChannel channel) {
		handleUpdates(channel);

		for (Missile m : missiles.values()) {
			if (requestedMissiles.size() > 0) {
				requestedMissiles.remove(m.getId());
			}
			m.addLifetime(timer);
			m.updateClient(timer);
			m.calcWorldTransformRelative(state.getCurrentSectorId(), state.getPlayer().getCurrentSector());

		}
		if(stalling.size() > 0 && System.currentTimeMillis() - lastStallingMsg > 5000){
			System.err.println("[CLIENT] Stalling missiles in the last 5 sec: "+stalling.size());
			stalling.clear();
			lastStallingMsg = System.currentTimeMillis();
		}
	}

	public Missile getMissile(short x) {
		return missiles.get(x);
	}

	public void receivedPosUpdate(MissileTargetPositionUpdate m) {
		targetManager.receivedPosUpdate(m);
	}

	public Short2ObjectOpenHashMap<Missile> getMissiles() {
		return missiles;
	}


}
