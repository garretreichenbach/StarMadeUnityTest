package org.schema.game.common.controller.elements;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SlotAssignment;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ShipConfigurationNotFoundException;

import java.io.IOException;
import java.util.Random;

public abstract class UsableControllableSingleElementManager
		<E extends ElementCollection<E, CM, EM>, CM extends ElementCollectionManager<E, CM, EM>, EM extends UsableControllableSingleElementManager<E, CM, EM>> extends
		UsableElementManager<E, CM, EM> implements TargetableSystemInterface {

	private CM collection;
	private final Class<CM> clazz;

	//INSERTED CODE
	public Class<CM> getCollectionManagerClass() {
		return clazz;
	}
	///

	public UsableControllableSingleElementManager(
			SegmentController segmentController, Class<CM> clazz) {
		super(segmentController);

		this.clazz = clazz;
	}
	
	@Override
	public void onElementCollectionsChanged(){
		this.lowestIntegrity = collection.getLowestIntegrity();
	}
	@Override
	public void init(ManagerContainer container){
		this.collection = getNewCollectionManager(null, clazz);
		assert (this.collection != null);
		if(this.collection instanceof PowerConsumer){
			container.addConsumer(((PowerConsumer)this.collection));
		}
		if(this.collection instanceof PlayerUsableInterface){
			container.addPlayerUsable(((PlayerUsableInterface)this.collection));
		}

	}
	public void onKilledBlock(long pos, short type, Damager from) {
		if(getSegmentController().isOnServer()){
			assert(this instanceof BlockKillInterface);
			if(lowestIntegrity < VoidElementManager.INTEGRITY_MARGIN){
				CM cm = this.collection;
				cm.checkIntegrity(pos, type, from);
			}
		}
	}
	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public E getRandomCollection(Random r) {
		if (collection.getElementCollections().size() > 0) {
			return collection.getElementCollections().get(r.nextInt(collection.getElementCollections().size()));
		}
		return null;
	}

	protected boolean convertDeligateControls(ControllerStateInterface unit, Vector3i controlledFromOrig, Vector3i controlledFrom) throws IOException {

		if (unit.getPlayerState() == null) {
			return true;
		}

		unit.getParameter(controlledFromOrig);
		unit.getParameter(controlledFrom);

		SlotAssignment shipConfiguration = null;
		SegmentPiece fromPiece = getSegmentBuffer().getPointUnsave(controlledFrom, new SegmentPiece());//autorequest true previously
		if(fromPiece == null){
			return false;
		}
		if (fromPiece != null && fromPiece.getType() == ElementKeyMap.CORE_ID) {
			try {
				shipConfiguration = checkShipConfig(unit);
				int currentlySelectedSlot = unit.getCurrentShipControllerSlot();
				if (!shipConfiguration.hasConfigForSlot(currentlySelectedSlot)) {
					return false;
				} else {
					controlledFrom.set(shipConfiguration.get(currentlySelectedSlot));
				}
			} catch (ShipConfigurationNotFoundException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the collection
	 */
	public final CM getCollection() {
		return collection;
	}

	@Override
	public String getManagerName() {
		return collection.getModuleName();
	}

	public abstract void onControllerChange();

	@Override
	public void flagCheckUpdatable() {
		setUpdatable(collection.isStructureUpdateNeeded());
	}

	
	
}


