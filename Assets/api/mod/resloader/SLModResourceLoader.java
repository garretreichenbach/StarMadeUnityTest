package api.mod.resloader;

import api.mod.ModSkeleton;
import api.mod.ModStarter;
import api.mod.StarLoader;
import api.utils.particle.ModParticleUtil;
import api.utils.particle.ModParticleVertexBuffer;
import org.schema.game.client.view.gui.LoadingScreenDetailed;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GraphicsContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Jake on 3/3/2021.
 * Handles mod resource loading.
 *
 * Stuff like textures and models need to be registered on the main graphics thread,
 * however, we cannot use the GameResourceLoader because it does not guarantee block textures are loaded before block config.
 * (Among some other oddities)
 *
 * Mod resource loading will lock both the graphics thread, AND will freeze the server until it has been loaded (To avoid heavy load, loading the block config before)
 *
 * Q: Why not just reload the block config as the last entry in GameResourceLoader?
 * A: It was causing issues with other classes deriving arrays with ElementKeyMap.highestType, which was incorrect since mod resources where not loaded.
 */
public class SLModResourceLoader {
    /**
     * Trigger mods to load their resources
     */
    public static void loadResourcesLoad(){
        if(!GraphicsContext.isInitialized()){ //TODO Ithirahad: @JakeV There was a variable called "initialized" before, and that's gone now. I have no clue if this method is an appropriate substitute; I just got rid of the compiler error.
            System.err.println("[SLModResourceLoader] Texture/Models do not need to be loaded on server, skipping.");
            return;
        }
        System.err.println("[SLModResourceLoader] Locking main thread until graphics thread loads mod resources");
        loadingResources.set(true);
        long ran = 0;
        while (loadingResources.get()){
            ran++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.err.println("[SLModResourceLoader] Unlocked server thread from: " + ran + " iterations");
    }
    public static void handleMainGraphicsLoop(){
        if(loadingResources.get()){
            loadParticle();
            loadResources();
            loadingResources.set(false);
        }
    }
    private static final AtomicBoolean loadingResources = new AtomicBoolean(false);

    private static void loadParticle(){
        ModStarter.setCurrentLoadStage(ModStarter.LoadStage.PARTICLE_LOADING);
        //TODO Mod sorting
        for (ModSkeleton mod : StarLoader.starMods) {
            if(mod.isEnabled()) {
                try {
                    LoadingScreenDetailed.modSecondaryStatus = mod.getDebugName();
                    mod.getRealMod().onLoadModParticles(new ModParticleUtil.LoadEvent());
                } catch (Exception e) {
                    if (ModStarter.handleModLoadException(mod, e)) {
                        System.exit(1);
                        return;
                    }
                }
            }
        }
        LoadingScreenDetailed.modMainStatus = "Building ModParticle systems...";
        ModParticleVertexBuffer.postRegisterParticles();
        LoadingScreenDetailed.modMainStatus = "Done";
    }
    private static void loadResources(){
        ModStarter.setCurrentLoadStage(ModStarter.LoadStage.GAME_RESOURCE_LOADER_LOAD);
        //TODO Mod sorting
        System.err.println("[StarLoader] Mod resource loading");
        for (ModSkeleton mod : StarLoader.starMods) {
            if(mod.isEnabled()) {
                System.err.println("[StarLoader] Mod resource loading for: " + mod.getDebugName());
                try {
                    LoadingScreenDetailed.modSecondaryStatus = mod.getDebugName();
                    mod.getRealMod().onResourceLoad(Controller.getResLoader());
                } catch (Exception e) {
                    if (ModStarter.handleModLoadException(mod, e)) {
                        System.exit(1);
                        return;
                    }
                }
            }
        }
        LoadingScreenDetailed.modMainStatus = "Done";
    }
}
