package org.schema.game.client.view.shader;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.common.data.element.Element;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class CubeMeshPointShader implements Shaderable {
	Matrix4f mvp = new Matrix4f();
	Matrix4f modelView = new Matrix4f();
	Matrix4f projection = new Matrix4f();

	private int textureAId = 0;

	public int getTextureAId() {
		return textureAId;
	}

	public void setTextureAId(int textureAId) {
		this.textureAId = textureAId;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		modelView = GlUtil.retrieveModelviewMatrix(modelView);
		projection = GlUtil.retrieveProjectionMatrix(projection);
		mvp = GlUtil.retrieveModelviewProjectionMatrix();
		GlUtil.modelBuffer.rewind();
		Matrix4fTools.load(GlUtil.modelBuffer, modelView);
		GlUtil.retrieveNormalMatrix(modelView);
		GlUtil.printGlErrorCritical();
		GlUtil.mvpBuffer.rewind();
		GlUtil.updateShaderMat4(shader, "mvpMatrix", GlUtil.mvpBuffer, false);
		GlUtil.normalBuffer.rewind();
		GlUtil.updateShaderMat3(shader, "normalMatrix", GlUtil.normalBuffer, false);
		GlUtil.updateShaderVector3f(shader, "lightDir", new Vector3f(0, 1, 0));
		GlUtil.printGlErrorCritical();
		GlUtil.updateShaderVector3f(shader, "lightColor", new Vector3f(1, 1, 1));
		GlUtil.printGlErrorCritical();
		GlUtil.updateShaderVector3f(shader, "lightAmbient", new Vector3f(0.3f, 0.3f, 0.3f));
		GlUtil.printGlErrorCritical();
		GlUtil.updateShaderVector4f(shader, "matColor", new Vector4f(0.3f, 0.3f, 0.3f, 1.0f));
		GlUtil.printGlErrorCritical();
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, textureAId);
		GlUtil.updateShaderFloat(shader, "Size2", Element.BLOCK_SIZE / 2f);
		//		GlUtil.updateShaderFloat(shader, "Size", Element.HALF_SIZE);
		GlUtil.updateShaderInt(shader, "mainTexA", 0);

		GlUtil.printGlErrorCritical();
		//		uniform mat4 mvpMatrix;
		//		uniform mat3 normalMatrix;
		//		uniform vec3 lightDir;
		//		uniform vec3 lightColor;
		//		uniform vec3 lightAmbient;
		//		uniform vec4 matColor;
	}

}
