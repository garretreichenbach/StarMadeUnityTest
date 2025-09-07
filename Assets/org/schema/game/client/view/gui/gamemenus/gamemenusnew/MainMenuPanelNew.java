package org.schema.game.client.view.gui.gamemenus.gamemenusnew;

import org.schema.game.client.controller.MainMenu;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.rules.RuleSetConfigDialogGame;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleSetStat;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;

public class MainMenuPanelNew extends GUIMainWindow implements GUIActiveInterface {
	private final MainMenu mainMenu;
	// private GUIContentPane personalTab;
	private boolean init;
	private GUIContentPane generalTab;

	public MainMenuPanelNew(InputState state, MainMenu mainMenu) {
		super(state, UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(((GameClientState) state).isAdmin() ? 274 : 243), "MainMenuPanelNewNAAA");
		this.mainMenu = mainMenu;
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		super.draw();
	}

	@Override
	public void onInit() {
		super.onInit();
		recreateTabs();
		orientate(ORIENTATION_HORIZONTAL_MIDDLE | ORIENTATION_VERTICAL_MIDDLE);
		init = true;
	}

	@Override
	public GameClientState getState() {
		return ((GameClientState) super.getState());
	}

	public void recreateTabs() {
		Object beforeTab = null;
		if(getSelectedTab() < getTabs().size()) {
			beforeTab = getTabs().get(getSelectedTab()).getTabName();
		}
		clearTabs();
		generalTab = addTab(Lng.str("GENERAL"));
		createGeneralPane();
		if(beforeTab != null) {
			for(int i = 0; i < getTabs().size(); i++) {
				if(getTabs().get(i).getTabName().equals(beforeTab)) {
					setSelectedTab(i);
					break;
				}
			}
		}
	}

	private void addButton(GUIHorizontalArea.HButtonType buttonType, String text, GUICallback callback, boolean first) {
		if(first) {
			generalTab.setTextBoxHeightLast(UIScale.getUIScale().scale(28));
		} else {
			generalTab.addNewTextBox(UIScale.getUIScale().P_BUTTON_PANE_HEIGHT);
		}
		int index = generalTab.getTextboxes().size() - 1;
		GUIHorizontalButtonTablePane buttons = new GUIHorizontalButtonTablePane(getState(), 1, 1, generalTab.getContent(index));
		buttons.onInit();
		buttons.addButton(0, 0, text, buttonType, callback, null);
		generalTab.getContent(index).attach(buttons);
	}

	private void createGeneralPane() {
		addButton(GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("RESUME"), new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					mainMenu.pressedResume();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, true);
		addButton(GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("OPTIONS"), new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					mainMenu.pressedOptions();
				}
			}
		}, false);
		if(getState().isAdmin()) {
			addButton(GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, Lng.str("RULES"), new GUICallback() {
				@Override
				public boolean isOccluded() {
					return !isActive();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse() && getState().isAdmin()) {
						mainMenu.deactivate();
						RuleSetManager ruleSetManager;
						try {
							ruleSetManager = new RuleSetManager(getState().getGameState().getRuleManager());
							ruleSetManager.setState(getState());
							GUIRuleSetStat stat = new GUIRuleSetStat(getState(), ruleSetManager);
							stat.gameState = getState().getGameState();
							RuleSetConfigDialogGame d = new RuleSetConfigDialogGame(getState(), stat);
							d.activate();
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
							AudioController.fireAudioEventID(502);
						} catch(IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}, false);
		}
		addButton(GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, mainMenu.getTutorialsStringObj(), new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					mainMenu.pressedTutorials();
				}
			}
		}, false);
		addButton(GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, Lng.str("RESPAWN"), new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					mainMenu.pressedSuicide();
				}
			}
		}, false);
		addButton(GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, Lng.str("EXIT"), new GUICallback() {
			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					mainMenu.pressedExit();
				}
			}
		}, false);
	}

	public PlayerState getOwnPlayer() {
		return getState().getPlayer();
	}

	public Faction getOwnFaction() {
		return getState().getFactionManager().getFaction(getOwnPlayer().getFactionId());
	}
}
