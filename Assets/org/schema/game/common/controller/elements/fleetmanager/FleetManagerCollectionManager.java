package org.schema.game.common.controller.elements.fleetmanager;

import api.element.block.Blocks;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class FleetManagerCollectionManager extends ElementCollectionManager<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> {

	public FleetManagerCollectionManager(SegmentController segController, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager> elementManager) {
		super(Blocks.FLEET_MANAGER.getId(), segController, elementManager);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<FleetManagerUnit> getType() {
		return FleetManagerUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public FleetManagerUnit getInstance() {
		return new FleetManagerUnit();
	}

	@Override
	protected void onChangedCollection() {

	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return "Fleet Manager";
	}
}
