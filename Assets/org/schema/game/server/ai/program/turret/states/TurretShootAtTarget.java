package org.schema.game.server.ai.program.turret.states;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.turret.TurretShipAIEntity;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

public class TurretShootAtTarget extends ShootAtTarget {


	public TurretShootAtTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity,
			ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().moveDir.set(new Vector3f(0, 0, 0));
		s.orientate(timer, Quat4fTools.getNewQuat(((TurretShipAIEntity)s).orientateDir.x, ((TurretShipAIEntity)s).orientateDir.y, ((TurretShipAIEntity)s).orientateDir.z, 0));

		Vector3f targetPosition = new Vector3f();
		Vector3f targetVelocity = new Vector3f();

		targetPosition.set(getTargetPosition());
		targetVelocity.set(getTargetVelocity());
		int targetId = (getTargetId());
		byte targetType = (getTargetType());

		if (targetPosition.length() > 0) {
			getEntity().getNetworkObject().targetPosition.set(targetPosition);
			getEntity().getNetworkObject().targetVelocity.set(targetVelocity);
			getEntity().getNetworkObject().targetId.set(targetId);
			getEntity().getNetworkObject().targetType.set(targetType);

			s.doShooting(unit, timer);

			getTargetPosition().set(0, 0, 0);

			
			stateTransition(Transition.SHOOTING_COMPLETED);
		}
		
	}

}
