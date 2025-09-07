package org.schema.schine.graphicsengine.forms;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class ParticleSystemSmokeShaded extends ParticleSystem implements Shaderable {

	private float cover;
	private Vector4f camPos;
	private Vector4f lightPos;
	private Shader shader;

	public ParticleSystemSmokeShaded(int lifeTime, int type, Sprite... sprite) {
		super(lifeTime, type, 10, 2, 5, sprite);

		lightPos = new Vector4f();
		camPos = new Vector4f();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.ParticleSystem#draw(javax.media.openGL11.GL21, javax.media.openGL11.GLU.gl2.GLU)
	 */
	@Override
	public void draw() {
//		shader = ShaderLibrary.volumeSmokeShader;
		shader.setShaderInterface(this);
		shader.load();
		super.draw();
		shader.unload();
	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {
		lightPos.set(scene.getLight().getPos().x, scene.getLight().getPos().y, scene.getLight().getPos().z, 1);
		camPos.set(Controller.getCamera().getPos().x, Controller.getCamera().getPos().y, Controller.getCamera().getPos().z, 0.0f);
		//		throw new NullPointerException("[redign note] fix cover setting");
		//		cover = ((GameDrawManager)scene).getSky().getCover();
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		cover = 5;
		Vector4f d = new Vector4f(getPos().x, getPos().y, getPos().z, 0);
		GlUtil.updateShaderVector4f(shader, "source", d);

		GlUtil.updateShaderFloat(shader, "cCover", cover * 2);

		GlUtil.updateShaderVector4f(shader, "lightPos", lightPos);

		//		GlUtil.updateShaderVector4f( shader, "camPos", camPos);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL12.GL_TEXTURE_3D, getSprite()[0].getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "tex", 0);
	}
}
