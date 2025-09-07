package org.schema.schine.graphicsengine.forms;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.shader.ShadowParams;

import com.bulletphysics.linearmath.Transform;

public class Skin implements Shaderable {

	private static ShadowParams shadowParams;
	//	public static final int BONE = EngineSettings.C_USE_NEW_PLAYER_MODEL.isOn() ? 28 : 29;
	private final Skeleton skeleton;
	private final WeightsBuffer weightsBuffer;
	public boolean spotActivated;
	public Vector4f color = new Vector4f(1, 1, 1, 1);
	private boolean initialized;
	private Transform[] boneMatrices;
	private int diffuseTexId;
	private Vector4f lastColor = new Vector4f();
	private int emissiveTexId;
	private int wait;
	private long currentFrameTime;

	public Skin(Skeleton skeleton, WeightsBuffer weightsBuffer) throws ResourceException {
		super();
		this.skeleton = skeleton;
		this.weightsBuffer = weightsBuffer;

	}

	public static void setShadow(ShadowParams shadowParams) {
		Skin.shadowParams = shadowParams;
	}

	/**
	 * @return the diffuseTexId
	 */
	public int getDiffuseTexId() {
		return diffuseTexId;
	}

	/**
	 * @param diffuseTexId the diffuseTexId to set
	 */
	public void setDiffuseTexId(int diffuseTexId) {
		this.diffuseTexId = diffuseTexId;
	}

	/**
	 * @return the skeleton
	 */
	public Skeleton getSkeleton() {
		return skeleton;
	}

	private void initialize() {
		weightsBuffer.initVBO();
		initialized = true;

		//		fringeMapId = TextureLoader.createFringeMap();
	}

	public void loadVBO() {
		if (!initialized) {
			GlUtil.printGlErrorCritical();
			initialize();
			GlUtil.printGlErrorCritical();
		}
		try {
			Shader boneShader = ShaderLibrary.getBoneShader(skeleton.getBoneCount(), spotActivated);

			boolean fresh = boneShader.recompiled;
			
			ShaderLibrary.getBoneShader(skeleton.getBoneCount(), spotActivated).setShaderInterface(this);

			ShaderLibrary.getBoneShader(skeleton.getBoneCount(), spotActivated).load();
			
			if( fresh || wait > 0 ){
				if(fresh){
					wait = 2;
				}
				//using the shader at the time of changing screen causes a invalid operation GL exception
				GL20.glUseProgram(0);
				
			}
			
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		weightsBuffer.loadShaderVBO();

	}

	@Override
	public void onExit() {

		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public void updateShader(DrawableScene scene) {
		
	}

	@Override
	public void updateShaderParameters(Shader shader) {

		assert (boneMatrices != null);
		GlUtil.updateShaderMat4Array(shader, "m_BoneMatrices", boneMatrices, false);

		if (!color.equals(lastColor) || shader.recompiled) {
			GlUtil.updateShaderVector4f(shader, "tint", color);
			lastColor.set(color);
		}

		//		GlUtil.updateShaderVector3f(shader, "lightVector", 0.38f, 0.73f, 0.57f);
		//		GlUtil.updateShaderVector3f(shader, "eyeVector", Controller.getCamera().getForward());
		//		GlUtil.updateShaderFloat(shader, "filmDepth" , 0.63f);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, diffuseTexId);

		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, emissiveTexId);

		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
		//		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, fringeMapId);
		//		GlUtil.updateShaderInt(shader, "fringeMap", 1);

		if (shadowParams != null) {
			shadowParams.execute(shader);
		}

		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		if (shader.recompiled) {
			GlUtil.updateShaderInt(shader, "diffuseMap", 0);
			GlUtil.updateShaderInt(shader, "emissiveMap", 1);
			shader.recompiled = false;
		}

	}

	public void unloadVBO() {
		weightsBuffer.unloadShaderVBO();
		try {
			ShaderLibrary.getBoneShader(skeleton.getBoneCount(), spotActivated).unload();
		} catch (ResourceException e) {
			e.printStackTrace();
		}
	}

	public void update(Timer timer) {

		//		testTranslate(timer, skeleton.getRootBone(), true);

		skeleton.update(timer);
		boneMatrices = skeleton.computeSkinningMatrices();

		if(wait > 0 && currentFrameTime != timer.currentTime){
			wait --;
			currentFrameTime = timer.currentTime;
		}
		
//		int i = 0;
//		for(Transform t : boneMatrices){
//			System.err.println(i+" KKKK \n"+t.getMatrix(new Matrix4f()));
//			i++;
//		}
	}

	/**
	 * @return the emissiveTexId
	 */
	public int getEmissiveTexId() {
		return emissiveTexId;
	}

	/**
	 * @param emissiveTexId the emissiveTexId to set
	 */
	public void setEmissiveTexId(int emissiveTexId) {
		this.emissiveTexId = emissiveTexId;
	}

}
