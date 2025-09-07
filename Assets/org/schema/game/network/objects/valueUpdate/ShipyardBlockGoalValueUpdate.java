package org.schema.game.network.objects.valueUpdate;

import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShipyardBlockGoalValueUpdate extends ParameterValueUpdate {


	

	private ElementCountMap blocksFrom;
	private ElementCountMap blocksto;

	public ShipyardBlockGoalValueUpdate() {}


	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeBoolean(blocksFrom != null);
		
		if(blocksFrom != null){
			blocksFrom.serialize(buffer);
			blocksto.serialize(buffer);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		boolean has = stream.readBoolean();
		
		if(has){
			blocksFrom = new ElementCountMap();
			blocksto = new ElementCountMap();
			
			blocksFrom.deserialize(stream);
			blocksto.deserialize(stream);
		}
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.clientGoalFrom = blocksFrom;
			v.clientGoalTo = blocksto;

			if(v.drawObserver != null){
				v.drawObserver.update(null, null, null);
			}
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		this.parameter = parameter;
		
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			blocksFrom = new ElementCountMap(v.getServerEntityState().currentMapFrom);
			blocksto = new ElementCountMap(v.getServerEntityState().currentMapTo);
		}
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIPYARD_BLOCK_GOAL;
	}

}
