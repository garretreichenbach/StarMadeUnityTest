package org.schema.game.client.view.gui.options.newoptions;

import org.schema.game.client.view.gui.options.GUIJoystickPanel;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsType;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea.HButtonType;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;

public abstract class OptionsPanelNew extends GUIMainWindow implements GUIActiveInterface {

	// private GUIContentPane personalTab;
	private boolean init;

	private GUIContentPane generalTab;

	private GUIContentPane controlsTab;

	private GUIActiveInterface a;

	public OptionsPanelNew(InputState state, GUIActiveInterface a) {
		super(state, 750, 550, "OptionsPanelNew");
		this.a = a;
	}

	public abstract void pressedOk();

	public abstract void pressedApply();

	public abstract void pressedCancel();

	@Override
	public boolean isActive() {
		return a == null || a.isActive();
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
		orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		init = true;
	}

	// public GameClientState getState() {
	// return ((GameClientState) super.getState());
	// }
	public void recreateTabs() {
		EngineSettings.dirty = true;
		try {
			EngineSettings.read();
		} catch(IOException exception) {
			throw new RuntimeException(exception);
		}
		Object beforeTab = null;
		if (getSelectedTab() < getTabs().size()) {
			beforeTab = getTabs().get(getSelectedTab()).getTabName();
		}
		clearTabs();
		generalTab = addTab(Lng.str("SETTINGS"));
		controlsTab = addTab(Lng.str("CONTROLS"));
		createSettingsPane();
		createControlsPane();
		if (beforeTab != null) {
			for (int i = 0; i < getTabs().size(); i++) {
				if (getTabs().get(i).getTabName().equals(beforeTab)) {
					setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void createControlsPane() {
		GUITabbedContent p = new GUITabbedContent(getState(), controlsTab.getContent(0));
		p.activationInterface = this;
		p.onInit();
		p.setPos(0, UIScale.getUIScale().smallinset, 0);
		controlsTab.getContent(0).attach(p);
		addSettingsTab(p, EngineSettingsType.MOUSE, Lng.str("MOUSE"));
		addKeyboardSettings(p, Lng.str("KEYBOARD"));
		GUIContentPane joystickTab = p.addTab(Lng.str("GAMEPAD"));
		GUIScrollablePanel j = new GUIScrollablePanel(10, 10, joystickTab.getContent(0), getState());
		j.setContent(new GUIJoystickPanel(j, getState()));
		joystickTab.getContent(0).attach(j);
		addOkCancel(controlsTab);
	}

	private void addKeyboardSettings(GUITabbedContent p, String name) {
		GUIContentPane tab = p.addTab(name);
		KeyboardScrollableListNew list = new KeyboardScrollableListNew(getState(), this, tab.getContent(0));
		list.onInit();
		tab.getContent(0).attach(list);
	}

	private void createSettingsPane() {
		GUITabbedContent p = new GUITabbedContent(getState(), generalTab.getContent(0));
		p.activationInterface = this;
		p.setPos(0, UIScale.getUIScale().smallinset, 0);
		p.onInit();
		generalTab.getContent(0).attach(p);
		addSettingsTab(p, EngineSettingsType.GENERAL, Lng.str("GENERAL"));
		addSettingsTab(p, EngineSettingsType.GRAPHICS, Lng.str("GRAPHICS"));
		addSettingsTab(p, EngineSettingsType.GRAPHICS_ADVANCED, Lng.str("ADV. GRAPHICS"));
		addSettingsTab(p, EngineSettingsType.SOUND, Lng.str("SOUND"));
		addOkCancel(generalTab);
	}

	private void addSettingsTab(GUITabbedContent p, EngineSettingsType s, String name) {
		GUIContentPane tab = p.addTab(name);
		OptionsScrollableListNew list = new OptionsScrollableListNew(getState(), this, tab.getContent(0), s);
		list.onInit();
		tab.getContent(0).attach(list);
	}

	private void addOkCancel(GUIContentPane t) {
		t.setTextBoxHeightLast(UIScale.getUIScale().scale(10));
		t.setListDetailMode(t.getTextboxes().get(t.getTextboxes().size() - 1));
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		int index = t.getTextboxes().size() - 1;
		GUIHorizontalButtonTablePane pane = new GUIHorizontalButtonTablePane(getState(), 3, 1, t.getContent(index));
		pane.onInit();
		pane.addButton(0, 0, Lng.str("OK"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(605);
					pressedOk();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		});
		pane.addButton(1, 0, Lng.str("APPLY"), HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(606);
					pressedApply();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		});
		pane.addButton(2, 0, Lng.str("CANCEL"), HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !a.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(607);
					pressedCancel();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return a.isActive();
			}
		});
		t.getContent(index).attach(pane);
	}
}
