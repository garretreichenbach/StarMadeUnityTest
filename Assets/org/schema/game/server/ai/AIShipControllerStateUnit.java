package org.schema.game.server.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.TransformaleObjectTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AIGameEntityState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.input.Mouse;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;

public class AIShipControllerStateUnit extends AIControllerStateUnit<Ship> {

	private static final TransformaleObjectTmpVars v = new TransformaleObjectTmpVars();

	public int timesBothMouseButtonsDown = 0;
	public boolean useBothMouseButtonsDown = false;

	public AIShipControllerStateUnit(AIGameEntityState<Ship> entState) {
		super(entState);
	}

	@Override
	public Vector3i getParameter(Vector3i out) {
		out.set(Ship.core);
		return out;
	}

	@Override
	public PlayerState getPlayerState() {
		return null;
	}



	@Override
	public boolean isUnitInPlayerSector() {
		return true;
	}

	@Override
	public Vector3i getControlledFrom(Vector3i out) {
		return Ship.core;
	}

	@Override
	public boolean isFlightControllerActive() {
		return true;
	}

	@Override
	public int getCurrentShipControllerSlot() {
		return 0;
	}



	@Override
	public void handleJoystickDir(Vector3f dir, Vector3f vforwardector3f, Vector3f left, Vector3f up) {
	}

	@Override
	public SimpleTransformableSendableObject getAquiredTarget() {
		Sendable s = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(getEntity().getNetworkObject().targetId.getInt());
		if(s instanceof SimpleTransformableSendableObject) {
			return (SimpleTransformableSendableObject) s;
		} else {
			return null;
		}

	}

	Vector3f forwTmp = new Vector3f();
	Vector3f upTmp = new Vector3f();
	Vector3f rightTmp = new Vector3f();

	@Override
	public boolean getShootingDir(SegmentController c, ShootContainer shootContainer, float distance, float speed, Vector3i collectionControllerPoint, boolean focused, boolean lead) {
		Object sTarget = null;
		if(getEntity().getNetworkObject().targetType.getByte() == SimpleGameObject.SIMPLE_TRANSFORMABLE_SENSABLE_OBJECT) {
			sTarget = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(getEntity().getNetworkObject().targetId.getInt());
		} else if(getEntity().getNetworkObject().targetType.getByte() == SimpleGameObject.MISSILE) {
			if(isOnServer()) {
				sTarget = ((GameServerState) getState()).getController().getMissileController().getMissileManager().getMissiles().get((short) getEntity().getNetworkObject().targetId.getInt());
			} else {
				sTarget = ((GameClientState) getState()).getController().getClientMissileManager().getMissile((short) getEntity().getNetworkObject().targetId.getInt());
			}

		} else if(getEntity().getNetworkObject().targetType.getByte() == SimpleGameObject.MINABLE) {
			sTarget = getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(getEntity().getNetworkObject().targetId.getInt());

		}
		if(!(sTarget instanceof SimpleGameObject)) {
			shootContainer.shootingDirTemp.set(getForward(forwTmp));
			shootContainer.shootingForwardTemp.set(getForward(forwTmp));
			shootContainer.shootingUpTemp.set(getUp(upTmp));
			shootContainer.shootingRightTemp.set(getRight(rightTmp));

			return false;
		}
		//		SimpleGameObject target = (SimpleGameObject)sTarget;
		Vector3f targetPosition = new Vector3f();
		Vector3f targetVelocity = new Vector3f();

		getEntity().getNetworkObject().targetPosition.getVector(targetPosition);
		getEntity().getNetworkObject().targetVelocity.getVector(targetVelocity);

		if(!getEntity().isOnServer() && getEntity().getSectorId() != ((GameClientState) getEntity().getState()).getCurrentSectorId()) {

			//the shooter(!) is in another sector than ourselves (we are looking at the shooting from another sector)
			//and shoots normalized to his position, which is then off for the client, so
			//we need to adapt the position from the actual entity sector

			localTransform.setIdentity();
			localTransform.origin.set(targetPosition);

			SimpleTransformableSendableObject.calcWorldTransformRelative(((GameClientState) getEntity().getState()).getCurrentSectorId(), ((GameClientState) getEntity().getState()).getPlayer().getCurrentSector(), getEntity().getSectorId(), localTransform, getState(), false, outputTransform, v);
			targetPosition.set(outputTransform.origin);

		}
		shootContainer.shootingDirTemp.set(UsableControllableElementManager.getShootingDir(c, getForward(forwTmp), getUp(upTmp), getRight(rightTmp), shootContainer.shootingForwardTemp, shootContainer.shootingUpTemp, shootContainer.shootingRightTemp, true, collectionControllerPoint, new SegmentPiece()));

		shootContainer.shootingDirStraightTemp.set(UsableControllableElementManager.getShootingDir(c, GlUtil.getForwardVector(forwTmp, c.getWorldTransform()), GlUtil.getUpVector(upTmp, c.getWorldTransform()), GlUtil.getRightVector(rightTmp, c.getWorldTransform()), shootContainer.shootingStraightForwardTemp, shootContainer.shootingStraightUpTemp, shootContainer.shootingStraightRightTemp, true, collectionControllerPoint, new SegmentPiece()));

		if(!Mouse.isDown(1)) {
			if(shootContainer.shootingDirTemp.equals(getForward(forwTmp))) {
				if(lead) {
					Vector3f targetAngularVelocity = new Vector3f();
					try {
						if(getAquiredTarget() instanceof SegmentController) ((SegmentController) getAquiredTarget()).getPhysicsObject().getAngularVelocity(targetAngularVelocity);
					} catch(NullPointerException ignored) {}
					//if the shot goes straight. predict target's position
					Vector3f bulletPath = Vector3fTools.predictPoint(targetPosition, targetVelocity, targetAngularVelocity, speed, shootContainer.weapontOutputWorldPos);

					//					System.err.println("PREDICTING::: "+bulletPath);
					shootContainer.shootingDirTemp.set(bulletPath);
				} else {
					shootContainer.shootingDirTemp.sub(targetPosition, shootContainer.weapontOutputWorldPos);
				}
			}
		}
		return true;
	}

	@Override
	public boolean isSelected(SegmentPiece controllerElement, Vector3i controlledFrom) {

		//is controleld by core
		return getEntity().getControlElementMap().isControlling(ElementCollection.getIndex(controlledFrom), controllerElement.getAbsoluteIndex(), controllerElement.getType());
	}

	public Long2ObjectOpenHashMap<AIFireState> fireStates = new Long2ObjectOpenHashMap<AIFireState>();

	@Override
	public boolean isAISelected(SegmentPiece controllerElement, Vector3i controlledFrom, int controllerIndexForAI, int max, ElementCollectionManager<?, ?, ?> colMan) {
		int index = (getEntity().getSelectedAIControllerIndex() % max);
		//		System.err.println(getState()+" "+colMan.getSegmentController()+" CURRET INDEX: "+index);
		boolean selected = controllerIndexForAI == ControllerStateInterface.ALWAYS_SELECTED_FOR_AI || index == controllerIndexForAI;

		AIFireState aiFireState = fireStates.get(controllerElement.getAbsoluteIndex());

		boolean currentlyFiring = false;
		if(aiFireState == null) {
			if(selected) {
				//only start a new fire process when it's the weapon's turn
				AIFireState fs = colMan.getAiFireState(this);
				if(fs != null) {
					//this weapon uses a firestate (takes more than one update to fire (e.g. charge or beam))
					fireStates.put(controllerElement.getAbsoluteIndex(), fs);
				}

				//fire state null means that the weapon can be executed in one update (like a cannon)
			}
		} else {
			if(aiFireState.isExecuted(getState().getUpdateTime())) {
				//weapon is done firing
				fireStates.remove(controllerElement.getAbsoluteIndex());
			} else {
				currentlyFiring = true;
			}
		}

		//returns true when its the weapon's turn to fire or when the weapon is already firing
		return selected || currentlyFiring;
	}

	@Override
	public boolean canFlyShip() {
		return true;
	}

	@Override
	public boolean canRotateShip() {
		return true;
	}

	@Override
	public float getBeamTimeout() {
		return 2;
	}

	@Override
	public boolean isSelected(PlayerUsableInterface usable, ManagerContainer<?> con) {
		return false;
	}


	@Override
	public boolean canFocusWeapon() {
		return false;
	}


	@Override
	public void addCockpitOffset(Vector3f camPos) {

	}
	@Override
	public boolean isDown(KeyboardMappings m) {
		if(m == KeyboardMappings.SHIP_ZOOM && useBothMouseButtonsDown){
			if(timesBothMouseButtonsDown > 0){
				//focus fire as many times as timesBothMouseButtonsDown
				timesBothMouseButtonsDown--;
				return false;
			}else{
				//unfocus rest
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isTriggered(KeyboardMappings m) {
		return false;
	}

}
