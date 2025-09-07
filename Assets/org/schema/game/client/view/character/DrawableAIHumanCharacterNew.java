package org.schema.game.client.view.character;

import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.creature.AICharacter;
import org.schema.game.common.data.creature.AICharacterPlayer;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.network.StateInterface;

import javax.vecmath.Vector3f;
import java.util.Random;

public class DrawableAIHumanCharacterNew extends AbstractDrawableHumanCharacter<AICharacterPlayer, AICharacter> {

	private static Random r = new Random();
	float lastMovingDist;
	private AICharacter playerCharacter;
	private PlayerSkin skin;
	private Vector3f lastPos = new Vector3f();

	public DrawableAIHumanCharacterNew(AICharacter playerCharacter, Timer timer, GameClientState state) {
		super(playerCharacter, timer, state);

		this.skin = playerCharacter.getFactionId() == -2 ? GameResourceLoader.traidingSkin[r.nextInt(GameResourceLoader.traidingSkin.length)] : null;

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.character.AbstractDrawableHumanCharacter#draw()
	 */
	@Override
	public void draw() {
		//		System.err.println("DRAWING AT "+getPlayerCharacter().getWorldTransformClient().origin+"; ");
		super.draw();
	}

	@Override
	protected void handleTextureUpdate(Mesh mesh, AICharacter oState) {
		mesh.getSkin().setDiffuseTexId(Controller.getResLoader().getSprite("playertex").getMaterial().getTexture().getTextureId());
	}

	@Override
	protected void handleState(Timer timer, AICharacter oState) {
		this.timer = timer;
		if(handleSitting()) {
			return;
		}
		boolean climbing = handleClimbing();
		boolean gravity = playerCharacter.getGravity().isGravityOn();
		boolean gravityAir = !playerCharacter.getCharacterController().onGround();
		Vector3f dir = new Vector3f(playerCharacter.getOwnerState().getMovingDir());
		Vector3f traveled = new Vector3f();
		traveled.sub(getWorldTransform().origin, lastPos);
		if(traveled.length() > 0) {
			lastMovingDist = traveled.length();
		}

		//		System.err.println(getPlayerCharacter()+" ANIMA: "+getAnimationString()+"; traveled: "+traveled);
		if(dir.lengthSquared() > 0) {
			dir.normalize();
			if(climbing) moveClimbingAnimation(dir, lastMovingDist);
			else moveDirectionAnimation(dir, gravity, gravityAir, lastMovingDist);
		} else {
			if(getEntity().forcedAnimation != null) {
				getEntity().forcedAnimation.apply(getEntity());
			} else {
				if(getEntity().getOwnerState().getConversationPartner() != null) {
					if(!isAnim(AnimationIndex.TALK_SALUTE)) {
						setAnim(AnimationIndex.TALK_SALUTE, 0.2f, LoopMode.LOOP);
					}
				} else {
					standDirectionAnimation(gravity, gravityAir);
				}
			}

		}
		lastPos = new Vector3f(getWorldTransform().origin);
	}

	@Override
	public AICharacter getPlayerCharacter() {
		return playerCharacter;
	}

	@Override
	public void setPlayerCharacter(AICharacter playerCharacter) {
		this.playerCharacter = playerCharacter;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.character.AbstractDrawableHumanCharacter#initBoneAttachable()
	 */
	@Override
	public BoneAttachable initBoneAttachable(AICharacter creature) {
		return new CreaturePart(state, (Mesh) Controller.getResLoader().getMesh("PlayerMdl").getChilds().get(0), this);
	}

	@Override
	public Vector3f getForward() {
		return playerCharacter.getOwnerState().getForward(new Vector3f());
	}

	@Override
	public Vector3f getUp() {
		return playerCharacter.getOwnerState().getUp(new Vector3f());
	}

	@Override
	public Vector3f getRight() {
		return playerCharacter.getOwnerState().getRight(new Vector3f());
	}

	@Override
	public Transform getWorldTransform() {

		return playerCharacter.getWorldTransformOnClient();
	}

	@Override
	protected float getScale() {
		return 1;
	}

	@Override
	protected AnimationIndexElement getAnimationState() {
		return playerCharacter.getAnimationState();
	}

	@Override
	public SimpleTransformableSendableObject getGravitySource() {
		return null;
	}

	@Override
	protected PlayerSkin getPlayerSkin() {

		return this.skin;
	}

	@Override
	public void loadClientBones(StateInterface state) {

	}

}
