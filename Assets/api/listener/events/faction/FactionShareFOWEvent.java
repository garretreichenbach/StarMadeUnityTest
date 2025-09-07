package api.listener.events.faction;

import api.listener.events.Event;
import org.schema.game.common.data.player.faction.Faction;

public class FactionShareFOWEvent extends Event {

    private Faction from;
    private Faction to;

    public FactionShareFOWEvent(Faction from, Faction to) {
        this.from = from;
        this.to = to;
    }

    public Faction getFrom() {
        return from;
    }

    public Faction getTo() {
        return to;
    }
}
