package org.schema.game.common.controller.elements.power.reactor.chamber;

import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class ReactorChamberCollectionManager extends ElementCollectionManager<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> {

	
	private final short chamberId;

	

	public ReactorChamberCollectionManager(short chamberId,
			SegmentController segController, ReactorChamberElementManager em) {
		super(chamberId, segController, em);
		assert(ElementKeyMap.isChamber(chamberId)):chamberId+"; "+ElementKeyMap.isChamber(chamberId);
		this.chamberId = chamberId;
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected void onFinishedCollection() {
		super.onFinishedCollection();
		
		getPowerInterface().checkRemovedChamber(getElementCollections());
	}

	@Override
	protected Class<ReactorChamberUnit> getType() {
		return ReactorChamberUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public void update(Timer timer) {
	}


	@Override
	public ReactorChamberUnit getInstance() {
		return new ReactorChamberUnit();
	}

	@Override
	protected void onChangedCollection() {
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{
				};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Reactor Chamber %s",ElementKeyMap.toString(chamberId));
	}

	@Override
	public float getSensorValue(SegmentPiece connected){
		return  0;
	}

	public short getChamberId() {
		return chamberId;
	}
	
}
