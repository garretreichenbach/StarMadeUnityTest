package org.schema.game.client.controller.manager.ingame;

import api.utils.gui.GUIMenuPanel;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButton;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.MusicTags;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class GUIMusicPanel extends GUIMenuPanel {

	private final MusicManager musicManager;
	public GUIHorizontalButton playButton;

	public GUIMusicPanel(InputState inputState, String windowName, int width, int height, MusicManager musicManager) {
		super(inputState, windowName, width, height);
		this.musicManager = musicManager;
	}

	@Override
	public void recreateTabs() {
		if(!guiWindow.getTabs().isEmpty()) guiWindow.clearTabs();
		createMusicTab();
		createPlaylistsTab();
		if(MusicManager.isAdmin()) createTagsTab();
	}

	private void createMusicTab() {
		GUIContentPane musicTab = guiWindow.addTab(Lng.str("MUSIC"));
		musicTab.setTextBoxHeightLast(350);

		//Time Counter
		musicManager.timeCounter = new GUITextOverlay(getState());
		musicManager.timeCounter.onInit();
		musicManager.timeCounter.setFont(FontLibrary.FontSize.MEDIUM_15);
		musicManager.timeCounter.setTextSimple("Stopped   00:00");
		musicTab.getContent(0).attach(musicManager.timeCounter);
		musicTab.setTextBoxHeightLast(30);

		//Button Pane
		musicTab.addNewTextBox(64);
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 2, musicTab.getContent(1));
		buttonPane.onInit();
		buttonPane.addButton(0, 0, "<<", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.previous();
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
		playButton = (GUIHorizontalButton) buttonPane.addButton(1, 0, new Object() {
			@Override
			public String toString() {
				return !musicManager.isPaused() ? Lng.str("PAUSE") : Lng.str("PLAY");
			}
		}, GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					if(!musicManager.isPaused()) musicManager.pause();
					else musicManager.resume();
					playButton.setText(!musicManager.isPaused() ? Lng.str("PAUSE") : Lng.str("PLAY"));
					playButton.onInit();
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
		buttonPane.addButton(2, 0, ">>", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.next();
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
		buttonPane.addButton(0, 1, Lng.str("SHUFFLE"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.shuffle = !musicManager.shuffle;
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isHighlighted(InputState state) {
				return musicManager.shuffle;
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});

		buttonPane.addButton(1, 1, Lng.str("STOP"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.audioController.stopMusic();
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
		buttonPane.addButton(2, 1, Lng.str("LOOP"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.setLooping(!musicManager.isLooping());
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isHighlighted(InputState state) {
				return musicManager.isLooping();
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		musicTab.getContent(1).attach(buttonPane);
		musicTab.setTextBoxHeightLast((int) buttonPane.getHeight());

		//Music List
		musicTab.addNewTextBox(350);
		MusicScrollableList musicList = new MusicScrollableList(getState(), musicManager, this, musicTab.getContent(2));		musicList.onInit();
		musicTab.getContent(2).attach(musicList);
	}

	private void createPlaylistsTab() {
		//Todo: Playlist support
//			GUIContentPane playlistsTab = guiWindow.addTab(Lng.str("PLAYLISTS"));
//			playlistsTab.setTextBoxHeightLast(30);
	}

	private void createTagsTab() {
		GUIContentPane tagsTab = guiWindow.addTab(Lng.str("TAGS"));
		tagsTab.setTextBoxHeightLast(350);
		GUIScrollablePanel tagsPanel = new GUIScrollablePanel(getWidth(), getHeight(), tagsTab.getContent(0), getState());
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, MusicTags.values().length, tagsPanel);
		tagsPanel.setContent(buttonPane);
		tagsPanel.onInit();
		int y = 0;
		for(MusicTags tags : MusicTags.values()) {
			buttonPane.addButton(0, y, tags.getTagName().replaceAll("_", " ") + Lng.str(" OFF"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						musicManager.stop();
						musicManager.getTagManager().tagMap.remove(tags.getTagId());
						musicManager.getTagManager().tagHighPrioFlagged = false;
					}
				}

				@Override
				public boolean isOccluded() {
					return !musicManager.getTagManager().tagMap.containsKey(tags.getTagId());
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return musicManager.getTagManager().tagMap.containsKey(tags.getTagId());
				}
			});
			buttonPane.addButton(1, y, tags.getTagName().replaceAll("_", " ") + Lng.str(" ON"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						musicManager.stop();
						musicManager.audioController.stopMusic();
						musicManager.getTagManager().updateMusicTag(tags, getState().getUpdateTime());
						musicManager.getTagManager().tagHighPrioFlagged = true;
						musicManager.getTagManager().update(getState().getGraphicsContext().timer);
					}
				}

				@Override
				public boolean isOccluded() {
					return musicManager.getTagManager().tagMap.containsKey(tags.getTagId());
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return !musicManager.getTagManager().tagMap.containsKey(tags.getTagId());
				}
			});
			y++;
		}
		tagsTab.getContent(0).attach(tagsPanel);
	}
}
