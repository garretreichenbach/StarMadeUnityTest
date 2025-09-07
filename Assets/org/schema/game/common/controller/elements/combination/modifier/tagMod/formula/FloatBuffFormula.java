package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public class FloatBuffFormula extends FloatFormula {

	@Override
	public float getOutput(float input) {
		float ratio = getRatio();
		float maxBonus = getMaxBonus();
		float output;
		float in;
		if(isPreScaled()) in = 1; //we just want the linear interpolation between 1 (no combo) and whatever the combo value is
		else in = input;

		float total = 1;
		if (isLinear() && !isPreScaled()) {
			total = getMasterSize() + getCombisize() + getEffectsize();
		}
		// the actual buff

		if (isInverse()) {
			float p = 1f - 1f / maxBonus;
			float m = ratio;
			float g = m * p;

			float modInput = in - g * in;

			output = modInput * total;

		} else {
			float pAff = maxBonus * ratio;
			output = total * in + (pAff * (total * in));

		}

		if(isLinear() && isPreScaled()) output *= input;
		return output;
	}

	@Override
	public boolean needsRatio() {
		return true;
	}

}
