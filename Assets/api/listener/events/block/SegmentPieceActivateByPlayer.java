package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.player.PlayerState;

public class SegmentPieceActivateByPlayer extends Event {


    private final SegmentPiece segmentPiece;
    private final PlayerState player;
    private PlayerInteractionControlManager controlManager;

    public SegmentPiece getSegmentPiece() {
        return segmentPiece;
    }

    public PlayerState getPlayer() {
        return player;
    }

    public SegmentPieceActivateByPlayer(SegmentPiece segmentPiece, PlayerState player, PlayerInteractionControlManager controlManager) {
        this.segmentPiece = segmentPiece;
        this.player = player;
        this.controlManager = controlManager;
    }

    public PlayerInteractionControlManager getControlManager() {
        return controlManager;
    }
}