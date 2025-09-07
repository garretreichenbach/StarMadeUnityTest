package org.schema.game.client.view.gui.crew.quarters;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.game.client.view.gui.crew.AICrewAssignStationScrollableList;
import org.schema.game.client.view.mainmenu.DialogInput;
import org.schema.game.common.data.element.quarters.Quarter;
import org.schema.game.common.data.element.quarters.crew.CrewMember;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class CrewAssignmentPanel extends DialogInput {

	private final CrewAssignmentPane inputPanel;

	public CrewAssignmentPanel(InputState state, Quarter quarter) {
		super(state);
		inputPanel = new CrewAssignmentPane("CREW_ASSIGNMENT_PANEL", state, 500, 350, this, "ASSIGN CREW", "", quarter);
		inputPanel.setCallback(this);
		inputPanel.setOkButton(false);
		inputPanel.onInit();
	}

	@Override
	public GUIElement getInputPanel() {
		return inputPanel;
	}

	@Override
	public void onDeactivate() {
		if(inputPanel != null) inputPanel.cleanUp();
	}

	public static class CrewAssignmentPane extends GUIInputPanel {

		private AICrewAssignStationScrollableList crewList;
		private final Quarter quarter;

		public CrewAssignmentPane(String name, InputState state, int width, int height, DialogInput dialog, String title, String description, Quarter quarter) {
			super(name, state, width, height, dialog, title, description);
			this.quarter = quarter;
		}

		@Override
		public void onInit() {
			super.onInit();
			(crewList = new AICrewAssignStationScrollableList(getState(), this, quarter, 500, 350)).onInit();
			getContent().attach(crewList);
		}

		public CrewMember getSelectedCrew() {
			try {
				return crewList.getSelectedRow().f;
			} catch(Exception exception) {
				return null;
			}
		}
	}
}
