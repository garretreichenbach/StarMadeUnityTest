package org.schema.game.server.ai;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.creature.AIPlayer;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.weapon.LaserWeapon;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.ShootingStateInterface;
import org.schema.game.server.ai.program.creature.NPCProgram;
import org.schema.game.server.ai.program.creature.character.states.CharacterEngaging;
import org.schema.game.server.ai.program.creature.character.states.CharacterWaitingForPathPlot;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AIGameEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

public abstract class CreatureAIEntity<A extends AIPlayer, E extends AICreature<A>> extends AIGameEntityState<E> {

	public Vector3f lastPath;
	private Vector3f currentMoveTarget = new Vector3f();
	private SimpleTransformableSendableObject followTarget;
	private Vector3f gotoTarget = new Vector3f();
	private SimpleTransformableSendableObject attackTarget;
	public CreatureAIEntity(String name, E s) {
		super(name, s);
	}

	public boolean canPlotPath() {
		boolean canPlot = getCurrentProgram().getOtherMachine("MOVE") != null && getCurrentProgram().getOtherMachine("MOVE").getFsm().getCurrentState() instanceof CharacterWaitingForPathPlot;
//		System.err.println("OTHER: "+canPlot+";;; "+getCurrentProgram().getOtherMachine("MOVE").getFsm().getCurrentState());
		return canPlot;
	}

	public void plotSecondaryAbsolutePath(Vector3f pos) throws FSMException {

		Vector3i blockPos = SimpleTransformableSendableObject.getBlockPositionRelativeTo(pos, getEntity().getAffinity(), new Vector3i());
		getEntity().getOwnerState().plotPath(blockPos);
		if (getCurrentProgram().getOtherMachine("MOVE") != null) {
			getCurrentProgram().getOtherMachine("MOVE").getFsm().getCurrentState().stateTransition(Transition.MOVE);
		}
		this.lastPath = pos;
	}

	public void plotSecondaryPath() throws FSMException {
		getEntity().getOwnerState().plotInstantPath();
		if (getCurrentProgram().getOtherMachine("MOVE") != null) {
			getCurrentProgram().getOtherMachine("MOVE").getFsm().getCurrentState().stateTransition(Transition.MOVE);
		}
		this.lastPath = new Vector3f();
	}

	public void cancelMoveCommad() throws FSMException {
		if (getCurrentProgram().getOtherMachine("MOVE") != null) {
			getCurrentProgram().getOtherMachine("MOVE").getFsm().getCurrentState().stateTransition(Transition.RESTART);
		}
		this.lastPath = new Vector3f();
	}

	public Vector3f getCurrentMoveTarget() {
		return currentMoveTarget;
	}

	@Override
	public String toString() {
		String r = name;
		if (getCurrentProgram() == null) {
			return r + "[NULL_PROGRAM]";
		}
		String s = r + "; Aff: " + getEntity().getAffinity() + "; HP: " + getEntity().getOwnerState().getHealth();
		for (FiniteStateMachine m : getCurrentProgram().getMachines()) {

			if (m.getFsm().getCurrentState() == null) {

				s += "\n->[" + m.getClass().getSimpleName() + "->NULL_STATE]";
			}
			s += "\n->[" + m.getClass().getSimpleName() + "->" + m.getFsm().getCurrentState().getClass().getSimpleName() + "]";

		}
		return s;
	}

	public abstract void start();

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AIGameEntityState#updateAIClient(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateAIClient(Timer timer) {

		Vector3f targetPosition = new Vector3f();
		Vector3f targetVelocity = new Vector3f();
		targetPosition.set(getEntity().getNetworkObject().targetPosition.getVector());
		targetVelocity.set(getEntity().getNetworkObject().targetVelocity.getVector());
		int targetId = getEntity().getNetworkObject().targetId.getInt();
		byte targetType = getEntity().getNetworkObject().targetType.getByte();
		if (targetPosition.lengthSquared() > 0) {
			shoot(targetId, targetType, targetPosition, targetVelocity, timer);
		} else {
			if (targetId == -1) {
				getEntity().getLookDir().set(0, 0, 0);

			}
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.AIGameEntityState#updateAIServer(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateAIServer(Timer timer) throws FSMException {

		State currentState = getCurrentProgram().getOtherMachine("ATT").getFsm().getCurrentState();

		if (currentState instanceof CharacterEngaging) {

		} else if (currentState instanceof ShootingStateInterface) {
			//			getEntity().getNetworkObject().moveDir.set(new Vector3f(0,0,0));
			//			orientate(timer, orientateDir);

			Vector3f targetPosition = new Vector3f();
			Vector3f targetVelocity = new Vector3f();
			int targetId = (((ShootingStateInterface) currentState).getTargetId());
			byte targetType = (((ShootingStateInterface) currentState).getTargetType());
			targetPosition.set(((ShootingStateInterface) currentState).getTargetPosition());
			targetVelocity.set(((ShootingStateInterface) currentState).getTargetVelocity());

			if (targetPosition.lengthSquared() > 0) {
				getEntity().getNetworkObject().targetPosition.set(targetPosition);
				getEntity().getNetworkObject().targetVelocity.set(targetVelocity);
				getEntity().getNetworkObject().targetId.set(targetId);
				getEntity().getNetworkObject().targetType.set(targetType);

				//				if(targetPosition.length() < ShipAIEntity.SHOOTING_RANGE){
				shoot(targetId, targetType, targetPosition, targetVelocity, timer);
				//				}
				//				System.err.println("TARGET POSITION AQUIRED: "+targetPosition);
				//				shootDumbMissiles(targetId, targetPosition, targetVelocity);
				//				shootHeatMissiles(targetId, targetPosition, targetVelocity);
				//				shootFifoMissiles(targetId, targetPosition, targetVelocity);

				((ShootingStateInterface) currentState).getTargetPosition().set(0, 0, 0);

				currentState.stateTransition(Transition.SHOOTING_COMPLETED);
			} else {
				getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));
				getEntity().getNetworkObject().targetVelocity.set(new Vector3f(0, 0, 0));
				getEntity().getNetworkObject().targetId.set(-2);
				getEntity().getNetworkObject().targetType.set((byte) -1);
			}
		} else {
			getEntity().getLookDir().set(0, 0, 0);
			getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));
			getEntity().getNetworkObject().targetId.set(-1);
			getEntity().getNetworkObject().targetType.set((byte) -1);
			getEntity().getOwnerState().setSelectedBuildSlot(0);
		}

	}

	public void shoot(int targetId, byte targetType, Vector3f targetDisplacement, Vector3f targetVelocity, Timer timer) {

		Object sTarget = null;
		if (targetType == SimpleGameObject.SIMPLE_TRANSFORMABLE_SENSABLE_OBJECT) {
			sTarget = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(targetId);
		} else if (targetType == SimpleGameObject.MISSILE) {
			if (isOnServer()) {
				sTarget = ((GameServerState) getState()).getController().getMissileController().getMissileManager().getMissiles().get((short) targetId);
			} else {
				sTarget = ((GameClientState) getState()).getController().getClientMissileManager().getMissile((short) targetId);
			}

		}

//		Sendable sTarget = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(targetId);
		if (sTarget == null || !(sTarget instanceof SimpleGameObject)) {
			System.err.println("[AI][SHOOT] " + getEntity().getState() + " Excpetion: target id " + targetId + " does not belong to an entity: " + sTarget);
			return;
		}
//		sdf

		long curTime = System.currentTimeMillis();
		SimpleGameObject target = (SimpleGameObject) sTarget;

		//		System.err.println(target.getState()+"; "+getEntity()+" SHOOTING AT "+target);
		Vector3f targetPosition = new Vector3f();
		if (getEntity().isOnServer()) {
			target.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);
			targetPosition.set(target.getClientTransform().origin);
		} else {
			targetPosition.set(target.getWorldTransformOnClient().origin);
		}
		Vector3f dir = new Vector3f();
		dir.sub(targetPosition, getEntity().getWorldTransform().origin);
		dir.normalize();

		getEntity().getLookDir().set(dir);
		Inventory inventory = getEntity().getOwnerState().getInventory();

		if (isOnServer()) {
			int weaponSlot = inventory.getFirstSlotMetatype(MetaObjectType.WEAPON.type);
			getEntity().getOwnerState().setSelectedBuildSlot(weaponSlot);
		}

		int weaponSlot = getEntity().getOwnerState().getSelectedBuildSlot();
		LaserWeapon wep;
		if (weaponSlot >= 0 && (wep = (LaserWeapon) ((MetaObjectState) getState()).getMetaObjectManager().getObject(inventory.getMeta(weaponSlot))) != null) {
			getEntity().getOwnerState().onFiredWeapon(wep);
			wep.fire(getEntity(), getEntity().getOwnerState(), dir, true, false, timer);
		} else {
			if (getEntity().getOwnerState().hasNaturalWeapon()) {
				getEntity().getOwnerState().fireNaturalWeapon(getEntity(), getEntity().getOwnerState(), dir);
			}
			//			System.err.println("NO WEAPON FOUND IN INVENTORY");
		}
	}

	public void setGotoOrderPosition(String targetAffinity, float targetX, float targetY, float targetZ) throws CannotReachTargetException, AlreadyAtTargetException {
		SimpleTransformableSendableObject affinity = null;

		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof SimpleTransformableSendableObject && targetAffinity.equals(((SimpleTransformableSendableObject) s).getUniqueIdentifier())) {
				affinity = ((SimpleTransformableSendableObject) s);
			}
		}
		this.gotoTarget.set(targetX, targetY, targetZ);
		if (affinity != null) {
			if (getEntity().getAffinity() != affinity) {
				getEntity().setAffinity(affinity);
			}
			boolean canGoThere = false;
			if (affinity instanceof SegmentController) {
				Vector3i point = new Vector3i(targetX + SegmentData.SEG_HALF, targetY + SegmentData.SEG_HALF, targetZ + SegmentData.SEG_HALF);
				for (int i = 0; i < 6; i++) {
					Vector3i from = new Vector3i(targetX + SegmentData.SEG_HALF, targetY + SegmentData.SEG_HALF, targetZ + SegmentData.SEG_HALF);
					from.add(Element.DIRECTIONSi[i]);
					if (((GameServerState) getEntity().getState()).getController().getSegmentPathFinder().getIc().canTravelPoint(point, from, (SegmentController) affinity)) {
						this.gotoTarget.set(point.x - SegmentData.SEG_HALF, point.y - SegmentData.SEG_HALF, point.z - SegmentData.SEG_HALF);
						canGoThere = true;
						break;
					}
				}
				if (!canGoThere) {
					for (int y = 0; y < 6; y++) {
						point = new Vector3i(targetX + SegmentData.SEG_HALF, targetY + SegmentData.SEG_HALF, targetZ + SegmentData.SEG_HALF);
						point.add(Element.DIRECTIONSi[y]);
						boolean ok = false;
						for (int i = 0; i < 6; i++) {
							Vector3i from = new Vector3i(targetX + SegmentData.SEG_HALF, targetY + SegmentData.SEG_HALF, targetZ + SegmentData.SEG_HALF);
							from.add(Element.DIRECTIONSi[i]);
							if (((GameServerState) getEntity().getState()).getController().getSegmentPathFinder().getIc().canTravelPoint(point, from, (SegmentController) affinity)) {
								ok = true;
								break;
							}
						}
						if (ok) {
							this.gotoTarget.set(point.x - SegmentData.SEG_HALF, point.y - SegmentData.SEG_HALF, point.z - SegmentData.SEG_HALF);
							canGoThere = true;
							break;
						}
					}
				}
			} else {
				canGoThere = true;
			}

			if (!canGoThere) {
				throw new CannotReachTargetException();
			}
			Vector3i posInAffinity = getEntity().getPosInAffinity(new Vector3i());
			if (posInAffinity.equals((int) gotoTarget.x + SegmentData.SEG_HALF, (int) gotoTarget.y + SegmentData.SEG_HALF, (int) gotoTarget.z + SegmentData.SEG_HALF)) {
				throw new AlreadyAtTargetException();
			}
			affinity.getWorldTransform().transform(this.gotoTarget);
			//move command will need absolute pos every time: if affinity is set, its converted into relative coordinates

		} else if (targetAffinity != null) {
			((GameServerState) getState()).getController().broadcastMessageAdmin(Lng.astr("WARNING: Object in move command:\n%s has not been found", attackTarget), ServerMessage.MESSAGE_TYPE_SIMPLE);
		}
		System.err.println("[AI] GOTO TARGET: " + gotoTarget + "; affinity: " + affinity);
	}

	/**
	 * @return the followTarget
	 */
	public SimpleTransformableSendableObject getFollowTarget() {
		return followTarget;
	}

	public void setFollowTarget(String followTargetUID) {
		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof SimpleTransformableSendableObject && followTargetUID.equals(((SimpleTransformableSendableObject) s).getUniqueIdentifier())) {
				this.followTarget = ((SimpleTransformableSendableObject) s);
				return;
			}
		}
		this.followTarget = null;

	}

	/**
	 * @return the gotoTarget
	 */
	public Vector3f getGotoTarget() {
		return gotoTarget;
	}

	/**
	 * @param gotoTarget the gotoTarget to set
	 */
	public void setGotoTarget(Vector3f gotoTarget) {
		this.gotoTarget = gotoTarget;
	}

	/**
	 * @return the attackTarget
	 */
	public SimpleTransformableSendableObject getAttackTarget() {
		return attackTarget;
	}

	public void setAttackTarget(String followTargetUID) {
		for (Sendable s : getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if (s instanceof SimpleTransformableSendableObject && followTargetUID.equals(((SimpleTransformableSendableObject) s).getUniqueIdentifier())) {
				this.attackTarget = ((SimpleTransformableSendableObject) s);
				return;
			}
		}
		this.attackTarget = null;

	}

	public void attackSecondary(SimpleTransformableSendableObject attackTarget) {
		try {
			((NPCProgram) getCurrentProgram()).attack(attackTarget);
		} catch (FSMException e) {
			e.printStackTrace();
		}

	}

	public void makeMoveTarget() throws CannotReachTargetException, AlreadyAtTargetException {
		float targetX = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_X)).getCurrentState();
		float targetY = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_Y)).getCurrentState();
		float targetZ = ((AIConfiguationElements<Float>) getEntity().getAiConfiguration().get(Types.TARGET_Z)).getCurrentState();
		String targetAffinity = ((AIConfiguationElements<String>) getEntity().getAiConfiguration().get(Types.TARGET_AFFINITY)).getCurrentState();

		System.err.println("[AINPC][INPUT] behavior goto " + targetX + ", " + targetY + ", " + targetZ);
		setGotoOrderPosition(targetAffinity, targetX, targetY, targetZ);
	}

	public boolean isMoveUpToTargetWhenEngaging() {
		return getEntity().isMeleeAttacker();
	}

	public boolean isMoveRandomlyWhenEngaging() {
		return !getEntity().isMeleeAttacker();
	}

}
