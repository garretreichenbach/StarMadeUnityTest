package org.schema.schine.graphicsengine.shader.bloom;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class LensFlareShader implements Shaderable {

	static int texSlot = GL13.GL_TEXTURE7;
	static int texSlotNum = texSlot - GL13.GL_TEXTURE0;
	static int texSlotDirt = GL13.GL_TEXTURE9;
	static int texSlotDirtNum = texSlotDirt - GL13.GL_TEXTURE0;
	public Vector3f lightPosOnScreen = new Vector3f();
	private float exposure = 1;
	private float decay = 0.3f;
	private float desity = 3;
	private float weight = 1;
	private int silouetteTexId = -1;
	private float lx;
	private float ly;

	/**
	 * @return the silouetteTexId
	 */
	public int getSilouetteTexId() {
		return silouetteTexId;
	}

	/**
	 * @param silouetteTexId the silouetteTexId to set
	 */
	public void setSilouetteTexId(int silouetteTexId) {
		this.silouetteTexId = silouetteTexId;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(texSlot);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {
		//		uniform float exposure;
		//		 uniform float decay;
		//		 uniform float density;
		//		 uniform float weight;
		//		 uniform vec2 lightPositionOnScreen;
		//		 uniform sampler2D firstPass;

		assert (silouetteTexId >= 0);

		GlUtil.updateShaderFloat(shader, "exposure", exposure);
		GlUtil.updateShaderFloat(shader, "decay", decay);
		GlUtil.updateShaderFloat(shader, "desity", desity);
		GlUtil.updateShaderFloat(shader, "weight", weight);

		GlUtil.updateShaderVector2f(shader, "lightPositionOnScreen", lx, ly);

		GlUtil.glActiveTexture(texSlot);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, silouetteTexId);

		GlUtil.updateShaderInt(shader, "firstPass", texSlotNum);

		GlUtil.glActiveTexture(texSlotDirt);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("dirtlense").getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "Dirt", texSlotDirtNum);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	public boolean update() {
		lx = (lightPosOnScreen.x / GLFrame.getWidth());
		ly = (1.0f - lightPosOnScreen.y / GLFrame.getHeight());
		//		System.err.println("LIGHTPOS: "+lightPosOnScreen+"; "+lx+", "+ly);
		return true;//lx >= 0f && lx <= 1.0f && ly >= 0f && ly <= 1.0f;
	}

}
