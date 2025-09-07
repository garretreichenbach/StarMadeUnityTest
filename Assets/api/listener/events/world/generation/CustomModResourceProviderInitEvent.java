package api.listener.events.world.generation;

import api.listener.events.Event;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;

import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.*;

public class CustomModResourceProviderInitEvent extends Event {
    private final PassiveResourceProvider pv;
    private ResourceProviderTypedef providerType;
    public CustomModResourceProviderInitEvent(PassiveResourceProvider passiveResourceProvider) {
        pv = passiveResourceProvider;
    }

    public PassiveResourceProvider getProvider() {
        return pv;
    }

    public void setProviderType(ResourceProviderTypedef val){
        providerType = val;
    }

    public ResourceProviderTypedef getProviderType() {
        return providerType;
    }
}
