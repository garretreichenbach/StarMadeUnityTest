package api.utils.game.chat.commands;

import api.ModPlayground;
import api.mod.ModSkeleton;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

public class ListModsCommand implements CommandInterface {
    String lastMessage = "";

    @Override
    public String getCommand() {
        return "list_mods";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"list_mods"};
    }

    @Override
    public String getDescription() {
        return "list all active mods on this server\n" +
                "Flags:\n" +
                "n: name\n" +
                "a: author\n" +
                "v: version\n" +
                "r: resource id\n" +
                "e: export to clipboard";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean onCommand(@Nullable PlayerState sender, String[] args) {
        List<String> argsList = Arrays.asList(args);
        boolean showAll = args.length==1&&args[0].equals("");
        boolean showName = argsList.contains("n")||showAll;
        boolean showVersion = argsList.contains("v")||showAll;
        boolean showAuthor = argsList.contains("a")||showAll;
        boolean showResource = argsList.contains("r")||showAll;
        boolean export = argsList.contains("e")||showAll;

        if (!showName && !showVersion && !showAuthor  && !showResource) {
            lastMessage = "";
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("Active mods:");
        for (ModSkeleton mod: StarLoader.starMods) {
            if (!mod.isEnabled())
                continue;
            stringBuilder.append("\n");
            if (showName)
                stringBuilder.append(mod.getRealMod().getName());

            if (showAuthor)
                stringBuilder.append(" a:").append(mod.getModAuthor());

            if (showVersion)
                stringBuilder.append(" v:").append(mod.getModVersion());

            if (showResource)
                stringBuilder.append(" ").append(mod.getSmdResourceId());
        }
        if (export) {
            StringSelection selection = new StringSelection(stringBuilder.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);
        }
        if (sender != null)
            PlayerUtils.sendMessage(sender,stringBuilder.toString());
        lastMessage = stringBuilder.toString();
        return true;
    }

    @Override
    public void serverAction(PlayerState sender, String[] args) {
        //ignored, bc deprecated
    }

    @Override
    public StarMod getMod() {
        return StarLoader.getModFromMainClass(ModPlayground.class);
    }
}
