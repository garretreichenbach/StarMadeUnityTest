package api.mod.gui;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUITextOverlayTable;
import org.schema.schine.input.InputState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Jake on 12/15/2020.
 * <insert description here>
 */
public class GUIModIcon extends GUITextOverlayTable {
    private final String iconURL;
    private boolean draw = false;
    public GUIModIcon(InputState inputState, String iconUrl) {
        super(inputState);
        this.iconURL = iconUrl;
        this.setTextSimple("");
        if(iconURL != null){
            draw = true;
        }
    }

//    public GUIModIcon(InputState var1, String iconURL) {
//        super(var1);
//        this.iconURL = iconURL;
//    }

    Sprite sprite = null;
    float r = 0;
    @Override
    public void draw() {
        super.draw();
        if(!draw) {
            return;
        }
        if(sprite == null) {
            fromURL(iconURL);
            Sprite sprite = map.get(iconURL);
            if (sprite != null) {
                this.sprite = sprite;
            }

            for (Runnable runnable : runQueue) {
                runnable.run();
            }
            runQueue.clear();

        } else {
//            sprite.setRot(0,0,r+=0.35F);
//            sprite.setInitionPos(new Vector3f(50,50,0));
            sprite.draw();
        }
    }

    private final static ConcurrentLinkedQueue<String> downloadingImages = new ConcurrentLinkedQueue<>();
    private final static ConcurrentHashMap<String, Sprite> map = new ConcurrentHashMap<>();
    private static ConcurrentLinkedQueue<Runnable> runQueue = new ConcurrentLinkedQueue<>();
    private static void fromURL(final String u){
        if(!downloadingImages.contains(u)) {
            downloadingImages.add(u);
            new Thread(() -> {
                try {
                    URL url = new URL(u);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "NING/1.0");
                    InputStream stream = urlConnection.getInputStream();
                    BufferedImage image = ImageIO.read(stream);
                    runQueue.add(() -> {
                        try {
                            downloadingImages.remove(u);
                            Sprite value = StarLoaderTexture.newSprite(image, "starloader_iconurl_" + u);
                            value.setPositionCenter(false);
                            int size = 80;
                            value.setPos(1, 5, 0);
                            value.setWidth(size);
                            value.setHeight(size);
                            map.put(u, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
