package org.schema.game.common.data.mines.updates;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.controller.elements.mines.MineController;

public abstract class MineUpdate implements SerializationInterface{
	
	public long timeStampServerSent;
	public abstract MineUpdateType getType();
	private interface UpdateInst{
		public MineUpdate inst();
	}
	public enum MineUpdateType{
		SECTOR_REQUEST(MineUpdateSectorRequest::new),
		SECTOR_DATA(MineUpdateSectorData::new),
		MINE_ADD(MineUpdateMineAdd::new),
		MINE_REMOVE(MineUpdateMineRemove::new),
		MINE_CHANGE_HP(MineUpdateMineHpChange::new),
		CLEAR_SECTOR(MineUpdateClearSector::new),
		MINE_HIT(MineUpdateMineHit::new),
		MINE_ARM(MineUpdateMineArmedChange::new),
		ARM_MINES_REQUEST(MineUpdateArmMinesRequest::new),
		MINE_CHANGE_AMMO(MineUpdateMineAmmoChange::new),
		;
		
		private UpdateInst inst;
		private MineUpdateType(UpdateInst inst) {
			this.inst = inst;
		}
		public static boolean check(){
			for(MineUpdateType t : MineUpdateType.values()) {
				if(t.inst.inst().getType() != t) {
					assert(false):t;
					return false;
				}
			}
			return true;
		}	
	}
	public static MineUpdate deserializeStatic(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		MineUpdateType type = MineUpdateType.values()[b.readByte()];
		MineUpdate inst = type.inst.inst();
		inst.deserialize(b, updateSenderStateId, isOnServer);
		return inst;
	}
	
	@Override
	public final void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeByte((byte)getType().ordinal());
		serializeData(b, isOnServer);
	}
	protected abstract void serializeData(DataOutput b, boolean isOnServer) throws IOException;

	public abstract void execute(ClientChannel clientChannel, MineController mineController);
	
}
