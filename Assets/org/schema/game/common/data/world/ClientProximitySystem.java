package org.schema.game.common.data.world;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

public class ClientProximitySystem {
	public static final int PROXIMITY_SIZE = 1;
	public static final int LEN = (PROXIMITY_SIZE * 2) + 1;
	public static final int LENxLEN = LEN * LEN;
	public static final int ALEN = (LEN * LEN * LEN);

	private Vector3i basePosition = new Vector3i();

	private byte[] systemType = new byte[ALEN];

	private PlayerState player;

	public ClientProximitySystem(PlayerState state) {
		this.player = state;
	}

	public void deserialize(DataInputStream stream) throws IOException {

		int x = stream.readInt();
		int y = stream.readInt();
		int z = stream.readInt();

		basePosition.set(x, y, z);

		for (int i = 0; i < ALEN; i++) {
			systemType[i] = stream.readByte();
			//			if(i == 13){
			//				System.err.println("DESERIALIZED CENTER OF PROXIMITY "+basePosition+": "+SectorType.values()[systemType[i]]);
			//			}
		}

		//		System.err.println("[CLIENT] CLIENT RECEIVED PROXIMITY SYSTEM");
	}

	public Vector3i getBasePosition() {
		return basePosition;
	}

	public int getFromAbsoluteSystemPos(Vector3i containingSystem) {
		Vector3i p = new Vector3i(
				(basePosition.x - containingSystem.x) + 1,
				(basePosition.y - containingSystem.y) + 1,
				(basePosition.z - containingSystem.z) + 1);

		/*
		 * one has to be added since basePosition is
		 * in this relative system in
		 * (1, 1, 1) to calculate the index
		 * 
		 * 0,0,0 would be -1 -1 -1 from base position
		 */

		//		System.err.println("AAAAB "+p+": "+containingSystem+" / "+basePosition);
		assert (p.x >= 0 && p.y >= 0 && p.z >= 0) : p + ": " + containingSystem + " / " + basePosition;

		int index = p.z * LENxLEN + p.y * LEN + p.x;

		assert (getPosFromIndex(index, new Vector3i()).equals(containingSystem)) : getPosFromIndex(index, new Vector3i()) + " / " + containingSystem;

		if (index < 0 || index >= ALEN) {
			return -1;
		}
		//		if(!player.isOnServer()){
		//			AbstractScene.infoList.add("SYSTEM "+basePosition+"; request "+containingSystem+" -> INDEX POS "+p+": "+index);
		//		}
		return index;
	}

	public Vector3i getPosFromIndex(int i, Vector3i out) {
		out.set(
				basePosition.x - PROXIMITY_SIZE,
				basePosition.y - PROXIMITY_SIZE,
				basePosition.z - PROXIMITY_SIZE);

		int relZ = i / (LENxLEN);

		int relY = (i - (relZ * (LENxLEN))) / LEN;

		int relX = i - ((relZ * (LENxLEN)) + relY * LEN);

		out.add(relX, relY, relZ);

		return out;
	}

	public byte getType(int systemIndex) {
		return systemType[systemIndex];
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeInt(basePosition.x);
		buffer.writeInt(basePosition.y);
		buffer.writeInt(basePosition.z);
		for (int i = 0; i < ALEN; i++) {
			buffer.writeByte(systemType[i]);
		}

	}

	public void updateServer() throws IOException {

		//		System.err.println("UPDATING SYSTEM SECTORPROXMITY");

		GameServerState state = (GameServerState) player.getState();

		Vector3i sec = new Vector3i(player.getCurrentSector());

		basePosition.set(sec);

		state.getUniverse().updateProximitySectorInformation(sec);

		int i = 0;

		Vector3i pos = new Vector3i(sec);

		StellarSystem own = state.getUniverse().getStellarSystemFromSecPos(pos);

		basePosition.set(own.getPos());

		for (int z = -PROXIMITY_SIZE; z <= PROXIMITY_SIZE; z++) {
			for (int y = -PROXIMITY_SIZE; y <= PROXIMITY_SIZE; y++) {
				for (int x = -PROXIMITY_SIZE; x <= PROXIMITY_SIZE; x++) {

					pos.set(basePosition.x + x, basePosition.y + y, basePosition.z + z);

					StellarSystem stellarSystem = state.getUniverse().getStellarSystemFromStellarPos(pos);

					systemType[i] = (byte) (stellarSystem.getCenterSectorType().ordinal());

					i++;
				}
			}
		}
		assert (i == ALEN) : i + "/" + ALEN;
		synchronized (player.getNetworkObject()) {
			player.getNetworkObject().proximitySystem.setChanged(true);
		}

	}
}
