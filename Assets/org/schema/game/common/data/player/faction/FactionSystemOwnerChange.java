package org.schema.game.common.data.player.faction;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;

public class FactionSystemOwnerChange implements SerializationInterface{
	public Vector3i targetSystem;
	public String initiator;
	public int factionId;
	public String baseUID;
	public Vector3i stationSector;
	public String realName;
	public int sender;
	public boolean forceOverwrite; //maybe used in admin commands
	public FactionSystemOwnerChange() {
		
	}
	public FactionSystemOwnerChange(String initiator, int factionId, String baseUID, Vector3i stationSector, Vector3i targetSystem, String realName) {
		super();
		this.initiator = initiator;
		this.factionId = factionId;
		this.baseUID = baseUID;
		this.stationSector = stationSector;
		this.realName = realName;
		this.targetSystem = targetSystem;
		if (targetSystem == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean onServer) throws IOException {
		this.sender = updateSenderStateId;
		this.initiator = b.readUTF();
		this.factionId = b.readInt();
		this.baseUID = b.readUTF();
		this.stationSector = Vector3i.deserializeStatic(b);
		this.realName = b.readUTF();
		this.targetSystem = Vector3i.deserializeStatic(b);
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		 b.writeUTF(this.initiator);
		 b.writeInt(this.factionId);
		 b.writeUTF(this.baseUID);
		 this.stationSector.serialize(b);
		 b.writeUTF(this.realName);
		 this.targetSystem.serialize(b);
	}

}
