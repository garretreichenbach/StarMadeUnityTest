package org.schema.game.common.data.element.meta.weapon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class LaserWeapon extends Weapon {

	public int damage = 10;//10000; //10f
	float speed = 120;//2000f;//80f;
	float distance = 200; //150;
	private int reload = 150;
	private long vol_lastShot = 0;
	private float projectileWidth = 1;
	private float impactForce = 0;
	private int penetrationDepth = 9;
	
	private Vector4f color = new Vector4f(
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			1.0f);


	

	public LaserWeapon(int id) {
		super(id, WeaponSubType.LASER.type);
		
		
	}
	@Override
	public String getName() {
		return Lng.str("Laser Weapon");
	}
	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		damage = stream.readInt();
		speed = stream.readFloat();
		color.set(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		damage = (Integer) v[0].getValue();
		damage = 0;
		speed = (Float) v[1].getValue();
		reload = (Integer) v[2].getValue();

		if (v[3].getType() == Type.VECTOR4f) {
			color = (Vector4f) v[3].getValue();
		}
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, damage),
				new Tag(Type.FLOAT, null, speed),
				new Tag(Type.INT, null, reload),
				new Tag(Type.VECTOR4f, null, color),
				FinishTag.INST});
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeInt(damage);
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
		AcidFormulaType acidForumula = AcidFormulaType.EQUAL_DIST;
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - vol_lastShot > reload) {
			playerCharacter.getParticleController().addProjectile(
					playerCharacter,
					new Vector3f(playerCharacter.getShoulderWorldTransform().origin),
					dir,
					damage,
					distance, 
					acidForumula.ordinal(),
					projectileWidth,
					penetrationDepth,
					impactForce,
					getWeaponUsableId(), 
					color, 0);
			vol_lastShot = currentTimeMillis;
		}
	}

	@Override
	protected String toDetailedString() {
		return Lng.str("Laser\nDamage: %s\nSpeed: %s\nReload: %s ms\nColor(RGBA): %s",  damage,  speed,  reload,  color);
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
		return super.equalsTypeAndSubId(other) && damage == ((LaserWeapon)other).damage && color.equals(((LaserWeapon)other).color) && speed == (((LaserWeapon)other).speed);
	}
	@Override
	protected void setupEffectSet(InterEffectSet s) {
		s.setStrength(InterEffectType.KIN, 0.8f);
		s.setStrength(InterEffectType.HEAT, 0.0f);
		s.setStrength(InterEffectType.EM, 0.2f);
		
	}
}
