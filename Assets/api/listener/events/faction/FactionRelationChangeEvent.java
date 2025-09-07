package api.listener.events.faction;

import api.listener.events.Event;
import api.listener.type.ServerEvent;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation;

@ServerEvent
public class FactionRelationChangeEvent extends Event {

    private final Faction from;
    private final Faction to;
    private final FactionRelation.RType oldRelation;
    private final FactionRelation.RType newRelation;

    public FactionRelationChangeEvent(Faction from, Faction to, FactionRelation.RType oldRelation, FactionRelation.RType newRelation) {
        this.from = from;
        this.to = to;
        this.oldRelation = oldRelation;
        this.newRelation = newRelation;
    }

    public Faction getFrom() {
        return from;
    }

    public Faction getTo() {
        return to;
    }

    public FactionRelation.RType getOldRelation() {
        return oldRelation;
    }

    public FactionRelation.RType getNewRelation() {
        return newRelation;
    }
}