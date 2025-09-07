package api.listener.events.player;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.game.common.data.player.PlayerState;

/**
 * Called when a player requests a blueprint from the catalog.
 */
@ServerEvent
public class PlayerRequestMetaBlueprintEvent extends Event {
    private PlayerState state;
    private boolean allowed;
    private boolean isShopInfiniteSupply;
    private String blueprintName;

    public PlayerRequestMetaBlueprintEvent(PlayerState state, boolean allowed, boolean isShopInfiniteSupply, String blueprintName) {
        this.state = state;
        this.allowed = allowed;
        this.isShopInfiniteSupply = isShopInfiniteSupply;
        this.blueprintName = blueprintName;
    }

    public PlayerState getState() {
        return state;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isShopInfiniteSupply() {
        return isShopInfiniteSupply;
    }

    public void setShopInfiniteSupply(boolean shopInfiniteSupply) {
        isShopInfiniteSupply = shopInfiniteSupply;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }
}
