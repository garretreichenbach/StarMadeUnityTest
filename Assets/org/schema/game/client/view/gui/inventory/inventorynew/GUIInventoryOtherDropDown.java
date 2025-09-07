package org.schema.game.client.view.gui.inventory.inventorynew;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUIInventoryOtherDropDown extends GUIDropDownList {
	private SegmentController c;

	public GUIInventoryOtherDropDown(InputState state, GUIAnchor dependend, SegmentController c, DropDownCallback dropDownCallback) {
		super(state, UIScale.getUIScale().scale(24), UIScale.getUIScale().h, UIScale.getUIScale().scale(300), dropDownCallback);
		this.dependend = dependend;
		this.c = c;

		updateList();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIDropDownList#draw()
	 */
	@Override
	public void draw() {

		if (((ManagedSegmentController<?>) c).getManagerContainer().isNamedInventoriesClientChanged()) {

			updateList();
		}

		super.draw();
	}

	private void updateList() {
		getList().clear();

		{
			GUIAnchor anc = new GUIAnchor(getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().h);
			GUITextOverlay ov = new GUITextOverlay(FontSize.MEDIUM_15, getState());
			ov.setTextSimple(Lng.str("Switch to named inventory"));
			ov.setPos(UIScale.getUIScale().scale(6), UIScale.getUIScale().scale(6), 0);
			anc.attach(ov);
			anc.setUserPointer(null);
			GUIListElement guiListElement = new GUIListElement(anc, anc, getState());
			assert (guiListElement.getHeight() > 0);
			getList().addWithoutUpdate(guiListElement);
		}
		for (StashInventory e : ((ManagedSegmentController<?>) c).getManagerContainer().getNamedInventoriesClient().values()) {
			assert (e != null);

			GUIAnchor anc = new GUIAnchor(getState(), UIScale.getUIScale().scale(400), UIScale.getUIScale().h);
			GUITextOverlay ov = new GUITextOverlay(FontSize.MEDIUM_15, getState());
			ov.setTextSimple(e.getCustomName());
			ov.setPos(UIScale.getUIScale().scale(6), UIScale.getUIScale().scale(6), 0);
			anc.attach(ov);
			anc.setUserPointer(e);
			GUIListElement guiListElement = new GUIListElement(anc, anc, getState());
			assert (guiListElement.getHeight() > 0);
			getList().addWithoutUpdate(guiListElement);
		}
		getList().updateDim();
		onListChanged();

		((ManagedSegmentController<?>) c).getManagerContainer().setNamedInventoriesClientChanged(false);
	}
}
