package org.schema.game.common.controller.elements.dockingBlock.fixed;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class FixedDockingBlockElementManager extends DockingBlockElementManager<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> {

	public FixedDockingBlockElementManager(SegmentController segmentController) {
		super(segmentController, ElementKeyMap.FIXED_DOCK_ID, ElementKeyMap.FIXED_DOCK_ID_ENHANCER);
	}

	@Override
	protected String getTag() {
		return "fixeddockingblock";
	}

	@Override
	public FixedDockingBlockCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<FixedDockingBlockCollectionManager> clazz) {
		return new FixedDockingBlockCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return "Fixed Docking System Collective";
	}


	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.FIXED_DOCK_ID);
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
		return false;
	}

}
