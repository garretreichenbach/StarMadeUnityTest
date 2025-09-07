package api.listener.events.register;

import api.listener.events.Event;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;

public class RegisterConfigGroupsEvent extends Event {
    private final ObjectArrayFIFOQueue<ConfigGroup> queue = new ObjectArrayFIFOQueue<>();
    private ConfigPool configPool;

    public RegisterConfigGroupsEvent(ConfigPool configPool) {
        this.configPool = configPool;
    }

    public ConfigPool getConfigPool() {
        return configPool;
    }

    public ObjectArrayFIFOQueue<ConfigGroup> getModConfigGroups() {
        return queue;
    }
}
