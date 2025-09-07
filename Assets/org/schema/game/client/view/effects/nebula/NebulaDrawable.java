package org.schema.game.client.view.effects.nebula;

import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

import javax.vecmath.Vector3f;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class NebulaDrawable implements Drawable, Shaderable {

	private final Texture texture;
	private final Vector3f[] colorGradient;
	private final Vector3f pos;
	private final int numSteps;
	private final float stepSize;
	private final float densityScale;
	private final int numLightSteps;
	private final float lightStepSize;
	private final Vector3f lightDir;
	private final float lightAbsorb;
	private final float darknessThreshold;
	private final float transmittance;
	private Mesh mesh;
	private Transform transform;
	private Vector3f scale;
	private boolean initialized;

	public NebulaDrawable(Texture texture, Vector3f[] colorGradient, Vector3f pos, int numSteps, float stepSize, float densityScale, int numLightSteps, float lightStepSize, Vector3f lightDir, float lightAbsorb, float darknessThreshold, float transmittance) {
		this.texture = texture;
		this.colorGradient = colorGradient;
		this.pos = pos;
		this.numSteps = numSteps;
		this.stepSize = stepSize;
		this.densityScale = densityScale;
		this.numLightSteps = numLightSteps;
		this.lightStepSize = lightStepSize;
		this.lightDir = lightDir;
		this.lightAbsorb = lightAbsorb;
		this.darknessThreshold = darknessThreshold;
		this.transmittance = transmittance;
	}

	public NebulaDrawable(Texture texture, Vector3f[] colorGradient, Vector3f pos) {
		this(texture, colorGradient, pos, 64, 0.015f, 0.025f, 12, 0.12f, new Vector3f(0, 0, 0), 0.3f, 0.18f, 1.0f);
	}

	@Override
	public void onInit() {
		mesh = (Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0);
		mesh.getMaterial().setTexture(texture);
		transform = new Transform();
		transform.setIdentity();
		transform.origin.set(pos);
		scale = new Vector3f(5.0f, 5.0f, 5.0f);
		initialized = true;
	}

	@Override
	public void draw() {
		if(!initialized) onInit();
		transform.origin.set(Controller.getCamera().getPos());

//		ShaderLibrary.nebulaShader.setShaderInterface(this);
//		ShaderLibrary.nebulaShader.load();
		mesh.loadVBO(true);

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(transform);
		GlUtil.scaleModelview(scale.x * 100, scale.y * 100, scale.z * 100);
		mesh.renderVBO();
		GlUtil.glPopMatrix();

		mesh.unloadVBO(true);
//		ShaderLibrary.nebulaShader.unload();
	}

	@Override
	public void cleanUp() {
		if(initialized) mesh.cleanUp();
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onExit() {

	}

	@Override
	public void updateShader(DrawableScene scene) {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
		GlUtil.updateShaderVector3f(shader, "color", colorGradient[0]); //Todo: Gradient over time
		GlUtil.updateShaderInt(shader, "width", texture.getWidth());
		GlUtil.updateShaderInt(shader, "height", texture.getHeight());
		GlUtil.updateShaderVector3f(shader, "camPos", Controller.getCamera().getPos());
		GlUtil.updateShaderInt(shader, "numSteps", numSteps);
		GlUtil.updateShaderFloat(shader, "stepSize", stepSize);
		GlUtil.updateShaderFloat(shader, "densityScale", densityScale);
		GlUtil.updateShaderInt(shader, "numLightSteps", numLightSteps);
		GlUtil.updateShaderFloat(shader, "lightStepSize", lightStepSize);
		GlUtil.updateShaderVector3f(shader, "lightDir", lightDir);
		GlUtil.updateShaderFloat(shader, "lightAbsorb", lightAbsorb);
		GlUtil.updateShaderFloat(shader, "darknessThreshold", darknessThreshold);
		GlUtil.updateShaderFloat(shader, "transmittance", transmittance);
	}
}
