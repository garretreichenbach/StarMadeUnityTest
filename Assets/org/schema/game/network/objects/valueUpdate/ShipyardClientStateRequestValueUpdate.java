package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardRequestType;

public class ShipyardClientStateRequestValueUpdate extends ParameterValueUpdate {


	
	private ShipyardRequestType request;

	public ShipyardClientStateRequestValueUpdate() {}

	public ShipyardClientStateRequestValueUpdate(ShipyardRequestType request) {
		this.request = request;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeByte((byte)request.ordinal());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		this.request = ShipyardRequestType.values()[stream.readByte()];
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			switch(request) {
				case CANCEL -> v.cancelOrderOnServer();
				case INFO -> v.sendShipyardGoalToClient();
				case STATE -> v.sendShipyardStateToClient();
				case UNDOCK -> v.undockRequestedFromShipyard();
				default -> {
				}
			}
			
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIPYARD_CLIENT_STATE_REQUEST;
	}

}
