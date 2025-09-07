package org.schema.game.client.view.gui.messagelog;

import org.schema.game.client.data.ClientMessageLog;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;

public class GUIClientLogPanel extends GUIAnchor implements GUICallback {

	private GUIOverlay background;
	private GUIScrollablePanel scrollPanel;
	private GUIElementList panelList;
	private ClientMessageLog messageLog;
	private GUIDropDownList makeGUIList;
	private boolean dateTag = false;
	private boolean typeTag = false;

	public GUIClientLogPanel(InputState state) {
		super(state);

		messageLog = ((GameClientState) getState()).getMessageLog();
		background = new GUIOverlay(Controller.getResLoader().getSprite("panel-std-gui-"), getState());

		width = background.getWidth();
		height = background.getHeight();

		scrollPanel = new GUIScrollablePanel(512, 366, getState());
		//add sub-panels
		panelList = new GUIElementList(getState());

		scrollPanel.setContent(panelList);

		GUICheckBox dateBox = new GUICheckBox(state) {

			@Override
			protected void activate() throws StateParameterNotFoundException {
				dateTag = true;
				messageLog.setChanged(true);
			}			@Override
			protected boolean isActivated() {
				return dateTag;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				dateTag = false;
				messageLog.setChanged(true);
			}


		};
		GUICheckBox typeBox = new GUICheckBox(state) {

			@Override
			protected boolean isActivated() {
				return typeTag;
			}

			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				typeTag = false;
				messageLog.setChanged(true);
			}

			@Override
			protected void activate() throws StateParameterNotFoundException {
				typeTag = true;
				messageLog.setChanged(true);
			}
		};
		GUITextButton bb = new GUITextButton(state, 20, 18, " X", this);
		bb.setUserPointer("X");

		GUITextOverlay tDate = new GUITextOverlay(state);
		GUITextOverlay tType = new GUITextOverlay(state);
		tDate.setTextSimple("Date");
		tType.setTextSimple("Type");

		tDate.setPos(260, 32, 0);
		dateBox.setPos(295, 32, 0);
		dateBox.setScale(0.5f, 0.5f, 0.5f);

		tType.setPos(360, 32, 0);
		typeBox.setPos(395, 32, 0);
		typeBox.setScale(0.5f, 0.5f, 0.5f);

		bb.setPos(260 + 530, 32, 0);

		this.attach(background);
		background.attach(tDate);
		background.attach(tType);
		background.attach(dateBox);
		background.attach(typeBox);
		background.attach(bb);
		background.attach(scrollPanel);

		scrollPanel.setPos(260, 64, 0);

		orientate(GUIElement.ORIENTATION_HORIZONTAL_MIDDLE | GUIElement.ORIENTATION_VERTICAL_MIDDLE);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#draw()
	 */
	@Override
	public void draw() {
		if (messageLog.isChanged()) {
			reList();
		}
		super.draw();
	}

	private void reList() {

		if (makeGUIList != null) {
			background.detach(makeGUIList);
		}
		makeGUIList = messageLog.makeGUIList(panelList, dateTag, typeTag);

		makeGUIList.setPos(500, 32, 0);
		panelList.setScrollPane(scrollPanel);

		background.attach(makeGUIList);
		messageLog.setChanged(false);
	}

	public String getCurrentChatPrefix() {
		return messageLog.getCurrentChatPrefix();
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		//deligate to ClientMessageLogPlayerInput
		getCallback().callback(callingGuiElement, event);
	}

}
