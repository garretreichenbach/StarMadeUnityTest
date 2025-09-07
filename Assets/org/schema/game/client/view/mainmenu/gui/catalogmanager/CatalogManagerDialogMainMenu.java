package org.schema.game.client.view.mainmenu.gui.catalogmanager;

import org.schema.game.client.controller.GameMainMenuController;
import org.schema.game.client.view.mainmenu.MainMenuInputDialog;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public class CatalogManagerDialogMainMenu extends MainMenuInputDialog {

    private final CatalogManagerPanel managerPanel;

    public CatalogManagerDialogMainMenu(GameMainMenuController state) {
        super(state);
        GameServerState.initPaths(false, 0);
        managerPanel = new CatalogManagerPanel(state, this);
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
