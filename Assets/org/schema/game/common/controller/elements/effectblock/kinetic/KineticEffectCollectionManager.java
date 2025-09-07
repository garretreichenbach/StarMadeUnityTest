package org.schema.game.common.controller.elements.effectblock.kinetic;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.effectblock.EffectCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;

public class KineticEffectCollectionManager extends EffectCollectionManager<KineticEffectUnit, KineticEffectCollectionManager, KineticEffectElementManager> {

	public KineticEffectCollectionManager(SegmentPiece element,
	                                  SegmentController segController, KineticEffectElementManager em) {
		super(element, ElementKeyMap.EFFECT_KINETIC, segController, em);
	}

	@Override
	protected Class<KineticEffectUnit> getType() {
		return KineticEffectUnit.class;
	}

	@Override
	public KineticEffectUnit getInstance() {
		return new KineticEffectUnit();
	}

	@Override
	public String getModuleName() {
		return Lng.str("Kinetic Effect");
	}

}
