package org.schema.game.client.view.mainmenu;

import api.mod.gui.ModManagerDialogMainMenu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.view.gui.advancedbuildmode.AdvancedBuildModeTest;
import org.schema.game.client.view.mainmenu.gui.catalogmanager.CatalogManagerDialogMainMenu;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton.ColorPalette;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.BasicInputController;
import org.schema.schine.sound.controller.AudioController;

import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.List;

public class MainMenuGUI extends GUIAnchor {

	public MainMenuGUI(GameMainMenuController state) {
		super(state);
	}

	private boolean init;

	private GUIColoredRectangle mainPanelBackground;

	private final List<GUIElement> buttons = new ObjectArrayList<GUIElement>();

	private final int buttonHeight = 38;

	private final int buttonSeperation = 8;

	public MainMenuOptionsMenu gameMenu;

	@Override
	public GameMainMenuController getState() {
		return (GameMainMenuController) super.getState();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
		mainPanelBackground.cleanUp();
		for (GUIElement b : buttons) {
			b.cleanUp();
		}
	}

	AdvancedBuildModeTest test;

	public static boolean runningSwingDialog = false;

	/**
	 * Buttons are not usable if either a main menu dialog is open or
	 * another dialog type (input)
	 * @return true if the buttons can be used and do not overlap with a dialog
	 */
	public boolean areButtonsUsable() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		boolean allMenuInput = true;
		for (DialogInterface d : playerInputs) {
			if (!(d instanceof MainMenuInputDialogInterface) || ((MainMenuInputDialogInterface) d).isInside()) {
				return false;
			}
		}
		return !runningSwingDialog;
	}

	private void deactivateAllDialogs() {
		List<DialogInterface> playerInputs = getState().getController().getInputController().getPlayerInputs();
		for (DialogInterface d : playerInputs) d.deactivate();
	}

	@Override
	public void onInit() {
		super.onInit();
		// Mouse.poll();
		// while(Mouse.next()); //consume all
		// if(!Version.isDev()){
		// Keyboard.poll();
		// while(Keyboard.next());
		// }
		test = new AdvancedBuildModeTest(getState());
		if (getState().hasLastUsed()) {
			addButton(Lng.str("CONTINUE LAST PLAY"), new GUICallback() {

				@Override
				public boolean isOccluded() {
					return !areButtonsUsable();
				}

				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if (event.pressedLeftMouse()) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(849);
						deactivateAllDialogs();
						AudioController.instance.stopMusic();
						getState().startLastUsed();
					}
				}
			}, new Object() {

				@Override
				public String toString() {
					String s = EngineSettings.LAST_GAME.getString().trim();
					if (s.length() == 0) {
						return "";
					} else {
						try {
							String[] split = EngineSettings.LAST_GAME.getString().trim().split(";");
							boolean singlePlayer = split[0].equals("SP");
							String worldOrHost = split[1];
							int port = Integer.parseInt(split[2]);
							String playerName = split[3];
							if (singlePlayer) {
								return Lng.str("Local Universe '%s' as '%s'", worldOrHost, playerName);
							} else {
								return Lng.str("Online Universe '%s:%s' as '%s'", worldOrHost, port, playerName);
							}
						} catch (Exception e) {
							e.printStackTrace();
							EngineSettings.LAST_GAME.setString("");
							try {
								EngineSettings.write();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						return "";
					}
				}
			});
		}
		addButton(Lng.str("LOCAL PLAY"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					(new LocalUniverseDialog(getState())).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(850);
				}
			}
		}, null);
		addButton(Lng.str("ONLINE PLAY"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					(new OnlineUniverseDialog(getState())).activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(851);
				}
			}
		}, null);
		// addButton("MOVIE TEST", new GUICallback() {
		// @Override
		// public boolean isOccluded() {
		// return !areButtonsUsable();
		// }
		//
		// @Override
		// public void callback(GUIElement callingGuiElement, MouseEvent event) {
		// if(event.pressedLeftMouse()){
		// deactivateAllDialogs();
		// try {
		// MovieDialog movieDialog = new MovieDialog(getState(), "MOVIE", 500, 500, 400, 100, new FileExt("./data/video/SchineSplashScreen.mp4"));
		// movieDialog.setLooping(true);
		// movieDialog.getInputPanel().setPos(400, 100, 0);
		// movieDialog.activate(); AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }, null);
		addButton(Lng.str("LANGUAGE"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					popupLanguageDialog();
				}
			}
		}, null);
		addButton(Lng.str("OPTIONS"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					try {
						EngineSettings.read();
					} catch(IOException exception) {
						exception.printStackTrace();
						EngineSettings.writeDefault();
					}
					gameMenu = new MainMenuOptionsMenu(getState());
					gameMenu.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(852);
				}
			}
		}, null);
		addButton(Lng.str("TOOLS"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					popupToolsAndModsDialog();
				}
			}
		}, null);
		//INSERTED CODE @268
		this.addButton("CATALOG", new GUICallback() {
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			public void callback(GUIElement var1, MouseEvent var2) {
				if (var2.pressedLeftMouse()) {
					deactivateAllDialogs();
					CatalogManagerDialogMainMenu catWindow = new CatalogManagerDialogMainMenu(getState());
					catWindow.activate();
				}

			}
		}, (Object)null);
		this.addButton("MODS", new GUICallback() {
			public boolean isOccluded() {
				return !MainMenuGUI.this.areButtonsUsable();
			}

			public void callback(GUIElement var1, MouseEvent var2) {
				if (var2.pressedLeftMouse()) {
					MainMenuGUI.this.deactivateAllDialogs();
					ModManagerDialogMainMenu modWindow = new ModManagerDialogMainMenu(getState());
					modWindow.activate();
				}

			}
		}, (Object)null);

		///
		addButton(Lng.str("CREDITS"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					deactivateAllDialogs();
					popupCreditsDialog();
				}
			}
		}, null);
		addButton(Lng.str("EXIT"), new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !areButtonsUsable();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(853);
					GLFrame.setFinished(true);
				}
			}
		}, null);
		mainPanelBackground = new GUIColoredRectangle(getState(), 335, 2 * buttonSeperation + (buttons.size()) * (buttonHeight + buttonSeperation), new Vector4f(0.3f, 0.3f, 0.4f, 0.4f));
		mainPanelBackground.rounded = 4;
		for (GUIElement e : buttons) {
			mainPanelBackground.attach(e);
		}
		init = true;
	}

	private void popupToolsAndModsDialog() {
		ToolsAndModsDialog d = new ToolsAndModsDialog(getState());
		d.getInputPanel().onInit();
		d.getInputPanel().background.setPos(470, 35, 0);
		d.getInputPanel().background.setWidth(GLFrame.getWidth() - 435);
		d.getInputPanel().background.setHeight(GLFrame.getHeight() - 70);
		d.addToolsAndModsButtons();
		d.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(854);
	}

	private void popupCreditsDialog() {
		PlayerOkCancelInput d = new PlayerOkCancelInput("Credits", getState(), 600, 800, Lng.str("StarMade Credits"), getCredits()) {

			@Override
			public void onDeactivate() {
			}

			@Override
			public void pressedOK() {
				deactivate();
			}
		};
		d.getInputPanel().setCancelButton(false);
		d.getInputPanel().onInit();
		d.getInputPanel().background.setPos(470, 35, 0);
		d.getInputPanel().background.setWidth(GLFrame.getWidth() - 435);
		d.getInputPanel().background.setHeight(GLFrame.getHeight() - 70);
		d.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(855);
	}

	private void popupLanguageDialog() {
		LanguageDialog d = new LanguageDialog(getState());
		d.getInputPanel().onInit();
		d.getInputPanel().background.setPos(470, 35, 0);
		d.getInputPanel().background.setWidth(GLFrame.getWidth() - 435);
		d.getInputPanel().background.setHeight(GLFrame.getHeight() - 70);
		d.addToolsAndModsButtons();
		d.activate();
		/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
		AudioController.fireAudioEventID(856);
	}

	private void addButton(String text, GUICallback callback, Object toolTip) {
		GUITextButton b = new GUITextButton(getState(), 400, UIScale.getUIScale().scale(buttonHeight), ColorPalette.TRANSPARENT, FontSize.BIG_30, text, callback);
		b.setTextPos(10, 4);
		b.setPos(0, UIScale.getUIScale().scale(buttonSeperation) + buttons.size() * (UIScale.getUIScale().scale(buttonHeight) + UIScale.getUIScale().scale(buttonSeperation)), 0);
		if (toolTip != null) {
			if (EngineSettings.DRAW_TOOL_TIPS.isOn()) {
				b.setToolTip(new GUIToolTip(getState(), toolTip, b));
			}
		}
		buttons.add(b);
	}

	@Override
	public void draw() {
		super.draw();
		if (!init) {
			onInit();
		}

		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		/*
		 * WARNING: GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT) is a dangerous call.
		 * But it's needed to draw the gui on top of everything. Make sure that
		 * nothing is drawn afterwards that needs to reference the cleared depth
		 * buffer
		 */
		GlUtil.glDepthMask(false);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		drawMenu();
		drawInputs();
		getState().getController().getInputController().getGuiCallbackController().drawToolTips();
		GlUtil.glDepthMask(true);
		GlUtil.setColorMask(true);
	}

	private void drawMenu() {
		GUIElement.enableOrthogonal();
		GlUtil.glPushMatrix();
		mainPanelBackground.setPos(80, GLFrame.getHeight() - (mainPanelBackground.getHeight() + GLFrame.getHeight() / 6), 0);
		mainPanelBackground.draw();
		// test.draw();
		GlUtil.glPopMatrix();
		GUIElement.disableOrthogonal();
	}

	private void drawInputs() {
		GUIElement.enableOrthogonal();
		GlUtil.glPushMatrix();
		BasicInputController ic = getState().getController().getInputController();
		List<DialogInterface> playerInputs = ic.getPlayerInputs();
		for(DialogInterface playerInput : playerInputs) {
			playerInput.getInputPanel().draw();
			if(ic.getCurrentActiveDropdown() != null) {
				ic.getCurrentActiveDropdown().drawExpanded();
			}
		}
		GUIElement.deactivateCallbacks = true;
		for (int i = 0; i < ic.getDeactivatedPlayerInputs().size(); i++) {
			DialogInterface inputPanel = ic.getDeactivatedPlayerInputs().get(i);
			;
			inputPanel.updateDeacivated();
			inputPanel.getInputPanel().draw();
			if (System.currentTimeMillis() - inputPanel.getDeactivationTime() > PlayerInput.drawDeactivatedTime) {
				ic.getDeactivatedPlayerInputs().get(i).getInputPanel().cleanUp();
				ic.getDeactivatedPlayerInputs().remove(i);
				i--;
			}
		}
		GUIElement.deactivateCallbacks = false;
		GlUtil.glPopMatrix();
		getState().getController().getInputController().drawDropdownAndContext();
		GUIElement.disableOrthogonal();
	}

	@Override
	public void update(Timer timer) {
		getState().getController().getInputController().getGuiCallbackController().updateToolTips(timer);
	}

	private static Object getCredits() {
		StringBuilder b = new StringBuilder();
		b.append(Lng.str("Lead Developer\n"));
		b.append("~     Robin Promesberger (schema)\n\n");
		b.append(Lng.str("Game Developers\n"));
		b.append("~     Jordan Peck (Auburn)\n");
		b.append("~     Mitch Petrie (micdoodle8)\n\n");
		b.append(Lng.str("2D Artists\n"));
		b.append("~     Tom Berridge (kupu)\n\n");
		b.append(Lng.str("3D Artists\n"));
		b.append("~     Jay Gaskell (Saber)\n");
		b.append("~     Keaton Pursell (Omni)\n\n");
		b.append(Lng.str("Music & SFX\n"));
		b.append("~     Daniel Tusjak (danki)\n\n");
		b.append(Lng.str("Launcher Development & Web\n"));
		b.append("~     Terra Rain (calani)\n\n");
		b.append(Lng.str("Game Test Lead\n"));
		b.append("~     Brent Van Hoecke (lancake)\n\n");
		b.append(Lng.str("Customer Experience Manager\n"));
		b.append("~     Andy P?ttmann (AndyP)\n\n");
		b.append(Lng.str("Creative Director\n"));
		b.append("~     Michael Debevec (Bench)\n\n");
		b.append(Lng.str("Social Media\n"));
		b.append("~     Eric Hobson (Criss)\n\n");
		b.append(Lng.str("Business & Community\n"));
		b.append("~     Tai Coromandel (DukeofRealms)\n\n");
		b.append(Lng.str("Special Thanks\n"));
		b.append("~     Adam Boyle (bspkrs) - Game Development\n");
		b.append("~     Joshua Keel (calbiri) - Assistant Game Design\n");
		b.append("~     Kramer Campbell (kramerc) - Web\n\n");
		b.append("~     Garret Reichenbach (TheDerpGamer) - Game Development\n");
		b.append(Lng.str("Game Testers\n"));
		b.append("~     Frank (SmilingDemon)\n");
		b.append("~     Andrew\n");
		b.append("~     Arsat\n");
		b.append("~     Megacrafter127\n");
		b.append("~     Danny May (Titansmasher)\n");
		b.append("~     Ray Kohler (Sven_The_Slayer)\n");
		b.append("~     Tim Night (spunkie)\n");
		b.append("~     Samuel (Zackey_TNT)\n");
		b.append("~     Schnellbier\n\n");
		b.append(Lng.str("StarLoader Team\n"));
		b.append("~     JakeV\n");
		b.append("~     TheDerpGamer\n");
		b.append("~     Ithirihad\n");
		b.append("~     LupoCani\n");
		b.append("~     Space Shaman (Ishax)\n\n");
		b.append(Lng.str("Top Bug Reporters\n"));
		b.append("~     Ithirahad\n");
		b.append("~     ErthParadine\n");
		b.append("~     LordXaosa\n");
		b.append("~     Nightrune\n");
		b.append("~     Malacodor\n");
		b.append("~     Croquelune\n");
		b.append("~     Napther\n\n");
		b.append(Lng.str("Translation Managers\n"));
		b.append("~     Maureen Blanchard - French\n");
		b.append("~     Monika Viste - Polish\n\n");
		b.append(Lng.str("Community Translators\n"));
		b.append("~     Mikihiko Miyashita (oasisdog) - Japanese\n");
		b.append("~     Ricardo Telles Carbonar - Portuguese, Brasilian\n");
		b.append("~     Alfonso Sanchez Dominguez (Fonso_s) - Spanish\n");
		b.append("~     Robert Ehelebe (Tarusol) - German\n");
		b.append("~     Kirill Gaev (liptoh890) - Russian\n");
		b.append("~     Alexander Sergeev (The_NorD) - Russian\n\n");
		b.append("\nThanks to all who contributed to our community translation project!\n");
		return b.toString();
	}
}
