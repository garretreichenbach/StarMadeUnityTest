package org.schema.game.common.controller.elements.effectblock.em;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class EmEffectElementManager extends EffectElementManager<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> {

	

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet EFFECT_CONFIG = new InterEffectSet();
	@Override
	public InterEffectSet getInterEffect() {
		return EFFECT_CONFIG;
	}
	public EmEffectElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.EFFECT_EM_COMPUTER, ElementKeyMap.EFFECT_EM, segmentController);

	}

	

	@Override
	protected String getTag() {
		return "em";
	}

	@Override
	public EmEffectCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<EmEffectCollectionManager> clazz) {
		return new EmEffectCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

}
