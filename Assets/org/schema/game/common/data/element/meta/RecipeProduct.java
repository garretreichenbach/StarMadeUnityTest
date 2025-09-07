package org.schema.game.common.data.element.meta;

import org.schema.game.common.data.element.FactoryResource;

public class RecipeProduct implements RecipeProductInterface {
	public FactoryResource[] inputResource;
	public FactoryResource[] outputResource;

	/**
	 * @return the inputResource
	 */
	@Override
	public FactoryResource[] getInputResource() {
		return inputResource;
	}

	/**
	 * @return the outputResource
	 */
	@Override
	public FactoryResource[] getOutputResource() {
		return outputResource;
	}
}
