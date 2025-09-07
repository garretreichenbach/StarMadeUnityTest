package org.schema.game.client.view.mainmenu.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.client.view.mainmenu.MainMenuGUI;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.util.List;

public class GUIFileChooserPanel extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow mainPanel;

	private GUIContentPane mainTab;

	private DialogInput diag;

	private List<GUIElement> toCleanUp = new ObjectArrayList<>();

	private FileChooserStats stats;

	private GUIActivatableTextBar inputBar;
	public static GUIActivatableTextBar pathBar;

	public GUIFileChooserPanel(InputState state, DialogInput diag, FileChooserStats stats) {
		super(state);
		this.diag = diag;
		this.stats = stats;
	}

	@Override
	public void cleanUp() {
		for (GUIElement e : toCleanUp) {
			e.cleanUp();
		}
		toCleanUp.clear();
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		mainPanel.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		mainPanel = new GUIMainWindow(getState(), 750, 500, 500, 400, "FileChooser");
		mainPanel.onInit();
		mainPanel.setPos(500, 300, 0);
		mainPanel.setWidth(750);
		mainPanel.setHeight(500);
		mainPanel.clearTabs();
		mainTab = createLocalTab();
		mainPanel.activeInterface = this;
		mainPanel.setCloseCallback(new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(766);
					diag.deactivate();
				}
			}
		});
	}

	@Override
	public boolean isInside() {
		return mainPanel.isInside();
	}

	private GUIContentPane createLocalTab() {
		int index = 0;
		GUIContentPane t = mainPanel.addTab(Lng.str("BROWSER"));

		t.setTextBoxHeightLast(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		pathBar = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, 260, 1, "PATH", t.getContent(index), stats, t1 -> stats.onPathChanged(t1));
		pathBar.setDeleteOnEnter(false);
		t.getContent(index).attach(pathBar);
		pathBar.setText(stats.getCurrentPath());
		index++;
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 3, 1, t.getContent(index));
		buttons.onInit();
		buttons.addButton(0, 0, Lng.str("DESKTOP"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.BACK)*/
					AudioController.fireAudioEventID(769);
					stats.onPressedDesktop();
					pathBar.setText(stats.getCurrentPath());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIFileChooserPanel.this.isActive();
			}
		});
		buttons.addButton(1, 0, Lng.str("HOME"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.BACK)*/
					AudioController.fireAudioEventID(769);
					stats.onPressedHome();
					pathBar.setText(stats.getCurrentPath());
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIFileChooserPanel.this.isActive();
			}
		});
		buttons.addButton(2, 0, Lng.str("COMPUTER"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.BACK)*/
					AudioController.fireAudioEventID(769);
					stats.onPressedComputer();
					pathBar.setText(stats.getCurrentPath());

				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIFileChooserPanel.this.isActive();
			}
		});
		t.getContent(index).attach(buttons);
		index++;
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);

		t.setTextBoxHeightLast(UIScale.getUIScale().scale(200));
		t.setListDetailMode(index, t.getTextboxes(0).get(index));
		GUIFileChooserList l = new GUIFileChooserList(getState(), t.getContent(index), this, stats);
		l.onInit();
		t.getContent(index).attach(l);
		index++;
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		inputBar = new GUIActivatableTextBar(getState(), FontLibrary.FontSize.MEDIUM_15, "FILENAME", t.getContent(index), stats, stats);
		t.getContent(index).attach(inputBar);
		inputBar.setText(stats.getCurrentSelectedName());
		inputBar.setDeleteOnEnter(false);
		index++;
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIDropDownList types = new GUIDropDownList(getState(), 200, 24, 300, stats, stats.getFileTypesGUIElements());
		types.dependend = t.getContent(index);
		t.getContent(index).attach(types);
		index++;
		t.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		GUIHorizontalButtonTablePane okCancel = new GUIHorizontalButtonTablePane(getState(), 2, 1, t.getContent(index));
		okCancel.onInit();
		okCancel.addButton(0, 0, Lng.str("OK"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIFileChooserPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(767);
					stats.onPressedOk(diag);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !stats.isDirectorySelected();
			}
		});
		okCancel.addButton(1, 0, Lng.str("CANCEL"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !GUIFileChooserPanel.this.isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(768);
					stats.onPressedCancel(diag);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return GUIFileChooserPanel.this.isActive();
			}
		});
		t.getContent(index).attach(okCancel);
		return t;
	}

	public void onFileNameChanged(String selectedName) {
		inputBar.setText(selectedName);
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public boolean isActive() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		return !MainMenuGUI.runningSwingDialog && (playerInputs.isEmpty() || playerInputs.get(playerInputs.size() - 1).getInputPanel() == this);
	}
}
