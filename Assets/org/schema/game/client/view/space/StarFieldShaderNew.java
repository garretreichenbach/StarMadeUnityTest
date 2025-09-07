package org.schema.game.client.view.space;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;

public class StarFieldShaderNew implements Shaderable {

	private SinusTimerUtil timerUtil = new SinusTimerUtil(20);
	private int starTexId = -1;
	private int starColorId = -1;

	public StarFieldShaderNew() {
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		if (starTexId < 0) {
			starTexId = Controller.getResLoader().getSprite("star").getMaterial().getTexture().getTextureId();
		}
		if (starColorId < 0) {
			starColorId = Controller.getResLoader().getSprite("starColorMap-gui-").getMaterial().getTexture().getTextureId();
		}
		GlUtil.updateShaderVector4f(shader, "Param", new Vector4f(1f, 0.1f, 0.0010f, 0.5f));

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, starTexId);
		GlUtil.updateShaderInt(shader, "Texture", 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, starColorId);
		GlUtil.updateShaderInt(shader, "ColorMap", 1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void update(Timer timer) {
		timerUtil.update(timer);
	}

}
