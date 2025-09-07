package org.schema.game.client.view.gui.weapon;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class WeaponPanelNew extends GUIElement implements GUIActiveInterface, WeaponControllerPanelInterface {

	public GUIMainWindow weaponPanel;

	// private GUIContentPane personalTab;
	private boolean init;

	private int fid;

	private boolean flagFactionTabRecreate;

	private GUIContentPane weaponsTab;

	private WeaponScrollableListNew wList;

	public WeaponPanelNew(InputState state) {
		super(state);
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		if (flagFactionTabRecreate) {
			recreateTabs();
			flagFactionTabRecreate = false;
		}
		weaponPanel.draw();
	}

	@Override
	public void onInit() {
		if (weaponPanel != null) {
			weaponPanel.cleanUp();
		}
		weaponPanel = new GUIMainWindow(getState(), 750, 550, "WeaponPanelNew");
		weaponPanel.onInit();
		weaponPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(739);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		weaponPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		this.fid = getOwnPlayer().getFactionId();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (weaponPanel.getSelectedTab() < weaponPanel.getTabs().size()) {
			beforeTab = weaponPanel.getTabs().get(weaponPanel.getSelectedTab()).getTabName();
		}
		weaponPanel.clearTabs();
		weaponsTab = weaponPanel.addTab(Lng.str("BLUEPRINTS"));
		createWeaponListPane();
		weaponPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < weaponPanel.getTabs().size(); i++) {
				if (weaponPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					weaponPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
		if (init) {
			if (this.fid != getOwnPlayer().getFactionId()) {
				if (getOwnPlayer().getFactionId() > 0 && getOwnFaction() == null) {
				} else {
					flagFactionTabRecreate = true;
					this.fid = getOwnPlayer().getFactionId();
				}
			}
		}
	}

	public void createWeaponListPane() {
		if (wList != null) {
			wList.cleanUp();
		}
		wList = new WeaponScrollableListNew(getState(), weaponsTab.getContent(0));
		wList.onInit();
		wList.getPos().y = 1;
		weaponsTab.getContent(0).attach(wList);
	}

	public PlayerState getOwnPlayer() {
		return WeaponPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return WeaponPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return weaponPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return weaponPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void checkForUpdate() {
	}

	public void drawToolTip() {
	}

	public void managerChanged(ElementCollectionManager<?, ?, ?> man) {
	}

	@Override
	public void setReconstructionRequested(boolean reconstructionRequested) {
		System.err.println("WEAPON LIST REQUESTED RECOSNTRUCTION");
		wList.flagDirty();
	}
}
