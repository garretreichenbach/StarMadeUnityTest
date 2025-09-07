package org.schema.game.client.view.character;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.effects.segmentcontrollereffects.RunningEffect;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.game.common.data.player.PlayerSkin;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.graphicsengine.animation.AnimationEventListener;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructEndPoint;
import org.schema.schine.graphicsengine.animation.structure.classes.Moving;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Bone;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Skin;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.CreatureStructure;
import org.schema.schine.resource.CreatureStructure.PartType;

import javax.vecmath.*;
import java.util.Collection;

public abstract class AbstractAnimatedObject<E extends AbstractCharacterInterface> implements Drawable, BoneLocationInterface {
	protected final GameClientState state;
	protected final ObjectArrayList<AnimatedObjectHoldAnimation> holders = new ObjectArrayList<AnimatedObjectHoldAnimation>();
	private final Transform t = new Transform();
	protected ConstantIndication indication;
	protected float yawBefore;
	protected long lastYawChange;
	protected float yaw;
	protected Timer timer;
	protected E creature;
	boolean lastGravityAir = false;
	Vector4f tintTmp = new Vector4f();
	private Matrix3f mRot = new Matrix3f();
	private Matrix3f mRotPitch = new Matrix3f();
	private Matrix3f mRotPitchB = new Matrix3f();
	private Matrix3f mRotPitchC = new Matrix3f();
	private Vector3f up = new Vector3f(0, 1, 0);
	private Vector3f down = new Vector3f(0, -1, 0);
	private boolean shadowMode;
	private float pitch;
	private Transform gravityOrientation = new Transform();
	private float aimScale;
	private boolean firstDraw = true;
	private BoneAttachable boneAttachable;
	private Vector3f lastDir = new Vector3f();
	private boolean inJump;

	public AbstractAnimatedObject(Timer timer, E creature, GameClientState state) {
		super();
		this.creature = creature;
		this.state = state;
		this.timer = timer;
		gravityOrientation.setIdentity();
		boneAttachable = initBoneAttachable(creature);
		initManualAttachments();

		setAnim(AnimationIndex.IDLING_FLOATING);
	}

	public abstract Vector3f getForward();

	public abstract Vector3f getUp();

	public abstract Vector3f getRight();

	public abstract void setForcedAnimation(PartType type, AnimationIndexElement selectedItem, boolean fullBody) throws AnimationNotSetException;

	public abstract void setAnimSpeedForced(PartType type, float speed, boolean fullBody);

	public abstract void setForcedLoopMode(PartType type, LoopMode mode, boolean fullBody);

	public E getEntity() {
		return creature;
	}

	public void initManualAttachments() {
	}

	protected Matrix3f getYawRotation() {
		Vector3f f = new Vector3f(getForward());
		Vector3f u = new Vector3f(getUp());

		gravityOrientation.transform(f);
		gravityOrientation.transform(u);
		if(skipYawRotation()) {
			//			//not rotating
		} else {
			if(f.epsilonEquals(up, 0.01f)) {
				yaw = FastMath.atan2Fast(-u.x, -u.z);
			} else if(f.epsilonEquals(down, 0.01f)) {
				yaw = FastMath.atan2Fast(u.x, u.z);
			} else {
				yaw = FastMath.atan2Fast(f.x, f.z);
			}
		}
		Matrix3f aa = new Matrix3f();
		aa.setIdentity();
		aa.rotY(yaw);
		return aa;
	}

	protected boolean needsPitchRotation() {
		return true;
	}

	protected boolean skipYawRotation() {
		return false;
	}

	protected void attach(Bone bone, String meshResource) {
		BoneAttachable attachedOn = boneAttachable.getAttachments().get(bone);
		if(isAttached(bone, meshResource)) {
			return;
		}
		assert (Controller.getResLoader().getMesh(meshResource) != null) : meshResource;
		final Mesh mesh = (Mesh) Controller.getResLoader().getMesh(meshResource).getChilds().get(0);
		boneAttachable.getAttachments().put(bone, new BoneAttachable(mesh, state, new BoneLocationInterface() {

			@Override
			public String getRootBoneName() {
				return mesh.getSkin().getSkeleton().getRootBone().name;
			}

			@Override
			public void initializeListeners(AnimationController controller,
			                                AnimationChannel channel, AnimationChannel channelTorso) {
			}

			@Override
			public String getRootTorsoBoneName() {
				return mesh.getSkin().getSkeleton().getRootBone().name;
			}

			@Override
			public String getHeldBoneName() {
				return null;
			}

			@Override
			public void loadClientBones(StateInterface state) {

			}
		}));

	}

	protected void detach(Bone bone, String meshResource) {
		if(isAttached(bone, meshResource)) {
			boneAttachable.getAttachments().remove(bone);
		}

	}

	protected boolean isAttached(Bone bone, String meshResource) {
		BoneAttachable attachedOn = boneAttachable.getAttachments().get(bone);
		return attachedOn != null && attachedOn.isEqualMeshFromResourceName(meshResource);

	}

	public void clearAttached(Bone bone) {
		BoneAttachable attachedOn = boneAttachable.getAttachments().get(bone);
		if(attachedOn != null) {
			boneAttachable.getAttachments().remove(bone);
		}
	}

	protected boolean isAttachedAnything(Bone bone) {
		BoneAttachable attachedOn = boneAttachable.getAttachments().get(bone);
		return attachedOn != null;

	}

	protected Matrix3f getPitchRotation(int axis, boolean negDir, float scale) {
		Vector3f f = new Vector3f(getForward());

		if(needsPitchRotation()) {
			assert (gravityOrientation.getMatrix(new Matrix4f()).determinant() != 0f);
			gravityOrientation.transform(f);
			pitch = (float) FastMath.acosFast(f.y);
			float restrictionBot = 0.85f;
			float restrictionUp = 0.2f;
			pitch = Math.max(restrictionBot, Math.min(FastMath.PI - restrictionUp, pitch));
			pitch -= FastMath.HALF_PI;
		}

		Matrix3f aa = new Matrix3f();
		aa.setIdentity();
		if(axis == 0) {
			aa.rotX(negDir ? -pitch * scale : pitch * scale);
		} else if(axis == 1) {
			aa.rotY(negDir ? -pitch * scale : pitch * scale);
		} else {
			aa.rotZ(negDir ? -pitch * scale : pitch * scale);
		}

		return aa;
	}

	public abstract Vector3f getOrientationUp();

	public abstract Vector3f getOrientationRight();

	public abstract Vector3f getOrientationForward();

	public abstract Transform getWorldTransform();

	protected abstract float getScale();

	public void standDirectionAnimation(boolean gravity, boolean gravityAir) {
		if(inJump && gravityAir) {
			if(!isAnim(AnimationIndex.MOVING_JUMPING_JUMPUP)) {
				inJump = false;
			}
			return;
		} else {
			inJump = false;
		}
		if(gravity) {

			if(!isAnim(AnimationIndex.MOVING_JUMPING_JUMPUP) && gravityAir && !lastGravityAir) {
				setAnim(AnimationIndex.MOVING_JUMPING_JUMPUP, LoopMode.DONT_LOOP);
				this.inJump = true;
			} else {
				if(!isAnim(AnimationIndex.IDLING_GRAVITY)) {
					setAnim(AnimationIndex.IDLING_GRAVITY, 0.2f, LoopMode.LOOP);
				}
			}
		} else {
			if(!isAnim(AnimationIndex.IDLING_FLOATING)) {
				setAnim(AnimationIndex.IDLING_FLOATING, 0.2f, LoopMode.LOOP);
			}
		}
	}

	public void moveClimbingAnimation(Vector3f dir, float movingSpeed) {
		if(dir.y < 0) {
			if(!isAnim(AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEUP)) setAnim(AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEUP, 0.2f, LoopMode.LOOP);
		} else {
			if(!isAnim(AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEDOWN)) setAnim(AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEDOWN, 0.2f, LoopMode.LOOP);
		}
		setAnimSpeed(movingSpeed);
		lastDir.set(dir);
	}

	public void moveDirectionAnimation(Vector3f dir, boolean gravity, boolean gravityAir, float movingSpeed) {

		Vector3f a = Vector3fTools.projectOnPlane(dir, getOrientationUp());
		Vector3f b = Vector3fTools.projectOnPlane(getForward(), getOrientationUp());

		a.normalize();
		b.normalize();
		float angle = a.angle(b);
		if(Vector3fTools.cross(a, b) < 0) {
			angle = FastMath.PI + (FastMath.PI - angle);
		}
		angle /= FastMath.QUARTER_PI;
		int dirIndex = FastMath.round(angle) % 8;

		Dir d = Dir.values()[dirIndex];

		AnimationIndexElement anim;
		LoopMode mode = LoopMode.LOOP;
		if(inJump && gravityAir) {
			if(!isAnim(AnimationIndex.MOVING_JUMPING_JUMPUP)) {
				inJump = false;
			}
			return;
		} else {
			inJump = false;
		}
		if(gravity) {
			if(!isAnim(AnimationIndex.MOVING_JUMPING_JUMPUP) && gravityAir && !lastGravityAir) {
				anim = AnimationIndex.MOVING_JUMPING_JUMPUP;
				mode = LoopMode.DONT_LOOP;
				this.inJump = true;
			} else {
				if(gravityAir) {
					anim = getFloatingAnimationFromDir(d);
				} else {
					anim = getRunningAnimationFromDir(d);
				}
			}
		} else {
			anim = getFloatingAnimationFromDir(d);
			Vector3f u = new Vector3f(getOrientationUp());
			u.sub(dir);
			if(u.length() < 0.5) {
				anim = AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEUP;
			}
			u.set(getOrientationUp());
			u.negate();
			u.sub(dir);
			if(u.length() < 0.5) {
				anim = AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEDOWN;
			}

		}

		//		System.err.println("WALK: ANIMATION "+ getAnimation(anim).getClass().getSimpleName());
		if(!isAnim(anim)) {
			setAnim(anim, 0.2f);

		}
		if(getAnimation(anim).isType(Moving.class)) {
			movingSpeed *= 21f;
			setAnimSpeed(movingSpeed);
		}
		lastGravityAir = gravityAir;
		lastDir.set(dir);
	}

	private AnimationIndexElement getRunningAnimationFromDir(Dir d) {

		switch(d) {
			case EAST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_EAST;
			case NORTH:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_NORTH;
			case NORTHEAST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_NORTHEAST;
			case NORTHWEST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_NORTHWEST;
			case SOUTH:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_SOUTH;
			case SOUTHEAST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_SOUTHEAST;
			case SOUTHWEST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_SOUTHWEST;
			case WEST:
				return AnimationIndex.MOVING_BYFOOT_RUNNING_WEST;
			default:
				break;

		}

		throw new NullPointerException();
	}

	private AnimationIndexElement getFloatingAnimationFromDir(Dir d) {

		switch(d) {
			case EAST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEN;
			case NORTH:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEN;
			case NORTHEAST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEN;
			case NORTHWEST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEN;
			case SOUTH:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVES;
			case SOUTHEAST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVES;
			case SOUTHWEST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVES;
			case WEST:
				return AnimationIndex.MOVING_NOGRAVITY_FLOATMOVEN;
			default:
				break;

		}

		throw new NullPointerException();
	}

	protected abstract AnimationIndexElement getAnimationState();

	@Override
	public void draw() {
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		if(firstDraw) {
			onInit();
		}
		if(timer == null || isInvisible()) {
			return;
		}

		tintTmp.set(creature.getTint());
		if(creature.isHit()) {
			tintTmp.y *= 0.3;
			tintTmp.z *= 0.3;
		} else {
		}

		GlUtil.setRightVector(getOrientationRight(), gravityOrientation);
		GlUtil.setUpVector(getOrientationUp(), gravityOrientation);
		GlUtil.setForwardVector(getOrientationForward(), gravityOrientation);

		gravityOrientation.inverse();
		if(hasNeck() && tiltHead()) {
			mRotPitch.setIdentity();
			if(tiltHead()) {
				mRotPitch.set(getPitchRotation(1, false, 0.7f));
			}
			Quat4fTools.set(mRotPitch, getNeck().getAddLocalRot());

		}
		if(hasRightCollarBone() || hasLeftCollarBone()) {
			mRotPitchB.setIdentity();
			mRotPitchB.set(getPitchRotation(0, true, aimScale));

			mRotPitchC.setIdentity();
			mRotPitchC.set(getPitchRotation(0, false, aimScale));

			if(isAnimTorsoActive() && isAnimTorso(AnimationIndex.UPPERBODY_GUN_IDLE)) {
				if(aimScale < 1) {
					aimScale = Math.min(1, aimScale + timer.getDelta() * 3f);
				}
				if(hasRightCollarBone()) {
					Quat4fTools.set(mRotPitchB, getRightCollarBone().getAddLocalRot());
				}
				if(hasLeftCollarBone()) {
					Quat4fTools.set(mRotPitchC, getLeftCollarBone().getAddLocalRot());
				}

			} else {
				aimScale = 0;
			}
		}
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		Matrix3f yawRotation = getYawRotation();

		mRot.set(yawRotation);

		GlUtil.glPushMatrix();

		if(getGravitySource() != null && getGravitySource() instanceof SegmentController) {
			RunningEffect effect = state.getWorldDrawer().getSegmentControllerEffectDrawer().getEffect((SegmentController) getGravitySource());
		}

//		if(this instanceof AbstractDrawableCreature){
//			System.err.println("GB::\n"+getWorldTransform().basis+"\nYAW: \n"+yawRotation);
//		}
		t.set(getWorldTransform());
		t.basis.mul(mRot);
		GlUtil.glMultMatrix(t);

		boneAttachable.updateState(getAnimationState());
		boneAttachable.updateAnimation(timer);

//		System.err.println("DRAWING ::: "+boneAttachable.mesh.getName()+" : "+ boneAttachable.getAnimName());

		if(getScale() != 1) {
			GlUtil.scaleModelview(getScale(), getScale(), getScale());
		}
		GlUtil.translateModelview(getLocalTranslation().x, getLocalTranslation().y, getLocalTranslation().z);

		boneAttachable.draw(timer, getPlayerSkin(), tintTmp);
		GlUtil.glPopMatrix();

		if(hasNeck()) {
			mRotPitch.setIdentity();
			getNeck().getAddLocalRot().set(0, 0, 0, 1);
		}
		if(hasRightCollarBone()) {
			mRotPitchB.setIdentity();
			getRightCollarBone().getAddLocalRot().set(0, 0, 0, 1);
		}
		if(hasLeftCollarBone()) {
			mRotPitchC.setIdentity();
			getLeftCollarBone().getAddLocalRot().set(0, 0, 0, 1);
		}
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.core.Drawable#onInit()
	 */
	@Override
	public void onInit() {

		firstDraw = false;
	}

	public abstract SimpleTransformableSendableObject getGravitySource();

	public boolean isOwnPlayerCharacter() {
		return false;
	}

	protected boolean tiltHead() {
		return false;
	}

	protected abstract PlayerSkin getPlayerSkin();

	protected abstract Vector3f getLocalTranslation();

	protected AxisAngle4f getYawRotationAA() {
		Vector3f f = getForward();
		Vector3f u = getUp();

		//		if(playerState == state.getPlayer() && (Mouse.isButtonDown(2) || Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))){
		//			//not rotating
		//		}else{
		if(f.epsilonEquals(up, 0.01f)) {
			yaw = FastMath.atan2Fast(-u.x, -u.z);
		} else if(f.epsilonEquals(down, 0.01f)) {
			yaw = FastMath.atan2Fast(u.x, u.z);
		} else {
			yaw = FastMath.atan2Fast(f.x, f.z);
		}
		//		}
		AxisAngle4f aa = new AxisAngle4f(up, yaw);
		return aa;
	}

	public void setAnim(AnimationIndexElement e) {
		boneAttachable.setAnim(e, 0, 0.0f);

	}

	protected void setLoopMode(LoopMode loopMode) {
		boneAttachable.setLoopMode(loopMode);

	}

	protected void setLoopModeTorso(LoopMode loopMode) {
		boneAttachable.setLoopModeTorso(loopMode);

	}

	protected void setAnimSpeed(float speed) {
		boneAttachable.setAnimSpeed(speed);

	}

	protected void setAnimTorsoSpeed(float speed) {
		boneAttachable.setAnimTorsoSpeed(speed);

	}

	public void setAnim(AnimationIndexElement e, float blend) {
		boneAttachable.setAnim(e, 0, blend);
	}

	public void setAnim(AnimationIndexElement e, LoopMode loopMode) {
		boneAttachable.setAnim(e, 0, 0.0f, loopMode);
	}

	public void setAnim(AnimationIndexElement e, float blend, LoopMode loopMode) {
		boneAttachable.setAnim(e, 0, blend, loopMode);
	}

	public void setAnimTorso(AnimationIndexElement e) {
		boneAttachable.setAnimTorso(e, 0, 0.0f);
	}

	public void setAnimTorso(AnimationIndexElement e, LoopMode loopMode) {
		boneAttachable.setAnimTorso(e, 0, 0.0f, loopMode);
	}

	public void setAnimTorso(AnimationIndexElement e, float blend) {
		boneAttachable.setAnimTorso(e, 0, blend);
	}

	public void setAnimTorso(AnimationIndexElement e, float blend, LoopMode loopMode) {
		boneAttachable.setAnimTorso(e, 0, blend, loopMode);
	}

	protected String getAnimationString() {
		return "Torso: " + boneAttachable.getAnimTorsoName() + "; Main: " + boneAttachable.getAnimName();
	}

	public float getAnimTorsoMaxTime() {
		return boneAttachable.getAnimTorsoMaxTime();
	}

	public float getAnimMaxTime() {
		return boneAttachable.getAnimMaxTime();
	}

	public boolean isAnimTorso(AnimationIndexElement e) {
		return boneAttachable.isAnimTorso(e);
	}

	public boolean isAnim(AnimationIndexElement e) {
		return boneAttachable.isAnim(e);
	}

	public float getAnimTorsoTime() {
		return boneAttachable.getAnimTorsoTime();
	}

	public void setAnimTorsoTime(float time) {
		boneAttachable.setAnimTorsoTime(time);
	}

	public float getAnimTime() {
		return boneAttachable.getAnimTime();
	}

	public void setAnimTime(float time) {
		boneAttachable.setAnimTime(time);
	}

	public boolean isAnimActive() {
		return boneAttachable.isAnimActive();
	}

	public boolean isAnimTorsoActive() {
		return boneAttachable.isAnimTorsoActive();
	}

	public abstract Skin getSkin();

	public abstract BoneAttachable initBoneAttachable(E creature);

	public abstract AnimationStructEndPoint getAnimation(AnimationIndexElement ai);

	/**
	 * @return the boneAttachable
	 */
	public BoneAttachable getBoneAttachable() {
		return boneAttachable;
	}

	public abstract void inititalizeHolders(Collection<AnimatedObjectHoldAnimation> holders);

	protected abstract void handleState();

	public abstract CreatureStructure getCreatureStructure();

	@Override
	public void initializeListeners(AnimationController controller,
	                                AnimationChannel channel, final AnimationChannel channelTorso) {

		inititalizeHolders(holders);
		for(AnimatedObjectHoldAnimation a : holders) {
			a.init(controller, channel, channelTorso);
		}

		controller.addListener(new AnimationEventListener() {

			@Override
			public void onAnimChange(AnimationController animationController,
			                         AnimationChannel channel, String name) {

			}

			@Override
			public void onAnimCycleDone(AnimationController animationController,
			                            AnimationChannel channel, String name) {
				AnimationStructEndPoint animation = getAnimation(AnimationIndex.HELMET_ON);
				if(animation != null &&
						animation.animations != null &&
						animation.animations.length > 0 &&
						name.equals(animation.animations[0]) && getHeldBone() != null && !getHeldBone().name.equals("none")) {
					detach(getHeldBone(), "Helmet");
					attach(getHeadBone(), "Helmet");
				}
			}

			@Override
			public boolean removeOnFinished() {
				return false;
			}
		});
		controller.addListener(new AnimationEventListener() {

			@Override
			public void onAnimChange(AnimationController animationController,
			                         AnimationChannel channel, String name) {

			}

			@Override
			public void onAnimCycleDone(AnimationController animationController,
			                            AnimationChannel channel, String name) {
				AnimationStructEndPoint animation = getAnimation(AnimationIndex.HELMET_OFF);
				if(animation != null &&
						animation.animations != null &&
						animation.animations.length > 0 &&
						name.equals(animation.animations[0]) && getHeldBone() != null && !getHeldBone().name.equals("none")) {
					detach(getHeldBone(), "Helmet");
				}
			}

			@Override
			public boolean removeOnFinished() {
				return false;
			}
		});
		controller.addListener(new AnimationEventListener() {

			@Override
			public void onAnimChange(AnimationController animationController,
			                         AnimationChannel channel, String name) {

			}

			@Override
			public void onAnimCycleDone(AnimationController animationController,
			                            AnimationChannel channel, String name) {
				AnimationStructEndPoint animation = getAnimation(AnimationIndex.MOVING_JUMPING_JUMPUP);
				if(animation != null &&
						animation.animations != null &&
						animation.animations.length > 0 &&
						name.equals(animation.animations[0])) {
					setAnim(AnimationIndex.MOVING_JUMPING_JUMPDOWN);
					inJump = false;
				}
			}

			@Override
			public boolean removeOnFinished() {
				return false;
			}
		});

	}

	protected boolean hasRightHand() {
		return !"none".equals(getCreatureStructure().rightHand);
	}

	protected boolean hasLeftHand() {
		return !"none".equals(getCreatureStructure().leftHand);
	}

	protected boolean hasHeldBone() {
		return !"none".equals(getCreatureStructure().heldBone);
	}

	@Override
	public String getHeldBoneName() {
		return getCreatureStructure().heldBone;
	}

	protected boolean hasHeadBone() {
		return !"none".equals(getCreatureStructure().headBone);
	}

	protected boolean hasRightHolster() {
		return !"none".equals(getCreatureStructure().rightHolster);
	}

	protected boolean hasLeftHolster() {
		return !"none".equals(getCreatureStructure().leftHolster);
	}

	protected boolean hasRightBackHolster() {
		return !"none".equals(getCreatureStructure().rightBackHolster);
	}

	protected boolean hasLeftBackHolster() {
		return !"none".equals(getCreatureStructure().leftBackHolster);
	}

	protected boolean hasNeck() {
		return !"none".equals(getCreatureStructure().neck);
	}

	protected boolean hasRightCollarBone() {
		return !"none".equals(getCreatureStructure().rightCollarBone);
	}

	protected boolean hasLeftCollarBone() {
		return !"none".equals(getCreatureStructure().leftCollarBone);
	}

	protected Bone getRightHand() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().rightHand);
		return b;
	}

	protected Bone getLeftHand() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().leftHand);
		return b;
	}

	protected Bone getHeldBone() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().heldBone);
		return b;
	}

	protected Bone getHeadBone() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().headBone);
		return b;
	}

	protected Bone getRightHolster() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().rightHolster);
		return b;
	}

	protected Bone getRightForeArm() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().rightForeArm);
		return b;
	}

	protected Bone getLeftForeArm() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().leftForeArm);
		return b;
	}

	protected Bone getLeftHolster() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().leftHolster);
		return b;
	}

	protected Bone getRightBackHolster() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().rightBackHolster);
		return b;
	}

	protected Bone getLeftBackHolster() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().leftBackHolster);
		return b;
	}

	protected Bone getNeck() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().neck);
		return b;
	}

	protected Bone getRightCollarBone() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().rightCollarBone);
		return b;
	}

	protected Bone getLeftCollarBone() {
		Bone b = getSkin().getSkeleton().getBone(getCreatureStructure().leftCollarBone);
		return b;
	}

	/**
	 * @return the shadowMode
	 */
	public boolean isShadowMode() {
		return shadowMode;
	}

	/**
	 * @param shadowMode the shadowMode to set
	 */
	public void setShadowMode(boolean shadowMode) {
		this.shadowMode = shadowMode;
	}

	enum Dir {
		NORTH,
		NORTHEAST,
		EAST,
		SOUTHEAST,
		SOUTH,
		SOUTHWEST,
		WEST,
		NORTHWEST,
	}

	@Override
	public String getRootBoneName() {
		return getSkin().getSkeleton().getRootBone().name;
	}

	@Override
	public String getRootTorsoBoneName() {
		return getCreatureStructure().upperBody;
	}

}
