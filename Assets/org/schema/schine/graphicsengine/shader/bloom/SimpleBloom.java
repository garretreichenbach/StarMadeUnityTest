package org.schema.schine.graphicsengine.shader.bloom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class SimpleBloom extends AbstractGaussianBloomShader implements Shaderable {
	private int textureId;

	public int getTextureId() {
		return textureId;
	}

	public void setTextureId(int textureId) {
		this.textureId = textureId;
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
		calculateMatrices();
		GlUtil.updateShaderMat4(shader, "MVP", mvpBuffer, false);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GlUtil.updateShaderInt(shader, "tex", 0);
	}

}
