package org.schema.game.client.view.gui.contracts;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ContractsPanel extends GUIElement implements GUIActiveInterface {

	private boolean initialized;
	private boolean flagTabRecreate;
	public GUIMainWindow contractsPanel;
	private GUIContentPane activeContractsTab;
	private GUIContentPane availableContractsTab;

	public ContractsPanel(InputState state) {
		super(state);
	}

	@Override
	public void onInit() {
		contractsPanel = new GUIMainWindow(getState(), UIScale.getUIScale().scale(650), UIScale.getUIScale().scale(550), Lng.str("Contracts"));
		contractsPanel.onInit();
		contractsPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.fireAudioEventID(501);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		contractsPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		initialized = true;
	}

	@Override
	public void draw() {
		if(!initialized) onInit();
		if(flagTabRecreate) {
			recreateTabs();
			flagTabRecreate = false;
		}
		contractsPanel.draw();
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public float getHeight() {
		return contractsPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return contractsPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if(contractsPanel.getSelectedTab() < contractsPanel.getTabs().size()) beforeTab = contractsPanel.getTabs().get(contractsPanel.getSelectedTab()).getTabName();
		contractsPanel.clearTabs();

		activeContractsTab = contractsPanel.addTab(Lng.str("ACTIVE"));

		if(getState().isInShopDistance()) {
			availableContractsTab = contractsPanel.addTab(Lng.str("AVAILABLE"));
		}

		contractsPanel.activeInterface = this;
		if(beforeTab != null) {
			for(int i = 0; i < contractsPanel.getTabs().size(); i++) {
				if(contractsPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					contractsPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	public void reset() {
		if(contractsPanel != null) contractsPanel.reset();
	}
}
