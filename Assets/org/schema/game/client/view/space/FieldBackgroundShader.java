package org.schema.game.client.view.space;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class FieldBackgroundShader implements Shaderable {

	public Vector4f color = new Vector4f(0.5f, 0.3f, 0.3f, 1.0f);
	public Vector4f color2 = new Vector4f(1.3f, 1.8f, 1.0f, 1.0f);
	public float time;
	public float seed;
	public int resolution;
	public boolean needsFieldUpdate = true;
	public float rotSecA;
	public float rotSecB;
	public float rotSecC;

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	//	uniform float time;
	//	uniform vec2 resolution;
	@Override
	public void updateShaderParameters(Shader shader) {
		
		GlUtil.updateShaderVector4f(shader, "viewport", Controller.viewport.get(0), Controller.viewport.get(1), Controller.viewport.get(2), Controller.viewport.get(3));
		GlUtil.updateShaderVector2f(shader, "resolution", resolution, resolution);
		GlUtil.updateShaderVector2f(shader, "invResolution", 1f / resolution, 1f / resolution);
		GlUtil.updateShaderFloat(shader, "time", seed);
		GlUtil.updateShaderInt(shader, "fieldIteration", 30);
		GlUtil.updateShaderInt(shader, "field2Iteration", 24);
		GlUtil.updateShaderVector4f(shader, "color", color);
		GlUtil.updateShaderVector4f(shader, "color2", color2);
		
		if (shader.recompiled) {
			shader.recompiled = false;
		}
	}

	public boolean isRecompiled() {
		return ShaderLibrary.fieldShader.recompiled;
	}

}
