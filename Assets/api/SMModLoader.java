package api;

import api.listener.Listener;
import api.mod.*;
import api.mod.exception.ModExceptionWindow;
import api.smd.SMDUtils;
import api.utils.StarRunnable;
import api.utils.game.chat.CommandInterface;
import me.jakev.starloader.LaunchClassLoader;
import org.apache.commons.io.IOUtils;
import org.luaj.vm2.lib.jse.LuajavaLib;
import org.schema.common.CallInterace;
import org.schema.common.LogUtil;
import org.schema.game.client.view.gui.LoadingScreenDetailed;
import org.schema.game.common.Starter;
import org.schema.game.common.api.ApiOauthController;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SMModLoader {
    public static final File modFolder = new File("mods");
    public static final File modLocalFolder = new File("modslocal");

    private static final Set<Integer> modSkeletonsLoaded = new HashSet<>();
    private static final Set<Integer> modSkeletonsLocalLoaded = new HashSet<>();

    private static void loadJarIntoLaunchClassLoader(File file){
        try {
            LaunchClassLoader classLoader = (LaunchClassLoader) SMModLoader.class.getClassLoader();

            classLoader.addJarURL(file.toURI().toURL());
            ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
            while (true) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) break;
                byte[] classBytes = IOUtils.toByteArray(zip);
                String name = entry.getName();
//                if (!name.startsWith("org/schema")) {
                if (name.endsWith(".class")) {
                    classLoader.classes.put(LaunchClassLoader.getFQNFromClass(name), classBytes);
                }
            }
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Loads a mod jar into the JVM and instansiates its main class
     */
    public static Class<?> instantiateFromSkeleton(ModSkeleton skeleton) {
        loadJarIntoLaunchClassLoader(skeleton.getJarFile());
        try {
            ClassLoader classLoader = SMModLoader.class.getClassLoader();
            return classLoader.loadClass(skeleton.getMainClass());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();//
        }
        throw new RuntimeException();
    }

    public static StarMod loadMod(ModSkeleton skeleton) {
        if (skeleton.isLoaded()) {
            System.err.println("[StarLoader] [SMModLoader] Did not load: " + skeleton.getDebugName() + " because it is already loaded.");
            return skeleton.getRealMod();
        }
        DebugFile.log("Loading Mod: " + skeleton.getDebugName());
        try {
            Class<?> mainClass = instantiateFromSkeleton(skeleton);
            Constructor<?> constructor = mainClass.getConstructors()[0];
            Object o = constructor.newInstance();
            if (!(o instanceof StarMod)) {
                DebugFile.err("Failed to load mod! not instanceof StarMod.");
                throw new IllegalArgumentException("Main class must be an instance of StarMod");
            } else {
                StarMod sMod = ((StarMod) o);
                skeleton.setLoaded(true);
                skeleton.setRealMod(sMod);
                sMod.setSkeleton(skeleton);
                skeleton.setClassLoader((LaunchClassLoader) sMod.getClass().getClassLoader());
                sMod.onLoad();
                return sMod;
            }
        } catch (InvocationTargetException e) {
            DebugFile.logError(e, null);
            DebugFile.err(" !! InvocationTargetException occurred while loading mod!!");
            DebugFile.err("This error is thrown when the main class itself throws an error while being constructed");
            DebugFile.err("===== The root cause of this error is as follows: =====");
            DebugFile.logError(e.getCause(), null);
            DebugFile.err("===== =========================================== =====");
        } catch (UnsupportedClassVersionError e){
            System.err.println("UnsupportedClassVersionError, printing error and cause");
            System.out.println();
            DebugFile.logError(e, null);
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void loadModSkeletons() {
        File[] modList = modFolder.listFiles();
        File[] modLocalList = modLocalFolder.listFiles();

        if (modList == null) throw new NullPointerException("Could not list files in mods folder.");
        if (modLocalList == null) throw new NullPointerException("Could not list files in local mods folder.");

        for (File file : modLocalList) {
            if (file.getName().endsWith(".jar")) {
                loadModSkeleton(file, true, false);
            }
        }
        for (File file : modList) {
            if (file.getName().endsWith(".jar")) {
                loadModSkeleton(file, false, false);
            }
        }
    }

    /**
     * Loads a ModSkeleton from a file
     */
    public static ModSkeleton loadModSkeleton(File f, boolean isLocal, boolean override) {
        try {
            ModSkeleton skeleton = ModSkeleton.fromJarFile(f, isLocal);

            if (modSkeletonsLocalLoaded.contains(skeleton.getSmdResourceId()))
                throw new IOException(String.format(
                        "Mod %s (# %s) already exists locally ('%s'), cannot override.",
                        skeleton.getName(),
                        skeleton.getSmdResourceId(),
                        f.getName()
                ));

            if (!override && modSkeletonsLoaded.contains(skeleton.getSmdResourceId())){
                System.err.println("[SMModLoader] Duplicate mod: " + f.getName() + " will not be loaded because it is a duplicate");
                return null;
            }

            StarLoader.starMods.add(skeleton);
            modSkeletonsLoaded.add(skeleton.getSmdResourceId());
            if (skeleton.isLocal()) modSkeletonsLocalLoaded.add(skeleton.getSmdResourceId());
            return skeleton;

        } catch (Exception e) {
            e.printStackTrace();
            boolean shouldContinue = ModExceptionWindow.display(ModPlayground.inst.getSkeleton(), e);
            if (shouldContinue) {
                return null;
            }
        }
        throw new RuntimeException();
    }

    //    public static void loadAllModsInModsFolder(){
//        DebugFile.log("Loading Mods...");
//        for (File file : modFolder.listFiles()) {
//            try {
//                JarFile jf = new JarFile(file);
//                loadMod(jf);
//                jf.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                DebugFile.log("MOD LOAD FAILED::::");
//                DebugFile.logError(e, null);
//            }
//        }
//    }
    //Uplinking is when the client must restart to apply class transformers
    public static boolean shouldUplink = false;
    public static ArrayList<Integer> uplinkMods = new ArrayList<>();
    public static String uplinkServerHost;
    public static int uplinkServerPort;
    public static String[] uplinkArgs;
    public static boolean runningAsServer = false;

    /** by lupoCani, JakeV on 2023-07-02
     * Bundles various patches to account for issues in libraries and other surrounding systems.
    * */
    private static void runCompatibilityFixes() {
        //Fixes some errors with java 7 not connecting to modern sites properly
        try {
            SSLContext ssl = SSLContext.getInstance("TLSv1.2");
            ssl.init(null, null, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        System.setProperty("https.protocols", "TLSv1.2");

        // The code to disable certificate checking is in ApiOauthController's static initializer
        DebugFile.log("Making sure certificates are always trusted by loading ApiOauthController.");
        System.err.println("Printing non-final variable in ApiOAuthController to load it: " + ApiOauthController.testPW);

        // Makes LuaJ use Starloader's LaunchClassLoader when instantiating classes, for compatibility.
        // This method is grafted onto the library via a copy of the LuajavaLib class in ThirdParty/src.
        // Make sure it's included in compilation.
        LuajavaLib.setCustomClassLoader(SMModLoader.class.getClassLoader());
    }

    public static void launch(String[] args) throws ClassNotFoundException {
        // Set up vanilla logger
        try {
            LogUtil.setUp(((Integer) EngineSettings.LOG_FILE_COUNT.getInt()).intValue(), new CallInterace() {
                @Override
                public void call() {
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!modFolder.exists()) {
            modFolder.mkdir();
        }
        if (!modLocalFolder.exists()) {
            modLocalFolder.mkdir();
        }
        DebugFile.info("Starting with args: " + Arrays.toString(args));


        System.err.println("Loading default mod");
        StarLoader.starMods.add(ModSkeleton.getVirtualMod("StarLoader", "internal", "StarLoader components", StarLoader.version, true, new ModPlayground()));

        DebugFile.log("Loading mod skeletons...");

        loadModSkeletons();

        runCompatibilityFixes();

        DebugFile.log("Checking for mod updates...");
        ModUpdater.checkUpdateAll();
        DebugFile.log("Done.");

        //Store launch arguments incase we need to restart the game
        uplinkArgs = args;

        //If we should load mods immediately, so they can apply class transformers (-uplink or -server)
        boolean loadModsImmediately = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase(Locale.ENGLISH);
            if (arg.equals("-download")) {
                String[] modSerialized = args[i + 1].split(",");
                int resId = Integer.parseInt(args[i + 1]);
                DebugFile.log("Downloading mod: " + resId + " v" + modSerialized[1]);
                try {
                    SMDUtils.downloadMod(new ModIdentifier(resId, modSerialized[1]));
                    DebugFile.log("Done. Exiting program");
                    System.exit(0);
                } catch (NullPointerException e) {
                    DebugFile.log("Mod not found, use the resource id + version on starmade dock [-download 1234123,1.0]");
                } catch (IOException e) {
                    DebugFile.log("IOException while trying to download, heres the error:");
                    e.printStackTrace();
                }
            } else if (arg.equals("-uplink")) {
                // -uplink host.name 4242 1,2,3,44,55,66,12345
                System.err.println("Running with -uplink, should connect to server on first splash screen draw");
                shouldUplink = true;
                loadModsImmediately = true;
                uplinkServerHost = args[i + 1];
                uplinkServerPort = Integer.parseInt(args[i + 2]);
                if(args.length > 4) {
                    for (String str : args[i + 3].split(",")) {
                        uplinkMods.add(Integer.parseInt(str));
                    }
                }

                System.err.println("Uplink info set successfully");
            } else if (arg.equals("-autoupdatemods")) {
                System.err.println("[SMModLoader] Checking for mod updates.");
                ArrayList<ModSkeleton> outdatedMods = new ArrayList<>();
                for (ModSkeleton starMod : StarLoader.starMods) {
                    SMDModInfo mod = SMDModData.getInstance().getModData(starMod.getSmdResourceId());
                    if (mod != null) {
                        System.err.println("[SMModLoader] Checked: " + starMod.getDebugName() + ", remote: " + mod.getLatestDownloadVersion());
                        if (!starMod.getModVersion().equals(mod.getLatestDownloadVersion())) {
                            outdatedMods.add(starMod);
                        }
                    }
                }
                System.err.println("[SMModLoader] Downloading all outdated mods...");
                for (ModSkeleton mod : outdatedMods) {
                    SMDModInfo modInfo = SMDModData.getInstance().getModData(mod.getSmdResourceId());
                    try {
                        ModIdentifier modId = new ModIdentifier(modInfo.getResourceId(), modInfo.getLatestDownloadVersion());
                        System.err.println("[SMModLoader] Downloading: " + modId);
                        deleteMod(mod);
                        ModUpdater.downloadAndLoadMod(modId, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                System.err.println("[SMModLoader] Done updating.");
            } else if (arg.equals("-server")) {
                loadModsImmediately = true;
                //When running with -server, assume we should "-uplink all_mods"
                runningAsServer = true;
                for (ModSkeleton modSkeleton : StarLoader.starMods) {
                    uplinkMods.add(modSkeleton.getSmdResourceId());
                }
            }
        }
        if (loadModsImmediately) {
            DebugFile.log("Loading -uplink/-server mods right away");
            LoadingScreenDetailed.modMainStatus = "Init Stage [ModLoad]: Loading -uplink mods";
            //If started with uplink/server, just load the mods right away so their class transformers are applied properly
            for (Integer modId : uplinkMods) {
                ModSkeleton mod = StarLoader.getLatestModFromId(modId);
                if (mod == null) throw new IllegalArgumentException("Mod not found with -uplink: " + modId);
                if (modId <= 0) {
                    System.err.println("Not loading virtual mod: " + modId);
                } else {
                    LoadingScreenDetailed.modSecondaryStatus = "Loading: " + mod.getDebugName();
                    SMModLoader.loadMod(mod);
                }

                //Subscribe mod to transform event
                if (mod.isCoreMod()) {
                    StarLoader.registerCoreMod(mod);
                }
            }
            LoadingScreenDetailed.modMainStatus = "Done Mod Load.";
        }
        //DebugFile.log("Download blueprint data");
//        SMDEntryUtils.fetchDataOnThread();
        //Have it only do this when Browse tab is activated
        try {
            DebugFile.log("Starting StarMade...");
            Starter.main(args);
        } catch (IOException e) {
            e.printStackTrace();
            DebugFile.logError(e, null);
        }
    }

    /**
     * Disables an individual mod, this is mostly a debug tool, as we cannot be sure deeper modifications to the game are erased.
     */
    public static void unloadMod(ModSkeleton mod) {
        DebugFile.warn("Hard unloading mod: " + mod.getName());
        if (mod.isEnabled()) {
            //Only disable if not already enabled (like on the title screen)
            ModStarter.disableMod(mod);
        }
        //Clear Listeners of self:
        for (ArrayList<Listener> value : StarLoader.listeners.values()) {
            ArrayList<Listener> removeQueue = new ArrayList<>();
            for (Listener listener : value) {
                if (listener.getMod() == mod.getRealMod()) {
                    removeQueue.add(listener);
                }
            }
            for (Listener listener : removeQueue) {
                value.remove(listener);
            }
        }
        //StarRunnables
        StarRunnable.deleteAllFromMod(mod);

        //Commands
        ArrayList<CommandInterface> removeQueue = new ArrayList<>();
        for(CommandInterface cmd : StarLoader.getAllCommands()) {
            if(cmd.getMod() == mod.getRealMod()) removeQueue.add(cmd);
        }
        for(CommandInterface ch : removeQueue) StarLoader.getAllCommands().remove(ch);

        StarLoader.starMods.remove(mod);
    }

    /**
     * Unloads and deletes a mod jar
     */
    public static void deleteMod(ModSkeleton f) {
        SinglePlayerModData.getInstance().setClientEnabled(ModIdentifier.fromMod(f), false);
        SMModLoader.unloadMod(f);
        boolean good = f.getJarFile().delete();
        if (!good) {
            System.err.println("!!! WARNING !!! COULD NOT DELETE MOD");
        }
    }
}
