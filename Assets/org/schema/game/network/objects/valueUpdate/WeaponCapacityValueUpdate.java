package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import api.mod.annotations.DoesNotWork;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType;

public class WeaponCapacityValueUpdate extends ValueUpdate {
	float timer;
	protected float val;
	protected WeaponType weapon;

	@Override
	public boolean applyClient(ManagerContainer<?> o) {
		o.setAmmoCapacity(weapon, this.val, timer, false);
		return true;
	}

	@Override
	public void setServer(ManagerContainer<?> o, long parameter) {
		setServer(o, WeaponType.values()[(int) parameter]);
	}

	public void setServer(ManagerContainer<?> o, WeaponType type) {
		this.weapon = type;
		this.val = o.getAmmoCapacity(weapon);
		this.timer = o.getAmmoCapacityTimer(weapon);
	}

	/**
	 * This value update type is meaningless without a weapon to assign to; as such, this method throws an error.
	 */
	@Override @DoesNotWork("Should not call setServer with one argument; calls must specify which weapon's ammunition capacity is being updated.")
	public void setServer(ManagerContainer<?> o) {
		throw new IllegalArgumentException("Calls to WeaponCapacityValueUpdate.setServer must specify weapon type to update!!!");
	}

	@Override
	public ValTypes getType() {
		return ValTypes.WEAPON_CAPACITY_UPDATE;
	}

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeFloat(val);
		buffer.writeFloat(timer);
		buffer.writeByte(weapon.ordinal());
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		val = stream.readFloat();
		timer = stream.readFloat();
		weapon = WeaponType.values()[stream.readByte()];
	}

}
