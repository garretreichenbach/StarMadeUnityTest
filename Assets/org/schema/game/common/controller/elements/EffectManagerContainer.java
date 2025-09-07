package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.effectblock.EffectElementManager;

public interface EffectManagerContainer {

	EffectElementManager<?, ?, ?> getEffect(short effectType);

}
