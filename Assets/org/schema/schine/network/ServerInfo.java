package org.schema.schine.network;

import api.mod.*;
import org.schema.common.SerializationInterface;
import org.schema.common.util.Version;
import org.schema.game.server.data.GameServerState;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This is a container for
 * server info retrieved from
 * a StarMade server
 *
 * @author Schema
 */
public class ServerInfo extends AbstractServerInfo implements SerializationInterface{

	private static final byte INFO_VERSION = 2;

	public static long curtime;
	
	private byte infoVersion;
	private Version version;
	private String name;
	private String desc;
	private long startTime;
	private int playerCount;
	private int maxPlayers;
	long ping;
	String host;
	int port;
	String connType;
	public String ip;
	public boolean reachable;
	private boolean modded = false;

	public ServerInfo(String host, int port, Object[] returnValues, long var4, String connectionType) {
		//INSERTED CODE @32
		//Skip all of the info about players and version by starting at 7
		System.err.println("[Starloader][ServerInfo] Registering info for: " + host);
		try {
			for (int i = 7; i < returnValues.length; i++) {
				modded = true;
				ModIdentifier modid = ModIdentifier.deserialize((String) returnValues[i]);
				ServerModInfo.registerModInfo(ServerModInfo.getServerUID(host, port), modid);

			}
		}catch (Exception e){
			System.err.println("Could not read server info: ");
			e.printStackTrace();
		}
		//ServerModInfo.dumpModInfos();
		///
		this.host = host;
		this.port = port;
		this.infoVersion = (Byte)returnValues[0];
		this.version = (Version) returnValues[1];
		this.name = (String)returnValues[2];
		this.desc = (String)returnValues[3];
		this.startTime = (Long)returnValues[4];
		this.playerCount = (Integer)returnValues[5];
		this.maxPlayers = (Integer)returnValues[6];
		this.ping = var4;
		this.connType = connectionType;
	}

	public final ArrayList<ModInfo> serverMods =  new ArrayList<ModInfo>();

	public ServerInfo() {
	}
	public ServerInfo(String host, int port) {

		this.host = host;
		this.port = port;

		this.version = new Version();
		this.name = "n/a";
		this.desc = "n/a";
		this.startTime = 0l;
		this.playerCount = 0;
		this.maxPlayers = 0;
		this.ping = 99999;
		this.connType = "n/a";
		this.reachable = false;


	}
	public void registerMods() {
		System.err.println("[Starloader][ServerInfo] Registering info for: " + host);
		for (int i = 7; i < serverMods.size(); i++) {
			//ServerModInfo.registerModInfo(ServerModInfo.getServerUID(host, port), serverMods.get(i)); ToDo: Fix this
		}
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte(this.infoVersion);
		this.version.serialize(b, isOnServer);
		b.writeUTF(this.name);
		b.writeUTF(this.desc);
		b.writeLong(this.startTime);
		b.writeInt(this.playerCount);
		b.writeInt(this.maxPlayers);

		b.writeInt(serverMods.size());
		for(int i = 0; i < serverMods.size(); i++) {
			serverMods.get(i).serialize(b, isOnServer);
		}
	}
	public void populate(GameServerState state) {
		this.infoVersion = INFO_VERSION;
		this.version = state.getVersion();
		this.name = state.getServerName();
		this.desc = state.getServerDesc();
		this.startTime = state.getServerStartTime();
		this.playerCount = state.getClients().size();
		this.maxPlayers = state.getMaxClients();
		serverMods.clear();
		for (ModSkeleton mod : StarLoader.starMods) {
			//serverMods.add(mod.info); TODO
		}

	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {

		this.infoVersion = b.readByte();
		this.version = Version.deserializeStatic(b, updateSenderStateId, isOnServer);
		this.name = b.readUTF();
		this.desc = b.readUTF();
		this.startTime = b.readLong();
		this.playerCount = b.readInt();
		this.maxPlayers = b.readInt();


		int modCount = b.readInt();
		for(int i = 0; i < modCount; i++) {
			ModInfo m = new ModInfo(b, updateSenderStateId, isOnServer);
		}
		registerMods();
		this.reachable = true;
	}

	/**
	 * @return the infoVersion
	 */
	@Override
	public byte getInfoVersion() {
		return infoVersion;
	}

	/**
	 * @return the version
	 */
	@Override
	public Version getVersion() {
		return version;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the desc
	 */
	@Override
	public String getDesc() {
		return desc;
	}

	/**
	 * @return the startTime
	 */
	@Override
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the playerCount
	 */
	@Override
	public int getPlayerCount() {
		return playerCount;
	}

	/**
	 * @return the maxPlayers
	 */
	@Override
	public int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * @return the ping
	 */
	@Override
	public long getPing() {
		return ping;
	}

	/**
	 * @return the host
	 */
	@Override
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * @return the connection type
	 */
	@Override
	public String getConnType() {
		return connType;
	}


	@Override
	public boolean isResponsive() {
		return reachable;
	}


	public boolean isModded() {
		return modded;


	}
}
