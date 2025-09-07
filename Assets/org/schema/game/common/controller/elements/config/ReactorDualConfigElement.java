package org.schema.game.common.controller.elements.config;

import org.schema.game.common.controller.elements.combination.modifier.Modifier;

public class ReactorDualConfigElement {
	public static int getIndex(boolean isUsingReactors){
		return isUsingReactors ? Modifier.NEW_POWER_REACTOR_INDEX : Modifier.OLD_POWER_INDEX;
	}
}
