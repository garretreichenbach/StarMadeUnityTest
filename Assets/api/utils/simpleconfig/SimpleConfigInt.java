package api.utils.simpleconfig;

import api.mod.config.FileConfiguration;

public class SimpleConfigInt extends SimpleConfigEntry<Integer> {

    public SimpleConfigInt(SimpleConfigContainer container, String name, Integer value) {
        super(container, name, value, null);
    }

    public SimpleConfigInt(SimpleConfigContainer container, String name, Integer value, String comment) {
        super(container, name, value, comment);
    }

    @Override
    protected void readValueInternal(FileConfiguration config) {
        value = config.getInt(name);
    }
}
