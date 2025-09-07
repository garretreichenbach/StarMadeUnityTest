package org.schema.game.common.data.world;

import api.listener.events.entity.SegmentControllerStarDamageEvent;
import api.listener.events.world.generation.AsteroidNormalPopulateEvent;
import api.listener.events.world.generation.AsteroidPositionEvent;
import api.listener.events.world.generation.AsteroidPreSpawnEvent;
import api.listener.events.world.generation.PlanetCreateEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SectorUpdateListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.FloatingRock.RockTemperature;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.HitType;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.damage.projectile.ParticleHitCallback;
import org.schema.game.common.controller.damage.projectile.ProjectileController;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.database.DatabaseInsertable;
import org.schema.game.common.controller.database.tables.SectorItemTable;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.generator.AsteroidCreatorThread;
import org.schema.game.common.controller.io.IOFileManager;
import org.schema.game.common.controller.rails.RailRequest;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.explosion.ExplosionData;
import org.schema.game.common.data.mission.spawner.DefaultSpawner;
import org.schema.game.common.data.mission.spawner.SpawnMarker;
import org.schema.game.common.data.mission.spawner.component.SpawnComponentCreature;
import org.schema.game.common.data.mission.spawner.component.SpawnComponentDestroySpawnerAfterCount;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionCreatureCountOnAffinity;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionPlayerProximity;
import org.schema.game.common.data.mission.spawner.condition.SpawnConditionTime;
import org.schema.game.common.data.physics.CubeRayCastResult;
import org.schema.game.common.data.physics.ModifiedDynamicsWorld;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.game.common.data.world.planet.terrestrial.TerrestrialBodyInformation;
import org.schema.game.common.data.world.space.PlanetCore;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.common.util.Collisionable;
import org.schema.game.server.controller.*;
import org.schema.game.server.controller.gameConfig.GameConfig;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;
import org.schema.game.server.data.CreatureType;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.physics.Physical;
import org.schema.schine.physics.Physics;
import org.schema.schine.physics.PhysicsState;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.UniqueInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.*;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

public class Sector implements TagSerializable, PhysicsState, ParticleHandler, PulseHandler, Damager, DatabaseInsertable, UniqueInterface {
	public static final AsteroidCreatorThread.AsteroidTypeOld[] asteroidsByTemperature;
	public static final int SECTOR_INITIAL = -2;
	public static final int SECTOR_FREE = -1;
	public static final Vector3i DEFAULT_SECTOR = new Vector3i(2, 2, 2);
	public static final int CLEAR_CODE_NONE = 0;
	public static final int CLEAR_CODE_THREADED = 1;
	public static final int CLEAR_CODE_SEQENTIAL = 2;
	public static final int itemDataSize = 2 + 4 + 4 + 4 + 4 + 4;
	//keep this to have consistent positions of spawning
	private static final int DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN = 1000;
	private static final ObjectArrayFIFOQueue<ProjectileController> particleControllerPool = new ObjectArrayFIFOQueue<ProjectileController>();
	private static final ObjectArrayFIFOQueue<PulseController> pulseControllerPool = new ObjectArrayFIFOQueue<PulseController>();
	public static int rockSize = 80;
	public static ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

	static {
		asteroidsByTemperature = new AsteroidCreatorThread.AsteroidTypeOld[AsteroidCreatorThread.AsteroidTypeOld.values().length];
		System.arraycopy(AsteroidCreatorThread.AsteroidTypeOld.values(), 0, asteroidsByTemperature, 0, asteroidsByTemperature.length);
		Arrays.sort(asteroidsByTemperature, (o1, o2) -> (int) ((o1.temperature * 100000.0f) - (o2.temperature * 100000.0f)));
		for(int i = 0; i < asteroidsByTemperature.length; i++) {
			//			System.err.println("ASTEROID: "+asteroidsByTemperature[i] +" :::::: "+asteroidsByTemperature[i].temperature);
		}
	}

	public final Set<EntityUID> entityUids = new ObjectOpenHashSet<EntityUID>();
	final ArrayList<SegmentController> localAdd = new ArrayList<SegmentController>();
	private final Set<SimpleTransformableSendableObject<?>> entities = new ObjectOpenHashSet<SimpleTransformableSendableObject<?>>();
	private final GameServerState state;
	private final SimpleTransformableSendableObjectList[] vicinityObjects = new SimpleTransformableSendableObjectList[27];
	private final ShortOpenHashSet missiles = new ShortOpenHashSet();
	private final Int2ObjectMap<FreeItem> items = new Int2ObjectOpenHashMap<FreeItem>();
	private final ParticleHitCallback particleHitCallback = new ParticleHitCallback();
	private final SectorSunDamager sunDamager = new SectorSunDamager();
	//	public static final float getSectorSize() = 1000;
	public Vector3i pos;
	public boolean terminated;
	Vector3f minOut = new Vector3f();
	Vector3f maxOut = new Vector3f();
	Vector3f minOutOther = new Vector3f();
	Vector3f maxOutOther = new Vector3f();
	Vector3f min = new Vector3f(-rockSize * SegmentData.SEG * Element.BLOCK_SIZE, -rockSize * SegmentData.SEG * Element.BLOCK_SIZE, -rockSize * SegmentData.SEG * Element.BLOCK_SIZE);
	Vector3f max = new Vector3f(rockSize * SegmentData.SEG * Element.BLOCK_SIZE, rockSize * SegmentData.SEG * Element.BLOCK_SIZE, rockSize * SegmentData.SEG * Element.BLOCK_SIZE);
	float stormTimeAccumulated;
	Transform tmpSecPos = new Transform();
	Vector3f tmpDir = new Vector3f();
	Vector3f tmpOPos = new Vector3f();
	Vector3f tmpOUp = new Vector3f();
	Vector3f tmpORight = new Vector3f();
	ArrayList<EditableSendableSegmentController> tmpL = new ArrayList<EditableSendableSegmentController>();
	ArrayList<AbstractCharacter<?>> tmpC = new ArrayList<AbstractCharacter<?>>();
	private float highestSubStep;
	private boolean active;
	private boolean sectorWrittenToDisk;
	private Physics physics;
	private RemoteSector remoteSector;
	private int protectionMode = SectorMode.PROT_NORMAL.code;
	private int id;
	private long lastPing;
	private boolean wasActive;
	private ProjectileController particleController;
	private PulseController pulseController;
	private boolean newCreatedSector;
	private long inactiveTime;
	private float distanceToSun;
	private StellarSystem system;
	private long lastWarning;
	private boolean flagRepair;
	private long dbID = -1;
	private long lastMessage;
	private boolean transientSector = true;
	private long seed;
	private Random random;
	private PlanetIcoCore planetCore;
	private GasPlanet gasPlanet;
	private SectorInformation.PlanetType planetTypeCache;
	private SectorInformation.GasPlanetType gasPlanetTypeCache;
	private SectorInformation.SectorType sectorTypeCache;
	private SpaceStation.SpaceStationType spaceStationTypeCache;
	private float sunIntensity;
	private Vector3i sunPosRel;
	private Vector3i sunPosRelSecond;
	private Vector3i sunOffset;
	private boolean changed;
	private long lastReplenished;

	public Sector(GameServerState state) {
		this.state = state;
		id = state.getNextFreeObjectId();
		physics = state.getUniverse().getPhysicsInstance(this);
		physics.getDynamicsWorld().setInternalTickCallback(new PhysicsCallback(), null);
		inactiveTime = System.currentTimeMillis();
		for(int i = 0; i < vicinityObjects.length; i++) {
			vicinityObjects[i] = new SimpleTransformableSendableObjectList();
		}
	}

	public static InputStream getItemBinaryStream(Map<Integer, FreeItem> items) throws IOException {
		byte[] b = SectorItemTable.getItemBinaryString(items);
		return new ByteArrayInputStream(b);
	}

	public static boolean isNeighbor(Vector3i from, Vector3i to) {
		return (Math.abs(from.x - to.x) <= 1) && (Math.abs(from.y - to.y) <= 1) && (Math.abs(from.z - to.z) <= 1); //sqrt(2)
	}

	public static boolean isNeighborNotSelf(Vector3i from, Vector3i to) {
		return !from.equals(to) && (Math.abs(from.x - to.x) <= 1) && (Math.abs(from.y - to.y) <= 1) && (Math.abs(from.z - to.z) <= 1); //sqrt(2)
	}

	public static boolean isNeighborNotSelf(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
		return !(fromX == toX && fromY == toY && fromZ == toZ) && (Math.abs(fromX - toX) <= 1) && (Math.abs(fromY - toY) <= 1) && (Math.abs(fromZ - toZ) <= 1); //sqrt(2)
	}

	public static boolean isNeighbor(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
		return (Math.abs(fromX - toX) <= 1) && (Math.abs(fromY - toY) <= 1) && (Math.abs(fromZ - toZ) <= 1); //sqrt(2)
	}

	public static void applyBlackHoleGrav(StateInterface state, Vector3i sysPos, Vector3i sunPosRel, Vector3i pos, int sectorId, Transform tmpSecPos, Vector3f tmpOPos, Vector3f tmpDir, Vector3i offset, Timer timer) {
		synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(s instanceof AbstractCharacter<?> c && ((AbstractCharacter<?>) s).getSectorId() == sectorId) {
					if(!c.isHidden() && !(c.getPhysicsDataContainer().getObject() instanceof PairCachingGhostObjectAlignable && ((PairCachingGhostObjectAlignable) c.getPhysicsDataContainer().getObject()).getAttached() != null)) {
						c.getGhostObject();
						Vector3f tmpGrav = new Vector3f();
						Vector3i toSun = new Vector3i((sysPos.x * VoidSystem.SYSTEM_SIZE + sunPosRel.x) - pos.x, (sysPos.y * VoidSystem.SYSTEM_SIZE + sunPosRel.y) - pos.y, (sysPos.z * VoidSystem.SYSTEM_SIZE + sunPosRel.z) - pos.z);
						if(state instanceof GameServerState && toSun.length() == 0) {
							try {
								if(c.getGhostObject().getAttached() == null) {
									c.damage(100000, ((GameServerState) state).getUniverse().getSector(pos));
								}
							} catch(IOException e) {
								e.printStackTrace();
							}
						} else {
							tmpSecPos.origin.set(toSun.x, toSun.y, toSun.z);
							tmpSecPos.origin.scale(((GameStateInterface) state).getSectorSize());
							tmpOPos.set(((AbstractCharacter<?>) s).getWorldTransform().origin);
							tmpDir.sub(tmpSecPos.origin, tmpOPos);
							if(tmpDir.length() > 0 && c.getGhostObject().getAttached() == null) {
								float len = tmpDir.length();
								float max = ((GameStateInterface) state).getGameState().getMaxGalaxySpeed() * 0.73f;
								float sysTot = ((GameStateInterface) state).getSectorSize() * VoidSystem.SYSTEM_SIZE / 2;
								tmpDir.normalize();
								tmpDir.scale(((Math.max(0.25f, 1.0f - len / sysTot)) * max) * timer.getDelta());
								Transform t = new Transform();
								c.getGhostObject().getWorldTransform(t);
								t.origin.add(tmpDir);
								c.getGhostObject().setWorldTransform(t);
							}
						}
					}
				} else if(s instanceof SegmentController && ((SegmentController) s).getMass() > 0 && ((SegmentController) s).getPhysicsDataContainer().getObject() != null && ((SegmentController) s).getSectorId() == sectorId) {
					RigidBody b = (RigidBody) ((SegmentController) s).getPhysicsDataContainer().getObject();
					Vector3f tmpGrav = new Vector3f();
					b.getGravity(tmpGrav);
					Vector3i toSun = new Vector3i((sysPos.x * VoidSystem.SYSTEM_SIZE + sunPosRel.x) - pos.x, (sysPos.y * VoidSystem.SYSTEM_SIZE + sunPosRel.y) - pos.y, (sysPos.z * VoidSystem.SYSTEM_SIZE + sunPosRel.z) - pos.z);
					tmpSecPos.origin.set(toSun.x, toSun.y, toSun.z);
					if(state instanceof GameServerState serverState && toSun.length() == 0) {
						FTLConnection ftl = serverState.getDatabaseIndex().getTableManager().getFTLTable().getFtl(pos, "BH_" + pos.x + "_" + pos.y + "_" + pos.z + "_OO_" + offset.x + "_" + offset.y + "_" + offset.z);
						if(ftl != null) {
							Vector3i to = ftl.to.get(0);
							//dont warp player right into the next black hole
							to.add(6, 6, 6);
							((SegmentController) s).getNetworkObject().graphicsEffectModifier.add((byte) 1);
							SectorSwitch queueSectorSwitch = serverState.getController().queueSectorSwitch(((SegmentController) s), to, SectorSwitch.TRANS_JUMP, false, true, true);
							if(queueSectorSwitch != null) {
								System.err.println("[SERVER][BlackHole] JUMPING TO: BH_" + pos.x + "_" + pos.y + "_" + pos.z + "; offset: " + offset);
								queueSectorSwitch.delay = System.currentTimeMillis() + 8000;
								queueSectorSwitch.jumpSpawnPos = new Vector3f();
								queueSectorSwitch.keepJumpBasisWithJumpPos = true;
								queueSectorSwitch.executionGraphicsEffect = 2;
								((SegmentController) s).sendControllingPlayersServerMessage(Lng.astr("A wormhole is warping you to \nsector", to), ServerMessage.MESSAGE_TYPE_INFO);
							}
						} else {
							System.err.println("[SERVER][BlackHole] cannot warp player. no jump route for black hole found: BH_" + pos.x + "_" + pos.y + "_" + pos.z + "_OO_" + offset.x + "_" + offset.y + "_" + offset.z + "; offset: " + offset);
						}
					}
					tmpSecPos.origin.scale(((GameStateInterface) state).getSectorSize());
					tmpOPos.set(((SegmentController) s).getWorldTransform().origin);
					float oSize = ((SegmentController) s).getSegmentBuffer().getBoundingBox().getSize();
					oSize /= 3;
					tmpDir.sub(tmpSecPos.origin, tmpOPos);
					float antiGravityStrength = ((SendableSegmentController) s).getBlockEffectManager().status.antiGravity;
					if(antiGravityStrength > 0) tmpDir.scale(1.0f - antiGravityStrength);
					if(tmpDir.length() > 0) {
						float len = tmpDir.length();
						float max = ((GameStateInterface) state).getGameState().getMaxGalaxySpeed() * 0.33f;
						float sysTot = ((GameStateInterface) state).getSectorSize() * VoidSystem.SYSTEM_SIZE / 2;
						tmpDir.normalize();
						tmpDir.scale((Math.max(0.25f, 1.0f - len / sysTot)) * max);
						b.setGravity(tmpDir);
						b.applyGravity();
						Vector3f linearVelocity = b.getLinearVelocity(new Vector3f());
						if(linearVelocity.length() > ((GameStateInterface) state).getGameState().getMaxGalaxySpeed() * 3) {
							linearVelocity.normalize();
							linearVelocity.scale(((GameStateInterface) state).getGameState().getMaxGalaxySpeed() * 3);
						}
					}
					b.setGravity(tmpGrav);
				}
			}
		}
	}

	public static boolean isPersonalOrTestSector(Vector3i pos) {
		return pos.x == Integer.MAX_VALUE - 32 && pos.z == Integer.MAX_VALUE - 32;
	}

	public static boolean isTutorialSector(Vector3i pos) {
		return pos.x >= 130000000 * 16 && pos.y >= 130000000 * 16 && pos.z >= 130000000 * 16 && pos.x < 130000000 * 16 + 16 && pos.y < 130000000 * 16 + 16 && pos.z < 130000000 * 16 + 16;
	}

	public static boolean isMode(int pMode, SectorMode p) {
		return isMode(pMode, p.code);
	}

	public static boolean isMode(int pMode, int p) {
		return (pMode & p) == p;
	}

	private static String uidToString(Set<EntityUID> e) {
		StringBuffer f = new StringBuffer();
		Iterator<EntityUID> it = e.iterator();
		while(it.hasNext()) {
			EntityUID next = it.next();
			f.append(next.uid + "(dbId " + next.id + ")");
			if(it.hasNext()) {
				f.append("; ");
			}
		}
		return f.toString();
	}

	public static String getPermissionString(int perm) {
		return "[Peace,Protected,NoEnter,NoExit,NoIndication,NoFpLoss]: " + getPermissionBit(perm, SectorMode.PROT_NO_SPAWN) + getPermissionBit(perm, SectorMode.PROT_NO_ATTACK) + getPermissionBit(perm, SectorMode.LOCK_NO_ENTER) + getPermissionBit(perm, SectorMode.LOCK_NO_EXIT) + getPermissionBit(perm, SectorMode.NO_INDICATIONS) + getPermissionBit(perm, SectorMode.NO_FP_LOSS);
	}

	public static int getPermissionBit(int from, SectorMode mode) {
		return getPermissionBit(from, mode.code);
	}

	public static int getPermissionBit(int from, int mode) {
		return isMode(from, mode) ? 1 : 0;
	}

	public static void writeSingle(GameServerState state, Sendable s) throws IOException, SQLException {
		state.getController().writeEntity((DiskWritable) s, true);
		if(s instanceof SendableSegmentController seg) {
			int written = seg.writeAllBufferedSegmentsToDatabase(false, false, false);
			if(s instanceof Ship || s instanceof SpaceStation) {
				System.err.println("[SERVER][DEBUG] WRITTEN " + s + "; lastWrite " + seg.getLastWrite() + "; written segments: " + written);
			}
			//Making sure its physically on the disk by forcing it out
			IOFileManager.writeAllOpenFiles(((SendableSegmentController) s).getSegmentProvider().getSegmentDataIO().getManager());
		}
	}

	public void clearVicinity() {
		for(int i = 0; i < vicinityObjects.length; i++) {
			vicinityObjects[i].clear();
		}
	}

	public void updateVicinity(SimpleTransformableSendableObject o) {
		Sector entitySector = state.getUniverse().getSector(o.getSectorId());
		if(isNeighbor(pos, entitySector.pos)) {
			int vicinityIndex = getVicinityIndex(entitySector.pos);
			vicinityObjects[vicinityIndex].add(o);
		}
	}

	public int getVicinityIndex(Vector3i otherPos) {
		int x = (otherPos.x - pos.x) + 1;
		int y = (otherPos.x - pos.x) + 1;
		int z = (otherPos.x - pos.x) + 1;
		return z * 9 + y * 3 + x;
	}

	public void addMetaItems() {
		state.getMetaObjectManager().getFromArchive(pos, items);
	}

	public void addRandomRock(GameServerState state, long seed, int sizeX, int sizeY, int sizeZ, Random random, int index) throws IOException {
		Vector3i posInSec = new Vector3i();
		setRandomPos(posInSec, random);
		Vector3i posFromSector = StellarSystem.getPosFromSector(this.pos, new Vector3i());
		StellarSystem stellar = state.getUniverse().getStellarSystemFromSecPos(posFromSector);

		byte[] ores = stellar.getNewAsteroidResources(this.pos, random);
		byte[] freq = new byte[ores.length];
		for(int i = 0; i < ores.length; i++) {
			freq[i] = stellar.systemResources.res[ores[i]];
			if(freq[i] == 0) freq[i] = 127; //presume it's an override
		}

		RockTemperature temp = RockTemperature.fromFloat(stellar.getTemperature(this.pos));
		FloatingRock rock = new FloatingRock(state, stellar.getRockMaterials(), temp, ores, freq);
		rock.setSeed(seed);
		int rand = random.nextInt(5);
		int deviation = rand - 2;

		int sizeChunkX = ByteUtil.divSeg(sizeX - 1) + 1;
		int sizeChunkY = ByteUtil.divSeg(sizeY - 1) + 1;
		int sizeChunkZ = ByteUtil.divSeg(sizeZ - 1) + 1;
		rock.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.ASTEROID.dbPrefix + System.currentTimeMillis() + "_" + this.pos.x + "_" + this.pos.y + "_" + this.pos.z + "_" + index);
		rock.getMinPos().set(-sizeChunkX, -sizeChunkY, -sizeChunkZ);
		rock.getMaxPos().set(sizeChunkX - 1, sizeChunkY - 1, sizeChunkZ - 1);
		rock.loadedMinPos = new Vector3i(rock.getMinPos());
		rock.loadedMaxPos = new Vector3i(rock.getMaxPos());
		rock.loadedGenSize = new Vector3i(sizeX, sizeY, sizeZ);
		rock.setId(state.getNextFreeObjectId());
		rock.setSectorId(id);
		//INSERTED CODE
		AsteroidPreSpawnEvent ase = new AsteroidPreSpawnEvent(rock, this, stellar);
		StarLoader.fireEvent(ase, true);
		///
		rock.initialize();
		int i = 0;
		long time = System.currentTimeMillis();
		while(checkCollision(rock, posInSec) != null && i < 1000) {
			//			System.err.println("RANDOM POS INCREASE: "+pos+"; "+i);
			setRandomPos(posInSec, random);
			i++;
		}
		//INSERTED CODE
		AsteroidPositionEvent ap = new AsteroidPositionEvent(this, rock, posInSec, index);
		StarLoader.fireEvent(ap, true);
		if(ap.posChanged()) {
			posInSec = ap.getPos();
			index = 1; //reset the counter; trust the modder with their own "placement" checks.
			//still no clue what any of this is, but it should be testable this way
		}
		///
		rock.warpTransformable(posInSec.x, posInSec.y, posInSec.z, true, null);

		long took = (System.currentTimeMillis() - time);
		if(took > 10) {
			System.err.println("[SECTOR] Placing ROCK took " + took + "ms");
		}
		if(i < 1000) {
			localAdd.add(rock);
			state.getController().getSynchController().addImmediateSynchronizedObject(rock);
		} else {
			try {
				throw new RuntimeException("Could not place rock " + rock.getMinPos() + "; " + rock.getMaxPos());
			} catch(RuntimeException e) {
				//				e.printStackTrace();
				//				System.out.println("[ERRORLOG][SECTOR] Could not place rock " + rock.getMinPos() + "; " + rock.getMaxPos());
				//				System.out.println("[ERRORLOG][SECTOR] PRINTING AABB of all objects");
				//				for (Sendable s : localAdd) {
				//					if (s instanceof Physical) {
				//						if (s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getSectorId() != rock.getSectorId()) {
				//							continue;
				//						}
				//						Physical p = ((Physical) s);
				//						//						System.err.println("[Sector][LocalCollisionCheck] "+p+":: "+p.getPhysicsDataContainer()+"; "+p.getPhysicsDataContainer().getShape());
				//						p.getPhysicsDataContainer().getShape().getAabb(p.getPhysicsDataContainer().getCurrentPhysicsTransform(), minOutOther, maxOutOther);
				//						System.out.println("[ERRORLOG][SECTOR] " + s + ": [" + minOutOther + " " + maxOutOther + "]");
				//					}
				//
				//				}
			}
		}
	}

	private Sendable checkCollision(SegmentController so, Vector3i pos) {
		long time = System.currentTimeMillis();
		if(so instanceof SegmentController) {
			SegmentController c = so;
			c.getInitialTransform().setIdentity();
			c.getInitialTransform().origin.set(pos.x, pos.y, pos.z);
			min.set((c.getMinPos().x - 2) * SegmentData.SEG, (c.getMinPos().y - 2) * SegmentData.SEG, (c.getMinPos().z - 2) * SegmentData.SEG);
			max.set((c.getMaxPos().x + 2) * SegmentData.SEG, (c.getMaxPos().y + 2) * SegmentData.SEG, (c.getMaxPos().z + 2) * SegmentData.SEG);
			//		System.err.println(min+"; "+bb+": "+c);
			AabbUtil2.transformAabb(min, max, 100 * Element.BLOCK_SIZE, c.getInitialTransform(), minOut, maxOut);
			for(Sendable s : localAdd) {
				if(s instanceof Physical) {
					if(s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getSectorId() != c.getSectorId()) {
						continue;
					}
					if(s instanceof SegmentController cc) {
						minOutOther.set((cc.getMinPos().x - 2) * SegmentData.SEG, (cc.getMinPos().y - 2) * SegmentData.SEG, (cc.getMinPos().z - 2) * SegmentData.SEG);
						maxOutOther.set((cc.getMaxPos().x + 2) * SegmentData.SEG, (cc.getMaxPos().y + 2) * SegmentData.SEG, (cc.getMaxPos().z + 2) * SegmentData.SEG);
						AabbUtil2.transformAabb(minOutOther, maxOutOther, 100 * Element.BLOCK_SIZE, cc.getInitialTransform(), minOutOther, maxOutOther);
					} else {
						Physical p = ((Physical) s);
						p.getPhysicsDataContainer().getShape().getAabb(p.getInitialTransform(), minOutOther, maxOutOther);
					}
					if(AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOutOther, maxOutOther)) {
						long took = (System.currentTimeMillis() - time);
						if(took > 10) {
							System.err.println("[Sector] [Sector] collision test at " + pos + " is true: trying another pos " + took + "ms");
						}
						return s;
					}
				}
			}
		}
		long took = (System.currentTimeMillis() - time);
		if(took > 10) {
			System.err.println("[Sector] No Collission: " + took + "ms");
		}
		return null;
	}

	public Sendable checkSectorCollision(SimpleTransformableSendableObject so, Vector3f pos) {
		long time = System.currentTimeMillis();
		Transform t = new Transform();
		t.basis.set(so.getWorldTransform().basis);
		t.origin.set(pos);
		so.getRemoteTransformable().getPhysicsDataContainer().getShape().getAabb(t, minOut, maxOut);
		//		System.err.println("CHECKING NOW: "+t.origin+": ["+minOut+", "+maxOut+"]");
		synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(s instanceof Physical p) {
					if(s instanceof SimpleTransformableSendableObject && ((SimpleTransformableSendableObject) s).getSectorId() != so.getSectorId()) {
						continue;
					}
					p.getPhysicsDataContainer().getShape().getAabb(p.getPhysicsDataContainer().getCurrentPhysicsTransform(), minOutOther, maxOutOther);
					if(AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, minOutOther, maxOutOther)) {
						//
						long took = (System.currentTimeMillis() - time);
						if(took > 10) {
							System.err.println("[Sector] collision test at " + pos + " is true: trying another pos " + minOut + ", " + maxOut + " ---> " + minOutOther + ", " + maxOutOther + ": " + took + "ms");
						}
						return s;
					}
				}
			}
		}
		long took = (System.currentTimeMillis() - time);
		if(took > 10) {
			System.err.println("[Sector] No Collission: " + took + "ms");
		}
		return null;
	}

	public void cleanUp() {
		synchronized(state.getLocalAndRemoteObjectContainer()) {
			//			System.err.println("Cleaning up Sector");
			((ModifiedDynamicsWorld) physics.getDynamicsWorld()).clean();
			state.getUniverse().addToFreePhysics(physics, this);
		}
	}

	private void doSunstorm(Timer timer, float invDistNorm) {
		stormTimeAccumulated += timer.getDelta();
		int i = 0;
		GameConfig gameConfig = state.getGameConfig();
		float stormTime = Math.max(0.1f, invDistNorm * gameConfig.sunMaxDelayBetweenHits);
		if(stormTimeAccumulated > stormTime) {
			//			Universe.calcSunPosInnerStarSystem(state, pos, system, state.getController().calculateStartTime(), tmpSecPos);
			tmpSecPos.setIdentity();
			Vector3i toSun = new Vector3i((system.getPos().x * VoidSystem.SYSTEM_SIZE + sunPosRel.x) - pos.x, (system.getPos().y * VoidSystem.SYSTEM_SIZE + sunPosRel.y) - pos.y, (system.getPos().z * VoidSystem.SYSTEM_SIZE + sunPosRel.z) - pos.z);
			if(sunPosRelSecond != null) {
				Vector3i toSun2 = new Vector3i((system.getPos().x * VoidSystem.SYSTEM_SIZE + sunPosRelSecond.x) - pos.x, (system.getPos().y * VoidSystem.SYSTEM_SIZE + sunPosRelSecond.y) - pos.y, (system.getPos().z * VoidSystem.SYSTEM_SIZE + sunPosRelSecond.z) - pos.z);
				if(toSun2.length() < toSun.length()) {
					toSun = toSun2;
				}
			}
			tmpSecPos.origin.set(toSun.x, toSun.y, toSun.z);
			tmpSecPos.origin.scale(((GameStateInterface) state).getSectorSize());
			tmpL.clear();
			tmpC.clear();
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(s instanceof Ship && ((SegmentController) s).getSectorId() == id) {
					tmpL.add(((EditableSendableSegmentController) s));
				} else if(s instanceof AbstractCharacter<?> && !((AbstractCharacter<?>) s).isHidden() && ((AbstractCharacter<?>) s).getSectorId() == id) {
					tmpC.add((AbstractCharacter<?>) s);
				}
			}
			//			System.err.println("sunstorm: "+tmpC);
			for(int k = 0; k < tmpC.size(); k++) {
				AbstractCharacter<?> abstractCharacter = tmpC.get(k);
				//				System.err.println("DAMAGING: "+abstractCharacter);
				if(Math.random() > (distanceToSun == 0 ? 0.001 : 0.00005)) {
					if(abstractCharacter.getGravity().source == null) {
						abstractCharacter.damage(1, this);
						abstractCharacter.sendControllingPlayersServerMessage(Lng.astr("You are taking damage\nfrom being too close to\na star!"), ServerMessage.MESSAGE_TYPE_ERROR);
					}
				}
			}
			if(tmpL.isEmpty()) {
				return;
			}
			EditableSendableSegmentController randomSecController = tmpL.get(random.nextInt(tmpL.size()));
			if(randomSecController.heatDamageId == null || !randomSecController.heatDamageId.equals(system.getPos())) {
				randomSecController.heatDamageId = system.getPos();
				randomSecController.heatDamageStart = timer.currentTime;
				randomSecController.sendControllingPlayersServerMessage(Lng.astr("### WARNING ###\nThe close proximity to a star is heating up your ship.\n%s sec before taking damage", (int) gameConfig.sunDamageDelay), ServerMessage.MESSAGE_TYPE_ERROR);
				return;
			}
			if(((double) (timer.currentTime - randomSecController.heatDamageStart)) < gameConfig.sunDamageDelay * 1000L) {
				return;
			}
			//			System.err.println("TAKING DAMAGE ON "+randomSecController+": "+damage);
			tmpOPos.set(randomSecController.getWorldTransform().origin);
			float oSize = randomSecController.getSegmentBuffer().getBoundingBox().getSize();
			oSize /= 3;
			tmpDir.sub(tmpOPos, tmpSecPos.origin);
			float len = tmpDir.length();
			tmpDir.normalize();
			tmpOUp.set(0, 1, 0);
			tmpORight.cross(tmpDir, tmpOUp);
			tmpORight.normalize();
			tmpOUp.cross(tmpORight, tmpDir);
			tmpOUp.normalize();
			tmpDir.scale(len + oSize);
			tmpOPos.add(tmpSecPos.origin, tmpDir);
			//			GlUtil.getUpVector(tmpOUp, randomSecController.getWorldTransform());
			//			GlUtil.getRightVector(tmpORight, randomSecController.getWorldTransform());
			tmpOUp.scale(oSize * ((random.nextFloat() - 0.5f) * 2.0f));
			tmpORight.scale(oSize * ((random.nextFloat() - 0.5f) * 2.0f));
			tmpOPos.add(tmpOUp);
			tmpOPos.add(tmpORight);
			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
				DebugPoint p = new DebugPoint(new Vector3f(tmpOPos), new Vector4f(1, 1, 0, 1), 10);
				DebugDrawer.points.add(p);
				DebugLine l = new DebugLine(tmpSecPos.origin, tmpOPos);
				DebugDrawer.lines.add(l);
			}
			CollisionWorld.ClosestRayResultCallback testRayCollisionPoint = ((PhysicsExt) physics).testRayCollisionPoint(tmpSecPos.origin, tmpOPos, false, null,/*randomSecController*/null, false, true, false);
			if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
				if(((CubeRayCastResult) testRayCollisionPoint).getSegment() != null) {
					SegmentController c = ((CubeRayCastResult) testRayCollisionPoint).getSegment().getSegmentController();
					if(c.railController.isInAnyRailRelationWith(randomSecController)) {
						randomSecController = (EditableSendableSegmentController) ((CubeRayCastResult) testRayCollisionPoint).getSegment().getSegmentController();
						short effectType = 0;
						float effetRatio = 0;
						float effetSize = 0;
						float damage = Math.max(0.1f, sunIntensity * (gameConfig.sunDamagePerBlock * randomSecController.railController.getRoot().getTotalElementsIncRails()));
						damage = Math.min(gameConfig.sunDamageMax, Math.max(damage, gameConfig.sunDamageMin));
						float damageBeforeShields = damage;
						particleHitCallback.reset();
						//						randomSecController.handleHit(particleHitCallback, testRayCollisionPoint, this, damage, damageBeforeShields, tmpSecPos.origin, tmpOPos, false, effectType, effetRatio, effetSize);
						CubeRayCastResult r = (CubeRayCastResult) testRayCollisionPoint;
						Transform where = new Transform();
						where.setIdentity();
						where.origin.set(r.hitPointWorld);
						float radius = gameConfig.sunDamageRadius;
						boolean shieldHit = false;
						if(randomSecController instanceof ManagedSegmentController<?>) {
							//check if the point of damage is within an active shield
							ManagerContainer<?> m = ((ManagedSegmentController<?>) randomSecController).getManagerContainer();
							if(m instanceof ShieldContainerInterface) {
								ShieldLocalAddOn shield = ((ShieldContainerInterface) m).getShieldAddOn().getShieldLocalAddOn();
								Vector3f whereLocal = new Vector3f(where.origin);
								randomSecController.getWorldTransformInverse().transform(whereLocal);
								whereLocal.x += Segment.HALF_DIM;
								whereLocal.y += Segment.HALF_DIM;
								whereLocal.z += Segment.HALF_DIM;
								ShieldLocal shieldInRadius = shield.getShieldInRadius((ShieldContainerInterface) m, whereLocal);
								if(shieldInRadius != null && shieldInRadius.active && shieldInRadius.getShields() > 0) {
									shieldHit = true;
								}
							}
						}
						//						if(shieldHit){
						//							damage = randomSecController.getConfigManager().apply(StatusEffectType.SHIELD_HEAT_DAMAGE_TAKEN, damage);
						//						}else{
						damage = randomSecController.getConfigManager().apply(StatusEffectType.HULL_HEAT_DAMAGE_TAKEN, damage);
						//						}
						long weaponId = Long.MIN_VALUE;
						//INSERTED CODE
						SegmentControllerStarDamageEvent event = new SegmentControllerStarDamageEvent(randomSecController, pos, damage, where, radius);
						StarLoader.fireEvent(event, true);
						if(event.isCanceled()) return;
						else {
							damage = event.getDamage();
							where = event.getWhere();
							radius = event.getRadius();
						}
						//
						randomSecController.addExplosion(sunDamager, DamageDealerType.EXPLOSIVE, HitType.ENVIROMENTAL, weaponId, where, radius, damage, true, () -> {
						}, ExplosionData.INNER | ExplosionData.IGNORESHIELDS_GLOBAL);
						if(randomSecController instanceof PlayerControllable && System.currentTimeMillis() - lastMessage > 3000) {
							randomSecController.sendControllingPlayersServerMessage(Lng.astr("WARNING!\nthe heat of a star is\ndamaging your hull!\nHide behind natural objects!\nINTENSITY: %s", damage), ServerMessage.MESSAGE_TYPE_ERROR);
							lastMessage = System.currentTimeMillis();
						}
					}
				}
			}
			i++;
			stormTimeAccumulated = 0;
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] top = (Tag[]) tag.getValue();
		pos = (Vector3i) top[0].getValue();
		assert (pos != null);
		Tag[] idents = (Tag[]) top[1].getValue();
		for(Tag t : idents) {
			if(t.getType() == Tag.Type.FINISH) {
				break;
			}
			String uid = (String) t.getValue();
			//			System.err.println("READING FROM SECTOR "+pos+": "+ uid);
			try {
				entityUids.add(new EntityUID(uid, DatabaseEntry.getEntityType(uid), -1L));
			} catch(DatabaseEntry.EntityTypeNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(top.length > 2 && top[2].getType() == Tag.Type.INT) {
			protectionMode = (Integer) top[2].getValue();
		}
		if(top.length > 3 && top[3].getType() == Tag.Type.STRUCT) {
			Tag[] t = (Tag[]) top[3].getValue();
			for(int i = 0; i < t.length - 1; i++) {
				FreeItem it = new FreeItem();
				it.fromTagStructure(t[i], state);
				it.setId(GameServerState.itemIds);
				GameServerState.itemIds++;
				if(it.getType() != Element.TYPE_NONE) {
					items.put(it.getId(), it);
				}
			}
		}
		System.err.println("Read From Disk: " + this);
	}

	@Override
	public Tag toTagStructure() {
		ObjectArrayList<SimpleTransformableSendableObject> entities = updateEntities();
		Tag[] idents = new Tag[entities.size() + 1];
		int i = 0;
		for(int g = 0; g < entities.size(); g++) {
			idents[i] = new Tag(Tag.Type.STRING, null, entities.get(g).getUniqueIdentifier());
			i++;
		}
		i = 0;
		Tag[] items = new Tag[getItems().size() + 1];
		for(FreeItem item : getItems().values()) {
			items[i] = item.toTagStructure(state);
			i++;
		}
		items[items.length - 1] = FinishTag.INST;
		idents[entities.size()] = FinishTag.INST;
		return new Tag(Tag.Type.STRUCT, "SECTOR", new Tag[]{new Tag(Tag.Type.VECTOR3i, "POS", pos), new Tag(Tag.Type.STRUCT, "idents", idents), new Tag(Tag.Type.INT, "PROT", protectionMode), new Tag(Tag.Type.STRUCT, "items", items), FinishTag.INST});
	}

	/**
	 * @return the dbID
	 */
	public long getDBId() {
		return dbID;
	}

	/**
	 * @param dbID the dbID to set
	 */
	public void setDBId(long dbID) {
		this.dbID = dbID;
	}

	/**
	 * @return the highestSubStep
	 */
	public float getHighestSubStep() {
		return highestSubStep;
	}

	/**
	 * @param highestSubStep the highestSubStep to set
	 */
	public void setHighestSubStep(float highestSubStep) {
		this.highestSubStep = highestSubStep;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public InputStream getItemBinaryStream() throws IOException {
		return getItemBinaryStream(items);
	}

	public Int2ObjectMap<FreeItem> getItems() {
		if(remoteSector != null) {
			return remoteSector.getItems();
		} else {
			return items;
		}
	}

	/**
	 * @return the lastPing
	 */
	public long getLastPing() {
		return lastPing;
	}

	@Override
	public float getLinearDamping() {
		if(state.getGameState() != null) {
			return state.getGameState().getLinearDamping();
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * @return the physics
	 */
	@Override
	public Physics getPhysics() {
		return physics;
	}

	/**
	 * @param physics the physics to set
	 */
	public void setPhysics(Physics physics) {
		this.physics = physics;
	}

	@Override
	public String getPhysicsSlowMsg() {
		return "[PHYSICS][SERVER] WARNING: PHYSICS SYNC IN DANGER. SECTOR: " + pos + " [" + id + "]";
	}

	@Override
	public float getRotationalDamping() {
		if(state.getGameState() != null) {
			return state.getGameState().getRotationalDamping();
		} else {
			throw new NullPointerException();
		}
	}

	@Override
	public void handleNextPhysicsSubstep(float maxPhysicsSubsteps) {
		highestSubStep = Math.max(highestSubStep, maxPhysicsSubsteps);
		//		if(getHighestSubStep() > 0){
		//			System.err.println("NNEIFNIND: "+getHighestSubStep());
		//		}
	}

	@Override
	public String toStringDebug() {
		return "Sector[" + id + "]" + pos + "; @" + physics;
	}

	@Override
	public short getNumberOfUpdate() {
		return state.getNumberOfUpdate();
	}

	@Override
	public ProjectileController getParticleController() {
		return particleController;
	}

	private void getParticleControllerFromPool() {
		if(particleControllerPool.isEmpty()) {
			particleController = new ProjectileController(state, id);
		} else {
			ProjectileController remove = particleControllerPool.dequeue();
			remove.setSectorId(id);
			particleController = remove;
		}
	}

	public SectorInformation.PlanetType getPlanetType() throws IOException {
		if(planetTypeCache == null) {
			StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
			planetTypeCache = sys.getPlanetType(pos);
		}
		return planetTypeCache;
	}

	public SectorInformation.GasPlanetType getGasPlanetType() throws IOException {
		if(gasPlanetTypeCache == null) {
			StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
			gasPlanetTypeCache = sys.getGasPlanetType(pos);
		}
		return gasPlanetTypeCache;
	}

	/**
	 * @return the protectionMode
	 */
	public int getProtectionMode() {
		return protectionMode;
	}

	/**
	 * @param protectionMode the protectionMode to set
	 */
	public void setProtectionMode(int protectionMode) {
		this.protectionMode = protectionMode;
	}

	/**
	 * @return the pulseController
	 */
	@Override
	public PulseController getPulseController() {
		return pulseController;
	}

	/**
	 * @param pulseController the pulseController to set
	 */
	public void setPulseController(PulseController pulseController) {
		this.pulseController = pulseController;
	}

	private void getPulseControllerFromPool() {
		if(pulseControllerPool.isEmpty()) {
			pulseController = new PulseController(state, id);
		} else {
			PulseController remove = pulseControllerPool.dequeue();
			remove.setSectorId(id);
			pulseController = remove;
		}
	}

	/**
	 * @return the remoteSector
	 */
	public RemoteSector getRemoteSector() {
		return remoteSector;
	}

	/**
	 * @param remoteSector the remoteSector to set
	 */
	public void setRemoteSector(RemoteSector remoteSector) {
		this.remoteSector = remoteSector;
	}

	public SectorInformation.SectorType getSectorType() throws IOException {
		if(sectorTypeCache == null) {
			StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
			sectorTypeCache = sys.getSectorType(pos);
		}
		return sectorTypeCache;
	}

	public SpaceStation.SpaceStationType getStationType() throws IOException {
		if(spaceStationTypeCache == null) {
			StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
			spaceStationTypeCache = sys.getSpaceStationTypeType(pos);
		}
		return spaceStationTypeCache;
	}

	@Override
	public String getUniqueIdentifier() {
		return "SECTOR_" + pos.x + "." + pos.y + "." + pos.z;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return id == ((Sector) obj).id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Sector[" + id + "]" + pos;
	}

	public boolean hasSectorRemoveTimeout(long currentTime) {
		int to = (ServerConfig.SECTOR_INACTIVE_CLEANUP_TIMEOUT.getInt());
		return to >= 0 && currentTime > inactiveTime + to * 1000L;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		if(!this.active && active) {
			synchronized(state.getLocalAndRemoteObjectContainer()) {
				ObjectArrayList<SimpleTransformableSendableObject> entsToWrite = updateEntities();
				for(SimpleTransformableSendableObject<?> o : entsToWrite) {
					if(o.isWrittenForUnload()) {
						System.err.println("[SERVER][DEBUG] Reactivated entity " + o + ", so it will be saved again (to prevent rollback bug). Sector: " + this);
						o.setWrittenForUnload(false);
					}
				}
				sectorWrittenToDisk = false;
				wasActive = true;
				ping();
				getParticleControllerFromPool();
				getPulseControllerFromPool();
				this.active = active;
			}
		} else {
			if(this.active && !active) {
				releaseParticleControllerFromPool();
				releasePulseControllerFromPool();
				inactiveTime = System.currentTimeMillis();
			}
			this.active = active;
		}
	}

	public boolean isNoIndications() {
		return isMode(protectionMode, SectorMode.NO_INDICATIONS);
	}

	public boolean isNoFPLoss() {
		return isMode(protectionMode, SectorMode.NO_FP_LOSS);
	}

	public boolean isPeace() {
		return isMode(protectionMode, SectorMode.PROT_NO_SPAWN);
	}

	public boolean isProtected() {
		return isMode(protectionMode, SectorMode.PROT_NO_ATTACK);
	}

	public boolean isNoEntry() {
		return isMode(protectionMode, SectorMode.LOCK_NO_ENTER);
	}

	public boolean isNoExit() {
		return isMode(protectionMode, SectorMode.LOCK_NO_EXIT);
	}

	/**
	 * @return the sectorWrittenToDisk
	 */
	public boolean isSectorWrittenToDisk() {
		return sectorWrittenToDisk;
	}

	public void loadEntities(GameServerState state) throws IOException, SQLException {
		ObjectArrayList<Sendable> loaded = new ObjectArrayList<Sendable>(entityUids.size());
		int currentRockCount = 0;
		for(EntityUID uid : entityUids) {
			if(uid.type == SimpleTransformableSendableObject.EntityType.SHIP && uid.spawnedOnlyInDb) {
				this.state.getUniverse().scheduleDefferedLoad(this, uid, 2);
				System.err.println("[SERVER][SECTOR] Deferred loading of database spawned entity: " + uid);
				continue;
			}
			Sendable loadEntitiy = loadEntitiy(state, uid);
			if(loadEntitiy != null) {
				loaded.add(loadEntitiy);
			}
			if(loadEntitiy != null && (loadEntitiy instanceof FloatingRock || loadEntitiy instanceof FloatingRockManaged)) {
				currentRockCount++;
			}
		}
		if(!transientSector && currentRockCount == entityUids.size() && ServerConfig.ASTEROID_SECTOR_REPLENISH_TIME_SEC.getInt() > -1) {
			int rockCount = getRockCount(getSectorType());
			long msTime = ServerConfig.ASTEROID_SECTOR_REPLENISH_TIME_SEC.getInt() * 1000L;
			if(rockCount > currentRockCount && System.currentTimeMillis() - lastReplenished > msTime) {
				if(rockCount > 0 || currentRockCount == 1) {
					System.err.println("[SECTOR] REPLENISHING ASTEROIDS IN SECTOR " + pos + ": LAST REPLENISHED: " + (new Date(lastReplenished)));
					setTransientSector(true);
					lastReplenished = System.currentTimeMillis();
				}
			}
		}
		if(transientSector) {
			if(EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("noasteroids")) {
			} else {
				//this will populate an untouched sector with the seed of this sector
				populateAsteroids(getSectorType(), random);
			}
		}
	}

	public SegmentController loadSingleEntitiyWithDock(GameServerState state, EntityUID uid, boolean design) throws SQLException {
		Sendable loadEntitiy = loadEntitiy(state, uid);
		if(loadEntitiy != null && loadEntitiy instanceof SegmentController s) {
			s.setVirtualBlueprint(design);
			for(RailRequest r : s.railController.getExpectedToDock()) {
				String dockedUID = r.docked.uniqueIdentifierSegmentController;
				loadSingleEntitiyWithDock(state, new EntityUID(dockedUID, DatabaseEntry.getEntityType(dockedUID), -1), design);
			}
			return (SegmentController) loadEntitiy;
		}
		return null;
	}

	public Sendable loadEntitiy(GameServerState state, EntityUID entUid) throws SQLException {
		String uid = entUid.uid;
		long entDbId = entUid.id;
		if(entDbId < 0) {
			// database id is not yet known so we have to request that
			entDbId = state.getDatabaseIndex().getTableManager().getEntityTable().getIdForFullUID(uid);
		}
		long t = System.currentTimeMillis();
		synchronized(state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
			boolean found = false;
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getUidObjectMap().get(uid);
			if(sendable != null) {
				if(sendable instanceof SimpleTransformableSendableObject) {
					System.err.println("[SECTOR] entity " + uid + " is still active -> not loaded from disk again: Loaded: " + sendable + "; currently in " + state.getUniverse().getSector(((SimpleTransformableSendableObject<?>) sendable).getSectorId()));
				} else {
					System.err.println("[SECTOR] entity " + uid + " is still active -> not loaded from disk again: Loaded: " + sendable + ";");
				}
				return sendable;
			}
		}
		SimpleTransformableSendableObject.EntityType entityType = DatabaseEntry.getEntityType(uid);
		if(entityType == SimpleTransformableSendableObject.EntityType.ASTEROID || entityType == SimpleTransformableSendableObject.EntityType.ASTEROID_MANAGED) {
			setTransientSector(false); //if there is a rock found in the saved entities the sector can't be transient
			String globalUI = DatabaseEntry.removePrefixWOException(uid);//foundRockManagedStart ? uid.substring(EntityType.ASTEROID_MANAGED.dbPrefix.length()) : uid.substring(EntityType.ASTEROID.dbPrefix.length());
			DatabaseEntry byUID = state.getDatabaseIndex().getTableManager().getEntityTable().getById(entDbId);
			if(byUID != null) {
				if(!byUID.touched) {
					Sendable s;
					if(entityType == SimpleTransformableSendableObject.EntityType.SHOP) {
						s = Universe.loadUntouchedShop(state, byUID, this);
					} else {
						s = Universe.loadUntouchedAsteroid(state, byUID, this);
					}
					return s;
				}
				//!found -> rock has to be loaded from file
			}
		}
		File f = SectorUtil.getEntityPath(uid);
		try {
			SimpleTransformableSendableObject<?> s = loadEntityForThisSector(entityType, f, entDbId);
			s.setTracked(entUid.tracked);
			s.setNeedsPositionCheckOnLoad(entUid.spawnedOnlyInDb);
			long took = System.currentTimeMillis() - t;
			if(took > 50) {
				System.err.println("[SERVER][SECTOR][LOADING_ENTITY] WARNING: " + this + " loading entity: " + uid + " took long: " + took + "ms");
			}
			return s;
		} catch(EntityNotFountException e) {
			try {
				e.printStackTrace();
				System.err.println("[SERVER][ERROR] Exception Cannot load  " + f.getName() + "; Removing entry from database");
				boolean removeSegmentController = state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(DatabaseEntry.removePrefix(uid), state);
				assert (removeSegmentController) : DatabaseEntry.removePrefix(uid);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		} catch(Exception e) {
			System.err.println("[SERVER][ERROR] Exception Loading Sector " + f.getName() + ";");
			e.printStackTrace();
		}
		return null;
	}

	public SimpleTransformableSendableObject<?> loadEntityForThisSector(SimpleTransformableSendableObject.EntityType type, File f, long entDbId) throws IOException, EntityNotFountException {
		return Universe.loadEntity(state, type, f, this, entDbId);
	}

	public void loadItems(byte[] bytes) throws IOException {
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
		int size = bytes.length / itemDataSize;
		//		System.err.println("[SERVER][SECTOR] READING " + size + " ITEMS");
		for(int i = 0; i < size; i++) {
			FreeItem it = new FreeItem();
			it.setId(GameServerState.getItemId());
			it.setType(stream.readShort());
			it.setCount(stream.readInt());
			it.setPos(new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat()));
			it.setMetaId(stream.readInt());
			if(it.getType() != Element.TYPE_NONE) {
				//				System.err.println("[SERVER][SECTOR] LOADED ITEM " + it.getType() + ": " + it.getCount() + " at " + it.getPos() + " with ID: " + it.getId());
				items.put(it.getId(), it);
			}
		}
	}

	public void loadUIDs(GameServerState state) {
		entityUids.addAll(state.getDatabaseIndex().getTableManager().getEntityTable().loadSectorEntities(pos));
		//		System.err.println("[SERVER][SECTOR] "+pos+" LOADED UIDs: "+Sector.uidToString(entityUids));
	}

	public void mode(SectorMode mode, boolean on) {
		mode(mode.code, on);
		if(remoteSector != null) {
			remoteSector.getRuleEntityManager().triggerSectorChmod();
		}
	}

	public void mode(int mode, boolean on) {
		if(on) {
			protectionMode |= mode;
		} else {
			protectionMode &= ~mode;
		}
		if(remoteSector != null && remoteSector.getNetworkObject() != null) {
			remoteSector.getNetworkObject().mode.set(protectionMode, true);
		}
		changed = true;
	}

	public void onAddedSector() throws IOException {
		assert (remoteSector == null);
		remoteSector = new RemoteSector(state);
		remoteSector.setSector(this);
		remoteSector.setItems(items);
		StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
		system = sys;
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(sys.getPos(), new Vector3i());
		Galaxy galaxy = state.getUniverse().getGalaxy(galaxyPos);
		distanceToSun = galaxy.getSunDistance(pos);
		sunIntensity = galaxy.getSunIntensityFromSec(pos);
		Vector3i relPos = Galaxy.getLocalCoordinatesFromSystem(sys.getPos(), new Vector3i());
		sunOffset = galaxy.getSunPositionOffset(relPos, new Vector3i());
		sunPosRel = new Vector3i(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
		sunPosRel.add(sunOffset);
		if(sys.getCenterSectorType() == SectorInformation.SectorType.DOUBLE_STAR) {
			sunPosRelSecond = new Vector3i();
			VoidSystem.getSecond(sunPosRel, sunOffset, sunPosRelSecond);
		}
		SectorInformation.SectorType sectorType = sys.getSectorType(pos);
		state.getController().getSynchController().addNewSynchronizedObjectQueued(remoteSector);
		if(getSectorType() == SectorInformation.SectorType.PLANET || getSectorType() == SectorInformation.SectorType.GAS_PLANET) sys.log("Added " + getSectorType().name() + " planetary sector " + pos + " to system at SEC " + pos);
		if(getSectorType() == SectorInformation.SectorType.GAS_PLANET) populateGasPlanetSector(state);
		//		System.err.println("[SECTOR] NEW SECTOR CREATED HOOK for "+sectorType);
	}

	private void onNewCreatedSector() throws IOException {
		changed = true;
		StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
		SectorInformation.SectorType sectorType = sys.getSectorType(pos);
		if(pos.equals(DEFAULT_SECTOR) && ServerConfig.PROTECT_STARTING_SECTOR.isOn()) {
			protect(true);
		}
		if(isPeace() || !ServerConfig.ENEMY_SPAWNING.isOn()) {
			System.err.println("[SECTOR] NEW SECTOR IS PROTECTED FROM SPAWNING ANY ENEMIES");
		} else {
			if(sectorType == SectorInformation.SectorType.ASTEROID && !EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("nomobs") && !VoidSystem.getContainingSystem(pos, new Vector3i()).equals(0, 0, 0)) {
				if(random.nextInt(100) == 0) {
					try {
						state.getController().initiateWave(random.nextInt(8) + 3, FactionManager.PIRATES_ID, 1, 3, BluePrintController.active, new Vector3i(pos));
					} catch(EntityNotFountException e) {
						e.printStackTrace();
					} catch(EntityAlreadyExistsException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void peace(boolean op) {
		mode(SectorMode.PROT_NO_SPAWN, op);
	}

	public void noEnter(boolean op) {
		mode(SectorMode.LOCK_NO_ENTER, op);
	}

	public void noExit(boolean op) {
		mode(SectorMode.LOCK_NO_EXIT, op);
	}

	public void noIndications(boolean op) {
		mode(SectorMode.NO_INDICATIONS, op);
	}

	public void noFpLoss(boolean op) {
		mode(SectorMode.NO_FP_LOSS, op);
	}

	public void ping() {
		lastPing = System.currentTimeMillis();
	}

	public void pingShort() {
		int unloadTime = 1000;
		//sector will unload in a 'unloadTime' ms
		long modLastPing = System.currentTimeMillis() - (ServerConfig.SECTOR_INACTIVE_TIMEOUT.getInt()) * 1000L + unloadTime;
		if(modLastPing < lastPing) {
			lastPing = modLastPing;
		}
	}

	public void populate(GameServerState state) throws IOException {
		//		textPopulate();
		StellarSystem sys = state.getUniverse().getStellarSystemFromSecPos(pos);
		if(ServerConfig.USE_PERSONAL_SECTORS.isOn()) {
			if(pos.x == 1000 * 16 && pos.z == 1000 * 16 && pos.y > 1 && pos.y % 16 == 0) {
				populatePersonalSector(state);
				return;
			}
		}
		SectorInformation.SectorType sectorType = sys.getSectorType(pos);
		Faction fac;
		System.err.println("SECTOR TYPE: " + pos + " -> " + sectorType.name());
		switch(sectorType) {
			case ASTEROID:
				if(getFactionId() < 0 && (fac = this.state.getFactionManager().getFaction(getFactionId())) != null && fac instanceof NPCFaction) {
					((NPCFaction) fac).populateAfterAsteroids(this, sectorType, random);
					return;
				}
				addShopToAsteroidSector(state);
				break;
			case SPACE_STATION:
				if(sys.getOwnerFaction() == FactionManager.PIRATES_ID) {
					popuplateSpaceStationSector(state, SpaceStation.SpaceStationType.PIRATE);
				} else {
					popuplateSpaceStationSector(state, sys.getSpaceStationTypeType(pos));
				}
				break;
			case PLANET:
				if(getFactionId() < 0 && (fac = this.state.getFactionManager().getFaction(getFactionId())) != null && fac instanceof NPCFaction) {
					((NPCFaction) fac).populatePlanet(this, sectorType, sys.getPlanetType(pos), random);
					return;
				}
				populatePlanetIcoSector(state);
				break;
			case GAS_PLANET:
				if(getFactionId() < 0 && (fac = this.state.getFactionManager().getFaction(getFactionId())) != null && fac instanceof NPCFaction) {
					((NPCFaction) fac).populateGasPlanet(this, sectorType, sys.getGasPlanetType(pos), random);
					return;
				}
				if(gasPlanet == null) populateGasPlanetSector(state);
				//this happens in onAddedSector because gas giant entities aren't saved on unload. but for nonstandard situations (e.g. admin repopulate) it has to be here too.
				break;
			case MAIN:
				populateMainSector(state);
				//				if(EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("nuplanet")){
				//populatePlanetIcoSector(state);
				//				}else{
				//					populateMainSector(state);
				//				}
				break;
			case DOUBLE_STAR:
			case GIANT:
			case SUN:
				populateSunSector(state);
				break;
			case BLACK_HOLE:
				populateBlackHoleSector(state);
				break;
			case VOID:
				populateEmptySector(state);
				//				System.err.println("POPULATE EMPTY " + pos);
				break;
			case LOW_ASTEROID:
				addShopToAsteroidSector(state);
				//				System.err.println("POPULATE LOW ASTEROID " + pos);
				break;
			default:
				assert (false) : "unknown sector type " + sys.getSectorType(pos);
				break;
		}
	}

	private int getRockCount(SectorInformation.SectorType sectorType) throws IOException {
		if(EngineSettings.SECRET.getString().toLowerCase(Locale.ENGLISH).contains("noasteroids")) {
			return 0;
		}
		Random randomT = new Random(seed);
		int asteroidCount = sectorType.getAsteroidCountMax();
		if(asteroidCount > 0) {
			int rock_count = randomT.nextInt(asteroidCount);
			if(sectorType == SectorInformation.SectorType.ASTEROID) {
				rock_count++;
			}
			return rock_count;
		}
		return 0;
	}

	private void addShopToAsteroidSector(GameServerState state) {
		if(random.nextFloat() < ServerConfig.SHOP_SPAWNING_PROBABILITY.getFloat()) {
			populateMainSector(state);
			/*
			ShopSpaceStation shop = new ShopSpaceStation(state);
			int nextFreeObjectId = state.getNextFreeObjectId();
			shop.setSeed(random.nextInt());
			shop.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix + seed + "_" + nextFreeObjectId);
			shop.getMinPos().set(new Vector3i(-0, -6, -0));
			shop.getMaxPos().set(new Vector3i(0, 6, 0));
			shop.setId(nextFreeObjectId);
			shop.setSectorId(id);
			shop.initialize();
			try {
				shop.fillInventory(false, false);
			} catch(NoSlotFreeException e) {
				e.printStackTrace();
			}
			shop.getInitialTransform().setIdentity();
			shop.getInitialTransform().origin.set(DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN / 2, random.nextInt(DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN / 4), DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN / 2);
			state.getController().getSynchController().addImmediateSynchronizedObject(shop);
			localAdd.add(shop);
			 */
		}
	}

	private void populateAsteroids(SectorInformation.SectorType sectorType, Random random) throws IOException {
		Faction var3;
		if(getFactionId() < 0 && (var3 = state.getFactionManager().getFaction(getFactionId())) != null && var3 instanceof NPCFaction) {
			// This needs its own event, event-ually. Giving modders access to NPC stuff is key to empowering the community to make the game a game, and a good one.
			// However I'm not going to mess with NPC faction-related things - not just yet, at least.
			// --Ithirahad
			((NPCFaction) var3).populateAsteroids(this, sectorType, random);
		} else {
			random = new Random(seed); //Asteroid seed randomizer
			Random randomAsteroidLocal = new Random(seed); //Asteroid size randomizer
			int asteroidCount;
			//INSERTED CODE
			int sectorMaxAsteroids = sectorType.getAsteroidCountMax();
			boolean doAsteroidPopulation = (asteroidCount = sectorMaxAsteroids) > 0;
			AsteroidNormalPopulateEvent anpe = new AsteroidNormalPopulateEvent(system, this, sectorType, random, randomAsteroidLocal, sectorMaxAsteroids, doAsteroidPopulation);
			StarLoader.fireEvent(anpe, true);
			asteroidCount = anpe.getMaxAsteroidPopulation();
			doAsteroidPopulation = anpe.isAllowingAsteroidPopulation();
			if(doAsteroidPopulation) {
				if(anpe.isOverridingPopulation()) asteroidCount = anpe.getForcedAsteroidCount();
				else {
					int min = anpe.getMinAsteroidPopulation(); //0 by default
					asteroidCount = min + this.random.nextInt(Math.max(1, asteroidCount - min));
					if(sectorType == SectorInformation.SectorType.ASTEROID) {
						++asteroidCount; //add the missing 1 if it's an asteroid sector, ensuring at least one asteroid present with no min set
					}
				}
				int var11 = ServerConfig.ASTEROID_RADIUS_MAX.getInt();
				for(int i = 0; i < asteroidCount; ++i) {
					long seed = random.nextLong();
					randomAsteroidLocal.setSeed(seed);
					int var6 = randomAsteroidLocal.nextInt(var11) + rockSize;
					int var7 = randomAsteroidLocal.nextInt(var11) + rockSize;
					int var8 = randomAsteroidLocal.nextInt(var11) + rockSize;
					addRandomRock(state, seed, var6, var7, var8, randomAsteroidLocal, i);
				}
			}
			///
		}
	}

	private void populateBlackHoleSector(GameServerState state2) {
	}

	private void populateEmptySector(GameServerState state2) {
	}

	private void populateMainSector(GameServerState state) {

		/*
		ShopSpaceStation greenShop = new ShopSpaceStation(state);
		greenShop.setSeed(0);
		greenShop.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix + System.currentTimeMillis());
		greenShop.getMinPos().set(new Vector3i(-0, -10, -0));
		greenShop.getMaxPos().set(new Vector3i(0, 10, 0));
		greenShop.setId(state.getNextFreeObjectId());
		greenShop.setSectorId(id);
		greenShop.initialize();
		try {
			greenShop.fillInventory(false, false);
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
		}
		greenShop.getInitialTransform().setIdentity();
		greenShop.getInitialTransform().origin.set(0, 0, 0);
		state.getController().getSynchController().addImmediateSynchronizedObject(greenShop);
		localAdd.add(greenShop); //*/

		BluePrintController c;
		int faction;

		c = BluePrintController.shopsTradingGuild;
		faction = FactionManager.TRAIDING_GUILD_ID;
		List<BlueprintEntry> readBluePrints = c.readBluePrints();

		boolean isAdvanced = random.nextInt(100) < 10 || pos.equals(DEFAULT_SECTOR);
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
			System.err.println("[SERVER][SECTOR] ERROR: Could not find shop blueprint for sector " + pos);
			return;
		}
		blueprintEntry.setEntityType(BlueprintType.SHOP);

		Transform t = new Transform();
		t.setIdentity();

		SegmentControllerOutline<?> loadBluePrint;
		try {
			String name = (isAdvanced) ? "Trade Station (" + pos.x + " " + pos.y + " " + pos.z + ") " + System.currentTimeMillis() : "Trade Outpost (" + pos.x + " " + pos.y + " " + pos.z + ") " + System.currentTimeMillis();
			loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), name, t, -1, faction, readBluePrints, pos, null, "<system>", buffer, true, null, new ChildStats(false));
			loadBluePrint.scrap = false;
			loadBluePrint.shop = true;
			loadBluePrint.spawnSectorId = new Vector3i(pos);
			System.err.println(loadBluePrint.getClass().getName());
			synchronized(state.getBluePrintsToSpawn()) {
				state.getBluePrintsToSpawn().add(loadBluePrint);
			}
		} catch(EntityNotFountException | IOException | EntityAlreadyExistsException e) {
			e.printStackTrace();
		}
	}

	private void populatePersonalSector(GameServerState state) {
		populateMainSector(state);
		/*
		ShopSpaceStation greenShop = new ShopSpaceStation(state);
		greenShop.setSeed(0);
		greenShop.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.SHOP.dbPrefix + System.currentTimeMillis());
		greenShop.getMinPos().set(new Vector3i(-0, -10, -0));
		greenShop.getMaxPos().set(new Vector3i(0, 10, 0));
		greenShop.setId(state.getNextFreeObjectId());
		greenShop.setSectorId(id);
		greenShop.getShoppingAddOn().setInfiniteSupply((true));
		greenShop.initialize();
		try {
			greenShop.fillInventory(false, false);
		} catch(NoSlotFreeException e) {
			e.printStackTrace();
		}
		greenShop.getInitialTransform().setIdentity();
		greenShop.getInitialTransform().origin.set(0, 0, 0);
		state.getController().getSynchController().addImmediateSynchronizedObject(greenShop);
		localAdd.add(greenShop);
		 */
	}

	public void populatePlanetIcoSector(GameServerState state) {
		VoidSystem sys = ((VoidSystem) _getSystem());
		TerrestrialBodyInformation info = sys.getPlanetInfo(pos);
		int planetRadius = info.getRadius();
		int segAverage = FastMath.fastRound(planetRadius / SegmentData.SEGf);
		int seed = info.getSeed();//1337;
		int xMinMax = IcosahedronHelper.segmentProviderXMinMax(planetRadius);
		Vector3i min = new Vector3i(-xMinMax, segAverage - 2, IcosahedronHelper.segmentProviderZMin(planetRadius));
		Vector3i max = new Vector3i(xMinMax, segAverage + 1, IcosahedronHelper.segmentProviderZMax(planetRadius));
		TerrainGenerator.TerrainGeneratorType type = info.getMainType().terrainGeneratorType;
		
		PlanetIcoCore core = new PlanetIcoCore(state);
		core.setPlanetType(info.getMainType());
		core.setId(state.getNextFreeObjectId());
		core.initialize();
		core.getInitialTransform().setIdentity();
		float coreRadius = planetRadius * ServerConfig.PLANET_CORE_RADIUS.getFloat();
		core.setRadius(coreRadius);
		core.setSectorId(id);
		core.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.PLANET_CORE.dbPrefix + pos.x + "_" + pos.y + "_" + pos.z);
//		state.getController().getSynchController().addNewSynchronizedObjectQueued(core);
		PlanetIco[] plates = new PlanetIco[core.getSides()];
		PlanetCreateEvent event = new PlanetCreateEvent(this, plates, core, type);
		StarLoader.fireEvent(event, true);
		for(int i = 0; i < core.getSides(); i++) {
			PlanetIco ico = new PlanetIco(state);
			ico.setPlanetCore(core);
			plates[i] = ico;
		}
		//INSERTED CODE
		if(!event.isCanceled()) {
			type = event.getPlanetType();
			TerrainGenerator terrainGenerator = type.inst(seed, planetRadius);
			System.err.println("#### PLANET_GEN: " + type + "::: " + terrainGenerator.getClass().getSimpleName());
			for(int i = 0; i < core.getSides(); i++) {
				PlanetIco ico = plates[i];
				ico.setId(state.getNextFreeObjectId());
				ico.setSeed(23452345L);
				ico.setTerrainGenerator(terrainGenerator);
				ico.setSectorId(id);
				ico.getMinPos().set(min);
				ico.getMaxPos().set(max);
				ico.setSideId((byte) i);
				ico.initialize();
				ico.radius = planetRadius;
				ico.setPlanetType(info.getMainType());
				ico.planetGenSeed = seed;
				ico.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.PLANET_ICO.dbPrefix + "SIDE_" + i + "_" + pos.x + "_" + pos.y + "_" + pos.z);
				state.getController().getSynchController().addNewSynchronizedObjectQueued(ico);
			}
			core.setPlates(plates);
			state.getController().getSynchController().addNewSynchronizedObjectQueued(core);

			for(PlanetIco ico : plates) {
				Transform transform = new Transform();
				transform.set(IcosahedronHelper.getSideTransform((byte) ico.getSideId()));
				ico.getInitialTransform().set(transform);
				ico.getWorldTransform().set(transform);
			}
		}
	}

	public void populateGasPlanetSector(GameServerState state) {
		if(gasPlanet != null) return; //no need to do this twice, ever
		/*
		float expected = ServerConfig.GAS_PLANET_SIZE_MEAN_VALUE.getFloat();
		float deviation = ServerConfig.GAS_PLANET_SIZE_DEVIATION_VALUE.getFloat();
		int planetRadius = (int) (expected + (random.nextGaussian() * (deviation / 3.0f)));
		int seed = random.nextInt();
		*/
		System.err.println("#### GAS_PLANET_GEN: SEC " + pos);
		VoidSystem sys = (VoidSystem) _getSystem();
		GasPlanetInformation source = sys.getGasPlanetInfo(pos);
		GasPlanet planet = new GasPlanet(state, source);
		planet.setId(state.getNextFreeObjectId());
		planet.initialize();
		planet.getInitialTransform().setIdentity();
		planet.setSectorId(id);
		planet.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.GAS_PLANET.dbPrefix + pos.x + "_" + pos.y + "_" + pos.z);
		planet.getInitialTransform().setIdentity();
		planet.getInitialTransform().origin.set(0, 0, 0);
		gasPlanet = planet;
		state.getController().getSynchController().addNewSynchronizedObjectQueued(planet);
	}

	public void populatePlanetSector(GameServerState state, SectorInformation.PlanetType planetType) {
		float mod = 1.45f;
		float expected = ServerConfig.PLANET_SIZE_MEAN_VALUE.getFloat();
		float deviation = ServerConfig.PLANET_SIZE_DEVIATION_VALUE.getFloat();
		// Since most nextGaussian() calls return values between -3 and 3, divide deviation by 3
		double planetSize = expected + (random.nextGaussian() * (deviation / 3.0));
		int planetRadius = Math.min(2000, Math.max(50, (int) planetSize));
		PlanetCore planetCore = new PlanetCore(state);
		planetCore.setId(state.getNextFreeObjectId());
		planetCore.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.PLANET_CORE.dbPrefix + pos.x + "_" + pos.y + "_" + pos.z);
		planetCore.setSectorId(id);
		planetCore.setRadius(planetRadius);
		planetCore.initialize();
		Dodecahedron dodecahedron = new Dodecahedron(planetCore.getRadius());
		dodecahedron.create();
		planetCore = planetCore;
		planetCore.getInitialTransform().setIdentity();
		state.getController().getSynchController().addNewSynchronizedObjectQueued(planetCore);
		int parts = 12; //default 12. use 11 to debug cored
		//INSERTED CODE
		Planet[] planetSegments = new Planet[parts];
		//
		for(int i = 0; i < parts; i++) {
			Planet planet = new Planet(state);
			planet.setCreatorId(planetType.ordinal());
			int size = (int) ((planetCore.getRadius() / 32));
			planet.setSeed(random.nextLong());
			planet.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.PLANET_SEGMENT.dbPrefix + pos.x + "_" + pos.y + "_" + pos.z + "_" + i + "_" + System.currentTimeMillis());
			planet.getMinPos().set(new Vector3i(-size, 0, -size));
			planet.getMaxPos().set(new Vector3i(size, 3, size));
			planet.setId(state.getNextFreeObjectId());
			planet.setSectorId(id);
			planet.initialize();
			planet.setPlanetCoreUID(planetCore.getUniqueIdentifier());
			planet.setPlanetCore(planetCore);
			planet.fragmentId = i;
			Transform transform = dodecahedron.getTransform(i, new Transform(), 0.5f, 0.5f);
			Vector3f dir = new Vector3f(transform.origin);
			dir.normalize();
			dir.scale(7.6f);
			transform.origin.add(dir);
			planet.getInitialTransform().set(transform);
			state.getController().getSynchController().addImmediateSynchronizedObject(planet);
			//INSERTED CODE
			planetSegments[i] = planet;
			//
			localAdd.add(planet);
		}
	}

	//INSERTED CODE
	public float _getDistanceToSun() {
		return distanceToSun;
	}

	public void _setDistanceToSun(float distanceToSun) {
		this.distanceToSun = distanceToSun;
	}

	public float _getSunIntensity() {
		return sunIntensity;
	}
	///

	public void _setSunIntensity(float sunIntensity) {
		this.sunIntensity = sunIntensity;
	}

	public StellarSystem _getSystem() {
		return system;
	}

	private void populateSunSector(GameServerState state2) {
	}

	private int getSectorSizeWithoutMargin() {
		return DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN;
	}

	private void popuplateNPCSpaceStationSector(GameServerState state) {
		Faction fac;
		if(getFactionId() < 0 && (fac = this.state.getFactionManager().getFaction(getFactionId())) != null && fac instanceof NPCFaction) {
			((NPCFaction) fac).populateSpaceStation(this, random);
		} else {
			popuplateDefaultNeutralSpaceStationSector(state);
		}
	}

	private void popuplateRandomSpaceStationSector(GameServerState state) {
		BluePrintController c;
		int faction;
		if(random.nextInt(3) == 0) {
			c = BluePrintController.stationsTradingGuild;
			faction = FactionManager.TRAIDING_GUILD_ID;
		} else {
			c = BluePrintController.stationsNeutral;
			faction = 0;
		}
		{
			List<BlueprintEntry> readBluePrints = c.readBluePrints();
			//			System.err.println("SPAWNING RANDOM STATIONI (on of: "+readBluePrints+");");
			if(!readBluePrints.isEmpty() && random.nextInt(60) > 0) {
				BlueprintEntry blueprintEntry = readBluePrints.get(random.nextInt(readBluePrints.size()));
				Transform t = new Transform();
				t.setIdentity();
				SegmentControllerOutline<?> loadBluePrint;
				try {
					SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
					loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), "Station_" + blueprintEntry.getName() + "_" + pos.x + "_" + pos.y + "_" + pos.z + "_" + System.currentTimeMillis(), t, -1, faction, readBluePrints, pos, null, "<system>", buffer, true, toDockOn, new ChildStats(true));
					loadBluePrint.scrap = (c == BluePrintController.stationsNeutral);
					loadBluePrint.shop = (faction == FactionManager.TRAIDING_GUILD_ID);
					loadBluePrint.spawnSectorId = new Vector3i(pos);
					synchronized(state.getBluePrintsToSpawn()) {
						state.getBluePrintsToSpawn().add(loadBluePrint);
					}
				} catch(EntityNotFountException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				} catch(EntityAlreadyExistsException e) {
					e.printStackTrace();
				}
			} else {
				popuplateDefaultNeutralSpaceStationSector(state);
			}
		}
	}

	private void popuplateDefaultNeutralSpaceStationSector(GameServerState state) {
		{
			SpaceStation testStation = new SpaceStation(state);
			testStation.setSeed(random.nextLong());
			testStation.setUniqueIdentifier(SimpleTransformableSendableObject.EntityType.SPACE_STATION.dbPrefix + System.currentTimeMillis());
			testStation.setRealName("Station " + id);
			testStation.getMinPos().set(new Vector3i(-3, -3, -3));
			testStation.getMaxPos().set(new Vector3i(3, 3, 3));
			testStation.setCreatorId(SpaceStation.SpaceStationType.RANDOM.ordinal());
			testStation.setId(state.getNextFreeObjectId());
			testStation.setSectorId(id);
			testStation.initialize();
			testStation.setScrap(true);
			testStation.getInitialTransform().setIdentity();
			state.getController().getSynchController().addImmediateSynchronizedObject(testStation);
			DefaultSpawner s = new DefaultSpawner();
			SpawnMarker marker = new SpawnMarker(new Vector3i(1, (int) (Math.random() * 10 - 5), 1), testStation, s);
			SpawnComponentCreature spawnComponentCreature = new SpawnComponentCreature();
			spawnComponentCreature.setBottom("LegsArag");
			spawnComponentCreature.setMiddle("TorsoShell");
			spawnComponentCreature.setName("Spider");
			spawnComponentCreature.setCreatureType(CreatureType.CREATURE_SPECIFIC);
			spawnComponentCreature.setFactionId(FactionManager.FAUNA_GROUP_ENEMY[0]);
			s.getComponents().add(spawnComponentCreature);
			s.getComponents().add(new SpawnComponentDestroySpawnerAfterCount(5));
			s.getConditions().add(new SpawnConditionTime(25000));
			s.getConditions().add(new SpawnConditionCreatureCountOnAffinity(1));
			s.getConditions().add(new SpawnConditionPlayerProximity(64));
			testStation.getSpawnController().getSpawnMarker().add(marker);
			localAdd.add(testStation);
		}
	}

	private void popuplatePirateSpaceStationSector(GameServerState state) {
		{
			BluePrintController c;
			int faction;
			c = BluePrintController.stationsPirate;
			faction = FactionManager.PIRATES_ID;
			List<BlueprintEntry> readBluePrints = c.readBluePrints();
			//			System.err.println("SPAWNING RANDOM STATIONI (on of: "+readBluePrints+");");
			if(!readBluePrints.isEmpty() && (!ServerConfig.USE_OLD_GENERATED_PIRATE_STATIONS.isOn() || random.nextInt(5) > 0)) {
				BlueprintEntry blueprintEntry = readBluePrints.get(random.nextInt(readBluePrints.size()));
				Transform t = new Transform();
				t.setIdentity();
				SegmentControllerOutline<?> loadBluePrint;
				try {
					SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
					loadBluePrint = c.loadBluePrint(state, blueprintEntry.getName(), "Station_" + blueprintEntry.getName() + "_" + pos.x + "_" + pos.y + "_" + pos.z + "_" + System.currentTimeMillis(), t, -1, faction, readBluePrints, pos, null, "<system>", buffer, true, toDockOn, new ChildStats(true));
					loadBluePrint.scrap = (c == BluePrintController.stationsNeutral);
					loadBluePrint.spawnSectorId = new Vector3i(pos);
					synchronized(state.getBluePrintsToSpawn()) {
						state.getBluePrintsToSpawn().add(loadBluePrint);
					}
				} catch(EntityNotFountException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				} catch(EntityAlreadyExistsException e) {
					e.printStackTrace();
				}
			} else {
				//				SpaceStation testStation = new SpaceStation(state);
				//				testStation.setSeed(getRandom().nextLong());
				//				testStation.setUniqueIdentifier("ENTITY_SPACESTATION_P" + System.currentTimeMillis());
				//
				//				testStation.setRealName("Prate Station " + this.getId());
				//
				//				testStation.getMinPos().set(new Vector3i(-3, -3, -3));
				//
				//				testStation.getMaxPos().set(new Vector3i(3, 3, 3));
				//
				//				testStation.setCreatorId(SpaceStationType.PIRATE.ordinal());
				//
				//				testStation.setFactionId(FactionManager.PIRATES_ID);
				//
				//				testStation.setTouched(true, false);
				//
				//				testStation.setId(state.getNextFreeObjectId());
				//				testStation.setSectorId(this.getId());
				//				testStation.initialize();
				//				testStation.getInitialTransform().setIdentity();
				//				state.getController().getSynchController().addImmediateSynchronizedObject(testStation);
				//				localAdd.add(testStation);
			}
		}
	}

	private void popuplateSpaceStationSector(GameServerState state, SpaceStation.SpaceStationType spaceStationType) {
		switch(spaceStationType) {
			case EMPTY -> populateEmptySector(state);
			case RANDOM -> popuplateRandomSpaceStationSector(state);
			case PIRATE -> popuplatePirateSpaceStationSector(state);
			case FACTION -> popuplateNPCSpaceStationSector(state);
			default -> {
			}
		}
	}

	public void protect(boolean op) {
		mode(SectorMode.PROT_NO_ATTACK, op);
	}

	public void queueRepairRequest() {
		flagRepair = true;
	}

	public void releaseParticleControllerFromPool() {
		assert (particleController != null);
		particleControllerPool.enqueue(particleController);
		particleController = null;
	}

	public void releasePulseControllerFromPool() {
		assert (pulseController != null);
		pulseControllerPool.enqueue(pulseController);
		pulseController = null;
	}

	private void repair() {
		changed = true;
		try {
			List<DatabaseEntry> bySector = state.getDatabaseIndex().getTableManager().getEntityTable().getBySector(pos, 0);
			for(DatabaseEntry e : bySector) {
				try {
					if(!state.getSegmentControllersByName().containsKey(e.uid.trim())) {
						String uid = e.uid.split("_", 3)[1];
						System.err.println("[REPAIR] FOUND SECTOR ENTITY: " + e.uid + " [" + uid + "]");
						loadEntityForThisSector(e.getEntityType(), new FileExt(e.uid + ".ent"), -1L);
					}
				} catch(IOException e1) {
					e1.printStackTrace();
				} catch(EntityNotFountException e1) {
					e1.printStackTrace();
				}
			}
		} catch(SQLException e1) {
			e1.printStackTrace();
		}
		File[] files = (new FileExt(GameServerState.ENTITY_DATABASE_PATH).listFiles());
	}

	public void setNew() {
		newCreatedSector = true;
	}

	private void setRandomPos(Vector3i pos, Random random) {
		pos.set(0, 0, 0);
		int area = DEFAULT_SECTOR_SIZE_WITHOUT_MARGIN;
		int x = (int) ((random.nextInt(area) - area / 2.0f) * 0.7f);
		int y = (int) ((random.nextInt(area) - area / 2.0f) * 0.7f);
		int z = (int) ((random.nextInt(area) - area / 2.0f) * 0.7f);
		pos.set(x, y, z);
	}

	public void testPopulate() throws Exception {
		Vector3i pos = new Vector3i();
		TeamDeathStar rock = new TeamDeathStar(state);
		rock.setSeed(random.nextLong());
		rock.setUniqueIdentifier("ENTITY_DEATHSTAR_GREEN_" + System.currentTimeMillis());
		rock.getMinPos().set(new Vector3i(-2, -2, -2));
		rock.getMaxPos().set(new Vector3i(2, 2, 2));
		rock.setId(state.getNextFreeObjectId());
		rock.setSectorId(id);
		//		rock.setTeam(state.getGreenTeam());
		rock.initialize();
		rock.getInitialTransform().setIdentity();
		rock.getInitialTransform().origin.set(0, -64, 0);
		localAdd.add(rock);
		state.getController().getSynchController().addImmediateSynchronizedObject(rock);
	}

	public String toDetailString() {
		String secType = "unknown(IOError)";
		try {
			SectorInformation.SectorType t = getSectorType();
			secType = t.name();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return "Sector[" + id + "]" + pos + "; Permission" + getPermissionString() + "; Seed: " + seed + "; Type: " + secType + ";";
	}

	//	public static final int PROT_NO_SPAWN = 1; //peace
	//	public static final int PROT_NO_ATTACK = 2; //prot
	//	public static final int LOCK_NO_ENTER = 4;
	//	public static final int LOCK_NO_EXIT = 8;
	//	public static final int NO_INDICATIONS = 16;
	//	public static final int NO_FP_LOSS = 32;
	public String getPermissionString() {
		return getPermissionString(protectionMode);
	}

	public int getPermissionBit(SectorMode mode) {
		return getPermissionBit(mode.code);
	}

	public int getPermissionBit(int mode) {
		return isMode(protectionMode, mode) ? 1 : 0;
	}

	private boolean checkSec() {
		Vector3i galaxyPos = Galaxy.getContainingGalaxyFromSystemPos(VoidSystem.getContainingSystem(pos, new Vector3i()), new Vector3i());
		Galaxy galaxy = state.getUniverse().getGalaxy(galaxyPos);
		Vector3i relPos = Galaxy.getLocalCoordinatesFromSystem(VoidSystem.getContainingSystem(pos, new Vector3i()), new Vector3i());
		Vector3i sunOffset2 = galaxy.getSunPositionOffset(relPos, new Vector3i());
		return (sunOffset.equals(sunOffset2));
	}

	public void update(Timer timer) throws IOException {
		assert (!terminated);
		//INSERTED CODE
		boolean shouldIterate = (!FastListenerCommon.sectorUpdateListeners.isEmpty());
		if(shouldIterate) {
			for(SectorUpdateListener listener : FastListenerCommon.sectorUpdateListeners) {
				listener.local_preUpdate(this, timer);
			}
		}
		///
		GameServerState.totalSectorCountTmp++;
		long ttg = System.currentTimeMillis();
		long time = System.currentTimeMillis();
		long tPhysics = 0;
		long tPaticles = 0;
		if(flagRepair) {
			repair();
			flagRepair = false;
		}
		if(active) {
			GameServerState.activeSectorCountTmp++;
			assert (particleController != null);
			long tt = System.currentTimeMillis();
			particleController.update(timer);
			pulseController.update(timer);
			tPaticles = System.currentTimeMillis() - tt;
			if(tPaticles > 150) {
				System.err.println("[SERVER] '''WARNING''' PATICLE UPDATE: " + pos + " ms TOOK " + tPaticles + "; Count: " + particleController.getParticleCount());
			}
			if(newCreatedSector) {
				onNewCreatedSector();
				newCreatedSector = false;
			}
			tt = System.currentTimeMillis();
			physics.update(timer, highestSubStep);
			tPhysics = System.currentTimeMillis() - tt;
			//			final ObjectArrayList<SimpleTransformableSendableObject> entsToWrite = updateEntities();
			//			for(SimpleTransformableSendableObject o : entsToWrite){
			//				if(o.isWrittenForUnload()){
			//					o.setWrittenForUnload(false);
			//				}
			//			}
			sectorWrittenToDisk = false;
			highestSubStep = 0;
			float heatRange = state.getGameConfig().sunMinIntensityDamageRange;
			if(system.isHeatDamage(pos, sunIntensity, distanceToSun, heatRange)) {
				float invDistNorm = (heatRange - distanceToSun / Math.max(1, sunIntensity)) / heatRange;
				doSunstorm(timer, invDistNorm);
			}
			if(system.getCenterSectorType() == SectorInformation.SectorType.BLACK_HOLE) {
				assert (checkSec());
				if(!system.getPos().equals(0, 0, 0) && !isTutorialSector() && !isPersonalOrTestSector()) {
					applyBlackHoleGrav(state, system.getPos(), sunPosRel, pos, id, tmpSecPos, tmpOPos, tmpDir, sunOffset, timer);
				}
			}
			//INSERTED CODE
			if(shouldIterate) {
				for(SectorUpdateListener listener : FastListenerCommon.sectorUpdateListeners) {
					listener.local_activeUpdate(this, timer);
				}
			}
			///
		} else {
			if(state.delayAutosave < System.currentTimeMillis()) {
				if(wasActive) {
					long t = System.currentTimeMillis();
					writeToDisk(CLEAR_CODE_SEQENTIAL, false, true, state.getUniverse());
					long took = System.currentTimeMillis() - t;
					if(took > 20) {
						System.err.println("[SERVER][SECTOR] WRITING SECTOR ID " + id + " -> " + pos + " TOOK " + took + "ms");
					}
				}
				wasActive = false;
			} else {
				System.err.println("[SERVER] Delay of unloading sector: delay issued by admin " + (state.delayAutosave - System.currentTimeMillis()) / 1000 + " secs");
			}
		}
		long timeTaken = System.currentTimeMillis() - time;
		//		if(timeTaken > 30){
		//			System.err.println("WARNING: sector update of single sector "+pos+" took "+timeTaken+" ms; Physics "+tPhysics+" ms");
		//			System.err.println("WARNING: sector info: "+pos+"; id "+id+"; type "+getSectorType().name()+"; ");
		//		}
		if(timeTaken > 130 && (System.currentTimeMillis() - lastWarning) > 100000) {
			System.err.println("WARNING: sector update of single sector " + pos + " took " + timeTaken + " ms; Physics " + tPhysics + " ms");
			System.err.println("WARNING: sector info: " + pos + "; id " + id + "; type " + getSectorType().name() + "; ");
			if(!GameClientController.isStarted()) {
				state.getController().broadcastMessageAdmin(Lng.astr("DEBUG: sector %s\nis causing server lag\n(took %s ms on update)\nPhysics: %sms", pos, timeTaken, tPhysics), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			lastWarning = System.currentTimeMillis();
		}
		//INSERTED CODE
		if(shouldIterate) {
			for(SectorUpdateListener listener : FastListenerCommon.sectorUpdateListeners) {
				listener.local_postUpdate(this, timer);
			}
		}
		///
	}

	public boolean isPersonalOrTestSector() {
		return isPersonalOrTestSector(pos);
	}

	public ObjectArrayList<SimpleTransformableSendableObject> updateEntities() {
		ObjectArrayList<SimpleTransformableSendableObject> entities = new ObjectArrayList<SimpleTransformableSendableObject>();
		try {
			for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if(s instanceof SimpleTransformableSendableObject st && ((SimpleTransformableSendableObject) s).getSectorId() == id) {
					Transform worldTransform = st.getWorldTransform();
					entities.add(st);
				}
			}
		} catch(ConcurrentModificationException e) {
			e.printStackTrace();
			System.err.println("CATCHED EXCEPTION!!!!!!!!!!!!!!!!!! (sector entity calc)");
			return updateEntities();
		}
		return entities;
	}

	/**
	 * triggered by autosave or terminate
	 * <p/>
	 * THIS IS DONE THREADED
	 *
	 * @param s
	 * @param clearCode
	 * @param universe
	 *
	 * @throws Exception
	 */
	private void writeEntity(Sendable s, int clearCode, Universe universe) throws Exception {
		if(s instanceof TagSerializable) {
			long t = System.currentTimeMillis();
			if(s instanceof SimpleTransformableSendableObject) {
				((SimpleTransformableSendableObject) s).transientSectorPos.set(pos);
				((SimpleTransformableSendableObject) s).transientSector = (transientSector);
			}
			long entityWriteTime = t - System.currentTimeMillis();
			writeSingle(state, s);
			if(clearCode == CLEAR_CODE_THREADED) {
				System.err.println("[SERVER] Terminate: DONE Writing Entity: " + this + ": " + s);
			}
			if(s instanceof SendableSegmentController seg) {
				if(clearCode == CLEAR_CODE_THREADED) {
					int sBefore = seg.getSegmentBuffer().size();
					int cleared = seg.getSegmentBuffer().clear(true);
				} else if(clearCode == CLEAR_CODE_SEQENTIAL) {
					universe.addToClear(((SendableSegmentController) s));
				}
			}
			long totalTime = t - System.currentTimeMillis();
			if(totalTime > 10) {
				System.err.println("[SECTOR] WARNING: Writing entity: " + s + " took " + totalTime + " ms. write: " + entityWriteTime + "; segments " + (totalTime - entityWriteTime));
			}
		}
	}

	public void writeToDisk(int clearCode, boolean terminate, boolean clear, Universe universe) throws IOException {
		for(FreeItem i : getItems().values()) {
			if(i.getType() < 0) {
				state.getMetaObjectManager().archive(pos, i);
			}
		}
		ObjectArrayList<SimpleTransformableSendableObject> entsToWrite = updateEntities();
		if(entsToWrite.isEmpty()) {
			//empty sector is not transient. (maybe cleaned purposely)
			setTransientSector(false);
		}
		boolean hasSpaceStation = false;
		boolean hasPlanet = false;
		for(int i = 0; i < entsToWrite.size(); i++) {
			SimpleTransformableSendableObject<?> s = entsToWrite.get(i);
			if(s instanceof TransientSegmentController && (((TransientSegmentController) s).isMoved() || ((TransientSegmentController) s).isTouched())) {
				s.transientSector = false;
				setTransientSector(false);
				if(transientSector) {
					System.err.println("SECTOR " + pos + " is Transient sector no longer because of " + s);
				}
			}
			if(s instanceof SpaceStation) {
				hasSpaceStation = true;
			}
			if(s instanceof Planet && !"none".equals(((Planet) s).getPlanetCoreUID())) {
				hasPlanet = true;
			}
			if(s instanceof PlanetIco && !"none".equals(((PlanetIco) s).getPlanetCoreUID())) {
				hasPlanet = true;
			}
		}
		if((getSectorType() == SectorInformation.SectorType.SPACE_STATION && !hasSpaceStation) || (getSectorType() == SectorInformation.SectorType.PLANET && !hasPlanet)) {
			StellarSystem stellarSystemFromSecPos = state.getUniverse().getStellarSystemFromSecPos(pos);
			Vector3i localCoordinates = StellarSystem.getLocalCoordinates(pos, new Vector3i());
			int index = stellarSystemFromSecPos.getIndex(localCoordinates);
			stellarSystemFromSecPos.setSectorType(index, SectorInformation.SectorType.VOID);
		}
		if(clearCode == CLEAR_CODE_THREADED) {
			System.err.println("[SERVER] Terminate: Writing Entity: " + this + "; trans: " + transientSector + "; " + entsToWrite);
		}
		GameServerState state = this.state;
		Runnable r = new Runnable() {
			private String threatstate = "not started";

			@Override
			public String toString() {
				return "WriterRunnable (ID: " + getId() + "; state: " + threatstate + ")";
			}

			@Override
			public void run() {
				threatstate = "waiting on synch";
				synchronized(state) {
					synchronized(state.getLocalAndRemoteObjectContainer()) {
						try {
							threatstate = "synched - database update";
							assert (state != null);
							assert (state.getDatabaseIndex() != null);
							assert (Sector.this != null);
							state.getDatabaseIndex().getTableManager().getSectorTable().updateOrInsertSector(Sector.this);
							if(getRemoteSector() != null) {
								getRemoteSector().getConfigManager().saveToDatabase(getState());
							}
							//						state.getController().writeEntity(Sector.this);
							long t = System.currentTimeMillis();
							threatstate = "synched - write";
							//entities are written on unload
							//							System.err.println("[SERVER] SERVER SECTOR WRITING STARTED #" + getId() + " active: " + isActive() + " at " + pos + " (ents: " + entsToWrite.size() + ")");
							//						if(terminate){
							SimpleTransformableSendableObject<?> ent;
							for(int i = 0; i < entsToWrite.size(); i++) {
								try {
									ent = entsToWrite.get(i);
									if(!ent.isWrittenForUnload()) {
										//dont set to unload if no clear (autosave or forced save)
										if(!ent.isMarkedForDeleteVolatile()) {
											writeEntity(ent, clearCode, universe);
										}
										ent.setWrittenForUnload(clear);
									} else {
										System.err.println("[SERVER][DEBUG] NOT writing " + ent + " (already written for unload)");
									}
								} catch(IOException e) {
									e.printStackTrace();
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
							//						}
							if((System.currentTimeMillis() - t) > 100) {
								System.err.println("[SERVER] (>100) SERVER SECTOR WRITING FINSISHED #" + getId() + " " + pos + " (ents: " + entsToWrite.size() + ") took: " + (System.currentTimeMillis() - t) + " ms");
							}
							sectorWrittenToDisk = true;
							threatstate = "synched - DONE";
						} catch(IOException e1) {
							e1.printStackTrace();
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
				threatstate = "terminated";
			}
		};
		try {
			state.getThreadQueue().enqueue(r);
		} catch(RejectedExecutionException e) {
			e.printStackTrace();
			System.err.println(state.getConnectionThreadPool().getActiveCount() + "/" + state.getConnectionThreadPool().getMaximumPoolSize());
		}
	}

	/**
	 * @return the transientSector
	 */
	public boolean isTransientSector() {
		return transientSector;
	}

	/**
	 * @param transientSector the transientSector to set
	 */
	public void setTransientSector(boolean transientSector) {
		if(this.transientSector && !transientSector) {
			changed = true;
		}
		this.transientSector = transientSector;
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
	 * @return the random
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * @param random the random to set
	 */
	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * @return the planetCore
	 */
	public PlanetIcoCore getPlanetCore() {
		return planetCore;
	}

	public void setPlanetCore(PlanetIcoCore planetCore) {
		this.planetCore = planetCore;
	}

	/**
	 * @return the sector's Gas Planet entity.
	 */
	public GasPlanet getGasPlanet() {
		return gasPlanet;
	}

	public void setGasPlanet(GasPlanet v) {
		this.gasPlanet = v;
	}

	/**
	 * @return the state
	 */
	@Override
	public GameServerState getState() {
		return state;
	}

	@Override
	public void sendHitConfirm(byte damageType) {
	}

	@Override
	public boolean isSegmentController() {
		return false;
	}

	@Override
	public int getFactionId() {
		try {
			return state.getUniverse().getStellarSystemFromSecPos(pos).getOwnerFaction();
		} catch(IOException e) {
			e.printStackTrace();
			assert (false);
		}
		return 0;
	}

	@Override
	public String getName() {
		try {
			return getSectorType() == SectorInformation.SectorType.BLACK_HOLE ? "Black Hole" : "Sun";
		} catch(IOException e) {
			e.printStackTrace();
		}
		return "n/a";
	}

	@Override
	public AbstractOwnerState getOwnerState() {
		return null;
	}

	public void removeMissile(short missileId) {
		missiles.remove(missileId);
	}

	public void addMissile(short missileId) {
		missiles.add(missileId);
	}

	/**
	 * @return the missiles
	 */
	public ShortOpenHashSet getMissiles() {
		return missiles;
	}

	public boolean isTutorialSector() {
		return isTutorialSector(pos);
	}

	@Override
	public boolean hasChangedForDb() {
		return changed;
	}

	@Override
	public void setChangedForDb(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void sendServerMessage(Object[] astr, byte msgType) {
	}

	public long getLastReplenished() {
		return lastReplenished;
	}

	public void setLastReplenished(long lastReplenished) {
		this.lastReplenished = lastReplenished;
	}

	public void addEntity(SimpleTransformableSendableObject simpleTransformableSendableObject) {
		entities.add(simpleTransformableSendableObject);
		state.getController().onEntityAddedToSector(this, simpleTransformableSendableObject);
	}

	public void removeEntity(SimpleTransformableSendableObject simpleTransformableSendableObject) {
		entities.remove(simpleTransformableSendableObject);
		state.getController().onEntityRemoveFromSector(this, simpleTransformableSendableObject);
	}

	public Set<SimpleTransformableSendableObject<?>> getEntities() {
		return entities;
	}

	@Override
	public void sendClientMessage(String str, byte type) {
	}

	@Override
	public float getDamageGivenMultiplier() {
		return 1;
	}

	public boolean isInterdicting(SegmentController segmentController, Sector currentSec) {
		if(!remoteSector.getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_ACTIVE, false)) {
			//			if(pos.equals(16, 16, 16)){
			//				for(EffectModule m : getRemoteSector().getConfigManager().getModulesList()){
			//					System.err.println("TT: "+m);
			//				}
			//				System.err.println("[SERVER] No interdiction for sector: "+this);
			//			}
			return false;
		}
		int dx = Math.abs(currentSec.pos.x - pos.x);
		int dy = Math.abs(currentSec.pos.y - pos.y);
		int dz = Math.abs(currentSec.pos.z - pos.z);
		int distance = Math.max(0, remoteSector.getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_DISTANCE, 1) - 1);
		int strength = remoteSector.getConfigManager().apply(StatusEffectType.WARP_INTERDICTION_STRENGTH, 1);
		if(dx + dy + dz <= distance) {
			if(segmentController.hasActiveReactors()) {
				return ((ManagedSegmentController<?>) segmentController).getManagerContainer().getPowerInterface().getActiveReactor().getLevel() <= strength;
			}
			return false;
		}
		return false;
	}

	@Override
	public SimpleTransformableSendableObject<?> getShootingEntity() {
		return null;
	}

	@Override
	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		return null;
	}

	@Override
	public int getSectorId() {
		return id;
	}

	public enum SectorMode {
		PROT_NORMAL(0),
		PROT_NO_SPAWN(1),
		PROT_NO_ATTACK(2),
		LOCK_NO_ENTER(4),
		LOCK_NO_EXIT(8),
		NO_INDICATIONS(16),
		NO_FP_LOSS(32),
		;
		public final int code;

		SectorMode(int code) {
			this.code = code;
		}

		public String getName() {
			return switch(this) {
				case LOCK_NO_ENTER -> Lng.str("No Entry");
				case LOCK_NO_EXIT -> Lng.str("No Exit");
				case NO_FP_LOSS -> Lng.str("No FP Loss");
				case NO_INDICATIONS -> Lng.str("No Indicators");
				case PROT_NORMAL -> Lng.str("Normal");
				case PROT_NO_ATTACK -> Lng.str("No Attack");
				case PROT_NO_SPAWN -> Lng.str("No Spawn");
				default -> "UNKNOWNSECTORMODE";
			};
		}
	}

	private class SectorSunDamager implements Damager {
		InterEffectSet damageSet = new InterEffectSet();

		public SectorSunDamager() {
			//pure heat damage
			damageSet.setStrength(InterEffectHandler.InterEffectType.HEAT, 1.0f);
		}

		@Override
		public void sendServerMessage(Object[] astr, byte msgType) {
		}

		@Override
		public StateInterface getState() {
			return Sector.this.getState();
		}

		@Override
		public void sendHitConfirm(byte damageType) {
		}

		@Override
		public boolean isSegmentController() {
			return false;
		}

		@Override
		public SimpleTransformableSendableObject<?> getShootingEntity() {
			return null;
		}

		@Override
		public int getFactionId() {
			return 0;
		}

		@Override
		public String getName() {
			return Lng.str("Evironmental Heat Damage");
		}

		@Override
		public AbstractOwnerState getOwnerState() {
			return null;
		}

		@Override
		public void sendClientMessage(String str, byte type) {
		}

		@Override
		public float getDamageGivenMultiplier() {
			return 1;
		}

		@Override
		public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
			return damageSet;
		}

		@Override
		public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
			return null;
		}

		@Override
		public int getSectorId() {
			return Sector.this.getSectorId();
		}
	}

	private class PhysicsCallback extends InternalTickCallback {
		@Override
		public void internalTick(DynamicsWorld dyncapicsWorld, float timeStep) {
			int numManifolds = getPhysics().getDynamicsWorld().getDispatcher().getNumManifolds();
			for(int i = 0; i < numManifolds; i++) {
				PersistentManifold contactManifold = getPhysics().getDynamicsWorld().getDispatcher().getManifoldByIndexInternal(i);
				CollisionObject obA = (CollisionObject) (contactManifold.getBody0());
				CollisionObject obB = (CollisionObject) (contactManifold.getBody1());
				if(obA == null || obB == null || (obA instanceof GhostObject) || (obB instanceof GhostObject) || obB.getUserPointer() == null || obB.getUserPointer() == null) {
					//					System.err.println("Exception: Cannot do tick with: "+obA+" and "+obB+" (missile?)");
					continue;
				}
				if((!(obA.getUserPointer() instanceof Integer)) || (!(obB.getUserPointer() instanceof Integer))) {
					getState().getController().broadcastMessage(Lng.astr("Physics Exception on server!\nPlease send in server logs\n%s\n%s", obA.getUserPointer() != null ? obA.getUserPointer().toString() : "null", obB.getUserPointer() != null ? obB.getUserPointer().toString() : null), ServerMessage.MESSAGE_TYPE_ERROR);
					continue;
				}
				int numContacts = contactManifold.getNumContacts();
				int aId = (Integer) obA.getUserPointer();
				int bId = (Integer) obB.getUserPointer();
				Sendable sendableA = null;
				Sendable sendableB = null;
				sendableA = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(aId);
				sendableB = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(bId);
				Vector3f ptAtmp = new Vector3f();
				Vector3f ptBtmp = new Vector3f();
				if(sendableA instanceof Collisionable a && sendableB instanceof Collisionable b) {
					boolean aCol = a.needsManifoldCollision();
					boolean bCol = b.needsManifoldCollision();
					if(aCol || bCol) {
						for(int j = 0; j < numContacts; j++) {
							ManifoldPoint pt = contactManifold.getContactPoint(j);
							if(pt.getDistance() < 0.0f && obA.getUserPointer() != null && obB.getUserPointer() != null) {
								Vector3f ptA = pt.getPositionWorldOnA(ptAtmp);
								Vector3f ptB = pt.getPositionWorldOnB(ptBtmp);
								Vector3f normalOnB = pt.normalWorldOnB;
								if(aCol) {
									a.onCollision(pt, sendableB);
								}
								if(bCol) {
									b.onCollision(pt, sendableA);
								}
							}
						}
					}
				}
			}
		}
	}

	private class SimpleTransformableSendableObjectList extends ArrayList<SimpleTransformableSendableObject> {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		/**
		 *
		 */
	}
}
