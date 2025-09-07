package api.utils.game.chat.commands;

import api.ModPlayground;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandGroup;
import api.utils.game.chat.CommandInterface;
import org.apache.commons.lang3.math.NumberUtils;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Command group for reading and writing to mod configs.
 *
 * @author TheDerpGamer
 */
public class ConfigCommand implements CommandGroup {

    private FileConfiguration currentConfig;

    @Override
    public CommandInterface[] getCommands() {
        return new CommandInterface[] {
                new ConfigEditCommand(),
                new ConfigSaveCommand(),
                new ConfigGetCommand(),
                new ConfigListCommand(),
                new ConfigSetCommand(),
                new ConfigReloadCommand()
        };
    }

    private boolean setConfigValue(String field, String value) {
        try {
            String fieldType = getType(currentConfig.getString(field));
            String inputType = getType(value);
            if(fieldType.equals(inputType)) {
                currentConfig.set(field, value);
                currentConfig.saveConfig();
                return true;
            } else return false;
        } catch(Exception exception) {
            return false;
        }
    }

    private String getType(String value) {
        value = value.toLowerCase(Locale.ENGLISH).trim();
        if(value.equals("true") || value.equals("false")) return "BOOLEAN";
        else if(value.contains(",")) return "LIST";
        else if(NumberUtils.isNumber(value)) {
            if(value.contains(".")) return "DOUBLE";
            else return "INTEGER";
        } else return "STRING";
    }

    private FileConfiguration getConfig(String modName) {
        return getConfig(modName, "config");
    }

    private FileConfiguration getConfig(String modName, String configName) {
        if(StarLoader.getModFromName(modName) != null) {
            if(StarLoader.getModFromName(modName).getRealMod().getConfig(configName) != null) {
                return StarLoader.getModFromName(modName).getRealMod().getConfig(configName);
            }
        }
        return null;
    }

    private boolean configExists(String modName) {
        return configExists(modName, "config");
    }

    private boolean configExists(String modName, String configName) {
        return getConfig(modName, configName) != null;
    }

    private void sendNoConfigMessage(PlayerState playerState) {
        PlayerUtils.sendMessage(playerState, "You need to select a config for editing first! Use \"/config edit <mod_name> [config_name]\" to edit a config file.");
    }

    private void reloadAllConfigs() {
        for(ModSkeleton modSkeleton : StarLoader.starMods) {
            if(modSkeleton.getRealMod().getConfig("config") != null) modSkeleton.getRealMod().getConfig("config").reloadConfig();
        }
    }

    public class ConfigEditCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_edit";
        }

        @Override
        public String[] getAliases() {
            return new String[] {
                    "edit_config"
            };
        }

        @Override
        public String getDescription() {
            return "Opens a mod config file for editing.\n" +
                   "- /%COMMAND% <mod_name> [config_name] : Opens a config file from the specified mod for editing. If left unspecified, 'config_name' will default to 'config'";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(args.length == 1) {
                if(configExists(args[0])) {
                    currentConfig = getConfig(args[0]);
                    PlayerUtils.sendMessage(sender, "Now editing config for mod \"" + args[0] + "\".");
                } else PlayerUtils.sendMessage(sender, "There is no mod by the name of \"" + args[0] + "\".");
            } else if(args.length == 2) {
                if(StarLoader.getModFromName(args[0]) != null) {
                    if(configExists(args[0], args[1])) {
                        currentConfig = getConfig(args[0], args[1]);
                        PlayerUtils.sendMessage(sender, "Now editing config \"" + args[1] + "\" for mod \"" + args[0] + "\".");
                    } else PlayerUtils.sendMessage(sender, "There is no config by the name of \"" + args[1] + "\" for mod \"" + args[1] + "\".");
                } else PlayerUtils.sendMessage(sender, "There is no mod by the name of \"" + args[0] + "\".");
            } else return false;
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }

    public class ConfigSaveCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_save";
        }

        @Override
        public String[] getAliases() {
            return new String[] {
                    "save_config"
            };
        }

        @Override
        public String getDescription() {
            return "Saves and closes the currently selected mod config.\n" +
                   "- /%COMMAND% : Saves the currently selected mod config and closes it for editing.";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(currentConfig == null) sendNoConfigMessage(sender);
            else if(args == null || args.length == 0) {
                currentConfig.saveConfig();
                PlayerUtils.sendMessage(sender, "Successfully saved and closed config \"" + currentConfig.getName() + "\".");
                currentConfig = null;
            } else return false;
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }

    public class ConfigGetCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_get";
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public String getDescription() {
            return "Fetches a value from the currently selected mod config.\n" +
                   "- /%COMMAND% <field_name> : Returns the currently selected value of the specified field in the currently selected mod config.";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(currentConfig == null) sendNoConfigMessage(sender);
            else if(args.length == 1) {
                if(currentConfig.getKeys().contains(args[0])) PlayerUtils.sendMessage(sender, "The currently selected value of \"" + args[0] + "\" is \"" + currentConfig.getString(args[0]) + "\".");
                else PlayerUtils.sendMessage(sender, "There is no field by the name \"" + args[0] + "\" in server config.");
            } else return false;
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }

    public class ConfigListCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_list";
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public String getDescription() {
            return "Lists all values from the currently selected mod config.\n" +
                   "- /%COMMAND% : Returns a list of values from the currently selected mod config.";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(currentConfig == null) sendNoConfigMessage(sender);
            else if(args.length == 1) {
                StringBuilder builder = new StringBuilder();
                if(!currentConfig.getName().toLowerCase(Locale.ENGLISH).equals("config")) builder.append(currentConfig.getMod().getName()).append(" - ").append(currentConfig.getName()).append(":\n");
                else builder.append(currentConfig.getMod().getName()).append(" - Configuration:\n");
                for(String field : currentConfig.getKeys()) builder.append(field).append(": ").append(currentConfig.getString(field)).append("\n");
                PlayerUtils.sendMessage(sender, builder.toString().trim());
            } else return false;
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }

    public class ConfigSetCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_set";
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public String getDescription() {
            return "Sets a value in the currently selected mod config.\n" +
                   "- /%COMMAND% <field_name> <field_value> : Sets the value of the specified field in the currently selected mod config.";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(currentConfig == null) sendNoConfigMessage(sender);
            else if(args.length == 2) {
                if(currentConfig.getKeys().contains(args[0])) {
                    boolean success = setConfigValue(args[0], args[1]);
                    if(success) {
                        PlayerUtils.sendMessage(sender, "Successfully set field \"" + args[0] + "\" to \"" + args[1] + "\".");
                        currentConfig.reloadConfig();
                    } else PlayerUtils.sendMessage(sender, "The value of \"" + args[0] + "\" must be of type " + getType(currentConfig.getString(args[1])) + ".");
                } else PlayerUtils.sendMessage(sender, "There is no field by the name \"" + args[0] + "\" in server config.");
            } else return false;
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }

    public class ConfigReloadCommand implements CommandInterface {

        @Override
        public String getCommand() {
            return "config_reload";
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public String getDescription() {
            return "Reloads the currently selected mod config, or all if there is no config being edited." +
                   "- /%COMMAND% : Reloads the currently selected mod config. If no config is being edited, reloads all configs.";
        }

        @Override
        public boolean isAdminOnly() {
            return true;
        }

        @Override
        public boolean onCommand(PlayerState sender, String[] args) {
            if(args.length != 0) return false;
            else {
                if(currentConfig == null) {
                    reloadAllConfigs();
                    PlayerUtils.sendMessage(sender, "Successfully reloaded all configs.");
                } else {
                    currentConfig.reloadConfig();
                    PlayerUtils.sendMessage(sender, "Successfully reloaded config \"" + currentConfig.getName() + "\".");
                }
            }
            return true;
        }

        @Override
        public void serverAction(@Nullable PlayerState sender, String[] args) {

        }

        @Override
        public StarMod getMod() {
            return ModPlayground.inst;
        }
    }
}