package org.schema.schine.graphicsengine.shader.bloom;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.Texture;

public class GodRayShader implements Shaderable {

	//	private float exposure = 1;
	//	private float decay = 0.3f;
	//	private float desity = 3;
	//	private float weight = 1;

	static int texSlotDirt = GL13.GL_TEXTURE9;
	static int texSlotDirtNum = texSlotDirt - GL13.GL_TEXTURE0;

	static int texSlotFlare = GL13.GL_TEXTURE8;
	static int texSlotFlareNum = texSlotFlare - GL13.GL_TEXTURE0;

	static int texSlot = GL13.GL_TEXTURE7;
	static int texSlotNum = texSlot - GL13.GL_TEXTURE0;

	static int texSlotScene = GL13.GL_TEXTURE6;
	static int texSlotSceneNum = texSlotScene - GL13.GL_TEXTURE0;
	public final Vector4f tint = new Vector4f(1, 1, 1, 1);
	public Vector3f lightPosOnScreen = new Vector3f();
	private int silouetteTexId = -1;
	private Texture lensFlareTex;
	private float lx;
	private float ly;
	private FrameBufferObjects fbo;

	/**
	 * @return the silouetteTexId
	 */
	public int getSilouetteTexId() {
		return silouetteTexId;
	}

	@Override
	public void onExit() {

		GlUtil.glActiveTexture(texSlot);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(texSlotFlare);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(texSlotScene);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(texSlotDirt);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {

		if (lensFlareTex == null) {
			lensFlareTex = Controller.getResLoader().getSprite("lens_flare").getMaterial().getTexture();
		}

		//		uniform float exposure;
		//		 uniform float decay;
		//		 uniform float density;
		//		 uniform float weight;
		//		 uniform vec2 lightPositionOnScreen;
		//		 uniform sampler2D firstPass;

		assert (silouetteTexId >= 0);
	
		GlUtil.updateShaderFloat(shader, "screenRatio", (float) GLFrame.getHeight() / (float) GLFrame.getWidth());
		//		GlUtil.updateShaderFloat(shader, "exposure", exposure);
		//		GlUtil.updateShaderFloat(shader, "decay", decay);
		//		GlUtil.updateShaderFloat(shader, "desity", desity);
		//		GlUtil.updateShaderFloat(shader, "weight", weight);

		GlUtil.updateShaderVector2f(shader, "lightPositionOnScreen", lx, ly);

		//		uniform vec4            Param1; // (SunPosX, SunPosY, 1.0/fbo.width, 0.5/FlareTexture.width)
		//		uniform vec4            Param2; // (Radius, Stride, Bright, Scale)

		//      color       		bright   scale  position  tc offset  tc width
		//Ghost { (0.6, 0.5, 0.35)   0.5     0.1    -0.45       0.25      0.25 }

		GlUtil.updateShaderVector4f(shader, "Param1", lx, ly, 1.0f / fbo.getWidth(), 0.5f / lensFlareTex.getWidth());
		GlUtil.updateShaderVector4f(shader, "Param2", 1, 0.2f, 1, 1);
		GlUtil.updateShaderVector4f(shader, "tint", tint);

		GlUtil.glActiveTexture(texSlot);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, silouetteTexId);
		GlUtil.updateShaderInt(shader, "firstPass", texSlotNum);

		GlUtil.glActiveTexture(texSlotFlare);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, lensFlareTex.getTextureId());
		GlUtil.updateShaderInt(shader, "Texture", texSlotFlareNum);

		GlUtil.glActiveTexture(texSlotScene);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getTextureID());
		GlUtil.updateShaderInt(shader, "Scene", texSlotSceneNum);

		GlUtil.glActiveTexture(texSlotDirt);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, Controller.getResLoader().getSprite("dirtlense").getMaterial().getTexture().getTextureId());
		GlUtil.updateShaderInt(shader, "Dirt", texSlotDirtNum);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
	}

	/**
	 * @param silouetteTexId the silouetteTexId to set
	 * @param fbo
	 */
	public void setSilouetteTexId(int silouetteTexId) {
		this.silouetteTexId = silouetteTexId;
	}
	public void setScene(FrameBufferObjects fbo){
		this.fbo = fbo;
	}

	public void update() {
		lx = (lightPosOnScreen.x / GLFrame.getWidth());
		ly = (1.0f - lightPosOnScreen.y / GLFrame.getHeight());
	}

}
