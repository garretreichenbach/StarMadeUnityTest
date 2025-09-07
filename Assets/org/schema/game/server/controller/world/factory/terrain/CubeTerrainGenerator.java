package org.schema.game.server.controller.world.factory.terrain;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.world.factory.WorldCreatorCubePlanetFactory;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CubeTerrainGenerator {
	static final char c = '\200';
	private static final int DISTANCE_RANGE = 2;
	private static final int DISTANCE_RANGE_FULL = 2 * DISTANCE_RANGE + 1;
	private final static int HEIGHT = 4;
	private final static int HEIGHT_BLOCKS = HEIGHT * 16;
	private final static double quarterBlockScale = 0.25D;
	//    public NoiseGeneratorOctaves mobSpawnerNoise;
	private static final byte blockSize = 16;
	private static final byte miniBlockRange = 4;
	private static final int extTerrainRange = 5;
	private static final byte extBlockSize = blockSize + 1;
	private static float distances[];
	private static short fieldInfoArray[] = new short[32768 / (8 / HEIGHT)];

	static {
		initializeDistances();
	}

	private final long seed;
	public Object2ObjectOpenHashMap<Vector3i, List<Region>> optimizedRegions;
	/**
	 * Random.
	 */
	protected Random rand;
	protected OctavesGenerator noiseGen1Oct16;
	//    int field_914_i[][];
	protected OctavesGenerator noiseGen2Oct16;
	protected OctavesGenerator noiseGen3Oct8;
	protected OctavesGenerator noiseGen4Oct4;
	//    private NoiseGeneratorOctaves noiseGen5Oct10for2D;
	protected OctavesGenerator noiseGen6Oct16for2D;
	protected AdditionalModifierAbstract caveGenerator;
	protected boolean hasColumns;
	protected boolean hasC;
	protected float defaultMax = 7;
	//    private double stoneNoise[];
	double noiseSmall8[];
	double noise1Big16[];
	double noise2Big16[];
	//    double noise2DSmall10[];
	double noise2DMid16[];
	Vector3i currentDev = new Vector3i();
	private Region[] regions;
	private double noiseArray[];
	private WorldCreatorCubePlanetFactory worldCreator;
	private byte waterLevel = 2;
	private double flatness = 2.35000000000000001D;
	private AdditionalModifierSlimColumn columnGenerator;
	private AdditionalModifierIceColumns cGenerator;
	private Vector3i p = new Vector3i();
	private Vector3i pFac = new Vector3i();

	public CubeTerrainGenerator(long seed) {

		//        stoneNoise = new double[256];
		//        field_914_i = new int[32][32];
		rand = new Random(seed);
		this.seed = seed;
		initNoises();
		//        mobSpawnerNoise = new NoiseGeneratorOctaves(rand, 8);

	}

	private static void initializeDistances() {
		distances = new float[DISTANCE_RANGE_FULL * DISTANCE_RANGE_FULL];

		for (int x = -DISTANCE_RANGE; x <= DISTANCE_RANGE; x++) {
			for (int z = -DISTANCE_RANGE; z <= DISTANCE_RANGE; z++) {
				float distance = 10f / FastMath.sqrt((x * x + z * z) + 0.2F);
				distances[x + DISTANCE_RANGE + (z + DISTANCE_RANGE) * DISTANCE_RANGE_FULL] = distance;
			}
		}
	}

	public void checkRegionHooks(Segment w) {
		//		System.err.println("CHECKING REGION HOOKS");
		if (regions != null) {
			GameServerState state = (GameServerState) w.getSegmentController().getState();
			for (int i = 0; i < regions.length; i++) {
				if (regions[i] instanceof UsableRegion && ((UsableRegion) regions[i]).hasHook()) {
					//					System.err.println("REGION HOOK "+regions[i]);
					((UsableRegion) regions[i]).addHook(state.getCreatorHooks(), w);
				}
			}
		}
	}

	private void createFromCorner(Segment w) throws SegmentDataWriteException {
		Vector3i p = new Vector3i();
		byte start = 0;
		byte end = (byte) TerrainGenerator.SEG;

		for (byte z = start; z < end; z++) {
			for (byte y = start; y < end; y++) {
				for (byte x = start; x < end; x++) {

					p.set(w.pos);
					p.x += x;
					p.y += y;
					p.z += z;
					for (Region r : regions) {
						if (r.contains(p)) {
							short deligate = r.deligate(p);
							if (deligate != Element.TYPE_ALL) {
								SegmentData data = w.getSegmentData();//.setInfoElementForcedAddUnsynched((byte)(x), (byte)((y)), (byte)(z), deligate, false);
								switch(worldCreator.side) {
									case (Element.TOP) -> data.setInfoElementForcedAddUnsynched((x), (byte) ((y % 16)), (z), deligate, false);
									case (Element.BOTTOM) -> data.setInfoElementForcedAddUnsynched((x), (byte) (15 - (y % 16)), (z), deligate, false);
									case (Element.RIGHT) -> data.setInfoElementForcedAddUnsynched((byte) ((y % 16)), (x), (z), deligate, false);
									case (Element.LEFT) -> data.setInfoElementForcedAddUnsynched((byte) (15 - (y % 16)), (x), (z), deligate, false);
									case (Element.FRONT) -> data.setInfoElementForcedAddUnsynched((x), (z), (byte) ((y % 16)), deligate, false);
									case (Element.BACK) -> data.setInfoElementForcedAddUnsynched((x), (z), (byte) (15 - (y % 16)), deligate, false);
								}
							}
							break;
						}
					}
				}
			}
		}
	}

	private void decorateHeight(int x, int z, int maxHeight, short[] informationArray, SegmentData data, boolean mirror) throws SegmentDataWriteException {
		int top = -1;

		int segPosY = Math.abs(data.getSegmentPos().y);

		int h = segPosY + 16;

		mainLoop:
		for (int y = 63; y >= 0; y--) {

			//       for (int i1 = h-1; i1 >= h-16; i1--)
			if (y < maxHeight) {

				int infoIndex = (x * 16 + z) * HEIGHT_BLOCKS + y;

				if (informationArray[infoIndex] > 0) {
					if (top < 0) {
						top = y;
					}
				}
				if (y >= segPosY && y < h) {

					if (regions != null) {
						p.set(data.getSegmentPos().x + x, y, data.getSegmentPos().z + z);

						if (optimizedRegions != null) {
							int factor = regions[0].optimizeFactor;

							pFac.x = (p.x + Short.MAX_VALUE) / factor;
							pFac.y = (p.y + Short.MAX_VALUE) / factor;
							pFac.z = (p.z + Short.MAX_VALUE) / factor;

							List<Region> rList = optimizedRegions.get(pFac);
							if (rList != null) {
								for (int i = 0; i < rList.size(); i++) {
									if (rList.get(i).contains(p)) {
										short deligate = rList.get(i).deligate(p);
										if (deligate != Element.TYPE_ALL) {
											//											System.err.println("SETTING DEL: "+deligate+": "+(byte)(x)+", "+(byte)((y%16))+", "+(byte)(z)+": "+deligate);
											switch(worldCreator.side) {
												case (Element.TOP) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), deligate, false);
												case (Element.BOTTOM) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (15 - (y % 16)), (byte) (z), deligate, false);
												case (Element.RIGHT) -> data.setInfoElementForcedAddUnsynched((byte) ((y % 16)), (byte) (x), (byte) (z), deligate, false);
												case (Element.LEFT) -> data.setInfoElementForcedAddUnsynched((byte) (15 - (y % 16)), (byte) (x), (byte) (z), deligate, false);
												case (Element.FRONT) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) ((y % 16)), deligate, false);
												case (Element.BACK) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) (15 - (y % 16)), deligate, false);
											}

											continue mainLoop;
										}
									}
								}
							}

						} else {

							//						System.err.println("CHECKING FOR "+p);
							for (int i = 0; i < regions.length; i++) {
								if (regions[i].contains(p)) {
									short deligate = regions[i].deligate(p);
									if (deligate != Element.TYPE_ALL) {
										//									System.err.println("SETTING DEL: "+deligate+": "+(byte)(x)+", "+(byte)((y%16))+", "+(byte)(z)+": "+deligate);
										switch(worldCreator.side) {
											case (Element.TOP) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), deligate, false);
											case (Element.BOTTOM) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (15 - (y % 16)), (byte) (z), deligate, false);
											case (Element.RIGHT) -> data.setInfoElementForcedAddUnsynched((byte) ((y % 16)), (byte) (x), (byte) (z), deligate, false);
											case (Element.LEFT) -> data.setInfoElementForcedAddUnsynched((byte) (15 - (y % 16)), (byte) (x), (byte) (z), deligate, false);
											case (Element.FRONT) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) ((y % 16)), deligate, false);
											case (Element.BACK) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) (15 - (y % 16)), deligate, false);
										}

										continue mainLoop;
									}
								}
							}
						}
					}

					if (top >= 0 && y == top) {
						switch(worldCreator.side) {
							case (Element.TOP) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), worldCreator.getTop(), false);
							case (Element.BOTTOM) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (15 - (y % 16)), (byte) (z), worldCreator.getTop(), false);
							case (Element.RIGHT) -> data.setInfoElementForcedAddUnsynched((byte) ((y % 16)), (byte) (x), (byte) (z), worldCreator.getTop(), false);
							case (Element.LEFT) -> data.setInfoElementForcedAddUnsynched((byte) (15 - (y % 16)), (byte) (x), (byte) (z), worldCreator.getTop(), false);
							case (Element.FRONT) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) ((y % 16)), worldCreator.getTop(), false);
							case (Element.BACK) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) (15 - (y % 16)), worldCreator.getTop(), false);
						}
					} else {
						switch(worldCreator.side) {
							case (Element.TOP) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
							case (Element.BOTTOM) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (15 - (y % 16)), (byte) (z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
							case (Element.RIGHT) -> data.setInfoElementForcedAddUnsynched((byte) ((y % 16)), (byte) (x), (byte) (z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
							case (Element.LEFT) -> data.setInfoElementForcedAddUnsynched((byte) (15 - (y % 16)), (byte) (x), (byte) (z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
							case (Element.FRONT) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) ((y % 16)), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
							case (Element.BACK) -> data.setInfoElementForcedAddUnsynched((byte) (x), (byte) (z), (byte) (15 - (y % 16)), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
						}
						//						if(mirror){
						//							data.setInfoElementForcedAddUnsynched((byte)(x), (byte)(15-(y%16)), (byte)(z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
						//						}else{
						//							data.setInfoElementForcedAddUnsynched((byte)(x), (byte)((y%16)), (byte)(z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
						//						}
					}
				}
			}

		}
	}

	/**
	 * Replaces the stone that was placed in with blocks that match the biome
	 *
	 * @param mirror
	 * @throws SegmentDataWriteException 
	 */
	public void decorateWithBlockTypes(int xPos, int yPos, int zPos, short informationArray[], SegmentData data, boolean mirror) throws SegmentDataWriteException {
		//        byte byte0 = 63;
		//        double d = 0.03125D;
		int dx = (64 - xPos) * 16;
		int dz = (64 - zPos) * 16;
		//        stoneNoise = noiseGen4.generateNoiseOctaves(stoneNoise, x * 16, z * 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D);
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				//                int k = (int)(stoneNoise[i + j * 16] / 3D + 3D + rand.nextDouble() * 0.25D);
				int l = -1;
				int distX = dx + (15 - x);
				int distZ = dz + (15 - z);

				float dist = FastMath.sqrt(distX * distX + distZ * distZ);
				//            	System.err.println("DIST "+dist);
				int maxHeight = 64;
				if (dist > 202) {
					maxHeight -= (32 - (FastMath.sqrt((240 - dist) * 40)));
					maxHeight = Math.max(1, maxHeight);
				}
				if (dist < 240) {
					decorateHeight(x, z, maxHeight, informationArray, data, mirror);
				}
			}
		}
	}

	private void fillMiniBlock(int miniX, int miniZ, short[] infoArray, double[] noiseArray) {
		for (int hY = 0; hY < blockSize; hY++) //16*16 = one mini chunk
		{
			double d = 0.125D;
			double self = noiseArray[((miniZ + 0) * extTerrainRange + (miniX + 0)) * extBlockSize + (hY + 0)];
			double top = noiseArray[((miniZ + 0) * extTerrainRange + (miniX + 1)) * extBlockSize + (hY + 0)];
			double right = noiseArray[((miniZ + 1) * extTerrainRange + (miniX + 0)) * extBlockSize + (hY + 0)];
			double topRight = noiseArray[((miniZ + 1) * extTerrainRange + (miniX + 1)) * extBlockSize + (hY + 0)];
			double front = (noiseArray[((miniZ + 0) * extTerrainRange + (miniX + 0)) * extBlockSize + (hY + 1)] - self) * d;
			double topFront = (noiseArray[((miniZ + 0) * extTerrainRange + (miniX + 1)) * extBlockSize + (hY + 1)] - top) * d;
			double rightFront = (noiseArray[((miniZ + 1) * extTerrainRange + (miniX + 0)) * extBlockSize + (hY + 1)] - right) * d;
			double topFrontRight = (noiseArray[((miniZ + 1) * extTerrainRange + (miniX + 1)) * extBlockSize + (hY + 1)] - topRight) * d;

			/*
			 * this is a block of 8
			 * 
			 * arranged as a cube
			 * 
			 */

			for (int miniChunkY = 0; miniChunkY < HEIGHT; miniChunkY++) // * 8 = height
			{

				double selfValue = self;
				double topValue = top;
				double selfDist = (right - self) * quarterBlockScale;
				double topDist = (topRight - top) * quarterBlockScale;

				for (int miniBlockZ = 0; miniBlockZ < 4; miniBlockZ++) // 4
				{
					int index = miniBlockZ + miniZ * 4 << 10 | 0 + miniX * 4 << 6 | hY * HEIGHT + miniChunkY;

					index -= HEIGHT_BLOCKS;

					double selfValueBefore = selfValue;
					double selfTopDist = (topValue - selfValue) * quarterBlockScale;
					selfValueBefore -= selfTopDist;

					for (int miniBlockY = 0; miniBlockY < 4; miniBlockY++) //4*4 -> all = (4*4)*16* (8*4*4) = 16*16*16 * 8 = one full chunk
					{
						if (hY * HEIGHT + miniChunkY == 0) {

							//always fill in bottom block

							infoArray[index += HEIGHT_BLOCKS] = worldCreator
									.getSolid();

						} else if ((selfValueBefore += selfTopDist) > 0.0D) {

							//normalized density greater than 0

							infoArray[index += HEIGHT_BLOCKS] = worldCreator
									.getSolid();

						} else if (hY * HEIGHT + miniChunkY < waterLevel) {

							//TODO use water if there is water

							infoArray[index += HEIGHT_BLOCKS] = worldCreator
									.getFiller();

						} else {
							//							System.err.println("AIR VALUE: "+selfValueBefore+": "+miniX+", "+hY+", "+miniZ+";    "+currentDev);
							//air

							infoArray[index += HEIGHT_BLOCKS] = 0;

						}
					}

					selfValue += selfDist;
					topValue += topDist;
				}

				self += front;
				top += topFront;
				right += rightFront;
				topRight += topFrontRight;
			}
		}
	}

	protected void generateNoiseFieldInit(int xPos, int yPos, int zPos, int width, int height, int depth) {
		double d = 690.123123D;
		double d1 = d;
		//        noise2DSmall10 = noiseGen5Oct10for2D.generateNoiseOctaves2D(noise2DSmall10, xPos, zPos, width, depth, 1.121D, 1.121D/*, 0.5D*/);
		//        noise2DMid16 = noiseGen6Oct16for2D.generateNoiseOctaves2D(noise2DMid16, xPos, zPos, width, depth, 200D, 200D/*, 0.5D*/);
		noise2DMid16 = noiseGen6Oct16for2D.make2DOctaves(xPos, zPos, width, depth, 200D, 200D/*, 0.5D*/, noise2DMid16);
		noiseSmall8 = noiseGen3Oct8.make3DOctaves(xPos, yPos, zPos, width, height, depth, d / 80D, d1 / 160D, d / 80D, noiseSmall8);
		noise1Big16 = noiseGen1Oct16.make3DOctaves(xPos, yPos, zPos, width, height, depth, d, d1, d, noise1Big16);
		noise2Big16 = noiseGen2Oct16.make3DOctaves(xPos, yPos, zPos, width, height, depth, d, d1, d, noise2Big16);
	}

	public SegmentData generateSegment(SegmentData data, int x, int y, int z, boolean mirror) throws SegmentDataWriteException {

		if (y > 3 && regions != null) {
			createFromCorner(data.getSegment());
			data.getSegmentController().getSegmentBuffer().updateBB(data.getSegment());
			return data;
		}

		assert (x >= 0 && y >= 0 && z >= 0);
		if (cGenerator == null) {
			cGenerator = new AdditionalModifierIceColumns(worldCreator.getTop(), worldCreator.getFiller(), worldCreator.getCaveBottom());
		}
		if (caveGenerator == null) {
			caveGenerator = new AdditionalModifierCave(worldCreator.getTop(), worldCreator.getSolid(), worldCreator.getFiller(), worldCreator.getCaveBottom(), seed + 10000000L * x + 100000L * y + z);
		}
		if (columnGenerator == null) {
			columnGenerator = new AdditionalModifierSlimColumn(worldCreator.getTop(), worldCreator.getFiller(), worldCreator.getCaveBottom());
		}

		Arrays.fill(fieldInfoArray, (short) 0);
		rand.setSeed(x * 0x4f9939f508L + z * 0x1ef1565bd5L + (mirror ? 2345235L : 0));

		//        System.err.println("GENERATING SEGMENT "+x+", "+y+", "+z);

		generateTerrain(x, y, z, fieldInfoArray); //16 * 16 * 16 * 8

		if (hasColumns) {
			columnGenerator.generate(data.getSegmentController().getSeed(), x, y, z, fieldInfoArray, rand);
		} else if (hasC) {
			cGenerator.generate(data.getSegmentController().getSeed(), x, y, z, fieldInfoArray, rand);
		} else {
			caveGenerator.generate(data.getSegmentController().getSeed(), x, y, z, fieldInfoArray, rand);
		}

		decorateWithBlockTypes(x, y, z, fieldInfoArray, data, mirror);

		for (int i = 0; i < worldCreator.getGen().length; i++) {
			for (int j = 0; j < 10; j++) {

				worldCreator.getGen()[i].generate(data, x * 16 + worldCreator.getGen()[i].getRangeX(rand),
						y * 16 + worldCreator.getGen()[i].getRangeY(rand),
						z * 16 + worldCreator.getGen()[i].getRangeZ(rand),
						rand);
			}
		}
		data.getSegmentController().getSegmentBuffer().updateBB(data.getSegment());
		return data;
	}

	/**
	 * Generates the shape of the terrain for the chunk though its all stone though the water is frozen if the
	 * temperature is low enough
	 */
	public void generateTerrain(int xPos, int yPos, int zPos, short infoArray[]) {
		currentDev.set(xPos, yPos, zPos);
		//        System.err.println("INITIALIZE NOISE: "+xPos * miniBlockRange+", "+0+", "+ zPos * miniBlockRange);
		noiseArray = initializeNoiseField(noiseArray, xPos * miniBlockRange, 0, zPos * miniBlockRange, extTerrainRange, extBlockSize, extTerrainRange);

		for (int miniZ = 0; miniZ < miniBlockRange; miniZ++) {
			for (int miniX = 0; miniX < miniBlockRange; miniX++) //4*4 = 16
			{
				fillMiniBlock(miniX, miniZ, infoArray, noiseArray);
			}
		}
	}

	/**
	 * @return the regions
	 */
	public Region[] getRegions() {
		return regions;
	}

	/**
	 * @param regions the regions to set
	 */
	public void setRegions(Region[] regions) {
		this.regions = regions;
	}

	/**
	 * generates a subset of the level's terrain data. Takes 7 arguments: the [empty] noise array, the position, and the
	 * size.
	 */
	protected double[] initializeNoiseField(double noiseField[], int xPos, int yPos, int zPos, int width, int height, int depth) {
		if (noiseField == null) {
			noiseField = new double[width * height * depth];
		}

		//		double d = 684.41200000000003D;
		//		double d1 = 684.41200000000003D;
		//        noise2DSmall10 = noiseGen5Oct10for2D.generateNoiseOctaves2D(noise2DSmall10, xPos, zPos, width, depth, 1.121D, 1.121D/*, 0.5D*/);
		//        noise2DMid16 = noiseGen6Oct16for2D.generateNoiseOctaves2D(noise2DMid16, xPos, zPos, width, depth, 200D, 200D/*, 0.5D*/);
		//		noise2DMid16 = noiseGen6Oct16for2D.generateNoiseOctaves2D(noise2DMid16, xPos, zPos, width, depth, 200D, 200D/*, 0.5D*/);
		//
		//
		//
		//		noiseSmall8 = noiseGen3Oct8.generateNoiseOctaves(noiseSmall8, xPos, yPos, zPos, width, height, depth, d / 80D, d1 / 160D, d / 80D);
		//		noise1Big16 = noiseGen1Oct16.generateNoiseOctaves(noise1Big16, xPos, yPos, zPos, width, height, depth, d, d1, d);
		//		noise2Big16 = noiseGen2Oct16.generateNoiseOctaves(noise2Big16, xPos, yPos, zPos, width, height, depth, d, d1, d);

		generateNoiseFieldInit(xPos, yPos, zPos, width, height, depth);
		//        xPos = zPos = 0;
		int resultIndex = 0;
		int noiseIndex = 0;

		for (int xIter = 0; xIter < width; xIter++) {
			for (int zIter = 0; zIter < depth; zIter++) {
				float normMax = 0.0F;
				float normMin = 0.0F;
				float distTotal = 0.0F;
				//                BiomeGenBase biomegenbase = biomesForGeneration[i1 + 2 + (j1 + 2) * (par5 + 5)];

				float minB = 0.0f;
				float maxB = 3.0f;

				for (int x = -DISTANCE_RANGE; x <= DISTANCE_RANGE; x++) {
					for (int z = -DISTANCE_RANGE; z <= DISTANCE_RANGE; z++) {
						//                        BiomeGenBase biomegenbase1 = biomesForGeneration[i1 + k1 + 2 + (j1 + l1 + 2) * (par5 + 5)];
						float distance = distances[x + 2 + (z + 2) * 5] / (minB + 2f);//(biomegenbase1.minHeight + 2.0F);

						//                        if (biomegenbase1.minHeight > biomegenbase.minHeight)
						//                        {
						//                            distance /= 2.0F;
						//                        }

						normMax += maxB * distance;//biomegenbase1.maxHeight * f4;
						normMin += minB * distance;//biomegenbase1.minHeight * f4;
						distTotal += distance;
					}
				}

				normMax /= distTotal;
				normMin /= distTotal;

				normMax = 3f;//normMax * 0.9F + 0.1F;
				normMin = -0.55f;//(normMin * 4F - 1.0F) / 8;//(float)(HEIGHT);

				//                System.err.println("NORMS: "+normMin+", "+normMax);
				double midNoise162D = noise2DMid16[noiseIndex] / 8000D; //8000

				if (midNoise162D < 0.0D) {
					midNoise162D = -midNoise162D * 0.29999999999999999D;
				}

				midNoise162D = midNoise162D * 3D - 2D; //normalize height

				if (midNoise162D < 0.0D) {
					midNoise162D /= 2D;

					if (midNoise162D < -1D) {
						midNoise162D = -1D;
					}

					midNoise162D /= 1.3999999999999999D;
					midNoise162D /= 2D;
				} else {
					if (midNoise162D > 1.0D) {
						midNoise162D = 1.0D;
					}

					midNoise162D /= 8d;//(double)(HEIGHT);
				}

				noiseIndex++;

				for (double y = 0; y < height; y++) {
					double nMin = normMin;
					double nMax = normMax;

					nMin += midNoise162D * flatness;

					//nMin = (nMin * height) / 16D;
					//                    System.err.println("MIN NOW: "+nMin);
					//                    double halfHeightPlusMin = height / 2D + nMin * 4D;
					double halfHeightPlusMin = height - 2.5d + nMin * defaultMax;// / 2D;

					double resultNoise = 0.0D;

					double normHeightVal = ((y - halfHeightPlusMin) * 12d /*12d*/ * (64)) / (64);// / nMax;

					if (normHeightVal < 0.0D) {
						normHeightVal *= 2D;
					}

					double big16Val1 = noise1Big16[resultIndex] / 512d;
					double big16Val2 = noise2Big16[resultIndex] / 512d;
					double small8Value = (noiseSmall8[resultIndex] / 10d + 1.0d) / 2d;
					//
					if (small8Value < 0.0D) {
						resultNoise = big16Val1;
					} else if (small8Value > 1.0D) {
						resultNoise = big16Val2;
					} else {
						resultNoise = big16Val1 + (big16Val2 - big16Val1) * small8Value;
					}

					resultNoise -= normHeightVal;

					if (y > height - 4) {
						double d11 = (float) (y - (height - 4)) / 3F;
						resultNoise = resultNoise * (1.0D - d11) + -10D * d11;
					}

					noiseField[resultIndex] = resultNoise;
					resultIndex++;
				}
			}
		}
		//        System.err.println("2DNOISE: "+xPos+", "+yPos+", "+zPos+" --- "+width+", "+height+", "+depth+"; "+Arrays.toString(noiseField));
		return noiseField;
	}

	protected void initNoises() {
		noiseGen1Oct16 = new OctavesGenerator(rand, 16);
		noiseGen2Oct16 = new OctavesGenerator(rand, 16);
		noiseGen3Oct8 = new OctavesGenerator(rand, 8);
		noiseGen4Oct4 = new OctavesGenerator(rand, 4);
		//        noiseGen5Oct10for2D = new NoiseGeneratorOctaves(rand, 10);
		noiseGen6Oct16for2D = new OctavesGenerator(rand, 16);
	}

	/**
	 * @param flatness the flatness to set
	 */
	public void setFlatness(double flatness) {
		this.flatness = flatness;
	}

	public void setWorldCreator(
			WorldCreatorCubePlanetFactory worldCreatorCubePlanetFactory) {
		this.worldCreator = worldCreatorCubePlanetFactory;

	}

}
