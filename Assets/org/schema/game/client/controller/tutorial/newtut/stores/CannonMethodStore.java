package org.schema.game.client.controller.tutorial.newtut.stores;

import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.data.element.ElementKeyMap;

/**
 * Utility class for storing methods used to calculate various stats for systems related to cannons that can't be simply
 * fetched from a static field.
 * <br/>Primarily used for the tutorial system.
 *
 * @author Garret Reichenbach
 */
public class CannonMethodStore {

	public static float baseCannonKineticEffect() {
		return CannonElementManager.basicEffectConfiguration.getStrength(InterEffectHandler.InterEffectType.KIN);
	}

	public static float baseCannonHeatEffect() {
		return CannonElementManager.basicEffectConfiguration.getStrength(InterEffectHandler.InterEffectType.HEAT);
	}

	public static float baseCannonEMEffect() {
		return CannonElementManager.basicEffectConfiguration.getStrength(InterEffectHandler.InterEffectType.EM);
	}

	public static float baseCannonMassPerBlock() {
		float totalMass = (ElementKeyMap.getInfo(ElementKeyMap.WEAPON_ID).getMass() * 2) + ElementKeyMap.getInfo(ElementKeyMap.EFFECT_EM).getMass();
		return totalMass / 3;
	}

	public static float baseCannonDistance() {
		return CannonElementManager.BASE_DISTANCE;
	}
}
