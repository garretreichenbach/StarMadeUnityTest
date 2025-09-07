package org.schema.game.client.view.gui.crew;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ai.AiInterfaceContainer;
import org.schema.game.common.controller.ai.UnloadedAiContainer;
import org.schema.game.common.controller.ai.UnloadedAiEntityException;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
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
public class AICrewAssignStationScrollableList extends ScrollableTableList<CrewMember> {

	private final GUIElement panel;
	private final Quarter quarter;

	public AICrewAssignStationScrollableList(InputState state, GUIElement panel, Quarter quarter, float width, float height) {
		super(state, width, height, panel);
		this.panel = panel;
		this.quarter = quarter;
	}

	@Override
	public void updateListEntries(GUIElementList list, Set<CrewMember> collection) {
		list.deleteObservers();
		list.addObserver(this);
		for(CrewMember node : collection) {
			GUIClippedRow priorityRow = createRow(node.getUID());

			GUIAnchor anchor = new GUIAnchor(getState(), panel.getWidth(), 28.0F);
			anchor.attach(createButtonPane(node, anchor));
			AICrewAssignStationScrollableListRow row = new AICrewAssignStationScrollableListRow(getState(), node, priorityRow);
			row.expanded = new GUIElementList(getState());
			row.expanded.add(new GUIListElement(anchor, getState()));
			row.expanded.attach(anchor);
			row.onInit();
			list.addWithoutUpdate(row);
		}
		list.updateDim();
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 0.15f, Comparator.comparing(UnloadedAiContainer::getUID));

		addColumn(Lng.str("Combat Skill"), 0.1f, Comparator.comparingInt(CrewMember::getCombatSkill));

		addColumn(Lng.str("Engineering Skill"), 0.1f, Comparator.comparingInt(CrewMember::getEngineeringSkill));

		addColumn(Lng.str("Physics Skill"), 0.1f, Comparator.comparingInt(CrewMember::getPhysicsSkill));

		addColumn(Lng.str("Biology Skill"), 0.1f, Comparator.comparingInt(CrewMember::getBiologySkill));

		activeSortColumnIndex = 0;
	}

	@Override
	protected Collection<CrewMember> getElementList() {
		ArrayList<CrewMember> available = new ArrayList<>();
		Collection<AiInterfaceContainer> temp = ((GameClientState) getState()).getPlayer().getPlayerAiManager().getCrew();
		for(AiInterfaceContainer container : temp) {
			try {
				available.add(CrewMember.fromContainer(container));
			} catch(UnloadedAiEntityException exception) {
				exception.printStackTrace();
			}
		}
		return available;
	}

	private GUIHorizontalButtonTablePane createButtonPane(CrewMember node, GUIAnchor anchor) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, anchor);
		buttonPane.addButton(0, 0, "Assign", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					quarter.addCrew(node);
					notifyObservers();
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
		buttonPane.onInit();
		return buttonPane;
	}

	private GUIClippedRow createRow(String label) {
		GUITextOverlayTable element = new GUITextOverlayTable(getState());
		element.setTextSimple(label);
		GUIClippedRow row = new GUIClippedRow(getState());
		row.attach(element);
		return row;
	}

	public class AICrewAssignStationScrollableListRow extends ScrollableTableList<CrewMember>.Row {

		public AICrewAssignStationScrollableListRow(InputState state, CrewMember crewMember, GUIElement... elements) {
			super(state, crewMember, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}
