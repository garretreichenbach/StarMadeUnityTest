package org.schema.game.common.controller;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import org.apache.commons.lang3.SerializationUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.game.common.data.world.space.FixedSpaceEntity;
import org.schema.game.network.objects.NetworkGasPlanet;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.schema.game.client.view.planetgas.GasPlanetSurfaceDrawer.GIANT_LOWER_SURFACE;

/**
 * Gas Planet class.
 *
 * @author TheDerpGamer
 */
public class GasPlanet extends FixedSpaceEntity implements Damager, CelestialBodyGravityHandler {
	private static final InterEffectSet PRESSURE_DAMAGE_PROFILE = new InterEffectSet(){
		{
			setStrength(InterEffectHandler.InterEffectType.EM,0.05f);
			setStrength(InterEffectHandler.InterEffectType.KIN, 0.6f);
			setStrength(InterEffectHandler.InterEffectType.HEAT, 0.25f);
		} //mostly crushing, some thermal effects/lightning
	};
	private final Transform initialTransform = new Transform();
	private GasPlanetInformation info;
	private NetworkGasPlanet networkObject;
	private String realName = "Gas Giant";

	public GasPlanet(StateInterface state, GasPlanetInformation sourceInfo) {
		this(state);
		info = sourceInfo;
	}

	public GasPlanet(StateInterface state) {
		super(state);
	}

	@Override
	public void initialize() {
		super.initialize();
		initialTransform.set(getWorldTransform());
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
	public SendableType getSendableType() {
		return SendableTypes.GAS_PLANET;
	}

	@Override
	public String toString() {
		return "GasPlanet(" + getId() + ")[s" + getSectorId() + "]";
	}

	@Override
	public String toNiceString() {
		String r;
		if(getFactionId() != 0) {
			Faction f = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());
			if(f != null) r = getRealName() + "[" + f.getName() + "]";
			else r = getRealName() + Lng.str("[UnknownFaction %d]", getFactionId());
		} else r = getRealName();
		//r += Lng.str(" (Radius: %dm)", (info == null? -1 : info.getRadius()));
		r += "(Radius: " + (networkObject == null ? -1 : networkObject.radius.get()) + "m)"; //TODO fix formatting string/etc
		return r;
	}

	@Override
	public String getRealName() {
		return realName;
	}

	@Override
	public CollisionType getCollisionType() {
		return CollisionType.SIMPLE;
	}

	@Override
	public void initPhysics() { //copied from PlanetCore; any bugs result from that
		super.initPhysics();

		if (getPhysicsDataContainer().getObject() == null) {
			int radius = 1;
			NetworkGasPlanet nto = getNetworkObject();
			if(nto != null){
				radius = getNetworkObject().radius.get();
			} else if(nto == null && info != null){
				System.err.println("No network object to build physics! Defaulting to planet datasheet");
				radius = info.getRadius();
			} else throw new RuntimeException("No information source to build gas giant entity!!!");

			Transform t = getRemoteTransformable().getInitialTransform();

			SphereShape ball = new SphereShape(radius * GIANT_LOWER_SURFACE);
			Transform local = new Transform();
			local.setIdentity();

			getPhysicsDataContainer().setShape(ball);
			getPhysicsDataContainer().setInitial(t);

			RigidBody bodyFromShape = getPhysics().getBodyFromShape(ball, getMass(), getPhysicsDataContainer().initialTransform);

			//			System.err.println("[ENTITY] "+getState()+" "+this+" initialized physics -> "+t.origin);

			bodyFromShape.setUserPointer(getId());

			getPhysicsDataContainer().setObject(bodyFromShape);

			getWorldTransform().set(t);
			assert (getPhysicsDataContainer().getObject() != null);
		} else {
			System.err.println("[SegmentController][WARNING] not adding initial physics object. it already exists");
		}
		if(getPhysicsDataContainer().getObject() != null) getPhysicsDataContainer().getObject().setCollisionFlags(CollisionFlags.STATIC_OBJECT | CollisionFlags.NO_CONTACT_RESPONSE);
		setFlagPhysicsInit(true);
	}

	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.OTHER_SPACE;
	}

	@Override
	public void sendHitConfirm(byte damageType) {

	}

	@Override
	public String getName() {
		return realName;
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
		return PRESSURE_DAMAGE_PROFILE;
	}

	@Override
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {

	}

	@Override
	public EntityType getType() {
		return EntityType.GAS_PLANET;
	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
		//TODO change sector type and update to client prox/etc
	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		/*
		if(needsInfo && info == null) {
			if(isOnServer()) {
				Vector3i sec = getRemoteSector().getServerSector().pos;
				VoidSystem sys = (VoidSystem) getRemoteSector().getServerSector()._getSystem();
				info = sys.getGasPlanetInfo(StellarSystem.getLocalCoordinates(sec, new Vector3i()));
				if(info != null) {
					updateToFullNetworkObject();
					needsInfo = false;
				}
			} //else we just wait for network synchronization
		} else
			needsInfo = false;
		 */
	}

	public RemoteSector getRemoteSector() {
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getSectorId());
		if(sendable instanceof RemoteSector) return ((RemoteSector) sendable);
		else return null;
	}

	@Override
	public void newNetworkObject() {
		networkObject = new NetworkGasPlanet(getState());
	}

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		setUniqueIdentifier(((NetworkGasPlanet)from).uid.get());
		realName = (((NetworkGasPlanet)from).name.get());
		/*if(!isOnServer()){
			byte[] tagBuffer = ((NetworkGasPlanet) from).tagsInfo.get();
			if(tagBuffer.length > 0) try {
				ByteArrayInputStream in = new ByteArrayInputStream(tagBuffer);
				Tag t = Tag.readFrom(in, true, true);
				info = new GasPlanetInformation(t);
			}catch(EOFException ex){
				System.out.println("[CLIENT] Error updating GasPlanet from NT object: End of File while decoding!");
				ex.printStackTrace();
			} catch(IOException ex) {
				System.out.println("[CLIENT] Error updating GasPlanet from NT object: IO Exception!");
				ex.printStackTrace();
			}
		}
		 */
	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		getNetworkObject().uid.set(getUniqueIdentifier());
		/*if(isOnServer()) {
			if(info != null) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					info.toTagStructure(true).writeTo(b, true);
					getNetworkObject().tagsInfo.set(b.toByteArray());
				} catch (IOException ex) {
					System.out.println("Error updating GasPlanet to NT object!");
					ex.printStackTrace();
				}
				networkObject.radius.set(info.getRadius());
			} else
				networkObject.radius.set((int) ServerConfig.GAS_PLANET_SIZE_MEAN_VALUE.getFloat());
		//} //clients should be passive here; this is a static object
		*/
		if(isOnServer()) {
			if (info != null) {
				networkObject.radius.set(info.getRadius());
				getNetworkObject().name.set(info.getName());
				setFactionId(getRemoteSector().getServerSector().getFactionId());
			} else {
				networkObject.radius.set((int) ServerConfig.GAS_PLANET_SIZE_MEAN_VALUE.getFloat());
			}
		}
    }

	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		/*if(isOnServer()) {
			if(info != null) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					info.toTagStructure(true).writeTo(b, true);
					getNetworkObject().tagsInfo.set(b.toByteArray());
				} catch (IOException ex) {
					System.out.println("Error updating GasPlanet to NT object!");
					ex.printStackTrace();
				}
				networkObject.radius.set(info.getRadius());
			} else
				networkObject.radius.set((int) ServerConfig.GAS_PLANET_SIZE_MEAN_VALUE.getFloat());
		//} //clients should be passive here; this is a static object
		 */
		if(isOnServer()) {
			if (info != null) {
				networkObject.radius.set(info.getRadius());
				setFactionId(getRemoteSector().getServerSector().getFactionId());
			} else {
				networkObject.radius.set((int) ServerConfig.GAS_PLANET_SIZE_MEAN_VALUE.getFloat());
			}
		}
    }

	@Override
	public void fromTagStructure(Tag tag) {
		assert "GasPlanet".equals(tag.getName());
		Tag[] subTags = tag.getStruct();
		super.fromTagStructure(subTags[0]);
		info = (GasPlanetInformation) SerializationUtils.deserialize(subTags[1].getByteArray()); //TODO should really prefer the world sheet to some tag-saved thing
		if(info != null){
			realName = info.getName();
		}
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Tag.Type.STRUCT, "GasPlanet", new Tag[] {
				super.toTagStructure(),
				new Tag(Tag.Type.BYTE_ARRAY, "information", SerializationUtils.serialize(info)),
				FinishTag.INST});
	}

	@Override
	protected boolean affectsGravityOf(SimpleTransformableSendableObject<?> target) {
		/*
		float distance = Math.abs(Vector3fTools.distance(target.getWorldTransform().origin, getWorldTransform().origin));
		if(distance < ServerConfig.SECTOR_SIZE.getInt() * 2) return !(target instanceof FloatingRock|| target instanceof AICharacter || target instanceof PlayerCharacter);
		else */
		return false;
	}

	@Override
	public boolean isGravitySource() {
		return true;
	}


	@Override
	protected boolean hasVirtual() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
		return false;
	}

	@Override
	public boolean isWrittenForUnload() {
		return true; //always regenerated upon load, so if anything asks we pretend it already was saved
	}

	@Override
	public NetworkGasPlanet getNetworkObject() {
		return networkObject;
	}

	public GasPlanetInformation getPlanetInfo() {
		return info;
	}

	@Override
	public Vector3f getGravityVector(SimpleTransformableSendableObject<?> target) {
		return new Vector3f(); //Todo: This should push entities out of the planet's atmosphere the closer they are to the center
	}
}
