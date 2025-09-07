package org.schema.game.common.controller.elements;

import java.util.List;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ManagerModuleCollection<E extends ElementCollection<E, CM, EM>, CM extends ControlBlockElementCollectionManager<E, CM, EM>, EM extends UsableControllableElementManager<E, CM, EM>>
		extends ManagerModuleControllable<E, CM, EM> {

	private final EM elementManager;
	private final Vector3i tmpAbsPos = new Vector3i();

	public ManagerModuleCollection(EM usableCollectionElementManager, short controllerID, short elementID) {
		super(usableCollectionElementManager, elementID, controllerID);
		this.elementManager = usableCollectionElementManager;
	}

	@Override
	public void addControlledBlock(Vector3i from, short fromType, Vector3i to, short toType) {
		elementManager.addConnectionIfNecessary(from, fromType, to, toType);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerModule#clear()
	 */
	@Override
	public void clear() {
		for (int i = 0; i < elementManager.getCollectionManagers().size(); i++) {
			ElementCollectionManager<E, CM, EM> ecm = elementManager.getCollectionManagers().get(i);
			ecm.clear();
		}
		elementManager.getCollectionManagers().clear();
		elementManager.getCollectionManagersMap().clear();
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

		elementManager.removeConnectionIfNecessary(controller,
				controlled, controlledType);

//		elementManager.onConnectionRemoved();

	}

	public void addControllerBlockFromAddedBlock(long absPos, Segment segment, boolean revalidate) {

		
		
		SegmentPiece p = new SegmentPiece(segment, 
				(byte)ByteUtil.modUSeg(ElementCollection.getPosX(absPos)), 
				(byte)ByteUtil.modUSeg(ElementCollection.getPosY(absPos)), 
				(byte)ByteUtil.modUSeg(ElementCollection.getPosZ(absPos)) );
		assert(segment.getSegmentController() == elementManager.getSegmentController());
		ElementCollection.getPosFromIndex(absPos, tmpAbsPos);
		assert(p.getAbsoluteIndex() == absPos);
		if(!elementManager.getSegmentController().isOnServer() && !elementManager.getSegmentController().getControlElementMap().receivedInitialClient){
//			System.err.println("ADDDING BLOCK DENIED");
			//client didnt have initial update yet. the block added should not be processed
			return;
		}
		if (elementManager.getCollectionManagersMap().containsKey(absPos)) {
			return;
		}

		CM newInstance = elementManager.getNewCollectionManager(p, null);
		if (newInstance instanceof ControlBlockElementCollectionManager<?, ?, ?>) {

			/**
			 * (this is used on server and client upon loading in!
			 * else the addConnectionIfNecessary is used)
			 *
			 * refresh this position if there are any connected blocks for this
			 * controller block already in the ControlElementMap
			 */
			ControlBlockElementCollectionManager<E, CM, EM> c = (newInstance);
			c.refreshControlled(elementManager.getSegmentController().getControlElementMap(), p.getType());
		}
		if(newInstance instanceof PowerConsumer){
			elementManager.getManagerContainer().addConsumer((PowerConsumer)newInstance);
		}
		if(newInstance instanceof PlayerUsableInterface){
			
			elementManager.getManagerContainer().addPlayerUsable((PlayerUsableInterface)newInstance);
		}
		getCollectionManagers().add(newInstance);
		getCollectionManagersMap().put(absPos, newInstance);
		elementManager.onAddedCollection(absPos, newInstance);

		//set time of shot to what the highest reload time was on this module
		newInstance.nextShot = elementManager.nextShot;
//		if (!revalidate) {
//			newInstance.startInitialization();
//		}

		newInstance.pieceRefresh();
		elementManager.flagCheckUpdatable();
	}

	/**
	 * @return the collectionManagers
	 */
	public List<CM> getCollectionManagers() {
		return elementManager.getCollectionManagers();
	}

	public Long2ObjectOpenHashMap<CM> getCollectionManagersMap() {
		return elementManager.getCollectionManagersMap();
	}

	
	public void removeControllerBlock(byte x, byte y, byte z, Segment segment) {
		long absoluteIndex = segment.getAbsoluteIndex(x, y, z);

		CM m = getCollectionManagersMap().remove(absoluteIndex);
		if (m != null) {
			if(m.getSegmentController().isOnServer()){
				if (m.getSegmentController() instanceof Ship && 
						m.getSlaveConnectedElementRaw() != Long.MIN_VALUE && 
						m.getSegmentController().getSegmentBuffer().existsPointUnsave(m.getSlaveConnectedElementRaw())) {
					m.getSegmentController().getControlElementMap()
					.addControllerForElement(ElementCollection.getIndex(Ship.core),
							ElementCollection.getPosIndexFrom4(m.getSlaveConnectedElementRaw()), (short) ElementCollection.getType(m.getSlaveConnectedElementRaw()));
				}
	
				if (m.getSegmentController() instanceof Ship && 
						m.getEffectConnectedElementRaw() != Long.MIN_VALUE && 
						m.getSegmentController().getSegmentBuffer().existsPointUnsave(m.getEffectConnectedElementRaw())) {
					m.getSegmentController().getControlElementMap()
					.addControllerForElement(ElementCollection.getIndex(Ship.core),
							ElementCollection.getPosIndexFrom4(m.getEffectConnectedElementRaw()), (short) ElementCollection.getType(m.getEffectConnectedElementRaw()));
				}
			}
			elementManager.totalSize -= m.getTotalSize();
			m.stopUpdate();
			elementManager.getCollectionManagers().remove(m);
			
			m.onRemovedCollection(absoluteIndex, m);
		}

	}

	@Override
	public void update(Timer timer, long time) {
		final int size = this.elementManager.getCollectionManagers().size();
		this.elementManager.beforeUpdate();

		if(this.elementManager.getSegmentController().isOnServer()){
//			this.elementManager.checkIntegrityServer();
		}
		
		for (int i = 0; i < size; i++) {
			ElementCollectionManager<E, CM, EM> ecm = this.elementManager.getCollectionManagers().get(i);
			ecm.updateStructure(time);
			if (ecm.needsUpdate()) {
				ecm.update(timer);
			}
		}
		this.elementManager.afterUpdate();
	}

	public String getManagerName() {
		return elementManager.getManagerName();
	}

	public GUIKeyValueEntry[] getGUIElementCollectionValues() {
		return elementManager.getGUIElementCollectionValues();
	}

	public boolean hasAtLeastOneCoreUnit() {
		if (getCollectionManagers().size() == 0) {
			return false;
		}
		for (int i = 0; i < getCollectionManagers().size(); i++) {

			if (getCollectionManagers().get(i).getTotalSize() > 0 && elementManager.getSegmentController().getControlElementMap().
					isControlling(Ship.core, getCollectionManagers().get(i).getControllerPos(), elementManager.controllerId)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void onFullyLoaded() {
		elementManager.uniqueConnections = null;
	}

	

	

}
