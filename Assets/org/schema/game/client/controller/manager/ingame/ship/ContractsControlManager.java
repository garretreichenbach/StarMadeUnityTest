package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.sound.controller.AudioController;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class ContractsControlManager extends AbstractControlManager {
    public ContractsControlManager(GameClientState state) {
        super(state);
    }

    @Override
    public void onSwitch(boolean active) {
        if(active) {
            AudioController.fireAudioEventID(180);
            notifyObservers();
        } else AudioController.fireAudioEventID(179);
        getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
        getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
        super.onSwitch(active);
    }

    @Override
    public void update(Timer timer) {
        CameraMouseState.setGrabbed(false);
        super.update(timer);
    }
}