/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.schema.game.client.view.gui.options;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.options.newoptions.KeyboardScrollableListNew;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.input.InputAction;
import org.schema.schine.input.InputState;
import org.schema.schine.input.InputType;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardContext;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

/**
 * @author brent
 */
public class GUIKeyboardDialog extends DialogInput {

	long deactive = 0;

	boolean active = false;

	private KeyboardMappings mapping;

	private GUIInputPanel input;

	private ScrollableTableList<?> ob;

	private boolean addAsNew = false;

	private boolean keyCombination = false;

	private GUICheckBoxTextPairNew keyCombiBox;

	private String currentCombiText = "";

	private KeyEventInterface currentPressed;

	private InputAction currentAction;

	public GUIKeyboardDialog(InputState state, KeyboardMappings mapping, ScrollableTableList<?> ob) {
		super(state);
		this.mapping = mapping;
		input = new GUIInputPanel("KEY_ASSIGN", getState(), this, Lng.str("Assign New Key to %s", mapping.getDescription()), new Object() {

			@Override
			public String toString() {
				// System.err.println("CU: "+currentCombi);
				return Lng.str("Press the input to assign to \n <%s>.\n%s", mapping.getDescription(), currentCombiText);
			}
		});
		input.setOkButton(false);
		input.setCallback(this);
		input.onInit();
		this.keyCombiBox = new GUICheckBoxTextPairNew(getState(), Lng.str("Key Combination Mode"), FontSize.MEDIUM_15) {

			@Override
			public boolean isChecked() {
				return keyCombination;
			}

			@Override
			public void deactivate() {
				keyCombination = false;
			}

			@Override
			public void activate() {
				keyCombination = true;
			}
		};
		this.ob = ob;
		this.keyCombiBox.setPos(5, 80, 0);
		input.background.attach(this.keyCombiBox);
	}

	@Override
	public void update(Timer timer) {
		super.update(timer);
		input.setOkButton(keyCombination);
		if (keyCombination && currentAction != null) {
			currentCombiText = currentAction.getName();
		} else {
			currentCombiText = "";
		}
		if (currentPressed != null && !Keyboard.isKeyDown(currentPressed.getKey())) {
			System.err.println("[KEYBOARD DIAG] RELEASED CURRENT PRESSED");
			assert (currentPressed.isInputType(InputType.KEYBOARD));
			currentPressed = null;
		// currentAction = null;
		// currentCombiText = "";
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		System.err.println("CCS: " + callingGuiElement);
		if (event.pressedLeftMouse() && !isSameUpdate()) {
			if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(599);
				cancel();
			} else if (keyCombination && currentAction != null && callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(598);
				mapping.addMapping(currentAction);
				KeyboardScrollableListNew.setChangedSetting(mapping);
				ob.flagDirty();
				deactivate();
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if (!isSameUpdate()) {
			if (e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
				cancel();
			} else {
				if (currentPressed != null && e.isReleased()) {
				// if(e.equalsEventBasic(currentPressed)) {
				// currentPressed = null;
				// currentAction = null;
				// System.err.println("[KEYBOARDDIALOG] COMBI REMOVED: ");
				// }else {
				// InputAction a = e.generateInputAction();
				// currentAction.modifierInput.remove(a);
				// System.err.println("[KEYBOARDDIALOG] COMBI MOD REMOVED: "+currentAction);
				// }
				}
				if (e.isPressed() && isInterpretAsInput(e)) {
					if (keyCombination) {
						if (currentPressed == null || !Keyboard.isKeyDown(currentPressed.getKey()) && e.isInputType(InputType.KEYBOARD)) {
							currentPressed = e;
							currentAction = e.generateInputAction();
							System.err.println("[KEYBOARDDIALOG] COMBI START: " + currentAction);
						} else if (currentAction != null && !e.equalsEventBasic(currentPressed)) {
							currentAction.modifierInput.add(e.generateInputAction());
							System.err.println("[KEYBOARDDIALOG] COMBI CONTINUE: " + currentAction);
						}
					} else {
						if (!EngineSettings.S_KEY_ALLOW_DUPLICATES.isOn()) {
							System.out.println("OPTIONS: Checking for Duplicates");
							checkForDuplicates(e, e.getKeyboardKeyRaw());
						}
						InputAction a = e.generateInputAction();
						assert (a != null);
						System.err.println("[KEYBOARD DIAG] ADDING ACTION " + a);
						mapping.addMapping(a);
						KeyboardScrollableListNew.setChangedSetting(mapping);
						ob.flagDirty();
						deactivate();
					}
				}
			}
		}
	}

	private boolean isInterpretAsInput(KeyEventInterface e) {
		if (e.isInputType(InputType.MOUSE)) {
			if (keyCombiBox.isInsideCheckbox()) {
				return false;
			}
			if (input.isMouseInAnyButton()) {
				return false;
			}
			if (input.getBackground().isMouseOnAnyDragElement()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public GUIElement getInputPanel() {
		return input;
	}

	@Override
	protected void initialize() {
	}

	@Override
	public void onDeactivate() {
		active = false;
		deactive = System.currentTimeMillis();
	}

	public boolean checkForDuplicates(KeyEventInterface e, int key) {
		for (KeyboardMappings m : KeyboardMappings.values()) {
			if (m != mapping && m.isDuplicateInputAction(e)) {
				// duplicate key
				if (checkRelated(mapping.getContext(), m.getContext())) {
					if (!mapping.ignoreDuplicate && !m.ignoreDuplicate) {
						System.err.println("KEYS RELATED: -> DUPLICATE");
					// ((GameClientState) getState()).getController().popupAlertTextMessage(
					// Lng.str("WARNING\nDuplicate detected:\nKeys for \"%s\"(%s) and\n\"%s\"(%s) have been\nswitched", m.getDescription(), m.getKeyChar(), mapping.getDescription(), mapping.getKeyChar()), 0);
					// m.setMapping(mapping.getMapping());
					}
				}
			}
		}
		return true;
	}

	private boolean checkRelated(KeyboardContext a, KeyboardContext b) {
		return isRelated(a, b) || isRelated(b, a);
	}

	private boolean isRelated(KeyboardContext a, KeyboardContext b) {
		if (a == b) {
			return true;
		}
		if (!a.isRoot()) {
			return isRelated(a.getParent(), b);
		} else {
			return false;
		}
	}
}
