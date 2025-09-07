package org.schema.game.client.view.character;

import org.schema.schine.graphicsengine.animation.structure.classes.AnimationStructEndPoint;

public class AnimationState {

	public final AnimationStructEndPoint state;
	public final int anim;

	public AnimationState(AnimationStructEndPoint state, int anim) {
		super();
		assert (state.animations != null) : state;
		this.state = state;
		this.anim = anim;
	}

	public String getAnimation() {
		assert (state.animations != null);
		return state.animations[anim];
	}

}
