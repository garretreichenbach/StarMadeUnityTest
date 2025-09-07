/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>Bone</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * Bone.java
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

import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Matrix16f;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Quat4Util;
import org.schema.schine.graphicsengine.animation.Animation;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The Class Bone.
 */
public class Bone implements Drawable {

	/**
	 * The rm.
	 */
	static float rm;
	private final ObjectArrayList<Bone> childs = new ObjectArrayList<Bone>();
	/**
	 * MODEL SPACE -> BONE SPACE (in animated state)
	 */
	public Vector3f worldPos = new Vector3f();
	public Quat4f worldRot = new Quat4f();
	public Vector3f worldScale = new Vector3f(1, 1, 1);
	/**
	 * The bone matrix.
	 */
	public Matrix16f boneMatrix;
	/**
	 * The rel bone matrix.
	 */
	public Matrix16f relBoneMatrix;
	/**
	 * The bone id.
	 */
	public int boneID = -1;
	public String name;
	public float rDebug;
	/**
	 * The parent.
	 */
	Bone parent;
	/**
	 * The r.
	 */
	float r = FastMath.nextRandomFloat();
	/**
	 * The b.
	 */
	float b = FastMath.nextRandomFloat();
	/**
	 * The g.
	 */
	float g = FastMath.nextRandomFloat();
	/**
	 * The color.
	 */
	public Vector4f color = new Vector4f(r, b, g, 1.0f);
	Quat4f rotateTmp = new Quat4f();
	/**
	 * Initial transform is the local bind transform of this bone.
	 * PARENT SPACE -> BONE SPACE
	 */
	private Vector3f initialPos = new Vector3f();
	private Quat4f initialRot = new Quat4f(0, 0, 0, 1);
	private Vector3f initialScale = new Vector3f(1, 1, 1);
	/**
	 * The inverse world bind transform.
	 * BONE SPACE -> MODEL SPACE
	 */
	private Vector3f worldBindInversePos = new Vector3f();
	private Quat4f worldBindInverseRot = new Quat4f(0, 0, 0, 1);
	private Vector3f worldBindInverseScale = new Vector3f();
	/**
	 * The local animated transform combined with the local bind transform and parent world transform
	 */
	private Vector3f localPos = new Vector3f();
	private Quat4f localRot = new Quat4f();
	private Vector3f localScale = new Vector3f(1, 1, 1);
	private Vector3f addLocalPos = new Vector3f();
	private Quat4f addlocalRot = new Quat4f(0, 0, 0, 1);
	private Vector3f addlocalScale = new Vector3f();
	/**
	 * The skeleton.
	 */
	private Skeleton skeleton;
	/**
	 * The verts.
	 */
	private ObjectArrayList<VertexBoneWeight> verts = new ObjectArrayList<VertexBoneWeight>();
	private Animation animation;

	/**
	 * Instantiates a new bone.
	 *
	 * @param boneID      the bone id
	 * @param description the description
	 */
	public Bone(int boneID, String name) {
		this.boneID = boneID;
		this.name = name;
		addlocalRot.set(0, 0, 0, 1);
		relBoneMatrix = new Matrix16f();
	}

	@Override
	public void cleanUp() {

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {

		GlUtil.glPushMatrix();
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glBegin(GL11.GL_LINES);
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(100, 0, 0);
		GL11.glVertex3f(100, 0, 0);
		GL11.glVertex3f(90, 10, 0);
		GL11.glVertex3f(100, 0, 0);
		GL11.glVertex3f(90, -10, 0);
		GL11.glEnd();
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		for (Bone f : getChilds()) {
			if (f instanceof Drawable) {
				GlUtil.glPushMatrix();
				((Drawable) f).draw();
				GlUtil.glPopMatrix();
			}
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.SceneNode#onInit(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void onInit() {
	}

	/**
	 * @return the addLocalPos
	 */
	public Vector3f getAddLocalPos() {
		return addLocalPos;
	}

	/**
	 * @param addLocalPos the addLocalPos to set
	 */
	public void setAddLocalPos(Vector3f addLocalPos) {
		this.addLocalPos = addLocalPos;
	}

	/**
	 * @return the addlocalRot
	 */
	public Quat4f getAddLocalRot() {
		return addlocalRot;
	}

	/**
	 * @return the animation
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * @param animation the animation to set
	 */
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	/**
	 * @return the childs
	 */
	public List<Bone> getChilds() {
		return childs;
	}

	/**
	 * @return the initialPos
	 */
	public Vector3f getInitialPos() {
		return initialPos;
	}

	/**
	 * @param initialPos the initialPos to set
	 */
	public void setInitialPos(Vector3f initialPos) {
		this.initialPos = initialPos;
	}

	/**
	 * @return the initialRot
	 */
	public Quat4f getInitialRot() {
		return initialRot;
	}

	/**
	 * @return the initialScale
	 */
	public Vector3f getInitialScale() {
		return initialScale;
	}

	/**
	 * @return the localPos
	 */
	public Vector3f getLocalPos() {
		return localPos;
	}

	/**
	 * @return the localRot
	 */
	public Quat4f getLocalRot() {
		return localRot;
	}

	/**
	 * Stores the skinning transform in the specified Matrix4f.
	 * The skinning transform applies the animation of the bone to a vertex.
	 *
	 * @param t
	 */
	void getOffsetTransform(Transform t) {

		rotateTmp.mul(worldRot, worldBindInverseRot);

		Vector3f translate = new Vector3f();
		Vector3f invPos = new Vector3f();

		Quat4Util.mult(rotateTmp, worldBindInversePos, invPos);

		translate.add(worldPos, invPos);

		t.setIdentity();
		t.origin.set(translate);
		t.basis.set(rotateTmp);

		Matrix4f m = new Matrix4f();
		Controller.getMat(t, m);
		Matrix4fTools.scale(m, new Vector3f(
				worldBindInverseScale.x,
				worldBindInverseScale.y,
				worldBindInverseScale.z));

		Controller.getMat(m, t);

		assert (worldRot.x != 0 || worldRot.y != 0 || worldRot.z != 0 || worldRot.w != 0);

	}

	public Bone getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public void setParent(Bone parent) {
		this.parent = parent;
	}

	/**
	 * Gets the skeleton.
	 *
	 * @return the skeleton
	 */
	public Skeleton getSkeleton() {
		return skeleton;
	}

	/**
	 * Sets the skeleton.
	 *
	 * @param skeleton the new skeleton
	 */
	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}

	/**
	 * Gets the verts.
	 *
	 * @return the verts
	 */
	public ObjectArrayList<VertexBoneWeight> getVerts() {
		return verts;
	}

	/**
	 * Reset the bone and it's children to bind pose.
	 */
	final void reset() {
		//    	System.err.println("RESET "+name+": "+initialPos+"; "+initialRot);

		localPos.set(initialPos);
		localRot.set(initialRot);
		localScale.set(initialScale);

		for (Bone c : childs) {
			c.reset();
		}
	}

	/**
	 * @param addlocalRot the addlocalRot to set
	 */
	public void setAddlocalRot(Quat4f addlocalRot) {
		this.addlocalRot = addlocalRot;
	}

	/**
	 * Sets the local animation transform of this bone.
	 * Bone is assumed to be in bind pose when this is called.
	 *
	 * @param overwritingAnimation
	 */
	public void setAnimTransforms(Vector3f translation, Quat4f rotation, Vector3f scale, boolean overwritingAnimation) {
		if (overwritingAnimation) {
			localPos.set(initialPos);
			localRot.set(initialRot);
			localScale.set(initialScale);
		}
		localPos.add(translation);
		localRot.mul(rotation);
		localScale.x *= scale.x;
		localScale.y *= scale.y;
		localScale.z *= scale.z;
		assert (worldRot.x != 0 || worldRot.y != 0 || worldRot.z != 0 || worldRot.w != 0);
	}

	/**
	 * Saves the current bone state as it's binding pose, including it's children.
	 */
	void setBindingPose() {

		initialPos.set(localPos);
		initialRot.set(localRot);
		initialScale.set(localScale);

		// Save inverse derived position/scale/orientation, used for calculate offset transform later
		worldBindInversePos.set(worldPos);
		worldBindInversePos.negate();

		worldBindInverseRot.set(worldRot);
		worldBindInverseRot.inverse();

		worldBindInverseScale.x = 1f / worldScale.x;
		worldBindInverseScale.y = 1f / worldScale.y;
		worldBindInverseScale.z = 1f / worldScale.z;

		for (Bone b : childs) {
			b.setBindingPose();
		}
		assert (worldRot.x != 0 || worldRot.y != 0 || worldRot.z != 0 || worldRot.w != 0);
	}

	/**
	 * Sets local bind transform for bone.
	 * Call setBindingPose() after all of the skeleton bones' bind transforms are set to save them.
	 * This happens when parsing the skeleton
	 */
	public void setBindTransforms(Vector3f translation, Quat4f rotation, Vector3f scale) {
		initialPos.set(translation);
		initialRot.set(rotation);
		initialScale.set(scale);

		localPos.set(translation);
		localRot.set(rotation);
		localScale.set(scale);

	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#toString()
	 */
	@Override
	public String toString() {
		return "Bone - " + name + " - pos: " + localPos + ", rot: " + localRot;
	}

	/**
	 * Updates world transforms for this bone and it's children.
	 *
	 * @param timer
	 */
	void update(Timer timer) {
		updateWorldVectors();

		for (Bone b : childs) {
			b.update(timer);
		}
	}

	/**
	 * Updates the world transforms for this bone, and, possibly the attach node
	 * if not null.
	 */
	void updateWorldVectors() {

		if (parent != null) {

			Quat4f rot = new Quat4f(localRot);

			rot.mul(addlocalRot);

			// worldRot = localRot * parentWorldRot

			worldRot = Quat4Util.mult(parent.worldRot, rot, new Quat4f());

//			System.err.println("CHILD SET: "+worldRot+"; parent "+parent.worldRot+"; local "+localRot+"; add "+addlocalRot+" = "+rot+"; init: "+ getInitialRot());

			// worldPos = parentWorldPos + (parentWorldRot * localPos)
			worldPos.set(Quat4Util.mult(parent.worldRot, localPos, new Vector3f()));

			worldPos.add(parent.worldPos);
			worldPos.add(addLocalPos);

			worldScale.x = localScale.x * parent.worldScale.x;
			worldScale.y = localScale.y * parent.worldScale.y;
			worldScale.z = localScale.z * parent.worldScale.z;

//			System.err.println("NOW: "+worldScale+"   "+name);
			//			System.err.println(name+":  "+initialPos+" ----> "+worldPos+"    =  "+parent.worldPos+" + "+localTimesParentWorldRot+"  local: "+getLocalPos());

		} else {
			worldRot.set(localRot);
			worldPos.set(localPos);
			worldScale.set(localScale);

//			System.err.println("SET: "+worldRot+"   "+getInitialRot()+"; "+name+" : "+childs.size());
		}
		assert (worldRot.x != 0 || worldRot.y != 0 || worldRot.z != 0 || worldRot.w != 0);
	}

	/**
	 * @return the localScale
	 */
	public Vector3f getLocalScale() {
		return localScale;
	}

	/**
	 * @param localScale the localScale to set
	 */
	public void setLocalScale(Vector3f localScale) {
		this.localScale = localScale;
	}

}
