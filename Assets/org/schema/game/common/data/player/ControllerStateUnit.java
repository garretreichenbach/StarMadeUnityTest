package org.schema.game.common.data.player;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.KeyboardMappings;

import javax.vecmath.Vector3f;

public class ControllerStateUnit implements ControllerStateInterface {
	public PlayerControllable playerControllable;
	public Object parameter;
	public PlayerState playerState;
	private Vector3i tmp = new Vector3i();
	private Vector3i lastEnter = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	private boolean okRotate;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return playerControllable.hashCode() + (parameter != null ? parameter.hashCode() : 100200100) + playerState.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ControllerStateUnit)) {
			return false;
		}
		ControllerStateUnit c = (ControllerStateUnit) o;
		return
				c.playerControllable.getId() == this.playerControllable.getId() &&
						c.parameter.equals(this.parameter) &&
						c.playerState.getId() == this.playerState.getId();
	}

	@Override
	public boolean isSelected(PlayerUsableInterface usable, ManagerContainer<?> con){
		PlayerUsableInterface playerUsable = con.getPlayerUsable(usable.getUsableId());
		if(playerUsable != null && parameter != null){
			long enterIndex = ElementCollection.getIndex((Vector3i)parameter);
			long selectedSlotIndex = con.getSelectedSlot(this, enterIndex);
			return selectedSlotIndex == usable.getUsableId();
		}
		return false;
	}
	@Override
	public String toString() {
		return "(" + playerState.getName() + ", " + playerControllable + ", " + parameter + ")";
	}

	@Override
	public Vector3i getParameter(Vector3i out) {
		out.set((Vector3i) parameter);
		return out;
	}

	@Override
	public PlayerState getPlayerState() {
		return playerState;
	}

	@Override
	public Vector3f getForward(Vector3f out) {
		return playerState.getForward(out);
	}

	@Override
	public Vector3f getUp(Vector3f out) {
		return playerState.getUp(out);
	}

	@Override
	public Vector3f getRight(Vector3f out) {
		return playerState.getRight(out);
	}


	@Override
	public boolean isUnitInPlayerSector() {
		if (!playerState.isOnServer()) {
			if (playerControllable instanceof SimpleTransformableSendableObject) {
				return ((GameClientState) playerState.getState()).getCurrentSectorId() == ((SimpleTransformableSendableObject) playerControllable).getSectorId();
			}
		}
		return true;
	}
	
	@Override
	public Vector3i getControlledFrom(Vector3i out) {
		Vector3i con = playerState.getCockpit().getBlock(out);
		return con;
	}

	@Override
	public boolean isFlightControllerActive() {
		return playerState.getNetworkObject().activeControllerMask.get(AbstractControlManager.CONTROLLER_SHIP_EXTERN).get();
	}

	@Override
	public int getCurrentShipControllerSlot() {
		return playerState.getCurrentShipControllerSlot();
	}

	@Override
	public boolean isDown(KeyboardMappings m) {
		return playerState.getControllerState().isDown(m);
	}
	@Override
	public void handleJoystickDir(Vector3f dir, Vector3f forward,
	                              Vector3f right, Vector3f up) {
		playerState.handleJoystickDir(dir, forward, right, up);
	}

	@Override
	public SimpleTransformableSendableObject getAquiredTarget() {
		return playerState.getAquiredTarget();
	}
	private Vector3f hitPoint = null;
	private short lastUpdate;
	private Vector3f lastPlayerForward = new Vector3f(); // New field to store the last player forward vector
	
	
	private Vector3f forwTmp = new Vector3f();
	private Vector3f upTmp = new Vector3f();
	private Vector3f rightTmp = new Vector3f();
	private SegmentPiece tmpPiece = new SegmentPiece();
	@Override
	public boolean getShootingDir(
			SegmentController c, 
			ShootContainer shootContainer, 
			float distance, 
			float speed, 
			Vector3i collectionControllerPoint, 
			boolean focused, 
			boolean lead) {
//		System.err.println("CONTROLFROM ORIGINAL: "+controlledFromOrig+"; Cockpit: "+playerState.getNetworkObject().cockpit.getVector(tmp)+"; "+controlledFromOrig.equals(playerState.getNetworkObject().cockpit.getVector(tmp)));
		boolean useOrientation = shootContainer.controlledFromOrig.equals(Ship.core) || shootContainer.controlledFromOrig.equals(playerState.getCockpit().getBlock(tmp));
		Vector3f currentForward = getForward(forwTmp); // Get current player forward
		Vector3f forward = UsableControllableElementManager.getShootingDir(
			c,
			currentForward, // Use currentForward
			getUp(upTmp),
			getRight(rightTmp),
			shootContainer.shootingDirTemp,
			shootContainer.shootingUpTemp,
			shootContainer.shootingRightTemp,
			useOrientation,
			collectionControllerPoint,
			tmpPiece);

		tmpPiece.reset();
		
		PhysicsExt physics = c.getPhysics();
		forward.scale(distance);
		forward.add(shootContainer.camPos);

		ClosestRayResultCallback testRayCollisionPoint = null;

		if (focused) {
//			System.err.println("FOCUS "+playerState.getState());
//			DebugPoint p0 = new DebugPoint(new Vector3f(camPos), new Vector4f(1,1,1,1));
//			DebugPoint p1 = new DebugPoint(new Vector3f(forward), new Vector4f(1,0,1,1));
//			DebugDrawer.points.add(p0);
//			DebugDrawer.points.add(p1);
			// Re-evaluate raycast if game update changed OR player's view changed
			if(lastUpdate != c.getState().getNumberOfUpdate() || !currentForward.equals(lastPlayerForward)){
				testRayCollisionPoint =
						physics.testRayCollisionPoint(shootContainer.camPos, forward, false, c, null, false, true, true);
				
				if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit()){
					hitPoint = testRayCollisionPoint.hitPointWorld;
				}else{
					hitPoint = null;
				}
				lastUpdate = c.getState().getNumberOfUpdate();
				lastPlayerForward.set(currentForward); // Update lastPlayerForward
			}
		}else{
			hitPoint = null;
		}
		if (hitPoint != null) {
//			System.err.println("TRYING FOCUS "+hitPoint);
//			System.err.println("RAY FROM "+controllerAbsolutePos);
			shootContainer.shootingDirTemp.sub(hitPoint, shootContainer.weapontOutputWorldPos);
		} else {
			shootContainer.shootingDirTemp.set(UsableControllableElementManager.getShootingDir(
					c,
					currentForward, // Use currentForward
					getUp(upTmp),
					getRight(rightTmp),
					shootContainer.shootingForwardTemp,
					shootContainer.shootingUpTemp, 
					shootContainer.shootingRightTemp, 
					useOrientation, 
					collectionControllerPoint,
					tmpPiece));
			tmpPiece.reset();
		}
		
		
		shootContainer.shootingDirStraightTemp.set(UsableControllableElementManager.getShootingDir(
			c,
			GlUtil.getForwardVector(forwTmp, c.getWorldTransform()),
			GlUtil.getUpVector(upTmp, c.getWorldTransform()),
			GlUtil.getRightVector(rightTmp, c.getWorldTransform()),
			shootContainer.shootingStraightForwardTemp,
			shootContainer.shootingStraightUpTemp, 
			shootContainer.shootingStraightRightTemp, 
			useOrientation, 
			collectionControllerPoint,
			tmpPiece));
		tmpPiece.reset();
		return true;
	}

	@Override
	public boolean isSelected(SegmentPiece controllerElement,
	                          Vector3i controlledFrom) {
		return controllerElement.equalsPos(controlledFrom);
	}
	private final Vector3i paramTmp = new Vector3i();
	@Override
	public boolean canFlyShip() {
		if (!(Ship.core.equals(getParameter(paramTmp)))) {
			//can only steer the ship at core
			return false;
		}

		return true;
	}

	@Override
	public boolean canRotateShip() {
		if (System.currentTimeMillis() - playerState.inControlTransition < ControllerState.DELAY_ORIENTATION_MS || !playerState.canRotate()) {
			return false;
		}
		if (playerState.isClientOwnPlayer()) {
			//			System.err.println("############# "+((GameClientState)playerState.getState()).currentEnterTry+", "+ playerControllable+"; TIME: "+(System.currentTimeMillis() - ((GameClientState)playerState.getState()).currentEnterTryTime)+" / "+ControllerState.DELAY_ORIENTATION_MS);
			if (((GameClientState) playerState.getState()).currentEnterTry == playerControllable && ((System.currentTimeMillis() - ((GameClientState) playerState.getState()).currentEnterTryTime) < ControllerState.DELAY_ORIENTATION_MS)) {
				return false;
			}
		}

		if(playerState.isDown(KeyboardMappings.FREE_CAM) || playerState.getNetworkObject().adjustMode.getBoolean()){
			
			return false;
		}
		
		Vector3i c = playerState.getCockpit().getBlock(new Vector3i());

		//cache the value
		if (!lastEnter.equals(c)) {
			okRotate = false;
			if (c.equals(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF)) {
				okRotate = true;
			}
			if (playerControllable != null && playerControllable instanceof SegmentController) {
				SegmentController con = (SegmentController) playerControllable;
				SegmentPiece pointUnsave = con.getSegmentBuffer().getPointUnsave(c);
				if (pointUnsave != null && pointUnsave.getOrientation() == Element.FRONT) {
					okRotate = true;
				}
			}
			lastEnter.set(c);
		}
		return okRotate;
	}

	@Override
	public float getBeamTimeout() {
		return -1;//use default from beam handler
	}

	@Override
	public boolean canFocusWeapon() {
		return true;
	}

	@Override
	public void addCockpitOffset(Vector3f camPos) {
		playerState.getCockpit().addCockpoitOffset(camPos);
	}

	@Override
	public boolean isAISelected(SegmentPiece controllerElement, Vector3i controlledFrom, int controllerIndexForAI, int max,
			ElementCollectionManager<?, ?, ?> colMan) {
		return true;
	}

	@Override
	public boolean isTriggered(KeyboardMappings mapping) {
		return playerState.getControllerState().isTriggered(mapping);
	}

	


	

	

}