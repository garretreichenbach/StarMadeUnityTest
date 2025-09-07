package org.schema.schine.graphicsengine.shader;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;

public class SilhouetteShaderAlpha implements Shaderable {

	public Vector4f color = new Vector4f(0, 0, 0, 1);


	@Override
	public void onExit() {
	}

	@Override
	public void updateShader(DrawableScene scene) {
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.updateShaderVector4f(shader, "silhouetteColor", color);
	}

}
