package org.schema.game.common.controller.elements.power.reactor.chamber;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class ReactorChamberUnit extends ElementCollection<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> {



	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, 
			ControlBlockElementCollectionManager<?, ?, ?> supportCol, 
			ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		Vector3i dim = new Vector3i();
		dim.sub(getMax(new Vector3i()), getMin(new Vector3i()));
		return ControllerManagerGUI.create(state, Lng.str("Reactor Chamber Module"), this,
				new ModuleValueEntry(Lng.str("Dimension"), dim),
				new ModuleValueEntry(Lng.str("Recharge"), "N/A"));
	}

	@Override
	public boolean onChangeFinished() {
		
		ConduitCollectionManager conduits = getPowerInterface().getConduits();
		
		//flag all overlapping conduits to check their overlap
		for(ConduitUnit c : conduits.getElementCollections()){
			if(ElementCollection.overlaps(c, this, 1, 0)){
				c.flagCalcConnections();
			}
		}
		return super.onChangeFinished();
	}
	
}