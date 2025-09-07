package org.schema.game.client.view.shader;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

import com.bulletphysics.linearmath.Transform;

public class ShadowShader implements Shaderable {
	private static float[] floatArrayBufferOrto = new float[16];
	private static float[] floatArrayBuffer = new float[16];
	private static FloatBuffer buff = MemoryUtil.memAllocFloat(16);
	//	private static Matrix4f biasMatrix = new Matrix4f(
	//			0.5f, 0.0f, 0.0f, 0.0f,
	//			0.0f, 0.5f, 0.0f, 0.0f,
	//			0.0f, 0.0f, 0.5f, 0.0f,
	//			0.5f, 0.5f, 0.5f, 1.0f
	//			);
	private static Matrix4f biasMatrix = new Matrix4f(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f
	);

	public static void putDepthMatrix(Shader shader, boolean bias) {
		Vector3f lightInvDir = new Vector3f(
				AbstractScene.mainLight.getPos());
		lightInvDir.normalize();
		//		lightInvDir.negate();
		lightInvDir.scale(100);

		GlUtil.glOrtho(-30, 30, -30, 30, -100, 100, floatArrayBufferOrto);
		//		org.lwjgl.util.vector.Matrix4f proj = new org.lwjgl.util.vector.Matrix4f();
		//		proj.setIdentity();
		//		GlUtil.gluPerspective(proj, EngineSettings.G_FOV.getFloat(),
		//				(float)GLFrame.getWidth() / (float)GLFrame.getHeight(), 0.1f, 400, false);
		//
		//		buff.rewind();
		//		proj.store(buff);
		//		buff.rewind();
		//		for(int i = 0; i < 16; i++){
		//			floatArrayBufferOrto[i] = buff.get(i);
		//		}

		Vector3f mL = new Vector3f(AbstractScene.mainLight.getPos());
		//		mL.normalize();
		//		mL.scale(500);
		mL.set(0, 0, 0);

		Matrix4f inv = new Matrix4f(Controller.modelviewMatrix);
		inv.invert();

		Vector3f forward = new Vector3f(AbstractScene.mainLight.getPos());
		forward.negate();
		forward.normalize();
		Vector3f up = new Vector3f(0, 1, 0);

		Vector3f rightTmp = new Vector3f();
		Vector3f forwardTmp = new Vector3f();
		Transform current = new Transform();

		rightTmp.cross(up, forward);
		rightTmp.normalize();
		rightTmp.negate();

		forwardTmp.set(forward);
		forwardTmp.negate();

		up.cross(rightTmp, forward);
		up.normalize();

		current.basis.setRow(0, rightTmp);
		current.basis.setRow(1, up);
		current.basis.setRow(2, forwardTmp);

		current.origin.set(lightInvDir);
		Vector3f o = new Vector3f(Controller.getCamera().getWorldTransform().origin);
		o.negate();
		//		current.origin.add(o);
		current.basis.transform(current.origin);

		//		Transform lookAtWithoutLoad = new Transform(Controller.getCamera().lookAt(false));
		Transform lookAtWithoutLoad = GlUtil.lookAtWithoutLoad(
				lightInvDir.x, lightInvDir.y, lightInvDir.z, mL.x, mL.y, mL.z, 0, 1,
				0);

		//		Transform lookAtWithoutLoad = current;

		// Compute the MVP matrix from the light's point of view
		Matrix4f depthMVP = new Matrix4f();
		Matrix4f depthProjectionMatrix = new Matrix4f();
		Matrix4f depthViewMatrix = new Matrix4f();
		Matrix4f depthModelMatrix = new Matrix4f();
		depthModelMatrix.setIdentity();

		lookAtWithoutLoad.getMatrix(depthViewMatrix);

		Transform tOrto = new Transform();

		tOrto.setFromOpenGLMatrix(floatArrayBufferOrto);
		tOrto.getMatrix(depthProjectionMatrix);

		depthMVP.set(depthProjectionMatrix);
		depthMVP.mul(depthViewMatrix);
		depthMVP.mul(depthModelMatrix);

		
		//		if(bias){
		//			Matrix4f m = new Matrix4f(biasMatrix);
		//			m.mul(depthMVP);
		//			depthMVP.set(m);
		//		}
		//
		Transform t = new Transform(depthMVP);

		t.getOpenGLMatrix(floatArrayBuffer);
		buff.rewind();
		buff.put(floatArrayBuffer);
		buff.rewind();
		// glm::mat4 depthMVP = depthProjectionMatrix * depthViewMatrix * depthModelMatrix;

		GlUtil.updateShaderMat4(shader, "depthMVP", buff, false);

		buff.rewind();
		Matrix4fTools.store(inv, buff);
		buff.rewind();
		GlUtil.updateShaderMat4(shader, "invModel", buff, false);
	}

	@Override
	public void onExit() {
		
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		putDepthMatrix(shader, false);

		if (shader.recompiled) {

			GlUtil.updateShaderCubeNormalsBiNormalsAndTangentsBoolean(shader);
			
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(6 * 3 * 4, 0).asFloatBuffer();

			fb.rewind();

			fb.put(2);
			fb.put(4);
			fb.put(0);

			fb.put(2);
			fb.put(4);
			fb.put(0);

			fb.put(4);
			fb.put(0);
			fb.put(2);

			fb.put(4);
			fb.put(0);
			fb.put(2);

			fb.put(0);
			fb.put(4);
			fb.put(2);

			fb.put(0);
			fb.put(4);
			fb.put(2);

			fb.rewind();
			GlUtil.updateShaderFloats3(shader, "quadPosMark", fb);

			shader.recompiled = false;
		}
		GlUtil.updateShaderFloat(shader, "lodThreshold", EngineSettings.LOD_DISTANCE_IN_THRESHOLD.getFloat());
	}
	
}
