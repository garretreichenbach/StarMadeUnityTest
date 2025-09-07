package org.schema.game.client.view.gui.crew.quarters;

import api.common.GameClient;
import api.utils.game.PlayerUtils;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class QuarterScrollableList extends ScrollableTableList<Quarter> {

	private final GUIElement panel;
	private final QuarterTab tab;

	public QuarterScrollableList(InputState state, GUIElement panel, QuarterTab tab) {
		super(state, panel.getWidth(), panel.getHeight(), panel);
		this.panel = panel;
		this.tab = tab;
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Priority"), 0.5f, Comparator.comparingInt(Quarter::getPriority));

		addColumn("Assigned Crew", 0.7f, Comparator.comparingInt(o -> o.getCrew().size()));

		addColumn("Type", 1.0f, Comparator.comparing(o -> o.getType().name()));

		addColumn("Status", 1.0f, Comparator.comparing(o -> o.getStatus().name()));

		addColumn("Integrity", 0.85f, (o1, o2) -> (int) (o1.getIntegrity() - o2.getIntegrity()));

		addDropdownFilter(new GUIListFilterDropdown<>(Quarter.QuarterType.getStrings()) {
			@Override
			public boolean isOk(String quarterType, Quarter quarter) {
				return quarterType.equals(quarter.getType().name()) || quarterType.equals("ALL");
			}
		}, new CreateGUIElementInterface<>() {
			@Override
			public GUIElement create(String quarterType) {
				GUIAnchor anchor = new GUIAnchor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple(quarterType);
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer(quarterType);
				anchor.attach(dropDown);
				return anchor;
			}

			@Override
			public GUIElement createNeutral() {
				GUIAnchor anchor = new GUIAnchor(getState(), 10.0F, 24.0F);
				GUITextOverlayTableDropDown dropDown;
				(dropDown = new GUITextOverlayTableDropDown(10, 10, getState())).setTextSimple("ALL");
				dropDown.setPos(4.0F, 4.0F, 0.0F);
				anchor.setUserPointer("ALL");
				anchor.attach(dropDown);
				return anchor;
			}
		}, ControllerElement.FilterRowStyle.FULL);

		activeSortColumnIndex = 0;
	}

	@Override
	protected Collection<Quarter> getElementList() {
		if(getController() != null) return getController().getQuarterManager().getQuartersById().values();
		else return new ArrayList<>();
	}

	@Override
	public void updateListEntries(GUIElementList list, Set<Quarter> collection) {
		list.deleteObservers();
		list.addObserver(this);
		for(Quarter quarter : collection) {
			GUIClippedRow priorityRow = createRow(String.valueOf((quarter.getPriority() + 1)));
			GUIClippedRow assignedCrew = createRow(quarter.getCrew().size() + " assigned");
			GUIClippedRow typeRow = createRow(quarter.getType().name());
			GUIClippedRow statusRow = createRow(quarter.getStatus().name());
			GUIClippedRow integrityRow = createRow(quarter.getIntegrity() * 100 + "%");
			QuarterScrollableListRow row = new QuarterScrollableListRow(getState(), quarter, priorityRow, assignedCrew, typeRow, statusRow, integrityRow);
			GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth(), 28.0F) {
				@Override
				public void draw() {
					super.draw();
					setWidth(row.getWidth());
				}
			};
			anchor.attach(createButtonPane(quarter, anchor));
			row.expanded = new GUIElementList(getState());
			row.expanded.add(new GUIListElement(anchor, getState()));
			row.expanded.attach(anchor);
			row.onInit();
			list.addWithoutUpdate(row);
		}
		list.updateDim();
	}

	private GUIHorizontalButtonTablePane createButtonPane(final Quarter quarter, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 4, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, "^", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					quarter.setPriority(Math.min(quarter.getPriority() - 1, 0));
					tab.redraw();
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
		buttonPane.addButton(1, 0, "V", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					quarter.setPriority(quarter.getPriority() + 1);
					tab.redraw();
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
		buttonPane.addButton(2, 0, Lng.str("MANAGE CREW"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) (new CrewAssignmentPanel(getState(), quarter)).activate();
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
		buttonPane.addButton(3, 0, Lng.str("MANAGE QUARTER"), GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
//					(new QuarterManagementPanel(getState(), quarter)).activate(); Todo
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

	private GUIClippedRow createRow(String label) {
		GUITextOverlayTable element = new GUITextOverlayTable(getState());
		element.setTextSimple(label);
		GUIClippedRow row = new GUIClippedRow(getState());
		row.attach(element);
		return row;
	}

	public static SegmentController getController() {
		if(PlayerUtils.getCurrentControl(GameClient.getClientPlayerState()) instanceof SegmentController) return (SegmentController) PlayerUtils.getCurrentControl(GameClient.getClientPlayerState());
		else return null;
	}

	public class QuarterScrollableListRow extends ScrollableTableList<Quarter>.Row {

		public QuarterScrollableListRow(InputState state, Quarter quarter, GUIElement... elements) {
			super(state, quarter, elements);
			this.highlightSelect = true;
			this.highlightSelectSimple = true;
			this.setAllwaysOneSelected(true);
		}
	}
}
