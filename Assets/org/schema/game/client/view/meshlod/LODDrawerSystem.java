package org.schema.game.client.view.meshlod;


import java.util.Collection;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.Timer;


public abstract class LODDrawerSystem<E extends LODCapable> implements LODDrawerSystemInterface<E>{

	
	private LODDrawerCollection<E>[] mines;
	private LODMeshSystem<E> meshSystem;
	protected final Vector3f camPos = new Vector3f();
	
	
	public abstract Collection<E> getEntries();
	
	public void create(LODMeshSystem<E> meshSystem) {
		mines = new LODDrawerCollection[meshSystem.getLevelCount()];
		for(int i = 0; i < mines.length; i++) {
			mines[i] = new LODDrawerCollection<E>();
		}
		
		this.meshSystem = meshSystem;
	}
	public void cleanUp() {
		meshSystem.cleanUp();
	}
	@Override
	public void drawLevel(int lodIndex, LODMesh<E>  mesh, LODDeferredSpriteCollection<E> defferedSprites) {
		if(mines[lodIndex].isEmpty()) {
			return;
		}
		mesh.beforeDraw(mines[lodIndex]);
//		if(mines[lodIndex].drawable.size() > 0) {
//			System.err.println("DRAWING OF "+lodIndex+"; "+mines[lodIndex].drawable.size());
//		}
		if(mesh.isDeferredA()) {
			for(E e : mines[lodIndex].drawable) {
				if(mesh.isBlending()) {
					e.getColor().w = Math.min(e.getMaxAlpha(), 1f - e.getBlending());
				}else {
					e.getColor().w = e.getMaxAlpha();
				}
				defferedSprites.add(e);
			}
			defferedSprites.deferredSprite = mesh.getDefferredSprite();
		}
		if(mesh.isDeferredB()) {
			for(E e : mines[lodIndex].drawable) {
				if( mesh.isBlending()) {
					e.getColor().w = Math.min(e.getMaxAlpha(), e.getBlending());
				}else {
					e.getColor().w = e.getMaxAlpha();
				}
				defferedSprites.add(e);
			}
			defferedSprites.deferredSprite = mesh.getDefferredSprite();
		}
		if(mesh.isDrawA()) {
			mesh.loadResourcesA();
			mesh.drawA(mines[lodIndex]);
			mesh.unloadResourcesA();
		}
		if(mesh.isDrawB()) {
			mesh.loadResourcesB();
			mesh.drawB(mines[lodIndex]);
			mesh.unloadResourcesB();
		}
		
		
		mesh.afterDraw(mines[lodIndex]);
	}


	@Override
	public void update(int lodIndex, Timer timer, LODMesh<E> mesh) {
		this.camPos.set(Controller.getCamera().getPos());
		mesh.update(timer, mines[lodIndex], this.camPos);
	}

	
	public void update(Timer timer) {
		meshSystem.update(timer, this);
		this.camPos.set(Controller.getCamera().getPos());
		
		Collection<E> entries = getEntries();
		
		for(E c : entries) {
			categorize(c);
		}
	}
	private void categorize(E c) {
		int oldLOD = c.updateCurrentLevel(camPos, meshSystem);
		
		
		if(oldLOD != c.getCurrentLODLevel()) {
			if(oldLOD >= 0) {
				mines[oldLOD].removeEntry(c);
			}
			if(c.isAlive()) {
				mines[c.getCurrentLODLevel()].addEntry(c);
			}else {
				if(c.getCurrentLODLevel() >= 0) {
					mines[c.getCurrentLODLevel()].removeEntry(c);
				}
			}
		}
	}
	protected void onRemoved(E removed) {
		if(removed != null) {
			removed.kill();
			for(int i = 0; i < mines.length; i++) {
				mines[i].drawable.remove(removed);
			}
		}
		
	}
	public void draw(LODDeferredSpriteCollection<E> defferedSprites) {
		meshSystem.draw(this, defferedSprites);
	}
}