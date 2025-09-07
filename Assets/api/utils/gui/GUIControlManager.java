package api.utils.gui;

import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;

/**
 * GUI control manager class for mods to extend in their own GUI menus.
 *
 * @author TheDerpGamer
 */
public abstract class GUIControlManager extends AbstractControlManager {

    private boolean initialized;
    private GUIMenuPanel menuPanel;

    public GUIControlManager(GameClientState clientState) {
        super(clientState);
        initialized = false;
    }

    public GUIMenuPanel getMenuPanel() {
        return menuPanel;
    }

    @Override
    public void setActive(boolean active) {
        if(!initialized) onInit();
        //getState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
        //getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().suspend(active);
        super.setActive(active);
    }

    @Override
    public void update(Timer timer) {
        CameraMouseState.setGrabbed(false);
        getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().suspend(true);
    }

    @Override
    public void onSwitch(boolean active) {
        if(active) {
            //getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll large");
            //setChanged();
            notifyObservers();
        } //else getState().getController().queueUIAudio("0022_menu_ui - swoosh scroll small");

        try {
            getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getShipExternalFlightController().suspend(active);
            getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getShipControlManager().getSegmentBuildController().suspend(active);
        } catch(NullPointerException e){
            System.err.println("Ignored NullPointerException in GUI logic: ");
            e.printStackTrace();
        }

}

    public void onInit() {
        menuPanel = createMenuPanel();
        initialized = true;
    }

    public void draw() {
        //if(!initialized) onInit();
        menuPanel.draw();
    }

    public void cleanUp() {
        if(initialized) menuPanel.cleanUp();
    }

    public abstract GUIMenuPanel createMenuPanel();
}