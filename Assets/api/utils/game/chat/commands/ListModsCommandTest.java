package api.utils.game.chat.commands;

import api.mod.StarLoader;
import api.mod.StarMod;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static api.mod.ModSkeleton.getVirtualMod;

public class ListModsCommandTest {
    public static void main(String[] args) throws IOException, UnsupportedFlavorException {
        ListModsCommand ci = new ListModsCommand();
        StarMod mock1 = new StarMod();
        mock1.setSkeleton(getVirtualMod("MOD A", "BOBBY B", "dummy mod", "1.0", false, mock1));
        StarLoader.enableMod(mock1.getSkeleton());

        StarMod mock2 = new StarMod();
        mock2.setSkeleton(getVirtualMod("MOD B", "Vizzy_T", "second dummy mod", "0.18.25 very special name", false, mock2));
        StarLoader.enableMod(mock2.getSkeleton());

        StarMod disabled = new StarMod();
        disabled.setSkeleton(getVirtualMod("MOD B", "Vizzy_T", "second dummy mod", "0.18.25 very special name", false, disabled));

        StarLoader.starMods.add(mock1.getSkeleton());
        StarLoader.starMods.add(mock2.getSkeleton());
        StarLoader.starMods.add(disabled.getSkeleton());

        ci.onCommand(null, new String[]{});
        assert ci.lastMessage.equals("Active mods:\n" +
                "MOD A a:BOBBY B v:1.0 -1\n" +
                "MOD B a:Vizzy_T v:0.18.25 very special name -1"):"show default: all";

        ci.onCommand(null, new String[]{"n"});
        assert ci.lastMessage.equals("Active mods:\n" +
                "MOD A\n" +
                "MOD B"):"show only name";

        ci.onCommand(null, new String[]{"v"});
        assert ci.lastMessage.equals("Active mods:\n" +
                " v:1.0\n" +
                " v:0.18.25 very special name"):"show only version";

        ci.onCommand(null, new String[]{"r"});
        assert ci.lastMessage.equals("Active mods:\n" +
                " -1\n" +
                " -1"):"show only resource id";

        ci.onCommand(null,new String[]{"n","e"});
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String text = (String) t.getTransferData(DataFlavor.stringFlavor);
        assert text.equals("Active mods:\n" +
                "MOD A\n" +
                "MOD B"):"do export for name";

        boolean success = ci.onCommand(null,new String[]{"invalid flag"});
        assert !success;

        System.out.println("SUCCESS");
    }
}
