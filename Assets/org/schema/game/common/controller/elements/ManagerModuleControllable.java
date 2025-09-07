package org.schema.game.common.controller.elements;

import org.schema.game.common.data.element.ElementCollection;
import org.schema.schine.graphicsengine.core.Timer;

public class ManagerModuleControllable
		<E extends ElementCollection<E, EC, EM>, EC extends ControlBlockElementCollectionManager<E, EC, EM>, EM extends UsableControllableElementManager<E, EC, EM>>
		extends ManagerModule<E, EC, EM> {

	private final short controllerID;

	public ManagerModuleControllable(EM usableCollectionElementManager,
	                                 short controlledID, short controllerID) {
		super(usableCollectionElementManager, controlledID);
		this.controllerID = controllerID;
	}

	/**
	 * @return the controllerID
	 */
	public short getControllerID() {
		return controllerID;
	}

	@Override
	public void update(Timer timer, long time) {
	}

	@Override
	public void onFullyLoaded() {
	}

	@Override
	public boolean needsAnyUpdate() {
		return getElementManager().isUpdatable() || (getElementManager().getCollectionManagers().size() > 0 && getElementManager().getCollectionManagers().get(0).needsUpdate());
	}
	
	
}
