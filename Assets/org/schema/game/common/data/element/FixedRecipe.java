package org.schema.game.common.data.element;

import java.util.Arrays;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.Recipe;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.element.meta.RecipeProduct;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FixedRecipe implements RecipeInterface {

	public FixedRecipeProduct[] recipeProducts;

	public short costType = -1;
	public int costAmount = 0;
	public String name = "undef";

	public boolean canAfford(PlayerState player) {
		if (costType == -1) {
			return player.getCredits() >= costAmount;
		} else {
			return player.getInventory(null).getOverallQuantity(costType) >= costAmount;
		}
	}

	public Recipe getMetaItem() {
		Recipe recipe = (Recipe) MetaObjectManager.instantiate(MetaObjectType.RECIPE.type, (short) -1, true);
		recipe.recipeProduct = new RecipeProduct[recipeProducts.length];

		for (int i = 0; i < recipe.recipeProduct.length; i++) {
			recipe.recipeProduct[i] = new RecipeProduct();
			recipe.recipeProduct[i].inputResource = new FactoryResource[recipeProducts[i].input.length];
			for (int j = 0; j < recipe.recipeProduct[i].inputResource.length; j++) {
				recipe.recipeProduct[i].inputResource[j] = new FactoryResource(recipeProducts[i].input[j].count, recipeProducts[i].input[j].type);
			}
			recipe.recipeProduct[i].outputResource = new FactoryResource[recipeProducts[i].output.length];
			for (int j = 0; j < recipe.recipeProduct[i].outputResource.length; j++) {
				recipe.recipeProduct[i].outputResource[j] = new FactoryResource(recipeProducts[i].output[j].count, recipeProducts[i].output[j].type);
			}

		}
		recipe.compile();

		if (costType == -1) {
			System.err.println("[FixedRecipe] Setting fixed price to " + costAmount + " credits");
			recipe.fixedPrice = costAmount;
		} else {
			System.err.println("[FixedRecipe] Setting fixed price to " + costAmount + " of " + costType);
			recipe.fixedPrice = -1;
		}
		return recipe;

	}

	public Element getNode(Document doc) throws CannotAppendXMLException {
		Element e = doc.createElement("Recipe");

		e.setAttribute("costAmount", String.valueOf(costAmount));

		if (costType == -1) {
			e.setAttribute("costType", "CREDITS");
		} else {
			String keyId = ElementInformation.getKeyId(costType);
			if (keyId == null) {
				throw new CannotAppendXMLException("[RecipeResource] Cannot find property key for Block ID " + costType + "; Check your Block properties file");
			}
			e.setAttribute("costType", keyId);
		}

		e.setAttribute("name", name);
		for (FixedRecipeProduct p : recipeProducts) {
			e.appendChild(p.getNode(doc));
		}
		return e;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[RECIPE: '" + name + "' COST: " + costAmount + " of " + (costType == -1 ? "CREDITS" : ElementKeyMap.getInfo(costType).getName()) + "; " + Arrays.toString(recipeProducts) + "]";
	}

	@Override
	public FixedRecipeProduct[] getRecipeProduct() {
		return recipeProducts;
	}

	@Override
	public void producedGood(int count, GameServerState state) {
	}

	@Override
	public float getBakeTime() {
		if (recipeProducts != null && recipeProducts.length > 0 && recipeProducts[0].output != null && recipeProducts[0].output.length > 0) {
			return ElementKeyMap.getInfo(recipeProducts[0].output[0].type).factoryBakeTime;
		} else {
			return 10.0f;
		}
	}

	public boolean isBuyable() {
		return costAmount >= 0;
	}

	public String getInfoString() {
		StringBuffer b = new StringBuffer("This factory produces the \"" + name + "\" product line\n\nHere are the product you can do:\n");
		for (FixedRecipeProduct p : recipeProducts) {
			b.append(p.toString());
			b.append("\n");
		}

		return b.toString();
	}

	public GUIElement getGUI(GameClientState state) {

		int c = 0;
		int yDis = 80;
		int w = 150;
		int h = 60;
		int xDist = 150;
		GUIGraph g = new GUIGraph(state);

		for (int j = 0; j < recipeProducts.length; j++) {
			FixedRecipeProduct p = recipeProducts[j];
			for (int i = 0; i < p.input.length; i++) {
				FactoryResource resIn = p.input[i];

				String txt = resIn.count + "x " + ElementKeyMap.getInfo(resIn.type).getName();

				GUIGraphElement e = ElementKeyMap.getInfo(resIn.type).getGraphElement(state, txt, i * xDist, 20 + c * yDis, w, h);
				g.addVertex(e);

				if (i == p.input.length - 1) {
					for (int k = 0; k < p.output.length; k++) {
						FactoryResource resOut = p.output[k];

						String txtOut = resOut.count + "x " + ElementKeyMap.getInfo(resOut.type).getName();

						GUIGraphElement eOut = ElementKeyMap.getInfo(resOut.type).getGraphElement(state, txtOut, (i + 2) * xDist + k * xDist, 20 + c * yDis, w, h);
						g.addVertex(eOut);

						if (k == 0) {
							g.addConnection(e, eOut);
						}
//						if(k < p.output.length-1){
//							GUIGraphElement eA =  ElementKeyMap.getInfo(resIn.type).getGraphElement(state, "&", (i+1)*200+k*200+75, 20+c*yDis, 50, h);
//							g.addVertex(eA);
//						}
					}
				} else {
//					GUIGraphElement eA =  ElementKeyMap.getInfo(resIn.type).getGraphElement(state, "&", i*200+75, 20+c*yDis, 50, h);
//					g.addVertex(eA);
				}
			}
			c++;
		}

//		Short2ObjectOpenHashMap<ObjectArrayList<FactoryResource>> resourceMap = new Short2ObjectOpenHashMap<ObjectArrayList<FactoryResource>>();
//		int output = 1;
//		for(FixedRecipeProduct p : recipeProducts){
//
//			ObjectArrayList<FactoryResource> objectArrayList = resourceMap.get(p.output[0].type);
//			output = p.output[0].count;
//			if(objectArrayList == null){
//				objectArrayList = new ObjectArrayList<FactoryResource>();
//				resourceMap.put(p.output[0].type, objectArrayList);
//			}
//			objectArrayList.add(p.input[0]);
//		}
//
//
//
//		for(Entry<Short, ObjectArrayList<FactoryResource>> resourceEntry : resourceMap.entrySet()){
//
//			GUIBlockSprite b = new GUIBlockSprite(state, resourceEntry.getKey());
//
//			int index = 0;
//			int goDown = 80;
//
//
//			String txt = output+"x "+ElementKeyMap.getInfo(resourceEntry.getKey()).getName();
//
//			if(resourceEntry.getValue().size() == 1){
//				//draw on same horizontal layer;
//				goDown = 0;
//				yDis = 100;
//			}
//			GUIGraphElement e =  ElementKeyMap.getInfo(resourceEntry.getKey()).getGraphElement(state, txt, 300, 20+c*yDis, w, h);
//			g.addVertex(e);
//
//			for(FactoryResource f : resourceEntry.getValue()){
//
//				String txtIn = f.count+"x "+ElementKeyMap.getInfo(f.type).getName();
//				if(resourceEntry.getValue().size() > 1){
//					if(index == 0){
//						txtIn = "either "+txtIn;
//					}else{
//						txtIn = "or "+txtIn;
//					}
//				}
//				GUIGraphElement el = ElementKeyMap.getInfo(f.type).getGraphElement(state, txtIn, (index%4)*200, 20+c*yDis+goDown+(index/4)*goDown, w, h);
//				g.addVertex(el);
//				g.addConnection(el, e);
//				index++;
//			}
//			c++;
//		}
		return g;
	}
}
