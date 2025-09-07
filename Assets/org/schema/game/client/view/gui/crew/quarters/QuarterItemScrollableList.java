package org.schema.game.client.view.gui.crew.quarters;

import api.utils.game.inventory.ItemStack;
import org.schema.game.common.data.element.quarters.CanteenQuarter;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.QuarterItemInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.graphicsengine.forms.gui.newgui.ScrollableTableList;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class QuarterItemScrollableList extends ScrollableTableList<ItemStack> {

	private final GUIElement pane;
	private final Quarter quarter;

	public QuarterItemScrollableList(InputState state, GUIElement pane, CanteenQuarter quarter) {
		super(state, pane.getWidth(), pane.getHeight(), pane);
		this.pane = pane;
		this.quarter = quarter;
	}

	@Override
	public void initColumns() {

		activeSortColumnIndex = 0;
	}

	@Override
	protected Collection<ItemStack> getElementList() {
		if(quarter instanceof QuarterItemInterface) return ((QuarterItemInterface) quarter).getItems();
		else return new ArrayList<>();
	}

	@Override
	public void updateListEntries(GUIElementList list, Set<ItemStack> collection) {
		list.deleteObservers();
		list.addObserver(this);
		for(ItemStack itemStack : collection) {
			GUIClippedRow iconRow = createIconRow(itemStack.getElementInfo().getBuildIconNum());
			GUIClippedRow nameRow = createRow(itemStack.getName());
			GUIClippedRow amountRow = createRow(String.valueOf(itemStack.getAmount()));

			QuarterItemScrollableListRow row = new QuarterItemScrollableListRow(getState(), itemStack, iconRow, nameRow, amountRow);
			GUIAnchor anchor = new GUIAnchor(getState(), pane.getWidth(), 28.0F);
			anchor.attach(createButtonPane(itemStack, anchor));
			row.expanded = new GUIElementList(getState());
			row.expanded.add(new GUIListElement(anchor, getState()));
			row.expanded.attach(anchor);
			row.onInit();
			list.addWithoutUpdate(row);
		}
		list.updateDim();
	}

	private ScrollableTableList<ItemStack>.GUIClippedRow createIconRow(int buildIconNum) {
		GUIOverlay iconElement = IconDatabase.getBuildIconsInstance(getState(), buildIconNum);
		GUIClippedRow iconRowElement = new GUIClippedRow(getState());
		iconRowElement.attach(iconElement);
		return iconRowElement;
	}

	private GUIClippedRow createRow(String label) {
		GUITextOverlayTable element = new GUITextOverlayTable(getState());
		element.setTextSimple(label);
		GUIClippedRow row = new GUIClippedRow(getState());
		row.attach(element);
		return row;
	}

	private GUIHorizontalButtonTablePane createButtonPane(final ItemStack itemStack, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("SET AMOUNT"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {

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
		buttonPane.addButton(0, 1, Lng.str("REMOVE"), GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {

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

	public class QuarterItemScrollableListRow extends ScrollableTableList<ItemStack>.Row {

		public QuarterItemScrollableListRow(InputState state, ItemStack itemStack, GUIElement... elements) {
			super(state, itemStack, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}
	}
}