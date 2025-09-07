package org.schema.game.client.view.mainmenu;

import api.ModPlayground;
import api.SMModLoader;
import api.common.GameCommon;
import api.mod.ModIdentifier;
import api.mod.ModSkeleton;
import api.mod.SinglePlayerModData;
import api.mod.StarLoader;
import api.utils.GameRestartHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.lwjgl.system.MemoryUtil;
import org.schema.game.client.controller.GUIFadingElement;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.schine.common.InputCharHandler;
import org.schema.schine.common.InputHandler;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.core.settings.ResolutionInterface;
import org.schema.schine.graphicsengine.core.settings.presets.EngineSettingsPreset;
import org.schema.schine.graphicsengine.forms.font.FontStyle;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.BasicInputController;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.mixer.AudioMixer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import static org.schema.schine.graphicsengine.core.settings.EngineSettings.write;

public abstract class DialogInput implements GUICallback, InputHandler, InputCharHandler, DialogInterface, GUIActiveInterface {

	public static final long drawDeactivatedTime = 200;

	private static long lastDialougeClick;

	private long deactivationTime;

	private final InputState state;

	private boolean deactivateOnEscape = true;

	private boolean inBackground;

	public String queueDeactivate;

	public DialogInput(InputState state) {
		this.state = state;
		initialize();
	}

	protected void initialize() {
	}

	@Override
	public void update(Timer timer) {
		if(queueDeactivate != null) {
			((GameClientState) getState()).getController().popupAlertTextMessage(queueDeactivate, 0);
			queueDeactivate = null;
			deactivate();
		}
	}

	@Override
	public boolean allowChat() {
		return false;
	}

	public static boolean isDelayedFromMainMenuDeactivation() {
		return (System.currentTimeMillis() - lastDialougeClick < 300);
	}

	private float secs = 25;

	private long activationTime;
	private boolean screenChanged = false;

	private void applySettings() {

		try {
			Set<EngineSettings> changed = new ObjectOpenHashSet<>();
			this.screenChanged = false;
			EngineSettingsChangeListener li = setting -> {
				EngineSettings s = (EngineSettings) setting;
				switch(s) {
					case G_RESOLUTION:
					case G_VSYNCH:
					case G_FULLSCREEN:
						System.err.println("[SETTINGS] ScreenSetting Changed by user");
						screenChanged = true;
						break;
					case F_BLOOM:
						if(EngineSettings.F_BLOOM.isOn() && !GraphicsContext.CAN_USE_FRAMEBUFFER) {
							EngineSettings.F_BLOOM.setOn(false);
							throw new RuntimeException("Error: Frame Buffer needed for this effect is not supported on this graohcs card\n\nIf you're running a dual graphics card setup (Intel + ATI/NVidia), please check if the process java and javaw are set to use the better graphics card.");
						}
						break;
					case G_USE_VERTEX_LIGHTING_ONLY:
						ShaderLibrary.reCompile(true);
						break;
					case G_TEXTURE_PACK:
					case G_TEXTURE_PACK_RESOLUTION:
						applyTexturePack();
						break;
					case G_NORMAL_MAPPING:
						Controller.getResLoader().enqueueWithReset(GameResourceLoader.getBlockTextureResourceLoadEntry());
						ShaderLibrary.reCompile(true);
						break;
					case G_WINDOWED_BORDERLESS:
						GraphicsContext.current.invokeScreenChanged();
						break;
					case G_SHADOW_QUALITY:
						if(EngineSettings.isShadowOn() && !GraphicsContext.CAN_USE_FRAMEBUFFER) {
							EngineSettings.setShadowOn(false);
							throw new RuntimeException("Error: Frame Buffer needed for this effect is not supported on this graohcs card\n\nIf you're running a dual graphics card setup (Intel + ATI/NVidia), please check if the process java and javaw are set to use the better graphics card.");
						} else {
							if(!EngineSettings.F_FRAME_BUFFER.isOn() && EngineSettings.isShadowOn()) {
								EngineSettings.F_FRAME_BUFFER.setOn(true);
							}
//                            ShaderLibrary.reCompile(true); Todo: Causes a GL Invalid Value Error
							GraphicsContext.current.invokeScreenChanged();
						}
						break;
					case G_PROD_BG:
					case G_PROD_BG_QUALITY:
						if(EngineSettings.G_PROD_BG.isOn() && !GraphicsContext.CAN_USE_FRAMEBUFFER) {
							EngineSettings.G_PROD_BG.setOn(false);
							throw new RuntimeException("Error: Frame Buffer needed for this effect is not supported on this graohcs card\n\nIf you're running a dual graphics card setup (Intel + ATI/NVidia), please check if the process java and javaw are set to use the better graphics card.");
						} else {
							if(getState() instanceof GameClientState && ((GameClientState) getState()).getWorldDrawer() != null) {
								((GameClientState) getState()).getWorldDrawer().getStarSky().reset();
							}
							ShaderLibrary.reCompile(true);
						}
						break;
					case AUDIO_MIXER_MASTER:
						AudioMixer.MASTER.setVolume(setting.getFloat());
						if(AudioController.instance.isMusicPlaying()) AudioController.instance.getMusicPlaying().setVolume(setting.getFloat());
						break;
					case AUDIO_MIXER_MUSIC:
						AudioMixer.MUSIC.setVolume(setting.getFloat());
						if(AudioController.instance.isMusicPlaying()) AudioController.instance.getMusicPlaying().setVolume(setting.getFloat());
						break;
					case AUDIO_MIXER_SFX:
						AudioMixer.SFX.setVolume(setting.getFloat());
						break;
					case AUDIO_MIXER_SFX_GUI:
						AudioMixer.GUI.setVolume(setting.getFloat());
						break;
					case AUDIO_MIXER_SFX_INGAME:
						AudioMixer.GAME.setVolume(setting.getFloat());
						break;
					default:
						break;
				}
			};

			for(EngineSettings s : EngineSettings.dirtyTmpSettings) {
				s.addChangeListener(li);
				s.applyFromTmp();
				s.removeChangeListener(li);
			}
			if(EngineSettings.dirtyTmpSettings.contains(EngineSettings.GRAPHICS_PRESET)) {
				((EngineSettingsPreset) EngineSettings.GRAPHICS_PRESET.getObject()).apply();
			}
			if(screenChanged) {
				applyScreenSettings();
				PlayerOkCancelInput p = (new PlayerOkCancelInput("RRESCONFIRM", getState(), UIScale.getUIScale().scale(540), UIScale.getUIScale().scale(340), Lng.str("CONFIRM"), new Object() {

					@Override
					public String toString() {
						return Lng.str("Screen settings changed. Keep?\n(Will automatically revert back in %s seconds)\n" + "NOTICE FOR 4k MONITOR OWNERS ON WINDOWS10: There is a scaling issue in fullscreen due\n" + "to Windows DPI scaling. Until a thirdparty library update can resolve this automatically,\n" + "there is a workaround:\n" + "1. For steam: Right click on StarMade -> properties -> local files Tab -> Browse local files button" + "2. navigate to the dep\\java\\jre1.7.0_80\\bin folder\n" + "3. here, right click on \"javaw.exe\" (must have the w on the end), and go to properties.\n" + "4. Go to the \"compatibility\" tab and check the \"override high DPI scaling behavior.\" checkbox and Select \"System\" in the dropdown.", (int) Math.ceil(secs));
					}
				}, FontStyle.big) {

					@Override
					public void pressedOK() {
						deactivate();
					}

					@Override
					public void update(Timer timer) {
						if(secs > 0) {
							secs -= timer.getDelta();
							// System.err
							// .println(timer.getDelta()+" ----d-d- "+secs);
						} else {
							//cancel();
							deactivate();
						}
					}

					@Override
					public void onDeactivate() {
					}

					@Override
					public void cancel() {
						EngineSettings.G_RESOLUTION.revert();
						EngineSettings.G_VSYNCH.revert();
						EngineSettings.G_FULLSCREEN.revert();
						EngineSettings.G_MULTI_SAMPLE.revert();

						applyScreenSettings();

						deactivate();
					}
				});
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(741);
			}

			write();
			EngineSettings.dirtyTmpSettings.clear();
			EngineSettings.dirty = false;
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void applyScreenSettings() {
		OpenGLWindowParams param = GraphicsContext.current.getParams();
		param.changed = true;
		param.fullscreen = EngineSettings.G_FULLSCREEN.isOn();
		param.resizable = !EngineSettings.G_FULLSCREEN.isOn();
		param.share = MemoryUtil.NULL;
		assert (EngineSettings.G_RESOLUTION.getObject() != null);
		param.res = (ResolutionInterface) EngineSettings.G_RESOLUTION.getObject();
		GraphicsContext.current.invokeScreenChanged();
	}

	public void applyGameSettings(DialogInput from, boolean deactivateFromDialog) {
		if(EngineSettings.dirty || KeyboardMappings.dirty) {
			if(EngineSettings.needsRestart) {
				PlayerOkCancelInput input = new PlayerOkCancelInput("CONFIRM RESTART", getState(), UIScale.getUIScale().scale(340), UIScale.getUIScale().scale(140), Lng.str("CONFIRM RESTART"), Lng.str("The game must be restarted to apply these changes. Apply?")) {
					@Override
					public void onDeactivate() {

					}

					@Override
					public void pressedOK() {
						try {
							write();
							if(GameCommon.isClientConnectedToServer() || GameCommon.isOnSinglePlayer()) {
								//We are in-game, we need to rejoin the server after restarting
								String connectionString = GameCommon.getUniqueContextId();
								String serverIP = connectionString.split("~")[0];
								int port = 4242;
								if(connectionString.contains("~")) port = Integer.parseInt(connectionString.split("~")[1]);
								ArrayList<Integer> mods = getEnabledMods();
								GameRestartHelper.runWithUplink(serverIP, port, mods);
							} else {
								//Not in-game, just restart to main menu
								GameRestartHelper.runWithArguments(SMModLoader.uplinkArgs);
							}
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}

					@Override
					public void cancel() {
						try {
							System.err.println("[ENGINESETTINGS] REVERTING SETTINGS");
							EngineSettings.read();
							KeyboardMappings.read();
							super.cancel();
						} catch(IOException exception) {
							exception.printStackTrace();
						}
					}

					private ArrayList<Integer> getEnabledMods() {
						ArrayList<Integer> mods = new ArrayList<>();
						if(SMModLoader.shouldUplink || SMModLoader.runningAsServer) {
							for(Integer modId : SMModLoader.uplinkMods) {
								if(modId > 0) mods.add(modId);
							}
						} else {
							for(ModSkeleton mod : StarLoader.starMods) {
								if(SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod)) && !(mod.getRealMod() instanceof ModPlayground)) mods.add(mod.getSmdResourceId());
							}
						}
						return mods;
					}
				};
				input.getInputPanel().setOkButtonText(Lng.str("CONFIRM RESTART"));
				input.activate();
				AudioController.fireAudioEventID(742);
			} else {
				PlayerOkCancelInput p = (new PlayerOkCancelInput("CONFIRM", getState(), UIScale.getUIScale().scale(340), UIScale.getUIScale().scale(140), Lng.str("CONFIRM"), Lng.str("Some settings have changed. Apply?")) {

					@Override
					public void pressedOK() {
						applySettings();
						try {
							System.err.println("[CLIENT][CONFIG] WRITING JOYSTICK CONFIG");
							getState().getController().getInputController().getJoystick().write();
							System.err.println("[CLIENT][CONFIG] WRITING KEYBOARD CONFIG");
							KeyboardMappings.write();
							if(getState() instanceof GameClientState) {
								((GameClientState) getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getHelpPanel().updateAll(getState());
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
						deactivate();
						if(deactivateFromDialog) {
							from.deactivate();
						}
					}

					@Override
					public void onDeactivate() {
						EngineSettings.dirtyTmpSettings.clear();
						EngineSettings.dirty = false;
						KeyboardMappings.dirty = false;
					}

					@Override
					public void cancel() {
						try {
							System.err.println("[ENGINESETTINGS] REVERTING SETTINGS");
							EngineSettings.read();
							KeyboardMappings.read();
							super.cancel();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				});
				p.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(742);
			}
		} else {
			if(deactivateFromDialog) {
				from.deactivate();
			}
		}
	}

	private void applyTexturePack() {
		final TexturePack pack = (TexturePack) EngineSettings.G_TEXTURE_PACK.getObject();
		if(pack.name.toLowerCase(Locale.ENGLISH).equals("pixel")) {
			PlayerOkCancelInput pl = (new PlayerOkCancelInput("CONFIRM", getState(), 340, 140, Lng.str("Texture Pack"), new Object() {

				@Override
				public String toString() {
					return Lng.str("Warning: This texture pack might not always be updated,\nso some blocks could appear transparent or not textured.");
				}
			}) {

				@Override
				public void onDeactivate() {
					applyTexturePackChecked(pack);
				}

				@Override
				public void pressedOK() {
					deactivate();
				}
			});
			pl.getInputPanel().setCancelButton(false);
			pl.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(744);
		} else if(!pack.name.toLowerCase(Locale.ENGLISH).equals("default")) {
			PlayerOkCancelInput pl = (new PlayerOkCancelInput("CONFIRM", getState(), 340, 140, Lng.str("Texture Pack"), new Object() {

				@Override
				public String toString() {
					return Lng.str("Warning: This texture pack is not officially supported.\nSome blocks could appear transparent or not textured.");
				}
			}) {

				@Override
				public void onDeactivate() {
					applyTexturePackChecked(pack);
				}

				@Override
				public void pressedOK() {
					deactivate();
				}
			});
			pl.getInputPanel().setCancelButton(false);
			pl.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(743);
		} else {
			applyTexturePackChecked(pack);
		}
	}

	private void applyTexturePackChecked(final TexturePack pack) {
		int res = EngineSettings.G_TEXTURE_PACK_RESOLUTION.getInt();
		boolean exists = false;
		for(int r : pack.resolutions) {
			if(res == r) {
				exists = true;
			}
		}
		if(!exists) {
			PlayerOkCancelInput pl = (new PlayerOkCancelInput("CONFIRM", getState(), 340, 140, Lng.str("Resolution"), new Object() {

				@Override
				public String toString() {
					return Lng.str("Selected resolution is not available for this texture pack.\nSwitching to default resolution.");
				}
			}) {

				@Override
				public void onDeactivate() {
					EngineSettings.G_TEXTURE_PACK_RESOLUTION.setObject(pack.resolutions[pack.resolutions.length - 1]);
					Controller.getResLoader().enqueueWithResetForced(GameResourceLoader.getBlockTextureResourceLoadEntry());
				}

				@Override
				public void pressedOK() {
					deactivate();
				}
			});
			pl.getInputPanel().setCancelButton(false);
			pl.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(745);
		} else {
			Controller.getResLoader().enqueueWithResetForced(GameResourceLoader.getBlockTextureResourceLoadEntry());
		}
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(callingGuiElement.getUserPointer() != null && !callingGuiElement.wasInside() && callingGuiElement.isInside()) {
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.HOVER)*/
			AudioController.fireAudioEventID(746);
		}
		if(event.pressedLeftMouse() && callingGuiElement.getUserPointer() != null) {
			if(callingGuiElement.getUserPointer().equals("OK")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
				AudioController.fireAudioEventID(749);
				deactivate();
				apply();
			} else if(callingGuiElement.getUserPointer().equals("CANCEL")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(748);
				deactivate();
			} else if(callingGuiElement.getUserPointer().equals("X")) {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
				AudioController.fireAudioEventID(747);
				deactivate();
			} else {
				assert (false) : "not known command: '" + callingGuiElement.getUserPointer() + "'";
			}
		}
	}

	protected void apply() {
	}

	public abstract void onDeactivate();

	@Override
	public void deactivate() {
		boolean remove = state.getController().getInputController().getPlayerInputs().remove(this);
		if(remove && getInputPanel() instanceof GUIFadingElement) {
			state.getController().getInputController().getDeactivatedPlayerInputs().add(this);
		} else {
			if(!inBackground) {
				getInputPanel().cleanUp();
			}
		}
		if(!remove) {
			System.err.println("[CLIENT][PlayerInput] not found: " + this + " to deactivate: " + state.getController().getPlayerInputs());
		} else {
			System.err.println("[CLIENT][PlayerInput] successfully deactivated " + this);
			BasicInputController.grabbedObjectLeftMouse = this;
		}
		onDeactivate();
		deactivationTime = System.currentTimeMillis();
		if(remove) {
			state.getController().getInputController().setLastDeactivatedMenu(deactivationTime);
		}
		lastDialougeClick = System.currentTimeMillis();
	}

	@Override
	public boolean isActive() {
		return isFocused();
	}

	public void activate() {
		assert (getInputPanel() != null);
		// the last of list is active
		// if is already in list, put as last
		this.activationTime = getState().getUpdateTime();
		deactivationTime = 0;
		state.getController().getInputController().getPlayerInputs().remove(this);
		state.getController().getInputController().getPlayerInputs().add(this);
	}

	public boolean isSameUpdate() {
		return this.activationTime == getState().getUpdateTime();
	}

	public void cancel() {
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
		AudioController.fireAudioEventID(750);
		deactivate();
	}

	@Override
	public boolean checkDeactivated() {
		return false;
	}

	@Override
	public long getDeactivationTime() {
		return deactivationTime;
	}

	/**
	 * @return the deactivateOnEscape
	 */
	public boolean isDeactivateOnEscape() {
		return deactivateOnEscape;
	}

	@Override
	public void updateDeacivated() {
		if(getInputPanel() instanceof GUIFadingElement) {
			float fade = 0;
			if(deactivationTime > 0) {
				float timeDeactivated = System.currentTimeMillis() - deactivationTime;
				if(timeDeactivated > drawDeactivatedTime) {
					fade = 1;
				} else {
					fade = timeDeactivated / drawDeactivatedTime;
				}
			}
			// System.err.println("FADE: "+fade);
			((GUIFadingElement) getInputPanel()).setFade(fade);
		}
	}

	/**
	 * @param deactivateOnEscape the deactivateOnEscape to set
	 */
	public void setDeactivateOnEscape(boolean deactivateOnEscape) {
		this.deactivateOnEscape = deactivateOnEscape;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		if(isDeactivateOnEscape() && e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
			deactivate();
		}
	}

	@Override
	public void handleCharEvent(KeyEventInterface e) {
	}

	public boolean isFocused() {
		return !state.getController().getPlayerInputs().isEmpty() && state.getController().getPlayerInputs().get(state.getController().getPlayerInputs().size() - 1) == this;
	}

	@Override
	public boolean isOccluded() {
		return !(state.getController().getPlayerInputs().isEmpty() || state.getController().getPlayerInputs().get(state.getController().getPlayerInputs().size() - 1) == this);
	}

	public InputState getState() {
		return state;
	}

	public boolean isInBackground() {
		return inBackground;
	}

	public void setInBackground(boolean inBackground) {
		this.inBackground = inBackground;
	}
}
