package api.mod.gui;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

/**
 * Main menu for the mod manager
 */
public class ModManagerDialogMainMenu extends MainMenuInputDialog {
    private final ModBrowserPanel managerPanel;

    public ModManagerDialogMainMenu(GameMainMenuController state) {
        super(state);
        managerPanel = new ModBrowserPanel(state, this);
        managerPanel.onInit();
    }

    @Override
    public GUIElement getInputPanel() {
        return managerPanel;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void onDeactivate() {
        managerPanel.cleanUp();
    }

    @Override
    public void update(Timer timer) {
        super.update(timer);
    }

    @Override
    public boolean isInside() {
        return managerPanel.isInside();
    }
}
