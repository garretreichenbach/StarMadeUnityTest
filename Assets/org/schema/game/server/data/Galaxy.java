package org.schema.game.server.data;

import api.listener.events.world.generation.*;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.namegen.NameGenerator;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.PositionableSubSpriteCollection;
import org.schema.game.client.view.gamemap.PositionableSubSpriteCollectionReal;
import org.schema.game.client.view.gamemap.StarPosition;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.data.simulation.npc.NPCFactionManager;
import org.schema.game.server.data.simulation.npc.geo.StarSystemResourceRequestContainer;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Math.*;
import static org.schema.game.common.data.element.ElementKeyMap.*;
import static org.schema.game.common.data.world.StellarSystem.MAX_RESOURCES_PER_SYSTEM;
import static org.schema.game.common.data.world.StellarSystem.RARE_RESOURCE_AVAILABILITY_CHANCE;

public class Galaxy {
	public static final int TYPE_SUN = 0;
	public static final int TYPE_GIANT = 1;
	public static final int TYPE_BLACK_HOLE = 2;
	public static final int TYPE_DOUBLE_STAR = 3;
	public static int size = 128; //Todo: This really should be a config option tbh
	public static int sizeSquared = size * size;
	public static int halfSize = size / 2;
	public static boolean USE_GALAXY = true;
	public final Vector3i galaxyPos;
	private final IntArrayList starIndices = new IntArrayList();
	private final Vector2f ttmp = new Vector2f();
	private final Vector3f normalStd = new Vector3f(0, 0, 1);
	private final List<Vector3i> blackHoles = new ObjectArrayList<Vector3i>();
	private final CComp sorter = new CComp();
	private final Vector3i tmp = new Vector3i();
	public Long2ObjectOpenHashMap<String> nameCache = new Long2ObjectOpenHashMap<String>();
	Random random;
	Random randomNPC;
	final Random randomRes;
	final Object resourceAssignmentLock = new Object();
	Vector3i tmpCPOS = new Vector3i();
	Vector3i tmpout = new Vector3i();
	Vector3i tmpoutFrom = new Vector3i();
	Vector3i tmpoutTo = new Vector3i();
	Vector3f tmpAxis = new Vector3f();
	Vector3f tmpRight = new Vector3f();
	Vector3f tmpFwd = new Vector3f();
	private long seed;
	private int displayList;
	private final byte[][][] galaxyMap = new byte[size][size][size];
	private final Vector4f[] colors = new Vector4f[2048];
	private final float[] colorIndices = new float[2048];
	private final int[] types = new int[2048];
	private final float[] intensity = new float[2048];
	private int numberOfStars;
	private final byte threshHold = 10;
	private PositionableSubSpriteCollectionReal spriteCollection;
	//	private byte[][][] galaxyMap = new byte[size][size][size];
	private final NameGenerator nameGenerator;
	private NPCFactionManager npcFactionManager;
	private final FastNoise[] resourceNoises = new FastNoise[VoidSystem.RAW_RESOURCES];

	public Galaxy(long seed, Vector3i galaxyPos) {
		this.seed = seed;
		random = new Random(seed);
		randomNPC = new Random(seed);
		randomRes = new Random(seed);
		this.galaxyPos = galaxyPos;
		try {
			nameGenerator = new NameGenerator("./data/config/systemNames.syl", seed);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		Random r = new Random(seed);
		for(int i = 0; i < resourceNoises.length; i++) {
			resourceNoises[i] = new FastNoise(r.nextInt());
			resourceNoises[i].SetNoiseType(FastNoise.NoiseType.Perlin);
			resourceNoises[i].SetFrequency(0.22f);
		}
	}

	public static int localCoordinate(int in) {
		return ByteUtil.modU128(in);
	}

	public static Vector3i getContainingGalaxyFromSystemPos(Vector3i systemPos, Vector3i out) {
		out.x = (systemPos.x + halfSize) >= 0 ? (systemPos.x + halfSize) / size : ((systemPos.x + halfSize) / size - 1);
		out.y = (systemPos.y + halfSize) >= 0 ? (systemPos.y + halfSize) / size : ((systemPos.y + halfSize) / size - 1);
		out.z = (systemPos.z + halfSize) >= 0 ? (systemPos.z + halfSize) / size : ((systemPos.z + halfSize) / size - 1);
		//		out.x = (systemPos.x-halfSize) >= 0 ? (systemPos.x-halfSize) / size : ((systemPos.x-halfSize) / size - 1);
		//		out.y = (systemPos.y-halfSize) >= 0 ? (systemPos.y-halfSize) / size : ((systemPos.y-halfSize) / size - 1);
		//		out.z = (systemPos.z-halfSize) >= 0 ? (systemPos.z-halfSize) / size : ((systemPos.z-halfSize) / size - 1);
		return out;
	}

	public static void main(String[] args) {
		for(int i = -200; i < 200; i++) {
			System.err.println("TT " + i + " -> " + localCoordinate(i));
		}
		//		Galaxy g = new Galaxy(1000);
		//		g.generate(100, 5, 5, 16, 2.0);
	}

	public static Vector2f randomDirection2D(Random random, Vector2f tmp) {
		float azimuth = random.nextFloat() * 2 * FastMath.PI;
		tmp.set(FastMath.cos(azimuth), FastMath.sin(azimuth));
		return tmp;
	}

	public static Vector3f randomDirection3D(Random random, Vector3f out, Vector2f tmp) {
		float z = (2 * random.nextFloat()) - 1; // z is in the range [-1,1]
		Vector2f planar = randomDirection2D(random, tmp);
		planar.scale(FastMath.carmackSqrt(1 - z * z));
		out.set(planar.x, planar.y, z);
		return out;
	}

	public static double normalize(double value, double vmin, double vmax, double nmin, double nmax) {
		return ((value - vmin) / (vmax - vmin)) * (nmax - nmin) + nmin;
	}

	public static double distance2D(double x1, double y1, double x2, double y2) {
		double x = x1 - x2;
		double y = y1 - y2;
		return Math.sqrt((x * x) + (y * y));
	}

	public static double rnd(double rangeMin, Random r) {
		return rangeMin * r.nextDouble();
	}

	public static double rnd(double rangeMin, double rangeMax, Random r) {
		return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
	}

	public static Vector3i getLocalCoordinatesFromSystem(Vector3i absSystem, Vector3i out) {
		int x = localCoordinate(absSystem.x + halfSize);
		int y = localCoordinate(absSystem.y + halfSize);
		int z = localCoordinate(absSystem.z + halfSize);
		out.set(x, y, z);
		return out;
	}

	public static Vector3i getRelPosInGalaxyFromAbsSystem(Vector3i pos, Vector3i out) {
		out.set(pos);
		out.add(halfSize, halfSize, halfSize);
		out.x = localCoordinate(out.x);
		out.y = localCoordinate(out.y);
		out.z = localCoordinate(out.z);
		return out;
	}

	public static boolean isPlanetOrbit(int orbitType) {
		return orbitType > Integer.MAX_VALUE / 2;
	}

	public void initializeGalaxyOnServer(GameServerState state) {
		generateBlackHoleNetworkOnServer(state);
		npcFactionManager = new NPCFactionManager(state, this);
	}

	public void generate() {
		random.setSeed(seed);
		randomNPC.setSeed(seed);
		generate(165000 + random.nextInt(10000) - 5000, 4 + random.nextInt(2) - (random.nextInt(10) == 0 ? 1 : 0), 12 + random.nextInt(5) - 2, halfSize, 0.15 + (random.nextFloat() * 0.05f - 0.025f));
	}

	public void generate(double stars, double arms, double spread, double range, double rot) {
		long t = System.currentTimeMillis();
		///INSERTED CODE
		GalaxyGenerationEvent gg = new GalaxyGenerationEvent(galaxyPos, seed, stars, arms, spread, range, rot);
		StarLoader.fireEvent(GalaxyGenerationEvent.class, gg, true);
		stars = gg.getStarCount();
		arms = gg.getArmCount();
		spread = gg.getSpread();
		range = gg.getRange();
		rot = gg.getRotation();
		seed = gg.getSeed();
		///
		for(int z = 0; z < size; z++) {
			for(int y = 0; y < size; y++) {
				Arrays.fill(galaxyMap[z][y], (byte) 0);
				//				for (int x = 0; x < size; x++) {
				//					galaxyMap[z][y][x] = 0;
				//				}
			}
		}
		random.setSeed(seed);
		randomNPC.setSeed(seed);
		for(int i = 0; i < types.length; i++) {
			float expected = 0.85f;
			float deviation = 0.2f;
			double d = expected + (random.nextGaussian() * deviation);
			if(d > 0.50) {
				types[i] = TYPE_SUN;
			} else {
				if(random.nextFloat() > 0.5) {
					types[i] = TYPE_GIANT;
				} else if(random.nextBoolean()) {
					types[i] = TYPE_BLACK_HOLE;
				} else {
					types[i] = TYPE_DOUBLE_STAR;
				}
			}
		}
		for(int i = 0; i < colors.length; i++) {
			float expected = 0.85f;
			float deviation = 0.2f;
			double d = expected + (random.nextGaussian() * deviation);
			if(d > 0.65) {
				colors[i] = new Vector4f(1, 1, 1, (float) Math.min(1, expected + (random.nextGaussian() * deviation)));
				colorIndices[i] = 1.0f / 16.0f; //white
				intensity[i] = 1.0f;
			} else {
				if(random.nextFloat() > 0.5) {
					float yellowness = (random.nextFloat() * 0.9f + 0.1f);
					colors[i] = new Vector4f(1, 1, 0.9f * yellowness, (float) Math.min(1, expected + (random.nextGaussian() * deviation)));
					colorIndices[i] = 5.0f / 16.0f; //yellow
					intensity[i] = 0.8f;
				} else if(random.nextBoolean()) {
					float blueness = (random.nextFloat() * 0.9f + 0.1f);
					colors[i] = new Vector4f(0.9f * blueness, 0.9f * blueness, 1, (float) Math.min(1, expected + (random.nextGaussian() * deviation)));
					colorIndices[i] = 8.0f / 16.0f; //blue
					intensity[i] = 2.0f;
				} else {
					float redness = (random.nextFloat() * 0.9f + 0.1f);
					colors[i] = new Vector4f(1, 0.7f * redness, 0.7f * redness, (float) Math.min(1, expected + (random.nextGaussian() * deviation)));
					colorIndices[i] = 15.0f / 16.0f; //red
					intensity[i] = 0.5f;
				}
			}
		}
		//		for(int i = 0; i < 2048; i++){
		//			if(types[i] == TYPE_GIANT){
		//				System.err.println(i+" GEN: "+types[i]+": "+colors[i]);
		//			}
		//		}
		//
		random.setSeed(seed);
		starIndices.clear();
		numberOfStars = 0;
		for(int i = 0; i < stars; i++) {
			// angle
			double angle = (int) (Math.floor(i * 1.0 / (stars / arms))) * (360.0 / arms);
			double multi;
			// center / arm relation
			if(random.nextDouble() > 0.5) {
				multi = rnd(0.1, 1, random);
			} else {
				multi = rnd(1, 2, random);
			}
			// distance and turbulence
			double dist = rnd(0, range, random) * rnd(1, rnd(rnd(multi, random), random), random);
			double turb = rnd(0, rnd(spread, random), random);
			if(random.nextDouble() > 0.5) {
				turb = -turb;
			}
			// star position x/z
			double x = dist * Math.cos(angle + (dist * rot)) + rnd(rnd(rnd(-spread, random), random), rnd(rnd(spread, random), random), random);
			double z = dist * Math.sin(angle + (dist * rot)) + rnd(rnd(rnd(-spread, random), random), rnd(rnd(spread, random), random), random);
			// star position y
			double bulge = Math.min(90, normalize(distance2D(x, z, 0, 0), 0, range / 2.0, 0, 180) / 2.0);
			double y = (Math.cos(bulge) * rnd(rnd(-spread, random), rnd(spread, random), random) / 2.0) + (turb / 10.0);
			int cX = (int) max(0, Math.min(size - 1, (x) + halfSize));
			int cY = (int) max(0, Math.min(size - 1, (y) + halfSize));
			int cZ = (int) max(0, Math.min(size - 1, (z) + halfSize));
			//			assert(!(cX == halfSize && cY == halfSize && cZ == halfSize) ||
			byte before = galaxyMap[cZ][cY][cX];
			galaxyMap[cZ][cY][cX] = (byte) (Math.min(Byte.MAX_VALUE, before + 1));

			///INSERTED CODE [Ithirahad]
			Vector3i loc = new Vector3i(cX, cY, cZ);
			StarCreationAttemptEvent scatt = new StarCreationAttemptEvent(random, loc, this, galaxyMap[cZ][cY][cX], getSunColor(loc), getIndex(cX, cY, cZ), getSunIntensityFromSys(loc), getSystemType(loc));
			StarLoader.fireEvent(StarCreationAttemptEvent.class, scatt, true);
			galaxyMap[cZ][cY][cX] = scatt.getStarWeight();
			///

			float transitionInnerBound = ServerConfig.GALAXY_DENSITY_TRANSITION_INNER_BOUND.getFloat();
			float transitionOuterBound = ServerConfig.GALAXY_DENSITY_TRANSITION_OUTER_BOUND.getFloat();
			float densityRateInner = ServerConfig.GALAXY_DENSITY_RATE_INNER.getFloat();
			float densityRateOuter = ServerConfig.GALAXY_DENSITY_RATE_OUTER.getFloat();

			float innerBound = transitionInnerBound * halfSize; //inside radius of transition zone, as a fraction of total galaxy radius (distance from centre to furthest star)
			float outerBound = transitionOuterBound * halfSize; //outside radius of transition zone, as fraction of total galaxy radius
			Vector3i g = galaxyPos;
			Vector3i galaxyOrigin = new Vector3i(g.x * size, g.y * size, g.z * size); //galaxy position in systems
			galaxyOrigin.add(64, 64, 64); //transform to corner-origin coordinates
			float distanceFromCenter = Vector3i.getDisatance(galaxyOrigin, loc);
			float preserveStarChance = 1.0F; //chance to keep a star, otherwise its weight value gets set below the creation threshold


			if(galaxyMap[cZ][cY][cX] >= threshHold + 1) {
				if(distanceFromCenter < innerBound) {
					preserveStarChance = densityRateInner; //we're inside the inner zone bounds; use the inner zone depopulation rate to avoid too much density at the core
				} else if(distanceFromCenter > outerBound) {
					preserveStarChance = densityRateOuter; //we're outside the outer zone bounds; use the outer zone depopulation rate so that the arms aren't too sparse
				} else {
					float transitionBandWidth = outerBound - innerBound;
					float distanceThroughBand = (distanceFromCenter - innerBound) / transitionBandWidth;
					if(densityRateInner < densityRateOuter) {
						preserveStarChance = densityRateInner + distanceThroughBand * (densityRateOuter - densityRateInner);
					} else {
						preserveStarChance = densityRateOuter + 1.0F - distanceThroughBand * (densityRateInner - densityRateOuter);
					} //interpolate between values
				}
				boolean removeStar = random.nextFloat() > preserveStarChance;
				if(removeStar && !loc.equals(64, 64, 64)) {
					galaxyMap[cZ][cY][cX] = 1; //below threshhold
				} else {
					galaxyMap[cZ][cY][cX] = (byte) (threshHold + 1); //above threshhold
				}

				if(galaxyMap[cZ][cY][cX] == threshHold + 1) {
					numberOfStars++;
					int index = getIndex(cX, cY, cZ);
					assert (index >= 0);
					int systemType = getSystemType(cX, cY, cZ);
					if(systemType == TYPE_BLACK_HOLE) {
						blackHoles.add(new Vector3i(cX - halfSize, cY - halfSize, cZ - halfSize));
					} else {
						starIndices.add(index);
					}
					//				Vector3i vector3i = new Vector3i();
					//				getPosFromIndex(index, vector3i);
					//				assert(vector3i.equals(cX, cY, cZ));
				}
				//			if(cX == halfSize && cY == halfSize && cZ == halfSize){
				//				System.err.println("CENTER SYSTEM "+galaxyMap[cZ][cY][cX]);
				//			}
				//			System.err.println("STAR: "+x+", "+y+", "+z);
			}
		}
		if(blackHoles.size() > 0) {
			((ObjectArrayList<Vector3i>) blackHoles).trim();
		}
		if(starIndices.size() > 0) {
			starIndices.trim();
		}
		///INSERTED CODE
		GalaxyFinishedGeneratingEvent gfg = new GalaxyFinishedGeneratingEvent(this);
		StarLoader.fireEvent(GalaxyFinishedGeneratingEvent.class, gfg, true);
		///
		long took = System.currentTimeMillis() - t;
		System.err.println("[UNIVERSE] creation time: " + took + " ms");
	}

	public String getBlockHoleUID(Vector3i relSystemPos, Vector3i outputSec) {
		Vector3i rel = new Vector3i(relSystemPos);
		rel.add(halfSize, halfSize, halfSize);
		Vector3i sunPositionOffset = getSunPositionOffset(rel, tmpCPOS);
		outputSec.set(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
		outputSec.add(sunPositionOffset);
		outputSec.add(relSystemPos.x * VoidSystem.SYSTEM_SIZE, relSystemPos.y * VoidSystem.SYSTEM_SIZE, relSystemPos.z * VoidSystem.SYSTEM_SIZE);
		outputSec.add(galaxyPos.x * size * VoidSystem.SYSTEM_SIZE, galaxyPos.y * size * VoidSystem.SYSTEM_SIZE, galaxyPos.z * size * VoidSystem.SYSTEM_SIZE);
		return "BH_" + outputSec.x + "_" + outputSec.y + "_" + outputSec.z + "_OO_" + sunPositionOffset.x + "_" + sunPositionOffset.y + "_" + sunPositionOffset.z;
	}

	public void generateBlackHoleNetworkOnServer(GameServerState state) {
		if(!USE_GALAXY) {
			return;
		}
		if(blackHoles.size() > 1) {
			Vector3i first = blackHoles.get(0);
			String blockHoleUID = getBlockHoleUID(first, tmpout);
			FTLConnection ftl = state.getDatabaseIndex().getTableManager().getFTLTable().getFtl(tmpout, blockHoleUID);
			if(ftl == null) {
				System.err.println("[SERVER][GALAXY] Generating Black Hole FTL");
				Collections.sort(blackHoles, (o1, o2) -> Float.compare(o1.lengthSquared(), o2.lengthSquared()));
				//				for(int i = 0; i < blackHoles.size(); i++){
				//					blackHoles.get(i).add(halfSize, halfSize, halfSize);
				//				}
				int gen = 0;
				int size = blackHoles.size();
				if(blackHoles.size() > 0) {
					try {
						while(blackHoles.size() > 0) {
							Vector3i from = blackHoles.remove(0);
							Vector3i to = null;
							if(blackHoles.size() > 0) {
								for(int i = 0; i < blackHoles.size(); i++) {
									if(to == null) {
										to = blackHoles.get(i);
									} else if(Vector3fTools.lengthSquared(from, blackHoles.get(i)) < Vector3fTools.lengthSquared(from, to)) {
										to = blackHoles.get(i);
									} else {
										//								break;
									}
								}
							} else {
								to = first;
							}
							assert (!from.equals(to));
							String blockHoleUIDFrom = getBlockHoleUID(from, tmpoutFrom);
							String blockHoleUIDTo = getBlockHoleUID(to, tmpoutTo);
							assert (getSystemType(getLocalCoordinatesFromSystem(VoidSystem.getContainingSystem(tmpoutFrom, new Vector3i()), new Vector3i())) == TYPE_BLACK_HOLE);
							assert (getSystemType(getLocalCoordinatesFromSystem(VoidSystem.getContainingSystem(tmpoutTo, new Vector3i()), new Vector3i())) == TYPE_BLACK_HOLE);
							//							assert(from.x >= galaxyPos.x*Galaxy.size-halfSize && from.y >= galaxyPos.y*Galaxy.size-halfSize && from.z >= galaxyPos.z*Galaxy.size-halfSize):from+"; "+galaxyPos;
							//							assert(from.x < galaxyPos.x*Galaxy.size+Galaxy.size-halfSize && from.y < galaxyPos.y*Galaxy.size+Galaxy.size-halfSize && from.z < galaxyPos.z*Galaxy.size+Galaxy.size-halfSize):from+"; "+galaxyPos;
							//							assert(tmpoutFrom.x >= galaxyPos.x*Galaxy.size*16-halfSize*16 && tmpoutFrom.y >= galaxyPos.y*Galaxy.size*16-halfSize*16 && tmpoutFrom.z >= galaxyPos.z*Galaxy.size*16-halfSize*16):from+"; "+galaxyPos+"; "+tmpoutFrom;
							//							assert(tmpoutFrom.x < galaxyPos.x*Galaxy.size*16+Galaxy.size*16-halfSize*16 && tmpoutFrom.y < galaxyPos.y*Galaxy.size*16+Galaxy.size*16-halfSize*16 && tmpoutFrom.z < galaxyPos.z*Galaxy.size*16+Galaxy.size*16-halfSize*16):from+"; "+galaxyPos+"; "+tmpoutFrom;
							boolean insertFTLEntry = state.getDatabaseIndex().getTableManager().getFTLTable().insertFTLEntry(blockHoleUIDFrom, tmpoutFrom, new Vector3i(), blockHoleUIDTo, tmpoutTo, new Vector3i(), FTLConnection.TYPE_WORM_HOLE, 0);
							if(insertFTLEntry) {
								gen++;
							} else {
								assert (false);
							}
							state.getUniverse().ftlDirty.enqueue(new Vector3i(tmpoutFrom));
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
					assert (gen == size) : gen + "; " + size;
				}
			}
		}
	}

	public int getIndex(int x, int y, int z) {
		return (z * sizeSquared) + (y * size) + (x);
	}

	public void getNormalizedPosFromIndex(int index, Vector3f out) {
		int z = index / sizeSquared;
		index -= z * sizeSquared;
		int y = index / size;
		index -= y * size;
		int x = index;
		out.set(x - halfSize, y - halfSize, z - halfSize);
	}

	public void getPosFromIndex(int index, Vector3f out) {
		int z = index / sizeSquared;
		index -= z * sizeSquared;
		int y = index / size;
		index -= y * size;
		int x = index;
		out.set(x, y, z);
	}

	public void getNormalizedPosFromIndex(int index, Vector3i out) {
		int z = index / sizeSquared;
		index -= z * sizeSquared;
		int y = index / size;
		index -= y * size;
		int x = index;
		out.set(x - halfSize, y - halfSize, z - halfSize);
	}

	public void getPosFromIndex(int index, Vector3i out) {
		int z = index / sizeSquared;
		index -= z * sizeSquared;
		int y = index / size;
		index -= y * size;
		int x = index;
		out.set(x, y, z);
	}

	public byte getHeatFromIndex(int index) {
		int z = index / sizeSquared;
		index -= z * sizeSquared;
		int y = index / size;
		index -= y * size;
		int x = index;
		return galaxyMap[z][y][x];
	}

	public void updateLocal(long time) {
		npcFactionManager.updateLocal(time);
	}

	public void clean() {
		if(displayList != 0) {
			GL11.glDeleteLists(displayList, 1);
		}
	}

	public void onInit() {
		spriteCollection = getPositionSpritesReal();
		//		clean();
		//		displayList = GL11.glGenLists(1);
		//		GL11.glNewList(displayList, GL11.GL_COMPILE);
		//
		//		GL11.glBegin(GL11.GL_POINTS);
		//
		//		for(int z = 0; z < size; z++){
		//			for(int y = 0; y < size; y++){
		//				for(int x = 0; x < size; x++){
		//
		//					byte val = galaxyMap[z][y][x];
		//					if(val > threshHold){
		//						GL11.glColor4f(1,1,1,1);
		////						GL11.glVertex3d(x+(random.nextDouble()-0.5*2)*0.3, y+(random.nextDouble()-0.5*2)*0.3, z+(random.nextDouble()-0.5*2)*0.3);
		////						GL11.glVertex3d(x*GameMapDrawer.size,y*GameMapDrawer.size,z*GameMapDrawer.size);
		//						GL11.glVertex3d((x-halfSize)*GameMapDrawer.size,(y-halfSize)*GameMapDrawer.size,(z-halfSize)*GameMapDrawer.size);
		//					}
		//				}
		//			}
		//		}
		//
		//
		//
		//		GL11.glEnd();
		//
		//		GL11.glEndList();
	}

	public int getRandomPlusMinus(Random random) {
		if(random.nextInt(15) > 0) {
			return 0;
		}
		if(random.nextInt(15) > 0) {
			//lower chance for 2 offset
			return random.nextBoolean() ? 2 : -2;
		} else {
			return random.nextBoolean() ? 1 : -1;
		}
	}

	/**
	 * hoe many orbits are in this system, and what type of orbit
	 *
	 * @param system
	 * @param out
	 *
	 * @return
	 */
	public int[] getSystemOrbits(Vector3i system, int[] out) {
		random.setSeed(getSystemSeed(system));
		for(int i = 0; i < 8; i++) {
			out[i] = random.nextFloat() > 0.45f ? random.nextInt(Integer.MAX_VALUE - 1) + 1 : 0;
		}
		return out;
	}

	public long getSystemSeed(Vector3i relSys) {
		long seed2 = relSys.hashCode() * seed;
		return seed2;
	}

	public long getSystemSeed(int x, int y, int z) {
		int result = (x ^ (x >>> 16));
		result = 15 * result + (y ^ (y >>> 16));
		result = 15 * result + (z ^ (z >>> 16));
		long seed2 = result * seed;
		return seed2;
	}

	public Vector3f getSystemAxis(Vector3i system, Vector3f out) {
		random.setSeed(getSystemSeed(system));
		randomDirection3D(random, out, ttmp);
		out.normalize();
		return out;
	}

	public Vector4f getSunColor(int x, int y, int z) {
		return colors[Math.abs(x * 123 + y * x * z * (x + 132) + y + z) % colors.length];
	}

	public float getSunColorIndex(int x, int y, int z) {
		return colorIndices[Math.abs(x * 123 + y * x * z * (x + 132) + y + z) % colorIndices.length];
	}

	public Vector4f getSunColor(Vector3i system) {
		return getSunColor(system.x, system.y, system.z);
	}

	public int getSystemType(int x, int y, int z) {
		if((FastMath.abs(x - 64) < 2 && FastMath.abs(y - 64) < 2 && FastMath.abs(z - 64) < 2) || (x == 130000000 && y == 130000000 && z == 130000000)) {
			return TYPE_SUN;
		}
		return types[Math.abs(x * 123 + y * x * z * (x + 132) + y + z) % types.length];
	}

	public int getSystemType(Vector3i system) {
		return getSystemType(system.x, system.y, system.z);
	}

	public float getSystemSunIntensity(int x, int y, int z) {
		return intensity[Math.abs(x * 123 + y * x * z * (x + 132) + y + z) % types.length];
	}

	public float getSystemSunIntensity(Vector3i system) {
		return getSystemSunIntensity(system.x, system.y, system.z);
	}

	public Vector3i getSunPositionOffset(Vector3i system, Vector3i out) {
		if(!USE_GALAXY) {
			//middle for old system
			out.set(0, 0, 0);
			return out;
		}
		random.setSeed(getSystemSeed(system));
		out.set(getRandomPlusMinus(random), getRandomPlusMinus(random), getRandomPlusMinus(random));
		return out;
	}

	public void getAxisMatrix(Vector3i system, Matrix3f out) {
		getSystemAxis(system, tmpAxis);
		tmpRight.cross(tmpAxis, normalStd);
		tmpRight.normalize();
		tmpFwd.cross(tmpRight, tmpAxis);
		tmpFwd.normalize();
		GlUtil.setUpVector(tmpAxis, out);
		GlUtil.setForwardVector(tmpFwd, out);
		GlUtil.setRightVector(tmpRight, out);
		assert (out.determinant() != 0) : out;
	}

	public void getPositions(ObjectArrayList<Vector3f> poses, FloatArrayList colors) {
		for(int z = 0; z < size; z++) {
			for(int y = 0; y < size; y++) {
				for(int x = 0; x < size; x++) {
					byte val = galaxyMap[z][y][x];
					if(val > threshHold) {
						Vector3f v = new Vector3f(galaxyPos.x * size + x, galaxyPos.y * size + y, galaxyPos.z * size + z);
						int systemType = getSystemType(x, y, z);
						//						if(systemType == TYPE_DOUBLE_STAR || systemType == TYPE_GIANT || systemType == TYPE_SUN){
						poses.add(v);
						colors.add(getSunColorIndex(x, y, z));
						//						}
					}
				}
			}
		}
	}

	public PositionableSubSpriteCollectionReal getPositionSpritesReal() {
		PositionableSubSpriteCollectionReal g = new PositionableSubSpriteCollectionReal();
		float[] vals = new float[numberOfStars * PositionableSubSpriteCollection.DATASIZE];
		int i = 0;
		Vector3i offset = new Vector3i();
		Vector3i pos = new Vector3i();
		Vector3i secPos = new Vector3i();
		for(int z = 0; z < size; z++) {
			for(int y = 0; y < size; y++) {
				for(int x = 0; x < size; x++) {
					byte val = galaxyMap[z][y][x];
					if(val > threshHold) {
						pos.set(x, y, z);
						StarPosition p = new StarPosition();
						int systemType = getSystemType(x, y, z);
						getSunPositionOffset(pos, offset);
						p.relPosInGalaxy.set(x, y, z);
						//do not use getter
						p.pos.set(galaxyPos.x * size + (x + (offset.x * 0.0625f)) - halfSize, galaxyPos.y * size + (y + (offset.y * 0.0625f)) - halfSize, galaxyPos.z * size + (z + (offset.z * 0.0625f)) - halfSize);
						p.color.set(getSunColor(x, y, z));
						switch(systemType) {
							case TYPE_SUN -> p.starSubSprite = 0;
							case TYPE_GIANT -> p.starSubSprite = 1;
							case TYPE_BLACK_HOLE -> p.starSubSprite = 2;
							case TYPE_DOUBLE_STAR -> {
								StarPosition sec = new StarPosition();
								pos.set(0, 0, 0);
								VoidSystem.getSecond(pos, offset, secPos);
								sec.pos.set(galaxyPos.x * size + (x + (secPos.x * 0.0625f)) - halfSize, galaxyPos.y * size + (y + (secPos.y * 0.0625f)) - halfSize, galaxyPos.z * size + (z + (secPos.z * 0.0625f)) - halfSize);
								sec.color.set(getSunColor(x + 30, y + 30, z + 30));
								sec.starSubSprite = 0;
								g.add(sec);
							}
							//							assert(false): secPos+": "+pos+"; "+offset;
						}
						g.add(p);
						i++;
					}
				}
			}
		}
		assert (i == numberOfStars);
		return g;
	}

	public boolean isStellarSystem(Vector3i relSysPos) {
		return galaxyMap[relSysPos.z][relSysPos.y][relSysPos.x] > threshHold && (getSystemType(relSysPos) == TYPE_DOUBLE_STAR || getSystemType(relSysPos) == TYPE_GIANT || getSystemType(relSysPos) == TYPE_SUN);
	}

	public PositionableSubSpriteCollection getPositionSprites() {
		float[] vals = new float[numberOfStars * PositionableSubSpriteCollection.DATASIZE];
		int i = 0;
		for(int z = 0; z < size; z++) {
			for(int y = 0; y < size; y++) {
				for(int x = 0; x < size; x++) {
					byte val = galaxyMap[z][y][x];
					if(val > threshHold) {
						int index = i * PositionableSubSpriteCollection.DATASIZE;
						//pos
						vals[index] = x - halfSize;
						vals[index + 1] = y - halfSize;
						vals[index + 2] = z - halfSize;
						//color
						vals[index + 3] = 1;
						vals[index + 4] = 1;
						vals[index + 5] = 1;
						vals[index + 6] = 1;
						//subsprite
						vals[index + 7] = 0;
						i++;
					}
				}
			}
		}
		assert (i == numberOfStars);
		return new PositionableSubSpriteCollection(vals);
	}

	public void draw(Camera camera, int mult, float spriteScale) {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_BLEND);
		//aditive blending for order independency
		GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		Sprite sprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath() + "stellarSprites-2x2-c-");
		sprite.setBillboard(true);
		sprite.setBlend(true);
		sprite.setFlip(true);
		camera.updateFrustum();
		sprite.blendFunc = 0;
		sprite.setDepthTest(true);
		if(spriteCollection == null) onInit();
		assert (spriteCollection != null);
		StarPosition.posMult = mult;
		StarPosition.posAdd = (100.0f / 16.0f) * 0.5f;
		StarPosition.spriteScale = spriteScale;
		Vector3f camPos = new Vector3f(camera.getPos());
		if(!camPos.equals(sorter.pos)) {
			sorter.pos.set(camPos);
			for(StarPosition p : spriteCollection) p.setDistanceToCam(Vector3fTools.length(camPos, p.getPos()));
			spriteCollection.sort(sorter);
		}
		sprite.setSelectionAreaLength(24);
		Sprite.draw3D(sprite, spriteCollection, camera);
		sprite.setSelectionAreaLength(0);
		sprite.blendFunc = 0;
		StarPosition.posMult = 1;
		StarPosition.posAdd = 0;
		StarPosition.spriteScale = 1;
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, 1);
		GL11.glColor4f(1, 1, 1, 1);
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

	public SectorInformation.SectorType getSystemTypeAt(Vector3i absSystemPos) {
		if(!USE_GALAXY) {
			return SectorInformation.SectorType.SUN;
		}
		Vector3i relPos = getLocalCoordinatesFromSystem(absSystemPos, tmp);
		byte heat = galaxyMap[relPos.z][relPos.y][relPos.x];
		//		assert(!absSystemPos.equals(0, 0, 0) || relPos.equals(halfSize, halfSize, halfSize)):absSystemPos+" -> "+relPos;
		return heat > threshHold ? SectorInformation.SectorType.SUN : SectorInformation.SectorType.VOID;
	}

	public int getNumberOfStars() {
		return numberOfStars;
	}

	/**
	 * @return the starIndices
	 */
	public IntArrayList getStarIndices() {
		return starIndices;
	}

	public StarSystemResourceRequestContainer getSystemResources(Vector3i absSysPos, StarSystemResourceRequestContainer out, GalaxyTmpVars t) {
		Vector3i relPos = getLocalCoordinatesFromSystem(absSysPos, t.tmp);
		return getSystemResourcesLocal(relPos, out, t);
	}

	/**
	 * @param relPos
	 * @param out
	 * @param t
	 * @return The set of raw resources that are available within the system with the local coordinates <code>relPos</code>.<br/>
	 * These resources will be returned as an array of bytes corresponding to the resource density (0 to 127), with the indices corresponding to ElementKeyMap.resources.<br/>
	 * For a void system, this will always be common metal and crystals only. For other systems, this will be common metal and crystal plus up to 5* other resources.
	 * Rare resources, i.e. the catalyst-forming resources Exogen, Quantanium, and Metate, have a 1 in 50* chance per resource of being spawned.
	 * <br/><br/>*(This should be changed to a config value.)
	 */
	public StarSystemResourceRequestContainer getSystemResourcesLocal(Vector3i relPos, StarSystemResourceRequestContainer out, GalaxyTmpVars t) {
		if(out.res.length != VoidSystem.RESOURCES) throw new IllegalArgumentException("Incorrect array size for system resources!!!");
		if(isVoid(relPos)){
			out.res[CommonMetal - 1] = 64;
			out.res[CommonCrystal - 1] = 64;
		} else synchronized(resourceAssignmentLock) {
			randomRes.setSeed(getSeed() - relPos.hashCode());
			int maxResCount = randomRes.nextInt(MAX_RESOURCES_PER_SYSTEM + 1); //needs to be small enough to encourage empire-building, but large enough that some systems become notably valuable and resource gathering isn't a massive chore.
			// I've chosen 5 out of the 8 standard ones, but (TODO) this should really be a config value.

			for(int resPosition : UBIQUITOUS_RESOURCES) {
				float val = resourceNoises[resPosition-1].GetNoise(relPos.x, relPos.y, relPos.z);
				byte b = (byte) (abs(val) * 128);
				out.res[resPosition - 1] = b; //TODO: (128 * maxCommonResOutOfZoneFraction) if not within resource-rich zone. should be around 0.6ish, as every player needs this stuff but there should still be an advantage for good systems
			}

			for(int resPosition : RARE_RESOURCES) {
				if(maxResCount > 0 && randomRes.nextFloat() < RARE_RESOURCE_AVAILABILITY_CHANCE) { //1 out of 50 systems. TODO config value
					float val = resourceNoises[resPosition-1].GetNoise(relPos.x, relPos.y, relPos.z);
					byte b = (byte) (abs(val) * 128);
					out.res[resPosition - 1] = b;
					maxResCount--;
				}
			}

			for (int i = 0; i < maxResCount;) {
				int resPosition = randomRes.nextInt(VoidSystem.RESOURCES);
				short blockID = ElementKeyMap.resources[resPosition];
				ElementInformation ore = ElementKeyMap.getInfo(blockID);
				if (!ElementKeyMap.isCommonResource(blockID) && //already assigned these above
						!ElementKeyMap.isRareResource(blockID) && //already dealt with these above
						!ore.deprecated && //should be self-explanatory
						out.res[resPosition] == 0) { //res[pos] not being zero would mean we already went over it
					float val = resourceNoises[resPosition].GetNoise(relPos.x, relPos.y, relPos.z);
					byte b = (byte) (abs(val) * 128);
					out.res[resPosition] = b; //TODO: (128 * maxResOutOfZoneFraction) if not within resource-rich zone. should be somewhere between 0.1 and 0.5ish. also perhaps zones can have an extra bonus for certain resources...?
					i++;
				}
			}
		}
		//TODO: This should prompt a StarLoader event/fastevent
		return out;
	}

	public float getSunIntensityFromSec(Vector3i absSectorPos) {
		Vector3i sysPos = StellarSystem.getPosFromSector(absSectorPos, new Vector3i());
		Vector3i relPos = getLocalCoordinatesFromSystem(sysPos, new Vector3i());
		return getSystemSunIntensity(relPos);
	}

	public float getSunIntensityFromSys(Vector3i absSystemPos) {
		Vector3i relPos = getLocalCoordinatesFromSystem(absSystemPos, new Vector3i());
		return getSystemSunIntensity(relPos);
	}

	public float getSunDistance(Vector3i absSectorPos) {
		Vector3i sysPos = StellarSystem.getPosFromSector(absSectorPos, new Vector3i());
		Vector3i relPos = getLocalCoordinatesFromSystem(sysPos, new Vector3i());
		int systemType = getSystemType(relPos);
		Vector3i offset = getSunPositionOffset(relPos, new Vector3i());
		Vector3i pos = new Vector3i(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
		pos.add(offset);
		Vector3i localSector = StellarSystem.getLocalCoordinates(absSectorPos, new Vector3i());
		Vector3i to = new Vector3i();
		to.sub(pos, localSector);
		float dist = to.length();
		if(systemType == TYPE_DOUBLE_STAR) {
			Vector3i pos2 = VoidSystem.getSecond(pos, offset, new Vector3i());
			Vector3i to2 = new Vector3i();
			to2.sub(pos2, localSector);
			float dist2 = to2.length();
			dist = Math.min(dist, dist2);
		} else if(systemType == TYPE_GIANT) {
			//reach is higher
			dist = max(0, dist - 1.44f);
		}
		return dist;
	}

	public boolean isVoid(Vector3i relPos) {
		return isVoid(relPos.x, relPos.y, relPos.z);
	}

	public boolean isVoidAbs(Vector3i relPos) {
		return isVoid(relPos.x + halfSize, relPos.y + halfSize, relPos.z + halfSize);
	}

	public boolean isVoid(int x, int y, int z) {
		byte val = galaxyMap[z][y][x];
		return val <= threshHold;
	}

	//INSERTED CODE [ithirahad]
	public String getName(int x, int y, int z) {
		long index = (long) z * sizeSquared + (long) y * size + x;
		String name = nameCache.get(index);
		if(name == null) {
			long systemSeed = getSystemSeed(x, y, z);
			random.setSeed(systemSeed);
			nameGenerator.setSeed(systemSeed);
			int syllables = random.nextInt(6) + 1; //originally inline in the function call below
			Vector3i location = new Vector3i(x, y, z); //new
			boolean isVoid = isVoid(x, y, z);
			if(isVoid) name = "Void"; //This should really be a translatable string for localization!
			else name = nameGenerator.compose(syllables);
			SystemNameGenerationEvent syng = new SystemNameGenerationEvent(name, nameGenerator, systemSeed, random, syllables, isVoid, getSystemType(location), getSunColor(location), this, location);
			StarLoader.fireEvent(SystemNameGenerationEvent.class, syng, true);
			name = syng.getName();
			nameCache.put(index, name); //I really hope this works with void systems lol
		}
		SystemNameGetEvent systemNameGetEvent = new SystemNameGetEvent(this, name, new Vector3i(x, y, z));
		StarLoader.fireEvent(SystemNameGetEvent.class, systemNameGetEvent, true);
		name = systemNameGetEvent.getName();
		return name;
	}
	///

	public String getName(Vector3i relPos) {
		return getName(relPos.x, relPos.y, relPos.z);
	}

	public NPCFactionManager getNpcFactionManager() {
		assert (npcFactionManager != null) : "Can only be called on server";
		return npcFactionManager;
	}

	public void setNpcFactionManager(NPCFactionManager npcFactionManager) {
		this.npcFactionManager = npcFactionManager;
	}

	public PositionableSubSpriteCollectionReal getSpriteCollection() {
		return spriteCollection;
	}

	private class CComp implements Comparator<StarPosition>, Serializable {
		public Vector3f pos = new Vector3f();

		@Override
		public int compare(StarPosition o1, StarPosition o2) {
			return Float.compare(o2.getDistanceToCam(), o1.getDistanceToCam());
		}
	}
}