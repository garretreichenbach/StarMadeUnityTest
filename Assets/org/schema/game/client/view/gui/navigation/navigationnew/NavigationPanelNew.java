package org.schema.game.client.view.gui.navigation.navigationnew;

import org.schema.game.client.data.GameClientState;
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
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class NavigationPanelNew extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow navigationPanel;

	// private GUIContentPane personalTab;
	private boolean init;

	private int fid;

	private boolean flagFactionTabRecreate;

	private GUIContentPane navigationTab;

	private NavigationScrollableListNew wList;

	public NavigationPanelNew(InputState state) {
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
		navigationPanel.draw();
	}

	@Override
	public void onInit() {
		if (navigationPanel != null) {
			navigationPanel.cleanUp();
		}
		navigationPanel = new GUIMainWindow(getState(), UIScale.getUIScale().scale(750), UIScale.getUIScale().scale(550), "NavigationPanelNew");
		navigationPanel.onInit();
		navigationPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(564);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		navigationPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		this.fid = getOwnPlayer().getFactionId();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (navigationPanel.getSelectedTab() < navigationPanel.getTabs().size()) {
			beforeTab = navigationPanel.getTabs().get(navigationPanel.getSelectedTab()).getTabName();
		}
		navigationPanel.clearTabs();
		// personalTab = catalogPanel.addTab(Lng.str("OWN");
		navigationTab = navigationPanel.addTab(Lng.str("NAVIGATION"));
		// createPersonalCatalogPane();
		createNavigationListPane();
		navigationPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < navigationPanel.getTabs().size(); i++) {
				if (navigationPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					navigationPanel.setSelectedTab(i);
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

	// public void createPersonalCatalogPane(){
	// if(mList != null){
	// mList.cleanUp();
	// }
	// mList = new CatalogScrollableListNew(getState(), personalTab.getContent(0), CatalogScrollableListNew.PERSONAL, new GUICallback() {
	// 
	// @Override
	// public boolean isOccluded() {
	// return false;
	// }
	// 
	// @Override
	// public void callback(GUIElement callingGuiElement, MouseEvent event) {
	// 
	// }
	// });
	// mList.onInit();
	// 
	// personalTab.getContent(0).attach(mList);
	// }
	public void createNavigationListPane() {
		if (wList != null) {
			wList.cleanUp();
		}
		navigationTab.setTextBoxHeightLast(UIScale.getUIScale().scale(107));
		navigationTab.addNewTextBox(UIScale.getUIScale().scale(1));
		NavigationOptionsButtonPanel c = new NavigationOptionsButtonPanel(getState(), this);
		c.onInit();
		navigationTab.setContent(0, c);
		wList = new NavigationScrollableListNew(getState(), navigationTab.getContent(1));
		wList.onInit();
		navigationTab.getContent(1).attach(wList);
	}

	public PlayerState getOwnPlayer() {
		return NavigationPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return NavigationPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return navigationPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return navigationPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void flagDirty() {
		wList.flagDirty();
	}

	public void reset() {
		navigationPanel.reset();
	}
}
