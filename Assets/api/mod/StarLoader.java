package api.mod;

import api.DebugFile;
import api.ModPlayground;
import api.StarLoaderHooks;
import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.Event;
import api.listener.fastevents.FastListenerCommon;
import api.utils.game.chat.CommandGroup;
import api.utils.game.chat.CommandInterface;
import api.utils.other.HashList;
import api.utils.other.LangUtil;

import javax.annotation.Nullable;
import java.util.*;

public class StarLoader{
    /**
     * Version of StarLoader
     * Versioning scheme: [Main].[Major feature].[Build]
     *
     * 0.x.xxx = Indev
     * 1.x.xxx = Main release
     * 2.x.xxx = Universe Update release
     */
    public static final String version = "0.5.100";
    public static final String versionName = "Alpha";
    public static ArrayList<ModSkeleton> starMods = new ArrayList<ModSkeleton>();
    public static HashMap<Class<? extends Event>, ArrayList<Listener>> listeners = new HashMap<Class<? extends Event>, ArrayList<Listener>>();

    private static HashList<StarMod, CommandInterface> commands = new HashList<>();

    private static ArrayList<String> clientCommandList = new ArrayList<>();

    public static void clearListeners() {
        listeners.clear();
        FastListenerCommon.clearAllListeners();
    }

    public static ArrayList<Listener> getListeners(Class<? extends Event> clazz) {
        return listeners.get(clazz);
    }

    public static boolean hasListeners(Class<? extends Event> clazz) {
        return !(getListeners(clazz) == null);
    }

    public static void fireEvent(Event ev, boolean isServer) {
        ev.server = isServer;

        List<Listener> listeners = StarLoader.getListeners(ev.getClass());
        if (listeners == null) // Avoid iterating on null Event listeners
            return;
        for (Listener listener : listeners) {
            try {
                listener.onEvent(ev);
            } catch (Exception e) {
                DebugFile.log("Exception during event: " + ev.getClass().getSimpleName());
                DebugFile.logError(e, null);
            }
        }
    }

    public static void sortListeners(){
        for (Map.Entry<Class<? extends Event>, ArrayList<Listener>> entry : listeners.entrySet()) {
            ArrayList<Listener> entries = entry.getValue();
            Collections.sort(entries, (o1, o2) -> Integer.compare(o1.getPriority().ordinal(), o2.getPriority().ordinal()));
        }
    }

    private static final ArrayList<ModSkeleton> coreMods = new ArrayList<>();
    public static void registerCoreMod(ModSkeleton mod) {
        coreMods.add(mod);
    }
    /**
     * Gets a list of all mods that need to use class transformers
     */
    public static ArrayList<ModSkeleton> getCoreMods(){
        return coreMods;
    }

    /**
     * Disables all client mods including StarLoader.
     */
    public static void disableAllMods() {
        for(ModSkeleton skeleton : starMods) {
            if(!(skeleton.getRealMod() instanceof ModPlayground)) SinglePlayerModData.getInstance().setClientEnabled(ModIdentifier.fromMod(skeleton), false);
        }
        SinglePlayerModData.getInstance().setClientEnabled(ModIdentifier.fromMod(ModPlayground.inst.getSkeleton()), false);
    }

    public static void enableMod(ModSkeleton mod) {
        DebugFile.log("== Enabling Mod " + mod.getName());
        if(!mod.getStarLoaderVersion().equals(StarLoader.version)){
            System.err.println("!! WARNING: Mod: " + mod.getDebugName() + " did not match StarLoader version [" + StarLoader.version + "], Mod SL Version: " + mod.getStarLoaderVersion());
        }
        StarLoaderHooks.onModEnableCommon(mod);
        mod.getRealMod().onEnable();
        mod.flagEnabled(true);
        DebugFile.log("== Mod " + mod.getName() + " Enabled");
    }

    public static void dumpModInfos(boolean enableOnly) {
        for (ModSkeleton mod : StarLoader.starMods) {
            if(!enableOnly || mod.isEnabled()) {
                DebugFile.log(mod.getDebugName());
            }
        }
    }
    public static <T extends Event> void registerListener(Class<T> clazz, StarMod mod, Listener<T> l) {
        registerListener(clazz, l, mod);
    }

    public static <T extends Event> void registerListener(Class<T> clazz, Listener<T> l, StarMod mod) {

        DebugFile.log("Registering listener " + clazz.getName());
        List<Listener> listeners = StarLoader.getListeners(clazz);
        l.setMod(mod);
        if (listeners == null) {
            ArrayList<Listener> new_listeners = new ArrayList<Listener>();
            new_listeners.add(l);
            StarLoader.listeners.put(clazz, new_listeners);
        } else {
            listeners.add(l);
        }
        sortListeners();
        DebugFile.log(" = Registered Listener. ");
    }

    public static <T extends Event> void unregisterListener(Class<T> clazz, Listener<T> l) {
        if (getListeners(clazz)== null)
            return;
        getListeners(clazz).remove(l);
    }

    //Legacy events, use fireEvent(Event, isServer);
    @Deprecated
    public static void fireEvent(Class<?> clazz, Event ev, boolean isServer) {
        fireEvent(ev, isServer);
    }

    //private static ArrayList<ImmutablePair<String, String>> commands = new ArrayList<ImmutablePair<String, String>>();

    public static ArrayList<String> getClientCommandList() {
        return clientCommandList;
    }

    public static void addClientCommandList(ArrayList<String> clientCommands) {
        for(String command : clientCommands) {
            if(!clientCommandList.contains(command)) clientCommandList.add(command);
        }
    }

    public static CommandInterface getCommand(String command) {
        if(GameClient.getClientState() != null) {

        }
        for(CommandInterface cmd : getAllCommands()) {
            if(LangUtil.stringsEqualIgnoreCase(cmd.getCommand(), command)) return cmd; //Check if command is equal
            if(cmd.getAliases() != null && cmd.getAliases().length > 0) { //Check command aliases
                for(String alias : cmd.getAliases()) {
                    alias = alias.replace("%SEPARATOR%", "_");
                    if(LangUtil.stringsEqualIgnoreCase(alias, command.replace(" ", "_"))) return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Registers the specified command.
     * @param command The command to register
     */
    public static void registerCommand(CommandInterface command) {
        commands.add(command.getMod(), command);
    }

    /**
     * Registers all commands from the specified command group.
     * @param commandGroup The command group to register
     */
    public static void registerCommand(CommandGroup commandGroup) {
        CommandInterface[] subCommands = commandGroup.getCommands();
        for(CommandInterface subCommand : subCommands) commands.add(subCommand.getMod(), subCommand);
    }

    public static ArrayList<CommandInterface> getModCommands(StarMod mod) {
        return commands.get(mod);
    }

    public static ArrayList<CommandInterface> getAllCommands() {
        ArrayList<CommandInterface> commandList = new ArrayList<>();
        for(ArrayList<CommandInterface> list : commands.values()) commandList.addAll(list);
        return commandList;
    }

    @Nullable
    public static <T> T getModFromMainClass(Class<T> modClass){
        for (ModSkeleton starMod : starMods) {
            if(starMod.getRealMod().getClass().equals(modClass)){
                return (T) starMod.getRealMod();
            }
        }
        return null;
    }

    //TODO Migrate to hashmap
    @Nullable
    public static ModSkeleton getModFromName(String name){
        for (ModSkeleton starMod : starMods) {
            if(starMod.getName().toLowerCase(Locale.ENGLISH).equals(name.toLowerCase(Locale.ENGLISH))){
                return starMod;
            }
        }
        return null;
    }
    @Nullable
    @Deprecated
    public static ModSkeleton getLatestModFromId(int id){
        // Try and find the latest jar (kinda jank but its easy and works in most cases)
        long lastModified = 0;
        ModSkeleton mod = null;
        for (ModSkeleton starMod : starMods) {
            if(starMod.getSmdResourceId() == id){
                if(id == -1) return starMod; // Return StL mod right away (it has no jar)
                long l = starMod.getJarFile().lastModified();
                if(l > lastModified){
                    lastModified = l;
                    mod = starMod;
                }
            }
        }
        return mod;
    }

    @Nullable
    public static ModSkeleton getModFromId(ModIdentifier id){
        for (ModSkeleton starMod : starMods) {
            if(starMod.getSmdResourceId() == id.id && starMod.getModVersion().equals(id.version)){
                return starMod;
            }
        }
        return null;
    }

    public static String getVersionString() {
        return version + " " + versionName;
    }
}
