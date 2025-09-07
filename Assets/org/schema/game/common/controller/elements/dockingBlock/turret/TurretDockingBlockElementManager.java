package org.schema.game.common.controller.elements.dockingBlock.turret;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class TurretDockingBlockElementManager extends DockingBlockElementManager<TurretDockingBlockUnit, TurretDockingBlockCollectionManager, TurretDockingBlockElementManager> {

	public TurretDockingBlockElementManager(SegmentController segmentController) {
		super(segmentController, ElementKeyMap.TURRET_DOCK_ID, ElementKeyMap.TURRET_DOCK_ENHANCE_ID);
	}

	@Override
	protected String getTag() {
		return "turretdockingblock";
	}

	@Override
	public TurretDockingBlockCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<TurretDockingBlockCollectionManager> clazz) {
		return new TurretDockingBlockCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return "Turret Docking System Collective";
	}


	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		typesThatNeedActivation.add(ElementKeyMap.TURRET_DOCK_ID);
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
				return false;
	}
}
