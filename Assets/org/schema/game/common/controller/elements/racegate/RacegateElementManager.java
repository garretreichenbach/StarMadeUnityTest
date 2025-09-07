package org.schema.game.common.controller.elements.racegate;

import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.InterControllerConnectionManager;
import org.schema.game.common.controller.elements.NTReceiveInterface;
import org.schema.game.common.controller.elements.NTSenderInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class RacegateElementManager extends UsableControllableElementManager<RacegateUnit, RacegateCollectionManager, RacegateElementManager> implements
		NTSenderInterface, NTReceiveInterface, InterControllerConnectionManager {

	@ConfigurationElement(name = "PowerNeededPerGateBlock")
	public static float POWER_CONST_NEEDED_PER_BLOCK = 50;

	@ConfigurationElement(name = "PowerNeededPerMass")
	public static float POWER_NEEDED_PER_MASS = 50;


	private final Long2ObjectOpenHashMap<String> raceDestinationMapInitial;

	private Long2ObjectOpenHashMap<Vector3i> raceDestinationLocal;

	public RacegateElementManager(final SegmentController segmentController, Long2ObjectOpenHashMap<String> warpDestinationMap, Long2ObjectOpenHashMap<Vector3i> raceDestinationLocal) {
		super(ElementKeyMap.RACE_GATE_CONTROLLER, ElementKeyMap.RACE_GATE_MODULE, segmentController);
		this.raceDestinationMapInitial = warpDestinationMap;
		this.raceDestinationLocal = raceDestinationLocal;
		assert (raceDestinationMapInitial != null);
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
	public ControllerManagerGUI getGUIUnitValues(RacegateUnit firingUnit,
	                                             RacegateCollectionManager col, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {

		return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Race Gate Unit"), firingUnit);
	}

	@Override
	public boolean canHandle(ControllerStateInterface unit) {
		return false;
	}
	
	@Override
	protected String getTag() {
		return "racegate";
	}

	@Override
	public RacegateCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<RacegateCollectionManager> clazz) {

		return new RacegateCollectionManager(position, getSegmentController(), this, raceDestinationMapInitial, raceDestinationLocal);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Race Gate System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}
}
