package org.schema.game.server.controller.world.factory.regions.hooks;

import javax.vecmath.Quat4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.generator.StationTurretCreatorThread;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.world.factory.regions.DockingStation;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

public class SpawnTurretRegionHook extends RegionHook<DockingStation> {

	private int difficulty;

	public SpawnTurretRegionHook(int difficulty) {
		super();
		this.difficulty = difficulty;
	}

	@Override
	public void execute() {
		System.err.println("[SERVER] EXECUTING REGION HOOK: " + this);

		SegmentController segmentController = onCreatedSegment.getSegmentController();
		GameServerState state = (GameServerState) segmentController.getState();

		Ship turret = new Ship(state);
		turret.setId(state.getNextFreeObjectId());
		turret.setCreatorThread(new StationTurretCreatorThread(turret, difficulty));
		turret.setFactionId(segmentController.getFactionId());
		turret.setSectorId(segmentController.getSectorId());
		turret.setUniqueIdentifier("ENTITY_SHIP_AITURRET_" + System.currentTimeMillis());
		turret.getMinPos().set(new Vector3i(-2, -2, -2));
		turret.getMaxPos().set(new Vector3i(2, 2, 2));
		turret.setRealName("Turret");
		if (segmentController.getFactionId() != FactionManager.ID_NEUTRAL) {
			try {
				turret.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", true);
				turret.getAiConfiguration().get(Types.TYPE).switchSetting("Turret", true);
				turret.getAiConfiguration().get(Types.ACTIVE).switchSetting("true", true);

				turret.getAiConfiguration().applyServerSettings();
			} catch (StateParameterNotFoundException e) {
				e.printStackTrace();
			}
		}
		turret.initialize();
		turret.getInitialTransform().setIdentity();
		turret.getInitialTransform().origin.set(region.controllerPosition.x - SegmentData.SEG_HALF, region.controllerPosition.y - SegmentData.SEG_HALF, region.controllerPosition.z - SegmentData.SEG_HALF);
		Quat4f rot = new Quat4f(0, 0, 0, 1);
		turret.getDockingController().requestDelayedDock(segmentController.getUniqueIdentifier(), region.controllerPosition, rot, 0);

		state.getController().getSynchController().addNewSynchronizedObjectQueued(turret);
	}

}
