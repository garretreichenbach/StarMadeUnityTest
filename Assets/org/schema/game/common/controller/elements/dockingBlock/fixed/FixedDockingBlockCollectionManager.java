package org.schema.game.common.controller.elements.dockingBlock.fixed;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class FixedDockingBlockCollectionManager extends DockingBlockCollectionManager<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> {

	public FixedDockingBlockCollectionManager(SegmentPiece element,
	                                          SegmentController segController, FixedDockingBlockElementManager em) {
		super(element, segController, ElementKeyMap.FIXED_DOCK_ID_ENHANCER, em);
	}

	@Override
	public void getDockingMoved(Vector3i min, Vector3i max, byte dockingOrientation) {

		getDockingArea(min, max);
		//
		//		int halfSize = defaultDockingHalfSize+enhancers;
		//		min.set(-halfSize, -halfSize, -halfSize);
		//		max.set(halfSize, halfSize, halfSize);
	}

	@Override
	protected Class<FixedDockingBlockUnit> getType() {
		return FixedDockingBlockUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public FixedDockingBlockUnit getInstance() {
		return new FixedDockingBlockUnit();
	}

	@Override
	public String getModuleName() {
		return "Fixed Docking System";
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, "Activate/Deactivate", hos, ContextFilter.IMPORTANT);
	}

	
}
