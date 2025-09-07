package org.schema.game.common.controller.elements.dockingBlock.turret;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class TurretDockingBlockCollectionManager extends DockingBlockCollectionManager<TurretDockingBlockUnit, TurretDockingBlockCollectionManager, TurretDockingBlockElementManager> {

	public TurretDockingBlockCollectionManager(SegmentPiece element,
	                                           SegmentController segController, TurretDockingBlockElementManager em) {
		super(element, segController, ElementKeyMap.TURRET_DOCK_ENHANCE_ID, em);
	}

	@Override
	public void getDockingMoved(Vector3i min, Vector3i max, byte dockingOrientation) {

		getDockingArea(min, max);
		switch(dockingOrientation) {
			case (Element.LEFT) -> {
				max.x = (Math.abs(max.x - min.x));
				min.x = -1;
			}
			case (Element.RIGHT) -> {
				min.x = -(Math.abs(max.x - min.x));
				max.x = 1;
			}
			case (Element.TOP) -> {
				max.y = (Math.abs(max.y - min.y));
				min.y = -1;
			}
			case (Element.BOTTOM) -> {
				min.y = -(Math.abs(max.y - min.y));
				max.y = 1;
			}
			case (Element.FRONT) -> {
				max.z = (Math.abs(max.z - min.z));
				min.z = -1;
			}
			case (Element.BACK) -> {
				min.z = -(Math.abs(max.z - min.z));
				max.z = 1;
			}
		}

		//		int halfSize = defaultDockingHalfSize+enhancers;
		//		min.set(-halfSize, -1, -halfSize);
		//		max.set(halfSize, halfSize*2-1, halfSize);
	}

	@Override
	protected Class<TurretDockingBlockUnit> getType() {
		return TurretDockingBlockUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public TurretDockingBlockUnit getInstance() {
		return new TurretDockingBlockUnit();
	}

	@Override
	public String getModuleName() {
		return "Turret Docking System";
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("undock"), hos, ContextFilter.IMPORTANT);
	}
}
