package org.schema.game.client.view.character;

import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndexElement;

public class AnimationNotSetException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public AnimationNotSetException(AnimationIndexElement animationState) {
		super(animationState != null ? animationState.toString() : "noAnimation");
	}

}
