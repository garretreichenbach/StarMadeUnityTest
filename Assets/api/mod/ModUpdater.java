package api.mod;

import api.DebugFile;
import api.ModPlayground;
import api.SMModLoader;
import api.smd.SMDUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ModUpdater {

    private static final ArrayList<ModSkeleton> removeQueue = new ArrayList<>();

    /**
     * Downloads a mod based on the ModIdentifier, and removes oldMod from the master list in StarLoader.java if not null
     * @param resId ModIdentifier of the mod.
     * @param oldMod The mod that the new mod replaces.
     * @return The loaded ModSkeleton
     * @throws IOException If any sort of network error happens
     */
    public static ModSkeleton downloadAndLoadMod(ModIdentifier resId, @Nullable ModSkeleton oldMod) throws IOException {
        if(oldMod != null) removeQueue.add(oldMod);
        File file = SMDUtils.downloadMod(resId);
        //Should overwrite existing files in the vm, or so I hope.
        ModSkeleton skeleton = SMModLoader.loadModSkeleton(file, false, true);
        if (skeleton == null) throw new IOException("Could not load downloaded mod skeleton, likely [local].");

        checkAndDeleteOldMods(ModIdentifier.fromMod(skeleton));

        SMModLoader.loadMod(skeleton);
        return skeleton;
    }
    /**
     *
     */
    public static void checkAndDeleteOldMods(ModIdentifier newMod){
        System.err.println("[ModUpdater] Deleting outdates mods for: " + newMod);
        ArrayList<ModSkeleton> mods = new ArrayList<>(StarLoader.starMods);
        for (ModSkeleton mod : mods) {
            if(newMod.id == mod.getSmdResourceId() && !newMod.version.equals(mod.getModVersion())){
                System.err.println("[ModUpdater] Version mismatch on: " + mod + " Deleting...");
                // If the mods have the same id, but different versions, delete it.

                // If the old mod was client enabled, make the new one client enabled too
                ModIdentifier mId = ModIdentifier.fromMod(mod);
                boolean clientEnabled = SinglePlayerModData.getInstance().isClientEnabled(mId);

                SMModLoader.deleteMod(mod);

                SinglePlayerModData.getInstance().setClientEnabled(newMod, clientEnabled);
            }
        }
    }

    /**
     * Check if any mods need an update. This will set the outOfDate flag for mods
     */
    public static void checkUpdateAll(){
        //Copy to a new array so it doesnt break if we load a mod while iterating through it
        ArrayList<ModSkeleton> mods = new ArrayList<>(StarLoader.starMods);
        for(ModSkeleton mod : mods) {
            SMDModInfo data = SMDModData.getInstance().getModData(mod.getSmdResourceId());
            if(data != null) {
                int resDate = data.getResourceDate();
                int installedResDate = SinglePlayerModData.getInstance().getDownloadDate(ModIdentifier.fromMod(mod));
                DebugFile.log(installedResDate + " [Installed] ==> [Remote] " + resDate + ", " + mod.getDebugName());
                if(installedResDate == -1) DebugFile.log("Mod: " + mod.getDebugName() + " was installed manually, not updating");
                else if(installedResDate < resDate) {
                    DebugFile.log("Mod: " + mod.getDebugName() + " out of date. flagging out of date");
                    mod.setOutOfDate(true);

                    //this mod is out of date
//                    try {
//                        downloadAndLoadMod(mod.getSmdResourceId(), mod);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        DebugFile.err("An error occurred while downloading mod " + mod.getName());
//                    }
                } else if (resDate < installedResDate){
                    DebugFile.log(mod.getDebugName() + "] Mod is a newer version on client compared to SMD.");
                    DebugFile.log(installedResDate + " [Installed] ==> [Remote] " + resDate);
                } else DebugFile.log("Mod up to date");
            } else if(!(mod.getRealMod() instanceof ModPlayground)) DebugFile.warn("Mod: " + mod.getDebugName() + " was not found on SMD. resId: " + mod.getSmdResourceId());
        }

        /*
        if(StarLoaderConfig.getConfig().getBoolean("replace-outdated-mods")) for(ModSkeleton starMod : removeQueue) SMModLoader.deleteMod(starMod);
        else for(ModSkeleton starMod : removeQueue) StarLoader.starMods.remove(starMod);
         */
        for(ModSkeleton starMod : removeQueue) StarLoader.starMods.remove(starMod);
        removeQueue.clear();
    }
}
