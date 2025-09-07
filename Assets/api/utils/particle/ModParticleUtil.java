package api.utils.particle;

import api.DebugFile;
import api.common.GameClient;
import api.common.GameServer;
import api.mod.StarMod;
import api.utils.textures.StarLoaderTexture;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.psys.modules.RendererModule;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Jake on 12/4/2020.
 * <insert description here>
 */
public class ModParticleUtil {
    private static final ArrayList<BufferedImage> modSprites = new ArrayList<>();

    static int registerParticleSprite(BufferedImage image) {
        modSprites.add(image);
        return modSprites.size() - 1;
    }

    static ArrayList<Vector2f[]> pointMap = new ArrayList<>();
    static Sprite mainSprite;

    static void postRegisterParticles() {
        DebugFile.log("[StarLoader] Post register particles...");
        if (!modSprites.isEmpty()) {
            pointMap.clear();
            DebugFile.log("[StarLoader] Merging particle textures...");
//            BufferedImage mainTexture = null;
//            try {
//                mainTexture = ImageIO.read(new File("test.png"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            final BufferedImage mainTexture = TextureSheetMergeAlgorithm.mergeTextures(modSprites, pointMap);

            try {
                File debugFile = new File("moddata/StarLoader/debug/MainParticleTexture.png");
                boolean mkdir = debugFile.getParentFile().mkdir();
                ImageIO.write(mainTexture, "png", debugFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            modSprites.clear();
            DebugFile.log("[StarLoader] Creating sprite on graphics thread");
            StarLoaderTexture.runOnGraphicsThread(() -> {
                synchronized (ModParticleUtil.class) {
                    mainSprite = StarLoaderTexture.newSprite(mainTexture, "starloader_mainparticletexture", true, true);
                    mainSprite.setPositionCenter(false);
                }
            });
        } else {
            DebugFile.log("[StarLoader] Nothing to do");
        }
    }

    private static final ModParticleVertexBuffer vertexBuffer = new ModParticleVertexBuffer();

    //TODO Better data structure for particles, ArrayDeque but sortable probably
    private static final ArrayList<ModParticle> particles = new ArrayList<>();
    private static final ConcurrentLinkedQueue<ModParticle> particleAddQueue = new ConcurrentLinkedQueue<>();

    /**
     * Gets a random vector to a point on a sphere of radius rad
     */
    public static Vector3f getRandomDir(float rad) {
        Vector3f vector3f = new Vector3f(TextureSheetMergeAlgorithm.nextGaussian(),
                TextureSheetMergeAlgorithm.nextGaussian(),
                TextureSheetMergeAlgorithm.nextGaussian());
        vector3f.normalize();
        vector3f.scale(rad);
        return vector3f;
    }
    public static ArrayList<PlayerState> getPlayersInRange(int sectorId) {
        return getPlayersInRange(GameServerState.instance.getUniverse().getSector(sectorId).pos);
    }
    public static ArrayList<PlayerState> getPlayersInRange(Vector3i sec) {
        ArrayList<PlayerState> r = new ArrayList<>();
        for (PlayerState value : GameServer.getServerState().getPlayerStatesByName().values()) {
            Vector3i currentSector = new Vector3i(value.getCurrentSector());
            currentSector.sub(sec);
            if (currentSector.lengthSquared() <= 4) {
                r.add(value);
            }
        }
        return r;
    }

    private static final Vector3f zeroVector = new Vector3f();
    public static void playClientDirect(ModParticle particle){
        particleAddQueue.add(particle);
        particle.spawn();
    }

    public static void playClient(int sectorId, Vector3f loc, int sprite, ModParticle particle) {
        if (GameClient.getClientState() == null) {
            return;
        }
        //If the particle should be discarded or not by the client
        boolean inClientRange = GameClient.getClientState().getController().isNeighborToClientSector(sectorId) || GameClientState.instance.getCurrentSectorId() == sectorId;
        if (!inClientRange) return; //Cull particle, its too far away anyway.
        //Unpack builder
        ///
        long ms = System.currentTimeMillis();
        particle.sectorId = sectorId;
        particle.startTime = ms;

        Vector3f v = calcSectorRelativePosition(sectorId, loc);
        particle.position.set(v);

        particle.updateCameraDistance();
        particle.spawn();
        particle.particleSpriteId = sprite;
        particleAddQueue.add(particle);
    }
    public static Vector3f calcSectorRelativePosition(int toSector, Vector3f inSectorPos) {
        int clientSector = GameClientState.instance.getCurrentSectorId();
        Vector3i clientSectorPos = GameClientState.instance.getCurrentRemoteSector().clientPos();
        if (clientSector == toSector) {
            return inSectorPos;
        } else {
            RemoteSector r = (RemoteSector) GameClientState.instance.getLocalAndRemoteObjectContainer()
                    .getLocalObjects().get(toSector);

            Vector3i relSectorPos = new Vector3i();
            relSectorPos.sub(r.clientPos(), clientSectorPos);
            Vector3f absSectorPos = new Vector3f();
            absSectorPos.set(
                    relSectorPos.x * GameClientState.instance.getSectorSize(),
                    relSectorPos.y * GameClientState.instance.getSectorSize(),
                    relSectorPos.z * GameClientState.instance.getSectorSize());

            absSectorPos.add(inSectorPos);
            return absSectorPos;
        }
    }

    public static void drawAll() {
        long time = System.currentTimeMillis();
        particles.addAll(particleAddQueue);
        particleAddQueue.clear();

        if (particles.isEmpty()) {
            return;
        }

        beginDraw();

        Iterator<ModParticle> iterator = particles.iterator();

        while (iterator.hasNext()) {
            ModParticle particle = iterator.next();
            particle.update(time);
            particle.position.add(particle.velocity);
            particle.ticksLived++;
            particle.updateCameraDistance();
            if (time > particle.startTime + particle.lifetimeMs) {
                particle.die();
                iterator.remove();
            }
        }


        Collections.sort(particles);
        vertexBuffer.draw(particles, RendererModule.FrustumCullingMethod.NONE);
        endDraw();
    }

    private static void beginDraw() {
        GlUtil.glDisable(GL11.GL_LIGHTING);
        GlUtil.glEnable(GL11.GL_BLEND);
        GlUtil.glEnable(GL11.GL_DEPTH_TEST);
        GlUtil.glDepthMask(false);
        GlUtil.glDisable(GL11.GL_CULL_FACE);
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, mainSprite.getMaterial().getTexture().getTextureId());
        GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
    }

    private static void endDraw() {
        GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlUtil.glDisable(GL11.GL_BLEND);
        GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
        GlUtil.glDisable(GL11.GL_DEPTH_TEST);
        GlUtil.glEnable(GL11.GL_CULL_FACE);
        GlUtil.glEnable(GL11.GL_LIGHTING);
    }

    public static class LoadEvent {
        public int addParticleSprite(BufferedImage image, StarMod mod) {
            return registerParticleSprite(image);
        }
    }
}
