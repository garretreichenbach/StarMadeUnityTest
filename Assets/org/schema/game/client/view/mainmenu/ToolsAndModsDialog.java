package org.schema.game.client.view.mainmenu;

import api.mod.gui.configui.ModConfigBrowser;
import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.controller.PlayerButtonTilesInput;
import org.schema.game.client.view.mainmenu.gui.effectconfig.EffectConfigDialog;
import org.schema.game.client.view.mainmenu.gui.effectconfig.GUIEffectStat;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.GUIRuleSetStat;
import org.schema.game.client.view.mainmenu.gui.ruleconfig.RuleSetConfigDialogMainMenu;
import org.schema.game.client.view.mainmenu.gui.screenshotviewer.ScreenshotViewerDialog;
import org.schema.game.common.Starter;
import org.schema.game.common.facedit.ElementEditorFrame;
import org.schema.game.common.gui.CatalogManagerEditorController;
import org.schema.game.common.starcalc.StarCalc;
import org.schema.game.common.staremote.Staremote;
import org.schema.game.common.util.DesktopUtils;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.editor.LanguageEditor;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIActivationCallback;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ToolsAndModsDialog extends PlayerButtonTilesInput implements MainMenuInputDialogInterface {

	public ToolsAndModsDialog(GameMainMenuController state) {
		super(null, state, UIScale.getUIScale().scale(650), UIScale.getUIScale().scale(400), Lng.str("Tools & Mods"), UIScale.getUIScale().scale(200), UIScale.getUIScale().scale(100));
	}

	@Override
	public void onDeactivate() {
	}

	@Override
	public boolean isActive() {
		return !MainMenuGUI.runningSwingDialog && (getState().getController().getPlayerInputs().isEmpty() || getState().getController().getPlayerInputs().get(getState().getController().getPlayerInputs().size() - 1) == this);
	}

	public void addToolsAndModsButtons() {
		addTile(Lng.str("Clear Client Cache"), Lng.str("use this if you have missing chunks in a game"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(863);
					Starter.cleanClientCacheWithoutBackup();
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Launch Block Editor"), Lng.str("Block editor for easy block modding"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					ElementEditorFrame frame = new ElementEditorFrame();
					SwingUtilities.invokeLater(() -> {
						frame.setVisible(true);
						frame.requestFocus();
					});
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Language Editor"), Lng.str("Create or edit languages"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					LanguageEditor.main(new String[]{"disposeonexit"});
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Edit Mod Configs"), Lng.str("Edit mod configs"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.fireAudioEventID(864);
					deactivate();
					(new ModConfigBrowser(getState())).activate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("StarMote"), Lng.str("Remotely access server without game graphics"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					Staremote m = new Staremote();
					m.exit = false;
					m.startConnectionGUI();
					deactivate();
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("StarCalc"), Lng.str("Calculate buff/nerf for blockBehavior.xml config changes"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(864);
					deactivate();
					StarCalc.main(null);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Catalog Manager"), Lng.str("Import/Export blueprints"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(865);
					deactivate();
					CatalogManagerEditorController m = new CatalogManagerEditorController(null);
					m.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					m.setVisible(true);
					m.setAlwaysOnTop(true);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Effect Config Editor"), Lng.str("Edit all possible status effects\nof the game"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(866);
					deactivate();
					// default path
					String path = null;
					GUIEffectStat stat = new GUIEffectStat(getState(), path);
					EffectConfigDialog d = new EffectConfigDialog(getState(), stat);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(867);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Open Screenshots Folder"), Lng.str("Open Screenshots Folder"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					try {
						File screenshotsFolder = new File("./screenshots");
						if(!screenshotsFolder.exists()) screenshotsFolder.mkdirs();
						Desktop.getDesktop().open(screenshotsFolder);
						AudioController.fireAudioEventID(869);
						deactivate();
					} catch(IOException exception) {
						exception.printStackTrace();
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Screenshot Manager"), Lng.str("View and Manage Screenshots"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					File screenshotsFolder = new File("./screenshots");
					if(!screenshotsFolder.exists()) screenshotsFolder.mkdirs();
					(new ScreenshotViewerDialog(getState())).activate();
					AudioController.fireAudioEventID(869);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Open Game Folder"), Lng.str("Open the game folder"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					DesktopUtils.openFolder(new File("./"));
					AudioController.fireAudioEventID(869);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return !isActive();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});

		addTile(Lng.str("Rule Editor"), Lng.str("Edit all the available rulesets\nyou want to use ingame"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !isActive();
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(868);
					deactivate();
					// default path
					String path = null;
					GUIRuleSetStat stat = new GUIRuleSetStat(getState(), path);
					RuleSetConfigDialogMainMenu d = new RuleSetConfigDialogMainMenu(getState(), stat);
					d.activate();
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
					AudioController.fireAudioEventID(869);
				}
			}
		}, new GUIActivationCallback() {

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return ToolsAndModsDialog.this.isActive();
			}
		});
		addTile(Lng.str("Audio Assets"), Lng.str("Manage in-game audio"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					AudioController.instance.startGUI();
					AudioController.fireAudioEventID(869);
					deactivate();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
	}

	@Override
	public GameMainMenuController getState() {
		return (GameMainMenuController) super.getState();
	}
}
