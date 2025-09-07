package api.utils.simpleconfig;

import api.mod.config.FileConfiguration;

public class SimpleConfigBool extends SimpleConfigEntry<Boolean>{

    public SimpleConfigBool(SimpleConfigContainer container, String name, Boolean value) {
        super(container, name, value, null);
    }
    public SimpleConfigBool(SimpleConfigContainer container, String name, Boolean value, String comment) {
        super(container, name, value, comment);
    }

    @Override
    protected void readValueInternal(FileConfiguration config) {
        value = config.getBoolean(name);
    }
}
