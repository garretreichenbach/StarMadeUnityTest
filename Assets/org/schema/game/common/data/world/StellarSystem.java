package org.schema.game.common.data.world;

import org.apache.poi.util.NotImplemented;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SectorInformation.GasPlanetType;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GalaxyTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.npc.geo.StarSystemResourceRequestContainer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector3f;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;
import static org.schema.game.common.data.element.ElementKeyMap.getResourceIndexFromItemID;
import static org.schema.game.common.data.element.ElementKeyMap.resources;
import static org.schema.game.common.data.world.SectorInformation.SectorType.*;

public abstract class StellarSystem implements TagSerializable {

	//	public static final long SYSTEM_YEAR_MS = 20*60*1000;

	public static final int ORBIT_COUNT_MAX = 8;
	public static final int DATA_SIZE = 2;
	private static final float starSystemSize = (VoidSystem.SYSTEM_SIZE / 2 - 1.5f);
	public static int MAX_RESOURCES_PER_SYSTEM = 5; //does not include common metal/crystal; they will spawn either way as basic bootstrapping requires them (amounts vary between systems however)
	public static float RARE_RESOURCE_AVAILABILITY_CHANCE = 0.02f; //1 in 50 systems can have catalytic resources //TODO: server config
	public static boolean debug;
	private final Vector3i position = new Vector3i();
	private final int[] orbitTakenByGeneration = new int[ORBIT_COUNT_MAX];
	public int[] orbitCircularWaitAmount = new int[ORBIT_COUNT_MAX];
	public double lastCheckOwner;
	/**
	 * The raw industrial resources available for generation and NPCs within this system. Keys correspond to a resource in the <code>ElementKeyMap.resources</code> array
	 */
	public StarSystemResourceRequestContainer systemResources = new StarSystemResourceRequestContainer();
	/**
	 * The sector placement information within the system. The first byte of each pair is the sector type (planet, station, etc.); currently, the second byte is metadata (planet type, etc.).<br/>
	 * The data index for a given sector is always DATA_SIZE * the local index of the sector within the stellarsystem.
	 */
	protected byte[] infos;
	private long simulationStart;
	private SectorInformation.SectorType centerSectorType;
	private long DBId = -1;
	private long universeDay;
	private Vector3i ownerPos = new Vector3i();
	private int ownerFaction;
	private String ownerUID;

	private final List<SystemLogItem> eventLog = new LinkedList<>();
	private ThreadLocal<Vector3i> tmpLocal = ThreadLocal.withInitial(Vector3i::new);

	public StellarSystem(byte[] infos) {
		log("---Recieved existing infos during initialization.---");
		this.infos = infos;
	}

	public StellarSystem() {
	}

	public static Vector3i getPosFromSector(Vector3i absPos, Vector3i out) {
		return VoidSystem.getContainingSystem(absPos, out);
	}

	public static boolean isBorderSystem(Vector3i absPos) {
		int x = ByteUtil.modU16(absPos.x);
		int y = ByteUtil.modU16(absPos.y);
		int z = ByteUtil.modU16(absPos.z);

		Vector3f posTmp = new Vector3f();
		posTmp.set(x - VoidSystem.SYSTEM_SIZE_HALF, y - VoidSystem.SYSTEM_SIZE_HALF, z - VoidSystem.SYSTEM_SIZE_HALF);
		return (posTmp.length() >= (starSystemSize) && posTmp.length() < (starSystemSize + 2.5f));
	}

	public static Vector3i getCenterSector(Vector3i absPos, Vector3i out) {
		int x = VoidSystem.localCoordinate(absPos.x);
		int y = VoidSystem.localCoordinate(absPos.y);
		int z = VoidSystem.localCoordinate(absPos.z);
		out.set(x, y, z);
		return out;
	}

	public static boolean isCenter(Vector3i absoluteSectorPos) {
		int x = VoidSystem.localCoordinate(absoluteSectorPos.x);
		int y = VoidSystem.localCoordinate(absoluteSectorPos.y);
		int z = VoidSystem.localCoordinate(absoluteSectorPos.z);
		return x == VoidSystem.SYSTEM_SIZE_HALF && y == VoidSystem.SYSTEM_SIZE_HALF && z == VoidSystem.SYSTEM_SIZE_HALF;
	}

	public static boolean isCenterNeighbor(Vector3i absPos) {
		int x = VoidSystem.localCoordinate(absPos.x) - VoidSystem.SYSTEM_SIZE_HALF;
		int y = VoidSystem.localCoordinate(absPos.y) - VoidSystem.SYSTEM_SIZE_HALF;
		int z = VoidSystem.localCoordinate(absPos.z) - VoidSystem.SYSTEM_SIZE_HALF;

		return Vector3fTools.length(x, y, z) < 1.42f; //sqrt(2)+epsilon
	}

	public static Vector3i getLocalCoordinates(Vector3i absPos, Vector3i out) {
		int x = VoidSystem.localCoordinate(absPos.x);
		int y = VoidSystem.localCoordinate(absPos.y);
		int z = VoidSystem.localCoordinate(absPos.z);
		out.set(x, y, z);

		return out;
	}

	public static boolean isStarSystem(Vector3i absPos) {
		return isStarSystem(absPos.x, absPos.y, absPos.z);
	}

	public static boolean isStarSystem(int xV, int yV, int zV) {
		int x = ByteUtil.modU16(xV);
		int y = ByteUtil.modU16(yV);
		int z = ByteUtil.modU16(zV);

		Vector3f posTmp = new Vector3f();
		posTmp.set(x - VoidSystem.SYSTEM_SIZE_HALF, y - VoidSystem.SYSTEM_SIZE_HALF, z - VoidSystem.SYSTEM_SIZE_HALF);

		return (posTmp.length() < (starSystemSize));
	}

	public float getDistanceIntensity(float sunIntensity, float distanceToSun) {
		return distanceToSun / Math.max(1, sunIntensity);
	}

	public boolean isHeatDamage(Vector3i absPos, float sunIntensity, float distanceToSun, float heatDamageTreshold) {
		boolean isHeatDamage = false;

		//checking if there is a star
		if(centerSectorType == SectorInformation.SectorType.SUN || centerSectorType == SectorInformation.SectorType.DOUBLE_STAR || centerSectorType == SectorInformation.SectorType.GIANT) {
			float distInten = getDistanceIntensity(sunIntensity, distanceToSun);
			if(ServerConfig.STAR_DAMAGE.isOn() && sunIntensity > 0 && distInten < heatDamageTreshold) {
				isHeatDamage = true;
			}
		}

		return isHeatDamage;
	}

	protected abstract byte[] createInfoArray();

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] root = tag.getStruct();
		if(root == null)
			throw new RuntimeException("!!! Recived null system !!!"); //might be the result of a cache error or something; shouldn't ever happen
		else {
			position.set((Vector3i) root[0].getValue());

			infos = (byte[]) (root[1].getValue());

			if (root[2].getType() == Type.LONG) {
				long simStart = (Long) root[2].getValue();

				// set the simultion to when it was saved
				simulationStart = Math.max(0, (simulationStart - universeDay)) + simStart;

			}

			String msg = "Loaded starsystem from tag: " + position;
			System.err.println(msg);
			log(msg);
		}
	}

	@Override
	public Tag toTagStructure(){
		return toTagStructure(false);
	}

	public Tag toTagStructure(boolean forClient) {
		Tag tPos = new Tag(Type.VECTOR3i, "pos", position);
		Tag infoTag = new Tag(Type.BYTE_ARRAY, null, infos);
		long simStartMod = 0;
		if (simulationStart != 0) simStartMod = simulationStart % universeDay;
		Tag simStart = new Tag(Type.LONG, "simStartMod", simStartMod);
		Tag result = new Tag(Type.STRUCT, "StarSystem", new Tag[] {tPos, infoTag, simStart, FinishTag.INST});
		System.out.println("[DEBUG] wrote StellarSystem to tag: " + result.getName() + " Struct[" + result.getStruct().length + "]");
		return result;
	}

	public void generate(Random random, Galaxy galaxy, GameServerState state, SectorGenerationInterface systemGeneration) {
		infos = createInfoArray();
		galaxy.getSystemResources(position, systemResources, new GalaxyTmpVars());
		generate(random, infos, galaxy, state, systemGeneration);
	}

	protected abstract void generate(Random random, byte[] infos, Galaxy galaxy, GameServerState state, SectorGenerationInterface systemGeneration);

	public Vector3i getAbsoluteSectorPos(int index, Vector3i out) {
		getLocalSectorPos(index, out);
		toAbsolute(out);
		return out;
	}

	/**
	 * Converts local coordinates to absolute coordinates.
	 * @param out The local coordinates to convert. Note that this will mutate the input, and will <b>NOT</b> return a fresh copy.
	 * @return The input Vector3i, converted to absolute.
	 */
	public Vector3i toAbsolute(Vector3i out){
		out.add(position.x * VoidSystem.SYSTEM_SIZE, position.y * VoidSystem.SYSTEM_SIZE, position.z * VoidSystem.SYSTEM_SIZE);
		return out;
	}
	
	public static Vector3i toAbsolute(Vector3i systemPos, Vector3i out) {
		out.add(systemPos.x * VoidSystem.SYSTEM_SIZE, systemPos.y * VoidSystem.SYSTEM_SIZE, systemPos.z * VoidSystem.SYSTEM_SIZE);
		return out;
	}

	/**
	 * @return the centerSectorType
	 */
	public SectorInformation.SectorType getCenterSectorType() {
		return centerSectorType;
	}

	/**
	 * @param centerSectorType the centerSectorType to set
	 */
	public void setCenterSectorType(SectorInformation.SectorType centerSectorType) {
		this.centerSectorType = centerSectorType;
	}

	public long getDBId() {
		return DBId;
	}

	/**
	 * @param dBId the dBId to set
	 */
	public void setDBId(long dBId) {
		DBId = dBId;
	}

	public int getIndex(int localX, int localY, int localZ) {
		return localZ * VoidSystem.SYSTEM_SIZE_X_SYSTEM_SIZE + localY * VoidSystem.SYSTEM_SIZE + localX;
	}

	public int getIndex(Vector3i localSectorPos) {
		return getIndex(localSectorPos.x, localSectorPos.y, localSectorPos.z);
	}

	public byte[] getInfos() {
		return infos;
	}

	public abstract int getLocalCoordinate(int in);

	/**
	 * Retrieve the local sector position
	 * @param index the 'natural' calculated index of the local sector within the system. <b>NOT</b> to be confused with the data index (index * DATA_SIZE), which would yield a totally different sector.
	 * @param out
	 * @return
	 */
	public Vector3i getLocalSectorPos(int index, Vector3i out) {
int z = index / VoidSystem.SYSTEM_SIZE_X_SYSTEM_SIZE;
		int zDiv = (z * VoidSystem.SYSTEM_SIZE_X_SYSTEM_SIZE);
		int y = (index - zDiv) / VoidSystem.SYSTEM_SIZE;
		int yDiv = y * VoidSystem.SYSTEM_SIZE;
		int x = ((index - zDiv) - yDiv) % VoidSystem.SYSTEM_SIZE;

		out.set(x, y, z);

		return out;
	}

	public String getName() {
		return "default";
	}

	public PlanetType getPlanetType(int index) {
		int dataIndex = index * DATA_SIZE;
		return PlanetType.values()[min(PlanetType.values().length - 1, infos[dataIndex + 1])];
	}

	public PlanetType getPlanetType(Vector3i absPos) {
		int x = getLocalCoordinate(absPos.x);
		int y = getLocalCoordinate(absPos.y);
		int z = getLocalCoordinate(absPos.z);
		return getPlanetType(getIndex(x, y, z));
	}

	/**
	 * @return the position
	 */
	public Vector3i getPos() {
		return position;
	}

	public SectorInformation.SectorType getSectorType(int index) {
		int dataIndex = index * DATA_SIZE;
		return SectorInformation.SectorType.values()[infos[dataIndex]];
	}

	public SectorInformation.SectorType getSectorType(Vector3i absPos) {
		int x = getLocalCoordinate(absPos.x);
		int y = getLocalCoordinate(absPos.y);
		int z = getLocalCoordinate(absPos.z);
		int index = getIndex(x, y, z);
		SectorInformation.SectorType result = getSectorType(index);
		//if(result == PLANET || result == GAS_PLANET) log("[StellarSystem] Retrieving sector type from system for sector " + absPos + " (Local: " + x +", " + y + ", " + z + "): " + result);
		return result;
	}

	/**
	 * @return the simulationStart
	 */
	public long getSimulationStart() {
		return simulationStart;
	}

	/**
	 * @param simulationStart the simulationStart to set
	 */
	public void setSimulationStart(long simulationStart) {
		this.simulationStart = simulationStart;
	}

	public SpaceStation.SpaceStationType getSpaceStationTypeType(int index) {
		int dataIndex = index * DATA_SIZE;

		return SpaceStation.SpaceStationType.values()[min(SpaceStation.SpaceStationType.values().length - 1, infos[dataIndex + 1])];
	}

	public SpaceStation.SpaceStationType getSpaceStationTypeType(Vector3i absPos) {
		int x = getLocalCoordinate(absPos.x);
		int y = getLocalCoordinate(absPos.y);
		int z = getLocalCoordinate(absPos.z);
		return getSpaceStationTypeType(getIndex(x, y, z));
	}

	public float getTemperature(Vector3i pos) {
		if(getCenterSectorType() == VOID) return 0.5f; //technically should be 0 since it's cold space

		int x = VoidSystem.localCoordinate(pos.x) - VoidSystem.SYSTEM_SIZE / 2;
		int y = VoidSystem.localCoordinate(pos.y) - VoidSystem.SYSTEM_SIZE / 2;
		int z = VoidSystem.localCoordinate(pos.z) - VoidSystem.SYSTEM_SIZE / 2;

		Vector3f max = new Vector3f(VoidSystem.SYSTEM_SIZE_HALF - 1, VoidSystem.SYSTEM_SIZE_HALF - 1, VoidSystem.SYSTEM_SIZE_HALF - 1);
		Vector3f actual = new Vector3f(x, y, z);

		float pc = min(1, actual.length() / max.length());

		return 1.0f - pc;
	}

	public boolean isChanged() {
		return false;
	}


	public abstract void loadInfos(Galaxy galaxy, byte[] bytes);

	public void addPlanet(Vector3i localCoords, PlanetType type) {
		if(localCoords.getFirstLargest() >= VoidSystem.SYSTEM_SIZE || localCoords.getFirstSmallest() < 0)
			throw new IllegalArgumentException("Provided coordinates for planet placement are not local coordinates! Provided: " + localCoords);
		int index = getIndex(localCoords);
		log("[StellarSystem] Adding terrestrial " + type + " planet at " + localCoords + "(index " + index + ")");
		int dataIndex = index * DATA_SIZE;
		infos[dataIndex + 1] = (byte) type.ordinal();
	}

	public void addPlanet(Vector3i localCoords, GasPlanetType type) {
		if(localCoords.getFirstLargest() >= VoidSystem.SYSTEM_SIZE || localCoords.getFirstSmallest() < 0)
			throw new IllegalArgumentException("Provided coordinates for planet placement are not local coordinates! Provided: " + localCoords);
		int index = getIndex(localCoords);
		log("[StellarSystem] Adding gas " + type + " planet at " + localCoords + "(index " + index + ")");
		int dataIndex = index * DATA_SIZE;
		infos[dataIndex + 1] = (byte) type.ordinal();
	}


	public void addPlanet(int index, PlanetType planetType) {
		Vector3i pos = getLocalSectorPos(index, new Vector3i());
		log("< Adding planet by index: " + index + "(as local sector " + pos + ") >");
		addPlanet(pos,planetType);
	}

	public void addPlanet(int index, GasPlanetType planetType) {
		Vector3i pos = getLocalSectorPos(index, new Vector3i());
		log("< Adding planet by index: " + index + "(as local sector " + pos + ") >");
		addPlanet(pos,planetType);
	}

	/*
	public short getPosIndex(int index){
		int dataIndex = index * DATA_SIZE;
		return ByteUtil.shortReadByteArray(infos, dataIndex+2);
	}
	public void setPosIndex(int index, short posIndexValue){
		int dataIndex = index * DATA_SIZE;
		ByteUtil.shortWriteByteArray(posIndexValue, infos, dataIndex+2);
	}
	*/

    public void setSectorType(int index, SectorInformation.SectorType type) {
		if (type == PLANET || type == GAS_PLANET)
			log("[StellarSystem] Setting sector type of sector " + index + " to " + type + ".");
		int dataIndex = index * DATA_SIZE;

		assert (infos[dataIndex] != (byte) SectorInformation.SectorType.BLACK_HOLE.ordinal());
		infos[dataIndex] = (byte) type.ordinal();
	}

	public void setStationType(int index, SpaceStation.SpaceStationType type) {
		int dataIndex = index * DATA_SIZE;
		infos[dataIndex + 1] = (byte) type.ordinal();
	}

	/**
	 * @return the ownerUID
	 */
	public String getOwnerUID() {
		return ownerUID;
	}

	/**
	 * @param ownerUID the ownerUID to set
	 */
	public void setOwnerUID(String ownerUID) {
		this.ownerUID = ownerUID;
	}

	/**
	 * @return the ownerFaction
	 */
	public int getOwnerFaction() {
		return ownerFaction;
	}

	/**
	 * @param ownerFaction the ownerFaction to set
	 */
	public void setOwnerFaction(int ownerFaction) {
		this.ownerFaction = ownerFaction;
	}

	/**
	 * @return the ownerPos
	 */
	public Vector3i getOwnerPos() {
		return ownerPos;
	}

	/**
	 * @param ownerPos the ownerPos to set
	 */
	public void setOwnerPos(Vector3i ownerPos) {
		this.ownerPos = ownerPos;
	}

	public int getOrbitWait(int j) {
		return orbitCircularWaitAmount[j];
	}

	public int getOrbitTakenIteration(int j) {
		return orbitTakenByGeneration[j];
	}

	public boolean isOrbitTakenByGeneration(int j) {
		return orbitTakenByGeneration[j] < 0;
	}

	public void setOrbitTakenByGeneration(int j) {
		orbitTakenByGeneration[j] = -1;
	}

	public void incrementOrbit(int j) {
		orbitTakenByGeneration[j]++;
	}

	public GasPlanetType getGasPlanetType(int index) {
		int dataIndex = index * DATA_SIZE;
		return GasPlanetType.values()[min(GasPlanetType.values().length - 1, infos[dataIndex + 1])];
	}

	public GasPlanetType getGasPlanetType(Vector3i pos) {
		int x = getLocalCoordinate(pos.x);
		int y = getLocalCoordinate(pos.y);
		int z = getLocalCoordinate(pos.z);
		return getGasPlanetType(getIndex(x, y, z));
	}

	public void log(String s) {
		eventLog.add(new SystemLogItem(s, Thread.currentThread().getStackTrace()));
	}

	public String getEventLog() {
		StringBuilder s = new StringBuilder();
		for(SystemLogItem line : eventLog){
			s.append(line.getMessage()).append('\n');
		}
		return s.toString();
	}

	public FloatingRock.AsteroidOuterMaterialProvider getRockMaterials() {
		return FloatingRock.FALLBACK_MATERIAL; //should not be called; VoidSystem and any other potential inheritor should implement their own alg
	}

	/**
	 * @param id the item ID
	 * @return
	 */
	public byte getResourceDensityFromId(short id){
		return systemResources.res[getResourceIndexFromItemID(id)];
	}

	public float getTrueResourceDensityFromId(short id) {
		return (float) getResourceDensityFromId(id) / 127f;
	}

	/**
	 * @param pos the coordinates of the sector. This is important for determining the allowed resources based on the asteroid temperature (and maybe other factors later on, e.g. proximity to anomalies that change resource types)
	 * @param random the Random to use for selecting resources
	 * @return An array of several resource indices (based on ElementKeyMap.resources) corresponding to ores that an asteroid may use within the stellar system.
	 */
	public byte[] getNewAsteroidResources(Vector3i pos, Random random) {
		if(getCenterSectorType() == VOID || random.nextFloat() < 0.4) return FloatingRock.COMMON_ORES; //TODO config variable or per system value for common ore override frequency
		int maxCount = 0;
		for(byte b : systemResources.res) if(b > 0) maxCount++; //will always be at least 2 due to common ores
		int count = 1 + random.nextInt(min(3,maxCount+1)); //TODO: should this just be static? not sure how much value there is in randomizing
		byte[] ores = new byte[count];
		int attempts=0;
		for(int i=0; i < count && attempts < VoidSystem.RESOURCES;){
			byte index = (byte)random.nextInt(VoidSystem.RESOURCES);
			byte resPermission = resCanSpawnAt(index,pos);
			if(resPermission > -1 && (systemResources.res[index] > 0 || resPermission == 1)) {
				//if (resPermission == 1 || !ElementKeyMap.isGas(resources[index])){ //can't have gases inside rocks... allowing it temporarily though
				ElementInformation ore = ElementKeyMap.getInfo(resources[index]);
				if(!ore.deprecated){
					ores[i] = index;
					i++;
				}
				//}
			} else attempts++;
			//This will produce doubles sometimes. Not sure if this is desirable, or if the array should just get truncated to remove doubles (would take longer)
		}
		if(attempts >= VoidSystem.RESOURCES) return FloatingRock.COMMON_ORES; //every system will have a frequency value for these; they're just the common resource types.
		return ores;
	}

	/**
	 * Determines if a resource is allowed to spawn in a given sector.<br/>
	 * Currently this function is a short-circuit, but it may eventually take into account factors such as temperature,
	 * distance from the ecliptic, or proximity to certain anomalies that may allow or prevent specific resources from spawning.
	 * @param resIndex the resource to check
	 * @param pos the sector location
	 * @return 1 if resource MUST spawn if selected, regardless of system resource values; 0 if resource may spawn, -1 if resource cannot spawn here
	 */
	@NotImplemented
	private byte resCanSpawnAt(int resIndex, Vector3i pos) {
		//short itemID = resources[resIndex];
		//if(ElementKeyMap.isCommonResource(itemID)) return true;
		//else {
		// ...
		//}
		return 0; //TODO
	}

	private record SystemLogItem(String message, StackTraceElement[] stackTrace) {
		public String getMessage() {
			return message;
		}

		public StackTraceElement[] getStackTrace() {
			return stackTrace;
		}
	}
}
