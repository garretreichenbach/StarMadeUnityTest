package org.schema.game.common.data.blockeffects.factory;

import org.schema.game.common.data.blockeffects.BlockEffect;
import org.schema.game.common.data.blockeffects.BlockEffectFactory;

public abstract class BlockEffectFactoryFac<E extends BlockEffect> {
	public abstract BlockEffectFactory<E> getInstance();
}
