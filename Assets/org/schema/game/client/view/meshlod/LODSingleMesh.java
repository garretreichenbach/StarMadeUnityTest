package org.schema.game.client.view.meshlod;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

public class LODSingleMesh<E extends LODCapable> extends LODMesh<E> {
	public final String meshA;
	private Mesh m;
	
	public LODSingleMesh(int lodIndex, float maxDistance, String mesh) {
		super(lodIndex, maxDistance);
		this.meshA = mesh;
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
	public boolean isDrawA() {
		return true;
	}
	@Override
	public boolean isDrawB() {
		return false;
	}
	@Override
	public void drawA(LODDrawerInterface<E> d) {
		d.drawInstances(m, true);
	}

	@Override
	public void drawB(LODDrawerInterface<E> d) {
	}

	@Override
	public boolean isDeferred() {
		return false;
	}

	@Override
	public Sprite getDefferredSprite() {
		throw new RuntimeException("Cannot defer from this mesh. It has no sprite");
	}
	@Override
	public boolean isBlending() {
		return false;
	}
	@Override
	public boolean isDeferredA() {
		return false;
	}

	@Override
	public boolean isDeferredB() {
		return false;
	}
}
