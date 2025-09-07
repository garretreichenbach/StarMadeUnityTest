package org.schema.game.client.view.gui;

import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.util.*;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class SavedRemotesScrollableList extends ScrollableTableList<String> implements Observer {

	private final Fleet fleet;
	private final GUIElement p;
	private final PlayerShipRemoteSelect select;

	public SavedRemotesScrollableList(InputState state, PlayerShipRemoteSelect select, float width, float height, GUIElement p, Fleet fleet) {
		super(state, width, height, p);
		this.fleet = fleet;
		this.p = p;
		this.select = select;
	}

	@Override
	public void update(Observable o, Object arg) {
		//TODO Ithirahad: @Goose No idea how this is supposed to be restructured; the Observer interface requires this method so I added it to eliminate the compiler error.
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 8, String::compareToIgnoreCase);

		addColumn(Lng.str("Status"), 4, String::compareToIgnoreCase);
	}

	@Override
	protected Collection<String> getElementList() {
		return fleet.getSavedRemotes().keySet();
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<String> collection) {
		mainList.deleteObservers();
		mainList.addObserver(this);
		for(String s : collection) {
			GUITextOverlayTable nameText = new GUITextOverlayTable(getState());
			nameText.setTextSimple(s);

			GUITextOverlayTable statusText = new GUITextOverlayTable(getState());
			statusText.setTextSimple((fleet.getSavedRemotes().get(s) ? Lng.str("Active") : Lng.str("Inactive")));

			SavedRemotesScrollableListRow listRow = new SavedRemotesScrollableListRow(getState(), s, nameText, statusText);
			GUIAnchor anchor = new GUIAnchor(getState(), p.getWidth() - 28.0f, 28.0f);
			anchor.attach(redrawButtonPane(s, anchor));
			listRow.expanded = new GUIElementList(getState());
			listRow.expanded.add(new GUIListElement(anchor, getState()));
			listRow.expanded.attach(anchor);
			listRow.onInit();
			mainList.addWithoutUpdate(listRow);
		}
		mainList.updateDim();
	}

	private GUIHorizontalButtonTablePane redrawButtonPane(final String s, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("Select"), GUIHorizontalArea.HButtonType.BUTTON_BLUE_MEDIUM, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					fleet.toggleRemote(s);
					select.deactivate();
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
		buttonPane.addButton(1, 0, Lng.str("Delete"), GUIHorizontalArea.HButtonType.BUTTON_RED_MEDIUM, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					fleet.removeRemote(s);
					select.deactivate();
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
		return buttonPane;
	}

	private class SavedRemotesScrollableListRow extends ScrollableTableList<String>.Row {

		public SavedRemotesScrollableListRow(InputState state, String s, GUIElement... elements) {
			super(state, s, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}
	}
}
