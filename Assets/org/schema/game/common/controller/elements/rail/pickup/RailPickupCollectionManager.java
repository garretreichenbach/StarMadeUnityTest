package org.schema.game.common.controller.elements.rail.pickup;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class RailPickupCollectionManager extends ElementCollectionManager<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> implements BlockActivationListenerInterface {

	public RailPickupCollectionManager(
			SegmentController segController, VoidElementManager<RailPickupUnit, RailPickupCollectionManager> em) {
		super(ElementKeyMap.PICKUP_AREA, segController, em);
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<RailPickupUnit> getType() {
		return RailPickupUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}
	
	

	@Override
	public void update(Timer timer) {
		
		if(!getElementCollections().isEmpty()){
			getElementCollections().get(0).update(timer);
		}
		super.update(timer);
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	public RailPickupUnit getInstance() {
		return new RailPickupUnit();
	}

	@Override
	protected void onChangedCollection() {
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{new ModuleValueEntry("", "")};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Rail Pickup System");
	}
	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.ALL_IN_ONE;
	}
	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		if (getSegmentController().isOnServer()) {
			long absIndex = piece.getAbsoluteIndex();
			for (RailPickupUnit d : getElementCollections()) {
				if (d.contains(absIndex)) {
					d.activate(piece, active);
					return active ? 1 : 0;
				}
			}
		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		if (getSegmentController().isOnServer()) {
			typesThatNeedActivation.add(ElementKeyMap.PICKUP_AREA);
		}
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
		return type == ElementKeyMap.PICKUP_AREA;
	}

}
