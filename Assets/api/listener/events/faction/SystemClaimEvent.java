package api.listener.events.faction;

import api.common.GameServer;
import api.listener.events.Event;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionSystemOwnerChange;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.StellarSystem;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by Jake on 10/18/2020.
 * <insert description here>
 */
public class SystemClaimEvent extends Event {

    private final StellarSystem system;
    private final FactionSystemOwnerChange ownershipChange;
    private Sector sector;
    private final Faction faction;

    public SystemClaimEvent(StellarSystem system, FactionSystemOwnerChange ownershipChange, Sector sector, @Nullable Faction faction) {

        this.system = system;
        this.ownershipChange = ownershipChange;
        this.sector = sector;
        this.faction = faction;
    }
    public SystemClaimEvent(StellarSystem system, FactionSystemOwnerChange ownershipChange, Vector3i sectorPos, Faction faction) {

        this.system = system;
        this.ownershipChange = ownershipChange;
        try {
            this.sector = GameServer.getServerState().getUniverse().getSector(sectorPos);
        } catch (IOException e) {
            this.sector = null;
            e.printStackTrace();
        }
        this.faction = faction;
    }

    public StellarSystem getSystem() {
        return system;
    }

    public FactionSystemOwnerChange getOwnershipChange() {
        return ownershipChange;
    }

    public Sector getSector() {
        return sector;
    }

    @Nullable
    public Faction getFaction() {
        return faction;
    }
}
