package org.schema.game.client.view.effects;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.schine.graphicsengine.core.AbstractScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.Keyboard;

public class DepthBufferScene {

	
	private GameClientState state;
	private static int depthFBO;
	private static int depthTexture;
	
//	
//	private int width = 1024;
//	private int height = 1024;
	private int width = GLFrame.getWidth();
	private int height = GLFrame.getHeight();
	
	private static boolean init;
	
	public DepthBufferScene(GameClientState state) {
		this.state = state;
	}

	
	public void onInit() throws GraphicsException {
		depthFBO = GL30.glGenFramebuffers();

		depthTexture = GL11.glGenTextures();



		// Allocate GPU-memory for the depth-texture.

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);

		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlUtil.printGlErrorCritical();
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GlUtil.printGlErrorCritical();
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);



		// Bind the texture to the framebuffers depth-attachment.

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depthFBO);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);



		// Tell the Framebuffer we won't provide any color-atachments.

		GL11.glDrawBuffer(GL11.GL_NONE); // For depth-only-renderings, if you need also frag-color don't use this.

		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new GraphicsException("FBO FAILED TO INIT");
		}

		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		
		init = true;
	}
	private static final boolean ACTIVE = false;
	public void createDepthTexture() throws GraphicsException {
		
		if(!ACTIVE) {
			return;
		}
		if(!init) {
			onInit();
		}
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depthFBO);
		GL11.glDepthMask(true);
		GL11.glClearColor(0,0,1,1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		state.getWorldDrawer().getSegmentDrawer().setSegmentRenderPass(SegmentDrawer.SegmentRenderPass.ALL);
		state.getWorldDrawer().prepareCamera();
		GL11.glDepthRange(0.0, 1.0);
		state.getWorldDrawer().getSegmentDrawer().draw(SegmentDrawer.shader, ShaderLibrary.depthCubeShader, false, false, null, (short) state.getNumberOfUpdate());
		state.getWorldDrawer().getSegmentDrawer().drawCubeLod(true);
		state.getWorldDrawer().getSegmentDrawer().enableCulling(true);
		state.getWorldDrawer().getCharacterDrawer().shadow = true;
		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)){
			GlUtil.printGlErrorCritical();
		}
		GL20.glUseProgram(0);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		
		state.getWorldDrawer().getCharacterDrawer().draw();

		state.getWorldDrawer().getShards().draw();

		if (state.getWorldDrawer().getCreatureTool() != null) {
			state.getWorldDrawer().getCreatureTool().draw();
		}
		state.getWorldDrawer().getCharacterDrawer().shadow = false;

		GlUtil.glEnable(GL11.GL_CULL_FACE);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		GL11.glClearColor(0,0,0,0);
		GL11.glDepthMask(true);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	
	public void drawDepthTextureOnScreen(FrameBufferObjects fbo) {
		if(fbo != null) {
			fbo.enable();
		}
//		GlUtil.glColor4f(0, 0, 0, 0);
		prepareDraw(depthTexture);
		
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(0, 0);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(0, GLFrame.getHeight());

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(GLFrame.getWidth(), GLFrame.getHeight());

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(GLFrame.getWidth(), 0);

		GL11.glEnd();
		
		finishUpDraw();
		
		if(fbo != null) {
			fbo.disable();
		}
	}
	
	private void prepareDraw(int texture) {
		//changing to ortogonal projection matrix
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();

		//ortogonal view for screen quad
		GlUtil.gluOrtho2D(0, GLFrame.getWidth(), 0, GLFrame.getHeight());
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPushMatrix();
		GlUtil.glLoadIdentity();

		GlUtil.glDisable(GL11.GL_LIGHTING);
		
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		
		GlUtil.glEnable( GL11.GL_TEXTURE_2D );
		//bind to framebuffer texture
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture);

		GlUtil.glDisable(GL11.GL_CULL_FACE);
	}
	private void finishUpDraw() {
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		GlUtil.glPopMatrix();
	}


	public int getDepthTextureId() {
		return depthTexture;
	}


	public static float getNearPlane() {
		return AbstractScene.getNearPlane();
	}


	public static float getFarPlane() {
		return AbstractScene.getFarPlane();
	}

}
