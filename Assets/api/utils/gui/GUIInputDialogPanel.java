package api.utils.gui;

import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.input.InputState;

/**
 * GUIInputDialogPanel
 * <Description>
 *
 * @author TheDerpGamer
 * @since 04/14/2021
 */
public abstract class GUIInputDialogPanel extends GUIInputPanel {

    public GUIInputDialogPanel(InputState inputState, String name, String displayTitle, String displayText, int width, int height, GUICallback guiCallback) {
        super(name.replaceAll(" ", "_"), inputState, width, height, guiCallback, displayTitle, displayText);
        setName(name.replaceAll(" ", "_"));
    }

    public GUIInputDialogPanel(InputState inputState, String name, String displayTitle, int width, int height, GUICallback guiCallback) {
        this(inputState, name, displayTitle, "", width, height, guiCallback);
    }
}
