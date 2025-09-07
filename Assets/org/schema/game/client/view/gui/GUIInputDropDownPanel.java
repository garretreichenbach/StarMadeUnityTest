package org.schema.game.client.view.gui;

import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

public class GUIInputDropDownPanel extends GUIInputPanel {

	private GUIDropDownList dropDown;
	public int yPos = 54;
	public GUIInputDropDownPanel(String windowId, InputState state, int initialWidth, int initialHeight, GUICallback guiCallback,
	                             Object info, Object description, GUIDropDownList list) {
		super(windowId, state, initialWidth, initialHeight, guiCallback, info, description);
		assert (list != null);
		this.dropDown = list;
	}

	public void updateList(GUIDropDownList list) {
		this.dropDown = list;
		
		this.dropDown.setPos(3, yPos, 0);
		this.dropDown.dependentWidthOffset = -5;
		
		this.dropDown.getList().updateDim();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.gui.GUIInputPanel#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		this.dropDown.setPos(3, yPos, 0);
		this.dropDown.dependentWidthOffset = -5;
		if (isNewHud()) {
			if(!((GUIDialogWindow) getBackground()).getMainContentPane().getContent(0).getChilds().contains(dropDown)){
				((GUIDialogWindow) getBackground()).getMainContentPane().getContent(0).attach(dropDown);
			}
		} else {
			getBackground().attach(dropDown);
		}

	}

	public GUIDropDownList getDropDown() {
		return dropDown;
	}

	public void setDropDown(GUIDropDownList dropDown) {
		this.dropDown = dropDown;
	}

}
