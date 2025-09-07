package org.schema.game.client.view.meshlod;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;

public class LODSprite<E extends LODCapable> extends LODMesh<E>{
	public final String spriteName;
	private final int subSpriteIndex;
	private Sprite sprite;
	
	public LODSprite(int lodIndex, float maxDistance, String spriteName, int subSpriteIndex) {
		super(lodIndex, maxDistance);
		this.spriteName = spriteName;
		this.subSpriteIndex = subSpriteIndex;
	}

	@Override
	public void loadResourcesA() {
		sprite = Controller.getResLoader().getSprite(spriteName);
		assert(sprite != null):spriteName;
	}

	@Override
	public void loadResourcesB() {
		
	}

	@Override
	public void unloadResourcesA() {
	}

	@Override
	public void unloadResourcesB() {
	}

	

	@Override
	public void drawA(LODDrawerInterface d) {
		d.drawSprites(sprite, subSpriteIndex);
	}

	@Override
	public void drawB(LODDrawerInterface d) {
		
	}
	@Override
	public boolean isDeferred() {
		return true;
	}

	@Override
	public boolean isDrawA() {
		return false;
	}
	@Override
	public boolean isDrawB() {
		return false;
	}
	@Override
	public Sprite getDefferredSprite() {
		sprite = Controller.getResLoader().getSprite(spriteName);
		assert(sprite != null):spriteName;
		return sprite;
	}
	@Override
	public boolean isBlending() {
		return false;
	}
	@Override
	public boolean isDeferredA() {
		return true;
	}

	@Override
	public boolean isDeferredB() {
		return false;
	}
}
