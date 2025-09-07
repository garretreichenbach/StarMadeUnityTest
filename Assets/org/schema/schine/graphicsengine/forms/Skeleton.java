/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Skeleton</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Skeleton.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.graphicsengine.forms;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.animation.Animation;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * The Class Skeleton.
 */
public class Skeleton {

	private final HashMap<Bone, Skeleton> attachments = new HashMap<Bone, Skeleton>();
	/**
	 * The bones.
	 */
	private final Int2ObjectOpenHashMap<Bone> bones = new Int2ObjectOpenHashMap<Bone>();
	private final Int2ObjectOpenHashMap<Transform> skinningMatrices = new Int2ObjectOpenHashMap<Transform>();
	/**
	 * List of animations
	 */
	private final HashMap<String, Animation> animationMap = new HashMap<String, Animation>();
	/**
	 * The root bone.
	 */
	private Bone rootBone;
	private boolean initialized;
	private Transform[] boneMatrices;

	/**
	 * Adds an animation to be available for playing to this
	 * <code>AnimControl</code>.
	 *
	 * @param anim The animation to add.
	 */
	public void addAnim(Animation anim) {

		animationMap.put(anim.getName(), anim);
	}

	public Transform[] computeSkinningMatrices() {
		if (boneMatrices == null) {
			boneMatrices = new Transform[skinningMatrices.size()];
			for (int i = 0; i < boneMatrices.length; i++) {
				boneMatrices[i] = new Transform();
				boneMatrices[i].setIdentity();
			}
//			assert(boneMatrices.length == Skin.BONE):boneMatrices.length; //const int NUM_BONES = 29; in shader
		}
		for (Entry<Integer, Bone> e : bones.entrySet()) {
			Transform transform = skinningMatrices.get(e.getKey());
			e.getValue().getOffsetTransform(transform);
			boneMatrices[e.getValue().boneID].set(transform);
		}
		return boneMatrices;
	}

	private void createSkinningMatrices() {
		for (Integer i : bones.keySet()) {
			Transform t = new Transform();
			t.setIdentity();
			skinningMatrices.put(i, t);
		}
	}

	/**
	 * Retrieve an animation from the list of animations.
	 *
	 * @param name The name of the animation to retrieve.
	 * @return The animation corresponding to the given name, or null, if no
	 * such named animation exists.
	 */
	public Animation getAnim(String name) {
		Animation animation = animationMap.get(name);
		if (animation == null) {
			throw new IllegalArgumentException("animation '" + name + "' not found in " + animationMap);
		}
		return animation;
	}

	/**
	 * Returns the length of the given named animation.
	 *
	 * @param name The name of the animation
	 * @return The length of time, in seconds, of the named animation.
	 */
	public float getAnimationLength(String name) {
		Animation a = animationMap.get(name);
		if (a == null) {
			throw new IllegalArgumentException("The animation " + name
					+ " does not exist in this AnimControl");
		}

		return a.getLength();
	}

	/**
	 * @return the animationMap
	 */
	public HashMap<String, Animation> getAnimationMap() {
		return animationMap;
	}

	/**
	 * @return The names of all animations that this <code>AnimControl</code>
	 * can play.
	 */
	public Collection<String> getAnimationNames() {
		return animationMap.keySet();
	}

	public Bone getBone(String name) {
		int boneIndex = getBoneIndex(name);
		return boneIndex >= 0 ? bones.get(boneIndex) : null;
	}

	public int getBoneCount() {
		return bones.size();
	}

	public int getBoneIndex(String boneName) {
		for (Bone b : bones.values()) {
			if (b.name.equals(boneName)) {
				return b.boneID;
			}
		}
		System.err.println("WARNING: bone not found in skeleton: " + boneName);
//		assert(false):boneName+"; ";
		return -1;
	}

	/**
	 * Gets the bones.
	 *
	 * @return the bones
	 */
	public Int2ObjectOpenHashMap<Bone> getBones() {
		return bones;
	}

	/**
	 * Gets the root bone.
	 *
	 * @return the root bone
	 */
	public Bone getRootBone() {
		return rootBone;
	}

	/**
	 * Sets the root bone.
	 *
	 * @param rootBone the new root bone
	 */
	public void setRootBone(Bone rootBone) {
		this.rootBone = rootBone;
	}

	public void initialize() {

		createSkinningMatrices();

		rootBone.update(null);

		rootBone.setBindingPose();

		initialized = true;
	}

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Remove an animation so that it is no longer available for playing.
	 *
	 * @param anim The animation to remove.
	 */
	public void removeAnim(Animation anim) {
		if (!animationMap.containsKey(anim.getName())) {
			throw new IllegalArgumentException("Given animation does not exist "
					+ "in this AnimControl");
		}

		animationMap.remove(anim.getName());
	}

	/**
	 * Reset the skeleton to bind pose.
	 */
	public void reset() {

		rootBone.reset();

	}

	/**
	 * @param list the animations to set
	 */
	public void setLoadedAnimations(List<Animation> list) {
		for (Animation a : list) {
			animationMap.put(a.getName(), a);
		}
	}

	/**
	 * Update.
	 *
	 * @param timer the frame time
	 * @param u     the u
	 * @
	 */
	public void update(Timer timer) {
		if (!initialized) {
			initialize();
		}
		rootBone.update(timer);
	}

	public void updateAttachment(Timer timer, Bone attachmentBone) {
//		System.err.println("BB: "+rootBone);
		Vector3f localPosBef = new Vector3f(rootBone.getLocalPos());
		Quat4f localRotBef = new Quat4f(rootBone.getLocalRot());
		Vector3f localScaleBef = new Vector3f(rootBone.getLocalScale());

		rootBone.getLocalPos().set(attachmentBone.worldPos);
		rootBone.getLocalRot().set(attachmentBone.worldRot);
		rootBone.getLocalScale().set(attachmentBone.worldScale);

		rootBone.getAddLocalRot().set(attachmentBone.getAddLocalRot());
		rootBone.getAddLocalPos().set(attachmentBone.getAddLocalPos());

		rootBone.update(timer);

		rootBone.getLocalPos().set(localPosBef);
		rootBone.getLocalRot().set(localRotBef);
		rootBone.getLocalScale().set(localScaleBef);

	}
}
