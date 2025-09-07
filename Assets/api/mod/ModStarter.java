package api.mod;

import api.DebugFile;
import api.ModPlayground;
import api.SMModLoader;
import api.StarLoaderHooks;
import api.common.GameClient;
import api.mod.config.PersistentObjectUtil;
import api.mod.exception.ModDependencyException;
import api.mod.exception.ModExceptionWindow;
import api.mod.exception.ModFailedToDownloadException;
import api.mod.resloader.SLModResourceLoader;
import api.network.Packet;
import api.utils.GameRestartHelper;
import api.utils.StarRunnable;
import api.utils.registry.UniversalRegistry;
import org.apache.commons.io.FileUtils;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.gui.LoadingScreenDetailed;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.network.StarMadeNetUtil;
import org.schema.schine.resource.FileExt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ModStarter {
    private static void registerClientSideMods(ArrayList<ModSkeleton> enableQueue) {
        for (ModSkeleton starMod : StarLoader.starMods) {
            boolean clientEnabled = SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(starMod));
            if (!starMod.isEnabled() && starMod.isClientMod() && clientEnabled) {
                //If the mod was not loaded, (not a core mod, not on the server), then load it
                if (!starMod.isLoaded()) {
                    //Lastly, don't try to load vmods
                    if(starMod.getSmdResourceId() != -1){
                        SMModLoader.loadMod(starMod);
                    }
                }
                enableQueue.add(starMod);
            }
        }
    }

    public static void preServerStart() {
        lastConnected = ServerConfig.WORLD.getString();
        justStartedServer = true;
        lastConnectedToClient = true;
        //Enable all mods in the mods folder
        DebugFile.log("[Server] Enabling mods...");
        ArrayList<ModSkeleton> enableQueue = new ArrayList<ModSkeleton>();
        //If we ran with -server or -uplink, load SMModLoader.uplinkMods
        if (SMModLoader.shouldUplink || SMModLoader.runningAsServer) {
            for (Integer modId : SMModLoader.uplinkMods) {
                ModSkeleton mod = StarLoader.getLatestModFromId(modId);
                if (mod == null) throw new RuntimeException("Mod not found with -uplink: " + modId);
                if (modId <= 0) {
                    System.err.println("Not loading virtual mod: " + modId);
                } else {
                    enableQueue.add(mod);
                }
            }
        } else {
            for (ModSkeleton mod : StarLoader.starMods) {
                //For StarLoader mod, read the Universal Registry data
                if (mod.getRealMod() instanceof ModPlayground) {
                    UniversalRegistry.readData((ModPlayground) mod.getRealMod());
                    //Otherwise, just add it to the enable queue
                } else if (SinglePlayerModData.getInstance().isClientEnabled(ModIdentifier.fromMod(mod))) {
                    enableQueue.add(mod);
                }
            }
            //If not started with uplink (direct connect), check if we should uplink
            for (ModSkeleton skeleton : enableQueue) {
                //If we have any coremods installed, run again with uplink
                if (skeleton.isCoreMod()) {
                    System.err.println("== CoreMod detected, running game with -uplink");
                    System.err.println("CoreMod: " + skeleton.getDebugName());
                    ArrayList<Integer> mods = new ArrayList<>();
                    for (ModSkeleton mod : enableQueue) {
                        mods.add(mod.getSmdResourceId());
                    }

                    try {
                        GameRestartHelper.runWithUplink("localhost", 4242, mods);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Load the mods files and what not
        for (ModSkeleton mod : enableQueue) {
            SMModLoader.loadMod(mod);
        }


        //Tell mods to assign URV's and what else in preEnable
        for (ModSkeleton starMod : enableQueue) {
            StarLoaderHooks.onPreEnableServer(starMod);
        }
        //Save Universal Registry after all mods have registered their data
        //Todo: Temp fix
        StarLoader.starMods.add(ModSkeleton.getVirtualMod("StarLoader", "internal", "StarLoader components", StarLoader.version, true, new ModPlayground()));
        //
        UniversalRegistry.writeToFile(ModPlayground.inst);
        //Lastly, enable mods.
        enableMods(enableQueue);

        System.err.println("[ModStarter] Reinitializing block data for server mods");
        ElementKeyMap.reinitializeData(new FileExt("./data/config/BlockConfig.xml"), false, null, GameResourceLoader.getConfigInputFile(), true);
    }

    public static void onDisconnect() {
        justStartedClient = false;
        justStartedServer = false;
    }

    public static void postServerStart() {
        //whatever lol
    }

    public static void downloadFile(URL url, String fileName) throws IOException {
        final URLConnection openConnection = url.openConnection();
        openConnection.setConnectTimeout(20000);
        openConnection.setRequestProperty("User-Agent", "StarMade-Client");
        FileUtils.copyInputStreamToFile(openConnection.getInputStream(), new File(fileName));
    }

    public static void disableAllMods() {
        DebugFile.log("==== Disabling All Mods ====");
        for (ModSkeleton mod : StarLoader.starMods) {
            if (mod.isEnabled()) {
                disableMod(mod);
            }
        }
        StarLoader.clearListeners();
        StarRunnable.deleteAll();
        StarLoader.getAllCommands().clear();
        Packet.clearPackets();
        //TODO: Retransform unloaded mod classes to original?

    }

    public static void disableMod(ModSkeleton mod) {
        mod.getRealMod().onDisable();
        PersistentObjectUtil.onDisableMod(mod);
        mod.flagEnabled(false);
    }

    // Reverse compat vars - These are buggy in the case a mod is enabled before preClientConnect is called
    @Deprecated
    public static boolean lastConnectedToServer = false;
    @Deprecated
    public static boolean lastConnectedToClient = false;
    // END

    public static boolean justStartedServer = false;
    public static boolean justStartedClient = false;
    public static boolean justStartedSinglePlayer = false;
    public static String lastConnected;

    public static boolean preClientConnect(String serverHost, int serverPort) {
        if (!serverHost.equals("localhost")) {
            lastConnected = serverHost + "~" + serverPort;
        }
        lastConnectedToServer = true;
        justStartedClient = true;
        DebugFile.log("[Pre-Client connect]");
        String serverUID = ServerModInfo.getServerUID(serverHost, serverPort);
        ArrayList<ModIdentifier> serverMods = ServerModInfo.getServerInfo(serverUID);
        if (serverMods == null) {
            DebugFile.log("Mod info not found for: " + serverHost + ":" + serverPort + " This is likely because they direct connected");
            StarMadeNetUtil starMadeNetUtil = new StarMadeNetUtil();
	        //                System.err.println(starMadeNetUtil.getServerInfo(serverHost, serverPort, 5000).toString());
	        //should register the mods.
	        serverMods = ServerModInfo.getServerInfo(serverUID);
        }
        if (serverMods == null) {
            DebugFile.log("Server mod info not found even after refresh.");
            LoadingScreenDetailed.modMainStatus = "Server mod info not found even after refresh.";
            serverMods = new ArrayList<ModIdentifier>();
        }
        if (serverHost.equals("localhost")) {
            DebugFile.log("Connecting to own server, mods are already enabled by the server [Loading no mods for client]");
        } else {
            LoadingScreenDetailed.modMainStatus = "Disabling existing mods...";
            disableAllMods();
            System.err.println("[ModStarter] Enabling mods onPreClientConnect, non-localhost");
            ArrayList<ModSkeleton> enableQueue = new ArrayList<ModSkeleton>();

            //Default mod download queue is every server mod, then they get removed as they are added to the enable queue
            ArrayList<ModIdentifier> downloadQueue = new ArrayList<>(serverMods);

            //For every mod on the server, enable it on the client.
            for (ModIdentifier serverMod : serverMods) {
                ModSkeleton mod = StarLoader.getModFromId(serverMod);
                if (serverMod.id == -1) {
                    if (!StarLoader.version.equals(serverMod.version)) {
                        System.err.println("[ModStarter] Warning: StarLoader version mismatch. local=" + StarLoader.version + ", remote=" + serverMod.version);
                    }else{
                        System.err.println("[ModStarter] StarLoader verision correct: " + StarLoader.version);
                    }
                    //Also, remove it from the download queue, as we obviously don't want to download it.
                    downloadQueue.remove(serverMod);
                } else if (mod != null) {
                    if (serverMod.equalsMod(mod)) {
                        DebugFile.log("[Client] >>> Added modId to enable queue: " + serverMod);
                        //If we have the mod on the client, remove it from the download queue
                        downloadQueue.remove(serverMod);
                        //Load the mod into the jvm, If it is already loaded then nothing will happen.
                        SMModLoader.loadMod(mod);
                        enableQueue.add(mod);
                    }
                }
            }

            // Download Mods if necessary
            if (!downloadQueue.isEmpty()) {
                LoadingScreenDetailed.modMainStatus = "Downloading server mods from StarMade dock...";
                //Now we need to download them from the client
                DebugFile.log("=== DEPENDENCIES NOT MET, DOWNLOADING MODS ===");
                for (ModIdentifier serverModId : downloadQueue) {
                    DebugFile.log("WE NEED TO DOWNLOAD: " + serverModId);
                    try {
                        LoadingScreenDetailed.modMainStatus = "Downloading: " + serverModId;
                        ModSkeleton sk = ModUpdater.downloadAndLoadMod(serverModId, null);
                        LoadingScreenDetailed.modMainStatus = "Mod loaded.";
                        enableQueue.add(sk);

                    } catch (Exception e) {
                        System.err.println("Failed to download, reason:");
                        e.printStackTrace();
                        try {
                            System.err.println("Trying to redownload mod in case of it being a rare corruption...");
                            LoadingScreenDetailed.modMainStatus = "Downloading: " + serverModId;
                            ModSkeleton sk = ModUpdater.downloadAndLoadMod(serverModId, null);
                            LoadingScreenDetailed.modMainStatus = "Mod loaded.";
                            enableQueue.add(sk);
                        } catch (IOException ex) {
                            System.err.println("Inner exception:");
                            ex.printStackTrace();
                        }

                        ModExceptionWindow.display(ModPlayground.inst.getSkeleton(), new ModFailedToDownloadException(serverModId, e));
//                        JOptionPane.showMessageDialog(null, "Failed to download mods... please send in starloader.log and log/starmade0.log");
//                        GameClient.getClientState().handleExceptionGraphically(new DisconnectException("Failed to download mods... please send in starloader.log and log/starmade0.log\nActualException: " + e.getClass().getSimpleName()));
                    }
                }
                //JOptionPane.showMessageDialog(null, "We are going to need to download some mods... fancy gui coming later");
                //DebugFile.log("We are going to download some mods, so dont start the client yet");
            }
            LoadingScreenDetailed.modMainStatus = "Done load cycle";
            LoadingScreenDetailed.modSecondaryStatus = "...";

            // If any of our mods require class resize (Most core mods), we will need to restart the client with uplink info
            if (SMModLoader.shouldUplink) {
                //However, if the client was started with uplink info, then the class transformers should already be applied.
            } else {
                for (ModSkeleton skeleton : enableQueue) {
                    //If we have any coremods installed, run again with uplink
                    if (skeleton.isCoreMod()) {
                        ArrayList<Integer> mods = new ArrayList<>();
                        for (ModSkeleton mod : enableQueue) {
                            mods.add(mod.getSmdResourceId());
                        }

                        try {
                            LoadingScreenDetailed.modMainStatus = "Core mods present, we are going to need to restart the client.";
                            GameRestartHelper.runWithUplink(serverHost, serverPort, mods);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
            }
            //When connecting to a server, enable enabled client-side mods
            LoadingScreenDetailed.modMainStatus = "Registering client-side mods";
            registerClientSideMods(enableQueue);

            //Enable all mods in the queue
            enableMods(enableQueue);
        }

        LoadingScreenDetailed.modMainStatus = "Startup complete";
        LoadingScreenDetailed.modSecondaryStatus = "...";
        DebugFile.log("===== Clearing server mods =====");
        ServerModInfo.wipeServerInfo(serverUID);
        DebugFile.log("===== Listing enabled mods =====");
        StarLoader.dumpModInfos(true);
        DebugFile.log("==========================================");
        return true;
    }

    public static void postClientConnect() {

    }

    //TODO: add this, currently just disables them on server join
    public static void onClientLeave() {
        disableAllMods();
        PersistentObjectUtil.flushLogs(true);
    }

    public static void main(String[] args) {
        try {
            downloadFile(new URL("https://starmadedock.net/content/turret-hotkey.8054/download"), "TurretHotKey.jar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will sort all mods so that when enabled (in order), dependencies will work correctly.
     * <p>
     * If there are any circular dependencies... That's not good
     */
    public static void sortMods(ArrayList<ModSkeleton> mods) {
        for (int i = 0; i < mods.size(); i++) {
            ModSkeleton mod = mods.get(i);
            //Put direct dependencies above self.
        }
    }

    /**
     * The load stage of starloader.
     */
    public enum LoadStage {
        //StarLoader lifecycle events
        MOD_SORTING,
        LOAD_STARLOADER,
        MOD_ENABLE,
        DONE_MOD_ENABLE,

        //StarMade lifecycle events
        GAME_RESOURCE_LOADER_LOAD,
        BLOCK_CONFIG,
        PARTICLE_LOADING,
        DONE,
        ;

        @Override
        public String toString() {
            String name = name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    private static LoadStage currentLoadStage = LoadStage.MOD_SORTING;

    /**
     * Displays a nice popup window for mod errors
     * Locks the game up.
     */
    public static boolean handleModLoadException(ModSkeleton mod, Exception e) {
        return !ModExceptionWindow.display(mod, e);
    }

    /**
     * Sets the text of the current load stage
     */
    public static void setCurrentLoadStage(LoadStage stage) {
        LoadingScreenDetailed.modMainStatus = "Mod load stage: " + stage;
        currentLoadStage = stage;
    }

    public static void setSecondaryText(String str) {
        LoadingScreenDetailed.modSecondaryStatus = str;
    }

    public static LoadStage getCurrentLoadStage() {
        return currentLoadStage;
    }

    public static void enableMods(ArrayList<ModSkeleton> mods) {
        //1. Sort all mods by their name TODO proper topological sorting
        setCurrentLoadStage(LoadStage.MOD_SORTING);
        Collections.sort(mods, (mod1, mod2) -> {
            int m1Hash = mod1.getName().hashCode();
            int m2Hash = mod2.getName().hashCode();
            return Integer.compare(m1Hash, m2Hash);
        });
        setCurrentLoadStage(LoadStage.LOAD_STARLOADER);
        //2. Enable StarLoader first always. Also clear packets
        Packet.clearPackets();
        StarLoader.enableMod(ModPlayground.inst.getSkeleton());

        //2. Recursively enable mods in order
        // Mod loading order is defined as long as dependencies are properly setup
        ArrayList<ModSkeleton> modLoadOrder = new ArrayList<>();
        for (ModSkeleton mod : mods) {
            //TODO use mod sorting rather than this which I dont think works 100% of the time
            enableModRec(mod, modLoadOrder);
        }

        //=============================================================================== Fire resource loading events

        //Mod Enable
        setCurrentLoadStage(LoadStage.MOD_ENABLE);
        for (ModSkeleton mod : modLoadOrder) {
            try {
                LoadingScreenDetailed.modSecondaryStatus = mod.getDebugName();
                StarLoader.enableMod(mod);
            } catch (Exception e) {
                if (handleModLoadException(mod, e)) {
                    System.exit(1);
                    return;
                }
            }
        }

        SLModResourceLoader.loadResourcesLoad();
        //=============================================================================================== Finish up
        setCurrentLoadStage(LoadStage.DONE);
        LoadingScreenDetailed.modSecondaryStatus = "";
        if (Math.random() < 0.02F) {
            LoadingScreenDetailed.modSecondaryStatus = "Hacking mainframe";
        }
        //Dump packet lookup
        Packet.dumpPacketLookup();

        StarLoaderHooks.onPostModsEnable();

    }

    private static void enableModRec(ModSkeleton mod, ArrayList<ModSkeleton> orderedMods) {
        for (Integer dependency : mod.getDependencies()) {
            ModSkeleton dep = StarLoader.getLatestModFromId(dependency);
            if (dep == null) {
                //Mod dependency was not found
                StarLoader.dumpModInfos(false);
                ModDependencyException exception = new ModDependencyException(mod, dependency);
                GameClient.getClientState().handleExceptionGraphically(exception);
                throw exception;
            }
            enableModRec(dep, orderedMods);
        }
        if (!mod.isEnabled()) {
            if(!orderedMods.contains(mod)){
                orderedMods.add(mod);
            }
        }
    }
}
