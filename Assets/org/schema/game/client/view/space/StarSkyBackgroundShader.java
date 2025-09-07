package org.schema.game.client.view.space;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

public class StarSkyBackgroundShader implements Shaderable {
	private static FloatBuffer projBuffer = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer modelBuffer = MemoryUtil.memAllocFloat(16);
	private static FloatBuffer mvpBuffer = MemoryUtil.memAllocFloat(16);
	private Texture cubeMapTexture;

	public StarSkyBackgroundShader() {

		loadBackgroundMap();

	}

	public Texture getCubeMapTexture() {
		return cubeMapTexture;
	}

	public void setCubeMapTexture(Texture cubeMapTexture) {
		this.cubeMapTexture = cubeMapTexture;
	}

	private void loadBackgroundMap() {
		assert (GameResourceLoader.skyTexture != null);

		this.cubeMapTexture = GameResourceLoader.skyTexture;
	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {

//		projBuffer.rewind();
//		Controller.projectionMatrix.store(projBuffer);
//		projBuffer.rewind();
//
//
//
//		modelBuffer.rewind();
//		Controller.modelviewMatrix.store(modelBuffer);
//		modelBuffer.rewind();
//
//		Matrix4f pmat = new Matrix4f();
//		pmat.load(projBuffer);
//		Matrix4f mmat = new Matrix4f();
//		mmat.load(modelBuffer);
//
//
//		Matrix4f mvp = new Matrix4f();
//
//		Matrix4f.mul(pmat, mmat, mvp);
//
//		mvp.store(mvpBuffer);
//		mvpBuffer.rewind();

//		GlUtil.updateShaderMat4(shader, "MVP", mvpBuffer, false);

		GlUtil.updateShaderInt(shader, "CubeMapTex", 0);

		GlUtil.updateShaderVector4f(shader, "MaterialColor", 1, 1, 1, 1);

		//		uniform samplerCube CubeMapTex; // The cube map
		//		uniform bool DrawSkyBox; // Are we drawing the sky box?
		//		uniform float ReflectFactor; // Amount of reflection
		//		uniform vec4 MaterialColor; // Color of the object's "Tint"
	}

}
