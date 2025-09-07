package org.schema.game.client.view.effects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class PlanetCoreDrawer implements Shaderable, Drawable {

	private float time;
	private PlanetIcoCore core;
	private Mesh mesh;

	public void setCore(PlanetIcoCore c) {
		if(core != c) {
			core = c;
			if(core != null) mesh = (Mesh) Controller.getResLoader().getMesh("Sphere").getChilds().iterator().next();
			else mesh = null;
		}
	}

	@Override
	public void updateShader(DrawableScene scene) {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.updateShaderFloat(shader, "time", time);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.lavaTexture.getTextureId());
		GlUtil.updateShaderInt(shader, "lavaTex", 0);
	}

	@Override
	public void onInit() {

	}

	@Override
	public void draw() {
		if(mesh != null) {
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, GameResourceLoader.lavaTexture.getTextureId());

			mesh.loadVBO(true);
			GlUtil.glEnable(GL11.GL_BLEND);
			ShaderLibrary.lavaShader.setShaderInterface(this);

			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			ShaderLibrary.lavaShader.load();

			mesh.setTransform(core.getWorldTransform());
			mesh.setScale(core.getRadius() * 1.1f, core.getRadius() * 1.1f, core.getRadius() * 1.1f);
			mesh.renderVBO();

			ShaderLibrary.lavaShader.unload();
			mesh.unloadVBO(true);

			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_CULL_FACE);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}

	@Override
	public void cleanUp() {
		if(mesh != null) mesh.cleanUp();
	}

	@Override
	public void onExit() {

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	public void update(Timer timer) {
		time += timer.getDelta();
	}
}
