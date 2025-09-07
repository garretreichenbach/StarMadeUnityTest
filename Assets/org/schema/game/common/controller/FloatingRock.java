package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.gui.shiphud.newhud.ColorPalette;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandler;
import org.schema.game.common.controller.damage.beam.DamageBeamHitHandlerSegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.beam.repair.RepairBeamHandler;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.*;
import org.schema.game.common.controller.generator.DynamicAsteroidCreatorThread;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector4f;
import java.util.Random;

import static org.schema.game.common.controller.FloatingRock.RockTemperature.*;
import static org.schema.game.common.data.element.ElementKeyMap.*;

public class FloatingRock extends EditableSendableSegmentController implements TransientSegmentController {

	boolean converting = false;
	ElementSynchedInfo synchedInfo;
	private boolean touched;
	private boolean moved;
	private boolean checkEmpty;
	public Vector3i loadedMinPos;
	public Vector3i loadedMaxPos;
	public Vector3i loadedGenSize;
	private final AsteroidOuterMaterialProvider materials;
	private final RockTemperature temperature;
	private byte[] ores; //by orientation ID, not element ID
	private byte[] oreFrequencies; //should be the same length as ores

	public static final AsteroidOuterMaterialProvider FALLBACK_MATERIAL = new AsteroidOuterMaterialProvider() {
		@Override
		public short[] getMainMaterials(RockTemperature t) {
			return new short[]{ElementKeyMap.TERRAIN_ROCK_WHITE};
		}

		@Override
		public short[] getSpeckleMaterials(RockTemperature t) {
			return new short[0];
		}

		@Override
		public TerrainStructureList[] getStructures(RockTemperature t) {
			return new TerrainStructureList[0];
		}
	};
	public static final byte[] COMMON_ORES = new byte[]{6, 15};
	public static final byte[] FALLBACK_ORES_FREQ = new byte[]{127,127};

	public FloatingRock(StateInterface state) {
		super(state);
		materials = FALLBACK_MATERIAL;
		temperature = TEMPERATE;
		setOres(COMMON_ORES,
				FALLBACK_ORES_FREQ);

	}

	@Deprecated //testing only; should always provide materials (star system should determine material rarity and select which to use)
	public FloatingRock(StateInterface state, AsteroidOuterMaterialProvider rockMaterials) {
		super(state);
		materials = rockMaterials;
		temperature = TEMPERATE;
		setOres(COMMON_ORES,
				new byte[]{127,127});
	}

	/**
	 * Create a new asteroid.
	 * @param state The current game state
	 * @param rockMaterials
	 * @param ores which ores to include, in terms
	 * @param oreRates frequency of ores from 0 to 127
	 */
	public FloatingRock(StateInterface state, AsteroidOuterMaterialProvider rockMaterials, RockTemperature temperature, byte[] ores, byte[] oreRates) {
		super(state);
		materials = rockMaterials;
		this.temperature = temperature;
		setOres(ores,oreRates);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.Damager#getPlayerState()
	 */
	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}
	@Override
	public void sendHitConfirm(byte damageType) {
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#getType()
	 */
	@Override
	public EntityType getType() {
		return EntityType.ASTEROID;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		if (((GameStateInterface) getState()).isPhysicalAsteroids()) {
			setMass(0.1f);
		} else {
			setMass(0f);
		}
	}

	@Override
	public String getAdditionalObjectInformation() {
		String r = "";
		if(!isOnServer()){
			SimpleTransformableSendableObject<?> cc = ((GameClientState)getState()).getCurrentPlayerObject();
			if(cc instanceof SegmentController){
				SegmentController c = (SegmentController)cc;
				if(cc.getReconStrengthRaw() > 0 && c.getConfigManager().apply(StatusEffectType.ORE_SCANNER, false)){
					r = "\n"+getElementClassCountMap().getResourceString();
				}
			}
				
		}
		return r+Lng.str("\n%s Blocks Total",getTotalElements());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#allowedType(short)
	 */
	@Override
	public boolean allowedType(short type) {
//        boolean canPlace = type != ElementKeyMap.BUILD_BLOCK_ID;
//        if (!canPlace && !isOnServer()) {
//            ((GameClientState)getState()).getController().popupAlertTextMessage(
//                    "ERROR\n" +
//                            "Build blocks cannot be placed on\n" +
//                            "Asteroids", 0);
//        }
		return /*canPlace && */super.allowedType(type);
	}

	@Override
	protected String getSegmentControllerTypeString() {
		return "Asteroid";
	}

//	/* (non-Javadoc)
//	 * @see org.schema.game.common.controller.EditableSendableSegmentController#handleHit(javax.vecmath.Vector3f, com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback, org.schema.schine.network.objects.Sendable, float)
//	 */
//	@Override
//	public ParticleHitCallback handleHit(ParticleHitCallback callback,
//	                                     Damager from, float damage, float damageBeforeShield, Vector3f startPos, Vector3f endPos, boolean shieldAbsorbed, long weaponId) {
//		super.handleHit(callback, from, damage, damageBeforeShield, startPos, endPos, shieldAbsorbed, weaponId);
//		if (isOnServer() && getTotalElements() <= 0) {
//			System.err.println("[FLOATINGROCK] DESTROYING " + this + " -> TOTAL ELEMENTS: " + getTotalElements());
//			destroy();
//		}
//		return callback;
//	}

	

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#isEmptyOnServer()
	 */
	@Override
	public boolean isEmptyOnServer() {

		if (!touched) {
	        /*
             * untouched asteroids are alwas not-empty. checking this will prevent
			 * the server from loading in every single asteroid, which puts
			 * immense load on the server (Simplex)
			 */
			return false;
		}

		return super.isEmptyOnServer();
	}

	@Override
	public void onAddedElementSynched(short type, byte orientation, byte x, byte y, byte z, Segment segment, boolean updateSegementBuffer, long absIndex, long time, boolean revalidate) {
		super.onAddedElementSynched(type, orientation, x, y, z, segment, updateSegementBuffer, absIndex, time, revalidate);

		if (isOnServer() && !(this instanceof FloatingRockManaged) && !segment.getSegmentData().isRevalidating()) {
			//WARNING this instanceof FloatingRockManaged is needed. else the managed floating rock will also convert
			synchedInfo = new ElementSynchedInfo(type, x, y, z, orientation, segment, updateSegementBuffer, absIndex, time);
			converting = true;
			System.err.println("[SERVER][FloatingRock] Converting " + this + " to managed asteroid");
			destroy();
		}
	}

	@Override
	protected void onCoreDestroyed(Damager from) {

	}

	@Override
	public void startCreatorThread() {
		if (getCreatorThread() == null) {

			setCreatorThread(new DynamicAsteroidCreatorThread(this));
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#toString()
	 */
	@Override
	public String toString() {
		return "Asteroid(" + getId() + ")sec[" + getSectorId() + "]" + (touched ? "(!)" : "");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.EditableSendableSegmentController#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		if (isOnServer() && getTotalElements() <= 0 &&
				System.currentTimeMillis() - getTimeCreated() > 50000
				&& isEmptyOnServer()) {
			System.err.println("[SERVER][FloatingRock] Empty rock: deleting!!!!!!!!!!!!!!!!!!! " + this);
			this.setMarkedForDeleteVolatile(true);
		}

		if (!getDockingController().isDocked() && flagUpdateMass) {
			if (isOnServer()) {

				boolean updateMass = updateMassServer();
				if (updateMass) {
					flagUpdateMass = false;
				}
				getPhysicsDataContainer().updateMass(getMass(), true);
			} else {
				getPhysicsDataContainer().updateMass(getMass(), true);
				flagUpdateMass = false;
			}
		}
		if (isOnServer() && !isMoved() && !getWorldTransform().origin.equals(getInitialTransform().origin)) {
			System.err.println("[SEGMENTCONTROLLER] FLOATING ROCK HAS MOVED " + this);
			setMoved(true);
		}

		if (isOnServer() && checkEmpty) {
			if (getTotalElements() <= 0) {
				destroy();
			}
			checkEmpty = false;
		}

		Starter.modManager.onSegmentControllerUpdate(this);
	}

	@Override
	public boolean hasStructureAndArmorHP() {
		return false;
	}

	//INSERTED CODE
	//Custom asteroid name system
	public void setModCustomNameServer(String customName){
		//TODO Not the greatest way to do this, find out how to make a field that gets synced over the network later.
		this.setRealName(customName);
		this.getNetworkObject().realName.set(customName);
	}
	///

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#getRealName()
	 */
	@Override
	public String getRealName() {
		//INSERTED CODE
		if(!realName.equals("undef")) return realName;
		///
		return Lng.str("Asteroid");
//		StringBuilder content = new StringBuilder();
//        for (int i = 0, lastPos = ores.length - 1; i <= lastPos; i++) {
//            content.append(ElementKeyMap.getInfo(resources[ores[i]]).getName());
//			if(i != lastPos) content.append(", ");
//        }
//		return Lng.str("Asteroid (Rich in %s)", content.toString());
	}

	@Override
	public boolean updateMassServer() {
		if (isOnServer()) {
			float mass = Math.max(0.01f, getTotalPhysicalMass());
			setMass(mass);
			boolean updateMass = getPhysicsDataContainer().updateMass(mass, false);
			//			if(updateMass){

			//				System.err.println("[UPDATEMASS][SHIP] MASS IS NOW "+mass+" on "+getState()+" "+this);
			//			}
			return updateMass;
		}
		return true;
	}

	protected void copyManaged() {
		final FloatingRockManaged managedRock = new FloatingRockManaged(this.getState());

		// Set new asteroid's data with current asteroid's
		managedRock.setId(((ServerStateInterface)getState()).getNextFreeObjectId());
		managedRock.setSectorId(getSectorId());
		managedRock.initialize();
		managedRock.getMinPos().set(getMinPos());
		managedRock.getMaxPos().set(getMaxPos());
		managedRock.setTouched(isTouched(), false);
		managedRock.setRealName(getRealName());
		managedRock.setFactionId(getFactionId());
		managedRock.setLastModifier(getLastModifier());
		managedRock.setSpawner(getSpawner());
		managedRock.setSurfaceMaterials(getSurfaceMaterials());
		if (getUniqueIdentifier().startsWith(EntityType.ASTEROID.dbPrefix)) {
			String s = EntityType.ASTEROID_MANAGED.dbPrefix + "_" +
					getUniqueIdentifier().substring(EntityType.ASTEROID.dbPrefix.length());
			managedRock.setUniqueIdentifier(s);
		} else {
			throw new RuntimeException("[ERROR] Bad asteroid unique identifier");
		}
		managedRock.setCreatorId(getCreatorId());

		// Point all segment controllers to new asteroid
		SegmentBufferManager segBuf = (SegmentBufferManager) getSegmentBuffer();
		for (SegmentBufferInterface sb : segBuf.getBuffer().values()) {
			sb.setSegmentController(managedRock);
		}
		segBuf.iterateOverNonEmptyElement((s, lastChanged) -> {
			s.setSegmentController(managedRock);
			return true;
		}, false);
		segBuf.setSegmentController(managedRock);
		managedRock.setSegmentBuffer(segBuf);

		managedRock.getRemoteTransformable().getInitialTransform().setIdentity();
		managedRock.getRemoteTransformable().getInitialTransform().origin.set(getInitialTransform().origin);

		managedRock.getRemoteTransformable().getWorldTransform().setIdentity();
		managedRock.getRemoteTransformable().getWorldTransform().origin.set(getWorldTransform().origin);
		if (getSeed() == 0) {
			managedRock.setSeed(Universe.getRandom().nextLong());
		} else {
			managedRock.setSeed(getSeed());
		}

		managedRock.setTouched(true, false);
		managedRock.setChangedForDb(true);
		
		managedRock.getElementClassCountMap().add(getElementClassCountMap());

		// Workaround for managed asteroid not getting onAddedElementSynched call for the first block placed
		managedRock.onAddedElementSynched(synchedInfo.type, synchedInfo.orientation, synchedInfo.x, synchedInfo.y, synchedInfo.z, synchedInfo.segment,
				synchedInfo.updateSegmentBuffer, synchedInfo.absIndex, synchedInfo.time, false);

		managedRock.totalPhysicalMass = this.totalPhysicalMass;
		// Set total elements, update mass, etc
		managedRock.setTotalElements(getTotalElements());
		managedRock.setOres(ores,oreFrequencies);

		((GameServerState) getState()).getController().getSynchController().addImmediateSynchronizedObject(managedRock);
		try {
			((GameServerState) getState()).getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] getOres() {
		return ores;
	}

	public byte[] getOreFrequencies() {
		return oreFrequencies;
	}

	protected void setOres(byte[] ores,byte[] freq) {
		if(ores.length != freq.length) throw new IllegalArgumentException("Mismatched number of ores and frequency values!");
		this.ores = ores;
		this.oreFrequencies = freq;
	}

	@Override
	public void destroyPersistent() {
		super.destroyPersistent();
		if (converting) {
			setTouched(true, false);
			if (!(this instanceof FloatingRockManaged) && isOnServer() && getUniqueIdentifier() != null && getTotalElements() > 0) {
				copyManaged();
			}
			converting = false;
		}
	}
	@Override
	protected Tag getExtraTagData() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				loadedMinPos != null ? new Tag(Type.VECTOR3i, null, loadedMinPos) : new Tag(Type.BYTE, null, (byte)0), 
				loadedMinPos != null ? new Tag(Type.VECTOR3i, null, loadedMaxPos) : new Tag(Type.BYTE, null, (byte)0), 
				loadedGenSize != null ? new Tag(Type.VECTOR3i, null, loadedGenSize) : new Tag(Type.BYTE, null, (byte)0), 
				FinishTag.INST,
		});
	}
	@Override
	protected void readExtraTagData(Tag t) {
		if (t.getType() == Type.STRUCT) {
			
			Tag[] ts = (Tag[]) t.getValue();
			
			if(ts[0].getType() == Type.VECTOR3i){
				loadedMinPos = (Vector3i) ts[0].getValue();
			}
			if(ts[1].getType() == Type.VECTOR3i){
				loadedMaxPos = (Vector3i) ts[1].getValue();
			}
			
			if(ts.length > 2 && ts[2].getType() == Type.VECTOR3i){
				loadedGenSize = (Vector3i) ts[2].getValue();
			}
			
		}
	}
	@Override
	public String toNiceString() {
		if (!isOnServer()) {
			Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(getSectorId());
			if (sendable != null) {
				return Lng.str("Rock %s", ((RemoteSector) sendable).clientPos().toString());
			}

		}
		return "Floating Rock <can be harvested>";
	}

	@Override
	public boolean isRepariableFor(RepairBeamHandler harvester, String[] cannotHitReason, Vector3i position) {
		cannotHitReason = new String[] {Lng.str("Astroids cannot be repaired")};
		return false;
	}

	@Override
	public boolean isSalvagableFor(Salvager harvester, String[] cannotHitReason, Vector3i position) {
		AbstractOwnerState p = harvester.getOwnerState();
		if(harvester.getFactionId() == getFactionId() && 
				((p != null && p instanceof PlayerState && !allowedToEdit((PlayerState) p)) || harvester.getOwnerFactionRights() < getFactionRights())){
			cannotHitReason[0] = Lng.str("Cannot salvage: insufficient faction rank");
			return false;
		}
		return true;
	}

	@Override
	public boolean isTouched() {
		return touched;
	}

	@Override
	public void setTouched(boolean b, boolean checkEmpty) {
		if (b != this.touched) {
			setChangedForDb(true);
		}
		this.touched = b;

		this.checkEmpty = checkEmpty;
		//moved is true if touched
		setMoved(b);

	}

	@Override
	public boolean isMoved() {
		return moved;
	}

	@Override
	public void setMoved(boolean b) {
		if (b != this.moved) {
			setChangedForDb(true);
		}

		if (b && !moved && isOnServer()) {
			Sector sector = ((GameServerState) getState()).getUniverse().getSector(getSectorId());
			if (sector != null) {
				sector.setTransientSector(false);
			}
		}
		this.moved = b;
	}

	@Override
	public boolean needsTagSave() {
		return false;
	}

	public AsteroidOuterMaterialProvider getSurfaceMaterials() {
		return materials;
	}

	public RockTemperature getTemperatureLevel() {
		return temperature;
	}

	private class ElementSynchedInfo {
		short type;
		byte x;
		byte y;
		byte z;
		byte orientation;
		Segment segment;
		boolean updateSegmentBuffer;
		long absIndex;
		long time;

		public ElementSynchedInfo(short type, byte x, byte y, byte z, byte orientation, Segment segment, boolean updateSegmentBuffer, long absIndex, long time) {
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
			this.segment = segment;
			this.updateSegmentBuffer = updateSegmentBuffer;
			this.absIndex = absIndex;
			this.time = time;
			this.orientation = orientation;
		}
	}

	public Vector3i getLoadedOrGeneratedMinPos() {
		if(loadedMinPos == null){
			//fallback for asteroids that havent had the feature
			loadedMinPos = new Vector3i(getMinPos());
		}
		return loadedMinPos;
	}

	public Vector3i getLoadedOrGeneratedMaxPos() {
		if(loadedMaxPos == null){
			//fallback for asteroids that havent had the feature
			loadedMaxPos = new Vector3i(getMaxPos());
		}
		return loadedMaxPos;
	}
	public Vector3i getLoadedOrGeneratedSizeGen() {
		if(loadedGenSize == null){
			//fallback for asteroids that havent had the feature
			int max = ServerConfig.ASTEROID_RADIUS_MAX.getInt();
			Random r = new Random(getSeed());
			//this is guaranteed to be the same size as it was on first generation
			loadedGenSize = new Vector3i(
					Sector.rockSize + r.nextInt(max),
					Sector.rockSize + r.nextInt(max), 
					Sector.rockSize + r.nextInt(max));
		}
		return loadedGenSize;
	}
	@Override
	public void getRelationColor(RType relation, boolean sameFaction, Vector4f out, float select, float pulse) {
		switch(relation) {
			case ENEMY -> out.set(ColorPalette.enemyAsteroid);
			case FRIEND -> out.set(ColorPalette.allyAsteroid);
			case NEUTRAL -> out.set(ColorPalette.neutralAsteroid);
		}
		if(sameFaction) {
			out.set(ColorPalette.factionAsteroid);
		}
		out.x += select;
		out.y += select;
		out.z += select;
	}
	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}
	private DamageBeamHitHandler damageBeamHitHandler = new DamageBeamHitHandlerSegmentController();
	public DamageBeamHitHandler getDamageBeamHitHandler() {
		return damageBeamHitHandler;
	}

	@Override
	public SendableType getSendableType() {
		return SendableTypes.FLOATING_ROCK;
	}

	public enum RockTemperature{
		COLD,
		TEMPERATE,
		HOT;

		public static RockTemperature fromFloat(float temperature) {
			if(temperature < 0.4) return COLD;
			else if(temperature > 0.8) return HOT;
			else return TEMPERATE;
		}
	}

	public interface AsteroidOuterMaterialProvider{ //needs to be an interface for modding capability
		/**
		 * @return the ID of the blocks to potentially use as the main outer shell of an asteroid
		 */
		public short[] getMainMaterials(RockTemperature t);
		/**
		 * @return the IDs of the blocks to potentially use as 'speckles' on an asteroid surface, e.g. lava. A value of -1 indicates no speckles as an option. This may be an empty array.
		 */
		public short[] getSpeckleMaterials(RockTemperature t);
		/**
		 * @return the set of structures to use on the asteroid. This may be (and, in practice, often is) an empty array.
		 */
		public TerrainStructureList[] getStructures(RockTemperature t); //TODO wrong data type; terrain structure lists want coordinates and such, and they are not iterable. they're meant for structure placement only
	}

	public enum StandardAsteroidMaterials implements AsteroidOuterMaterialProvider{
		COOL_COLORS,
		PURPLE_STUFF,
		WARM_COLORS,
		NEUTRALS,
		SANDY,
		DARK;

		@Override
		public short[] getMainMaterials(RockTemperature t) {
			switch (this) {
				case COOL_COLORS:
					return switch(t){
						case COLD -> s(TERRAIN_ICEPLANET_ROCK,TERRAIN_ICE_ID,TERRAIN_ROCK_WHITE,TERRAIN_ROCK_BLUE,TERRAIN_ROCK_PURPLE);
						case TEMPERATE -> s(TERRAIN_ROCK_NORMAL,TERRAIN_ROCK_WHITE,TERRAIN_ROCK_BLUE,TERRAIN_ROCK_GREEN);
						case HOT -> s(TERRAIN_ROCK_MARS,TERRAIN_ROCK_BLACK);
					};
				case PURPLE_STUFF:
					return switch(t){
						case COLD -> s(TERRAIN_ROCK_BLACK,TERRAIN_ROCK_PURPLE,TERRAIN_ROCK_BLUE);
						case TEMPERATE -> s(TERRAIN_PURPLE_ALIEN_ROCK,TERRAIN_PURPLE_ALIEN_TOP,TERRAIN_PURPLE_ALIEN_VINE,TERRAIN_ROCK_PURPLE,TERRAIN_ROCK_MARS);
						case HOT -> s(TERRAIN_ROCK_GREEN,TERRAIN_ROCK_PURPLE,TERRAIN_PURPLE_ALIEN_ROCK);
					};
				case WARM_COLORS:
					return switch(t){
						case COLD -> s(TERRAIN_DIRT_ID,TERRAIN_ROCK_NORMAL,TERRAIN_ROCK_YELLOW);
						case TEMPERATE -> s(TERRAIN_ROCK_ORANGE,TERRAIN_ROCK_RED,TERRAIN_MARS_DIRT,TERRAIN_SAND_ID);
						case HOT -> s(TERRAIN_ROCK_BLACK,TERRAIN_MARS_DIRT);
					};
				case NEUTRALS:
					return switch(t){
						case COLD -> s(TERRAIN_ROCK_NORMAL,TERRAIN_ROCK_WHITE);
						case TEMPERATE -> s(TERRAIN_ROCK_GREEN,TERRAIN_DIRT_ID);
						case HOT -> s(TERRAIN_ROCK_BLACK,TERRAIN_ROCK_YELLOW);
					};
				case SANDY:
					return switch(t){
						case COLD -> s(TERRAIN_ROCK_NORMAL,TERRAIN_ICEPLANET_ROCK);
						case TEMPERATE -> s(TERRAIN_ROCK_YELLOW,TERRAIN_SAND_ID,TERRAIN_DIRT_ID);
						case HOT -> s(TERRAIN_MARS_DIRT,TERRAIN_ROCK_RED,TERRAIN_SAND_ID);
					};
				case DARK:
					return switch(t){
						case COLD -> s(TERRAIN_ROCK_MARS,TERRAIN_ROCK_BLACK);
						case TEMPERATE -> s(TERRAIN_ROCK_BLACK,TERRAIN_ROCK_PURPLE);
						case HOT -> s(TERRAIN_ROCK_RED,TERRAIN_DIRT_ID,TERRAIN_ROCK_BLACK);
					};
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public short[] getSpeckleMaterials(RockTemperature t) {
			switch (this) {
				case COOL_COLORS:
					return switch(t){
						case COLD -> s((short)-1,TERRAIN_ICE_ID,TERRAIN_ICEPLANET_CRYSTAL,CRYS_BLUE);
						case TEMPERATE -> s((short)-1,CRYS_WHITE);
						case HOT -> s();
					};
				case PURPLE_STUFF:
					return switch(t){
						case COLD -> s((short)-1,CRYS_BLACK,TERRAIN_ROCK_GREEN);
						case TEMPERATE -> s((short)-1,TERRAIN_ICEPLANET_CRYSTAL,CRYS_RED);
						case HOT -> s((short)-1,CRYS_ORANGE);
					};
				case WARM_COLORS:
					return switch(t){
						case COLD -> s((short)-1,(short)-1,CRYS_YELLOW);
						case TEMPERATE -> s();
						case HOT -> s((short)-1,TERRAIN_LAVA_ID,TERRAIN_ROCK_MARS);
					};
				case NEUTRALS:
					return switch(t){
						case COLD -> s((short)-1,CRYS_WHITE,TERRAIN_ICE_ID);
						case TEMPERATE -> s((short)-1,(short)-1,(short)-1,TERRAIN_SAND_ID,TERRAIN_SAND_ID,CRYS_GREEN,TERRAIN_ROCK_NORMAL);
						case HOT -> s((short)-1,TERRAIN_LAVA_ID,TERRAIN_ROCK_MARS);
					};
				case SANDY:
					return switch(t){
						case COLD -> s((short)-1,(short)-1,TERRAIN_ICE_ID,CRYS_YELLOW);
						case TEMPERATE -> s((short)-1,(short)-1,TERRAIN_ROCK_NORMAL);
						case HOT -> s((short)-1,TERRAIN_LAVA_ID);
					};
				case DARK:
					return switch(t){
						case COLD -> s(CRYS_BLACK);
						case TEMPERATE -> s((short)-1,(short)-1,CRYS_PURPLE,CRYS_BLUE);
						case HOT -> s(TERRAIN_LAVA_ID,TERRAIN_MARS_DIRT,CRYS_YELLOW);
					};
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public TerrainStructureList[] getStructures(RockTemperature t) {
			switch (this) {
				case COOL_COLORS:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts();
						case HOT -> ts();
					};
				case PURPLE_STUFF:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts();
						case HOT -> ts();
					};
				case WARM_COLORS:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts();
						case HOT -> ts();
					};
				case NEUTRALS:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts();
						case HOT -> ts();
					};
				case SANDY:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts(); //TODO space tumbleweeds of purple alien vine stuff
						case HOT -> ts();
					};
				case DARK:
					return switch(t){
						case COLD -> ts();
						case TEMPERATE -> ts();
						case HOT -> ts();
					};
				default:
					throw new IllegalArgumentException();
			}
		}

		short[] s(short... v){
			return v;
		}

		TerrainStructureList[] ts(TerrainStructureList... v){
			return v;
		}
	}
}
