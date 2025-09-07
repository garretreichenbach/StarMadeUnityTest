package org.schema.game.common.data.world.space;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.shape.SphereShapeExt;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.network.objects.NetworkPlanetCore;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector4f;

/**
 * Planet Core for Ico (new) Planets. Borrows a lot of functionality from PlanetCore.java but implements it for the new planets.
 */
public class PlanetIcoCore extends FixedSpaceEntity {

	private NetworkPlanetCore networkPlanetCore;
	private float radius = 200.0f;
	private String realName = "";
	private int sides = 20;
	private String[] plates = new String[sides];
	private float totalMass;
	private SectorInformation.PlanetType planetType;

	public PlanetIcoCore(StateInterface state) {
		super(state);
	}

	@Override
	public void initPhysics() {
		super.initPhysics();
		if(getPhysicsDataContainer().getObject() == null) {
			Transform t = getRemoteTransformable().getInitialTransform();
			SphereShapeExt root = new SphereShapeExt(radius * 1.1f, this);
			Transform local = new Transform();
			local.setIdentity();
			getPhysicsDataContainer().setShape(root);
			getPhysicsDataContainer().setInitial(t);
			RigidBody bodyFromShape = getPhysics().getBodyFromShape(root, getMass(), getPhysicsDataContainer().initialTransform);
			bodyFromShape.setUserPointer(getId());
			getPhysicsDataContainer().setObject(bodyFromShape);
			getWorldTransform().set(t);
			assert (getPhysicsDataContainer().getObject() != null);
		} else System.err.println("[SegmentController][WARNING] not adding initial physics object. it already exists");
		setFlagPhysicsInit(true);
	}

	@Override
	public float getMass() {
		if(totalMass == 0) updateMass();
		return totalMass;
	}

	public void updateMass() {
		totalMass = 0;
		for(int i = 0; i < sides; i++) {
			PlanetIco plate = (PlanetIco) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(plates[i]);
			if(plate != null) totalMass += plate.getTotalPhysicalMass();
		}
	}

	public PlanetIco[] getPlates() {
		PlanetIco[] plates = new PlanetIco[sides];
		for(int i = 0; i < sides; i++) {
			plates[i] = (PlanetIco) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(this.plates[i]);
		}
		return plates;
	}

	public void setPlates(PlanetIco[] plates) {
		for(int i = 0; i < sides; i++) {
			if(plates[i] != null) this.plates[i] = plates[i].getUniqueIdentifier();
			else this.plates[i] = null;
		}
	}

	private Tag[] platesToTags() {
		Tag[] tags = new Tag[sides + 1];
		for(int i = 0; i < sides; i++) tags[i] = new Tag(Tag.Type.STRING, "plate" + (i + 1), plates[i]);
		tags[sides] = FinishTag.INST;
		return tags;
	}

	public int getSides() {
		return sides;
	}

	public void setSides(int sides) {
		this.sides = sides;
	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	@Override
	public EntityType getType() {
		return EntityType.PLANET_CORE;
	}

	@Override
	public void newNetworkObject() {
		networkPlanetCore = new NetworkPlanetCore(getState());
		updateMass();
	}

	@Override
	public NetworkPlanetCore getNetworkObject() {
		return networkPlanetCore;
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.PLANET_CORE;
	}

	@Override
	public void updateToNetworkObject() {
		updateMass();
		super.updateToNetworkObject();
		NetworkPlanetCore n = networkPlanetCore;
		n.radius.set(radius);
		n.uid.set(getUniqueIdentifier());
		for(int i = 0; i < sides; i++) n.plates.get(i).set(plates[i]);
		n.mass.set(getMass());
		n.planetType.set(planetType.name());
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);
		NetworkPlanetCore n = (NetworkPlanetCore) o;
		radius = n.radius.getFloat();
		setUniqueIdentifier(n.uid.get());
		plates = new String[sides];
		for(int i = 0; i < sides; i++) plates[i] = networkPlanetCore.plates.get(i).get();
		totalMass = n.mass.getFloat();
		planetType = SectorInformation.PlanetType.valueOf(n.planetType.get());
	}

	@Override
	public void updateToFullNetworkObject() {
		updateMass();
		super.updateToFullNetworkObject();
		NetworkPlanetCore n = networkPlanetCore;
		n.radius.set(radius);
		n.uid.set(getUniqueIdentifier());
		for(int i = 0; i < sides; i++) n.plates.get(i).set(plates[i]);
		n.mass.set(getMass());
		n.planetType.set(planetType.name());
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		NetworkPlanetCore n = (NetworkPlanetCore) o;
		radius = n.radius.getFloat();
		setUniqueIdentifier(n.uid.get());
		plates = new String[sides];
		for(int i = 0; i < sides; i++) plates[i] = networkPlanetCore.plates.get(i).get();
		totalMass = n.mass.getFloat();
		planetType = SectorInformation.PlanetType.valueOf(n.planetType.get());
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] tags = tag.getStruct();
		setUniqueIdentifier(tags[0].getString());
		radius = tags[1].getFloat();
		realName = tags[2].getString();
		for(int i = 0; i < sides; i++) plates[i] = tags[3].getStruct()[i].getString();
		totalMass = tags[4].getFloat();
		planetType = SectorInformation.PlanetType.valueOf(tags[5].getString());
		super.fromTagStructure(tags[6]);
	}

	@Override
	public Tag toTagStructure() {
		updateMass();
		Tag uniqueIDTag = new Tag(Tag.Type.STRING, "UniqueIdentifier", getUniqueIdentifier());
		Tag radiusTag = new Tag(Tag.Type.FLOAT, "Radius", radius);
		Tag realNameTag = new Tag(Tag.Type.STRING, "RealName", realName);
		Tag platesTag = new Tag(Tag.Type.STRUCT, "Plates", platesToTags());
		Tag massTag = new Tag(Tag.Type.FLOAT, "Mass", totalMass);
		Tag planetTypeTag = new Tag(Tag.Type.STRING, "PlanetType", planetType.name());
		return new Tag(Tag.Type.STRUCT, null, new Tag[]{uniqueIDTag, radiusTag, realNameTag, platesTag, massTag, planetTypeTag, super.toTagStructure(), FinishTag.INST});
	}

	@Override
	public void getRelationColor(FactionRelation.RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyOther);
			case FRIEND -> out.set(ColorPalette.allyOther);
			case NEUTRAL -> out.set(ColorPalette.neutralOther);
		}
		if(sameFaction) out.set(ColorPalette.factionOther);
		out.x += select;
		out.y += select;
		out.z += select;
	}

	@Override
	protected boolean hasVirtual() {
		return false;
	}

	@Override
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realname) {
		realName = realname;
		synchronized(getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for(Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(s instanceof PlanetIco && ((PlanetIco) s).getPlanetCoreUID().equals(getUniqueIdentifier())) {
					((PlanetIco) s).setRealName(realName);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "PlanetCore( id " + getId() + ")";
	}

	@Override
	public CollisionType getCollisionType() {
		return CollisionType.PLANET_CORE;
	}

	@Override
	public void sendHitConfirm(byte damageType) {

	}

	@Override
	public boolean isSegmentController() {
		return false;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public String getName() {
		return Lng.str("Planet %s", realName);
	}

	@Override
	public String toNiceString() {
		return Lng.str("%s Planet", getPlanetType().name);
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public void sendClientMessage(String str, byte type) {

	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {

	}

	public SectorInformation.PlanetType getPlanetType() {
		return planetType;
	}

	public void setPlanetType(SectorInformation.PlanetType planetType) {
		this.planetType = planetType;
	}

	public float getPlateRadius() {
		if(plates.length > 0 && plates[0] != null) {
			PlanetIco plate = (PlanetIco) getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(plates[0]);
			return plate.radius;
		}
		return 0;
	}
}
