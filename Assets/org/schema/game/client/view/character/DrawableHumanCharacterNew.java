package org.schema.game.client.view.character;

import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.input.JoystickMappingFile;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;

import javax.vecmath.Vector3f;

public class DrawableHumanCharacterNew extends AbstractDrawableHumanCharacter<PlayerState, PlayerCharacter> {

	float lastMovingDist;
	private Vector3f lastPos = new Vector3f();

	public DrawableHumanCharacterNew(PlayerCharacter playerCharacter, Timer timer, GameClientState state) {
		super(playerCharacter, timer, state);
	}

	private void checkHelmet(PlayerState playerState) {
		boolean helmetWasOn = isAttached(getHeadBone(), "Helmet");

		boolean helmentInSlot = playerState.getHelmetSlot() >= 0 && playerState.getInventory(null).getType(playerState.getHelmetSlot()) == MetaObjectType.HELMET.type;
		if(helmentInSlot && !isAnimTorso(AnimationIndex.HELMET_ON) && !helmetWasOn) {
			if(!isAnimTorsoActive()) {
				setAnimTorso(AnimationIndex.HELMET_ON, LoopMode.DONT_LOOP_DEACTIVATE);
				attach(getHeldBone(), "Helmet");
			}
			if(isAnimTorsoActive() && isAnimTorso(AnimationIndex.HELMET_OFF)) {
				float t = getAnimTorsoTime();
				setAnimTorso(AnimationIndex.HELMET_ON, LoopMode.DONT_LOOP_DEACTIVATE);
				setAnimTorsoTime(getAnimTorsoMaxTime() - t);
				attach(getHeldBone(), "Helmet");
			}
		} else {
			if(!helmentInSlot && !isAnimTorso(AnimationIndex.HELMET_OFF) && (isAnimTorso(AnimationIndex.HELMET_ON) || helmetWasOn)) {
				if(isAnimTorso(AnimationIndex.HELMET_ON)) {
					float t = getAnimTorsoTime();
					setAnimTorso(AnimationIndex.HELMET_OFF, LoopMode.DONT_LOOP_DEACTIVATE);
					setAnimTorsoTime(getAnimTorsoMaxTime() - t);
					detach(getHeadBone(), "Helmet");
					attach(getHeldBone(), "Helmet");
				} else {
					setAnimTorso(AnimationIndex.HELMET_OFF, LoopMode.DONT_LOOP_DEACTIVATE);
					detach(getHeadBone(), "Helmet");
					attach(getHeldBone(), "Helmet");
				}
			}
		}
	}

	@Override
	protected void handleTextureUpdate(Mesh mesh, PlayerCharacter oState) {
		PlayerState playerState = oState.getOwnerState();
		mesh.currentTexCoordSet = playerState.getNetworkObject().playerFaceId.get();
		//		mesh.getSkin().setDiffuseTexId(playerState.getSkinManager().getTextureId());
		mesh.getSkin().setDiffuseTexId(Controller.getResLoader().getSprite("playertex").getMaterial().getTexture().getTextureId());

	}

	@Override
	protected void handleState(Timer timer, PlayerCharacter oState) {
		PlayerState playerState = oState.getOwnerState();

		if(handleSitting()) {
			return;
		}

		boolean isClimbing = handleClimbing();

		try {
			boolean a = playerState.isDown(KeyboardMappings.FORWARD);
			a = a | playerState.isDown(KeyboardMappings.BACKWARDS);
			a = a | playerState.isDown(KeyboardMappings.STRAFE_LEFT);
			a = a | playerState.isDown(KeyboardMappings.STRAFE_RIGHT);
			a = a | playerState.isDown(KeyboardMappings.UP);
			a = a | playerState.isDown(KeyboardMappings.DOWN);

			state.getController().getJoystick();
			if(JoystickMappingFile.ok()) {
				a = a | state.getController().getJoystickAxis(JoystickAxisMapping.FORWARD_BACK) != 0;
				a = a | state.getController().getJoystickAxis(JoystickAxisMapping.RIGHT_LEFT) != 0;
			}

			checkHelmet(playerState);

			if(getEntity().getProhibitingBuildingAroundOrigin() > 0) {
				if(!isAttachedAnything(getLeftHolster())) {
					attach(getLeftHolster(), "BuildInhibitor");
				}
			} else {
				if(isAttached(getLeftHolster(), "BuildInhibitor")) {
					detach(getLeftHolster(), "BuildInhibitor");
				}
			}

			boolean back = playerState.isDown(KeyboardMappings.BACKWARDS);

			Vector3f up = playerState.getUp(new Vector3f());
			Vector3f down = new Vector3f(up);
			down.scale(-1);
			Vector3f right = playerState.getRight(new Vector3f());
			Vector3f left = new Vector3f(right);
			right.scale(-1);
			Vector3f forward = playerState.getForward(new Vector3f());
			Vector3f backward = new Vector3f(forward);
			backward.scale(-1);

			Vector3f dir = new Vector3f();
			if(playerState.isDown(KeyboardMappings.FORWARD)) {
				if(isClimbing) dir.add(up);
				else dir.add(forward);
			}

			if(playerState.isDown(KeyboardMappings.BACKWARDS)) {
				if(isClimbing) dir.add(down);
				else dir.add(backward);
			}

			if(playerState.isDown(KeyboardMappings.STRAFE_LEFT) && !isClimbing) {
				dir.add(left);
			}
			if(playerState.isDown(KeyboardMappings.STRAFE_RIGHT) && !isClimbing) {
				dir.add(right);
			}
			if(playerState.isDown(KeyboardMappings.UP)) {
				dir.add(up);
			}
			if(playerState.isDown(KeyboardMappings.DOWN)) {
				dir.add(down);
			}

			playerState.handleJoystickDir(dir, forward, right, up);

			boolean gravity = creature.getGravity().isGravityOn() && !isClimbing;
			boolean gravityAir = !creature.getCharacterController().onGround() && !isClimbing;
			Vector3f traveled = new Vector3f();
			traveled.sub(getWorldTransform().origin, lastPos);
			if(traveled.length() > 0) {
				lastMovingDist = traveled.length();
			}
			if(dir.lengthSquared() > 0) {
				if(isClimbing) moveClimbingAnimation(dir, lastMovingDist);
				else moveDirectionAnimation(dir, gravity, gravityAir, lastMovingDist);
			} else {
				standDirectionAnimation(gravity, gravityAir);
			}
			lastPos = new Vector3f(getWorldTransform().origin);
			this.timer = timer;
			indication.setText(getPlayerName() + playerState.getFactionController().getFactionString());
		} catch(Exception e) {
			System.err.println("DRAWABLE CHARACTER UPDATE FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "; PlayerState: " + playerState);
			e.printStackTrace();
		}
	}

	@Override
	public PlayerCharacter getPlayerCharacter() {
		return creature;
	}

	@Override
	public void setPlayerCharacter(PlayerCharacter playerCharacter) {
		this.creature = playerCharacter;
	}

	@Override
	public Vector3f getForward() {
		return getPlayerCharacter().getOwnerState().getForward(new Vector3f());
	}

	@Override
	public Vector3f getUp() {
		return getPlayerCharacter().getOwnerState().getUp(new Vector3f());
	}

	@Override
	public Vector3f getRight() {
		return getPlayerCharacter().getOwnerState().getRight(new Vector3f());
	}

	@Override
	public Transform getWorldTransform() {
		return getPlayerCharacter().getWorldTransformOnClient();
	}

	@Override
	protected float getScale() {
		return 1;
	}

	@Override
	protected AnimationIndexElement getAnimationState() {
		//a controllable player character has no AI input for animation
		//this is ok, since its updated with bone attachable
		return getPlayerCharacter().getAnimationState();
	}

	@Override
	public SimpleTransformableSendableObject getGravitySource() {
		return getPlayerCharacter().getGravity().source;
	}

	@Override
	public boolean isOwnPlayerCharacter() {
		return getPlayerCharacter().isClientOwnObject();
	}

	@Override
	protected boolean tiltHead() {
		//		System.err.println(!isAttached(getHeadBone(), "Helmet") +" "+ !isAnimTorso(AnimationIndex.HELMET_ON) +" "+ !isAnimTorso(AnimationIndex.HELMET_OFF));
		return !isAnimTorso(AnimationIndex.HELMET_ON) && !isAnimTorso(AnimationIndex.HELMET_OFF);
	}

	@Override
	protected PlayerSkin getPlayerSkin() {
		return creature.getOwnerState().getSkinManager().getTextureId();
	}

	@Override
	public void loadClientBones(StateInterface state) {

	}

}
