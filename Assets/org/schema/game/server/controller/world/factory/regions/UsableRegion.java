package org.schema.game.server.controller.world.factory.regions;

import java.util.Collection;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ControlElementMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.world.factory.regions.hooks.RegionHook;

public abstract class UsableRegion extends Region {

	public final Vector3i controllerPosition;
	private ControlElementMap cMap;
	private byte controlBlockOrientation;

	public UsableRegion(Vector3i controllerPosition, Region[] regions, Vector3i min, Vector3i max,
	                    int priority, int orientation, byte controlBlockOrientation) {
		super(regions, min, max, priority, orientation);
		this.controllerPosition = controllerPosition;
		this.controlBlockOrientation = controlBlockOrientation;
	}

	public void addHook(Collection<RegionHook<? extends UsableRegion>> hooks, Segment lastCreated) {
		RegionHook<? extends UsableRegion> createHook = createHook(lastCreated);
		synchronized (hooks) {
			hooks.add(createHook);
		}
	}

	public abstract RegionHook<? extends UsableRegion> createHook(Segment lastCreated);

	/* (non-Javadoc)
	 * @see org.schema.game.server.controller.world.factory.regions.Region#getBlockOrientation(org.schema.common.util.linAlg.Vector3i)
	 */
	@Override
	public byte getBlockOrientation(Vector3i pos) {
		return pos.equals(controllerPosition) ? controlBlockOrientation : -1;
	}

	/**
	 * @return the cMap
	 */
	public ControlElementMap getcMap() {
		return cMap;
	}

	/**
	 * @param cMap the cMap to set
	 */
	public void setcMap(ControlElementMap cMap) {
		this.cMap = cMap;
	}

	public abstract boolean hasHook();

}
