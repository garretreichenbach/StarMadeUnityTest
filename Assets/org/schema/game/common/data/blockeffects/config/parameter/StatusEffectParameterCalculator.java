package org.schema.game.common.data.blockeffects.config.parameter;

import java.util.List;

import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public interface StatusEffectParameterCalculator {

	void calculate(EffectModule effectModule, StatusEffectType type, StatusEffectParameterType valueType,
			List<EffectConfigElement> list);

}
