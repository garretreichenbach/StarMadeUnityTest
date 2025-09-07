package org.schema.game.client.view;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.FrameBufferDrawListener;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.TimeStatistics;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.CubeData;
import org.schema.game.client.view.effects.DepthBufferScene;
import org.schema.game.client.view.effects.GraphicsException;
import org.schema.game.client.view.effects.Shadow;
import org.schema.game.client.view.shader.ShadowShader;
import org.schema.game.common.controller.ShopperInterface;
import org.schema.game.common.data.element.ElementCollectionMesh;
import org.schema.game.common.data.world.LightTransformable;
import org.schema.game.common.data.world.SimpleTransformable;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.server.controller.ServerSegmentProvider;
import org.schema.game.server.controller.pathfinding.AbstractPathFindingHandler;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.GraphicsStateInterface;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.InfoSetting;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Light;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.shader.GammaShader;
import org.schema.schine.graphicsengine.shader.OculusDistortionShader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.SilhouetteShader2D;
import org.schema.schine.graphicsengine.shader.bloom.GaussianBloomShader;
import org.schema.schine.graphicsengine.shader.bloom.GodRayShader;
import org.schema.schine.graphicsengine.shader.targetBloom.Blurrer;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.server.ServerProcessor;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

public class MainGameGraphics extends AbstractScene implements GraphicsStateInterface, ScreenChangeCallback {

	private static final boolean MOUSE_DRAW = false;
	private final WorldToScreenConverter worldToScreenConverter = new WorldToScreenConverter();
	private final FromMeComp fromMeDist = new FromMeComp();
	private boolean mouseUpdate;
	private boolean reshape;
	private GameClientState state;
	private GaussianBloomShader bloomShader;
	private FrameBufferObjects backgroundFbo;
	private SilhouetteShader2D silhouetteShader2D;
	private ShadowShader shadowShader;
	private FrameBufferObjects silhouetteFbo;
	private FrameBufferObjects silhouetteFboPlumes;
	private FrameBufferObjects foregroundFbo;
	private GodRayShader godRayShader;
	private Vector3f lightPosOnScreen = new Vector3f();
	private Vector3f secondLightPosOnScreen = new Vector3f();
	private long lastUpdate;
	private String memoryInfo = "";
	private Shadow shadow;
	private FrameBufferObjects occFBO;
	private GammaShader gammaShader;
	private Blurrer bloomPointPass;
	private boolean debug = true;
	private DepthBufferScene depthBuffer;
	private boolean screenChanged;

	public MainGameGraphics(GameClientState state) {
		super(state);
		this.state = state;
		GraphicsContext.current.registerScreenChangeCallback(this);
	}

	@Override
	public void addPhysicsDebugDrawer() {

	}

	@Override
	public void applyEngineSettings() {

	}

	@Override
	public void cleanUp() {
		System.err.println("[CLIENT][GRAPHICS] cleaning up main graphics");
		ShaderLibrary.cleanUp();
		state.getWorldDrawer().cleanUp();
		if(fbo != null) {
			fbo.cleanUp();
		}
		if(bloomShader != null) {
			bloomShader.cleanUp();
		}
		if(backgroundFbo != null) {
			backgroundFbo.cleanUp();
		}
		if(foregroundFbo != null) {
			foregroundFbo.cleanUp();
		}
		if(foregroundFbo != null) {
			foregroundFbo.cleanUp();
		}
		if(occFBO != null) {
			occFBO.cleanUp();
		}
		if(silhouetteFbo != null) {
			silhouetteFbo.cleanUp();
		}
		if(silhouetteFboPlumes != null) {
			silhouetteFboPlumes.cleanUp();
		}
		if(bloomPointPass != null) {
			bloomPointPass.cleanUp();
		}
		if(shadow != null) {
			shadow.cleanUp();
		}
		if(state.getWorldDrawer() != null) {
			state.getWorldDrawer().cleanUp();
		}
		GraphicsContext.current.unregisterScreenChangeCallback(this);
	}

	private void bloomPass(int ocMode) {
		if(bloomShader == null) {
			this.bloomShader = new GaussianBloomShader(fbo);
			bloomShader.initialize(fbo.getDepthRenderBufferId());
			bloomShader.setSilhouetteTexture(silhouetteFbo.getTexture());
			this.godRayShader = new GodRayShader();
			ShaderLibrary.godRayShader.setShaderInterface(godRayShader);
		}

		fbo.enable();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GlUtil.glDisable(GL11.GL_BLEND);

		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//cubemap texture background
		try {
			if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
				state.getWorldDrawer().getStarSky().drawBackground();
			}
		} catch(GLException e) {
			e.printStackTrace();
		}

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
			backgroundFbo.draw(OculusVrHelper.OCCULUS_NONE);
		}
		GlUtil.glDisable(GL11.GL_BLEND);

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		foregroundFbo.draw(OculusVrHelper.OCCULUS_NONE);

		fbo.disable();

		state.getWorldDrawer().drawAdditional(foregroundFbo, fbo, depthBuffer);

		if(ocMode != 0) {
			if(ocMode == OculusVrHelper.OCCULUS_LEFT) {
				occFBO.enable();
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				occFBO.disable();
			}
			bloomShader.draw(occFBO, ocMode);

		} else {
			GlUtil.glEnable(GL11.GL_BLEND);

			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			foregroundFbo.enable();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			foregroundFbo.disable();

			//draw to forground shader
			bloomShader.draw(foregroundFbo, OculusVrHelper.OCCULUS_NONE);
			GlUtil.glDisable(GL11.GL_BLEND);

			//draw back to main fbo
			fbo.enable();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			foregroundFbo.draw(ocMode);
			fbo.disable();
		}

		if(ocMode != 0) {

			occFBO.enable();

			int rtFullWidth = GLFrame.getWidth();
			int rtFullHeight = GLFrame.getHeight();

			rtFullWidth *= OculusVrHelper.getScaleFactor();
			rtFullHeight *= OculusVrHelper.getScaleFactor();

			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			godRayShader.tint.set(state.getWorldDrawer().sunColor);
			if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
				fbo.draw(ShaderLibrary.godRayShader, ocMode);
			}
			GlUtil.glDisable(GL11.GL_BLEND);

			occFBO.disable();

			//Rigth is second pass -> render fully made vbo
			if(ocMode == OculusVrHelper.OCCULUS_RIGHT) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				//					occFBO.draw(0);

				OculusDistortionShader s = new OculusDistortionShader();
				s.setFBO(OculusVrHelper.OCCULUS_LEFT);
				ShaderLibrary.ocDistortion.setShaderInterface(s);
				occFBO.drawOC(ShaderLibrary.ocDistortion, 0);

				s.setFBO(OculusVrHelper.OCCULUS_RIGHT);
				occFBO.drawOC(ShaderLibrary.ocDistortion, 0);

			}

		} else {
			FrameBufferObjects blurOutput = this.bloomPointPass.blur(fbo);

			godRayShader.lightPosOnScreen.set(lightPosOnScreen);
			godRayShader.update();
			godRayShader.tint.set(state.getWorldDrawer().sunColor);
			godRayShader.setSilouetteTexId(silhouetteFbo.getTexture());
			godRayShader.setScene(blurOutput);

			fbo.enable();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
				blurOutput.draw(ShaderLibrary.godRayShader, ocMode);
			} else {
				blurOutput.draw(ocMode);
			}

			fbo.disable();

			//draw final picture
			fbo.draw(ocMode);

			state.getWorldDrawer().currentFBO = fbo;
		}

		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		state.getWorldDrawer().drawGUI();
		state.getWorldDrawer().currentFBO = null;
	}

	private void createFrameBuffers() {
		int width = GLFrame.getWidth();
		int height = GLFrame.getHeight();
		if(fbo != null) {
			fbo.cleanUp();
		}
		GlUtil.printGlErrorCritical();
		if(backgroundFbo != null) {
			backgroundFbo.cleanUp();
		}
		GlUtil.printGlErrorCritical();
		if(foregroundFbo != null) {
			foregroundFbo.cleanUp();
		}
		GlUtil.printGlErrorCritical();
		if(silhouetteFbo != null) {
			silhouetteFbo.cleanUp();
		}
		if(silhouetteFboPlumes != null) {
			silhouetteFboPlumes.cleanUp();
		}
		if(occFBO != null) {
			occFBO.cleanUp();
		}

		GlUtil.printGlErrorCritical();
		if(EngineSettings.isShadowOn()) {
			if(shadow == null) {
				//never reinitialize since shadow dont depend on resolution changes
				shadow = new Shadow(state);
				shadow.init();
			}
		}
		GlUtil.printGlErrorCritical();

		fbo = new FrameBufferObjects("MAIN", width, height);
		backgroundFbo = new FrameBufferObjects("Background", width, height);

		foregroundFbo = new FrameBufferObjects("Foreground", width, height);

		silhouetteFbo = new FrameBufferObjects("Silhouette", width / 2, height / 2);

		silhouetteFboPlumes = new FrameBufferObjects("SilhouettePlums", width, height);
		if(silhouetteShader2D == null) {
			silhouetteShader2D = new SilhouetteShader2D();
		}
		if(shadowShader == null) {
			shadowShader = new ShadowShader();
		}

		if(EngineSettings.O_OCULUS_RENDERING.isOn()) {
			occFBO = new FrameBufferObjects("Occulus", width, height);
		}

		if(this.bloomPointPass != null) {
			this.bloomPointPass.cleanUp();
		}
		this.bloomPointPass = new Blurrer(silhouetteFboPlumes, OculusVrHelper.OCCULUS_NONE);
	}

	public static boolean isFBOOn() {
		return EngineSettings.F_FRAME_BUFFER.isOn() || EngineSettings.F_BLOOM.isOn() || EngineSettings.isShadowOn();
	}

	@Override
	public void draw() {
		state.getWorldDrawer().onStartFrame();

		boolean fboOn = isFBOOn();
		if(!Controller.getResLoader().isLoaded() || !state.isReady() || GLFrame.getWidth() <= 0 || GLFrame.getHeight() <= 0) return;

		if(firstDraw) {
			onInit();
			firstDraw = false;
			reshape = false;
		}

		if(reshape) {
			if(fboOn) initializeFrameBuffers();
			reshape = false;
		}

		if(EngineSettings.G_SHADER_RELOAD.isOn()) {
			System.err.println("RECOMPILING SHADERS");
			ShaderLibrary.reCompile(Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT));
			EngineSettings.G_SHADER_RELOAD.switchOn();
		}

		GL11.glDepthFunc(GL11.GL_LEQUAL);

		if(!AbstractSceneNode.isMirrorMode()) {
			initProjection(OculusVrHelper.OCCULUS_NONE);
		}

		worldToScreenConverter.storeCurrentModelviewProjection();
		worldToScreenConverter.convert(state.getScene().getLight().getPos(), lightPosOnScreen, false);

		if(state.getWorldDrawer().secondSunLightPos != null) {
			worldToScreenConverter.convert(state.getWorldDrawer().secondSunLightPos, secondLightPosOnScreen, false);
		}

		if(fboOn) {
			try {
				//If window is minimized, do not draw
				if(state.getGraphicsContext().getWidth() <= 0 || state.getGraphicsContext().getHeight() <= 0) return;
				//Check if window is focused
				//if not, do normal pass
				int focused = GLFW.glfwGetWindowAttrib(GLFW.glfwGetCurrentContext(), GLFW.GLFW_FOCUSED);
				if(focused == 0) normalPass();
				else framebufferPass(OculusVrHelper.OCCULUS_NONE);
			} catch(NullPointerException exception) {
				//assume window is not focused
			} catch(Exception exception) {
				exception.printStackTrace();
				EngineSettings.F_FRAME_BUFFER.setOn(false);
				try {
					EngineSettings.write();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
				normalPass();
			}
		} else {
			normalPass();
		}
		state.getWorldDrawer().retrieveMousePosition();
		{
			TimeStatistics.reset("INFO-DRAW");
			GlUtil.glPushMatrix();
			drawInfo();
			GlUtil.glPopMatrix();
			TimeStatistics.set("INFO-DRAW");
		}

		if(MOUSE_DRAW) {
			if(mouseUpdate || Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				mouseUpdate = false;
			}
		}
		GlUtil.glColor4f(1, 1, 1, 1);

		state.getWorldDrawer().onEndFrame();

	}

	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		// testText.setPos(50,100,100);
		ShaderLibrary.loadShaders();
		if(isFBOOn()) {
			createFrameBuffers();
			initializeFrameBuffers();
		}
		//			getController().attachMouseAndKeyboard(this);
		//			attachCamera();
		//			canvas.addKeyListener(state.getWorldDrawer());
		//			canvas.addMouseListener(state.getWorldDrawer());
		depthBuffer = new DepthBufferScene(state);
		this.gammaShader = new GammaShader();
		state.getPhysics().getDynamicsWorld().setDebugDrawer(new GLDebugDrawer());

		addPhysicsDebugDrawer();

		//high view in front
		//		Controller.camera.setForward(new Vector3f(0.006727384f, -0.5807032f, -0.8140875f));
		//		Controller.camera.setUp(new Vector3f(0.0047985995f, 0.81411535f, -0.5806834f));
		//		Controller.camera.setLeft(new Vector3f(-0.99996585f, 0.0f, -0.008263429f));
		//		Controller.camera.setPosition(new Vector3f(2874.478f, 2209.7996f, 5351.9995f));
		//		Controller.camera.setRotation(new Vector3f(-35.50002f, 180.47346f, 56.11412f));

		//low iso view
		//		Controller.camera.setForward(new Vector3f(0.62316346f, -0.5045282f, 0.597594f));
		//		Controller.camera.setUp(new Vector3f(0.3641479f, 0.8633952f, 0.3492063f));
		//		Controller.camera.setLeft(new Vector3f(0.6921442f, 0.0f, -0.7217592f));
		//		Controller.camera.setPosition(new Vector3f(2109.9666f, 695.90436f, 2957.0244f));
		//		Controller.camera.setRotation(new Vector3f(-123.86902f, 271.77362f, 56.11412f));

		Controller.getResLoader().getMesh("Box").setStaticMesh(true);

		state.getWorldDrawer().onInit();
	}

	@Override
	public void drawScene() {
		GlUtil.glPushMatrix();

		if(state.getCurrentPlayerObject() != null) {

			fromMeDist.where = state.getCurrentPlayerObject().getWorldTransform().origin;
			Collections.sort(state.spotlights, fromMeDist);

			for(Light l : spotLights) {
				l.cleanUp();
			}
			spotLights.clear();

			for(int i = 0; i < state.spotlights.size() && i < 4; i++) {

				LightTransformable sp = state.spotlights.get(i);

				Light spot = sp.getLight();
				if(spot == null) {
					spot = new Light();
					sp.setLight(spot);
				} else {
					spot.reassign();
				}
				Vector3f origin = sp.getWorldTransformOnClient().origin;
				spot.setPos(origin.x, origin.y, origin.z);
				if(sp.getOwnerState() != null && (sp.getOwnerState() != state.getPlayer() || !KeyboardMappings.FREE_CAM.isDown())) {
					sp.getOwnerState().getForward(spot.spotDirection);

					Vector3f up = sp.getOwnerState().getUp(new Vector3f());
					up.scale(0.25f);
					spot.spotUp.set(up);

				}
				spot.getPos().add(spot.spotUp);
				spot.setSpecular(new Vector4f(.3f, .3f, .3f, 1f));
				spot.setDiffuse(new Vector4f(.2f, .2f, .2f, 1f));
				spot.setShininess(new float[]{8.0f});
				spot.quadAttenuation = 0.05f;
				spotLights.add(spot);
				spot.draw();
				spot.deactivate();
			}

		}

		getMainLight().draw();
		GlUtil.glColor4f(1, 1, 1, 1);

		TimeStatistics.reset("WORLD-DRAW");
		state.getWorldDrawer().shadow = shadow;
		state.getWorldDrawer().draw();
		TimeStatistics.set("WORLD-DRAW");
		if(state.getScene().fbo != null && state.getScene().fbo.isEnabled()) {
		} else {
			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
				GlUtil.glDisable(GL11.GL_LIGHTING);
				//				getState().getPhysics().getDynamicsWorld().debugDrawWorld();

				//				state.getPhysics().drawDebugObjects( );
				GlUtil.glEnable(GL11.GL_DEPTH_TEST);
				GlUtil.glMatrixMode(GL11.GL_PROJECTION);
				GlUtil.glPushMatrix();

				float aspect = (float) GLFrame.getWidth() / (float) GLFrame.getHeight(); //1.333333333333333333333333f
				if(!EngineSettings.O_OCULUS_RENDERING.isOn()) {
					GlUtil.gluPerspective(Controller.projectionMatrix, EngineSettings.G_FOV.getFloat() * AbstractScene.getZoomFactorForRender(!state.getWorldDrawer().getGameMapDrawer().isMapActive()), aspect, 0.05f, state.getSectorSize() * 7, true);
				} else {
					Matrix4f gluPerspective = GlUtil.gluPerspective(Controller.projectionMatrix, EngineSettings.G_FOV.getFloat() * AbstractScene.getZoomFactorForRender(!state.getWorldDrawer().getGameMapDrawer().isMapActive()), OculusVrHelper.getAspectRatio(), 0.05f, state.getSectorSize() * 7, true);

					Matrix4fTools.mul(Controller.occulusProjMatrix, gluPerspective, gluPerspective);

					GlUtil.glLoadMatrix(gluPerspective);
				}
				GlUtil.glMatrixMode(GL11.GL_MODELVIEW);

				DebugDrawer.drawBoundingBoxes();
				DebugDrawer.drawBoundingXses();
				DebugDrawer.drawPoints();
				DebugDrawer.drawBoxes();
				DebugDrawer.drawLines();

				GL11.glColor4f(1, 1, 1, 1);

				GlUtil.glMatrixMode(GL11.GL_PROJECTION);
				GlUtil.glPopMatrix();
				GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
			}
		}
		zSortedMap.clear();
		GlUtil.glPopMatrix();
	}

	@Override
	public FrameBufferObjects getFbo() {
		return fbo;
	}

	@Override
	public Light getLight() {
		return getMainLight();
	}

	@Override
	public void reshape() {

	}

	@Override
	public void update(Timer timer) {
		long t = System.currentTimeMillis();

		boolean si = EngineSettings.S_INFO_DRAW.getObject() == InfoSetting.SOME_INFO || EngineSettings.S_INFO_DRAW.getObject() == InfoSetting.ALL_INFO;
		infoList.add(0, "v" + VersionContainer.VERSION + "   [FPS] " + timer.getFps() + (si ? " (" + StringTools.formatPointZeroZero(timer.lastDrawMilli) + "ms)" : ""));
		infoList.add(1, "\n");//[PING] " + state.getPing());

		if((GameClientState.freeMemory / 1024) / 1024 < 10) {
			state.getController().popupAlertTextMessage(Lng.str("WARNING!\nYou are running low on memory! \nPlease increase the memory setting in the Launcher\n(Next to the \"Launch\" button)."), 0);
		}
		if(System.currentTimeMillis() - lastUpdate > 900) {
			memoryInfo = "[MEMORY] free: " + StringTools.formatPointZero((GameClientState.freeMemory / 1024) / 1024.0) + "mb, taken: " + StringTools.formatPointZero((GameClientState.takenMemory / 1024) / 1024.0) + "mb, total " + StringTools.formatPointZero((GameClientState.totalMemory / 1024) / 1024.0) + "mb; " + GLFrame.currentMemString;
			lastUpdate = System.currentTimeMillis();
		}
		infoList.add(2, memoryInfo + " (ECM: " + ElementCollectionMesh.meshesInUse + ")");
		if(EngineSettings.S_INFO_DRAW.getObject() == InfoSetting.ALL_INFO) {
			infoList.add(3, "[CL SEG A/F; D; [VRAM-save]] " + GameClientState.lastAllocatedSegmentData + " / " + GameClientState.lastFreeSegmentData + " (CMI: " + SegmentDrawer.dataPool.stats() + "; sc" + state.getWorldDrawer().getSegmentDrawer().getSegmentControllers().size() + "; q" + state.getWorldDrawer().getSegmentDrawer().getQueueSize() + ") " + GameClientState.drawnSegements + "; [VBO: " + (GameClientState.realVBOSize / 1024) / 1024 + "mb] " + CubeData.drawnMeshes + "; Res: " + StringTools.formatPointZero(GameClientState.dataReceived / 1000000f) + "MB; avgL: " + StringTools.formatPointZero(GameClientState.avgBlockLightTime) + "; occ: " + SegmentOcclusion.occluded + "/" + SegmentOcclusion.total + "[f: " + SegmentOcclusion.failed + "]");
		} else {
			infoList.add(3, "[CL SEG] " + GameClientState.lastAllocatedSegmentData + " / " + GameClientState.lastFreeSegmentData + " (CMI: " + SegmentDrawer.dataPool.stats() + "; sc" + state.getWorldDrawer().getSegmentDrawer().getSegmentControllers().size() + "; q" + state.getWorldDrawer().getSegmentDrawer().getQueueSize() + ")");
		}

		if(EngineSettings.S_INFO_DRAW.getObject() == InfoSetting.ALL_INFO) {
			infoList.add(4, "[SE SEG] " + GameServerState.lastAllocatedSegmentData + " / " + GameServerState.lastFreeSegmentData + "; " + ServerProcessor.totalPackagesQueued + "; " + GameServerState.activeSectorCount + "/" + GameServerState.totalSectorCount + "; REQ: " + GameServerState.segmentRequestQueue + "; DC: " + GameServerState.totalDockingChecks + "; Res: " + GameServerState.dataReceived / 1000000f + "MB // PathCalc: " + AbstractPathFindingHandler.path_in_calc + "; " + AbstractPathFindingHandler.currentIt);
		} else {
			infoList.add(4, "[CHUNK loaded/pool; SECTOR active/total;] " + GameServerState.lastAllocatedSegmentData + " / " + GameServerState.lastFreeSegmentData + "; " + GameServerState.activeSectorCount + "/" + GameServerState.totalSectorCount + "; \nfirstStageCalls: nonemptypretest: " + ServerSegmentProvider.firstStages + ", empty: " + ServerSegmentProvider.predeteminedEmpty);
		}
		if(EngineSettings.S_INFO_DRAW.getObject() == InfoSetting.ALL_INFO) {
			infoList.add("[SHOPS] NEAREST: " + state.getCurrentClosestShop() + "; ");
			if(state.getCurrentPlayerObject() != null && state.getCurrentPlayerObject() instanceof ShopperInterface) {
				infoList.add("[SHOPS] IN RANGE: " + ((ShopperInterface) state.getCurrentPlayerObject()).getShopsInDistance() + "; ");
			}
		}
		GameClientState.drawnSegements = 0;
		CubeData.drawnMeshes = 0;
		//		infoList.add(3,"[PHYSICS] shapes: "+ManagedStaticDrawableSegment.physicsBoxes);
		//		infoList.add("Cube Buffers intialized: "+CubeOptOptMesh.initializedBuffers);
		//		infoList.add("Occluded : Visible  "+CubeOptOptMesh.occludedMeshes+" : "+CubeOptOptMesh.visibleMeshes+"   / "+(CubeOptOptMesh.occludedMeshes+CubeOptOptMesh.visibleMeshes));
		for(Entry<String, Long> e : TimeStatistics.timer.entrySet()) {
			infoList.add(e.getKey() + ": " + (float) e.getValue());
		}
		if(Controller.getCamera() != null) {
			infoList.add("++CAM:   " + Controller.getCamera().getClass().getSimpleName() + ": " + Controller.getCamera().getPos());
		}

		state.getWorldDrawer().update(timer);
	}

	public static boolean drawBloomedEffects() {
		return EngineSettings.PLUME_BLOOM.isOn() && isFBOOn();
	}

	private void framebufferPass(int occulusMode) throws GraphicsException {
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0, 0, 0, 0);
		boolean sc = screenChanged;
		if(fbo == null || sc) {
			createFrameBuffers();
			initializeFrameBuffers();
		}

		if(EngineSettings.isShadowOn()) {
			if(shadow == null || sc) {
				shadow = new Shadow(state);
				shadow.init();
			}

			GlUtil.glPushMatrix();
			getMainLight().draw();
			shadow.makeShadowMap(state.getGraphicsContext().timer);
			GlUtil.glPopMatrix();
		}
		if(sc) {
			screenChanged = false;
		}
		try {
			state.getWorldDrawer().getStarSky().createField();
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
		} catch(GLException e1) {
			e1.printStackTrace();
		}

		/*
		 * DRAW BACKGROUND (STARS etc)
		 */

		backgroundFbo.enable();
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		for(FrameBufferDrawListener fbdl : FastListenerCommon.frameBufferDrawListeners) {
			fbdl.preDrawStarSky(this);
		}

		if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive() && !WorldDrawer.flagEntityRender) {
			state.getWorldDrawer().getStarSky().drawStars(false);
			state.getWorldDrawer().getStarSky().drawStars(true);
		}
		backgroundFbo.disable();

		depthBuffer.createDepthTexture();

		/*
		 * DRAW FOREGROUND (Models, Structures, etc)
		 */
		foregroundFbo.enable();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		TimeStatistics.reset("SCENE-DRAW");
		state.getWorldDrawer().currentFBO = foregroundFbo;

		drawScene();

		TimeStatistics.set("SCENE-DRAW");
		foregroundFbo.disable();




		/*
		 * COMPOSE SILOUETTE (draw sun fbo as white and forground fbo as black)
		 */

		if(drawBloomedEffects()) {
			//make silhouette texture
			silhouetteFboPlumes.enable();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			GL11.glDepthRange(0.0, 1.0);

			ShaderLibrary.silhouetteAlpha.load();
			if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
				state.getWorldDrawer().prepareCamera();

				for(FrameBufferDrawListener fbdl : FastListenerCommon.frameBufferDrawListeners) {
					fbdl.preProjectileDraw(this);
				}

				state.getWorldDrawer().getPlumAndMuzzleDrawer().drawRaw();
				state.getWorldDrawer().getFlareDrawerManager().drawRaw();

				state.getWorldDrawer().drawProjectiles(1f);
				state.getWorldDrawer().drawPointExplosions();
				state.getWorldDrawer().getBeamDrawerManager().draw(1.0f);

				GlUtil.glMatrixMode(GL11.GL_PROJECTION);
				GlUtil.glPopMatrix();
				GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
			}

			silhouetteFboPlumes.disable();
		}

		debugCheck();
		ShaderLibrary.silhouetteShader2D.setShaderInterface(silhouetteShader2D);
		debugCheck();
		//make silhouette texture
		silhouetteFbo.enable();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		silhouetteShader2D.setTextureId(foregroundFbo.getTexture());
		debugCheck();
		silhouetteShader2D.color.set(1, 1, 1, 1);
		ShaderLibrary.silhouetteShader2D.load();
		if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
			FrameBufferDrawListener.firePreGodRaysDraw(this);
			if(FrameBufferDrawListener.drawSphereForGodRays) {
				state.getWorldDrawer().drawSunBallForGodRays();
			}
		}

		debugCheck();
		ShaderLibrary.silhouetteShader2D.unload();
		debugCheck();
		GlUtil.glEnable(GL11.GL_BLEND);
		debugCheck();
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		debugCheck();
		silhouetteShader2D.color.set(0, 0, 0, 1);
		debugCheck();
		ShaderLibrary.silhouetteShader2D.load();
		debugCheck();
		foregroundFbo.draw(OculusVrHelper.OCCULUS_NONE);
		debugCheck();
		ShaderLibrary.silhouetteShader2D.unload();
		debugCheck();
		GlUtil.glDisable(GL11.GL_BLEND);
		debugCheck();
		silhouetteFbo.disable();
		debugCheck();

		if(EngineSettings.isShadowOn() && (EngineSettings.G_SHADOW_DISPLAY_SHADOW_MAP.isOn() || (state.isDebugKeyDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_MINUS)))) {
			shadow.showDepthTex();
			return;
		}

		if(EngineSettings.F_BLOOM.isOn()) {
			debugCheck();
			try {
				bloomPass(occulusMode); //This throws sometimes when the window is minimized, why?
			} catch(NullPointerException exception) {
				exception.printStackTrace();
			}
			debugCheck();
		} else {
			state.getWorldDrawer().currentFBO = fbo;
			fbo.enable();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			try {

				if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
					GlUtil.glPushMatrix();
					state.getWorldDrawer().getStarSky().drawBackground();
					GlUtil.glPopMatrix();
				}

			} catch(GLException e) {
				e.printStackTrace();
			}
			if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
				state.getWorldDrawer().getStarSky().drawStars(false);
				state.getWorldDrawer().getStarSky().drawStars(true);
			}
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);

			debugCheck();
			foregroundFbo.draw(OculusVrHelper.OCCULUS_NONE);
//			foregroundFbo.drawDepthTexture(0);
			debugCheck();

			GlUtil.glDisable(GL11.GL_BLEND);
			fbo.disable();

			state.getWorldDrawer().drawAdditional(foregroundFbo, fbo, depthBuffer);

			if(occulusMode != 0) {

				occFBO.enable();
				if(occulusMode == OculusVrHelper.OCCULUS_LEFT) {
					GL11.glClearColor(0, 0, 0, 1);
				}

				int rtFullWidth = GLFrame.getWidth();
				int rtFullHeight = GLFrame.getHeight();

				rtFullWidth *= OculusVrHelper.getScaleFactor();
				rtFullHeight *= OculusVrHelper.getScaleFactor();

				//	    		if (occulusMode == OculusVrHelper.OCCULUS_LEFT){
				//	    			fbo.renderFullscreenQuad(0, 0, rtFullWidth / 2, rtFullHeight);
				//				}else if (occulusMode == OculusVrHelper.OCCULUS_RIGHT){
				//					fbo.renderFullscreenQuad(rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight);
				//				}
				debugCheck();
				fbo.draw(occulusMode);
				debugCheck();
				occFBO.disable();
				debugCheck();
				//Rigth is second pass -> render fully made vbo
				if(occulusMode == OculusVrHelper.OCCULUS_RIGHT) {
					OculusDistortionShader s = new OculusDistortionShader();
					s.setFBO(OculusVrHelper.OCCULUS_LEFT);
					ShaderLibrary.ocDistortion.setShaderInterface(s);
					occFBO.drawOC(ShaderLibrary.ocDistortion, 0);

					s.setFBO(OculusVrHelper.OCCULUS_RIGHT);
					occFBO.drawOC(ShaderLibrary.ocDistortion, 0);

				}
			} else {
				try {
					assert (this.bloomPointPass != null);
					debugCheck();
					//bloomPointPass is for the bloom of thrusters, etc
					FrameBufferObjects blurOutput = this.bloomPointPass.blur(fbo);
					debugCheck();
					state.getWorldDrawer().currentFBO = blurOutput;
					ShaderLibrary.gamma.setShaderInterface(gammaShader);
					ShaderLibrary.gamma.load();
					if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F11)) {
						silhouetteFboPlumes.draw(occulusMode);
					} else {
						blurOutput.draw(occulusMode);
					}
					ShaderLibrary.gamma.unload();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			state.getWorldDrawer().drawGUI();
		}
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	public void debugCheck() {
		if(debug) {
			GlUtil.printGlErrorCritical();
		}
	}

	/**
	 * @param state the state to set
	 */
	public void setState(GameClientState state) {
		this.state = state;
	}

	/**
	 * @return the worldToScreenConverter
	 */
	public WorldToScreenConverter getWorldToScreenConverter() {
		return worldToScreenConverter;
	}

	private void initializeFrameBuffers() {
		try {
			fbo.setWithDepthAttachment(false);
			fbo.initialize();

			backgroundFbo.setReuseDepthBuffer(fbo.getDepthRenderBufferId());
			backgroundFbo.initialize();

			silhouetteFbo.setReuseDepthBuffer(fbo.getDepthRenderBufferId());
			silhouetteFbo.initialize();

			foregroundFbo.setWithDepthTexture(true);
			foregroundFbo.multisampled = GraphicsContext.MULTISAMPLES > 0;
			foregroundFbo.initialize();

			assert (foregroundFbo.getDepthTextureID() != 0);
			silhouetteFboPlumes.setReuseDepthTexture(foregroundFbo.getDepthTextureID());
			silhouetteFboPlumes.initialize();

			assert (silhouetteFboPlumes.getDepthTextureID() == foregroundFbo.getDepthTextureID());

			if(occFBO != null) {
				occFBO.initialize();
			}
			bloomPointPass.setReuseRenderDepthBuffer(fbo.getDepthRenderBufferId());
			bloomPointPass.initialize();

			GlUtil.printGlErrorCritical();
		} catch(GLException e) {

			e.printStackTrace();
		}
	}

	private void normalPass() {
		//		TimeStatistics.reset("SKY-DRAW");
		//		AbstractScene.infoList.add("FBO OFF");
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		state.getWorldDrawer().getStarSky().draw();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		//		TimeStatistics.set("SKY-DRAW");

		TimeStatistics.reset("SCENE-DRAW");
		drawScene();
		TimeStatistics.set("SCENE-DRAW");

		state.getWorldDrawer().drawGUI();

	}

	@Override
	public void println(String s) {
		System.err.println(s);

	}

	public void updateCurrentCamera(Timer timer) {
		debug = Keyboard.isKeyDown(GLFW.GLFW_KEY_F2);
//		System.err.println("UPDATE CURRENT CAM "+Mouse.getDX()+"; "+Mouse.getDY());
		Controller.getCamera().update(timer, false);
	}

	private class FromMeComp implements Comparator<SimpleTransformable> {
		Vector3f where;
		Vector3f aTmp = new Vector3f();
		Vector3f bTmp = new Vector3f();

		@Override
		public int compare(SimpleTransformable o1, SimpleTransformable o2) {
			aTmp.sub(o1.getWorldTransformOnClient().origin, where);
			bTmp.sub(o2.getWorldTransformOnClient().origin, where);
			return Float.compare(aTmp.lengthSquared(), bTmp.lengthSquared());
		}
	}

	@Override
	public void onWindowSizeChanged(int width, int height) {
		screenChanged = true;
	}

}
