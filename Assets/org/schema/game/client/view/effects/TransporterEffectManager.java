package org.schema.game.client.view.effects;

import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.TransporterModuleInterface;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class TransporterEffectManager implements Shaderable, Drawable{

	
	public Int2ObjectOpenHashMap<TransporterEffectGroup> groups = new Int2ObjectOpenHashMap<TransporterEffectGroup>();
	public TransporterEffectManager(GameClientState state) {
	}
	public void removeFromSegmentController(SegmentController s){
		groups.remove(s.getId());
	}
	public void addFromSegmentController(SegmentController s){
		if(!groups.containsKey(s.getId())){
			
			if(s instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)s).getManagerContainer() instanceof TransporterModuleInterface){
				TransporterModuleInterface t = (TransporterModuleInterface) ((ManagedSegmentController<?>)s).getManagerContainer();
				
				List<TransporterCollectionManager> collectionManagers = t.getTransporter().getCollectionManagers();
				if(collectionManagers.size() > 0){
					groups.put(s.getId(), new TransporterEffectGroup(s, collectionManagers));
				}
			}
			
			
		}
	}
	
	public void updateLocal(Timer timer){
		time += timer.getDelta();
		for(TransporterEffectGroup g : groups.values()){
			g.updateLocal(timer);
			
		}
	}
	@Override
	public void draw(){
		if(groups.size() > 0){
			
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlUtil.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			ShaderLibrary.transporterShader.setShaderInterface(this);
			ShaderLibrary.transporterShader.load();
			Mesh m = (Mesh) Controller.getResLoader().getMesh("Cylinder").getChilds().get(0);
			m.loadVBO(true);
			
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			for(TransporterEffectGroup g : groups.values()){
				g.draw(m);
			}
			m.unloadVBO(true);		
			
			ShaderLibrary.transporterShader.unload();
			
			
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
		}
	}
	@Override
	public void onInit() {
				
	}
	public void onColChanged(
			TransporterCollectionManager t) {
		addFromSegmentController(t.getSegmentController());
	}
	private Set<SimpleTransformableSendableObject<?>> toDel = new ObjectOpenHashSet<SimpleTransformableSendableObject<?>>();
	private float time;
	public void sectorEntitiesChanged(
			ObjectCollection<SimpleTransformableSendableObject<?>> values) {
		
		for(TransporterEffectGroup d : groups.values()){
			toDel.add(d.segmentController);
		}
		for(SimpleTransformableSendableObject<?> d : values){
			if(d instanceof SegmentController){
				toDel.remove(d);
				addFromSegmentController((SegmentController)d);
			}
		}
		for(SimpleTransformableSendableObject<?> d : toDel){
			removeFromSegmentController((SegmentController)d);
		}
	}
	public void onColRemoved(
			TransporterCollectionManager transporterCollectionManager) {
		if(transporterCollectionManager.getElementManager().getCollectionManagers().isEmpty()){
			removeFromSegmentController(transporterCollectionManager.getSegmentController());
		}
	}
	@Override
	public void onExit() {
	}
	@Override
	public void updateShader(DrawableScene scene) {
	}
	@Override
	public void updateShaderParameters(Shader shader) {
		GlUtil.updateShaderFloat(shader, "time", time);
	}
	@Override
	public void cleanUp() {
				
	}
	@Override
	public boolean isInvisible() {
				return false;
	}
}
