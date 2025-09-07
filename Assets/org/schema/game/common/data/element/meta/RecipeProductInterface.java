package org.schema.game.common.data.element.meta;

import org.schema.game.common.data.element.FactoryResource;

public interface RecipeProductInterface {
	/**
	 * @return the inputResource
	 */
	public FactoryResource[] getInputResource();

	/**
	 * @return the outputResource
	 */
	public FactoryResource[] getOutputResource();
}
