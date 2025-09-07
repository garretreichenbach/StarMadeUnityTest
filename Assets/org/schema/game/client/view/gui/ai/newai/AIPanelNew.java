package org.schema.game.client.view.gui.ai.newai;

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
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class AIPanelNew extends GUIElement implements GUIActiveInterface {

	public GUIMainWindow aiPanel;

	private GUIContentPane currentEntityTab;

	// private GUIContentPane personalTab;
	private boolean init;

	private boolean flagFactionTabRecreate;

	private AIEntityScrollableList entityAIList;

	public AIPanelNew(InputState state) {
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
		aiPanel.draw();
	}

	@Override
	public void onInit() {
		if (aiPanel != null) {
			aiPanel.cleanUp();
		}
		aiPanel = new GUIMainWindow(getState(), 750, 550, "AIPanelNew");
		aiPanel.onInit();
		aiPanel.setCloseCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CLOSE)*/
					AudioController.fireAudioEventID(326);
					getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
				}
			}

			@Override
			public boolean isOccluded() {
				return !getState().getController().getPlayerInputs().isEmpty();
			}
		});
		aiPanel.orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		recreateTabs();
		init = true;
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if (aiPanel.getSelectedTab() < aiPanel.getTabs().size()) {
			beforeTab = aiPanel.getTabs().get(aiPanel.getSelectedTab()).getTabName().toString();
		}
		aiPanel.clearTabs();
		currentEntityTab = aiPanel.addTab(Lng.str("CURRENT ENTITY AI"));
		createCurrentEntityPane();
		// createPersonalCatalogPane();
		aiPanel.activeInterface = this;
		if (beforeTab != null) {
			for (int i = 0; i < aiPanel.getTabs().size(); i++) {
				if (aiPanel.getTabs().get(i).getTabName().equals(beforeTab)) {
					aiPanel.setSelectedTab(i);
					break;
				}
			}
		}
	}

	@Override
	public void update(Timer timer) {
		entityAIList.update(timer);
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
	public void createCurrentEntityPane() {
		if (entityAIList != null) {
			entityAIList.cleanUp();
		}
		entityAIList = new AIEntityScrollableList(getState(), currentEntityTab.getContent(0));
		entityAIList.onInit();
		currentEntityTab.getContent(0).attach(entityAIList);
	}

	public PlayerState getOwnPlayer() {
		return AIPanelNew.this.getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return AIPanelNew.this.getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}

	@Override
	public float getHeight() {
		return aiPanel.getHeight();
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	@Override
	public float getWidth() {
		return aiPanel.getWidth();
	}

	@Override
	public boolean isActive() {
		return getState().getController().getPlayerInputs().isEmpty();
	}

	public void reset() {
		aiPanel.reset();
	}
}
