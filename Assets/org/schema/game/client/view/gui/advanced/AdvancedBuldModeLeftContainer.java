package org.schema.game.client.view.gui.advanced;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advancedEntity.AdvancedEntity;
import org.schema.game.client.view.gui.advancedstats.AdvancedStructureStats;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextButton;
import org.schema.schine.sound.controller.AudioController;

import java.io.IOException;
import java.util.List;

public class AdvancedBuldModeLeftContainer {

	public final AdvancedStructureStats advancedStrctureStats;

	public final AdvancedEntity advancedEntity;

	public AdvancedGUIBuildModeLeftElement selected;

	private final List<AdvancedGUIBuildModeLeftElement> bmPanel = new ObjectArrayList<AdvancedGUIBuildModeLeftElement>();

	private GameClientState state;

	private GUITextButton entityButton;

	private GUITextButton structureButton;

	public AdvancedBuldModeLeftContainer(GameClientState state) {
		this.state = state;
		bmPanel.add(advancedStrctureStats = new AdvancedStructureStats((GameClientState) state));
		bmPanel.add(advancedEntity = new AdvancedEntity((GameClientState) state));
		selected = advancedEntity;
		final AdvancedGUIMinimizeCallback minimizeCallback = new AdvancedGUIMinimizeCallback(state, true) {

			@Override
			public boolean isActive() {
				return selected.isActive() && selected.main.isActive();
			}

			@Override
			public void initialMinimized() {
				setMinimizedInitial(EngineSettings.STRUCTURE_STATS_MINIMIZED.isOn());
			}

			@Override
			public void onMinimized(boolean minimized) {
				EngineSettings.STRUCTURE_STATS_MINIMIZED.setOn(minimized);
				try {
					EngineSettings.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public String getMaximizedText() {
				return Lng.str("\\/");
			}
			@Override
			public String getMinimizedText() {
				return Lng.str("^ Structure ^");
			}

		};
		entityButton = new GUITextButton(state, 170, 20, new Object() {
			@Override
			public String toString() {
				return advancedEntity.getPanelName();
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !(advancedEntity.isActive() && advancedEntity.main.isActive());
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SWITCH)*/
					AudioController.fireAudioEventID(300);
					switchFrom(advancedStrctureStats);
				}
			}
		}) {

			@Override
			public void draw() {
				if (!minimizeCallback.isMinimized()) {
					minimizeCallback.setButtonPosition(this, advancedEntity);
					getPos().y -= (getWidth() + 5);
					super.draw();
				}
				if (selected == advancedEntity) {
					getBackgroundColor().set(1, 1, 0, 1);
					getBackgroundColorMouseOverlay().set(1, 1, 0, 1);
				} else {
					getBackgroundColor().set(0.3f, 0.3f, 0.6f, 0.9f);
					getBackgroundColorMouseOverlay().set(0.3f, 0.3f, 0.6f, 0.9f);
				}
			}
		};
		structureButton = new GUITextButton(state, 170, 20, new Object() {

			@Override
			public String toString() {
				return advancedStrctureStats.getPanelName();
			}
		}, new GUICallback() {

			@Override
			public boolean isOccluded() {
				return !(advancedStrctureStats.isActive() && advancedStrctureStats.main.isActive());
			}

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SWITCH)*/
					AudioController.fireAudioEventID(301);
					switchFrom(advancedEntity);
				}
			}
		}) {

			@Override
			public void draw() {
				if (!minimizeCallback.isMinimized()) {
					minimizeCallback.setButtonPosition(this, advancedStrctureStats);
					getPos().y += minimizeCallback.getButtonWidth() + (5);
					super.draw();
				}
				if (selected == advancedStrctureStats) {
					getBackgroundColor().set(1, 1, 0, 1);
					getBackgroundColorMouseOverlay().set(1, 1, 0, 1);
				} else {
					getBackgroundColor().set(0.3f, 0.3f, 0.6f, 0.9f);
					getBackgroundColorMouseOverlay().set(0.3f, 0.3f, 0.6f, 0.9f);
				}
			}
		};
		minimizeCallback.additionalButtons.add(entityButton);
		minimizeCallback.additionalButtons.add(structureButton);
		for (AdvancedGUIBuildModeLeftElement e : bmPanel) {
			e.setMinimizeCallback(minimizeCallback);
		}
	}

	public void update(Timer timer) {
		for (AdvancedGUIBuildModeLeftElement e : bmPanel) {
			e.update(timer);
		}
	}

	public void onInit() {
		entityButton.onInit();
		for (AdvancedGUIBuildModeLeftElement e : bmPanel) {
			e.onInit();
		}
	}

	public void draw() {
		selected.draw();
	}

	public void drawToolTip(long currentTimeMillis) {
		selected.drawToolTip(currentTimeMillis);
	}

	public boolean isInside() {
		return selected.isInside();
	}

	public String getSwitchToNameFrom(AdvancedGUIBuildModeLeftElement from) {
		return getNext(from).getPanelName();
	}

	public void switchFrom(AdvancedGUIBuildModeLeftElement from) {
		selected = getNext(from);
	}

	public AdvancedGUIBuildModeLeftElement getNext(AdvancedGUIBuildModeLeftElement from) {
		int indexOf = bmPanel.indexOf(from);
		if (indexOf >= 0) {
			return bmPanel.get((indexOf + 1) % bmPanel.size());
		} else {
			return from;
		}
	}
}
