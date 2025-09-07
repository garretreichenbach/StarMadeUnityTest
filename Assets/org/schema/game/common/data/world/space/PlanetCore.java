package org.schema.game.common.data.world.space;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentBuffer;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerPlanetCore;
import org.schema.game.common.controller.damage.beam.DamageBeamHittable;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.CollisionType;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.shape.DodecahedronShapeExt;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.network.objects.NetworkPlanetCore;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PlanetCore extends FixedSpaceEntity implements DamageBeamHittable {

	public static final int MAX_HP = 10000000;//0000000;
	public static final int MAX_HP_RECHARGE_PER_SEC = 5000;
	int tmpFaction;
	private NetworkPlanetCore networkPlanetCore;
	private float radius = 200.0f;
	private float hitPoints = MAX_HP;
	private short[] ores = {};
	private short[] top = {};
	private short[] rock = {};
	private short[] flowers = {};
	private short[] fill = {};
	private boolean destroyed;
	private String realName = "";
	private Dodecahedron h;


	public PlanetCore(StateInterface state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.PLANET_CORE;
	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.PLANET_CORE;
	}
	@Override
	public NetworkPlanetCore getNetworkObject() {
		return networkPlanetCore;
	}
	public CollisionType getCollisionType() {
		return CollisionType.PLANET_CORE;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject o) {
		super.initFromNetworkObject(o);

		NetworkPlanetCore n = (NetworkPlanetCore) o;

		radius = n.radius.getFloat();
		setUniqueIdentifier(n.uid.get());
		hitPoints = networkPlanetCore.hp.getFloat();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject, int)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		if (!isOnServer()) {
			hitPoints = networkPlanetCore.hp.getFloat();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);

		if (hitPoints < MAX_HP) {
			hitPoints = Math.min(MAX_HP, hitPoints + MAX_HP_RECHARGE_PER_SEC * timer.getDelta());
		}

		if (destroyed && isOnServer()) {

			setFactionAll(0);

			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());

			if (sector != null) {
				try {
					StellarSystem ss = ((GameServerState) getState()).getUniverse().getStellarSystemFromSecPos(sector.pos);

					if (ss != null) {
						int x = ss.getLocalCoordinate(sector.pos.x);
						int y = ss.getLocalCoordinate(sector.pos.y);
						int z = ss.getLocalCoordinate(sector.pos.z);
						int index = ss.getIndex(x, y, z);
						ss.setSectorType(index, SectorInformation.SectorType.ASTEROID);
						sector.setChangedForDb(true);
						sector.setTransientSector(false);
						for (PlayerState p : ((GameServerState) getState()).getPlayerStatesByName().values()) {
							p.updateProximitySectors();
						}
					} else {
						try {
							((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("Error occured on exploding\nplanet. please send\nin a report"), ServerMessage.MESSAGE_TYPE_ERROR);
							throw new IllegalArgumentException("[SERVER] " + getUniqueIdentifier() + " " + this + " System of destroyed planed not loaded: " + getSectorId());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("Error occured on exploding\nplanet. please send\nin a report"), ServerMessage.MESSAGE_TYPE_ERROR);
				}
			} else {
				try {
					((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("Error occured on exploding\nplanet. please send\nin a report"), ServerMessage.MESSAGE_TYPE_ERROR);
					throw new IllegalArgumentException("[SERVER] " + getUniqueIdentifier() + " " + this + " Sector of destroyed planed not loaded: " + getSectorId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			markForPermanentDelete(true);
			setMarkedForDeleteVolatile(true);
			synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof Planet && ((Planet) s).getCore() == this) {
						((Planet) s).setPlanetCore(null);
						((Planet) s).setPlanetCoreUID("none");
						((Planet) s).setBlownOff(new Vector3f(getWorldTransform().origin));
					}
				}
			}
			((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("Exploded Planet\n%s",  this), ServerMessage.MESSAGE_TYPE_ERROR);
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateToFullNetworkObject()
	 */
	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();

		NetworkPlanetCore n = networkPlanetCore;

		n.radius.set(radius);
		n.uid.set(getUniqueIdentifier());
		n.hp.set(hitPoints);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#updateToNetworkObject()
	 */
	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();

		if (isOnServer()) {
			networkPlanetCore.hp.set(hitPoints);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#fromTagStructure(org.schema.schine.resource.tag.Tag)
	 */
	@Override
	public void fromTagStructure(Tag tag) {

		Tag[] maintags = (Tag[]) tag.getValue();

		Tag[] pcTags = (Tag[]) maintags[0].getValue();

		setUniqueIdentifier((String) pcTags[0].getValue());
		radius = (Float) pcTags[1].getValue();

		ores = Tag.shortArrayFromTagStruct(pcTags[2]);
		top = Tag.shortArrayFromTagStruct(pcTags[3]);
		rock = Tag.shortArrayFromTagStruct(pcTags[4]);
		flowers = Tag.shortArrayFromTagStruct(pcTags[5]);
		fill = Tag.shortArrayFromTagStruct(pcTags[6]);

		super.fromTagStructure(maintags[1]);
	}

	@Override
	public Tag toTagStructure() {

		Tag pcTag = new Tag(Tag.Type.STRUCT, "PlanetCore",
				new Tag[]{

						new Tag(Tag.Type.STRING, null, getUniqueIdentifier()),
						new Tag(Tag.Type.FLOAT, null, radius),
						Tag.listToTagStruct(ores, null),
						Tag.listToTagStruct(top, null),
						Tag.listToTagStruct(rock, null),
						Tag.listToTagStruct(flowers, null),
						Tag.listToTagStruct(fill, null),

						super.toTagStructure(),
						FinishTag.INST});

		return new Tag(Tag.Type.STRUCT, null,
				new Tag[]{pcTag, super.toTagStructure(), FinishTag.INST});

	}

	@Override
	public void getRelationColor(FactionRelation.RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyOther);
			case FRIEND -> out.set(ColorPalette.allyOther);
			case NEUTRAL -> out.set(ColorPalette.neutralOther);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionOther);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#hasVirtual()
	 */
	@Override
	protected boolean hasVirtual() {
		return false;
	}

	@Override
	public String getRealName() {
		return realName;
	}

	public void setRealNameToAll(String realname) {
		realName = realname;
		synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof Planet && ((Planet) s).getPlanetCoreUID().equals(getUniqueIdentifier())) {
					((Planet) s).setRealName(realName);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PlanetCore( id " + getId() + "; hp " + hitPoints + ")";
	}

	/**
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * @return the ores
	 */
	public short[] getOres() {
		return ores;
	}

	/**
	 * @param ores the ores to set
	 */
	public void setOres(short[] ores) {
		this.ores = ores;
	}

	/**
	 * @return the top
	 */
	public short[] getTop() {
		return top;
	}

	/**
	 * @param top the top to set
	 */
	public void setTop(short[] top) {
		this.top = top;
	}

	/**
	 * @return the rock
	 */
	public short[] getRock() {
		return rock;
	}

	/**
	 * @param rock the rock to set
	 */
	public void setRock(short[] rock) {
		this.rock = rock;
	}

	/**
	 * @return the flowers
	 */
	public short[] getFlowers() {
		return flowers;
	}

	/**
	 * @param flowers the flowers to set
	 */
	public void setFlowers(short[] flowers) {
		this.flowers = flowers;
	}

	/**
	 * @return the fill
	 */
	public short[] getFill() {
		return fill;
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFill(short[] fill) {
		this.fill = fill;
	}

//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback callback,
//	                                     Damager particleOwner, float damage, float damageBeforeShield,
//	                                     Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
//
//		if (!canHit(particleOwner)) {
//			callback.hit = false;
//			return callback;
//		}
//
//		hitPoints = (hitPoints - damage);
//
//		onDamage(particleOwner);
//		callback.hit = true;
//		callback.addDamageDone(damage);
//		return callback;
//	}



	@Override
	public boolean isVulnerable() {
		return true;
	}

	@Override
	public boolean checkAttack(Damager from, boolean checkDocked,
	                           boolean notifyFaction) {
		return canHit(from);
	}

	private boolean canHit(Damager from) {
		synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof Planet && ((Planet) s).getPlanetCoreUID().equals(getUniqueIdentifier()) && ((Planet) s).isHomeBase()) {
					return false;
				}
			}
		}
		if (isOnServer()) {
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			if (sector != null && sector.isProtected()) {
				if (from != null && from instanceof PlayerControllable) {
					List<PlayerState> attachedPlayers = ((PlayerControllable) from).getAttachedPlayers();
					for (int i = 0; i < attachedPlayers.size(); i++) {
						PlayerState ps = attachedPlayers.get(i);
						if (System.currentTimeMillis() - ps.lastSectorProtectedMsgSent > 5000) {
							ps.lastSectorProtectedMsgSent = System.currentTimeMillis();
							ps.sendServerMessage(new ServerMessage(Lng.astr("This Sector is Protected!"), ServerMessage.MESSAGE_TYPE_WARNING, ps.getId()));
						}
					}
				}
				return false;
			}
		} else {
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getSectorId());
			if (sendable != null && sendable instanceof RemoteSector) {
				return !((RemoteSector) sendable).isProtectedClient();
			}
		}
		return true;
	}

	/**
	 * @return the hitPoints
	 */
	public float getHitPoints() {
		return hitPoints;
	}

	/**
	 * @param hitPoints the hitPoints to set
	 */
	public void setHitPoints(float hitPoints) {
		this.hitPoints = hitPoints;
	}

	/**
	 * @return the destroyed
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * @param destroyed the destroyed to set
	 */
	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}

//	@Override
//	public int handleBeamDamage(
//			BeamState beam, int beamHits,
//			BeamHandlerContainer<? extends SimpleTransformableSendableObject> owner,
//			Vector3f from, Vector3f to,
//			CubeRayCastResult cubeResult, boolean ignoreShields, Timer timer) {
//		if (!canHit(owner)) {
//			return 0;
//		}
//
//		if (beamHits > 0) {
//
//			float damage = (int) (beamHits * beam.getPower());
//			hitPoints = ((getHitPoints() - damage));
//			onDamage(owner);
//		}
//
//		return beamHits;
//	}

	public void setFactionAll(int factionId) {
		boolean changed = factionId != getFactionId();

		if (changed) {
			setFactionId(factionId);
			synchronized (getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof Planet && ((Planet) s).getPlanetCoreUID().equals(getUniqueIdentifier())) {
						if (((Planet) s).getElementClassCountMap().get(ElementKeyMap.FACTION_BLOCK) == 0 ||
								!((FactionState) getState()).getFactionManager().existsFaction(((Planet) s).getFactionId())) {
							//only change plates that have no active faction block
							((Planet) s).setFactionId(factionId);
						}
					}
				}
			}
		}

	}

	@Override
	public void sendHitConfirm(byte damageType) {
		
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#isSegmentController()
	 */
	@Override
	public boolean isSegmentController() {
				return false;
	}

	@Override
	public String getName() {
		return toNiceString();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();

		// Update map for deleted planet core
		Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
		Vector3i sysPos = StellarSystem.getPosFromSector(new Vector3i(sector.pos), new Vector3i());
		((GameServerState) getState()).getGameMapProvider().updateMapForAllInSystem(sysPos);
	}

	@Override
	public void newNetworkObject() {
		networkPlanetCore = new NetworkPlanetCore(getState());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.space.FixedSpaceEntity#initPhysics()
	 */
	@Override
	public void initPhysics() {
		super.initPhysics();
		if (getPhysicsDataContainer().getObject() == null) {
			Transform t = getRemoteTransformable().getInitialTransform();

			h = new Dodecahedron(radius);
			h.create();
			ObjectArrayList<Vector3f> points = new ObjectArrayList<Vector3f>();

			for (int i = 0; i < 12; i++) {
				Vector3f[] polygon = h.getPolygon(i);
				Collections.addAll(points, polygon);
			}
			DodecahedronShapeExt root = new DodecahedronShapeExt(points, this);

			root.dodecahedron = h;
			Transform local = new Transform();
			local.setIdentity();

			getPhysicsDataContainer().setShape(root);
			getPhysicsDataContainer().setInitial(t);

			RigidBody bodyFromShape = getPhysics().getBodyFromShape(root, getMass(), getPhysicsDataContainer().initialTransform);

			//			System.err.println("[ENTITY] "+getState()+" "+this+" initialized physics -> "+t.origin);

			bodyFromShape.setUserPointer(getId());

			getPhysicsDataContainer().setObject(bodyFromShape);

			getWorldTransform().set(t);
			assert (getPhysicsDataContainer().getObject() != null);
		} else {
			System.err.println("[SegmentController][WARNING] not adding initial physics object. it already exists");
		}
		setFlagPhysicsInit(true);
	}

	@Override
	public String toNiceString() {
//		if(System.currentTimeMillis() - lastCheck > 5000){
//			synchronized(getState().getLocalAndRemoteObjectContainer().getLocalObjects()){
//				for(Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()){
//					if(s instanceof Planet && ((Planet)s).getPlanetCoreUID().equals(getUniqueIdentifier()) && ((Planet)s).getRealName().length() > 0 && !((Planet)s).getRealName().toLowerCase(Locale.ENGLISH).equals("planet")){
//						this.realName = ((Planet)s).getRealName();
//					}
//				}
//			}
//			lastCheck = System.currentTimeMillis();
//		}

		return Lng.str("Planet Core %s [%s HP]", realName, StringTools.formatSeperated((int) hitPoints));
	}
	private final Vector3f minOut = new Vector3f();
	private final Vector3f maxOut = new Vector3f();
	private final Vector3f minOutC = new Vector3f();
	private final Vector3f maxOutC = new Vector3f();
	private final Vector3f[] maxOutCorners = new Vector3f[8];
	Transform aTemp = new Transform();
	Transform bTemp = new Transform();
	CubeRayCastResult r = new CubeRayCastResult(new Vector3f(), new Vector3f(), null);
	{
		for(int i = 0; i < maxOutCorners.length; i++){
			maxOutCorners[i] = new Vector3f();
		}
		aTemp.setIdentity();
		bTemp.setIdentity();
	}

	Vector3f tPos = new Vector3f();
	public boolean occludes(SegmentBuffer sb, Vector3f minSBBBOut, Vector3f maxSBBBOut, Vector3f minSBBBOutC, Vector3f maxSBBBOutC) {
		
		Transform t = sb.getSegmentController().getWorldTransformOnClient();
		minOut.set(sb.regionBlockStart.x-8, sb.regionBlockStart.y-8, sb.regionBlockStart.z-8);
		maxOut.set(sb.regionBlockEnd.x-8, sb.regionBlockEnd.y-8, sb.regionBlockEnd.z-8);

//		DrawableRemoteSegment.transformAabb(minOut, maxOut, 0, t, minOutC, maxOutC);

		maxOutCorners[0].set(minOut.x, minOut.y, minOut.z);
		maxOutCorners[1].set(maxOut.x, minOut.y, minOut.z);
		maxOutCorners[2].set(minOut.x, maxOut.y, minOut.z);
		maxOutCorners[3].set(minOut.x, minOut.y, maxOut.z);
		
		maxOutCorners[4].set(maxOut.x, maxOut.y, minOut.z);
		maxOutCorners[5].set(maxOut.x, minOut.y, maxOut.z);
		maxOutCorners[6].set(minOut.x, maxOut.y, maxOut.z);
		maxOutCorners[7].set(maxOut.x, maxOut.y, maxOut.z);
		
		
		tPos.set(getWorldTransformOnClient().origin);
//		tPos.x -= SegmentData.SEG_HALF;
//		tPos.y -= SegmentData.SEG_HALF;
//		tPos.z -= SegmentData.SEG_HALF;
		if(sb.getSegmentController() instanceof Planet && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
			DebugDrawer.boxes.add(new DebugBox(new Vector3f(maxOutCorners[0]), new Vector3f(maxOutCorners[7]), t, 1, 1, 0, 1));
			Dodecahedron.debug = true;
		}
		for(int i = 0; i < maxOutCorners.length; i++){
			
			t.transform(maxOutCorners[i]); //do NOT use AABB as it may intersect with the dodeca
			
//			System.err.println("FROM "+aTemp.origin +" -> "+bTemp.origin+": "+r.hasHit());
			
//			Vector3f cPos = ((GameClientState)getState()).getCharacter() != null ? ((GameClientState)getState()).getCharacter().getWorldTransformOnClient().origin : Controller.getCamera().getPos();
			Vector3f cPos = Controller.getCamera().getPos();
//			if(Dodecahedron.debug){
//				DebugDrawer.lines.add(new DebugLine(new Vector3f(cPos), new Vector3f(maxOutCorners[i]), new Vector4f( 0,0,1,0.7f)));
//			}
			
			
//			if(!h.testRay(tPos, Controller.getCamera().getPos(), maxOutCorners[i])){
			if(!h.testRay(tPos, cPos, maxOutCorners[i])){
				return false;
			}
			
		}
		Dodecahedron.debug = false;
//		System.err.println("OCCLUDES REGION: "+sb);
		
		//rays to all corners of the BoundingBox didn't collide with the core
		return true;
	}

	@Override
	public void sendClientMessage(String str, byte type) {
				
	}
	@Override
	public TopLevelType getTopLevelType(){
		return TopLevelType.OTHER_SPACE;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
				return null;
	}

	@Override
	public byte getFactionRights() {
		return 0;
	}
	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}
	@Override
	public byte getOwnerFactionRights() {
		return 0;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	private final DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerPlanetCore();
	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}
	public boolean canBeDamagedBy(Damager from, DamageDealerType beam) {
		return true;
	}
}
