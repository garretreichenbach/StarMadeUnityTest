package org.schema.game.client.view.sundrawer;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.effects.OcclusionLensflare;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

public class SunDrawer implements Drawable, Shaderable {

	public float sizeMult = 1;
	/**
	 * The time.
	 */
	private float time;
	/**
	 * The sky tex.
	 */
	private int skyTex;
	/**
	 * The sky normal.
	 */
	private int skyNormal;
	/**
	 * The sharp.
	 */
	private float sharp;
	/**
	 * The cover.
	 */
	private float cover;
	private Mesh planetMesh;
	private Vector3i relSectorPos = new Vector3i(1, 1, 1); //set to something other than 0,0,0 to avoid plasma at the startup
	private Sprite sprite;
	private int oQuery;
	private int samples;
	private int queried;
	private OcclusionLensflare lensFlare;
	private int maxSamples;
	private GameClientState state;
	private Vector4f color = new Vector4f(1, 1, 1, 1);
	private boolean useQueries = true;

	private boolean useLensFlare = true;

	private boolean depthTest = false;
	private boolean drawPlasma = false;

	public SunDrawer(GameClientState state) {

		this.state = state;
		lensFlare = new OcclusionLensflare();
	}

	public void checkSamples() {
		if (queried > 5) {
			//			GlUtil.printGlErrorCritical();
			//			avail = GL15.glGetQueryObjectui(oQuery, GL15.GL_QUERY_RESULT_AVAILABLE) == GL11.GL_TRUE;
			//			GlUtil.printGlErrorCritical();
			//			if(avail){
			samples = GL15.glGetQueryObjectui(oQuery, GL15.GL_QUERY_RESULT);
			queried = 0;
			//			System.err.println("SAMPLES: "+samples);
			//			}
			//			GlUtil.printGlErrorCritical();

		}
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void draw() {

		/*
		 * FOR BLOOM ON
		 */
		if (!Controller.getCamera().isPointInFrustrum(AbstractScene.mainLight.getPos())) {
			return;
		}

		GlUtil.glPushMatrix();

		if (drawPlasma && relSectorPos.x == 0 && relSectorPos.y == 0 && relSectorPos.z == 0) {
			GlUtil.glPushMatrix();
			GlUtil.translateModelview(Controller.getCamera().getPos());
			GlUtil.scaleModelview(3, 3, 3);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

			ShaderLibrary.plasmaShader.loadWithoutUpdate();
			GlUtil.updateShaderFloat(ShaderLibrary.plasmaShader, "time", time * 10f);
			planetMesh.draw();
			ShaderLibrary.plasmaShader.unloadWithoutExit();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glPopMatrix();

		}

		sprite.setPos(AbstractScene.mainLight.getPos());
		sprite.setTint(color);

		float minLen = Math.min(1,
				Math.max(0, AbstractScene.mainLight.getPos().length() / (((GameStateInterface) state).getSectorSize() * (VoidSystem.SYSTEM_SIZE / 2))));
		float sizeAdaption = minLen * 2f;

		sprite.setBillboard(true);
		sprite.setScale(0.5f + sizeAdaption, 0.5f + sizeAdaption, 0.5f + sizeAdaption);
		sprite.setDepthTest(true);

		if (useQueries) {
			if (queried == 0) {
				//make sure that sun always has max depth
				GL11.glDepthRange(0.999, 1.0);
				GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, oQuery);
				Sprite.draw(sprite, 0);
				GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
				GL11.glDepthRange(0.0, 1.0);
			}

			queried++;
		}

		//			System.err.println("SPRITE "+sprite.getScale()+": "+samples);
		sprite.setDepthTest(false);
		if (samples > 0 || !useQueries) {
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			//				System.err.println("SSS: "+samples);

			//				Sprite.draw(sprite, 0);

			maxSamples = Math.max(1, Math.max(samples, maxSamples - 1));

			lensFlare.setFillRate((float) samples / (float) maxSamples);
			lensFlare.sizeMult = sizeMult;
			lensFlare.drawLensFlareEffects = useLensFlare;
			lensFlare.depthTest = depthTest;
			lensFlare.setMainColor(color);
			lensFlare.draw();

		}
		sprite.setScale(1, 1, 1);
		GlUtil.glPopMatrix();

		GlUtil.glEnable(GL11.GL_CULL_FACE);
		//			GlUtil.glEnable(GL11.GL_DEPTH_TEST);

	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		planetMesh = (Mesh) (Controller.getResLoader().getMesh("Sphere").getChilds().get(0));
		//		boxMesh = (Mesh) (Controller.getResLoader().getMesh("Box").getChilds().get(0));
		sprite = Controller.getResLoader().getSprite("sunSprite-c-");

		oQuery = GL15.glGenQueries();
		sharp = 0.0000005f;
		cover = 0.7f;

		skyTex = Controller.getResLoader().getSprite("detail").getMaterial().getTexture().getTextureId();
		skyNormal = Controller.getResLoader().getSprite("detail_normal").getMaterial().getTexture().getTextureId();
		lensFlare.onInit();
	}

	public void drawPlasma() {
		if (relSectorPos.x == 0 && relSectorPos.y == 0 && relSectorPos.z == 0) {
//			System.err.println("PLAAAASMA");
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glPushMatrix();
			GlUtil.translateModelview(Controller.getCamera().getPos());
			GlUtil.scaleModelview(3, 3, 3);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			ShaderLibrary.plasmaShader.loadWithoutUpdate();
			GlUtil.updateShaderFloat(ShaderLibrary.plasmaShader, "time", time * 10f);
			planetMesh.draw();
			ShaderLibrary.plasmaShader.unloadWithoutExit();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glPopMatrix();
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_CULL_FACE);

			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		} else {
		}
	}

	/**
	 * Gets the cover.
	 *
	 * @return the cover
	 */
	public float getCover() {
		return cover;
	}

	/**
	 * Sets the cover.
	 *
	 * @param cover the cover to set
	 */
	public void setCover(float cover) {
		this.cover = cover;
	}

	/**
	 * Gets the sharp.
	 *
	 * @return the sharp
	 */
	public float getSharp() {
		return sharp;
	}

	/**
	 * Sets the sharp.
	 *
	 * @param sharp the sharp to set
	 */
	public void setSharp(float sharp) {
		this.sharp = sharp;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	/**
	 * uniform sampler2D tex;
	 * uniform sampler2D nmap;
	 * uniform float cSharp;
	 * uniform float cCover;
	 * uniform float cMove;
	 * uniform vec4 lightPos;.
	 *
	 * @param gl            the gl
	 * @param glu           the glu
	 * @param shaderprogram the shaderprogram
	 */
	@Override
	public void updateShaderParameters(Shader shader) {
		;
		GlUtil.updateShaderFloat(shader, "cSharp", sharp);
		GlUtil.updateShaderFloat(shader, "cCover", cover);
		GlUtil.updateShaderFloat(shader, "cMove", time);
		GlUtil.updateShaderVector4f(shader, "lightPos", AbstractScene.mainLight.getPos().x,
				AbstractScene.mainLight.getPos().y,
				AbstractScene.mainLight.getPos().z,
				1);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, skyTex);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, skyNormal);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE2);

		GlUtil.updateShaderInt(shader, "tex", 0);
		GlUtil.updateShaderInt(shader, "nmap", 1);
	}

	public void setRelativeSectorPos(Vector3i currentSector) {
		this.relSectorPos.set(currentSector);
	}

	public void setDrawSunPlasma(boolean val){
		drawPlasma = val;
	}

	public void update(Timer timer) {
		time += timer.getDelta() * 0.07f;
	}

	public void setColor(Vector4f sunColor) {
		this.color.set(sunColor);
	}

	/**
	 * @return the useQueries
	 */
	public boolean isUseQueries() {
		return useQueries;
	}

	/**
	 * @param useQueries the useQueries to set
	 */
	public void setUseQueries(boolean useQueries) {
		this.useQueries = useQueries;
	}

	/**
	 * @return the useLensFlare
	 */
	public boolean isUseLensFlare() {
		return useLensFlare;
	}

	/**
	 * @param useLensFlare the useLensFlare to set
	 */
	public void setUseLensFlare(boolean useLensFlare) {
		this.useLensFlare = useLensFlare;
	}

	public void setDepthTest(boolean b) {
		depthTest = b;
	}
}

