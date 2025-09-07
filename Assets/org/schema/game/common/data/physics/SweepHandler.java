package org.schema.game.common.data.physics;

import org.schema.schine.graphicsengine.forms.BoundingBox;

public interface SweepHandler {

	void separation(Pair<BoundingBox> gp);

	void overlap(Pair<BoundingBox> p);

}
