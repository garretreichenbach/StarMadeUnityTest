package org.schema.game.client.view.mainmenu.gui.ruleconfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.client.controller.PlayerTextInput;
import org.schema.game.common.controller.rules.RuleManagerProvider;
import org.schema.game.common.controller.rules.RulePropertyContainer;
import org.schema.game.common.controller.rules.RuleSet;
import org.schema.game.common.controller.rules.RuleSetManager;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.network.objects.remote.RemoteRuleSetManager;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GUIRuleSetStat extends GUIObservable implements RuleManagerProvider {

	public RuleSetManager manager;

	public RuleSet selectedRuleset;

	public RulePropertyContainer getProperties() {
		if (gameState != null) {
			return gameState.getRuleProperties();
		}
		return null;
	}

	private String path;

	public SendableGameState gameState;

	private final InputState inputState;

	public GUIRuleSetStat(InputState state, RuleSetManager manager) {
		this.manager = manager;
		this.inputState = state;
	}

	public GUIRuleSetStat(InputState state, String path) {
		if (path == null) {
			this.path = RuleSetManager.rulesPath;
		}
		this.inputState = state;
		load(state, this.path, false);
	}

	public void updateLocal(Timer timer) {
	}

	public void change() {
		notifyObservers();
	}

	public String getLoadedPath() {
		return path;
	}

	public void saveAs(InputState state, File file) {
		try {
			manager.writeToDisk(file);
			PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("SAVED"), Lng.str("Successfully saved\n%s", file.getAbsolutePath())) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(835);
		} catch (Exception e) {
			e.printStackTrace();
			PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("ERROR"), Lng.str("An error happened while saving rule config!\nSee logs for details.\n%s\n%s", file.getAbsolutePath(), e.getClass().getSimpleName())) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(834);
		}
	}

	public void save(InputState state, String p) {
		if (gameState != null) {
			System.err.println("[CLIENT] Sending all rules and properties to server");
			manager.includePropertiesInSendAndSaveOnServer = true;
			assert (manager.getProperties() != null);
			gameState.getNetworkObject().ruleSetManagerBuffer.add(new RemoteRuleSetManager(manager, gameState.getNetworkObject()));
			PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("SAVED"), Lng.str("Ruleset sent to server. It will be saved there")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(836);
			return;
		}
		if (p != null) {
			this.path = p;
		}
		File file = new File(path);
		saveAs(state, file);
	}

	public void load(InputState state, String path, boolean showDialog) {
		manager = new RuleSetManager();
		File file = new File(path);
		try {
			manager.loadFromDisk(path);
			if (showDialog) {
				PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("LOADED"), Lng.str("Successfully loaded\n%s", file.getAbsolutePath())) {

					@Override
					public void pressedOK() {
						deactivate();
					}

					@Override
					public void onDeactivate() {
					}
				};
				c.getInputPanel().setCancelButton(false);
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(838);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!(e instanceof FileNotFoundException) || showDialog) {
				// dont show dialog when initially loading without config file
				PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("ERROR"), Lng.str("An error happened while loading rule config!\nSee logs for details.\n%s\n%s", file.getAbsolutePath(), e.getClass().getSimpleName())) {

					@Override
					public void pressedOK() {
						deactivate();
					}

					@Override
					public void onDeactivate() {
					}
				};
				c.getInputPanel().setCancelButton(false);
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(837);
			}
		}
		change();
	}

	@Override
	public RuleSetManager getRuleSetManager() {
		return manager;
	}

	public SendableGameState getGameState() {
		return gameState;
	}

	public void importFile(File f) {
		try {
			RuleSetManager s = new RuleSetManager();
			s.loadFromDisk(f);
			System.err.println("LOADED FROM DISK. RULESETS: " + s.getRuleSets().size());
			List<RuleSet> toAdd = new ObjectArrayList<RuleSet>(s.getRuleSets());
			boolean allOkWithoutDupes = true;
			for (int i = 0; i < toAdd.size(); i++) {
				boolean t = checkNameAndAdd(toAdd.get(i), i == toAdd.size() - 1);
				allOkWithoutDupes = t && allOkWithoutDupes;
			}
			if (allOkWithoutDupes) {
				// message if there were no dupes to feedback that something happened. otherwise there will be input dialogs to do the same
				PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("INFO"), Lng.str("Successfully imported %s ", f.getName())) {

					@Override
					public void pressedOK() {
						deactivate();
					}

					@Override
					public void onDeactivate() {
					}
				};
				c.getInputPanel().setCancelButton(false);
				c.getInputPanel().onInit();
				c.activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(840);
			}
		} catch (Exception e) {
			e.printStackTrace();
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("ERROR"), Lng.str("An error happened during reading of the file %s: %s%s", f.getName(), e.getClass().getSimpleName(), e.getMessage() != null ? (": " + e.getMessage()) : "")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(839);
		}
	}

	private boolean checkNameAndAdd(final RuleSet r, final boolean last) {
		if (manager.containtsName(r)) {
			System.err.println("NAME EXISTS: " + r.uniqueIdentifier);
			(new PlayerTextInput("PPLExport", inputState, 64, Lng.str("Import"), Lng.str("The name of the ruleset '%s' already exists. Please pick another or skip.", r.uniqueIdentifier)) {

				@Override
				public void onFailedTextCheck(String msg) {
				}

				@Override
				public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
					return null;
				}

				@Override
				public String[] getCommandPrefixes() {
					return null;
				}

				@Override
				public boolean onInput(final String entry) {
					if (entry.length() == 0) {
						return false;
					}
					r.uniqueIdentifier = entry;
					checkNameAndAdd(r, last);
					return true;
				}

				@Override
				public void cancel() {
					r.uniqueIdentifier = "";
					super.cancel();
				}

				@Override
				public void onDeactivate() {
					if (r.uniqueIdentifier.length() > 0 && !manager.containtsName(r)) {
						System.err.println("ADDING RULESET " + r.uniqueIdentifier);
						manager.addRuleSet(r);
						change();
					}
				}
			}).activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(841);
			return false;
		} else {
			return true;
		}
	}

	public void exportSelected(File f) {
		try {
			System.err.println("EXPORT SELECTED RULESET TO " + f.getCanonicalPath());
			selectedRuleset.export(f);
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("INFO"), Lng.str("Successfully exported as %s", f.getName())) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(843);
		} catch (Exception e) {
			e.printStackTrace();
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("ERROR"), Lng.str("An error happened during the writing of the file %s: %s%s", f.getName(), e.getClass().getSimpleName(), e.getMessage() != null ? (": " + e.getMessage()) : "")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(842);
		}
	}

	public void exportAll(File f) {
		try {
			manager.writeToDisk(f);
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("INFO"), Lng.str("Successfully exported as %s", f.getName())) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(845);
		} catch (Exception e) {
			e.printStackTrace();
			PlayerOkCancelInput c = new PlayerOkCancelInput("CONFIRM", inputState, 300, 150, Lng.str("ERROR"), Lng.str("An error happened during the writing of the file %s: %s%s", f.getName(), e.getClass().getSimpleName(), e.getMessage() != null ? (": " + e.getMessage()) : "")) {

				@Override
				public void pressedOK() {
					deactivate();
				}

				@Override
				public void onDeactivate() {
				}
			};
			c.getInputPanel().setCancelButton(false);
			c.getInputPanel().onInit();
			c.activate();
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
			AudioController.fireAudioEventID(844);
		}
	}
}
