package org.schema.game.client.view.gui.ai;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.PlayerCreatureAISettingsInput;
import org.schema.game.client.controller.PlayerGameTextInput;
import org.schema.game.client.controller.manager.AiConfigurationManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.ai.UnloadedAiEntityException;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.player.CrewFleetRequest;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class AiInterfaceExtendedPanel extends GUIElement implements GUICallback {

	private GameClientState state;

	private AiInterfaceContainer ai;

	private boolean showAdminSettings;

	public AiInterfaceExtendedPanel(InputState state, AiInterfaceContainer f, boolean showAdminSettings) {
		super(state);
		this.showAdminSettings = showAdminSettings;
		this.state = (GameClientState) getState();
		this.ai = f;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SWITCH)*/
			AudioController.fireAudioEventID(319);
			if (AIGameCreatureConfiguration.BEHAVIOR_IDLING.equals(callingGuiElement.getUserPointer())) {
				idle();
			} else if (AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING.equals(callingGuiElement.getUserPointer())) {
				follow();
			} else if (AIGameCreatureConfiguration.BEHAVIOR_ROAMING.equals(callingGuiElement.getUserPointer())) {
				roam();
			} else if (AIGameCreatureConfiguration.BEHAVIOR_ATTACKING.equals(callingGuiElement.getUserPointer())) {
				attack();
			} else if (AIGameCreatureConfiguration.BEHAVIOR_GOTO.equals(callingGuiElement.getUserPointer())) {
				gotoPos();
			} else if ("delete".equals(callingGuiElement.getUserPointer())) {
				delete();
			} else if ("rename".equals(callingGuiElement.getUserPointer())) {
				rename();
			} else if ("settings".equals(callingGuiElement.getUserPointer())) {
				settings();
			}
		}
	}

	private void settings() {
		System.err.println(ai + " settings ");
		try {
			if (ai.getType() == CrewFleetRequest.TYPE_CREW) {
				PlayerCreatureAISettingsInput in = new PlayerCreatureAISettingsInput(state, ai.getAi());
				in.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(320);
			} else {
				((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("Not implemented yet for fleet..."), 0);
			}
		} catch (UnloadedAiEntityException e) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			e.printStackTrace();
		}
	}

	private void rename() {
		final String realName;
		try {
			realName = ai.getRealName();
		} catch (UnloadedAiEntityException e) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			return;
		}
		PlayerGameTextInput playerTextInput = new PlayerGameTextInput("AiInterfaceExtendedPanel_RENAME", (GameClientState) getState(), 32, "Rename", "Enter a new name (must be at least 3 letters)", realName) {

			@Override
			public String[] getCommandPrefixes() {
				return null;
			}

			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}

			@Override
			public void onFailedTextCheck(String msg) {
			}

			@Override
			public void onDeactivate() {
			}

			@Override
			public boolean onInput(String entry) {
				String enteredName = entry.trim();
				if (enteredName.length() < 3) {
					setErrorMessage("Name to short");
					return false;
				}
				System.err.println("[DIALOG] APPLYING AI NAME CHANGE: " + entry + ": from " + realName + " to " + enteredName + "; changed? " + realName.equals(enteredName));
				try {
					if (!realName.equals(enteredName)) {
						if (ai.getAi() instanceof SendableSegmentController) {
							SendableSegmentController c = (SendableSegmentController) ai.getAi();
							System.err.println("[CLIENT] sending name for object: " + c + ": " + enteredName);
							c.getNetworkObject().realName.set(enteredName, true);
							assert (c.getNetworkObject().realName.hasChanged());
							assert (c.getNetworkObject().isChanged());
						} else if (ai.getAi() instanceof AICreature<?>) {
							AICreature<?> c = (AICreature<?>) ai.getAi();
							System.err.println("[CLIENT] sending name for object: " + c + ": " + enteredName);
							c.getNetworkObject().realName.set(enteredName, true);
							assert (c.getNetworkObject().realName.hasChanged());
							assert (c.getNetworkObject().isChanged());
						}
					}
				} catch (UnloadedAiEntityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		};
		playerTextInput.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(321);
	}

	private void delete() {
		System.err.println(ai + " delete ");
		((GameClientState) getState()).getPlayer().getPlayerAiManager().removeAI(ai);
	}

	private void gotoPos() {
		System.err.println(ai + " goto ");
	// try {
	// ((AIGameConfiguration<?, ?>)ai.getAi().getAiConfiguration())
	// .get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_GOTO, true);
	// } catch (StateParameterNotFoundException e) {
	// e.printStackTrace();
	// }
	}

	private void idle() {
		System.err.println(ai + " idle ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_IDLING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			e.printStackTrace();
		}
	}

	private void follow() {
		System.err.println(ai + " follow ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.FOLLOW_TARGET).switchSetting(((GameClientState) getState()).getPlayer().getFirstControlledTransformable().getUniqueIdentifier(), true);
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (PlayerControlledTransformableNotFound e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			e.printStackTrace();
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
		}
	}

	private void roam() {
		System.err.println(ai + " roam ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ROAMING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			e.printStackTrace();
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
		}
	}

	private void attack() {
		System.err.println(ai + " attack ");
		try {
			((AIGameConfiguration<?, ?>) ai.getAi().getAiConfiguration()).get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ATTACKING, true);
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
		} catch (UnloadedAiEntityException e) {
			((GameClientState) getState()).getController().popupAlertTextMessage(Lng.str("AI Entity not loaded!"), 0);
			e.printStackTrace();
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		drawAttached();
	}

	@Override
	public void onInit() {
		GUITextButton idleButton = new GUITextButton(state, 90, 20, Lng.str("Idle"), this, getAIControlManager());
		idleButton.setUserPointer(AIGameCreatureConfiguration.BEHAVIOR_IDLING);
		idleButton.setTextPos(5, 1);
		GUITextButton settingsButton = new GUITextButton(state, 90, 20, Lng.str("Settings"), this, getAIControlManager());
		settingsButton.setUserPointer("settings");
		settingsButton.setTextPos(5, 1);
		GUITextButton followMe = new GUITextButton(state, 90, 20, Lng.str("Follow me"), this, getAIControlManager());
		followMe.setUserPointer(AIGameCreatureConfiguration.BEHAVIOR_FOLLOWING);
		followMe.setTextPos(5, 1);
		GUITextButton rename = new GUITextButton(state, 90, 20, Lng.str("Rename"), this, getAIControlManager());
		rename.setUserPointer("rename");
		rename.setTextPos(5, 1);
		GUITextButton delete = new GUITextButton(state, 80, 20, new Vector4f(0.7f, 0.2f, 0.2f, 0.9f), new Vector4f(0.99f, 0.99f, 0.99f, 1.0f), FontSize.SMALL_15, "Discharge", this, getAIControlManager());
		delete.setUserPointer("delete");
		delete.setTextPos(6, 1);
		followMe.getPos().x = idleButton.getPos().x + 105;
		rename.getPos().x = followMe.getPos().x + 105;
		settingsButton.getPos().x = rename.getPos().x + 105;
		delete.getPos().x = settingsButton.getPos().x + 105;
		GUITextOverlay description = new GUITextOverlay(state);
		description.getPos().y = 35;
		description.setTextSimple(new Object() {

			@Override
			public String toString() {
				try {
					return Lng.str("%s\n(You can quickly control this entity by holding control and pressing the number on the keyboard)", ai.getRealName());
				} catch (UnloadedAiEntityException e) {
					return Lng.str("%s\n(UNLOADED)", ai.getUID());
				}
			}
		});
		setMouseUpdateEnabled(true);
		attach(idleButton);
		attach(followMe);
		attach(settingsButton);
		attach(delete);
		attach(rename);
		attach(description);
		// attach(goTo);
		if (showAdminSettings) {
		// attach(ownerButton);
		}
	}

	@Override
	public float getHeight() {
		return 80;
	}

	@Override
	public float getWidth() {
		return 510;
	}

	public AiConfigurationManager getAIControlManager() {
		return ((GameClientState) getState()).getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getAiConfigurationManager();
	}
}
