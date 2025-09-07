package org.schema.game.common.controller.elements.config;

import org.schema.common.config.DoubleMultiConfigField;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;

public class DoubleReactorDualConfigElement extends ReactorDualConfigElement implements DoubleMultiConfigField{
	public double[] field = new double[2];
	private boolean hasOld;
	@Override
	public double get(int index) {
		return field[index];
	}

	@Override
	public void set(int i, double val) {
		if(i == Modifier.OLD_POWER_INDEX){
			hasOld = true;
		}
		field[i] = val;
	}
	public double get(boolean isUsingReactors){
		int index = getIndex(isUsingReactors);
		assert(index != Modifier.OLD_POWER_INDEX || hasOld):"No old power value parsed for this but variable is declared dual";
		return get(index);
	}
}
