package org.schema.game.server.ai.program.searchanddestroy.states;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

public class ShipMovingToSector extends ShipGameState {

	/**
	 *
	 */
	
	public float year;
	public Vector3i relSystemPos = new Vector3i();
	private long started;
	private Vector3f targetMod = new Vector3f();
	private Transform trans = new Transform();
	private Transform transR = new Transform();
	private Vector3i relSectorPos = new Vector3i();
	private Vector3f absSectorPos = new Vector3f();
	private Vector3f movingDir = new Vector3f();
	private Vector3f absCenterPos = new Vector3f();

	public ShipMovingToSector(AiEntityStateInterface gObj) {
		super(gObj);
	}

	/**
	 * @return the movingDir
	 */
	public Vector3f getMovingDir() {
		return movingDir;
	}

	/**
	 * @param movingDir the movingDir to set
	 */
	public void setMovingDir(Vector3f movingDir) {
		this.movingDir = movingDir;
	}

	@Override
	public boolean onEnter() {

		int random = Universe.getRandom().nextInt(7);
		switch(random) {
			case (0) -> targetMod.set(0, 1, 0);
			case (1) -> targetMod.set(0, -1, 0);
			case (2) -> targetMod.set(1, 0, 0);
			case (3) -> targetMod.set(-1, 0, 0);
			case (4) -> targetMod.set(0, 0, 1);
			case (5) -> targetMod.set(0, 0, -1);
			case (6) -> targetMod.set(0, 0, 0);
		}

		targetMod.scale((float) (Math.random() * 300));
		started = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		GameServerState state = (GameServerState) getEntity().getState();

		Sector sector = state.getUniverse().getSector(getEntity().getSectorId());

		Vector3i secPos = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getSectorTarget();

		if (sector != null) {

			Vector3i pPos = new Vector3i(secPos);
			pPos.sub(sector.pos);

			long startTime = state.getController().calculateStartTime();

			Vector3i sysPos = StellarSystem.getPosFromSector(secPos, new Vector3i());
			if (StellarSystem.isStarSystem(secPos)) {

				float pc = ((GameStateInterface) state).getGameState().getRotationProgession();
				;
				if (StellarSystem.isStarSystem(sector.pos)) {
					year = pc;
				} else {
					year = 0;
				}

			} else {
				//			System.err.println("SECTOR "+secPos+" IS NOT A STAR");
				year = 0;
			}
			sysPos.scale(VoidSystem.SYSTEM_SIZE);
			sysPos.add(VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2, VoidSystem.SYSTEM_SIZE / 2);
			sysPos.sub(sector.pos);
			relSystemPos.set(sysPos);

			relSectorPos.set(pPos);

			absSectorPos.set(
					relSectorPos.x * ((GameStateInterface) getEntity().getState()).getSectorSize(),
					relSectorPos.y * ((GameStateInterface) getEntity().getState()).getSectorSize(),
					relSectorPos.z * ((GameStateInterface) getEntity().getState()).getSectorSize());

			absCenterPos.set(
					(relSystemPos.x) * ((GameStateInterface) getEntity().getState()).getSectorSize(),
					(relSystemPos.y) * ((GameStateInterface) getEntity().getState()).getSectorSize(),
					(relSystemPos.z) * ((GameStateInterface) getEntity().getState()).getSectorSize());
			//		System.err.println("SEC CEN "+relSectorPos+"; "+relSystemPos);
			trans.setIdentity();
			transR.setIdentity();

			if (relSystemPos.length() > 0) {
				if (relSectorPos.length() > 0) {
					trans.origin.add(absCenterPos);
					trans.basis.rotX((FastMath.TWO_PI) * year);
					Vector3f d = new Vector3f();
					d.sub(absSectorPos, absCenterPos);
					trans.origin.add(d);

					trans.basis.transform(trans.origin);
				}

			} else {
				trans.basis.rotX((FastMath.TWO_PI * 2) * year);
				trans.origin.set(absSectorPos);
				trans.basis.transform(trans.origin);
			}

		}

		Vector3f from = getEntity().getWorldTransform().origin;

		from.add(targetMod);

		movingDir.sub(trans.origin, from);

		if (System.currentTimeMillis() - started > 1000) {
			stateTransition(Transition.SEARCH_FOR_TARGET);
		}

		return false;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		getEntity().getNetworkObject().targetPosition.set(0, 0, 0);

		Vector3f moveDir = new Vector3f();
		moveDir.set(movingDir);
		getEntity().getNetworkObject().moveDir.set(moveDir);
		s.moveTo(timer, moveDir, true);		
	}

}
