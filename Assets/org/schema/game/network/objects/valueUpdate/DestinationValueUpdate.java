package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ActivationManagerInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.data.element.meta.weapon.MarkerBeam;

public class DestinationValueUpdate extends StringModuleValueUpdate {

	protected long destParam;

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		assert (destParam != Long.MIN_VALUE);
		buffer.writeLong(destParam);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		destParam = stream.readLong();
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ActivationCollectionManager v = ((ActivationManagerInterface) o).getActivation().getCollectionManagersMap().get(parameter);
		if (v != null) {
			MarkerBeam mm = new MarkerBeam(-1);
			mm.markerLocation = destParam;
			mm.marking = this.val;
			mm.realName = this.val;
			v.setDestination(mm);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		ActivationCollectionManager v = ((ActivationManagerInterface) o).getActivation().getCollectionManagersMap().get(parameter);
		if (v != null && v.getDestination() != null) {
			this.val = v.getDestination().marking;
			this.destParam = v.getDestination().markerLocation;
		}
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.DESTINATION;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScanChargeValueUpdate [parameter=" + parameter + ", val=" + val
				+ "]";
	}

}
