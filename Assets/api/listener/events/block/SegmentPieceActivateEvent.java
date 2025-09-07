package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.activation.ActivationElementManager;
import org.schema.game.common.data.SegmentPiece;

public class SegmentPieceActivateEvent extends Event {

    private final ActivationElementManager manager;
    private final SegmentPiece segmentPiece;
    private final ActivationCollectionManager activationCollectionManager;

    public SegmentPieceActivateEvent(ActivationElementManager manager, SegmentPiece segmentPiece, ActivationCollectionManager activationCollectionManager) {
        this.manager = manager;
        this.segmentPiece = segmentPiece;
        this.activationCollectionManager = activationCollectionManager;
    }

    public ActivationElementManager getManager() {
        return manager;
    }

    public SegmentPiece getSegmentPiece() {
        return segmentPiece;
    }

    public ActivationCollectionManager getActivationCollectionManager() {
        return activationCollectionManager;
    }
}
