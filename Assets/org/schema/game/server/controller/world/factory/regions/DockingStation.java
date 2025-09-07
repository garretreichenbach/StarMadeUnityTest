package org.schema.game.server.controller.world.factory.regions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ControlledElementContainer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.hooks.SpawnTurretRegionHook;

public class DockingStation extends UsableRegion {

	private final int expected;
	Vector3i tmp = new Vector3i();
	private int addedBlocks = 0;
	private int difficulty;
	private boolean addedController;

	private boolean needsHook;

	private boolean hookExecuted;

	private int dockingBlockSize;

	public DockingStation(Vector3i controllerPosition, Region[] regions, Vector3i min, Vector3i max,
	                      int priority, int orientation, byte controlBlockOrientation, int difficulty, int dockingBlockSize) {
		super(controllerPosition, regions, min, max, priority, orientation, controlBlockOrientation);

		expected = dockingBlockSize * 2 + dockingBlockSize * 2 + dockingBlockSize - 4;

		this.difficulty = difficulty;
		this.dockingBlockSize = dockingBlockSize;
	}

	@Override
	public SpawnTurretRegionHook createHook(Segment lastCreated) {

		SpawnTurretRegionHook hook = new SpawnTurretRegionHook(difficulty);
		hook.initialize(this, lastCreated);
		hookExecuted = true;
		needsHook = false;

		return hook;
	}

	@Override
	public boolean hasHook() {
		return needsHook;
	}

	@Override
	protected short placeAlgorithm(Vector3i pos) {
		if (pos.equals(controllerPosition)) {
			addedController = true;
			addedBlocks++;
			needsHook = !hookExecuted && addedBlocks >= expected && addedController;

			//			System.err.println(this+" ADDING DOCKING BLOCK: "+pos+"  "+Element.getSideString(getBlockOrientation(controllerPosition)));

			return ElementKeyMap.TURRET_DOCK_ID;

		} else {
			tmp.set(controllerPosition);
			Vector3i dir = Element.DIRECTIONSi[getBlockOrientation(controllerPosition)];
			tmp.add(dir);

			if ((dir.x > 0 && pos.x > controllerPosition.x) || (dir.x < 0 && pos.x < controllerPosition.x) ||
					(dir.y > 0 && pos.y > controllerPosition.y) || (dir.y < 0 && pos.y < controllerPosition.y) ||
					(dir.z > 0 && pos.z > controllerPosition.z) || (dir.z < 0 && pos.z < controllerPosition.z)
					) {
				//docking area
				return Element.TYPE_NONE;
			} else {
				tmp.set(controllerPosition);
				tmp.sub(dir);
				if (pos.equals(tmp) ||
						(pos.x == tmp.x && pos.y == tmp.y && pos.z != tmp.z) ||
						(pos.x == tmp.x && pos.z == tmp.z && pos.y != tmp.y) ||
						(pos.z == tmp.z && pos.y == tmp.y && pos.x != tmp.x)) {

					if (Math.abs(pos.x - controllerPosition.x) < dockingBlockSize &&
							Math.abs(pos.y - controllerPosition.y) < dockingBlockSize &&
							Math.abs(pos.z - controllerPosition.z) < dockingBlockSize) {

						getcMap().addDelayed(new ControlledElementContainer(ElementCollection.getIndex(controllerPosition), ElementCollection.getIndex(pos), ElementKeyMap.TURRET_DOCK_ENHANCE_ID, true, true));

						addedBlocks++;
						needsHook = !hookExecuted && addedBlocks >= expected && addedController;

						return ElementKeyMap.TURRET_DOCK_ENHANCE_ID;
					}
				}
			}
		}
		return Element.TYPE_ALL;
	}

}
