package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.network.client.ClientState;

public class GUITexDrawableAreaCustomWrappable extends GUITexDrawableArea implements Shaderable {

	public float wrapX = 1.0f;
	public float wrapY = 1.0f;
	
	public GUITexDrawableAreaCustomWrappable(ClientState state,
			Texture texture, float xOffset, float yOffset) {
		super(state, texture, xOffset, yOffset);
	}

	@Override
	public void draw() {
		ShaderLibrary.guiTextureWrapperShader.setShaderInterface(this);
		ShaderLibrary.guiTextureWrapperShader.load();
		super.draw();
		ShaderLibrary.guiTextureWrapperShader.unload();
	}

	@Override
	public void onExit() {
		
	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.updateShaderFloat(shader, "wrapX", wrapX);
		GlUtil.updateShaderFloat(shader, "wrapY", wrapY);
		GlUtil.updateShaderVector4f(shader, "tint", getColor());
		GlUtil.updateShaderInt(shader, "tex", 0);
	}

	
	
	

}
