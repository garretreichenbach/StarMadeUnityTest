package org.schema.game.common.data.player;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BlueprintPlayerHandleRequest implements SerializationInterface {
	public String catalogName;
	public String entitySpawnName;
	public boolean save;
	public boolean directBuy;
	public int toSaveShip = -1;
	public boolean setOwnFaction;
	public BlueprintClassification classification;
	public int spawnOnId = -1;
	public long spawnOnBlock = Long.MIN_VALUE;
	public boolean fill;

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeUTF(catalogName);
		b.writeUTF(entitySpawnName);
		b.writeBoolean(save);
		b.writeBoolean(directBuy);
		b.writeInt(toSaveShip);
		b.writeBoolean(setOwnFaction);
		if(classification != null){
			b.writeByte(classification.ordinal());
		}else{
			b.writeByte(-1);	
		}
		b.writeInt(spawnOnId);
		b.writeLong(spawnOnBlock);
		b.writeBoolean(fill);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		catalogName = b.readUTF();
		entitySpawnName = b.readUTF();
		save = b.readBoolean();
		directBuy = b.readBoolean();
		toSaveShip = b.readInt();
		setOwnFaction = b.readBoolean();
		byte cid = b.readByte();
		if(cid >= 0){
			classification = BlueprintClassification.values()[cid];
		}
		spawnOnId = b.readInt();
		spawnOnBlock = b.readLong();
		fill = b.readBoolean();
	}

	public SegmentPiece getToSpawnOnRail(PlayerState playerState, GameServerState s) {
		SegmentPiece spawnOnRail = null;
		if(spawnOnId > 0) {
			//rail block to spawn on selected
			Sendable sendable = s.getLocalAndRemoteObjectContainer().getLocalObjects().get(spawnOnId);
			
			if(!(sendable instanceof SegmentController)) {
				playerState.sendServerMessagePlayerError(Lng.astr("Cannot spawn on rail: selected object invalid"));
				return null;
			}
			SegmentController c = (SegmentController)sendable;
			
			SegmentPiece pointUnsave = c.getSegmentBuffer().getPointUnsave(spawnOnBlock);
			
			if(pointUnsave == null || !pointUnsave.isValid() || !pointUnsave.getInfo().isRailDockable()) {
				playerState.sendServerMessagePlayerError(Lng.astr("Cannot spawn on rail: selected block is not a rail block to dock on"));
				return null;
			}
			spawnOnRail = pointUnsave;
		}
		return spawnOnRail;
	}

}
