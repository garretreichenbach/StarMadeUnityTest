package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

public class ManagerModuleSingle
		<E extends ElementCollection<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableControllableSingleElementManager<E, CM, EM>>
		extends ManagerModule<E, CM, EM> {

	private final short controllerID;
	private final EM elementManager;

	public ManagerModuleSingle(EM usableCollectionElementManager, short controllerID, short elementID) {
		super(usableCollectionElementManager, elementID);
		this.elementManager = usableCollectionElementManager;
		this.controllerID = controllerID;

	}
	/**
	 * @return the controllerID
	 */
	public short getControllerID() {
		return controllerID;
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerModule#addControlledBlock(org.schema.common.util.linAlg.Vector3i, org.schema.common.util.linAlg.Vector3i, short)
	 */
	@Override
	public void addControlledBlock(Vector3i from, short fromType, Vector3i to, short toType) {

		getCollectionManager().addModded(to, toType);
		elementManager.onControllerChange();
	}
	@Override
	public void onFullyLoaded() {
	}
	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerModule#clear()
	 */
	@Override
	public void clear() {
		getCollectionManager().clear();
		elementManager.totalSize = 0;
	}

	@Override
	public EM getElementManager() {
		return elementManager;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerModule#removeControllerBlock(org.schema.common.util.linAlg.Vector3i, org.schema.common.util.linAlg.Vector3i, short)
	 */
	@Override
	public void onConnectionRemoved(Vector3i controller, Vector3i controlled,
	                                short controlledType) {
		getCollectionManager().remove(controlled);
		elementManager.onControllerChange();
	}

	public void addElement(long absIndex, short type) {
		//can use tmp since position is added as long
		getCollectionManager().doAdd(absIndex, type);
	}

	/**
	 * @return the collectionManagers
	 */
	public CM getCollectionManager() {
		assert (elementManager != null);
		assert (elementManager.getCollection() != null);
		return elementManager.getCollection();
	}

	public void removeElement(byte x, byte y, byte z, Segment segment) {
		getCollectionManager().remove(ElementCollection.getIndex(x, y, z, segment));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerModule#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer, long time) {
		ElementCollectionManager<E, CM, EM> collection = elementManager.getCollection();
		assert (collection != null);
		collection.updateStructure(time);
		if (collection.needsUpdate()) {
			collection.update(timer);
		}
	}

	@Override
	public boolean needsAnyUpdate() {
		ElementCollectionManager<E, CM, EM> collection = elementManager.getCollection();
		return (collection.rawCollection != null && collection.rawCollection.size() > 0 && collection.needsUpdate()) || elementManager.isUpdatable();
	}

	

}
