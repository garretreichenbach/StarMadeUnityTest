package org.schema.game.client.controller;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.gui.GUIInputDropDownPanel;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.DropDownCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.Collection;

public abstract class PlayerDropDownInput extends DialogInput implements DropDownCallback {

	protected GUIInputPanel inputPanel;

	protected GUIDropDownList list;

	private GUIListElement current;

	protected FontInterface fontSize = FontSize.MEDIUM_15;

	public PlayerDropDownInput(String windowId, InputState state, Object info, int height) {
		super(state);
		init(windowId, state, 420, 180, info, height, "");
		update(state, info, height, "", new ObjectArrayList<GUIElement>(1));
	}

	public PlayerDropDownInput(String windowId, InputState state, Object info, int height, Object description, Collection<? extends GUIElement> elements) {
		super(state);
		init(windowId, state, 420, 180, info, height, description);
		update(state, info, height, description, elements);
	}

	public PlayerDropDownInput(String windowId, InputState state, Object info, int height, Object description, GUIElement... elements) {
		super(state);
		init(windowId, state, 420, 180, info, height, description);
		update(state, info, height, description, elements);
	}

	public PlayerDropDownInput(String windowId, InputState state, int intitialWidth, int initialHeight, Object info, int height) {
		super(state);
		init(windowId, state, intitialWidth, initialHeight, info, height, "");
		update(state, info, height, "", new ObjectArrayList<GUIElement>(1));
	}

	public PlayerDropDownInput(String windowId, InputState state, int intitialWidth, int initialHeight, Object info, int height, Object description, Collection<? extends GUIElement> elements) {
		super(state);
		init(windowId, state, intitialWidth, initialHeight, info, height, description);
		update(state, info, height, description, elements);
	}

	public PlayerDropDownInput(String windowId, InputState state, int intitialWidth, int initialHeight, Object info, int height, Object description, GUIElement... elements) {
		super(state);
		init(windowId, state, intitialWidth, initialHeight, info, height, description);
		update(state, info, height, description, elements);
	}

	public PlayerDropDownInput(String windowId, InputState state, int intitialWidth, int initialHeight, Object info, int height, int style, Object description, GUIElement... elements) {
		super(state);
		init(windowId, state, intitialWidth, initialHeight, info, height, description);
		update(state, info, height, description, elements);
	}

	private void init(String windowId, InputState state, int initialWidth, int initialHeight, Object info, int height, Object description) {
		assert (list == null);
		assert (inputPanel == null);
		this.list = new GUIDropDownList(getState(), UIScale.getUIScale().scale(300), height, UIScale.getUIScale().scale(200), this);
		inputPanel = new GUIInputDropDownPanel(windowId, state, initialWidth, initialHeight, this, info, description, list) {

			/* (non-Javadoc)
			 * @see org.schema.game.client.view.gui.GUIInputPanel#draw()
			 */
			@Override
			public void draw() {
				if (GUIElement.isNewHud()) {
					list.dependend = ((GUIDialogWindow) inputPanel.getBackground()).getMainContentPane().getContent(0);
				}
				// System.err.println("DRAWING:::: "+((GUIDialogWindow)getBackground()).getMainContentPane().getContent(0).getWidth());
				assert (((GUIDialogWindow) inputPanel.getBackground()).getMainContentPane().getContent(0) == ((GUIDialogWindow) getBackground()).getMainContentPane().getContent(0));
				super.draw();
			}
		};
		inputPanel.onInit();
		inputPanel.setCallback(this);
	}

	public void setDropdownYPos(int yPos) {
		((GUIInputDropDownPanel) inputPanel).yPos = yPos;
		((GUIInputDropDownPanel) inputPanel).updateList(list);
	}

	public void update(InputState state, Object info, int height, Object description, GUIElement... elements) {
		assert (height > 0);
		list.getList().clear();
		for (GUIElement e : elements) {
			if(e == null) continue;
			assert (e.getHeight() > 0);
			GUIListElement guiListElement = new GUIListElement(e, e, state);
			assert (guiListElement.getHeight() > 0);
			list.getList().addWithoutUpdate(guiListElement);
		// assert(guiListElement.getHeight() > 0);
		// assert(guiListElement.getHeight() == 24):guiListElement.getHeight()+"; "+e+"; "+e.getHeight();
		}
		list.getList().updateDim();
		list.onListChanged();
		((GUIInputDropDownPanel) inputPanel).updateList(list);
	}

	public void setSelectedUserPointer(Object o) {
		((GUIInputDropDownPanel) inputPanel).getDropDown().setSelectedUserPointer(o);
	}

	public void update(InputState state, Object info, int height, Object description, Collection<? extends GUIElement> elements) {
		list.getList().clear();
		for (GUIElement e : elements) {
			assert (e != null);
			assert (e.getHeight() > 0);
			GUIListElement guiListElement = new GUIListElement(e, e, state);
			assert (guiListElement.getHeight() > 0);
			list.getList().addWithoutUpdate(guiListElement);
		}
		list.getList().updateDim();
		list.onListChanged();
		((GUIInputDropDownPanel) inputPanel).updateList(list);
	// assert(!GUIElement.isNewHud() || list.dependend.getWidth() > 0):list.dependend+"; "+list.dependend.getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.schine.graphicsengine.forms.gui.GUICallback#callback(org.schema
	 * .schine.graphicsengine.forms.gui.GUIElement)
	 */
	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (list.isExpanded()) {
			} else {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(229);
					pressedOK(current);
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(230);
					cancel();
				}
			}
		}
	}

	protected GUIListElement getSelectedValue() {
		return current;
	}

	@Override
	public GUIInputPanel getInputPanel() {
		return inputPanel;
	}

	@Override
	public abstract void onDeactivate();

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.DropDownCallback#onSelectionChanged(org.schema.schine.graphicsengine.forms.gui.GUIListElement)
	 */
	@Override
	public void onSelectionChanged(GUIListElement element) {
		this.current = element;
	}

	public abstract void pressedOK(GUIListElement current);

	public void setErrorMessage(String msg) {
		inputPanel.setErrorMessage(msg, 2000);
	}
}
