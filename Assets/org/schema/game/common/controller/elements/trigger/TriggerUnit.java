package org.schema.game.common.controller.elements.trigger;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.common.language.Lng;

public class TriggerUnit extends ElementCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TriggerUnit [significator=" + significator + "]";
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		//		return col.getElementManager().getGUIUnitValues(this, col, supportCol, effectCol);
		return ControllerManagerGUI.create(state, Lng.str("Trigger Module"), this,

				new ModuleValueEntry(Lng.str("Trigger"), 0)

		);
	}

}