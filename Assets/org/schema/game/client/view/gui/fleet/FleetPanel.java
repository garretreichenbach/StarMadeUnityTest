package org.schema.game.client.view.gui.fleet;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class FleetPanel extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow fleetPanel;

	private GUIContentPane optionTab;

	private boolean init;

	private boolean flagFleetTabRecreate;

	public FleetPanel(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		if(flagFleetTabRecreate) {
			recreateTabs();
			flagFleetTabRecreate = false;
		}
		fleetPanel.draw();
	}

	@Override
	public void onInit() {
		fleetPanel = new GUIMainWindow(getState(), UIScale.getUIScale().scale(650), UIScale.getUIScale().scale(550), Lng.str("Fleet"));
		fleetPanel.onInit();
		fleetPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(501);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		fleetPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if(fleetPanel.getSelectedTab() < fleetPanel.getTabs().size()) {
			beforeTab = fleetPanel.getTabs().get(fleetPanel.getSelectedTab()).getTabName();
		}
		fleetPanel.clearTabs();
		optionTab = fleetPanel.addTab(Lng.str("FLEET"));
		createOptionPane();
		fleetPanel.activeInterface = this;
		if(beforeTab != null) {
			for(int i = 0; i < fleetPanel.getTabs().size(); i++) {
				if(fleetPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					fleetPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
	}

	public FleetManager getFleetManager() {
		return getState().getFleetManager();
	}

	private void createOptionPane() {
		FleetOptionButtons cop = new FleetOptionButtons(getState(), this);
		cop.onInit();
		optionTab.setContent(0, cop);
		optionTab.setTextBoxHeightLast(UIScale.getUIScale().scale(86 + 24));
		optionTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		FleetScrollableListNew c0 = new FleetScrollableListNew(getState(), optionTab.getContent(1));
		c0.onInit();
		optionTab.getContent(1).attach(c0);
		optionTab.setTextBoxHeightLast(UIScale.getUIScale().scale(250));
		optionTab.addNewTextBox(UIScale.getUIScale().scale(10));
		FleetMemberScrollableListNew c1 = new FleetMemberScrollableListNew(getState(), optionTab.getContent(2));
		c1.onInit();
		optionTab.getContent(2).attach(c1);
		// FactionOptionFactionContent c1 = new FactionOptionFactionContent(getState(), this);
		// c1.onInit();
		// optionTab.setContent(1, c1);
	}

	public PlayerState getOwnPlayer() {
		return FleetPanel.this.getState().getPlayer();
	}

	@Override
	public float getHeight() {
		return fleetPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return fleetPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void reset() {
		if(fleetPanel != null) {
			fleetPanel.reset();
		}
	}
}
