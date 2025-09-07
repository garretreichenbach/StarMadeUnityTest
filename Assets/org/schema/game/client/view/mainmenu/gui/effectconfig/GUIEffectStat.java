package org.schema.game.client.view.mainmenu.gui.effectconfig;

import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.game.common.data.blockeffects.config.*;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager.EffectEntityType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

import java.io.File;
import java.io.FileNotFoundException;

public class GUIEffectStat extends GUIObservable implements ConfigPoolProvider {

	public ConfigPool configPool;

	public ConfigGroup selectedGroup;

	public EffectConfigElement selectedElement;

	public ConfigEntityManager testManager;

	private String path;

	public GUIEffectStat(InputState state, ConfigPool pool) {
		this.configPool = pool;
	}

	public GUIEffectStat(InputState state, String path) {
		if (path == null) {
			this.path = "./data/config/" + ConfigPool.CONFIG_FILENAME;
		}
		load(state, this.path, false);
	}

	public void updateLocal(Timer timer) {
		if (testManager != null) {
			testManager.updateLocal(timer, null);
		}
	}

	public void change() {
		notifyObservers();
	}

	public String getLoadedPath() {
		return path;
	}

	public void save(InputState state, String p) {
		if (p != null) {
			this.path = p;
		}
		if(path == null) {
			path = "./";
		}
		File file = new File(path);
		try {
			configPool.write(file);
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
			AudioController.fireAudioEventID(763);
		} catch (Exception e) {
			e.printStackTrace();
			PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("ERROR"), Lng.str("An error happened while saving effect config!\nSee logs for details.\n%s\n%s", file.getAbsolutePath(), e.getClass().getSimpleName())) {

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
			AudioController.fireAudioEventID(762);
		}
	}

	public void load(InputState state, String path, boolean showDialog) {
		configPool = new ConfigPool();
		File file = new File(path);
		try {
			configPool.readConfigFromFile(file);
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
				AudioController.fireAudioEventID(765);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!(e instanceof FileNotFoundException) || showDialog) {
				// dont show dialog when initially loading without config file
				PlayerOkCancelInput c = new PlayerOkCancelInput("INFO_NOTE", state, 400, 160, Lng.str("ERROR"), Lng.str("An error happened while loading effect config!\nSee logs for details.\n%s\n%s", file.getAbsolutePath(), e.getClass().getSimpleName())) {

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
				AudioController.fireAudioEventID(764);
			}
		}
		testManager = new ConfigEntityManager(0, EffectEntityType.OTHER, this);
		change();
	}

	@Override
	public ConfigPool getConfigPool() {
		return configPool;
	}
}
