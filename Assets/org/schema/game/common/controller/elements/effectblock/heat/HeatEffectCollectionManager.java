package org.schema.game.common.controller.elements.effectblock.heat;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class HeatEffectCollectionManager extends EffectCollectionManager<HeatEffectUnit, HeatEffectCollectionManager, HeatEffectElementManager> {

	public HeatEffectCollectionManager(SegmentPiece element,
	                                  SegmentController segController, HeatEffectElementManager em) {
		super(element, ElementKeyMap.EFFECT_HEAT, segController, em);
	}

	@Override
	protected Class<HeatEffectUnit> getType() {
		return HeatEffectUnit.class;
	}

	@Override
	public HeatEffectUnit getInstance() {
		return new HeatEffectUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Heat Effect");
	}

}
