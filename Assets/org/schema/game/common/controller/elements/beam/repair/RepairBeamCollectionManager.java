package org.schema.game.common.controller.elements.beam.repair;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class RepairBeamCollectionManager extends BeamCollectionManager<RepairUnit, RepairBeamCollectionManager, RepairElementManager>  {

	private final RepairBeamHandler handler;

	public RepairBeamCollectionManager(SegmentPiece element,
	                                   SegmentController segController, RepairElementManager em) {

		super(element, ElementKeyMap.REPAIR_ID, segController, em);

		this.handler = new RepairBeamHandler(segController, this);
	}

	/**
	 * @return the handler
	 */
	@Override
	public RepairBeamHandler getHandler() {
		return handler;
	}


	@Override
	protected Class<RepairUnit> getType() {
		return RepairUnit.class;
	}


	@Override
	public RepairUnit getInstance() {
		return new RepairUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Repair Beam System");
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Repair"), hos, ContextFilter.IMPORTANT);
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Switch Firing Mode [%s]", getFireMode().getName()), hos, ContextFilter.CRUCIAL);
	}

	@Override
	public FireMode getFireMode() {
		return FireMode.UNFOCUSED;
	}
	
}
