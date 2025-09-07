package org.schema.game.server.ai.program.turret.states;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.turret.TurretShipAIEntity;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class EngagingTurretTarget extends ShipGameState implements ShootingProcessInterface {

	private static final long MAX_ENGAGE_TIME = 5000;
	/**
	 *
	 */
	
	private final Vector3f rotDir = new Vector3f();
	private long startedToEngage;
	private Vector3f tmp = new Vector3f();
	private String descString = "";

	public EngagingTurretTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	public Vector3f getRotDir() {
		return rotDir;
	}

	@Override
	public boolean onEnter() {
		rotDir.set(0, 0, 0);
		startedToEngage = System.currentTimeMillis();
		descString = "STARTENGAGE";
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		if (getEntityState().getState().getUpdateTime() - startedToEngage > MAX_ENGAGE_TIME ) {
			stateTransition(Transition.RESTART);
			return false;
		}
		if(getAIConfig().get(Types.MANUAL).isOn()){
			final SegmentController root = getEntity().railController.getRoot();
			ManagerContainer<?> o = ((ManagedSegmentController<?>)root).getManagerContainer();
			
			if(root instanceof PlayerControllable && !(((PlayerControllable)root).getAttachedPlayers()).isEmpty()){
				for(ControllerStateUnit u : ((PlayerControllable)root).getAttachedPlayers().get(0).getControllerState().getUnits()){
					PlayerUsableInterface playerUsable = o.getPlayerUsable(PlayerUsableInterface.USABLE_ID_SHOOT_TURRETS);
					if(playerUsable != null){
						if(u.isSelected(playerUsable, ((ManagedSegmentController<?>)root).getManagerContainer()) && u.playerControllable == root){
							stateTransition(Transition.RESTART);
							return false;
						}
					}
				}
			}
		}
		SimpleGameObject target = ((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget();

		if (target != null) {

			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);
			if (!checkTarget(target, this)) {
//				System.err.println("TURRET TARGET CHECK FAILED: "+target);
				stateTransition(Transition.RESTART);
				return false;
			}
			Vector3f from = getEntity().getWorldTransform().origin;

			Vector3f to = new Vector3f(target.getClientTransformCenterOfMass(serverTmp).origin);

			rotDir.sub(to, from);
			Vector3f targetAngularVelocity = new Vector3f();
			if(target instanceof SegmentController) {
				SegmentController targetController = (SegmentController) target;
				if(targetController.getPhysicsObject() != null) targetController.getPhysicsObject().getAngularVelocity(targetAngularVelocity);
			}

			if (target.getLinearVelocity(tmp).lengthSquared() > 0) {
				boolean hasWeaponsBesidesBeam = true;
				if (getEntity() != null) {
					ShipManagerContainer sc = getEntity().getManagerContainer();
					hasWeaponsBesidesBeam = !sc.getBeam().hasAtLeastOneCoreUnit() || sc.getWeapon().hasAtLeastOneCoreUnit() || sc.getMissile().hasAtLeastOneCoreUnit();
				}
				if (hasWeaponsBesidesBeam) {
					//if the shot goes straight. predict target's position
					Vector3f predictedPath = Vector3fTools.predictPoint(to, target.getLinearVelocity(tmp), targetAngularVelocity, getEntityState().getAntiMissileShootingSpeed(), from);
					rotDir.set(predictedPath);
				}
			}

			Vector3f mNorm = new Vector3f(rotDir);
			mNorm.normalize();
			Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(), getEntity().getWorldTransform());
			
			Vector3f diff = new Vector3f();
			
			diff.sub(forwardVector, mNorm);

			if (mNorm.epsilonEquals(forwardVector, ShipAIEntity.EPSILON_RANGE_TURRET)) {
				stateTransition(Transition.IN_SHOOTING_POSITION);
				descString = "INRANGE: "+mNorm+"; "+forwardVector;
				return true;
			}else{
				descString = "OORANGE: "+mNorm+"; "+forwardVector;
			}
		} else {
			System.err.println("[AI] " + getEntity() + " HAS NO TARGET: resetting");
			stateTransition(Transition.RESTART);
		}
		return false;
	}

	@Override
	public String getDescString() {
		return descString;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().moveDir.set(new Vector3f(0, 0, 0));
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));
		getEntity().getNetworkObject().targetVelocity.set(new Vector3f(0, 0, 0));

		((TurretShipAIEntity)s).orientateDir.set(rotDir);
		getEntity().getNetworkObject().orientationDir.set(((TurretShipAIEntity)s).orientateDir, 0);
		s.orientate(timer, Quat4fTools.getNewQuat(((TurretShipAIEntity)s).orientateDir.x, ((TurretShipAIEntity)s).orientateDir.y, ((TurretShipAIEntity)s).orientateDir.z, 0));		
	}

}
