package org.schema.game.client.view.character;

import org.schema.game.common.data.player.AbstractCharacterInterface;
import org.schema.schine.graphicsengine.core.Drawable;
import org.schema.schine.graphicsengine.core.Timer;

public interface DrawableCharacterInterface<E extends AbstractCharacterInterface> extends Drawable {

	void onRemove();

	void update(Timer timer);

	int getId();

	public boolean isInClientRange();
	
	public boolean isInFrustum();

	void setShadowMode(boolean shadow);

	E getEntity();

}
