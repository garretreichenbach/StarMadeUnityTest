package org.schema.game.client.view.meshlod;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

public class LODMeshSprite<E extends LODCapable> extends LODMesh<E>{

	public final String meshA;
	public final String spriteName;
	private Mesh m;
	private Sprite sprite;
	private final int subSpriteIndex;
	
	public LODMeshSprite(int lodIndex, float maxDistance, String meshA, String spriteName, int subSpriteIndex) {
		super(lodIndex, maxDistance);
		this.meshA = meshA;
		this.spriteName = spriteName;
		this.subSpriteIndex = subSpriteIndex;
	}

	@Override
	public void loadResourcesA() {
		Mesh mesh = Controller.getResLoader().getMesh(meshA);
		assert(mesh != null):"Could not load "+meshA;
		m = (Mesh) mesh.getChilds().get(0);
		loadShader(m.getMaterial().getTexture(), ShaderLibrary.mineShader);
		m.loadVBO(true);
	}

	@Override
	public void loadResourcesB() {
		sprite = Controller.getResLoader().getSprite(spriteName);
		assert(sprite != null):spriteName;
	}

	@Override
	public void unloadResourcesA() {
		Mesh mesh = Controller.getResLoader().getMesh(meshA);
		m = (Mesh)mesh.getChilds().get(0);
		unloadShader(m.getMaterial().getTexture(), ShaderLibrary.mineShader);
		m.unloadVBO(true);		
	}

	

	@Override
	public void unloadResourcesB() {
	}


	@Override
	public void drawA(LODDrawerInterface<E> d) {
		d.drawInstances(m, true);
	}
	@Override
	public boolean isDrawA() {
		return true;
	}
	@Override
	public boolean isDrawB() {
		return false;
	}
	@Override
	public void drawB(LODDrawerInterface d) {
		d.drawSprites(sprite, subSpriteIndex);
	}

	@Override
	public boolean isDeferred() {
		return true;
	}

	@Override
	public Sprite getDefferredSprite() {
		sprite = Controller.getResLoader().getSprite(spriteName);
		assert(sprite != null):spriteName;
		return sprite;
	}
	@Override
	public boolean isBlending() {
		return true;
	}


	@Override
	public boolean isDeferredA() {
		return false;
	}

	@Override
	public boolean isDeferredB() {
		return true;
	}
}
