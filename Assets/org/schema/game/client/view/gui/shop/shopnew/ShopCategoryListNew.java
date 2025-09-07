package org.schema.game.client.view.gui.shop.shopnew;

import java.util.ArrayList;
import java.util.Locale;

import org.schema.game.client.controller.manager.ingame.shop.ShopControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIEnterableList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class ShopCategoryListNew extends GUIAnchor implements GUIChangeListener, OnInputChangedCallback {

	private final GUIElementList categoryList;

	private final GUIElementList filteredFlatList;

	private boolean init;

	private GUIScrollablePanel motherPanel;

	private String filterText = "";

	private ShopListElementCallback shopElementCallback;

	public ShopCategoryListNew(InputState state, GUIScrollablePanel motherPanel) {
		super(state);
		categoryList = new GUIElementList(state);
		categoryList.setScrollPane(motherPanel);
		this.shopElementCallback = new ShopListElementCallback();
		filteredFlatList = new GUIElementList(state);
		filteredFlatList.setScrollPane(motherPanel);
		filteredFlatList.setCallback(shopElementCallback);
		this.motherPanel = motherPanel;
	}

	private void addRecursively(ElementCategory categoryHirarchy, GUIElementList parentList, int lvl) {
		parentList.setCallback(shopElementCallback);
		GUITextOverlay col = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		col.setText(new ArrayList());
		col.getText().add("+ " + categoryHirarchy.getCategory());
		GUITextOverlay back = new GUITextOverlay(FontSize.MEDIUM_15, getState());
		back.setText(new ArrayList());
		back.getText().add("- " + categoryHirarchy.getCategory());
		GUIAnchor backAnc = new GUIAnchor(getState(), UIScale.getUIScale().scale(500), UIScale.getUIScale().h);
		back.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		backAnc.attach(back);
		GUIAnchor colAnc = new GUIAnchor(getState(), UIScale.getUIScale().scale(500), UIScale.getUIScale().h);
		col.setPos(UIScale.getUIScale().inset, UIScale.getUIScale().inset, 0);
		colAnc.attach(col);
		GUIEnterableList enterList = new GUIEnterableList(getState(), colAnc, backAnc);
		enterList.scrollPanel = motherPanel;
		enterList.getPos().x = lvl * UIScale.getUIScale().scale(5);
		boolean empty = true;
		if (categoryHirarchy.hasChildren()) {
			for (int i = 0; i < categoryHirarchy.getChildren().size(); i++) {
				addRecursively(categoryHirarchy.getChildren().get(i), enterList.getList(), lvl + 1);
				empty = false;
			}
		}
		enterList.getList().setCallback(shopElementCallback);
		int index = 0;
		for (int i = 0; i < categoryHirarchy.getInfoElements().size(); i++) {
			ElementInformation elementInformation = categoryHirarchy.getInfoElements().get(i);
			if (elementInformation.isShoppable()) {
				// System.err.println("-.-.-. adding "+elementInformation.getName()+" TO "+ElementParser.getStringFromType(categoryHirarchy.getCategory()));
				addToList(enterList.getList(), elementInformation, index);
				index++;
				empty = false;
			}
		}
		/*
		 * only add to parent list if this list has any members in it
		 */
		if (!empty) {
			enterList.addObserver(this);
			enterList.setUserPointer("CATEGORY");
			enterList.onInit();
			enterList.setMouseUpdateEnabled(true);
			GUIListElement listElement = new GUIListElement(enterList, enterList, getState());
			enterList.setParent(this);
			parentList.add(listElement);
		}
	}

	private void addToList(GUIElementList elementsPanelList, ElementInformation info, int index) {
		elementsPanelList.add(info.getShopItemElement(getState()).getListElement());
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		super.draw();
	}

	@Override
	public void onInit() {
		super.onInit();
		ElementCategory categoryHirarchy = ElementKeyMap.getCategoryHirarchy();
		int lvl = 0;
		if (categoryHirarchy.hasChildren()) {
			for (int i = 0; i < categoryHirarchy.getChildren().size(); i++) {
				addRecursively(categoryHirarchy.getChildren().get(i), categoryList, lvl);
			}
		}
		int index = 0;
		for (int i = 0; i < categoryHirarchy.getInfoElements().size(); i++) {
			ElementInformation elementInformation = categoryHirarchy.getInfoElements().get(i);
			if (elementInformation.isShoppable()) {
				addToList(categoryList, elementInformation, index);
				index++;
			}
		}
		this.attach(categoryList);
		categoryList.onInit();
		categoryList.setMouseUpdateEnabled(true);
		this.setMouseUpdateEnabled(true);
		init = true;
	}

	@Override
	public float getHeight() {
		if (filterText.length() > 0) {
			return filteredFlatList.getHeight();
		} else {
			return categoryList.getHeight();
		}
	}

	@Override
	public float getWidth() {
		if (filterText.length() > 0) {
			return filteredFlatList.getWidth();
		} else {
			return categoryList.getWidth();
		}
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	public ShopControllerManager getShopControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getShopControlManager();
	}

	@Override
	public boolean isInside() {
		if (motherPanel != null) {
			return motherPanel.isInside() && super.isInside();
		}
		return super.isInside();
	}

	private void updateSeatchList(String t) {
		int index = 0;
		filteredFlatList.clear();
		if (t.length() > 0) {
			for (short type : ElementKeyMap.keySet) {
				ElementInformation elementInformation = ElementKeyMap.getInfo(type);
				if (elementInformation.isShoppable() && elementInformation.getName().toLowerCase(Locale.ENGLISH).contains(t.toLowerCase(Locale.ENGLISH))) {
					filteredFlatList.addWithoutUpdate(elementInformation.getShopItemElement(getState()).getListElement());
					index++;
				}
			}
			filteredFlatList.updateDim();
			detachAll();
			attach(filteredFlatList);
		} else {
			filteredFlatList.updateDim();
			detachAll();
			attach(categoryList);
		}
	}

	/**
	 * Input from search bar
	 */
	@Override
	public String onInputChanged(String t) {
		this.filterText = t;
		updateSeatchList(t);
		return t;
	}

	private class ShopListElementCallback implements GUICallback {

		@Override
		public void callback(GUIElement callingGui, MouseEvent event) {
			if (event.pressedLeftMouse() && callingGui instanceof GUIListElement) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(679);
				GUIListElement en = (GUIListElement) callingGui;
				if ("CATEGORY".equals(en.getContent().getUserPointer())) {
				} else {
					if (getShopControlManager().getCurrentlySelectedListElement() != null) {
						getShopControlManager().getCurrentlySelectedListElement().getList().deselectAll();
						getShopControlManager().getCurrentlySelectedListElement().setSelected(false);
					}
					en.setSelected(true);
					getShopControlManager().setSelectedElementClass((Short) en.getContent().getUserPointer());
					getShopControlManager().setCurrentlySelectedListElement(en);
				}
			}
		}

		@Override
		public boolean isOccluded() {
			return motherPanel != null && (!motherPanel.isActive() || motherPanel.isInsideScrollBar());
		}
	}

	@Override
	public void onChange(boolean updateListDim) {
		categoryList.updateDim();
	}
}
