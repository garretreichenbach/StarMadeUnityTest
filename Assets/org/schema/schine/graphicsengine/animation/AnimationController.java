package org.schema.schine.graphicsengine.animation;

import java.util.ArrayList;
import java.util.Collection;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Skeleton;
import org.schema.schine.graphicsengine.forms.Skin;

public final class AnimationController extends AbstractController implements Cloneable {

	/**
	 * Animation channels
	 */
	private final ArrayList<AnimationChannel> channels = new ArrayList<AnimationChannel>();
	/**
	 * Animation event listeners
	 */
	private final ArrayList<AnimationEventListener> listeners = new ArrayList<AnimationEventListener>();
	/**
	 * Skeleton object must contain corresponding data for the targets' weight buffers.
	 */
	Skin skin;
	long lastUpdate = -1;

	/**
	 * Serialization only. Do not use.
	 */
	public AnimationController() {
	}

	//    /**
	//     * Internal use only.
	//     */
	//    public Control cloneForSpatial(Spatial spatial) {
	//        try {
	//            AnimControl clone = (AnimControl) super.clone();
	//            clone.spatial = spatial;
	//            clone.channels = new ArrayList<AnimChannel>();
	//            clone.listeners = new ArrayList<AnimEventListener>();
	//
	//            if (skin.getSkeleton() != null) {
	//                clone.skin.getSkeleton() = new Skeleton(skin.getSkeleton());
	//            }
	//
	//            // animationMap is reference-copied, animation data should be shared
	//            // to reduce memory usage.
	//
	//            return clone;
	//        } catch (CloneNotSupportedException ex) {
	//            throw new AssertionError();
	//        }
	//    }

	/**
	 * Creates a new animation control for the given skin.getSkeleton().
	 * The method {@link AnimControl#setLoadedAnimations(java.util.HashMap) }
	 * must be called after initialization in order for this class to be useful.
	 *
	 * @param skin.getSkeleton() The skin.getSkeleton() to animate
	 */
	public AnimationController(Skin skin) {
		this.skin = skin;
		if (this.skin.getSkeleton() != null && !this.skin.getSkeleton().isInitialized()) {
			this.skin.getSkeleton().initialize();
		}
		reset();
	}

	/**
	 * Adds an animation to be available for playing to this
	 * <code>AnimControl</code>.
	 *
	 * @param anim The animation to add.
	 */
	public void addAnim(Animation anim) {

		skin.getSkeleton().addAnim(anim);
	}

	/**
	 * Adds a new listener to receive animation related events.
	 *
	 * @param listener The listener to add.
	 */
	public void addListener(AnimationEventListener listener) {
		if (listeners.contains(listener)) {
			throw new IllegalArgumentException("The given listener is already "
					+ "registed at this AnimControl");
		}

		listeners.add(listener);
	}

	/**
	 * Clears all the channels that were created.
	 *
	 * @see AnimControl#createChannel()
	 */
	public void clearChannels() {
		channels.clear();
	}

	/**
	 * Clears all the listeners added to this <code>AnimControl</code>
	 *
	 * @see AnimControl#addListener(com.jme3.animation.AnimEventListener)
	 */
	public void clearListeners() {
		listeners.clear();
	}

	/**
	 * Create a new animation channel, by default assigned to all bones
	 * in the skin.getSkeleton().
	 *
	 * @return A new animation channel for this <code>AnimControl</code>.
	 */
	public AnimationChannel createChannel() {
		AnimationChannel channel = new AnimationChannel(this);
		channels.add(channel);
		return channel;
	}

	/**
	 * Retrieve an animation from the list of animations.
	 *
	 * @param name The name of the animation to retrieve.
	 * @return The animation corresponding to the given name, or null, if no
	 * such named animation exists.
	 */
	public Animation getAnim(String name) {

		return skin.getSkeleton().getAnim(name);
	}

	/**
	 * Returns the length of the given named animation.
	 *
	 * @param name The name of the animation
	 * @return The length of time, in seconds, of the named animation.
	 */
	public float getAnimationLength(String name) {
		return skin.getSkeleton().getAnimationLength(name);
	}

	/**
	 * @return The names of all animations that this <code>AnimControl</code>
	 * can play.
	 */
	public Collection<String> getAnimationNames() {
		return skin.getSkeleton().getAnimationNames();
	}

	/**
	 * Return the animation channel at the given index.
	 *
	 * @param index The index, starting at 0, to retrieve the <code>AnimChannel</code>.
	 * @return The animation channel at the given index, or throws an exception
	 * if the index is out of bounds.
	 * @throws IndexOutOfBoundsException If no channel exists at the given index.
	 */
	public AnimationChannel getChannel(int index) {
		return channels.get(index);
	}

	/**
	 * @return The number of channels that are controlled by this
	 * <code>AnimControl</code>.
	 * @see AnimControl#createChannel()
	 */
	public int getNumChannels() {
		return channels.size();
	}

	/**
	 * @return The skin.getSkeleton() of this <code>AnimControl</code>.
	 */
	public Skeleton getSkeleton() {
		return skin.getSkeleton();
	}

	void notifyAnimChange(AnimationChannel channel, String name) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onAnimChange(this, channel, name);
		}
	}

	//    /**
	//     * Internal use only.
	//     */
	//    @Override
	//    public void setSpatial(Spatial spatial) {
	//        if (spatial == null && skin.getSkeleton()Control != null) {
	//            this.spatial.removeControl(skin.getSkeleton()Control);
	//        }
	//
	//        super.setSpatial(spatial);
	//
	//        //Backward compatibility.
	//        if (spatial != null && skin.getSkeleton()Control != null) {
	//            spatial.addControl(skin.getSkeleton()Control);
	//        }
	//    }

	void notifyAnimCycleDone(AnimationChannel channel, String name) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onAnimCycleDone(this, channel, name);
			if (listeners.get(i).removeOnFinished()) {
				listeners.remove(i);
				i--;
			}
		}
	}

	/**
	 * Remove an animation so that it is no longer available for playing.
	 *
	 * @param anim The animation to remove.
	 */
	public void removeAnim(Animation anim) {
		skin.getSkeleton().removeAnim(anim);
	}

	/**
	 * Removes the given listener from listening to events.
	 *
	 * @param listener
	 * @see AnimControl#addListener(com.jme3.animation.AnimEventListener)
	 */
	public void removeListener(AnimationEventListener listener) {
		if (!listeners.remove(listener)) {
			throw new IllegalArgumentException("The given listener is not "
					+ "registed at this AnimControl");
		}
	}

	final void reset() {
		if (skin != null) {
			skin.getSkeleton().reset();
		}
	}

	/**
	 * Internal use only.
	 *
	 * @
	 */
	@Override
	public void update(Timer timer) {

		if (skin.getSkeleton() != null) {
			if (!skin.getSkeleton().isInitialized()) {
				skin.getSkeleton().initialize();
			}

			skin.getSkeleton().reset(); // reset skin.getSkeleton() to bind pose
			for (int i = 0; i < channels.size(); i++) {
				//if updated twice in the same frame (e.g. for shadows) update with 0 time the second time
				channels.get(i).update(timer.lastUpdate != lastUpdate ? timer : null);
			}
			lastUpdate = timer.lastUpdate;
			if (skin != null) {
				skin.update(timer);
			}
		}

	}

}
