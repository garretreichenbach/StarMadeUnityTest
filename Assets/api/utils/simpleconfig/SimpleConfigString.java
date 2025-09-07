package api.utils.simpleconfig;

import api.mod.config.FileConfiguration;

public class SimpleConfigString extends SimpleConfigEntry<String>{
    public SimpleConfigString(SimpleConfigContainer container, String name, String value) {
        super(container, name, value, null);
    }
    public SimpleConfigString(SimpleConfigContainer container, String name, String value, String comment) {
        super(container, name, value, comment);
    }
    @Override
    protected void readValueInternal(FileConfiguration config) {
        value = config.getString(name);
    }
}
