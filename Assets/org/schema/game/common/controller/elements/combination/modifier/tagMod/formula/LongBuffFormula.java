package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public class LongBuffFormula extends LongFormula {

	@Override
	public long getOuput(long input) {
		float ratio = getRatio();

		float maxBonus = getMaxBonus();

		float actualBonus;
		if (isInverse()) {
			actualBonus = 1f / (ratio * maxBonus);
		} else {
			actualBonus = (ratio * maxBonus);
		}

		long output = (long) (input * actualBonus);

		return output;
	}

}
