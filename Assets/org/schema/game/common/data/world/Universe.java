package org.schema.game.common.data.world;

import api.listener.events.world.generation.GalaxyInstantiateEvent;
import api.listener.events.world.generation.SystemPreGenerationEvent;
import api.listener.events.world.sector.SectorUnloadEvent;
import api.listener.events.world.sector.SegmentControllerUnloadEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.*;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.DebugControlManager;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.data.DebugServerPhysicalObject;
import org.schema.game.common.data.EntityFileTools;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.chat.ChannelRouter;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.physics.RigidBodySegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.player.faction.config.FactionSystemOwnerBonusConfig;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GalaxyTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.game.server.data.simulation.npc.geo.StarSystemResourceRequestContainer;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteSerializableObject;
import org.schema.schine.physics.Physics;
import org.schema.schine.physics.PhysicsState;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.io.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Universe implements TagSerializable {

	public static final int SECTOR_GENERATION_LENGTH = 5;
	public static final float SECTOR_MARGIN = 300;
	private static final int MAX_PHYSICS_REPOSITORY_SIZE = 30;
	public static boolean resetMinableAndVulnerable = ServerConfig.HOST_NAME_TO_ANNOUNCE_TO_SERVER_LIST.getString().startsWith("play.star-made.org");
	private static Random random;
	//TODO: warning: might be a memory leak longterm (but only if a server runs for a very very long time)
	public final Object2LongOpenHashMap<String> writeMap = new Object2LongOpenHashMap<String>();
	public final ObjectArrayFIFOQueue<Vector3i> ftlDirty = new ObjectArrayFIFOQueue<Vector3i>();
	public final LongArrayFIFOQueue tradeNodesDirty = new LongArrayFIFOQueue();
	public final Object2LongOpenHashMap<Vector3i> attackSector = new Object2LongOpenHashMap<Vector3i>();
	private final Object2ObjectOpenHashMap<Vector3i, VoidSystem> starSystemMap = new Object2ObjectOpenHashMap<Vector3i, VoidSystem>();
	private final Object2ObjectOpenHashMap<Vector3i, Galaxy> galaxyMap = new Object2ObjectOpenHashMap<Vector3i, Galaxy>();
	private final Int2ObjectOpenHashMap<Sector> sectors;
	private final Object2ObjectOpenHashMap<Vector3i, Sector> sectorPositions;
	private final GameServerState state;
	private final ObjectArrayFIFOQueue<SendableSegmentController> toClear = new ObjectArrayFIFOQueue<SendableSegmentController>();
	private final List<Physics> physicsRepository = new ObjectArrayList<Physics>();
	private final Vector3i where = new Vector3i();
	private final Vector3f otherSecCenter = new Vector3f();
	private final Vector3f nearVector = new Vector3f();
	private final Transform out = new Transform();
	private final Transform ttmp = new Transform();
	private final Matrix3f rot = new Matrix3f();
	private final Vector3f bb = new Vector3f();
	private final Vector3f dist = new Vector3f();
	private final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();
	private final GalaxyManager galaxyManager;
	Vector3f tmpObjectPos = new Vector3f();
	Vector3i belogingVector = new Vector3i();
	Transform std = new Transform();
	Vector3i otherSecAbs = new Vector3i();
	//	private StarSystem loadOrGenerateStarSystem(Vector3i systemPos) throws IOException {
	//
	//		File dir = new FileExt(GameServerState.ENTITY_DATABASE_PATH);
	//		System.out.println("[SERVER] LOADING SYSTEM... "+systemPos);
	//		assert(dir.exists() && dir.isDirectory());
	//
	//
	//		String fileName = GameServerState.ENTITY_DATABASE_PATH+"/"+getSystemFileName(systemPos);
	//		Object lock = null;
	//		synchronized(state.fileLocks){
	//			lock = state.fileLocks.get(fileName);
	//			if(lock == null){
	//				lock = new Object();
	//				state.fileLocks.put(fileName, lock);
	//			}
	//		}
	//
	//		synchronized(lock){
	//			return loadStarSystemFromFile(fileName, systemPos);
	//		}
	//
	//	}
	Random systemRandom = new Random();
	private Int2ObjectOpenHashMap<VirtualEntityAttachment> virtualMap = new Int2ObjectOpenHashMap<VirtualEntityAttachment>();
	private String name;
	private ObjectOpenHashSet<Sector> inactiveWrittenSectors = new ObjectOpenHashSet<Sector>();
	private ObjectOpenHashSet<Sector> entityCleaningSectors = new ObjectOpenHashSet<Sector>();
	private long lastPing;
	private long seed;
	private List<DeferredLoad> deffereedLoads = new ObjectArrayList<DeferredLoad>();
	private long lastTime;
	private long lastAttackCheck;

	public Universe(GameServerState state) {
		if(GameServerState.ENTITY_DATABASE_PATH == null) {
			random = new Random();
			System.err.println("NO UNIVERSE PATH GIVEN. RETURN");
			sectors = new Int2ObjectOpenHashMap<Sector>();
			sectorPositions = new Object2ObjectOpenHashMap<Vector3i, Sector>();
			galaxyManager = new GalaxyManager(state);
			this.state = state;
		} else {
			sectors = new Int2ObjectOpenHashMap<Sector>();
			sectorPositions = new Object2ObjectOpenHashMap<Vector3i, Sector>();
			galaxyManager = new GalaxyManager(state);
			this.state = state;

			VirtualEntityAttachment.universe = this;
			seed = 0;
			File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH + File.separator + ".seed");

			if(f.exists()) {
				System.err.println("[INIT] Seed File: " + f.getAbsolutePath());
				DataInputStream in = null;
				try {
					in = new DataInputStream(new FileInputStream(f));
					seed = in.readLong();
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
					f.delete();
				} finally {
					if(in != null) {
						try {
							in.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			if(!f.exists()) {
				System.err.println("[INIT] Not found Seed File: " + f.getAbsolutePath() + " " + GameServerState.ENTITY_DATABASE_PATH + "; Path exists? " + (new FileExt(GameServerState.ENTITY_DATABASE_PATH)).exists() + "; dir? " + (new FileExt(GameServerState.ENTITY_DATABASE_PATH)).isDirectory());

				seed = System.currentTimeMillis();
				DataOutputStream bf = null;
				try {
					assert (f.getParentFile().exists()) : f.getAbsolutePath();
					assert (f.getParentFile().isDirectory()) : f.getAbsolutePath();
					bf = new DataOutputStream(new FileOutputStream(f));
					bf.writeLong(seed);
					bf.flush();
				} catch(IOException e) {
					e.printStackTrace();
				} finally {
					if(bf != null) {
						try {
							bf.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			random = new Random(seed);
		}
	}

	public static void calcSecPos(StateInterface state, Vector3i fromAbsSec, Vector3i toSec, long startTime, long time, Transform out) {
		calcSecPos(state, fromAbsSec, toSec, startTime, time, out, false);
	}

	private static void calcSecPos(StateInterface state, Vector3i fromAbsSec, Vector3i toSec, long startTime, long time, Transform out, boolean inv) {

		Vector3i relSysPos = StellarSystem.getPosFromSector(toSec, new Vector3i());

		Vector3i fromOldToNew = new Vector3i();
		fromOldToNew.sub(toSec, fromAbsSec);

		startTime = 0;
		time = 0;
		float pc = 0;
		relSysPos.scale(VoidSystem.SYSTEM_SIZE);
		relSysPos.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
		relSysPos.sub(fromAbsSec);

		Vector3f absSectorPos = new Vector3f();
		Vector3f absCenterPos = new Vector3f();
		absSectorPos.set(fromOldToNew.x * ((GameStateInterface) state).getSectorSize(), fromOldToNew.y * ((GameStateInterface) state).getSectorSize(), fromOldToNew.z * ((GameStateInterface) state).getSectorSize());

		absCenterPos.set((relSysPos.x) * ((GameStateInterface) state).getSectorSize(), (relSysPos.y) * ((GameStateInterface) state).getSectorSize(), (relSysPos.z) * ((GameStateInterface) state).getSectorSize());
		//			System.err.println("SEC CEN "+relSectorPos+"; "+relSystemPos);
		out.setIdentity();

		if(absCenterPos.lengthSquared() > 0) {
			out.origin.add(absCenterPos);
			out.basis.rotX((FastMath.PI * 2) * pc);
			Vector3f d = new Vector3f();
			d.sub(absSectorPos, absCenterPos);
			out.origin.add(d);
			out.basis.transform(out.origin);

		} else {

			//this is the center
			out.basis.rotX((FastMath.PI * 2) * pc);
			out.origin.set(absSectorPos);
			out.basis.transform(out.origin);
		}

	}

	public static void calcSecPosInv(StateInterface state, Vector3i fromAbsSec, Vector3i toSec, long startTime, long time, Transform out) {
		calcSecPos(state, fromAbsSec, toSec, startTime, time, out, true);
	}

	public static void calcSunPosInnerStarSystem(StateInterface state, Vector3i fromAbsSec, StellarSystem system, long startTime, Transform out) {
		float pc = ((GameStateInterface) state).getGameState().getRotationProgession();

		out.basis.rotX((FastMath.PI * 2) * pc);

		out.origin.set((system.getPos().x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2) - fromAbsSec.x, (system.getPos().y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2) - fromAbsSec.y, (system.getPos().z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2) - fromAbsSec.z);
		out.origin.scale(((GameStateInterface) state).getSectorSize());

		out.basis.transform(out.origin);
	}

	/**
	 * @return the random
	 */
	public static Random getRandom() {
		return random;
	}

	public static Sendable loadUntouchedAsteroid(GameServerState state, DatabaseEntry e, Sector sector) {
		assert (EntityType.ASTEROID.dbTypeId == 3);
		assert (e.type == EntityType.ASTEROID.dbTypeId);

		FloatingRock floatingRock = new FloatingRock(state);
		floatingRock.setId(state.getNextFreeObjectId());
		floatingRock.setSectorId(sector.getId());
		floatingRock.initialize();
		floatingRock.getMinPos().set(e.minPos);
		floatingRock.getMaxPos().set(e.maxPos);
		floatingRock.setTouched(e.touched, false);
		floatingRock.setRealName(e.realName);
		floatingRock.setFactionId(e.faction);
		floatingRock.setLastModifier(e.lastModifier);
		floatingRock.setSpawner(e.spawner);
		floatingRock.setUniqueIdentifier(e.uid);
		floatingRock.setCreatorId(e.creatorID);
		floatingRock.setTracked(e.tracked);

		floatingRock.dbId = e.dbId;

		if(state.isPhysicalAsteroids()) {
			floatingRock.setMass(0.1f);
		} else {
			floatingRock.setMass(0);
		}

		floatingRock.getRemoteTransformable().getInitialTransform().setIdentity();
		floatingRock.getRemoteTransformable().getInitialTransform().origin.set(e.pos);

		floatingRock.getRemoteTransformable().getWorldTransform().setIdentity();
		floatingRock.getRemoteTransformable().getWorldTransform().origin.set(e.pos);
		if(e.seed == 0) {
			floatingRock.setSeed(Universe.getRandom().nextLong());
		} else {
			floatingRock.setSeed(e.seed);
		}

		state.getController().getSynchController().addNewSynchronizedObjectQueued(floatingRock);

		return floatingRock;
	}

	public static SimpleTransformableSendableObject<?> loadEntity(GameServerState state, EntityType entType, File f, Sector sector, long entDbId) throws IOException, EntityNotFountException {
		if(entType == EntityType.SHIP) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			Ship ship = new Ship(state);
			ship.setId(state.getNextFreeObjectId());
			ship.setSectorId(sector.getId());
			ship.dbId = entDbId;
			ship.initialize();
			//			System.err.println("adding loaded object from disk: "+f.getName()+" in sector "+sector.pos);
			long t = System.currentTimeMillis();
			ship.fromTagStructure(tag);
			long took = System.currentTimeMillis() - t;

			synchronized(state.getUniverse().writeMap) {
				ship.setLastWrite(state.getUniverse().writeMap.getLong(ship.getUniqueIdentifier()));
			}

			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: SHIP IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			//			System.err.println("added loaded object from disk: "+ship+" from "+f.getName());

			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				ship.setMinable(true);
				ship.setVulnerable(true);
			}
			state.getController().getSynchController().addNewSynchronizedObjectQueued(ship);

			Starter.modManager.onSegmentControllerSpawn(ship);
			return ship;
		} else if(entType == EntityType.ASTEROID || entType == EntityType.ASTEROID_MANAGED) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			FloatingRock floatingRock = entType == EntityType.ASTEROID ? new FloatingRock(state) : new FloatingRockManaged(state);
			floatingRock.setId(state.getNextFreeObjectId());
			floatingRock.setSectorId(sector.getId());
			floatingRock.dbId = entDbId;
			floatingRock.initialize();
			long t = System.currentTimeMillis();
			floatingRock.fromTagStructure(tag);
			long took = System.currentTimeMillis() - t;
			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: ROCK IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			synchronized(state.getUniverse().writeMap) {
				floatingRock.setLastWrite(state.getUniverse().writeMap.getLong(floatingRock.getUniqueIdentifier()));
			}
			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				floatingRock.setMinable(true);
				floatingRock.setVulnerable(true);
			}
			//				System.err.println("adding loaded object from disk: "+floatingRock);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(floatingRock);
			Starter.modManager.onSegmentControllerSpawn(floatingRock);
			return floatingRock;
		} else if(entType == EntityType.SHOP) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			ShopSpaceStation shop = new ManagedShop(state);
			shop.setId(state.getNextFreeObjectId());
			shop.setSectorId(sector.getId());
			shop.dbId = entDbId;
			shop.initialize();
			long t = System.currentTimeMillis();
			shop.fromTagStructure(tag);
			long took = System.currentTimeMillis() - t;
			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: SHOP IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			synchronized(state.getUniverse().writeMap) {
				shop.setLastWrite(state.getUniverse().writeMap.getLong(shop.getUniqueIdentifier()));
			}

			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				shop.setMinable(true);
				shop.setVulnerable(true);
			}
			//				System.err.println("adding loaded object from disk: "+shop);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(shop);
			Starter.modManager.onSegmentControllerSpawn(shop);
			return shop;
		} else if(entType == EntityType.SPACE_STATION) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			SpaceStation spaceStation = new SpaceStation(state);
			spaceStation.setId(state.getNextFreeObjectId());
			spaceStation.setSectorId(sector.getId());
			spaceStation.dbId = entDbId;
			spaceStation.initialize();
			long t = System.currentTimeMillis();
			spaceStation.fromTagStructure(tag);
			long took = System.currentTimeMillis() - t;
			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: STATION IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			synchronized(state.getUniverse().writeMap) {
				spaceStation.setLastWrite(state.getUniverse().writeMap.getLong(spaceStation.getUniqueIdentifier()));
			}
			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				spaceStation.setMinable(true);
				spaceStation.setVulnerable(true);
			}
			//							System.err.println("[UNIVERSE] adding loaded object from disk: "+spaceStation);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(spaceStation);
			Starter.modManager.onSegmentControllerSpawn(spaceStation);
			return spaceStation;
		} else if(entType == EntityType.PLANET_SEGMENT) {
			throw new RuntimeException("Old planet segment found, indicating an incompatible world!\nPlease delete the server-database folder and restart the server.");
			/*
			Tag tag = state.getController().readEntity(f.getName(), "");
			Planet planet = new Planet(state);
			planet.setId(state.getNextFreeObjectId());
			planet.setSectorId(sector.getId());
			planet.dbId = entDbId;
			planet.initialize();
			long t = System.currentTimeMillis();

			planet.fromTagStructure(tag);

			if(!planet.getPlanetCoreUID().equals("none")) {

				planet.setPlanetCore(sector.getPlanetCore());

				if(planet.getCore() == null) {
					try {
						System.err.println("[UNIVERSE][PLANET] LOADING PLANET CORE: " + planet.getPlanetCoreUID());
						Tag coreTag = state.getController().readEntity(planet.getPlanetCoreUID(), ".ent");
						PlanetIcoCore planetCore = new PlanetIcoCore(state);
						planetCore.setId(state.getNextFreeObjectId());
						planetCore.setSectorId(sector.getId());
						planetCore.initialize();
						planetCore.fromTagStructure(coreTag);

						state.getController().getSynchController().addNewSynchronizedObjectQueued(planetCore);
						planet.setPlanetCore(planetCore);

						sector.setPlanetCore(planetCore);
					} catch(EntityNotFountException e) {
						e.printStackTrace();
						planet.setPlanetCoreUID("none");
					}
				} else {
					System.err.println("[UNIVERSE][PLANET] PLANET CORE ALREADY LOADED: " + planet.getCore());
				}

			} else {
				System.err.println("[UNIVERSE][PLANET] NO PLANET CORE");
			}

			long took = System.currentTimeMillis() - t;
			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: PLANET IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			synchronized(state.getUniverse().writeMap) {
				planet.setLastWrite(state.getUniverse().writeMap.getLong(planet.getUniqueIdentifier()));
			}
			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				planet.setMinable(true);
				planet.setVulnerable(true);
			}
			//				System.err.println("adding loaded object from disk: "+shop);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(planet);
			Starter.modManager.onSegmentControllerSpawn(planet);
			return planet;
			 */
		} else if(entType == EntityType.PLANET_ICO) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			PlanetIco planet = new PlanetIco(state);
			planet.setId(state.getNextFreeObjectId());
			planet.setSectorId(sector.getId());
			planet.dbId = entDbId;
			planet.initialize();
			long t = System.currentTimeMillis();

			planet.fromTagStructure(tag);

			long took = System.currentTimeMillis() - t;
			if(took > 10) {
				System.err.println("[SERVER][LOADENTITY] WARNING: PLANET IN " + sector + " FROM TAG: " + f.getName() + " took long: " + took + "ms");
			}
			synchronized(state.getUniverse().writeMap) {
				planet.setLastWrite(state.getUniverse().writeMap.getLong(planet.getUniqueIdentifier()));
			}
			if(resetMinableAndVulnerable && !sector.isTutorialSector()) {
				planet.setMinable(true);
				planet.setVulnerable(true);
			}
			//				System.err.println("adding loaded object from disk: "+shop);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(planet);
			Starter.modManager.onSegmentControllerSpawn(planet);
			planet.setPlanetCore(sector.getPlanetCore());
			if(planet.getCore() == null) {
				try {
					System.err.println("[UNIVERSE][PLANET] LOADING PLANET CORE: " + planet.getPlanetCoreUID());
					Tag coreTag = state.getController().readEntity(planet.getPlanetCoreUID(), ".ent");
					PlanetIcoCore planetCore = new PlanetIcoCore(state);
					planetCore.setId(state.getNextFreeObjectId());
					planetCore.setSectorId(sector.getId());
					planetCore.initialize();
					planetCore.fromTagStructure(coreTag);
					state.getController().getSynchController().addNewSynchronizedObjectQueued(planetCore);
					planet.setPlanetCore(planetCore);
					sector.setPlanetCore(planetCore);
				} catch(EntityNotFountException e) {
					e.printStackTrace();
					planet.setPlanetCoreUID("none");
				}
			}
			return planet;
		} else if(entType == EntityType.DEATH_STAR) {
			Tag tag = state.getController().readEntity(f.getName(), "");
			TeamDeathStar dStar = new TeamDeathStar(state);
			dStar.setId(state.getNextFreeObjectId());
			dStar.setSectorId(sector.getId());
			dStar.initialize();
			dStar.fromTagStructure(tag);

			synchronized(state.getUniverse().writeMap) {
				dStar.setLastWrite(state.getUniverse().writeMap.getLong(dStar.getUniqueIdentifier()));
			}
			//				System.err.println("adding loaded object from disk: "+shop);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(dStar);
			Starter.modManager.onSegmentControllerSpawn(dStar);
			return dStar;
		}
		System.err.println("[LOADENTITY] Exception: no loading routine for type: " + entType.name());
		return null;
	}

	public static void write(DiskWritable ss, String fileName) throws IOException {
		EntityFileTools.write(GameServerState.fileLocks, ss, GameServerState.ENTITY_DATABASE_PATH, fileName);
	}

	public static Sendable loadUntouchedShop(GameServerState state, DatabaseEntry byUID, Sector sector) {

		BluePrintController c;
		int faction;

		c = BluePrintController.shopsTradingGuild;
		faction = FactionManager.TRAIDING_GUILD_ID;
		List<BlueprintEntry> readBluePrints = c.readBluePrints();

		boolean isAdvanced = byUID.uid.contains("Advanced");
		BlueprintEntry blueprintEntry = null;
		try {
			BlueprintEntry standardShop = c.getBlueprint("Trade Outpost");
			BlueprintEntry advancedShop = c.getBlueprint("Trade Station");
			if(isAdvanced) blueprintEntry = advancedShop;
			else blueprintEntry = standardShop;
		} catch(Exception e1) {
			e1.printStackTrace();
		}
		if(blueprintEntry == null) {
			System.err.println("[SERVER][SECTOR] ERROR: Could not find shop blueprint for sector " + sector.pos);
			return null;
		}
		blueprintEntry.setEntityType(BlueprintType.SHOP);

		Transform t = new Transform();
		t.setIdentity();

		SegmentControllerOutline<?> loadBluePrint;
		try {
			loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), byUID.realName, t, -1, faction, readBluePrints, sector.pos, null, "<system>", Sector.buffer, true, null, new ChildStats(false));
			loadBluePrint.scrap = false;
			loadBluePrint.shop = true;
			loadBluePrint.spawnSectorId = new Vector3i(sector.pos);
			System.err.println(loadBluePrint.getClass().getName());
			synchronized(state.getBluePrintsToSpawn()) {
				state.getBluePrintsToSpawn().add(loadBluePrint);
			}
		} catch(EntityNotFountException | IOException | EntityAlreadyExistsException e) {
			e.printStackTrace();
		}
		return state.getSegmentControllersByName().get(byUID.uid);
	}

	private Sector addNewSector(Vector3i pos) throws IOException {
		Sector sector = new Sector(state);
		sector.pos = new Vector3i(pos);
		sector.setSeed(getSectorSeed(pos));
		sector.setRandom(new Random(sector.getSeed()));
		sector.setChangedForDb(true);
		try {
			state.getDatabaseIndex().getTableManager().getSectorTable().updateOrInsertSector(sector);
		} catch(SQLException e) {
			e.printStackTrace();
		}

		addSector(sector);
		sector.populate(state);

		return sector;
	}

	public long getSectorSeed(Vector3i sector) {
		return seed + sector.code();
	}

	public void addSector(Sector s) throws IOException {
		//		System.err.println("[UNIVERSE] ADDING SECTOR "+s);
		assert (s != null);
		sectors.put(s.getId(), s);
		s.onAddedSector();
		sectorPositions.put(s.pos, s);
	}

	public void addToClear(SendableSegmentController sendableSegmentController) {
		synchronized(toClear) {
			toClear.enqueue(sendableSegmentController);
		}
	}

	public void addToFreePhysics(Physics physics, Sector s) {
		boolean cleanUpForDelete = false;
		((PhysicsExt) physics).setState(null);
		((PhysicsExt) physics).getDynamicsWorld().setInternalTickCallback(null, null);
		synchronized(physicsRepository) {
			cleanUpForDelete = physicsRepository.size() > MAX_PHYSICS_REPOSITORY_SIZE;
			if(!cleanUpForDelete && !physicsRepository.contains(physics)) {
				//				System.err.println("[SERVER] physics for "+this+": "+physics+" has been added to repository");
				physicsRepository.add(physics);
				physics.softClean();
			}
		}
		if(cleanUpForDelete) {
			System.out.println("[SERVER][UNIVERSE] Cleaned up Physics, because repository is full");
			physics.cleanUp();
			s.setPhysics(null);
			s.entityUids.clear();
		}
	}

	public boolean existsSector(int sectorId) {
		Sector sector = sectors.get(sectorId);
		return sector != null;
	}

	@Override
	public void fromTagStructure(Tag tag) {

	}

	@Override
	public Tag toTagStructure() {
		return null;
	}

	public boolean getIsSectorActive(int sectorId) {
		Sector sector = sectors.get(sectorId);
		return sector != null && sector.isActive();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	//	public SectorInformation getSectorInformationSingle(Vector3i what) throws IOException{
	//
	//		StellarSystem system = getStellarSystemFromSecPos(what);
	//		SectorInformation sectorInformation = system.getInfoFromAbsolute(what);
	//		return sectorInformation;
	//	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Physics getPhysicsInstance(PhysicsState pState) {
		synchronized(physicsRepository) {
			if(physicsRepository.size() > 0) {
				PhysicsExt remove = (PhysicsExt) physicsRepository.remove(0);
				remove.setState(pState);
				//				 System.err.println("[SERVER] physics for "+this+": "+remove+" has been fetched repository");
				return remove;
			}
		}
		return new PhysicsExt(pState);
	}

	/**
	 * @return the physicsRepository
	 */
	public List<Physics> getPhysicsRepository() {
		return physicsRepository;
	}

	public Sector getSector(int sectorId) {
		return sectors.get(sectorId);
	}

	public Sector getSector(Vector3i pos) throws IOException {
		return getSector(pos, true);
	}

	public Sector getSector(Vector3i pos, boolean activate) throws IOException {
		try {
			if(!sectorPositions.containsKey(pos)) {
				loadOrGenerateSector(pos);
			}
			Sector sector = sectorPositions.get(pos);
			if(!sector.isActive() && (entityCleaningSectors.contains(sector) || inactiveWrittenSectors.contains(sector))) {
				loadOrGenerateSector(pos);
			}
			if(!sectors.containsKey(sector.getId())) {
				assert (sector != null);

				sectors.put(sector.getId(), sector);
			}
			assert (sector != null) : pos + " - " + sectorPositions;
			if(activate) {
				//do not activate e.g. when an AI or Missile does a sector change
				sector.setActive(true);
			}

			return sector;
		} catch(SQLException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	public Vector3i getSectorBelonging(SimpleTransformableSendableObject o) {

		if(o.isHidden()) {
			//don't change the sector for hidded objects
			//if they are needed, they will change sector
			//along with other objects (like the playerCharacter)
			return getSector(o.getSectorId()).pos;
		}
		if(o instanceof SegmentController && ((SegmentController) o).getDockingController().isDocked()) {

			//don't change the sector of docked objects
			//they change sectors with the parent object
			return getSector(o.getSectorId()).pos;
		}
		if(o instanceof SegmentController && ((SegmentController) o).railController.isDockedAndExecuted()) {
			return getSector(((SegmentController) o).railController.getRoot().getSectorId()).pos;
		}
		if(o instanceof AbstractCharacter<?> && ((PairCachingGhostObjectAlignable) ((AbstractCharacter<?>) o).getPhysicsDataContainer().getObject()).getAttached() != null) {
			return getSector(o.getSectorId()).pos;
		}
		//TODO check which vars have changed and do not change sector if the distance didn't change significally
		Sector sector = getSector(o.getSectorId());
		Vector3i pos = sector.pos;
		int nearest = -1;
		nearVector.set(o.getWorldTransform().origin);
		boolean isSelfStarSystem = StellarSystem.isStarSystem(sector.pos);
		for(int i = 0; i < Element.DIRECTIONSi.length; i++) {

			std.setIdentity();

			otherSecAbs.set(pos);
			otherSecAbs.add(Element.DIRECTIONSi[i]);
			Sector otherSec = sectorPositions.get(otherSecAbs);
			if(otherSec != null) {

				SimpleTransformableSendableObject.calcWorldTransformRelative(o.getSectorId(), pos, otherSec.getId(), std, state, true, out, v);

				//				ConstantIndication c = new ConstantIndication(new Transform(out), otherSec.pos.toString());
				//				c.setDist(10000000);
				//				int indexOf = HudIndicatorOverlay.toDrawTexts.indexOf(c);
				//				if(indexOf < 0){
				//				HudIndicatorOverlay.toDrawTexts.add(c);
				//				}else{
				//					 HudIndicatorOverlay.toDrawTexts.get(indexOf).getCurrentTransform().set(out);
				//				}

				dist.sub(o.getWorldTransform().origin, out.origin);

				if(dist.lengthSquared() < nearVector.lengthSquared()) {
					nearVector.set(dist);
					nearest = i;
					//					System.err.println(i+"SECTOR CHANGE ::::: "+Element.DIRECTIONSi[i]+": "+dist.length()+" / "+nearVector.length());
				}
			} else {
				//we are now in unloaded sector


				v.dir.sub(otherSecAbs, pos);

				v.otherSecCenter.set(v.dir.x * ((GameStateInterface) state).getSectorSize(), v.dir.y * ((GameStateInterface) state).getSectorSize(), v.dir.z * ((GameStateInterface) state).getSectorSize());

				v.t.set(std);

				v.t.origin.add(v.otherSecCenter);

				out.set(v.t);

				dist.sub(o.getWorldTransform().origin, out.origin);

				if(dist.lengthSquared() < nearVector.lengthSquared()) {
					nearVector.set(dist);
					nearest = i;
					//					System.err.println(i+"SECTOR CHANGE ::::: "+Element.DIRECTIONSi[i]+": "+dist.length()+" / "+nearVector.length());
				}
			}
		}
		if(nearest >= 0) {

			belogingVector.set(pos);
			belogingVector.add(Element.DIRECTIONSi[nearest]);

			int jump = SectorSwitch.TRANS_LOCAL;

			state.getController().queueSectorSwitch(o, belogingVector, jump, false);
			return belogingVector;
		} else {
			return pos;
		}
	}

	/**
	 * @return the sectors
	 */
	public Collection<Sector> getSectorSet() {
		return sectors.values();
	}

	public Sector getSectorWithoutLoading(Vector3i pos) {
		return sectorPositions.get(pos);
	}

	public SystemOwnershipType getSystemOwnerShipType(StellarSystem stellarSystemFromSecPos, int ownFaction) {
		if(stellarSystemFromSecPos.getOwnerFaction() == 0) {
			return SystemOwnershipType.NONE;
		}
		if(ownFaction != 0 && stellarSystemFromSecPos.getOwnerFaction() == ownFaction) {
			return SystemOwnershipType.BY_SELF;
		}
		RType relation = state.getFactionManager().getRelation(ownFaction, stellarSystemFromSecPos.getOwnerFaction());
		return switch(relation) {
			case ENEMY -> SystemOwnershipType.BY_ENEMY;
			case FRIEND -> SystemOwnershipType.BY_ALLY;
			case NEUTRAL -> SystemOwnershipType.BY_NEUTRAL;
			default -> SystemOwnershipType.NONE;
		};
	}

	public SystemOwnershipType getSystemOwnerShipType(int sectorId, int ownFaction) {
		Sector sector = getSector(sectorId);
		if(sector != null) {
			try {
				StellarSystem stellarSystemFromSecPos = getStellarSystemFromSecPos(sector.pos);

				return getSystemOwnerShipType(stellarSystemFromSecPos, ownFaction);

			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return SystemOwnershipType.NONE;
	}

	public StellarSystem getStellarSystemFromSecPos(Vector3i absPos) throws IOException {

		Vector3i sysPos = VoidSystem.getContainingSystem(absPos, new Vector3i());

		VoidSystem voidSystem = starSystemMap.get(sysPos);

		if(voidSystem == null) {
			Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, new Vector3i());
			Galaxy galaxy = getGalaxy(galaxyPos);

			voidSystem = loadOrGenerateVoidSystem(sysPos, galaxy);
			starSystemMap.put(voidSystem.getPos(), voidSystem);
		}
		assert (VoidSystem.getContainingSystem(absPos, new Vector3i()).equals(voidSystem.getPos()));
		return voidSystem.getInternal(absPos);
	}

	public Galaxy getGalaxyFromSystemPos(Vector3i sysPos) {
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, new Vector3i());
		Galaxy galaxy = getGalaxy(galaxyPos);
		return galaxy;
	}

	public Galaxy getGalaxy(Vector3i galaxyPos) {
		Galaxy galaxy = galaxyMap.get(galaxyPos);

		if(galaxy == null) {
			galaxy = new Galaxy(seed + galaxyPos.hashCode(), new Vector3i(galaxyPos));

			///INSERTED CODE
			GalaxyInstantiateEvent e = new GalaxyInstantiateEvent(galaxy, galaxyPos);
			StarLoader.fireEvent(GalaxyInstantiateEvent.class, e, true);
			galaxy = e.getGalaxy();
			///

			galaxy.generate();
			galaxy.initializeGalaxyOnServer(state);

			galaxyMap.put(new Vector3i(galaxyPos), galaxy);
		}

		return galaxy;
	}

	public VoidSystem getStellarSystemFromStellarPosIfLoaded(Vector3i sysPos) throws IOException {
		return starSystemMap.get(sysPos);
	}

	public StellarSystem overwriteSystem(Vector3i sysPos, SectorGenerationInterface iFace, boolean forceDbUpdateIfExists) throws IOException {
		VoidSystem voidSystem = getStellarSystemFromStellarPosIfLoaded(sysPos);
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, new Vector3i());
		Galaxy galaxy = getGalaxy(galaxyPos);

		if(voidSystem == null) {
			voidSystem = loadOrGenerateVoidSystem(sysPos, galaxy, iFace, forceDbUpdateIfExists);
			assert (voidSystem.getPos().equals(sysPos));
			starSystemMap.put(voidSystem.getPos(), voidSystem);
		} else {
			generateSystem(voidSystem, galaxy, iFace, forceDbUpdateIfExists);
		}
		return voidSystem;
	}

	public StellarSystem getStellarSystemFromStellarPos(Vector3i sysPos) throws IOException {
		VoidSystem voidSystem = getStellarSystemFromStellarPosIfLoaded(sysPos);
		if(voidSystem == null) {

			Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sysPos, new Vector3i());
			Galaxy galaxy = getGalaxy(galaxyPos);

			voidSystem = loadOrGenerateVoidSystem(sysPos, galaxy);
			assert (voidSystem.getPos().equals(sysPos));
			starSystemMap.put(voidSystem.getPos(), voidSystem);
		}
		return voidSystem;
	}

	public boolean isEmpty() {
		return sectors.isEmpty();
	}

	public boolean isSectorActive(Vector3i sectorId) {
		Sector sector = sectorPositions.get(sectorId);
		return sector != null && sector.isActive();
	}

	public boolean isSectorLoaded(Vector3i what) {
		return sectorPositions.containsKey(what);
	}

	public void loadOrGenerateSector(Vector3i what) throws IOException, SQLException {

		Vector3i secPos = new Vector3i(what);
		Sector sector = null;

		long t = System.currentTimeMillis();
		sector = new Sector(state);
		long instTime = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();

		final boolean loaded = state.getDatabaseIndex().getTableManager().getSectorTable().loadSector(what, sector);
		long loadTime = System.currentTimeMillis() - t;
		long newTime = 0;
		long addTime = 0;

		if(!loaded) {
			t = System.currentTimeMillis();
			sector = addNewSector(secPos);
			sector.setNew();
			sector.setLastReplenished(t);
			/*
			 * load all entities even though the sector is new because a fleet
			 * or similar can place an entity in this previously non existing
			 * sector
			 */
			sector.loadUIDs(state);
			newTime = System.currentTimeMillis() - t;
		} else {
			sector.setSeed(seed + what.code());
			sector.setRandom(new Random(sector.getSeed()));
			t = System.currentTimeMillis();
			sector.loadUIDs(state);
			addSector(sector);
			addTime = System.currentTimeMillis() - t;

		}

		t = System.currentTimeMillis();
		sector.setActive(true);
		sector.loadEntities(state);
		if(sector.isTutorialSector()) {
			sector.mode(SectorMode.LOCK_NO_ENTER, true);
			sector.mode(SectorMode.LOCK_NO_EXIT, true);
		}
		long entityTime = System.currentTimeMillis() - t;
		if(instTime > 10 || loadTime > 10 || addTime > 10 || entityTime > 10) {
			System.out.println("[SERVER][UNIVERSE] WARNING: LOADING SECTOR TOOK SOME TIME... " + secPos + ": STATS: inst " + instTime + "ms, loadDB " + loadTime + "ms, add " + addTime + "ms, entity " + entityTime + "ms");
		}
	}

	private VoidSystem loadOrGenerateVoidSystem(Vector3i systemPos, Galaxy galaxy) throws IOException {

		SectorGenerationInterface iFace = new SectorGenerationDefault();
		return loadOrGenerateVoidSystem(systemPos, galaxy, iFace, false);
	}

	private VoidSystem loadOrGenerateVoidSystem(Vector3i systemPos, Galaxy galaxy, SectorGenerationInterface iFace, boolean forceDbUpdateIfExists) throws IOException {

		VoidSystem ssys = new VoidSystem();

		ssys.log("Universe attempting retrieval; will generate if needed.");
		if(state.getDatabaseIndex().getTableManager().getSystemTable().loadSystem(state, systemPos, ssys)) {
			//nothing to do: system has been loded from database
			ssys.log("Retrieved by Universe from database.");
			assert (ssys.getDBId() >= 0);
		} else {
			//system not found in database, creating new one
			ssys.log("Creating as new by Universe.");
			ssys.getPos().set(systemPos);
			generateSystem(ssys, galaxy, iFace, forceDbUpdateIfExists);
		}
		return ssys;
	}

	public void generateSystem(VoidSystem ssys, Galaxy galaxy, SectorGenerationInterface iFace, boolean forceDbUpdateIfExists) throws IOException {
		systemRandom.setSeed(seed + ssys.getPos().hashCode());

		///INSERTED CODE
		SystemPreGenerationEvent event = new SystemPreGenerationEvent(ssys, galaxy, iFace, forceDbUpdateIfExists);
		StarLoader.fireEvent(SystemPreGenerationEvent.class, event, false);
		iFace = event.getGenerator();
		///

		ssys.generate(systemRandom, galaxy, state, iFace);

		try {
			long id = state.getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(ssys, forceDbUpdateIfExists);
			if(ssys.getDBId() < 0) {
				ssys.setDBId(id);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void pingSectorBlock(Vector3i currentSector) throws IOException {
		for(int x = currentSector.x - 1; x <= currentSector.x + 1; x++) {
			for(int y = currentSector.y - 1; y <= currentSector.y + 1; y++) {
				for(int z = currentSector.z - 1; z <= currentSector.z + 1; z++) {
					where.set(x, y, z);
					getSector(where).ping();
				}
			}
		}
	}

	public void pingSectors() throws IOException {
		GameServerState state = this.state;

		for(PlayerState s : state.getPlayerStatesByName().values()) {
			pingSectorBlock(s.getCurrentSector());
			//			getSector(((PlayerState) s).getCurrentSector()).ping();
		}
	}

	public void resetAllSectors() {
		//		for(Integer i : sectors.keySet()){
		//			Sector old = getSector(i);
		//			old.cleanUp();
		//		}
		Sector old;
		try {
			old = getSector(new Vector3i(Sector.DEFAULT_SECTOR));

			Sector n = new Sector(state);

			n.setId(old.getId());
			n.pos = new Vector3i(Sector.DEFAULT_SECTOR);

			n.populate(state);

			addSector(n);
			old.cleanUp();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void updateSector(Sector sector, long current, int timeoutSecs, Timer timer, Int2LongOpenHashMap sectorLag) throws IOException {
		long time = System.currentTimeMillis();

		if(EngineSettings.N_TRANSMIT_RAW_DEBUG_POSITIONS.isOn()) {
			DynamicsWorld dynamicsWorld = sector.getPhysics().getDynamicsWorld();
			for(CollisionObject c : dynamicsWorld.getCollisionObjectArray()) {
				if(c instanceof RigidBodySegmentController) {
					DebugServerPhysicalObject p = new DebugServerPhysicalObject();
					p.setObject((RigidBodySegmentController) c, sector.getId());
					state.getGameState().getNetworkObject().debugPhysical.add(new RemoteSerializableObject(p, true));
				} else {
					if(c instanceof PairCachingGhostObjectAlignable) {
						DebugServerPhysicalObject p = new DebugServerPhysicalObject();
						p.setObject(((PairCachingGhostObjectAlignable) c).getObj());
						state.getGameState().getNetworkObject().debugPhysical.add(new RemoteSerializableObject(p, true));
					}
				}
			}
		}
		long tp = System.currentTimeMillis();
		long transmit = tp - time;

		if(sector.isActive() && timeoutSecs >= 0 && current - sector.getLastPing() > timeoutSecs * 1000) {

			boolean playerPresent = false;

			for(PlayerState s : state.getPlayerStatesByName().values()) {
				if(s.getCurrentSectorId() == sector.getId()) {
					playerPresent = true;
					sector.ping();
				}
			}
			if(!playerPresent) {

				sector.setActive(false);
			}
		}
		long playerPingTaken = System.currentTimeMillis() - tp;


		long mm = System.currentTimeMillis();
//		//WARNING: Enabling this might cause random lag as polling the keyboard in another thread causes interrupptions of the pipeline
//		if(false && Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_PERIOD)){
//			try {
//				System.err.println("CRWATING ARTIFICIAL LAG");
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			}
//		}
		long KBTaken = System.currentTimeMillis() - mm;


		long st = System.currentTimeMillis();
		sector.update(timer);
		long updateTaken = System.currentTimeMillis() - st;


		st = System.currentTimeMillis();
		if(!sector.isActive() && sector.hasSectorRemoveTimeout(current) && sector.isSectorWrittenToDisk() && !entityCleaningSectors.contains(sector)) {
			//all entities for the sector have been written
			inactiveWrittenSectors.add(sector);
		}
		long inactiveWritten = System.currentTimeMillis() - st;
		long timeTaken = (System.currentTimeMillis() - time) + sectorLag.get(sector.getId());

		if(timeTaken > 50 && state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(sector.getRemoteSector().getId())) {
			sector.getRemoteSector().announceLag(timeTaken);
//			System.err.println("LAAG "+this+"; "+timeTaken+"; "+(System.currentTimeMillis() - time)+"; updateTaken "+updateTaken+"; inactiveWritten: "+inactiveWritten+"; ping: "+playerPingTaken+"; transmit: "+transmit+"; KBTaken: "+KBTaken);
		}
	}

	public void update(Timer timer, Int2LongOpenHashMap sectorLag) throws IOException {
		this.lastTime = timer.currentTime;
		if(timer.currentTime - lastPing > 500) {
			long tPing = System.currentTimeMillis();
			pingSectors();
			long pingTime = System.currentTimeMillis() - tPing;
			if(pingTime > 10) {
				System.err.println("[UNIVERSE] WARNING: Sector Ping took " + pingTime);
			}
			lastPing = System.currentTimeMillis();
		}


		for(int i = 0; i < deffereedLoads.size(); i++) {
			DeferredLoad deferredLoad = deffereedLoads.get(i);
			if(deferredLoad.sector.isActive()) {
				deferredLoad.delay--;
				if(deferredLoad.delay <= 0) {
					try {
						deferredLoad.sector.loadEntitiy(state, deferredLoad.uid);
					} catch(SQLException e) {
						e.printStackTrace();
					}
					deffereedLoads.remove(i--);
				}
			} else {
				deffereedLoads.remove(i--);
			}
		}
		if(timer.currentTime - lastAttackCheck > 60000 * 5) {
			LongIterator iterator = attackSector.values().iterator();

			while(iterator.hasNext()) {
				if(timer.currentTime - iterator.nextLong() > 60000 * 30) {
					iterator.remove();
				}
			}
			lastAttackCheck = timer.currentTime;

		}
		for(Galaxy g : galaxyMap.values()) {
			g.updateLocal(timer.currentTime);
		}

		long tUpdate = System.currentTimeMillis();
		int timeoutSecs = (ServerConfig.SECTOR_INACTIVE_TIMEOUT.getInt());
//		FastEntrySet<Sector> eSet = sectors.int2ObjectEntrySet();

		long current = System.currentTimeMillis();
		GameServerState.totalSectorCountTmp = 0;
		GameServerState.activeSectorCountTmp = 0;
		for(Sector sector : getSectorSet()) {
			updateSector(sector, current, timeoutSecs, timer, sectorLag);
		}
		GameServerState.totalSectorCount = GameServerState.totalSectorCountTmp;
		GameServerState.activeSectorCount = GameServerState.activeSectorCountTmp;
		long updateTime = System.currentTimeMillis() - tUpdate;
		if(updateTime > 30) {
			System.err.println("[UNIVERSE] WARNING: Sector UPDATE took " + updateTime + "; sectors updated: " + getSectorSet().size());
		}
		if(ftlDirty.size() > 0) {
			synchronized(ftlDirty) {
				while(!ftlDirty.isEmpty()) {
					Vector3i dequeue = ftlDirty.dequeue();

					galaxyManager.sendDirectFtlUpdateOnServer(dequeue);
				}
			}

		}
		if(tradeNodesDirty.size() > 0) {
			synchronized(tradeNodesDirty) {
				while(!tradeNodesDirty.isEmpty()) {
					long dequeue = tradeNodesDirty.dequeueLong();

					galaxyManager.sendDirectTradeUpdateOnServer(dequeue);
				}
			}

		}
		long tClean = System.currentTimeMillis();
		if(!toClear.isEmpty()) {
			synchronized(toClear) {
				if(!toClear.isEmpty()) {
					SendableSegmentController s = toClear.dequeue();
				}
			}
		}
		int inactQueue = inactiveWrittenSectors.size();
		for(Sector sector : inactiveWrittenSectors) {
			Sector r1 = sectorPositions.remove(sector.pos);
			Sector r2 = sectors.remove(sector.getId());

			System.err.println("[SECTOR][CLEANUP] removing entities and " + sector + ": " + r1 + "; " + r2);
			assert (r1 != null && r2 != null);
			ObjectArrayList<SimpleTransformableSendableObject> entities = sector.updateEntities();
			long t = System.currentTimeMillis();
			//INSERTED CODE
			SectorUnloadEvent event = new SectorUnloadEvent(entities, sector);
			StarLoader.fireEvent(event, true);
			///
			for(SimpleTransformableSendableObject o : entities) {
				if(!o.isWrittenForUnload()) {
					System.err.println("[SECTOR][CLEANUP] Warning: written flag was not set for " + o);
				}
				if(o instanceof SendableSegmentController) {
					SendableSegmentController segCon = (SendableSegmentController) o;
					//INSERTED CODE
					SegmentControllerUnloadEvent event2 = new SegmentControllerUnloadEvent(segCon);
					StarLoader.fireEvent(event2, true);
					///

					((SendableSegmentController) segCon).onClear();
					int sBefore = segCon.getSegmentBuffer().size();
					int cleared = segCon.getSegmentBuffer().clear(true);

					if(segCon instanceof ManagedSegmentController) {
						((ManagedSegmentController<?>) segCon).getManagerContainer().clear();
					}
//					segCon.getControlElementMap().clear();
					segCon.getSegmentProvider().releaseFileHandles();
				}
				o.setMarkedForDeleteVolatile(true);
			}
			long taken = System.currentTimeMillis() - t;
			if(taken > 30) {
				System.err.println("[SERVER] WARNING: clearing written objects of sector " + sector + " took " + taken + " ms");
			}
			entityCleaningSectors.add(sector);

			int oldId = sector.getId();
			Sector newSector = null;
			for(PlayerState s : state.getPlayerStatesByName().values()) {
				if(s.getCurrentSectorId() == oldId) {
					if(newSector == null) {
						newSector = getSector(s.getCurrentSector());
					}

					if(newSector.pos.equals(s.getCurrentSector())) {
						s.setCurrentSectorId(newSector.getId());
					} else {
						throw new IllegalArgumentException("Sector invalid for player: " + s + " has " + s.getCurrentSectorId() + " -> " + s.getCurrentSector() + "; new sector: " + newSector);
					}
				}
			}

		}
		inactiveWrittenSectors.clear();
		int cleanSecQueue = entityCleaningSectors.size();

		ObjectIterator<Sector> iterator = entityCleaningSectors.iterator();
		while(iterator.hasNext()) {

			Sector sector = iterator.next();
			boolean restEntities = false;
			ObjectArrayList<SimpleTransformableSendableObject> entities = sector.updateEntities();
			for(SimpleTransformableSendableObject o : entities) {
				if(state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(o.getId())) {
					restEntities = true;
					break;
				}
			}
			if(!restEntities) {
				//

				sector.cleanUp();
				sector.getRemoteSector().setMarkedForDeleteVolatile(true);
				sector.terminated = true;
				iterator.remove();
				//				System.err.println("[SERVER] successfully cleaned up sector "+sector+"; now physically removing "+r1+"; "+r2);

			} else {
				System.err.println("[SERVER] waiting for sector " + sector + " entities to be cleaned up " + entities);
				for(SimpleTransformableSendableObject o : entities) {
					if(!o.isMarkedForDeleteVolatile()) {
						System.err.println("[SERVER] not cleaned up: " + o + "; deleteSent: " + o.isMarkedForDeleteVolatileSent());
					}
				}
			}

		}
		long cleanTook = System.currentTimeMillis() - tClean;
		if(cleanTook > 5) {
			System.err.println("[UNIVERSE] WARNING SECTOR CLEANUP TIME: " + cleanTook + "; QUEUE: " + cleanSecQueue + "; InactSectors: " + inactQueue);
		}
		galaxyManager.updateServer();
		DebugControlManager.requestPhysicsCheck = false;
	}

	public void updateProximitySectorInformation(Vector3i what) throws IOException {
		Vector3i[] pos = new Vector3i[8];
		pos[0] = new Vector3i(what.x + 8, what.y + 8, what.z + 8);
		pos[1] = new Vector3i(what.x - 8, what.y + 8, what.z + 8);
		pos[2] = new Vector3i(what.x + 8, what.y - 8, what.z + 8);
		pos[3] = new Vector3i(what.x - 8, what.y - 8, what.z + 8);
		pos[4] = new Vector3i(what.x + 8, what.y + 8, what.z - 8);
		pos[5] = new Vector3i(what.x - 8, what.y + 8, what.z - 8);
		pos[6] = new Vector3i(what.x + 8, what.y - 8, what.z - 8);
		pos[7] = new Vector3i(what.x - 8, what.y - 8, what.z - 8);

		for(int i = 0; i < pos.length; i++) {
			getStellarSystemFromSecPos(pos[i]);
		}
		//		System.err.println("[STELLAR] get info for "+what);
	}

	private void writeAdditionalEntities() throws IOException {
		if(state != null && state.getGameState() != null) {
			System.err.println("[SERVER] WRITING ADDITIONAL ENTITIES");

			write(state.getGameState().getFactionManager(), "FACTIONS.fac");

			state.getCatalogManager().writeToDisk();

			for(Galaxy g : galaxyMap.values()) {

				EntityFileTools.write(GameServerState.fileLocks, g.getNpcFactionManager(), GameServerState.ENTITY_DATABASE_PATH, g.getNpcFactionManager().getFileNameTag());


			}

			write(state.getChannelRouter(), ChannelRouter.FILENAME);

			write(state.getGameState().getTradeManager(), TradeManager.FILENAME);

			System.err.println("[SERVER] WRITING ADDITIONAL ENTITIES DONE");
		}
	}

	private void writeSimulationState() throws IOException {
		state.getSimulationManager().writeToDatabase();
		write(state.getSimulationManager(), state.getSimulationManager().getUniqueIdentifier() + ".sim");
	}

	public void writeStarSystems() throws IOException, SQLException {

		long time = System.currentTimeMillis();
		for(StellarSystem ss : starSystemMap.values()) {

			if(ss.isChanged()) {
				state.getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(ss, false);
			}

			//			String fileName = GameServerState.ENTITY_DATABASE_PATH+getVoidSystemFileName(ss.getPos());
			//			write(ss, fileName);

			//			if(ss instanceof VoidSystem){
			//				fileName = GameServerState.ENTITY_DATABASE_PATH+getSystemFileName(ss.getPos());
			//				write(((VoidSystem)ss).getStarSystem(), fileName);
			//			}
		}

	}

	/**
	 * triggered on shutdown or autosave
	 *
	 * @param terminate
	 * @param clear
	 * @throws IOException
	 * @throws SQLException
	 */
	public void writeToDatabase(boolean terminate, boolean clear) throws IOException, SQLException {

		writeAdditionalEntities();

		for(Sector s : sectors.values()) {

			if(!s.isActive() && s.isSectorWrittenToDisk() && !terminate) {
				continue;
			}

			if(terminate) {
				s.writeToDisk(Sector.CLEAR_CODE_THREADED, clear, true, this);

			} else if(clear) {
				s.writeToDisk(Sector.CLEAR_CODE_SEQENTIAL, true, false, this);
			} else {
				//this one is triggered on autosave
				s.writeToDisk(Sector.CLEAR_CODE_NONE, false, false, this);
			}

		}
		writeStarSystems();

		writeSimulationState();

		try {
			state.getMetaObjectManager().save();
		} catch(Exception e) {
			e.printStackTrace();
		}

		if(terminate) {
			try {
				state.getConnectionThreadPool().shutdown();
				System.out.println("[SERVER] WAITING FOR LOGIN THREAD POOL TO TERMINATE; Active: " + state.getConnectionThreadPool().getActiveCount());
				state.getConnectionThreadPool().awaitTermination(5, TimeUnit.SECONDS);
				System.out.println("[SERVER] SERVER LOGIN THREAD POOL TERMINATED");

				System.out.println("[SERVER] KILLING ACTIVE EXPLOSION THREADS; Active: " + state.getTheadPoolExplosions().getActiveCount());
				state.getTheadPoolExplosions().shutdownNow();


			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the seed
	 */
	public long getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(long seed) {
		this.seed = seed;
	}

	/**
	 * @return the galaxyManager
	 */
	public GalaxyManager getGalaxyManager() {
		return galaxyManager;
	}

	public void onShutdown() {
		if(galaxyManager != null) {
			galaxyManager.shutdown();
		}
	}

	public StarSystemResourceRequestContainer updateSystemResourcesWithDatabaseValues(Vector3i sysPos, Galaxy galaxy, StarSystemResourceRequestContainer tmp, GalaxyTmpVars tmpVars) {
		VoidSystem voidSystem = starSystemMap.get(sysPos);
		if(voidSystem == null) {
			if(!state.getDatabaseIndex().getTableManager().getSystemTable().updateSystemResources(sysPos, tmp)) {
				tmp = galaxy.getSystemResources(sysPos, tmp, tmpVars);
			}

		} else {
			voidSystem.updateSystemResources(tmp);
		}

		return tmp;
	}

	public void onRemovedSectorSynched(Sector sec) {

		state.getController().onSectorRemovedSynch(sec);
	}

	public void onAddedSectorSynched(Sector sec) {
		for(Faction fac : state.getFactionManager().getFactionCollection()) {
			fac.onAddedSectorSynched(sec);
		}
		state.getController().onSectorAddedSynch(sec);
	}

	public ObjectCollection<Galaxy> getGalaxies() {
		return galaxyMap.values();
	}

	public void scheduleDefferedLoad(Sector sector, EntityUID uid, int delay) {
		DeferredLoad d = new DeferredLoad(sector, uid, delay);
		deffereedLoads.add(d);
	}

	public void attackInSector(Vector3i pos) {
		attackSector.put(pos, lastTime);
	}

	public enum SystemOwnershipType {
		NONE(), BY_ALLY(), BY_ENEMY(), BY_SELF(), BY_NEUTRAL();

		private SystemOwnershipType() {

		}

		/**
		 * @return the miningBonusMult
		 */
		public int getMiningBonusMult() {
			return switch(this) {
				case BY_ALLY -> (int) FactionSystemOwnerBonusConfig.MINING_BONUS_OTHERS;
				case BY_ENEMY -> (int) FactionSystemOwnerBonusConfig.MINING_BONUS_OTHERS;
				case BY_NEUTRAL -> (int) FactionSystemOwnerBonusConfig.MINING_BONUS_OTHERS;
				case BY_SELF -> (int) FactionSystemOwnerBonusConfig.MINING_BONUS_OWNER;
				case NONE -> (int) FactionSystemOwnerBonusConfig.MINING_BONUS_UNOWNED;
				default -> 1;
			};
		}
	}

	private class DeferredLoad {
		Sector sector;
		EntityUID uid;
		int delay;

		public DeferredLoad(Sector sector, EntityUID uid, int delay) {
			super();
			this.sector = sector;
			this.uid = uid;
			this.delay = delay;
		}

	}
}
