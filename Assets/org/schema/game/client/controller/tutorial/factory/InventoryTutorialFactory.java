package org.schema.game.client.controller.tutorial.factory;

import org.schema.game.client.controller.tutorial.states.InventoryClosedTestState;
import org.schema.game.client.controller.tutorial.states.InventoryOpenTestState;
import org.schema.game.client.controller.tutorial.states.TextState;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.input.KeyboardMappings;

public class InventoryTutorialFactory extends AbstractFSMFactory {

	public InventoryTutorialFactory(State startState, State resetState, State tutorialEndState,
	                                GameClientState state) {
		super(startState, resetState, tutorialEndState, state);

	}

	@Override
	protected State create(State startState) {
		GameClientState state = (GameClientState) this.state;
		State inventory0 = new InventoryOpenTestState(getObj(),
				"Press '" + KeyboardMappings.INVENTORY_PANEL.getKeyChar() + "' to enter \n" +
						"your inventory.", state);

		State inventory1 = new TextState(getObj(), state,
				"Good!\n" +
						"Here you see all the building\n" +
						"modules you currently have.\n" +
						""
				, 8000);

		State inventory2 = new TextState(getObj(), state,
				"You can drag and stack item\n"
				, 8000);

		State inventory3 = new TextState(getObj(), state,
				"To take a custom amount of an item,\n" +
						"right-click on it."
				, 12000);

		State inventory4 = new TextState(getObj(), state,
				"You can also drag them in the\n" +
						"action bar at the bottom to assign them\n" +
						"to the respective number on your keyboard."
				, 12000);

		transition(startState, inventory0);
		transition(inventory0, inventory1);
		transition(inventory1, inventory2);
		transition(inventory2, inventory3);
		transition(inventory3, inventory4);

		return inventory4;
	}

	@Override
	public void defineStartAndEnd() {
		GameClientState state = (GameClientState) this.state;
		setStartState(new TextState(getObj(), state,
				"Let's go on to learn about the inventory"
				, 8000));
		setEndState(new InventoryClosedTestState(getObj(),
				"when you are done, press '" + KeyboardMappings.INVENTORY_PANEL.getKeyChar() + "' again \n" +
						"to exit from your inventory", state));
	}

}
