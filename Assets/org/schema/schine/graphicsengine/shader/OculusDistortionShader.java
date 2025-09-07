package org.schema.schine.graphicsengine.shader;

import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;

public class OculusDistortionShader implements Shaderable {

	private int ocState;
	private int width;
	private int height;

	public OculusDistortionShader() {

	}

	public void setFBO(int ocState) {
		this.ocState = ocState;
	}

	@Override
	public void onExit() {
		
	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		//texture loaded in fbo
		GlUtil.updateShaderInt(shader, "texSceneFinal", 0);

		assert (ocState != 0);

		width = GLFrame.getWidth();
		height = GLFrame.getHeight();

		width *= OculusVrHelper.getScaleFactor();
		height *= OculusVrHelper.getScaleFactor();

		if (ocState == OculusVrHelper.OCCULUS_LEFT) {
			updateOcShaderParametersForVP(shader, 0, 0, width / 2, height, ocState);
		} else {
			updateOcShaderParametersForVP(shader, width / 2, 0, width / 2, height, ocState);
		}
	}

	private void updateOcShaderParametersForVP(Shader shader, int vpX, int vpY, int vpWidth, int vpHeight, int ocState) {
		float w = (float) vpWidth / (float) width;
		float h = (float) vpHeight / (float) height;
		float x = (float) vpX / (float) width;
		float y = (float) vpY / (float) height;

		float as = (float) vpWidth / vpHeight;

		GlUtil.updateShaderVector4f(shader, "ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1],
				OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3]);

		float ocLensCenter = (ocState == OculusVrHelper.OCCULUS_RIGHT) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

		GlUtil.updateShaderVector2f(shader, "ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f);
		GlUtil.updateShaderVector2f(shader, "ocScreenCenter", x + w * 0.5f, y + h * 0.5f);

		float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

		GlUtil.updateShaderVector2f(shader, "ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as);
		GlUtil.updateShaderVector2f(shader, "ocScaleIn", (2 / w), (2 / h) / as);
	}

	public Shader getShader() {
		return ShaderLibrary.ocDistortion;
	}

}
