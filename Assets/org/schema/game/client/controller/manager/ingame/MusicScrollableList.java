package org.schema.game.client.controller.manager.ingame;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.asset.AudioAsset;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

import static org.schema.game.client.controller.manager.ingame.MusicManager.getArtist;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class MusicScrollableList extends ScrollableTableList<AudioAsset> {

	private final MusicManager musicManager;
	private final GUIMusicPanel panel;

	public MusicScrollableList(InputState state, MusicManager musicManager, GUIMusicPanel panel, GUIAnchor anchor) {
		super(state, 30.0f, 30.0f, anchor);
		this.musicManager = musicManager;
		this.panel = panel;
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 0.5f, Comparator.comparing(o -> o.getFile().getName()));
		addColumn(Lng.str("Artist"), 0.3f, Comparator.comparing(MusicManager::getArtist));
		addColumn(Lng.str("Length"), 0.2f, Comparator.comparing(o -> o.getData().getDuration()));
		addTextFilter(new GUIListFilterText<>() {
			@Override
			public boolean isOk(String input, AudioAsset listElement) {
				return listElement.getFile().getName().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH));
			}
		}, ControllerElement.FilterRowStyle.FULL);
	}

	@Override
	protected Collection<AudioAsset> getElementList() {
		return musicManager.getMusic();
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<AudioAsset> collection) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(AudioAsset asset : collection) {
			GUIClippedRow nameRow = createRow(asset.getFile().getName().replaceAll(".ogg", ""));
			GUIClippedRow artistRow = createRow(getArtist(asset));
			GUIClippedRow lengthRow = createRow(getLengthDisplay(asset));
			MusicScrollableListRow row = new MusicScrollableListRow(getState(), asset, nameRow, artistRow, lengthRow);
			GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth() - 28.0f, 28.0F);
			anchor.attach(createButtonPane(anchor, asset));
			row.expanded = new GUIElementList(getState());
			row.expanded.add(new GUIListElement(anchor, getState()));
			row.expanded.attach(anchor);
			row.onInit();
			guiElementList.addWithoutUpdate(row);
		}
		guiElementList.updateDim();
	}

	private GUIHorizontalButtonTablePane createButtonPane(GUIAnchor anchor, AudioAsset asset) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("PLAY"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					musicManager.play(asset);
					panel.playButton.setText(!musicManager.isPaused() ? Lng.str("PAUSE") : Lng.str("PLAY"));
					panel.playButton.onInit();
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
		}); //Todo: Playlist support
		return buttonPane;
	}

	private String getLengthDisplay(AudioAsset asset) {
		float rawLength = asset.getData().getDuration();
		int minutes = (int) (rawLength / 60);
		int seconds = (int) (rawLength % 60);
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}

	private GUIClippedRow createRow(String label) {
		GUITextOverlayTable element = new GUITextOverlayTable(getState());
		element.setTextSimple(label);
		GUIClippedRow row = new GUIClippedRow(getState());
		row.attach(element);
		return row;
	}

	public class MusicScrollableListRow extends ScrollableTableList<AudioAsset>.Row {

		public MusicScrollableListRow(InputState state, AudioAsset userData, GUIElement... elements) {
			super(state, userData, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}