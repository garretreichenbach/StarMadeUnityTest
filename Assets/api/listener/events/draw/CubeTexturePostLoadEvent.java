package api.listener.events.draw;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * Created by Jake on 10/4/2020.
 * <insert description here>
 */
public class CubeTexturePostLoadEvent extends Event {
    private final Texture[] texArray;
    private final String pack;
    private final int resolution;
    private final String customTextureURL;

    public CubeTexturePostLoadEvent(Texture[] texArray, String pack, int resolution, String customTextureURL) {
        this.texArray = texArray;
        this.pack = pack;
        this.resolution = resolution;
        this.customTextureURL = customTextureURL;
    }

    public Texture[] getTexArray() {
        return texArray;
    }

    public String getPack() {
        return pack;
    }

    public int getResolution() {
        return resolution;
    }

    public String getCustomTextureURL() {
        return customTextureURL;
    }
}
