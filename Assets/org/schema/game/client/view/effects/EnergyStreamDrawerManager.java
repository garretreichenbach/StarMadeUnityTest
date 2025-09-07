package org.schema.game.client.view.effects;

import org.lwjgl.glfw.GLFW;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PowerChangeListener;
import org.schema.game.client.data.RailDockingListener;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.power.reactor.PowerImplementation;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.input.Keyboard;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class EnergyStreamDrawerManager implements Drawable, RailDockingListener, PowerChangeListener {

	private final Object2ObjectOpenHashMap<SegmentController, EnergyStreamDrawer> map = new Object2ObjectOpenHashMap<SegmentController, EnergyStreamDrawer>();
	private final GameClientState state;

	private float conTime;
	private final ObjectOpenHashSet<SegmentController> changedSet = new ObjectOpenHashSet<SegmentController>();
	private float time;
	private boolean debugKey;
	private EnergyStreamDrawer currentEnteredDrawer;

	public EnergyStreamDrawerManager(GameClientState state) {
		this.state = state;
		
		state.getDockingListeners().add(this);
		state.getPowerChangeListeners().add(this);
	}

	@Override
	public void cleanUp() {
		for(EnergyStreamDrawer e : map.values()){
			e.cleanUp();
		}
	}
	public static Shader shader;
	@Override
	public void draw() {

//		for (EnergyStreamDrawer s : map.values()) {
//			s.drawDebug();
//		}
		if(EngineSettings.USE_ADV_ENERGY_BEAM_SHADER.isOn()){
			shader = ShaderLibrary.tubesStreamShader;
		}else{
			shader = ShaderLibrary.tubesShader;
		}
		shader.loadWithoutUpdate();
		GlUtil.updateShaderFloat(shader, "time", time);
		
		for (EnergyStreamDrawer s : map.values()) {
			
			if (!(s.getSegmentController()).isCloakedFor(state.getCurrentPlayerObject())) {
				
				s.draw();
			}
		}
		if(currentEnteredDrawer != null && !map.containsKey(currentEnteredDrawer.getSegmentController())){
			if(!state.getCurrentSectorEntities().containsKey(currentEnteredDrawer.getSegmentController().getId())){
				currentEnteredDrawer.cleanUp();
				currentEnteredDrawer = null;
			}else{
				if(state.isInAnyStructureBuildMode()){
					currentEnteredDrawer.draw();
				}
			}
		}

		shader.unloadWithoutExit();
		GlUtil.glColor4fForced(1, 1, 1, 1);
	}

	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {

	}

	public void update(Timer timer) {
		time += timer.getDelta();
		conTime += timer.getDelta() * 2f;
		conTime -= (int) conTime;
		
		if(!this.changedSet.isEmpty()){
			ObjectIterator<SegmentController> iterator = this.changedSet.iterator();
			while(iterator.hasNext()){
				SegmentController s = iterator.next();
				if(s.isFullyLoaded()){
//					System.err.println("UPDATING STREAM::::::: "+s+"; "+((ManagedSegmentController<?>)s).getManagerContainer().getPowerInterface().getStabilizerPaths().size());
					if(PowerImplementation.hasEnergyStream(s)){
						if (!map.containsKey(s)) {
							EnergyStreamDrawer connectionDrawer = new EnergyStreamDrawer(s);
							map.put(s, connectionDrawer);
						}
						map.get(s).flagUpdate();
//						System.err.println("ADDED:::::::: "+s+"; "+((ManagedSegmentController<?>)s).getManagerContainer().getPowerInterface().getStabilizerPaths().size());
					}else{
//						System.err.println("REMOVED:::::::: "+s+"; "+((ManagedSegmentController<?>)s).getManagerContainer().getPowerInterface().getStabilizerPaths().size());
						EnergyStreamDrawer remove = map.remove(s);
						if(remove != null){
							remove.cleanUp();
						}
					}
					iterator.remove();
				}
			}
		}
		if(state.getCurrentPlayerObject() == null ){
			if(currentEnteredDrawer != null){
				currentEnteredDrawer.cleanUp();
				currentEnteredDrawer = null;
			}
		}else if(PowerImplementation.hasEnergyStreamDocked(state.getCurrentPlayerObject())){
			if(currentEnteredDrawer == null || currentEnteredDrawer.getSegmentController() != (SegmentController)state.getCurrentPlayerObject()){
				if(currentEnteredDrawer != null){
					currentEnteredDrawer.cleanUp();
				}
				currentEnteredDrawer = new EnergyStreamDrawer((SegmentController)state.getCurrentPlayerObject());
			}
		}
		boolean bKey = debugKey;
		
		debugKey = Keyboard.isKeyDown(GLFW.GLFW_KEY_F1) && Keyboard.isKeyDown(GLFW.GLFW_KEY_END);
		
		if(!bKey && debugKey){
			if(EnergyStreamDrawer.radiusScale == 1.0f){
				EnergyStreamDrawer.radiusScale = 10.0f;
			}else{
				EnergyStreamDrawer.radiusScale = 1.0f;
			}
			for(EnergyStreamDrawer e : map.values()){
				e.flagUpdate();
			}
		}
	}
	
	
	public void flagChanged(SegmentController s) {
		this.changedSet.add(s);
	}

	public void updateEntities() {
		for (SimpleTransformableSendableObject<?> s : state.getCurrentSectorEntities().values()) {
			
			if (PowerImplementation.hasEnergyStream(s)) {
				if (!map.containsKey(s)) {
					EnergyStreamDrawer connectionDrawer = new EnergyStreamDrawer((SegmentController) s);
					map.put((SegmentController) s, connectionDrawer);
				}
			}else{
				EnergyStreamDrawer r = map.remove(s);
				if(r != null){
					r.cleanUp();
				}
			}
		}
		ObjectIterator<SegmentController> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			SegmentController s = iterator.next();
			if (!s.isNeighbor(s.getSectorId(), state.getCurrentSectorId()) || !s.getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(s.getId())) {
				EnergyStreamDrawer connectionDrawer = map.get(s);
				if(connectionDrawer != null){
					connectionDrawer.cleanUp();
				}
				iterator.remove();
			}
		}

	}

	@Override
	public void powerChanged(SegmentController c, PowerChangeType t) {
		if(t == PowerChangeType.STABILIZER_PATH){
			flagChanged(c);
		}
	}

	@Override
	public void dockingChanged(SegmentController c, boolean docked) {
		flagChanged(c);		
	}
	public void clear() {
		cleanUp();
		map.clear();
	}
}
