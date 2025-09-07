package org.schema.game.client.view.gui.fleet;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;

public class AddShipToFleetPlayerInput extends PlayerGameOkCancelInput{

	private AvailableFleetShipsInRangeScrollableListNew l;
	private Fleet fleet;

	public AddShipToFleetPlayerInput(GameClientState state, Fleet fleet) {
		super("ASHIPFLEET", state, 700, 400, Lng.str("Add Ship To Fleet %s",fleet.getName()), "");
		
		this.fleet = fleet;
		getInputPanel().setCancelButton(true);
		getInputPanel().onInit();
		
		GUIDialogWindow w = ((GUIDialogWindow)getInputPanel().background);
		
		l = new AvailableFleetShipsInRangeScrollableListNew(getState(), w.getMainContentPane().getContent(0));
		l.onInit();
		w.getMainContentPane().getContent(0).attach(l);
	}






	@Override
	public void onDeactivate() {
		l.cleanUp();
	}

	@Override
	public void pressedOK() {
		
		for(SegmentController s : l.getSelectedSegmentController()){
			System.err.println("[CLIENT] REQUESTING TO FLEET ADD: "+s);
			getState().getFleetManager().requestShipAdd(fleet, s);
		}
		deactivate();
	}
	
}
