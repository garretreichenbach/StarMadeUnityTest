package org.schema.game.common.controller.bpmarket;

import api.common.GameClient;
import api.common.GameServer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.NetworkPlayer;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.remote.RemoteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Manages the Blueprint Market data.
 *
 * @author TheDerpGamer
 */
public class BlueprintMarketManager {

	public static final byte PUT = 0;
	public static final byte REMOVE = 1;

	private static final File file = new File(GameServerState.DATABASE_PATH + "bp_market_data.smdat");
	private final HashSet<BlueprintMarketData> serverCache = new HashSet<>();
	private final HashSet<BlueprintMarketData> clientCache = new HashSet<>();

	public boolean changed = true;
	private final boolean onServer;

	public BlueprintMarketManager(boolean onServer) {
		this.onServer = onServer;
		if(onServer) load();
	}

	public boolean isUpdated() {
		return changed;
	}

	public void load() {
		if(!file.exists() || file.length() == 0) {
			System.out.println("No data to load for BlueprintMarketManager");
			return;
		}
		serverCache.clear();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			JSONObject json = new JSONObject(new JSONTokener(fileInputStream));
			JSONArray jsonArray = json.getJSONArray("server_data");
			for(int i = 0; i < jsonArray.length(); i++) serverCache.add(new BlueprintMarketData(jsonArray.getJSONObject(i)));
			System.out.println("Loaded " + serverCache.size() + " BlueprintMarketData from database");
		} catch(IOException exception) {
			exception.printStackTrace();
			System.out.println("[SERVER][EXCEPTION][FATAL]: Failed to load BlueprintMarketData due to IOException:\n" + exception.getMessage());
			throw new RuntimeException("[BP MARKET MANAGER] Failed to initialize BlueprintMarketManager", exception);
		}
	}

	public void save() {
		try {
			if(!file.exists()) file.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(file);
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for(BlueprintMarketData data : serverCache) jsonArray.put(data.toJson());
			jsonObject.put("server_data", jsonArray);
			outputStream.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.close();
			System.out.println("Saved " + jsonArray.length() + " BlueprintMarketData to database");
		} catch(IOException exception) {
			exception.printStackTrace();
			System.out.println("[SERVER][EXCEPTION][FATAL]: Failed to save BlueprintMarketData due to IOException:\n" + exception.getMessage());
			throw new RuntimeException("[BP MARKET MANAGER] Failed to save BlueprintMarketManager", exception);
		}
	}
	
	public BlueprintMarketData getDataByName(String name) {
		for(BlueprintMarketData data : clientCache) {
			if(data.getName().equals(name)) return data;
		}
		return null;
	}
	
	public BlueprintMarketData getDataByUUID(String uuid) {
		for(BlueprintMarketData data : clientCache) {
			if(data.getDataUUID().equals(uuid)) return data;
		}
		return null;
	}

	public Collection<BlueprintMarketData> getAdminItems() {
		ArrayList<BlueprintMarketData> adminItems = new ArrayList<>();
		for(BlueprintMarketData data : clientCache) {
			if(data.isAdmin()) adminItems.add(data);
		}
		return adminItems;
	}

	public Collection<BlueprintMarketData> getMarketItems() {
		ArrayList<BlueprintMarketData> marketItems = new ArrayList<>();
		for(BlueprintMarketData data : clientCache) {
			if(!data.isAdmin()) marketItems.add(data);
		}
		return marketItems;
	}

	public void initFromNetworkObject(NetworkPlayer networkPlayer) {
		if(onServer) return;
		for(RemoteString remoteString : networkPlayer.blueprintMarketItemModBuffer.getReceiveBuffer()) {
			JSONObject jsonObject = new JSONObject(remoteString.get());
			BlueprintMarketData data = new BlueprintMarketData(jsonObject);
			if(jsonObject.getInt("mode") == PUT && !clientCache.contains(data)) clientCache.add(data);
			else if(jsonObject.getInt("mode") == REMOVE) clientCache.remove(data);
		}
		networkPlayer.blueprintMarketItemModBuffer.clearReceiveBuffer();
	}

	public void updateToNetworkObject(NetworkPlayer networkPlayer) {
		//Todo: Keep track of and only send changed data, because otherwise we could be sending a lot of data
		if(!onServer) return;
		for(BlueprintMarketData data : serverCache) {
			JSONObject jsonObject = data.toJson();
			jsonObject.put("mode", PUT);
			networkPlayer.blueprintMarketItemModBuffer.add(new RemoteString(jsonObject.toString(), networkPlayer));
		}
	}

	public void updateFromNetworkObject(NetworkPlayer networkPlayer, int senderId) {
		changed = !networkPlayer.blueprintMarketItemModBuffer.getReceiveBuffer().isEmpty();
		if(changed) {
			if(onServer) {
				ObjectArrayList<RemoteString> toSend = new ObjectArrayList<>();
				toSend.addAll(networkPlayer.blueprintMarketItemModBuffer.getReceiveBuffer());
				for(RemoteString remoteString : toSend) {
					JSONObject jsonObject = new JSONObject(remoteString.get());
					BlueprintMarketData data = new BlueprintMarketData(jsonObject);
					if(jsonObject.getInt("mode") == PUT && !serverCache.contains(data)) serverCache.add(data);
					else if(jsonObject.getInt("mode") == REMOVE) serverCache.remove(data);
					for(PlayerState playerState : GameServer.getServerState().getPlayerStatesByName().values()) {
						if(playerState.getNetworkObject().id.getInt() != senderId) playerState.getNetworkObject().blueprintMarketItemModBuffer.add(remoteString);
					}
				}
				save();
			} else {
				for(RemoteString remoteString : networkPlayer.blueprintMarketItemModBuffer.getReceiveBuffer()) {
					JSONObject jsonObject = new JSONObject(remoteString.get());
					BlueprintMarketData data = new BlueprintMarketData(jsonObject);
					if(jsonObject.getInt("mode") == PUT && !clientCache.contains(data)) clientCache.add(data);
					else if(jsonObject.getInt("mode") == REMOVE) clientCache.remove(new BlueprintMarketData(jsonObject));
				}
			}
		}
		networkPlayer.blueprintMarketItemModBuffer.clearReceiveBuffer();
	}

	public void addData(BlueprintMarketData data) {
		if(onServer) return;
		if(clientCache.contains(data)) return;
		NetworkPlayer networkPlayer = GameClient.getClientPlayerState().getNetworkObject();
		JSONObject jsonObject = data.toJson();
		jsonObject.put("mode", PUT);
		networkPlayer.blueprintMarketItemModBuffer.add(new RemoteString(jsonObject.toString(), networkPlayer));
		clientCache.add(data);
		changed = true;
	}

	public void removeData(BlueprintMarketData data) {
		if(onServer) return;
		if(!clientCache.contains(data)) return;
		NetworkPlayer networkPlayer = GameClient.getClientPlayerState().getNetworkObject();
		JSONObject jsonObject = data.toJson();
		jsonObject.put("mode", REMOVE);
		networkPlayer.blueprintMarketItemModBuffer.add(new RemoteString(jsonObject.toString(), networkPlayer));
		clientCache.remove(data);
		changed = true;
	}
}
