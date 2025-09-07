package org.schema.game.common.controller.elements.beam.harvest;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.Salvager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.beam.BeamCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class SalvageBeamCollectionManager extends BeamCollectionManager<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> {

	private final SalvageBeamHandler handler;

	public SalvageBeamCollectionManager(SegmentPiece element,
	                                    SegmentController segController, SalvageElementManager em) {

		super(element, ElementKeyMap.SALVAGE_ID, segController, em);

		this.handler = new SalvageBeamHandler((Salvager) segController, this);
	}

	/**
	 * @return the handler
	 */
	@Override
	public SalvageBeamHandler getHandler() {
		return handler;
	}


	@Override
	protected Class<SalvageUnit> getType() {
		return SalvageUnit.class;
	}

	

	@Override
	public SalvageUnit getInstance() {
		return new SalvageUnit();
	}
	

	@Override
	public String getModuleName() {
		return Lng.str("Salvage System");
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Salvage"), hos, ContextFilter.IMPORTANT);
		h.addHelper(KeyboardMappings.SWITCH_FIRE_MODE, Lng.str("Switch Firing Mode [%s]", getFireMode().getName()), hos, ContextFilter.CRUCIAL);
	}
}
