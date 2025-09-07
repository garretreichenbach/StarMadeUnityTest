package api.element.recipe;

import org.schema.game.common.data.element.FixedRecipe;

public class CustomModRefinery {
    private FixedRecipe recipe;
    private String name;
    private String productionTextf;
    private int bakeTimef;

    public CustomModRefinery(FixedRecipe recipe, String name, String productionText, int bakeTime) {
        this.recipe = recipe;
        this.name = name;
        productionTextf = productionText;
        bakeTimef = bakeTime;
    }

    public int getBakeTime() {
        return bakeTimef;
    }

    public FixedRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(FixedRecipe recipe) {
        this.recipe = recipe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductionText() {
        return productionTextf;
    }
}
