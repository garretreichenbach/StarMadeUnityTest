package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerCollectionManager;
import org.schema.game.common.data.element.ScannerManagerInterface;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongRangeScanChargeValueUpdate extends FloatModuleValueUpdate {
	int charges;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		LongRangeScannerCollectionManager v = ((ScannerManagerInterface) o).getLongRangeScanner().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.getChargeManager().setCharge(this.val);
			v.getChargeManager().setChargesCount(charges);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		LongRangeScannerCollectionManager v = ((ScannerManagerInterface) o).getLongRangeScanner().getCollectionManagersMap().get(parameter);
		if (v != null) {
			this.val = v.getChargeManager().getCharge();
			this.charges = v.getChargeManager().getChargesCount();
		}
		this.parameter = parameter;
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeInt(charges);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		charges = stream.readInt();
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SCAN_CHARGE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScanChargeValueUpdate [parameter=" + parameter + ", val=" + val
				+ ", charges=" + charges + "]";
	}

}
