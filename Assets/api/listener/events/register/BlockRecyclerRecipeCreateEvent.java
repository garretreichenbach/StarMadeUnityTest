package api.listener.events.register;

import api.listener.events.Event;
import org.schema.game.common.data.element.FixedRecipe;

public class BlockRecyclerRecipeCreateEvent extends Event {
    private FixedRecipe recipe;
    public BlockRecyclerRecipeCreateEvent(FixedRecipe recyclerRecipe) {
        super();
        recipe = recyclerRecipe;
    }

    public FixedRecipe getRecyclerFixedRecipe(){
        return recipe;
    }
}
