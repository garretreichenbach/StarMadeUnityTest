package org.schema.game.common.data.element.meta.weapon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.missile.MissileController;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;

public class RocketLauncherWeapon extends Weapon {

	public int damage = 200;
	public float speed = 30f;
	public float distance = 300f;
	public int reload = 13000;

	private Vector4f color = new Vector4f(
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			Math.min(1f, (float) Math.random() + (float) Math.random()),
			1.0f);


	private long vol_lastShot = 0;

	public RocketLauncherWeapon(int id) {
		super(id, WeaponSubType.ROCKET_LAUNCHER.type);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		damage = stream.readInt();
		speed = stream.readFloat();
		color.set(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());
		float placeholder = stream.readFloat();
		distance = stream.readFloat();
		reload = stream.readInt();
	}
	@Override
	public String getName() {
		return Lng.str("Rocket Launcher");
	}
	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		damage = (Integer) v[0].getValue();
		speed = (Float) v[1].getValue();
		reload = (Integer) v[2].getValue();
		color = (Vector4f) v[3].getValue();
		float placeholder = (Float) v[4].getValue();
		distance = (Float) v[5].getValue();
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.INT, null, damage),
				new Tag(Type.FLOAT, null, speed),
				new Tag(Type.INT, null, reload),
				new Tag(Type.VECTOR4f, null, color),
				new Tag(Type.FLOAT, null, 0f),
				new Tag(Type.FLOAT, null, distance),
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

		stream.writeFloat(0f);
		stream.writeFloat(distance);
		stream.writeInt(reload);

	}
	private Vector3f dirTmp = new Vector3f();
	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, boolean addButton, boolean removeButton, Timer timer) {

		fire(playerCharacter, state, state.getForward(dirTmp), addButton, removeButton, timer);

	}

	@Override
	public void fire(AbstractCharacter<?> playerCharacter, AbstractOwnerState state, Vector3f dir, boolean addButton, boolean removeButton, Timer timer) {

		dir.scale(speed);

		long currentTimeMillis = System.currentTimeMillis();
//		System.err.println(playerCharacter.getState()+" :::: "+(currentTimeMillis - vol_lastShot)/1000+"; "+reload);
		if (currentTimeMillis - vol_lastShot > reload) {
			if (state.isOnServer()) {
				MissileController missileController = ((GameServerState) state.getState()).getController().getMissileController();
				short lightType = 0;
				System.err.println("[SERVER] Character fireing missile launcher: " + playerCharacter.getWorldTransform().origin + " -> " + dir);
				long weaponId = (long)getId();
				Missile missile = missileController.addDumbMissile(playerCharacter, new Transform(playerCharacter.getShoulderWorldTransform()), dir, speed, damage, distance, weaponId, lightType);
				missile.selfDamage = true;
			}
			vol_lastShot = currentTimeMillis;
		}
	}

	@Override
	protected String toDetailedString() {
		return Lng.str("Rocket Launcher\nDamage: %s\nSpeed: %s\nReload: %s ms\nColor(RGBA): %s\nDistance: %s",  damage,  speed,  reload,  color,   distance);
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

	/**
	 * @return the vol_lastShot
	 */
	public long getVol_lastShot() {
		return vol_lastShot;
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) && damage == ((RocketLauncherWeapon)other).damage && color.equals(((RocketLauncherWeapon)other).color) && speed == (((RocketLauncherWeapon)other).speed);
	}
	@Override
	protected void setupEffectSet(InterEffectSet s) {
		s.setStrength(InterEffectType.KIN, 0.2f);
		s.setStrength(InterEffectType.HEAT, 0.8f);
		s.setStrength(InterEffectType.EM, 0.0f);
	}
}
