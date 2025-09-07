package org.schema.game.client.view.camera.drone;

import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.texture.Texture;
import org.schema.schine.network.objects.Sendable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CameraDroneManager implements Drawable, SendableAddedRemovedListener{

	private Mesh droneMesh;
	private boolean init;
	private final Map<PlayerState, CameraDrone> drones = new Object2ObjectOpenHashMap<PlayerState, CameraDrone>();
	
	private final GameClientState state;
	
	
	
	public CameraDroneManager(GameClientState state) {
		super();
		this.state = state;
		
	}

	
	@Override
	public void cleanUp() {
		this.state.getController().removeSendableAddedRemovedListener(this);
		init = false;
		droneMesh = null;
		drones.clear();
	}

	@Override
	public void draw() {
		if(!init) {
			onInit();
		}
		loadShader(droneMesh.getMaterial().getTexture(), ShaderLibrary.mineShader);
		droneMesh.loadVBO(true);
		for(CameraDrone e : drones.values()) {
			GlUtil.glPushMatrix();
			e.draw(droneMesh);
			GlUtil.glPopMatrix();
		}
		droneMesh.unloadVBO(true);
		unloadShader(droneMesh.getMaterial().getTexture(), ShaderLibrary.mineShader);
	}
	protected void loadShader(Texture texture, Shader shader) {
		shader.loadWithoutUpdate();
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
		GlUtil.updateShaderInt(shader, "diffuseMap", 0);
		GlUtil.updateShaderVector4f(shader, "tint", 1,1,1,1);
		
	}
	protected void unloadShader(Texture texture, Shader shader) {
		shader.unloadWithoutExit();
		GlUtil.glActiveTexture(GL13.GL_TEXTURE0);
		GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	@Override
	public boolean isInvisible() {
		return false;
	}

	@Override
	public void onInit() {
		droneMesh = (Mesh) Controller.getResLoader().getMesh("CameraDrone").getChilds().get(0);
		
		this.state.getController().addSendableAddedRemovedListener(this);
		for(Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
			onAddedSendable(s);
		}
		
		init = true;
	}

	@Override
	public void onAddedSendable(Sendable s) {
		if(s instanceof PlayerState) {
			PlayerState p = (PlayerState)s;
			drones.put(p, new CameraDrone(p));
		}
	}

	@Override
	public void onRemovedSendable(Sendable s) {
		if(s instanceof PlayerState) {
			PlayerState p = (PlayerState)s;
			drones.remove(p);
		}		
	}

	
	
}
