package org.schema.game.common.data.element;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FixedRecipes {
	public final ObjectArrayList<FixedRecipe> recipes = new ObjectArrayList<FixedRecipe>();

	public void appendDoc(Element recipeRoot, Document doc) throws DOMException, CannotAppendXMLException {
		for (FixedRecipe f : recipes) {
			recipeRoot.appendChild(f.getNode(doc));
		}
	}
}
