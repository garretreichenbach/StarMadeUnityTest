package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShieldAddOn;
import org.schema.game.common.controller.elements.ShieldContainerInterface;
import org.schema.game.common.controller.elements.ShieldLocal;
import org.schema.game.common.controller.elements.ShieldLocalAddOn;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShieldLocalFullValueUpdate extends ValueUpdate {
	
	public final List<ShieldLocal> shields = new ObjectArrayList<ShieldLocal>();
	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		ShieldAddOn sh = ((ShieldContainerInterface)o).getShieldAddOn();
		ShieldLocalAddOn shieldLocalAddOn = sh.getShieldLocalAddOn();
		shieldLocalAddOn.receivedShields(shields);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		throw new RuntimeException("illegal call");
	}

	@Override
	public ValTypes getType() {
		return ValTypes.SHIELD_LOCAL_FULL;
	}

	public void setServer(ManagerContainer<?> managerContainer, ShieldLocalAddOn shieldLocalAddOn) {
		shields.addAll(shieldLocalAddOn.getAllShields());
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeShort(shields.size());
		for(ShieldLocal l : shields){
			l.serialize(buffer, onServer);
		}
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		final short size = stream.readShort();
		for(int i = 0; i < size; i++){
			ShieldLocal l = new ShieldLocal();
			l.deserialize(stream, updateSenderStateId, onServer);
			shields.add(l);
		}
	}

}
