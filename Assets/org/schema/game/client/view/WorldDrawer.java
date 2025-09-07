package org.schema.game.client.view;

import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SystemDrawListener;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import api.utils.draw.ModWorldDrawer;
import api.utils.particle.ModParticleUtil;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.TimeStatistics;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientSectorChangeListener;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.client.view.beam.BeamDrawerManager;
import org.schema.game.client.view.blackholedrawer.BlackHoleDrawer;
import org.schema.game.client.view.camera.drone.CameraDroneManager;
import org.schema.game.client.view.character.CharactersDrawer;
import org.schema.game.client.view.creaturetool.CreatureTool;
import org.schema.game.client.view.cubes.CubeBruteCollectionDrawer;
import org.schema.game.client.view.effects.*;
import org.schema.game.client.view.effects.segmentcontrollereffects.SegmentControllerEffectDrawer;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gui.GUI3DBlockElement;
import org.schema.game.client.view.gui.GUIBlockConsistenceGraph;
import org.schema.game.client.view.gui.GuiDrawer;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.mainmenu.gui.screenshotviewer.ScreenshotManager;
import org.schema.game.client.view.mines.MineDrawer;
import org.schema.game.client.view.occulus.OculusUtil;
import org.schema.game.client.view.planetdrawer.PlanetDrawer;
import org.schema.game.client.view.planetgas.GasPlanetAtmosphereInnerDrawer;
import org.schema.game.client.view.planetgas.GasPlanetAtmosphereOuterDrawer;
import org.schema.game.client.view.planetgas.GasPlanetSurfaceDrawer;
import org.schema.game.client.view.shards.Shard;
import org.schema.game.client.view.shards.ShardDrawer;
import org.schema.game.client.view.space.StarSkyNew;
import org.schema.game.client.view.sundrawer.SunDrawer;
import org.schema.game.client.view.tools.IconTextureBakery;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.physics.RigidDebrisBody;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.planet.gasgiant.GasPlanetInformation;
import org.schema.game.common.data.world.planet.old.Planet;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.graphicsengine.OculusVrHelper;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.ShaderLibrary.CubeShaderType;
import org.schema.schine.graphicsengine.util.GifEncoder;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.physics.Physical;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
//import org.schema.game.client.view.planetnewtest.PlanetNew;

public class WorldDrawer extends AbstractSceneNode implements ClientSectorChangeListener, ScreenChangeCallback {
	//INSERTED CODE
	public static final ConcurrentLinkedQueue<Runnable> runQueue = new ConcurrentLinkedQueue<>();
	static final long STRUCTURE_BLINKING_MS = 1200;
	public static boolean insideBuildMode;
	public static boolean flagTextureBake = false;
	public static boolean flagScreenShotWithGUI = false;
	public static boolean flagScreenShotWithoutGUI = false;
	public static boolean flagCreatureTool;
	public static boolean flagRecipeTrees;
	public static boolean flagAreaDefineDrawer;
	public static boolean drawError;
	public static boolean flagEntityRender;
	public static int segmentControllerRenderId;
	static SegmentController justEntered;
	static long justEnteredStart;
	private final LiftDrawer liftDrawer;
	private final PlumeAndMuzzleDrawer plumAndMuzzleDrawer;
	private final BuildModeDrawer buildModeDrawer;
	private final CharactersDrawer characterDrawer;
	private final TransporterEffectManager transporterEffectManager;
	private final BeamDrawerManager beamDrawerManager;
	private final ShieldDrawerManager shieldDrawerManager;
	private final FlareDrawerManager flareDrawerManager;
	private final CubeBruteCollectionDrawer cubeBruteCollectionDrawer = new CubeBruteCollectionDrawer();
	private final ShardDrawer shards = new ShardDrawer();
	private final AreaDefineDrawer areaDefineDrawer = new AreaDefineDrawer();
	private final GameClientState state;
	private final Atmosphere atmosphere;
	private final StarSkyNew starSky;
	private final PlanetCoreDrawer planetCoreDrawer;
	private final GasPlanetSurfaceDrawer gasPlanetSurfaceDrawer;
	private final GasPlanetAtmosphereOuterDrawer gasPlanetAtmosphereOuterDrawer;
	private final GasPlanetAtmosphereInnerDrawer gasPlanetAtmosphereInnerDrawer;
	private final ExplosionDrawer explosionDrawer;
	private final SpaceParticleDrawer spaceParticleDrawer;
	private final MissileTrailDrawer trailDrawer;
	private final List<ProjectileCannonDrawerVBO> projectileDrawers;
	private final BlackHoleDrawer blackHoleDrawer;
	private final SunDrawer sunDrawer;
	private final SunDrawer sunDistDrawer;
	private final SunDrawer sunDrawerSec;
	private final ConnectionDrawerManager connectionDrawerManager;
	private final EnergyStreamDrawerManager energyStreamDrawerManager;
	private final SegmentDrawer segmentDrawer;
	private final CameraDroneManager cameraDroneManager;
	//	private final NebulaDrawer nebulaDrawer;
	public Shadow shadow;
	public GifEncoder gifEncoder;
	//	public final Galaxy testGalaxy = new Galaxy(System.currentTimeMillis());
	public Vector3f secondSunLightPos;
	public Vector4f sunColor = new Vector4f();
	public Vector4f secondSunColor = new Vector4f();
	public boolean initialized;
	private boolean flagDrawWireframe;
	//	public PlanetNew pNew;
	//	private SegmentDrawer planetDrawer;
	long diffAcc;
	Transform tmp = new Transform();
	//needed for readPixels
	FrameBufferObjects currentFBO;
	boolean first = true;
	Transform tmpTrans = new Transform();
	Vector3f absSectorPos = new Vector3f();
	Matrix3f rot = new Matrix3f();
	private GameMapDrawer gameMapDrawer;
	private OculusUtil oculusUtil = new OculusUtil();
	private SegmentControllerEffectDrawer segmentControllerEffectDrawer;
	private SinusTimerUtil arrowSineUtil = new SinusTimerUtil(9f);
	private PlanetDrawer planetDrawer;
	private GuiDrawer guiDrawer;
	private CreatureTool creatureTool;
	private IconTextureBakery bakery;
	private ItemDrawer itemDrawer;
	private Integer[] specialSectorsBuffer;
	private Integer[] specialSectors = new Integer[0];
	private int drawingSpecialSectorsId = -2;
	private Vector3f absoluteMousePosition = new Vector3f();
	private boolean flagManagedSegmentControllerUpdate;
	private boolean flagSegmentControllerUpdate;
	private boolean flagCharacterUpdate;
	private boolean wasOnPlanet;
	private boolean flagPlanetCoreUpdate;
	private Vector3i tmpSysPos = new Vector3i();
	private Vector3i tmpSecPos = new Vector3i();
	private Vector3i tmpSecPosCurrent = new Vector3i();
	private Vector3i extPos = new Vector3i();
	private Vector3i extOffset = new Vector3i();
	private Vector3i extPosMod = new Vector3i();
	private List<Drawable> drawables = new ObjectArrayList<Drawable>();
	private boolean planetTest;
	private int i;
	private MineDrawer mineDrawer;
	//INSERTED CODE
	private ArrayList<ModWorldDrawer> modDrawables = new ArrayList<ModWorldDrawer>();
	private boolean screenChanged;
	private static Transform cachedCamera;

	///
	public WorldDrawer(GameClientState state) {
		this.state = state;
		projectileDrawers = new ObjectArrayList<ProjectileCannonDrawerVBO>();
		drawables.add(beamDrawerManager = new BeamDrawerManager(state));
		drawables.add(segmentDrawer = new SegmentDrawer(state));
		drawables.add(shieldDrawerManager = new ShieldDrawerManager(state));
		drawables.add(flareDrawerManager = new FlareDrawerManager());
		drawables.add(buildModeDrawer = new BuildModeDrawer(state));
		drawables.add(gameMapDrawer = new GameMapDrawer(state));
		drawables.add(liftDrawer = new LiftDrawer());
		drawables.add(guiDrawer = new GuiDrawer(state.getGUIController()));
		drawables.add(planetDrawer = new PlanetDrawer(state));
		drawables.add(blackHoleDrawer = new BlackHoleDrawer());
		drawables.add(sunDrawer = new SunDrawer(state));
		drawables.add(sunDistDrawer = new SunDrawer(state));
		drawables.add(sunDrawerSec = new SunDrawer(state));
		drawables.add(starSky = new StarSkyNew(state));
		drawables.add(transporterEffectManager = new TransporterEffectManager(state));
		drawables.add(characterDrawer = new CharactersDrawer(state));
		drawables.add(plumAndMuzzleDrawer = new PlumeAndMuzzleDrawer());
		drawables.add(explosionDrawer = new ExplosionDrawer());
		drawables.add(planetCoreDrawer = new PlanetCoreDrawer());
		drawables.add(spaceParticleDrawer = new SpaceParticleDrawer());
		drawables.add(segmentControllerEffectDrawer = new SegmentControllerEffectDrawer(state));
		drawables.add(trailDrawer = new MissileTrailDrawer(state));
		drawables.add(connectionDrawerManager = new ConnectionDrawerManager(state));
		drawables.add(energyStreamDrawerManager = new EnergyStreamDrawerManager(state));
		drawables.add(itemDrawer = new ItemDrawer(state));
		drawables.add(atmosphere = new Atmosphere(state));
		drawables.add(cameraDroneManager = new CameraDroneManager(state));
		drawables.add(mineDrawer = new MineDrawer(state));
		drawables.add(gasPlanetAtmosphereOuterDrawer = new GasPlanetAtmosphereOuterDrawer(state));
		drawables.add(gasPlanetAtmosphereInnerDrawer = new GasPlanetAtmosphereInnerDrawer(state));
		drawables.add(gasPlanetSurfaceDrawer = new GasPlanetSurfaceDrawer(state));
		ProjectileCannonDrawerVBO projectileDrawerVBO = new ProjectileCannonDrawerVBO((state).getParticleController());
		projectileDrawers.add(projectileDrawerVBO);
		drawables.add(projectileDrawerVBO);
		//INSERTED CODE
		RegisterWorldDrawersEvent event = new RegisterWorldDrawersEvent(this);
		StarLoader.fireEvent(event, false);
		this.drawables.addAll(event.getModDrawables());
		this.modDrawables.addAll(event.getModDrawables());
		///
		state.getController().addSectorChangeListener(this);
		GraphicsContext.current.registerScreenChangeCallback(this);
	}

	public static void flagEntityRender(SegmentController currentControl) {
		flagEntityRender = true;
		segmentControllerRenderId = currentControl.getId();
		cachedCamera = Controller.getCamera().getWorldTransform();
		Transform temp = new Transform(currentControl.getWorldTransform());
		Controller.getCamera().reset();
		temp.basis.set(Controller.getCamera().lookAt(false).basis);
		temp.basis.invert();
		temp.basis.rotY(-(FastMath.HALF_PI + FastMath.QUARTER_PI));
		BoundingBox boundingBox = currentControl.getBoundingBox();
		Controller.getCamera().setCameraOffset(boundingBox.getSize());
		Controller.getCamera().getWorldTransform().set(temp);
	}

	public void toggleWireframe() {
		flagDrawWireframe = !flagDrawWireframe;
	}

	public static void processRunQueue() {
		if(!runQueue.isEmpty()) {
			for(Runnable runnable : runQueue) {
				try {
					runnable.run();
				} catch(Exception e) {
					System.err.println("[StarLoader] [WorldDrawer] Exception in graphics run queue");
					e.printStackTrace();
				}
			}
			runQueue.clear();
		}
	}

	public AreaDefineDrawer getAreaDefineDrawer() {
		return areaDefineDrawer;
	}

	//INSERTED CODE
	public ItemDrawer _getItemDrawer() {
		return itemDrawer;
	}

	public void _setItemDrawer(ItemDrawer itemDrawer) {
		this.itemDrawer = itemDrawer;
	}

	public List<Drawable> _getDrawables() {
		return drawables;
	}

	///
	@Override
	public void cleanUp() {
		for(Drawable d : drawables) {
			d.cleanUp();
		}
		GraphicsContext.current.unregisterScreenChangeCallback(this);
	}

	public void prepareCamera() {
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPushMatrix();
		float aspect = (float) GLFrame.getWidth() / GLFrame.getHeight(); //1.333333333333333333333333f#
		if(!EngineSettings.O_OCULUS_RENDERING.isOn()) {
			GlUtil.gluPerspective(Controller.projectionMatrix, EngineSettings.G_FOV.getFloat() * AbstractScene.getZoomFactorForRender(!gameMapDrawer.isMapActive()), aspect, 0.05f, state.getSectorSize() * 2, true);
		} else {
			Matrix4f gluPerspective = GlUtil.gluPerspective(Controller.projectionMatrix, EngineSettings.G_FOV.getFloat() * AbstractScene.getZoomFactorForRender(!gameMapDrawer.isMapActive()), OculusVrHelper.getAspectRatio(), 0.05f, state.getSectorSize() * 2, true);
			Matrix4fTools.mul(Controller.occulusProjMatrix, gluPerspective, gluPerspective);
			GlUtil.glLoadMatrix(gluPerspective);
		}
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		Controller.getCamera().updateFrustum();
	}

	public void retrieveMousePosition() {
		if(PlayerInteractionControlManager.isAdvancedBuildMode(state) && isInStructureBuildMode()) {
			absoluteMousePosition = AbstractScene.getAbsoluteMousePosition(absoluteMousePosition);
		}
	}

	///
	@Override
	public void draw() {
		//INSERTED CODE
		StarRunnable.tickAll(true);
		///
		PlanetDrawer.culled = 0;
		drawError = false;
		if(first || Keyboard.isKeyDown(GLFW.GLFW_KEY_F2) || (i % 10000 == 0)) {
			//Print error once without exception. then continue to print it again with exception
			//is it happens again to pinpoint where it is
			GlUtil.printGlError();
			drawError = true;
			first = false;
		}
		i++;
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		if(EngineSettings.D_INFO_CONTROLMANAGER.isOn()){
		//			state.getGlobalGameControlManager().printActive(0);
		//		}
		SectorInformation.PlanetType ownType = null;
		boolean drawAtmosphereLater = false;
		Vector3i relSys = Galaxy.getRelPosInGalaxyFromAbsSystem(tmpSysPos, new Vector3i());
		if(!state.getCurrentGalaxy().isVoid(relSys.x, relSys.y, relSys.z)) {
			sunDrawer.drawPlasma();
		}
		segmentControllerEffectDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		if(state.getPlayer().getProximitySector().getSectorId() != drawingSpecialSectorsId) {
			synchronized(state.getPlayer().getProximitySector()) {
				drawingSpecialSectorsId = state.getPlayer().getProximitySector().getSectorId();
				updateSpecialSectors();
			}
		}
		if(specialSectorsBuffer != null) {
			specialSectors = specialSectorsBuffer;
			specialSectorsBuffer = null;
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		tmp.setIdentity();
		Vector3i secPos = new Vector3i();
		ClientProximitySector clientPxSec = state.getPlayer().getProximitySector();
		boolean onPlanet = false;
		long startTime = state.getController().calculateStartTime();
		float pc = state.getGameState().getRotationProgession();

		//INSERTED CODE
		for(ModWorldDrawer drawable : this.modDrawables) {
			drawable.preCameraPrepare();
		}
		///

		int lastMax = 0;
		GasPlanetSurfaceDrawer.clearGasGiants();
		for(int i = 0; i < specialSectors.length; i++) {
			int secIndex = specialSectors[i];
			state.getPlayer().getProximitySector().getPosFromIndex(secIndex, secPos);
			SectorInformation.SectorType sType = SectorInformation.SectorType.values()[clientPxSec.getSectorType(secIndex)];
			if(sType == SectorInformation.SectorType.PLANET) {
				SectorInformation.PlanetType pType = SectorInformation.PlanetType.values()[clientPxSec.getPlanetType(secIndex)];
				//				System.err.println("PLANET SECTOR: "+secIndex+"; "+secPos);
				if(state.getPlayer().getCurrentSector().equals(secPos)) {
					ownType = pType;
					atmosphere.setPlanetType(ownType);
					drawAtmosphereLater = atmosphere.isInside();
					onPlanet = true;
				} else {
					Vector3i pPos = new Vector3i(secPos);
					pPos.sub(state.getPlayer().getCurrentSector());
					tmp.basis.setIdentity();
					Vector3i sysPos = StellarSystem.getPosFromSector(secPos, new Vector3i());
					//					if(StellarSystem.isStarSystem(secPos)){
					planetDrawer.drawFromPlanet = onPlanet || wasOnPlanet;
					planetDrawer.year = pc;
					planetDrawer.setPlanetSectorPos(pPos);
					planetDrawer.setPlanetType(pType); //TODO replace with planet-specific details from new system
					planetDrawer.absSecPos = secPos;
					planetDrawer.draw();
					GlUtil.glColor4f(1, 1, 1, 1);
				}
			} else if(sType == SectorInformation.SectorType.GAS_PLANET) {
				VoidSystem system = state.getController().getClientChannel().getGalaxyManagerClient().getSystemOnClient(secPos);
				if(system != null) {
					try {
						GasPlanetInformation g = clientPxSec.getGasPlanetInfo(secIndex);
						if(g != null) GasPlanetSurfaceDrawer.addGasGiant(g.getLocation(), g);
					} catch(Exception e) {
						e.printStackTrace(); //these exceptions are caught in case of unfocused window, so have to manually log them else they vanish. TODO: fix that.
						throw e;
					}
				}
			}
		}
		gasPlanetSurfaceDrawer.drawGasGiants(false); //long-distance draw
//		gasPlanetAtmosphereOuterDrawer.drawAtmospheres(false);

		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		wasOnPlanet = onPlanet;
		starSky.setDrawFromPlanet(onPlanet);
		starSky.setYear(pc);
		GL11.glDepthRange(0.0, 1.0);
		//		if(state.getPlayer().getCurrentSector().equals(0, 0, 0)){
		//			atmosphere.draw();
		//		}
		//		planetDrawer.draw();
		this.prepareCamera();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDepthRange(0.0, 1.0);
		//		int r = 40;
		//		EllipsoidBuildHelper.drawEllipsoid(r*4, r*4, r, 0, r);
		if(onPlanet && !drawAtmosphereLater) {
			atmosphere.draw();
		}
		//		AbstractScene.infoList.add("CUURENT OBJ: "+state.getCurrentPlayerObject());
		if(shadow == null) {
			this.characterDrawer.draw();
			if(creatureTool != null) {
				creatureTool.draw();
			}
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		if(flagDrawWireframe) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		long tt = System.currentTimeMillis();
		connectionDrawerManager.draw();
		energyStreamDrawerManager.draw();
		long took = System.currentTimeMillis() - tt;
		if(took > 30) {
			System.err.println("[CLIENT] CONNECTION DRAWING TOOK " + took);
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		liftDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		if(!EngineSettings.F_FRAME_BUFFER.isOn()) {
		drawProjectiles(0.0f);
		//		}
		mineDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		if(!EngineSettings.F_FRAME_BUFFER.isOn()) {
		beamDrawerManager.prepareSorted();
		//		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		beamDrawerManager.drawSalvageBoxes();
		TimeStatistics.reset("SEGMENTS");
		//		GlUtil.glDisable(GL11.GL_LIGHTING);
		//		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		if(shadow != null) {
			shadow.renderScene();
		} else {
			segmentDrawer.setSegmentRenderPass(SegmentDrawer.SegmentRenderPass.OPAQUE);
			segmentDrawer.draw();
			shards.draw();
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		segmentDrawer.drawTextBoxes();
		state.getParticleSystemManager().draw();
		TimeStatistics.set("SEGMENTS");
		cubeBruteCollectionDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		itemDrawer.draw();
		cameraDroneManager.draw();
		if(flagDrawWireframe) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		explosionDrawer.drawShieldBubbled();
//		nebulaDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		try {
			if(justEntered != null && System.currentTimeMillis() - justEnteredStart < STRUCTURE_BLINKING_MS && state.getGlobalGameControlManager() != null && state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive()) {
				if(justEntered.blinkTime == 0) {
					justEntered.blinkTime = System.currentTimeMillis();
				}
				int t = (int) (System.currentTimeMillis() - justEnteredStart);
				GlUtil.glPushMatrix();
				Mesh mesh = (Mesh) Controller.getResLoader().getMesh("Arrow").getChilds().get(0);
				Transform tran = new Transform(justEntered.getWorldTransform());
				Vector3f f = new Vector3f(0, 0, 1);//GlUtil.getForwardVector(new Vector3f(), tran);
				f.scale(arrowSineUtil.getTime());
				tran.basis.transform(f);
				tran.origin.add(f);
				GlUtil.glMultMatrix(tran);
				//				GlUtil.translateModelview(f.x, f.y, f.z);
				GlUtil.scaleModelview(0.3f, 0.3f, 0.3f);
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glColor4f(1, 1, 1, arrowSineUtil.getTime());
				mesh.draw();
				GlUtil.glPopMatrix();
				GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glColor4f(1, 1, 1, 1);
			} else {
				justEntered = null;
				justEnteredStart = 0;
			}
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		if(drawAtmosphereLater) {
			atmosphere.draw();
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		plumAndMuzzleDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		//		getBeamDrawerManager().draw();
		//		TimeStatistics.reset("sparticles");
		spaceParticleDrawer.onPlanet(atmosphere.isInside());
		spaceParticleDrawer.draw();
		//		testGalaxy.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		state.getPulseController().draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		flareDrawerManager.draw();
		//		TimeStatistics.set("sparticles");
		//		GlUtil.printGlErrorCritical();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		shieldDrawerManager.draw();
		//		GlUtil.printGlErrorCritical();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		planetCoreDrawer.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		transporterEffectManager.draw();
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		gasPlanetSurfaceDrawer.drawGasGiants(true);
		gasPlanetAtmosphereOuterDrawer.drawAtmospheres(true);
		gasPlanetAtmosphereInnerDrawer.draw();
		trailDrawer.draw();
		//INSERTED CODE
		ModParticleUtil.drawAll();
		for(ModWorldDrawer drawable : this.modDrawables) {
			drawable.postWorldDraw();
			drawable.draw();
		}
		///
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		//draw own planet if we are outside of its athmosphere
		if(ownType != null && !drawAtmosphereLater) {
			//			System.err.println("DRAW PLANET");
			planetDrawer.drawFromPlanet = false;//onPlanet;
			planetDrawer.setPlanetSectorPos(new Vector3i(0, 0, 0));
			planetDrawer.setPlanetType(ownType);
			planetDrawer.draw();
			GlUtil.glColor4f(1, 1, 1, 1);
		}
		boolean gameMapDraw = gameMapDrawer.doDraw();
		if(gameMapDraw) {
			gameMapDrawer.draw();
			//INSERTED CODE
			for(ModWorldDrawer drawable : this.modDrawables) {
				drawable.postGameMapDraw();
			}
			///
		}
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		Controller.getCamera().updateFrustum();
		if(!gameMapDraw) {
			if(drawError) {
				GlUtil.printGlErrorCritical();
			}
			drawSystems(startTime, onPlanet);
		}
		this.prepareCamera();
		if(!gameMapDraw) {
			segmentDrawer.setSegmentRenderPass(SegmentDrawer.SegmentRenderPass.TRANSPARENT);
			segmentDrawer.draw();
			this.buildModeDrawer.draw();
		}
		if(planetTest) {
			//			pNew.draw();
		}
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		Controller.getCamera().updateFrustum();
		if(EngineSettings.P_PHYSICS_DEBUG_MODE.isOn()) {
			displayPhysicsInfo();
		}
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		SegmentDrawer.forceFullLightingUpdate = false;
		//		System.err.println("CulledPlanetrs: "+PlanetDrawer.culled);
	}

	public void drawPointExplosions() {
		explosionDrawer.drawPoints();
	}

	public void drawProjectiles(float zoom) {
		for(int i = 0; i < projectileDrawers.size(); i++) {
			projectileDrawers.get(i).setZoomFac(zoom);
			projectileDrawers.get(i).draw();
		}
	}

	@Override
	public void onInit() {
		for(Drawable d : drawables) {
			d.onInit();
		}
		this.initialized = true;
	}

	public void clearAll() {
		plumAndMuzzleDrawer.clear();
		shieldDrawerManager.clear();
		beamDrawerManager.clear();
		segmentDrawer.clearSegmentControllers();
		characterDrawer.clear();
		connectionDrawerManager.clear();
		energyStreamDrawerManager.clear();
	}

	@Override
	public AbstractSceneNode clone() {
		return null;
	}

	@Override
	public void update(Timer timer) {
		if(!initialized) {
			return;
		}
		TimeStatistics.reset("update");
		oculusUtil.update(timer);
		long t = System.currentTimeMillis();
		long taken = System.currentTimeMillis() - t;
		if(taken > 10) {
			System.err.println("[DRAWER][WARNING] synUPDATE took " + taken + " ms");
		}
		t = System.currentTimeMillis();
		if(flagSegmentControllerUpdate) {
			segmentDrawer.updateSegmentControllerSet();
			flagSegmentControllerUpdate = false;
		}
		shards.update(timer, state);
		connectionDrawerManager.update(timer);
		energyStreamDrawerManager.update(timer);
		if(flagCreatureTool) {
			if(creatureTool == null) {
				creatureTool = new CreatureTool(state, timer);
			} else {
				creatureTool.onDiasble();
				creatureTool = null;
			}
			flagCreatureTool = false;
		}
		mineDrawer.update(timer);
		segmentDrawer.textBox.update(timer);
		if(flagPlanetCoreUpdate) {
			planetCoreDrawer.setCore(null);
			for(Sendable s : state.getCurrentSectorEntities().values()) {
				if(s instanceof PlanetIcoCore && ((PlanetIcoCore) s).getSectorId() == state.getCurrentSectorId()) {
					//TODO maybe for neighbor sectors also
					planetCoreDrawer.setCore((PlanetIcoCore) s);
				}
			}
			flagPlanetCoreUpdate = false;
		}
		long taken1 = System.currentTimeMillis() - t;
		if(taken1 > 10) {
			System.err.println("[DRAWER][WARNING] seg controller set update took " + taken1 + " ms");
		}
		t = System.currentTimeMillis();
		if(flagManagedSegmentControllerUpdate) {
			segManControllerUpdate();
			flagManagedSegmentControllerUpdate = false;
		}
		long taken2 = System.currentTimeMillis() - t;
		if(taken2 > 10) {
			System.err.println("[DRAWER][WARNING] segManControllerUpdate took " + taken2 + " ms");
		}
		if(flagCharacterUpdate) {
			characterDrawer.updateCharacterSet(timer);
			flagCharacterUpdate = false;
		}
		arrowSineUtil.update(timer);
		starSky.update(timer);
		characterDrawer.update(timer);
		planetDrawer.update(timer);
		blackHoleDrawer.update(timer);
		sunDrawer.update(timer);
		sunDrawerSec.update(timer);
		transporterEffectManager.updateLocal(timer);
		gameMapDrawer.update(timer);
		gasPlanetAtmosphereInnerDrawer.update(timer);
		gasPlanetAtmosphereOuterDrawer.update(timer);
		gasPlanetSurfaceDrawer.update(timer);
//		nebulaDrawer.update(timer);
		//		AbstractScene.infoList.add("WRITING JOBS: "+ClientSegmentProvider.dbJobs);
		TimeStatistics.reset("update sDrawer");
		segmentDrawer.update(timer);
		TimeStatistics.set("update sDrawer");
		if(TimeStatistics.get("update sDrawer") > 15) {
			System.err.println("[DRAWER][WARNING] SegDrawer update took " + TimeStatistics.get("update sDrawer") + " ms");
		}
		explosionDrawer.update(timer);
		planetCoreDrawer.update(timer);
		//		TimeStatistics.reset("update sparticleDrawer");
		if(EngineSettings.G_SPACE_PARTICLE.isOn()) {
			spaceParticleDrawer.update(timer);
		}
		//		TimeStatistics.set("update sparticleDrawer");
		shieldDrawerManager.update(timer);
		flareDrawerManager.update(timer);
		plumAndMuzzleDrawer.update(timer);
		trailDrawer.update(timer);
		segmentControllerEffectDrawer.update(timer);
		//		TimeStatistics.reset("update guiDrawer");
		guiDrawer.update(timer);
		//		TimeStatistics.set("update guiDrawer");
		//		AbstractScene.infoList.add("Explosions: "+getExplosionDrawers().size());
		beamDrawerManager.update(timer);
		//INSERTED CODE
		for(ModWorldDrawer modDraw : this.modDrawables) {
			modDraw.update(timer);
		}
		///
		TimeStatistics.set("update");
		if(TimeStatistics.get("update") > 15) {
			System.err.println("[DRAWER][WARNING] update took " + TimeStatistics.get("update") + " ms");
		}
	}

	public void displayPhysicsInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("Physics Data (!!warning slow): ");
		ObjectArrayList<CollisionObject> collisionObjectArray = state.getPhysics().getDynamicsWorld().getCollisionObjectArray();
		for(CollisionObject d : collisionObjectArray) {
			sb.append(d.getCollisionShape().getClass().getSimpleName() + ": ");
			for(Sendable s : state.getCurrentSectorEntities().values()) {
				if(s instanceof Physical) {
					if(((Physical) s).getPhysicsDataContainer().getObject() == d) {
						sb.append(s);
					}
				}
			}
			sb.append("; \n");
		}
		AbstractScene.infoList.add(sb.toString());
	}

	private void doScreenShot() {
		File f = new FileExt("./screenshots/");
		if(!f.exists()) f.mkdirs();
		File[] listFiles = f.listFiles();
		int nr = 0;
		boolean through = true;
		while(through) {
			through = false;
			for(File en : listFiles) {
				if(en.getName().startsWith("starmade-screenshot-" + StringTools.formatFourZero(nr) + ".png")) {
					System.err.println("Screen Already Exists: ./screenshots/starmade-screenshot-" + StringTools.formatFourZero(nr) + ".png");
					nr++;
					through = true;
					break;
				}
			}
		}
		String s = "./screenshots/starmade-screenshot-" + StringTools.formatFourZero(nr);
		GlUtil.writeScreenToDisk(s, "png", GLFrame.getWidth(), GLFrame.getHeight(), 4, currentFBO);
		File file = new FileExt(s + ".png");
		if(file.exists()) {
			try {
				ScreenshotManager.addData(new ScreenshotManager.ScreenshotData(file));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void drawGUI() {
		if(flagAreaDefineDrawer) areaDefineDrawer.draw();

		GlUtil.glColor4f(1, 1, 1, 1);
		if(flagEntityRender) {
			resetEntityRender();
		}
		if(WorldDrawer.flagScreenShotWithoutGUI) {
			doScreenShot();
			WorldDrawer.flagScreenShotWithoutGUI = false;
		}
		if(gifEncoder != null && !EngineSettings.GIF_GUI.isOn()) {
			GlUtil.writeScreenToGif(gifEncoder, GLFrame.getWidth(), GLFrame.getHeight(), 4, currentFBO);
		}
		if(screenChanged) {
			if(guiDrawer != null) {
				guiDrawer.cleanUp();
				drawables.remove(guiDrawer);
			}
			guiDrawer = new GuiDrawer(state.getGUIController());
			guiDrawer.onInit();
			drawables.add(guiDrawer);
			screenChanged = false;
		}
		TimeStatistics.reset("GUI");
		guiDrawer.draw();
		TimeStatistics.set("GUI");
		if(gifEncoder != null && EngineSettings.GIF_GUI.isOn()) {
			GlUtil.writeScreenToGif(gifEncoder, GLFrame.getWidth(), GLFrame.getHeight(), 4, currentFBO);
		}
		if(WorldDrawer.flagScreenShotWithGUI) {
			doScreenShot();
			WorldDrawer.flagScreenShotWithGUI = false;
		}
		GUI3DBlockElement.setMatrix();
		if(EngineSettings.T_ENABLE_TEXTURE_BAKER.isOn()) {
			if(bakery == null) {
				bakery = new IconTextureBakery();
			}
			bakery.drawTest();
		}
		if(flagRecipeTrees) {
			try {
				GUIBlockConsistenceGraph.bake(currentFBO, state);
			} catch(GLException e) {
				e.printStackTrace();
			}
			flagRecipeTrees = false;
		}
		if(flagTextureBake) {
			IconTextureBakery b = new IconTextureBakery();
			if(bakery == null) {
				bakery = new IconTextureBakery();
			}
			b.sheetNumber = bakery.sheetNumber;
			try {
				b.bake();
			} catch(GLException e) {
				e.printStackTrace();
			}
			flagTextureBake = false;
		}
		//
	}

	private void resetEntityRender() {
		GlUtil.writeScreenToDisk("./screenshots/entity-render_" + System.currentTimeMillis(), "png", GLFrame.getWidth(), GLFrame.getHeight(), 4, currentFBO);
		Controller.getCamera().getWorldTransform().set(cachedCamera);
		flagEntityRender = false;
		segmentControllerRenderId = -1;
		Controller.getCamera().setCameraOffset(0);
	}

	public void drawSunBallForGodRays() {
		state.getScene().getMainLight().draw();
		Vector3f lPos = AbstractScene.mainLight.getPos();
		Vector3i relSysPos = Galaxy.getLocalCoordinatesFromSystem(state.getPlayer().getCurrentSystem(), new Vector3i());
		int systemType = state.getCurrentGalaxy().getSystemType(relSysPos);
		if((systemType == Galaxy.TYPE_BLACK_HOLE || state.getCurrentGalaxy().isVoid(relSysPos))) {
			return;
		}
		if(state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
			return;
		}
		{
			GlUtil.glPushMatrix();
			//		sunDrawer.draw();
			GlUtil.translateModelview(lPos.x, lPos.y, lPos.z);
			float max = 30000;
			float smallFac = 1;
			float normalSize = systemType == Galaxy.TYPE_GIANT ? 18 : 8;
			float d = (float) Math.pow(lPos.length(), 1.3f);
			if(d < 30000) {
				smallFac = d / 30000f;
				normalSize *= smallFac;
			}
			Controller.getResLoader().getMesh("Sphere").setScale(normalSize, normalSize, normalSize);
			Controller.getResLoader().getMesh("Sphere").draw();
			GlUtil.glPopMatrix();
		}
		if(secondSunLightPos != null) {
			GlUtil.glPushMatrix();
			//		sunDrawer.draw();
			GlUtil.translateModelview(secondSunLightPos.x, secondSunLightPos.y, secondSunLightPos.z);
			float max = 30000;
			float smallFac = 1;
			float normalSize = 4;
			float d = (float) Math.pow(lPos.length(), 1.3f);
			if(d < 30000) {
				smallFac = d / 30000f;
				normalSize *= smallFac;
			}
			Controller.getResLoader().getMesh("Sphere").setScale(normalSize, normalSize, normalSize);
			Controller.getResLoader().getMesh("Sphere").draw();
			GlUtil.glPopMatrix();
		}
	}

	public void addShard(RigidDebrisBody bodyFromShape, Mesh convexHC, short type, int sectorId, Vector3f gravity) {
		Shard s = new Shard(bodyFromShape, convexHC, sectorId, gravity);
		s.setType(type);
		shards.add(s);
		//		System.err.println("ADD SHARD: "+s);
	}

	private void drawSystems(long startTime, boolean onPlanet) {
		//INSERTED CODE
		ArrayList<SystemDrawListener> listeners = FastListenerCommon.systemDrawListeners;
		boolean shouldIterate = !listeners.isEmpty();
		///
		ClientProximitySystem proximitySystem = state.getPlayer().getProximitySystem();
		int nearest = -1;
		Vector3i nearestVec = new Vector3i();
		//		for(int i = 0; i < ClientProximitySystem.ALEN; i++){
		//
		//			byte type = proximitySystem.getType(i);
		//
		//			SectorType sType = SectorType.values()[type];
		//
		//			proximitySystem.getPosFromIndex(i, tmpSysPos);
		//
		//			tmpSecPos.set(
		//					tmpSysPos.x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE/2,
		//					tmpSysPos.y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE/2,
		//					tmpSysPos.z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE/2);
		//
		//			Vector3i pPos = new Vector3i(tmpSecPos);
		//			pPos.sub(state.getPlayer().getCurrentSector());
		//			if(nearest < 0 || pPos.length() < nearestVec.length()){
		//				nearest = i;
		//				nearestVec.set(pPos);
		//			}
		//			tmp.setIdentity();
		//			if(sType == SectorType.SUN){
		//
		//				setMainLight(tmpSecPos, startTime, tmpSysPos, pPos, onPlanet, tmp);
		//
		//			}else if(sType == SectorType.BLACK_HOLE){
		//				//				System.err.println("Drawing black hole at "+tmpSecPos);
		//				setMainLight(tmpSecPos, startTime, tmpSysPos, pPos, onPlanet, tmp);
		//				blackHoleDrawer.setPlanetSectorPos(pPos);
		//				blackHoleDrawer.draw();
		//			}
		//		}
		Vector3i currentSys = state.getPlayer().getCurrentSystem();
		if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive()) {
			sunDistDrawer.setUseQueries(false);
			sunDistDrawer.setUseLensFlare(false);
			sunDistDrawer.setDepthTest(true);
			for(int z = -1; z < 2; z++) {
				for(int y = -1; y < 2; y++) {
					for(int x = -1; x < 2; x++) {
						extPosMod.set(currentSys);
						extPosMod.add(x, y, z);
						Vector3i relpos = Galaxy.getLocalCoordinatesFromSystem(extPosMod, extPos);
						if(!state.getCurrentGalaxy().isVoid(relpos)) {
							//INSERTED CODE
							if(shouldIterate) {
								for(SystemDrawListener listener : listeners) {
									listener.preSystemDraw(relpos);
								}
							}
							///
							int systemType = state.getCurrentGalaxy().getSystemType(relpos);
							Vector3i sunPositionOffset = state.getCurrentGalaxy().getSunPositionOffset(relpos, extOffset);
							Vector4f sunColor = state.getCurrentGalaxy().getSunColor(relpos);
							tmpSecPos.set((currentSys.x + x) * VoidSystem.SYSTEM_SIZE + (VoidSystem.SYSTEM_SIZE / 2 + sunPositionOffset.x), (currentSys.y + y) * VoidSystem.SYSTEM_SIZE + (VoidSystem.SYSTEM_SIZE / 2 + sunPositionOffset.y), (currentSys.z + z) * VoidSystem.SYSTEM_SIZE + (VoidSystem.SYSTEM_SIZE / 2 + sunPositionOffset.z));
							Vector3i pPos = new Vector3i(tmpSecPos);
							pPos.sub(state.getPlayer().getCurrentSector());
							setMainLight(tmpSecPos, startTime, tmpSysPos, pPos, onPlanet, tmp);
							if(systemType == Galaxy.TYPE_BLACK_HOLE) {
								blackHoleDrawer.draw();
							} else if(!(x == 0 && y == 0 && z == 0)) {
								if(systemType == Galaxy.TYPE_GIANT) {
									sunDistDrawer.setColor(sunColor);
									sunDistDrawer.sizeMult = 0.43f;
									sunDistDrawer.draw();
								} else if(systemType == Galaxy.TYPE_DOUBLE_STAR) {
									sunDistDrawer.setColor(sunColor);
									sunDistDrawer.sizeMult = 0.15f;
									sunDistDrawer.draw();
									Vector3i second = VoidSystem.getSecond(tmpSecPos, sunPositionOffset, new Vector3i());
									pPos = new Vector3i(second);
									pPos.sub(state.getPlayer().getCurrentSector());
									setMainLight(second, startTime, tmpSysPos, pPos, onPlanet, tmp);
									sunDistDrawer.draw();
								} else if(systemType == Galaxy.TYPE_SUN) {
									sunDistDrawer.setColor(sunColor);
									sunDistDrawer.sizeMult = 0.15f;
									sunDistDrawer.draw();
								}
							}
							//INSERTED CODE
							if(shouldIterate) {
								for(SystemDrawListener listener : listeners) {
									listener.postSystemDraw(relpos);
								}
							}
							///
						}
					}
				}
			}
			sunDistDrawer.setColor(new Vector4f(1, 1, 1, 1));
			sunDistDrawer.sizeMult = 1;
			sunDistDrawer.setDepthTest(false);
			sunDistDrawer.setUseLensFlare(true);
			sunDistDrawer.setUseQueries(true);
		}
		//end with nearest position as light
		StellarSystem.getPosFromSector(state.getPlayer().getCurrentSector(), tmpSysPos);
		if(!proximitySystem.getBasePosition().equals(tmpSysPos)) {
			System.err.println("[CLIENT] WARNING: System not yet right: " + tmpSysPos + " / " + proximitySystem.getBasePosition());
			return;
		}
		tmpSecPos.set(tmpSysPos.x * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2, tmpSysPos.y * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2, tmpSysPos.z * VoidSystem.SYSTEM_SIZE + VoidSystem.SYSTEM_SIZE / 2);
		Vector3i relSys = Galaxy.getRelPosInGalaxyFromAbsSystem(tmpSysPos, new Vector3i());
		Vector3i sunPositionOffset = state.getCurrentGalaxy().getSunPositionOffset(relSys, new Vector3i());
		tmpSecPos.add(sunPositionOffset);
		Vector3i pPos = new Vector3i(tmpSecPos);
		pPos.sub(state.getPlayer().getCurrentSector());
		setMainLight(tmpSecPos, startTime, tmpSysPos, pPos, onPlanet, tmp);
		GL11.glDepthRange(0.0, 1.0);
		int systemType = state.getCurrentGalaxy().getSystemType(relSys);

		sunDrawer.setRelativeSectorPos(pPos);
		sunColor.set(state.getCurrentGalaxy().getSunColor(relSys));
		sunDrawer.sizeMult = systemType == Galaxy.TYPE_GIANT ? 3 : 1;
		boolean isStar = systemType == Galaxy.TYPE_DOUBLE_STAR || systemType == Galaxy.TYPE_GIANT || systemType == Galaxy.TYPE_SUN;
		sunDrawer.setDrawSunPlasma(isStar);
		if(!state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getMapControlManager().isTreeActive() && isStar) {
			if(!EngineSettings.F_BLOOM.isOn()) {
				if(state.getCurrentGalaxy().isStellarSystem(relSys)) {
					//			System.err.println("Drawing sun at "+tmpSecPos);
					//					System.err.println("PPOS: "+pPos+"; "+tmpSecPos+" col: "+sunColor);
					sunDrawer.setColor(sunColor);
					sunDrawer.draw();
				}
			} else {
				//			System.err.println("Drawing NO SUN at "+tmpSecPos);
			}
			if(systemType == Galaxy.TYPE_DOUBLE_STAR) {
				Vector3i second = VoidSystem.getSecond(tmpSecPos, sunPositionOffset, new Vector3i());
				Vector3i pPos2 = new Vector3i(second);
				pPos2.sub(state.getPlayer().getCurrentSector());
				sunDrawerSec.setRelativeSectorPos(pPos2);
				secondSunColor.set(state.getCurrentGalaxy().getSunColor(relSys.x + 30, relSys.y + 30, relSys.z + 30));
				setMainLight(second, startTime, tmpSysPos, pPos2, onPlanet, tmp);
				this.secondSunLightPos = new Vector3f(AbstractScene.mainLight.getPos());
				if(!EngineSettings.F_BLOOM.isOn()) {
					sunDrawerSec.setColor(secondSunColor);
					sunDrawerSec.draw();
				}
				setMainLight(tmpSecPos, startTime, tmpSysPos, pPos, onPlanet, tmp);
			} else {
				this.secondSunLightPos = null;
			}
		}
		AbstractScene.infoList.add("##### SEC/SYS " + tmpSysPos + " ; Abs: " + AbstractScene.mainLight.getPos());
		GlUtil.glColor4f(1, 1, 1, 1);
	}

	public void flagJustEntered(SegmentController ship) {
		WorldDrawer.justEntered = ship;
		WorldDrawer.justEnteredStart = System.currentTimeMillis();
	}

	/**
	 * @return the absoluteMousePosition
	 */
	public Vector3f getAbsoluteMousePosition() {
		return absoluteMousePosition;
	}

	/**
	 * @return the beamDrawerManager
	 */
	public BeamDrawerManager getBeamDrawerManager() {
		return beamDrawerManager;
	}

	/**
	 * @return the buildModeDrawer
	 */
	public BuildModeDrawer getBuildModeDrawer() {
		return buildModeDrawer;
	}

	/**
	 * @return the connectionDrawerManager
	 */
	public ConnectionDrawerManager getConnectionDrawerManager() {
		return connectionDrawerManager;
	}

	/**
	 * @return the cubeBruteCollectionDrawer
	 */
	public CubeBruteCollectionDrawer getCubeBruteCollectionDrawer() {
		return cubeBruteCollectionDrawer;
	}

	public ExplosionDrawer getExplosionDrawer() {
		return explosionDrawer;
	}

	/**
	 * @return the flareDrawerManager
	 */
	public FlareDrawerManager getFlareDrawerManager() {
		return flareDrawerManager;
	}

	/**
	 * @return the gameMapDrawer
	 */
	public GameMapDrawer getGameMapDrawer() {
		return gameMapDrawer;
	}

	/**
	 * set custom game map drawer.
	 * must be run on GUI thread to avoid thread-synchro errors.
	 * use StarloaderTexture.runOnGraphicsThread
	 *
	 * @param drawer
	 */
	public void setGameMapDrawer(GameMapDrawer drawer) {
		if(drawer != null) drawables.remove(drawer);
		drawables.add(drawer);
		this.gameMapDrawer = drawer;
	}

	public GuiDrawer getGuiDrawer() {
		return guiDrawer;
	}

	/**
	 * @return the liftDrawer
	 */
	public LiftDrawer getLiftDrawer() {
		return liftDrawer;
	}

	/**
	 * @return the plumAndMuzzleDrawer
	 */
	public PlumeAndMuzzleDrawer getPlumAndMuzzleDrawer() {
		return plumAndMuzzleDrawer;
	}

	public List<ProjectileCannonDrawerVBO> getProjectileDrawers() {
		return projectileDrawers;
	}

	public SegmentDrawer getSegmentDrawer() {
		return segmentDrawer;
	}

	/**
	 * @return the shieldDrawerManager
	 */
	public ShieldDrawerManager getShieldDrawerManager() {
		return shieldDrawerManager;
	}

	public StarSkyNew getStarSky() {
		return starSky;
	}

	/**
	 * @return the trailDrawer
	 */
	public MissileTrailDrawer getTrailDrawer() {
		return trailDrawer;
	}

	public void handleKeyEvent(KeyEventInterface e) {
		Keyboard.enableRepeatEvents(true);
		if(EngineSettings.T_ENABLE_TEXTURE_BAKER.isOn()) {
			if(bakery != null) {
				bakery.handleKeyEvent(e);
			}
		}
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * @return the flagCharacterUpdate
	 */
	public boolean isFlagCharacterUpdate() {
		return flagCharacterUpdate;
	}

	/**
	 * @param flagCharacterUpdate the flagCharacterUpdate to set
	 */
	public void setFlagCharacterUpdate(boolean flagCharacterUpdate) {
		this.flagCharacterUpdate = flagCharacterUpdate;
	}

	/**
	 * @return the flagManagedSegmentControllerUpdate
	 */
	public boolean isFlagManagedSegmentControllerUpdate() {
		return flagManagedSegmentControllerUpdate;
	}

	/**
	 * @param flagManagedSegmentControllerUpdate the flagManagedSegmentControllerUpdate to set
	 */
	public void setFlagManagedSegmentControllerUpdate(boolean flagManagedSegmentControllerUpdate) {
		this.flagManagedSegmentControllerUpdate = flagManagedSegmentControllerUpdate;
	}

	public boolean isInStructureBuildMode() {
		PlayerInteractionControlManager pi = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		return pi.isInAnyStructureBuildMode();
		//				(pi.getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive() ||
		//						pi.getSegmentControlManager().getSegmentBuildController().isTreeActive())
		//						&& insideBuildMode;
	}

	public void onEndFrame() {
		TimeStatistics.reset("CONTEXT");
		segmentDrawer.handleContextSwitches();
		SegmentControllerEffectDrawer.unaffectedTranslation = null;
		TimeStatistics.set("CONTEXT");
		state.getController().onEndFrame();
	}

	public void onStartFrame() {
		//		TimeStatistics.reset("CONTEXT");
		//		segmentDrawer.handleContextSwitches();
		//		TimeStatistics.set("CONTEXT");
		if(segmentDrawer != null) {
			segmentDrawer.checkSamples();
		}
		if(sunDrawer != null) {
			sunDrawer.checkSamples();
		}
		if(sunDrawerSec != null) {
			sunDrawerSec.checkSamples();
		}
		state.getController().onStartFrame();
	}

	public void resetCamera() {
		Controller.getCamera().reset();
	}

	private void segManControllerUpdate() {
		long t0 = System.currentTimeMillis();
		plumAndMuzzleDrawer.clear();
		shieldDrawerManager.clear();
		long tookClear = System.currentTimeMillis() - t0;
		if(tookClear > 5) {
			System.err.println("[WORLDDRAWER] WARNING: CLEAR TOOK " + tookClear);
		}
		beamDrawerManager.refresh(state.getCurrentSectorEntities());
		t0 = System.currentTimeMillis();
		flareDrawerManager.refresh(state.getCurrentSectorEntities());
		transporterEffectManager.sectorEntitiesChanged(state.getCurrentSectorEntities().values());
		for(Sendable s : state.getCurrentSectorEntities().values()) {
			long t1 = System.currentTimeMillis();
			if(s instanceof Ship) {
				plumAndMuzzleDrawer.addPlume(new ExhaustPlumes((Ship) s));
				plumAndMuzzleDrawer.addMuzzle(new MuzzleFlash((Ship) s));
				shieldDrawerManager.add((Ship) s);
			}
			if(s instanceof SpaceStation || s instanceof Planet) {
				shieldDrawerManager.add((ManagedSegmentController<?>) s);
				flareDrawerManager.addController((ManagedSegmentController<?>) s);
			}
			long tookUp = System.currentTimeMillis() - t1;
			if(tookUp > 5) {
				System.err.println("[WORLDDRAWER] WARNING: DRAWER UPDATE OF " + s + " took " + tookUp);
			}
		}
		long tookAdd = System.currentTimeMillis() - t0;
		if(tookAdd > 5) {
			System.err.println("[WORLDDRAWER] WARNING: ADD TOOK " + tookAdd);
		}
		t0 = System.currentTimeMillis();
		connectionDrawerManager.updateEntities();
		energyStreamDrawerManager.updateEntities();
		long tookConnection = System.currentTimeMillis() - t0;
		if(tookConnection > 5) {
			System.err.println("[WORLDDRAWER] WARNING: CONNECTION UPDATE TOOK " + tookConnection);
		}
	}

	/**
	 * @param flagSegmentControllerUpdate the flagSegmentControllerUpdate to set
	 */
	public void setFlagSegmentControllerUpdate(boolean flagSegmentControllerUpdate) {
		this.flagSegmentControllerUpdate = flagSegmentControllerUpdate;
	}

	private void setMainLight(Vector3i secPos, long startTime, Vector3i sysPos, Vector3i pPos, boolean onPlanet, Transform sunTrans) {
		AbstractScene.farPlane = (((GameStateInterface) state).getSectorSize() + 10) * 32;
		//		//		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)){
		//		lastPC = pc;
		//		}else{
		//		}
		absSectorPos.set(pPos.x * ((GameStateInterface) state).getSectorSize(), pPos.y * ((GameStateInterface) state).getSectorSize(), pPos.z * ((GameStateInterface) state).getSectorSize());
		if(onPlanet) {
			sunTrans.setIdentity();
			sunTrans.origin.set(absSectorPos);
			float year = state.getGameState().getRotationProgession();
			rot.setIdentity();
			rot.rotX((FastMath.PI * 2) * year);
			rot.invert();
			TransformTools.rotateAroundPoint(absSectorPos, rot, sunTrans, tmpTrans);
			AbstractScene.mainLight.setPos(sunTrans.origin);
		} else {
			AbstractScene.mainLight.setPos(absSectorPos);
		}
		//
		//		if(StellarSystem.isStarSystem(state.getPlayer().getCurrentSector())){
		//
		//		}
		//
		//
		//		//		tmp.origin.set(absCenterPos);
		//		//		tmp.origin.sub(absSectorPos);
		//
		//		tmp.transform(absSectorPos);
	}

	public void updateSpecialSectors() {
		Runnable r = () -> {
			it.unimi.dsi.fastutil.objects.ObjectArrayList<Integer> l = new it.unimi.dsi.fastutil.objects.ObjectArrayList<>();
			for(int i = 0; i < ClientProximitySector.ALEN; i++) {
				SectorInformation.SectorType sectorType = SectorInformation.SectorType.values()[state.getPlayer().getProximitySector().getSectorType(i)];
				if(sectorType == SectorInformation.SectorType.PLANET || sectorType == SectorInformation.SectorType.GAS_PLANET) { //TODO condense to marked sectors system? will get cumbersome as more "special" sector types are added, also not great for modding. -Ithirahad
					l.add(i);
				}
			}
			specialSectorsBuffer = l.toArray(new Integer[l.size()]);
		};
		state.getConnectionThreadPool().execute(r);
	}

	public CharactersDrawer getCharacterDrawer() {
		return characterDrawer;
	}

	/**
	 * @return the creatureTool
	 */
	public CreatureTool getCreatureTool() {
		return creatureTool;
	}

	/**
	 * @param creatureTool the creatureTool to set
	 */
	public void setCreatureTool(CreatureTool creatureTool) {
		this.creatureTool = creatureTool;
	}

	/**
	 * @return the flagPlanetCoreUpdate
	 */
	public boolean isFlagPlanetCoreUpdate() {
		return flagPlanetCoreUpdate;
	}

	/**
	 * @param flagPlanetCoreUpdate the flagPlanetCoreUpdate to set
	 */
	public void setFlagPlanetCoreUpdate(boolean flagPlanetCoreUpdate) {
		this.flagPlanetCoreUpdate = flagPlanetCoreUpdate;
	}

	/**
	 * @return the shards
	 */
	public ShardDrawer getShards() {
		return shards;
	}

	/**
	 * @return the segmentControllerEffectDrawer
	 */
	public SegmentControllerEffectDrawer getSegmentControllerEffectDrawer() {
		return segmentControllerEffectDrawer;
	}

	/**
	 * @param segmentControllerEffectDrawer the segmentControllerEffectDrawer to set
	 */
	public void setSegmentControllerEffectDrawer(SegmentControllerEffectDrawer segmentControllerEffectDrawer) {
		this.segmentControllerEffectDrawer = segmentControllerEffectDrawer;
	}

	public int isSpotLightSupport() {
		boolean spot = state.spotlights.size() > 0;
		//		if(state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager()
		//				.getPlayerIntercationManager().getPlayerCharacterManager().isTreeActive()){
		//
		//			spot = state.getPlayer().getAssingedPlayerCharacter() != null &&
		//					state.getPlayer().getAssingedPlayerCharacter().getFlashLightActive() != null;
		//		}
		//		System.err.println("SPOT:::: "+spot);
		return spot ? 0 : CubeShaderType.NO_SPOT_LIGHTS.bit;
	}

	public TransporterEffectManager getTransporterEffectManager() {
		return transporterEffectManager;
	}

	public void onStopClient() {
		if(segmentDrawer != null) {
			segmentDrawer.onStopClient();
		}
	}

	public void drawAdditional(FrameBufferObjects foregroundFbo, FrameBufferObjects fbo, DepthBufferScene depthBuffer) {
		prepareCamera();
		GL11.glDepthRange(0.0, 1.0);
		explosionDrawer.draw(foregroundFbo, fbo, depthBuffer, DepthBufferScene.getNearPlane(), DepthBufferScene.getFarPlane());
		GlUtil.glMatrixMode(GL11.GL_PROJECTION);
		GlUtil.glPopMatrix();
		GlUtil.glMatrixMode(GL11.GL_MODELVIEW);
		if(drawError) {
			GlUtil.printGlErrorCritical();
		}
		boolean doOutline = false;
		boolean drawELementCollection = segmentDrawer.drawCheckElementCollections();
		if(drawELementCollection && (gameMapDrawer == null || !gameMapDrawer.doDraw())) {
			foregroundFbo.enable();
			if(!doOutline) {
				GL11.glClearColor(0, 0, 0, 0);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			}
			foregroundFbo.disable();
			if(gameMapDrawer == null || !gameMapDrawer.doDraw()) {
				segmentDrawer.drawElementCollectionsToFrameBuffer(foregroundFbo);
			}
			//			fbo.enable();
			doOutline = true;
		}
		if(!drawELementCollection && (gameMapDrawer == null || !gameMapDrawer.doDraw())) {
			SimpleTransformableSendableObject<?> selectedEntity = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedEntity();
			if(selectedEntity != null && selectedEntity instanceof SegmentController && state.getShip() != selectedEntity) {
				SegmentController c = (SegmentController) selectedEntity;
				if(c.isInClientRange() && (EngineSettings.PERMA_OUTLINE.isOn() || KeyboardMappings.SELECT_OUTLINE.isDown())) {
					foregroundFbo.enable();
					if(!doOutline) {
						GL11.glClearColor(0, 0, 0, 0);
						GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
					}
					Vector4f col = new Vector4f();
					HudIndicatorOverlay.getColor(c, col, false, state);
					//					System.err.println("COL: "+col);
					col.w = 1;
					ShaderLibrary.cubeShader13SimpleWhite.loadWithoutUpdate();
					GlUtil.updateShaderVector4f(ShaderLibrary.cubeShader13SimpleWhite, "col", col);
					ShaderLibrary.cubeShader13SimpleWhite.unloadWithoutExit();
					int drawn = segmentDrawer.drawSegmentController(c, ShaderLibrary.cubeShader13SimpleWhite);
					foregroundFbo.disable();
					if(drawn > 0) {
						doOutline = true;
					}
				}
			}
		}
		if(doOutline) {
			fbo.enable();
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			//cubemap texture background
			//			ElementCollectionDrawer.debugMode = true;
			segmentDrawer.drawElementCollectionsFromFrameBuffer(foregroundFbo, drawELementCollection ? 0.5f : 0.1f);
			GlUtil.glDisable(GL11.GL_BLEND);
			fbo.disable();
		}
	}

	@Override
	public void onSectorChangeSelf(int newSector, int oldSector) {
		long t = System.currentTimeMillis();
		guiDrawer.onSectorChange();
		long took = System.currentTimeMillis() - t;
		if(took > 10) {
			System.err.println("[WORLDDRAWER] WARNING: SECTOR CHANGE UPDATE FOR DRAWER TOOK " + took);
		}
	}

	@Override
	public void onWindowSizeChanged(int width, int height) {
		this.screenChanged = true;
	}
}
