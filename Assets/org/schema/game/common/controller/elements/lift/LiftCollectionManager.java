package org.schema.game.common.controller.elements.lift;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.NTReceiveInterface;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.network.objects.NetworkLiftInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteVector4i;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class LiftCollectionManager extends ElementCollectionManager<LiftUnit, LiftCollectionManager, VoidElementManager<LiftUnit, LiftCollectionManager>> implements NTReceiveInterface, BlockActivationListenerInterface {

	private final ObjectArrayFIFOQueue<Vector4i> toActivate = new ObjectArrayFIFOQueue<Vector4i>();

	public LiftCollectionManager(
			SegmentController segController, VoidElementManager<LiftUnit, LiftCollectionManager> em) {
		super(ElementKeyMap.LIFT_ELEMENT, segController, em);
	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		//		System.err.println("ACIVATE LIFTER: "+active+"; server "+getSegmentController().isOnServer()+";; "+piece.getAbsoluteIndex()+"; "+piece);
		//		if(getSegmentController().isOnServer()){
		LongOpenHashSet longOpenHashSet = getSegmentController().getControlElementMap().getControllingMap().getAll().get(piece.getAbsoluteIndex());
		//			System.err.println("TRYING LIFT: "+longOpenHashSet);
		if (longOpenHashSet != null) {
			for (long index4 : longOpenHashSet) {
				long absPos = ElementCollection.getPosIndexFrom4(index4);
				//					System.err.println("ACTIVATING:::: "+absPos);
				for (LiftUnit d : getElementCollections()) {
					if (d.contains(absPos)) {
						//							System.err.println("ACTIVATING LIFT: "+absPos+": "+active);
						if (active) {
							d.activate(true);
						} else if (!active) {
							d.deactivate();
						}
						return active ? 1 : 0;
					}
				}
			}
		}
		//		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		if (getSegmentController().isOnServer()) {
			typesThatNeedActivation.add(ElementKeyMap.LIFT_ELEMENT);
		}
	}

	public void activate(Vector3i absolutePos, boolean active) {
		System.err.println("[LIFT] CHECKING FOR LIFT UNIT IN " + getElementCollections().size() + " COLLECTIONS");

		long index = ElementCollection.getIndex(absolutePos);
		for (LiftUnit u : getElementCollections()) {

			if (u.getNeighboringCollection().contains(index)) {
				if (active) {
					System.err.println(getSegmentController().getState() + " [LIFTCOLLECTIONMANAGER] ACTIVATING LIFT " + absolutePos + " " + getSegmentController().getState() + " " + getSegmentController());
					System.err.println("[LIFTCOLLECTIONMANAGER] ACTIVATING " + u + "; this unit size " + u.getNeighboringCollection().size() + " / units " + getElementCollections().size());
					u.activate(false);
				} else {
					u.deactivate();
				}
			}
		}
	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<LiftUnit> getType() {
		return LiftUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public LiftUnit getInstance() {
		return new LiftUnit();
	}

	@Override
	protected void onChangedCollection() {
	}

	@Override
	public void update(Timer timer) {
//		System.err.println("LIFT ELEMT COLLECTIONS: "+getElementCollections().size());

		for (LiftUnit u : getElementCollections()) {
			u.update(timer);
		}
		if (!toActivate.isEmpty()) {
			synchronized (toActivate) {
				Vector4i vector = toActivate.dequeue();
				activate(new Vector3i(vector.x, vector.y, vector.z), vector.w != 0);
			}
		}
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[0];
	}

	@Override
	public String getModuleName() {
		return Lng.str("Lift System");
	}

	@Override
	public void clearCollectionForApply() {
		for (LiftUnit u : getElementCollections()) {
			u.deactivate();
		}
		super.clearCollectionForApply();
	}

	@Override
	public void updateFromNT(NetworkObject networkObject) {

		RemoteBuffer<RemoteVector4i> liftActivate = ((NetworkLiftInterface) networkObject).getLiftActivate();

		for (RemoteVector4i rv : liftActivate.getReceiveBuffer()) {
			Vector4i vector = rv.getVector();
			synchronized (this.toActivate) {
				this.toActivate.enqueue(vector);
			}
			if (getSegmentController().isOnServer()) {
				liftActivate.add(new RemoteVector4i(vector, networkObject));
			}
		}
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
		return false;
	}
}
