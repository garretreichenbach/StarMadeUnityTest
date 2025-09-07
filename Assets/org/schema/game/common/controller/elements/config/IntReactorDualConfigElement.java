package org.schema.game.common.controller.elements.config;

import org.schema.common.config.IntMultiConfigField;
import org.schema.game.common.controller.elements.combination.modifier.Modifier;

public class IntReactorDualConfigElement extends ReactorDualConfigElement implements IntMultiConfigField{
	public int[] field = new int[2];
	private boolean hasOld;
	@Override
	public int get(int index) {
		return field[index];
	}

	@Override
	public void set(int i, int val) {
		if(i == Modifier.OLD_POWER_INDEX){
			hasOld = true;
		}
		field[i] = val;
	}
	public int get(boolean isUsingReactors){
		int index = getIndex(isUsingReactors);
		assert(index != Modifier.OLD_POWER_INDEX || hasOld):"No old power value parsed for this but variable is declared dual";
		return get(index);
	}
}
