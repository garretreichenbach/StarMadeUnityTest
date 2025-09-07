package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.mines.MineController;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.mines.Mine.MineDataException;
import org.schema.game.common.data.mines.ServerMineManager;

public class MineUpdateSectorData extends MineUpdate{
	public static class MineData implements SerializationInterface{
		
		public int id;
		public short hp;
		public short ammo;
		public long ownerId;
		public int factionId;
		public short[] composition;
		public Vector3f localPos;
		public boolean armed;
		
		public void setFrom(Mine mine) {
			this.id = mine.getId();
			this.hp = mine.getHp();
			this.ownerId = mine.getOwnerId();
			this.factionId = mine.getFactionId();
			this.composition = new short[Mine.COMPOSITION_COUNT];
			this.localPos = new Vector3f(mine.getWorldTransform().origin);
			this.armed = mine.isArmed();
			this.ammo = mine.getAmmo();
			mine.getComposition(this.composition);
		}
		public Mine initDeserialization(GameClientState clientState, int sectorId, Vector3i sector) {
			
			Mine m = new Mine(clientState);
			m.setId(id);
			m.setHp(hp);
			m.setOwnerId(ownerId);
			m.setFactionId(factionId);
			m.setCompositionByVal(composition);
			m.setSectorId(sectorId);
			m.setSectorPos(new Vector3i(sector));
			m.getWorldTransform().origin.set(localPos);
			m.setArmed(armed);
			m.setAmmo(ammo);
			return m;
		}
		
		@Override
		public void serialize(DataOutput b, boolean isOnServer) throws IOException {
			b.writeInt(id);
			b.writeShort(hp);
			b.writeLong(ownerId);
			b.writeInt(factionId);
			b.writeBoolean(armed);
			b.writeShort(ammo);
			Vector3fTools.serialize(localPos, b);
			for(int i = 0; i < Mine.COMPOSITION_COUNT; i++) {
				b.writeShort(composition[i]);
			}
		}

		@Override
		public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
			this.id = b.readInt();
			this.hp = b.readShort();
			this.ownerId = b.readLong();
			this.factionId = b.readInt();
			this.armed = b.readBoolean();
			this.ammo = b.readShort();
			this.localPos = Vector3fTools.deserialize(new Vector3f(), b);
			this.composition = new short[Mine.COMPOSITION_COUNT];
			for(int i = 0; i < Mine.COMPOSITION_COUNT; i++) {
				this.composition[i] = b.readShort();
			}
		}
	}
	public MineUpdateSectorData() {
	}
	private MineData[] mineData;
	private int sectorId;
	@Override
	public MineUpdateType getType() {
		return MineUpdateType.SECTOR_DATA;
	}

	@Override
	protected void serializeData(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(sectorId);
		b.writeInt(mineData.length);
		for(int i = 0; i < mineData.length; i++) {
			mineData[i].serialize(b, isOnServer);
		}
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		sectorId = b.readInt();
		mineData = new MineData[b.readInt()];
		for(int i = 0; i < mineData.length; i++) {
			mineData[i] = new MineData();
			mineData[i].deserialize(b, updateSenderStateId, isOnServer);
		}
	}

	public MineData[] getMineData() {
		return mineData;
	}

	public void setMineData(MineData[] mineData) {
		this.mineData = mineData;
	}

	public int getSectorId() {
		return sectorId;
	}

	public void setSectorId(int sectorId) {
		this.sectorId = sectorId;
	}

	@Override
	public void execute(ClientChannel clientChannel, MineController mineController) {
		for(MineData m : mineData) {
			try {
				mineController.addMineClient(m, sectorId);
			} catch (MineDataException e) {
				e.printStackTrace();
			}
		}
	}

	public void setFrom(ServerMineManager mm) {
		mineData = new MineData[mm.getMines().size()];
		int i = 0;
		for(Mine m : mm.getMines().values()) {
			MineData d = new MineData();
			d.setFrom(m);
			mineData[i] = d;
			i++;
		}
		this.sectorId = mm.getSectorId();
	}
}
