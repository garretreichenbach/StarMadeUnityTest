package org.schema.game.server.controller.world.factory.station;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;
import org.schema.game.server.controller.world.factory.regions.DockingStation;
import org.schema.game.server.controller.world.factory.regions.PowerGeneratorRegion;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.RingRegion;
import org.schema.game.server.controller.world.factory.regions.ShaftInnerRegion;
import org.schema.game.server.controller.world.factory.regions.ShaftRegion;
import org.schema.game.server.controller.world.factory.regions.ShieldGeneratorRegion;
import org.schema.game.server.controller.world.factory.regions.TresureRegion;
import org.schema.game.server.controller.world.factory.regions.TunnelRegion;
import org.schema.game.server.controller.world.factory.regions.TunnelSplitRegion;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;
import org.schema.game.server.controller.world.factory.terrain.AdditionalModifierAbstract;
import org.schema.game.server.controller.world.factory.terrain.AdditionalModifierPirateTunnel;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class WorldCreatorPirateStationFactory extends WorldCreatorFactory {
	private final static int HEIGHT = 4;
	private final static int HEIGHT_BLOCKS = HEIGHT * SegmentData.SEG;
	private static short fieldInfoArray[] = new short[32768 / (8 / HEIGHT)];
	Vector3i pos = new Vector3i();
	private Region[] regions;
	private AdditionalModifierAbstract pirateCaves;
	private short tower = ElementKeyMap.HULL_COLOR_PURPLE_ID;
	private Random random;
	private ShortArrayList colors = new ShortArrayList(6);
	private int difficulty;
	private Vector3i powerMin = new Vector3i(-5, 0, -5);

	private Vector3i powerMax = new Vector3i(5, 60, 5);

	public WorldCreatorPirateStationFactory(long seed) {
		if (random == null) {
			this.random = new Random(seed);
		}
		if (pirateCaves == null) {
			pirateCaves = new AdditionalModifierPirateTunnel(
					ElementKeyMap.HULL_COLOR_GREEN_ID,
					tower,
					ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL);
		}

		difficulty = random.nextInt(15);
		regions = new Region[40];
		regions[0] = createShaft(new Vector3i(0, 1, 0), 52, 0);
		regions[1] = createInnerShaft(new Vector3i(0, 1, 0), 52, 0);

		int upperTunnelHeight = 4;

		regions[2] = createTunnel(new Vector3i(0, 50, 0), 55, upperTunnelHeight, 0);
		regions[3] = createTunnel(new Vector3i(0, 50, 0), 55, upperTunnelHeight, 1);
		regions[4] = createTunnelSplit(new Vector3i(0, 50, 0), 55, upperTunnelHeight, 0);
		regions[5] = createTunnelSplit(new Vector3i(0, 50, 0), 55, upperTunnelHeight, 1);

		regions[6] = createTunnel(new Vector3i(0, 50, -55), 55, upperTunnelHeight, 0);
		regions[7] = createTunnel(new Vector3i(0, 50, -55), 55, upperTunnelHeight, 1);
		regions[8] = createTunnelSplit(new Vector3i(0, 50, -55), 55, upperTunnelHeight, 0);
		regions[9] = createTunnelSplit(new Vector3i(0, 50, -55), 55, upperTunnelHeight, 1);

		regions[10] = createRing(new Vector3i(0, 50, 0), 57, 3, 0);

		int lowerTunnelHeight = 5;

		regions[11] = createTunnel(new Vector3i(0, 5, 0), 55, lowerTunnelHeight, 0);
		regions[12] = createTunnel(new Vector3i(0, 5, 0), 55, lowerTunnelHeight, 1);
		regions[13] = createTunnelSplit(new Vector3i(0, 5, 0), 55, lowerTunnelHeight, 0);
		regions[14] = createTunnelSplit(new Vector3i(0, 5, 0), 55, lowerTunnelHeight, 1);

		regions[15] = createTunnel(new Vector3i(0, 5, -55), 55, lowerTunnelHeight, 0);
		regions[16] = createTunnel(new Vector3i(0, 5, -55), 55, lowerTunnelHeight, 1);
		regions[17] = createTunnelSplit(new Vector3i(0, 5, -55), 55, lowerTunnelHeight, 0);
		regions[18] = createTunnelSplit(new Vector3i(0, 5, -55), 55, lowerTunnelHeight, 1);

		regions[19] = createRing(new Vector3i(0, 4, 0), 57, 4, 0);

		regions[20] = createPowerGeneratorRegion(new Vector3i(30, 0, 30), 0);
		regions[21] = createPowerGeneratorRegion(new Vector3i(-30, 0, 30), 0);
		regions[22] = createPowerGeneratorRegion(new Vector3i(-30, 0, -30), 0);
		regions[23] = createPowerGeneratorRegion(new Vector3i(30, 0, -30), 0);

		regions[24] = createTurretDock(new Vector3i(0, 48, -62), 40, 0, Element.BACK);
		regions[25] = createTurretDock(new Vector3i(0, 48, 62), 40, 0, Element.FRONT);
		regions[26] = createTurretDock(new Vector3i(62, 48, 0), 40, 0, Element.RIGHT);
		regions[27] = createTurretDock(new Vector3i(-62, 48, 0), 40, 0, Element.LEFT);

		regions[28] = createTurretDock(new Vector3i(0, 6, -62), 40, 0, Element.BACK);
		regions[29] = createTurretDock(new Vector3i(0, 6, 62), 40, 0, Element.FRONT);
		regions[30] = createTurretDock(new Vector3i(62, 6, 0), 40, 0, Element.RIGHT);
		regions[31] = createTurretDock(new Vector3i(-62, 6, 0), 40, 0, Element.LEFT);

		regions[32] = createTurretDock(new Vector3i(0, 54, -10), 40, 0, Element.TOP);
		regions[33] = createTurretDock(new Vector3i(15, 0, 15), 40, 0, Element.BOTTOM);

		regions[34] = new TresureRegion(new Vector3i(2, 1, -2), regions, new Vector3i(-3, 0, -3), new Vector3i(3, 2, 3), 20, 0, (byte) 0);
		regions[35] = new TresureRegion(new Vector3i(2, 1, 2), regions, new Vector3i(-3, 0, -3), new Vector3i(3, 2, 3), 20, 0, (byte) 0);
		regions[36] = new TresureRegion(new Vector3i(-2, 1, 2), regions, new Vector3i(-3, 0, -3), new Vector3i(3, 2, 3), 20, 0, (byte) 0);
		regions[37] = new TresureRegion(new Vector3i(-2, 1, -2), regions, new Vector3i(-3, 0, -3), new Vector3i(3, 2, 3), 20, 0, (byte) 0);

		regions[38] = createShieldGeneratorRegion(new Vector3i(0, 0, 35), 0);
		regions[39] = createShieldGeneratorRegion(new Vector3i(0, 0, -35), 0);

		for (int i = 0; i < regions.length; i++) {
			regions[i].calculateOverlapping();
		}
	}

	private Region createInnerShaft(Vector3i from, int length, int orientation) {
		Vector3i rMin = new Vector3i(-4, 0, -4);
		rMin.add(from);

		Vector3i rMax = new Vector3i(4, length, 4);
		rMax.add(from);

		ShaftInnerRegion hangarRegion = new ShaftInnerRegion(regions, rMin, rMax, 7, orientation);
		return hangarRegion;

	}

	private Region createPowerGeneratorRegion(Vector3i pos, int orientation) {
		Vector3i rMin = new Vector3i(powerMin);
		rMin.add(pos);

		Vector3i rMax = new Vector3i(powerMax);
		rMax.add(pos);

		PowerGeneratorRegion hangarRegion = new PowerGeneratorRegion(regions, rMin, rMax, 5, orientation);

		return hangarRegion;

	}

	private Region createRing(Vector3i from, float radius, int height, int orientation) {
		Vector3i rMin = new Vector3i(-radius, -height, -radius);
		rMin.add(from);

		Vector3i rMax = new Vector3i(radius, height, radius);
		rMax.add(from);

		RingRegion hangarRegion = new RingRegion(regions, rMin, rMax, 5, orientation, radius);
		return hangarRegion;

	}

	private Region createShaft(Vector3i from, int length, int orientation) {
		Vector3i rMin = new Vector3i(-5, 0, -5);
		rMin.add(from);

		Vector3i rMax = new Vector3i(5, length, 5);
		rMax.add(from);

		ShaftRegion hangarRegion = new ShaftRegion(regions, rMin, rMax, 5, orientation);
		return hangarRegion;

	}

	private Region createShieldGeneratorRegion(Vector3i pos, int orientation) {
		Vector3i rMin = new Vector3i(powerMin);
		rMin.add(pos);

		Vector3i rMax = new Vector3i(powerMax);
		rMax.add(pos);

		ShieldGeneratorRegion hangarRegion = new ShieldGeneratorRegion(regions, rMin, rMax, 5, orientation);

		return hangarRegion;

	}

	private Region createTunnel(Vector3i from, int length, int height, int orientation) {
		Vector3i rMin = new Vector3i(-5, -height, 0);
		rMin.add(from);

		Vector3i rMax = new Vector3i(5, height, length);
		rMax.add(from);

		colors.add(ElementKeyMap.HULL_COLOR_BLACK_ID);
		colors.add(ElementKeyMap.HULL_COLOR_BLUE_ID);
		colors.add(ElementKeyMap.HULL_COLOR_PURPLE_ID);
		colors.add(ElementKeyMap.HULL_COLOR_YELLOW_ID);
		colors.add(ElementKeyMap.HULL_COLOR_RED_ID);
		colors.add(ElementKeyMap.HULL_ID);
		colors.add(ElementKeyMap.GLASS_ID);

		Collections.shuffle(colors, random);

		TunnelRegion hangarRegion = new TunnelRegion(regions, rMin, rMax, 5, orientation) {

			@Override
			public short getColor1() {
				return colors.getShort(1);
			}

			@Override
			public short getColor2() {
				return colors.getShort(2);
			}

			@Override
			public short getColor3() {
				return colors.getShort(3);
			}

			@Override
			public short getHull() {
				return colors.getShort(0);
			}

			@Override
			public short getLight() {
				return ElementKeyMap.LIGHT_RED;
			}

		};
		return hangarRegion;

	}

	private Region createTunnelSplit(Vector3i from, int length, int height, int orientation) {
		Vector3i rMin = new Vector3i(-4, -(height - 1), 0);
		rMin.add(from);

		Vector3i rMax = new Vector3i(4, (height - 1), length);
		rMax.add(from);

		TunnelSplitRegion hangarRegion = new TunnelSplitRegion(regions, rMin, rMax, 6, orientation);
		return hangarRegion;

	}

	private Region createTurretDock(Vector3i from, int size, int orientation, int dockOrientation) {
		Vector3i rMin = new Vector3i(-size / 2, -size / 2, -size / 2);
		rMin.add(from);

		Vector3i rMax = new Vector3i(size / 2, size / 2, size / 2);
		rMax.add(from);

		DockingStation hangarRegion = new DockingStation(
				from, regions, rMin, rMax, 12, orientation, (byte) dockOrientation, difficulty, 7);
		return hangarRegion;

	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {

		for (int i = 0; i < regions.length; i++) {
			if (regions[i] instanceof UsableRegion) {
				((UsableRegion) regions[i]).setcMap(w.getSegmentController().getControlElementMap());
			}
		}
		random.setSeed(world.getSeed() + w.pos.x * 16 + w.pos.y * 256 + w.pos.z);
		if (w.pos.y >= 0 && FastMath.sqrt(w.pos.x * w.pos.x + w.pos.y * w.pos.y) <= 96) {
			Arrays.fill(fieldInfoArray, (short) 0);
			pirateCaves.generate(world.getSeed(), ByteUtil.divUSeg(w.pos.x), ByteUtil.divUSeg(Math.abs(w.pos.y)), ByteUtil.divUSeg(w.pos.z), fieldInfoArray, random);
			try {
			decorateWithBlockTypes(w.pos.x, w.pos.y, w.pos.z, fieldInfoArray, w.getSegmentData(), false);

			if (w.pos.equals(0, 0, 0)) {
				
					w.getSegmentData().setInfoElementForcedAddUnsynched((byte) 8, (byte) 8, (byte) 8, ElementKeyMap.HULL_COLOR_BROWN_ID, false);
					w.getSegmentData().setInfoElementForcedAddUnsynched((byte) 8, (byte) 9, (byte) 8, ElementKeyMap.FACTION_BLOCK, false);
				
				
			}
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
			}
		}
		GameServerState state = (GameServerState) w.getSegmentController().getState();
		for (int i = 0; i < regions.length; i++) {
			if (regions[i] instanceof UsableRegion && ((UsableRegion) regions[i]).hasHook()) {
				((UsableRegion) regions[i]).addHook(state.getCreatorHooks(), w);
			}
		}

		world.getSegmentBuffer().updateBB(w);
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

	private void decorateHeight(int x, int z, int maxHeight, short[] informationArray, SegmentData data, boolean mirror) throws SegmentDataWriteException {
		int top = -1;

		int segPosY = Math.abs(data.getSegment().pos.y);

		int h = segPosY + 16;

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
					Segment segment = data.getSegment();
					pos.set(segment.pos.x + x, y, segment.pos.z + z);

					boolean contains = false;
					for (Region r : regions) {
						short deligate = 0;
						if (r.contains(pos)) {
							deligate = r.deligate(pos);
							if ((r.getPriority() < 6) && informationArray[infoIndex] != 0) {
								//place nothing for gateways
							} else {
								if (deligate != Element.TYPE_ALL) {
									byte blockOrientation = r.getBlockOrientation(pos);
									//										if(pos.equals(0, 54, -50)){
									//											System.err.println("PLACED WITH REGION: "+r+"; ");
									//										}
									//										if(blockOrientation > 0){
									//											System.err.println("PLACING DOCKING BLOCK: "+pos+": "+Element.getSideString(r.getBlockOrientation(pos)));
									//										}

									placeSolid((byte) (x), (byte) ((y % 16)), (byte) (z), segment, deligate, blockOrientation);
								}
							}
							contains = true;
							break;

						}
					}
					if (contains) {
						continue;
					}

					if (informationArray[infoIndex] == 0) {
						if (x >= 0 && y >= 0 && z >= 0 && x < 16 && y < HEIGHT_BLOCKS && z < 16) {

							int infoIndexL = ((x - 1) * 16 + z) * HEIGHT_BLOCKS + y;
							int infoIndexR = ((x + 1) * 16 + z) * HEIGHT_BLOCKS + y;
							int infoIndexT = (x * 16 + z) * HEIGHT_BLOCKS + (y + 1);
							int infoIndexD = (x * 16 + z) * HEIGHT_BLOCKS + (y - 1);
							int infoIndexF = (x * 16 + (z + 1)) * HEIGHT_BLOCKS + y;
							int infoIndexB = (x * 16 + (z - 1)) * HEIGHT_BLOCKS + y;

							int infoIndexT2 = (x * 16 + z) * HEIGHT_BLOCKS + (y + 1);
							int infoIndexD2 = (x * 16 + z) * HEIGHT_BLOCKS + (y - 1);
							int ind = 0;
							short type = 0;
							if (
									(x > 0 && (type = informationArray[ind = infoIndexL]) > 0) ||
											(x < 15 && (type = informationArray[ind = infoIndexR]) > 0) ||
											(y < HEIGHT_BLOCKS - 1 && (type = informationArray[ind = infoIndexT]) > 0) ||
											(y > 0 && (type = informationArray[ind = infoIndexD]) > 0) ||
											(z < 15 && (type = informationArray[ind = infoIndexF]) > 0) ||
											(z > 0 && (type = informationArray[ind = infoIndexB]) > 0)

									) {

								//							if((ind == infoIndexL || ind == infoIndexR || ind == infoIndexF || ind == infoIndexB)
								//								&&((y < HEIGHT_BLOCKS-2 && (informationArray[infoIndexT2]) > 0) &&
								//									(y > 1 && (informationArray[infoIndexD2]) > 0)
								//									))
								//							{
								//
								//								data.setInfoElementForcedAddUnsynched((byte)(x), (byte)((y%16)), (byte)(z), ElementKeyMap.GLASS_ID, false);
								//							}else{

								if ((y >= 62) && random.nextInt(16) == 0) {
									data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), ElementKeyMap.LIGHT_ID, false);
								} else {
									data.setInfoElementForcedAddUnsynched((byte) (x), (byte) ((y % 16)), (byte) (z), type, false);
								}
								//							}
							}

						}
					}

					//					if(top > 0 && y == top){
					//////						data.setInfoElementForcedAddUnsynched((byte)(x), (byte)((y%16)), (byte)(z), ElementKeyMap.TERRAIN_LAVA_ID, false);
					//					}else{
					//						data.setInfoElementForcedAddUnsynched((byte)(x), (byte)((y%16)), (byte)(z), informationArray[infoIndex] > 0 ? informationArray[infoIndex] : 0, false);
					//					}
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
		int dx = xPos; //(64-xPos)*16;
		int dz = zPos;//(64-zPos)*16;
		//        stoneNoise = noiseGen4.generateNoiseOctaves(stoneNoise, x * 16, z * 16, 0, 16, 16, 1, d * 2D, d * 2D, d * 2D);
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				//                int k = (int)(stoneNoise[i + j * 16] / 3D + 3D + rand.nextDouble() * 0.25D);
				int l = -1;
				int distX = dx + x;//(15 - x);
				int distZ = dz + z;//(15 - z);

				float dist = FastMath.sqrt(distX * distX + distZ * distZ);
				//            	System.err.println("DIST "+dist);
				int maxHeight = 64;
				//				if(dist > 202){
				//					maxHeight -= (32 - (FastMath.sqrt((240 - dist)*40)));
				//					maxHeight = Math.max(1, maxHeight);
				//				}
				if (dist < 80) {
					decorateHeight(x, z, maxHeight, informationArray, data, mirror);
				}
			}
		}
	}

}
