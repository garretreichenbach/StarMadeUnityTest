package api;

import api.common.GameClient;
import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.draw.debugging.DebugDrawer;
import api.utils.game.chat.commands.*;
import api.utils.registry.UniversalRegistry;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.network.RegisteredClientOnServer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;

public class ModPlayground extends StarMod {
    public static void main(String[] args) {

    }

    private void registerCommands() {
        StarLoader.registerCommand(new CommandListCommand());
        StarLoader.registerCommand(new HelpCommand());
        StarLoader.registerCommand(new ListModsCommand());
        StarLoader.registerCommand(new ConfigCommand());

        //For Crew development debugging only, remove in release
        StarLoader.registerCommand(new CrewCommand());
        //
    }

    public static ModPlayground inst;
    public ModPlayground(){
        inst = this;
    }

    @Override
    public void onBlockConfigLoad(BlockConfig config) {
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        if(ServerConfig.BLUEPRINTS_USE_COMPONENTS.isOn()) {
            BluePrintController bpct = BluePrintController.active;
            try { //Todo: Find a better place to put this
                for(BlueprintEntry blueprint : bpct.readBluePrints()) blueprint.countWithChilds = blueprint.getElementCountMapWithChilds().calculateComponents();
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void broadcastMessage(String message) {
        broadcastMessage(message, true);
    }

    public static void broadcastMessage(String message, boolean prefixWithState) {
        StringBuilder msgBuilder = new StringBuilder();
        if(GameServerState.instance == null) {
            if(prefixWithState){
                msgBuilder.append("[c] ");
            }
            msgBuilder.append(message);
            GameClient.sendMessage(msgBuilder.toString());
        } else {
            if(prefixWithState){
                msgBuilder.append("[s] ");
            }
            msgBuilder.append(message);
            for(RegisteredClientOnServer client : GameServerState.instance.getClients().values()) {
                try {
                    client.serverMessage(msgBuilder.toString());
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static Random rand = new Random();
    public static int randInt(int min, int max){
        return min+rand.nextInt(max-min);
    }
    public static float randFloat(float min, float max){
        return min+rand.nextFloat()*(max-min);
    }
    public static double nextGaussian(){
        return rand.nextGaussian();
    }

    private static void giveMetaObjectToPlayer(PlayerState player, MetaObject meta){
        try {

            int var8 = player.getInventory().getFreeSlot();
            player.getInventory().put(var8, meta);
            player.sendInventoryModification(var8, Long.MIN_VALUE);
        } catch (NoSlotFreeException var6) {
            var6.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        //Debug moving fast, very useful
        /*StarLoader.registerListener(KeyPressEvent.class, new Listener<KeyPressEvent>() {
            @Override
            public void onEvent(KeyPressEvent event) {
                if(event.getChar() == 'j'){
                    PlayerCharacter player = GameClientState.instance.getPlayer().getAssingedPlayerCharacter();
                    modifyField(PlayerCharacter.class, "speed", player, 310);
                }else if(event.getChar() == 'l'){
                    PlayerCharacter player = GameClientState.instance.getPlayer().getAssingedPlayerCharacter();
                    modifyField(PlayerCharacter.class, "speed", player, 4);
                }
            }
        }, this);*/

    }
    public static void modifyField(Class<?> cla, String field, Object instance, Object value){
        try {
            Field f = cla.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
//        ModGUIHandler.registerNewInputDialog(this.getSkeleton(), new MyInputDialog());

//        FastListenerCommon.gameMapListeners.add(new MyGameMapListener());
//        FastListenerCommon.getSegmentDrawListeners().add(new SegmentDrawListener(){
//
//            @Override
//            public void preDrawSegment(DrawableRemoteSegment segment) {
//            }
//
//            @Override
//            public void postDrawSegment(DrawableRemoteSegment segment) {
//                GlUtil.glBegin(GL11.GL_LINES);
//
//                GlUtil.glColor4f(1,0,0,1);
//                for (float i = 0; i < Math.PI * 2; i+=0.01) {
//                    float x = (float) (Math.sin(i)* 10);
//                    float y = 80;
//                    float z = (float) (Math.cos(i)* 10);
//                    GL11.glVertex3f(x,y,z);
//                }
//                GlUtil.glEnd();
//            }
//        });
        DebugFile.log("Enabling default mod...");
        DebugFile.log("Registering commands...");
        registerCommands();
        DebugFile.log("Registering packets...");
        StarLoaderHooks.registerAllPackets();
        UniversalRegistry.dumpRegistry();


        StarLoader.registerListener(RegisterWorldDrawersEvent.class, new Listener<>() {
	        @Override
	        public void onEvent(RegisterWorldDrawersEvent event) {
                event.getModDrawables().add(new DebugDrawer());
	        }
        }, inst);


        /*StarLoader.registerListener(MetaObjectPreInstantiateEvent.class, new Listener<MetaObjectPreInstantiateEvent>() {
            @Override
            public void onEvent(MetaObjectPreInstantiateEvent event) {
                if(event.getMetaId() == CustomMetaObjectRegistry.getId("BruhGun")){
                    ModPlayground.broadcastMessage("SWAPPED!!!");
                    event.injectMetaObject(new BruhGun(event.getSubId()));
                }
            }
        });
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if(event.getMessage().text.contains("sniper")){
                    MetaObject bruhGun = MetaObjectManager.instantiate(CustomMetaObjectRegistry.getId("BruhGun"), (short) -1, event.isServer());
                    giveMetaObjectToPlayer(GameCommon.getPlayerFromName(event.getMessage().sender), bruhGun);
                }
            }
        });*/
//        StarLoader.registerListener(PulseAddEvent.class, new Listener<PulseAddEvent>() {
//            @Override
//            public void onEvent(PulseAddEvent event) {
//                Pulse pulse = event.getPulse();
//                MyPulse p = new MyPulse(pulse.getState(),
//                        pulse.getWorldTransform(),
//                        ((SimpleTransformableSendableObject) pulse.getOwner()),
//                        10F,
//                        700,
//                        new Vector4f(0, 0.3F, 1, 0.6F));
//                event.setPulse(p);
//            }
//        });
    }
}

//class MyPulse extends Pulse {
//    //Transform location, Vector3f dir, SegmentController owner, float force, float radius, long weaponId, Vector4f pulseColor
//    float r;
//    int ran = 0;
//    int originalSectorId;
//    Transform originalLocation;
//
//    public MyPulse(StateInterface anInterface, Transform transform, SimpleTransformableSendableObject object, float force, float rad, Vector4f color) {
//        super(anInterface, (byte) 1, transform, object, new Vector3f(0, 5, 0), object, force, rad, object.getSectorId(), -1, color);
//        r = rad;
//        originalLocation = new Transform(transform);
//        originalSectorId = object.getSectorId();
//        collapseProgress = r - 100;
//    }
//
//    @Override
//    public void draw(Mesh mesh) {
//        super.draw(mesh);
//    }
//
//    float collapseProgress;
//
//    @Override
//    public void update(Timer timer) {
//        if (this.isActive()) {
//            super.update(timer);
//            ran += 4;
//            this.currentRadius = Math.min(ran, r - 200);
//            this.getInitialTransform().set(originalLocation);
//            final GameServerState serv = GameServer.getServerState();
//            if (serv != null) {
//                Sector sector = GameServer.getUniverse().getSector(originalSectorId);
//                System.err.println("===== MISSILES ======");
//                ArrayList<Missile> updatedMissiles = new ArrayList<>();
//                ShortOpenHashSet missiles = sector.getMissiles();
//                for (Short mId : missiles) {
//                    Missile m = serv.getController().getMissileManager().getMissiles().get(mId);
//                    if (m != null) {
//                        Vector3f missileLoc = new Vector3f(m.getWorldTransform().origin);
//                        missileLoc.sub(this.originalLocation.origin);
//                        float distanceToCenterSquared = missileLoc.lengthSquared();
//                        if (distanceToCenterSquared <= currentRadius * currentRadius) {
//                            updatedMissiles.add(m);
//                            System.err.println(m.getId());
//                        }
//                    }
//                    //Speed, damage, distance
//                }
//                int SPECIAL_MISSILE_ID = -123;
//                for (final Missile m : updatedMissiles) {
//                    if (m.getWeaponId() != SPECIAL_MISSILE_ID) {
//                        m.setDistance(0);
//                        PlayerState p = null;
//                        if(m.getShootingEntity() instanceof SegmentController){
//                            ArrayList<PlayerState> players = SegmentControllerUtils.getAttachedPlayers((SegmentController) m.getShootingEntity());
//                            if(!players.isEmpty()){
//                                p = players.get(0);
//                            }
//                        }
//                        if(p != null) {
//                            Missile spawned = serv.getController().getMissileController().addDumbMissile(
//                                    /*m.getOwner()*/ p,
//                                    m.getWorldTransform(), m.getLinearVelocity(new Vector3f()), m.getSpeed(), m.getDamage()/2F, 3000, SPECIAL_MISSILE_ID, ((short) 6));
//                        }
//                    }
//                }
//                System.err.println("===== CANNON SHOTS =====");
//                ProjectileController particleController = sector.getParticleController();
//                ProjectileParticleContainer c = particleController.getParticles();
//                int[] projectiles = SectorUtils.getCannonProjectiles(sector);
//                for (int i = 0; i < particleController.getParticleCount(); i++) {
//                    c.setDamage(i, c.getDamage(i)*0.9F);
//                }
////                for (int projectile : projectiles) {
////                    particleController.deleteParticle(projectile);
////                }
//                System.err.println("COUNT::: " + particleController.getParticleCount());
//            }
//
//            if (ran > 6000) {
//                this.currentRadius = collapseProgress -= 10;
//                if (collapseProgress <= 1) {
//                    this.setActive(false);
//                }
//            }
//        }
//    }
//}
