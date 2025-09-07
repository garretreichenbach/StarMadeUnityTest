package org.schema.game.client.view.gui;

import java.util.ArrayList;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

public class GUIInputInfoPanel extends GUIElement implements GUIInputInterface {

	private final GUITextButton buttonOK;

	private final GUITextButton buttonCancel;

	private GUITextOverlay infoText;

	private GUITextOverlay descriptionText;

	private GUITextOverlay errorText;

	private GUIDialogWindow background;

	private boolean okButton = false;

	private boolean cancelButton = false;

	private long timeError;

	private long timeErrorShowed;

	private boolean firstDraw = true;

	private Object[] awnsers;

	private GUIAwnserInterface actInt;

	public GUIInputInfoPanel(String windowId, ClientState state, GUICallback guiCallback, Object info, Object description, Object[] awnsers, GUIAwnserInterface actInt) {
		super(state);
		this.setCallback(guiCallback);
		this.actInt = actInt;
		infoText = new GUITextOverlay(FontSize.SMALL_15, state);
		descriptionText = new GUITextOverlay(FontSize.SMALL_14, state);
		errorText = new GUITextOverlay(state);
		background = new GUIDialogWindow(state, 600, 350, windowId);
		background.activeInterface = actInt;
		buttonOK = new GUITextButton(state, 100, 24, ColorPalette.OK, "OK", guiCallback);
		buttonOK.setUserPointer("OK");
		buttonOK.setMouseUpdateEnabled(true);
		buttonCancel = new GUITextButton(state, 100, 24, ColorPalette.CANCEL, "CANCEL", guiCallback);
		buttonCancel.setCallback(guiCallback);
		buttonCancel.setUserPointer("CANCEL");
		this.awnsers = awnsers;
		background.setCloseCallback(guiCallback);
		ArrayList<Object> t = new ArrayList<Object>();
		t.add(info);
		infoText.setText(t);
		ArrayList<Object> ti = new ArrayList<Object>();
		ti.add(description);
		descriptionText.setText(ti);
		ArrayList<Object> te = new ArrayList<Object>();
		errorText.setText(te);
		background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
	}

	@Override
	public void cleanUp() {
		background.cleanUp();
		infoText.cleanUp();
	}

	@Override
	public void draw() {
		if (firstDraw) {
			onInit();
		}
		if (needsReOrientation()) {
			background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		}
		GlUtil.glPushMatrix();
		transform();
		if (timeError < System.currentTimeMillis() - timeErrorShowed) {
			errorText.getText().clear();
		}
		buttonOK.setPos(8, (int) (background.getHeight() - (42 + buttonOK.getHeight())), 0);
		buttonCancel.setPos((int) (buttonOK.getPos().x + (buttonOK.getWidth() + 5)), (int) (buttonOK.getPos().y), 0);
		infoText.setPos((int) (background.getWidth() / 2 - infoText.getMaxLineWidth() / 2), -23, 0);
		for (AbstractSceneNode a : getChilds()) {
			a.draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		background.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		background.onInit();
		infoText.onInit();
		buttonOK.onInit();
		buttonCancel.onInit();
		descriptionText.onInit();
		background.getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(200));
		background.getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(70));
		GUIScrollablePanel p = new GUIScrollablePanel(10, 10, background.getMainContentPane().getContent(0), getState());
		p.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		GUIAnchor c = new GUIAnchor(getState()) {

			@Override
			public void draw() {
				setWidth(descriptionText.getMaxLineWidth());
				setHeight(descriptionText.getTextHeight());
				super.draw();
			}
		};
		c.attach(descriptionText);
		p.setContent(c);
		p.onInit();
		background.getMainContentPane().getContent(0).attach(p);
		int buttonHeight = 50;
		final GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, awnsers.length, background.getMainContentPane().getContent(1));
		buttons.onInit();
		for (int i = awnsers.length - 1; i >= 0; i--) {
			final int buttonIndex = i;
			buttons.addButton(0, i, awnsers[i], i == awnsers.length - 1 ? HButtonType.BUTTON_RED_MEDIUM : HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(504);
						actInt.pressedAwnser(buttonIndex);
					}
				}

				@Override
				public boolean isOccluded() {
					return !actInt.isActive();
				}
			}, null);
		}
		GUIScrollablePanel scrollButtons = new GUIScrollablePanel(10, 10, background.getMainContentPane().getContent(1), getState());
		GUIAnchor cBut = new GUIAnchor(getState()) {

			@Override
			public void draw() {
				setWidth(buttons.getWidth());
				setHeight(buttons.getHeight());
				super.draw();
			}
		};
		cBut.attach(buttons);
		scrollButtons.setContent(cBut);
		scrollButtons.onInit();
		background.getMainContentPane().getContent(1).attach(scrollButtons);
		this.attach(background);
		background.attach(infoText);
		background.attach(errorText);
		if (okButton) {
			background.attach(buttonOK);
		}
		if (cancelButton) {
			background.attach(buttonCancel);
		}
		infoText.setPos(UIScale.getUIScale().scale(95), UIScale.getUIScale().scale(11), 0);
		descriptionText.setPos(UIScale.getUIScale().smallinset, UIScale.getUIScale().smallinset, 0);
		errorText.setPos(UIScale.getUIScale().scale(97), UIScale.getUIScale().scale(300), 0);
		buttonOK.setPos(UIScale.getUIScale().scale(735), UIScale.getUIScale().scale(460), 0);
		buttonOK.setScale(0.45f, 0.45f, 0.45f);
		buttonCancel.setPos(UIScale.getUIScale().scale(800), UIScale.getUIScale().scale(460), 0);
		buttonCancel.setScale(0.45f, 0.45f, 0.45f);
		firstDraw = false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIElement#doOrientation()
	 */
	@Override
	protected void doOrientation() {
		if (isNewHud()) {
			background.doOrientation();
		} else {
			super.doOrientation();
		}
	}

	@Override
	public float getHeight() {
		return background.getWidth();
	}

	@Override
	public float getWidth() {
		return background.getHeight();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public void orientate(int orientation) {
		if (isNewHud()) {
			background.orientate(orientation);
		} else {
			super.orientate(orientation);
		}
	}

	/**
	 * @return the background
	 */
	public GUIDialogWindow getBackground() {
		return background;
	}

	/**
	 * @return the buttonCancel
	 */
	public GUITextButton getButtonCancel() {
		return buttonCancel;
	}

	/**
	 * @return the buttonOK
	 */
	public GUITextButton getButtonOK() {
		return buttonOK;
	}

	/**
	 * @return the descriptionText
	 */
	public GUITextOverlay getDescriptionText() {
		return descriptionText;
	}

	/**
	 * @param descriptionText the descriptionText to set
	 */
	public void setDescriptionText(GUITextOverlay descriptionText) {
		this.descriptionText = descriptionText;
	}

	public boolean isCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(boolean cancelButton) {
		this.cancelButton = cancelButton;
	}

	public boolean isOkButton() {
		return okButton;
	}

	public void setOkButton(boolean okButton) {
		this.okButton = okButton;
	}

	public void setErrorMessage(String msg, long timeShowed) {
		errorText.setTextSimple(msg);
		timeError = System.currentTimeMillis();
		timeErrorShowed = timeShowed;
	}
}
