package org.schema.game.common.controller.elements.activationgate;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public class ActivationGateCollectionManager extends ControlBlockElementCollectionManager<ActivationGateUnit, ActivationGateCollectionManager, ActivationGateElementManager> {

	private long lastPopup;

	public ActivationGateCollectionManager(SegmentPiece element,
	                                 SegmentController segController, ActivationGateElementManager em) {
		super(element, ElementKeyMap.ACTIVATION_GATE_MODULE, segController, em);

		assert (element != null);
	}



	@Override
	public CollectionShape requiredNeigborsPerBlock() {
		return CollectionShape.LOOP;
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<ActivationGateUnit> getType() {
		return ActivationGateUnit.class;
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public ActivationGateUnit getInstance() {
		return new ActivationGateUnit();
	}

	@Override
	protected void onChangedCollection() {
		super.onChangedCollection();
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
	}

	@Override
	public void update(Timer timer) {
//		System.err.println("UPDATE: "+isValid()+"; "+getState());
		if (isValid()) {
			
			getElementCollections().get(0).update(timer);
		} else {
			
			if (!getSegmentController().isOnServer() && ((GameClientState) getSegmentController().getState()).getCurrentSectorId() == getSegmentController().getSectorId()) {
				if (System.currentTimeMillis() - lastPopup > 5000) {
					Transform t = new Transform();
					t.setIdentity();
					Vector3i p = getControllerPos();
					t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
					getSegmentController().getWorldTransform().transform(t.origin);
					RaisingIndication raisingIndication = new RaisingIndication(t, "Invalid gate Build\nMust be one 2D loop!", 1f, 0.1f, 0.1f, 1f);
					raisingIndication.speed = 0.1f;
					raisingIndication.lifetime = 3.0f;
					HudIndicatorOverlay.toDrawTexts.add(raisingIndication);

					lastPopup = System.currentTimeMillis();
				}
			}

			//			System.err.println("NOT UPDATING INVALID WARPGATE cols: "+getElementCollections().size()+"; ");
			//			if(getElementCollections().size() == 1){
			//				System.err.println("DEBUG VALID: "+getElementCollections().get(0).getValidInfo());
			//			}
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {

		return new GUIKeyValueEntry[]{};
	}

	@Override
	public String getModuleName() {
		return Lng.str("Activation Gate System");
	}

	public boolean isValid() {
		return getElementCollections().size() == 1 && getElementCollections().get(0).isValid();
	}


}
