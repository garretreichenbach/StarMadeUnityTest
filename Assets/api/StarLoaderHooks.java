package api;

import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.listener.events.draw.CubeTexturePostLoadEvent;
import api.listener.events.network.ClientLoginEvent;
import api.listener.events.network.ClientSendableAddEvent;
import api.listener.events.systems.ReactorRecalibrateEvent;
import api.listener.events.world.ServerSendableAddEvent;
import api.listener.fastevents.BlockConfigLoadListener;
import api.listener.fastevents.FastListenerCommon;
import api.mod.ModSkeleton;
import api.mod.ModStarter;
import api.mod.StarLoader;
import api.mod.config.PacketSCSyncConfig;
import api.mod.config.PersistentObjectUtil;
import api.network.packets.PacketUtil;
import api.network.packets.client.PacketCSAdminCommand;
import api.network.packets.server.PacketSendCommandsToClient;
import api.utils.addon.PacketSCSyncSimpleAddOn;
import api.utils.addon.SimpleAddOn;
import api.utils.game.module.ModManagerContainerModule;
import api.utils.game.module.PacketCSRequestMCModuleData;
import api.utils.game.module.PacketCSSendMCModuleData;
import api.utils.game.module.PacketSCSyncMCModule;
import api.utils.game.module.util.PacketCSSendSimpleDataMCModuleData;
import api.utils.other.HashList;
import api.utils.registry.PacketSCSyncUniversalRegistry;
import api.utils.registry.UniversalRegistry;
import api.utils.sound.PacketSCPlayAudio;
import api.utils.textures.GraphicsOperator;
import api.utils.textures.StarLoaderTexture;
import api.utils.textures.TextureSwapper;
import org.schema.common.util.StringTools;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.client.view.WorldDrawer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.texture.Texture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains hooks for when the mod loader itself needs to use the event system
 *
 * Its preferred to put event listeners here rather than when the event is fired or in ModPlayground
 * (For simplicity and to keep them organized)
 */
public class StarLoaderHooks {
    /**
     * StarLoader uses some packets to make modder's lives easier
     * They are registered in onEnable
     */
    public static void registerAllPackets(){
        PacketUtil.registerPacket(PacketSCSyncSimpleAddOn.class);
        PacketUtil.registerPacket(PacketSCPlayAudio.class);
        PacketUtil.registerPacket(PacketSendCommandsToClient.class);
        PacketUtil.registerPacket(PacketCSAdminCommand.class);
        PacketUtil.registerPacket(PacketSCSyncUniversalRegistry.class);
        PacketUtil.registerPacket(PacketSCSyncConfig.class);
        PacketUtil.registerPacket(PacketSCSyncMCModule.class);
        PacketUtil.registerPacket(PacketCSRequestMCModuleData.class);
        PacketUtil.registerPacket(PacketCSSendMCModuleData.class);
        PacketUtil.registerPacket(PacketCSSendSimpleDataMCModuleData.class);
    }

    /**
     * Block config load
     */
    public static void initBlockData() {

        //Call preload for mods
        for (BlockConfigLoadListener listener : FastListenerCommon.blockConfigLoadListeners) {
            listener.preBlockConfigLoad();
        }

        DebugFile.log("[BlockConfig]Initializing block data for enabled mods.");
        final BlockConfig config = new BlockConfig();
        ModStarter.setCurrentLoadStage(ModStarter.LoadStage.BLOCK_CONFIG);
        for (ModSkeleton mod : StarLoader.starMods) {
            if (mod.isLoaded()) {
                DebugFile.log("Initializing block for mod: " + mod.getName());
                try {
                    //Call individual preload
                    for (BlockConfigLoadListener listener : FastListenerCommon.blockConfigLoadListeners) {
                        listener.onModLoadBlockConfig_PRE(mod.getRealMod());
                    }
                    mod.getRealMod().onBlockConfigLoad(config);
                    //Call individual preload
                    for (BlockConfigLoadListener listener : FastListenerCommon.blockConfigLoadListeners) {
                        listener.onModLoadBlockConfig_POST(mod.getRealMod());
                    }
                }catch (Exception e) {
                    ModStarter.handleModLoadException(mod, e);
                }
            }
        }

        //Call postload for mods
        for (BlockConfigLoadListener listener : FastListenerCommon.blockConfigLoadListeners) {
            listener.postBlockConfigLoad();
        }

        // Write the UR again to save block ids
        UniversalRegistry.writeToFile(ModPlayground.inst);

        // Make the fake blocks
        BlockConfig.makeFakeBlocksForUnloadedMods();

        //Regenerate LOD shapes/Factory enhancers, rather than just obliterating the list in addElementToExisting
        for (Map.Entry<Short, ElementInformation> next : ElementKeyMap.getInformationKeyMap().entrySet()) {
            Short keyId = next.getKey();
            ElementKeyMap.lodShapeArray[keyId] = next.getValue().hasLod();
            ElementKeyMap.factoryInfoArray[keyId] = ElementKeyMap.getFactorykeyset().contains(keyId);
        }

        BlockConfig.printElementDebug();

        ModStarter.setCurrentLoadStage(ModStarter.LoadStage.DONE);
    }

    /**
     * SimpleAddOn's listen to reactor recalibrate very frequently, so its easier if every SAA is subscribed to it
     */
    public static void onReactorRecalibrateEvent(ReactorRecalibrateEvent event){
        SegmentController s = event.getImplementation().getSegmentController();
        if(s instanceof ManagedUsableSegmentController){
            ManagedUsableSegmentController<?> m = (ManagedUsableSegmentController<?>) s;
            for (PlayerUsableInterface addOn : m.getManagerContainer().getPlayerUsable()) {
                if(addOn instanceof SimpleAddOn){
                    ((SimpleAddOn) addOn).onReactorRecalibrate(event);
                }
            }
        }
    }
    public static void onPreEnableServer(ModSkeleton mod) {
        mod.getRealMod().onUniversalRegistryLoad();
    }

    public static void onModEnableCommon(ModSkeleton mod) {
        PersistentObjectUtil.onModEnable(mod);
    }

    //Draw unique colours behind texture sheets
    public static boolean debugTextureSheets = false;

    /**
     * When cube textures (for blocks) are loaded. Could be low or high res, normal maps or regular images
     * @param texArray
     * @param pack
     * @param res
     * @param custom
     */
    private static boolean loadedCubes = false;
    public static void onCubeTextureLoad(Texture[] texArray, String pack, int res, String custom){
        //Handle draw thread stuff
        WorldDrawer.processRunQueue();
        boolean loadingRegulars = texArray == GameResourceLoader.cubeTextures || texArray == GameResourceLoader.cubeTexturesLow;
        boolean loadingNormals = texArray == GameResourceLoader.cubeNormalTextures || texArray == GameResourceLoader.cubeNormalTexturesLow;
        boolean needToStitchTextures = !StarLoaderTexture.textures.isEmpty();
        if(needToStitchTextures) {
            if (loadingRegulars || loadingNormals) {
                if (!loadedCubes) {
                    swapAllTextures();
                }
                loadedCubes = true;
                DebugFile.log("Cube texture loaded, applying patches...");
                BufferedImage[] newImages = new BufferedImage[8];
                boolean[] modifiedTextures = new boolean[8];
                newImages[0] = TextureSwapper.getImageFromTexture(texArray[0]);
                newImages[1] = TextureSwapper.getImageFromTexture(texArray[1]);
                newImages[2] = TextureSwapper.getImageFromTexture(texArray[2]);
                int width = newImages[0].getWidth();
                int height = newImages[0].getHeight();
                for (int i = 3; i < 8; i++) {
                    newImages[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                }

                for (Map.Entry<Integer, StarLoaderTexture> integerStarLoaderTextureEntry : StarLoaderTexture.textures.entrySet()) {
                    int texId = integerStarLoaderTextureEntry.getKey();
                    System.err.println("Texture ID: " + texId);
                    StarLoaderTexture tex = integerStarLoaderTextureEntry.getValue();
                    TextureSwapper.setBlockTexture(width / TextureSwapper.TEXTURES_STORED_PER_ROW, newImages, texId, tex, loadingNormals, modifiedTextures);
                }

                for (int i = 0; i < newImages.length; i++) {
                    if(!modifiedTextures[i]) continue;

                    BufferedImage img = newImages[i];
                    Texture textureSheet = texArray[i];
                    System.err.println("[StarLoaderHooks] Remaking texture sheet" + i);
                    if (textureSheet != null) {
                        String name = textureSheet.getName();
                        Texture tex = TextureSwapper.getTextureFromImage(img, name, false, true);
                        texArray[i] = tex;
                    } else {
//                    Texture array not present on file (?)
                        texArray[i] = TextureSwapper.getTextureFromImage(img, "", false, true);
                    }
                }
            }
        }else{
            System.err.println("[StarLoader] [StarLoaderHooks] [onCubeTextureLoad] Not stitching textures - No block textures present");
        }
        CubeTexturePostLoadEvent event = new CubeTexturePostLoadEvent(texArray, pack, res, custom);
        StarLoader.fireEvent(event, false);
    }

    public static Texture onOverlayTextureLoad(Texture texture, String pack, int res) {
        if(StarLoaderTexture.iconTextures.isEmpty() && StarLoaderTexture.overlayTextures.isEmpty()){
            System.err.println("[StarLoader] [StarLoaderHooks] [onOverlayTextureLoad] Not stitching overlay textures - No iconTextures or overlayTextures present");
            return texture;
        }
        BufferedImage img = TextureSwapper.getImageFromTexture(texture);
        if(img == null){
            System.err.println("[StarLoader] [StarLoaderHooks] [onOverlayTextureLoad] Provided image is null");
            return null;
        }
        int width = texture.getWidth();

        //Draw the first 2 rows, this is done to remove the solid black pixels on the overlay texture
        BufferedImage newOverlayImage = new BufferedImage(res*16, res*16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newOverlayImage.getGraphics();
        g.drawImage(img.getSubimage(0,0,res * 16,res * 2), 0, 0, null);
        g.dispose();

        for (Map.Entry<Integer, StarLoaderTexture> integerStarLoaderTextureEntry : StarLoaderTexture.overlayTextures.entrySet()) {
            int texId = integerStarLoaderTextureEntry.getKey();
            StarLoaderTexture tex = integerStarLoaderTextureEntry.getValue();
            System.err.println( width/TextureSwapper.TEXTURES_STORED_PER_ROW + "< res >" + res);
            TextureSwapper.setOverlayTexture(res, newOverlayImage, texId, tex);
        }
        Texture newTexture = TextureSwapper.getTextureFromImage(newOverlayImage, texture.getName(), true, true);

        boolean debugDrawOverlays = false;
        if(debugDrawOverlays){
            try {
                ImageIO.write(newOverlayImage, "png", new File("overlaysMod.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        loadIconTextures();
//        return texture;
        return newTexture;
    }
    public static void loadIconTextures(){
        System.err.println("Loading custom block icon textures");
        HashMap<Integer, BufferedImage> images = new HashMap<>();
        for (Map.Entry<Integer, StarLoaderTexture> textureEntry : StarLoaderTexture.iconTextures.entrySet()) {
            int texId = textureEntry.getKey();
            StarLoaderTexture tex = textureEntry.getValue();
            System.err.println("Texid: " + texId);
            int sheet = texId/(16*16);
            BufferedImage bufferedImage = images.get(sheet);
            if(bufferedImage == null){
                images.put(sheet, new BufferedImage(64*16,64*16, BufferedImage.TYPE_INT_ARGB));
                System.err.println("Created new buffered image");
                bufferedImage = images.get(sheet);
            }
            System.err.println("Set icon");
            TextureSwapper.setIconTexture(bufferedImage, texId, tex);
        }


        for (Map.Entry<Integer, BufferedImage> integerBufferedImageEntry : images.entrySet()) {
            int sheet = integerBufferedImageEntry.getKey();

            BufferedImage tex = integerBufferedImageEntry.getValue();
            String spriteName = "build-icons-" + StringTools.formatTwoZero(sheet) + "-16x16-gui-";
            Sprite sprite = StarLoaderTexture.newSprite(tex, spriteName);
            sprite.setMultiSpriteMax(16,16);
            sprite.setWidth(64);
            sprite.setHeight(64);
            sprite.setPositionCenter(false);
            sprite.onInit();
        }
    }


    private static void swapAllTextures(){
        HashList<Sprite, GraphicsOperator> opMap = StarLoaderTexture.getTextureOperationMap();
        for (Sprite sprite : opMap.keySet()) {
            ArrayList<GraphicsOperator> operators = opMap.get(sprite);
            try {
                //Create image from file
                BufferedImage img = TextureSwapper.getImageFromSprite(sprite);
                Graphics g = img.getGraphics();
                //Apply operators to buffered image
                for (GraphicsOperator operator : operators) {
                    operator.apply(img, g);
                }
                //Swap sprite with new texture
                TextureSwapper.swapSpriteTexture(sprite, img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static boolean secondEnable = false;
    public static void onPostModsEnable(){
        // TODO This is a fix for a bug where the texture pack needs to be reloaded for
        //  block textures to be re-applied. Need to figure out why leaving causes them to vanish in the first place
//        if(secondEnable){
//            System.err.println("[StarLoaderHooks] Reloading texture pack because 2nd join");
//            Controller.getResLoader().enqueueWithResetForced(GameResourceLoader.getBlockTextureResourceLoadEntry());
//        }
//        secondEnable = true;

    }

    /**
     * Pass important lifecycle events to all mods
     */
    public static void onServerInitialize(ServerInitializeEvent event){
        for (ModSkeleton mod : StarLoader.starMods) {
            if(mod.isEnabled()){
                mod.getRealMod().onServerCreated(event);
            }
        }
    }
    public static void onClientInitialize(ClientInitializeEvent event){
        for (ModSkeleton mod : StarLoader.starMods) {
            if(mod.isEnabled()){
                mod.getRealMod().onClientCreated(event);
            }
        }
    }

    public static void onClientLoginEvent(ClientLoginEvent event) {
        PacketUtil.sendPacket(event.getServerProcessor(), new PacketSCSyncUniversalRegistry());
        PacketUtil.sendPacket(event.getServerProcessor(), new PacketSendCommandsToClient());
    }
    public static void onServerSendableAddEvent(ServerSendableAddEvent event){

    }


    public static void onClientSendableAddEvent(ClientSendableAddEvent event){
        //Request sync of all mod modules
        if(event.getSendable() instanceof ManagedUsableSegmentController<?>){
            ManagerContainer<?> container = ((ManagedUsableSegmentController<?>) event.getSendable()).getManagerContainer();
            for (Map.Entry<Short, ModManagerContainerModule> entry : container.getModModuleMap().entrySet()) {
                short id = entry.getKey();
                ModManagerContainerModule module = entry.getValue();
                PacketCSRequestMCModuleData packet = new PacketCSRequestMCModuleData(container, module);
                PacketUtil.sendPacketToServer(packet);
            }
        }
    }
}
