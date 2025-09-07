package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.game.common.data.world.SectorGenerationInterface;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;

public class SystemPreGenerationEvent extends Event {
    private final VoidSystem system;
    private final Galaxy galaxy;
    private SectorGenerationInterface generator;
    private boolean var4; //TODO: wtf is this?

    public SystemPreGenerationEvent(VoidSystem var1, Galaxy var2, SectorGenerationInterface var3, boolean var4) {
        system = var1;
        galaxy = var2;
        generator = var3;
        this.var4 = var4;
    }

    public VoidSystem getSystem() {
        return system;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public SectorGenerationInterface getGenerator() {
        return generator;
    }

    public void setGenerator(SectorGenerationInterface generator) {
        this.generator = generator;
    }

    public boolean getVar4() {
        return var4;
    }
}
