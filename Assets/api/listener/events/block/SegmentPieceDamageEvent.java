package api.listener.events.block;

import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;

@Deprecated
public class SegmentPieceDamageEvent extends Event {
    private final SegmentController controller;
    private final long pos;
    private final short type;
    private int damage;
    private final DamageDealerType damageType;
    private final Damager from;

    public SegmentPieceDamageEvent(SegmentController controller, long pos, short type, int damage, DamageDealerType damageType, Damager from) {

        this.controller = controller;
        this.pos = pos;
        this.type = type;
        this.damage = damage;
        this.damageType = damageType;
        this.from = from;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public SegmentController getController() {
        return controller;
    }

    public long getPos() {
        return pos;
    }

    public short getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public DamageDealerType getDamageType() {
        return damageType;
    }

    public Damager getFrom() {
        return from;
    }
}
