package org.schema.game.common.controller.elements.gasMiner;

import api.common.GameCommon;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.config.ConfigurationElement;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ControlElementMapper;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.ShootContainer;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.simulation.resource.PassiveResourceManager;
import org.schema.game.server.data.simulation.resource.PassiveResourceProvider;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.*;

import static api.element.block.Blocks.GAS_HARVEST_MODULE;
import static api.mod.ModStarter.justStartedServer;
import static org.schema.game.common.data.element.ElementKeyMap.STASH_ELEMENT;
import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.DefaultResourceProviderTypes.*;
import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.DefaultResourceProviderTypes.ATMOSPHERE;
import static org.schema.game.server.data.simulation.resource.PassiveResourceProvider.DefaultResourceProviderTypes.NEBULA;
import static org.schema.schine.network.server.ServerMessage.MESSAGE_TYPE_INFO;

public class GasHarvesterElementManager extends UsableControllableFiringElementManager<GasHarvesterUnit,GasHarvesterCollectionManager,GasHarvesterElementManager> implements BlockActivationListenerInterface, IntegrityBasedInterface {
    @ConfigurationElement(name="ReactorPowerConsumptionResting")
    public static float REACTOR_POWER_CONSUMPTION_RESTING = 15f;
    @ConfigurationElement(name="ReactorPowerConsumptionCharging")
    public static float REACTOR_POWER_CONSUMPTION_CHARGING = 40f;
    @ConfigurationElement(name="HarvestSampleRatePerSecond")
    public static float HARVEST_SAMPLE_RATE_PER_SECOND = 10f;
    @ConfigurationElement(name="BaseHarvestQuantityPerSamplePerBlock")
    public static float GAS_ITEM_HARVEST_PER_BLOCK = 1f;
    @ConfigurationElement(name="MaxHarvestMultiplierFromSpeed")
    public static float HARVEST_MAX_BONUS_FROM_SPEED = 9f;
    @ConfigurationElement(name="HarvestCycleTimeSeconds")
    public static float HARVESTER_CYCLE_TIME_SECONDS = 15.0f;
    private final ShootContainer shootContainer = new ShootContainer();
    private static final boolean debug = false;

    protected static final ShortOpenHashSet harvestableTypes = new ShortOpenHashSet(new short[]{
            STAR.getProviderTypeId(),
            NEBULA.getProviderTypeId(),
            ATMOSPHERE.getProviderTypeId()
    });

    List<ResourceRoutingQueueEntry> resourceRoutingQueue = new LinkedList<>();

    public GasHarvesterElementManager(SegmentController segmentController) {
        super(ElementKeyMap.GAS_SCOOP_CONTROLLER, ElementKeyMap.GAS_SCOOP_MODULE, segmentController);
    }

    public void claimResources(){
        for (Iterator<ResourceRoutingQueueEntry> iterator = resourceRoutingQueue.iterator(); iterator.hasNext();) {
            ResourceRoutingQueueEntry m = iterator.next();
            float chamberBonus = getConfigManager().apply(StatusEffectType.GAS_HARVEST_BONUS_ACTIVE,1.0f);

            int[][] quantities = m.source.claimActiveResources(m.harvestPower,m.destination.getCapacity() - m.destination.getVolume());
            for(int[] col : quantities) {
                short type = (short) col[0];
                int quantity = col[1];
                float environmentalBonus = PassiveResourceProvider.getSituationMiningBonus(getSegmentController().getUniqueIdentifier(),type,harvestableTypes,getSegmentController().getFactionId(),getSegmentController().getSector(new Vector3i()));

                        quantity *= environmentalBonus;
                int mod = m.destination.incExistingOrNextFreeSlotWithoutException(type, (int) Math.floor(quantity * chamberBonus));
                m.destination.sendInventoryModification(mod);
                iterator.remove();
            }
        }
    }

    public void doShot(GasHarvesterUnit c, GasHarvesterCollectionManager m,
                       ShootContainer shootContainer, PlayerState playerState, Timer timer) {
        if (c.canUse(timer.currentTime, false)) {
            Set<PlayerState> players = new HashSet<>(getAttachedPlayers());
            if(playerState != null) players.add(playerState); //idk if in-core firer is considered an attached player

            Inventory inv = null;
            long computerBlockIndex = m.getControllerIndex();
            Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> linkedBlocks = new Short2ObjectOpenHashMap<>();
            ControlElementMapper cma = getSegmentController().getControlElementMap().getControllingMap();
            if(cma.containsKey(computerBlockIndex)) linkedBlocks.putAll(cma.get(computerBlockIndex));

            if(linkedBlocks.containsKey(STASH_ELEMENT)){
                LongIterator inventoryIndices = linkedBlocks.get(STASH_ELEMENT).iterator();
                while(inventoryIndices.hasNext() && inv == null) {
                    inv = getManagerContainer().getInventory(ElementCollection.getPosIndexFrom4(inventoryIndices.nextLong()));
                    if(inv != null && inv.isOverCapacity()) inv = null;
                }
            }
            if(inv == null && playerState != null && !playerState.getInventory().isOverCapacity())
                inv = playerState.getInventory();
            //if no inventories with space are available, inv will remain null and no further code executes here.

            if (inv != null &&  (isUsingPowerReactors() || consumePower(c.getPowerConsumption() ))) {
                Transform t = new Transform();
                t.setIdentity();
                t.origin.set(shootContainer.weapontOutputWorldPos);

                c.setShotReloading((long) (HARVESTER_CYCLE_TIME_SECONDS * 1000));

                Vector3f dir = new Vector3f(shootContainer.shootingDirTemp);
                dir.normalize();
                long weaponId = m.getUsableId();

                if(GameClientState.instance != null);
                if(!GameCommon.isClientConnectedToServer()) startHarvestCycle(c, inv, dir, t, weaponId, m.getColor(), players);

                handleResponse(ShootingRespose.FIRED, c, shootContainer.weapontOutputWorldPos);
            } else if (inv == null) {
                for (PlayerState player : players)
                    player.sendServerMessage(Lng.astr("No inventory to deposit harvested gases!"), MESSAGE_TYPE_INFO);
            } else {
                handleResponse(ShootingRespose.NO_POWER, c, shootContainer.weapontOutputWorldPos);
            }
        } else {
            handleResponse(ShootingRespose.RELOADING, c, shootContainer.weapontOutputWorldPos);
        }
    }

    private void startHarvestCycle(final GasHarvesterUnit c, final Inventory targetInv, final Vector3f dir, Transform t, long weaponId, Vector4f color, final Collection<PlayerState> players) {
        final SegmentController seg = getSegmentController();
        Vector3i sector = seg.getSector(new Vector3i());

        Vector3f pos = seg.getWorldTransform().origin;
        pos.add(c.getOutput().toVector3f());


        if(justStartedServer){
            LinkedList<PassiveResourceProvider> wells = new LinkedList<>();

            List<PassiveResourceProvider> allSources = PassiveResourceManager.getProvidersAt(seg.getSector(new Vector3i()));
            //PassiveResourceProvider[] allSources = ResourcesReSourced.container.getPassiveSources(sector);
            short typeId;
            for(PassiveResourceProvider p : allSources) {
                typeId = p.getSourceTypeInfo().getProviderTypeId();
                if (typeId == ATMOSPHERE.getProviderTypeId() || typeId == NEBULA.getProviderTypeId()){
                    wells.add(p); //we can harvest from this; add it
                }
            }

            if(wells.isEmpty()) for (PlayerState player : players)
                player.sendServerMessage(Lng.astr("No gaseous source in sector!\r\n Fly into a planet sector where the planet's atmosphere has available resources, or a star system where the star has available resources."), MESSAGE_TYPE_INFO); //TODO: or nebula
            else for(final PassiveResourceProvider well : wells) {
                new RealTimeDelayedAction((long) (1/HARVEST_SAMPLE_RATE_PER_SECOND) * 1000, -1, false){
                    float sampledHarvestPower = 0;
                    int passes = 0;
                    final long endTime = System.currentTimeMillis() + (long) (((double) HARVESTER_CYCLE_TIME_SECONDS) * 1000);
                    @Override
                    public void doAction() {
                        sampledHarvestPower += c.getHarvestPower();
                        passes++;

                        if(c.getPowered() <= 0.95f){
                            for (PlayerState player : players)
                                player.sendServerMessage(Lng.astr("Harvester is not powered!\r\n Harvested gas has been vented to avoid catastrophic containment failure."), MESSAGE_TYPE_INFO);
                            this.cancel();
                        }
                        else {
                            /*
                            RamscoopEffect.FireHarvestEffectServer(seg.getSectorId(), seg.getId(), seg.getSector(new Vector3i()),c.getOutput().toVector3f(),dir,seg.getLinearVelocity(new Vector3f()), HARVEST_SAMPLE_RATE_MS, c.getStaticHarvestPower(), getResourceColor(well));
                            if (System.currentTimeMillis() >= endTime) {
                                sampledHarvestPower = sampledHarvestPower / passes; //average
                                resourceRoutingQueue.add(new ResourceRoutingQueueEntry(well, sampledHarvestPower, targetInv));
                                claimResources(); //I know this makes the queue redundant, but I figured I'd use a separate method in case it turns out to be async unsafe
                                this.cancel();
                            }
                             */
                        }
                    }
                }.startCountdown();
            }
        }
    }

    @Override
    public int onActivate(SegmentPiece piece, boolean oldActive, boolean active) {
        long absPos = piece.getAbsoluteIndex();
        for (int i = 0; i < getCollectionManagers().size(); i++) {
            for (GasHarvesterUnit d : getCollectionManagers().get(i).getElementCollections()) {
                if (d.contains(absPos)) {
                    d.setMainPiece(piece, active);

                    return active ? 1 : 0;
                }
            }
        }
        return active ? 1 : 0;
    }

    @Override
    public void updateActivationTypes(ShortOpenHashSet typesThatNeedActivation) {
        typesThatNeedActivation.add(GAS_HARVEST_MODULE.getId());
    }

    @Override
    protected String getTag() {
        return "gasharvester";
    }

    @Override
    public GasHarvesterCollectionManager getNewCollectionManager(SegmentPiece block, Class<GasHarvesterCollectionManager> clazz) {
        return new GasHarvesterCollectionManager(block, getSegmentController(), this);
    }

    @Override
    public String getManagerName() {
        return "Gas Harvester System Collective";
    }

    @Override
    public void handle(ControllerStateInterface unit, Timer timer) {
        //		if(!getSegmentController().isOnServer()){
        //			debug = Keyboard.isKeyDown(GLFW.GLFW_KEY_NUMPAD1);
        //		}
        if (!unit.isFlightControllerActive()) {
            if (debug) {
                System.err.println("FLIGHT CONTROLLER INACTIVE");
            }
            return;
        }
        if (getCollectionManagers().isEmpty()) {
            if (debug) {
                System.err.println("NO HARVESTER GROUPS");
            }
            //nothing to succ with
            return;
        }
        try {
            if (!convertDeligateControls(unit, shootContainer.controlledFromOrig, shootContainer.controlledFrom)) {
                if (debug) {
                    System.err.println("NO SLOT");
                }
                return;
            }
        } catch (IOException e) {
            System.err.println("[MOD][BastionInitiative] ERROR CONVERTING DELEGATE CONTROLS FOR GAS HARVESTER: ");
            e.printStackTrace();
            return;
        }
        long time = System.currentTimeMillis();
        int unpowered = 0;
        getPowerManager().sendNoPowerHitEffectIfNeeded();
        if (debug) {
            System.err.println("GAS HARVESTING CONTROLLERS: " + getState() + ", " + getCollectionManagers().size() + " FROM: " + shootContainer.controlledFrom);
        }
        for (int i = 0; i < getCollectionManagers().size(); i++) {
            GasHarvesterCollectionManager cm = getCollectionManagers().get(i);
            if (unit.isSelected(cm.getControllerElement(), shootContainer.controlledFrom)) {
                boolean controlling = shootContainer.controlledFromOrig.equals(shootContainer.controlledFrom);
                controlling |= getControlElementMap().isControlling(shootContainer.controlledFromOrig, cm.getControllerPos(), controllerId);
                if (debug) {
                    System.err.println("Controlling " + controlling + " " + getState());
                }

                if (controlling) {
                    if(!cm.allowedOnServerLimit()){
                        continue;
                    }
                    if (shootContainer.controlledFromOrig.equals(Ship.core)) {
                        unit.getControlledFrom(shootContainer.controlledFromOrig);
                    }
                    if (debug) {
                        System.err.println("Controlling " + controlling + " " + getState() + ": " + cm.getElementCollections().size());
                    }
                    for (int u = 0; u < cm.getElementCollections().size(); u++) {
                        GasHarvesterUnit c = cm.getElementCollections().get(u);

                        Vector3i v = c.getOutput();

                        shootContainer.weapontOutputWorldPos.set(
                                v.x - SegmentData.SEG_HALF,
                                v.y - SegmentData.SEG_HALF,
                                v.z - SegmentData.SEG_HALF);
                        if (debug) {
                            System.err.println("GAS HARVESTER \"OUTPUT\" ON " + getState() + " -> " + v + "");
                        }
                        if (getSegmentController().isOnServer()) {
                            getSegmentController().getWorldTransform().transform(shootContainer.weapontOutputWorldPos);
                        } else {
                            getSegmentController().getWorldTransformOnClient().transform(shootContainer.weapontOutputWorldPos);
                        }

                        shootContainer.centeralizedControlledFromPos.set(shootContainer.controlledFromOrig);
                        shootContainer.centeralizedControlledFromPos.sub(Ship.core);


                        shootContainer.camPos.set(getSegmentController().getAbsoluteElementWorldPosition(shootContainer.centeralizedControlledFromPos, shootContainer.tmpCampPos));


                        boolean focus = false;
                        boolean lead = false;
                        unit.getShootingDir(getSegmentController(), shootContainer, c.getDistanceFull(), 1, cm.getControllerPos(), focus, lead);

                        shootContainer.shootingDirTemp.normalize();

                        doShot(c, cm, shootContainer, unit.getPlayerState(), timer);

                        //							getParticleController().addProjectile(getSegmentController(), weapontOutputWorldPos, shootingDirTemp, c.getDamage(), c.getDistance());

                    }
                    if (cm.getElementCollections().isEmpty() && clientIsOwnShip()) {
                        ((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo Modules connected \nto entry point"), 0);
                    }
                }
            }
        }
        if (unpowered > 0 && clientIsOwnShip()) {
            ((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nHarvester Elements unpowered: %s",  unpowered), 0);
        }
        if (getCollectionManagers().isEmpty() && clientIsOwnShip()) {
            ((GameClientState) getState()).getController().popupInfoTextMessage(Lng.str("WARNING!\n \nNo harvester controllers."), 0);
        }

        claimResources();
    }

    @Override
    public ControllerManagerGUI getGUIUnitValues(GasHarvesterUnit firingUnit, GasHarvesterCollectionManager var2, ControlBlockElementCollectionManager<?, ?, ?> var3, ControlBlockElementCollectionManager<?, ?, ?> var4){
        return ControllerManagerGUI.create((GameClientState) getState(), Lng.str("Gas Harvester Unit"), firingUnit,
                new ModuleValueEntry(Lng.str("Range (Planets)"), "2 Sectors"),
                new ModuleValueEntry(Lng.str("Range (Stars)"), "1 System"), //TODO ...and nebulae
                //TODO: Nerf range later
                new ModuleValueEntry(Lng.str("PowerConsumptionResting"), firingUnit.getPowerConsumedPerSecondResting()),
                new ModuleValueEntry(Lng.str("PowerConsumptionCharging"), firingUnit.getPowerConsumedPerSecondCharging())
        );
    }

    private class ResourceRoutingQueueEntry {
        final PassiveResourceProvider source;
        final float harvestPower;
        final Inventory destination;

        private ResourceRoutingQueueEntry(PassiveResourceProvider source, float harvestPower, Inventory destination) {
            this.source = source;
            this.harvestPower = harvestPower;
            this.destination = destination;
        }
    }

    public abstract class RealTimeDelayedAction {
        private abstract static class IntervalRunnable extends StarRunnable {
            protected long interval = 0;
            protected void setInterval(long milliseconds){
                interval = milliseconds;
            }
        }

        public final IntervalRunnable runnable;
        private int repeats = -1;
        private boolean doFirstImmediately;

        public RealTimeDelayedAction(final long timeMs) {
            runnable = new IntervalRunnable() {
                boolean firstRun = true;
                long previousTime = System.nanoTime();
                long t;

                @Override
                public void run() {
                    long currTime = System.nanoTime();
                    int deltaT = (int) ((currTime - previousTime) / 1000000);
                    previousTime = currTime;
                    this.t += deltaT;
                    if (this.t >= interval || doFirstImmediately && firstRun) {
                        doAction();
                        if(repeats > 0 || repeats == -1) {
                            if(repeats > 0) repeats--;
                            t = 0;
                        }
                        else if(repeats == 0){
                            this.cancel();
                        }
                    }
                    firstRun = false;
                }
            };
        }

        public RealTimeDelayedAction(final long timeMs, int repeats){
            this(timeMs);
            this.repeats = repeats;
        }

        public RealTimeDelayedAction(final long timeMs, int repeats, boolean skipFirstWait){
            this(timeMs, repeats);
            this.repeats = repeats;
            this.doFirstImmediately = skipFirstWait;
        }

        public void setInterval(long ms){
            runnable.setInterval(ms);
        }
        public abstract void doAction();

        public final void startCountdown(){
            runnable.runTimer( 1);
        }

        public final void cancel(){
            repeats = 0;
            runnable.cancel();
        }
    }
}
