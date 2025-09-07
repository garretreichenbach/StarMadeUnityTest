package api.utils.game.chat.commands;

import api.ModPlayground;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public class CommandListCommand implements CommandInterface {

    @Override
    public String getCommand() {
        return "commands";
    }

    @Override
    public String[] getAliases() {
        return new String[] {
                "commands",
                "command_list",
                "list_commands"
        };
    }

    @Override
    public String getDescription() {
        return "Lists all available commands.\n" +
                "- /%COMMAND% [search] : If search is specified, filters commands with matching or similar names.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        HashSet<CommandInterface> adminCommands = new HashSet<>();
        HashSet<CommandInterface> playerCommands = new HashSet<>();

        if(args != null && args.length > 0) {
            StringBuilder builder = new StringBuilder();
            for(String arg : args) builder.append(arg).append(" ");
            String argsString = builder.toString().trim();
            for(CommandInterface cmd : StarLoader.getAllCommands()) {
                for(String alias : cmd.getAliases()) {
                    if(alias.toLowerCase(Locale.ROOT).contains(argsString.toLowerCase(Locale.ROOT))) {
                        if(cmd.isAdminOnly()) adminCommands.add(cmd);
                        else playerCommands.add(cmd);
                    }
                }
            }
        } else {
            for(CommandInterface cmd : StarLoader.getAllCommands()) {
                if(cmd.isAdminOnly())adminCommands.add(cmd);
                else playerCommands.add(cmd);
            }
        }

        if(sender.isAdmin()) {
            StringBuilder adminBuilder = new StringBuilder();
            for(CommandInterface ac : adminCommands) adminBuilder.append("/").append(ac.getCommand()).append("     alias:").append(Arrays.toString(ac.getAliases())).append("\n");
            PlayerUtils.sendMessage(sender,"ADMIN COMMANDS:\n" +  adminBuilder.toString());
        }

        StringBuilder playerBuilder = new StringBuilder();
        for(CommandInterface c : playerCommands) playerBuilder.append("/").append(c.getCommand()).append("     alias:").append(Arrays.toString(c.getAliases())).append("\n");
        PlayerUtils.sendMessage(sender,  "PLAYER COMMANDS:\n" + playerBuilder.toString());

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
