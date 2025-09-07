package org.schema.game.common.controller.elements.ammo;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.element.ElementCollection;

public abstract class AmmoCapacityUnit<E extends AmmoCapacityUnit<E, CM, EM>, CM extends AmmoCapacityCollectionManager<E, CM, EM>, EM extends AmmoCapacityElementManager<E, CM, EM>> extends ElementCollection<E,CM,EM> {

    public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
        return elementCollectionManager.getElementManager().getGUIUnitValues((E) this, elementCollectionManager, supportCol, effectCol);
        //		return ControllerManagerGUI.create(state, "Thruster Module", this, new ModuleValueEntry(Lng.str("Thrust", StringTools.formatPointZero(thrust)));
    }
}
