package api.utils.simpleconfig;

import api.mod.config.FileConfiguration;

public class SimpleConfigDouble extends SimpleConfigEntry<Double> {

    public SimpleConfigDouble(SimpleConfigContainer container, String name, Double value) {
        super(container, name, value, null);
    }

    public SimpleConfigDouble(SimpleConfigContainer container, String name, Double value, String comment) {
        super(container, name, value, comment);
    }

    @Override
    protected void readValueInternal(FileConfiguration config) {
        value = config.getDouble(name);
    }
}
