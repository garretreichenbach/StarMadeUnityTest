package org.schema.game.common.data.world;

import api.listener.events.world.generation.GasPlanetTypeSelectEvent;
import api.listener.events.world.generation.TerrestrialPlanetTypeSelectEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Random;

public class SectorInformation {

	public static Vector3f tmpAxis = new Vector3f();
	public static Vector3i tmpTT = new Vector3i();
	public static Vector3i tmp = new Vector3i();
	public static Vector3i tmpOut = new Vector3i();
	public static Vector3i tmpOutGal = new Vector3i();
	public static int[] orbits = new int[8];
	public static Transform orbitRot = new Transform();

	public SectorInformation() {
	}

	public static boolean generateOrbits(GameServerState state, Galaxy galaxy, int secX, int secY, int secZ,
	                                     StellarSystem system, int index, Random rand, SectorGenerationInterface iFace) {
		Vector3i relPosInGalaxy = Galaxy.getRelPosInGalaxyFromAbsSystem(system.getPos(), tmpOut);

		galaxy.getSystemOrbits(relPosInGalaxy, orbits);
		int systemType = galaxy.getSystemType(relPosInGalaxy);
		Vector3i sunPositionOffset = galaxy.getSunPositionOffset(relPosInGalaxy, tmpTT);

		galaxy.getAxisMatrix(relPosInGalaxy, orbitRot.basis);

		Vector3f normal = galaxy.getSystemAxis(relPosInGalaxy, tmpAxis);
		tmp.set(secX, secY, secZ);
		Vector3i s = tmp;
		float size = 100;
		float halfsize = size / 2;
		float sectorSize = size / VoidSystem.SYSTEM_SIZEf;
		float sectorSizeHalf = sectorSize / 2;

		Vector3f center = new Vector3f(
				sunPositionOffset.x * sectorSize,
				sunPositionOffset.y * sectorSize,
				sunPositionOffset.z * sectorSize);

		float x = VoidSystem.localCoordinate(secX);
		float y = VoidSystem.localCoordinate(secY);
		float z = VoidSystem.localCoordinate(secZ);

		Vector3f min = new Vector3f(-halfsize + x * sectorSize, -halfsize + y * sectorSize, -halfsize + z * sectorSize);
		Vector3f max = new Vector3f(-halfsize + x * sectorSize + sectorSize, -halfsize + y * sectorSize + sectorSize, -halfsize + z * sectorSize + sectorSize);

		BoundingBox box = new BoundingBox(min, max);
		if(BoundingBox.intersectsPlane(center, normal, box)) {
			for(int j = 0; j < orbits.length; j++) {

				int orbitType = orbits[j];
				if(orbitType > 0) {

					float radius = (j + 1) * sectorSize + sectorSizeHalf;
					Vector3f dist = new Vector3f();
					dist.sub(box.getCenter(new Vector3f()), center);
					dist.normalize();
					dist.scale(radius);

					dist.add(center);

					if(box.isInside(dist)) {

						//we have a good sector
						if(Galaxy.isPlanetOrbit(orbitType)) {

							if(system.isOrbitTakenByGeneration(j)) {
								if(iFace.orbitTakenByGeneration(state, secX, secY, secZ, system, index, galaxy, rand)) {
									return true;
								}

							} else {

								if(system.getOrbitTakenIteration(j) >= system.getOrbitWait(j)) {
									//PLANET
									system.setOrbitTakenByGeneration(j);
									system.log("[Sector Information][Orbit Generation] Definitely a planet at orbit " + j + ", in sector (" + secX + ", " + secY + ", " + secZ + ") - index " + index);
									iFace.definitePlanet(state, secX, secY, secZ, system, index, galaxy, rand);

									return true;
								} else {
									//on planet orbit

									system.incrementOrbit(j);

									if(iFace.onOrbitButNoPlanet(state, secX, secY, secZ, system, index, galaxy, rand)) {
										return true;
									}

								}

							}

						} else {
							//ASTEROID BELT
							iFace.onAsteroidBelt(state, secX, secY, secZ, system, index, galaxy, rand);

							return true;
						}

					}
				}
			}
		}
		return false;
	}

	public static void generate(GameServerState state, Galaxy galaxy, int secX, int secY, int secZ,
	                            StellarSystem system, int index, Random rand, SectorGenerationInterface iFace) {

		if(system.getSectorType(index) == SectorType.SUN || system.getSectorType(index) == SectorType.DOUBLE_STAR || system.getSectorType(index) == SectorType.GIANT) {
			//dont generate anything in the middle
			return;
		}

		if(iFace.staticSectorGeneration(state, secX, secY, secZ, system, index, galaxy, rand)) {
			return;
		}
		iFace.generate(state, secX, secY, secZ, system, index, galaxy, rand);

	}

	public static boolean isPlanetSpotTaken(int x, int y, int z,
	                                        StellarSystem system) {
		for(int xx = x - 1; xx < x + 2; xx++) {
			for(int yy = y - 1; yy < y + 2; yy++) {
				for(int zz = z - 1; zz < z + 2; zz++) {
					if(system.getSectorType(system.getIndex(
							Math.max(0, Math.min(VoidSystem.SYSTEM_SIZE - 1, xx)),
							Math.max(0, Math.min(VoidSystem.SYSTEM_SIZE - 1, yy)),
							Math.max(0, Math.min(VoidSystem.SYSTEM_SIZE - 1, zz)))) == SectorType.PLANET) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void generateTerrestrialPlanet(int x, int y, int z,
	                                             StellarSystem system, int index, Random rand) {
		int randomInt = rand.nextInt(PlanetType.values().length);
		while(!PlanetType.values()[randomInt].allowedSpawn(system, index)) {
			randomInt = rand.nextInt(PlanetType.values().length);
		}
		///INSERTED CODE
		PlanetType type = SectorInformation.PlanetType.values()[randomInt];
		system.log("[SectorInformation] Chose terrestrial type: " + type.name());

		TerrestrialPlanetTypeSelectEvent e = new TerrestrialPlanetTypeSelectEvent(x, y, z, system, index, type);
		StarLoader.fireEvent(TerrestrialPlanetTypeSelectEvent.class, e, false);
		type = e.getPlanetType();

		system.addPlanet(index, type);
		///
	}

	public static void generateGasPlanet(int x, int y, int z,
	                                     StellarSystem system, int index, Random rand) {
		int randomInt = rand.nextInt(GasPlanetType.values().length);
		while(!GasPlanetType.values()[randomInt].allowedSpawn(system, index)) {
			randomInt = rand.nextInt(GasPlanetType.values().length);
		}
		///INSERTED CODE
		GasPlanetType type = SectorInformation.GasPlanetType.values()[randomInt];
		system.log("[SectorInformation] Chose gasgiant type: " + type.getName());

		GasPlanetTypeSelectEvent e = new GasPlanetTypeSelectEvent(x, y, z, system, index, type);
		StarLoader.fireEvent(GasPlanetTypeSelectEvent.class, e, false);
		type = e.getPlanetType();

		system.addPlanet(index, type); //down-conversion to local coords after manual up-conversion to global inside the SectorInformation.generate call in VoidSystem... maybe pass local coords instead? idk
		///
	}

	public enum GasPlanetType { //TODO migrate to GasGiantInformation, make into interface with typeid rather than straight unextendable enums.
		HOT("Hot Gas Giant", "A Gas Giant with a superheated atmosphere containing " +
				"dense materials, such as metals and sand, that are transformed into scalding vapours by the extreme temperatures.\n" +
				"These planets can contain rare heavy materials, however, due to the hazardous atmosphere, any ships attempting to mine here " +
				"will need Environmental Protection chambers in order to avoid taking damage." +
				"\n\n The heavy elements within the planet's atmosphere will also massively inhibit sensors' ability to detect smaller ships inside the atmosphere,\n" +
				"but will also degrade the effectiveness of a ship's sensors while it is inside.", 3000f, 0.2f, 0.4f, 0.4f, new Short2IntOpenHashMap() {
			{
				put(ElementKeyMap.RESS_ORE_METAL_COMMON, 1);
				put(ElementKeyMap.TERRAIN_SAND_ID, 2); //it rains glass
			}
		}),
		MISTY("Misty Gas Giant", "A Gas Giant with a misty atmosphere, typically containing " +
				"light gaseous resources.\nShips don't need any specialized equipment to mine here, however these Gas " +
				"Giants tend to contain less resources than their more hazardous counterparts." +
				"\n\n The dense haze within the planet's atmosphere will also greatly inhibit sensors' ability to detect smaller ships inside the atmosphere,\n" +
				"but will also degrade the effectiveness of a ship's sensors while it is inside.", 0f, 0.0f, 0.2f, 0.2f, new Short2IntOpenHashMap()),
		WINDY("Windy Gas Giant", "A Gas Giant with a windy atmosphere, typically containing " +
				"light gaseous resources.\nShips don't need any specialized equipment to mine here, however these Gas " +
				"Giants tend to have very turbulent atmospheres, making larger and slower ships difficult to control here.", 0f, 1f, 1f, 0.6f, new Short2IntOpenHashMap()),
		FROZEN("Frozen Gas Giant", "A Gas Giant with a very cold atmosphere, typically containing some amount of" +
				" water ice and other cold substances.\n" +
				"Ships don't need any specialized equipment to mine here, however these Gas Giants do not " +
				"typically contain valuable resources.", 0f, 0f, 1f, 0.6f, new Short2IntOpenHashMap() {
			{
				put(ElementKeyMap.TERRAIN_ICE_ID, 2);
			}
		}),
		ENERGETIC("Energetic Gas Giant", "A rare Gas Giant with a highly energetic atmosphere, typically rich in " +
				"a unique type of gaseous resource.\nHowever, due to the hazardous atmosphere, any ships attempting to mine here " +
				"will experience navigation issues, and may receive minor damage without Environmental Protection chambers.", 100f, 0.2f, 0.05f, 0.3f, new Short2IntOpenHashMap() {
			{
				//TODO Metate/gas catalyst resource when implemented
			}
		});

		public static GasPlanetType getRandom(Random rand) {
			return values()[rand.nextInt(values().length)];
		}

		private final String name;
		private final String description;
		private final float maxDamage;
		private final float windForce;
		private final float minScanStrength;
		private final float minRadarSigStrength;
		public final Short2IntOpenHashMap resourceMap; //<Type, Base Amount>

		/**
		 * @param name
		 * @param description
		 * @param maxDamage           The maximum amount of damage per pulse (based on depth into the atmosphere) that this atmosphere can do without environmental damage protection chambers.
		 * @param windForce           The amount of wind deflection a ship will experience in the atmosphere (based on depth into the atmosphere)
		 * @param minScanStrength     The lowest multiplier (between 1 and this number) that a ship's scanning strength will be nerfed by as it descends into the gas giant.
		 * @param minRadarSigStrength The lowest multiplier (between 1 and this number) that a ship's scan signature will be reduced by as it descends into the gas giant.
		 * @param inherentResources   The resource types inherent to this gas giant. These should <b>only</b> include generic resources that every instance of this giant has, NOT special system resources.
		 */
		GasPlanetType(String name, String description, float maxDamage, float windForce, float minScanStrength, float minRadarSigStrength, Short2IntOpenHashMap inherentResources) {
			this.name = name;
			this.description = description;
			this.maxDamage = maxDamage;
			this.windForce = windForce;
			this.minScanStrength = minScanStrength;
			this.minRadarSigStrength = minRadarSigStrength;
			this.resourceMap = inherentResources;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public boolean allowedSpawn(StellarSystem system, int index) {
			float temp = system.getTemperature(system.getLocalSectorPos(index, new Vector3i()));
			switch(this) {
				case HOT -> {
					return temp >= 0.7;
				}
				case MISTY -> {
					return temp <= 0.7;
				}
				case WINDY -> {
					return temp > 0.2 && temp <= 0.7;
				}
				case FROZEN -> {
					return temp <= 0.3;
				}
				case ENERGETIC -> {
					//TODO if(((VoidSystem)system).resourceTypes.contains(ElementKeyMap.RESS_GAS_METATE)) &&
					return temp < 0.9;
				}
				default -> {
					return temp > 0.3 && temp <= 0.7;
				}
			}
		}
	}

	public enum PlanetType {
		MESA(Lng.str("Mesa"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.MESA, 0.5f, 0.75f, true, "planet-mars-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(1, 0.2f, 0.2f, 0.8f), new Vector4f(1, 0.2f, 0.2f, 1)),
		VOLCANIC(Lng.str("Volcanic"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.VOLCANIC, 0.7f, 1.0f, true, "planet-mars-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(1, 0.2f, 0.2f, 0.8f), new Vector4f(1, 0.2f, 0.2f, 1)),
		EARTH(Lng.str("Earth"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.EARTH, 0.35f, 0.6f, true, "planet-earth-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(0.39215f, 0.584313f, 0.92941f, 0.8f), new Vector4f(0.39215f, 0.584313f, 0.92941f, 1)),
		DESERT(Lng.str("Desert"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.DESERT, 0.5f, 0.8f, true, "planet-desert-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(0.61f, 0.8f, 1.0f, 1), new Vector4f(0.61f, 0.8f, 1.0f, 1)),
		CORRUPTED(Lng.str("Corrupted"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.CORRUPTED, 0.2f, 0.45f, true, "planet-purple-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(0.2781f, 0.7411f, 0.2f, 1), new Vector4f(0.2781f, 0.7411f, 0.2f, 1.0f)),
		FROZEN(Lng.str("Frozen"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.FROZEN, 0.1f, 0.25f, true, "planet-ice-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(0.8f, 0.8f, 0.8f, 0.3f), new Vector4f(0.8f, 0.8f, 0.8f, 0.9f)),
		BARREN(Lng.str("Barren"), Lng.str(""), TerrainGenerator.TerrainGeneratorType.BARREN, 0.0f, 1.0f, true, "planet-ice-diff", "planet-earth-clouds", "planet-earth-normal", "planet-earth-specular", new Vector4f(0.0f, 0.0f, 0.0f, 0.0f), new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));

		public final String name;
		public final String description;
		public final TerrainGenerator.TerrainGeneratorType terrainGeneratorType;
		public final float minTemp;
		public final float maxTemp;
		public final String diff;
		public final String clouds;
		public final String normal;
		public final String specular;
		public final Vector4f atmosphere;
		public final Vector4f atmosphereInner;
		public final boolean enabled;

		PlanetType(String name, String description, TerrainGenerator.TerrainGeneratorType terrainGeneratorType, float minTemp, float maxTemp, boolean generate, String diff, String clouds, String normal, String specular, Vector4f atmosphere, Vector4f atmosphereInner) {
			this.name = name;
			this.description = description;
			this.terrainGeneratorType = terrainGeneratorType;
			this.minTemp = minTemp;
			this.maxTemp = maxTemp;
			this.diff = diff;
			this.clouds = clouds;
			this.normal = normal;
			this.specular = specular;
			this.atmosphere = atmosphere;
			this.atmosphereInner = atmosphereInner;
			enabled = generate;
		}

		public boolean allowedSpawn(StellarSystem system, int index) {
			float temp = system.getTemperature(system.getLocalSectorPos(index, new Vector3i()));
			return !(temp < minTemp) && !(temp > maxTemp);
		}
	}

	public enum SectorType {
		SPACE_STATION(0),
		ASTEROID(8),
		PLANET(0),
		GAS_PLANET(6),
		MAIN(0),
		SUN(0),
		BLACK_HOLE(0),
		VOID(0),
		LOW_ASTEROID(2),
		GIANT(0),
		DOUBLE_STAR(2);

		private final int asteroidCountMax;

		private SectorType(int asteroidCountMax) {
			this.asteroidCountMax = asteroidCountMax;
		}

		/**
		 * @return the asteroidCountMax
		 */
		public int getAsteroidCountMax() {
			return asteroidCountMax;
		}
	}

}
