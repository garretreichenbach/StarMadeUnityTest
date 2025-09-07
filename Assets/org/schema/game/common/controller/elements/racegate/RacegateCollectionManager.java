package org.schema.game.common.controller.elements.racegate;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.Destination;
import org.schema.game.common.controller.elements.InterControllerCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class RacegateCollectionManager extends ControlBlockElementCollectionManager<RacegateUnit, RacegateCollectionManager, RacegateElementManager> implements InterControllerCollectionManager, PowerConsumer{

	private long lastPopup;
	private Destination raceDestination = new Destination();
	private float powered;
	
	public RacegateCollectionManager(SegmentPiece element,
	                                 SegmentController segController, RacegateElementManager em, Long2ObjectOpenHashMap<String> warpDestinationMapInitial, Long2ObjectOpenHashMap<Vector3i> raceDestinationLocal) {
		super(element, ElementKeyMap.RACE_GATE_MODULE, segController, em);

		assert (element != null);
		assert (warpDestinationMapInitial != null);
		String initialDest = warpDestinationMapInitial.remove(element.getAbsoluteIndex());
		Vector3i initDescLocal = raceDestinationLocal.get(element.getAbsoluteIndex());

		if (initialDest != null) {
			raceDestination.uid = initialDest;
			raceDestination.local.set(initDescLocal);
		}
	}

	@Override
	protected Tag toTagStructurePriv() {
		Tag valid = new Tag(Type.BYTE, null, isValid() ? (byte) 1 : (byte) 0);
		Tag wd = new Tag(Type.STRING, null, raceDestination.uid);
		Tag ld = new Tag(Type.VECTOR3i, null, raceDestination.local);

		return new Tag(Type.STRUCT, null, new Tag[]{valid, wd, ld, FinishTag.INST});
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
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
	protected Class<RacegateUnit> getType() {
		return RacegateUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}

	@Override
	public RacegateUnit getInstance() {
		return new RacegateUnit();
	}

	@Override
	protected void onChangedCollection() {
		if (!getSegmentController().isOnServer()) {
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getGuiDrawer()
					.managerChanged(this);
		}
	}

	@Override
	public void update(Timer timer) {
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
					RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Invalid Racegate Build\nMust be one 2D loop!"), 1f, 0.1f, 0.1f, 1f);
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
		return Lng.str("Race Gate System");
	}

	public boolean isValid() {
		return getElementCollections().size() == 1 && getElementCollections().get(0).isValid();
	}

	@Override
	public String getWarpDestinationUID() {
		return raceDestination.uid;
	}

	public void setDestination(String uidDest, Vector3i local) {
		raceDestination.uid = uidDest;
		raceDestination.local.set(local);
	}


	@Override
	public int getWarpType() {
		return FTLConnection.TYPE_RACE_WAY;
	}

	@Override
	public int getWarpPermission() {
		return 0;
	}

	@Override
	public Vector3i getLocalDestination() {
		return raceDestination.local;
	}

	public Destination getNextGate() {
		return raceDestination;
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return this.powered;
	}
	private double getReactorPowerUsage(){
		return 0.1;
	}
	@Override
	public double getPowerConsumedPerSecondResting() {
		return getReactorPowerUsage();
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		return getReactorPowerUsage();
	}
	@Override
	public boolean isPowerCharging(long curTime) {
		return false;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.OTHERS;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging,
			float poweredResting) {
		
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}

	@Override
	public void dischargeFully() {
	}
}
