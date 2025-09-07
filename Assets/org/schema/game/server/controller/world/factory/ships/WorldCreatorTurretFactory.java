package org.schema.game.server.controller.world.factory.ships;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;
import org.schema.game.server.controller.world.factory.regions.CoreRegion;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.StripeRegion;
import org.schema.game.server.controller.world.factory.regions.UsableRegion;
import org.schema.game.server.controller.world.factory.regions.WeaponRegion;

public class WorldCreatorTurretFactory extends WorldCreatorFactory {
	@SuppressWarnings("unused")

	private Region[] regions;
	private int difficulty;

	public WorldCreatorTurretFactory(long seed, int difficulty) {
		this.difficulty = difficulty;
		regions = new Region[3];
		regions[0] = createCore(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
		regions[1] = createWeapon(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF+1), 10 + difficulty, 0, Element.BOTTOM);
		//the orientation is essentially not building below the core
		regions[2] = createStripes(new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF+1), 12 + difficulty, 0, Element.BOTTOM);

		for (int i = 0; i < regions.length; i++) {
			regions[i].calculateOverlapping();
		}
	}

	private Region createCore(Vector3i pos) {
		Vector3i rMin = new Vector3i(pos.x, pos.y, pos.z);

		Vector3i rMax = new Vector3i(pos.x + 1, pos.y + 1, pos.z + 1);

		CoreRegion region = new CoreRegion(
				pos, regions, rMin, rMax, 20, 0, (byte) 0);
		return region;
	}

	private Region createStripes(Vector3i from, int size, int orientation, int dockOrientation) {
		Vector3i rMin = new Vector3i(-2, -2, -2);
		rMin.add(from);

		Vector3i rMax = new Vector3i(3, 3, size / 2);
		rMax.add(from);
		short type = ElementKeyMap.HULL_COLOR_BLACK_ID;
		if (difficulty > 5) {
			type = ElementKeyMap.SHIELD_CAP_ID;
		}
		StripeRegion region = new StripeRegion(regions, rMin, rMax, 5, orientation, dockOrientation, from, type);
		return region;

	}

	private Region createWeapon(Vector3i from, int size, int orientation, int dockOrientation) {
		Vector3i rMin = new Vector3i(-1, -1, -2);
		rMin.add(from);

		Vector3i rMax = new Vector3i(2, 2, size / 2);
		rMax.add(from);

		WeaponRegion region = new WeaponRegion(
				from, regions, rMin, rMax, 8, orientation, (byte) dockOrientation);
		return region;

	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		for (int i = 0; i < regions.length; i++) {
			if (regions[i] instanceof UsableRegion) {
				((UsableRegion) regions[i]).setcMap(w.getSegmentController().getControlElementMap());
			}
		}
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
								try {
									placeSolid(x, y, z, w, deligate);
								} catch (SegmentDataWriteException e) {
									e.printStackTrace();
								}
							}
							break;
						}
					}
				}
			}
		}
		world.getSegmentBuffer().updateBB(w);

	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

}
