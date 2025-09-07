package org.schema.game.client.view.gui.shop.shopnew;

import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.input.InputState;

public class ShopScrollableCategoryList extends GUIScrollablePanel implements OnInputChangedCallback {

	private ShopCategoryListNew categoryList;
	private GUIElement mother;

	public ShopScrollableCategoryList(InputState state, GUIElement dependend, GUIElement mother) {
		super(10, 10, state);
		this.categoryList = new ShopCategoryListNew(state, this);
		setContent(categoryList);
		this.dependent = dependend;
		this.mother = mother;
		setMouseUpdateEnabled(true);
	}

	@Override
	public String onInputChanged(String t) {
		this.categoryList.onInputChanged(t);
		return t;
	}

	@Override
	public boolean isActive() {
		return super.isActive() && mother.isActive();
	}

}
