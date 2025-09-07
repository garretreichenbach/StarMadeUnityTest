package org.schema.game.common.controller.elements.factory;

import org.schema.game.common.data.element.meta.RecipeInterface;

public interface FactoryProducerInterface {

	public RecipeInterface getCurrentRecipe();

	public int getFactoryCapability();
}
