package api.element.recipe;

import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.FixedRecipe;
import org.schema.game.common.data.element.FixedRecipeProduct;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to easily created FixedRecipe's
 * 
 * Syntax Example: new FixedRecipeBuilder().input(core, 10).input(block, 5).output(ice, 3).output(shields, 2).build();
 *      This will create a FixedRecipe with 1 entry, inputs 10 cores, 5 blocks, and outputs 3 ice, 2 shields.
 *  
 *  Syntax Example: new FixedRecipeBuilder().input(ice, 2).output(core, 1).next().input(shields, 1).output(cow, 10)
 *      This will created a fixedrecipe with 2 entries.
 *      
 *      
 *  Calling input() will add a new entry to the list of recipes, then calling input/output will specify its behaviour.
 */
public class FixedRecipeBuilder {
    public FixedRecipeBuilder() {
        next();
    }
    private ArrayList<FixedRecipeProduct> products = new ArrayList<>();
    
    public FixedRecipe build(){
        FixedRecipe recipe = new FixedRecipe();
        recipe.recipeProducts = products.toArray(new FixedRecipeProduct[0]);
        return recipe;
    }
    
    public FixedRecipeBuilder input(short id, int count){
        FixedRecipeProduct product = products.get(products.size() - 1);
        
        //Increase input array by 1
        product.input = Arrays.copyOf(product.input, product.input.length + 1);
        
        //Insert new input into array
        product.input[product.input.length - 1] = new FactoryResource(count, id);
        return this;
    }

    public FixedRecipeBuilder output(short id, int count){
        FixedRecipeProduct product = products.get(products.size() - 1);

        //Increase input array by 1
        product.output = Arrays.copyOf(product.output, product.output.length + 1);

        //Insert new input into array
        product.output[product.output.length - 1] = new FactoryResource(count, id);
        return this;
    }
    public FixedRecipeBuilder next(){
        FixedRecipeProduct prod = new FixedRecipeProduct();
        prod.input = new FactoryResource[0];
        prod.output = new FactoryResource[0];
        products.add(prod);
        return this;
    }
}
