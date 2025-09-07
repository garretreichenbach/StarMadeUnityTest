package org.schema.game.common.controller.elements.warpgate;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;

import com.bulletphysics.linearmath.Transform;
import org.schema.schine.common.language.Lng;

public class WarpgateElementManager extends UsableControllableElementManager<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> implements
		NTSenderInterface, NTReceiveInterface, InterControllerConnectionManager, BlockActivationListenerInterface {

	@ConfigurationElement(name = "PowerNeededPerGateBlock")
	public static float POWER_CONST_NEEDED_PER_BLOCK = 50;

	@ConfigurationElement(name = "PowerNeededPerMass")
	public static float POWER_NEEDED_PER_MASS = 50;

	@ConfigurationElement(name = "DistanceInSectors")
	public static float DISTANCE_IN_SECTORS = 128f;

	@ConfigurationElement(name = "ReactorPowerNeededPerBlockPerSec")
	public static float REACTOR_POWER_CONST_NEEDED_PER_BLOCK = 50;

	@ConfigurationElement(name = "ReactorPowerNeededPow")
	public static float REACTOR_POWER_POW = 1.1f;


	private final Long2ObjectOpenHashMap<String> warpDestinationMapInitial;

	private Long2ObjectOpenHashMap<Vector3i> warpLocal;

	public WarpgateElementManager(final SegmentController segmentController, Long2ObjectOpenHashMap<String> warpDestinationMap, Long2ObjectOpenHashMap<Vector3i> warpLocal) {
		super(ElementKeyMap.WARP_GATE_CONTROLLER, ElementKeyMap.WARP_GATE_MODULE, segmentController);
		this.warpDestinationMapInitial = warpDestinationMap;
		this.warpLocal = warpLocal;
		assert (warpDestinationMapInitial != null);
	}

	@Override
	public void updateFromNT(NetworkObject o) {
	}

	@Override
	public void updateToFullNT(NetworkObject networkObject) {
		if (getSegmentController().isOnServer()) {
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.missile.MissileElementManager#getGUIUnitValues(org.schema.game.common.controller.elements.missile.MissileUnit, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager, org.schema.game.common.controller.elements.ControlBlockElementCollectionManager)
	 */
	@Override
	public ControllerManagerGUI getGUIUnitValues(WarpgateUnit firingUnit,
	                                             WarpgateCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Warp Gate Unit"), firingUnit);
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		return false;
	}

	@Override
	protected String getTag() {
		return "warpgate";
	}

	@Override
	public WarpgateCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<WarpgateCollectionManager> clazz) {

		WarpgateCollectionManager result = new WarpgateCollectionManager(position, getSegmentController(), this, warpDestinationMapInitial, warpLocal);
		if(getManagerContainer() instanceof StationaryManagerContainer){
			StationaryManagerContainer<?> mc = (StationaryManagerContainer<?>) getManagerContainer();
			result.setActive(mc.getWarpGateInitialActivation(position.getAbsoluteIndex()));
		}
		return result;
	}

	@Override
	public String getManagerName() {
		return Lng.str("Warp Gate System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}

	@Override
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
		WarpgateCollectionManager m = getCollectionManagersMap().get(piece.getAbsoluteIndex());
		if(m != null) {
			m.setActive(active);
		}
		return active ? 1 : 0;
	}

	@Override
	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
		if (getSegmentController().isOnServer()) typesThatNeedActivation.add(ElementKeyMap.WARP_GATE_CONTROLLER);
	}

	@Override
	public boolean isHandlingActivationForType(short type) {
		boolean result = type==ElementKeyMap.WARP_GATE_CONTROLLER;
		if(result && isOnServer())
			System.err.println("[SERVER] Handling activation on WG " + this);
		return result;
	}
}
