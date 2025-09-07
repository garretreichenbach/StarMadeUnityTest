package org.schema.game.client.view.beam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.SelectionShader;
import org.schema.game.client.view.effects.TransformCameraDistanceComparator;
import org.schema.game.common.controller.BeamHandlerContainer;
import org.schema.game.common.controller.elements.BeamState;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.controller.observer.DrawerObserver;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.AbstractCharacter;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.graphicsengine.texture.textureImp.Texture3D;
import org.schema.schine.input.Keyboard;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;

public class BeamDrawerManager implements Drawable, Shaderable, DrawerObserver {

	public static ArrayList<BeamDrawer> drawerPool = new ArrayList<BeamDrawer>();
	public static int drawCalls = 0;
	static Mesh mesh;
	static Mesh singlecubemesh;
	private static Texture3D tex;
	private final Map<SimpleTransformableSendableObject<?>, List<BeamDrawer>> beamDrawers = new Object2ObjectOpenHashMap<SimpleTransformableSendableObject<?>, List<BeamDrawer>>();
	private final ObjectArrayFIFOQueue<UsableControllableElementManager<?, ?, ?>> needsUpdate = new ObjectArrayFIFOQueue<UsableControllableElementManager<?, ?, ?>>();
	private final ObjectArrayFIFOQueue<ShipBeam> toAdd = new ObjectArrayFIFOQueue<ShipBeam>();
	private final TransformCameraDistanceComparator comparatorStart = new TransformCameraDistanceComparator(false);
	private final TransformCameraDistanceComparator comparatorEnd = new TransformCameraDistanceComparator(true);
	private final ObjectHeapPriorityQueue<BeamState> sortedStates = new ObjectHeapPriorityQueue<BeamState>(comparatorStart);
	private final ObjectHeapPriorityQueue<BeamState> sortedStatesEnd = new ObjectHeapPriorityQueue<BeamState>(comparatorEnd);
	boolean alphaToCoverage = false;
	private boolean init = false;
	private SelectionShader selectionShader;
	private int beamPointer;
	private BeamDrawer[] toDraw = new BeamDrawer[EngineSettings.G_MAX_BEAMS.getInt()];
	private final List<Vector3f> beamHits = new ObjectArrayList<Vector3f>();
	private final List<Vector4f> beamHitColors = new ObjectArrayList<Vector4f>();
	private final FloatArrayList beamHitSizes = new FloatArrayList();
	private float ticks;
	private boolean drawNeeded;
	private final ClientState state;
	private float zoomFac;
	private float time;
	public BeamDrawerManager(ClientState state) {
		this.state = state;
	}
	public static BeamDrawer getDrawerFromPool(BeamDrawerManager thisMan, List<? extends BeamHandlerContainer<?>> managers) {
		if (drawerPool.isEmpty()) {
			return new BeamDrawer(thisMan, managers);
		} else {
			BeamDrawer remove = null;
			synchronized (drawerPool) {
				remove = drawerPool.remove(drawerPool.size()-1);
			}
			remove.set(managers, thisMan);
			return remove;
		}
	}

	public static void release(BeamDrawer d) {
		synchronized (drawerPool) {
			d.clearObservers();
			d.reset();
			drawerPool.add(d);
		}
	}

	public void add(List<? extends BeamHandlerContainer> managers, SimpleTransformableSendableObject p) {
		if(managers == null) {
			try {
				throw new Exception("tried to add empty manager list");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		BeamDrawer b = getDrawerFromPool(this, (List<? extends BeamHandlerContainer<?>>) managers);
			toAdd.enqueue(new ShipBeam(b, p));
	}
	public void add(BeamHandlerContainer<?> managers, SimpleTransformableSendableObject p) {
		if(managers == null) {
			try {
				throw new Exception("tried to add empty manager list");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		List<BeamHandlerContainer<?>> l = new ObjectArrayList<BeamHandlerContainer<?>>(1);
		l.add(managers);
		BeamDrawer b = getDrawerFromPool(this, l);
			toAdd.enqueue(new ShipBeam(b, p));
	}
	@Override
	public void cleanUp() {
	}
	@Override
	public void draw() {
		throw new RuntimeException("use draw(zoomFac)");
	}
	private final Vector4f curColor = new Vector4f();
	public void draw(float zoomFac) {
		drawCalls = 0;
		if (!init) {
			onInit();
		}
//		prepareSorted();
		if (!drawNeeded) {
			return;
		}
		
		
		prepareDraw(zoomFac);

		
		for (int i = 0; i < beamPointer; i++) {
			
			BeamDrawer d = toDraw[i];
			d.draw();
			
		}

		endDraw();

		if(beamHits.size() > 0) {
			
			ShaderLibrary.simpleColorShader.loadWithoutUpdate();
			
			Mesh sphere = (Mesh) Controller.getResLoader().getMesh("SphereLowPoly").getChilds().get(0);
			sphere.loadVBO(true);
			
			curColor.set(0,0,0,0);
			final float basicSize = 0.2f;
			for(int i = 0; i < beamHits.size(); i++) {
				Vector3f pos = beamHits.get(i);
				float size = beamHitSizes.getFloat(i);
				Vector4f color = beamHitColors.get(i);
				GlUtil.glColor4f(color);
				
				if(!curColor.equals(color)) {
					GlUtil.updateShaderVector4f(ShaderLibrary.simpleColorShader, "col", 
							Math.min(1f, color.x + 0.1f),
							Math.min(1f, color.y + 0.1f),
							Math.min(1f, color.z + 0.1f),
							Math.min(1f, color.w + 0.3f)
							);
					curColor.set(color);
				}
				GlUtil.glPushMatrix();
				GlUtil.glTranslatef(pos);
				
				GlUtil.scaleModelview(basicSize*size, basicSize*size, basicSize*size);
				sphere.renderVBO();
				
				GlUtil.glPopMatrix();
				
			}
			GlUtil.glColor4f(1, 1, 1, 1);
			sphere.unloadVBO(true);
			
			beamHits.clear();
			beamHitSizes.clear();
			
			
			ShaderLibrary.simpleColorShader.unloadWithoutExit();
		}
//		drawSalvageBoxes();

		//		System.err.println("TOTAL DRAWN "+drawCalls);
	}

	@Override
	public boolean isInvisible() {
				return false;
	}

	@Override
	public void onInit() {
		if(init) {
			return;
		}
		if (mesh == null) {
			mesh = (Mesh) Controller.getResLoader().getMesh("SimpleBeam").getChilds().get(0);
			;
		}
		if (tex == null) {
			tex = GameResourceLoader.noiseVolume;
		}

		singlecubemesh = ((Mesh) Controller.getResLoader().getMesh("Box").getChilds().get(0));
		selectionShader = new SelectionShader(singlecubemesh.getMaterial().getTexture().getTextureId());
		init = true;
	}

	public void clear() {
		long t = System.currentTimeMillis();

		for (int i = 0; i < toDraw.length; i++) {
			toDraw[beamPointer] = null;
		}
		Collection<List<BeamDrawer>> values = this.beamDrawers.values();
		for (List<BeamDrawer> beamDrawers : values) {
			for (int i = 0; i < beamDrawers.size(); i++) {
				beamDrawers.get(i).clearObservers();
				release(beamDrawers.get(i));
			}
		}

		beamPointer = 0;

		beamDrawers.clear();

		long took = (System.currentTimeMillis() - t);
		if (took > 10) {
			System.err.println("[CLIENT] WARNING: CLEARING BREAM DRAW MANAGER TOOK " + took);
		}
	}

	public void drawSalvageBoxes() {
		if (!init) {
			onInit();
		}
		if (!drawNeeded || Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			return;
		}
		GlUtil.glEnable(GL11.GL_CULL_FACE);
		GlUtil.glDisable(GL11.GL_LIGHTING);

		ShaderLibrary.beamBoxShader.setShaderInterface(selectionShader);
		ShaderLibrary.beamBoxShader.load();
		singlecubemesh.loadVBO(true);

		for (int i = 0; i < beamPointer; i++) {

			toDraw[i].drawSelectionBoxes();
		}

		singlecubemesh.unloadVBO(true);
		ShaderLibrary.beamBoxShader.unload();
		GlUtil.glDepthMask(true);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_BLEND);
	}

	/**
	 * @return the sortedStates
	 */
	public ObjectHeapPriorityQueue<BeamState> getSortedStates() {
		return sortedStates;
	}

	/**
	 * @return the sortedStatesEnd
	 */
	public ObjectHeapPriorityQueue<BeamState> getSortedStatesEnd() {
		return sortedStatesEnd;
	}

	public void notifyOfBeam(BeamDrawer o, boolean active) {
		if (active) {
			//
			if (beamPointer < toDraw.length) {

				toDraw[beamPointer] = o;
				beamPointer++;
			}
			//			System.err.println("NOTIFIED OF BEAM "+beamPointer);
		} else {
			//							System.err.println("NOTIFIED OF BEAM END");
			if (beamPointer > 0 && beamPointer < toDraw.length) {
				for (int i = 0; i < toDraw.length; i++) {
					if (toDraw[i] == o) {
						toDraw[i] = toDraw[beamPointer - 1];
						beamPointer--;
						break;
					}
				}
			}
		}
		drawNeeded = beamPointer > 0;
	}

	@Override
	public void onExit() {
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL12.GL_TEXTURE_3D, 0);
		GlUtil.glDisable(GL12.GL_TEXTURE_3D);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);

	}

	@Override
	public void updateShader(DrawableScene scene) {

	}

	@Override
	public void updateShaderParameters(Shader shader) {
		if(shader.recompiled) {
			BeamDrawer.resetShader = true;
		}
		
		GlUtil.updateShaderFloat(shader, "beamTime", time);
		
		assert (tex.getId() > 0);
		
		if (shader.recompiled) {
			 
			GlUtil.printGlErrorCritical();
		}

		GlUtil.updateShaderFloat(shader, "zoomFac", zoomFac);
		
		
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL12.GL_TEXTURE_3D);
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL12.GL_TEXTURE_3D, tex.getId());
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}
		
		GlUtil.updateShaderInt(shader, "noiseTex", 0);
		if (shader.recompiled) {
			GlUtil.printGlErrorCritical();
		}

	}

	public void prepareDraw(float zoomFac) {
		if(EngineSettings.F_FRAME_BUFFER.isOn() && zoomFac == 0d) {
			GL11.glDepthMask(false);
		}
		this.zoomFac = zoomFac;
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		GlUtil.glDisable(GL11.GL_CULL_FACE);
		if (alphaToCoverage) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glEnable(GL13.GL_SAMPLE_ALPHA_TO_COVERAGE);
		} else {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
				GlUtil.printGlErrorCritical();
			}
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		ShaderLibrary.simpleBeamShader.setShaderInterface(this);
		ShaderLibrary.simpleBeamShader.load();
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		mesh.loadVBO(true);
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_F2)) {
			GlUtil.printGlErrorCritical();
		}
		BeamDrawer.prepareDraw();
	}

	public void endDraw() {
		mesh.unloadVBO(true);
		if (alphaToCoverage) {
			GlUtil.glDisable(GL13.GL_SAMPLE_ALPHA_TO_COVERAGE);
		} else {
			GlUtil.glDisable(GL11.GL_BLEND);
		}
		ShaderLibrary.simpleBeamShader.unload();
		GL11.glDepthMask(true);
	}

	public void prepareSorted() {
		if (!init) {
			onInit();
		}
		if (!drawNeeded) {
			//			System.err.println("UPDATED: "+getSortedStates().size());
			return;
		}
//		System.err.println("-----------------SORTING START");
		sortedStates.clear();
		for (int i = 0; i < beamPointer; i++) {
			while(!toDraw[i].isValid() && beamPointer > 0) {
				if(i < beamPointer-1) {
					toDraw[i] = toDraw[beamPointer-1];
				}
				beamPointer--;
			}
			if(i < beamPointer) {
				BeamDrawer d = toDraw[i];
				d.insertStart(sortedStates);
			}
			
			
			
			//			d.insertEnd(getSortedStates());
//			System.err.println("TODRAW: "+i+": "+d);
		}
//		System.err.println("SORTED: "+getSortedStates()+"; beam pointer: "+beamPointer);
	}

	public void refresh(
			Int2ObjectOpenHashMap<SimpleTransformableSendableObject<?>> currentSectorEntities) {
		Iterator<SimpleTransformableSendableObject<?>> iterator = beamDrawers.keySet().iterator();
		while (iterator.hasNext()) {
			SimpleTransformableSendableObject<?> next = iterator.next();
			if (!currentSectorEntities.containsKey(next.getId())) {
				List<BeamDrawer> arrayList = beamDrawers.get(next);
				for (BeamDrawer d : arrayList) {
					d.clearObservers();
					release(d);
				}
				arrayList.clear();
				iterator.remove();
				
			}
		}

		for (SimpleTransformableSendableObject s : currentSectorEntities.values()) {
			if (!beamDrawers.containsKey(s)) {
				refresh(s);
			}
		}

	}

	public void refresh(Sendable sendable) {
		if (sendable instanceof SimpleTransformableSendableObject) {
			SimpleTransformableSendableObject<?> s = (SimpleTransformableSendableObject<?>) sendable;
			
			if(s instanceof ManagedSegmentController<?>) {
				List<BeamElementManager<?, ?, ?>> beamManagers = ((ManagedSegmentController<?>)s).getManagerContainer().getBeamManagers();
				for( BeamElementManager<?, ?, ?> e : beamManagers) {
					add(e.getCollectionManagers(), (s));
				}
				List<BeamHandlerContainer<?>> bi = ((ManagedSegmentController<?>)s).getManagerContainer().getBeamInterfacesSingle();
				
				for(BeamHandlerContainer<?> e : bi) {
					add(e, s);
				}
				
				
			}
			
			
			if (s instanceof AbstractCharacter<?>) {
				List<BeamHandlerContainer<?>> d = new ObjectArrayList<BeamHandlerContainer<?>>();
				d.add(((AbstractCharacter<?>) s));
				add(d, s);
			}
		}
	}

	@Override
	public void update(DrawerObservable o, Object arg, Object message) {
		//				System.err.println("BEAM DRAWER UPDATE: "+o+"; "+arg);

		if (o instanceof UsableControllableElementManager<?, ?, ?>) {

			UsableControllableElementManager<?, ?, ?> elementManager = (UsableControllableElementManager<?, ?, ?>) o;

			needsUpdate.enqueue(elementManager);

		}

	}

	public void update(Timer timer) {

		//		alphaToCoverage = Keyboard.isKeyDown(GLFW.GLFW_KEY_Y);

		if (alphaToCoverage) {
			System.err.println("ALPHA COVERAGE");
		}
		time += timer.getDelta();
		if (!toAdd.isEmpty()) {

			while (!toAdd.isEmpty()) {
				ShipBeam e = toAdd.dequeue();
				if (!beamDrawers.containsKey(e.ship)) {
					beamDrawers.put(e.ship, new ObjectArrayList<BeamDrawer>());
				}
				beamDrawers.get(e.ship).add(e.beam);
				//							System.err.println("ADDED BEAM OBSERVER "+e.beam.getBeamHandlers());
			}

		}
		if (!needsUpdate.isEmpty()) {
			// update directly from ShipManagerContainer#add/remove HarvestingBeam
			long t = System.currentTimeMillis();
			while (!needsUpdate.isEmpty()) {

				UsableControllableElementManager<?, ?, ?> elementManager = needsUpdate.dequeue();

				List<BeamDrawer> segmentControllerBeamDrawerList = beamDrawers.get(elementManager.getSegmentController());

				if (segmentControllerBeamDrawerList != null) {

					for (int i = 0; i < segmentControllerBeamDrawerList.size(); i++) {
						BeamDrawer beamDrawer = segmentControllerBeamDrawerList.get(i);
						beamDrawer.clearObservers();
						release(beamDrawer);
					}

					segmentControllerBeamDrawerList.clear();

					refresh(elementManager.getSegmentController());

				}
				for (int i = 0; i < toDraw.length; i++) {
					toDraw[i] = null;
				}
				beamPointer = 0;
			}
			long took = (System.currentTimeMillis() - t);
			if (took > 10) {
				System.err.println("[CLIENT] WARNING: REFRESHED THE BEAM DRAWERS " + took);
			}

		}

		for (int i = 0; i < beamPointer; i++) {
			BeamDrawer d = toDraw[i];
			d.update(timer);
		}

		ticks += timer.getDelta() / 100 * ((Math.random() + 0.0001f) / 0.1f);
		if (ticks > 1f) {
			ticks -= ((int) ticks);
		}
	}

	private class ShipBeam {
		private BeamDrawer beam;
		private SimpleTransformableSendableObject ship;

		public ShipBeam(BeamDrawer b, SimpleTransformableSendableObject p) {
			super();
			this.beam = b;
			this.ship = p;
		}

	}

	public long getTime() {
		return state.getUpdateTime();
	}
	public float getZoomFac() {
		return zoomFac;
	}
	public void setZoomFac(float zoomFac) {
		this.zoomFac = zoomFac;
	}
	public void addHitpoint(Vector3f hitPoint, float f, Vector4f color) {
		beamHits.add(hitPoint);
		beamHitSizes.add(f);
		beamHitColors.add(color);
	}

}
