package org.schema.game.client.view.character;

import org.schema.schine.graphicsengine.animation.AnimationChannel;
import org.schema.schine.graphicsengine.animation.AnimationController;
import org.schema.schine.graphicsengine.animation.AnimationEventListener;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructEndPoint;
import org.schema.schine.graphicsengine.forms.Bone;

public abstract class AnimatedObjectHoldAnimation {
	private AnimationIndexElement draw;
	private AnimationIndexElement idle;
	private AnimationIndexElement putAway;
	private final AbstractAnimatedObject<?> o;
	private String model;

	private Bone hand;
	private Bone holster;

	public AnimatedObjectHoldAnimation(AbstractAnimatedObject<?> o, AnimationIndexElement draw,
	                                   AnimationIndexElement idle, AnimationIndexElement putAway,
	                                   String model, Bone hand, Bone holster) {
		super();
		this.draw = draw;
		this.idle = idle;
		this.putAway = putAway;
		this.o = o;
		this.model = model;
		this.hand = hand;
		this.holster = holster;
	}

	public abstract boolean checkCondition();

	public void apply() {
		if (checkCondition()) {
			
			if (!o.isAttachedAnything(hand)) {
			
				if (!o.isAnimTorsoActive()) {
					o.setAnimTorso(draw, LoopMode.DONT_LOOP);
					if (holster != null) {
						o.detach(holster, model);
					}
					o.attach(hand, model);
				}
				if (o.isAnimTorsoActive() && o.isAnimTorso(putAway)) {
					float t = o.getAnimTorsoTime();
					o.setAnimTorso(draw, LoopMode.DONT_LOOP);
					o.setAnimTorsoTime(o.getAnimTorsoMaxTime() - t);
					if (holster != null) {
						o.detach(holster, model);
					}
					o.attach(hand, model);
				}
			}
		} else {
			if (o.isAnimTorsoActive() && (o.isAnimTorso(draw) || o.isAnimTorso(idle)) && o.isAttached(hand, model)) {
				if (o.isAnimTorso(draw)) {
					float t = o.getAnimTorsoTime();
					o.setAnimTorso(putAway, LoopMode.DONT_LOOP_DEACTIVATE);
					o.setAnimTorsoTime(o.getAnimTorsoMaxTime() - t);
					if (holster != null) {
						o.detach(holster, model);
					}
					o.attach(hand, model);
				} else {
					o.setAnimTorso(putAway, LoopMode.DONT_LOOP_DEACTIVATE);
					if (holster != null) {
						o.detach(holster, model);
					}
					o.attach(hand, model);
				}
			}

		}
	}

	public void init(AnimationController controller,
	                 AnimationChannel channel, final AnimationChannel channelTorso) {
		controller.addListener(new AnimationEventListener() {

			@Override
			public void onAnimChange(AnimationController animationController,
			                         AnimationChannel channel, String name) {

			}

			@Override
			public void onAnimCycleDone(AnimationController animationController,
			                            AnimationChannel channel, String name) {
				AnimationStructEndPoint animation = o.getAnimation(draw);
				if (name.equals(animation.animations[0])) {
					o.setAnimTorso(idle, 0.0f);
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
				AnimationStructEndPoint animation = o.getAnimation(putAway);
				if (name.equals(animation.animations[0])) {
					if(o.isAttached(hand, model)){
						o.detach(hand, model);
						if (holster != null) {
							if(o.isAttachedAnything(holster)){
								o.clearAttached(holster);
							}
							o.attach(holster, model);
						}
					}
				}
			}

			@Override
			public boolean removeOnFinished() {
				return false;
			}
		});
	}

}
