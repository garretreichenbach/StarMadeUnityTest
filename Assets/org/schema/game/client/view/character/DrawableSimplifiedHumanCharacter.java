package org.schema.game.client.view.character;

import javax.vecmath.Vector3f;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.player.simplified.SimplifiedCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.linearmath.Transform;

public class DrawableSimplifiedHumanCharacter extends AbstractDrawableSimplifiedHumanCharacter<SimplifiedCharacter>{

	private Vector3f lastPos;
	private float lastMovingDist;

	public DrawableSimplifiedHumanCharacter(
			SimplifiedCharacter playerCharacter, Timer timer,
			GameClientState state) {
		super(playerCharacter, timer, state);
	}

	@Override
	public void loadClientBones(StateInterface state) {
	}

	@Override
	protected void handleTextureUpdate(Mesh mesh, SimplifiedCharacter oState) {
		mesh.getSkin().setDiffuseTexId(Controller.getResLoader().getSprite("playertex").getMaterial().getTexture().getTextureId());		
	}

	@Override
	protected void handleState(Timer timer, AbstractOwnerState state) {
		this.timer = timer;
		if (handleSitting()) {
			return;
		}
		boolean gravity = getEntity().getGravity().isGravityOn();
		boolean gravityAir = !getEntity().onGround();
		Vector3f dir = new Vector3f(getEntity().getMovingDir());
		Vector3f traveled = new Vector3f();
		traveled.sub(getWorldTransform().origin, lastPos);
		if (traveled.length() > 0) {
			lastMovingDist = traveled.length();
		}

		//		System.err.println(getPlayerCharacter()+" ANIMA: "+getAnimationString()+"; traveled: "+traveled);
		if (dir.lengthSquared() > 0) {
			dir.normalize();
			moveDirectionAnimation(dir, gravity, gravityAir, lastMovingDist);
		} else {

			if (getEntity().forcedAnimation != null) {
				getEntity().forcedAnimation.apply(getEntity());
			} else {
				if (getEntity().getConversationPartner() != null) {
					if (!isAnim(AnimationIndex.TALK_SALUTE)) {
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
	public Vector3f getForward() {
		return getEntity().getForward();
	}

	@Override
	public Vector3f getUp() {
		return getEntity().getUp();
	}

	@Override
	public Vector3f getRight() {
		return getEntity().getRight();
	}

	@Override
	public Transform getWorldTransform() {
		return getEntity().getWorldTransform();
	}

	@Override
	protected float getScale() {
		return 1;
	}

	@Override
	protected AnimationIndexElement getAnimationState() {
		return getEntity().getAnimationState();
	}

	@Override
	public SimpleTransformableSendableObject getGravitySource() {
		return getEntity().getGravity() != null ? getEntity().getGravity().source : null;
	}

	@Override
	protected PlayerSkin getPlayerSkin() {
		return getEntity().getSkin();
	}
}
