package org.schema.schine.graphicsengine.animation.structure.classes;

public abstract class AnimationIndexElement {
	public abstract AnimationStructEndPoint get(AnimationStructure root);

	public abstract boolean isType(Class<? extends AnimationStructSet> clazz);

}
