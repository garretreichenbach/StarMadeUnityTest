package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public class FloatNervFomula extends FloatFormula {

	@Override
	public float getOutput(float input) {
		float ratio = getRatio();
		float maxBonus = getMaxBonus();
		float in;
		if(isPreScaled()) in = 1; //we just want the linear interpolation between 1 (no combo) and whatever the combo value is
		else in = input;

		float actualBonus;
		float output;

		float total = 1;
		if (isLinear() && !isPreScaled()) {
			total = getMasterSize() + getCombisize() + getEffectsize();
		}

		// the actual nerf

		if (isInverse()) {
			float pAff = maxBonus * ratio;
			output = total * in + (pAff * (total * in));
		} else {
			float p = 1f - 1f / maxBonus;
			float m = ratio;
			float g = m * p;

			float modInput = in - g * in;

			output = modInput * total;
		}

		if(isLinear() && isPreScaled()) output *= input;
		return output;
	}

	@Override
	public boolean needsRatio() {
		return true;
	}
}
