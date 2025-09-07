package org.schema.game.common.data.element.meta.weapon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class HealBeam extends Weapon {

	float speed = 70f;
	private int healPower = 5;
	private int reload = 150;

	private Vector4f color = new Vector4f(
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			1.0f);

	public HealBeam(int id) {
		super(id, WeaponSubType.HEAL.type);
	}
	@Override
	public String getName() {
		return Lng.str("Heal Beam");
	}
	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		healPower = stream.readInt();
		speed = stream.readFloat();
		color.set(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		healPower = (Integer) v[0].getValue();
		speed = (Float) v[1].getValue();
		reload = (Integer) v[2].getValue();

		if (v[3].getType() == Type.VECTOR4f) {
			color = (Vector4f) v[3].getValue();
		}
	}
	public boolean isIgnoringShields() {
		return true;
	}
	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, healPower),
				new Tag(Type.FLOAT, null, speed),
				new Tag(Type.INT, null, reload),
				new Tag(Type.VECTOR4f, null, color),
				FinishTag.INST});
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeInt(healPower);
		stream.writeFloat(speed);

		stream.writeFloat(color.x);
		stream.writeFloat(color.y);
		stream.writeFloat(color.z);
		stream.writeFloat(color.w);

	}

	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, boolean addButton, boolean removeButton, Timer timer) {
		Vector3f dir = state.getForward(new Vector3f());

		fire(playerCharacter, state, dir, addButton, removeButton, timer);
	}

	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir, boolean addButton, boolean removeButton, Timer timer) {

		dir.scale(speed);

		if (!state.isOnServer() && ((GameClientState) state.getState()).getCurrentSectorId() != playerCharacter.getSectorId()) {
			return;
		}

		if (playerCharacter instanceof PlayerCharacter) {
			((PlayerCharacter) playerCharacter).shootHealingBeam(((PlayerState) playerCharacter.getOwnerState()).getControllerState().getUnits().iterator().next(), healPower, this, addButton, removeButton);
		}
	}

	@Override
	protected String toDetailedString() {
		return Lng.str("HealBeam\nPower: %s\nSpeed: %s\nColor(RGBA): %s",  healPower,  speed,  color);
	}

	/**
	 * @return the color
	 */
	public Vector4f getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Vector4f color) {
		this.color = color;
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) && healPower == ((HealBeam)other).healPower && color.equals(((HealBeam)other).color);
	}
	@Override
	protected void setupEffectSet(InterEffectSet s) {
		s.setStrength(InterEffectType.EM, 1.0f);
		
	}
	public HitType getHitType() {
		return HitType.SUPPORT;
	}
}
