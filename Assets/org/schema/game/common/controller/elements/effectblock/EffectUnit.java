package org.schema.game.common.controller.elements.effectblock;

import org.schema.game.common.data.element.ElementCollection;

public abstract class EffectUnit<E extends EffectUnit<E, CM, EM>, CM extends EffectCollectionManager<E, CM, EM>, EM extends EffectElementManager<E, CM, EM>> extends ElementCollection<E, CM, EM> {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EffectUnit [significator=" + significator + "]";
	}

	//	@Override
	//	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
	//		return col.getElementManager().getGUIUnitValues(this, col, supportCol, effectCol);
	////		return ControllerManagerGUI.create(state, "Pulse Module", this,
	////
	////				new ModuleValueEntry(Lng.str("Effect",StringTools.formatPointZero(0))
	////
	////				);
	//	}

}