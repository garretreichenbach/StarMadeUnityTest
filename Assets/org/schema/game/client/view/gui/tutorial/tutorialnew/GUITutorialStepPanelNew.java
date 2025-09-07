package org.schema.game.client.view.gui.tutorial.tutorialnew;

import org.schema.game.client.controller.tutorial.TutorialDialog;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.sound.controller.AudioController;

public class GUITutorialStepPanelNew extends GUIDialogWindow {

	private TutorialDialog dialog;

	private String message;

	private GUIAnchor textAnc;

	private GUITextOverlay msgOverlay;

	public GUITutorialStepPanelNew(String windowId, ClientState state, String message, TutorialDialog dialog, Sprite image) {
		super(state, 390, 300, windowId);
		this.dialog = dialog;
		this.activeInterface = dialog;
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();
		getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
		getMainContentPane().setListDetailMode(getMainContentPane().getTextboxes().get(0));
		textAnc = new GUIAnchor(getState(), 10, 10);
		GUIScrollablePanel ts = new GUIScrollablePanel(10, 10, getMainContentPane().getTextboxes().get(0), getState());
		ts.setScrollable(GUIScrollablePanel.SCROLLABLE_HORIZONTAL | GUIScrollablePanel.SCROLLABLE_VERTICAL);
		msgOverlay = new GUITextOverlay(FontSize.SMALL_14, getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#onDirty()
			 */
			@Override
			public void onDirty() {
				textAnc.setWidth(msgOverlay.getMaxLineWidth() + 10);
				textAnc.setHeight(msgOverlay.getTextHeight() + 10);
			}
		};
		msgOverlay.setTextSimple(message);
		msgOverlay.onInit();
		msgOverlay.updateTextSize();
		textAnc.setPos(5, 5, 0);
		textAnc.attach(msgOverlay);
		textAnc.setWidth(msgOverlay.getMaxLineWidth());
		textAnc.setHeight(msgOverlay.getTextHeight());
		ts.setContent(textAnc);
		getMainContentPane().getTextboxes().get(0).attach(ts);
		getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane p = new GUIHorizontalButtonTablePane(getState(), 3, 1, getMainContentPane().getTextboxes().get(1));
		p.onInit();
		getMainContentPane().getTextboxes().get(1).attach(p);
		p.addButton(0, 0, "BACK", HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(728);
					dialog.pressedBack();
				}
			}

			@Override
			public boolean isOccluded() {
				return !GUITutorialStepPanelNew.this.isActive();
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				TutorialMode tutorialMode = ((GameClientState) state).getController().getTutorialMode();
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				TutorialMode tutorialMode = ((GameClientState) state).getController().getTutorialMode();
				return GUITutorialStepPanelNew.this.isActive() && tutorialMode.hasBack();
			}
		});
		p.addButton(1, 0, Lng.str("SKIP"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialStepPanelNew.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(729);
					dialog.pressedSkip();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITutorialStepPanelNew.this.isActive();
			}
		});
		p.addButton(2, 0, Lng.str("NEXT"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialStepPanelNew.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(730);
					dialog.pressedNext();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITutorialStepPanelNew.this.isActive();
			}
		});
		getMainContentPane().addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane pEnd = new GUIHorizontalButtonTablePane(getState(), 1, 1, getMainContentPane().getTextboxes().get(2));
		pEnd.onInit();
		getMainContentPane().getTextboxes().get(2).attach(pEnd);
		pEnd.addButton(0, 0, new Object() {

			@Override
			public String toString() {
				return (((GameClientState) getState()).getController().getTutorialMode() != null && (((GameClientState) getState()).getController().getTutorialMode().isEndState())) ? Lng.str("CLOSE") : Lng.str("EXIT TUTORIAL");
			}
		}, HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUITutorialStepPanelNew.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(731);
					dialog.pressedEnd();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUITutorialStepPanelNew.this.isActive();
			}
		});
		orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
	}

	public void updateMessage(String msg, Sprite image, TutorialDialog tutorialDialog) {
		this.dialog = tutorialDialog;
		this.activeInterface = tutorialDialog;
		msgOverlay.setTextSimple(msg);
		msgOverlay.updateTextSize();
		textAnc.setWidth(msgOverlay.getMaxLineWidth() + 10);
		textAnc.setHeight(msgOverlay.getTextHeight() + 10);
	}
}
