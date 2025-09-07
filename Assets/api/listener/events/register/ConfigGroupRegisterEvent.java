package api.listener.events.register;

import api.listener.events.Event;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;

/**
 * Called when a ConfigGroup is registered in the main ConfigPool.
 * Could be on server for recieved from server by client
 */
public class ConfigGroupRegisterEvent extends Event {


    private final ConfigPool configPool;
    private final ConfigGroup configGroup;

    public ConfigGroupRegisterEvent(ConfigPool configPool, ConfigGroup configGroup) {

        this.configPool = configPool;
        this.configGroup = configGroup;
    }

    public ConfigPool getConfigPool() {
        return configPool;
    }

    public ConfigGroup getConfigGroup() {
        return configGroup;
    }
}
