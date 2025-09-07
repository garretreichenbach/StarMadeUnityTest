package org.schema.game.common.data.player;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.input.KeyboardMappings;

public interface ControllerStateInterface {

	public static final int ALWAYS_SELECTED_FOR_AI = Integer.MIN_VALUE;

	public Vector3i getParameter(Vector3i out);

	public PlayerState getPlayerState();

	public Vector3f getForward(Vector3f out);

	public Vector3f getUp(Vector3f out);

	public Vector3f getRight(Vector3f out);


	public boolean isUnitInPlayerSector();

	public Vector3i getControlledFrom(Vector3i out);

	public boolean isFlightControllerActive();

	public int getCurrentShipControllerSlot();

	public boolean isSelected(PlayerUsableInterface usable, ManagerContainer<?> con);
	
	public void handleJoystickDir(Vector3f dir, Vector3f vforwardector3f,
	                              Vector3f left, Vector3f up);

	public SimpleTransformableSendableObject getAquiredTarget();

	public boolean getShootingDir(SegmentController c, ShootContainer shootContainer, float distance, float speed, Vector3i collectionControllerPoint, boolean focused, boolean lead);

	public boolean isSelected(SegmentPiece controllerElement,
	                          Vector3i controlledFrom);
	public boolean isAISelected(SegmentPiece controllerElement,
            Vector3i controlledFrom, int controllerIndexForAI, int max, ElementCollectionManager<?, ?, ?> colMan);
	public boolean canFlyShip();

	public boolean canRotateShip();

	public float getBeamTimeout();


	public boolean canFocusWeapon();

	public void addCockpitOffset(Vector3f camPos);
	
	
	public boolean isDown(KeyboardMappings m);

	public boolean isTriggered(KeyboardMappings m);

}
