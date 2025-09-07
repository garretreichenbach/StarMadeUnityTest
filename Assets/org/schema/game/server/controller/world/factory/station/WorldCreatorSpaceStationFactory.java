package org.schema.game.server.controller.world.factory.station;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;
import org.schema.game.server.controller.world.factory.regions.BridgeRegion;
import org.schema.game.server.controller.world.factory.regions.DownSpikeRegion;
import org.schema.game.server.controller.world.factory.regions.HangarRegion;
import org.schema.game.server.controller.world.factory.regions.PowerGeneratorRegion;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.ShaftInnerRegion;
import org.schema.game.server.controller.world.factory.regions.ShaftRegion;
import org.schema.game.server.controller.world.factory.regions.TunnelRegion;
import org.schema.game.server.controller.world.factory.regions.TunnelSplitRegion;

public class WorldCreatorSpaceStationFactory extends WorldCreatorFactory {
	public static int HEIGHT = 15;
	private Region[] regions;
	private Vector3i hangarMin = new Vector3i(-25, -20, -30);

	private Vector3i hangarMax = new Vector3i(25, 10, 30);

	private Vector3i powerMin = new Vector3i(-15, 0, -15);
	private Vector3i powerMax = new Vector3i(15, 60, 15);

	private Vector3i bridgeMin = new Vector3i(-40, 0, -40);
	private Vector3i bridgeMax = new Vector3i(40, 20, 40);

	public WorldCreatorSpaceStationFactory(long seed) {
	}

	private Region createBridgeRegion(Vector3i pos, int orientation) {

		Vector3i rMin = new Vector3i(bridgeMin);
		rMin.add(pos);

		Vector3i rMax = new Vector3i(bridgeMax);
		rMax.add(pos);

		BridgeRegion hangarRegion = new BridgeRegion(regions, rMin, rMax, 5, orientation);

		return hangarRegion;

	}

	private Region createDownSpikeRegion(Vector3i pos, int orientation) {

		Vector3i rMin = new Vector3i(bridgeMin);
		rMin.add(pos);

		Vector3i rMax = new Vector3i(bridgeMax);
		rMax.add(pos);

		DownSpikeRegion hangarRegion = new DownSpikeRegion(regions, rMin, rMax, 4, orientation);

		return hangarRegion;

	}

	private void createFromCorner(Segment w, SegmentController world) throws SegmentDataWriteException {
		Vector3i p = new Vector3i();
		byte start = 0;
		byte end = SegmentData.SEG;

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
								placeSolid(x, y, z, w, deligate);
							}
							break;
						}
					}
				}
			}
		}
		world.getSegmentBuffer().updateBB(w);
	}

	private Region createHangarRegion(Vector3i pos, int orientation) {
		Vector3i rMin = new Vector3i(hangarMin);
		rMin.add(pos);

		Vector3i rMax = new Vector3i(hangarMax);
		rMax.add(pos);

		HangarRegion hangarRegion = new HangarRegion(regions, rMin, rMax, 5, orientation);

		return hangarRegion;

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

	private Region createShaft(Vector3i from, int length, int orientation) {
		Vector3i rMin = new Vector3i(-5, 0, -5);
		rMin.add(from);

		Vector3i rMax = new Vector3i(5, length, 5);
		rMax.add(from);

		ShaftRegion hangarRegion = new ShaftRegion(regions, rMin, rMax, 5, orientation);
		return hangarRegion;

	}

	private Region createTunnel(Vector3i from, int length, int orientation) {
		Vector3i rMin = new Vector3i(-5, -5, 0);
		rMin.add(from);

		Vector3i rMax = new Vector3i(5, 5, length);
		rMax.add(from);

		TunnelRegion hangarRegion = new TunnelRegion(regions, rMin, rMax, 5, orientation);
		return hangarRegion;

	}

	private Region createTunnelSplit(Vector3i from, int length, int orientation) {
		Vector3i rMin = new Vector3i(-4, -4, 0);
		rMin.add(from);

		Vector3i rMax = new Vector3i(4, 4, length);
		rMax.add(from);

		TunnelSplitRegion hangarRegion = new TunnelSplitRegion(regions, rMin, rMax, 6, orientation);
		return hangarRegion;

	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		if (regions == null) {
			regions = new Region[24];

			int d = 55;

			regions[0] = createHangarRegion(new Vector3i(0, 0, -d), 0);
			regions[1] = createHangarRegion(new Vector3i(0, 0, -d), 1);
			regions[2] = createHangarRegion(new Vector3i(0, 0, d), 2);
			regions[3] = createHangarRegion(new Vector3i(0, 0, d), 3);

			regions[4] = createTunnel(new Vector3i(0, -15, -28), 55, 0);
			regions[5] = createTunnel(new Vector3i(0, -15, -28), 55, 1);
			regions[6] = createTunnelSplit(new Vector3i(0, -15, -28), 55, 0);
			regions[7] = createTunnelSplit(new Vector3i(0, -15, -28), 55, 1);

			regions[8] = createShaft(new Vector3i(0, -19, 0), 80, 0);
			regions[9] = createInnerShaft(new Vector3i(0, -19, 0), 80, 0);

			regions[10] = createBridgeRegion(new Vector3i(0, 54, 0), 0);
			regions[11] = createDownSpikeRegion(new Vector3i(0, 54 - bridgeMax.y, 0), 0);

			regions[12] = createPowerGeneratorRegion(new Vector3i(50, -20, 50), 0);
			regions[13] = createPowerGeneratorRegion(new Vector3i(50, -20, -50), 0);
			regions[14] = createPowerGeneratorRegion(new Vector3i(-50, -20, -50), 0);
			regions[15] = createPowerGeneratorRegion(new Vector3i(-50, -20, 50), 0);

			regions[16] = createTunnel(new Vector3i(50, 20, -50), 100, 0);
			regions[17] = createTunnel(new Vector3i(50, 20, -50), 100, 1);
			regions[18] = createTunnelSplit(new Vector3i(50, 20, -50), 80, 0);
			regions[19] = createTunnelSplit(new Vector3i(50, 20, -50), 80, 1);

			regions[20] = createTunnel(new Vector3i(-50, 20, -50), 100, 0);
			regions[21] = createTunnel(new Vector3i(-50, 20, -50), 100, 1);
			regions[22] = createTunnelSplit(new Vector3i(-50, 20, -50), 80, 0);
			regions[23] = createTunnelSplit(new Vector3i(-50, 20, -50), 80, 1);

			for (int i = 0; i < regions.length; i++) {
				regions[i].calculateOverlapping();
			}
		}
		try {
			createFromCorner(w, world);
		} catch (SegmentDataWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

}
