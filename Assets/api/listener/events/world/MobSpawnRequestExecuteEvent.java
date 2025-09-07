package api.listener.events.world;

import api.listener.events.Event;
import com.bulletphysics.linearmath.Transform;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.controller.BluePrintController;

public class MobSpawnRequestExecuteEvent extends Event {

    private final Vector3i sector;
    private int factionId;
    private String blueprintName;
    private final BluePrintController bbc;
    private int count;
    private final Transform transform;

    public MobSpawnRequestExecuteEvent(Vector3i sector, int factionId, String blueprintName, BluePrintController bbc, int count, Transform transform) {

        this.sector = sector;
        this.factionId = factionId;
        this.blueprintName = blueprintName;
        this.bbc = bbc;
        this.count = count;
        this.transform = transform;
    }

    public Vector3i getSector() {
        return new Vector3i(sector);
    }

    public int getFactionId() {
        return factionId;
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public BluePrintController getBbc() {
        return bbc;
    }

    public int getSpawnCount() {
        return count;
    }

    public void setSpawnCount(int count) {
        this.count = count;
    }

    public Transform getTransform() {
        return transform;
    }
}
