package org.schema.game.common.controller.elements;

import org.schema.game.common.data.SegmentPiece;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public interface BlockActivationListenerInterface {
	public int onActivate(SegmentPiece piece, boolean oldActive, boolean active);

	public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation);

	public boolean isHandlingActivationForType(short type);
}
