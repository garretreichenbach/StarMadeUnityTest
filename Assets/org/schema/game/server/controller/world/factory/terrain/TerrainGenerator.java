package org.schema.game.server.controller.world.factory.terrain;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.SegmentLodDrawer;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.WorldCreatorPlanetFactory;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TerrainGenerator {

	public static int SEG = 16;
	public static int SEG_HALF = 8;
	
	public final static int plateauHeight = 4;
	private static final int DISTANCE_RANGE = 2;
	private static final int DISTANCE_RANGE_FULL = 2 * DISTANCE_RANGE + 1;
	private final static int HEIGHT_BLOCKS = plateauHeight * 16;
	private final static double quarterBlockScale = 0.25d;
	private static final byte blockSize = 16;
	private static final byte blockColumnSize = 4;
	private static final int blockColumnCountPerXZDim = 5;
	private static final byte blockColumnCountPerYDim = blockSize + 1;
	static boolean block = false;
	private static float distances[];

	static {
		initializeDistances();
	}

	final int maxHeight = 64;
	final int minHeight = 1;
	final int margin = 11;
	private final byte waterLevel = 2;
	private final long seed;
	public Object2ObjectOpenHashMap<Vector3i, List<Region>> optimizedRegions;
	public Region[] regions;
	public AdditionalModifierAbstract caveGenerator;
	public AdditionalModifierSlimColumn columnGenerator;
	public AdditionalModifierIceColumns cGenerator;
	protected OctavesGenerator noiseGen1Oct16;
	protected OctavesGenerator noiseGen2Oct16;
	protected OctavesGenerator noiseGen3Oct8;
	protected OctavesGenerator noiseGen4Oct4;
	protected OctavesGenerator noiseGen6Oct16for2D;
	protected float defaultMax = 7; // default 7
	protected float planetEdgeHeight = 24; // default 24
	protected float polyMargin = 28; // default 28
	protected boolean hasColumns;
	protected boolean hasC;
	protected double halfHeightFactor = 4.7;
	protected double heighNormValue = 48;// default 128;
	private WorldCreatorPlanetFactory worldCreator;
	private double flatness = 2.35000000000000001D;


	public TerrainGenerator(long seed) {

		// stoneNoise = new double[256];
		// field_914_i = new int[32][32];
		this.seed = seed;
		initNoises(new Random(seed));
		// mobSpawnerNoise = new NoiseGeneratorOctaves(rand, 8);

	}

	private static void initializeDistances() {
		distances = new float[DISTANCE_RANGE_FULL * DISTANCE_RANGE_FULL];

		for (int x = -DISTANCE_RANGE; x <= DISTANCE_RANGE; x++) {
			for (int z = -DISTANCE_RANGE; z <= DISTANCE_RANGE; z++) {
				float distance = 10f / FastMath.sqrt((x * x + z * z) + 0.2F);
				distances[x + DISTANCE_RANGE + (z + DISTANCE_RANGE)
						* DISTANCE_RANGE_FULL] = distance;
			}
		}
	}

	public void checkRegionHooks(Segment w, RequestDataPlanet requestData) {
		// System.err.println("CHECKING REGION HOOKS");
		if (regions != null) {
			// regions operate on the info of all segments and must therefor be
			// synched
			synchronized (regions) {
				GameServerState state = (GameServerState) w
						.getSegmentController().getState();
				for (int i = 0; i < regions.length; i++) {
					if (regions[i] instanceof UsableRegion
							&& ((UsableRegion) regions[i]).hasHook()) {
						// System.err.println("REGION HOOK "+regions[i]);
						((UsableRegion) regions[i]).addHook(
								state.getCreatorHooks(), w);
					}
				}
			}
		}
	}

	// final float mar;

	// final float mar = 1.14f;

	private void createFromCorner(Vector3i pos, SegmentDataInterface data, RequestDataPlanet requestData) throws SegmentDataWriteException {
		Vector3i p = new Vector3i();
		byte start = 0;
		byte end = (byte) TerrainGenerator.SEG;
		synchronized (regions) {
			for (byte z = start; z < end; z++) {
				for (byte y = start; y < end; y++) {
					for (byte x = start; x < end; x++) {

						int margin = this.margin;
						float mar = 0;// this.mar;
						int dx = pos.x;
						int dz = pos.z;

						if (Dodecahedron.pnpoly(worldCreator.poly, margin(dx
								+ x), margin(dz + z), mar)) {
							p.set(pos.x + x, pos.y + y, pos.z + z);

							for (Region r : regions) {
								if (r.contains(p)) {
									short deligate = r.deligate(p);
									if (deligate != Element.TYPE_ALL) {
										data
												.setInfoElementForcedAddUnsynched(
														(x), ((y)), (z),
														deligate, false);
									}
									break;
								}
							}

						}
					}
				}
			}
		}
	}

	private void decorateHeight(int x, int z, int dx, int dz, int maxHeight,
	                            int minHeight, short[] informationArray, SegmentDataInterface data,
	                            RequestDataPlanet requestData) throws SegmentDataWriteException {
		int top = -1;

		if(data == null){
			throw new NullPointerException("Data null");
		}
		if(worldCreator == null){
			throw new NullPointerException("worldCreator null");
		}
		int segPosYMin = Math.abs(data.getSegmentPos().y);

		int segPosYMax = segPosYMin + 16;

		float marginSolo = requestData.getR().mar - 1.0f;
		
		int xzCoordIndex = (x * 16 + z) * HEIGHT_BLOCKS;
		short topType = worldCreator.getTop();
		byte activateOnPlacement = ElementInformation
		.activateOnPlacement(topType);
		
		final int min = Math.max(segPosYMin, 0);
		final int max = Math.min(maxHeight, segPosYMax);
		
		
		float testX = margin(dx + (15 - x)); 
		float testZ = margin(dz + (15 - z));
		
		float planetHeightInv = 1f/planetEdgeHeight;
		mainLoop:
		for (int y = max-1; y >= min; y--) {

			if (y < planetEdgeHeight && y > 0 && 
					!Dodecahedron.pnpoly(
						worldCreator.poly, 
						testX,
						testZ,
						1.0f + (y * planetHeightInv) * marginSolo)) {
				//smooth out dodecahedron edges
				return;
			}


			int infoIndex = xzCoordIndex + y;

			if (y <= minHeight && y >= segPosYMin && y < segPosYMax
					&& informationArray[infoIndex] <= 0) {
				
				data.setInfoElementForcedAddUnsynched(
						(byte) (x),
						((byte) ((y) % 16)),  
						(byte) (z), 
						topType, 
						(byte) Element.TOP,
						activateOnPlacement, 
						false);
			} else {

				if (informationArray[infoIndex] > 0) {
					if (top < 0) {
						//first solid block is the top and is set once
						top = y;
					}
				}
				if (y >= segPosYMin && y < segPosYMax) {

					if (regions != null) {
						synchronized (regions) {
							requestData.getR().p.set(data.getSegmentPos().x + x,
									y, data.getSegmentPos().z + z);

							if (optimizedRegions != null) {
								int factor = regions[0].optimizeFactor;

								requestData.getR().pFac.x = (requestData.getR().p.x + Short.MAX_VALUE)
										/ factor;
								requestData.getR().pFac.y = (requestData.getR().p.y + Short.MAX_VALUE)
										/ factor;
								requestData.getR().pFac.z = (requestData.getR().p.z + Short.MAX_VALUE)
										/ factor;

								List<Region> rList = optimizedRegions
										.get(requestData.getR().pFac);
								
								if (rList != null) {
									final int rsize = rList.size();
									for (int i = 0; i < rsize; i++) {
										if (rList.get(i).contains(
												requestData.getR().p)) {
											short deligate = rList
													.get(i)
													.deligate(requestData.getR().p);
											if (deligate != Element.TYPE_ALL) {
												data.setInfoElementForcedAddUnsynched(
														(byte) (x),
														((byte) ((y) % 16)),  
														(byte) (z),
														deligate, false);
												continue mainLoop;
											}
										}
									}
								}

							} else {
								final int rLength = regions.length;
								for (int i = 0; i < rLength; i++) {
									if (regions[i].contains(requestData.getR().p)) {
										short deligate = regions[i]
												.deligate(requestData.getR().p);
										if (deligate != Element.TYPE_ALL) {
											data.setInfoElementForcedAddUnsynched(
													(byte) (x),
													((byte) ((y) % 16)),  
													(byte) (z), deligate,
													false);
											continue mainLoop;
										}
									}
								}
							}
						}
					}

					if (top > 0 && y == top) {
						data.setInfoElementForcedAddUnsynched(
								(byte) (x),
								((byte) ((y) % 16)),  
								(byte) (z), 
								topType, 
								(byte) Element.TOP,
								activateOnPlacement, false);
					} else {
						data.setInfoElementForcedAddUnsynched(
								(byte) (x),
								((byte) ((y) % 16)),  
								(byte) (z),
								informationArray[infoIndex] > 0 ? informationArray[infoIndex]
										: 0, false);
					}
				}
			}

		}
	}

	public float margin(int input) {
		input -= 8;
		return input - 0.5f;// (float)(input < 0 ? input + margin : input -
		// margin) - 0.5f;
	}

	
	private boolean isPointInDodeca(int dx, int dz, int x, int z, RequestDataPlanet requestData){
		return Dodecahedron.pnpoly(worldCreator.poly,
				margin(dx + (15 - x)), margin(dz + (15 - z)), requestData.getR().mar);
	}
	private boolean isSegmentInDodeca(int xPos, int zPos, int dx, int dz, RequestDataPlanet requestData){
		
		return 
				isPointInDodeca(dx, dz, 0, 0, requestData) ||
				isPointInDodeca(dx, dz, 0, 15, requestData) ||
				isPointInDodeca(dx, dz, 15, 15, requestData) ||
				isPointInDodeca(dx, dz, 15, 0, requestData);
				
		
		
	}
	private boolean isSegmentAllInDodeca(int xPos, int zPos, int dx, int dz, RequestDataPlanet requestData){
		
		return 
				isPointInDodeca(dx, dz, 0, 0, requestData) &&
				isPointInDodeca(dx, dz, 0, 15, requestData) &&
				isPointInDodeca(dx, dz, 15, 15, requestData) &&
				isPointInDodeca(dx, dz, 15, 0, requestData);
		
		
		
	}
	
	/**
	 * Replaces the stone that was placed in with blocks that match the biome
	 *
	 * @param mirror
	 * @throws SegmentDataWriteException 
	 */
	public void decorateWithBlockTypes(int xPos, int yPos, int zPos,
	                                   short informationArray[], SegmentDataInterface data,
	                                   RequestDataPlanet requestData) throws SegmentDataWriteException {
		
		
		// byte byte0 = 63;
		// double d = 0.03125D;
		int dx = (64 - xPos) * 16;
		int dz = (64 - zPos) * 16;
		// stoneNoise = noiseGen4.generateNoiseOctaves(stoneNoise, x * 16, z *
		// 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D);
		
		if(!isSegmentInDodeca(xPos, zPos, dx, dz, requestData)){
			return;
		}
		boolean allInDodeca = isSegmentAllInDodeca(xPos, zPos, dx, dz, requestData);
		
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				// int k = (int)(stoneNoise[i + j * 16] / 3D + 3D +
				// rand.nextDouble() * 0.25D);
				int l = -1;
				int distX = dx + (15 - x);
				int distZ = dz + (15 - z);

				// float dist = FastMath.sqrt(distX * distX + distZ * distZ);
				// System.err.println("DIST "+dist);
				int maxHeight = this.maxHeight;
				int minHeight = this.minHeight;

				int margin = this.margin;
				float mar = requestData.getR().mar;
				if (allInDodeca || Dodecahedron.pnpoly(worldCreator.poly,
						margin(dx + (15 - x)), margin(dz + (15 - z)), mar)) {
					float nearestEdge = Dodecahedron.nearestEdge(
							worldCreator.poly, margin(dx + (15 - x)), margin(dz
									+ (15 - z)), mar);

					maxHeight = Math.min(
							maxHeight,
							Math.round(Math.max(
									planetEdgeHeight,
									(FastMath.pow(nearestEdge
											+ planetEdgeHeight + 24f, 0.8f)))));
					minHeight = (int) Math.max(1, planetEdgeHeight
							- (nearestEdge - 4));

					if (nearestEdge > 20) {
						maxHeight = 64;
					} else if (nearestEdge > 5) {
						maxHeight = (int) Math.min(
								FastMath.pow(maxHeight, 1.07f), 64);
					}

					// System.err.println("NEAREST EDGE "+nearestEdge);
					// if(Dodecahedron.pnpoly(PlanetCreatorThread.polygon, dx +
					// (15 - x), dz + (15 - z))){
					// maxHeight -= (32 - (FastMath.sqrt((240 - dist)*40)));
					// maxHeight = Math.max(1, maxHeight);
					// }

					decorateHeight(x, z, dx, dz, maxHeight, minHeight,
							informationArray, data, requestData);
				}
			}
		}
	}

	private void fill4x4BlockColumn(int miniX, int miniZ, short[] infoArray,
	                           double[] noiseArray, RequestDataPlanet requestData) {
		
		//the noise is arranged in 4x4x4 size, so we have to smooth that out
		//by normalizing over the surrounding values
		
		//we are subdividing height in 16 groups of 4
		//and smooth those groups to get a plateau like
		//look
		final double oneEigth = 0.125D;
		final int g = blockColumnCountPerXZDim; 
		final int gg = blockColumnCountPerYDim; 
		for (int plateau = 0; plateau < blockSize; plateau++) // 16*16 = one mini chunk
		{
			
			double self = noiseArray[((miniZ + 0) * g + (miniX + 0))
					* gg + (plateau + 0)];
			double top = noiseArray[((miniZ + 0) * g + (miniX + 1))
					* gg + (plateau + 0)];
			double right = noiseArray[((miniZ + 1) * g + (miniX + 0))
					* gg + (plateau + 0)];
			double topRight = noiseArray[((miniZ + 1) * g + (miniX + 1))
					* gg + (plateau + 0)];
			double front = (noiseArray[((miniZ + 0) * g + (miniX + 0))
					* gg + (plateau + 1)] - self)
					* oneEigth;
			double topFront = (noiseArray[((miniZ + 0) * g + (miniX + 1))
					* gg + (plateau + 1)] - top)
					* oneEigth;
			double rightFront = (noiseArray[((miniZ + 1) * g + (miniX + 0))
					* gg + (plateau + 1)] - right)
					* oneEigth;
			double topFrontRight = (noiseArray[((miniZ + 1) * g + (miniX + 1))
					* gg + (plateau + 1)] - topRight)
					* oneEigth;

			/*
			 * 4 height plates in 16 height plateaus  
			 */
			for (int subPlate = 0; subPlate < plateauHeight; subPlate++) // *
			
			{

				double selfValue = self;
				double topValue = top;
				double selfDist = (right - self) * quarterBlockScale;
				double topDist = (topRight - top) * quarterBlockScale;

				for (int plane4by4Z = 0; plane4by4Z < 4; plane4by4Z++) // 4
				{
					int index = plane4by4Z + miniZ * 4 << 10
							| 0 + miniX * 4 << 6 | plateau * plateauHeight
							+ subPlate;

					index -= HEIGHT_BLOCKS;

					double selfValueBefore = selfValue;
					double selfTopDist = (topValue - selfValue)
							* quarterBlockScale;
					selfValueBefore -= selfTopDist;

					for (int plane4By4X = 0; plane4By4X < 4; plane4By4X++) // 4*4
				
					{
						if (plateau * plateauHeight + subPlate == 0) {

							// always fill in bottom block
							infoArray[index += HEIGHT_BLOCKS] = 0;

						} else if ((selfValueBefore += selfTopDist) > 0.0) {

							// normalized density greater than 0

							infoArray[index += HEIGHT_BLOCKS] =
//									ElementKeyMap.HULL_HELPER[(requestData.getR().miniblock)%ElementKeyMap.HULL_HELPER.length];
									worldCreator
									.getSolid();

						} else if (plateau * plateauHeight + subPlate < waterLevel) {

							// TODO use water if there is water
							infoArray[index += HEIGHT_BLOCKS] =
//									ElementKeyMap.HULL_HELPER[(requestData.getR().miniblock)%ElementKeyMap.HULL_HELPER.length];
							worldCreator
									.getFiller();

						} else {
							// air

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

	protected void initOctaves(int xPos, int yPos, int zPos, int width,
	                           int height, int depth, RequestDataPlanet requestData) {

		double scale2D = 200 * requestData.getR().rScale;
		requestData.getR().noise2DMid16 = noiseGen6Oct16for2D.make2DOctaves(xPos,
				zPos, width, depth, scale2D, scale2D, requestData.getR().noise2DMid16);
		
		double xzScale = 622.23452345D * requestData.getR().rScale;
		double yScale = xzScale;

		double smoothNess = 85;
		double smoothNessHeigth = 150;

		requestData.getR().noiseSmall8 = noiseGen3Oct8.make3DOctaves(xPos, yPos, zPos,
				width, height, depth, 
				xzScale / smoothNessHeigth, 
				yScale / smoothNessHeigth, 
				xzScale / smoothNessHeigth,
				requestData.getR().noiseSmall8);

		requestData.getR().noise1Big16 = noiseGen1Oct16.make3DOctaves(
				xPos, yPos, zPos, 
				width, height, depth, 
				xzScale, yScale, xzScale, 
				requestData.getR().noise1Big16);

		requestData.getR().noise2Big16 = noiseGen2Oct16.make3DOctaves(xPos, yPos, zPos, 
				width, height, depth, 
				xzScale, yScale, xzScale, 
				requestData.getR().noise2Big16);
	}
	public SegmentDataInterface generateSegment(SegmentDataInterface data, Vector3i segPos, int x, int y, int z,
	                                   boolean mirror, RequestDataPlanet requestData) throws SegmentDataWriteException {

		// mar = 28;//0.28f;
		requestData.getR().mar = polyMargin;
		requestData.getR().mar = requestData.getR().mar * (1f / worldCreator.radius);
		requestData.getR().mar += 1f;

		if (y > 3 && regions != null) {
			createFromCorner(segPos, data, requestData);
//			data.getSegmentController().getSegmentBuffer()
//					.updateBB(data.getSegment());
			return data;
		}
	
		assert (x >= 0 && y >= 0 && z >= 0);
		if (cGenerator == null) {
			cGenerator = new AdditionalModifierIceColumns(
					worldCreator.getSolid(), worldCreator.getFiller(),
					worldCreator.getCaveBottom());
		}
		if (caveGenerator == null) {
			caveGenerator = new AdditionalModifierCave(worldCreator.getTop(),
					worldCreator.getSolid(), worldCreator.getFiller(),
					worldCreator.getCaveBottom(), seed + 10000000L * x
					+ 100000L * y + z);
		}
		if (columnGenerator == null) {
			columnGenerator = new AdditionalModifierSlimColumn(
					worldCreator.getTop(), worldCreator.getFiller(),
					worldCreator.getCaveBottom());
		}

		requestData.getR().rand.setSeed(x * 0x4f9939f508L + z * 0x1ef1565bd5L);
		requestData.getR().radius = worldCreator.radius;
		// System.err.println("GENERATING SEGMENT "+data.getSegmentPos()+"  "+this);
		// try{
		// throw new
		// RuntimeException(cachePos.toString()+"; "+data.getSegmentPos());
		// }catch(Exception e){
		// e.printStackTrace();
		// }
		if (!requestData.getR().created) {
//			System.err.println("::::: CREATING FOR "+data.getSegmentPos());
			// assert(!block);
			// block = true;
			Arrays.fill(requestData.getR().data, (short) 0);
			generateTerrain(x, z, requestData.getR().data, requestData); // 16 * 16 *
			// 16 * 8
			requestData.getR().rand.setSeed(x * 0x4f9939f508L + z * 0x1ef1565bd5L);

			if (hasColumns) {
				columnGenerator.generate(seed, x, y, z,
						requestData.getR().data, requestData.getR().rand);
			} else if (hasC) {
				cGenerator.generate(seed, x, y, z,
						requestData.getR().data, requestData.getR().rand);
			} else {
				caveGenerator.generate(seed, x, y, z,
						requestData.getR().data, requestData.getR().rand);
			}
			requestData.getR().cachePos.set(data.getSegmentPos());
			requestData.getR().created = true;
		} else {
			assert (requestData.getR().cachePos.x == data.getSegmentPos().x && requestData.getR().cachePos.z == data
					.getSegmentPos().z) : requestData.getR().cachePos;
		}
		requestData.getR().rand.setSeed(x * 0x4f9939f508L + z * 0x1ef1565bd5L);

		decorateWithBlockTypes(x, y, z, requestData.getR().data, data, requestData);

		for (int i = 0; i < worldCreator.getGen().length; i++) {
			for (int j = 0; j < 10; j++) {

				worldCreator.getGen()[i].generate(
						data,
						x
								* 16
								+ worldCreator.getGen()[i]
								.getRangeX(requestData.getR().rand),
						y
								* 16
								+ worldCreator.getGen()[i]
								.getRangeY(requestData.getR().rand),
						z
								* 16
								+ worldCreator.getGen()[i]
								.getRangeZ(requestData.getR().rand),
						requestData.getR().rand);
			}
		}
//		data.getSegmentController().getSegmentBuffer()
//				.updateBB(data.getSegment());
		return data;
	}

	public static void main(String[] sdf){
//		TerrainGenerator gt0 = new TerrainGenerator(123);
//		RequestDataPlanet pt0 = new RequestDataPlanet();
//		gt0.initOctaves(0, 0, 0, 9, 9, 9, pt0);
//		
//		for(int i = 0; i < 4; i++){
//			System.err.println(i+" ::: "+pt0.noise1Big16[i]);
//		}
//		
//		System.err.println("\n-----");
//		
//		TerrainGenerator gt1 = new TerrainGenerator(123);
//		RequestDataPlanet pt1 = new RequestDataPlanet();
//		pt1.rScale = 2d;
//		gt1.initOctaves(0, 0, 0, 4, 4, 4, pt1);
//		
//		for(int i = 0; i < 4; i++){
//			System.err.println(i+" ::: "+pt1.noise1Big16[i]);
//		}
		
		
		
		TerrainGenerator g0 = new TerrainGenerator(123);
		double[] a;
		RequestDataPlanet p0 = new RequestDataPlanet();
		p0.getR().radius = 100;
		p0.getR().noiseArray = g0.createNoiseArray(p0.getR().noiseArray, 
				0, 
				0, 
				0, 
				11,
				11, 
				11, 
				0, 0, p0);
		
//		for(int i = 0; i < 4; i++){
//			System.err.print(p0.noiseArray[i]+", ");
//		}
//		for(int i = 0; i < 11*11*11; i++){
//			System.err.println(i+" ::: "+p0.noise1Big16[i]);
//		}
		System.err.println("\n-----");
		
		final int s = 8;
		final int sD = s*2;
		
		RequestDataPlanet p1 = new RequestDataPlanet();
		TerrainGenerator g1 = new TerrainGenerator(123);
		
		p1.getR().radius = 100;
		p1.getR().rScale = 2d;
		p1.getR().noiseArray = g1.createNoiseArray(p1.getR().noiseArray, 
				0, 0, 0, 
				6, 6, 6, 
				0, 0, p1);
		
//		for(int i = 0; i < 4; i++){
//			System.err.print(p1.noiseArray[i]+", ");
//		}
//		for(int i = 0; i < 6*6*6; i++){
//			System.err.println(i+" ::: "+p1.noise1Big16[i]);
//		}
	}

	/**
	 * Generates the shape of the terrain for the chunk though its all stone
	 * though the water is frozen if the temperature is low enough
	 */
	public void generateTerrain(int xPos, int zPos, short infoArray[],
	                            RequestDataPlanet requestData) {

		// System.err.println("INITIALIZE NOISE: "+xPos *
		// miniBlockRange+", "+0+", "+ zPos * miniBlockRange);
		requestData.getR().radius = worldCreator.radius;
		int dx = (64 - xPos) * 16;
		int dz = (64 - zPos) * 16;
		// stoneNoise = noiseGen4.generateNoiseOctaves(stoneNoise, x * 16, z *
		// 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D);
		
		if(!isSegmentInDodeca(xPos, zPos, dx, dz, requestData)){
			return;
		}
		
		//create 16 (4 for x and 4 for y with a total of 16 height values) mini 
		//blocks for a complete segment column.
		requestData.getR().noiseArray = createNoiseArray(requestData.getR().noiseArray, 
						xPos * blockColumnSize, 
						0, 
						zPos * blockColumnSize, 
						blockColumnCountPerXZDim,
						blockColumnCountPerYDim, 
						blockColumnCountPerXZDim, 
						xPos, zPos, requestData);
		
		
		//one mini block is a 4x4 column from the bottom to the top 
		requestData.getR().miniblock = 0;
		for (int miniZ = 0; miniZ < blockColumnSize; miniZ++) {
			for (int miniX = 0; miniX < blockColumnSize; miniX++) {
				fill4x4BlockColumn(miniX, miniZ, infoArray, requestData.getR().noiseArray, requestData);
				requestData.getR().miniblock++;
			}
		}
		
//		if(xPos == 64 && zPos == 64){
//		for (int z = 0; z < 16; z++) {
//			for (int x = 0; x < 16; x++) {
//				int xzCoordIndex = (x * 16 + z) * HEIGHT_BLOCKS;
//				for (int y = 63; y >= 0; y--) {
//					int infoIndex = xzCoordIndex + y;
//					if(requestData.getR().data[infoIndex] > 0){
//						System.err.println(infoIndex+" SSDSS "+x+", "+y+", "+z);
//						break;
//					}
//				}
//			}
//		}
//		}else{
//			System.err.println("PPP "+xPos+", "+zPos);
//		}
	}
	/**
	 * Generates the shape of the terrain for the chunk though its all stone
	 * though the water is frozen if the temperature is low enough
	 */
	public void generateTerrainMicro(int xPos, int zPos, short infoArray[],
	                            RequestDataPlanet requestData, SegmentLodDrawer drw) {

		final int s = 5;
		final int sD = s*2;
		requestData.getR().radius = worldCreator.radius;
		Arrays.fill(requestData.getR().data, (short)0);
		requestData.getR().noiseArray = createNoiseArray(requestData.getR().noiseArray, 
				(64)*blockColumnSize, 0, (64)*blockColumnSize, 
				33, 17, 33, 
				0, 0, requestData);
		requestData.getR().miniblock = 0;
		final int g = 33;
		final int gg = 17;
		for (int miniZ = 0; miniZ < 32; miniZ++) {
			for (int miniX = 0; miniX < 32; miniX++) {
//				DR4x4BlockColumn(miniX, miniZ, requestData.getR().noiseArray, requestData.getR().data, requestData, miniX, miniZ, drw);
				for(int plateau = 0; plateau < 16; plateau++){
					int i = (miniZ  * g + miniX  ) * (gg)  + (plateau + 0);
					
					double top = requestData.getR().noiseArray[((miniZ + 0) * g + (miniX + 1))
					    					* gg + (plateau + 0)];
					
					double d = requestData.getR().noiseArray[i];
					
					double dDist = (top - d) * 0.25;
//					System.err.println(miniX+" , "+miniZ+" , "+plateau+" : "+i+" ;;; "+g+" : "+gg+"; .... "+requestData.getR().noiseArray.length+" -> "+d);
					if(d  > 0d){
						
						drw.drawSmall(-8+(miniZ)*4, -8+(plateau)*4, -8+(miniX)*4);
					}else{
//						System.err.println("DRAW "+d);
					}
	//				fill4x4BlockColumn(miniX, miniZ, infoArray, requestData.getR().noiseArray, requestData);
					requestData.getR().miniblock++;
				}
			}
		}
		
		
//		for (int z = 0; z < 16; z++) {
//			for (int x = 0; x < 16; x++) {
//				int xzCoordIndex = (x * 16 + z) * HEIGHT_BLOCKS;
//				for (int y = 63; y >= 0; y--) {
//					int infoIndex = xzCoordIndex + y;
//					if(requestData.getR().data[infoIndex] > 0){
////						System.err.println(infoIndex+" CCDCC "+x+", "+y+", "+z);
//						drw.drawBlock(x-8, y-8, z-8);
//						break;
//					}
//				}
//			}
//		}
//		System.err.println("--------");
	}
	
	protected double[] createNoiseArray(double noiseField[], int xPos,
	                                    int yPos, int zPos, int width, int height, int depth, int segPosX,
	                                    int setPosZ, RequestDataPlanet requestData) {
		if (noiseField == null) {
			noiseField = new double[width * height * depth];
		}
		initOctaves(xPos, yPos, zPos, width, height, depth, requestData);
		updateDistances(
				(width  ), 
				(height ), 
				(depth  ), 
				noiseField, segPosX, setPosZ, requestData);

		return noiseField;
	}
//	public static void main(String ar[]){
//		double l = 10;
//		double inv = 1d/512d;
//		long t = System.nanoTime();
//		for(int i = 0; i < (16*16*128)*10000; i++){
//			l += l / 512d;
//			l += l / 512d;
//			l += l / 512d;
//		}
//		long t1 = System.nanoTime() - t;
//		l = 10;
//		t = System.nanoTime();
//		for(int i = 0; i < (16*16*128)*10000; i++){
//			l += l * inv;
//			l += l * inv;
//			l += l * inv;
//		}
//		long t2 = System.nanoTime() - t;
//		System.err.println("TIME: "+t1/1000000d+" .... "+t2/1000000d);
//		
//	}
	private void updateDistances(int width,
	                             int height, int depth, double[] noiseField, int segPosX, int segPosZ,
	                             RequestDataPlanet requestData) {
		int resultIndex = 0;
		int noiseIndex = 0;

		float innderRadiusInv = 1.0f / requestData.getR().radius * 0.618f; // golden ratio
		float minB = 0.0f;
		float maxB = 3.0f;

		float minDistInv = 1 / (minB + 2f);
		
		int xx = (((segPosX) - 64) * 16 + ((0) * 4));
		int zz = (((segPosZ) - 64) * 16 + ((0) * 4));

		float dist = FastMath.carmackSqrt(xx * xx + zz * zz);
		
		float borderFactor = (1.0f - (Math.min(0.999f, dist
				* innderRadiusInv)));
		float normMax = 0.0F;
		float normMin = 0.0F;
		normMax = borderFactor * requestData.getR().normMax;
		normMin = requestData.getR().normMin;
		
		
		int bHeight = (int) ((height-1)* requestData.getR().rScale)+1;
		
		for (int xIter = 0; xIter < width; xIter++) {
			for (int zIter = 0; zIter < depth; zIter++) {
				


				double midNoise162D = getMidNoise(noiseIndex, requestData);
				noiseIndex++;

				for (double y = 0; y < height; y++) {
					noiseField[resultIndex] = get3dNoiseResult(
							(y*requestData.getR().rScale), 
							normMin, normMax, bHeight, 
							midNoise162D, resultIndex,
							noiseField, requestData);
					resultIndex++;
				}
			}
		}
	}

	private double get3dNoiseResult(double y, double normMin, double normMax,
	                                int height, double midNoise162D, int resultIndex,
	                                double[] noiseField, RequestDataPlanet requestData) {
		double nMin = normMin;
		double nMax = normMax;

		
		nMin += midNoise162D * flatness;

		// nMin = (nMin * height) / 16D;
		// System.err.println("MIN NOW: "+nMin);
		// double halfHeightPlusMin = height / 2D + nMin * 4D;
		// double halfHeightPlusMin = height-2.5d + nMin*defaultMax;// / 2D;

		double halfHeightPlusMin = height * 0.5d + nMin * halfHeightFactor;// /
		// 2D;

		double resultNoise = 0.0D;

		double normHeightVal = ((y - halfHeightPlusMin) * 12d * (heighNormValue))
				/ (heighNormValue) / nMax;

		if (normHeightVal < 0.0D) {
			normHeightVal *= 2d;
		}

		
		
		double noiseA = requestData.getR().noise1Big16[resultIndex] / 512d;
		double noiseB = requestData.getR().noise2Big16[resultIndex] / 512d;
		double noiseMix = (requestData.getR().noiseSmall8[resultIndex] / 10d + 1.0d) * 0.5d;
		
		
		//
		if (noiseMix < 0.0D) {
			resultNoise = noiseA;
		} else if (noiseMix > 1.0D) {
			resultNoise = noiseB;
		} else {
			resultNoise = noiseA + ((noiseB - noiseA) * noiseMix);
		}

		resultNoise -= normHeightVal;
		
		double maxMargin = 4; // default 4;

		
		
		
		if (y > height - maxMargin) {
			double hNorm = (float) (y - (height - maxMargin)) / 3F;
			resultNoise = resultNoise * (1.0D - hNorm) + -10D * hNorm;
		}
//		System.err.println("NMIN: "+y+" -- "+resultIndex+": "+height +":: "+noiseA+", "+noiseB+", "+noiseMix+", nhv: "+normHeightVal+"  -> "+resultNoise);
		return resultNoise;
	}

	private double getMidNoise(int noiseIndex, RequestDataPlanet requestData) {
		double n = requestData.getR().noise2DMid16[noiseIndex] * 0.000125;

		if (n < 0.0d) {
			n = -n * 0.2894523423d;
		}

		n = (n * 3d) - 2d; // normalize height

		if (n < 0.0d) {
			n *= 0.5d;
			n = Math.max(-1d, n);
			n = (n / 1.388742232222D) * 0.5d;
		} else {
			n = Math.min(n, 1d);
			n *= 0.125d;
		}
		return n;
	}

	protected void initNoises(Random random) {
		noiseGen1Oct16 = new OctavesGenerator(random, 16);
		noiseGen2Oct16 = new OctavesGenerator(random, 16);
		noiseGen3Oct8 = new OctavesGenerator(random, 8);
		noiseGen4Oct4 = new OctavesGenerator(random, 4);
		noiseGen6Oct16for2D = new OctavesGenerator(random, 16);
	}

	/**
	 * @param flatness the flatness to set
	 */
	public void setFlatness(double flatness) {
		this.flatness = flatness;
	}

	/**
	 * @param regions the regions to set
	 */
	public void setRegions(Region[] regions) {
		this.regions = regions;
	}

	public void setWorldCreator(
			WorldCreatorPlanetFactory worldCreatorPlanetFactory) {
		this.worldCreator = worldCreatorPlanetFactory;

	}

}
