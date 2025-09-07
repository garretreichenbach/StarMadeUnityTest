package org.schema.game.common.controller.elements.combination.modifier.tagMod.formula;

public class SetFomula extends FloatFormula {

	
	
	@Override
	public float getOutput(float input) {
		if (isLinear()) {
			if(isPreScaled()){
				return input * getMaxBonus();
			}
			float total = getMasterSize() + getCombisize() + getEffectsize();
			return total * getMaxBonus();
		}
		return getMaxBonus();
	}

	@Override
	public boolean needsRatio() {
		return false;
	}
}
