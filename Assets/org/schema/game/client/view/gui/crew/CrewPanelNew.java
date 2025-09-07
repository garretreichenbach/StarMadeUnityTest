package org.schema.game.client.view.gui.crew;

import api.utils.game.PlayerUtils;
import org.schema.game.client.controller.PlayerCrewMenu;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.crew.quarters.QuarterTab;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.quarters.crew.CrewUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class CrewPanelNew extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow crewPanel;

	private GUIContentPane crewTab;
	private QuarterTab quarterTab;
	private boolean init;

	private AICrewScrollableListNew crewList;

	private PlayerCrewMenu crewMenu;

	public CrewPanelNew(InputState state, PlayerCrewMenu crewMenu) {
		super(state);
		this.crewMenu = crewMenu;
	}

	@Override
	public void cleanUp() {
		if (crewList != null) {
			crewList.cleanUp();
		}
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		crewPanel.draw();
	}

	@Override
	public void onInit() {
		if (crewPanel != null) {
			crewPanel.cleanUp();
		}
		crewPanel = new GUIMainWindow(getState(), 750, 550, "CrewPanelNew");
		crewPanel.onInit();
		crewPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(442);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		crewPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		crewPanel.doOrientation();
		recreateTabs();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if(crewPanel.getSelectedTab() < crewPanel.getTabs().size()) beforeTab = crewPanel.getTabs().get(crewPanel.getSelectedTab()).getTabName();
		crewPanel.clearTabs();
		crewTab = crewPanel.addTab(Lng.str("CREW MANAGEMENT"));
		createCrewPane();

		if(PlayerUtils.getCurrentControl(getOwnPlayer()) instanceof SegmentController) {
			quarterTab = new QuarterTab(getState(), crewPanel, this);
			crewPanel.getTabs().add(quarterTab);
			quarterTab.onInit();
		}

		if(getOwnPlayer().isAdmin()) {
			crewTab.setTextBoxHeightLast((int) (crewPanel.getHeight() - 139));
			crewTab.addNewTextBox(32);
			GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, crewTab.getContent(1));
			buttonPane.onInit();
			buttonPane.addButton(0, 0, Lng.str("SPAWN CREW (ADMIN)"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) CrewUtils.addCrew(getOwnPlayer(), CrewUtils.randomName());
				}

				@Override
				public boolean isOccluded() {
					return !(PlayerUtils.getCurrentControl(getOwnPlayer()) instanceof SegmentController);
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
			crewTab.getContent(1).attach(buttonPane);
		}

		crewPanel.activeInterface = this;
		if(beforeTab != null) {
			for(int i = 0; i < crewPanel.getTabs().size(); i++) {
				if(crewPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					crewPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
	}

	public void createCrewPane() {
		if (crewList != null) crewList.cleanUp();

		crewList = new AICrewScrollableListNew(getState(), crewTab.getContent(0), getWidth(), getHeight());
		crewList.onInit();
		crewTab.getContent(0).attach(crewList);
	}

	public PlayerState getOwnPlayer() {
		return getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return crewPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return crewPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return crewMenu.isActive();
	}

	public void reset() {
		crewPanel.reset();
	}
}
