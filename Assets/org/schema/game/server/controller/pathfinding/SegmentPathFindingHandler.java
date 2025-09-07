package org.schema.game.server.controller.pathfinding;

import org.schema.game.common.controller.pathfinding.SegmentPathCalculator;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class SegmentPathFindingHandler extends AbstractPathFindingHandler<SegmentPathCalculator, SegmentPathRequest> {

	private boolean foundCore;
	private SegmentPathRequest currentPathRequest;
	private LongArrayList deepCopy;

	public SegmentPathFindingHandler(GameServerState state) {
		super(state, "SegmentPF", new SegmentPathCalculator());
	}

	@Override
	protected void init() {
		start();
	}

	@Override
	protected boolean canProcess(SegmentPathRequest cr) {
		return cr.getType() == 0 || !ElementKeyMap.getInfo(cr.getType()).isPhysical(cr.isActive());
	}

	@Override
	protected void afterCalculate(boolean foundCore, SegmentPathRequest cr) {
		this.foundCore = foundCore;
		this.currentPathRequest = cr;
		if (foundCore) {
			deepCopy = new LongArrayList(getIc().getPath().size());
			for (int i = getIc().getPath().size() - 1; i >= 0; i--) {
				deepCopy.add(getIc().getPath().get(i));
			}
			getIc().optimizePath(deepCopy);
		}

		//		System.err.println("[SegmentPathFinder] Path calc finished. enqueueing response (found: "+foundCore+")" );
		enqueueSynchedResponse();
	}

	@Override
	public void handleReturn() {
		if (foundCore) {
			//			System.err.println("[SegmentPathFinder] PATH FOUND");
			//			LongArrayList deepCopy = new LongArrayList(getIc().getPath().size());
			//			for(int i = getIc().getPath().size()-1; i >= 0; i--){
			//				deepCopy.add(getIc().getPath().get(i));
			//			}
			currentPathRequest.getCallback().pathFinished(foundCore, deepCopy);
		} else {
			System.err.println("[SegmentPathFinder] NO PATH FOUND");
			currentPathRequest.getCallback().pathFinished(foundCore, null);
		}
	}

}
