package org.schema.game.common.controller.elements.effectblock.heat;

import org.schema.common.config.ConfigurationElement;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;

public class HeatEffectElementManager extends EffectElementManager<HeatEffectUnit, HeatEffectCollectionManager, HeatEffectElementManager> {

	

	@ConfigurationElement(name = "EffectConfiguration")
	public static InterEffectSet EFFECT_CONFIG = new InterEffectSet();
	@Override
	public InterEffectSet getInterEffect() {
		return EFFECT_CONFIG;
	}
	public HeatEffectElementManager(final SegmentController segmentController) {
		super(ElementKeyMap.EFFECT_HEAT_COMPUTER, ElementKeyMap.EFFECT_HEAT, segmentController);

	}

	

	@Override
	protected String getTag() {
		return "heat";
	}

	@Override
	public HeatEffectCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<HeatEffectCollectionManager> clazz) {
		return new HeatEffectCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {
	}

}
