package api.mod.gui;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;

import java.awt.image.BufferedImage;

/**
 * Created by Jake on 2/19/2021.
 * <insert description here>
 */
public class GUIIcon extends GUITextOverlayTable {
    private BufferedImage icon;
    public GUIIcon(InputState inputState, BufferedImage icon) {
        super(inputState);
        this.icon = icon;
        this.setTextSimple("");
    }

//    public GUIModIcon(InputState var1, String iconURL) {
//        super(var1);
//        this.iconURL = iconURL;
//    }

    Sprite sprite = null;
    @Override
    public void draw() {
        super.draw();
        if(sprite == null) {
            sprite = StarLoaderTexture.newSprite(icon, "starloader_iconurl_" + icon.hashCode());
            icon = null;
            sprite.setPositionCenter(false);
            int size = 50;
            sprite.setPos(1,5,0);
            sprite.setWidth(size);
            sprite.setHeight(size);
            return;
        }
        sprite.draw();
    }
}