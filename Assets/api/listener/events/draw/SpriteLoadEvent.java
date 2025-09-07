package api.listener.events.draw;

import api.listener.events.Event;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;

/**
 * Created by Jake on 9/28/2020.
 * <insert description here>
 */
public class SpriteLoadEvent extends Event {
    private final String path;
    private final String name;
    private final Texture texture;
    private final Sprite sprite;

    public SpriteLoadEvent(String path, String name, Texture texture, Sprite sprite) {

        this.path = path;
        this.name = name;
        this.texture = texture;
        this.sprite = sprite;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Texture getTexture() {
        return texture;
    }

    public Sprite getSprite() {
        return sprite;
    }
}
