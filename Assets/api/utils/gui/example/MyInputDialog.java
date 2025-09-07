package api.utils.gui.example;

import api.utils.gui.GUIInputDialog;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

/**
 * MyInputDialog
 *
 * Test code - To be removed
 *
 * @author TheDerpGamer
 * @since 04/14/2021
 */
public class MyInputDialog extends GUIInputDialog {

    public MyInputDialog() {
        super();
    }

    @Override
    public MyInputDialogPanel createPanel() {
        return new MyInputDialogPanel(getState(), this);
    }

    @Override
    public void callback(GUIElement callingElement, MouseEvent mouseEvent) {
        if(!isOccluded() && mouseEvent.pressedLeftMouse()) {
            //User pointer is typically the displayed label / text on the button
            switch((String) callingElement.getUserPointer()) {
                case "X":
                case "CANCEL":
                    //Deactivate dialog
                    deactivate();
                    break;
                case "OK":
                    //Do stuff
                    break;
            }
        }
    }
}
