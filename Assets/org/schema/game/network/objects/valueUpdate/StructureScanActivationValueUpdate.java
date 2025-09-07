package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.JumpProhibiterModuleInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorCollectionManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerCollectionManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StructureScanActivationValueUpdate extends BooleanValueUpdate {
	long lastStart;
	
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		StructureScannerElementManager ele = o.getShortRangeScanner().getElementManager();
		if (ele != null) {
			ele.getCollection().setActiveState(val,lastStart);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		StructureScannerCollectionManager v = o.getShortRangeScanner().getElementManager().getCollection();
		if (v != null) {
			this.val = o.getShortRangeScanner().getElementManager().getCollection().isActive();
			this.lastStart = o.getShortRangeScanner().getElementManager().getCollection().getActivationManager().getLastActivation();
		}
	}

	@Override
	public ValTypes getType() {
		return ValTypes.STRUCTURE_SCANNER_ACTIVATION;
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeLong(lastStart);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		lastStart = stream.readLong();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StructureScanActivationValueUpdate [active=" + val + ", lastActivation=" + lastStart
				+ "]";
	}

}
