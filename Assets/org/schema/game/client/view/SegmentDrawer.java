package org.schema.game.client.view;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SegmentDrawListener;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Matrix4fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.BuildToolsManager;
import org.schema.game.client.controller.manager.ingame.PlayerGameControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.beam.BeamDrawer;
import org.schema.game.client.view.cubes.CubeData;
import org.schema.game.client.view.cubes.CubeDataPool;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshInterface;
import org.schema.game.client.view.cubes.cubedyn.CubeMeshDynOpt;
import org.schema.game.client.view.cubes.cubedyn.DrawMarker;
import org.schema.game.client.view.cubes.cubedyn.LODCubeMeshManagerBulkOptimized;
import org.schema.game.client.view.cubes.lodshapes.LodDraw;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.effects.Shadow;
import org.schema.game.client.view.effects.ShieldDrawer;
import org.schema.game.client.view.effects.segmentcontrollereffects.RunningEffect;
import org.schema.game.client.view.effects.segmentcontrollereffects.SegmentControllerEffectDrawer;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13;
import org.schema.game.client.view.textbox.AbstractTextBox;
import org.schema.game.client.view.textbox.Replacement;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.*;
import org.schema.game.common.data.world.space.PlanetIcoCore;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.TransformableSubSprite;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.Keyboard;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.*;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.*;

public class SegmentDrawer implements Drawable {

	public static final SegmentOcclusion segmentOcclusion = new SegmentOcclusion();
	public static final int DISTANCE_VIEWER = 0;
	public static final int DISTANCE_CAMERA = 1;
	public static final int LIGHTING_THREAD_COUNT = 3;
	public static final float ORDERED_DRAW_DIST_OPAUQ_THRESHOLD = 100;
	public static final long WARNING_MARGIN = 50;
	private static final int ADDITIVE = 1;
	private static final int MULTIPLICATIVE = 2;
	private static final LODCubeMeshManagerBulkOptimized[] LODCubeManager = {new LODCubeMeshManagerBulkOptimized(), new LODCubeMeshManagerBulkOptimized()};
	private static final float BLENDED_NEAR_THRESH = 30;
	private static final float BLENDED_NEAR_THRESH_SQUARED = BLENDED_NEAR_THRESH * BLENDED_NEAR_THRESH;
	private static final float ORDERED_DRAW_DIST_OPAUQ_THRESHOLD_SQUARED = ORDERED_DRAW_DIST_OPAUQ_THRESHOLD * ORDERED_DRAW_DIST_OPAUQ_THRESHOLD;
	public static int distanceMode = DISTANCE_CAMERA;
	public static boolean forceFullLightingUpdate;
	public static CubeDataPool dataPool;
	public static boolean directModelview;
	public static boolean seperateDrawing = true;
	public static boolean reinitializeMeshes;
	public static CubeMeshQuadsShader13 shader;
	public static boolean drawLOD;
	public static boolean drawNormal = true;
	public static float LOD_THRESH_SQUARED;
	static int threadCount;
	static int meshCounter;
	private static final Transform beamTmp = new Transform();
	private static final int blendFunc = 0;

	static {
		beamTmp.setIdentity();
	}

	protected final ObjectArrayList<DrawableRemoteSegment> generatedSegments = new ObjectArrayList<>(512);
	//	public static void main(String...argv) throws Exception {
//
//		int spins = 99;
//		int sleepMillis = 12;
//
//		for (int i=0; i<spins; i++) {
//			long timeNanos = System.nanoTime();
//
//			long timeMillis = System.currentTimeMillis();
//			Thread.sleep(sleepMillis);
//			timeMillis = System.currentTimeMillis() - timeMillis;
//			System.out.println("millis: " + timeMillis);
//			timeNanos = System.nanoTime() - timeNanos;
//			long timeMillis2 = timeNanos / (1000 * 1000);
//			System.out.println("millis from nanos: " + timeMillis2 + ", nanos: " + timeNanos+" "+(1000 * 1000));
//		}
//
//		//		  for (int i=0; i<spins; i++) {
//		//		   long timeNanos = System.nanoTime();
//		//		   Thread.sleep(sleepMillis);
//		//		   timeNanos = System.nanoTime() - timeNanos;
//		//		   long timeMillis = timeNanos / (1000 * 1000);
//		//		   System.out.println("millis from nanos: " + timeMillis + ", nanos: " + timeNanos);
//		//		  }
//	}
	private final ObjectOpenHashSet<DrawableRemoteSegment> updateLocks = new ObjectOpenHashSet<DrawableRemoteSegment>();
	private final ObjectArrayList<SegmentController> segmentControllers;
	private final GameClientState state;
	private final SegmentLightingUpdateThreadManager segmentLightingUpdate;
	private final SegmentSorterThread segmentSorter;
	private final List<DrawableRemoteSegment> removedSegments = new ObjectArrayList<DrawableRemoteSegment>();
	private final List<DrawableRemoteSegment> disposedSegs = new ObjectArrayList<DrawableRemoteSegment>();
	private final Vector3i start = new Vector3i();
	private final Vector3i end = new Vector3i();
	private final ObjectOpenHashSet<DrawableRemoteSegment> afterGenerated = new ObjectOpenHashSet<DrawableRemoteSegment>();
	private final ObjectArrayList<SegmentData> lodShapes = new ObjectArrayList<SegmentData>();
	private final ElementCollectionDrawer elementCollectionDrawer;
	public int sortingSerial = 1;
	public int inDrawBufferCount;
	//	private Vector3f minAABB = new Vector3f();
	//	private Vector3f maxAABB = new Vector3f();
	//	private Vector3f minAABBWorld = new Vector3f();
	//	private Vector3f maxAABBWorld = new Vector3f();
	//	public static float VISISBLE = 100000;
	//	private BoundingBox worldBoundingBox = new BoundingBox(new Vector3f(-VISISBLE,-VISISBLE,-VISISBLE), new Vector3f(VISISBLE,VISISBLE,VISISBLE));
	//	private Vector3f maxAABBinv = new Vector3f();
	public SegDrawStats stats = new SegDrawStats();
	public TextBoxSeg textBoxes = new TextBoxSeg();
	public int drawnLastFrame;
	protected DrawableRemoteSegment[] drawnSegments;
	protected DrawableRemoteSegment[] drawnSegmentsBySegmentController;
	//	Vector3f camPosHelper = new Vector3f();
	protected DrawableRemoteSegment[] drawnSegmentsBySegmentControllerDouble;
	protected DrawableRemoteSegment[] drawnBlendedSegments;
	protected DrawableRemoteSegment[] drawnOpaqueSegments;
	protected DrawableRemoteSegment[] drawnBlendedSegmentsBySegment;
	protected DrawableRemoteSegment[] drawnSegmentsDouble;
	protected DrawableRemoteSegment[] deactivatedSegments;
	protected DrawableRemoteSegment[] deactivatedSegmentsDouble;
	Vector3f segPosTmp = new Vector3f();
	boolean ff;
	AbstractTextBox textBox;
	int blendedPointer;
	int blendedPointerBySeg;
	int opaquePointer;
	Vector3f beforeShift = new Vector3f(-1000000, -1000000, 1000000);
	Vector3f afterShift = new Vector3f();
	int bb;
	short updateNum;
	private final SegmentLodDrawer lod = new SegmentLodDrawer(this);
	private boolean requireFullDrawFromSort;
	private final Int2ObjectOpenHashMap<ObjectArrayList<SAABB>> saabbMapLive = new Int2ObjectOpenHashMap<ObjectArrayList<SAABB>>();
	private SegmentRenderPass segmentRenderPass;
	private int drawnSegmentsPointer;
	private boolean firstDraw = true;
	private boolean recreate = true;
	private final HashSet<DrawableRemoteSegment> disposable = new HashSet<DrawableRemoteSegment>();
	private SegmentController currentSegmentController;
	private long timeDrawn;
	private boolean resorted;
	private final Vector3f minBBOut = new Vector3f();
	private final Vector3f maxBBOut = new Vector3f();
	private final Vector3f minSBBBOut = new Vector3f();
	private final Vector3f maxSBBBOut = new Vector3f();
	private final Vector3f minSBBBOutC = new Vector3f();
	private final Vector3f maxSBBBOutC = new Vector3f();
	private final Vector3f posOut = new Vector3f();
	private boolean sorterUpdate;
	private final Matrix4f modelview = new Matrix4f();
	private final FloatBuffer modelviewBuffer = MemoryUtil.memAllocFloat(16);
	private final Vector3i lastTransform = new Vector3i(-1, 0, 0);
	private boolean frustumCulling = EngineSettings.G_FRUSTUM_CULLING.isOn();
	private final Matrix4f outMatrix = new Matrix4f();
	private final Matrix4f mMatrix = new Matrix4f();
	private boolean culling = true;
	private boolean cullFace = true;
	private boolean turnOffAllLight;
	private int max = EngineSettings.G_MAX_SEGMENTSDRAWN.getInt();
	private boolean flagMaxChanged;
	private final SegAABBDrawer d = new SegAABBDrawer();
	private final ObjectArrayList<SegmentOcclusion> occlusionsUsed = new ObjectArrayList<SegmentOcclusion>();
	private RunningEffect effect;
	private SegmentController currentDrawing;
	private long blinkingTime;
	private LodDraw[] lodDraws = new LodDraw[4096];
	private int lodPointer;
	private final Transform tmpTrns = new Transform();
	private final Quat4f quatTmp = new Quat4f();
	private final Vector3f vecTmp = new Vector3f();
	private final Matrix3f matTmp = new Matrix3f();
	private SegmentController filter;
	private int filterDrawn;
	private FontLibrary.FontInterface textBoxFont;
	private final Matrix3f rotTmp = new Matrix3f();
	//	private void assignCubeMesh(DrawableRemoteSegment e){
	//		if(e.getMeshIndex() < 0){
	//			int i = 0;
	//			while(cubeMeshes[meshCounter] != null && cubeMeshes[meshCounter].currentSegmentContext != null
	//					&& cubeMeshes[meshCounter].currentSegmentContext != e
	//					&& cubeMeshes[meshCounter].currentSegmentContext.getMeshIndex() >= 0){
	//
	//				meshCounter = ( meshCounter + 1 ) % cubeMeshes.length;
	//
	//				if(i > cubeMeshes.length){
	//					System.err.println("TOO many meshes: "+i);
	//					return;
	//				}
	//				i++;
	//			}
	//
	//			e.setMeshIndex(meshCounter);
	//			meshCounter = ( meshCounter + 1 ) % cubeMeshes.length;
	//
	//			if(cubeMeshes[e.getMeshIndex()]  == null){
	//				cubeMeshes[e.getMeshIndex()] = new CubeOptOptMesh();
	//				cubeMeshes[e.getMeshIndex()].prepare();
	//				cubeMeshes[e.getMeshIndex()].currentSegmentContext = e;
	//				this.initializedCubeMeshes ++;
	//
	//			}else{
	//				if(cubeMeshes[e.getMeshIndex()].currentSegmentContext != null && cubeMeshes[e.getMeshIndex()].currentSegmentContext != e){
	//					cubeMeshes[e.getMeshIndex()].currentSegmentContext.setMeshIndex(-1);
	//					cubeMeshes[e.getMeshIndex()].currentSegmentContext.setNeedsMeshUpdate(true);
	//				}
	//				cubeMeshes[e.getMeshIndex()].currentSegmentContext = e;
	//			}
	//			e.setNeedsMeshUpdate(true);
	//		}
	//	}
	private final List<PlanetIcoCore> currentCores = new ObjectArrayList<>();
	private boolean finished;

	{
		for(int h = 0; h < lodDraws.length; h++) {
			lodDraws[h] = new LodDraw();
		}
	}
	public SegmentDrawer(GameClientState state) {
		this.state = state;
		shader = new CubeMeshQuadsShader13();
		dataPool = new CubeDataPool();
		drawnSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnBlendedSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnOpaqueSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnBlendedSegmentsBySegment = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnSegmentsDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		deactivatedSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		deactivatedSegmentsDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnSegmentsBySegmentController = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		drawnSegmentsBySegmentControllerDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
		elementCollectionDrawer = new ElementCollectionDrawer(state);
		segmentLightingUpdate = new SegmentLightingUpdateThreadManager(LIGHTING_THREAD_COUNT);
		segmentSorter = new SegmentSorterThread();

		segmentControllers = new ObjectArrayList<SegmentController>(128);

	}

	private static Shader getCubeLodShader(int lodRes) {
		if(lodRes == 0) {
			return ShaderLibrary.lodCubeShaderD4;
		} else {
			return ShaderLibrary.lodCubeShaderD8;
		}
	}

	private void afterResort() {

		//		System.err.println("AFTER RESORT");
		synchronized(generatedSegments) {
			synchronized(disposable) {
				for(int i = 0; i < drawnSegmentsPointer; i++) {
					if(deactivatedSegments[i] != null && deactivatedSegments[i].getSortingSerial() < sortingSerial) {
						deactivatedSegments[i].setActive(false);
						disposable.add(deactivatedSegments[i]);
					}
					afterGenerated.remove(drawnSegments[i]);
				}

				//add all finished segments that are not in raster to the disposable segments
				disposable.addAll(afterGenerated);

				afterGenerated.clear();

				synchronized(removedSegments) {
					disposable.addAll(removedSegments);

					removedSegments.clear();
				}
				for(DrawableRemoteSegment e : disposable) {
					if(!e.isInUpdate()) {
						state.getWorldDrawer().getFlareDrawerManager().clearSegment(e);
						e.releaseContainerFromPool();
						e.disposeAll();
						e.setInUpdate(false);
						e.setActive(false);
						disposedSegs.add(e);
					}
				}
				if(!disposedSegs.isEmpty()) {
					//								System.err.println("Disposed: "+disposedSegs.size()+"/"+disposable.size());
				}
				if(disposedSegs.size() != disposable.size()) {
					System.err.println("[SEGDRAWER] not Disposed LEFT: " + disposedSegs.size() + "/" + disposable.size());
				}
				disposable.removeAll(disposedSegs);
				disposedSegs.clear();

			}
		}
		synchronized(segmentSorter.waitForApply) {
			segmentSorter.applied = true;
			segmentSorter.waitForApply.notify();
		}

//		if(USE_OCCLUSION_PICK){
//			d.update(saabbMapLive, saabSize);
//		}
		sorterUpdate = true;

		dataPool.cleanUp(sortingSerial);
	}

	private boolean checkNeedsMeshUpdate(DrawableRemoteSegment e, long timeMilli) {

		if(e.occlusionFailed && !e.needsMeshUpdate() && !e.isInUpdate()) {

			SegmentBufferManager man = (SegmentBufferManager) e.getSegmentController().getSegmentBuffer();
			start.set(e.pos);
			end.set(e.pos);
			start.sub(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG);
			end.add(2 * SegmentData.SEG, 2 * SegmentData.SEG, 2 * SegmentData.SEG);

			long dif = timeMilli - e.occlusionFailTime;

			if(dif > 10000 || (dif > 100 && man.getLatestChangedInArea(start, end, false) > e.lastLightingUpdateTime)) {
				//occlusion failed some time ago. reschedule update
				e.occlusionFailed = false;
				e.setNeedsMeshUpdate(true);
			}

		}


		if((e.needsMeshUpdate() || e.getCurrentCubeMesh() == null) && !e.isInUpdate()) {
			//			System.err.println("NEEDS UPDATE "+e.needsMeshUpdate()+" li "+lightingUpdates.contains(e)+" SIZE "+lightingUpdates.size());
			segmentLightingUpdate.addToUpdateQueue(e);
			//			System.err.println("NEEDS UPDATE "+e.needsMeshUpdate()+" li "+lightingUpdates.contains(e)+" SIZE "+lightingUpdates.size());
			//				System.err.println("CURRENT CM IS NULL (not in update) "+e.pos);
			//				e.debugDraw(0.1f, 1, 0, 1, 1);
			// has no old context
			return e.getCurrentCubeMesh() != null;
		}
		return true;
	}

	public int getQueueSize() {
		return segmentLightingUpdate.getQueueSize();
	}

	@Override
	public void cleanUp() {
		cleanUpCubeMeshes();
		for(int i = 0; i < LODCubeManager.length; i++) {
			LODCubeManager[i].cleanUp();
		}
	}

	@Override
	public void draw() {


		if(drawNormal) {

			assert (segmentRenderPass != null);

			BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();
			boolean buildMode = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSegmentControlManager().getSegmentBuildController().isTreeActive();

			buildMode = buildMode || state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive();

			if(buildMode) {
				draw(shader, ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport() | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit), EngineSettings.G_DRAW_SHIELDS.isOn(), true, segmentOcclusion, state.getNumberOfUpdate());
			} else {
				draw(shader, ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport()), EngineSettings.G_DRAW_SHIELDS.isOn(), true, segmentOcclusion, state.getNumberOfUpdate());
			}


			drawCubeLod(false);
		}
		if(drawLOD) {
			drawLOD(1);
		}

//		lod.draw();
//		System.err.println("MDCHANGED: TOTAL: "+stats.modelViewChangedTotal+"; BLENDED: "+stats.modelViewChangedBlend+";;; OPAQUE SEGS: "+stats.opaqueSegments+"; BLENDED SEGS: "+stats.blendedSegments);
	}

	public void drawElementCollectionsFromFrameBuffer(FrameBufferObjects fbo, float meshForce) {
		elementCollectionDrawer.drawFrameBuffer(fbo, meshForce);
	}

	public void drawElementCollectionsToFrameBuffer(FrameBufferObjects fbo) {
		elementCollectionDrawer.drawToFrameBuffer(fbo);
	}

	public boolean drawCheckElementCollections() {
		return elementCollectionDrawer.checkDraw();
	}

	public void drawCubeLod(boolean shadow) {
		boolean debug = Keyboard.isKeyDown(GLFW.GLFW_KEY_F2);
		lodPointer = 0;
		if(!lodShapes.isEmpty()) {
			int size = lodShapes.size();
			for(int j = 0; j < size; j++) {
				SegmentData c = lodShapes.get(j);
				if(c.getLodTypeAndOrientcubeIndex() == null) {
					continue;
				}
				Vector3i pos = c.getSegment().pos;
				int sz = c.drawingLodShapes.size();
				for(int i = 0; i < sz; i++) {

					int lodInfoIndex = c.drawingLodShapes.get(i);

					short type = c.getLodTypeAndOrientcubeIndex()[i * 2];
					if(!ElementKeyMap.isValidType(type) || !ElementKeyMap.getInfoFast(type).hasLod()) {

						while(lodPointer >= lodDraws.length) {
							int oldLen = lodDraws.length;
							lodDraws = Arrays.copyOf(lodDraws, lodDraws.length * 2);
							for(int h = oldLen; h < lodDraws.length; h++) {
								lodDraws[h] = new LodDraw();
							}
						}

						LodDraw lodDraw = lodDraws[lodPointer];
						lodDraw.lightingAndPos = c.getLodData();
						lodDraw.pointer = i * SegmentData.lodDataSize;
						lodDraw.type = type;
						lodDraw.faulty = true;
						lodPointer++;

					} else {
						ElementInformation info = ElementKeyMap.getInfoFast(type);
						boolean active = c.isActive(lodInfoIndex);
						int modelCount = info != null ? info.getModelCount(active) : 1;
						for(int modelIndex = 0; modelIndex < modelCount; modelIndex++) {


							while(lodPointer >= lodDraws.length) {
								int oldLen = lodDraws.length;
								lodDraws = Arrays.copyOf(lodDraws, lodDraws.length * 2);
								for(int h = oldLen; h < lodDraws.length; h++) {
									lodDraws[h] = new LodDraw();
								}
							}

							LodDraw lodDraw = lodDraws[lodPointer];
							lodDraw.lightingAndPos = c.getLodData();
							lodDraw.pointer = i * SegmentData.lodDataSize;
							lodDraw.type = type;
							lodDraw.faulty = false;

							lodDraw.mesh = info.getModel(modelIndex, active);
							assert (lodDraw.mesh != null);


							short orientation = c.getLodTypeAndOrientcubeIndex()[i * 2 + 1];
							Oriencube oc = (Oriencube) BlockShapeAlgorithm.algorithms[5][info.blockStyle == BlockStyle.SPRITE ? (orientation % 6) * 4 : orientation];

							if(info.getId() == 104) {
								int o = orientation % 6;
								oc = BlockShapeAlgorithm.getOrientcube(o, o > 1 ? Element.FRONT : Element.TOP);
							}


							lodDraw.transform.set(c.getSegmentController().getWorldTransformOnClient());

							SegmentData.getPositionFromIndexWithShift(lodInfoIndex, pos, vecTmp);

							tmpTrns.set(oc.getBasicTransform());
							tmpTrns.origin.set(0, 0, 0);

							quatTmp.set(lodDraw.mesh.getInitialQuadRot());
							matTmp.set(quatTmp);

							//FIXME REMOVE WHEN BLOCKSTYLES ARE GONE!!!!!!!!!!!
							if(info.getBlockStyle() == BlockStyle.SPRITE) {
								//rotate sprite shape LOD because their inital rot is fucked up
								rotTmp.setIdentity();
								rotTmp.rotX(SingleBlockDrawer.timesR * (FastMath.PI / 2.0f));
								tmpTrns.basis.mul(rotTmp);
							}

							tmpTrns.basis.mul(matTmp);

							tmpTrns.origin.set(lodDraw.mesh.getInitionPos());
							tmpTrns.origin.add(vecTmp);

							Matrix4fTools.transformMul(lodDraw.transform, tmpTrns);


							lodPointer++;
						}
					}
				}


			}
		}
		if(lodPointer > 0) {
			if(debug) {
				GlUtil.printGlErrorCritical();
			}
			Arrays.sort(lodDraws, 0, lodPointer);
			int size = lodShapes.size();
			Shader s = null;

			if(debug) {
				GlUtil.printGlErrorCritical();
			}

			Mesh currentMesh = null;

			int lights = 4;

			FloatBuffer lightVecBuffer = GlUtil.getDynamicByteBuffer(lights * 3 * 4, 0).asFloatBuffer();
			FloatBuffer lightDiffuseBuffer = GlUtil.getDynamicByteBuffer(lights * 4 * 4, 1).asFloatBuffer();
			GlUtil.glEnable(GL11.GL_TEXTURE_2D);

			for(int i = 0; i < lodPointer; i++) {
				LodDraw c = lodDraws[i];
				if(c.faulty) {
					continue;
				}
				Mesh mesh = c.mesh;
				if(mesh != currentMesh) {


					if(debug) {
						GlUtil.printGlErrorCritical();
					}
					if(currentMesh != null) {
						currentMesh.unloadVBO(true);
					}
					mesh.loadVBO(true);
					currentMesh = mesh;

					if(shadow) {
						if(s == null) {
							s = ShaderLibrary.lodCubeShaderShadow;
							s.loadWithoutUpdate();
						}
					} else {
						if(!mesh.getMaterial().isMaterialBumpMapped()) {
							if(s != ShaderLibrary.lodCubeShaderNormalOff) {
								if(s != null) {
									s.unloadWithoutExit();
								}
								s = ShaderLibrary.lodCubeShaderNormalOff;
								s.loadWithoutUpdate();
							}
						} else {
							if(!currentMesh.hasTangents && (s == null || s == ShaderLibrary.lodCubeShaderNormalOff || s == ShaderLibrary.lodCubeShaderTangent)) {
								if(s != null) {
									s.unloadWithoutExit();
								}
								s = ShaderLibrary.lodCubeShader;
								s.loadWithoutUpdate();
							}
							if(currentMesh.hasTangents && (s == null || s == ShaderLibrary.lodCubeShaderNormalOff || s == ShaderLibrary.lodCubeShader)) {
								if(s != null) {
									s.unloadWithoutExit();
								}
								s = ShaderLibrary.lodCubeShaderTangent;
								s.loadWithoutUpdate();
							}
						}
					}
					if(shader.shadowParams != null) {
						shader.shadowParams.execute(s);
					}
					if(debug) {
						GlUtil.printGlErrorCritical();
					}
					GlUtil.glActiveTexture(GL13.GL_TEXTURE0);


					GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getTexture() != null ? mesh.getMaterial().getTexture().getTextureId() : 0);

					GlUtil.updateShaderVector3f(s, "viewPos", Controller.getCamera().getPos());
					GlUtil.updateShaderVector3f(s, "lightPos", AbstractScene.mainLight.getPos());


					GlUtil.updateShaderInt(s, "mainTex", 0);

					if(mesh.getMaterial().isMaterialBumpMapped()) {
						GlUtil.glActiveTexture(GL13.GL_TEXTURE1);

						GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getNormalMap().getTextureId());

						GlUtil.updateShaderInt(s, "normalTex", 1);
					}
					if(debug) {
						GlUtil.printGlErrorCritical();
					}
					if(mesh.getMaterial().getEmissiveTexture() != null) {

						GlUtil.glActiveTexture(GL13.GL_TEXTURE2);

						GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getEmissiveTexture().getTextureId());

						GlUtil.updateShaderInt(s, "emissiveTex", 2);
						GlUtil.updateShaderBoolean(s, "emissiveOn", true);
					} else {
						GlUtil.updateShaderBoolean(s, "emissiveOn", false);
					}
					if(debug) {
						GlUtil.printGlErrorCritical();
					}
					GlUtil.glActiveTexture(GL13.GL_TEXTURE0);

				}
				if(debug) {
					GlUtil.printGlErrorCritical();
				}
				GlUtil.glPushMatrix();

				GlUtil.glMultMatrix(c.transform);
//				System.err.println("MSH "+mesh.getName()+"; \n"+mesh.getInitialQuadRot()+"; \n"+mesh.getParent().getTransform().getMatrix(new javax.vecmath.Matrix4f()));
				GlUtil.glMultMatrix(mesh.getParent().getTransform());
				if(debug) {
					GlUtil.printGlErrorCritical();
				}

				c.fillLightBuffers(lightVecBuffer, lightDiffuseBuffer);

				if(debug) {
					GlUtil.printGlErrorCritical();
				}

				GlUtil.updateShaderFloats3(s, "lightVec", lightVecBuffer);
				if(debug) {
					GlUtil.printGlErrorCritical();
				}
				GlUtil.updateShaderFloats4(s, "lightDiffuse", lightDiffuseBuffer);

				if(debug) {
					GlUtil.printGlErrorCritical();
				}


				mesh.renderVBO();
				if(debug) {
					GlUtil.printGlErrorCritical();
				}

				GlUtil.glPopMatrix();
			}
			if(currentMesh != null) {
				currentMesh.unloadVBO(true);
			}

			if(debug) {
				GlUtil.printGlErrorCritical();
			}
			if(s != null) {
				s.unloadWithoutExit();
			}

			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE1);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE2);
			GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
			if(debug) {
				GlUtil.printGlErrorCritical();
			}
		}
	}

	//	private Set<Segment> toClear = new HashSet<Segment>();
	@Override
	public boolean isInvisible() {

		return false;
	}

	@Override
	public void onInit() {
		for(int i = 0; i < LODCubeManager.length; i++) {
			LODCubeManager[i].Initiailize();
		}

		updateSegmentControllerSet();

		textBox = new AbstractTextBox(state);
		textBox.onInit();
		segmentLightingUpdate.start();
		segmentSorter.initialize();
		segmentSorter.start();
		textBoxes.initialize();
		d.generate((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()));

		segmentOcclusion.initializeAABB();

		if(GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			//this will only be true on nividia cards
			if(!EngineSettings.USE_GL_MULTI_DRAWARRAYS_INITIAL_SET.isOn()) {
				//turn on multidraw for nvidia only
				//ATI has a chunk draw problem, and fucking intel flickers
				EngineSettings.USE_GL_MULTI_DRAWARRAYS.setOn(true);
				EngineSettings.USE_GL_MULTI_DRAWARRAYS_INITIAL_SET.setOn(true);
			}
		}
		textBoxFont = FontLibrary.FontSize.MEDIUM_18.getUnscaled();


		firstDraw = false;

	}

	public void cleanUpCubeMeshes() {
		dataPool.cleanUpGL();
	}

	public void clearSegmentControllers() {
		synchronized(segmentControllers) {
			segmentControllers.clear();
		}

	}

	public void completeVisUpdate(SegmentController controller) {

		//		System.err.println("UPDATING complete Vis of "+this);
		controller.getSegmentBuffer().iterateOverEveryElement(new SegmentBufferIteratorEmptyInterface() {

			@Override
			public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
				return true;
			}

			@Override
			public boolean handle(Segment s, long lastChanged) {
				if(s != null) {
					synchronized(disposable) {
						disposable.add(((DrawableRemoteSegment) s));
					}
					((DrawableRemoteSegment) s).setNeedsMeshUpdate(true);
					((DrawableRemoteSegment) s).lightTries = 0;
				}
				return true;
			}


		}, false);

	}

	public void contextSwitch(DrawableRemoteSegment segment, int c) {

		assert (segment.getCurrentBufferContainer() != null);
		long t = System.currentTimeMillis();

		SegmentData segmentData = segment.getSegmentData();

		if(segmentData != null) {
			segmentData.loadLodFromContainer(segment.getCurrentBufferContainer());
		}
		segment.LODMeshLock.queuedBuffer[0] = segment.getCurrentBufferContainer().getLod4Buffer();

		segment.LODMeshLock.queuedBuffer[1] = segment.getCurrentBufferContainer().getLod8Buffer();

		LODCubeManager[0].contextSwitch(segment, segment.LODMeshLock, 0);
		LODCubeManager[1].contextSwitch(segment, segment.LODMeshLock, 1);

		segment.getNextCubeMesh().contextSwitch(segment.getCurrentBufferContainer(), segment, c);


		state.getWorldDrawer().getFlareDrawerManager().updateSegment(segment);

		long updateTime = System.currentTimeMillis() - t;
		//		if(updateTime > 20){
		//			System.err.println("[DRAWER][WARNING] CONTEXT SWITCH CUBEMESH took > 20 ms! ("+updateTime+" ms)");
		//		}

	}

	public int drawSegmentController(SegmentController c, Shader cubeShader) {
		if(c == null || !c.isInClientRange() || !drawNormal) {
			return 0;
		}
		int elementCountToDraw = Math.min(max, drawnSegmentsPointer);
		drawnLastFrame = elementCountToDraw;

		beforeShift.set(-1000000, -1000000, 1000000);
		culling = true;

		long tDrawN = System.nanoTime();
		long tDraw = System.currentTimeMillis();
		lastTransform.set(-1, 0, 0);
		GL11.glClearColor(0, 0, 0, 1); //debug
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); //debug
		CubeData.resetDrawn();
		filterDrawn = 0;
		if(seperateDrawing) {
			try {
				modelview.set(Controller.modelviewMatrix);
//				cubeShader = ShaderLibrary.getCubeShader(getState().getWorldDrawer().isSpotLightSupport());
				filter = c;
				prepareDraw(cubeShader, shader);
				segmentRenderPass = SegmentRenderPass.ALL;
				//default drawing method
				drawSeperated(elementCountToDraw, System.currentTimeMillis(), cubeShader, false, false);
				endDraw(cubeShader);

				segmentRenderPass = null;
			} finally {
				filter = null;
			}
		}
		return filterDrawn;
	}

	private void drawSeperated(int elementCountToDraw, long timeMilli, Shader cubeShader, boolean shields, boolean update) {

		boolean useTextureQuality = !Keyboard.isKeyDown(GLFW.GLFW_KEY_F7);

		boolean normalShader = cubeShader == ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport());
		boolean buildModeShader = cubeShader == ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport() | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit);
		boolean shadowShader = Shadow.creatingMap;
		Shader lastLoaded = cubeShader;
		if(segmentRenderPass == SegmentRenderPass.ALL || segmentRenderPass == SegmentRenderPass.OPAQUE) {
			GlUtil.enableBlend(false);
			GlUtil.glDisable(GL11.GL_BLEND);

			/*
			 * |||||||||||| OPAQUE PASS |||||||||||| Draw opaque first close ordered, then bulk far
			 *
			 * the marked segments are far enough away that they can be drawn with a simpler light model
			 * clearing a fragment shader bottleneck of dragging normal/light/etc as varying variables
			 * to the fragment shader, while still getting full quality as well as bump mapping on
			 * close segments
			 */
			try {
				long t = System.currentTimeMillis();
				markFarAndDrawNearOpaqueSeperated(elementCountToDraw, timeMilli, buildModeShader);
				long taken = System.currentTimeMillis() - t;
				if(taken > WARNING_MARGIN) {
					System.err.println("DRAWING TIME WARNING 0: " + taken);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			long t = System.currentTimeMillis();
			lastLoaded = seperatedOpaqueStage(elementCountToDraw, useTextureQuality, normalShader, shadowShader, buildModeShader, false, timeMilli, cubeShader, shields, update, true);
			long taken = System.currentTimeMillis() - t;
			if(taken > WARNING_MARGIN) {
				System.err.println("DRAWING TIME WARNING 1: " + taken);
			}
		}

		/*
		 * |||||||||||| BLENDED PASS |||||||||||| Draw blended elements
		 * First far bulk, then close ordered
		 */
		Shader loadedShader = cubeShader;
		if(normalShader) {
			lastLoaded.unload();

			loadedShader = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.VERTEX_LIGHTING.bit | ShaderLibrary.CubeShaderType.BLENDED.bit);

			loadedShader.setShaderInterface(cubeShader.getShaderInterface());
			loadedShader.load();
		} else if(buildModeShader) {
			lastLoaded.unload();
			loadedShader = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.VERTEX_LIGHTING.bit | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit | ShaderLibrary.CubeShaderType.BLENDED.bit);
			loadedShader.setShaderInterface(cubeShader.getShaderInterface());
			loadedShader.load();
		} else if(shadowShader) {
			cubeShader.unload();
			loadedShader = Shadow.getShadowShader(true);
			loadedShader.setShaderInterface(cubeShader.getShaderInterface());
			loadedShader.load();
		}
		if(segmentRenderPass == SegmentRenderPass.ALL || segmentRenderPass == SegmentRenderPass.TRANSPARENT) {


			GlUtil.enableBlend(true);
			enableBlend();

			//FAR
			long t = System.currentTimeMillis();
			markSeperatedBySegBlend(elementCountToDraw, timeMilli, buildModeShader);
			long taken = System.currentTimeMillis() - t;
			if(taken > WARNING_MARGIN) {
				System.err.println("DRAWING TIME WARNING 2: " + taken);
			}


			t = System.currentTimeMillis();
			CubeData.manager.drawMulti(true, loadedShader);
			taken = System.currentTimeMillis() - t;
			if(taken > WARNING_MARGIN) {
				System.err.println("DRAWING TIME WARNING 3: " + taken);
			}


			shader.quality = CubeMeshQuadsShader13.CubeTexQuality.SELECTED;


			//NEAR

			if(normalShader) {
				loadedShader.unload();
				Shader cShader = ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport() | ShaderLibrary.CubeShaderType.BLENDED.bit);
				cShader.setShaderInterface(cubeShader.getShaderInterface());
				cShader.load();
				t = System.currentTimeMillis();
				drawSeperatedSortedBlend(elementCountToDraw, timeMilli, cShader, buildModeShader, shields, update);
				taken = System.currentTimeMillis() - t;
				if(taken > WARNING_MARGIN) {
					System.err.println("DRAWING TIME WARNING 4: " + taken);
				}
			} else if(buildModeShader) {
				loadedShader.unload();
				Shader cShader = ShaderLibrary.getCubeShader(state.getWorldDrawer().isSpotLightSupport() | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit | ShaderLibrary.CubeShaderType.BLENDED.bit);
				cShader.setShaderInterface(cubeShader.getShaderInterface());
				cShader.load();
				t = System.currentTimeMillis();
				drawSeperatedSortedBlend(elementCountToDraw, timeMilli, cShader, buildModeShader, shields, update);
				taken = System.currentTimeMillis() - t;
				if(taken > WARNING_MARGIN) {
					System.err.println("DRAWING TIME WARNING 5: " + taken);
				}
			} else {
				t = System.currentTimeMillis();
				drawSeperatedSortedBlend(elementCountToDraw, timeMilli, cubeShader, buildModeShader, shields, update);
				taken = System.currentTimeMillis() - t;
				if(taken > WARNING_MARGIN) {
					System.err.println("DRAWING TIME WARNING 6: " + taken);
				}
			}

			if(shadowShader || normalShader || buildModeShader) {
				cubeShader.load();
			}
		}

		segmentRenderPass = null; // Set to null to confirm it is re-set before drawing again
	}

	private Shader seperatedOpaqueStage(int elementCountToDraw, boolean useTextureQuality, boolean normalShader, boolean shadowShader, boolean buildModeShader, boolean depthPass, long timeMilli, Shader cubeShader, boolean shields, boolean update, boolean clearMaked) {
		shader.quality = CubeMeshQuadsShader13.CubeTexQuality.SELECTED;

		long t = System.currentTimeMillis();

		//draw close opaque. needs to be sorted
		GlUtil.glPushMatrix();
		drawSeperatedSortedOpaque(elementCountToDraw, timeMilli, cubeShader, buildModeShader, shields, update);
		GlUtil.glPopMatrix();

		long taken = System.currentTimeMillis() - t;
		if(taken > WARNING_MARGIN) {
			System.err.println("DRAWING TIME WARNING SOS1: " + taken);
		}

		if(useTextureQuality) {
			shader.quality = CubeMeshQuadsShader13.CubeTexQuality.LOW;
		}


		// PASS DRAW REST OF SEGMENTS ONE SWOOP. DATA IS ALIGNED AND ITS BY SEGMENT CONTROLLER
		Shader lastLoaded = null;
		if(depthPass) {
			//shader already set
		} else if(buildModeShader) {
			cubeShader.unload();
			lastLoaded = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.VERTEX_LIGHTING.bit | ShaderLibrary.CubeShaderType.LIGHT_ALL.bit);
			lastLoaded.setShaderInterface(cubeShader.getShaderInterface());
			lastLoaded.load();
		} else if(normalShader) {
			cubeShader.unload();
			lastLoaded = ShaderLibrary.getCubeShader(ShaderLibrary.CubeShaderType.VERTEX_LIGHTING.bit);
			lastLoaded.setShaderInterface(cubeShader.getShaderInterface());
			lastLoaded.load();
		} else if(shadowShader) {
		}
		t = System.currentTimeMillis();
		CubeData.manager.drawMulti(clearMaked, lastLoaded);
		taken = System.currentTimeMillis() - t;
		if(taken > WARNING_MARGIN) {
			System.err.println("DRAWING TIME WARNING SOS2: " + taken);
		}

		return lastLoaded;
	}

	private void markFarAndDrawNearOpaqueSeperated(int elementCountToDraw, long timeMilli, boolean buildMode) {

		assert (drawNormal);
		int VISMASK = (KeyboardMappings.PLAYER_LIST.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) ? Element.FLAG_FRONT : Element.VIS_ALL;

		boolean debug = KeyboardMappings.PLAYER_LIST.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN);
		blendedPointer = 0;
		blendedPointerBySeg = 0;
		opaquePointer = 0;
		for(int i = 0; i < elementCountToDraw; i++) {


			boolean takenToDrawBlended = false;
			//			for (int i = 0; i < elementCountToDraw; i++) {
			DrawableRemoteSegment segmentToDraw = drawnSegmentsBySegmentController[i];
			DrawableRemoteSegment segmentToDrawOrdered = drawnSegments[i];

			if((segmentToDraw == null || segmentToDrawOrdered == null) || (filter != null && filter != segmentToDraw.getSegmentController() && filter != segmentToDrawOrdered.getSegmentController())) {
				continue;
			}


			boolean hasBlended = false;

			boolean orderedSegOk = !segmentToDrawOrdered.getSegmentController().isInvisibleNextDraw() && !segmentToDrawOrdered.isEmpty() && inViewFrustum(segmentToDrawOrdered) && segmentToDrawOrdered.getCurrentCubeMesh() != null;


			//close opaque -> draw later ordered
			if(orderedSegOk && segmentToDrawOrdered.lastSegmentDistSquared < ORDERED_DRAW_DIST_OPAUQ_THRESHOLD_SQUARED) {
				if(debug && (segmentToDrawOrdered.getSegmentData() instanceof SegmentData4Byte)) {
					continue;
				}
				if(filter == null || filter == segmentToDrawOrdered.getSegmentController()) {
					drawnOpaqueSegments[opaquePointer] = segmentToDrawOrdered;
					opaquePointer++;
				}
			}
			boolean perSegOk = !segmentToDraw.getSegmentController().isInvisibleNextDraw() && !segmentToDraw.isEmpty() && !(!checkNeedsMeshUpdate(segmentToDraw, timeMilli) | !segmentToDraw.isActive()) && inViewFrustum(segmentToDraw);

			//far opaque. draw at once -> mark
			if(perSegOk) {
				if(debug && (segmentToDraw.getSegmentData() instanceof SegmentData4Byte)) {
					continue;
				}
				Transform t = segmentToDraw.getSegmentController().getWorldTransformOnClient();

				if(segmentToDraw.lastSegmentDistSquared >= ORDERED_DRAW_DIST_OPAUQ_THRESHOLD_SQUARED) {
					int optionBits = 0;
					if(isDrawVirtual(segmentToDraw, buildMode)) {
						optionBits |= DrawMarker.VIRTUAL;
					}
					if(filter == null || filter == segmentToDraw.getSegmentController()) {
						filterDrawn++;
						((CubeMeshDynOpt) segmentToDraw.getCurrentCubeMesh().cubeMesh).mark(t, segmentToDraw.getSegmentController().getId(), optionBits, false, VISMASK);
					}
				}

				hasBlended = segmentToDraw.getCurrentCubeMesh().getBlendedElementsCount() > 0;
			}

			//far blended. can be drawn at once
			if(perSegOk && segmentToDraw.lastSegmentDistSquared >= BLENDED_NEAR_THRESH_SQUARED) {
				if(debug && (segmentToDraw.getSegmentData() instanceof SegmentData4Byte)) {
					continue;
				}
				if(filter == null || filter == segmentToDraw.getSegmentController()) {
					filterDrawn++;
					drawnBlendedSegmentsBySegment[blendedPointerBySeg] = segmentToDraw;
					blendedPointerBySeg++;
				}
//				takenToDrawBlended = true;

			}

			//close blended -> needs ordered draw
			if(orderedSegOk && segmentToDrawOrdered.lastSegmentDistSquared < BLENDED_NEAR_THRESH_SQUARED) {
				if(filter == null || filter == segmentToDrawOrdered.getSegmentController()) {
					if(debug && (segmentToDrawOrdered.getSegmentData() instanceof SegmentData4Byte)) {
						continue;
					}
					filterDrawn++;
					drawnBlendedSegments[blendedPointer] = segmentToDrawOrdered;
					blendedPointer++;
					takenToDrawBlended = true;
				}

			}
//			if(i < 24 && !takenToDrawBlended){
//				System.err.println(i+" DIST: "+segmentToDrawOrdered.lastSegmentDist+" :: "+orderedSegOk);
//			}
		}

	}

	private boolean isDrawVirtual(DrawableRemoteSegment segmentToDraw, boolean buildMode) {
		return segmentToDraw.getSegmentController().isVirtualBlueprint() && (!buildMode || segmentToDraw.getSegmentController() != state.getShip()) && segmentToDraw.getSegmentController().percentageDrawn >= 1;
	}

	private void drawSeperatedSortedOpaque(int elementCountToDraw, long timeMilli, Shader cubeShader, boolean buildMode, boolean shields, boolean update) {
		//from back to front
		currentSegmentController = null;
		Shader currentCube = cubeShader;
		Shader original = cubeShader;
		Shader virtual = null;
		if(original.optionBits >= 0) {
			virtual = ShaderLibrary.getCubeShader(original.optionBits | ShaderLibrary.CubeShaderType.VIRTUAL.bit);
			virtual.setShaderInterface(cubeShader.getShaderInterface());
		}
		for(int i = opaquePointer - 1; i >= 0; i--) {
			DrawableRemoteSegment segmentToDraw = drawnOpaqueSegments[i];
			if(filter == null || filter == segmentToDraw.getSegmentController()) {
				filterDrawn++;
				if(virtual != null) {
					//				System.err.println("VIRTUAL: "+segmentToDraw.getSegmentController().isVirtualBlueprint()+"; virtLoaded: "+(currentCube == virtual));
					boolean drawVirtual = isDrawVirtual(segmentToDraw, buildMode);
					if(drawVirtual && currentCube != virtual) {
						original.unload();
						virtual.load();
						currentCube = virtual;
						currentSegmentController = null; //reinit needed
						beforeShift.set(-1000000, -1000000, 1000000);
					} else if(!drawVirtual && currentCube != original) {
						virtual.unload();
						original.load();
						currentCube = original;
						currentSegmentController = null; //reinit needed
						beforeShift.set(-1000000, -1000000, 1000000);
					}
				} else {

					assert (cubeShader == ShaderLibrary.depthCubeShader || EngineSettings.isShadowOn() || filter != null);
				}
				assert ((!isDrawVirtual(segmentToDraw, buildMode) || GlUtil.loadedShader.optionBits < 0) || ((GlUtil.loadedShader.optionBits & ShaderLibrary.CubeShaderType.VIRTUAL.bit) == ShaderLibrary.CubeShaderType.VIRTUAL.bit)) : ((GlUtil.loadedShader.optionBits & ShaderLibrary.CubeShaderType.VIRTUAL.bit) == ShaderLibrary.CubeShaderType.VIRTUAL.bit) + "; " + ((currentCube.optionBits & ShaderLibrary.CubeShaderType.VIRTUAL.bit) == ShaderLibrary.CubeShaderType.VIRTUAL.bit);

				int VISMASK = (KeyboardMappings.PLAYER_LIST.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) ? (Element.FLAG_FRONT | Element.FLAG_TOP | Element.FLAG_RIGHT) : Element.VIS_ALL;

				draw(segmentToDraw, CubeMeshInterface.OPAQUE, timeMilli, currentCube, shields, update, false, VISMASK);
			}
		}
		if(currentCube == virtual) {
			virtual.unload();
			original.load();
		}

	}

	private void markSeperatedBySegBlend(int elementCountToDraw, long timeMilli, boolean buildMode) {
		int VISMASK = (KeyboardMappings.PLAYER_LIST.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) ? Element.FLAG_FRONT : Element.VIS_ALL;
		for(int i = 0; i < blendedPointerBySeg; i++) {

			DrawableRemoteSegment segmentToDraw = drawnBlendedSegmentsBySegment[i];
			if(filter == null || segmentToDraw.getSegmentController() == filter) {
				filterDrawn++;
				Transform t = segmentToDraw.getSegmentController().getWorldTransformOnClient();
				int optionBits = 0;
				if(isDrawVirtual(segmentToDraw, buildMode)) {
					optionBits |= DrawMarker.VIRTUAL;
				}
				((CubeMeshDynOpt) segmentToDraw.getCurrentCubeMesh().cubeMesh).mark(t, segmentToDraw.getSegmentController().getId(), optionBits, true, VISMASK);
				//			draw(segmentToDraw, CubeMeshInterface.BLENDED, timeMilli, cubeShader, shields, update, false);
			}
		}
	}

	private void drawSeperatedSortedBlend(int elementCountToDraw, long timeMilli, Shader cubeShader, boolean buildMode, boolean shields, boolean update) {
		int VISMASK = (KeyboardMappings.PLAYER_LIST.isDown() && Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) ? Element.FLAG_FRONT : Element.VIS_ALL;
		//from back to front
		currentSegmentController = null;
		for(int i = blendedPointer - 1; i >= 0; i--) {
			DrawableRemoteSegment segmentToDraw = drawnBlendedSegments[i];
			if(filter == null || filter == segmentToDraw.getSegmentController()) {
				filterDrawn++;
				Transform t = segmentToDraw.getSegmentController().getWorldTransformOnClient();
				int optionBits = 0;
				if(isDrawVirtual(segmentToDraw, buildMode)) {
					optionBits |= DrawMarker.VIRTUAL;
				}
				((CubeMeshDynOpt) segmentToDraw.getCurrentCubeMesh().cubeMesh).mark(t, segmentToDraw.getSegmentController().getId(), optionBits, true, VISMASK);
			}
		}
		CubeData.manager.drawMulti(true, cubeShader);
	}

	public void drawLOD(int which) {
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL20.glEnableVertexAttribArray(ShaderLibrary.CUBE_SHADER_VERT_INDEX);

		LODCubeMeshManagerBulkOptimized cm = LODCubeManager[which];
		long timeMilli = System.currentTimeMillis();

		Shader shader = getCubeLodShader(which);

		synchronized(drawnSegments) {

			int elementCountToDraw = Math.min(max, drawnSegmentsPointer);

			GlUtil.glDisable(GL11.GL_BLEND);
//			GlUtil.glEnable(GL11.GL_BLEND);
//			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			shader.setShaderInterface(new Shaderable() {
				@Override
				public void updateShaderParameters(Shader shader) {
				}

				@Override
				public void updateShader(DrawableScene scene) {
				}

				@Override
				public void onExit() {
				}
			});
			shader.load();

			boolean multi = true;
			for(int i = 0; i < elementCountToDraw; i++) {
				DrawableRemoteSegment e = drawnSegmentsBySegmentController[i];

				checkNeedsMeshUpdate(e, timeMilli);

				if(e.LODMeshLock.updateFlag) {
					e.LODMeshLock.updateFlag = false;
//					LODCubeManager.addToQueue(timeMilli, e);
				}
				if(multi) {
					cm.mark(e, e.getSegmentController().getWorldTransformOnClient(), e.getSegmentController().getId(), 0, which);
				} else {
					cm.drawMeshWithTrans(e, which);
				}
			}

			if(multi) {
				cm.drawMulti(true, shader);
			}

			shader.unload();
			GL20.glDisableVertexAttribArray(ShaderLibrary.CUBE_SHADER_VERT_INDEX);


		}
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	public void draw(Shaderable cubeShaderInterface, Shader cubeShader, boolean shields, boolean update, SegmentOcclusion segmentOcclusion, short updateNum) {
		if(!drawNormal) {
			return;
		}
		beforeShift.set(-1000000, -1000000, 1000000);
		effect = null;
		currentDrawing = null;
		lodShapes.clear();
		this.updateNum = updateNum;
		BeamState currentBeam = null;
		ObjectHeapPriorityQueue<BeamState> sortedStates = state.getWorldDrawer().getBeamDrawerManager().getSortedStates();
		LOD_THRESH_SQUARED = EngineSettings.LOD_DISTANCE_IN_THRESHOLD.getFloat() + 16.0f;
		LOD_THRESH_SQUARED *= LOD_THRESH_SQUARED;
		boolean beams = EngineSettings.G_DRAW_BEAMS.isOn() && (segmentRenderPass == SegmentRenderPass.ALL || segmentRenderPass == SegmentRenderPass.TRANSPARENT);

		long time = System.currentTimeMillis();
		boolean salvageMarkers = !Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL);
		Vector3f camPos = Controller.getCamera().getPos();
		if(beams && !sortedStates.isEmpty()) {
			currentBeam = sortedStates.dequeue();
		}

		if(beams && (!sortedStates.isEmpty() || currentBeam != null)) {
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			state.getWorldDrawer().getBeamDrawerManager().prepareDraw(0.0f);
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			if(currentBeam != null) {
				BeamDrawer.drawConnection(currentBeam, beamTmp, time, camPos);
			}
			int i = 0;
			while(!sortedStates.isEmpty()) {
//				System.err.println("DRAW REST OF BEAMS "+i+": "+currentBeam);
				BeamDrawer.drawConnection(sortedStates.dequeue(), beamTmp, time, camPos);
				i++;
			}
			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			state.getWorldDrawer().getBeamDrawerManager().endDraw();
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
		}

		frustumCulling = EngineSettings.G_FRUSTUM_CULLING.isOn() && culling;
		seperateDrawing = !Keyboard.isKeyDown(GLFW.GLFW_KEY_F9);
		//		directModelview = Keyboard.isKeyDown(GLFW.GLFW_KEY_F12);

		long start = System.currentTimeMillis();
		ff = true;
		if(firstDraw) {
			onInit();
		}
		if(max != 0 && max != (EngineSettings.G_MAX_SEGMENTSDRAWN.getInt())) {
			flagMaxChanged = true;
		}
		if(flagMaxChanged && !state.getGlobalGameControlManager().getOptionsControlManager().isTreeActive()) {
			System.err.println("[SEGMENTDRAWER] Changed max drawn -> " + (EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()));
			//max segments changed
			state.getController().popupInfoTextMessage(Lng.str("#SegmentsDrawn count will update\nwhen all queued up\nupdates are done.\nPlease stand still to not queue new ones!"), 0);
			//wait for all current light to finish
			synchronized(segmentLightingUpdate.lightingUpdates) {
				if(segmentLightingUpdate.lightingUpdates.size() > 0) {
					System.err.println("LIGHT QUEUE: " + segmentLightingUpdate.lightingUpdates.size());
					segmentLightingUpdate.lightingUpdates.notify();
				} else {

					//add all active meshes to disposable and mark for update
					synchronized(segmentControllers) {
						for(SegmentController c : segmentControllers) {
							completeVisUpdate(c);
						}
					}
					d.generate((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()));

					SegmentDrawer.segmentOcclusion.setInitialized(false);
					SegmentDrawer.segmentOcclusion.reinitialize((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()) * 2);
					if(Shadow.occlusions != null) {
						for(int i = 0; i < Shadow.occlusions.length; i++) {
							Shadow.occlusions[i].setInitialized(false);
							Shadow.occlusions[i].reinitialize((EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()) * 2);
						}
					}

					//release all meshes
					afterResort();
					//clean up all meshes
					dataPool.cleanUpGL();
					//recreate pool
					dataPool = new CubeDataPool();

					synchronized(segmentSorter) {
						drawnSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnBlendedSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnOpaqueSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnBlendedSegmentsBySegment = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnSegmentsDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						deactivatedSegments = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						deactivatedSegmentsDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnSegmentsBySegmentController = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnSegmentsBySegmentControllerDouble = new DrawableRemoteSegment[dataPool.POOL_SIZE];
						drawnSegmentsPointer = 0;
					}
					max = EngineSettings.G_MAX_SEGMENTSDRAWN.getInt();
					flagMaxChanged = false;
					state.getController().popupInfoTextMessage("#SegmentDrawn count successfully\nupdated to " + max, 0);
				}
			}
		}

		if(forceFullLightingUpdate) {
			System.err.println("Executing FULL vis update");
			synchronized(segmentControllers) {
				for(SegmentController c : segmentControllers) {
					completeVisUpdate(c);
				}
			}
		}
		if(reinitializeMeshes) {
			//			System.err.println("Executing FULL vis update");
			//			try {
			//				dataPool.reinitAll();
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}
			//			reinitializeMeshes = false;
		}
		long prepareTime = System.currentTimeMillis();
		// set up fog
		GameClientState.avgBlockLightTime = segmentLightingUpdate.getAvgHandleTime();
		GameClientState.avgBlockLightLockTime = segmentLightingUpdate.getAvgSegLockTime();

		if((System.currentTimeMillis() - segmentLightingUpdate.t) > 1000) {
			segmentLightingUpdate.t = System.currentTimeMillis();
			segmentLightingUpdate.updatesPerSecond = segmentLightingUpdate.updates;
			segmentLightingUpdate.updates = 0;
		}
		AbstractScene.infoList.add("CONTEXT UPDATES: " + segmentLightingUpdate.updatesPerSecond + "; enqueued: " + segmentLightingUpdate.lightingUpdates.size());
		timeDrawn = System.currentTimeMillis();
		time = System.currentTimeMillis();
		//		TimeStatistics.reset("Shader load "+getSegmentController());
		prepareDraw(cubeShader, cubeShaderInterface);
		stats.timeForFrustum = 0;
		stats.timeForCheckUpdate = 0;
		stats.timeForDrawVisible = 0;
		stats.timeForUniforms = 0;
		stats.timeForTotalDraw = 0;
		stats.timeForActualDraw = 0;
		modelview.set(Controller.modelviewMatrix);

		GlUtil.glPushMatrix();

		int count = 0;

		//		GL11.glShadeModel(GL11.GL_SMOOTH);

		stats.drawnBoxes = 0;

		int elementCountToDraw = 0;

		sorterUpdate = false;

		long timeMilli = System.currentTimeMillis();


		/**
		 * DRAW PHASE
		 */
		synchronized(drawnSegments) {

			elementCountToDraw = Math.min(max, drawnSegmentsPointer);

			long tDrawN = System.nanoTime();
			long tDraw = System.currentTimeMillis();
			lastTransform.set(-1, 0, 0);

			CubeData.resetDrawn();


			/**
			 * #########################################################
			 * ######################### DRAW ##########################
			 * #########################################################
			 */
			//default drawing method
			drawSeperated(elementCountToDraw, timeMilli, cubeShader, shields, update);


			int timeToDraw = (int) (System.currentTimeMillis() - tDraw);
			long timeToDrawN = (System.nanoTime() - tDrawN);
			if(timeToDrawN / 1000000.0f > 30) {
				System.err.println("[SEGMENT_DRAWER] DRAWING TIME OF " + elementCountToDraw + " elements: " + timeToDraw + "(" + timeToDrawN / 1000000.0f + ")" + "; unifroms: " + stats.timeForUniforms / 1000000.0f + "; pointer " + stats.timeForDrawVisible / 1000000.0f + "; upChk " + stats.timeForCheckUpdate / 1000000.0f + "; frust " + stats.timeForFrustum / 1000000.0f + "; update " + stats.meshUpdateTime / 1000000.0f + "; draw: " + stats.timeForActualDraw / 1000000.0f + "; totD: " + stats.timeForTotalDraw / 1000000.0f

				);
			}
		}
		endDraw(cubeShader);
		long s = (System.currentTimeMillis() - time);

		GlUtil.glPopMatrix();

		long totaDrawTime = System.currentTimeMillis() - start;
		if(System.currentTimeMillis() - stats.lastLightQueueInfoUpdate > 1000) {
			stats.lastLightQueueInfoUpdate = System.currentTimeMillis();
		}
		//		AbstractScene.infoList.add("LUR: "+stats.failedLightingsRequeuedPerSecond);
		//		AbstractScene.infoList.add("RQU/RSEG/RR: "+GameClientState.requestQueue+" / "+GameClientState.requestedSegments+" / "+GameClientState.returnedRequests);

		if(Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
	}

	public boolean draw(DrawableRemoteSegment e, int blended, long timeMilli, Shader cubeShader, boolean shields, boolean update, boolean aabbOnly, int vismask) {

		if(e.getSegmentController().isInvisibleNextDraw()) {
			return false;
		}

		if(bb % 10 == 0 && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {
			assert (false);
			if(e.isEmpty()) {
				DebugBox b = new DebugBox(new Vector3f(e.pos.x - SegmentData.SEG_HALF, e.pos.y - SegmentData.SEG_HALF, e.pos.z - SegmentData.SEG_HALF), new Vector3f(e.pos.x + SegmentData.SEG_HALF, e.pos.y + SegmentData.SEG_HALF, e.pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 0, 1, 0, 1);
				DebugDrawer.boxes.add(b);
			} else {

				SegmentData data = e.getSegmentData();
				if(!e.isActive()) {
					DebugBox b = new DebugBox(new Vector3f(data.getSegment().pos.x - SegmentData.SEG_HALF, data.getSegment().pos.y - SegmentData.SEG_HALF, data.getSegment().pos.z - SegmentData.SEG_HALF), new Vector3f(data.getSegment().pos.x + SegmentData.SEG_HALF, data.getSegment().pos.y + SegmentData.SEG_HALF, data.getSegment().pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 0, 0, 1, 1);
					DebugDrawer.boxes.add(b);
				} else {
					if(e.occlusionFailed) {
						DebugBox b = new DebugBox(new Vector3f(data.getSegment().pos.x - SegmentData.SEG_HALF, data.getSegment().pos.y - SegmentData.SEG_HALF, data.getSegment().pos.z - SegmentData.SEG_HALF), new Vector3f(data.getSegment().pos.x + SegmentData.SEG_HALF, data.getSegment().pos.y + SegmentData.SEG_HALF, data.getSegment().pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 1, 0, 0, 1);
						DebugDrawer.boxes.add(b);
					} else {
						DebugBox b = new DebugBox(new Vector3f(data.getSegment().pos.x - SegmentData.SEG_HALF, data.getSegment().pos.y - SegmentData.SEG_HALF, data.getSegment().pos.z - SegmentData.SEG_HALF), new Vector3f(data.getSegment().pos.x + SegmentData.SEG_HALF, data.getSegment().pos.y + SegmentData.SEG_HALF, data.getSegment().pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 1, 1, 1, 1);
						DebugDrawer.boxes.add(b);
					}
				}
			}

		}
		bb++;
		if(e.isEmpty()) {
			return false;
		}

		if((!checkNeedsMeshUpdate(e, timeMilli) | !e.isActive())) {
			//cube mesh is null (not initialized but send to update)
			return false;
		}

		e.lastDrawn = timeDrawn;

		if(currentDrawing != e.getSegmentController()) {
			effect = state.getWorldDrawer().getSegmentControllerEffectDrawer().getEffect(e.getSegmentController());
			currentDrawing = e.getSegmentController();
		}
		if(effect == null && SegmentControllerEffectDrawer.unaffectedTranslation == null && !inViewFrustum(e)) {
			return false;
		}
		//INSERTED CODE
		for(SegmentDrawListener listener : FastListenerCommon.segmentDrawListeners) {
			listener.preDrawSegment(e);
		}
		///
		if(currentSegmentController != e.getSegmentController()) {
			if(!directModelview) {
				GlUtil.glPopMatrix();
				GlUtil.glPushMatrix();
				loadTransform(e.getSegmentController());
				if(blended == CubeMeshInterface.BLENDED) {
				}
			}

			lastTransform.set(-1, 0, 0);
			currentSegmentController = e.getSegmentController();

			if(!aabbOnly && (cubeShader.optionBits >= 0 && ((cubeShader.optionBits & ShaderLibrary.CubeShaderType.LIGHT_ALL.bit) == ShaderLibrary.CubeShaderType.LIGHT_ALL.bit))) {

				BuildToolsManager buildToolsManager = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getBuildToolsManager();

				if(turnOffAllLight) {
					GlUtil.updateShaderInt(cubeShader, "allLight", 0);
					turnOffAllLight = false;
				}
				if(cubeShader.optionBits >= 0 && (cubeShader.optionBits & ShaderLibrary.CubeShaderType.LIGHT_ALL.bit) == ShaderLibrary.CubeShaderType.LIGHT_ALL.bit) {
					if(state.getCurrentPlayerObject() == currentSegmentController) {
						PlayerGameControlManager gc = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager();
						boolean buildMode = gc.getPlayerIntercationManager().getSegmentControlManager().getSegmentBuildController().isTreeActive();
//						System.err.println("BBEF:: "+buildMode);
						buildMode = buildMode || gc.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().isTreeActive();
//						System.err.println("BBEFAFF:: "+buildMode);
						// > 1 means light up
						int light = (buildToolsManager.lighten && !gc.getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().isTreeActive()) ? 2 : 0;
						if(buildMode && !EngineSettings.G_DRAW_NO_OVERLAYS.isOn()) {
							//used to show triggers
							light++;
						}
						GlUtil.updateShaderInt(cubeShader, "allLight", light);
						turnOffAllLight = true;
					}
				}

			}

		}

		if(currentSegmentController.blinkTime > 0) {
			long t = timeMilli - currentSegmentController.blinkTime;
			if(t < WorldDrawer.STRUCTURE_BLINKING_MS) {
				blinkingTime = t;
				GlUtil.updateShaderFloat(cubeShader, "selectTime", t);
				if(currentSegmentController.blinkShader == null) {
					currentSegmentController.blinkShader = cubeShader;
				}
			} else {

				if(currentSegmentController.blinkShader != null) {
					cubeShader.unload();

					for(Shader s : ShaderLibrary.getCubeShaders()) {
						s.loadWithoutUpdate();
						GlUtil.updateShaderFloat(s, "selectTime", 0);
						s.unloadWithoutExit();
					}
					currentSegmentController.blinkTime = 0;
					currentSegmentController.blinkShader = null;
					cubeShader.load();


				}
			}
		} else if(blinkingTime > 0) {
			//switch off blining for other structures
			cubeShader.unload();
			for(Shader s : ShaderLibrary.getCubeShaders()) {
				s.loadWithoutUpdate();
				GlUtil.updateShaderFloat(s, "selectTime", 0);
				s.unloadWithoutExit();
			}
			cubeShader.load();
			blinkingTime = 0;
		}

		int x = ByteUtil.divU256(ByteUtil.divUSeg(e.pos.x) + 128);
		int y = ByteUtil.divU256(ByteUtil.divUSeg(e.pos.y) + 128);
		int z = ByteUtil.divU256(ByteUtil.divUSeg(e.pos.z) + 128);
		afterShift.set(x, y, z);
		afterShift.scale(256);
		if(!beforeShift.equals(afterShift)) {
			GlUtil.updateShaderVector3f(cubeShader, "shift", afterShift);
			beforeShift.set(afterShift);
		}

		//		stats.meshUpdateTime += (System.nanoTime()-time);

		SegmentData segmentData = e.getSegmentData();
		if(segmentData != null && segmentData.drawingLodShapes != null) {
			if(segmentData.drawingLodShapes.size() > 0 && e.lastSegmentDistSquared < LOD_THRESH_SQUARED) {
				lodShapes.add(segmentData);
			}
		}

		//		stats.meshPrepareTime += (System.nanoTime()-time);

		if(e.getCurrentCubeMesh() != null) {

//			if(!directModelview){
			if(aabbOnly || CubeMeshBufferContainer.vertexComponents < 3) {
				if(lastTransform.x == -1) {
					//					GlUtil.updateShaderVector3f(ShaderLibrary.cubeShader13, "position", (float)e.pos.x, (float)e.pos.y, (float)e.pos.z);
					GL11.glTranslatef(e.pos.x, e.pos.y, e.pos.z);
				} else {
					/*
					 * we are still in the same segmentController. use lastTransform
					 * to set the position relatively this saves pushing/popping
					 * matrix or another translate call a last transform.x == -1
					 * indicated we are in another segmentController
					 *
					 * since segment positions are dividable by SegmentData.SEG, -1 will never
					 * occur naturally
					 */
					//					GlUtil.updateShaderVector3f(ShaderLibrary.cubeShader13, "position", (float)e.pos.x, (float)e.pos.y, (float)e.pos.z);
					GL11.glTranslatef(-lastTransform.x + e.pos.x, -lastTransform.y + e.pos.y, -lastTransform.z + e.pos.z);
				}
			}
//			}else{
//				loadSegmentTransform(currentSegmentController, e.pos);
//			}

			if(effect != null) {
				GlUtil.glPushMatrix();
				effect.modifyModelview(state);
			}

			if(effect == null || effect.isDrawOriginal()) {
				//don't draw original if there is a running effect

				if(aabbOnly) {
					GL11.glDrawArrays(GL11.GL_QUADS, 0, 24);
				} else {
					e.getCurrentCubeMesh().draw(blended, vismask);
				}
			}

			if(effect != null) {

				GlUtil.glPopMatrix();
			}

			GameClientState.drawnSegements++;

			if(!aabbOnly && shields) {
				ShieldDrawer shieldDrawer = state.getWorldDrawer().getShieldDrawerManager().get(e.getSegmentController());

				if(shieldDrawer != null && shieldDrawer.hasHit(e) && ShaderLibrary.cubeShieldShader != null && shieldDrawer.getShieldShader() != null) {
					cubeShader.unload();

					ShaderLibrary.cubeShieldShader.setShaderInterface(shieldDrawer.getShieldShader());

					ShaderLibrary.cubeShieldShader.load();

//					if (CubeMeshBufferContainer.vertexComponents < 3) {
//						GlUtil.updateShaderVector3f(ShaderLibrary.cubeShieldShader, "segPos", e.pos.x, e.pos.y, e.pos.z);
//					}


					e.getCurrentCubeMesh().draw(blended, vismask);

					ShaderLibrary.cubeShieldShader.unload();

					cubeShader.load();
				}

				if(effect != null) {
					GlUtil.glPushMatrix();
					effect.modifyModelview(state);

					effect.loadShader();

					e.getCurrentCubeMesh().draw(effect.overlayBlendMode(), vismask);

					effect.unloadShader();

					GlUtil.glPopMatrix();

					cubeShader.load();
				}

			}

			lastTransform.set(e.pos);

			//			stats.timeForQuery += e.getCurrentCubeMesh().cubeMesh.timeForQuery;
			//			stats.timeForDrawVisible += e.getCurrentCubeMesh().cubeMesh.timeForDrawVisible;
			//			stats.timeForDrawOccluded += e.getCurrentCubeMesh().cubeMesh.timeForDrawOccluded;
			//
			//			stats.timeForContextSwitches += e.getCurrentCubeMesh().cubeMesh.timeForContextSwitch;
			//			stats.timeForUniforms += e.getCurrentCubeMesh().cubeMesh.timeForContextUniforms;
			//			stats.timeForActualDraw += e.getCurrentCubeMesh().cubeMesh.timeForActualDraw;
			//			stats.timeStatePrepare += e.getCurrentCubeMesh().cubeMesh.statePrepareTime;

		} else {
			//			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
			//				e.debugDraw(0.3f, 1, 0, 1, 1);
			//			}
		}

		//INSERTED CODE
		for(SegmentDrawListener listener : FastListenerCommon.segmentDrawListeners) {
			listener.postDrawSegment(e);
		}
		///
		stats.drawnBoxes += e.isEmpty() ? 0 : e.getSize();

		//			if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
		//				if(e.getSegmentData() != null){
		//					ShaderLibrary.cubeShader13.unloadWithoutExit();
		//					Vector3f offset = new Vector3f();//e.getDim().x*(float)Element.HALF_SIZE/2f,e.getDim().y*(float)Element.HALF_SIZE/2f,e.getDim().z*(float)Element.HALF_SIZE/2f);
		////					e.getSegmentData().getOctree().drawOctree(offset);
		//					if(System.currentTimeMillis()-100 > lastOctreeDrawClear){
		//						e.getSegmentData().getOctree().resetHits();
		//						lastOctreeDrawClear = System.currentTimeMillis();
		//					}
		//					ShaderLibrary.cubeShader13.loadWithoutUpdate();
		//				}
		//			}

		return e.getCurrentCubeMesh().getBlendedElementsCount() > 0;
	}

	public void drawTextBoxes() {

//		ShaderLibrary.scanlineShader.setShaderInterface(textBox);
//		ShaderLibrary.scanlineShader.load();
		int drawn = 0;
		synchronized(drawnSegments) {
			textBox.draw(textBoxes);
		}
		//			System.err.println("TEXT BOXES: "+textBoxesPointer+"; drawn "+drawn);
//		ShaderLibrary.scanlineShader.unload();
	}

	private void endDraw(Shader cubeShader) {
		GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		currentSegmentController = null;

		GlUtil.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		cubeShader.unload();

		if(GraphicsContext.INTEGER_VERTICES) {
			GL20.glDisableVertexAttribArray(ShaderLibrary.CUBE_SHADER_VERT_INDEX);
		}
	}

	public void forceAdd(DrawableRemoteSegment seg) {
		synchronized(drawnSegments) {
			if(calcInViewFrustum(seg) && drawnSegmentsPointer < drawnSegments.length) {

				seg.segmentBufferAABBHelper = seg.getSegmentBufferRegion();

				drawnSegments[drawnSegmentsPointer] = seg;
				drawnSegmentsBySegmentController[drawnSegmentsPointer] = seg;
				drawnSegmentsPointer++;
			}
		}
	}

	public List<DrawableRemoteSegment> getRemovedSegments() {
		return removedSegments;
	}

	public void handleContextSwitches() {

//		LODCubeManager.doContextSwitches();

		synchronized(generatedSegments) {
			/*
			 * sync needed, because another update of the same seg could wait to be updated.
			 */

			for(int i = 0; i < generatedSegments.size(); i++) {
				DrawableRemoteSegment e = generatedSegments.get(i);
				assert (e.isInUpdate()) : e.pos;
				assert (e.getNextCubeMesh() != null) : e.pos;
				synchronized(e.cubeMeshLock) {
					//initialize if necessary
					//use context of next segment
					if(!e.occlusionFailed || e.getCurrentCubeMesh() == null) {
						contextSwitch(e, i);
					}

					e.releaseContainerFromPool();
					//release current cubemesh and replace with next
					if(!e.occlusionFailed || e.getCurrentCubeMesh() == null) {
						e.applyCurrent();
					} else {
						e.keepOld();
					}

					e.setActive(true);
					e.setInUpdate(false);

				}

				afterGenerated.add(e);

			}
			generatedSegments.clear();
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
		}
	}

	boolean inViewFrustum(DrawableRemoteSegment e) {
		if(!frustumCulling) {
			return true;
		}
		if(e.segmentBufferAABBHelper.aabbHelperUpdateNum != updateNum) {
			e.segmentBufferAABBHelper.inViewFrustum = e.segmentBufferAABBHelper.isInViewFrustum(minSBBBOut, maxSBBBOut, minSBBBOutC, maxSBBBOutC);
			e.segmentBufferAABBHelper.inViewFrustumFully = e.segmentBufferAABBHelper.isFullyInViewFrustum(minSBBBOut, maxSBBBOut, minSBBBOutC, maxSBBBOutC);
			e.segmentBufferAABBHelper.aabbHelperUpdateNum = updateNum;
		}
		return e.segmentBufferAABBHelper.inViewFrustumFully || (e.segmentBufferAABBHelper.inViewFrustum && calcInViewFrustum(e));
	}

	private boolean calcInViewFrustum(DrawableRemoteSegment e) {

		e.getAABBClient(minBBOut, maxBBOut, posOut);
		posOut.sub(Controller.getCamera().getPos());
		e.lastSegmentDistSquared = posOut.lengthSquared();
		e.cachedFrustum = e.isInViewFrustum(updateNum);
		return e.cachedFrustum;
	}

	public boolean isRecreate() {
		return recreate;
	}

	public void setRecreate(boolean recreate) {
		this.recreate = recreate;
	}

	private void loadTransform(SegmentController controller) {
		//		GL20.glEnableVertexAttribArray(0);
		//		GL20.glEnableVertexAttribArray(1);

		GlUtil.glMultMatrix(controller.getWorldTransformOnClient());
		//		GlUtil.glLoadMatrix(controller.getWorldTransformClient());
	}

	public void onRemovedSegmentController(SegmentController sendable) {
	}

	private void prepareDraw(Shader cubeShader, Shaderable shaderInterface) {
		cubeShader.setShaderInterface(shaderInterface);
		cubeShader.load();
		//		GlUtil.glDisable(GL11.GL_LIGHTING);
		//		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		if(cullFace) {
			GlUtil.glEnable(GL11.GL_CULL_FACE);
		} else {
			GlUtil.glDisable(GL11.GL_CULL_FACE);
		}
		if(!seperateDrawing) {
			enableBlend();
		} else {
			if(segmentRenderPass == SegmentRenderPass.OPAQUE) {
				GlUtil.glDisable(GL11.GL_BLEND);
			} else {
				GlUtil.glEnable(GL11.GL_BLEND);
			}
		}
		stats.meshUpdateTime = 0;
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		if(GraphicsContext.INTEGER_VERTICES) {
			GL20.glEnableVertexAttribArray(ShaderLibrary.CUBE_SHADER_VERT_INDEX);
		}
	}

	private void enableBlend() {
		GlUtil.glEnable(GL11.GL_BLEND);
		if(blendFunc == ADDITIVE) {
			GlUtil.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			GlUtil.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
		} else if(blendFunc == MULTIPLICATIVE) {
			GlUtil.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			GlUtil.glBlendFuncSeparate(GL11.GL_ZERO, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		} else {
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
	}

	public void update(Timer timer) {
		dataPool.checkPoolSize();
		shader.update(timer);
		segmentLightingUpdate.notifyUpdateQueue();
		elementCollectionDrawer.update(timer, segmentControllers);

		for(int i = 0; i < LODCubeManager.length; i++) {
			LODCubeManager[i].update(timer);
		}

	}

	public void updateSegmentControllerSet() {

		synchronized(segmentControllers) {
			for(int i = 0; i < segmentControllers.size(); i++) {
				SegmentController controller = segmentControllers.get(i);
				if(controller.getNetworkObject().markedDeleted.get() || !controller.isInClientRange()) {
					SegmentController remove = segmentControllers.remove(i);
					i--;
					//					System.err.println("SEG DRAWER REMOVING "+remove);
				}
			}
			currentCores.clear();
			for(Sendable s : state.getCurrentSectorEntities().values()) {
				if(s instanceof PlanetIcoCore) {
					currentCores.add((PlanetIcoCore) s);
				}
				if(s instanceof SegmentController segmentController) {

					boolean contains = segmentControllers.contains(segmentController);
					if(!contains && segmentController != null) {
						segmentControllers.add(segmentController);
					}
				}
			}
		}
	}

	public boolean wasSorterUpdate() {
		return sorterUpdate;
	}

	public void enableCulling(boolean b) {
		this.culling = b;
	}

	/**
	 * @return the cullFace
	 */
	public boolean isCullFace() {
		return cullFace;
	}

	/**
	 * @param cullFace the cullFace to set
	 */
	public void setCullFace(boolean cullFace) {
		this.cullFace = cullFace;
	}

	/**
	 * @return the segmentControllers
	 */
	public ObjectArrayList<SegmentController> getSegmentControllers() {
		return segmentControllers;
	}

	public void checkSamples() {

		synchronized(drawnSegments) {

			long tResort = System.currentTimeMillis();

			synchronized(segmentSorter.waitForApply) {
				if(resorted) {
					afterResort();
					resorted = false;

					long timeToResort = (System.currentTimeMillis() - tResort);
					if(timeToResort > 20) {
						System.err.println("RESORTING TIME : " + timeToResort);
					}
				}
//					}

			}

			if(requireFullDrawFromSort) {
				requireFullDrawFromSort = false;
			}
		}
	}

	/**
	 * @return the state
	 */
	public GameClientState getState() {
		return state;
	}

	public SegmentRenderPass getSegmentRenderPass() {
		return segmentRenderPass;
	}

	public void setSegmentRenderPass(SegmentRenderPass segmentRenderPass) {
		this.segmentRenderPass = segmentRenderPass;
	}

	public void onStopClient() {
		finished = true;
	}

	public ElementCollectionDrawer getElementCollectionDrawer() {
		return elementCollectionDrawer;
	}

	public enum SegmentRenderPass {
		OPAQUE,
		TRANSPARENT,
		ALL
	}

	public class SAABB {
		public int pointer;
		Vector3i position = new Vector3i();
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();

		public void reset() {
			min.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
			max.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		}
	}

	private class SegComparator implements Comparator<DrawableRemoteSegment>, Serializable {
		private final Vector3f pos;
		private final Vector3f tV = new Vector3f();

		public SegComparator(boolean dir) {
			pos = new Vector3f();
		}

		/**
		 * Parameters: f1 the first float to compare. f2 the second float to
		 * compare.
		 * <p/>
		 * Returns: the value 0 if f1 is numerically equal to f2; a
		 * value less than 0 if f1 is numerically less than f2; and a value
		 * greater than 0 if f1 is numerically greater than f2.
		 */
		@Override
		public int compare(DrawableRemoteSegment f1, DrawableRemoteSegment f2) {

			if(f1 == f2 || f1.equals(f2)) {
				return 0;
			}

			return Float.compare(f1.camDist, f2.camDist);

		}
	}

	@SuppressWarnings("unused")
	private class SegDrawStats {
		public long noVisCount;
		public long lastLightQueueInfoUpdate;
		private long timeForDrawVisible;
		private long timeForUniforms;
		private long timeForActualDraw;
		private long timeForTotalDraw;
		private long drawnBoxes;
		private long meshUpdateTime;
		private long timeForFrustum;
		private long timeForCheckUpdate;
		private long timeToSort;
		private long lastSortTime;
	}

	private class SegmentLightingUpdateThreadManager extends Thread {
		//		private static final int MAX_LIGHT_UPDATE_QUEUE = 300;
		private final ObjectArrayList<SegmentLightingUpdateThread> availableThreads;
		private final ObjectArrayList<SegmentLightingUpdateThread> allThreads;
		private final ObjectOpenHashSet<DrawableRemoteSegment> lightingUpdates = new ObjectOpenHashSet<DrawableRemoteSegment>(1024);
		private final ObjectOpenHashSet<DrawableRemoteSegment> lightingUpdatesQueue = new ObjectOpenHashSet<DrawableRemoteSegment>(1024);
		private final SegComparator segComparator = new SegComparator(false);
		public Object updatesPerSecond;
		public int updates;
		public long t;

		public SegmentLightingUpdateThreadManager(int count) {
			super("SegmentLightingUpdateThreadManager");
			setDaemon(true);
			availableThreads = new ObjectArrayList<SegmentLightingUpdateThread>(count);
			allThreads = new ObjectArrayList<SegmentLightingUpdateThread>(count);
			for(int i = 0; i < count; i++) {
				SegmentLightingUpdateThread s = new SegmentLightingUpdateThread(this);
				s.start();
				availableThreads.add(s);
				allThreads.add(s);
			}
		}

		public float getAvgHandleTime() {
			float t = 0;
			for(int i = 0; i < allThreads.size(); i++) {
				t += allThreads.get(i).getAverage();
			}

			return t / allThreads.size();
		}

		public float getAvgSegLockTime() {
			float t = 0;
			for(int i = 0; i < allThreads.size(); i++) {
				t += allThreads.get(i).getAverageLock();
			}

			return t / allThreads.size();
		}

		public void addThreadToPool(SegmentLightingUpdateThread segmentLightingUpdateThread, DrawableRemoteSegment seg, boolean removeUpdateLock) {
			float takenTotal = 0;
			float threads;
			if(removeUpdateLock) {
				synchronized(updateLocks) {
					updateLocks.remove(seg);

				}
			}
			synchronized(availableThreads) {

				availableThreads.add(segmentLightingUpdateThread);
				availableThreads.notify();
			}
		}

		public void addToUpdateQueue(DrawableRemoteSegment e) {
			if(e.isEmpty()) {
				e.setNeedsMeshUpdate(false);
			} else {
				if(!e.inLightingQueue) {
					e.inLightingQueue = true;
					//no need to synchronize: access is sequential
					lightingUpdatesQueue.add(e);
				}
			}

		}

		public void notifyUpdateQueue() {
			if(!lightingUpdatesQueue.isEmpty()) {
				synchronized(lightingUpdates) {
					lightingUpdates.addAll(lightingUpdatesQueue);
					lightingUpdates.notify();
				}
				lightingUpdatesQueue.clear();
			}
		}

		public int getQueueSize() {
			return lightingUpdatesQueue.size();
		}

		@Override
		public void run() {
			try {
				while(Controller.getCamera() == null) {
					sleep(100);
				}

				while(!GLFrame.isFinished() && !finished) {

					synchronized(this) {
						//wait here in case of max segment changing
					}

					SegmentLightingUpdateThread reservedThread = null;
					synchronized(availableThreads) {
						while(availableThreads.isEmpty()) {
							availableThreads.wait(10000);
							if(finished) {
								return;
							}
						}
						reservedThread = availableThreads.remove(0);
					}

					synchronized(lightingUpdates) {
						while(lightingUpdates.isEmpty()) {
							lightingUpdates.wait(1000);
							if(finished) {
								return;
							}
						}

						segComparator.pos.set(Controller.getCamera().getPos());

						DrawableRemoteSegment nearest = null;
						for(DrawableRemoteSegment s : lightingUpdates) {
							if(nearest == null || segComparator.compare(s, nearest) < 0) {
								nearest = s;
							}
						}

						DrawableRemoteSegment next = nearest;

						lightingUpdates.remove(next);
						next.inLightingQueue = false;
						synchronized(updateLocks) {
							synchronized(disposable) {
								if(next.isInUpdate() || updateLocks.contains(next)) {

									addThreadToPool(reservedThread, next, false);
									continue;
								}
								updateLocks.add(next);
							}
						}
						synchronized(next.cubeMeshLock) {
							next.setInUpdate(true);
							next.lastLightingUpdateTime = System.currentTimeMillis();
							next.setNeedsMeshUpdate(false);
						}
						reservedThread.addToUpdate(next);
						//						System.err.println("DELIGATED " + next.pos + " TO "
						//								+ remove.getName());
					}

				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

		private class SegmentLightingUpdateThread extends Thread {

			DrawableRemoteSegment nextSeg;
			DrawableRemoteSegment updatingSeg;
			private Occlusion occlusionData;
			private final SegmentLightingUpdateThreadManager manager;
			private double takenLock;
			private double taken;
			private int handled = 1;
			private int id;

			public SegmentLightingUpdateThread(SegmentLightingUpdateThreadManager manager) {
				super("LightUpdate" + (threadCount));
				id = threadCount;
				threadCount++;
				setPriority(4);
				initializeOcclusion();
				this.manager = manager;
				setDaemon(true);
			}

			public float getAverage() {
				return (float) (taken / handled);
			}

			public float getAverageLock() {
				return (float) (takenLock / handled);
			}

			public void addToUpdate(DrawableRemoteSegment s) {
				synchronized(this) {

					nextSeg = s;
					notify();
				}
			}

			private void resetOcclusion(SegmentData data, CubeMeshBufferContainer containerFromPool) {
				if(data.getSize() <= 0) {
					return;
				}
				occlusionData.reset(data, containerFromPool);
			}

			private void compute(DrawableRemoteSegment nextUpdatedSegment, CubeMeshBufferContainer containerFromPool, CubeData cubeOptOptMesh, int tries) {
				try {
					if(!nextUpdatedSegment.isEmpty() && nextUpdatedSegment.getSegmentData() != null) {
						SegmentData segmentData = nextUpdatedSegment.getSegmentData();
						long t = System.currentTimeMillis();
						segmentData.rwl.readLock().lock();
						takenLock += (System.currentTimeMillis() - t);
						try {
							resetOcclusion(segmentData, containerFromPool);

							occlusionData.compute(segmentData, containerFromPool);

							if(!nextUpdatedSegment.occlusionFailed || nextUpdatedSegment.getCurrentCubeMesh() == null) {
								assert (cubeOptOptMesh != null);
								cubeOptOptMesh.createIndex(segmentData, containerFromPool);
							}
						} finally {
							segmentData.rwl.readLock().unlock();
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
					System.err.println("[CLIENT] Exception: " + e.getClass().getSimpleName() + " in computing Lighting of " + nextUpdatedSegment.getSegmentController() + ". retrying");
					nextUpdatedSegment.setHasVisibleElements(true);
					nextUpdatedSegment.occlusionFailed = true;
					nextUpdatedSegment.occlusionFailTime = System.currentTimeMillis();
				}
			}

			private void handle(DrawableRemoteSegment nextUpdatedSegment) {

				//the next segment will be the nearest anyway!

				assert (nextUpdatedSegment.getNextCubeMesh() == null);

				synchronized(nextUpdatedSegment.cubeMeshLock) {
					CubeData cubeOptOptMesh = dataPool.getMesh(nextUpdatedSegment);
					nextUpdatedSegment.setNextCubeMesh(cubeOptOptMesh);
					CubeMeshBufferContainer containerFromPool = nextUpdatedSegment.getContainerFromPool();
					compute(nextUpdatedSegment, containerFromPool, cubeOptOptMesh, 0);
					synchronized(generatedSegments) {
						int in = -1;
						if((in = generatedSegments.indexOf(nextUpdatedSegment)) >= 0) {
							//doesn't happen
							DrawableRemoteSegment remove = generatedSegments.remove(in);
							assert (nextUpdatedSegment != remove);
							synchronized(disposable) {
								disposable.add(remove);
							}
							remove.setInUpdate(false);
						}
						generatedSegments.add(nextUpdatedSegment);
					}
				}

				manager.updates++;

			}

			public void initializeOcclusion() {
				if(id >= Occlusion.occluders.length) {
					id = 0;
				}
				occlusionData = Occlusion.occluders[id];
			}

			@Override
			public void run() {
				try {
					while(!GLFrame.isFinished() && !finished) {
						synchronized(this) {
							while(nextSeg == null && updatingSeg == null) {
								wait(10000);
								if(finished) {
									return;
								}
							}
							updatingSeg = nextSeg;
							nextSeg = null;
						}
						long t = System.currentTimeMillis();
						if(finished) {
							return;
						}
						handle(updatingSeg);
						taken += (System.currentTimeMillis() - t);
						handled++;

						Thread.sleep(2);

						manager.addThreadToPool(this, updatingSeg, true);
						updatingSeg = null;

					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}


	}

	private class SegmentSorterThread extends Thread {
		public final TextBoxSeg textBoxes;
		protected final ObjectArrayList<SegmentSortElement> presortedSegments;
		protected final ObjectArrayList<Region> presortedRegions;
		private final ObjectArrayList<Region> regionPool = new ObjectArrayList<Region>(1024);
		private final ObjectArrayList<SegmentSortElement> segmentRegionPool = new ObjectArrayList<SegmentSortElement>(1024);
		private final SegmentSortIterator iteratorImpl;
		private final Vector3f camPos = new Vector3f();
		private final Comparator<DrawableRemoteSegment> sortPerSegmentImpl = (o1, o2) -> o1.getSegmentController().getId() - o2.getSegmentController().getId();
		public Object waitForApply = new Object();
		public boolean applied;
		private final ObjectArrayList<SAABB> saabbPool = new ObjectArrayList<SAABB>();
		private final Int2ObjectOpenHashMap<ObjectArrayList<SAABB>> saabbMap = new Int2ObjectOpenHashMap<ObjectArrayList<SAABB>>();
		private final SegmentRegionComperator segmentRegionComperator;
		private final RegionComparator regionComparator;
		private final SegmentDisposeIterator iteratorDisposeImpl;
		private final HashSet<DrawableRemoteSegment> localDisposable = new HashSet<DrawableRemoteSegment>();

		public SegmentSorterThread() {
			super("SegentSorter");
			setDaemon(true);
			setPriority(MIN_PRIORITY);
			segmentRegionComperator = new SegmentRegionComperator();
			regionComparator = new RegionComparator(false);
			textBoxes = new TextBoxSeg();

			presortedSegments = new ObjectArrayList<SegmentSortElement>();
			presortedRegions = new ObjectArrayList<Region>();
			iteratorImpl = new SegmentSortIterator();
			iteratorDisposeImpl = new SegmentDisposeIterator();
		}

		public void initialize() {
			textBoxes.initialize();
		}

		private void releaseAllSegmentRegion(List<SegmentSortElement> r) {
			for(int i = 0; i < r.size(); i++) {
				releaseSegmentSortElement(r.get(i));
			}
		}

		private void releaseSegmentSortElement(SegmentSortElement r) {
			r.seg = null;
			segmentRegionPool.add(r);
		}

		private SegmentSortElement getSegmentRegion() {
			if(segmentRegionPool.isEmpty()) {
				return new SegmentSortElement();
			} else {
				return segmentRegionPool.remove(segmentRegionPool.size() - 1);
			}
		}

		private void releaseAllRegion(List<Region> r) {
			for(int i = 0; i < r.size(); i++) {
				releaseRegion(r.get(i));
			}
		}

		private void releaseRegion(Region r) {
			r.buffer = null;
			regionPool.add(r);
		}

		private Region getRegion() {
			if(regionPool.isEmpty()) {
				return new Region();
			} else {
				return regionPool.remove(regionPool.size() - 1);
			}
		}

		private boolean sortRegions(ObjectArrayList<Region> presortedRegions, ObjectArrayList<SegmentController> segmentControllers) {

			releaseAllRegion(presortedRegions);
			presortedRegions.clear();
			SimpleTransformableSendableObject<?> cur = state.getCurrentPlayerObject();
			synchronized(segmentControllers) {
				for(int i = 0; i < segmentControllers.size(); i++) {
					SegmentController controller = segmentControllers.get(i);

					if(controller instanceof Ship) {
						if(controller.isCloakedFor(cur) && !((Ship) controller).getAttachedPlayers().contains(state.getPlayer())) {
							continue;
						}
					}

					synchronized(((SegmentBufferManager) controller.getSegmentBuffer()).getBuffer()) {
						for(SegmentBufferInterface s : ((SegmentBufferManager) controller.getSegmentBuffer()).getBuffer().values()) {
							if(!s.isEmpty()) {
								Region region = getRegion();
								region.transform.set(controller.getWorldTransformOnClient());
								region.buffer = (SegmentBuffer) s;
								region.pos.set((region.buffer.getRegionStart().x + SegmentBufferManager.DIMENSION_HALF) * SegmentData.SEG - SegmentData.SEG_HALF, (region.buffer.getRegionStart().y + SegmentBufferManager.DIMENSION_HALF) * SegmentData.SEG - SegmentData.SEG_HALF, (region.buffer.getRegionStart().z + SegmentBufferManager.DIMENSION_HALF) * SegmentData.SEG - SegmentData.SEG_HALF);
								region.transform.transform(region.pos);
								presortedRegions.add(region);
							}
						}
					}
				}
			}
			try {
				Collections.sort(presortedRegions, regionComparator);
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		private void sortElements(ObjectArrayList<SegmentSortElement> presortedSegments, List<Region> regions) throws Exception {

			releaseAllSegmentRegion(presortedSegments);
			presortedSegments.clear();

			stats.noVisCount = 0;
			inDrawBufferCount = 0;

			int maxFill = dataPool.POOL_SIZE * 8;
			int fill = 0;

			//			iteratorImpl.camPos.set(Controller.getCamera().getPos());
			for(int i = 0; i < regions.size(); i++) {
				Region region = regions.get(i);
				SegmentBuffer buffer = region.buffer;
				int newFill = fill + buffer.getTotalNonEmptySize();
				if(newFill < maxFill) {
					fill += buffer.getTotalNonEmptySize();
					iteratorImpl.currentRegion = buffer;
					//synched
					buffer.iterateOverNonEmptyElement(iteratorImpl, true);
				} else {

					if(buffer.isActive()) {
						System.err.println("DEACTIVATING REGION: " + buffer.getRegionStart() + " of " + buffer.getSegmentController() + "; fill: " + fill);
						//synched
						buffer.iterateOverNonEmptyElement(iteratorDisposeImpl, true);
					}
				}

				int overfilled = 0;

			}

			stats.lastSortTime = System.currentTimeMillis();
		}

		@Override
		public void run() {
			while(!GLFrame.isFinished() && !finished) {
				try {
					camPos.set(Controller.getCamera().getPos());
					regionComparator.pos.set(camPos);
					iteratorImpl.camPos.set(camPos);

					localDisposable.clear();

					long time = System.currentTimeMillis();

					boolean sortingOK = sortRegions(presortedRegions, segmentControllers);
					if(!sortingOK) {
						continue;
					}

					sortElements(presortedSegments, presortedRegions);

					stats.timeToSort = System.currentTimeMillis() - time;

					try {
						Collections.sort(presortedSegments, segmentRegionComperator);
					} catch(Exception e) {
						e.printStackTrace();
						System.err.println("[Exception] Catched: Resorting triggered by exception");
						continue;
					}
					SimpleTransformableSendableObject<?> cur = state.getCurrentPlayerObject();
					synchronized(segmentControllers) {
						Vector3f posBuffer = new Vector3f();
						textBoxes.pointer = 0;
						for(SegmentController c : segmentControllers) {
							if(!(c instanceof Ship) || !c.isCloakedFor(cur)) {
								int size = c.getTextBlocks().size();

								for(int i = 0; i < size; i++) {

									long index = c.getTextBlocks().getLong(i);

									if(ElementCollection.getType(index) != ElementKeyMap.LOGIC_REMOTE_INNER) {
										ElementCollection.getPosFromIndex(index, posBuffer);
										posBuffer.x -= SegmentData.SEG_HALF;
										posBuffer.y -= SegmentData.SEG_HALF;
										posBuffer.z -= SegmentData.SEG_HALF;

										c.getWorldTransformOnClient().transform(posBuffer);
										posBuffer.sub(camPos);
										float len = posBuffer.length();
										if(len < 128) {
											textBoxes.v[textBoxes.pointer].v = index;
											textBoxes.v[textBoxes.pointer].dist = len;
											textBoxes.v[textBoxes.pointer].c = c;

											textBoxes.pointer++;
										}
										if(textBoxes.pointer >= textBoxes.v.length) {
											break;
										}
									}
								}
							}

						}
					}
					Arrays.sort(textBoxes.v, 0, textBoxes.pointer);
					//					for(Entry<DrawableRemoteSegment> sEn : distances.float2ObjectEntrySet() ){
					//						presortedSegments.get(f).getSegmentController().getAbsoluteSegmentWorldPositionClient(presortedSegments.get(f), tV);
					//						tV.sub(camPos);
					//						System.err.println(sEn.getValue()+": "+sEn.getFloatKey()+"; "+presortedSegments.get(f)+": "+tV.lengthSquared());
					//
					//						assert(sEn.getValue() == presortedSegments.get(f)):sEn.getValue()+"; "+presortedSegments.get(f);
					//						f++;
					//					}
					synchronized(this) {
						assert drawnSegmentsDouble.length == dataPool.POOL_SIZE;
						//					for(Entry<DrawableRemoteSegment> sEn : distances.float2ObjectEntrySet() ){

						int pointer = 0;
						int f = 0;
						int i = 0;
						int saaabSizeLocal = 0;
						for(SegmentSortElement presortedRegion : presortedSegments) {

							DrawableRemoteSegment s = presortedRegion.seg;

							s.segmentBufferAABBHelper = s.segmentBufferAABBHelperSorting;
							s.segmentBufferAABBHelperSorting = null;
							if(i < dataPool.POOL_SIZE - 100) {
								DrawableRemoteSegment old = drawnSegmentsDouble[pointer];
								deactivatedSegmentsDouble[pointer] = old;
								drawnSegmentsDouble[pointer] = s;
								drawnSegmentsDouble[pointer].setSortingSerial(sortingSerial);
								drawnSegmentsDouble[pointer].sortingId = pointer;

								drawnSegmentsBySegmentControllerDouble[pointer] = s;
								drawnSegmentsBySegmentControllerDouble[pointer].setSortingSerial(sortingSerial);
								drawnSegmentsBySegmentControllerDouble[pointer].sortingId = pointer;

								pointer++;
							} else {
								if(s.isActive()) {
									localDisposable.add(s);
									s.setNeedsMeshUpdate(true);
								}
							}
							i++;

						}

						if(pointer > 0) {
							Arrays.sort(drawnSegmentsBySegmentControllerDouble, 0, pointer - 1, sortPerSegmentImpl);
						}

						synchronized(drawnSegments) {

							saabbMapLive.clear();
							saabbMapLive.putAll(saabbMap);
							for(int g = 0; g < textBoxes.pointer; g++) {

								SegmentDrawer.this.textBoxes.v[g].c = textBoxes.v[g].c;
								SegmentDrawer.this.textBoxes.v[g].dist = textBoxes.v[g].dist;
								SegmentDrawer.this.textBoxes.v[g].v = textBoxes.v[g].v;

							}

							SegmentDrawer.this.textBoxes.pointer = textBoxes.pointer;

							//switch double buffer
							//double buffer used to minimize synch time
							DrawableRemoteSegment[] tmpDrawnSegments = drawnSegments;
							DrawableRemoteSegment[] tmpDrawnSegmentsBySegmentController = drawnSegmentsBySegmentController;
							DrawableRemoteSegment[] tmpDeactivatedDrawnSegments = deactivatedSegments;

							drawnSegmentsPointer = pointer;

							drawnSegments = drawnSegmentsDouble;
							deactivatedSegments = deactivatedSegmentsDouble;
							drawnSegmentsBySegmentController = drawnSegmentsBySegmentControllerDouble;

							drawnSegmentsDouble = tmpDrawnSegments;
							deactivatedSegmentsDouble = tmpDeactivatedDrawnSegments;
							drawnSegmentsBySegmentControllerDouble = tmpDrawnSegmentsBySegmentController;

							if(drawnSegmentsPointer + 1 < drawnSegments.length) {
								//clean up rest of segments not displayed (so they have no more reference and the garbage collector can pick them up)
								for(int p = drawnSegmentsPointer + 1; p < drawnSegments.length; p++) {
									drawnSegments[p] = null;
									deactivatedSegments[p] = null;
								}
							}

							assert (drawnSegmentsDouble != drawnSegments) : "Pointers equal...";
							requireFullDrawFromSort = true;
						}
					}

					synchronized(disposable) {
						disposable.addAll(localDisposable);
					}

					synchronized(waitForApply) {
						resorted = true;
						applied = false;
						while(!applied) {
							waitForApply.wait();
						}
					}
					sleep(500);
					sortingSerial++;
				} catch(InterruptedException e) {
					e.printStackTrace();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		private class SegmentRegionComperator implements Comparator<SegmentSortElement>, Serializable {
			public SegmentRegionComperator() {
			}

			@Override
			public synchronized int compare(SegmentSortElement a, SegmentSortElement b) {

				if(a.seg == b.seg) {
					return 0;
				}

				return Float.compare(a.camDist, b.camDist);

			}

		}

		private class RegionComparator implements Comparator<Region>, Serializable {
			private final Vector3f pos;
			private final Vector3f tV = new Vector3f();

			public RegionComparator(boolean dir) {
				pos = new Vector3f();
			}

			private float getCameraDistanceS(Region v) {
				tV.sub(v.pos, pos);
				return tV.lengthSquared();
			}

			@Override
			public synchronized int compare(Region a, Region b) {

				if(a.buffer == b.buffer) {
					return 0;
				}
				return Float.compare(getCameraDistanceS(a), getCameraDistanceS(b));

			}


		}

		private class SegmentDisposeIterator implements SegmentBufferIteratorInterface {

			@Override
			public boolean handle(Segment s, long lastChanged) {
				DrawableRemoteSegment e = (DrawableRemoteSegment) s;
				e.segmentBufferAABBHelperSorting = null;
				if(e.isActive()) {
					//					if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
					//						e.debugDraw(2.5f, 1, 0.5f, 0, 1);
					//					}
					localDisposable.add(e);
				}
				return !GLFrame.isFinished();
			}

		}

		private class SegmentSortIterator implements SegmentBufferIteratorInterface {
			public SegmentBuffer currentRegion;
			Vector3f camPos = new Vector3f();
			Vector3f tmp = new Vector3f();


			@Override
			public boolean handle(Segment s, long lastChanged) {

				DrawableRemoteSegment e = (DrawableRemoteSegment) s;
				e.segmentBufferAABBHelperSorting = null;
				if((!e.isEmpty()) && (e.hasVisibleElements() || e.occlusionFailed)) {

					e.segmentBufferAABBHelperSorting = currentRegion;

					assert (e.getSegmentData().getSegment() == e);

					//						if(!culling || isInViewingDistance(s.getSegmentController(), e, camPos, segPosTmp)){
					SegmentSortElement segmentRegion = getSegmentRegion();
					segmentRegion.seg = e;

					s.getSegmentController().getAbsoluteSegmentWorldPositionClient(e, segmentRegion.pos);
					tmp.sub(segmentRegion.pos, camPos);
					segmentRegion.camDist = tmp.length();
					e.camDist = segmentRegion.camDist;
					presortedSegments.add(segmentRegion);
					inDrawBufferCount++;
					//						}else{
					//							if(e.isActive()){
					//								localDisposable.add(e);
					//							}
					//
					//
					//						}
				} else {

					if(e.isEmpty()) {
						if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {
							DebugBox b = new DebugBox(new Vector3f(e.pos.x - SegmentData.SEG_HALF, e.pos.y - SegmentData.SEG_HALF, e.pos.z - SegmentData.SEG_HALF), new Vector3f(e.pos.x + SegmentData.SEG_HALF, e.pos.y + SegmentData.SEG_HALF, e.pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 0, 0.5f, 1, 1);
							DebugDrawer.boxes.add(b);
						}
					} else {
						if(!e.hasVisibleElements()) {
							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {
								DebugBox b = new DebugBox(new Vector3f(e.pos.x - SegmentData.SEG_HALF, e.pos.y - SegmentData.SEG_HALF, e.pos.z - SegmentData.SEG_HALF), new Vector3f(e.pos.x + SegmentData.SEG_HALF, e.pos.y + SegmentData.SEG_HALF, e.pos.z + SegmentData.SEG_HALF), e.getSegmentController().getWorldTransformOnClient(), 1, 0.5f, 0, 1);
								DebugDrawer.boxes.add(b);
							}
						}

						if(e.isActive()) {
							localDisposable.add(e);
						}
					}
					if(stats.noVisCount > 0) {
					}
				}
				return !GLFrame.isFinished();
			}

		}

		private class Region {
			private final Vector3f pos = new Vector3f();
			private final Transform transform = new Transform();
			private SegmentBuffer buffer;
		}

		private class SegmentSortElement {
			float camDist;
			Vector3f pos = new Vector3f();
			DrawableRemoteSegment seg;
		}


	}

	public class TextBoxSeg {

		public final TextBoxElement[] v = new TextBoxElement[1200];
		public int pointer;

		public TextBoxSeg() {

		}

		public void initialize() {
			for(int i = 0; i < v.length; i++) {
				v[i] = new TextBoxElement();
				v[i].text = new GUITextOverlay(textBoxFont, getState());
				v[i].text.debug = true;
				v[i].text.setTextSimple("");
				v[i].text.doDepthTest = true;
				v[i].worldpos = new Transform();
				v[i].worldpos.setIdentity();
			}
		}

		public class TextBoxElement implements Comparable<TextBoxElement>, Comparator<TextBoxElement>, TransformableSubSprite, Serializable {
			public GUITextOverlay text;
			public Vector3f posBuffer = new Vector3f();
			public Transform worldpos;
			public Vector3f offset = new Vector3f();
			public Vector3f rotation = new Vector3f();
			public float dist;
			public long v;
			public SegmentController c;
			public String rawText = "";
			public String realText = "";
			public Vector4f color = new Vector4f();
			public FontLibrary.FontInterface font;
			public ArrayList<Replacement> replacements = new ArrayList<Replacement>();
			public StringBuffer buffer;
			public boolean drawBG = true;
			public boolean holographic;
			public String bgColorName = "blue";
			public boolean changedBgColor;
			public GUIOverlay bg = new GUIOverlay(Controller.getResLoader().getSprite("screen-gui-blue"), state);


			@Override
			public int compareTo(TextBoxElement o) {
				return Float.compare(dist, o.dist);
			}

			@Override
			public int compare(TextBoxElement o1, TextBoxElement o2) {
				return Float.compare(o1.dist, o2.dist);
			}


			@Override
			public float getScale(long time) {
				return -0.00395f;
			}

			@Override
			public int getSubSprite(Sprite sprite) {
				return 0;
			}

			@Override
			public boolean canDraw() {
				return true;
			}

			@Override
			public Transform getWorldTransform() {
				return worldpos;
			}

			public void setBGColor(String color) {
				if(color != null && !color.isEmpty() && !color.equals(bgColorName)) {
					bgColorName = color;
					changedBgColor = true;
				}
			}
		}
	}


}
