package org.schema.game.client.view;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class SelectionShader implements Shaderable {
	private int texId;
	private float texMult = 1;

	public SelectionShader(int textureId) {
		this.texId = textureId;
	}

	@Override
	public void onExit() {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.7f, 0.7f, 0.65f);
		GlUtil.updateShaderFloat(shader, "texMult", texMult);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GlUtil.updateShaderInt(shader, "mainTexA", 0);
	}
}
