package org.schema.game.client.controller.manager;



import org.schema.game.client.controller.JoinMenu;
import org.schema.game.client.controller.manager.ingame.InGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.InputType;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public class GlobalGameControlManager extends AbstractControlManager {

//	public static final int HELP_KEY = -1;
//	public static final int TUTORIAL_SKIP_KEY = GLFW.GLFW_KEY_U;
//	public static final int TUTORIAL_END_KEY = GLFW.GLFW_KEY_DELETE;
//	public static final int TUTORIAL_END_STEP_KEY = GLFW.GLFW_KEY_END;
//	public static final int TUTORIAL_REPEAT_STEP_KEY = GLFW.GLFW_KEY_HOME;
//	public static final int TUTORIAL_RESTART_KEY = GLFW.GLFW_KEY_INSERT;
//	public static final int MAIN_MENU_KEY = GLFW.GLFW_KEY_ESCAPE;
	
	private DebugControlManager debugControlManager;
	private InGameControlManager ingameControlManager;
	private OptionGameControlManager optionsControlManager;
	private MainMenuManager mainMenuManager;
	private HelpManager helpManager;
	private boolean initialized;

	public GlobalGameControlManager(GameClientState state) {
		super(state);

	}

	public void activateMainMenu() {

		boolean mainMenuActive = mainMenuManager.isActive();

		// T108: Possible breaking change: don't disable in-game control when main menu is active
//		ingameControlManager.setActive(mainMenuActive);

		if (!mainMenuActive && optionsControlManager.isActive()) {
			optionsControlManager.setActive(false);
			optionsControlManager.deactivateMenu();
		}

		mainMenuManager.setActive(!mainMenuActive);

//		if(!mainMenuManager.isActive()){
//			synchronized (getState().getController().getPlayerInputs()) {
//				for(int i = 0; i <  getState().getController().getPlayerInputs().size(); i++){
//					PlayerInput p  = getState().getController().getPlayerInputs().get(i);
//					if(p instanceof MainMenu){
//						getState().getController().getPlayerInputs().get(i).deactivate();
//						break;
//					}
//				}
//			}
//		}

	}

	/**
	 * @return the debugControlManager
	 */
	public DebugControlManager getDebugControlManager() {
		return debugControlManager;
	}

	/**
	 * @return the helpManager
	 */
	public HelpManager getHelpManager() {
		return helpManager;
	}

	/**
	 * @return the ingameControlManager
	 */
	public InGameControlManager getIngameControlManager() {
		return ingameControlManager;
	}

	/**
	 * @return the mainMenuManager
	 */
	public MainMenuManager getMainMenuManager() {
		return mainMenuManager;
	}

	/**
	 * @return the optionsControlManager
	 */
	public OptionGameControlManager getOptionsControlManager() {
		return optionsControlManager;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if(e.isInputType(InputType.MOUSE) && e.isPressed()) {
			//end forced ungrabbing of the screen if screen is clicked in any way
			if (CameraMouseState.ungrabForced) {
				CameraMouseState.ungrabForced = false;
			}
		}
		
//		if(getState().getController().getTutorialController().isBackgroundVideoActive()){
//			if(e.isTriggered(KeyboardMappings.TUTORIAL_KEY_CLOSE) ||
//				e.isTriggered(KeyboardMappings.TUTORIAL_KEY_PAUSE) ||
//				e.isTriggered(KeyboardMappings.TUTORIAL_KEY_ZOOM_IN) ||
//				e.isTriggered(KeyboardMappings.TUTORIAL_KEY_ZOOM_OUT) ){
//				return;
//			}
//		}
		int playerInputsBeforeHandle = getState().getController().getPlayerInputs().size();
		if (KeyboardMappings.PLAYER_LIST.isDown()) {
			debugControlManager.handleKeyEvent(e);
		}
		super.handleKeyEvent(e);

		if(e.isTriggered(KeyboardMappings.FULLSCREEN_TOGGLE)) {
			EngineSettings.G_FULLSCREEN.setOn(!EngineSettings.G_FULLSCREEN.isOn());
			DialogInput.applyScreenSettings();
		}
		
		if (e.isTriggered(KeyboardMappings.RELEASE_MOUSE)) {
			CameraMouseState.ungrabForced = !CameraMouseState.ungrabForced;
		} else if (e.isTriggered(KeyboardMappings.SCREENSHOT_WITH_GUI) && !getState().isDebugKeyDown()) {
			WorldDrawer.flagScreenShotWithGUI = true;
		} else if (e.isTriggered(KeyboardMappings.SCREENSHOT_WITHOUT_GUI) && !getState().isDebugKeyDown()) {
			WorldDrawer.flagScreenShotWithoutGUI = true;
		}else if (!e.isDebugKey() && e.isTriggered(KeyboardMappings.TUTORIAL)){
			if(!getState().getController().getTutorialController().isTutorialSelectorActive()){
				getState().getController().getTutorialController().onActivateFromTopTaskBar();
			}else{
				getState().getController().getTutorialController().onDeactivateFromTopTaskBar();
			}
		}
		if(e.isTriggered(KeyboardMappings.MAIN_MENU)){
			if (EngineSettings.S_EXIT_ON_ESC.isOn()) {
				System.err.println("[GLOBALGAMECONTROLLER] ESCAPE: EXIT: " + EngineSettings.S_EXIT_ON_ESC.isOn());
				GLFrame.setFinished(true);
			} else {
				if (ingameControlManager.getChatControlManager().isActive()) {
					ingameControlManager.getChatControlManager().setActive(false);
				} else if (mainMenuManager.isActive()) {
					activateMainMenu();
				} else {
					if (playerInputsBeforeHandle == 0) {
						if (ingameControlManager.getPlayerGameControlManager().getPlayerIntercationManager().isSuspended() || !ingameControlManager.getPlayerGameControlManager().getPlayerIntercationManager().isActive()) {
							boolean inJoinMenu = false;
							for (int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
								if (getState().getController().getPlayerInputs().get(i) instanceof JoinMenu) {
									inJoinMenu = true;
								}
							}
							if (inJoinMenu) {
								activateMainMenu();
							} else if (!inJoinMenu && getState().getController().getPlayerInputs().size() == 0) {
								if (!isHinderedInteraction()) {
									ingameControlManager.getPlayerGameControlManager().deactivateAll();
								}
							}
						} else {
							activateMainMenu();
						}
					}
				}

			}
		}
		
	}


	@Override
	public void update(Timer timer) {
		
		
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			debugControlManager.update(timer);
		}
		if(ingameControlManager.getPlayerGameControlManager().getPlayerIntercationManager().isTreeActive()) {
			//set all keypresses to unpressed for the server
			getState().getPlayer().getControllerState().resetNetworkInputClient();
		}
		super.update(timer);
	}

	public void initialize() {
		debugControlManager = new DebugControlManager(getState());
		ingameControlManager = new InGameControlManager(getState());
		optionsControlManager = new OptionGameControlManager(getState());
		mainMenuManager = new MainMenuManager(getState());

		helpManager = new HelpManager(getState());

		getControlManagers().add(debugControlManager);
		getControlManagers().add(ingameControlManager);
		getControlManagers().add(optionsControlManager);
		getControlManagers().add(mainMenuManager);

		getControlManagers().add(helpManager);

		ingameControlManager.setActive(true);

		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

}
