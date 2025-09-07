package org.schema.game.common.controller.elements.activation;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import java.io.IOException;

public class ActivationElementManager extends UsableControllableElementManager<AbstractUnit, ActivationCollectionManager, ActivationElementManager> implements BlockActivationListenerInterface, TagModuleUsableInterface, ManagerReloadInterface {

	public final static String TAG_ID = "ACD";
	public static boolean debug = false;
	private Vector3i controlledFromOrig = new Vector3i();
	private Vector3i controlledFrom = new Vector3i();
	private long lastActivationTime;

	public ActivationElementManager(final SegmentController segmentController) {
		super(Element.TYPE_ALL, Element.TYPE_ALL, segmentController);
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		if(!ElementKeyMap.isValidType(piece.getType())) {
			return -1;
		}
		if (!ElementKeyMap.getInfo(piece.getType()).isSignal()) {
			System.err.println("SET::: "+ElementKeyMap.toString(piece.getType()));
			return active ? 1 : 0;
		}

		ActivationCollectionManager activationCollectionManager = getCollectionManagersMap().get(piece.getAbsoluteIndex());
		if (activationCollectionManager != null) {
//						System.err.println(getState()+"; "+getSegmentController()+": --> ACTIVATE BLOCK "+piece+" -> "+active+" --> "+activationCollectionManager);
			return activationCollectionManager.onActivate(this, piece, oldActive, active);
		} else {

			if (getSegmentController().getControlElementMap().getControllingMap().getAll().containsKey(piece.getAbsoluteIndex())) {
				//no collection manager but control structure exists
				return -1;
			}
		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
	}
	@Override
	public String getReloadStatus(long id) {
		return Lng.str("STANDBY");
	}
	@Override
	public void drawReloads(Vector3i iconPos, Vector3i iconSize, long controllerPos) {
	}
	@Override
	public int getCharges() {
		return 0;
	}

	@Override
	public int getMaxCharges() {
		return 0;
	}
	@Override
	public BlockMetaDataDummy getDummyInstance(){
		return new ActivationDestMetaDataDummy();
	}
	@Override
	public String getTagId() {
		return TAG_ID;
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(AbstractUnit firingUnit,
	                                             ActivationCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Activation Unit"), firingUnit

		);
	}

	@Override
	protected String getTag() {
		return "activation";
	}

	@Override
	public ActivationCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<ActivationCollectionManager> clazz) {
		return new ActivationCollectionManager(position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Activation System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

		if (!getSegmentController().isOnServer()) {
			return;
		}
		long curTime = System.currentTimeMillis();
		if (!unit.isFlightControllerActive()) {
			if (debug) {
				System.err.println("NOT ACTIVE");
			}
			return;
		}
		if (getCollectionManagers().isEmpty()) {
			if (debug) {
				System.err.println("NO WEAPONS");
			}
			//nothing to shoot with
			return;
		}
		try {
			if (!convertDeligateControls(unit, controlledFromOrig, controlledFrom)) {
				if (debug) {
					System.err.println("NO SLOT");
				}
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int unpowered = 0;
		getPowerManager().sendNoPowerHitEffectIfNeeded();
		if (debug) {
			System.err.println("FIREING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + controlledFrom);
		}
//		for (int i = 0; i < getCollectionManagers().size(); i++) {
//			ActivationCollectionManager m = getCollectionManagers().get(i);
//			if (unit.isSelected(m.getControllerElement(), controlledFrom)) {
//
//				long timeSinceAct = getState().getUpdateTime() - lastActivationTime;
//
//				if (unit.isMouseButtonDown(0) && !unit.wasMouseButtonDownServer(0)) {
//					m.getControllerElement().refresh();
//					long da;
//					if (!m.getControllerElement().isActive()) {
//						da = ElementCollection.getActivation(m.getControllerElement().getAbsoluteIndex(), true, false);
//					} else {
//						da = ElementCollection.getDeactivation(m.getControllerElement().getAbsoluteIndex(), true, false);
//					}
//					((SendableSegmentController) getSegmentController()).getBlockActivationBuffer().enqueue(da);
//
//					lastActivationTime = getState().getUpdateTime();
//				}
//			}
//		}
	}
	

	@Override
	public boolean isHandlingActivationForType(short type) {
		return true;
	}
	public boolean isUsingRegisteredActivation() {
		return true;
	}
}
