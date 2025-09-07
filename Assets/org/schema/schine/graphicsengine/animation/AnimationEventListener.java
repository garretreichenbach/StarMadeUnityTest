package org.schema.schine.graphicsengine.animation;

public abstract class AnimationEventListener {

	public abstract void onAnimChange(AnimationController animationController,
	                                  AnimationChannel channel, String name);

	public abstract void onAnimCycleDone(AnimationController animationController,
	                                     AnimationChannel channel, String name);

	public abstract boolean removeOnFinished();

}
