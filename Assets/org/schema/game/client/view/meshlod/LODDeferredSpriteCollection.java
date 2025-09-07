package org.schema.game.client.view.meshlod;

import org.schema.schine.graphicsengine.forms.Sprite;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LODDeferredSpriteCollection<E extends LODCapable> extends ObjectArrayList<E>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8875572023215012643L;
	public Sprite deferredSprite;
}
