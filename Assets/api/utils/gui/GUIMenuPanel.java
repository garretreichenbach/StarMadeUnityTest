package api.utils.gui;

import api.common.GameClient;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIMainWindow;
import org.schema.schine.input.InputState;

/**
 * GUIMenuPanel.java
 * GUI menu panel class for mods to extend in their own GUI menus.
 *
 * @author TheDerpGamer
 * @since 03/18/2021
 */
public abstract class GUIMenuPanel extends GUIElement implements GUIActiveInterface {

    private boolean initialized;
    public GUIMainWindow guiWindow;

    public GUIMenuPanel(InputState inputState, String windowName, int width, int height) {
        super(inputState);
        this.initialized = false;
        this.guiWindow = new GUIMainWindow(getState(), width, height, windowName.replace(" ", "_"));
        setName(windowName.replace(" ", "_"));
    }

    @Override
    public void onInit() {
        guiWindow.onInit();
        guiWindow.setCloseCallback(new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent event) {
                if(event.pressedLeftMouse()) {
                    //GameClient.getClientState().getController().queueUIAudio("0022_menu_ui - back");
                    GameClient.getClientState().getWorldDrawer().getGuiDrawer().getPlayerPanel().deactivateAll();
                }
            }

            @Override
            public boolean isOccluded() {
                return !getState().getController().getPlayerInputs().isEmpty();
            }
        });
        recreateTabs();
        initialized = true;
    }

    @Override
    public void draw() {
        if(!initialized) onInit();
        guiWindow.draw();
    }

    @Override
    public void cleanUp() {
        if(initialized) guiWindow.cleanUp();
    }

    @Override
    public float getWidth() {
        return guiWindow.getWidth();
    }

    @Override
    public float getHeight() {
        return guiWindow.getHeight();
    }

    public abstract void recreateTabs();

    public void recreateTabsSafe() {
        if (initialized)
            recreateTabs();
    }

    public boolean getInitialized() {
        return initialized;
    }
}
