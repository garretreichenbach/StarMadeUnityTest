package api.utils.gui.example;

import api.utils.gui.GUIInputDialogPanel;
import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.input.InputState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * MyInputDialogPanel
 * Test code - To be removed
 *
 * @author TheDerpGamer
 * @since 04/14/2021
 */
public class MyInputDialogPanel extends GUIInputDialogPanel {

    public MyInputDialogPanel(InputState inputState, GUICallback guiCallback) {
        super(inputState, "stinky_dialog", "uh oh", "stinky", 750, 400, guiCallback);
    }

    @Override
    public void onInit() {
        super.onInit();
        GUIContentPane contentPane = ((GUIDialogWindow) background).getMainContentPane();
        //Get content pane
        contentPane.setTextBoxHeightLast(350);
        //This just ensures the content pane doesn't have a height of 0, the actual height will automatically
        //adjust to fill the area when it's drawn

        GUIAnchor upperContent = contentPane.getContent(0);
        //Get the initial content pane before adding new ones
        GUIAnchor lowerContent = contentPane.addNewTextBox(0, 150).getContent();
        //Add a new content pane at the bottom
        contentPane.addDivider(300);
        final GUIAnchor rightContent = contentPane.getContent(1, 0);
        //Add a divider to the right of both panes

        GUITextOverlay upperOverlay = new GUITextOverlay(getState());
        upperOverlay.autoWrapOn = upperContent;
        //Have the text overlay automatically wrap text to fit the content area
        upperOverlay.onInit();
        //Initialize overlay
        upperOverlay.setTextSimple("ha ha");
        upperContent.attach(upperOverlay);

        GUITextOverlay lowerOverlay = new GUITextOverlay(getState());
        lowerOverlay.autoWrapOn = lowerContent;
        lowerOverlay.onInit();
        lowerOverlay.setTextSimple("funny");
        lowerContent.attach(lowerOverlay);

        StarLoaderTexture.runOnGraphicsThread(() -> {
            try {
                URLConnection connection = (new URL("https://i.imgur.com/EhJgpif.png")).openConnection();
                connection.setRequestProperty("User-Agent", "NING/1.0");
                BufferedImage image = ImageIO.read(connection.getInputStream());
                Sprite sprite = StarLoaderTexture.newSprite(image, "stinky");
                GUIOverlay rightOverlay = new GUIOverlay(sprite, getState());
                rightOverlay.onInit();
                rightContent.attach(rightOverlay);
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}
