package org.schema.game.common.data.world.planet.gasgiant;

import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.planet.PlanetaryInformation;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.simulation.resource.PassiveResourceManager;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Color4f;
import java.util.Random;

import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.DefaultResourceProviderTypes.ATMOSPHERE;
import static org.schema.schine.resource.tag.Tag.Type;

public class GasPlanetInformation extends PlanetaryInformation {
	public static float BASE_ROTATION_RATE = 0.1f;
	public static float BASE_GIANT_SIZE = 0.35f; //model scale, relative to sector size
	private static int RADIUS_PRECISION = 1000;

	/**
	 * Ratio of Gas Harvester-available resource pool to passively-extractable resources per tick.
	 */
	public static float ACTIVE_POOL_RATIO_SIZE = 2000; //TODO add to config

	private SectorInformation.GasPlanetType basicType; //TODO migrate these enums to the information classes (for terrestrials also)
	private Color4f color1 = new Color4f(0, 0.5f, 1, 1);
	private Color4f color2 = new Color4f(1, 0, 1, 1); //debug colours
	private float axialTiltX;
	private float axialTiltY;
	private float rotationRate;
	private int textureID; // likewise should be converted to byte; unlikely to have more than 255 textures

	public GasPlanetInformation(Vector3i location, int seed, SectorInformation.GasPlanetType basicType) {
		super(location, "Planet", seed);
		this.basicType = basicType;
		init(getRand());
	}

	public GasPlanetInformation(Tag source) {
		super(source);
		init(getRand());
	}

	@Override
	protected void fromPlanetSubTagStructure(Tag[] tags) {
		//TODO add stuff if there's anything
		basicType = SectorInformation.GasPlanetType.values()[tags[0].getInt()];
		init(getRand());
	}

	@Override
	protected Tag[] toPlanetSubTagStructure() {
		return new Tag[]{
				new Tag(Type.INT, "GasPlanetType", basicType.ordinal()),
				new Tag(Tag.Type.FINISH, "[END]", null)
		};
	}

	@Override
	protected void init(Random rng) {
		super.init(rng);
		setCategoricalName(basicType.getName());
		setDescription(basicType.getDescription());
		color1 = createColor1(seed, basicType);
		color2 = createColor2(seed, basicType);
		textureID = rng.nextInt(3); //There are 3 different gas giant textures. TODO: get all from folder at game start and sum them up or sth
		axialTiltX = (float) rng.nextGaussian() * 30;
		axialTiltY = (float) rng.nextGaussian() * 30;
		rotationRate = (float) (rng.nextGaussian() + 1);

		if(isServer()) {
			if(getAtmosphereResources() == null) {
				// Amount of resources available to mine with the Gas Harvester. Resources regenerate over time.
				Short2IntOpenHashMap activeHarvestingResources = new Short2IntOpenHashMap();
				PassiveResourceProvider provider = new PassiveResourceProvider(new Short2IntOpenHashMap(), getLocation(), ATMOSPHERE.getProviderTypeId());
				setAtmosphereResources(provider);
				for(short id : basicType.resourceMap.keySet()) {
					activeHarvestingResources.put(id, Math.round(basicType.resourceMap.get(id) * ACTIVE_POOL_RATIO_SIZE));
					getAtmosphereResources().addResource(id, basicType.resourceMap.get(id));
				}
				getAtmosphereResources().setActivePool(activeHarvestingResources);
				PassiveResourceManager.addProvider(provider);
			}
			getAtmosphereResources().updatePassive(System.currentTimeMillis());
			getAtmosphereResources().updateActivePool();
		}
	}

	private static Color4f createColor1(long seed, SectorInformation.GasPlanetType basicType) {
		Color4f result = new Color4f();
		Random rng = new Random(seed);
		float a = rng.nextFloat();
		float b = rng.nextFloat();

		switch(basicType) {
			case HOT -> result.set(a * 0.4f, a * 0.14f, b * 0.14f, 1f);
			case ENERGETIC -> result.set(a * 0.4f, a * 0.46f, a * 0.43f, 1f);
			case MISTY -> result.set(a * 0.6f + (rng.nextFloat() * 0.05f), a * 0.6f + (rng.nextFloat() * 0.05f), a * 0.6f + (b * 0.1f), 1f);
			case WINDY -> result.set(a * 0.4f, a * 0.14f, a * 0.03f, 1f);
			case FROZEN -> {
				if(rng.nextBoolean())
					result.set(0.1f, 0.1f, 0.35f + (a * 0.2f), 1f);
				else
					result.set(0.15f, 0.6f - (a * 0.07f), 0.75f - (b * 0.1f), 1f);
				//banded and mostly-unbanded variants
			}
		}
		return result;
	}

	private static Color4f createColor2(long seed, SectorInformation.GasPlanetType basicType) {
		Color4f result = new Color4f();
		Random rng = new Random(seed);
		float a = rng.nextFloat();
		float b = rng.nextFloat();

		switch(basicType) {
			case HOT -> result.set(0.89f - (a * 0.1f), 0.88f - (b * 0.08f), 0.76f - (b * 0.1f), 1f);
			case WINDY -> result.set(0.89f - (a * 0.2f), 0.88f - (a * 0.2f), 0.76f + (((2 * b) - 1) * 0.2f), 1f);
			case FROZEN -> {
				if(rng.nextBoolean())
					result.set(0.15f, 0.15f + (a * 0.19f), 0.37f + (a * 0.2f), 1f);
				else
					result.set(0.2f + (a * 0.2f), 0.62f - (a * 0.09f), 0.77f - (b * 0.12f), 1f);
			}
			default -> result.set(0.89f * (0.8f + (a * 0.2f)), 0.88f + (a * b * 0.02f), 0.7f + (b * 0.05f), 1f);
		}
		return result;
	}

	@Override
	public boolean claimOnSystemClaim() {
		return true; //no block surface to place a facmod
	}

	@Override
	protected int createRadius(Random rand) {
		return Math.round(RADIUS_PRECISION * (0.25f + (0.15f * (rand.nextFloat())))); //(diameter will be between 0.5 and 0.8 * sector size.)
		//radius as % of sector size
	}

	@Override
	public int getRadius() { //in meters
		return Math.round((((float) super.getRadius()) / (float) RADIUS_PRECISION) * ServerConfig.SECTOR_SIZE.getInt());
	}

	public Color4f getColor1() {
		return color1;
	}

	public Color4f getColor2() {
		return color2;
	}

	public float getAxialTiltX() {
		return axialTiltX;
	}

	public float getAxialTiltY() {
		return axialTiltY;
	}

	public float getRotationRate() {
		return rotationRate * BASE_ROTATION_RATE;
	}

	public int getTextureID() {
		return textureID;
	}

	public SectorInformation.GasPlanetType getType() {
		return basicType;
	}

	public void setTypeAndRefresh(SectorInformation.GasPlanetType type) {
		basicType = type;
		init(getRand());
	}
}
