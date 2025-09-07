package org.schema.game.common.controller.elements.effectblock.kinetic;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class KineticEffectElementManager extends EffectElementManager<KineticEffectUnit, KineticEffectCollectionManager, KineticEffectElementManager> {

	

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet EFFECT_CONFIG = new InterEffectSet();
	@Override
	public InterEffectSet getInterEffect() {
		return EFFECT_CONFIG;
	}
	public KineticEffectElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.EFFECT_KINETIC_COMPUTER, ElementKeyMap.EFFECT_KINETIC, segmentController);

	}

	

	@Override
	protected String getTag() {
		return "kinetic";
	}

	@Override
	public KineticEffectCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<KineticEffectCollectionManager> clazz) {
		return new KineticEffectCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

}
