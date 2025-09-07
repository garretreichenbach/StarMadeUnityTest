package org.schema.game.client.data.gamemap.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.gamemap.GameMap;
import org.schema.game.client.data.gamemap.SystemMap;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.network.objects.remote.RemoteMapEntryRequest;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class GameMapRequestManager {

	public static final int UPDATE_TIME = 60000;
	public static final int REFRESH_TIME = 60000 * 5;
	private final Map<Vector3i, GameMap> systemMap = new Object2ObjectOpenHashMap<Vector3i, GameMap>();
	private final List<GameMapRequest> requests = new ArrayList<GameMapRequest>();
	private final List<GameMapAnswer> answers = new ArrayList<GameMapAnswer>();
	private final Object2LongOpenHashMap<Vector3i> requestedMap = new Object2LongOpenHashMap<Vector3i>();
	private ClientChannel clientChannel;
	public GameMapRequestManager(ClientChannel clientChannel) {
		this.clientChannel = clientChannel;
	}

	public void check(Vector3i secPos) {
		Vector3i sysPos = VoidSystem.getPosFromSector(secPos, new Vector3i());

		if (!systemMap.containsKey(sysPos)) {
			if (!requestedMap.containsKey(sysPos) || (System.currentTimeMillis() - requestedMap.get(sysPos).longValue()) > REFRESH_TIME) {
				requestSystem(sysPos);
				requestedMap.put(sysPos, System.currentTimeMillis());
			}
		} else {
			if (!requestedMap.containsKey(sysPos) || (System.currentTimeMillis() - requestedMap.get(sysPos).longValue()) > REFRESH_TIME) {
				requestSystem(sysPos);
				requestedMap.put(sysPos, System.currentTimeMillis());
			}
		}

	}
	public void resetCache() {
		systemMap.clear();
		
	}
	/**
	 * @return the systemMap
	 */
	public Map<Vector3i, GameMap> getSystemMap() {
		return systemMap;
	}


	public void requestSystem(Vector3i pos) {
		System.err.println("[CLIENT][MAPREQUESTMANAGER] requesting system map for " + pos);
		synchronized (requests) {
			requests.add(new GameMapRequest(GameMap.TYPE_SYSTEM, pos));
		}
	}

	public void requestSystemSimple(Vector3i pos) {
		System.err.println("[CLIENT][MAPREQUESTMANAGER] requesting system simple for " + pos);
		synchronized (requests) {
			requests.add(new GameMapRequest(GameMap.TYPE_SYSTEM_SIMPLE, pos));
		}
	}


	public void update(Timer timer) {
		if (!requests.isEmpty()) {
			synchronized (requests) {
				while (!requests.isEmpty()) {
					GameMapRequest r = requests.remove(0);
					clientChannel.getNetworkObject().mapRequests.add(new RemoteMapEntryRequest(r, false));
				}
			}
		}
		if (!answers.isEmpty()) {
			synchronized (answers) {
				while (!answers.isEmpty()) {
					GameMapAnswer a = answers.remove(0);
					GameMap map = systemMap.get(a.pos);
					if (map == null) {
						map = new SystemMap();
						map.setPos(a.pos);
						systemMap.put(new Vector3i(a.pos), map);
					}
					map.update(a.data);

//					System.err.println("[CLIENT][MAPREQUEST] MAP DATA RECEIVED SIZE: "+a.data.length);
					for (int j = 0; j < a.data.length; j++) {
//						System.err.println("MAP DATA RECEIVED "+a.data[j]);
					}

					a.data = null;
				}
			}
		}
//		if(clientChannel.getState() instanceof ClientState &&
//				((GameClientState)clientChannel.getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()){
//			long t = System.currentTimeMillis();
//			if(t - lastUpdate > UPDATE_TIME){
//				for(Entry<Vector3i, Long> e : requestedMap.entrySet()){
//					if(t - e.getValue() > REFRESH_TIME){
//						requestSystem(new Vector3i(e.getKey()));
//						requestedMap.put(e.getKey(), t);
//					}
//				}
//				lastUpdate = t;
//			}
//		}

	}

	public void updateFromNetworkObject(
			NetworkClientChannel n) {
		for (int i = 0; i < n.mapAnswers.getReceiveBuffer().size(); i++) {
			GameMapAnswer a = n.mapAnswers.getReceiveBuffer().get(i).get();

			/*
			 * taking the GameMapAnswer instance is ok
			 * instance is not pooled.
			 * RemoteMapEntryAnswerBuffer makes new instances
			 * in fromByteStream
			 */
			synchronized (answers) {
				answers.add(a);
			}

		}
	}

	

}
