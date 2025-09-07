package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.inventory.Inventory;

public class ShipyardErrorValueUpdate extends ParameterValueUpdate {

	private String errorString;

//	protected String currentState;

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeUTF(errorString);
	}
	public ShipyardErrorValueUpdate(){}

	public ShipyardErrorValueUpdate(String error) {
		this.errorString = error;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		errorString = stream.readUTF();
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			Inventory secondInventory = ((GameClientState)v.getState()).getGlobalGameControlManager()
			.getIngameControlManager().getPlayerGameControlManager()
			.getInventoryControlManager().getSecondInventory();
			if(secondInventory != null && secondInventory.getInventoryHolder() == o && ((GameClientState)v.getState()).getGlobalGameControlManager()
					.getIngameControlManager().getPlayerGameControlManager()
					.getInventoryControlManager().isTreeActive() && secondInventory.getParameter() == ElementCollection.getIndex(v.getControllerPos())){
				((GameClientState)v.getState()).getController().popupAlertTextMessage(errorString, 0);
			}
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
		assert(errorString != null);
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIPYARD_ERROR_UPDATE;
	}


}
