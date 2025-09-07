package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIElementList;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Scrollable Table List for but only for Strings.
 * <br/>Does not support any other data types and does not have any search or filtering. This is meant for purely visual lists with no interaction.
 *
 * @author Garret Reichenbach
 */
public class GUIScrollableStringTableList extends ScrollableTableList<String[]> {

	private final ArrayList<String> headers;
	private final ArrayList<String[]> values;

	public GUIScrollableStringTableList(InputState state, GUIElement pane, ArrayList<String> headers, ArrayList<String[]> values) {
		super(state, 10, 10, pane);
		this.headers = headers;
		this.values = values;
		setCallback(new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {

			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
	}

	@Override
	public void initColumns() {
		for(String header : headers) {
			addColumn(header, 3.0f, (o1, o2) -> values.indexOf(o1) - values.indexOf(o2));
		}
	}

	@Override
	public Collection<String[]> getElementList() {
		return values;
	}

	@Override
	public void updateListEntries(GUIElementList mainList, Set<String[]> collection) {
		for(String[] data : collection) {
			List<GUIClippedRow> rows = new ArrayList<>();
			for(String s : data) rows.add(getSimpleRow(s));
			GUIScrollableStringTableListRow row = new GUIScrollableStringTableListRow(getState(), data, rows.toArray(new GUIElement[0]));
			row.onInit();
			mainList.addWithoutUpdate(row);
		}
		mainList.updateDim();
	}

	public class GUIScrollableStringTableListRow extends ScrollableTableList<String[]>.Row {

		public GUIScrollableStringTableListRow(InputState state, String[] data, GUIElement... elements) {
			super(state, data, elements);
		}

		@Override
		public void clickedOnRow() {
			//Do nothing
		}
	}
}
