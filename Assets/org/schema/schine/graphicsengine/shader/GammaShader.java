package org.schema.schine.graphicsengine.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

public class GammaShader implements Shaderable {


	private int siluetteFB;


	public GammaShader() {

	}


	@Override
	public void onExit() {
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		//texture loaded in fbo
		GlUtil.updateShaderInt(shader, "fbTex", 0);
		GlUtil.updateShaderFloat(shader, "gammaInv", 1.0f/(EngineSettings.G_GAMMA.getFloat()));
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, siluetteFB);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		
		GlUtil.updateShaderInt(shader, "silTex", 1);
	}


	public void setFGSilouette(int textureID) {
		this.siluetteFB = textureID;
	}

	

}
