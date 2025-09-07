package org.schema.schine.graphicsengine.animation;

import java.util.ArrayList;
import java.util.BitSet;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Bone;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * <code>AnimChannel</code> provides controls, such as play, pause,
 * fast forward, etc, for an animation. The animation
 * channel may influence the entire model or specific bones of the model's
 * skeleton. A single model may have multiple animation channels influencing
 * various parts of its body. For example, a character model may have an
 * animation channel for its feet, and another for its torso, and
 * the animations for each channel are controlled independently.
 *
 * @author Kirill Vainer
 */
public final class AnimationChannel {

	private static final float DEFAULT_BLEND_TIME = 0.15f;

	private static final int MAX_BLEND_QUEUE = 1;
	private final ArrayList<Blend> blendQueue = new ArrayList<Blend>();
	private AnimationController control;

	private BitSet affectedBones;
	private IntOpenHashSet affectedBonesIndexSet;
	private Animation animation;

	private float time;
	private float speed;
	private LoopMode loopMode;
	private boolean overwritePreviousAnimation;

	AnimationChannel(AnimationController control) {
		this.control = control;
	}

	private static float clampWrapTime(float t, float max, LoopMode loopMode) {
		if (max == Float.POSITIVE_INFINITY)
			return t;

		if (t < 0f) {
			//float tMod = -(-t % max);
			return switch(loopMode) {
				case DONT_LOOP_DEACTIVATE -> 0;
				case DONT_LOOP -> 0;
				case CYCLE -> t;
				case LOOP -> max - t;
			};
		} else if (t > max) {
			return switch(loopMode) {
				case DONT_LOOP_DEACTIVATE -> max;
				case DONT_LOOP -> max;
				case CYCLE -> /*-max;*/-(2f * max - t);
				case LOOP -> t - max;
			};
		}

		return t;
	}

	/**
	 * Add all the bones of the model's skeleton to be
	 * influenced by this animation channel.
	 */
	public void addAllBones() {
		affectedBones = null;
	}

	/**
	 * Add a single bone to be influenced by this animation channel.
	 */
	public void addBone(Bone bone) {
		int boneIndex = bone.boneID;
		if (affectedBones == null) {
			affectedBones = new BitSet(control.getSkeleton().getBoneCount());
			affectedBonesIndexSet = new IntOpenHashSet(control.getSkeleton().getBoneCount());
		}
		affectedBones.set(boneIndex);
		affectedBonesIndexSet.add(boneIndex);
	}

	/**
	 * Add a single bone to be influenced by this animation channel.
	 */
	public void addBone(String name) {
		addBone(control.getSkeleton().getBone(name));
	}

	/**
	 * Add bones to be influenced by this animation channel, starting
	 * from the given bone and going toward its children.
	 */
	public void addFromRootBone(Bone bone) {
		addBone(bone);
		if (bone.getChilds() == null) {
			return;
		}
		for (Bone childBone : bone.getChilds()) {
			//			addBone(childBone);
			addFromRootBone(childBone);
		}
	}

	/**
	 * Add bones to be influenced by this animation channel, starting
	 * from the given named bone and going toward its children.
	 */
	public void addFromRootBone(String name) {
		addFromRootBone(control.getSkeleton().getBone(name));
	}

	/**
	 * Add bones to be influenced by this animation channel starting from the
	 * given bone and going toward the root bone.
	 */
	public void addToRootBone(Bone bone) {
		addBone(bone);
		while (bone.getParent() != null) {
			bone = bone.getParent();
			addBone(bone);
		}
	}

	/**
	 * Add bones to be influenced by this animation channel starting from the
	 * given bone name and going toward the root bone.
	 */
	public void addToRootBone(String name) {
		addToRootBone(control.getSkeleton().getBone(name));
	}

	public BitSet getAffectedBones() {
		return affectedBones;
	}

	/**
	 * @return the affectedBonesIndexSet
	 */
	public IntOpenHashSet getAffectedBonesIndexSet() {
		return affectedBonesIndexSet;
	}

	/**
	 * @return The name of the currently playing animation, or null if
	 * none is assigned.
	 * @see AnimChannel#setAnim(java.lang.String)
	 */
	public String getAnimationName() {
		return animation != null ? animation.getName() : "no animation";
	}

	/**
	 * @return The length of the currently playing animation, or zero
	 * if no animation is playing.
	 * @see AnimChannel#getTime()
	 */
	public float getAnimMaxTime() {
		return animation != null ? animation.getLength() : 0f;
	}

	/**
	 * Returns the parent control of this AnimChannel.
	 *
	 * @return the parent control of this AnimChannel.
	 * @see AnimControl
	 */
	public AnimationController getControl() {
		return control;
	}

	/**
	 * @return The loop mode currently set for the animation. The loop mode
	 * determines what will happen to the animation once it finishes
	 * playing.
	 * <p/>
	 * For more information, see the LoopMode enum class.
	 * @see LoopMode
	 * @see AnimChannel#setLoopMode(com.jme3.animation.LoopMode)
	 */
	public LoopMode getLoopMode() {
		return loopMode;
	}

	/**
	 * @param loopMode Set the loop mode for the channel. The loop mode
	 *                 determines what will happen to the animation once it finishes
	 *                 playing.
	 *                 <p/>
	 *                 For more information, see the LoopMode enum class.
	 * @see LoopMode
	 */
	public void setLoopMode(LoopMode loopMode) {
		this.loopMode = loopMode;
	}

	/**
	 * @return The speed that is assigned to the animation channel. The speed
	 * is a scale value starting from 0.0, at 1.0 the animation will play
	 * at its default speed.
	 * @see AnimChannel#setSpeed(float)
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed Set the speed of the animation channel. The speed
	 *              is a scale value starting from 0.0, at 1.0 the animation will play
	 *              at its default speed.
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/**
	 * @return The time of the currently playing animation. The time
	 * starts at 0 and continues on until getAnimMaxTime().
	 * @see AnimChannel#setTime(float)
	 */
	public float getTime() {
		return time;
	}

	/**
	 * @param time Set the time of the currently playing animation, the time
	 *             is clamped from 0 to {@link #getAnimMaxTime()}.
	 */
	public void setTime(float time) {
		this.time = FastMath.clamp(time, 0, getAnimMaxTime());
	}

	public boolean isActive() {
		return animation != null;
	}

	/**
	 * @return the overwritePreviousAnimation
	 */
	public boolean isOverwritePreviousAnimation() {
		return overwritePreviousAnimation;
	}

	/**
	 * @param overwritePreviousAnimation the overwritePreviousAnimation to set
	 */
	public void setOverwritePreviousAnimation(boolean overwritePreviousAnimation) {
		this.overwritePreviousAnimation = overwritePreviousAnimation;
	}

	/**
	 * Set the current animation that is played by this AnimChannel.
	 * <p/>
	 * See {@link #setAnim(java.lang.String, float)}.
	 * The blendTime argument by default is 150 milliseconds.
	 *
	 * @param name The name of the animation to play
	 */
	public void setAnim(String name) {
		setAnim(name, DEFAULT_BLEND_TIME);
	}

	/**
	 * Set the current animation that is played by this AnimChannel.
	 * <p/>
	 * This resets the time to zero, and optionally blends the animation
	 * over <code>blendTime</code> seconds with the currently playing animation.
	 * Notice that this method will reset the control's speed to 1.0.
	 *
	 * @param name      The name of the animation to play
	 * @param blendTime The blend time over which to blend the new animation
	 *                  with the old one. If zero, then no blending will occur and the new
	 *                  animation will be applied instantly.
	 */
	public void setAnim(String name, float blendTime) {
		if (name == null) {
			animation = null;
			return;
		}
		//			throw new NullPointerException("no name");

		if (blendTime < 0f)
			throw new IllegalArgumentException("blendTime cannot be less than zero");

		Animation anim = control.getAnim(name);
		if (anim == null) {
			throw new IllegalArgumentException("Cannot find animation named: '" + name + "' in " + control.getAnimationNames());
		}

		control.notifyAnimChange(this, name);

		if (animation != null && blendTime > 0f) {
			// activate blending
			Blend blend = new Blend();

			blend.blendFrom = animation;
			blend.timeBlendFrom = time;
			blend.speedBlendFrom = speed;
			blend.loopModeBlendFrom = loopMode;
			blend.blendAmount = 0f;
			blend.blendRate = 1f / blendTime;

			blendQueue.add(0, blend);
			while (blendQueue.size() > MAX_BLEND_QUEUE) {
				blendQueue.remove(blendQueue.size() - 1);
			}
		}

		animation = anim;
		time = 0;
		speed = 1f;
		loopMode = LoopMode.LOOP;
	}

	void update(Timer timer) {
		if (!isActive()) {
			return;
		}
		float frameTime = timer != null ? timer.getDelta() : 0;
//		if (blendFrom != null){
//			blendFrom.setTime(timeBlendFrom, 1f - blendAmount, control, this);
//			//blendFrom.setTime(timeBlendFrom, control.skeleton, 1f - blendAmount, affectedBones);
//			timeBlendFrom += timer.getDelta() * speedBlendFrom;
//			timeBlendFrom = clampWrapTime(timeBlendFrom,
//					blendFrom.getLength(),
//					loopModeBlendFrom);
//			if (timeBlendFrom < 0){
//				timeBlendFrom = -timeBlendFrom;
//				speedBlendFrom = -speedBlendFrom;
//			}
//
//			blendAmount += timer.getDelta() * blendRate;
//			if (blendAmount > 1f){
//				blendAmount = 1f;
//				blendFrom = null;
//			}
//		}
		float smallest = 1.0f;
		for (int i = 0; i < blendQueue.size(); i++) {

			Blend blend = blendQueue.get(i);
			blend.blendFrom.setTime(blend.timeBlendFrom, 1f - blend.blendAmount, control, this);
			//blendFrom.setTime(timeBlendFrom, control.skeleton, 1f - blendAmount, affectedBones);
			blend.timeBlendFrom += frameTime * blend.speedBlendFrom;
			blend.timeBlendFrom = clampWrapTime(blend.timeBlendFrom,
					blend.blendFrom.getLength(),
					blend.loopModeBlendFrom);
			if (blend.timeBlendFrom < 0) {
				blend.timeBlendFrom = -blend.timeBlendFrom;
				blend.speedBlendFrom = -blend.speedBlendFrom;
			}

			blend.blendAmount += frameTime * blend.blendRate;
			if (blend.blendAmount > 1f) {
				blend.blendAmount = 1f;
				blendQueue.remove(i);
				i--;
//				blendFrom = null;
			}
			smallest = Math.min(blend.blendAmount, smallest);
		}

		animation.setTime(time, smallest, control, this);
		//animation.setTime(time, control.skeleton, blendAmount, affectedBones);
		time += frameTime * speed;

		if (animation.getLength() > 0) {
			if (time >= animation.getLength()) {
				control.notifyAnimCycleDone(this, animation.getName());
			} else if (time < 0) {
				control.notifyAnimCycleDone(this, animation.getName());
			}
		}
		//could be deactivated in listener callback
		if (!isActive()) {
			return;
		}
		time = clampWrapTime(time, animation.getLength(), loopMode);
		if (loopMode == LoopMode.DONT_LOOP_DEACTIVATE && time == animation.getLength()) {
			animation = null;
		}

		if (time < 0) {
			time = -time;
			speed = -speed;
		}

	}

	private class Blend {
		private Animation blendFrom;
		private float timeBlendFrom;
		private float speedBlendFrom;
		private float blendAmount = 1f;
		private float blendRate = 0;
		private LoopMode loopModeBlendFrom;
	}

}
