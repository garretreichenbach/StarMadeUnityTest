package org.schema.game.common.controller.elements.effectblock.em;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class EmEffectCollectionManager extends EffectCollectionManager<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> {

	public EmEffectCollectionManager(SegmentPiece element,
	                                  SegmentController segController, EmEffectElementManager em) {
		super(element, ElementKeyMap.EFFECT_EM, segController, em);
	}

	@Override
	protected Class<EmEffectUnit> getType() {
		return EmEffectUnit.class;
	}

	@Override
	public EmEffectUnit getInstance() {
		return new EmEffectUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("EM Effect");
	}

}
