package org.schema.game.common.data.element.meta.weapon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.element.ElementCollection;
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

public class MarkerBeam extends Weapon {

	public String marking = "unmarked";
	public long markerLocation = Long.MIN_VALUE;
	public String realName = "unmarked";
	float speed = 70f;
	private Vector4f color = new Vector4f(
			(1f),
			(1f),
			(1f),
			1.0f);

	public MarkerBeam(int id) {
		super(id, WeaponSubType.MARKER.type);
	}

	public static MarkerBeam getMarkingFromTag(Tag t, int shift) {
		Tag[] v = (Tag[]) t.getValue();
		MarkerBeam m = new MarkerBeam(0);
		m.marking = (String) v[0].getValue();
		m.markerLocation = (Long) v[1].getValue();
		if(shift != 0){
			m.markerLocation = ElementCollection.shiftIndex(m.markerLocation, shift, shift, shift);
		}
		return m;
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		marking = stream.readUTF();
		realName = stream.readUTF();
		markerLocation = stream.readLong();
	}

	@Override
	public void fromTag(Tag tag) {
		Tag[] v = (Tag[]) tag.getValue();
		marking = (String) v[0].getValue();
		realName = (String) v[1].getValue();
		markerLocation = (Long) v[2].getValue();
		
	}

	@Override
	public Tag getBytesTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.STRING, null, marking),
				new Tag(Type.STRING, null, realName),
				new Tag(Type.LONG, null, markerLocation),
				FinishTag.INST});
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeUTF(marking);
		stream.writeUTF(realName);
		stream.writeLong(markerLocation);
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
			((PlayerCharacter) playerCharacter).shootMarkerBeam(((PlayerState) playerCharacter.getOwnerState()).getControllerState().getUnits().iterator().next(), 100, this, addButton, removeButton);
		}
	}
	@Override
	public String getName() {
		return Lng.str("Marker Beam");
	}
	@Override
	protected String toDetailedString() {
		return Lng.str("Marker Beam\nCurrent Marking On: %s\nLocal Pos: %s\nRight click -> mark\nLeft click -> enter marker informatinon",  realName,  (markerLocation != Long.MIN_VALUE ? ElementCollection.getPosFromIndex(markerLocation, new Vector3i()) : Lng.str("undefined")));
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

	public Tag toTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{

				new Tag(Type.STRING, null, marking),
				new Tag(Type.LONG, null, markerLocation),
				FinishTag.INST
		});
	}

	public boolean equalsBeam(MarkerBeam obj) {
		return marking != null && marking.equals(obj.marking) && markerLocation == obj.markerLocation;
	}
	@Override
	public boolean equalsObject(MetaObject other) {
		return super.equalsTypeAndSubId(other) &&  marking.equals(((MarkerBeam)other).marking) && markerLocation == (((MarkerBeam)other).markerLocation);
	}
	@Override
	protected void setupEffectSet(InterEffectSet s) {
		
	}
	public HitType getHitType() {
		return HitType.SUPPORT;
	}
}
