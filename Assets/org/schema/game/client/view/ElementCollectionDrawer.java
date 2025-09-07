package org.schema.game.client.view;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13;
import org.schema.game.client.view.shader.OutlineShader;
import org.schema.game.common.controller.BlockTypeSearchMeshCreator;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.ElementCollectionManager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModule;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.UsableControllableSingleElementManager;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.controller.elements.power.reactor.StabilizerUnit;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitUnit;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.CustomOutputUnit;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementCollectionMesh;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.FrameBufferObjects;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.input.Keyboard;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ElementCollectionDrawer implements Shaderable {

	public static boolean flagAllDirty;
	private final GameClientState state;
	private final OutlineShader shader = new OutlineShader();
	private final Object2ObjectOpenHashMap<SegmentController, MContainerDrawJob> drawMap = new Object2ObjectOpenHashMap<SegmentController, MContainerDrawJob>();
	private final Set<MContainerDrawJob> tmpSet = new ObjectOpenHashSet<MContainerDrawJob>();
	private long lastUpdate;
	private boolean flagUpdate;

	private final List<ElementCollection<?, ?, ?>> toDraw = new ObjectArrayList<ElementCollection<?, ?, ?>>();
	public static boolean debugMode;
	public static BlockTypeSearchMeshCreator searchForTypeResult;

	public ElementCollectionDrawer(GameClientState state) {
		this.state = state;

	}

	public void drawToFrameBuffer(FrameBufferObjects fbo) {
		debugMode = Keyboard.isKeyDown(GLFW.GLFW_KEY_COMMA) && Keyboard.isKeyDown(GLFW.GLFW_KEY_MINUS) && Keyboard.isKeyDown(GLFW.GLFW_KEY_PERIOD);

		fbo.enable();
		if (debugMode) {
			System.err.println("OUTLINE DEBUG");
//			drawDebug();
			draw();
		} else {
			draw();
		}
		fbo.disable();
		
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}

	public void drawFrameBuffer(FrameBufferObjects fbo, float meshForce) {
		debugMode = Keyboard.isKeyDown(GLFW.GLFW_KEY_COMMA) && Keyboard.isKeyDown(GLFW.GLFW_KEY_MINUS) && Keyboard.isKeyDown(GLFW.GLFW_KEY_PERIOD);
		if (debugMode) {
			fbo.draw(0);
		} else {
			shader.draw(fbo, meshForce);
		}
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}

	private enum DrawMode {
		RAW,
		OPTIMIZED
	}
	public static final DrawMode DRAW_MODE = DrawMode.OPTIMIZED;

	public boolean hasDrawn() {
		return toDraw.size() > 0;
	}

	public void drawDebug() {
		if (toDraw.size() > 0) {
			drawOptimized(true);
		}
	}

	public void draw() {
//		System.err.println("DDD::: "+searchForTypeResult);
		if (DRAW_MODE == DrawMode.RAW) {
			ShaderLibrary.cubeGroupShader.setShaderInterface(this);
			ShaderLibrary.cubeGroupShader.load();

			for (Entry<SegmentController, MContainerDrawJob> e : drawMap.entrySet()) {
				SegmentController c = e.getKey();
				List<ElementCollection<?, ?, ?>> toDraw = e.getValue().toDraw;
				if (toDraw.size() > 0) {
					drawRaw(c, toDraw);
				}
			}

			ShaderLibrary.cubeGroupShader.unload();
		} else if (toDraw.size() > 0 || searchForTypeResult != null) {
			drawOptimized(false);
		}
		toDraw.clear();
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}
	public static class MContainerDrawJob{
		private ManagedSegmentController<?> m;
		
		@Override
		public boolean equals(Object obj) {
			return ((MContainerDrawJob)obj).m.equals(m);
		}

		@Override
		public int hashCode() {
			return m.hashCode();
		}
		List<ElementCollection<?, ?, ?>> toDraw = new ObjectArrayList<ElementCollection<?, ?, ?>>();
		private boolean changed = true;
		
		public void register(ManagedSegmentController<?> m){
			this.m = m;
			m.getManagerContainer().registerGraphicsListener(this);
		}

		public void flagChanged() {
			this.changed = true;
		}
		private void addManagerContainerMeshesToDraw(ManagerContainer<?> managerContainer, List<ElementCollection<?, ?, ?>> toDraw) {
			for (ManagerModule<?, ?, ?> mod : managerContainer.getModules()) {
				if (mod.getElementManager() instanceof UsableControllableSingleElementManager<?, ?, ?>) {
					UsableControllableSingleElementManager<?, ?, ?> voidMan = (UsableControllableSingleElementManager<?, ?, ?>) mod.getElementManager();
					ElementCollectionManager<?, ?, ?> ec = voidMan.getCollection();
					List<ElementCollection<?, ?, ?>> lst = (List<ElementCollection<?, ?, ?>>) ec.getElementCollections();

					addList(toDraw, lst);

				} else if (mod.getElementManager() instanceof UsableControllableElementManager<?, ?, ?>) {
					UsableControllableElementManager<?, ?, ?> man = (UsableControllableElementManager<?, ?, ?>) mod.getElementManager();
					for (ControlBlockElementCollectionManager<?, ?, ?> c : man.getCollectionManagers()) {
						List<ElementCollection<?, ?, ?>> lst = (List<ElementCollection<?, ?, ?>>) c.getElementCollections();
						addList(toDraw, lst);
					}
				}
			}
		}

		private void addList(List<ElementCollection<?, ?, ?>> toDraw, List<ElementCollection<?, ?, ?>> lst) {
			for (ElementCollection<?, ?, ?> e : lst) {
				if (e.getMesh() != null) {
					toDraw.add(e);
				}
			}
		}

		public void process() {
			if(changed){
				addManagerContainerMeshesToDraw(m.getManagerContainer(), toDraw);			
				changed = false;
			}
		}

		public void unregister() {
						
		}
	}
	public void update(Timer timer, ObjectArrayList<SegmentController> segmentControllers) {
		if (timer.currentTime - lastUpdate > 500 || flagUpdate) {
			tmpSet.addAll(drawMap.values());
			
			for (SegmentController c : segmentControllers) {
				if (c instanceof ManagedSegmentController<?>) {
					MContainerDrawJob job = drawMap.get(c);
					if(job == null){
						job = new MContainerDrawJob();
						job.register((ManagedSegmentController<?>) c);
						drawMap.put(c, job);
					}else{
						tmpSet.remove(job);
					}
					job.process();
				}
			}
			
			
			for(MContainerDrawJob m : tmpSet){
				m.unregister();
				drawMap.remove(m.m);
			}
			lastUpdate = timer.currentTime;
			flagUpdate = false;
			tmpSet.clear();
		}

		if (flagAllDirty) {
			System.err.println("[ELEMENTCOLLECTIONDRAWER] FLAG ALL DIRTY");
			for (SegmentController c : segmentControllers) {
				if (c instanceof ManagedSegmentController<?>) {
					ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) c).getManagerContainer();
					managerContainer.flagAllCollectionsDirty();
				}
			}
			flagAllDirty = false;
		}
	}
	public boolean checkDraw() {
		toDraw.clear();
		short selectedType = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getSelectedTypeWithSub();

		for (Entry<SegmentController, MContainerDrawJob> e : drawMap.entrySet()) {
			final SegmentController c = e.getKey();
			
			if ((state.getCurrentPlayerObject() != null && c != state.getCurrentPlayerObject() && 
					(state.getCurrentPlayerObject().canSeeReactor(c, true) || state.getCurrentPlayerObject().canSeeChambers(c, true) || state.getCurrentPlayerObject().canSeeWeapons(c, true))
					) || 
					(state.isInAnyBuildMode() && (c == state.getCurrentPlayerObject() || (state.isInCharacterBuildMode() && BuildModeDrawer.currentPiece != null && BuildModeDrawer.currentPiece.getSegmentController() == c)))) {
				
			
				final List<ElementCollection<?, ?, ?>> td = e.getValue().toDraw;
				if (td.size() > 0) {
					for (ElementCollection<?, ?, ?> col : td) {
						ElementCollectionMesh mesh = col.getMesh();
						if (mesh != null) {
	
							if (state.getCurrentPlayerObject() != null && c != state.getCurrentPlayerObject()) {
								if (col instanceof MainReactorUnit
									&& state.getCurrentPlayerObject().canSeeReactor(c, true)) {
									mesh.markDraw();
								}
								if (col instanceof ReactorChamberUnit
									&& state.getCurrentPlayerObject().canSeeChambers(c, true)) {
									mesh.markDraw();
								}
								if (col instanceof CustomOutputUnit
									&& state.getCurrentPlayerObject().canSeeWeapons(c, true)) {
									mesh.markDraw();
								}
							} else if ((col instanceof ReactorChamberUnit || col instanceof MainReactorUnit || col instanceof ConduitUnit || col instanceof StabilizerUnit)
								&& state.isInAnyBuildMode() && (c == state.getCurrentPlayerObject() || state.isInCharacterBuildMode())) {
	//							if(col.contains(BuildModeDrawer.currentPiece.getAbsoluteIndex())){
								float alpha = 0.5f;
	
								if (col instanceof ConduitUnit && selectedType == ElementKeyMap.REACTOR_CONDUIT) {
									mesh.setColor(1, 1, 0, alpha);
									mesh.markDraw();
								}
								if (col instanceof MainReactorUnit && (selectedType == ElementKeyMap.REACTOR_MAIN || selectedType == ElementKeyMap.REACTOR_STABILIZER)) {
									if(((ManagedSegmentController<?>)col.getSegmentController()).getManagerContainer().getPowerInterface().isActiveReactor(col.idPos)){
										mesh.setColor(0.23f, 0.23f, 1, 0.78f);
									}else{
										mesh.setColor(1.0f, 1, 0, 0.71f);
									}
									mesh.markDraw();
								}
								if (col instanceof ReactorChamberUnit && ElementKeyMap.isChamber(selectedType)) {
									ElementInformation info = ElementKeyMap.getInfo(col.elementCollectionManager.getEnhancerClazz());
									if (info.isReactorChamberGeneral()) {
										mesh.setColor(0.7f, 0, 1, alpha);
									} else {
										mesh.setColor(1.0f, 0, 0.7f, alpha);
									}
									mesh.markDraw();
								}
	
								if (col instanceof StabilizerUnit && selectedType == ElementKeyMap.REACTOR_STABILIZER) {
									if(BuildModeDrawer.currentPiece != null && col.containsAABB(BuildModeDrawer.currentPiece.getAbsoluteIndex())){
										mesh.setColor(0.4f, 0.8f, 1, alpha);
									}else{
										mesh.setColor(0.2f, 0.3f, 0.7f, alpha);
									}
									mesh.markDraw();
								}
	
							}
	
							if (mesh.isDraw() && mesh.isVisibleFrustum(c.getWorldTransformOnClient())) {
								toDraw.add(col);
							}
						}
					}
				}
			}
		}
		return toDraw.size() > 0 || searchForTypeResult != null;
	}

	private void drawOptimized(final boolean debug) {
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if (debug) {
			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GlUtil.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		}

		if(toDraw.size() > 0){
			GlUtil.glPushMatrix();
	
			SegmentController c = null;
	
			for (ElementCollection<?, ?, ?> e : toDraw) {
				if (c != e.getSegmentController()) {
					if (c != null) {
						GlUtil.glPopMatrix();
					}
					c = e.getSegmentController();
					GlUtil.glPushMatrix();
					GlUtil.glMultMatrix(c.getWorldTransformOnClient());
				}
				ElementCollectionMesh mesh = e.getMesh();
				if (mesh != null) {
					if (debug) {
						mesh.drawDebug();
					} else {
						mesh.draw();
					}
				}
			}
			GlUtil.glPopMatrix();
			GlUtil.glPopMatrix();
		}
		
		if(searchForTypeResult != null){
			
			if(searchForTypeResult.c != state.getCurrentPlayerObject()){
				searchForTypeResult.cleanUp();
				searchForTypeResult = null;
				
			}else{
				searchForTypeResult.m.markDraw();
				GlUtil.glPushMatrix();
				GlUtil.glMultMatrix(searchForTypeResult.c.getWorldTransformOnClient());
				searchForTypeResult.m.draw();
//				System.err.println("CLLL: "+searchForTypeResult.m.getVertexCount()+"; ");
				GlUtil.glPopMatrix();
			}
		}
		
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1, 1);
		if (debug) {
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GlUtil.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		}

	}

	private void drawRaw(SegmentController c, List<ElementCollection<?, ?, ?>> toDraw) {
		assert (ElementCollectionMesh.DRAW_MODE == org.schema.game.common.data.element.ElementCollectionMesh.DrawMode.RAW);
		if (ElementCollectionMesh.USE_INT_ATT()) {
			GL20.glEnableVertexAttribArray(ElementCollectionMesh.VERT_ATTRIB_INDEX);
		}
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlUtil.glPushMatrix();
		GlUtil.glMultMatrix(c.getWorldTransformOnClient());
		for (ElementCollection<?, ?, ?> e : toDraw) {
			if (e.getMesh() != null) {
				if (e.getMesh().isVisibleFrustum(c.getWorldTransformOnClient())) {
					e.getMesh().draw();
				}
			}
		}

		GlUtil.glPopMatrix();
		GlUtil.glDisable(GL11.GL_BLEND);
		if (ElementCollectionMesh.USE_INT_ATT()) {
			GL20.glDisableVertexAttribArray(ElementCollectionMesh.VERT_ATTRIB_INDEX);
		}

		GL11.glColor4f(1, 1, 1, 1);
	}

	

	@Override
	public void onExit() {
		
	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
			GlUtil.updateShaderCubeNormalsBiNormalsAndTangentsBoolean(shader);
			GlUtil.printGlErrorCritical();
			FloatBuffer fb = GlUtil.getDynamicByteBuffer(6 * 3 * 4, 0)
				.asFloatBuffer();
			fb.rewind();
			for (int i = 0; i < CubeMeshQuadsShader13.quadPosMark.length; i++) {
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].x);
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].y);
				fb.put(CubeMeshQuadsShader13.quadPosMark[i].z);
			}

			fb.rewind();
			GlUtil.updateShaderFloats3(shader, "quadPosMark", fb);
			GlUtil.printGlErrorCritical();

			// putTexOrder(shader);
			shader.recompiled = false;
			GlUtil.printGlErrorCritical();

		}
	}

	public void flagUpdate() {
		this.flagUpdate = true;
	}
}
