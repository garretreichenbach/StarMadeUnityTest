package org.schema.game.common.controller.elements.pulse.push;

import javax.vecmath.Vector4f;

import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.pulse.PulseCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.ContextFilter;
import org.schema.schine.input.KeyboardMappings;

public class PushPulseCollectionManager extends PulseCollectionManager<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> {

	private static Vector4f defaultColor = new Vector4f(0.8f, 0.8f, 1, 1);

	public PushPulseCollectionManager(SegmentPiece element,
	                                  SegmentController segController, PushPulseElementManager em) {
		super(element, ElementKeyMap.PUSH_PULSE_ID, segController, em);
	}

	@Override
	protected Class<PushPulseUnit> getType() {
		return PushPulseUnit.class;
	}

	@Override
	public PushPulseUnit getInstance() {
		return new PushPulseUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Push Pulse System");
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ControlBlockElementCollectionManager#getDefaultColor()
	 */
	@Override
	public Vector4f getDefaultColor() {
		return defaultColor;
	}
	@Override
	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Hos hos) {
		h.addHelper(KeyboardMappings.SHIP_PRIMARY_FIRE, Lng.str("Pulse"), hos, ContextFilter.IMPORTANT);
	}
}
