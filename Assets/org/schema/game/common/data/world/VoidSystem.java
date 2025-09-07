package org.schema.game.common.data.world;

import api.common.GameCommon;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.FloatingRock.StandardAsteroidMaterials;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.game.common.data.world.planet.terrestrial.TerrestrialBodyInformation;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.geo.StarSystemResourceRequestContainer;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Color4f;
import java.util.Random;

import static java.lang.Math.min;
import static org.schema.game.common.data.world.SectorInformation.SectorType.*;

public class VoidSystem extends StellarSystem {
	public static int RESOURCES = 19; //Metal, Crystal + 8 coloured resources + 3 catalysis resources + all the deprecated stuff //TODO: Make this system StarLoader expandable. Also these two vars are kind of redundant unless we get multi resource ores, and the logic isn't in place for that at the moment.
	public static int RAW_RESOURCES = 19;
	public static final int SYSTEM_SIZE = 16;
	public static final float SYSTEM_SIZEf = 16;
	public static final int SYSTEM_SIZE_X_SYSTEM_SIZE = SYSTEM_SIZE * SYSTEM_SIZE;
	public static final int SYSTEM_SIZE_HALF = SYSTEM_SIZE / 2;
	public static final String UNCLAIMABLE = "UNCLAIMABLE";

	private final Int2ObjectOpenHashMap<VoidSystemObjectInfo> spaceObjectCache = new Int2ObjectOpenHashMap<>(){
		@Override
		public VoidSystemObjectInfo put(int k, VoidSystemObjectInfo voidSystemObjectInfo) {
			if(k > infos.length/DATA_SIZE || k < 0)
				throw new IllegalArgumentException("[VoidSystem] Cannot add at index " + k + ": Invalid index position!!!\nIf the number is positive, this is likely caused by a data index being used as a position index.");
			return super.put(k, voidSystemObjectInfo);
		}
	};
	private Vector3i tmpLocal = new Vector3i();
	private int rockMaterials = -1; //which rock outer material palette from StandardAsteroidMaterials to use. TODO: replace with visuals provider

	public VoidSystem() {
		log("--(Created By VoidSystem Initializer)--");
	}

	public static Vector3i getContainingSystem(Vector3i sectorPos, Vector3i out) {
		out.x = sectorPos.x >= 0 ? sectorPos.x / SYSTEM_SIZE : ((sectorPos.x + 1) / SYSTEM_SIZE - 1);
		out.y = sectorPos.y >= 0 ? sectorPos.y / SYSTEM_SIZE : ((sectorPos.y + 1) / SYSTEM_SIZE - 1);
		out.z = sectorPos.z >= 0 ? sectorPos.z / SYSTEM_SIZE : ((sectorPos.z + 1) / SYSTEM_SIZE - 1);
		return out;
	}

	public static int localCoordinate(int in) {
		return ByteUtil.modU16(in);
	}

	public static Vector3i getSecond(Vector3i firstStarPos, Vector3i sunPositionOffset, Vector3i out) {
		Vector3i second = out;
		second.set(firstStarPos);
		if(sunPositionOffset.length() > 0) {
			if(sunPositionOffset.x != 0) {
				second.x = firstStarPos.x - sunPositionOffset.x;
			} else if(sunPositionOffset.x != 0) {
				second.y = firstStarPos.y - sunPositionOffset.y;
			} else {
				second.z = firstStarPos.z - sunPositionOffset.z;
			}
		} else {
			second.add(-1, -1, -1);
		}
		return second;
	}

	public static Vector3i getSunSectorPosLocal(Galaxy galaxy, Vector3i systemPos, Vector3i out) {
		Vector3i relPos = Galaxy.getRelPosInGalaxyFromAbsSystem(systemPos, new Vector3i());
		int systemType = galaxy.getSystemType(relPos.x, relPos.y, relPos.z);
		Vector3i sunPositionOffset = galaxy.getSunPositionOffset(relPos, new Vector3i());
		out.set(SYSTEM_SIZE / 2, SYSTEM_SIZE / 2, SYSTEM_SIZE / 2);
		out.add(sunPositionOffset);
		return out;
	}

	public static Vector3i getSunSectorPosAbs(Galaxy galaxy, Vector3i systemPos, Vector3i out) {
		getSunSectorPosLocal(galaxy, systemPos, out);
		out.add(systemPos.x * SYSTEM_SIZE, systemPos.y * SYSTEM_SIZE, systemPos.z * SYSTEM_SIZE);
		return out;
	}

	public Vector3i getSunSectorPosAbs(Galaxy galaxy, Vector3i out) {
		return getSunSectorPosAbs(galaxy, getPos(), out);
	}

	public Vector3i getSunSectorPosLocal(Galaxy galaxy, Vector3i out) {
		return getSunSectorPosLocal(galaxy, getPos(), out);
	}

	@Override
	protected byte[] createInfoArray() {
		return new byte[SYSTEM_SIZE * SYSTEM_SIZE * SYSTEM_SIZE * DATA_SIZE];
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.StellarSystem#fromTagStructure(org.schema.schine.resource.Tag)
	 */
	@Override
	public Tag toTagStructure(boolean forClient) {
		return super.toTagStructure(forClient);
		//TODO on client don't send any resource info
	}

	@Override
	public void fromTagStructure(Tag tag) {
		super.fromTagStructure(tag);
		int h = SYSTEM_SIZE / 2;
		int index = (h * SYSTEM_SIZE_X_SYSTEM_SIZE) + (h * SYSTEM_SIZE) + h;
		setCenterSectorType(getSectorType(index));
		log("--(Info loaded from tag structure)--");
		String msg = "[VOIDSYSTEM] LOADED CENTER SECTOR TYPE: " + getPos() + ": " + getCenterSectorType();
		log(msg);
		System.err.println(msg);
		setSimulationStart(getSimulationStart());
		log("Got simulation start time: " + getSimulationStart());
	}

	@Override
	protected void generate(Random random, byte[] infos, Galaxy galaxy, GameServerState state, SectorGenerationInterface systemGeneration) {
		log("--(Began generation...)--");
		int i = 0;
		for(int g = 0; g < ORBIT_COUNT_MAX; g++) {
			//so planets are put randomly on the orbits
			orbitCircularWaitAmount[g] = (int) (random.nextInt(((int) (FastMath.TWO_PI * ((float) g + 1)))) * 1.4f);
		}
		Vector3i relPos = Galaxy.getRelPosInGalaxyFromAbsSystem(this.getPos(), new Vector3i());
		//		Vector4f sunColor = galaxy.getSunColor(relPos);
		int systemType = galaxy.getSystemType(relPos.x, relPos.y, relPos.z);
		setCenterSectorType(galaxy.getSystemTypeAt(getPos()));
		Vector3i sunPositionOffset = galaxy.getSunPositionOffset(relPos, new Vector3i());
		Vector3i starPosition = new Vector3i(SYSTEM_SIZE / 2, SYSTEM_SIZE / 2, SYSTEM_SIZE / 2);
		starPosition.add(sunPositionOffset);
		if(getPos().equals(130000000, 130000000, 130000000)) {
			log("Began tutorial sector generation...");
			setCenterSectorType(VOID);
			//TUTORIAL SYSTEM
			for(int z = 0; z < SYSTEM_SIZE; z++) {
				for(int y = 0; y < SYSTEM_SIZE; y++) {
					for(int x = 0; x < SYSTEM_SIZE; x++) {
						setSectorType(i, VOID);
						i++;
					}
				}
			}
		} else if(getCenterSectorType() == VOID) {
			log("Began void system sector generation...");
			for(int z = 0; z < SYSTEM_SIZE; z++) {
				for(int y = 0; y < SYSTEM_SIZE; y++) {
					for(int x = 0; x < SYSTEM_SIZE; x++) {
						if(!systemGeneration.staticSectorGeneration(state, x + getPos().x * SYSTEM_SIZE, y + getPos().y * SYSTEM_SIZE, z + getPos().z * SYSTEM_SIZE, this, i, galaxy, random)) {
							if(random.nextInt(6000) == 0) {
								setSectorType(i, SPACE_STATION);
								setStationType(i, SpaceStation.SpaceStationType.RANDOM);
							} else if(random.nextInt(5000) == 0) {
								setSectorType(i, SPACE_STATION);
								setStationType(i, SpaceStation.SpaceStationType.PIRATE);
							} else if(random.nextInt(1000) == 0) {
								setSectorType(i, LOW_ASTEROID);
							} else {
								setSectorType(i, VOID);
							}
						}
						i++;
					}
				}
			}
		} else {
			switch(systemType) {
				case Galaxy.TYPE_SUN -> {
					setCenterSectorType(SUN);
					setSectorType(getIndex(starPosition), SUN);
					log("Set SUN centre sector for system type");
				}
				case Galaxy.TYPE_BLACK_HOLE -> {
					setCenterSectorType(BLACK_HOLE);
					setSectorType(getIndex(starPosition), BLACK_HOLE);
					log("Set BLACK_HOLE centre sector for system type");
				}
				case Galaxy.TYPE_DOUBLE_STAR -> {
					setCenterSectorType(DOUBLE_STAR);
					setSectorType(getIndex(starPosition), DOUBLE_STAR);
					Vector3i second = getSecond(starPosition, sunPositionOffset, new Vector3i());
					setSectorType(getIndex(second), DOUBLE_STAR);
					log("Set DOUBLE_STAR centre sector for system type");
				}
				case Galaxy.TYPE_GIANT -> {
					setCenterSectorType(GIANT);
					for(int z = -1; z < 1; z++) {
						for(int y = -1; y < 1; y++) {
							for(int x = -1; x < 1; x++) {
								Vector3i zone = new Vector3i(starPosition);
								zone.add(x, y, z);
								setSectorType(getIndex(zone), GIANT);
							}
						}
					}
					log("Set GIANT centre sector for system type");
				}
			}
			assert (getSectorType(starPosition) == getCenterSectorType()) : getSectorType(starPosition) + " ; " + getCenterSectorType();
			if(getCenterSectorType() == BLACK_HOLE) {
				//black holes are void!
				log("Began wormhole system sector generation...");
				for(int z = 0; z < SYSTEM_SIZE; z++) {
					for(int y = 0; y < SYSTEM_SIZE; y++) {
						for(int x = 0; x < SYSTEM_SIZE; x++) {
							if(getSectorType(i) != BLACK_HOLE) {
								setSectorType(i, VOID);
							}
							i++;
						}
					}
				}
			} else {
				//Normal generation for other center types
				log("Began standard sunsystem sector generation...");
				for(int z = 0; z < SYSTEM_SIZE; z++) {
					for(int y = 0; y < SYSTEM_SIZE; y++) {
						for(int x = 0; x < SYSTEM_SIZE; x++) {
							assert (getIndex(x, y, z) == i);
							if(getSectorType(i) == BLACK_HOLE || getSectorType(i) == SUN || getSectorType(i) == DOUBLE_STAR || getSectorType(i) == GIANT) {
								//set beforehand. nothing to do here
							} else {
								assert (getIndex(starPosition) != i) : getSectorType(i) + "; " + getCenterSectorType() + "; " + getCenterSectorType().ordinal();
								SectorInformation.generate(state, galaxy, x + getPos().x * SYSTEM_SIZE, y + getPos().y * SYSTEM_SIZE, z + getPos().z * SYSTEM_SIZE, this, i, random, systemGeneration);
							}
							i++;
						}
					}
				}
			}
			assert (getSectorType(starPosition) == getCenterSectorType()) : getSectorType(starPosition) + " ; " + getCenterSectorType();
		}
		log("--(Finished generation.)--");
	}

	@Override
	public int getLocalCoordinate(int in) {
		return localCoordinate(in);
	}

	@Override
	public void loadInfos(Galaxy galaxy, byte[] bytes) {
		log("[VoidSystem] Loaded existing infos to system...");
		this.infos = bytes;
		Vector3i relPos = Galaxy.getLocalCoordinatesFromSystem(getPos(), new Vector3i());
		Vector3i offset = galaxy.getSunPositionOffset(relPos, new Vector3i());
		int h = SYSTEM_SIZE / 2;
		int index = ((h + offset.z) * SYSTEM_SIZE_X_SYSTEM_SIZE) + ((h + offset.y) * SYSTEM_SIZE) + (h + offset.x);
		setCenterSectorType(getSectorType(index));
	}

	public StellarSystem getInternal(Vector3i absPos) {
		return this;
	}

	@Override
	public String toString() {
		return "Void" + getPos();
	}

	private int getLocalSeed(){
		return getLocalSeed(0);
	}

	private int getLocalSeed(int secIndex) {
		return (int) (getAbsoluteSectorPos(secIndex, new Vector3i()).hashCode() * GameCommon.getGameState().getUniverseSeed());
	}

	public void updateSystemResources(StarSystemResourceRequestContainer r) {
		r.factionId = getOwnerFaction();
		System.arraycopy(this.systemResources.res, 0, r.res, 0, r.res.length);
	}

	public void setSystemResources(byte[] bytes) { //TODO may be issue with signed/unsigned bytes?
		System.arraycopy(bytes, 0, systemResources.res, 0, min(bytes.length,systemResources.res.length));
	}

	public TerrestrialBodyInformation getPlanetInfo(Vector3i sector) {
		int index = getIndex(StellarSystem.getLocalCoordinates(sector,tmpLocal));
		return getPlanetInfo(index);
	}

	public TerrestrialBodyInformation getPlanetInfo(int index){
		if(index < 0 || index * DATA_SIZE >= infos.length)
			throw new IllegalArgumentException("Invalid index to retrieve planet!");
		else if(getSectorType(index) != PLANET)
			throw new IllegalArgumentException("No planet sector at " + index + "!");

		if(spaceObjectCache.containsKey(index)) {
            return (TerrestrialBodyInformation) spaceObjectCache.get(index);
        } else {
			log("Getting planet: created as new for index " + index);
            return createTerrestrialPlanetDetails(index);
        }
	}

	private TerrestrialBodyInformation createTerrestrialPlanetDetails(int posIndex) {
		Vector3i loc = getAbsoluteSectorPos(posIndex,new Vector3i());
		TerrestrialBodyInformation v = new TerrestrialBodyInformation(loc, getLocalSeed(posIndex), getPlanetType(posIndex));
		spaceObjectCache.put(posIndex,v);
		return v;
	}

	public GasPlanetInformation getGasPlanetInfo(Vector3i sector) {
		int index = getIndex(StellarSystem.getLocalCoordinates(sector,tmpLocal));
		return getGasPlanetInfo(index);
	}

	public GasPlanetInformation getGasPlanetInfo(int posIndex) {
		if(posIndex < 0 || posIndex * DATA_SIZE >= infos.length)
			throw new IllegalArgumentException("Invalid index to retrieve planet!");
		else if(getSectorType(posIndex) != GAS_PLANET)
			throw new IllegalArgumentException("No planet sector at position index " + posIndex + "!");

		if(spaceObjectCache.containsKey(posIndex)) {
            return (GasPlanetInformation) spaceObjectCache.get(posIndex);
        } else {
			log("Getting gas planet: created as new for position index " + posIndex);
            return createGasPlanetDetails(posIndex);
        }
	}

	private GasPlanetInformation createGasPlanetDetails(int posIndex) {
		Vector3i loc = getAbsoluteSectorPos(posIndex,new Vector3i());//get absolute calls get local, which divides by data size, so we multiply here.
		GasPlanetInformation v = new GasPlanetInformation(loc, getLocalSeed(posIndex), getGasPlanetType(posIndex));
		spaceObjectCache.put(posIndex,v);
		return v;
	}

	public interface VoidSystemVisualsProvider{
		public FloatingRock.AsteroidOuterMaterialProvider getAsteroidMaterials(FloatingRock.RockTemperature temp);
		public Color4f getSkyboxColor1(); //should usually remain random outside of very special systems
		public Color4f getSkyboxColor2();
	}

	@Override
	public FloatingRock.AsteroidOuterMaterialProvider getRockMaterials() {
		//TODO get from visuals provider, TODO StarLoader event
		if(rockMaterials == -1){
			Random rand = new Random(getLocalSeed());
			rockMaterials = rand.nextInt(StandardAsteroidMaterials.values().length);
		}
		return StandardAsteroidMaterials.values()[rockMaterials];
	}
}
