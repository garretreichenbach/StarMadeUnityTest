package org.schema.game.common.controller.elements.warpgate;


import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.Destination;
import org.schema.game.common.controller.elements.InterControllerCollectionManager;
import org.schema.game.common.controller.elements.StationaryManagerContainer;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.FTLConnection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Vector3f;

public class WarpgateCollectionManager extends ControlBlockElementCollectionManager<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> implements InterControllerCollectionManager, PowerConsumer{

	private long lastPopup;
	private Destination warpDestination = new Destination();
	private float powered;
	private boolean active = true;

	public WarpgateCollectionManager(SegmentPiece element,
	                                 SegmentController segController, WarpgateElementManager em, Long2ObjectOpenHashMap<String> warpDestinationMapInitial, Long2ObjectOpenHashMap<Vector3i> warpLocal) {
		super(element, ElementKeyMap.WARP_GATE_MODULE, segController, em);

		assert (element != null);
		assert (warpDestinationMapInitial != null);
		String initialDest = warpDestinationMapInitial.remove(element.getAbsoluteIndex());

		if (initialDest != null) {
			this.warpDestination.uid = initialDest;
			this.warpDestination.local.set(warpLocal.get(element.getAbsoluteIndex()));
		}
	}

	@Override
	protected Tag toTagStructurePriv() {
		Tag valid = new Tag(Type.BYTE, null, isValid() ? (byte) 1 : (byte) 0);
		Tag wd = new Tag(Type.STRING, null, warpDestination.uid);
		Tag ld = new Tag(Type.VECTOR3i, null, this.warpDestination.local);
		Tag isActive = new Tag(Type.BYTE, null, active ? (byte) 1 : (byte) 0);
		return new Tag(Type.STRUCT, null, new Tag[]{valid, wd, ld, isActive, FinishTag.INST});
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
	protected Class<WarpgateUnit> getType() {
		return WarpgateUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return true;
	}
	
	@Override
	public WarpgateUnit getInstance() {
		return new WarpgateUnit();
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
					RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Invalid Warpgate Build\nMust be one 2D loop!"), 1f, 0.1f, 0.1f, 1f);
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
		return Lng.str("Warp Gate System");
	}

	public boolean isValid() {
		return getElementCollections().size() == 1 && getElementCollections().getFirst().isValid();
	}

	@Override
	public String getWarpDestinationUID() {
		return this.warpDestination.uid;
	}

	public void setDestination(String uidDest, Vector3i local) {
		warpDestination.uid = uidDest;
		warpDestination.local.set(local);
	}

	@Override
	public int getWarpType() {
		return FTLConnection.TYPE_WARP_GATE;
	}

	@Override
	public int getWarpPermission() {
		return 0;
	}

	@Override
	public Vector3i getLocalDestination() {
		return warpDestination.local;
	}

	private double getReactorPowerUsage(){
		double pw = (double)WarpgateElementManager.REACTOR_POWER_CONST_NEEDED_PER_BLOCK * (double)getTotalSize();
		pw = Math.pow(pw, WarpgateElementManager.REACTOR_POWER_POW);
		return getConfigManager().apply(StatusEffectType.WARP_POWER_EFFICIENCY, pw);
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
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return this.powered;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
	}
	public int getMaxDistance(){
		return (int) (getConfigManager().apply(StatusEffectType.WARP_DISTANCE, WarpgateElementManager.DISTANCE_IN_SECTORS));
	}
	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.WARP_GATE;
	}
	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public void dischargeFully() {
	}

	@Override
	public void onLogicActivate(SegmentPiece selfBlock, boolean oldActive, Timer timer) {
		//TODO play sound
	}

	public void setActive(boolean v) {
		active = v;
		if(getContainer() instanceof StationaryManagerContainer){
			((StationaryManagerContainer<?>)getContainer()).setWarpGateInitialActivation(getControllerIndex(),v);
		}
		if(!isOnServer()){
			Transform t = new Transform();
			t.setIdentity();
			Vector3i p = getControllerPos();
			t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
			getSegmentController().getWorldTransform().transform(t.origin);
			Vector3f color = new Vector3f();
			color.x = v ? 0.1f : 1.0f;
			color.y = v ? 0.3f : 0.75f;
			color.z = v ? 1.0f : 0.1f;
			RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Warpgate %s!", v ? Lng.str("Activating") : Lng.str("Deactivating")), color.x,color.y,color.z, 1f);
			raisingIndication.speed = 0.1f;
			raisingIndication.lifetime = 3.0f;
			HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
		}
	}

	@Override
	protected void onRemovedCollection(long absPos, WarpgateCollectionManager instance) {
		super.onRemovedCollection(absPos, instance);
		if(getContainer() instanceof StationaryManagerContainer){
			((StationaryManagerContainer<?>)getContainer()).resetWarpGateInitialActivation(getControllerIndex());
		}
	}

	public boolean isActive() {
		return active;
	}
}
