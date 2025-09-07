package org.schema.schine.graphicsengine.util;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.GlUtil;

import com.bulletphysics.linearmath.Transform;

public class WorldToScreenConverterFixedAspect extends WorldToScreenConverter {

	public boolean overflowX;
	float[] sBufferModel = new float[16];

	@Override
	public Vector3f convert(Vector3f worldPos, Vector3f outPosOnScreen, Vector3f cameraPos, Vector3f cameraForward, boolean overScaleOutsideWindow) {
		Vector3f forward = new Vector3f(cameraForward);

		forward.normalize();
		toVec.set(worldPos);

		dir.sub(worldPos, cameraPos);

		dir.normalize();

		fBuffer.rewind();

		GlUtil.gluProject(worldPos.x,
				worldPos.y,
				worldPos.z,
				modelBuffer,
				projBuffer,
				screenBuffer,
				fBuffer);
		//		GLU.gluProject(	toVec.x	,
		//				toVec.y,
		//				toVec.z,
		//			 	modelBuffer,
		//			 	projBuffer,
		//			 	screenBuffer,
		//			 	fBuffer);

		toVec.set(fBuffer.get(0), fBuffer.get(1), fBuffer.get(2));
		float dot = forward.dot(dir);

		getMiddleOfScreen(middle);
		outPosOnScreen.set(toVec.x, 600 - toVec.y, 0);
		overflowX = false;
		if (dot < 0) {
			Vector3f dirToMiddle = new Vector3f();
			dirToMiddle.sub(middle, outPosOnScreen);
			if (dirToMiddle.length() == 0) {
				//exactly on the backside in the middle of the screen
				//maybehandle by using last dir
				dirToMiddle.set(lastDir);
			}
			overflowX = true;
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

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.util.WorldToScreenConverter#getMiddleOfScreen(javax.vecmath.Vector3f)
	 */
	@Override
	public Vector3f getMiddleOfScreen(Vector3f out) {
		out.set(400, 300, 0);
		return out;
	}

	public Vector3f convert(Vector3f worldPos, Vector3f outPosOnScreen, Vector3f cameraPos, Vector3f cameraForward, boolean overScaleOutsideWindow, Matrix4f proj,
	                        Transform worldTransform) {
		modelBuffer.rewind();
		projBuffer.rewind();
		screenBuffer.rewind();

		float[] coefficients = Matrix4fTools.getCoefficients(worldTransform.getMatrix(new javax.vecmath.Matrix4f()));
		//		worldTransform.getOpenGLMatrix(sBufferModel);
		//		float[] coefficients = sBufferModel;
		Matrix4fTools.store(proj, projBuffer);

		modelBuffer.put(coefficients);

		//		float[] p = new float[16];
		//		projBuffer.rewind();
		//		projBuffer.get(p);
		//
		//		System.err.println("PROJ MATRIX\n"+Arrays.toString(p));
		//		modelBuffer.rewind();
		//		modelBuffer.get(p);
		//		System.err.println("MODEL MATRIX\n"+Arrays.toString(p));

		screenBuffer.put(0);
		screenBuffer.put(0);
		screenBuffer.put(800 - 5);
		screenBuffer.put(600 - 5);
		screenBuffer.rewind();
		modelBuffer.rewind();
		projBuffer.rewind();

		return convert(worldPos, outPosOnScreen, cameraPos, cameraForward, overScaleOutsideWindow);
	}

}
