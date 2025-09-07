package org.schema.game.common.controller.elements.config;

import org.schema.common.config.FloatMultiConfigField;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;

public class FloatReactorDualConfigElement extends ReactorDualConfigElement implements FloatMultiConfigField{
	public float[] field = new float[2];
	private boolean hasOld;
	@Override
	public float get(int index) {
		return field[index];
	}

	@Override
	public void set(int i, float val) {
		if(i == Modifier.OLD_POWER_INDEX){
			hasOld = true;
		}
		field[i] = val;
	}
	public float get(boolean isUsingReactors){
		int index = getIndex(isUsingReactors);
		assert(index != Modifier.OLD_POWER_INDEX || hasOld):"No old power value parsed for this but variable is declared dual";
		return get(index);
	}
}
