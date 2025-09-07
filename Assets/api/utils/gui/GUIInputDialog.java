package api.utils.gui;

import api.common.GameClient;
import org.schema.game.client.controller.PlayerInput;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.KeyEventInterface;

/**
 * GUIInputDialog
 * <Description>
 *
 * @author TheDerpGamer
 * @since 04/14/2021
 */
public abstract class GUIInputDialog extends PlayerInput {

    private GUIInputDialogPanel inputPanel;

    public GUIInputDialog() {
        super(GameClient.getClientState());
        (inputPanel = createPanel()).onInit();
    }

    public abstract GUIInputDialogPanel createPanel();

    @Override
    public GUIInputDialogPanel getInputPanel() {
        return inputPanel;
    }

    @Override
    public void handleKeyEvent(KeyEventInterface keyEvent) {
        //Use this to handle stuff like exiting the dialog when ESC key is pressed.
        super.handleKeyEvent(keyEvent);
    }

    @Override
    public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
        //Use this to handle element activation and interaction
    }

    @Override
    public void onDeactivate() {
        inputPanel.cleanUp();
    }
}
