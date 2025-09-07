package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldContainerInterface;

public class ShieldLocalSingleValueUpdate extends DoubleValueUpdate {
	
	public long shieldId;
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		((ShieldContainerInterface) o).getShieldAddOn().getShieldLocalAddOn().receivedShieldSingle(shieldId, val);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		throw new RuntimeException("illegal call");
	}
	
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		buffer.writeLong(shieldId);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		shieldId = stream.readLong();
	}

	public void setServer(ManagerContainer<?> o, long parameter, double shield) {
		this.val = shield;
		this.shieldId = parameter;
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIELD_LOCAL;
	}

}
