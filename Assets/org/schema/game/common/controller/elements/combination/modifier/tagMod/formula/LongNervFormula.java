package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public class LongNervFormula extends LongFormula {

	@Override
	public long getOuput(long input) {

		float ratio = getRatio();

		float maxBonus = getMaxBonus();

		float actualNerv;
		if (isInverse()) {
			actualNerv = (ratio * maxBonus);
		} else {
			actualNerv = 1f / (ratio * maxBonus);
		}

		long output = (long) ((input + input * ratio) * actualNerv);

		return output;
	}

}
