package org.schema.game.client.view;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.*;
import org.schema.game.client.controller.manager.ingame.*;
import org.schema.game.client.controller.manager.ingame.character.PlayerExternalController;
import org.schema.game.client.controller.manager.ingame.ship.ShipControllerManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.camera.BuildShipCamera;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.effects.ConstantIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.StabilizerUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.creature.AICreature;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.physics.*;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.world.GameTransformable;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.util.timer.LinearTimerUtil;
import org.schema.schine.graphicsengine.util.timer.SinusTimerUtil;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ConcurrentModificationException;
import java.util.List;

public class BuildModeDrawer implements Drawable {

	public static final ArmorValue armorValue = new ArmorValue();
	public static SegmentPiece currentPiece;
	public static ElementInformation currentInfo;
	public static int currentSide;
	public static SegmentPiece selectedBlock;
	public static ElementInformation selectedInfo;
	public static float currentOptStabDist;
	public static float currentStabDist;
	public static long currentPieceIndexIntegrity;
	public static double currentPieceIntegrity;
	public static double currentStabEfficiency;
	public static boolean inReactorAlignSlider;
	public static boolean inReactorAlignAlwaysVisible;
	public static int inReactorAlignSliderSelectedAxis = -1;
	private final Vector3f dist = new Vector3f();
	private final SingleBlockDrawer drawer = new SingleBlockDrawer();
	private final ShortOpenHashSet conDrw = new ShortOpenHashSet();
	private final PolygonToolsVars v = new PolygonToolsVars();
	private final Vector3f tmp = new Vector3f();
	private final Vector4f stabColor = new Vector4f();
	private final SinusTimerUtil colorMod = new SinusTimerUtil(7);
	private final ArmorCheckTraverseHandler pt = new ArmorCheckTraverseHandler();
	private final Vector3f cPosA = new Vector3f();
	private final Vector3f cPosB = new Vector3f();
	public CubeRayCastResult testRayCollisionPoint;
	Transform t = new Transform();
	Transform tinv = new Transform();
	Vector3i pp = new Vector3i();
	Vector3i posTmp = new Vector3i();
	int i;
	StringBuffer touching = new StringBuffer();
	private boolean firstDraw = true;
	private final GameClientState state;
	private final LinearTimerUtil linearTimer;
	private final LinearTimerUtil linearTimerC;
	private final ConstantIndication indication;
	private final Vector3b lastCubePos = new Vector3b();
	private Segment lastSegment;
	private Mesh mesh;
	private int blockOrientation = -1;
	private long blockChangedTime;
	private SelectionShader selectionShader;
	private SelectionShader selectionShaderSolid;
	private final Vector3i toBuildPos = new Vector3i();
	private final Vector3i loockingAtPos = new Vector3i();
	private final Vector3f pTmp = new Vector3f();
	private boolean flagUpdate;
	private GameTransformable currentObject;
	private boolean drawDebug;
	private final LinearTimerUtil linearTimerSl = new LinearTimerUtil(1.0f);
	private int currentSelectedStabSide = -1;
	private final CubeRayCastResult rayCallbackTraverse = new CubeRayCastResult(new Vector3f(), new Vector3f(), null) {

		@Override
		public InnerSegmentIterator newInnerSegmentIterator() {

			return pt;
		}

	};
	private long lastArmorCheck;
	public BuildModeDrawer(GameClientState state) {
		this.state = state;
		linearTimer = new LinearTimerUtil();
		linearTimerC = new LinearTimerUtil(6.1f);
		indication = new ConstantIndication(new Transform(), "");
	}

	private static void drawArrow(float x, float y, float z, float xs, float ys, float zs, Transform currentWT, Vector4f color) {
		DebugLine[] centerOfMassCross = DebugLine.getArrow(new Vector3f(x, y, z), new Vector3f(xs, ys, zs), color, 0.25f, 3.0f, 0.5f, 50.0f, currentWT);
		for(int i = 0; i < centerOfMassCross.length; i++) {
			DebugLine d = centerOfMassCross[i];
			d.drawRaw();
		}
	}

	private static void drawPoint(SegmentController s, float xs, float ys, float zs, Vector4f color, boolean full) {
		DebugLine[] centerOfMassCross = DebugLine.getCross(s != null ? s.getWorldTransformOnClient() : TransformTools.ident, new Vector3f(xs, ys, zs), 1.5f, 1.5f, 1.5f, full);
		for(int i = 0; i < centerOfMassCross.length; i++) {
			DebugLine d = centerOfMassCross[i];
			d.setColor(color);
			d.drawRaw();
		}
	}

	public void addBlockedDockIndicator(SegmentPiece segmentPiece, SegmentController segmentController, DockingBlockCollectionManager lastColManTried) {
	}

	@Override
	public void cleanUp() {
		drawer.cleanUp();
	}

	@Override
	public void draw() {
		if(firstDraw) {
			onInit();
			GlUtil.printGlErrorCritical();
		}
		if(EngineSettings.G_DRAW_NO_OVERLAYS.isOn()) {
			return;
		}
		if(touching.length() > 0) {
			touching.delete(0, touching.length());
		}
		drawDebug = WorldDrawer.drawError;
		if(drawDebug) {
			GlUtil.printGlErrorCritical();
		}

		SimpleTransformableSendableObject<?> cur = state.getCurrentPlayerObject();
		if(!getShipControllerManager().getSegmentBuildController().isTreeActive() && getPlayerManager().isActive()) {
			WorldDrawer.insideBuildMode = false;
			drawCharacterExternalMode();
			return;
		}
		if(drawDebug) {
			GlUtil.printGlErrorCritical();
		}
		if(cur instanceof SegmentController c && getPlayerIntercationManager().isInAnyStructureBuildMode()) {
			//			drawFor(c);
			drawFor(c);
		}


	}

	public void drawForAll(SegmentController c) {
		SegmentController root = c.railController.getRoot();
		drawForChilds(root);
	}

	public void drawForChilds(SegmentController c) {
		drawFor(c);
		for(RailRelation r : c.railController.next) {
			drawForChilds(r.docked.getSegmentController());
		}
	}

	private void drawFor(SegmentController c) {
		currentSelectedStabSide = -1;
		if(c == null) {
			return;
		}
		if(drawDebug) {
			GlUtil.printGlErrorCritical();
		}

		if(c instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> m = ((ManagedSegmentController<?>) c).getManagerContainer();
			if(m.isUsingPowerReactors() && getPlayerIntercationManager().getSelectedTypeWithSub() == ElementKeyMap.REACTOR_STABILIZER) {
				m.getStabilizer().drawMesh();
			}
		}
		drawPowerHull(c, true);


		if(drawDebug) {
			GlUtil.printGlErrorCritical();
		}

		WorldDrawer.insideBuildMode = true;
		//getShipControllerManager().getSegmentBuildController().isTreeActive()  &&
		if(c instanceof Ship) {

			BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
			boolean addMode = buildToolsManager.isAddMode();
			if(buildToolsManager.getBuildHelper() != null) {
				buildToolsManager.getBuildHelper().draw();
			}


			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}

			GlUtil.glPushMatrix();

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			boolean drawArrow = true;
			if(ElementKeyMap.isValidType(getPlayerIntercationManager().getSelectedTypeWithSub())) {
//				if (ElementKeyMap.getInfo(getPlayerIntercationManager().getSelectedType()).getBlockStyle() != BlockStyle.NORMAL&& ElementKeyMap.getInfo(getPlayerIntercationManager().getSelectedType()).getBlockStyle() != 2) {
//					drawArrow = false;
//				}
			} else {
				drawArrow = false;
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			Transform drawToBuild = getToBuildTransform(c);


			if(buildToolsManager.isInCreateDockingMode() && buildToolsManager.getBuildToolCreateDocking().core != null) {
				Vector3f absolutePos = buildToolsManager.getBuildToolCreateDocking().core.getAbsolutePos(new Vector3f());
				absolutePos.x -= SegmentData.SEG_HALF;
				absolutePos.y -= SegmentData.SEG_HALF;
				absolutePos.z -= SegmentData.SEG_HALF;
				c.getWorldTransform().transform(absolutePos);
				Transform t = new Transform(c.getWorldTransform());
				t.origin.set(absolutePos);


//				buildToolsManager.getBuildToolCreateDocking().core.setActive((buildToolsManager.getBuildToolCreateDocking().potentialCoreOrientation >= SegmentData.MAX_ORIENT));
				buildToolsManager.getBuildToolCreateDocking().core.setOrientation((byte) (buildToolsManager.getBuildToolCreateDocking().potentialCoreOrientation));

				Oriencube algorithm = (Oriencube) buildToolsManager.getBuildToolCreateDocking().core.getAlgorithm(ElementKeyMap.RAIL_BLOCK_BASIC);
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}

//				drawOrientation(t, algorithm.getOrientCubePrimaryOrientation());

				Vector3f d = new Vector3f(Element.DIRECTIONSf[algorithm.getOrientCubePrimaryOrientationSwitchedLeftRight()]);
				d.scale(0.5f);
				c.getWorldTransform().basis.transform(d);

				t.origin.add(d);

				drawOrientationArrow(t, algorithm.getOrientCubeSecondaryOrientation());
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
			} else if(drawToBuild != null && drawArrow) {
				int blockOrientation = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation();
				drawOrientationArrow(drawToBuild, blockOrientation);
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(isDrawPreview() && !buildToolsManager.isInCreateDockingMode()) {
//				drawToBuildBox(c, drawer, ShaderLibrary.selectionShader, selectionShader, false);
				Transform sm = drawToBuildBox(c, drawer, ShaderLibrary.selectionShader, selectionShader, addMode);
				if(sm != null && getPlayerIntercationManager().getSelectedTypeWithSub() == ElementKeyMap.REACTOR_STABILIZER) {
					if(VoidElementManager.isUsingReactorDistance()) {
						drawReactorDistance(c, ElementCollection.getIndex(toBuildPos));
					}
				}

			}


			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			mesh.loadVBO(true);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			//ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
			//ShaderLibrary.selectionShader.load();
			GlUtil.glDisable(GL11.GL_CULL_FACE);

			if(EngineSettings.G_BASIC_SELECTION_BOX.isOn()) {
				ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
				ShaderLibrary.selectionShader.load();
//				drawToBuildBox(c, null, ShaderLibrary.selectionShader, selectionShader, false);
				drawToBuildBox(c, null, ShaderLibrary.selectionShader, selectionShader, addMode);
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
			} else {

				ShaderLibrary.solidSelectionShader.setShaderInterface(selectionShaderSolid);
				ShaderLibrary.solidSelectionShader.load();
//				drawToBuildBox(c, null, ShaderLibrary.solidSelectionShader, selectionShaderSolid, false);
				drawToBuildBox(c, null, ShaderLibrary.solidSelectionShader, selectionShaderSolid, addMode);

				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}

			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			drawCreateDock(c);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			drawCameraHighlight(c);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
			ShaderLibrary.selectionShader.load();
			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1.0f, 1.0f, 0.0f, 0.65f);

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.9f, 0.6f, 0.2f, 0.65f);
			drawCurrentSelectedElement(c, getShipControllerManager().getSegmentBuildController().getSelectedBlock());

			drawControlledElements(c, getShipControllerManager().getSegmentBuildController().getSelectedBlock());


			GlUtil.glEnable(GL11.GL_CULL_FACE);

			ShaderLibrary.selectionShader.unload();
			mesh.unloadVBO(true);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			SymmetryPlanes s = getActiveBuildController().getSymmetryPlanes();
			if(s.isXyPlaneEnabled() || s.isXzPlaneEnabled() || s.isYzPlaneEnabled()) {
				if(EngineSettings.G_SHOW_SYMMETRY_PLANES.isOn()) {
					drawCurrentSymetriePlanesElement(c);
				}
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(s.getPlaceMode() > 0) {
				drawCurrentSymetriePlanesElement(c, null, s.getPlaceMode(), toBuildPos, s.getExtraDist() * 0.5f);
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(c.getMass() > 0 && buildToolsManager.showCenterOfMass) {
				drawCenterOfMass(c);
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface) {
				drawLocalShields(((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()));
			}
//			if(VoidElementManager.isUsingReactorDistance()){
//          drawStabilizerOrientation(c);
//				drawReactorCoordinateSystems(c);
//			}

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(buildToolsManager.isInCreateDockingMode()) {
				drawCreateDockingMode(c);
			}

			drawToBuildConnection(c);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glPopMatrix();
		} else { //getSegmentControlManager().getSegmentBuildController().isTreeActive()
			SegmentController segmentController = c;
			GlUtil.glPushMatrix();
			BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
			boolean addMode = buildToolsManager.isAddMode();
			if(buildToolsManager.getBuildHelper() != null) {
				buildToolsManager.getBuildHelper().draw();
			}

			if(isDrawPreview() && !buildToolsManager.isInCreateDockingMode()) {
				//				drawToBuildBox(segmentController, drawer, ShaderLibrary.selectionShader, selectionShader, false);
				Transform sm = drawToBuildBox(segmentController, drawer, ShaderLibrary.selectionShader, selectionShader, addMode);
				if(sm != null && getPlayerIntercationManager().getSelectedTypeWithSub() == ElementKeyMap.REACTOR_STABILIZER) {
					if(VoidElementManager.isUsingReactorDistance()) {
						drawReactorDistance(c, ElementCollection.getIndex(toBuildPos));
					}
				}


			}

			mesh.loadVBO(true);

			GlUtil.glDisable(GL11.GL_CULL_FACE);

			Transform drawToBuild;

			if(EngineSettings.G_BASIC_SELECTION_BOX.isOn()) {
				ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
				ShaderLibrary.selectionShader.load();
				GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.7f, 0.77f, 0.1f, 0.65f);
				//				drawToBuildBox(segmentController, null, ShaderLibrary.selectionShader, selectionShader, false);
				drawToBuild = drawToBuildBox(segmentController, null, ShaderLibrary.selectionShader, selectionShader, addMode);


			} else {
				ShaderLibrary.solidSelectionShader.setShaderInterface(selectionShaderSolid);
				ShaderLibrary.solidSelectionShader.load();
				GlUtil.updateShaderVector4f(ShaderLibrary.solidSelectionShader, "selectionColor", 0.7f, 0.77f, 0.1f, 0.65f);
				//				drawToBuildBox(segmentController, null, ShaderLibrary.solidSelectionShader, selectionShaderSolid, false);
				drawToBuild = drawToBuildBox(segmentController, null, ShaderLibrary.solidSelectionShader, selectionShaderSolid, addMode);


			}

			drawCreateDock(segmentController);
			drawCameraHighlight(segmentController);

			ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
			ShaderLibrary.selectionShader.load();

			//			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 1.0f, 1.0f, 0.0f, 1f);
			//			drawCurrentCamElement(segmentController);

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.9f, 0.6f, 0.2f, 0.65f);
			drawCurrentSelectedElement(segmentController, getSegmentControlManager().getSegmentBuildController().getSelectedBlock());

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.4f, 0.1f, 0.9f, 0.65f);
			drawControlledElements(segmentController, getSegmentControlManager().getSegmentBuildController().getSelectedBlock());

			GlUtil.glEnable(GL11.GL_CULL_FACE);

			ShaderLibrary.selectionShader.unload();
			mesh.unloadVBO(true);

			SymmetryPlanes s = getSegmentControlManager().getSegmentBuildController().getSymmetryPlanes();
			if(s.isXyPlaneEnabled() || s.isXzPlaneEnabled() || s.isYzPlaneEnabled()) {
				drawCurrentSymetriePlanesElement(segmentController);
			}
			if(s.getPlaceMode() > 0) {
				drawCurrentSymetriePlanesElement(segmentController, null, s.getPlaceMode(), toBuildPos, s.getExtraDist() * 0.5f);
			}
			if(c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) c).getManagerContainer() instanceof ShieldContainerInterface) {
				drawLocalShields(((ShieldContainerInterface) ((ManagedSegmentController<?>) c).getManagerContainer()));
			}
//			if(VoidElementManager.isUsingReactorDistance()) {
//				drawStabilizerOrientation(c);
//				drawReactorCoordinateSystems(c);
//			}

			if(buildToolsManager.isInCreateDockingMode() && buildToolsManager.getBuildToolCreateDocking().core != null) {
				Vector3f absolutePos = buildToolsManager.getBuildToolCreateDocking().core.getAbsolutePos(new Vector3f());
				absolutePos.x -= SegmentData.SEG_HALF;
				absolutePos.y -= SegmentData.SEG_HALF;
				absolutePos.z -= SegmentData.SEG_HALF;
				segmentController.getWorldTransform().transform(absolutePos);
				Transform t = new Transform(segmentController.getWorldTransform());
				t.origin.set(absolutePos);


				//				buildToolsManager.getBuildToolCreateDocking().core.setActive((buildToolsManager.getBuildToolCreateDocking().potentialCoreOrientation >= SegmentData.MAX_ORIENT));
				buildToolsManager.getBuildToolCreateDocking().core.setOrientation((byte) (buildToolsManager.getBuildToolCreateDocking().potentialCoreOrientation));

				Oriencube algorithm = (Oriencube) buildToolsManager.getBuildToolCreateDocking().core.getAlgorithm(ElementKeyMap.RAIL_BLOCK_BASIC);


				//				drawOrientation(t, algorithm.getOrientCubePrimaryOrientation());

				Vector3f d = new Vector3f(Element.DIRECTIONSf[algorithm.getOrientCubePrimaryOrientationSwitchedLeftRight()]);
				d.scale(0.5f);
				getSegmentControlManager().getEntered().getSegmentController().getWorldTransform().basis.transform(d);

				t.origin.add(d);

				drawOrientationArrow(t, algorithm.getOrientCubeSecondaryOrientation());
			} else if(drawToBuild != null) {
				int blockOrientation = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation();
				drawOrientationArrow(drawToBuild, blockOrientation);
			}

			GlUtil.glPopMatrix();
		}
		GlUtil.glColor4f(1, 1, 1, 1);
		inReactorAlignSlider = false;
	}

	private void drawPowerHull(SegmentController c, boolean wire) {
		BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		if(!buildToolsManager.reactorHull) {
			return;
		}
		if(wire) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}
//		if(cnt%2 == 0){
//			return;
//		}
		GlUtil.glPushMatrix();
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glColor4f(1, 1, 1, 0.8f);
		if(c != null && c instanceof ManagedSegmentController<?>) {

			GlUtil.glMultMatrix(c.getWorldTransformOnClient());
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) c).getManagerContainer();
			List<MainReactorUnit> mainReactors = managerContainer.getPowerInterface().getMainReactors();
			for(MainReactorUnit u : mainReactors) {
				if(u.tris != null) {
					GlUtil.glPushMatrix();
					GL11.glBegin(GL11.GL_TRIANGLES);
					GlUtil.glColor4f(0.4f, 0.4f, 0.3f, 0.5f);
					float h = SegmentData.SEG_HALF;
					for(int i = 0; i < u.tris.length; i++) {
						Triangle t = u.tris[i];

						Vector3f norm = t.getNormal();
//						System.err.println("TRI #"+i+": "+t);


						GL11.glVertex3f(t.v1.x - h, t.v1.y - h, t.v1.z - h);
						GL11.glNormal3f(norm.x, norm.y, norm.z);
						GL11.glVertex3f(t.v2.x - h, t.v2.y - h, t.v2.z - h);
						GL11.glNormal3f(norm.x, norm.y, norm.z);
						GL11.glVertex3f(t.v3.x - h, t.v3.y - h, t.v3.z - h);
						GL11.glNormal3f(norm.x, norm.y, norm.z);
					}
					GL11.glEnd();
					GlUtil.glPopMatrix();
				}
			}
		}
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glPopMatrix();
		GlUtil.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	private void drawCreateDockingMode(SegmentController c) {

	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

		mesh = ((Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0));
		selectionShader = new SelectionShader(mesh.getMaterial().getTexture().getTextureId());
		selectionShaderSolid = new SelectionShader(-1);
		firstDraw = false;
	}

	private void drawCenterOfMass(SegmentController c) {
		if(c.getPhysicsDataContainer().getShape() != null) {
			DebugLine[] centerOfMassCross = c.getCenterOfMassCross();

			for(int i = 0; i < centerOfMassCross.length; i++) {
				DebugLine d = centerOfMassCross[i];
				d.draw();
			}
		}
	}

	public void drawBlock(long currentBlock, SegmentController ship, LinearTimerUtil lTime) {

		pTmp.set(ElementCollection.getPosX(currentBlock) - SegmentData.SEG_HALF, ElementCollection.getPosY(currentBlock) - SegmentData.SEG_HALF, ElementCollection.getPosZ(currentBlock) - SegmentData.SEG_HALF);

		dist.set(ElementCollection.getPosX(currentBlock) - SegmentData.SEG_HALF, ElementCollection.getPosY(currentBlock) - SegmentData.SEG_HALF, ElementCollection.getPosZ(currentBlock) - SegmentData.SEG_HALF);
		ship.getWorldTransform().transform(dist);
		if(Controller.getCamera().isPointInFrustrum(dist)) {
			dist.sub(Controller.getCamera().getWorldTransform().origin);

			if(dist.length() < 64) {
				GlUtil.glPushMatrix();
				GlUtil.translateModelview(pTmp.x, pTmp.y, pTmp.z);
				float v = 1.05f + lTime.getTime() * 0.05f;
				GlUtil.scaleModelview(v, v, v);
				mesh.renderVBO();
				GlUtil.glPopMatrix();
			}
		}
	}

	public void drawBlock(Vector3i currentBlock, SegmentController ship, LinearTimerUtil lTime) {
		if(currentBlock != null) {

			pTmp.set(currentBlock.x - SegmentData.SEG_HALF, currentBlock.y - SegmentData.SEG_HALF, currentBlock.z - SegmentData.SEG_HALF);

			GlUtil.glPushMatrix();
			GlUtil.translateModelview(pTmp.x, pTmp.y, pTmp.z);
			float v = 1.05f + lTime.getTime() * 0.05f;
			GlUtil.scaleModelview(v, v, v);
			mesh.renderVBO();
			GlUtil.glPopMatrix();

		}
	}

	public void drawCharacterExternalMode() {
		if(firstDraw) {
			onInit();
		}

		if(blockOrientation != state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation()) {
			if(blockOrientation >= 0) {
				blockChangedTime = System.currentTimeMillis();
			}

			blockOrientation = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation();
		}
		SegmentPiece sBlock = getPlayerManager().getSelectedBlock();

		//		System.err.println("EXTERNAL DRAW OF "+sBlock);

		if(sBlock != null) {
			GlUtil.glPushMatrix();
			mesh.loadVBO(true);
			ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
			ShaderLibrary.selectionShader.load();

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.9f, 0.6f, 0.2f, 0.65f);
			drawCurrentSelectedElement(sBlock.getSegment().getSegmentController(), sBlock);

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.4f, 0.1f, 0.9f, 0.65f);
			drawControlledElements(sBlock.getSegment().getSegmentController(), sBlock);

			ShaderLibrary.selectionShader.unload();
			mesh.unloadVBO(true);

			GlUtil.glPopMatrix();
		}
		if(state.getCharacter() != null && System.currentTimeMillis() - blockChangedTime < 3000) {
			drawOrientationArrow(state.getCharacter().getWorldTransform(), state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBlockOrientation());
		}

	}

	public void drawLocalShields(ShieldContainerInterface s) {

		if(currentPiece != null && (currentPiece.getType() == ElementKeyMap.SHIELD_CAP_ID || currentPiece.getType() == ElementKeyMap.SHIELD_REGEN_ID)) {

			s.getShieldAddOn().getShieldLocalAddOn().markDrawCollectionByBlock(currentPiece.getAbsoluteIndex());
		}


		if(getPlayerIntercationManager().getSelectedTypeWithSub() != ElementKeyMap.SHIELD_CAP_ID && getPlayerIntercationManager().getSelectedTypeWithSub() != ElementKeyMap.SHIELD_REGEN_ID) {
			//only show when shield blocks selected
			return;
		}
		if(s.getSegmentController().railController.isDocked()) {
			//dont draw for docked
			return;
		}
		List<ShieldLocal> activeShields = s.getShieldAddOn().getShieldLocalAddOn().getActiveShields();
		List<ShieldLocal> inactiveShields = s.getShieldAddOn().getShieldLocalAddOn().getInactiveShields();
		if(!activeShields.isEmpty() || !inactiveShields.isEmpty()) {
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_SRC_ALPHA, GL11.GL_ONE);

			Mesh mesh = (Mesh) (Controller.getResLoader().getMesh("Sphere").getChilds().get(0));
			GlUtil.glPushMatrix();
			t.set(s.getSegmentController().getWorldTransform());
			GlUtil.glMultMatrix(t);
			Vector4f colorActive = new Vector4f(1, 1, 1, 0.1f);
			Vector4f colorInactive = new Vector4f(1, 0.5f, 0.5f, 0.2f);
			Vector4f colorOrigin = new Vector4f(1, 1.0f, 0.2f, 0.6f);
			Vector4f colorCapacityOrigin = new Vector4f(1, 0.2f, 1.0f, 0.6f);
			Vector4f colorCapacityArrow = new Vector4f(1, 0.6f, 1.0f, 0.9f);
			for(ShieldLocal l : activeShields) {
				GlUtil.glPushMatrix();
				drawLocalShield(s, l, colorActive, colorOrigin, colorCapacityOrigin, colorCapacityArrow, mesh);
				GlUtil.glPopMatrix();
			}
			for(ShieldLocal l : inactiveShields) {
				GlUtil.glPushMatrix();
				drawLocalShield(s, l, colorInactive, colorOrigin, colorCapacityOrigin, colorCapacityArrow, mesh);
				GlUtil.glPopMatrix();
			}
			GlUtil.glPopMatrix();
			GlUtil.glColor4f(1, 1, 1, 1.0f);
			GlUtil.glDisable(GL11.GL_BLEND);
		}
	}

	private void drawLocalShield(ShieldContainerInterface s, ShieldLocal l, Vector4f color, Vector4f colorOrigin, Vector4f colorCapacityOrigin, Vector4f colorCapacityArrow, Mesh mesh) {
		int x = ElementCollection.getPosX(l.outputPos) - Segment.HALF_DIM;
		int y = ElementCollection.getPosY(l.outputPos) - Segment.HALF_DIM;
		int z = ElementCollection.getPosZ(l.outputPos) - Segment.HALF_DIM;
		drawPoint(null, x, y, z, colorOrigin, true);
		if(l.active) {
			for(long pIndex : l.supportCoMIds) {
				int xs = ElementCollection.getPosX(pIndex) - Segment.HALF_DIM;
				int ys = ElementCollection.getPosY(pIndex) - Segment.HALF_DIM;
				int zs = ElementCollection.getPosZ(pIndex) - Segment.HALF_DIM;
				drawPoint(null, xs, ys, zs, colorCapacityOrigin, true);

				GlUtil.glDisable(GL11.GL_DEPTH_TEST);
				drawArrow(xs, ys, zs, x, y, z, s.getSegmentController().getWorldTransformOnClient(), colorCapacityArrow);
				GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			}
		}
		GlUtil.glColor4f(color);
		GlUtil.translateModelview(x, y, z);
		GlUtil.glDepthMask(false);
//		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		if(l.isPositionInRadiusWorld(s.getSegmentController().getWorldTransformOnClient(), Controller.getCamera().getPos())) {
			GL11.glCullFace(GL11.GL_FRONT);
		} else {
			GL11.glCullFace(GL11.GL_BACK);
		}

		GlUtil.drawSphere(l.radius, 20);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GlUtil.drawSphere(l.radius, 20);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		GlUtil.glDepthMask(true);
//		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glCullFace(GL11.GL_BACK);


	}

	public void drawStabilizerOrientation(SegmentController c) {
		if(VoidElementManager.STABILIZER_BONUS_CALC == StabBonusCalcStyle.BY_ANGLE) {
			return;
		}
		if(c instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> m = ((ManagedSegmentController<?>) c).getManagerContainer();
			if(m.hasActiveReactors() && m.getStabilizer().getElementCollections().size() > 0) {
				GlUtil.glColor4f(1, 1, 1, 1);
				GlUtil.glDisable(GL11.GL_TEXTURE_2D);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);

				GlUtil.glDisable(GL11.GL_CULL_FACE);
				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(c.getWorldTransformOnClient());
				for(int i = 0; i < m.getStabilizer().getElementCollections().size(); i++) {
					StabilizerUnit u = m.getStabilizer().getElementCollections().get(i);

					int side = u.getReactorSide();
					if(side >= 0) {
						if(currentPiece != null && u.getNeighboringCollection().contains(currentPiece.getAbsoluteIndex())) {
							currentSelectedStabSide = side;
						}

						float startX = (float) u.getCoMOrigin().x - Segment.HALF_DIM;
						float startY = (float) u.getCoMOrigin().y - Segment.HALF_DIM;
						float startZ = (float) u.getCoMOrigin().z - Segment.HALF_DIM;

						tmp.set(Element.DIRECTIONSf[side]);

						tmp.scale(10);
						ReactorTree activeReactor = m.getPowerInterface().getActiveReactor();
						activeReactor.getBonusMatrix().transform(tmp);

						tmp.x += startX;
						tmp.y += startY;
						tmp.z += startZ;

						if(u.isBonusSlot()) {
							stabColor.set(Element.SIDE_COLORS[side]);

							stabColor.x += colorMod.getTime();
							stabColor.y += colorMod.getTime();
							stabColor.z += colorMod.getTime();
							stabColor.w = 0.9f;
						} else {
							stabColor.set(0.5f, 0.5f, 0.5f, 0.5f);
						}
						GlUtil.glDisable(GL11.GL_DEPTH_TEST);
						drawArrow(startX, startY, startZ, tmp.x, tmp.y, tmp.z, c.getWorldTransformOnClient(), stabColor);

//						drawPoint(c,
//								u.getCoMOrigin().x-(float)Segment.HALF_DIM,
//								u.getCoMOrigin().y-(float)Segment.HALF_DIM,
//								u.getCoMOrigin().z-(float)Segment.HALF_DIM,
//								new Vector4f(1,1,1,1), false);
						GlUtil.glEnable(GL11.GL_DEPTH_TEST);


					}
				}
				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glEnable(GL11.GL_CULL_FACE);
				GlUtil.glPopMatrix();
				GlUtil.glColor4f(1, 1, 1, 1);
			}
		}
	}

	public void drawReactorDistance(SegmentController c, long toPosition) {
		if(c instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> m = ((ManagedSegmentController<?>) c).getManagerContainer();
			if(m.isUsingPowerReactors() && m.getPowerInterface().getMainReactors().size() > 0) {
				PowerInterface pw = m.getPowerInterface();

				Vector3f from = new Vector3f();
				Vector3f to = new Vector3f();
				ElementCollection.getPosFromIndex(toPosition, to);
				float min = Float.POSITIVE_INFINITY;
				for(MainReactorUnit r : pw.getMainReactors()) {
					float dist = r.distanceToThis(toPosition, v);
					if(dist < min) {
						min = dist;
						from.set(v.outFrom);
					}
				}

				t.set(c.getWorldTransform());
				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(t);

				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glEnable(GL11.GL_BLEND);
				//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
				GlUtil.glColor4f(0.5f, 1, 0.5f, 0.9f);
				GlUtil.glDisable(GL11.GL_CULL_FACE);

				GL11.glLineWidth(2.5f);

				double optDist = pw.getReactorOptimalDistance();


				currentStabDist = min;
				currentOptStabDist = (float) optDist;
				currentStabEfficiency = pw.calcStabilization(optDist, min);

				if(min < optDist) {
					GlUtil.glColor4f(1, 0.27f, 0.34f, 0.9f);
				}

				GL11.glBegin(GL11.GL_LINES);

				GL11.glVertex3f(from.x - Segment.HALF_DIM, from.y - Segment.HALF_DIM, from.z - Segment.HALF_DIM);
				GL11.glVertex3f(to.x - Segment.HALF_DIM, to.y - Segment.HALF_DIM, to.z - Segment.HALF_DIM);

				if(min < optDist) {
					Vector3f dst = new Vector3f();
					dst.sub(to, from);
					float diff = (float) (optDist - min);

					dst.normalize();
					dst.scale(diff);
					dst.add(to);
					GlUtil.glColor4f(0.5f, 0.5f, 1.0f, 0.9f);

					GL11.glVertex3f(to.x - Segment.HALF_DIM, to.y - Segment.HALF_DIM, to.z - Segment.HALF_DIM);
					GL11.glVertex3f(dst.x - Segment.HALF_DIM, dst.y - Segment.HALF_DIM, dst.z - Segment.HALF_DIM);
				}

				GL11.glEnd();

				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glEnable(GL11.GL_CULL_FACE);
				GlUtil.glPopMatrix();
			}
		}
	}

	public void drawControlledElements(SegmentController c, SegmentPiece selectedBlock) {
		if(selectedBlock == null) {
			return;
		}
		conDrw.clear();
		Vector4f defaultColor = new Vector4f(0.4f, 0.1f, 0.9f, 0.65f);
		Vector4f color = new Vector4f(defaultColor);
		try {
			long currentPieceIndex = Long.MIN_VALUE;
			if(currentPiece != null) {
				currentPieceIndex = currentPiece.getAbsoluteIndex();
			}


			Vector3i selectedBlockVec = selectedBlock.getAbsolutePos(posTmp);
			long slaveSupport = Long.MIN_VALUE;
			long slaveEffect = Long.MIN_VALUE;
			long lightEffect = Long.MIN_VALUE;

			if(c instanceof ManagedSegmentController<?>) {
				ManagerContainer<?> mc = ((ManagedSegmentController<?>) c).getManagerContainer();
				ManagerModuleCollection<?, ?, ?> mmc = mc.getModulesControllerMap().get(selectedBlock.getType());

				if(mmc != null) {
					UsableElementManager<?, ?, ?> elementManager = mmc.getElementManager();
					if(elementManager instanceof UsableControllableElementManager<?, ?, ?>) {

						ControlBlockElementCollectionManager<?, ?, ?> cm = ((UsableControllableElementManager<?, ?, ?>) elementManager).getCollectionManagersMap().get(ElementCollection.getIndex(selectedBlockVec));
						if(cm != null) {
							cm.drawnUpdateNumber = state.getNumberOfUpdate();
							slaveSupport = cm.getSlaveConnectedElement();
							slaveEffect = cm.getEffectConnectedElement();
							lightEffect = cm.getLightConnectedElement();


							for(ElementCollection<?, ?, ?> e : cm.getElementCollections()) {
								if(e.contains(currentPieceIndex)) {
									currentPieceIndexIntegrity = currentPieceIndex;
									currentPieceIntegrity = e.getIntegrity();
// 									touching.append("Structural Integrity: (base: "+VoidElementManager.COLLECTION_INTEGRITY_START_VALUE+"): "+Math.round(e.getIntegrity())+"\n");

									for(int i = 0; i < 7; i++) {
										touching.append("Touch " + i + "/6: " + e.touching[i] + "; x" + VoidElementManager.getIntegrityBaseTouching(i) + " -> " + Math.round(e.touching[i] * VoidElementManager.getIntegrityBaseTouching(i)) + "\n");
									}
								}
								e.markDraw();
								e.setDrawColor(defaultColor.x + ((1.0f / (1.0f - defaultColor.x)) * linearTimerSl.getTime() * 0.5f), defaultColor.y + ((1.0f / (1.0f - defaultColor.y)) * linearTimerSl.getTime() * 0.5f), defaultColor.z + ((1.0f / (1.0f - defaultColor.z)) * linearTimerSl.getTime() * 0.5f), defaultColor.w);
								conDrw.add(cm.getEnhancerClazz());
							}
						}
					}
				}
			}

			GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", color);

			if(EngineSettings.G_DRAW_SELECTED_BLOCK_WOBBLE.isOn()) {
				if(selectedBlock != null) {

					PositionControl elementsControlledBy = c.getControlElementMap().getDirectControlledElements(Element.TYPE_ALL, selectedBlockVec);
					if(elementsControlledBy != null) {
						prepareBlockDraw(c.getWorldTransform());
						for(long v : elementsControlledBy.getControlMap()) {
							if(EngineSettings.F_FRAME_BUFFER.isOn() && !EngineSettings.G_DRAW_SELECTED_BLOCK_WOBBLE_ALWAYS.isOn() && conDrw.contains((short) ElementCollection.getType(v))) {
								//is drawn per mesh
								continue;
							}
							if(v == slaveSupport) {
								color.set(0.1f, 0.5f, 0.8f, 0.65f);
								GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", color);
							}
							if(v == slaveEffect) {
								color.set(0.1f, 0.9f, 0.1f, 0.65f);
								GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", color);
							}
							if(v == lightEffect) {
								color.set(0.6f, 0.9f, 0.2f, 0.65f);
								GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", color);
							}
							drawBlock(v, c, linearTimerC);

							if(!color.equals(defaultColor)) {
								color.set(defaultColor);
								GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", color);
							}
						}
						endBlockDraw();

					}
				}
			}


		} catch(ConcurrentModificationException e) {
			e.printStackTrace();
		}

	}

	public void drawCurrentCamElement(SegmentController ship) {
		if(Controller.getCamera() instanceof BuildShipCamera cam) {

			t.set(ship.getWorldTransform());
			//			Vector3i g = new Vector3i(cam.getCurrentBlock());
			Vector3f p = cam.getRelativeCubePos();

			t.basis.transform(p);
			t.origin.add(p);

			GlUtil.glPushMatrix();
			GlUtil.glMultMatrix(t);
			GlUtil.scaleModelview(1.01f, 1.01f, 1.01f);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glColor4f(0, 0, 1, 0.6f);
			GlUtil.scaleModelview(0.1f, 0.1f, 0.1f);
			mesh.renderVBO();

			GlUtil.glColor4f(1, 1, 1, 1.0f);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glPopMatrix();

		}
	}

	public void drawCurrentSelectedElement(SegmentController ship, SegmentPiece block) {

		if(block != null) {
			prepareBlockDraw(ship.getWorldTransform());
			block.refresh();
			Vector3i currentBlock = block.getAbsolutePos(posTmp);

			drawBlock(currentBlock, ship, linearTimer);
			endBlockDraw();
		}

	}

	public void drawCurrentSymetriePlanesElement(SegmentController ship) {
		SymmetryPlanes symmetryPlanes = getActiveBuildController().getSymmetryPlanes();
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		//		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, 0.7f);
		GlUtil.glDisable(GL11.GL_CULL_FACE);

		Vector3f p = new Vector3f(symmetryPlanes.getYzPlane().x - SegmentData.SEG_HALF + symmetryPlanes.getYzExtraDist() * 0.5f, symmetryPlanes.getXzPlane().y - SegmentData.SEG_HALF + symmetryPlanes.getXzExtraDist() * 0.5f, symmetryPlanes.getXyPlane().z - SegmentData.SEG_HALF + symmetryPlanes.getXyExtraDist() * 0.5f);

		t.set(ship.getWorldTransform());

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(t);

		GL11.glBegin(GL11.GL_LINES);
		if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {

			GL11.glVertex3f((ship.getMinPos().x) * SegmentData.SEG, p.y, p.z);
			GL11.glVertex3f((ship.getMaxPos().x) * SegmentData.SEG, p.y, p.z);

			GL11.glVertex3f(p.x, (ship.getMinPos().y) * SegmentData.SEG, p.z);
			GL11.glVertex3f(p.x, (ship.getMaxPos().y) * SegmentData.SEG, p.z);

			GL11.glVertex3f(p.x, p.y, (ship.getMinPos().z) * SegmentData.SEG);
			GL11.glVertex3f(p.x, p.y, (ship.getMaxPos().z) * SegmentData.SEG);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isXzPlaneEnabled()) {
			//x is not set
			GL11.glVertex3f((ship.getMinPos().x) * SegmentData.SEG, p.y, p.z);
			GL11.glVertex3f((ship.getMaxPos().x) * SegmentData.SEG, p.y, p.z);

		} else if(symmetryPlanes.isXyPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//y is not set
			GL11.glVertex3f(p.x, (ship.getMinPos().y) * SegmentData.SEG, p.z);
			GL11.glVertex3f(p.x, (ship.getMaxPos().y) * SegmentData.SEG, p.z);

		} else if(symmetryPlanes.isXzPlaneEnabled() && symmetryPlanes.isYzPlaneEnabled()) {
			//z is not set
			GL11.glVertex3f(p.x, p.y, (ship.getMinPos().z) * SegmentData.SEG);
			GL11.glVertex3f(p.x, p.y, (ship.getMaxPos().z) * SegmentData.SEG);
		}
		GL11.glEnd();

		GlUtil.glPopMatrix();
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glColor4f(1, 1, 1, 1f);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_BLEND);

		float size;
		if(symmetryPlanes.isXyPlaneEnabled()) {
			size = symmetryPlanes.getXyExtraDist() * 0.5f;
			drawCurrentSymetriePlanesElement(ship, null, SymmetryPlanes.MODE_XY, symmetryPlanes.getXyPlane(), size);
		}
		if(symmetryPlanes.isXzPlaneEnabled()) {
			size = symmetryPlanes.getXzExtraDist() * 0.5f;
			drawCurrentSymetriePlanesElement(ship, null, SymmetryPlanes.MODE_XZ, symmetryPlanes.getXzPlane(), size);
		}
		if(symmetryPlanes.isYzPlaneEnabled()) {
			size = symmetryPlanes.getYzExtraDist() * 0.5f;
			drawCurrentSymetriePlanesElement(ship, null, SymmetryPlanes.MODE_YZ, symmetryPlanes.getYzPlane(), size);
		}

	}

	private void drawReactorCoordinateSystems(SegmentController c) {
		if(c instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> con = ((ManagedSegmentController<?>) c).getManagerContainer();
			PowerInterface pw = con.getPowerInterface();
			if(pw.getActiveReactor() != null) {
				drawReactorCoordinateSystem(c, pw.getActiveReactor());
			}
		}
	}

	private void drawReactorCoordinateSystem(SegmentController c, ReactorTree reactor) {
		Transform add = new Transform();
		add.setIdentity();
		add.basis.set(reactor.getBonusMatrix());
		Vector3i pos = ElementCollection.getPosFromIndex(reactor.getCenterOfMass(), new Vector3i());
//		pos.x -= Segment.HALF_DIM;
//		pos.y -= Segment.HALF_DIM;
//		pos.z -= Segment.HALF_DIM;

		Vector3f min = new Vector3f(-1, -1, -1);
		Vector3f max = new Vector3f(+1, +1, +1);

		t.set(c.getWorldTransform());


		Vector4f zA = new Vector4f(Element.SIDE_COLORS[Element.BACK]);
		Vector4f zB = new Vector4f(Element.SIDE_COLORS[Element.FRONT]);
		Vector4f yA = new Vector4f(Element.SIDE_COLORS[Element.TOP]);
		Vector4f yB = new Vector4f(Element.SIDE_COLORS[Element.BOTTOM]);
		Vector4f xA = new Vector4f(Element.SIDE_COLORS[Element.LEFT]);
		Vector4f xB = new Vector4f(Element.SIDE_COLORS[Element.RIGHT]);
		float alpha = inReactorAlignSlider ? 0.2f : 0.00f;
		if(inReactorAlignAlwaysVisible) {
			alpha = 0.3f;
		}
		zA.w = alpha;
		zB.w = alpha;
		yA.w = alpha;
		yB.w = alpha;
		xA.w = alpha;
		xB.w = alpha;
		float shift = 0;
		Vector4f sel = switch(currentSelectedStabSide) {
			case (Element.BACK) -> zA;
			case (Element.FRONT) -> zB;
			case (Element.TOP) -> yA;
			case (Element.BOTTOM) -> yB;
			case (Element.LEFT) -> xA;
			case (Element.RIGHT) -> xB;
			default -> null;
		};
		if(sel != null) {
			alpha = 0.05f;
			zA.w = alpha;
			zB.w = alpha;
			yA.w = alpha;
			yB.w = alpha;
			xA.w = alpha;
			xB.w = alpha;

			sel.x += colorMod.getTime();
			sel.y += colorMod.getTime();
			sel.z += colorMod.getTime();
			sel.w = 0.3f + colorMod.getTime() * 0.3f;

			drawReactoAlignCross(add, pos, min, max, shift);
		} else if(inReactorAlignAlwaysVisible) {
			drawReactoAlignCross(add, pos, min, max, shift);
		} else if(inReactorAlignSliderSelectedAxis >= 0) {
			drawReactoAlignCross(add, pos, min, max, shift);
		}
		drawCurrentSymetriePlanesElement(c, add, SymmetryPlanes.MODE_XY, pos, shift, zA, zB, min, max);
		drawCurrentSymetriePlanesElement(c, add, SymmetryPlanes.MODE_XZ, pos, shift, yA, yB, min, max);
		drawCurrentSymetriePlanesElement(c, add, SymmetryPlanes.MODE_YZ, pos, shift, xA, xB, min, max);

		inReactorAlignSliderSelectedAxis = -1;
	}

	private void drawReactoAlignCross(Transform add, Vector3i pos, Vector3f min, Vector3f max, float shift) {
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(t);
		GlUtil.glTranslatef(pos.x - Segment.HALF_DIM + shift, pos.y - Segment.HALF_DIM + shift, pos.z - Segment.HALF_DIM + shift);
		GlUtil.glMultMatrix(add);

		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, 0.6f);
		GlUtil.glDisable(GL11.GL_CULL_FACE);


		GL11.glBegin(GL11.GL_LINES);
		if(inReactorAlignSliderSelectedAxis < 0 || inReactorAlignSliderSelectedAxis == SymmetryPlanes.MODE_YZ) {
			GL11.glVertex3f((min.x) * SegmentData.SEG + shift, 0, 0);
			GL11.glVertex3f((max.x) * SegmentData.SEG + shift, 0, 0);
		}
		if(inReactorAlignSliderSelectedAxis < 0 || inReactorAlignSliderSelectedAxis == SymmetryPlanes.MODE_XZ) {
			GL11.glVertex3f(0, (min.y) * SegmentData.SEG + shift, 0);
			GL11.glVertex3f(0, (max.y) * SegmentData.SEG + shift, 0);
		}
		if(inReactorAlignSliderSelectedAxis < 0 || inReactorAlignSliderSelectedAxis == SymmetryPlanes.MODE_XY) {
			GL11.glVertex3f(0, 0, (min.z) * SegmentData.SEG + shift);
			GL11.glVertex3f(0, 0, (max.z) * SegmentData.SEG + shift);
		}
		GL11.glEnd();
		GlUtil.glPopMatrix();
	}

	private void drawCurrentSymetriePlanesElement(SegmentController ship, Transform additional, int mode, Vector3i plane, float size) {
		float alpha = 0.3f;
		Vector3f min = new Vector3f(ship.getMinPos().x - 1, ship.getMinPos().y - 1, ship.getMinPos().z - 1);
		Vector3f max = new Vector3f(ship.getMaxPos().x + 1, ship.getMaxPos().y + 1, ship.getMaxPos().z + 1);
		Vector4f colorA = new Vector4f();
		Vector4f colorB = new Vector4f();
		if(mode == SymmetryPlanes.MODE_XY) {
			colorA.set(0, 0, 1, alpha);
		} else if(mode == SymmetryPlanes.MODE_XZ) {
			colorA.set(0, 1, 0, alpha);
		} else {
			colorA.set(1, 0, 0, alpha);
		}
		colorB.set(colorA);

		drawCurrentSymetriePlanesElement(ship, additional, mode, plane, size, colorA, colorB, min, max);
	}

	private void drawCurrentSymetriePlanesElement(SegmentController ship, Transform additional, int mode, Vector3i plane, float shift, Vector4f colorA, Vector4f colorB, Vector3f min, Vector3f max) {
		GlUtil.glDepthMask(false);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_CULL_FACE);

		float tx = 0.07f;
		Controller.getResLoader().getSprite("symm-plane").getMaterial().getTexture().attach(0);


		Vector3f maxTex = new Vector3f(max.x - min.x, max.y - min.y, max.z - min.z);

		t.set(ship.getWorldTransform());
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(t);
		GlUtil.glTranslatef(plane.x - Segment.HALF_DIM + shift, plane.y - Segment.HALF_DIM + shift, plane.z - Segment.HALF_DIM + shift);
		if(additional != null) {
			GlUtil.glMultMatrix(additional);
		}
		float minX = min.x * SegmentData.SEG;
		float maxX = max.x * SegmentData.SEG;

		float minY = min.y * SegmentData.SEG;
		float maxY = max.y * SegmentData.SEG;

		float minZ = min.z * SegmentData.SEG;
		float maxZ = max.z * SegmentData.SEG;
		if(mode == SymmetryPlanes.MODE_XY) {


			GL11.glBegin(GL11.GL_QUADS);

			GlUtil.glColor4f(colorA);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(minX, minY, 0);
			GL11.glTexCoord2f(0, maxTex.y / tx);
			GL11.glVertex3f(minX, maxY, 0);
			GL11.glTexCoord2f(maxTex.x / tx, maxTex.y / tx);
			GL11.glVertex3f(maxX, maxY, 0);
			GL11.glTexCoord2f(maxTex.x / tx, 0);
			GL11.glVertex3f(maxX, minY, 0);

			GlUtil.glColor4f(colorB);
			GL11.glTexCoord2f(maxTex.x / tx, 0);
			GL11.glVertex3f(maxX, minY, 0);
			GL11.glTexCoord2f(maxTex.x / tx, maxTex.y / tx);
			GL11.glVertex3f(maxX, maxY, 0);
			GL11.glTexCoord2f(0, maxTex.y / tx);
			GL11.glVertex3f(minX, maxY, 0);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(minX, minY, 0);

			GL11.glEnd();

		}
		if(mode == SymmetryPlanes.MODE_XZ) {


			GL11.glBegin(GL11.GL_QUADS);

			GlUtil.glColor4f(colorA);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(minX, 0, minZ);
			GL11.glTexCoord2f(0, maxTex.z / tx);
			GL11.glVertex3f(minX, 0, maxZ);
			GL11.glTexCoord2f(maxTex.x / tx, maxTex.z / tx);
			GL11.glVertex3f(maxX, 0, maxZ);
			GL11.glTexCoord2f(maxTex.x / tx, 0);
			GL11.glVertex3f(maxX, 0, minZ);


			GlUtil.glColor4f(colorB);
			GL11.glTexCoord2f(maxTex.x / tx, 0);
			GL11.glVertex3f(maxX, 0, minZ);
			GL11.glTexCoord2f(maxTex.x / tx, maxTex.z / tx);
			GL11.glVertex3f(maxX, 0, maxZ);
			GL11.glTexCoord2f(0, maxTex.z / tx);
			GL11.glVertex3f(minX, 0, maxZ);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(minX, 0, minZ);

			GL11.glEnd();

		}
		if(mode == SymmetryPlanes.MODE_YZ) {

			GL11.glBegin(GL11.GL_QUADS);

			GlUtil.glColor4f(colorA);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(0, minY, minZ);
			GL11.glTexCoord2f(0, maxTex.z / tx);
			GL11.glVertex3f(0, minY, maxZ);
			GL11.glTexCoord2f(maxTex.y / tx, maxTex.z / tx);
			GL11.glVertex3f(0, maxY, maxZ);
			GL11.glTexCoord2f(maxTex.y / tx, 0);
			GL11.glVertex3f(0, maxY, minZ);


			GlUtil.glColor4f(colorB);
			GL11.glTexCoord2f(maxTex.y / tx, 0);
			GL11.glVertex3f(0, maxY, minZ);
			GL11.glTexCoord2f(maxTex.y / tx, maxTex.z / tx);
			GL11.glVertex3f(0, maxY, maxZ);
			GL11.glTexCoord2f(0, maxTex.z / tx);
			GL11.glVertex3f(0, minY, maxZ);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex3f(0, minY, minZ);


			GL11.glEnd();
		}
		GlUtil.glPopMatrix();
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glColor4f(1, 1, 1, 1.0f);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_BLEND);

		GlUtil.glDepthMask(true);
	}

	private void drawOrientationArrow(Transform where, int blockOrientation) {
		BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		GlUtil.glPushMatrix();
		Mesh mesh = (Mesh) Controller.getResLoader().getMesh("Arrow").getChilds().get(0);
		Transform tran = new Transform(where);


		SegmentController.setConstraintFrameOrientation((byte) blockOrientation, tran, GlUtil.getRightVector(new Vector3f(), tran), GlUtil.getUpVector(new Vector3f(), tran), GlUtil.getForwardVector(new Vector3f(), tran));

		Vector3f f = new Vector3f(0, 0, 0.1f);//GlUtil.getForwardVector(new Vector3f(), tran);
		f.scale(linearTimer.getTime() / 5.0f);
		f.z -= 0.3f;

		tran.basis.transform(f);
		tran.origin.add(f);
		GlUtil.glMultMatrix(tran);

		//		GlUtil.translateModelview(f.x, f.y, f.z);
		GlUtil.scaleModelview(0.13f, 0.13f, 0.13f);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 1, 1, linearTimer.getTime() - 0.5f);

		mesh.draw();
		GlUtil.glPopMatrix();
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glColor4f(1, 1, 1, 1);
	}

	private Transform getToBuildTransform(SegmentController segCon) {
		if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
			CubeRayCastResult cubeResult = testRayCollisionPoint;
			if(cubeResult.getSegment() == null) {
				return null;
			}
			assert (segCon != null);
			assert (t != null);
			assert (segCon.getWorldTransform() != null);
			t.set(segCon.getWorldTransform());

			Vector3f p = new Vector3f(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
			p.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
			p.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
			p.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);

			Vector3f hitPoint = new Vector3f(testRayCollisionPoint.hitPointWorld);
			segCon.getWorldTransformInverse().transform(hitPoint);

			//			System.err.println("CHECKING COLLISION OF "+p+" AGAINST "+testRayCollisionPoint.hitPointWorld);

			pp.set((int) p.x, (int) p.y, (int) p.z);
			toBuildPos.set(pp.x + SegmentData.SEG_HALF, pp.y + SegmentData.SEG_HALF, pp.z + SegmentData.SEG_HALF);

			//			this.nextToBuildPos.set()

			BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();

			Vector3f s = new Vector3f(buildToolsManager.getWidth(), buildToolsManager.getHeight(), buildToolsManager.getDepth());

			float dir = 1;
			if(!PlayerInteractionControlManager.isAdvancedBuildMode(state)) {
				s.set(1, 1, 1);
			}
			Vector3f scale = new Vector3f();
			IntSet disabledSides = new IntOpenHashSet();
			for(int i = 0; i < 6; ++i) {
				Vector3i dir0 = Element.DIRECTIONSi[i];
				SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(new Vector3i(toBuildPos.x + dir0.x, toBuildPos.y + dir0.y, toBuildPos.z + dir0.z));
				if(piece != null && piece.getType() != Element.TYPE_NONE) {
					disabledSides.add(i);
				}
			}
			SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(toBuildPos);
			int side = Element.getSide(hitPoint, piece == null ? null : piece.getAlgorithm(), pp, piece != null ? piece.getType() : (short) 0, piece != null ? piece.getOrientation() : 0, disabledSides);
			currentSide = side;
			switch(side) {
				case (Element.RIGHT) -> {
					if(buildToolsManager.isAddMode()) {
						p.x += 1.0f;
					}
					toBuildPos.x += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.LEFT) -> {
					if(buildToolsManager.isAddMode()) {
						p.x -= 1.0f;
					}
					toBuildPos.x -= 1;
					scale.set(-s.x, s.y, s.z);
				}
				case (Element.TOP) -> {
					if(buildToolsManager.isAddMode()) {
						p.y += 1.0f;
					}
					toBuildPos.y += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.BOTTOM) -> {
					if(buildToolsManager.isAddMode()) {
						p.y -= 1.0f;
					}
					toBuildPos.y -= 1;
					scale.set(s.x, -s.y, s.z);
				}
				case (Element.FRONT) -> {
					if(buildToolsManager.isAddMode()) {
						p.z += 1.0f;
					}
					toBuildPos.z += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.BACK) -> {
					if(buildToolsManager.isAddMode()) {
						p.z -= 1.0f;
					}
					toBuildPos.z -= 1;
					scale.set(s.x, s.y, -s.z);
				}
				default -> {
				}
				//					System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
			}
			//			if(s.x > 1 || s.y > 1 || s.z > 1){
			p.x += (scale.x / 2) - (0.5f * Math.signum(scale.x));
			p.y += (scale.y / 2) - (0.5f * Math.signum(scale.y));
			p.z += (scale.z / 2) - (0.5f * Math.signum(scale.z));
			//			}

			Vector3f shipSpacePoint = new Vector3f(p);
			t.basis.transform(shipSpacePoint);
			t.origin.add(shipSpacePoint);

			return new Transform(t);

		}
		return null;
	}

	private boolean isDrawPreview() {
		BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		return EngineSettings.G_PREVIEW_TO_BUILD_BLOCK.isOn() && buildToolsManager.isAddMode() && !buildToolsManager.isCopyMode();
	}

	public Transform drawToBuildBox(SegmentController segCon, SingleBlockDrawer drawer, Shader shader, SelectionShader sInterface, boolean addMode) {


		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if(drawDebug) {
			GlUtil.printGlErrorCritical();
		}
		if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
			CubeRayCastResult cubeResult = testRayCollisionPoint;
			if(cubeResult.getSegment() == null) {
				return null;
			}
			assert (segCon != null);
			assert (t != null);
			assert (segCon.getWorldTransform() != null);
			t.set(segCon.getWorldTransform());

			Vector3f p = new Vector3f(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
			p.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
			p.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
			p.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);


			BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
			if(buildToolsManager.getBuildHelper() != null && !buildToolsManager.getBuildHelper().placed) {
				buildToolsManager.getBuildHelper().localTransform.origin.set(p);
			}
			if(buildToolsManager.isInCreateDockingMode() && buildToolsManager.getBuildToolCreateDocking().docker != null || buildToolsManager.isSelectMode()) {
				//Don't draw another box
				return null;
			}
			Vector3f hitPoint = new Vector3f(testRayCollisionPoint.hitPointWorld);
			segCon.getWorldTransformInverse().transform(hitPoint);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}

			pp.set((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.z));
			toBuildPos.set(pp.x + SegmentData.SEG_HALF, pp.y + SegmentData.SEG_HALF, pp.z + SegmentData.SEG_HALF);
			loockingAtPos.set(toBuildPos);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			Vector3f s = buildToolsManager.getSizef();

			float dir = 1;
			if(drawer == null) {
				if(!addMode) {
					if(buildToolsManager.isCopyMode()) {
						GlUtil.updateShaderVector4f(shader, "selectionColor", 0.6f, 0.6f, 0.04f, 1.0f);
					} else if(buildToolsManager.isPasteMode()) {
						GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.1f, 0.5f, 1.0f);
					} else {
						GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.1f, 0.1f, 1.0f);
					}
					s.x = -s.x;
					s.y = -s.y;
					s.z = -s.z;
				} else {
					GlUtil.updateShaderVector4f(shader, "selectionColor", 0.6f, 0.6f, 0.04f, 1.0f);
				}
			}

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(!PlayerInteractionControlManager.isAdvancedBuildMode(state)) {
				s.set(1, 1, 1);
			}

			float sizeMax = FastMath.max(Math.abs(s.x), Math.abs(s.y), Math.abs(s.z));
			Vector3f distance = new Vector3f(cubeResult.hitPointWorld);
			distance.sub(Controller.getCamera().getWorldTransform().origin);
			float barWidth = Math.min(0.1F, sizeMax / 100.0F);
			barWidth += Math.min(0.2F, distance.length() / 500.0F);

			Vector3f scale = new Vector3f();

			IntSet disabledSides = new IntOpenHashSet();
			for(int i = 0; i < 6; ++i) {
				Vector3i dir0 = Element.DIRECTIONSi[i];
				SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(new Vector3i(toBuildPos.x + dir0.x, toBuildPos.y + dir0.y, toBuildPos.z + dir0.z));
				if(piece != null && ElementKeyMap.isValidType(piece.getType())) {
					ElementInformation infoFast = ElementKeyMap.getInfoFast(piece.getType());
//					if(!infoFast.getBlockStyle().solidBlockStyle){
					disabledSides.add(i);
//					}
				}
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(toBuildPos);

			int side = Element.getSide(hitPoint, piece == null ? null : piece.getAlgorithm(), pp, piece != null ? piece.getType() : (short) 0, piece != null ? piece.getOrientation() : 0, disabledSides);
//			System.err.println("SIDE:: "+Element.getSideString((short)side)+"; "+disabledSides);
			switch(side) {
				case (Element.RIGHT) -> {
					if(addMode) {
						p.x += 1.0f;
					}
					toBuildPos.x += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.LEFT) -> {
					if(addMode) {
						p.x -= 1.0f;
					}
					toBuildPos.x -= 1;
					scale.set(-s.x, s.y, s.z);
				}
				case (Element.TOP) -> {
					if(addMode) {
						p.y += 1.0f;
					}
					toBuildPos.y += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.BOTTOM) -> {
					if(addMode) {
						p.y -= 1.0f;
					}
					toBuildPos.y -= 1;
					scale.set(s.x, -s.y, s.z);
				}
				case (Element.FRONT) -> {
					if(addMode) {
						p.z += 1.0f;
					}
					toBuildPos.z += 1;
					scale.set(s.x, s.y, s.z);
				}
				case (Element.BACK) -> {
					if(addMode) {
						p.z -= 1.0f;
					}
					toBuildPos.z -= 1;
					scale.set(s.x, s.y, -s.z);
				}
				default -> {
				}
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(buildToolsManager.isInCreateDockingMode()) {
				buildToolsManager.getBuildToolCreateDocking().potentialCreateDockPos = null;
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(buildToolsManager.getCopyArea() != null && buildToolsManager.isPasteMode()) {

				shader.unload();

				mesh.unloadVBO(true);

				Vector3f shipSpacePoint = new Vector3f(p);
				Transform gt = new Transform(t);
				gt.basis.transform(shipSpacePoint);
				gt.origin.add(shipSpacePoint);
				GlUtil.glPushMatrix();

				GlUtil.glMultMatrix(gt);

				buildToolsManager.getCopyArea().draw();

				GlUtil.glPopMatrix();

				mesh.loadVBO(true);

				shader.setShaderInterface(sInterface);
				shader.load();

				if(drawer == null) {
					if(!addMode) {
						if(buildToolsManager.isCopyMode()) {
							GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.8f, 0.2f, 1.0f);
						} else if(buildToolsManager.isPasteMode()) {
							GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.1f, 0.5f, 1.0f);
						} else {
							GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.1f, 0.1f, 1.0f);
						}
					} else {
						GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.77f, 0.1f, 1.0f);
					}
				}
			}
			GlUtil.glColor4f(1, 1, 1, 1.0f);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(buildToolsManager.isInCreateDockingMode()) {

				GlUtil.updateShaderVector4f(shader, "selectionColor", 0.7f, 0.1f, 0.1f, 1.0f);
				if(buildToolsManager.getBuildToolCreateDocking().docker == null) {

					SegmentPiece ppc = segCon.getSegmentBuffer().getPointUnsave(loockingAtPos);
					if(ppc != null && ElementKeyMap.isValidType(ppc.getType()) && ElementKeyMap.getInfoFast(ppc.getType()).isRailDockable()) {
						ElementInformation info = ElementKeyMap.getInfoFast(ppc.getType());
						Oriencube algo = (Oriencube) BlockShapeAlgorithm.getAlgo(info.getBlockStyle(), ppc.getOrientation());

						if(Element.switchLeftRight(algo.getOrientCubePrimaryOrientation()) == side) {
							GlUtil.updateShaderVector4f(shader, "selectionColor", 0.1f, 0.8f, 0.1f, 1.0f);
							buildToolsManager.getBuildToolCreateDocking().potentialCreateDockPos = new VoidUniqueSegmentPiece(ppc);
						}
					}
				} else {
					GlUtil.updateShaderVector4f(shader, "selectionColor", 0.1f, 0.1f, 0.9f, 1.0f);
				}
			}
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			if(EngineSettings.G_BASIC_SELECTION_BOX.isOn()) {
				Vector3f scaleL = new Vector3f(scale);
				Vector3f position = new Vector3f(p);
				Transform t2 = new Transform(t);

				//			if(s.x > 1 || s.y > 1 || s.z > 1){
				position.x += (scaleL.x / 2) - (0.5f * Math.signum(scaleL.x));
				position.y += (scaleL.y / 2) - (0.5f * Math.signum(scaleL.y));
				position.z += (scaleL.z / 2) - (0.5f * Math.signum(scaleL.z));

				//			}

				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
				Vector3f shipSpacePoint;


				shipSpacePoint = new Vector3f(position);
				t2.basis.transform(shipSpacePoint);
				t2.origin.add(shipSpacePoint);


				GlUtil.glPushMatrix();

				GlUtil.glMultMatrix(t2);

				if(addMode) {
					scaleL.scale(0.99993f);
				} else {
					scaleL.scale(1.00003f);
				}
				if(drawer == null) {
//				GlUtil.scaleModelview(scale.x, scale.y, scale.z);
					GlUtil.scaleModelview(scaleL.x, scaleL.y, scaleL.z);
				} else {
					//				GlUtil.scaleModelview(1.01f, 1.01f, 1.01f);
				}
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
				//			System.err.println("SCALE "+scale);

				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}

				if(drawer == null) {

					mesh.renderVBO();

				}


				GlUtil.glPopMatrix();
			} else {
				p.x -= (barWidth / 2.0F) * Math.signum(scale.x);
				p.y -= (barWidth / 2.0F) * Math.signum(scale.y);
				p.z -= (barWidth / 2.0F) * Math.signum(scale.z);

				//Draws a stretched out box (scaled mesh) for each of the 12 edges of the build box
				int last = 12;
				for(int i = 0; i < last; ++i) {

					Vector3f scaleL = new Vector3f(scale);
					Vector3f position = new Vector3f(p);
					Transform t2 = new Transform(t);

					switch(i) {
						case 0:
							position.x += (barWidth * Math.signum(scaleL.x));
							scaleL.x -= barWidth * Math.signum(scaleL.x);
							scaleL.y = barWidth * Math.signum(scaleL.y);
							scaleL.z = barWidth * Math.signum(scaleL.z);
							break;
						case 1:
							scaleL.x -= barWidth * Math.signum(scaleL.x);
							scaleL.y = barWidth * Math.signum(scaleL.y);
							scaleL.z = barWidth * Math.signum(scaleL.z);
							break;
						case 2:
							position.x += (barWidth * Math.signum(scaleL.x));
							scaleL.x -= barWidth * Math.signum(scaleL.x);
							scaleL.y = barWidth * Math.signum(scaleL.y);
							scaleL.z = barWidth * Math.signum(scaleL.z);
							break;
						case 3:
							position.x += (barWidth * Math.signum(scaleL.x));
							scaleL.x -= barWidth * Math.signum(scaleL.x);
							scaleL.y = barWidth * Math.signum(scaleL.y);
							scaleL.z = barWidth * Math.signum(scaleL.z);
							break;
						case 4:
						case 5:
						case 6:
							position.z += (barWidth * Math.signum(scaleL.z));
						case 7:
							scaleL.z -= barWidth * Math.signum(scaleL.z);
							scaleL.y = barWidth * Math.signum(scaleL.y);
							scaleL.x = barWidth * Math.signum(scaleL.x);
							break;
						case 8:
						case 9:
						case 10:
						case 11:
							scaleL.y += barWidth * Math.signum(scaleL.y);
							scaleL.x = barWidth * Math.signum(scaleL.x);
							scaleL.z = barWidth * Math.signum(scaleL.z);
							break;
					}

					//			if(s.x > 1 || s.y > 1 || s.z > 1){
					position.x += (scaleL.x / 2) - (0.5f * Math.signum(scaleL.x));
					position.y += (scaleL.y / 2) - (0.5f * Math.signum(scaleL.y));
					position.z += (scaleL.z / 2) - (0.5f * Math.signum(scaleL.z));

					if(i == 1) {
						position.y += scale.y;
						position.x += barWidth * Math.signum(scaleL.x);
					} else if(i == 2) {
						position.z += scale.z;
					} else if(i == 3) {
						position.y += scale.y;
						position.z += scale.z;
					} else if(i == 5) {
						position.x += scale.x;
					} else if(i == 6) {
						position.y += scale.y;
					} else if(i == 7) {
						position.x += scale.x;
						position.y += scale.y;
						position.z += barWidth * Math.signum(scaleL.z);
					} else if(i == 9) {
						position.x += scale.x;
					} else if(i == 10) {
						position.z += scale.z;
					} else if(i == 11) {
						position.x += scale.x;
						position.z += scale.z;
					}

					//			}
					if(drawDebug) {
						GlUtil.printGlErrorCritical();
					}
					Vector3f shipSpacePoint;

					shipSpacePoint = new Vector3f(position);
					t2.basis.transform(shipSpacePoint);
					t2.origin.add(shipSpacePoint);

					GlUtil.glPushMatrix();

					GlUtil.glMultMatrix(t2);

					if(addMode) {
						scaleL.scale(0.99993f);
					} else {
						scaleL.scale(1.00003f);
					}
					if(drawer == null) {
//				GlUtil.scaleModelview(scale.x, scale.y, scale.z);
						GlUtil.scaleModelview(scaleL.x, scaleL.y, scaleL.z);
					} else {
						//				GlUtil.scaleModelview(1.01f, 1.01f, 1.01f);
					}
					if(drawDebug) {
						GlUtil.printGlErrorCritical();
					}
					//			System.err.println("SCALE "+scale);

					if(drawDebug) {
						GlUtil.printGlErrorCritical();
					}

					if(drawer == null) {

						mesh.renderVBO();

					}

					GlUtil.glColor4f(1, 1, 1, 1.0f);

					GlUtil.glPopMatrix();
				}

				p.x += (barWidth / 2.0F) * Math.signum(scale.x);
				p.y += (barWidth / 2.0F) * Math.signum(scale.y);
				p.z += (barWidth / 2.0F) * Math.signum(scale.z);
			}

			//			}

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}

			p.x += (scale.x / 2) - (0.5f * Math.signum(scale.x));
			p.y += (scale.y / 2) - (0.5f * Math.signum(scale.y));
			p.z += (scale.z / 2) - (0.5f * Math.signum(scale.z));

			Vector3f shipSpacePoint = new Vector3f(p);
			t.basis.transform(shipSpacePoint);
			t.origin.add(shipSpacePoint);

			GlUtil.glPushMatrix();

			GlUtil.glMultMatrix(t);

			if(addMode) {
				scale.scale(0.99993f);
			} else {
				scale.scale(1.00003f);
			}

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			//			System.err.println("SCALE "+scale);

			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}


			if(drawer != null && addMode) {
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
				boolean wasLoaded = mesh.isVboLoaded();
				if(mesh.isVboLoaded()) {
					mesh.unloadVBO(true);
				}
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
				short type = getPlayerIntercationManager().getSelectedTypeWithSub();
				if(ElementKeyMap.isValidType(type)) {

					drawer.alpha = 0.5f;
					ElementInformation info = ElementKeyMap.getInfo(type);
					if(info.getBlockStyle() != BlockStyle.NORMAL) {
						drawer.setSidedOrientation((byte) 0);
						drawer.setShapeOrientation24((byte) getPlayerIntercationManager().getBlockOrientation());
					} else if(ElementKeyMap.getInfo(type).getIndividualSides() > 3) {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation((byte) getPlayerIntercationManager().getBlockOrientation());
					} else if(ElementKeyMap.getInfo(type).orientatable) {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation((byte) getPlayerIntercationManager().getBlockOrientation());
					} else {
						drawer.setShapeOrientation24((byte) 0);
						drawer.setSidedOrientation((byte) 0);
					}

					if(drawDebug) {
						GlUtil.printGlErrorCritical();
					}
					//					drawer.activateBlinkingOrientation(ElementKeyMap.getInfo(type).getIndividualSides() < 4
					//							&& ElementKeyMap.getInfo(type).isOrientatable());
					drawer.activateBlinkingOrientation(ElementKeyMap.getInfo(type).isOrientatable());
					if(drawDebug) {
						GlUtil.printGlErrorCritical();
					}
					GL11.glCullFace(GL11.GL_BACK);
					drawer.useSpriteIcons = false;
					drawer.drawType(type, t);
					drawer.useSpriteIcons = true;
				}
				if(drawDebug) {
					GlUtil.printGlErrorCritical();
				}
				if(wasLoaded) {
					mesh.loadVBO(true);
				}
			}

			GlUtil.glColor4f(1, 1, 1, 1.0f);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_BLEND);
			GL11.glCullFace(GL11.GL_BACK);
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glPopMatrix();

			return new Transform(t);
			//			System.err.println("SIDE: "+Element.getSideString(Element.getSide(testRayCollisionPoint.hitPointWorld, new Vector3i(p.x, p.y, p.z))));

		}
		return null;
	}

	private void drawCameraHighlight(SegmentController segCon) {

		BuildToolsManager btm = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		if(btm.isSelectMode()) {
			BuildSelection selection = btm.getSelectMode();
			selection.draw(state, segCon, mesh, selectionShader);

		}


	}

	private void drawCreateDock(SegmentController segCon) {
		BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
		if(buildToolsManager.isInCreateDockingMode() && buildToolsManager.getBuildToolCreateDocking().docker != null) {


			{
				VoidUniqueSegmentPiece docker = buildToolsManager.getBuildToolCreateDocking().docker;

				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

				Transform dT = null;
				dT = new Transform(segCon.getWorldTransform());
				Vector3f mP = docker.getAbsolutePos(new Vector3f());
				mP.x -= SegmentData.SEG_HALF;
				mP.y -= SegmentData.SEG_HALF;
				mP.z -= SegmentData.SEG_HALF;
				dT.basis.transform(mP);
				dT.origin.add(mP);


				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(dT);
				if(drawer == null) {


					GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.1f, 0.3f, 0.9f, 0.65f);
					mesh.renderVBO();

				} else {
					boolean wasLoaded = mesh.isVboLoaded();
					if(mesh.isVboLoaded()) {
						mesh.unloadVBO(true);
					}
					drawer.alpha = 0.5f;

					drawer.setSidedOrientation((byte) 0);

//					System.err.println("HH "+docker.getAlgorithm());

					byte o = BlockShapeAlgorithm.getLocalAlgoIndex(ElementKeyMap.getInfo(docker.getType()).getBlockStyle(), docker.getOrientation());

					drawer.setShapeOrientation24((docker.getOrientation())); //+ (docker.isActive() ? 0 : SegmentData.MAX_ORIENT)

					drawer.useSpriteIcons = false;
					drawer.drawType(buildToolsManager.getBuildToolCreateDocking().docker.getType(), dT);
					drawer.useSpriteIcons = true;

					if(wasLoaded) {
						mesh.loadVBO(true);
					}


					drawer.setActive(false);
				}

				GlUtil.glColor4f(1, 1, 1, 1.0f);
				GlUtil.glEnable(GL11.GL_LIGHTING);
				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glPopMatrix();
			}

			{

				VoidUniqueSegmentPiece core;
				boolean draw = true;
				if(buildToolsManager.getBuildToolCreateDocking().core == null) {
					core = new VoidUniqueSegmentPiece();
					core.uniqueIdentifierSegmentController = segCon.getUniqueIdentifier();
					core.setType(ElementKeyMap.CORE_ID);


					Vector3f pos = new Vector3f(Controller.getCamera().getPos());
					Vector3f forw = Controller.getCamera().getForward(new Vector3f());


					forw.scale(buildToolsManager.getBuildToolCreateDocking().coreDistance);

					pos.add(forw);

					segCon.getWorldTransformInverse().transform(pos);


					pos.x = FastMath.round(pos.x) + SegmentData.SEG_HALF;
					pos.y = FastMath.round(pos.y) + SegmentData.SEG_HALF;
					pos.z = FastMath.round(pos.z) + SegmentData.SEG_HALF;

					core.voidPos.set(new Vector3i(pos));


					SegmentPiece pointUnsave = segCon.getSegmentBuffer().getPointUnsave(core.voidPos);

					if(pointUnsave != null && ElementKeyMap.isValidType(pointUnsave.getType()) || core.voidPos.equals(buildToolsManager.getBuildToolCreateDocking().docker.voidPos)) {
						draw = false;
						buildToolsManager.getBuildToolCreateDocking().potentialCore = null;
					} else {
						core.setSegmentController(segCon);
						buildToolsManager.getBuildToolCreateDocking().potentialCore = core;
					}


				} else {
					core = buildToolsManager.getBuildToolCreateDocking().core;
				}


				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glDisable(GL11.GL_LIGHTING);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

				Transform dT = null;
				dT = new Transform(segCon.getWorldTransform());
				Vector3f mP = core.getAbsolutePos(new Vector3f());
				mP.x -= SegmentData.SEG_HALF;
				mP.y -= SegmentData.SEG_HALF;
				mP.z -= SegmentData.SEG_HALF;
				dT.basis.transform(mP);
				dT.origin.add(mP);


				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(dT);
//				if(drawer == null){
				ShaderLibrary.selectionShader.setShaderInterface(selectionShader);
				ShaderLibrary.selectionShader.load();

				GlUtil.updateShaderVector4f(ShaderLibrary.selectionShader, "selectionColor", 0.1f, 0.9f, 0.6f, 0.65f);
				mesh.renderVBO();

//				}else{
//					boolean wasLoaded = mesh.isVboLoaded();
//					if (mesh.isVboLoaded()) {
//						mesh.unloadVBO(true);
//					}
//					drawer.alpha = 0.5f;
//
//					drawer.setSidedOrientation((byte) 0);
//
//					byte o = BlockShapeAlgorithm.getLocalAlgoIndex(ElementKeyMap.getInfo(core.getType()).getBlockStyle(),
//							core.getOrientation(), core.isActive());
//
//					drawer.setShapeOrientation(core.getOrientation());
//					drawer.setActive(core.isActive());
//
//					drawer.useSpriteIcons = false;
//					drawer.drawType(buildToolsManager.getBuildToolCreateDocking().docker.getType(), dT);
//					drawer.useSpriteIcons = true;
//
//					if (wasLoaded) {
//						mesh.loadVBO(true);
//					}
//
//
//					drawer.setActive(false);
//				}

				GlUtil.glColor4f(1, 1, 1, 1.0f);
				GlUtil.glEnable(GL11.GL_LIGHTING);
				GlUtil.glDisable(GL11.GL_BLEND);
				GlUtil.glPopMatrix();
			}
		}

	}

	private Transform drawToBuildConnection(SegmentController segCon) {
		if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {

			CubeRayCastResult cubeResult = testRayCollisionPoint;
			if(cubeResult.getSegment() == null) {
				return null;
			}
			if(PlayerInteractionControlManager.isAdvancedBuildMode(state)) {
				return null;
			}
			t.set(segCon.getWorldTransform());

			Vector3f pTo = new Vector3f(cubeResult.getSegment().pos.x, cubeResult.getSegment().pos.y, cubeResult.getSegment().pos.z);
			pTo.x += (cubeResult.getCubePos().x - SegmentData.SEG_HALF);
			pTo.y += (cubeResult.getCubePos().y - SegmentData.SEG_HALF);
			pTo.z += (cubeResult.getCubePos().z - SegmentData.SEG_HALF);

			short selectedType = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();

			Vector3f pFrom = new Vector3f();
			SegmentBuildController activeBuildController = getActiveBuildController();
			SegmentPiece sBlock = activeBuildController.getSelectedBlock();
			if(sBlock != null && sBlock.getType() != Element.TYPE_NONE && selectedType != Element.TYPE_NONE) {
				Vector3i absolutePos = sBlock.getAbsolutePos(new Vector3i());
				pFrom.set(absolutePos.x - SegmentData.SEG_HALF, absolutePos.y - SegmentData.SEG_HALF, absolutePos.z - SegmentData.SEG_HALF);
			} else {
				return null;
			}

			Vector3f hitPoint = new Vector3f(testRayCollisionPoint.hitPointWorld);
			segCon.getWorldTransformInverse().transform(hitPoint);

			//			System.err.println("CHECKING COLLISION OF "+p+" AGAINST "+testRayCollisionPoint.hitPointWorld);

			float dir = 1;

			Vector3f scale = new Vector3f();
			IntSet disabledSides = new IntOpenHashSet();
			for(int i = 0; i < 6; ++i) {
				Vector3i dir0 = Element.DIRECTIONSi[i];
				SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(new Vector3i(toBuildPos.x + dir0.x, toBuildPos.y + dir0.y, toBuildPos.z + dir0.z));
				if(piece != null && piece.getType() != Element.TYPE_NONE) {
					disabledSides.add(i);
				}
			}
			SegmentPiece piece = segCon.getSegmentBuffer().getPointUnsave(toBuildPos);
			if(piece != null) {
				switch(Element.getSide(hitPoint, piece == null ? null : piece.getAlgorithm(), pp, piece != null ? piece.getType() : (short) 0, piece != null ? piece.getOrientation() : 0, disabledSides)) {
					case (Element.RIGHT) -> pTo.x += 1.0f;
					case (Element.LEFT) -> pTo.x -= 1.0f;
					case (Element.TOP) -> pTo.y += 1.0f;
					case (Element.BOTTOM) -> pTo.y -= 1.0f;
					case (Element.FRONT) -> pTo.z += 1.0f;
					case (Element.BACK) -> pTo.z -= 1.0f;
					default -> {
					}
					//					System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
				}
			}

			//			System.err.println("CHECKING COLLISION OF "+p+" AGAINST "+testRayCollisionPoint.hitPointWorld);

			pp.set((int) pTo.x, (int) pTo.y, (int) pTo.z);

			GlUtil.glPushMatrix();

			GlUtil.glMultMatrix(t);
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			//			System.err.println("SCALE "+scale);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glLineWidth(4);

			if(ElementKeyMap.isValidType(selectedType) && ElementKeyMap.getInfo(selectedType).getControlledBy().contains(sBlock.getType())) {
				GlUtil.glColor4f(0, 0.8f, 0, 1.0f);
			} else {
				GlUtil.glColor4f(0.8f, 0, 0, 0.6f);
			}

			//			System.err.println("DRAWING LINE FROM "+pFrom+" to "+pTo);

			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3f(pFrom.x, pFrom.y, pFrom.z);
			GL11.glVertex3f(pTo.x, pTo.y, pTo.z);
			GL11.glEnd();
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			GL11.glLineWidth(2);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);

			GlUtil.glPopMatrix();
			if(drawDebug) {
				GlUtil.printGlErrorCritical();
			}
			return new Transform(t);
			//			System.err.println("SIDE: "+Element.getSideString(Element.getSide(testRayCollisionPoint.hitPointWorld, new Vector3i(p.x, p.y, p.z))));

		}
		return null;
	}

	private void endBlockDraw() {
		GlUtil.glColor4f(1, 1, 1, 1.0f);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glPopMatrix();
	}

	public void flagControllerSetChanged() {
	}

	public void flagUpdate() {
		flagUpdate = true;
	}

	public SegmentBuildController getActiveBuildController() {
		if(getSegmentControlManager().getSegmentBuildController().isTreeActive()) {
			return getSegmentControlManager().getSegmentBuildController();
		} else if(getShipControllerManager().getSegmentBuildController().isTreeActive()) {
			return getShipControllerManager().getSegmentBuildController();
		}
		return null;
	}

	public PlayerInteractionControlManager getPlayerIntercationManager() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	public PlayerExternalController getPlayerManager() {
		return getPlayerIntercationManager().getPlayerCharacterManager();
	}

	public SegmentControlManager getSegmentControlManager() {
		return getPlayerIntercationManager().getSegmentControlManager();
	}

	public ShipControllerManager getShipControllerManager() {
		return getPlayerIntercationManager().getInShipControlManager().getShipControlManager();
	}

	private void prepareBlockDraw(Transform t) {

		this.t.set(t);

		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glColor4f(1, 0, 1, 0.6f);

		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(t);
	}

	private void textPopups() {

//		if(selectedInfo != null){
//			String sel = "SELECTED:\n"+selectedInfo.getName()+(deselectable ? "\ndeselect with "+KeyboardMappings.SELECT_MODULE.getKeyChar() : "");
//			if(selectedBlock != null && selectedInfo.getBlockStyle() != BlockStyle.NORMAL){
//				sel+="\n"+BlockShapeAlgorithm.getAlgo(selectedInfo.getBlockStyle(), selectedBlock.getOrientation(), selectedBlock.isActive()).getClass().getSimpleName();
//			}
//			state.getController().popupSelectedTextMessage(sel, 0);
//		}else{
//			state.getController().deactivateSelectedTextMessage();
//		}
//
//		if(currentInfo != null){
//			String c = currentInfo.getName()+(connectableToSelected ? "\n\n(dis)connect to "+selectedInfo.getName()+"\nwith "+KeyboardMappings.CONNECT_MODULE.getKeyChar() : "")+(selectable ? "\nselect with "+KeyboardMappings.CONNECT_MODULE.getKeyChar() : "");
//			if(currentPiece != null && currentInfo.getBlockStyle() != BlockStyle.NORMAL){
//				c+="\n"+BlockShapeAlgorithm.getAlgo(currentInfo.getBlockStyle(), currentPiece.getOrientation(), currentPiece.isActive()).getClass().getSimpleName();
//			}
//			state.getController().popupInviewTextMessage(c, 0);
//		}else{
//			state.getController().deactivateInviewTextMessage();
//		}
	}

	public void update(Timer timer) {
		HudIndicatorOverlay.toDrawTexts.remove(indication);
		if(!getSegmentControlManager().getSegmentBuildController().isTreeActive() && !getShipControllerManager().getSegmentBuildController().isTreeActive() && !getPlayerManager().isActive()) {
			return;
		}

		if(state.getCharacter() == null) {
			return;
		}

		if(flagUpdate) {
			lastSegment = null;
			currentPiece = null;
			currentInfo = null;
			flagUpdate = false;
		}
		colorMod.update(timer);
		linearTimer.update(timer);
		linearTimerC.update(timer);
		linearTimerSl.update(timer);
		try {

			SegmentBuildController activeBuildController = getActiveBuildController();

			Vector3f camPos = new Vector3f(Controller.getCamera().getPos());
			if(activeBuildController == null && state.getCharacter() == state.getCurrentPlayerObject()) {
				camPos.set(state.getCharacter().getHeadWorldTransform().origin);
			}

			Vector3f forw = new Vector3f(Controller.getCamera().getForward());
			if(Float.isNaN(forw.x)) {
				return;
			}
			if(PlayerInteractionControlManager.isAdvancedBuildMode(state)) {
				Vector3f mouseTo = new Vector3f(state.getWorldDrawer().getAbsoluteMousePosition());
				forw.sub(mouseTo, camPos);
			}
			forw.normalize();
			forw.scale(activeBuildController != null ? SegmentBuildController.EDIT_DISTANCE : PlayerExternalController.EDIT_DISTANCE);
			Vector3f camTo = new Vector3f(camPos);
			camTo.add(forw);


			PlayerCharacter owner = state.getCharacter(); //dont collide with own character
			SegmentController filter = state.getCurrentPlayerObject() instanceof SegmentController ? ((SegmentController) state.getCurrentPlayerObject()) : null;

			if(filter != null) {
				testRayCollisionPoint = new CubeRayCastResult(camPos, camTo, owner, filter);
			} else {
				testRayCollisionPoint = new CubeRayCastResult(camPos, camTo, owner);
			}

//			testRayCollisionPoint.setDebug(true);

			testRayCollisionPoint.setDamageTest(false);
			testRayCollisionPoint.setIgnoereNotPhysical(true);
			testRayCollisionPoint.setIgnoreDebris(true);
			testRayCollisionPoint.setZeroHpPhysical(true);
			testRayCollisionPoint.setCheckStabilizerPaths(false);
			testRayCollisionPoint.setHasCollidingBlockFilter(false);
			testRayCollisionPoint.setCollidingBlocks(null);

//			testRayCollisionPoint = ((PhysicsExt) state.getPhysics()).testRayCollisionPoint(
//					camPos, camTo, false, owner, filter, true, true, false);

			state.getPhysics().getDynamicsWorld().rayTest(camPos, camTo, testRayCollisionPoint);

			if(testRayCollisionPoint.collisionObject != null && !(testRayCollisionPoint.collisionObject instanceof RigidBodySegmentController)) {
				//collision with non cube
				//clear hit segment
				testRayCollisionPoint.setSegment(null);
			}


//			System.err.println("CURRENT:: "+filter+"; "+testRayCollisionPoint.hasHit());
			if(testRayCollisionPoint != null && testRayCollisionPoint.hasHit() && testRayCollisionPoint instanceof CubeRayCastResult) {
				CubeRayCastResult cubeResult = testRayCollisionPoint;
				if(cubeResult.collisionObject instanceof PairCachingGhostObjectAlignable) {
					currentObject = ((PairCachingGhostObjectAlignable) cubeResult.collisionObject).getObj();
					currentPiece = null;
					currentInfo = null;
//					System.err.println("HIT ASTRO");
				} else if(cubeResult.getSegment() != null) {
					if((cubeResult.getSegment() != null && lastSegment != null && !cubeResult.getSegment().equals(lastSegment)) || !cubeResult.getCubePos().equals(lastCubePos) || currentPiece == null) {

						lastCubePos.set(cubeResult.getCubePos());
						lastSegment = cubeResult.getSegment();

						currentObject = lastSegment.getSegmentController();
						currentPiece = new SegmentPiece(lastSegment, lastCubePos);
						currentInfo = ElementKeyMap.getInfo(currentPiece.getType());


					} else {
//						System.err.println("NO CHANGE: "+currentInfo);
					}
				} else {
//					System.err.println("NO CUBE");
					currentPiece = null;
					currentInfo = null;
					currentObject = null;

				}
			} else {
//				System.err.println("NO HIT");
				currentObject = null;
				currentPiece = null;
				currentInfo = null;
			}
//			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && currentInfo == null) {
//				System.err.println("NO INFO");
//			}
			if(currentInfo != null) {
				if(currentInfo.isArmor()) {
					retrieveArmorInfo(currentPiece.getSegmentController(), currentPiece, new Vector3f(camPos), new Vector3f(forw));
				} else {
					armorValue.reset();
				}

				if(Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && Controller.getCamera().getCameraOffset() < 1) {
//					System.err.println("IND "+currentInfo);

					indication.setText(Lng.str("%s [%s]\n%s; RMass: %s;\n%s\n%s", currentInfo.getId(), currentPiece, currentPiece.getSegmentController().getUniqueIdentifier(), StringTools.formatPointZero(currentPiece.getSegmentController().railController.calculateRailMassIncludingSelf()), touching.toString(), (currentPiece.getSegmentController().isUsingOldPower() ? "[OLD POWER]" : "[NEW POWER]")));
					currentPiece.getTransform(indication.getCurrentTransform());
					HudIndicatorOverlay.toDrawTexts.add(indication);
				} else if(state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager().buildInfo) {

					indication.setText(Lng.str("%s [%s] %s [%d/%d HP]", currentInfo.getName(), Element.getSideString(currentPiece.getOrientation()), currentPiece.getAbsolutePos(new Vector3i()).toString(), currentPiece.getHitpointsFull(), currentInfo.getMaxHitPointsFull()));

					currentPiece.getTransform(indication.getCurrentTransform());
					HudIndicatorOverlay.toDrawTexts.add(indication);
				}/*else if (currentInfo.getId() == ElementKeyMap.REACTOR_STABILIZER && currentPiece != null && currentPiece.getSegmentController() instanceof ManagedSegmentController<?>) {

					ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>)currentPiece.getSegmentController()).getManagerContainer();
					if(managerContainer.isUsingPowerReactors()){
						float dist = managerContainer.getPowerInterface().getStabilizerDistance(currentPiece.getAbsoluteIndex());
						double optDist = managerContainer.getPowerInterface().getReactorOptimalDistance();
						double efficiency = managerContainer.getPowerInterface().getStabilizerEfficiency(currentPiece.getAbsoluteIndex());
						double efficiencyTotal = managerContainer.getPowerInterface().getStabilizerEfficiencyTotal();

						indication.setText(Lng.str("Distance %s / Optimal %s; Efficiency Block %s%%; Total: %s%%", StringTools.formatPointZeroZeroZero(dist), StringTools.formatPointZeroZeroZero(optDist), StringTools.formatPointZero(efficiency*100f), StringTools.formatPointZero(efficiencyTotal*100f)));
						currentPiece.getTransform(indication.getCurrentTransform());
						HudIndicatorOverlay.toDrawTexts.add(indication);
					}
				} */

			} else if(currentObject != null) {
				if(currentObject instanceof AbstractCharacter<?>) {
					if(currentObject instanceof AICreature<?>) {
						//						((AICreature<?>) currentObject).getRealName()
						indication.setText(Lng.str("[%s]: Talk to %s", KeyboardMappings.ACTIVATE.getKeyChar(), ((AICreature<?>) currentObject).getRealName()));
						indication.getCurrentTransform().set(currentObject.getWorldTransformOnClient());
						HudIndicatorOverlay.toDrawTexts.add(indication);
					}
				}
			}
			if(currentPiece == null) {
				armorValue.reset();
			}
			if(activeBuildController == null) {
				return;
			}

			if(activeBuildController.getSelectedBlock() != null) {

				activeBuildController.getSelectedBlock().refresh();
				if(selectedBlock != activeBuildController.getSelectedBlock() && activeBuildController.getSelectedBlock().getType() != Element.TYPE_NONE) {
					selectedBlock = activeBuildController.getSelectedBlock();
					selectedInfo = ElementKeyMap.getInfo(selectedBlock.getType());
				} else if(activeBuildController.getSelectedBlock().getType() == Element.TYPE_NONE) {
					selectedBlock = null;
					selectedInfo = null;
				}
			} else {
				selectedBlock = null;
				selectedInfo = null;
			}
			if(selectedBlock != null) {
				short type = selectedBlock.getType();
				if(type != Element.TYPE_NONE && currentInfo != null) {
					if(selectedInfo != null) {
						try {
							if(currentInfo.getControlledBy().contains(type)) {
							}
						} catch(ElementClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					if(currentInfo.isController()) {
					}
				}
			}
			if(selectedInfo != null && currentInfo == selectedInfo) {
			}
			textPopups();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("[BUILDMODEDRAWER] " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}

//		System.err.println("IND::: "+HudIndicatorOverlay.toDrawTexts.size());
	}

	private void retrieveArmorInfo(SegmentController c, SegmentPiece cp, Vector3f camPos, Vector3f forw) {
		Vector3f camTo = new Vector3f(camPos);
		forw.normalize();
		forw.scale(400);
		camTo.add(forw);

		if(cPosA.epsilonEquals(camPos, 0.1f) && cPosB.epsilonEquals(camTo, 4.0f) && (state.getUpdateTime() - lastArmorCheck) < 1000) {
			return;
		}

		lastArmorCheck = state.getUpdateTime();
		cPosA.set(camPos);
		cPosB.set(camTo);


		rayCallbackTraverse.closestHitFraction = 1.0f;
		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);

		rayCallbackTraverse.rayFromWorld.set(camPos);
		rayCallbackTraverse.rayToWorld.set(camTo);

		rayCallbackTraverse.setFilter(c); //filter for performance since inital check already succeeded
		rayCallbackTraverse.setOwner(state.getCharacter());
		rayCallbackTraverse.setIgnoereNotPhysical(false);
		rayCallbackTraverse.setIgnoreDebris(false);
		rayCallbackTraverse.setRecordAllBlocks(false);
		rayCallbackTraverse.setZeroHpPhysical(false); //dont hit 0 hp blocks
		rayCallbackTraverse.setDamageTest(true);
		rayCallbackTraverse.setCheckStabilizerPaths(false); //hit stablizer paths
		rayCallbackTraverse.setSimpleRayTest(true);

		pt.armorValue = armorValue;
		armorValue.reset();

		c.getPhysics().getDynamicsWorld().rayTest(camPos, camTo, rayCallbackTraverse);

		if(armorValue.typesHit.size() > 0) {
			armorValue.calculate();
		}


		rayCallbackTraverse.collisionObject = null;
		rayCallbackTraverse.setSegment(null);
		rayCallbackTraverse.setFilter();

	}

}