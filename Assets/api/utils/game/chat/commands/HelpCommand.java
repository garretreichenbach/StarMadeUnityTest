package api.utils.game.chat.commands;

import api.ModPlayground;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;

public class HelpCommand implements CommandInterface {

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String[] getAliases() {
        return new String[] {
                "/?",
                "/h",
                "/help"
        };
    }

    @Override
    public String getDescription() {
        return "Displays description and usages for a specified command\n" +
                "- /%COMMAND% <command|alias> : Searches for the specified command or command alias and displays it's description and usages.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        if(args != null && args.length > 0) {
            StringBuilder builder = new StringBuilder();
            for(String s : args) builder.append(s).append(" ");
            String commandName = builder.toString().trim();
            CommandInterface command = StarLoader.getCommand(commandName);
            if(command != null) {
                if(command.isAdminOnly() && !sender.isAdmin()) PlayerUtils.sendMessage(sender, "[ERROR]: You do not have permission to view command " + commandName);
                else PlayerUtils.sendMessage(sender, commandName + ":\n" + command.getDescription().replace("%COMMAND%", commandName));
            } else PlayerUtils.sendMessage(sender, "[ERROR]: " + commandName + " is not a valid command");
        } else return false;
        return true;
    }

    @Override
    public void serverAction(@Nullable PlayerState sender, String[] args) {

    }

    @Override
    public StarMod getMod() {
        return StarLoader.getModFromMainClass(ModPlayground.class);
    }
}
