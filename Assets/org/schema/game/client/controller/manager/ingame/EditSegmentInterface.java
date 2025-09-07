package org.schema.game.client.controller.manager.ingame;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.SegmentPiece;

public interface EditSegmentInterface {
	public Vector3i getCore();

	public SegmentPiece getEntered();

	public EditableSendableSegmentController getSegmentController();
}
