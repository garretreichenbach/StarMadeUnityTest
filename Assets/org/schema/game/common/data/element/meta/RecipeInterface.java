package org.schema.game.common.data.element.meta;

import org.schema.game.server.data.GameServerState;

public interface RecipeInterface {

	public RecipeProductInterface[] getRecipeProduct();

	public void producedGood(int count, GameServerState state);

	public float getBakeTime();

}
