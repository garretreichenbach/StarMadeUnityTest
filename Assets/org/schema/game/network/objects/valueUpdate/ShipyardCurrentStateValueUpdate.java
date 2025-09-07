package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipyardManagerContainerInterface;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;

public class ShipyardCurrentStateValueUpdate extends ParameterValueUpdate {

	private double completion;
	private byte state;
	private int designLoaded;

//	protected String currentState;

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		if(completion < 0){
			buffer.writeByte((byte)completion);
		}else{
			buffer.writeByte((byte)Math.min(100,Math.max(0, completion*100)));
		}
		buffer.writeByte(state);
		buffer.writeInt(designLoaded);
	}
	public ShipyardCurrentStateValueUpdate(){}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		completion = stream.readByte()/100f;
		state = stream.readByte();
		designLoaded = stream.readInt();
	}

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.setCompletionOrderPercent(completion);
			v.currentClientState = state;
			v.setCurrentDesign(designLoaded);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		ShipyardCollectionManager v = ((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap().get(parameter);
		
		if (v != null) {
			this.completion = v.getCompletionOrderPercent();
			this.state = v.getStateByteOnServer();
			this.designLoaded = v.getCurrentDesign();
		}else{
			this.state = -1;
			assert(false):((ShipyardManagerContainerInterface) o).getShipyard().getElementManager().getCollectionManagersMap()+"; "+parameter;
		}
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIPYARD_STATE_UPDATE;
	}


}
