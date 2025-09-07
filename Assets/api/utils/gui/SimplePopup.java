package api.utils.gui;

import org.schema.game.client.controller.PlayerOkCancelInput;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.input.InputState;

/**
 * Displays a confirm dialog containing text
 */
public class SimplePopup extends PlayerOkCancelInput {
    //"CONFIRM", getState(), 300, 150, Lng.str("Error"), "A Blueprint with the name " + bpName + " already exists in your catalog!"
    public SimplePopup( InputState inputState, String text) {
        super("SimplePopup", inputState, "Simple popup", text);
        getInputPanel().onInit();
        getInputPanel().setCancelButton(false);
        getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
        getInputPanel().background.setWidth((float)(GLFrame.getWidth() - 435));
        getInputPanel().background.setHeight((float)(GLFrame.getHeight() - 70));
        activate();
    }

    public SimplePopup(InputState inputState, String title, String text) {
        super("SimplePopup", inputState, title, text);
        getInputPanel().onInit();
        getInputPanel().setCancelButton(false);
        getInputPanel().background.setPos(470.0F, 35.0F, 0.0F);
        getInputPanel().background.setWidth((float)(GLFrame.getWidth() - 435));
        getInputPanel().background.setHeight((float)(GLFrame.getHeight() - 70));
        activate();
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void pressedOK() {
        deactivate();
    }
}
