package org.schema.game.client.view.gui;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

import com.bulletphysics.linearmath.Transform;

public abstract class OrientationElement implements Drawable {

	protected GameClientState state;


	private Matrix3f rot = new Matrix3f();
	private Transform mView = new Transform();
	private FloatBuffer fb = MemoryUtil.memAllocFloat(16);
	private float[] ff = new float[16];
	private boolean init;
	private Mesh mesh;

	public OrientationElement(GameClientState state) {
		this.state = state;
	}

	@Override
	public void cleanUp() {
		
	}

	@Override
	public void draw() {
		if (!init) {
			onInit();
		}
		try {
			if (canDraw()) {

				/*
				 * by multiplying the rotation matrix of the segmentController (ship),
				 * and also the current modelview rotation matrix (camera rotation)
				 * onto the identity of a fresh modelview matrix
				 * the arrow points always in direction of the forward of the
				 * segment controller
				 */
				Matrix4f modelviewMatrix = Controller.modelviewMatrix;
				fb.rewind();
				Matrix4fTools.store(modelviewMatrix, fb);
				fb.rewind();
				fb.get(ff);
				mView.setFromOpenGLMatrix(ff);
				mView.origin.set(0, 0, 0);

				GUIElement.enableOrthogonal3d();
				GlUtil.glPushMatrix();

				//draw on top of screen
				GlUtil.translateModelview(GLFrame.getWidth() / 2, 105f, 0);

				//y axis has to be flipped (cause: orthographic projection used)
				GlUtil.scaleModelview(10f, -10f, 10f);

				rot.set(getWorldTransform().basis);
				mView.basis.mul(rot);

				GlUtil.glMultMatrix(mView);

				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glDisable(GL11.GL_DEPTH_TEST);
				GlUtil.glEnable(GL11.GL_NORMALIZE);
				GlUtil.glDisable(GL11.GL_CULL_FACE);
				GlUtil.glEnable(GL11.GL_DEPTH_TEST);
				//			mesh.draw();
				mesh.getMaterial().attach(0);
				//			GlUtil.glDisable(GL11.GL_LIGHTING);

				mesh.drawVBO();

				mesh.getMaterial().detach();
				GlUtil.glPopMatrix();
				GUIElement.disableOrthogonal();
				GlUtil.glEnable(GL11.GL_LIGHTING);
				GlUtil.glDisable(GL11.GL_NORMALIZE);
				GlUtil.glEnable(GL11.GL_DEPTH_TEST);
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
		} catch (NullPointerException e) {
			System.err.println("EXCPETION HAS BEEN HANDLED");
			e.printStackTrace();
		}
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		mesh = (Mesh) Controller.getResLoader().getMesh("Arrow").getChilds().get(0);
		init = true;
	}

	public abstract Transform getWorldTransform();

	public abstract boolean canDraw();

	public Ship getShip() {
		return state.getShip();
	}

}
