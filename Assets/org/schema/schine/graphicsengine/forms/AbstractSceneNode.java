/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>AbstractSceneNode</H2>
 * <H3>org.schema.schine.graphicsengine.forms</H3>
 * AbstractSceneNode.java
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

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.schine.graphicsengine.animation.Animation;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.texture.Material;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Standart AbstractSceneNode object for openGL11. It describes an abstact AbstractSceneNode wich can be
 * anything. it has a Position a Rotation and a Scale. To implement individually
 * are the update (how to change the pos, rot, scale) and the draw Function
 * (what actually will be drawn)
 *
 * @author schema
 */
public abstract class AbstractSceneNode implements Positionable, Scalable, Drawable {

	/**
	 * The Constant VISIBILITY_VISIBLE.
	 */
	public static final int VISIBILITY_VISIBLE = 1;

	/**
	 * The Constant VISIBILITY_HIDDEN.
	 */
	public static final int VISIBILITY_HIDDEN = 2;

	public static final int VISIBILITY_TREE_VISIBLE = 4;

	public static final int VISIBILITY_TREE_HIDDEN = 8;

	/**
	 * The mirror mode.
	 */
	private static boolean mirrorMode;

	/**
	 * The temp trans.
	 */
	private static float[] tempTrans = new float[16];
	//static FloatBuffer fbTemp = MemoryUtil.memAllocFloat(tempTrans.length);
	/**
	 * Billboard spherical begin.
	 *
	 * @param gl the gl
	 * @param glu the glu
	 * @param camX the cam x
	 * @param camY the cam y
	 * @param camZ the cam z
	 * @param objPosX the obj pos x
	 * @param objPosY the obj pos y
	 * @param objPosZ the obj pos z
	 */
	//	protected void billboardSphericalBegin() {
	//
	//		float modelview[] = new float[16] ;
	//		int i,j;
	//
	//		// save the current modelview matrix
	//		GlUtil.glPushMatrix();
	//
	//		// get the current modelview matrix
	//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX , modelview, 0);
	//
	//		// undo all rotations
	//		// beware all scaling is lost as well
	//		for( i=0; i<3; i++ )
	//		    for( j=0; j<3; j++ ) {
	//			if ( i==j )
	//			    modelview[i*4+j] = 1.0f;
	//			else
	//			    modelview[i*4+j] = 0.0f;
	//		    }
	//
	//		// set the modelview with no rotations
	//		GL11.glLoadMatrixf(modelview, 0);
	//
	//
	//	}
	protected boolean isTransformRotScaleIdentity() {
		return scale.x == 1f && scale.y == 1f && scale.z == 1f 
				&& transform.basis.m00 == 1.0f && transform.basis.m11 == 1.0f && transform.basis.m22 == 1.0f;
	}
	private static FloatBuffer tempModelviewBuffer = MemoryUtil.memAllocFloat(16);
	/**
	 * The inition pos.
	 */
	protected Vector3f initionPos;
	/**
	 * The material.
	 */
	protected Material material;
	/**
	 * The animation.
	 */
	protected Animation animation = new Animation();
	/**
	 * The animations.
	 */
	protected List<Animation> animations = new ArrayList<>();
	/**
	 * The quat rot.
	 */
	protected Vector4f quatRot;
	/**
	 * The transform.
	 */
	private Transform transform;
	/**
	 * The visibility.
	 */
	private int visibility = VISIBILITY_VISIBLE;
	/**
	 * The animated.
	 */
	private boolean animated = false;
	private boolean loaded;
	/**
	 * The description.
	 */
	private String name = "default";
	/**
	 * The childs.
	 */
	protected final List<AbstractSceneNode> childs = new ObjectArrayList<AbstractSceneNode>();
	/**
	 * The animation rot.
	 */
	private Vector4f animationRot;
	/**
	 * The initial quad rot.
	 */
	private Quat4f initialQuadRot;
	/**
	 * The initial scale.
	 */
	private Vector3f initialScale;
	/**
	 * The bounding box.
	 */
	private BoundingBox boundingBox;
	/**
	 * The bounding sphere radius.
	 */
	private float boundingSphereRadius;
	/**
	 * The scale.
	 */
	private Vector3f scale = new Vector3f(1, 1, 1);
	private AbstractSceneNode parent;
	private boolean flipCulling;
	private static final String defaultName = "UnnamedSceneNode";
	/**
	 * Instantiates a new form.
	 */
	public AbstractSceneNode() {
		this.name = defaultName;
		transform = new Transform();
		transform.setIdentity();

		initionPos = new Vector3f();
		quatRot = new Vector4f(0, 0, 0, 1);
		animationRot = new Vector4f();
		initialQuadRot = new Quat4f(0, 0, 0, 1);
		initialScale = new Vector3f(1, 1, 1);
	}

	/**
	 * Checks if is mirror mode.
	 *
	 * @return true, if is mirror mode
	 */
	public static boolean isMirrorMode() {
		return mirrorMode;
	}

	/**
	 * Sets the mirror mode.
	 *
	 * @param mirrorMode the new mirror mode
	 */
	public static void setMirrorMode(boolean mirrorMode) {
		AbstractSceneNode.mirrorMode = mirrorMode;
	}

	/**
	 * transforms the view Matrix to the pos rot and scale of the current AbstractSceneNode.
	 *
	 * @param gl  the current graphicsLibrary Session
	 * @param glu the currently used Graphics Library Utils
	 * @param f   the f
	 */
	public static void transform(AbstractSceneNode f) {

		// GL11.glScalef(scale.x, scale.y, scale.z); //scale (working without
		// scale change)
		if (!(f.initionPos.x == 0 && f.initionPos.y == 0 && f.initionPos.z == 0)) {
			GlUtil.translateModelview(f.initionPos.x, f.initionPos.y, f.initionPos.z); // move
		}

		GlUtil.glMultMatrix(f.transform);

		// normal rotation
		if (!(f.quatRot.x == 0 && f.quatRot.y == 0 && f.quatRot.z == 0 && f.quatRot.w == 1)) {

			f.animationRot.set(f.quatRot);
			Vector3fTools.quaternionToDegree(f.animationRot);
			// System.err.println("Anim Rot "+description+": "+animationRot);
			GL11.glRotatef((float) Math.toDegrees(f.animationRot.w), f.animationRot.x,
					f.animationRot.y, f.animationRot.z);
		}
		if (!(f.scale.x == 1 && f.scale.y == 1 && f.scale.z == 1)) {
			GL11.glScalef(f.scale.x, f.scale.y, f.scale.z);
		}

	}

	public static void transformPlane(AbstractSceneNode f) {
		GlUtil.glMultMatrix(f.transform);
	}

	/**
	 * Activate culling.
	 *
	 * @param gl the gl
	 */
	public void activateCulling() {
		if (EngineSettings.G_CULLING_ACTIVE.isOn()) {
			//			System.err.println("culling "+this);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			if ((AbstractSceneNode.isMirrorMode() && !Water.drawingRefraction) || flipCulling) {
				GL11.glCullFace(GL11.GL_FRONT);
			} else {
				GL11.glCullFace(GL11.GL_BACK);
			}
			//			GL11.glFrontFace(GL11.GL_CW);
		}
	}

	/**
	 * Attach.
	 *
	 * @param f the f
	 * @return true, if successful
	 */
	public boolean attach(AbstractSceneNode f) {
		f.setParent(this);
		return childs.add(f);
	}

	@Override
	public abstract AbstractSceneNode clone();

	//	/* (non-Javadoc)
	//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	//	 */
	//	@Override
	//	public int compareTo(ZSortedDrawable o) {
	//		Vector3f oPos = o.getBufferedTransformationPosition();
	//		Vector3f tPos = this.getBufferedTransformationPosition();
	//		if(oPos == null){
	//			//			throw new NullPointerException("Cannot compare "+this+" with "+o+" (o Pos null)");
	//			return Integer.MAX_VALUE;
	//		}
	//		oPos.sub( Controller.getCamera().getPos());
	//		tPos.sub(Controller.getCamera().getPos());
	//		int round = FastMath.round(oPos.length()-tPos.length());
	//		return  round;
	//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name.equals(defaultName) ? super.toString() : name;
	}

	/**
	 * Deactivate culling.
	 *
	 * @param gl the gl
	 */
	public void deactivateCulling() {
		GlUtil.glDisable(GL11.GL_CULL_FACE);
	}

	//	@Override
	//	public void drawZSorted()  {
	//		getBufferedTransformation().rewind();
	//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, getBufferedTransformation());
	//		AbstractScene.zSortedMap.add(this);
	//	}

	/**
	 * Detach.
	 *
	 * @param f the f
	 * @return true, if successful
	 */
	public boolean detach(AbstractSceneNode f) {
		if (f != null) {
			f.setParent(null);
			return childs.remove(f);
		}
		return false;
	}

	/**
	 * Gets the animation.
	 *
	 * @return the animation
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Sets the animation.
	 *
	 * @param animation the new animation
	 */
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	/**
	 * Gets the animation.
	 *
	 * @param description the description
	 * @return the animation
	 */
	public Animation getAnimation(String name) {
		Animation a = null;
		if (animation != null && name.equals(animation.getName())) {
			a = animation;
			return a;
		} else {
			for (AbstractSceneNode f : childs) {
				a = f.getAnimation(name);
				if (a != null && a.getName().equals(name)) {
					// System.err.println(" returned animation of "+f.name+
					// " from search "+description+" in "+this.name);
					return a;
				}
			}
		}
		if (a == null) {
			System.err.println(" returned null from search " + name + " in "
					+ this.name);
		}
		return a;
	}

	/**
	 * Gets the animations.
	 *
	 * @return the animations
	 */
	public List<Animation> getAnimations() {
		return animations;
	}

	/**
	 * Sets the animations.
	 *
	 * @param animations the new animations
	 */
	public void setAnimations(List<Animation> animations) {
		this.animations = animations;
	}

	//	/* (non-Javadoc)
	//	 * @see org.schema.schine.graphicsengine.core.ZSortedDrawable#getBufferedTransformation()
	//	 */
	//	@Override
	//	public FloatBuffer getBufferedTransformation() {
	//		return null;
	//	}
	//
	//	@Override
	//	public Vector3f getBufferedTransformationPosition(){
	//		//		Vector3f pos = new Vector3f(bufferedTransformation.get(12),bufferedTransformation.get(13),bufferedTransformation.get(14));
	//		return null;
	//	}

	protected FloatBuffer getBillboardSphericalBeginMatrix() {

		int i, j;

		tempModelviewBuffer.rewind();
		Matrix4fTools.store(Controller.modelviewMatrix, tempModelviewBuffer);

		// undo all rotations
		// beware all scaling is lost as well
		//sets the rotation matrix to identity
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 3; j++) {
				if (i == j) {
					tempModelviewBuffer.put(i * 4 + j, 1.0f);
				} else {
					tempModelviewBuffer.put(i * 4 + j, 0.0f);
				}
			}
		}
		//		tempModelviewBuffer.rewind();
		//		Matrix4f t = new Matrix4f();
		//		t.load(tempModelviewBuffer);
		//		System.err.println("TTTT "+t);
		tempModelviewBuffer.rewind();
		// set the modelview with no rotations
		return tempModelviewBuffer;

	}

	/**
	 * Gets the bounding box.
	 *
	 * @return the bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Sets the bounding box.
	 *
	 * @param boundingBox the new bounding box
	 */
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	/**
	 * Gets the bounding sphere radius.
	 *
	 * @return the boundingSphereRadius
	 */
	public float getBoundingSphereRadius() {
		return boundingSphereRadius;
	}

	/**
	 * Sets the bounding sphere radius.
	 *
	 * @param boundingSphereRadius the boundingSphereRadius to set
	 */
	public void setBoundingSphereRadius(float boundingSphereRadius) {
		this.boundingSphereRadius = boundingSphereRadius;
	}

	/**
	 * Gets the childs.
	 *
	 * @return the childs
	 */
	public List<AbstractSceneNode> getChilds() {
		return childs;
	}


	/**
	 * Gets the initial quad rot.
	 *
	 * @return the initial quad rot
	 */
	public Quat4f getInitialQuadRot() {
		return initialQuadRot;
	}

	/**
	 * Sets the initial quad rot.
	 *
	 * @param initialQuadRot the new initial quad rot
	 */
	public void setInitialQuadRot(Vector4f initialQuadRot) {
		this.initialQuadRot = new Quat4f(initialQuadRot.x, initialQuadRot.y, initialQuadRot.z, initialQuadRot.w);
	}

	/**
	 * Gets the initial scale.
	 *
	 * @return the initial scale
	 */
	public Vector3f getInitialScale() {
		return initialScale;
	}

	/**
	 * Sets the initial scale.
	 *
	 * @param initialScale the new initial scale
	 */
	public void setInitialScale(Vector3f initialScale) {
		this.initialScale = initialScale;
	}

	/**
	 * Gets the inition pos.
	 *
	 * @return the inition pos
	 */
	public Vector3f getInitionPos() {
		return initionPos;
	}

	/**
	 * Sets the inition pos.
	 *
	 * @param initionPos the new inition pos
	 */
	public void setInitionPos(Vector3f initionPos) {
		this.initionPos = initionPos;
	}

	/**
	 * returns the Material Class set for this AbstractSceneNode.
	 *
	 * @return the material
	 */

	public Material getMaterial() {
		return material;
	}

	/**
	 * sets the Material of this AbstractSceneNode ->Material.
	 *
	 * @param material the new material
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	@SuppressWarnings("unchecked")
	public <U extends AbstractSceneNode> U getParent() {
		return (U) parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public <U extends AbstractSceneNode> void setParent(U parent) {
		this.parent = parent;
	}

	//	public boolean equals(Object o){
	//		if(o != null &&  o instanceof AbstractSceneNode){
	//			return o.hashCode() == this.hashCode();
	//		}
	//		return false;
	//	}

	/**
	 * Gets the pos.
	 *
	 * @return the absolute position of this AbstractSceneNode
	 */
	@Override
	public Vector3f getPos() {
		return transform.origin;
	}

	/**
	 * Sets the pos.
	 *
	 * @param pos the new pos
	 */
	public void setPos(Vector3f pos) {
		this.transform.origin.set(pos);

	}

	/**
	 * Gets the rot4.
	 *
	 * @return the rot4
	 */
	public Vector4f getRot4() {
		return quatRot;
	}

	/**
	 * Gets the scale.
	 *
	 * @return the absolute scale of this AbstractSceneNode
	 */
	@Override
	public Vector3f getScale() {
		return scale;
	}

	/**
	 * sets the absolute scale of this AbstractSceneNode.
	 *
	 * @param scale the new scale
	 */
	@Override
	public void setScale(Vector3f scale) {
		this.scale.set(scale);

	}

	/**
	 * Gets the transform.
	 *
	 * @return the transform
	 */
	public Transform getTransform() {
		return transform;
	}

	/**
	 * Sets the transform.
	 *
	 * @param transform the new transform
	 */
	public void setTransform(Transform transform) {
		this.transform = transform;
	}

	/**
	 * Gets the tree string.
	 *
	 * @return the tree string
	 */
	public String getTreeString() {
		StringBuffer tree = new StringBuffer();
		tree.append("[AbstractSceneNode] Scene of " + name + "\n");
		printChilds(tree, 0);
		tree.append("[AbstractSceneNode] end of " + name + "\n");
		return tree.toString();
	}

	/**
	 * Gets the visibility.
	 *
	 * @return the visibility
	 */
	public int getVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility.
	 *
	 * @param visibility the visibility to set
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public Vector3f getWorldTranslation() {
		Vector3f pos = new Vector3f(getPos());
		if (getParent() != null) {
			pos.add(getParent().getWorldTranslation());
		}
		return pos;
	}

	/**
	 * Checks if is animated.
	 *
	 * @return true, if is animated
	 */
	public boolean isAnimated() {
		return animated;
	}

	/**
	 * Sets the animated.
	 *
	 * @param animated the new animated
	 */
	public void setAnimated(boolean animated) {
		this.animated = animated;
	}

	@Override
	public boolean isInvisible() {
		return visibility != VISIBILITY_VISIBLE;
	}
	public void translate(){
		GlUtil.translateModelview(getPos());
	}
	public void translateBack(){
		GlUtil.translateModelview(-getPos().x, -getPos().y, -getPos().z);
	}
	/**
	 * @return the loaded
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @param loaded the loaded to set
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	@Deprecated
	public boolean isVisibleInFrustum() {

		BoundingBox bb = boundingBox;
		if (bb == null) {
			return false;
		}
		Vector3f wt = getWorldTranslation();
		float width = Math.abs(bb.max.x - bb.min.x);
		float height = Math.abs(bb.max.y - bb.min.y);
		float depth = Math.abs(bb.max.z - bb.min.z);
		return true;
	}

	/**
	 * Make biggest bounding box.
	 *
	 * @param a the min
	 * @param b the max
	 * @return the bounding box
	 */
	public BoundingBox makeBiggestBoundingBox(Vector3f startBoundMin, Vector3f startBoundMax) {
		BoundingBox bb = new BoundingBox(startBoundMin, startBoundMax);
		for (AbstractSceneNode f : childs) {
			bb = f.makeBiggestBoundingBox(startBoundMin, startBoundMax);
			// System.err.println("checking " + f.name + " - " + bb +" " +
			// f.getBoundingBox());

		}
		if (boundingBox != null) {

			if (bb.max.x < boundingBox.max.x) {
				bb.max.x = (boundingBox.max.x);
			}
			if (bb.max.y < boundingBox.max.y) {
				bb.max.y = (boundingBox.max.y);
			}
			if (bb.max.z < boundingBox.max.z) {
				bb.max.z = (boundingBox.max.z);
			}

			if (bb.min.x > boundingBox.min.x) {
				bb.min.x = (boundingBox.min.x);
			}
			if (bb.min.y > boundingBox.min.y) {
				bb.min.y = (boundingBox.min.y);
			}
			if (bb.min.z > boundingBox.min.z) {
				bb.min.z = (boundingBox.min.z);
			}
			// System.err.println("bb now "+bb);
		}
		return bb;

	}

	/**
	 * Make bounding sphere.
	 */
	public void makeBoundingSphere() {
		this.boundingSphereRadius = 0;

		float len = boundingBox.max.length();
		if (len > boundingSphereRadius) {
			boundingSphereRadius = len;
		}
		len = boundingBox.min.length();
		if (len > boundingSphereRadius) {
			boundingSphereRadius = len;
		}
	}

	/**
	 * Move.
	 *
	 * @param arg0 the arg0
	 */
	public void move(KeyEvent arg0) {

	}

	/**
	 * Prints the childs.
	 *
	 * @param tree the tree
	 * @param lvl  the lvl
	 */
	private void printChilds(StringBuffer tree, int lvl) {
		for (int i = 0; i < lvl; i++) {
			tree.append("--");
		}

		tree.append("- " + (name) + "\n");
		for (AbstractSceneNode f : childs) {
			f.printChilds(tree, lvl + 1);
		}
	}
	protected static final Matrix3f rotTmp = new Matrix3f();
	/**
	 * Rotate by.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void rotateBy(float x, float y, float z) {
		
		
		if(x != 0){
			rotTmp.setIdentity();
			rotTmp.rotX((float) Math.toRadians(x));
			transform.basis.mul(rotTmp);
		}
		
		if(y != 0){
			rotTmp.setIdentity();
			rotTmp.rotY((float) Math.toRadians(y));
			transform.basis.mul(rotTmp);
		}
		
		if(z != 0){
			rotTmp.setIdentity();
			rotTmp.rotZ((float) Math.toRadians(z));
			transform.basis.mul(rotTmp);
		}
		
		
		
	    /*
         * set base rotation to the objects ground rotation (coming from
		 * the model itself)
		 */
		
		

	}

	/**
	 * Select animation.
	 *
	 * @param description the description
	 * @throws AnimationNotFoundException
	 */
	public void selectAnimation(String name) throws AnimationNotFoundException {
		if (animations == null || animation == null) {
			throw new AnimationNotFoundException("Animation " + animation + " not found in " + animations);
		}
		if (!name.equals(animation.getName())) {
			for (Animation a : animations) {
				if (a.getName().equals(name)) {
					//System.err.println(" animation change of "+this.name+" to "
					// +description);
					animation = a;
					return;
				}
			}
		}
		for (AbstractSceneNode f : childs) {
			f.selectAnimation(name);
		}

	}

	/**
	 * @param bufferedTransformation the bufferedTransformation to set
	 */
	public void setBufferedTransformation(FloatBuffer bufferedTransformation) {
	}

	/**
	 * set the absolute Position of the AbstractSceneNode.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setPos(float x, float y, float z) {
		transform.origin.set(x, y, z);
	}
	public void setPos(float x, float y) {
		transform.origin.x = x;
		transform.origin.y = y;
	}
	/**
	 * Sets the quat rot.
	 *
	 * @param rot the new quat rot
	 */
	public void setQuatRot(Vector4f rot) {
		this.quatRot = rot;
	}

	/**
	 * Sets the rot.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setRot(float x, float y, float z) {
		transform.basis.setIdentity();
		rotateBy(x, y, z);

	}

	/**
	 * sets The absolute Rotation of this AbstractSceneNode.
	 *
	 * @param rot the new rot
	 */
	public void setRot(Vector3f rot) {
		setRot(rot.x, rot.y, rot.z);
	}

	/**
	 * Sets the scale.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setScale(float x, float y, float z) {
		scale.set(x, y, z);
		//		if(this instanceof GUIOverlay){
		//			System.err.println(this.scale);
		//		}
	}

	/**
	 * transforms the view Matrix to the pos rot and scale of the current AbstractSceneNode.
	 *
	 * @param gl  the current graphicsLibrary Session
	 * @param glu the currently used Graphics Library Utils
	 */
	public void transform() {
		transform(this);

	}

	/**
	 * updates the variables of this form.
	 *
	 * @param frameTime the frame time
	 * @
	 */
	public void update(Timer timer) {

		for (AbstractSceneNode f : childs) {
			f.update(timer);
		}

	}

	/**
	 * @return the flipCulling
	 */
	public boolean isFlipCulling() {
		return flipCulling;
	}

	/**
	 * @param flipCulling the flipCulling to set
	 */
	public void setFlipCulling(boolean flipCulling) {
		this.flipCulling = flipCulling;
	}

}
