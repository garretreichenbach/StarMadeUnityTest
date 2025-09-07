package api.listener.events.player;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;

@ServerEvent
public class PlayerJoinFactionEvent extends Event {

    private final Faction previousFaction;
    private final Faction newFaction;
    private final PlayerState player;

    public PlayerJoinFactionEvent(Faction previousFaction, Faction newFaction, PlayerState player) {
        this.previousFaction = previousFaction;
        this.newFaction = newFaction;
        this.player = player;
    }

    public Faction getPreviousFaction() {
        return previousFaction;
    }

    public Faction getNewFaction() {
        return newFaction;
    }

    public PlayerState getPlayer() {
        return player;
    }
}
