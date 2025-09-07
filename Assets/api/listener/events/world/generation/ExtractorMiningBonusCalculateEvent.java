package api.listener.events.world.generation;

import api.listener.events.Event;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;

//example utility for mods: creating bigger bonuses for certain areas of the galaxy, increasing a faction mining bonuses via RPG-style skill tree, etc.
public class ExtractorMiningBonusCalculateEvent extends Event {
    private final String entityUID;
    private final int factionId;
    private final Vector3i sector;
    private final short resType;
    private final ShortOpenHashSet resourceSupplies;
    private int bonus;

    public ExtractorMiningBonusCalculateEvent(String entityUID, int factionId, Vector3i sector, short sourceType, ShortOpenHashSet resourceSupplies, int bonus) {

        this.entityUID = entityUID;
        this.factionId = factionId;
        this.sector = sector;
        this.resType = sourceType;
        this.resourceSupplies = resourceSupplies;
        this.bonus = bonus;
    }

    public String getEntityUID() {
        return entityUID;
    }

    public int getFactionId() {
        return factionId;
    }

    public Vector3i getSector() {
        return sector;
    }

    public short getSourceType() {
        return resType;
    }

    public ShortOpenHashSet getResourceSupplies() {
        return resourceSupplies;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }
}
