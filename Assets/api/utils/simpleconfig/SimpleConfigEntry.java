package api.utils.simpleconfig;

import api.mod.config.FileConfiguration;

import javax.validation.constraints.DecimalMax;


/** lupoCani on 2022-06-16
 *
 * Holds one config value as an explicit java variable for quick reading.
 * @param <valType> the type of value, can be Boolean, Integer, Float or String.
 */
public abstract class SimpleConfigEntry<valType extends java.io.Serializable> implements java.io.Serializable{
    final private SimpleConfigContainer container;  //The config container the entry belongs to.
    final public String name;                       //The name (path) of the config entry.
    final public String typeName;                   //Human readable type na,e
    final public valType defaultValue;              //The default value of the entry.

    @Deprecated
    public valType value;                           //The current value of the entry. (Will be made private.)
    public boolean isDefault;   //True if the value comes from the 'default value' constructor parameter.
    protected String commentStr;

    /** lupoCani on 2022-06-16
     *
     * Creates a config entry. Also adds the entry to the entries list held by the config container.
     * @param container the config container the entry belongs to.
     * @param name the name (path) of the config entry.
     * @param defaultValue the default value of the entry, if the config cannot be read or the entry is missing.
     */
    protected SimpleConfigEntry(SimpleConfigContainer container, String name, valType defaultValue, String comment) {
        if (defaultValue == null) throw new NullPointerException("Config entry value cannot be null");

        isDefault = true;
        this.container = container;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.name = name;

        container.entries.add(this);
        this.typeName = value.getClass().getSimpleName();
        commentStr = comment;
    }

    public valType getValue() {
        return value;
    }
    public void setValue(valType value) {
        this.value = value;
    }
    
    public void readValue() {
        try {
            if (!container.getConfig().getKeys().contains(name))
                return;

            readValueInternal(container.getConfig());
            isDefault = validateValue();
        }
        catch (IllegalArgumentException e) {
            container.println("Wrong config value type for " + name);
        }
        catch (NullPointerException e) {
            container.println("Could not find config entry for " + name);
        }
        catch (Exception e) {
            container.println("Unkown error trying to read config entry for " + name);
        }
    }

    public void resetValue() {
        this.value = this.defaultValue;
        this.isDefault = true;
    }

    public boolean equalsDefault() {
        return defaultValue.equals(value);
    }

    protected abstract void readValueInternal(FileConfiguration config);

    protected boolean validateValue() {
        return false;
    }

    public String getComment() {
        if (this.commentStr != null)
            return typeName+" "+name+" = "+defaultValue+": " + this.commentStr;
        else
            return null;
    }

    public void setComment(String comment) {
        this.commentStr = comment;
    }

    public void writeValue() {
        try {
            container.getConfig().set(name, value);
            container.getConfig().setComment(name, getComment());
            isDefault = false;
        }
        catch (NullPointerException e) {
            container.println("Null pointer exception trying to write config entry for " + name);
        }
    }

    @Override
    public String toString() {
        return "'" + name + "': '" + value + "' (isDefault=" + isDefault + ")";
    }
}


