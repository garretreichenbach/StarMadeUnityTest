package org.schema.schine.graphicsengine.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GLU;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.input.Mouse;

public class WorldToScreenConverter {

	protected Vector3f lastDir = new Vector3f();
	protected Vector3f middle = new Vector3f();
	FloatBuffer fBuffer = MemoryUtil.memAllocFloat(3);
	FloatBuffer modelBuffer = MemoryUtil.memAllocFloat(16);
	FloatBuffer projBuffer = MemoryUtil.memAllocFloat(16);
	IntBuffer screenBuffer = MemoryUtil.memAllocInt(16);
	Vector3f toVec = new Vector3f();
	Vector3f dir = new Vector3f();
	private IntBuffer viewportTemp = MemoryUtil.memAllocInt(16);
	private FloatBuffer projectionTemp = MemoryUtil.memAllocFloat(16);
	private FloatBuffer modelviewTemp = MemoryUtil.memAllocFloat(16);
	private FloatBuffer coord = MemoryUtil.memAllocFloat(3);

	public Vector3f convert(Vector3f worldPos, Vector3f outPosOnScreen, boolean overScaleOutsideWindow) {
		return convert(worldPos, outPosOnScreen, overScaleOutsideWindow, Controller.getCamera());
	}

	public Vector3f convert(Vector3f worldPos, Vector3f outPosOnScreen, boolean overScaleOutsideWindow, Camera camera) {
		return convert(worldPos, outPosOnScreen, camera.getPos(), camera.getForward(), overScaleOutsideWindow);
	}

	public Vector3f convert(Vector3f worldPos, Vector3f outPosOnScreen, Vector3f cameraPos, Vector3f cameraForward, boolean overScaleOutsideWindow) {
		Vector3f forward = cameraForward;
		toVec.set(worldPos);
		dir.sub(worldPos, cameraPos);
		float camToPosLen = dir.length();

		dir.normalize();

		fBuffer.rewind();

		GLU.gluProject(toVec.x,
				toVec.y,
				toVec.z,
				modelBuffer,
				projBuffer,
				screenBuffer,
				fBuffer);

		toVec.set(fBuffer.get(0), fBuffer.get(1), fBuffer.get(2));
		float dot = forward.dot(dir);

		getMiddleOfScreen(middle);
		outPosOnScreen.set(toVec.x, GLFrame.getHeight() - toVec.y, 0);

		if (dot < 0) {
			Vector3f dirToMiddle = new Vector3f();
			dirToMiddle.sub(middle, outPosOnScreen);
			if (dirToMiddle.length() == 0) {
				//exactly on the backside in the middle of the screen
				//maybehandle by using last dir
				dirToMiddle.set(lastDir);
			}
			dirToMiddle.normalize();
			lastDir.set(dirToMiddle);
			if (overScaleOutsideWindow) {
				dirToMiddle.scale(10000000);
			}
			//throw arrow off screen in the opposite direction that the back side projection is
			outPosOnScreen.add(dirToMiddle);

		}
		return outPosOnScreen;
	}

	public Vector3f getMiddleOfScreen(Vector3f out) {
		out.set(GLFrame.getWidth() / 2, GLFrame.getHeight() / 2, 0);
		return out;
	}

	public boolean getMousePosition(Sprite sprite, Vector3f pos, Vector3f scale) {
		float winX, winY, winZ;

		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		//		float scaleX = new Vector3f(modelviewMatrix.m00,modelviewMatrix.m01,modelviewMatrix.m02).length();
		//		float scaleY = new Vector3f(modelviewMatrix.m10,modelviewMatrix.m11,modelviewMatrix.m12).length();
		float scaleX = scale.x;
		float scaleY = scale.y;

		winX = Mouse.getX() * scaleX;
		winY = Mouse.getY() * scaleY;
		winZ = 0;
		Matrix4f projectionMatrix = Controller.projectionMatrix;
		viewportTemp.rewind();
		viewportTemp.put(Controller.viewport);
		Controller.viewport.rewind();
		viewportTemp.rewind();

		viewportTemp.put(0, 0);
		viewportTemp.put(1, 0);
		viewportTemp.put(2, (int) (viewportTemp.get(2) * scaleX));
		viewportTemp.put(3, (int) (viewportTemp.get(3) * scaleY));

		modelviewTemp.rewind();
		Matrix4fTools.store(Controller.modelviewMatrix, modelviewTemp);
		modelviewTemp.rewind();

		Matrix4f m = new Matrix4f();
		m.setIdentity();
		//		m.scale(new org.lwjgl.util.vector.Vector3f(0.01f,-0.01f,0.01f));
		//		m.store(modelviewTemp);
		modelviewTemp.rewind();
		modelviewTemp.put(12, Controller.modelviewMatrix.m30 - pos.x);
		modelviewTemp.put(13, Controller.modelviewMatrix.m31 - pos.y);
		modelviewTemp.put(14, Controller.modelviewMatrix.m32 - pos.z);
		modelviewTemp.rewind();

		projectionTemp.rewind();
		Matrix4fTools.store(projectionMatrix, projectionTemp);
		projectionTemp.rewind();
	    /*
         * this method uses the build in opengl unproject
		 *
		 * because this function was designed for
		 * use in 3d, using it for a known
		 * projection matrix in 2D
		 * produces unecessary calculations
		 */
		GLU.gluUnProject(winX, winY, winZ, modelviewTemp, projectionTemp,
				viewportTemp, coord);

		float relX = (coord.get(0));
		float relY = (coord.get(1));
		float relZ = (coord.get(2));
		boolean xIn = relX < sprite.getWidth() * scaleX * scaleX
				&& relX > 0;
		boolean yIn = relY < sprite.getHeight() * scaleY * scaleY
				&& relY > 0;
		System.err.println("REL -- " + relX + "; " + relY + "; " + relZ);
		return xIn && yIn;
	}

	public void storeCurrentModelviewProjection() {
		modelBuffer.rewind();
		projBuffer.rewind();
		screenBuffer.rewind();
		Matrix4fTools.store(Controller.modelviewMatrix, modelBuffer);
		Matrix4fTools.store(Controller.projectionMatrix, projBuffer);

		screenBuffer.put(0);
		screenBuffer.put(0);
		screenBuffer.put(GLFrame.getWidth() - 5);
		screenBuffer.put(GLFrame.getHeight() - 5);
		screenBuffer.rewind();
		modelBuffer.rewind();
		projBuffer.rewind();
	}

}
