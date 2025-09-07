package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.JumpProhibiterModuleInterface;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorCollectionManager;

public class JumpInhibitorValueUpdate extends ParameterValueUpdate {

	boolean active;
	
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		JumpInhibitorCollectionManager v = ((JumpProhibiterModuleInterface) o).getJumpProhibiter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			v.setActive(active);
			return true;
		} else {
			return false;
//			assert(false);
		}
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		JumpInhibitorCollectionManager v = ((JumpProhibiterModuleInterface) o).getJumpProhibiter().getCollectionManagersMap().get(parameter);
		if (v != null) {
			this.active = v.isActive();
		}
		this.parameter = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.JUMP_INHIBITOR;
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeBoolean(active);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		active = stream.readBoolean();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JumpInhibitorValueUpdate [parameter=" + parameter + ", active=" + active
				+ "]";
	}

}
