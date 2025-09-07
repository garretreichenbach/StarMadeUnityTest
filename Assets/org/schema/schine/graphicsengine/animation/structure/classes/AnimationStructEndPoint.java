package org.schema.schine.graphicsengine.animation.structure.classes;

import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.Node;

public abstract class AnimationStructEndPoint extends AnimationStructSet {
	public String[] animations;

	public String desc = "NOTHING";
	private ArrayList<String> animation = new ArrayList<String>();

	public String get() {
		return animations[0];
	}

	@Override
	public void checkAnimations(String def) {

		int size = animation.size();
		if (size == 0) {
			animation.add(def);
			animations = new String[1];
			animations[0] = def;
		} else {
			animations = new String[size];
			for (int i = 0; i < animation.size(); i++) {
				animations[i] = animation.get(i);
			}
		}
		desc = animation.toString();
		animation = null;
	}

	@Override
	public void parseAnimation(Node root, String def) {
		if (root != null) {
			animation.add(root.getTextContent());
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "; " + desc + "; " + (animation != null ? Arrays.toString(animations) : "null");
	}

	public abstract AnimationIndexElement getIndex();
}
