package org.schema.game.client.view.gui.playerstats;

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

public class PlayerStatisticsPanelNew extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow playerStatPanel;
	//	private GUIContentPane personalTab;
	private boolean init;
	private int fid;
	private boolean flagFactionTabRecreate;
	private GUIContentPane statisticsTab;
	private PlayerStatisticsScrollableListNew wList;
	public PlayerStatisticsPanelNew(InputState state) {
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
		playerStatPanel.draw();
	}

	@Override
	public void onInit() {
		if (playerStatPanel != null) {
			playerStatPanel.cleanUp();
		}
		playerStatPanel = new GUIMainWindow(getState(), 750, 550, "PlayerStatsPanelNew"){
								
			@Override
			public boolean canMoveAndResizeWhenMouseGrabbed() {
				return true;
			}
			
		};
		playerStatPanel.onInit();

		playerStatPanel.setCloseCallback(new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});

		playerStatPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);

		recreateTabs();

		this.fid = getOwnPlayer().getFactionId();

		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (playerStatPanel.getSelectedTab() < playerStatPanel.getTabs().size()) {
			beforeTab = playerStatPanel.getTabs().get(playerStatPanel.getSelectedTab()).getTabName();
		}
		playerStatPanel.clearTabs();

//		personalTab = catalogPanel.addTab(Lng.str("OWN");
		statisticsTab = playerStatPanel.addTab(Lng.str("NAVIGATION"));

//		createPersonalCatalogPane();
		createNavigationListPane();

		playerStatPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < playerStatPanel.getTabs().size(); i++) {
				if (playerStatPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					playerStatPanel.setSelectedTab(i);
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

	//	public void createPersonalCatalogPane(){
//		if(mList != null){
//			mList.cleanUp();
//		}
//		mList = new CatalogScrollableListNew(getState(), personalTab.getContent(0), CatalogScrollableListNew.PERSONAL, new GUICallback() {
//
//			@Override
//			public boolean isOccluded() {
//				return false;
//			}
//
//			@Override
//			public void callback(GUIElement callingGuiElement, MouseEvent event) {
//
//			}
//		});
//		mList.onInit();
//
//		personalTab.getContent(0).attach(mList);
//	}
	public void createNavigationListPane() {
		statisticsTab.setTextBoxHeightLast(UIScale.getUIScale().scale(106));
		wList = new PlayerStatisticsScrollableListNew(getState(), statisticsTab.getContent(0));
		wList.onInit();
		statisticsTab.getContent(0).attach(wList);

	}

	public PlayerState getOwnPlayer() {
		return PlayerStatisticsPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return PlayerStatisticsPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return playerStatPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return playerStatPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void flagDirty() {
		wList.flagDirty();
	}

	public void reset() {
		playerStatPanel.reset();
	}

	public void playerListUpdated() {
		wList.onChange(false);
	}

}
