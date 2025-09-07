package org.schema.game.client.controller.manager.ingame;

import java.io.File;

import org.lwjgl.glfw.GLFW;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.PlayerLagStatsInput;
import org.schema.game.client.controller.PlayerMessageLogPlayerInput;
import org.schema.game.client.controller.PlayerMessagesPlayerInput;
import org.schema.game.client.controller.PlayerNetworkStatsInput;
import org.schema.game.client.controller.PlayerNotYetInitializedException;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.MessageLogManager;
import org.schema.game.client.controller.manager.PlayerMailManager;
import org.schema.game.client.controller.manager.freemode.AutoRoamController;
import org.schema.game.client.controller.manager.freemode.FreeRoamController;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.ResolutionInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.util.GifEncoder;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.FileExt;
import org.schema.schine.sound.controller.AudioController;

public class InGameControlManager extends AbstractControlManager {

	public static final int PLAYER_MESSAGE_LOG_KEY = GLFW.GLFW_KEY_F4;

	private ChatControlManager chatControlManager;

	private FreeRoamController freeRoamController;

	private AutoRoamController autoRoamController;

	private MessageLogManager messageLogManager;

	private PlayerMailManager playerMailManager;

	private PlayerGameControlManager playerGameControlManager;

	public InGameControlManager(GameClientState state) {
		super(state);
		initialize();
	}

	/**
	 * @return the autoRoamController
	 */
	public AutoRoamController getAutoRoamController() {
		return autoRoamController;
	}

	/**
	 * @param autoRoamController the autoRoamController to set
	 */
	public void setAutoRoamController(AutoRoamController autoRoamController) {
		this.autoRoamController = autoRoamController;
	}

	/**
	 * @return the chatControlManager
	 */
	public ChatControlManager getChatControlManager() {
		return chatControlManager;
	}

	public FreeRoamController getFreeRoamController() {
		return freeRoamController;
	}

	public PlayerGameControlManager getPlayerGameControlManager() {
		return playerGameControlManager;
	}

	public void activateMesssageLog() {
		boolean act = messageLogManager.isActive();
		messageLogManager.setActive(!act);
		if (!messageLogManager.isActive()) {
			for (int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
				DialogInterface p = getState().getController().getPlayerInputs().get(i);
				if (p instanceof PlayerMessageLogPlayerInput) {
					getState().getController().getPlayerInputs().get(i).deactivate();
					break;
				}
			}
		}
	}

	public void activatePlayerMesssages() {
		boolean act = playerMailManager.isActive();
		playerMailManager.setActive(!act);
		if (!playerMailManager.isActive()) {
			for (int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
				DialogInterface p = getState().getController().getPlayerInputs().get(i);
				if (p instanceof PlayerMessagesPlayerInput) {
					getState().getController().getPlayerInputs().get(i).deactivate();
					break;
				}
			}
		}
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		boolean chatActive = chatControlManager.isActive();
		// System.err.println("CHAT ACK:: "+chatActive);
		/*
		 * update dialogs
		 */
		boolean hasInputPanels = !getState().getPlayerInputs().isEmpty();
		if (getState().getController().getPlayerInputs().size() == 1) {
			if (getState().getController().getPlayerInputs().get(0).allowChat()) {
				if (e.isTriggered(KeyboardMappings.CHAT)) {
					if (!chatActive) {
						// if(getState().getController().getPlayerInputs().get(0) instanceof PlayerMessageLogPlayerInput){
						// getState().getChat().getTextInput().append(((PlayerMessageLogPlayerInput)getState().getController().getPlayerInputs().get(0)).getCurrentChatPrefix());
						// }
						chatControlManager.setDelayedActive(true);
					} else {
						// make that last enter count in the chat controller
						// chatControlManager.handleKeyEvent();
						if (EngineSettings.CHAT_CLOSE_ON_ENTER.isOn()) {
							chatControlManager.setDelayedActive(false);
						}
					}
				} else if (e.isTriggered(KeyboardMappings.MAIN_MENU)) {
					// if(messageLogManager.isActive()){
					// activateMesssageLog();
					// }
					// if(playerMailManager.isActive()){
					// activatePlayerMesssages();
					// }
					if (chatActive) {
						chatControlManager.setDelayedActive(false);
					}
				} else {
					if (chatActive) {
						chatControlManager.handleKeyEvent(e);
					}
				}
			}
			if (e.isTriggered(KeyboardMappings.PLAYER_MESSAGE_LOG_KEY) && !getState().isDebugKeyDown()) {
				activatePlayerMesssages();
			}
		}
		if (hasInputPanels) {
			return;
		}
		if (e.isTriggered(KeyboardMappings.NETWORK_STATS_PANEL)) {
			if (!getState().isDebugKeyDown() && getState().getController().getPlayerInputs().isEmpty()) {
				PlayerNetworkStatsInput p = new PlayerNetworkStatsInput(getState());
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(139);
			}
		}
		if (e.isTriggered(KeyboardMappings.LAG_STATS_PANEL)) {
			if (!getState().isDebugKeyDown() && getState().getController().getPlayerInputs().isEmpty()) {
				if (getState().isAdmin()) {
					PlayerLagStatsInput p = new PlayerLagStatsInput(getState());
					p.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(140);
				} else {
					getState().getController().popupAlertTextMessage(Lng.str("Network Statistics only allowed for admins!"), 0);
				}
			}
		}
		if (e.isTriggered(KeyboardMappings.RECORD_GIF)) {
			if (getState().getWorldDrawer().gifEncoder == null) {
				int fps = Integer.parseInt(EngineSettings.GIF_FPS.getString());
				getState().getWorldDrawer().gifEncoder = new GifEncoder();
				getState().getWorldDrawer().gifEncoder.setFrameRate(fps);
				getState().getWorldDrawer().gifEncoder.setRepeat(0);
				ResolutionInterface res = (ResolutionInterface) EngineSettings.GIF_RESOLUTION.getObject();
				float imgWidth =  res.getWidth();
				float imgHeight =  res.getHeight();
				float ratio = (float) GLFrame.getHeight() / GLFrame.getWidth();
				imgHeight = ratio * imgHeight;
				getState().getWorldDrawer().gifEncoder.setSize((int) imgWidth, (int) imgHeight);
				GraphicsContext.limitFPS = fps;
				File f = new FileExt("./screenshots/");
				if(!f.exists()) f.mkdirs();
				File[] listFiles = f.listFiles();
				int nr = 0;
				boolean through = true;
				while (through) {
					through = false;
					for (File en : listFiles) {
						if (en.getName().startsWith("starmade-gif-" + StringTools.formatFourZero(nr) + ".gif")) {
							System.err.println("Screen Already Exists: ./starmade-gif-" + StringTools.formatFourZero(nr) + ".gif");
							nr++;
							through = true;
							break;
						}
					}
				}
				String fileName = "./screenshots/starmade-gif-" + StringTools.formatFourZero(nr) + ".gif";
				System.err.println("Recording GIF: " + fileName);
				getState().getWorldDrawer().gifEncoder.start(fileName);
			} else {
				System.err.println("STOPPING GIF RECORDING!");
				getState().getWorldDrawer().gifEncoder.finish();
				getState().getWorldDrawer().gifEncoder = null;
				GraphicsContext.limitFPS = null;
			}
		} else if (e.isTriggered(KeyboardMappings.PLAYER_MESSAGE_LOG_KEY) && !e.isDebugKey()) {
			activatePlayerMesssages();
		} else if (e.isTriggered(KeyboardMappings.CHAT)) {
			if (!chatActive) {
				chatControlManager.setDelayedActive(true);
			} else {
				// make that last enter count in the chat controller
				if (EngineSettings.CHAT_CLOSE_ON_ENTER.isOn()) {
					chatControlManager.setDelayedActive(false);
				}
			}
		} else if (e.isDebugKey() && e.getKey() == GLFW.GLFW_KEY_F11) {
			System.err.println("[CLIENT] F11 Pressed: lwjgl event key: " + e.getKey());
			if (!chatActive) {
				boolean freeRoamActive = freeRoamController.isActive();
				if (!freeRoamActive && !getState().getController().getPlayerInputs().isEmpty()) {
					// no freeroam mode in dialog mode
					return;
				}
				if (freeRoamActive && getState().getCharacter() == null) {
					System.out.println("no player character: spawning");
					try {
						getState().getController().spawnAndActivatePlayerCharacter();
					} catch (PlayerNotYetInitializedException ex) {
						ex.printStackTrace();
						getState().getController().popupAlertTextMessage("Player not yet initialized", 0);
					}
				} else {
					playerGameControlManager.setActive(freeRoamActive);
					freeRoamController.setActive(!freeRoamActive);
				}
				if (autoRoamController.isActive() && !freeRoamActive) {
					autoRoamController.setActive(false);
					freeRoamController.setActive(true);
				}
				System.err.println("FREEROAM: "+ freeRoamController.isActive());
			}
		}
		/*
		 * handle childs here
		 * do not handle them on top
		 * because the last open
		 * dialog getting 'entered'
		 * will cause the chat to pop up
		 */
		super.handleKeyEvent(e);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		if (!getState().getController().getPlayerInputs().isEmpty()) {
			CameraMouseState.setGrabbed(false);
		}
	}

	public void initialize() {
		chatControlManager = new ChatControlManager(getState());
		freeRoamController = new FreeRoamController(getState());
		autoRoamController = new AutoRoamController(getState());
		playerGameControlManager = new PlayerGameControlManager(getState());
		messageLogManager = new MessageLogManager(getState());
		playerMailManager = new PlayerMailManager(getState());
		getControlManagers().add(playerMailManager);
		getControlManagers().add(messageLogManager);
		getControlManagers().add(chatControlManager);
		getControlManagers().add(freeRoamController);
		getControlManagers().add(autoRoamController);
		getControlManagers().add(playerGameControlManager);
		autoRoamController.setActive(true);
	}

	public boolean isInBuildMode() {
		return playerGameControlManager.getPlayerIntercationManager().getSegmentControlManager().isTreeActive() || playerGameControlManager.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive();
	}

	/**
	 * @return the messageLogManager
	 */
	public MessageLogManager getMessageLogManager() {
		return messageLogManager;
	}

	/**
	 * @return the playerMessageManager
	 */
	public PlayerMailManager getPlayerMailManager() {
		return playerMailManager;
	}

	public boolean isAnyMenuOrChatActive() {
		return playerMailManager.isActive() || messageLogManager.isActive() || chatControlManager.isActive() || playerGameControlManager.isAnyMenuActive();
	}
}
