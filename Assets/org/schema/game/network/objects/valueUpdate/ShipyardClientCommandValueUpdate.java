package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardCommandType;
import org.schema.game.network.objects.remote.ShipyardCommand;

public class ShipyardClientCommandValueUpdate extends ParameterValueUpdate {

	protected ShipyardCommand destParam;

	
	public ShipyardClientCommandValueUpdate() {}
	public ShipyardClientCommandValueUpdate(ShipyardCommand cmd) {
		super();
		this.destParam = cmd;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		destParam.serialize(buffer);
		buffer.writeInt(destParam.factionId);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		destParam = new ShipyardCommand();
		destParam.deserialize(stream, updateSenderStateId);
		destParam.factionId = stream.readInt();
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.handleShipyardCommandOnServer(destParam.factionId, ShipyardCommandType.values()[destParam.getCommand()], destParam.getArgs());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
//		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
//		if (v != null) {
//		}
		assert(destParam != null);
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIPYARD_CLIENT_COMMAND;
	}

}
