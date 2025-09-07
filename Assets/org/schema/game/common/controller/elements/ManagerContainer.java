package org.schema.game.common.controller.elements;

import api.element.block.Blocks;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.StorageItemPullListener;
import api.utils.game.module.ModManagerContainerModule;
import api.utils.game.module.ModTagUtils;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.SerializationInterface;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.data.*;
import org.schema.game.client.view.ElementCollectionDrawer.MContainerDrawJob;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelpManager;
import org.schema.game.client.view.gui.shiphud.newhud.HudContextHelperContainer.Hos;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.SegmentController.PullPermission;
import org.schema.game.common.controller.damage.DamageDealer;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.damage.effects.MetaWeaponEffectInterface;
import org.schema.game.common.controller.elements.FocusableUsableModule.FireMode;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.activation.ActivationDestMetaDataDummy;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager.WeaponType;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.controller.elements.armorhp.ArmorHPUnit;
import org.schema.game.common.controller.elements.beam.BeamElementManager;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.controller.elements.cannon.ZoomableUsableModule;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.controller.elements.cargo.CargoElementManager;
import org.schema.game.common.controller.elements.cargo.CargoUnit;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockElementManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockUnit;
import org.schema.game.common.controller.elements.dockingBlock.DockingElementManagerInterface;
import org.schema.game.common.controller.elements.effectblock.EffectAddOnManager;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.factory.CargoCapacityElementManagerInterface;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerCollection;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerUnit;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerCollectionManager;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerUnit;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.*;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.rail.TurretShotPlayerUsable;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionCollectionManager;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionElementManager;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionUnit;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupCollectionManager;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupUnit;
import org.schema.game.common.controller.elements.sensor.SensorCollectionManager;
import org.schema.game.common.controller.elements.sensor.SensorElementManager;
import org.schema.game.common.controller.elements.sensor.SensorUnit;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateElementManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateUnit;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerCollectionManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerUnit;
import org.schema.game.common.controller.elements.stealth.StealthCollectionManager;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;
import org.schema.game.common.controller.elements.stealth.StealthUnit;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerCollectionManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerUnit;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.*;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.network.objects.LongStringPair;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.ShortIntPair;
import org.schema.game.network.objects.remote.*;
import org.schema.game.network.objects.valueUpdate.DestinationValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ServerValueRequestUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BBWirelessLogicMarker;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.LongIntPair;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.w3c.dom.Document;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static org.schema.game.common.data.element.ElementKeyMap.STASH_ELEMENT;

public abstract class ManagerContainer<E extends SegmentController> implements InventoryHolder {

	public static final long TIME_STEP_STASH_PULL = 10000;
	private static final int BLOCK_ADD_REMOVE_LISTEN_DELAY = 500;
	private static boolean startedClientStatic;
	public final Long2FloatOpenHashMap floatValueMap = new Long2FloatOpenHashMap();
	protected final Object2ObjectOpenHashMap<UsableElementManager<?, ?, ?>, ManagerModule<?, ?, ?>> em2modulesMap = new Object2ObjectOpenHashMap<UsableElementManager<?, ?, ?>, ManagerModule<?, ?, ?>>();
	protected final ObjectArrayList<ManagerModule<?, ?, ?>> modules = new ObjectArrayList<ManagerModule<?, ?, ?>>();
	protected final ObjectArrayList<ManagerModule<?, ?, ?>> updateModules = new ObjectArrayList<ManagerModule<?, ?, ?>>();
	protected final ObjectArrayList<ManagerModule<?, ?, ?>> handleModules = new ObjectArrayList<ManagerModule<?, ?, ?>>();
	protected final ModuleMap modulesMap = new ModuleMap();
	protected final ModuleCollectionMap modulesControllerMap = new ModuleCollectionMap();
	protected final ModuleCollectionMap effectMap = new ModuleCollectionMap();
	protected final ObjectArrayList<TargetableSystemInterface> targetableSystems = new ObjectArrayList<TargetableSystemInterface>();
	private final ObjectArrayList<NTReceiveInterface> receiverModules = new ObjectArrayList<NTReceiveInterface>();
	private final ObjectArrayList<ManagerUpdatableInterface> updatable = new ObjectArrayList<ManagerUpdatableInterface>();
	private final ObjectArrayList<NTDistributionReceiverInterface> distributionReceiverModules = new ObjectArrayList<NTDistributionReceiverInterface>();
	private final ObjectArrayList<HittableInterface> hittableModules = new ObjectArrayList<HittableInterface>();
	private final ObjectArrayList<BlockKillInterface> killableBlockModules = new ObjectArrayList<BlockKillInterface>();
	private final ObjectArrayList<NTSenderInterface> senderModules = new ObjectArrayList<NTSenderInterface>();
	private final ObjectArrayList<ElementChangeListenerInterface> changeListenModules = new ObjectArrayList<ElementChangeListenerInterface>();
	private final ObjectArrayList<BlockActivationListenerInterface> blockActivateListenModules = new ObjectArrayList<BlockActivationListenerInterface>();
	private final ObjectArrayList<BeamElementManager<?, ?, ?>> beamModules = new ObjectArrayList<BeamElementManager<?, ?, ?>>();
	private final Set<ActiveInventory> activeInventories = new ObjectOpenHashSet<ActiveInventory>();
	private final ShortOpenHashSet typesThatNeedActivation = new ShortOpenHashSet();
	private final ObjectArrayList<Inventory> delayedInventoryAdd = new ObjectArrayList<Inventory>();
	private final ObjectArrayList<TimedUpdateInterface> timedUpdateInterfaces = new ObjectArrayList<TimedUpdateInterface>();
	private final ObjectArrayList<Vector4i> delayedInventoryRemove = new ObjectArrayList<Vector4i>();

	private final InventoryMap inventories = new InventoryMap();
	private final Long2ObjectOpenHashMap<BlockMetaDataDummy> initialBlockMetaData = new Long2ObjectOpenHashMap<BlockMetaDataDummy>();
	private final LongArrayFIFOQueue ntInventoryProdMods = new LongArrayFIFOQueue();
	private final ObjectArrayFIFOQueue<LongIntPair> ntInventoryProdLimitMods = new ObjectArrayFIFOQueue<LongIntPair>();
	private final ObjectArrayFIFOQueue<LongStringPair> ntInventoryCustomNameMods = new ObjectArrayFIFOQueue<LongStringPair>();
	private final ObjectArrayFIFOQueue<ShortIntPair> ntInventoryFilterMods = new ObjectArrayFIFOQueue<ShortIntPair>();
	private final ObjectArrayFIFOQueue<ShortIntPair> ntInventoryFillMods = new ObjectArrayFIFOQueue<ShortIntPair>();
	private final ObjectArrayFIFOQueue<Inventory> ntInventoryMods = new ObjectArrayFIFOQueue<Inventory>();
	private final ObjectArrayFIFOQueue<Inventory> ntInventoryRemoves = new ObjectArrayFIFOQueue<Inventory>();
	private final ObjectArrayFIFOQueue<InventoryMultMod> ntInventoryMultMods = new ObjectArrayFIFOQueue<InventoryMultMod>();
	private final ObjectArrayFIFOQueue<InventorySlotRemoveMod> ntInventorySlotRemoveMods = new ObjectArrayFIFOQueue<InventorySlotRemoveMod>();
	private final ObjectArrayList<WeaponElementManagerInterface> weapons = new ObjectArrayList<WeaponElementManagerInterface>();
	public final SingleControllerStateUnit unitSingle;
	private final E segmentController;
	//	private final ObjectArrayFIFOQueue<ReceivedDistribution> receivedDistributions = new ObjectArrayFIFOQueue<ReceivedDistribution>();
	private final boolean onServer;
	private final Long2ObjectOpenHashMap<StashInventory> namedInventoriesClient = new Long2ObjectOpenHashMap<StashInventory>();
	private final Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> delayedCummulativeInventoryModsToSend = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();
	private final LongOpenHashSet filterInventories = new LongOpenHashSet();
	private final Transform tTmp = new Transform();
	private final Vector3f sPos = new Vector3f();
	private static final ManagerUpdatabaleComparator updatableComp = new ManagerUpdatabaleComparator();
	//	private final ArrayList<SegmentPiece> beaconMods = new ArrayList<SegmentPiece>();
	public boolean loadInventoriesFromTag = true; //set to false by blueprint spawn
	protected Collection<ManagerModuleCollection<? extends DockingBlockUnit<?, ?, ?>, ? extends DockingBlockCollectionManager<?, ?, ?>, ? extends DockingBlockElementManager<?, ?, ?>>> dockingModules = new ArrayList<ManagerModuleCollection<? extends DockingBlockUnit<?, ?, ?>, ? extends DockingBlockCollectionManager<?, ?, ?>, ? extends DockingBlockElementManager<?, ?, ?>>>();
	private ObjectArrayFIFOQueue<InventoryClientAction> clientInventoryActions = new ObjectArrayFIFOQueue<InventoryClientAction>();
	private Short2IntOpenHashMap relevantMap = new Short2IntOpenHashMap();
	private long flagAnyBlockAdded = -1;
	private long flagAnyBlockRemoved = -1;
	private ObjectArrayFIFOQueue<ValueUpdate> valueUpdates = new ObjectArrayFIFOQueue<ValueUpdate>();
	private boolean namedInventoriesClientChanged = true;
	private long lastInventoryFilterStep = 0;
	public double volumeTotal;
	private final Long2ObjectOpenHashMap<DelayedInvProdSet> invProdMapDelayed = new Long2ObjectOpenHashMap<DelayedInvProdSet>();
	public long shopBlockIndex = Long.MIN_VALUE;
	private final Long2ObjectOpenHashMap<BlockAct> blockActivationsForModules = new Long2ObjectOpenHashMap<BlockAct>();
	private final List<ModuleExplosion> moduleExplosions = new ObjectArrayList<ModuleExplosion>();
	private final Vector4f colorCoreDefault = new Vector4f(0.1f, 0.5f, 1.0f, 1.0f);
	private final Vector4f colorCore = new Vector4f(colorCoreDefault);
	private long itemToSpawnCheckStart = -1;
	private long factionBlockPos = Long.MIN_VALUE;
	private final Long2ObjectMap<PlayerUsableInterface> playerUsable = new Long2ObjectOpenHashMap<PlayerUsableInterface>();
	private final Long2ObjectMap<RevealingActionListener> revealingActionListeners = new Long2ObjectOpenHashMap<RevealingActionListener>();

	public final BlockConnectionPath stabilizerNodePath = new BlockConnectionPath(this);
	protected boolean[] specialBlocks;
	private final List<TagModuleUsableInterface> tagModules = new ObjectArrayList<TagModuleUsableInterface>();
	private TurretShotPlayerUsable turretShotUsable;
	private final Long2LongOpenHashMap flaggedConnections = new Long2LongOpenHashMap();

	private List<ManualMouseEvent> manualEvents = new ObjectArrayList<ManualMouseEvent>();

	private final PullInvHanlder pHandler = new PullInvHanlder();

	private final SegmentPiece tmp = new SegmentPiece();
	private final PowerInterface powerInterface;
	private final LongOpenHashSet buildBlocks = new LongOpenHashSet();
	private final Set<ConfigProviderSource> effectConfigSources = new ObjectOpenHashSet<ConfigProviderSource>();
	private final EffectAddOnManager effectAddOnManager;
	private ObjectArrayFIFOQueue<RecharchableSingleModule> rechargableSingleModulesToAdd;
	private boolean notLoadedOnClient = true;
	private ReactorBoostAddOn reactorBoostAddOn;
	private InterdictionAddOn interdictionBoostAddOn;
	private final LongSet slavesAndEffects = new LongOpenHashSet();
	private ObjectArrayFIFOQueue<InventoryDetailRequest> inventoryDetailRequests = new ObjectArrayFIFOQueue<InventoryDetailRequest>();
	private MContainerDrawJob graphicsListener;
	public long lastChangedElement;
	private final ObjectArrayFIFOQueue<ReceivedBeamLatch> receivedBeamLatches = new ObjectArrayFIFOQueue<ReceivedBeamLatch>();
	public final List<BeamHandlerContainer<?>> beamInteracesSingle = new ObjectArrayList<BeamHandlerContainer<?>>();
	private final ObjectArrayFIFOQueue<EffectChangeHanlder> effectChangeQueue = new ObjectArrayFIFOQueue<EffectChangeHanlder>();
	private final ObjectArrayFIFOQueue<UsableElementManager<?, ?, ?>> checkUpdatableQueue = new ObjectArrayFIFOQueue<UsableElementManager<?, ?, ?>>();
	private final ObjectArrayList<RailDockingListener> railDockingListeners = new ObjectArrayList<RailDockingListener>();
	private PlayerUsableInterface lastUsableUsed;
	private final StateInterface state;

	@SuppressWarnings("unchecked")
	public ManagerContainer(StateInterface state, E segmentController) {
		super();
		this.state = state;
		this.segmentController = segmentController;
		rechargableSingleModulesToAdd = new ObjectArrayFIFOQueue<RecharchableSingleModule>();
		this.powerInterface = new PowerImplementation(this);
		onServer = segmentController.isOnServer();
		unitSingle = new SingleControllerStateUnit();
		effectAddOnManager = new EffectAddOnManager(this);
		initialize(state);
		this.reactorBoostAddOn = new ReactorBoostAddOn(this);
		this.interdictionBoostAddOn = new InterdictionAddOn(this);
		addRechModules();
		for(ManagerModule<?, ?, ?> m : modules) {
			m.init(this);

			if(m.getElementManager() instanceof WeaponElementManagerInterface) {
				weapons.add((WeaponElementManagerInterface) m.getElementManager());
			}
			if(m.getElementManager() instanceof RailDockingListener) {
				railDockingListeners.add(((RailDockingListener) m.getElementManager()));
			}
			if(m.getElementManager() instanceof PowerConsumer) {
				addConsumer((PowerConsumer) m.getElementManager());
			}
			if(m.getElementManager() instanceof PlayerUsableInterface) {
				addPlayerUsable((PlayerUsableInterface) m.getElementManager());
			}
			if(m.getElementManager() instanceof TagModuleUsableInterface) {
				addTagUsable(((TagModuleUsableInterface) m.getElementManager()));
			}

			em2modulesMap.put(m.getElementManager(), m);
			//enqueue for initial check
			checkUpdatableQueue.enqueue(m.getElementManager());
		}

		turretShotUsable = new TurretShotPlayerUsable(this);
		addPlayerUsable(turretShotUsable);
		try {
			reparseBlockBehavior(false);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		addConsumer(new RailPowerConsumer(this));

		for(int i = 0; i < modules.size(); i++) {
			ManagerModule<?, ?, ?> managerModule = modules.get(i);

			if(managerModule.getElementManager() instanceof BeamHandlerDeligator) {
				beamInteracesSingle.add(((BeamHandlerDeligator) managerModule.getElementManager()).getBeamHandler());
			}
			if(managerModule instanceof ManagerModuleControllable<?, ?, ?>) {
				ManagerModuleControllable<?, ?, ?> mm = (ManagerModuleControllable<?, ?, ?>) managerModule;
				assert (!modulesMap.containsKey(mm.getControllerID())) : "already contains: " + ElementKeyMap.toString(mm.getControllerID()) + "; " + mm + "; " + modulesMap;

				modulesMap.put(mm.getControllerID(), mm);

				if(ElementKeyMap.isValidType(mm.getControllerID()) && ElementKeyMap.getInfo(mm.getControllerID()).isEffectCombinationController()) {
					assert (mm.getElementManager() instanceof EffectElementManager<?, ?, ?>);
					effectMap.put(mm.getControllerID(), (ManagerModuleCollection<?, ?, ?>) mm);
				} else {
					assert (!(mm.getElementManager() instanceof EffectElementManager<?, ?, ?>));
				}
				if(mm instanceof ManagerModuleCollection<?, ?, ?>) {
					assert (!modulesControllerMap.containsKey(mm.getControllerID())) : mm + "; " + modulesMap;
					modulesControllerMap.put(mm.getControllerID(), (ManagerModuleCollection<?, ?, ?>) mm);
				}

				if(mm.getElementManager() instanceof TimedUpdateInterface) {
					timedUpdateInterfaces.add((TimedUpdateInterface) mm.getElementManager());
				}
				//				if(mm.getElementManager() instanceof BlockActivationListenerInterface){
				//					if(!blockActivateListenModules.contains(mm.getElementManager())){
				//						blockActivateListenModules.add((BlockActivationListenerInterface) mm.getElementManager());
				//					}else{
				//						System.err.println("[WARNING] Tried to double add elementModule: "+mm.getElementManager()+" ("+mm+")");
				//					}
				//				}
			}
			//not sure if this is an "else"
			if(modulesMap.containsKey(managerModule.getElementID())) {
				ManagerModule<?, ?, ?> m = modulesMap.get(managerModule.getElementID());
				while(m.getNext() != null) {
					m = m.getNext();
				}
				m.setNext(managerModule);
			} else {
				modulesMap.put(managerModule.getElementID(), managerModule);
			}

			if(managerModule instanceof ManagerModuleSingle<?, ?, ?>) {
				ManagerModuleSingle<?, ?, ?> m = (ManagerModuleSingle<?, ?, ?>) managerModule;
				Object collectionManager = m.getCollectionManager();
				addTypeModifiers(collectionManager);

			}

			if(!(managerModule.getElementManager() instanceof VoidElementManager)) {
				handleModules.add(managerModule);
			}
			if(managerModule.getElementManager() instanceof DockingElementManagerInterface) {
				dockingModules.add((ManagerModuleCollection<? extends DockingBlockUnit<?, ?, ?>, ? extends DockingBlockCollectionManager<?, ?, ?>, ? extends DockingBlockElementManager<?, ?, ?>>) managerModule);
			}

			if(managerModule.getElementManager() instanceof TargetableSystemInterface) {
				targetableSystems.add((TargetableSystemInterface) managerModule.getElementManager());
			}

			addTypeModifiers(managerModule.getElementManager());
		}
		modules.trim();
		handleModules.trim();
		timedUpdateInterfaces.trim();
		weapons.trim();
		railDockingListeners.trim();

		afterInitialize();
		assert (modulesMap.checkIntegrity());

	}

	protected abstract void afterInitialize();

	public void reparseBlockBehavior(boolean clear) throws IOException {

		if(clear) {
			if(segmentController.isOnServer()) {
				UsableElementManager.initializedServer.clear();
			} else {
				UsableElementManager.initializedClient.clear();
			}
		}

		for(int i = 0; i < modules.size(); i++) {
			ManagerModule<?, ?, ?> managerModule = modules.get(i);
			boolean ok = false;
			while(!ok) {
				try {
					assert (((GameStateInterface) segmentController.getState()).getBlockBehaviorConfig() != null);

					Document blockBehaviorConfig = ((GameStateInterface) segmentController.getState()).getBlockBehaviorConfig();

					managerModule.getElementManager().parse(blockBehaviorConfig);
					ok = true;
				} catch(Exception e) {
					if(GameClientController.availableGUI) {
						GuiErrorHandler.processErrorDialogException(e);
						((GameStateControllerInterface) segmentController.getState().getController()).parseBlockBehavior("./data/config/blockBehaviorConfig.xml");
					} else {
						e.printStackTrace();
						((GameServerState) state).getController().broadcastMessage(Lng.astr("BLOCK CONFIG PARSING\nFAILED ON SERVER!"), ServerMessage.MESSAGE_TYPE_ERROR);
						((GameStateControllerInterface) segmentController.getState().getController()).parseBlockBehavior("./data/config/revertBlockBehaviorConfig.xml");
					}

				}
			}
		}
	}

	private void addInventory(long absIndex, short type) {
		assert (segmentController.isOnServer());
		if((!inventories.containsKey(absIndex) || delayedInventoryRemove.contains(new Vector4i(ElementCollection.getPosFromIndex(absIndex, new Vector3i()), 0)))) {
			if(ElementKeyMap.isInventory(type)) {
				delayedInventoryAdd.add(new StashInventory(this, absIndex));
			} else if(type == ElementKeyMap.RECYCLER_ELEMENT_OLD) {
				delayedInventoryAdd.add(new ItemsToCreditsInventory(this, absIndex));
			} else if(ElementKeyMap.getFactorykeyset().contains(type)) {
				delayedInventoryAdd.add(new StashInventory(this, absIndex));
			}
		}

	}

	private void addTypeModifiers(Object o) {
		if(o instanceof ManagerUpdatableInterface) {
			updatable.add((ManagerUpdatableInterface) o);
		}

		if(o instanceof NTReceiveInterface) {
			receiverModules.add((NTReceiveInterface) o);
		}
		if(o instanceof NTDistributionReceiverInterface) {
			distributionReceiverModules.add((NTDistributionReceiverInterface) o);
		}
		if(o instanceof BlockActivationListenerInterface) {
			blockActivateListenModules.add((BlockActivationListenerInterface) o);

			(((BlockActivationListenerInterface) o)).updateActivationTypes(typesThatNeedActivation);
		}
		if(o instanceof HittableInterface) {
			hittableModules.add((HittableInterface) o);
		}
		if(o instanceof BlockKillInterface) {
			killableBlockModules.add((BlockKillInterface) o);
		}
		if(o instanceof NTSenderInterface) {
			senderModules.add((NTSenderInterface) o);
		}
		if(o instanceof ElementChangeListenerInterface) {
			changeListenModules.add((ElementChangeListenerInterface) o);
		}
		if(o instanceof BeamElementManager<?, ?, ?>) {
			beamModules.add((BeamElementManager<?, ?, ?>) o);
		}

		if(o instanceof BeamHandlerContainer<?>) {
			beamInteracesSingle.add((BeamHandlerContainer<?>) o);
		}

		if(o instanceof BeamHandlerDeligator) {
			beamInteracesSingle.add(((BeamHandlerDeligator) o).getBeamHandler());
		}
	}

	private void announceInventory(long pos, boolean preserveControl, Inventory inventory, boolean add) {
		synchronized(inventories) {

			if(add) {
				assert (segmentController.getSegmentBuffer().getPointUnsave(pos).getType() != ElementKeyMap.CARGO_SPACE);
				inventories.put(pos, inventory);
				RemoteInventory i = new RemoteInventory(inventory, this, add, segmentController.isOnServer());
				getInventoryInterface().getInventoriesChangeBuffer().add(i);

			} else {
				System.err.println("[MANAGERCONTAINER] ANNOUNCE INVENTORY REMOVE ON " + segmentController + ": preserve " + preserveControl);
				Inventory remove = inventories.remove(pos);
				if(remove != null) {
					onInventoryRemove(remove, preserveControl);
					RemoteInventory i = new RemoteInventory(remove, this, add, segmentController.isOnServer());
					getInventoryInterface().getInventoriesChangeBuffer().add(i);
				}

			}
		}

		//		if(add){
		//			inventory.sendAll(getInventoryInterface());
		//		}
	}

	public boolean canBeControlled(short fromType, short toType) {
		return (ElementKeyMap.isValidType(toType) && ElementKeyMap.isValidType(fromType)) && (modulesMap.containsKey(toType) || ElementInformation.canBeControlled(fromType, toType));

	}

	public void clear() {
		for(int i = 0; i < modules.size(); i++) {
			modules.get(i).clear();
		}
		slavesAndEffects.clear();
		notLoadedOnClient = true;
	}

	protected void fromExtraTag(Tag tag) {
	}

	public void fromTagStructure(Tag tag) {

		if(tag.getName().equals("container")) {
			Tag[] parts = (Tag[]) tag.getValue();
			fromTagInventory(parts[0]);
			if(parts[1].getType() == Type.STRUCT) {
				Tag[] distTags = (Tag[]) parts[1].getValue();
				for(int i = 0; i < distTags.length; i++) {
					if(distTags[i].getType() != Type.FINISH) {
						//						System.err.println("LOADING DISTRIBUTION TAG "+i+" FOR "+getSegmentController());
						fromTagDistribution(distTags[i]);
					}
				}
			}

			if(this instanceof PowerManagerInterface && parts.length > 2) {
				PowerAddOn p = ((PowerManagerInterface) this).getPowerAddOn();
				if("pw".equals(parts[2].getName())) {
					p.fromTagStructureOld(parts[2]);
				} else if(parts[2].getType() == Type.STRUCT) {
					p.fromTagStructure(parts[2]);
				}
			}
			if(this instanceof ShieldContainerInterface && parts.length > 3) {
				if(parts[3].getType() == Type.DOUBLE) {
					//old shields
					assert (((ShieldContainerInterface) this).getShieldRegenManager() != null);
					assert (parts[3].getValue() != null);

					((ShieldContainerInterface) this).getShieldAddOn().setInitialShields((Double) parts[3].getValue());
				} else if(parts[3].getType() == Type.STRUCT) {
					//new local shields
					((ShieldContainerInterface) this).getShieldAddOn().getShieldLocalAddOn().fromTagStructure(parts[3]);
				}
			}

			if(parts.length > 4) {
				fromExtraTag(parts[4]);
			}
			if(parts.length > 5 && parts[5].getType() != Type.FINISH) {
				((SendableSegmentController) segmentController).fromActivationStateTag(parts[5]);
			}
			if(parts.length > 6 && parts[6].getType() != Type.FINISH) {
				segmentController.readTextBlockData(parts[6]);
			}
			if(parts.length > 7 && parts[7].getType() != Type.FINISH) {
				readRelevantBlockCounts(parts[7]);
			}
			if(parts.length > 8 && parts[8].getType() != Type.FINISH) {
				fromWarpGateTag(parts[8]);
			}
			if(parts.length > 9 && parts[9].getType() != Type.FINISH) {
				int shift = 0;
				if(segmentController.isLoadedFromChunk16()) {
					shift = Chunk16SegmentData.SHIFT_;
				}
				fromTagModule(parts[9], shift);

			}
			if(parts.length > 10 && parts[10].getType() != Type.FINISH) {
				segmentController.aiFromTagStructure(parts[10]);
			}
			if(parts.length > 11 && parts[11].getType() != Type.FINISH) {
				segmentController.getSlotAssignment().fromTagStructure(parts[11]);
			}
			if(parts.length > 12 && parts[12].getType() != Type.FINISH) {
				fromRaceGateTag(parts[12]);
			}
			if(parts.length > 13 && parts[13].getType() != Type.FINISH) {
				fromAdditionalDummiesNotLoadedLastTime(parts[13]);
			}
			if(parts.length > 14 && parts[14].getType() != Type.FINISH) {
				Tag.listFromTagStructSP(moduleExplosions, parts[14], fromValue -> {
					Tag md = (Tag) fromValue;
					ModuleExplosion m = new ModuleExplosion();
					m.fromTagStructure(md);
					return m;
				});
			}
			if(parts.length > 15 && parts[15].getType() != Type.FINISH) {
				powerInterface.fromTagStructure(parts[15]);
			}
			if(parts.length > 16 && parts[16].getType() != Type.FINISH) {
				segmentController.pullPermission = PullPermission.values()[parts[16].getByte()];
			}
			//INSERTED CODE
			//If mod tag exists (new blueprints) fire it off to the ModManagerContainerModule's present in this file.
			if(parts.length > 17 && parts[17].getType() != Type.FINISH) {
				System.err.println("CALLED ON TAG DESERIALIZE");
				ModTagUtils.onTagDeserialize(this, parts[17]);
			}
			///
		} else if(tag.getName().equals("controllerStructure")) {
			handleTag(tag);
		} else {
			Tag[] sm = (Tag[]) tag.getValue();
			//substructure (e.g. ship)
			for(int i = 0; i < sm.length; i++) {
				if(sm[i].getType() != Type.FINISH) {
					handleTag(sm[i]);
				}
			}
		}

	}

	//INSERTED CODE
	protected HashMap<Short, ModManagerContainerModule> modModuleMap;

	public ModManagerContainerModule getModMCModule(short blockId) {
		return modModuleMap.get(blockId);
	}

	public HashMap<Short, ModManagerContainerModule> getModModuleMap() {
		return modModuleMap;
	}

	///

	private void fromAdditionalDummiesNotLoadedLastTime(Tag tag) {
		//no chunk16 conversion needed, since in this state they
		//have been already loaded once and then saved again 
		Tag[] moduleTags = (Tag[]) tag.getValue();
		for(int i = 0; i < moduleTags.length - 1; i++) {
			fromTagDummy(moduleTags[i], 0, moduleTags[i].getName());
		}
	}

	public void fromTagModule(Tag tag, int shift) {
		Tag[] moduleTags = (Tag[]) tag.getValue();
		for(int i = 0; i < moduleTags.length - 1; i++) {
			fromTagModuleSigle(moduleTags[i], shift);
		}
	}

	private void fromTagDistribution(Tag tag) {
	}

	private void fromTagModuleSigle(Tag tag, int shift) {
		Tag[] t = (Tag[]) tag.getValue();

		//ElementManager
		for(int i = 0; i < t.length && t[i].getType() != Type.FINISH; i++) {
			fromTagDummy(t[i], shift, tag.getName());
		}
	}

	private void fromTagDummy(Tag t, int shift, String name) {

		for(TagModuleUsableInterface m : tagModules) {
			if(m.getTagId().equals(name)) {
				BlockMetaDataDummy u = m.getDummyInstance();
				assert (u != null) : "missing implementation of function: Class: " + m.getClass().getSimpleName();
				u.fromTagStructure(t, shift);
				this.initialBlockMetaData.put(u.pos, u);
				return;
			}
		}
		assert ("EF-9223372036854775780".equals(name)) : "No handler found: " + name;

		//		if (DockingBlockElementManager.TAG_ID.equals(name)) {
		//
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new DockingMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else if (JumpDriveElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new JumpDriveMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		}  else if (TransporterElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new TransporterMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else if (JumpInhibitorElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new JumpInhibitorMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else if (ScannerElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new ScannerMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else if (ActivationElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new ActivationDestMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else if (ShipyardElementManager.TAG_ID.equals(name)) {
		//			//CollectionManagers
		//			BlockMetaDataDummy u = new ShipyardMetaDataDummy();
		//			u.fromTagStructure(t, shift);
		//			this.getInitialBlockMetaData().put(ElementCollection.getIndex(u.pos), u);
		//		} else {
		//			assert (false) : name;
		//		}
	}

	public void fromTagInventory(Tag t) {

		Tag[] inventors = (Tag[]) t.getValue();
		for(int i = 0; i < inventors.length; i++) {
			if(inventors[i].getType() == Type.FINISH) {
				break;
			}
			Tag[] ent = (Tag[]) inventors[i].getValue();

			int type = (Integer) ent[0].getValue();
			Vector3i key = (Vector3i) ent[1].getValue();

			if(segmentController.isLoadedFromChunk16()) {
				key.add(Chunk16SegmentData.SHIFT);
			}

			Inventory inventory = null;

			if(type == 3) {
				inventory = new StashInventory(this, ElementCollection.getIndex(key));
			}
			if(type == 1) {
				inventory = new ItemsToCreditsInventory(this, ElementCollection.getIndex(key));
			}
			assert (inventory != null) : "unknown type: " + type;

			inventory.fromTagStructure(ent[2]);
			if(!loadInventoriesFromTag) {
				inventory.clear();
				System.err.println("[TAG] " + segmentController + " clearing inventory (keeping filters)");
			}
			inventories.put(inventory.getParameterIndex(), inventory);
		}
	}

	protected Tag getExtraTag() {
		return new Tag(Type.BYTE, "ex", (byte) 0);
	}

	/**
	 * @return the initialBlockMetaData
	 */
	public Long2ObjectOpenHashMap<BlockMetaDataDummy> getInitialBlockMetaData() {
		return initialBlockMetaData;
	}

	/**
	 * @return the inventories
	 */
	@Override
	public InventoryMap getInventories() {
		return inventories;
	}

	public Inventory getInventory(Vector3i pos) {
		long index = ElementCollection.getIndex(pos);
		return inventories.get(index);
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		CargoCollectionManager cargoCollectionManager = getCargo().getCollectionManagersMap().get(inventory.getParameterIndex());
		return cargoCollectionManager == null ? 0 : cargoCollectionManager.getCapacity();
	}

	@Override
	public void volumeChanged(double volumeBefore, double volumeNow) {
		double diff = volumeNow - volumeBefore;

		volumeTotal += diff;

		segmentController.flagupdateMass();
	}

	@Override
	public void sendInventoryErrorMessage(Object[] astr, Inventory inv) {

		if(isOnServer()) {
			//			System.err.println("INVENTORY ERROR MESSAGE: "+Arrays.toString(astr)+" on "+inv);

			ServerMessage m = new ServerMessage(astr, ServerMessage.MESSAGE_TYPE_ERROR_BLOCK);
			m.block = inv.getParameterIndex();

			//			getSegmentController().getNetworkObject().messagesToBlocks.add(new RemoteServerMessage(m, getSegmentController().getNetworkObject()));

			//			getSegmentController().sendControllingPlayersServerMessage(astr, ServerMessage.MESSAGE_TYPE_ERROR);
		}
	}

	@Override
	public Inventory getInventory(long pos) {
		return inventories.get(pos);
	}

	@Override
	public String getName() {
		return segmentController.getRealName();
	}

	@Override
	public StateInterface getState() {
		return state;
	}

	@Override
	public String printInventories() {
		return inventories.toString();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ContainerInterface#handleActivate(org.schema.game.common.data.SegmentPiece)
	 */

	@Override
	public void sendInventoryModification(IntCollection slots, long parameter) {

		Inventory inventory = getInventory(parameter);

		if(inventory != null) {
			InventoryMultMod m = new InventoryMultMod(slots, inventory, parameter);

			getInventoryInterface().getInventoryMultModBuffer().add(new RemoteInventoryMultMod(m, segmentController.getNetworkObject()));
		} else {
			try {
				throw new IllegalArgumentException("[INVENTORY] Exception: tried to send inventory " + parameter + " (inventory was null at that position)");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void sendInventoryModification(int slot, long parameter) {

		IntArrayList l = new IntArrayList(1);
		l.add(slot);
		sendInventoryModification(l, parameter);

	}

	@Override
	public void sendInventorySlotRemove(int slot, long parameter) {
		Inventory inventory = getInventory(parameter);
		if(inventory != null) {
			getInventoryInterface().getInventorySlotRemoveRequestBuffer().add(new RemoteInventorySlotRemove(new InventorySlotRemoveMod(slot, parameter), isOnServer()));
		} else {
			try {
				throw new IllegalArgumentException("[INVENTORY] Exception: tried to send inventory " + parameter);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected NetworkInventoryInterface getInventoryInterface() {
		return (NetworkInventoryInterface) segmentController.getNetworkObject();
	}

	/**
	 * @return the modules
	 */
	public ObjectArrayList<ManagerModule<?, ?, ?>> getModules() {
		return modules;
	}

	/**
	 * @return the modulesControllerMap
	 */
	public ModuleCollectionMap getModulesControllerMap() {
		return modulesControllerMap;
	}

	/**
	 * @return the modulesMap
	 */
	public ModuleMap getModulesMap() {
		return modulesMap;
	}

	/**
	 * @return the segmentController
	 */
	public E getSegmentController() {
		return segmentController;
	}

	private PlayerUsableInterface lastUsable;
	private float timeSpentHudLastUsable;
	private boolean flagModuleKilled;
	private float integrityUpdateDelay;
	private boolean flagBlockAdded;
	private boolean flagBlockDamaged;
	private boolean flagBlockKilled;
	private float repairDelay;
	private long lastThrownInvalidConnectionException;

	public void addHudConext(ControllerStateUnit unit, HudContextHelpManager h, Timer timer) {
		if(unit.parameter instanceof Vector3i) {
			assert (unit.getPlayerState() != null);
			long enterIndex = ElementCollection.getIndex((Vector3i) unit.parameter);
			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);

			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
			if(p != null) {
				Hos hos;
				if(lastUsable == p) {
					timeSpentHudLastUsable += timer.getDelta();
				} else {
					timeSpentHudLastUsable = 0;
				}
				if(timeSpentHudLastUsable < EngineSettings.SHOW_MODULE_HELP_ON_CURSOR.getInt()) {
					hos = Hos.MOUSE;
				} else {
					hos = Hos.LEFT;
				}

				p.addHudConext(unit, h, hos);
				lastUsable = p;
			}
			lastUsable = p;

			for(long a : PlayerUsableInterface.ALWAYS_SELECTED) {
				PlayerUsableInterface always = playerUsable.get(a);
				//				assert(always != null):a;
				if(always != null) {
					always.addHudConext(unit, h, Hos.LEFT);
				}
			}
		} else {
			lastUsable = null;
		}
	}

	public float getSelectedWeaponRange(ControllerStateUnit unit) {
		if(unit.parameter instanceof Vector3i) {
			assert (unit.getPlayerState() != null);
			long enterIndex = ElementCollection.getIndex((Vector3i) unit.parameter);
			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);

			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
			if(p != null) {
				return p.getWeaponDistance();
			}
		}
		return 0;
	}

	public float getSelectedWeaponSpeed(ControllerStateUnit unit) {
		if(unit.parameter instanceof Vector3i) {
			assert (unit.getPlayerState() != null);
			long enterIndex = ElementCollection.getIndex((Vector3i) unit.parameter);
			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);

			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
			if(p != null) {
				return p.getWeaponSpeed();
			}
		}
		return 0;
	}

	public void handleKeyPress(ControllerStateInterface unitInterface, Timer timer) {
		ControllerStateUnit unit = (ControllerStateUnit) unitInterface;
		if(unit.parameter instanceof Vector3i) {
			assert (unit.getPlayerState() != null);
			long enterIndex = ElementCollection.getIndex((Vector3i) unit.parameter);
			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);

			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
			if(p != null) {
				if(lastUsableUsed != p) {
					if(lastUsableUsed != null) {
						lastUsableUsed.onSwitched(false);
					}
					p.onSwitched(true);
					lastUsableUsed = p;
				}
				p.handleKeyPress(unit, timer);
			}

			for(long a : PlayerUsableInterface.ALWAYS_SELECTED) {
				PlayerUsableInterface always = playerUsable.get(a);
				//				assert(always != null):a;
				if(always != null) {
					always.handleKeyPress(unit, timer);
					;
				}
			}
		}
	}

	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer) {
		if(unit.parameter instanceof Vector3i) {
			long enterIndex = ElementCollection.getIndex((Vector3i) unit.parameter);
			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);

			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
			if(p != null) {
				p.handleKeyEvent(unit, mapping, timer);
			}

			for(long a : PlayerUsableInterface.ALWAYS_SELECTED) {
				PlayerUsableInterface always = playerUsable.get(a);
				if(always != null) {
					always.handleKeyEvent(unit, mapping, timer);
					;
				}
			}
		}
	}

	/**
	 * @param block
	 * @param active
	 * @return true if success (false if not all loaded)
	 */
	public boolean handleBlockActivate(SegmentPiece block, boolean oldActive, boolean active) {

		for(BlockActivationListenerInterface i : blockActivateListenModules) {
			if(i.isHandlingActivationForType(block.getType())) {
				int act = i.onActivate(block, oldActive, active);
				if(act < 0) {
					//				System.err.println("RETURNED "+act+" on "+i);
					return false;
				}
			}
		}
		return true;
	}

    public abstract AmmoCapacityCollectionManager<?, ?, ?> getAmmoSystem(WeaponType weaponType);

    public void flagSendAllAmmo() {
    }

    /**
     * @return the stealth system collection
     */
    public ManagerModuleCollection<StealthUnit, StealthCollectionManager, StealthElementManager> getStealth() {
        return null;
    }

    /**
     * @return the structure scan system collection
     */
    public ManagerModuleCollection<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> getShortRangeScanner() {
        return null;
    }

    /**
     * @return the long range scan system collection
     */
    public ManagerModuleCollection<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> getLongRangeScanner() {
        return null;
    }

	public ManagerModuleCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> getWarpGate() {
		return null;
	}

	private class BlockAct{
		UsableControllableElementManager<?, ?, ?> module;
		long block;
		long timeStarted;
		public long trigger = Long.MIN_VALUE;
	}

	/**
	 * registers a module to be used
	 * <p>
	 * this is where weapons etc are fired from by logic
	 *
	 * @param usableControllableElementManager
	 * @param block
	 * @param timer
	 */
	public void registerActivationForModule(UsableControllableElementManager<?, ?, ?> usableControllableElementManager, long block, long fromActivationBlockServer, Timer timer) {
		ManagerContainer<E>.BlockAct blockAct = blockActivationsForModules.get(block);
		if(blockAct == null) {
			blockAct = new BlockAct();
			blockActivationsForModules.put(block, blockAct);
		}
		blockAct.trigger = fromActivationBlockServer;
		blockAct.block = block;
		blockAct.module = usableControllableElementManager;
		blockAct.timeStarted = timer.currentTime;
	}

	/**
	 * @param block
	 * @param oldActive
	 * @param timer
	 * @return true if success (false if not all loaded)
	 */
	public boolean handleActivateBlockActivate(SegmentPiece block, long fromActivationBlockServer, boolean oldActive, Timer timer) {
		PlayerUsableInterface usable = getPlayerUsable(block.getAbsoluteIndex());
		if(usable != null) {
			usable.onLogicActivate(block, oldActive, timer);
		}

		ManagerModuleCollection<?, ?, ?> managerModule = modulesControllerMap.get(block.getType());

		//this is where weapons are fired etc
		if(managerModule != null && managerModule.getElementManager().isUsingRegisteredActivation()) {
			if(block.isActive() && !oldActive) {
				if(!segmentController.isVirtualBlueprint()) {
					registerActivationForModule(managerModule.getElementManager(), block.getAbsoluteIndex(), fromActivationBlockServer, timer);
				}
			}
		}

		//this is mostly for picking an output block
		if(block.isActive() && !oldActive && block.getType() == ElementKeyMap.TURRET_DOCK_ID || block.getType() == ElementKeyMap.FIXED_DOCK_ID) {
			return handleBlockActivate(block, oldActive, block.isActive());
		} else if(!segmentController.isOnServer()) {
			//always handle on client (received from network)
			return handleBlockActivate(block, oldActive, block.isActive());
		}

		return true;
	}

	protected void handleGlobalInventorySlotRemove(InventorySlotRemoveMod a) {
		assert (false);
	}

	protected void handleGlobalInventory(InventoryMultMod a) {
		assert (false);
	}

	public void handleInventoryFromNT(RemoteInventoryBuffer buffer, RemoteLongBuffer prodBuffer, RemoteBuffer<RemoteLongIntPair> prodLimitBuffer, RemoteShortIntPairBuffer filterBuffer, RemoteShortIntPairBuffer fillBuffer, RemoteLongStringBuffer customNameModBuffer) {

		if(prodBuffer != null) {
			for(int i = 0; i < prodBuffer.getReceiveBuffer().size(); i++) {
				long v = prodBuffer.getReceiveBuffer().getLong(i);
				synchronized(ntInventoryProdMods) {
					ntInventoryProdMods.enqueue(v);
				}
			}
		}
		if(prodLimitBuffer != null) {
			for(int i = 0; i < prodLimitBuffer.getReceiveBuffer().size(); i++) {
				LongIntPair v = prodLimitBuffer.getReceiveBuffer().get(i).get();
				synchronized(ntInventoryProdLimitMods) {
					ntInventoryProdLimitMods.enqueue(v);
				}
			}
		}
		if(customNameModBuffer != null) {
			for(int i = 0; i < customNameModBuffer.getReceiveBuffer().size(); i++) {
				LongStringPair v = customNameModBuffer.getReceiveBuffer().get(i).get();
				synchronized(ntInventoryCustomNameMods) {
					ntInventoryCustomNameMods.enqueue(v);
				}
			}
		}

		if(filterBuffer != null) {
			for(int i = 0; i < filterBuffer.getReceiveBuffer().size(); i++) {
				ShortIntPair v = filterBuffer.getReceiveBuffer().get(i).get();
				synchronized(ntInventoryFilterMods) {
					ntInventoryFilterMods.enqueue(v);
				}
			}
		}
		if(fillBuffer != null) {
			for(int i = 0; i < fillBuffer.getReceiveBuffer().size(); i++) {
				ShortIntPair v = fillBuffer.getReceiveBuffer().get(i).get();
				synchronized(ntInventoryFillMods) {
					ntInventoryFillMods.enqueue(v);
				}
			}
		}
		ObjectArrayList<RemoteInventory> changeBuffer = buffer.getReceiveBuffer();

		for(int i = 0; i < changeBuffer.size(); i++) {
			Inventory inventory = changeBuffer.get(i).get();
			if(changeBuffer.get(i).isAdd()) {
				synchronized(ntInventoryMods) {
					ntInventoryMods.enqueue(inventory);
				}
			} else {
				synchronized(ntInventoryRemoves) {
					ntInventoryRemoves.enqueue(inventory);
				}
			}
		}

		{
			ObjectArrayList<RemoteInventoryMultMod> receiveBuffer = getInventoryInterface().getInventoryMultModBuffer().getReceiveBuffer();

			for(int i = 0; i < receiveBuffer.size(); i++) {
				RemoteInventoryMultMod a = receiveBuffer.get(i);
				synchronized(ntInventoryMultMods) {
					ntInventoryMultMods.enqueue(a.get());
				}

			}

		}
		{
			ObjectArrayList<RemoteInventorySlotRemove> receiveBuffer = getInventoryInterface().getInventorySlotRemoveRequestBuffer().getReceiveBuffer();

			for(int i = 0; i < receiveBuffer.size(); i++) {
				InventorySlotRemoveMod a = receiveBuffer.get(i).get();
				synchronized(ntInventorySlotRemoveMods) {
					ntInventorySlotRemoveMods.enqueue(a);
				}

			}

		}

		if(getInventoryNetworkObject().getInventoryClientActionBuffer().getReceiveBuffer().size() > 0) {
			for(RemoteInventoryClientAction ia : getInventoryNetworkObject().getInventoryClientActionBuffer().getReceiveBuffer()) {
				InventoryClientAction inventoryClientAction = ia.get();
				synchronized(clientInventoryActions) {
					clientInventoryActions.enqueue(inventoryClientAction);
				}
			}
		}
		//		ObjectArrayList<RemoteIntArray> activateBuffer = getInventoryInterface().getInventoryActivateBuffer().getReceiveBuffer();
		//		for(int i = 0; i < activateBuffer.size(); i++){
		//			RemoteIntArray a = activateBuffer.get(i);
		//			synchronized(ntActiveInventorySingleMods){
		//				ntActiveInventorySingleMods.enqueue(a);
		//			}
		//
		//
		//		}
	}

	private void handleTag(Tag t) {
		if(t.getName().equals("wepContr")) {
			fromTagDistribution(t);
		} else if(t.getName().equals("controllerStructure")) {
			fromTagInventory(t);
		} else {
			assert (false) : t.getName();
		}
	}

	public void initFromNetworkObject(NetworkObject from) {
		powerInterface.initFromNetworkObject(from);
		if(!isOnServer()) {
			relevantMap.putAll(segmentController.getNetworkObject().relevantBlockCounts.get());

			for(Entry<Short, Integer> e : relevantMap.entrySet()) {
				short type = e.getKey();
				int count = e.getValue();

				ManagerModule<?, ?, ?> managerModule = modulesMap.get(type);
				if(managerModule != null && managerModule instanceof ManagerModuleSingle<?, ?, ?>) {
					ManagerModuleSingle<?, ?, ?> m = (ManagerModuleSingle<?, ?, ?>) managerModule;
					//					System.err.println("[CLIENT] "+segmentController+" setting expected element count for "+ElementKeyMap.toString(type)+" to "+count);
					m.getCollectionManager().expected = count;
					m.getCollectionManager().rawCollection = new FastCopyLongOpenHashSet(count);
				}
			}

		}

	}

	protected abstract void initialize(StateInterface state);

	public void onAction() {

	}

	private void updateConnectionModified(long updateTime) {
		if(!flaggedConnections.isEmpty()) {
			ObjectIterator<it.unimi.dsi.fastutil.longs.Long2LongMap.Entry> iterator = flaggedConnections.long2LongEntrySet().fastIterator();
			while(iterator.hasNext()) {
				it.unimi.dsi.fastutil.longs.Long2LongMap.Entry next = iterator.next();
				if(updateTime - next.getLongValue() > 10000) {
					iterator.remove();
				}
			}
		}
	}

	private void flagConnectionModified(long index, long updateTime) {
		flaggedConnections.put(index, updateTime);
	}

	public boolean isConnectionFlagged(long index3Controller) {
		return flaggedConnections.containsKey(index3Controller);
	}

	private static void addToSpecialMap(short type, boolean[] specialBlockMap) {
		specialBlockMap[type] = true;
	}

	protected void getSpecialMap(boolean[] specialBlockMap) {
		for(short type : ElementKeyMap.typeList()) {
			ElementInformation info = ElementKeyMap.getInfoFast(type);

			if(info.isSignal() || info.isRailTrack() || info.isInventory() || (modulesControllerMap.get(type) != null) || (modulesMap.get(ElementKeyMap.getCollectionType(type)) != null) || type == ElementKeyMap.SIGNAL_SENSOR || type == ElementKeyMap.CORE_ID || type == ElementKeyMap.FACTION_BLOCK || type == ElementKeyMap.SHOP_BLOCK_ID || type == ElementKeyMap.BUILD_BLOCK_ID || type == ElementKeyMap.COCKPIT_ID || type == ElementKeyMap.REPULSE_MODULE || type == ElementKeyMap.RAIL_BLOCK_DOCKER || type == ElementKeyMap.SHIPYARD_CORE_POSITION || type == ElementKeyMap.AI_ELEMENT || type == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE || ElementKeyMap.factoryInfoArray[type] || info.isLightSource() ||
					// INSERTED CODE, makes modmcmodule's blocks special
					modModuleMap.containsKey(type)) {
				addToSpecialMap(type, specialBlockMap);
			}
		}
	}

	public void addControllerBlockWithAddingBlock(short type, long absPos, Segment seg, boolean revalidate) {

		ElementInformation info = ElementKeyMap.getInfoFast(type);
		ManagerModuleCollection<?, ?, ?> managerModule;
		if(info.isSignal()) {
			managerModule = modulesControllerMap.get(Element.TYPE_SIGNAL);
		} else if(info.isRailTrack()) {
			managerModule = modulesControllerMap.get(Element.TYPE_RAIL_INV);
		} else {
			managerModule = modulesControllerMap.get(type);
		}

		if(type == ElementKeyMap.SIGNAL_SENSOR) {
			managerModule = getSensor();
		}
		if(type == ElementKeyMap.CORE_ID) {

			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map = segmentController.getControlElementMap().getControllingMap().get(absPos);

			if(map != null) {
				for(short s : ElementKeyMap.lightTypes) {
					FastCopyLongOpenHashSet typedMap = map.get(s);
					if(typedMap != null && !typedMap.isEmpty()) {
						long l = typedMap.iterator().nextLong();
						short toType = (short) ElementCollection.getType(l);
						if(ElementKeyMap.isValidType(toType)) {
							colorCore.set(ElementKeyMap.getInfo(toType).getLightSourceColor());
						}
						break;
					}
				}
			}
		}
		if(managerModule != null) {

			if(managerModule.getElementManager() instanceof CargoCapacityElementManagerInterface) {
				//all elements that have inventory other than Stash (storage) itself (i.e. shipyard, shop, factory, etc) need to be manually added as if it were a storage, in order to track cargo capacity and behave like storage
				//				System.err.println("ADDING NORMAL "+managerModule+"; "+getModulesControllerMap().get(ElementKeyMap.STASH_ELEMENT));
				modulesControllerMap.get(STASH_ELEMENT).addControllerBlockFromAddedBlock(absPos, seg, revalidate);
				//note for future: anything with truly distinct behaviour (Lock Boxes?) needs to be special cased here, otherwise just treat it as a stash
			}

			managerModule.addControllerBlockFromAddedBlock(absPos, seg, revalidate);

			if(isOnServer()) {
				onSpecialTypeAdd(type, absPos);
			}
			if(ElementKeyMap.isInventory(type)) {
				//allways add filtered inventory for shipyard computers
				filterInventories.add(absPos);

			}
		} else {

			ManagerModule<?, ?, ?> m = modulesMap.get(ElementKeyMap.getCollectionType(type));

			if(m != null && m instanceof ManagerModuleSingle) {
				((ManagerModuleSingle<?, ?, ?>) m).addElement(absPos, type);
			} else {
				assert (type != ElementKeyMap.POWER_ID_OLD);
				if(isOnServer()) {
					onSpecialTypeAdd(type, absPos);
				}
			}

			if(ElementKeyMap.isInventory(type)) {
				if(segmentController.getControlElementMap().getControllingMap().getAll().containsKey(absPos)) {
					if(!segmentController.getControlElementMap().getControllingMap().getAll().get(absPos).isEmpty()) {
						filterInventories.add(absPos);
					}
				}

			} else if(type == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) {
				Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> c = segmentController.getControlElementMap().getControllingMap().get(absPos);
				if(c != null) {
					FastCopyLongOpenHashSet mm = c.get(ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE);
					if(mm != null) {
						for(long to : mm) {
							stabilizerNodePath.put(absPos, ElementCollection.getPosIndexFrom4(to));
						}
					}
				}
			}
		}
	}

	public void onAddedElementSynched(short type, Segment segment, long absIndex, long time, boolean revalidate) {

		flagTargetRecalc = true;
		assert (type < specialBlocks.length) : ElementKeyMap.toString(type) + "; " + specialBlocks.length;
		if(!specialBlocks[type]) {
			return;
		}

		flagBlockAdded = true;
		if((type == ElementKeyMap.SHOP_BLOCK_ID)) {
			shopBlockIndex = absIndex;
			if(segmentController.isOnServer()) {
				filterInventories.add(absIndex);
			}
		}
		if(type == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) {
			stabilizerNodePath.addBlock(absIndex);
		}
		if(type == ElementKeyMap.FACTION_BLOCK) {
			factionBlockPos = absIndex;
		}
		if(type == ElementKeyMap.BUILD_BLOCK_ID) {
			buildBlocks.add(absIndex);
		}
		if(segmentController.isOnServer() && (ElementKeyMap.factoryInfoArray[type] || type == ElementKeyMap.SHIPYARD_COMPUTER)) {

			LongOpenHashSet set = segmentController.getControlElementMap().getControllingMap().getAll().get(absIndex);
			if(type == ElementKeyMap.SHIPYARD_COMPUTER || (set != null && set.size() > 0)) {
				filterInventories.add(absIndex);
			}
			addInventory(absIndex, type);
		}

		addControllerBlockWithAddingBlock(type, absIndex, segment, revalidate);

		flagAnyBlockAdded = time;

		//INSERTED CODE
		ModManagerContainerModule module = this.modModuleMap.get(type);
		if(module != null) {
			int var9 = ByteUtil.modUSeg(ElementCollection.getPosX(absIndex));
			int var8 = ByteUtil.modUSeg(ElementCollection.getPosY(absIndex));
			int var6 = ByteUtil.modUSeg(ElementCollection.getPosZ(absIndex));
			byte var10 = segment.getSegmentData().getOrientation((byte) var9, (byte) var8, (byte) var6);
			module.handlePlace(absIndex, var10);
		}
		///

	}

	public void onRemovedElementSynched(short type, int oldSize, byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		flagTargetRecalc = true;
		if(type == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) {
			stabilizerNodePath.remove(segment.getAbsoluteIndex(x, y, z));
			stabilizerNodePath.removeBlock(segment.getAbsoluteIndex(x, y, z));

		}

		if(ElementKeyMap.getFactorykeyset().contains(type)) {
			removeInventory(x, y, z, segment, preserveControl);
		}
		if((type == ElementKeyMap.SHOP_BLOCK_ID)) {
			shopBlockIndex = Long.MIN_VALUE;
		}
		if(type == ElementKeyMap.FACTION_BLOCK) {
			this.factionBlockPos = Long.MIN_VALUE;
		}
		if(type == ElementKeyMap.BUILD_BLOCK_ID) {
			buildBlocks.remove(segment.getAbsoluteIndex(x, y, z));
		}

		ElementInformation info = ElementKeyMap.getInfoFast(type);
		ManagerModuleCollection<?, ?, ?> managerModule;
		if(info.isSignal()) {
			managerModule = modulesControllerMap.get(Element.TYPE_SIGNAL);
		} else if(info.isRailTrack()) {
			managerModule = modulesControllerMap.get(Element.TYPE_RAIL_INV);
		} else {
			managerModule = modulesControllerMap.get(type);
		}
		if(type == ElementKeyMap.SIGNAL_SENSOR) {
			managerModule = getSensor();
		}
		if(managerModule != null) {
			if(managerModule.getElementManager() instanceof CargoCapacityElementManagerInterface) {
				modulesControllerMap.get(STASH_ELEMENT).removeControllerBlock(x, y, z, segment);
			}
			managerModule.removeControllerBlock(x, y, z, segment);

			if(ElementKeyMap.isInventory(type)) {
				filterInventories.remove(ElementCollection.getIndex(x, y, z));
				removeInventory(x, y, z, segment, preserveControl);
			}
			onSpecialTypesRemove(type, x, y, z, segment, preserveControl);
		} else {
			ManagerModule<?, ?, ?> m;
			if(ElementKeyMap.isDoor(type)) {
				m = modulesMap.get(ElementKeyMap.DOOR_ELEMENT);
			} else {
				m = modulesMap.get(type);
			}
			if(m != null && m instanceof ManagerModuleSingle) {
				((ManagerModuleSingle<?, ?, ?>) m).removeElement(x, y, z, segment);
			} else {
				onSpecialTypesRemove(type, x, y, z, segment, preserveControl);
			}
			if(ElementKeyMap.isInventory(type)) {
				long absoluteIndex = segment.getAbsoluteIndex(x, y, z);
				filterInventories.remove(absoluteIndex);
			}
		}
		flagAnyBlockRemoved = System.currentTimeMillis();

		//INSERTED CODE
		ModManagerContainerModule module = this.modModuleMap.get(type);
		if(module != null) {
			module.handleRemove(segment.getAbsoluteIndex(x, y, z));
		}
		///
	}

	public void onConnectionAdded(Vector3i from, short fromType, Vector3i to, short toType) {
		final long fromIndex = ElementCollection.getIndex(from);
		final long toIndex = ElementCollection.getIndex(to);
		flagConnectionModified(fromIndex, state.getUpdateTime());
		flagTargetRecalc = true;
		if((fromType == STASH_ELEMENT || (ElementKeyMap.isToStashConnectable(fromType) && toType == STASH_ELEMENT)) || (fromType == Blocks.LOCK_BOX.getId() || (ElementKeyMap.isToStashConnectable(fromType) && toType == Blocks.LOCK_BOX.getId()))) {
			filterInventories.add(fromIndex);
			if(toType != ElementKeyMap.CARGO_SPACE) {
				return;
			}
		}
		if(fromType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE && toType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) {
			if(stabilizerNodePath.containsKey(fromIndex)) {
				long wasConnectedTo = stabilizerNodePath.get(fromIndex);
				if(wasConnectedTo != toIndex) {
					//remove previous
					segmentController.getControlElementMap().removeControllerForElement(fromIndex, wasConnectedTo, ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE);
				}
			}
			stabilizerNodePath.put(fromIndex, toIndex);
			return;
		}
		if(fromType == ElementKeyMap.CORE_ID && ElementKeyMap.getInfo(toType).isLightSource()) {
			segmentController.getControlElementMap().disconnectAllLightBlocks(ElementCollection.getIndex(from), ElementCollection.getIndex4(to, toType));
			this.colorCore.set(ElementKeyMap.getInfo(toType).getLightSourceColor());
			return;
		}
		if(ElementKeyMap.getInfo(fromType).isRailSpeedActivationConnect(toType)) {
			//dont add activator modules to manager for rail speed. they are used as pure connection
			return;
		}

		ManagerModule<?, ?, ?> managerModule = modulesControllerMap.get(fromType);

		if(managerModule != null && (managerModule.getElementManager() instanceof CargoCapacityElementManagerInterface)) {
			modulesControllerMap.get(STASH_ELEMENT).addControlledBlock(from, fromType, to, toType);
		}

		if(managerModule == null) {
			managerModule = modulesMap.get(toType);
		}

		if(ElementKeyMap.isValidType(fromType) && ElementKeyMap.getInfo(fromType).controlsAll()) {
			managerModule = modulesMap.get(Element.TYPE_ALL);
		}

		if(managerModule == null && ElementKeyMap.getInfo(fromType).isRailSpeedTrackConnect(toType)) {
			managerModule = modulesMap.get(Element.TYPE_RAIL_TRACK);
		}
		if(fromType == ElementKeyMap.SIGNAL_SENSOR) {
			managerModule = getSensor();
		}
		if(managerModule == null) {
			if(segmentController.isClientOwnObject()) {
				((GameClientState) segmentController.getState()).getController().popupAlertTextMessage(Lng.str("Cannot connect module \non this structure"), 0);
			} else {
				//note that this will also fire on other clients or when outside ship
				System.err.println(segmentController.getState() + " WARNING: Could not find Manager Module for " + ElementKeyMap.toString(fromType) + " -> " + ElementKeyMap.toString(toType) + " on " + this.getClass().getSimpleName() + ": " + modulesMap);
			}
			return;
		}
		assert (managerModule != null) : "critical: no module found for " + segmentController + ": " + toType + "; " + modulesMap;

		if(managerModule.getNext() != null) {

			//			System.err.println(getState()+" [MANAGER-CONTAINER] "+"; "+getSegmentController()+"; connecting from type "+ElementKeyMap.toString(fromType)+" "+from);
			while(managerModule.getNext() != null) {
				assert (managerModule instanceof ManagerModuleCollection<?, ?, ?>);
				ManagerModuleCollection<?, ?, ?> g = (ManagerModuleCollection<?, ?, ?>) managerModule;
				if(g.getControllerID() == fromType || ((g.getControllerID() == Element.TYPE_SIGNAL && ElementKeyMap.getInfo(fromType).isSignal())) || ((g.getControllerID() == Element.TYPE_RAIL_TRACK && ElementKeyMap.getInfo(fromType).isRailTrack()))) {
					break;
				}
				managerModule = managerModule.getNext();
			}
		}

		assert (managerModule != null) : toType + " -> " + ElementKeyMap.getInfo(toType).getName() + ": " + modulesMap;
		if(managerModule == null) {
			throw new NullPointerException("Could not find Manager Module for " + toType + ": " + modulesMap);
		}

		if(managerModule instanceof ManagerModuleCollection<?, ?, ?>) {
			//			System.err.println(getState()+" [MANAGER-CONTAINER] "+"; "+getSegmentController()+"; CONNECT "+ElementKeyMap.toString(toType)+" TO MAN-MOD-COL: "+managerModule);

			managerModule.addControlledBlock(from, fromType, to, toType);
		} else {
			//			System.err.println(getState()+" [MANAGER-CONTAINER] "+getState()+"; "+getSegmentController()+"; CANNOT CONNECT "+ElementKeyMap.toString(toType)+" TO MAN-MOD-SINGLE: "+managerModule);
		}
	}

	public void onConnectionRemoved(Vector3i controller, short fromType, Vector3i controlled, short controlledType) {

		//		flagConnectionModified(ElementCollection.getIndex(controller), getState().getUpdateTime());
		flagTargetRecalc = true;
		if((fromType == STASH_ELEMENT || (ElementKeyMap.isToStashConnectable(fromType) && controlledType == STASH_ELEMENT)) || (fromType == Blocks.LOCK_BOX.getId() || (ElementKeyMap.isToStashConnectable(controlledType) && controlledType == Blocks.LOCK_BOX.getId()))) {
			//remove the controller as filtered inventory if it has no connection left
			long index = ElementCollection.getIndex(controller);
			LongOpenHashSet set = segmentController.getControlElementMap().getControllingMap().getAll().get(index);
			if(set == null || set.isEmpty()) {
				filterInventories.remove(ElementCollection.getIndex(controller));
			}
			if(controlledType != ElementKeyMap.CARGO_SPACE) {
				return;
			}
		}
		if(fromType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE && controlledType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) {
			long fromIndex = ElementCollection.getIndex(controller);
			if(stabilizerNodePath.containsKey(fromIndex)) {
				long l = stabilizerNodePath.get(fromIndex);
				if(l == ElementCollection.getIndex(controlled)) {
					stabilizerNodePath.remove(fromIndex);
				}
			}
			return;
		}
		if(controlledType == ElementKeyMap.SHIPYARD_CORE_POSITION) {
			System.err.println("[ManagerContainer] SHIPYARD CORE POSITION BLOCK CONNECTION REMOVED");
			return;
		}
		if(fromType == ElementKeyMap.CORE_ID && ElementKeyMap.getInfo(controlledType).isLightSource()) {
			this.colorCore.set(colorCoreDefault);
			return;
		}

		//		if(ElementKeyMap.getInfo(controlledType).isRailTrack()){
		//			return;
		//		}
		assert (modulesMap != null);

		assert (controlledType != Element.TYPE_NONE);

		if(!(ElementInformation.canBeControlledOld(fromType, controlledType) || ElementInformation.canBeControlled(fromType, controlledType) || (ElementKeyMap.isValidType(controlledType) && modulesMap.containsKey(controlledType)))) {
			if((state.getUpdateTime() - lastThrownInvalidConnectionException) > 3000) {
				try {
					throw new Exception("Object controllable status invalid: " + ElementKeyMap.toString(fromType) + "; " + ElementKeyMap.toString(controlledType));
				} catch(Exception e) {
//					e.printStackTrace();
				}
				lastThrownInvalidConnectionException = state.getUpdateTime();
			}
		}

		//				System.err.println("ON CONTROLLER REMOVED: "+controller+" -> "+controlled+";  "+ElementKeyMap.toString(fromType)+ "-> "+ElementKeyMap.toString(controlledType)+": "+modulesMap.get(controlledType));

		if(!ElementKeyMap.isValidType(fromType) || !ElementKeyMap.isValidType(controlledType)) {
			System.err.println("Exception: tried to remove controller to invalid type: " + ElementKeyMap.toString(fromType) + " -> " + ElementKeyMap.toString(controlledType) + ": " + controller + " -> " + controlled);
			return;
		}
		if(ElementKeyMap.getInfo(fromType).isRailSpeedActivationConnect(controlledType)) {
			//dont add activator modules to manager for rail speed. they are used as pure connection
			return;
		}
		if(!ElementKeyMap.isValidType(controlledType) || (!modulesMap.containsKey(controlledType) && !ElementKeyMap.getInfo(controlledType).isSignal() && !ElementKeyMap.getInfo(controlledType).isRailTrack() && !ElementKeyMap.getInfo(controlledType).canActivate())) {
			if(state.getNumberOfUpdate() - lastThrownInvalidConnectionException > 2000) {
				System.err.println("Exception: tried to remove controller: " + ElementKeyMap.toString(controlledType) + ": " + modulesMap + " for " + segmentController);
				lastThrownInvalidConnectionException = state.getNumberOfUpdate();
			}
			return;
		}

		ManagerModule<?, ?, ?> managerModule = modulesMap.get(controlledType);

		ManagerModule<?, ?, ?> managerModuleC = modulesControllerMap.get(fromType);
		if(managerModuleC != null && (managerModuleC.getElementManager() instanceof CargoCapacityElementManagerInterface)) {
			modulesControllerMap.get(STASH_ELEMENT).onConnectionRemoved(controller, controlled, controlledType);
		}

		if(ElementKeyMap.isValidType(fromType) && ElementKeyMap.getInfo(fromType).controlsAll()) {
			managerModule = modulesMap.get(Element.TYPE_ALL);
		}
		if(fromType == ElementKeyMap.SIGNAL_SENSOR) {
			managerModule = getSensor();
		}

		if(managerModule == null && ElementKeyMap.getInfo(fromType).isLightConnect(controlledType)) {
			//light got disconnected
			//no manager stuff to do
			return;
		}

		if(ElementKeyMap.getInfo(fromType).isCombiConnectAny(controlledType)) {
			assert (managerModuleC != null);
			managerModule = managerModuleC; //this was an effect/combi disconnect

		}
		if(managerModule == null && ElementKeyMap.getInfo(fromType).isRailSpeedTrackConnect(controlledType)) {
			managerModule = modulesMap.get(Element.TYPE_RAIL_TRACK);
			assert (managerModule != null);
		}
		if(managerModule == null && fromType == ElementKeyMap.SALVAGE_CONTROLLER_ID && (controlledType == STASH_ELEMENT || controlledType == Blocks.LOCK_BOX.getId())) {
			//storage got disconnected from salvage computer
			//nothing to do
			return;
		}

		if(managerModule == null && ElementKeyMap.getInfo(controlledType).isSignal()) {
			return;
		}

		if(managerModule == null) {
			if(state.getUpdateTime() - lastThrownInvalidConnectionException > 5000) {
				System.err.println("Exception: manager module null");
				try {
					throw new Exception("manager null (doesnt belong / maybe deprecated block module) " + ElementKeyMap.toString(fromType) + " -> " + ElementKeyMap.toString(controlledType) + "; canCon: " + ElementInformation.canBeControlled(fromType, controlledType) + "; ModuleMap.get(controlledType): " + modulesMap.containsKey(controlledType));
				} catch(Exception e) {
					e.printStackTrace();
				}
				lastThrownInvalidConnectionException = state.getUpdateTime();
			}
			return;
		}

		if(managerModule.getNext() != null) {
			while(managerModule.getNext() != null) {
				assert (managerModule instanceof ManagerModuleCollection<?, ?, ?>);
				ManagerModuleCollection<?, ?, ?> g = (ManagerModuleCollection<?, ?, ?>) managerModule;
				if(g.getControllerID() == fromType || ((g.getControllerID() == Element.TYPE_SIGNAL && ElementKeyMap.getInfo(fromType).isSignal()) || ((g.getControllerID() == Element.TYPE_RAIL_TRACK && ElementKeyMap.getInfo(fromType).isRailTrack())))) {
					break;
				}
				managerModule = managerModule.getNext();
			}
		}

		assert (managerModule != null) : controlledType + " -> " + ElementKeyMap.getInfo(controlledType).getName() + ": " + modulesMap;

		if(managerModule instanceof ManagerModuleCollection<?, ?, ?>) {
			//			System.err.println(" [MANAGER-CONTAINER] "+getState()+"; "+getSegmentController()+"; DISCONNECT "+controlledType+" TO MAN-MOD-COL: "+managerModule);
			managerModule.onConnectionRemoved(controller, controlled, controlledType);
		} else {
			//			System.err.println(" [MANAGER-CONTAINER] "+getState()+"; "+getSegmentController()+"; CANNOT DISCONNECT "+controlledType+" TO MAN-MOD-SINGLE: "+managerModule);
		}

	}

	public void onBlockDamage(long pos, short type, int damage, DamageDealerType damageType, Damager from) {
		for(int i = 0; i < hittableModules.size(); i++) {
			hittableModules.get(i).onHit(pos, type, damage, damageType);
		}

		if(isOnServer()) {
			powerInterface.onBlockDamageServer(from, damage, type, pos);
		}
		if(damage > 0) {
			flagBlockDamaged = true;
		}
	}

	public void onBlockKill(long pos, short type, Damager from) {
		for(int i = 0; i < killableBlockModules.size(); i++) {
			killableBlockModules.get(i).onKilledBlock(pos, type, from);
		}
		if(isOnServer() && ElementKeyMap.isValidType(type) && (type == ElementKeyMap.REACTOR_MAIN || type == ElementKeyMap.REACTOR_STABILIZER || type == ElementKeyMap.REACTOR_CONDUIT || ElementKeyMap.getInfo(type).isReactorChamberAny())) {
			powerInterface.onBlockKilledServer(from, type, pos);
			flagModuleKilled = true;
		}
		if(modulesMap.containsKey(type)) {
			flagModuleKilled = true;
		}
		flagBlockKilled = true;
	}

	public void onHitNotice() {
	}

	private void onInventoryRemove(Inventory remove, boolean preserveControl) {
		assert (isOnServer());
		if(!preserveControl) {

			if(segmentController.isOnServer()) {
				System.err.println("[MANAGERCONTAINER] REMOVING INVENTORY! (now spawning in space)" + segmentController.getState() + " - " + segmentController);
				remove.spawnInSpace(segmentController);
			}
			//on server the volume change happens as the items are spawned in space
		}
	}

	protected void onSpecialTypeAdd(short type, long absPos) {
		assert (isOnServer());
		if(type == STASH_ELEMENT || type == Blocks.LOCK_BOX.getId()) {
			// note: this is only the MANAGER being added
			addInventory(absPos, type);
			LongOpenHashSet set = segmentController.getControlElementMap().getControllingMap().getAll().get(absPos);
			if(set != null && set.size() > 0) {

				filterInventories.add(absPos);
			}
		}
	}

	public boolean isOnServer() {
		return onServer;
	}

	protected void onSpecialTypesRemove(short type, byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		if(type == STASH_ELEMENT || type == Blocks.LOCK_BOX.getId()) {
			filterInventories.remove(ElementCollection.getIndex(x, y, z));
			removeInventory(x, y, z, segment, preserveControl);
		}
	}

	private void removeInventory(byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		if(segmentController.isOnServer()) {
			System.err.println("[MANAGERCONTAINER] onRemovedElement REMOVING INVENTORY!! " + segmentController + "; " + segmentController.getState() + " preserveC: " + preserveControl);
			if(!preserveControl) {
				Vector3i absPos = segment.getAbsoluteElemPos(x, y, z, new Vector3i());
				Vector4i ePos = new Vector4i(absPos, preserveControl ? 1 : 0);
				if(inventories.containsKey(ElementCollection.getIndex(absPos)) && !delayedInventoryRemove.contains(ePos)) {
					delayedInventoryRemove.add(ePos);
				}
			} else {
				//only remove inventory when it's a user command
				//inventories are too dangerous to unload
				System.err.println("[SERVER] keeping inventory for now so it's not emptied by anything " + segmentController);
			}
		}
	}

	public void sendInventoryDelayed(Inventory inventory, int slot) {
		IntOpenHashSet intOpenHashSet = delayedCummulativeInventoryModsToSend.get(inventory);
		if(intOpenHashSet == null) {
			intOpenHashSet = new IntOpenHashSet();
			delayedCummulativeInventoryModsToSend.put(inventory, intOpenHashSet);
		}
		intOpenHashSet.add(slot);
	}

	public Tag getInventoryTag() {
		Tag[] invents;
		synchronized(inventories) {
			invents = new Tag[inventories.size() + 1];

			int c = 0;
			//			System.err.println("[SERVER][INVENTORY] writing inventories for "+getSegmentController()+"");
			for(Entry<Long, Inventory> entry : inventories.entrySet()) {
				Tag[] ent = new Tag[4];
				ent[0] = new Tag(Type.INT, null, entry.getValue().getLocalInventoryType());
				ent[1] = new Tag(Type.VECTOR3i, null, ElementCollection.getPosFromIndex(entry.getKey(), new Vector3i()));
				ent[2] = entry.getValue().toTagStructure();
				ent[3] = FinishTag.INST;
				invents[c] = new Tag(Type.STRUCT, null, ent);
				c++;
			}
			invents[inventories.size()] = FinishTag.INST;

		}
		Tag inventories = new Tag(Type.STRUCT, null, invents); //was "controllerStructure" in old version. no longer needed!
		return inventories;
	}

	public Tag toTagStructure() {

		Tag inventories = getInventoryTag();

		Tag distTag = null;
		distTag = new Tag(Type.INT, "shipMan0", 0);
		Tag powerTag = null;
		if(this instanceof PowerManagerInterface) {
			PowerAddOn p = ((PowerManagerInterface) this).getPowerAddOn();
			powerTag = p.toTagStructure();
		} else {
			powerTag = new Tag(Type.BYTE, null, 0);
		}
		Tag shieldTag = null;
		if(this instanceof ShieldContainerInterface) {
			if(isUsingPowerReactors()) {
				shieldTag = ((ShieldContainerInterface) this).getShieldAddOn().getShieldLocalAddOn().toTagStructure();
			} else {
				shieldTag = new Tag(Type.DOUBLE, "sh", ((ShieldContainerInterface) this).getShieldAddOn().getShields());
			}
		} else {
			shieldTag = new Tag(Type.BYTE, null, 0);
		}

		Tag actStateTag = ((SendableSegmentController) segmentController).getActivationStateTag();

		Tag texts = segmentController.getTextBlockTag();

		Tag relevantElementCountMap = getRelevantElementCountMap();

		Tag warpGateInfo = getWarpGateTag();

		Tag raceGateInfo = getRaceGateTag();

		Tag aiTag = segmentController.aiToTagStructure();

		//		Tag weapons = weapon.getElementManager().toTagStructure();
		//		return new Tag(Type.STRUCT, "shipMan0", new Tag[]{super.toTagStructure(), weapons, FinishTag.INST});

		Tag unloadedDummies = getUnloadedDummiesTag();

		Tag moduleExplTag = Tag.listToTagStruct(moduleExplosions, null);

		Tag powerReactorTag = powerInterface.toTagStructure();

		//INSERTED CODE
		Tag modTag = ModTagUtils.onTagSerialize(this.modModuleMap.values());
		///
		return new Tag(Type.STRUCT, "container", new Tag[]{inventories, distTag, powerTag, shieldTag, getExtraTag(), actStateTag, texts, relevantElementCountMap, warpGateInfo, getModuleTag(), aiTag, segmentController.getSlotAssignment().toTagStructure(), raceGateInfo, unloadedDummies, moduleExplTag, powerReactorTag, Tag.getByteTag((byte) segmentController.pullPermission.ordinal()),
				//INSERTED CODE
				//Insert mod data into tag structure
				modTag,
				 ///
				FinishTag.INST});
	}

	private Tag getUnloadedDummiesTag() {
		Tag[] dumm = new Tag[initialBlockMetaData.values().size() + 1];
		dumm[dumm.length - 1] = FinishTag.INST;

		int i = 0;
		for(BlockMetaDataDummy d : initialBlockMetaData.values()) {
			dumm[i] = d.toTagStructure();
			i++;
		}
		return new Tag(Type.STRUCT, segmentController.isLoadedFromChunk16() ? "c16" : null, dumm);
	}

	public Tag getModuleTag() {
		List<Tag> moduleTagList = new ObjectArrayList<Tag>();
		for(TagModuleUsableInterface uModule : tagModules) {
			moduleTagList.add(uModule.toTagStructure());
		}

		Tag moduleTags = null;
		Tag[] t = new Tag[moduleTagList.size() + 1];
		for(int i = 0; i < moduleTagList.size(); i++) {
			t[i] = moduleTagList.get(i);
		}
		t[t.length - 1] = FinishTag.INST;
		moduleTags = new Tag(Type.STRUCT, null, t);
		return moduleTags;
	}

	protected void fromWarpGateTag(Tag tag) {
	}

	protected Tag getWarpGateTag() {
		return new Tag(Type.BYTE, null, (byte) 0);
	}

	protected void fromRaceGateTag(Tag tag) {
	}

	protected Tag getRaceGateTag() {
		return new Tag(Type.BYTE, null, (byte) 0);
	}

	/**
	 * collect info about
	 * single collection modules (doors, shields)
	 * to make a prediction when loading on the HashSets.
	 * knowing the expected size will mean that there is only one
	 * allocation needed ano no rehashing
	 *
	 * @return
	 */
	private Tag getRelevantElementCountMap() {
		ShortArrayList l = new ShortArrayList();
		for(int i = 0; i < modules.size(); i++) {
			if(modules.get(i) instanceof ManagerModuleSingle<?, ?, ?>) {
				short elementID = ((ManagerModuleSingle<?, ?, ?>) modules.get(i)).getElementID();
				l.add(elementID);
			}
		}

		Tag[] tags = new Tag[l.size() + 1];
		tags[tags.length - 1] = FinishTag.INST;

		for(int i = 0; i < l.size(); i++) {
			Tag type = new Tag(Type.SHORT, null, l.get(i));
			int c = segmentController.getElementClassCountMap().get(l.get(i));
			Tag count = new Tag(Type.INT, null, c);
			tags[i] = new Tag(Type.STRUCT, null, new Tag[]{type, count, FinishTag.INST});
		}

		return new Tag(Type.STRUCT, null, tags);
	}

	private void readRelevantBlockCounts(Tag tag) {
		Tag[] tags = (Tag[]) tag.getValue();

		for(int i = 0; i < tags.length - 1; i++) {

			Tag[] typeCount = (Tag[]) tags[i].getValue();

			short type = (Short) typeCount[0].getValue();
			int count = (Integer) typeCount[1].getValue();
			if(ElementKeyMap.isValidType(type)) {
				ManagerModule<?, ?, ?> managerModule = modulesMap.get(type);
				if(managerModule instanceof ManagerModuleSingle<?, ?, ?>) {
					ManagerModuleSingle<?, ?, ?> m = (ManagerModuleSingle<?, ?, ?>) managerModule;
					if(count < 0) {
						System.err.println("[SERVER][LAOD] Exception: " + this.segmentController + " provided a negative value as expected block count for: " + ElementKeyMap.toString(type) + "; This file might have been messed with!");
						m.getCollectionManager().expected = 2;
					} else {
						m.getCollectionManager().expected = Math.max(2, count);
					}
					if(count > 128) {
						relevantMap.put(type, count);
					}
				}
			}
		}
	}

	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		for(int i = 0; i < receiverModules.size(); i++) {
			receiverModules.get(i).updateFromNT(segmentController.getNetworkObject());
		}

		if(segmentController.getNetworkObject() instanceof NTValueUpdateInterface) {
			ObjectArrayList<RemoteValueUpdate> receiveBuffer = ((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().getReceiveBuffer();
			if(!receiveBuffer.isEmpty()) {
				synchronized(valueUpdates) {
					for(int i = 0; i < receiveBuffer.size(); i++) {
						RemoteValueUpdate mam = receiveBuffer.get(i);
						valueUpdates.enqueue(mam.get());
					}
				}
			}
		}
		powerInterface.updateFromNetworkObject(o);

		handleInventoryFromNT(getInventoryInterface().getInventoriesChangeBuffer(), getInventoryInterface().getInventoryProductionBuffer(), getInventoryInterface().getInventoryProductionLimitBuffer(), getInventoryInterface().getInventoryFilterBuffer(), getInventoryInterface().getInventoryFillBuffer(), getInventoryInterface().getInventoryCustomNameModBuffer());

		if(!isOnServer() && !isInClientRange()) {
			((GameClientState) state).unloadedInventoryUpdates.enqueue(this);
		}

		for(int i = 0; i < segmentController.getNetworkObject().blockDelayTimers.getReceiveBuffer().size(); i += 2) {
			integrityUpdateDelay = segmentController.getNetworkObject().blockDelayTimers.getReceiveBuffer().get(i);
			repairDelay = segmentController.getNetworkObject().blockDelayTimers.getReceiveBuffer().get(i + 1);
		}

	}

	public boolean isInClientRange() {
		return segmentController.isInClientRange();
	}

	public void handleInventoryReceivedNT() {
		if(getInventoryInterface() != null && !ntInventoryRemoves.isEmpty()) {
			synchronized(ntInventoryRemoves) {
				while(!ntInventoryRemoves.isEmpty()) {
					Inventory inventory = ntInventoryRemoves.dequeue();
					//make sure to select actual removed inventory
					inventory = inventories.remove(inventory.getParameterIndex());
					if(!isOnServer() && inventory != null) {
						volumeChanged(inventory.getVolume(), 0);
						if(inventory instanceof StashInventory && ((StashInventory) inventory).getCustomName() != null && ((StashInventory) inventory).getCustomName().length() > 0) {
							namedInventoriesClient.remove(inventory.getParameterIndex());
							namedInventoriesClientChanged = true;
						}
					}
				}
			}
		}
		if(!segmentController.isOnServer()) {
			for(int i = 0; i < inventories.inventoriesList.size(); i++) {
				inventories.inventoriesList.get(i).clientUpdate();
			}
		}
		if(getInventoryInterface() != null && !ntInventoryMods.isEmpty()) {
			synchronized(ntInventoryMods) {
				while(!ntInventoryMods.isEmpty()) {
					Inventory inventory = ntInventoryMods.dequeue();
					if(inventory instanceof ShopInventory) {
						handleShopInventoryReceived((ShopInventory) inventory);
					} else {
						if(!isOnServer() && inventory instanceof StashInventory && ((StashInventory) inventory).getCustomName() != null && ((StashInventory) inventory).getCustomName().length() > 0) {
							namedInventoriesClient.put(inventory.getParameterIndex(), ((StashInventory) inventory));
							namedInventoriesClientChanged = true;
						}

						inventories.put(inventory.getParameterIndex(), inventory);
						if(!segmentController.isOnServer()) {
							inventory.requestMissingMetaObjects();

							DelayedInvProdSet prod = invProdMapDelayed.remove(inventory.getParameterIndex());
							if(prod != null && inventory instanceof StashInventory) {
								if(prod.inventoryProduction != 0) {
									getInventoryInterface().getInventoryProductionBuffer().add(ElementCollection.getIndex4(inventory.getParameterIndex(), prod.inventoryProduction));
								}
								if(prod.inventoryFilter != null) {

									for(Entry<Short, Integer> e : prod.inventoryFilter.entrySet()) {
										getInventoryInterface().getInventoryFilterBuffer().add(new RemoteShortIntPair(new ShortIntPair(inventory.getParameterIndex(), e.getKey(), e.getValue()), segmentController.isOnServer()));
									}
								}
								if(prod.inventoryProductionLimit != 0) {
									getInventoryInterface().getInventoryProductionLimitBuffer().add(new RemoteLongIntPair(new LongIntPair(inventory.getParameterIndex(), prod.inventoryProductionLimit), segmentController.isOnServer()));
								}
								if(prod.inventoryFillUpFilters != null) {

									for(Entry<Short, Integer> e : prod.inventoryFillUpFilters.entrySet()) {
										getInventoryInterface().getInventoryFillBuffer().add(new RemoteShortIntPair(new ShortIntPair(inventory.getParameterIndex(), e.getKey(), e.getValue()), segmentController.isOnServer()));
									}
								}
							}

						}
					}
					//					System.err.println("ADDED INVENTORY: "+inventory.getParameter()+" on "+getSegmentController().getState()+": "+inventory);
				}
			}
		}
		if(!ntInventorySlotRemoveMods.isEmpty()) {
			synchronized(ntInventorySlotRemoveMods) {
				while(!ntInventorySlotRemoveMods.isEmpty()) {
					InventorySlotRemoveMod a = ntInventorySlotRemoveMods.dequeue();

					if(a.parameter == Long.MIN_VALUE) {
						handleGlobalInventorySlotRemove(a);
					} else {
						Inventory inventory = getInventory(a.parameter);

						if(inventory != null) {
							boolean send = isOnServer();
							inventory.removeSlot(a.slot, send);
						}
					}

				}
			}
		}
		if(!ntInventoryMultMods.isEmpty()) {
			synchronized(ntInventoryMultMods) {
				List<InventoryMultMod> failed = new ObjectArrayList<InventoryMultMod>();
				while(!ntInventoryMultMods.isEmpty()) {
					InventoryMultMod a = ntInventoryMultMods.dequeue();
					if(a.parameter == Long.MIN_VALUE) {
						handleGlobalInventory(a);
					} else {
						Inventory inventory = getInventory(a.parameter);

						if(inventory != null) {
							if(isOnServer()) {
								//this is chest modifications from client by taking stuff or putting stuff
								boolean emptyBef = inventory.isEmpty();
								boolean fullBef = inventory.isAlmostFull();

								inventory.handleReceived(a, getInventoryInterface());

								boolean emptyAft = inventory.isEmpty();
								boolean fullAft = inventory.isAlmostFull();

								if(!emptyBef && emptyAft) {
									//nonempty -> empty
									sendConnected(inventory.getParameterIndex(), false, ElementKeyMap.SIGNAL_OR_BLOCK_ID);
								} else if(emptyBef && !emptyAft) {
									//empty -> nonempty
									sendConnected(inventory.getParameterIndex(), true, ElementKeyMap.SIGNAL_OR_BLOCK_ID);
								} else if(!fullBef && fullAft) {
									//nonfull -> full
									sendConnected(inventory.getParameterIndex(), false, ElementKeyMap.ACTIVAION_BLOCK_ID);
								} else if(fullBef && !fullAft) {
									//full -> nonfull
									sendConnected(inventory.getParameterIndex(), true, ElementKeyMap.ACTIVAION_BLOCK_ID);
								}
							} else {
								inventory.handleReceived(a, getInventoryInterface());
							}

						} else {
							if(!segmentController.isOnServer() && isInClientRange()) {
								SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(a.parameter);
								if(pointUnsave == null) {
									failed.add(a);
								}
								System.err.println("[MANAGERCONTAINER] Exc: NOT FOUND MULT INVENTORY (received mod): " + segmentController + " - " + a.parameter + "; destination: " + pointUnsave + "; " + inventories.keySet());
							}

							assert (!segmentController.isOnServer());
						}
					}
				}
				if(!failed.isEmpty()) {
					while(!failed.isEmpty()) {
						ntInventoryMultMods.enqueue(failed.remove(0));
					}
				}
			}
		}
		if(!ntInventoryProdMods.isEmpty()) {
			synchronized(ntInventoryProdMods) {
				while(!ntInventoryProdMods.isEmpty()) {
					long v = ntInventoryProdMods.dequeueLong();
					Vector3i pos = ElementCollection.getPosFromIndex(v, new Vector3i());
					short type = (short) ElementCollection.getType(v);
					Inventory inventory = getInventory(pos);
					if(inventory != null && inventory instanceof StashInventory) {

						((StashInventory) inventory).setProduction(type);

						if(segmentController.isOnServer()) {
							getInventoryInterface().getInventoryProductionBuffer().add(v);
						}
					}
				}
			}
		}
		if(!ntInventoryProdLimitMods.isEmpty()) {
			synchronized(ntInventoryProdLimitMods) {
				while(!ntInventoryProdLimitMods.isEmpty()) {
					LongIntPair v = ntInventoryProdLimitMods.dequeue();
					Vector3i pos = ElementCollection.getPosFromIndex(v.l, new Vector3i());
					Inventory inventory = getInventory(pos);
					if(inventory != null && inventory instanceof StashInventory) {

						((StashInventory) inventory).setProductionLimit(v.i);

						if(segmentController.isOnServer()) {
							getInventoryInterface().getInventoryProductionLimitBuffer().add(new RemoteLongIntPair(v, isOnServer()));
						}
					}
				}
			}
		}
		if(!ntInventoryCustomNameMods.isEmpty()) {
			synchronized(ntInventoryCustomNameMods) {
				while(!ntInventoryCustomNameMods.isEmpty()) {
					LongStringPair v = ntInventoryCustomNameMods.dequeue();
					Vector3i pos = ElementCollection.getPosFromIndex(v.longVal, new Vector3i());
					Inventory inventory = getInventory(pos);
					if(inventory != null && inventory instanceof StashInventory) {

						((StashInventory) inventory).setCustomName(v.stringVal);

						if(segmentController.isOnServer()) {
							getInventoryInterface().getInventoryCustomNameModBuffer().add(new RemoteLongString(v, true));
						} else {
							if(v.stringVal.length() > 0) {
								namedInventoriesClient.put(v.longVal, ((StashInventory) inventory));
							} else {
								namedInventoriesClient.remove(v.longVal);
							}
							namedInventoriesClientChanged = true;
						}
					}
				}
			}
		}
		if(!ntInventoryFilterMods.isEmpty()) {
			synchronized(ntInventoryFilterMods) {
				while(!ntInventoryFilterMods.isEmpty()) {
					ShortIntPair v = ntInventoryFilterMods.dequeue();
					Vector3i pos = ElementCollection.getPosFromIndex(v.pos, new Vector3i());
					short type = v.type;
					int count = v.count;
					Inventory inventory = getInventory(pos);
					if(inventory != null && inventory instanceof StashInventory) {

						if(count == 0) {
							((StashInventory) inventory).getFilter().filter.remove(type);
						} else {
							assert (count > 0);
							((StashInventory) inventory).getFilter().filter.put(type, count);
						}

						if(segmentController.isOnServer()) {
							getInventoryInterface().getInventoryFilterBuffer().add(new RemoteShortIntPair(v, true));
						}
					}
				}
			}
		}
		if(!ntInventoryFillMods.isEmpty()) {
			synchronized(ntInventoryFillMods) {
				while(!ntInventoryFillMods.isEmpty()) {
					ShortIntPair v = ntInventoryFillMods.dequeue();
					Vector3i pos = ElementCollection.getPosFromIndex(v.pos, new Vector3i());
					short type = v.type;
					int count = v.count;
					Inventory inventory = getInventory(pos);
					if(inventory != null && inventory instanceof StashInventory) {

						if(count == 0) {
							((StashInventory) inventory).getFilter().fillUpTo.remove(type);
						} else {
							assert (count > 0);
							((StashInventory) inventory).getFilter().fillUpTo.put(type, count);
						}

						if(segmentController.isOnServer()) {
							getInventoryInterface().getInventoryFillBuffer().add(new RemoteShortIntPair(v, true));
						}
					}
				}
			}
		}
		if(!clientInventoryActions.isEmpty()) {
			assert (isOnServer());
			Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();
			synchronized(clientInventoryActions) {
				while(!clientInventoryActions.isEmpty()) {
					try {
						InventoryClientAction d = clientInventoryActions.dequeue();

						assert (d.ownInventoryOwnerId == getId());

						Sendable s = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(d.otherInventoryOwnerId);
						if(s != null) {
							InventoryHolder holder;
							if(s instanceof ManagedSegmentController<?>) {
								holder = ((ManagedSegmentController<?>) s).getManagerContainer();
							} else {
								holder = (InventoryHolder) s;
							}

							Inventory inventory = getInventory(d.ownInventoryPosId);
							if(inventory != null) {
								inventory.doSwitchSlotsOrCombine(d.slot, d.otherSlot, d.subSlot, holder.getInventory(d.otherInventoryPosId), d.count, moddedSlots);
							} else {
								assert (false);
							}

						} else {
							assert (false);
						}

					} catch(InventoryExceededException e) {
						e.printStackTrace();
					}
				}
			}
			if(!moddedSlots.isEmpty()) {
				for(Entry<Inventory, IntOpenHashSet> e : moddedSlots.entrySet()) {
					e.getKey().sendInventoryModification(e.getValue());
				}
			}
		}
	}

	private void addRechModules() {
		while(!rechargableSingleModulesToAdd.isEmpty()) {
			RecharchableSingleModule d = rechargableSingleModulesToAdd.dequeue();
			addPlayerUsable(d);
			addConsumer(d);
			addTagUsable(d);
			addUpdatable(d);

		}
	}

	public void updateLocal(Timer timer) {
		state.getDebugTimer().start(segmentController, "ManagerContainerUpdate");

		// T269 Handle removes before additions, to fix inventories being immediately
		// 	removed after being added when blocks are replaced with another inventory
		long t = System.currentTimeMillis();

		final int uMod = updatable.size();
		for(int i = 0; i < uMod; i++) {
			ManagerUpdatableInterface m = updatable.get(i);
			if(m.canUpdate()) {
				m.update(timer);
			} else {
				m.onNoUpdate(timer);
			}
		}

		if(integrityUpdateDelay > 0f) {
			integrityUpdateDelay = Math.max(0f, integrityUpdateDelay - timer.getDelta());
		}
		if(repairDelay > 0f) {
			repairDelay = Math.max(0f, repairDelay - timer.getDelta());
		}
		if(flagBlockDamaged) {

			repairDelay = RepairElementManager.REPAIR_OUT_OF_COMBAT_DELAY_SEC;
			flagBlockDamaged = false;
		}
		if(flagBlockKilled) {
			repairDelay = RepairElementManager.REPAIR_OUT_OF_COMBAT_DELAY_SEC;
			flagBlockKilled = false;
		}
		if(flagModuleKilled) {
			integrityUpdateDelay = VoidElementManager.COLLECTION_INTEGRITY_UNDER_FIRE_UPDATE_DELAY_SEC;
			flagModuleKilled = false;
		}
		if(flagBlockAdded) {
			if(segmentController.isFullyLoadedWithDock()) {
				//reset integrity delay when ship was already loaded and a block got added
				integrityUpdateDelay = 0;
			}
			flagBlockAdded = false;
		}
		if(flagElementCollectionChanged) {
			if(isOnServer()) {
				segmentController.getRuleEntityManager().triggerOnCollectionUpdate();
			}
			flagElementCollectionChanged = false;
		}
		if(!manualEvents.isEmpty()) {
			for(int i = 0; i < manualEvents.size(); i++) {
				ManualMouseEvent e = manualEvents.get(i);
				if(!e.sent && !isOnServer()) {
					NetworkSegmentProvider networkObject = ((ClientSegmentProvider) segmentController.getSegmentProvider()).getSendableSegmentProvider().getNetworkObject();
					e.send(networkObject);
				}
				e.sent = true;
				boolean remove = e.execute(this, timer);
				if(remove) {
					manualEvents.remove(i--);
				}
			}
		}

		if(!this.receivedBeamLatches.isEmpty()) {
			while(!this.receivedBeamLatches.isEmpty()) {
				ReceivedBeamLatch d = this.receivedBeamLatches.dequeue();
				for(BeamElementManager<?, ?, ?> e : beamModules) {
					boolean b = e.handleBeamLatch(d);
					if(b) {
						break;
					}
				}
			}
		}

		updateConnectionModified(timer.currentTime);
		while(!((SendableSegmentController) segmentController).receivedBlockMessages.isEmpty()) {
			assert (!isOnServer());
			ServerMessage s = ((SendableSegmentController) segmentController).receivedBlockMessages.dequeue();
			((GameClientState) state).getPlayer().handleReceivedBlockMsg(this, s);
		}
		if(!blockActivationsForModules.isEmpty()) {
			ObjectIterator<ManagerContainer<E>.BlockAct> it = blockActivationsForModules.values().iterator();

			while(it.hasNext()) {
				ManagerContainer<E>.BlockAct next = it.next();
				unitSingle.block = next.block;
				next.module.handle(unitSingle, timer);

				if(isOnServer()) {

					if(next.trigger != Long.MIN_VALUE) {
						//this is a toggle, so we wait until the connected activation block deactivates
						SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(next.trigger, tmp);
						if(pointUnsave != null) {
							if(!pointUnsave.isValid() || pointUnsave.isDead() || !pointUnsave.isActive()) {
								it.remove();
							}
						}
					} else if(timer.currentTime - next.timeStarted > 500) {

						//on deactivate. set the block active flag to false to stop the client from executing
						SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(next.block, tmp);
						if(pointUnsave != null) {
							pointUnsave.setActive(false);
							((SendableSegmentController) segmentController).sendBlockActiveChanged(ElementCollection.getPosX(next.block), ElementCollection.getPosY(next.block), ElementCollection.getPosZ(next.block), pointUnsave.isActive());
						}
						it.remove();
					}

				} else {
					//on client stop executing once the block is no longer active

					SegmentPiece pointUnsave = segmentController.getSegmentBuffer().getPointUnsave(next.block, tmp);
					if(pointUnsave != null && !pointUnsave.isActive()) {
						it.remove();
					}
				}
			}
		}

		for(int i = 0; i < moduleExplosions.size(); i++) {
			ModuleExplosion moduleExplosion = moduleExplosions.get(i);
			if(!moduleExplosion.isFinished()) {
				moduleExplosion.update(timer, segmentController);
			}
			if(moduleExplosion.isFinished()) {
				moduleExplosions.remove(i);
				i--;
			}
		}

		if(!inventoryDetailRequests.isEmpty()) {
			while(!inventoryDetailRequests.isEmpty()) {
				InventoryDetailRequest d = inventoryDetailRequests.dequeue();
				for(long invPos : d.requested) {
					Inventory inv = getInventory(invPos);
					if(inv != null) {
						inv.receivedFilterRequest(d.prov);
					} else {
						System.err.println("[MANAGERCONTAINER] WARNING: " + segmentController + " Inventory Detail request: ID not found " + invPos);
					}
				}
			}
		}
		while(!effectChangeQueue.isEmpty()) {
			EffectChangeHanlder dequeue = effectChangeQueue.dequeue();
			dequeue.onEffectChanged();
		}
		if(segmentController.itemsToSpawnWith != null) {
			if(itemToSpawnCheckStart <= 0) {
				itemToSpawnCheckStart = timer.currentTime;
			}
			if(timer.currentTime - itemToSpawnCheckStart > 8000) {
				ObjectArrayList<Inventory> invs = new ObjectArrayList<Inventory>(inventories.inventoriesList);

				Collections.sort(invs, (o1, o2) -> {
					double i1 = o1.getCapacity() - o1.getVolume();
					double i2 = o2.getCapacity() - o2.getVolume();
					//biggest first
					return CompareTools.compare(i2, i1);
				});
				for(Inventory r : invs) {

					if(r.getCapacity() - r.getVolume() > 1) {
						IntOpenHashSet changed = new IntOpenHashSet();
						long totalAmountBef = segmentController.itemsToSpawnWith.getTotalAmount();
						r.incAndConsume(segmentController.itemsToSpawnWith, changed);
						long totalAmount = segmentController.itemsToSpawnWith.getTotalAmount();
						System.err.println("[SERVER][SPAWNWITHCARGO] PUT IN " + (totalAmountBef - totalAmount) + "; CHANGED SLOTS " + changed.size());
						if(changed.size() > 0) {
							r.sendInventoryModification(changed);
						}

						if(segmentController.itemsToSpawnWith.getExistingTypeCount() == 0) {
							break;
						}
					}
				}
				System.err.println("[SERVER][SPAWNWITHCARGO] CARGO TYPES STILL TO SPAWN: " + segmentController.itemsToSpawnWith.getExistingTypeCount());
				if(segmentController.itemsToSpawnWith.getExistingTypeCount() == 0 || timer.currentTime - itemToSpawnCheckStart > 30000) {
					//				assert(false);
					segmentController.itemsToSpawnWith = null;
				}
			}
		}

		if(!valueUpdates.isEmpty()) {
			ObjectArrayList<ValueUpdate> failed = new ObjectArrayList<ValueUpdate>();
			synchronized(valueUpdates) {
				while(!valueUpdates.isEmpty()) {
					ValueUpdate dequeue = valueUpdates.dequeue();
					//					System.err.println("VALUE UPDATE RECEIVED");
					boolean applyClient = dequeue.applyClient(this);
					//					System.err.println("REC:::: "+dequeue);
					if(!applyClient && dequeue.failedCount < 50) {
						dequeue.failedCount++;
						dequeue.lastTry = timer.currentTime;
						failed.add(dequeue);
					} else if(isOnServer() && dequeue.deligateToClient) {
						((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(dequeue, segmentController.isOnServer()));
					}
				}
				for(int i = 0; i < failed.size(); i++) {
					valueUpdates.enqueue(failed.get(i));
				}

			}
		}

		if(!delayedCummulativeInventoryModsToSend.isEmpty()) {
			for(Entry<Inventory, IntOpenHashSet> a : delayedCummulativeInventoryModsToSend.entrySet()) {
				a.getKey().sendInventoryModification(a.getValue());
			}
			delayedCummulativeInventoryModsToSend.clear();
		}

		handleInventoryReceivedNT();

		handleFilterInventories(timer);

		final int sMod = modules.size();

		while(!checkUpdatableQueue.isEmpty()) {
			UsableElementManager<?, ?, ?> man = checkUpdatableQueue.dequeue();
			ManagerModule<?, ?, ?> managerModule = em2modulesMap.get(man);
			assert (managerModule != null) : man;
			if(managerModule.needsAnyUpdate()) {
				if(!updateModules.contains(managerModule)) {
					updateModules.add(managerModule);
				}
			} else {
				updateModules.remove(managerModule);
			}
		}

		for(int i = 0; i < updateModules.size(); i++) {
			updateModules.get(i).update(timer, timer.currentTime);
		}

		boolean inventoriesChanged = false;
		// T269 Handle removes before additions, to fix inventories being immediately
		// 	removed after being added when blocks are replaced with another inventory
		while(!delayedInventoryRemove.isEmpty()) {
			Vector4i remove = delayedInventoryRemove.remove(0);
			announceInventory(ElementCollection.getIndex(remove.x, remove.y, remove.z), remove.w == 1, null, false);
			inventoriesChanged = true;
		}
		while(!delayedInventoryAdd.isEmpty()) {

			Inventory adding = delayedInventoryAdd.remove(0);

			if(!inventories.containsKey(adding.getParameterIndex())) {
				if(segmentController.isOnServer()) {
					System.err.println("[SERVER] " + segmentController + " ADDING NEW INVENTORY " + adding.getParameter());
				} else {
					System.err.println("[CLIENT] " + segmentController + " ADDING NEW INVENTORY " + adding.getParameter());
				}
				announceInventory(adding.getParameterIndex(), false, adding, true);
				inventoriesChanged = true;
			}
		}
		if(inventoriesChanged) {
			activeInventories.clear();
			for(Inventory i : inventories.values()) {
				// this will only fire for the server!
				if(i instanceof ActiveInventory) {
					activeInventories.add((ActiveInventory) i);
				}
			}
		} else {
			if(segmentController.isOnServer()) {
				// skip when update was done,
				// so timer.delta won't fuck up
				// on the first run after load
				for(ActiveInventory i : activeInventories) {
					// this will only happen on server
					if(i.isActivated()) {
						i.updateLocal(timer);
					}
				}
			}
		}
		if(flagAnyBlockAdded > 0 && state.getUpdateTime() > flagAnyBlockAdded + BLOCK_ADD_REMOVE_LISTEN_DELAY) {
			for(int i = 0; i < changeListenModules.size(); i++) {
				changeListenModules.get(i).onAddedAnyElement();
			}
			flagAnyBlockAdded = -1;
		}
		if(flagAnyBlockRemoved > 0 && state.getUpdateTime() > flagAnyBlockRemoved + BLOCK_ADD_REMOVE_LISTEN_DELAY) {
			for(int i = 0; i < changeListenModules.size(); i++) {
				changeListenModules.get(i).onRemovedAnyElement();
			}
			flagAnyBlockRemoved = -1;
		}
		long tDone = System.currentTimeMillis();

		long taken = tDone - t;
		if(taken > 10) {
			System.err.println(segmentController.getState() + " MANAGER_CONTAINER TOOK TOO LONG: " + taken + "ms; " + getName() + "; " + this);
		}
		state.getDebugTimer().end(segmentController, "ManagerContainerUpdate");

		//INSERTED CODE
		for(ModManagerContainerModule value : modModuleMap.values()) {
			value.handle(timer);
		}
		///
	}

	protected void handleShopInventoryReceived(ShopInventory inventory) {

	}

	public void sendFullDestinationUpdate() {
		if(this instanceof ActivationManagerInterface) {
			ActivationManagerInterface a = (ActivationManagerInterface) this;

			for(int i = 0; i < a.getActivation().getCollectionManagers().size(); i++) {
				ActivationCollectionManager aa = a.getActivation().getCollectionManagers().get(i);
				if(aa.getDestination() != null) {
					DestinationValueUpdate destUpdate = new DestinationValueUpdate();
					destUpdate.setServer(((ManagedSegmentController<?>) segmentController).getManagerContainer(), aa.getControllerElement().getAbsoluteIndex());

					((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(destUpdate, segmentController.isOnServer()));
				}
			}
		}
		segmentController.getNetworkObject().additionalBlueprintData.set(true);
	}

	private void handleDockedInventories(Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> changedMap) {
		SegmentPiece docked = segmentController.railController.previous.docked;
		SegmentPiece[] dockedToArray = segmentController.railController.previous.getCurrentRailContactPiece(new SegmentPiece[6]);
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmDocker = segmentController.getControlElementMap().getControllingMap().get(docked.getAbsoluteIndex());

		FastCopyLongOpenHashSet dockerStashed;
		if(mmDocker != null && ((dockerStashed = mmDocker.get(STASH_ELEMENT)) != null || (dockerStashed = mmDocker.get(Blocks.LOCK_BOX.getId())) != null) && dockerStashed.size() > 0) {
			SegmentPiece tmp = new SegmentPiece();
			for(long dockStashInv : dockerStashed) {

				SegmentPiece stashOnDockedPoint = segmentController.getSegmentBuffer().getPointUnsave(dockStashInv, tmp);

				if(stashOnDockedPoint != null) {

					Inventory inventory = ((InventoryHolder) stashOnDockedPoint.getSegmentController()).getInventories().get(stashOnDockedPoint.getAbsoluteIndex());

					if(inventory != null) {
						for(int i = 0; i < dockedToArray.length; i++) {
							SegmentPiece railBlock = dockedToArray[i];
							if(railBlock != null && ElementKeyMap.isRailLoadOrUnload(railBlock.getType())) {

								Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmTotRail = railBlock.getSegmentController().getControlElementMap().getControllingMap().get(railBlock.getAbsoluteIndex());
								FastCopyLongOpenHashSet rInvs;
								if(mmTotRail != null && ((rInvs = mmTotRail.get(STASH_ELEMENT)) != null || (rInvs = mmTotRail.get(Blocks.LOCK_BOX.getId())) != null)) {

									for(long rInv : rInvs) {

										Inventory otherInv = ((InventoryHolder) railBlock.getSegmentController()).getInventories().get(ElementCollection.getPosIndexFrom4(rInv));

										if(otherInv != null) {
											SegmentPiece rInvBlock = railBlock.getSegmentController().getSegmentBuffer().getPointUnsave(rInv);

											if(rInvBlock != null && stashOnDockedPoint.isActive() && rInvBlock.isActive()) {

												if(railBlock.getType() == ElementKeyMap.RAIL_LOAD) {

													//docker pulls

													/*
													 * docker is always allowed
													 * if the rail permission to
													 * dock exists and storage
													 * is connected this way,
													 * there is no saving of
													 * multiple allowed ships
													 * needed if more then one
													 * is leeching atm
													 */
													boolean transferOk = doInventoryTransferFrom((StashInventory) inventory, otherInv, changedMap);

													((ManagerContainer<?>) inventory.getInventoryHolder()).sendConnected(inventory.getParameterIndex(), transferOk, ElementKeyMap.ACTIVAION_BLOCK_ID);

												} else if(railBlock.getType() == ElementKeyMap.RAIL_UNLOAD) {

													//rail pulls

													if(docked.getSegmentController().isAllowedToTakeItemsByRail(railBlock.getSegmentController())) {
														boolean transferOk = doInventoryTransferFrom((StashInventory) otherInv, inventory, changedMap);

														((ManagerContainer<?>) otherInv.getInventoryHolder()).sendConnected(otherInv.getParameterIndex(), transferOk, ElementKeyMap.ACTIVAION_BLOCK_ID);
													}
												}
											}
										}
									}
								}

							}
						}
					}
				}
			}
		}
	}

	private void handleFilterInventories(Timer timer) {
		if(!isOnServer()) {
			return;
		}

		long currentStep = segmentController.getState().getController().getServerRunningTime() / TIME_STEP_STASH_PULL;
		if(currentStep > lastInventoryFilterStep) {
			SegmentPiece tmp = new SegmentPiece();
			SegmentPiece tmpCon = new SegmentPiece();
			Vector3i tmpPos = new Vector3i();
			//			System.err.println("INVENTORY STEP "+getSegmentController()+filterInventories.size());
			Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> changedMap = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();

			if(segmentController.railController.isDocked()) {
				handleDockedInventories(changedMap);
			}
			boolean transferOk = false;

			/// INSERTED CODE @...
			for(StorageItemPullListener listener : FastListenerCommon.storageItemPullListeners)
				listener.onItemPullChecks(this, filterInventories);
			///...
			for(long f : filterInventories) {

				Inventory pullTo;
				if((pullTo = getInventory(f)) != null && pullTo.getFilter().filter.size() > 0) {

					SegmentPiece pointUnsave = pullTo.getBlockIfPossible();
					if(pointUnsave != null && pointUnsave.isActive()) {
						assert (pullTo instanceof StashInventory);
						/// INSERTED CODE @...
						for(StorageItemPullListener listener : FastListenerCommon.storageItemPullListeners)
							listener.onPreItemPull((StashInventory) pullTo, pointUnsave, transferOk);
						///...
						boolean fullBefore = pullTo.isAlmostFull();
						boolean emptyBefore = pullTo.isEmpty();

						StashInventory pullToStash = (StashInventory) pullTo;
						ShortArrayList types = ElementKeyMap.inventoryTypes;
						Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmap = segmentController.getControlElementMap().getControllingMap().get(f);

						if(mmap != null) {
							for(short type : types) {
								//use per type, so any enhancers/cargo is not cycled through
								LongOpenHashSet connected = mmap.get(type);
								if(connected != null) {
									for(long con : connected) {
										SegmentPiece conPiece = segmentController.getSegmentBuffer().getPointUnsave(con, tmpCon);

										if(conPiece != null) {
											Inventory pullFromInv = ((InventoryHolder) conPiece.getSegmentController()).getInventory(conPiece.getAbsoluteIndex());
											if(pullFromInv != null) {
												long timeTransfer = System.currentTimeMillis();
												transferOk = doInventoryTransferFrom(pullToStash, pullFromInv, changedMap);
												long takenTrasfer = System.currentTimeMillis() - timeTransfer;
												if(takenTrasfer > 50) {
													System.err.println("[SERVER] " + segmentController + "; Inventory transfer pull took: " + takenTrasfer + "ms; ");
												}
											}
										}
									}
								}
							}
						}

						boolean fullAfter = pullTo.isAlmostFull();
						boolean emptyAfter = pullTo.isEmpty();

						//						System.err.println("PULLER: "+pullTo);
						//						System.err.println("FULL  ::::::: "+fullBefore+" -> "+fullAfter);
						//						System.err.println("EMPTY ::::::: "+emptyBefore+" -> "+emptyAfter);

						sendConnected(f, transferOk, ElementKeyMap.ACTIVAION_BLOCK_ID);
						if(emptyBefore && !emptyAfter) {
							sendConnected(f, true, ElementKeyMap.SIGNAL_OR_BLOCK_ID);
						}
						/// INSERTED CODE @...
						for(StorageItemPullListener listener : FastListenerCommon.storageItemPullListeners)
							listener.onPostItemPull((StashInventory) pullTo, pointUnsave, transferOk);
						///...
					}
				}
			}
			for(Entry<Inventory, IntOpenHashSet> e : changedMap.entrySet()) {
				e.getKey().sendInventoryModification(e.getValue());
			}

			lastInventoryFilterStep = currentStep;
		}
	}

	private class PullInvHanlder implements TypeAmountLoopHandle {
		private StashInventory pullTo;
		private Inventory pullFrom;
		private IntOpenHashSet changedSlotsOthers;
		private IntOpenHashSet ownChanged;
		private long nanoTotalBatch = 0;
		private long nanoTotalInc = 0;
		private boolean transferOk = true;

		public void set(StashInventory pullTo, Inventory pullFrom, Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> changedMap, IntOpenHashSet changedSlotsOthers, IntOpenHashSet ownChanged, long nanoTotalBatch, long nanoTotalInc, boolean transferOk) {
			this.pullTo = pullTo;
			this.pullFrom = pullFrom;
			this.changedSlotsOthers = changedSlotsOthers;
			this.ownChanged = ownChanged;
			this.nanoTotalBatch = nanoTotalBatch;
			this.nanoTotalInc = nanoTotalInc;
			this.transferOk = transferOk;
		}

		@Override
		public void handle(short type, int count) {
			//			short type = pullToFilter.getTypes().getShort(i);
			//			int count = pullToFilter.get(type);
			if(type > 0) {

				int overallQuantityInOther = pullFrom.getOverallQuantity(type);

				if(count > overallQuantityInOther) {
					count = overallQuantityInOther;
				}
				int amountToFill = 0;
				int pullUpTo = pullTo.getFilter().fillUpTo.get(type);
				if(pullUpTo > 0) {
					int overallQuantityInToPull = pullTo.getOverallQuantity(type);

					if(overallQuantityInToPull >= pullUpTo) {
						//don't pull
						return;
					} else {
						amountToFill = pullUpTo - overallQuantityInToPull;
						count = Math.min(amountToFill, count);
					}

				}
				if(count > 0) {
					if(pullTo.canPutIn(type, count)) {
						//							pullFrom.deleteAllSlotsWithType(type, changedSlotsOthers);

						//							int slot = pullFrom.incExistingOrNextFreeSlot(type, overallQuantity - count);
						long n = System.nanoTime();
						pullFrom.decreaseBatch(type, count, changedSlotsOthers);
						nanoTotalBatch += (System.nanoTime() - n);
						//							changedSlotsOthers.add(slot);

						n = System.nanoTime();
						int oSlot = pullTo.incExistingOrNextFreeSlot(type, count);
						nanoTotalInc += (System.nanoTime() - n);
						ownChanged.add(oSlot);
					} else {
						transferOk = false;
					}
				}
			} else if(type < 0) {
				short metaId = 0;
				short subId = -1;
				if(type > -256) {
					metaId = type;
				} else {
					metaId = (short) -(Math.abs(type) / 256);
					subId = (short) ((Math.abs(type) % 256) + metaId);
				}

				int firstMetaItemByType = pullFrom.getFirstMetaItemByType(metaId, subId);

				if(firstMetaItemByType >= 0) {
					if(!pullTo.isOverCapacity(((MetaObjectState) getState()).getMetaObjectManager().getVolume(pullFrom.getMeta(firstMetaItemByType)))) {
						int meta = pullFrom.getMeta(firstMetaItemByType);

						pullFrom.put(firstMetaItemByType, (short) 0, 0, -1);
						changedSlotsOthers.add(firstMetaItemByType);

						int putNextFreeSlot = pullTo.putNextFreeSlot(metaId, 1, meta);

						ownChanged.add(putNextFreeSlot);
					}
				}

			}
		}

	}

	private boolean doInventoryTransferFrom(StashInventory pullTo, Inventory pullFrom, Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> changedMap) {
		IntOpenHashSet ownChanged = changedMap.get(pullTo);

		TypeAmountFastMap pullToFilter = pullTo.getFilter().filter;
		TypeAmountFastMap pullToFillFilter = pullTo.getFilter().fillUpTo;

		if(ownChanged == null) {
			ownChanged = new IntOpenHashSet(pullToFilter.size());
			changedMap.put(pullTo, ownChanged);
		}
		boolean emptyBeforeCon = pullFrom.isEmpty();

		IntOpenHashSet changedSlotsOthers = changedMap.get(pullFrom);
		if(changedSlotsOthers == null) {
			changedSlotsOthers = new IntOpenHashSet(pullToFilter.size());
			changedMap.put(pullFrom, changedSlotsOthers);
		}
		//we now have both inventories and can move over items
		final int size = pullToFilter.getTypes().size();

		long nanoTotalBatch = 0;
		long nanoTotalInc = 0;

		pHandler.set(pullTo, pullFrom, changedMap, changedSlotsOthers, ownChanged, nanoTotalBatch, nanoTotalInc, true);
		pullToFilter.handleLoop(pHandler);

		//		double a = (double)nanoTotalBatch/1000000d;
		//		double b = (double)nanoTotalInc/1000000d;
		//		System.err.println("PULL TIME ::: "+a+"; "+b);

		boolean emptyAfterCon = pullFrom.isEmpty();

		if(!emptyBeforeCon && emptyAfterCon) {
			sendConnected(pullFrom.getParameterIndex(), false, ElementKeyMap.SIGNAL_OR_BLOCK_ID);
		} else if(emptyBeforeCon && !emptyAfterCon) {
			sendConnected(pullFrom.getParameterIndex(), true, ElementKeyMap.SIGNAL_OR_BLOCK_ID);
		}

		return pHandler.transferOk;
	}

	public void sendConnected(long blockIndex, boolean signal, short connectedType) {
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmap = segmentController.getControlElementMap().getControllingMap().get(blockIndex);
		if(mmap != null) {
			FastCopyLongOpenHashSet toSend = mmap.get(connectedType);
			if(toSend != null) {
				for(long s : toSend) {
					long d = 0;
					if(signal) {
						d = ElementCollection.getActivation(s, true, false);
					} else {
						d = ElementCollection.getDeactivation(s, true, false);
					}
					((SendableSegmentController) segmentController).getBlockActivationBuffer().enqueue(d);
				}
			}
		}
	}

	public void updateToFullNetworkObject(NetworkObject o) {
		for(int i = 0; i < senderModules.size(); i++) {
			senderModules.get(i).updateToFullNT(o);
		}
		powerInterface.updateToFullNetworkObject(o);
		if(isOnServer()) {
			segmentController.getNetworkObject().relevantBlockCounts.get().putAll(relevantMap);
			segmentController.getNetworkObject().relevantBlockCounts.setChanged(true);
		}
	}

	public void updateToNetworkObject(NetworkObject o) {
		powerInterface.updateToNetworkObject(o);
	}

	/**
	 * @return the typesThatNeedActivation
	 */
	public ShortOpenHashSet getTypesThatNeedActivation() {
		return typesThatNeedActivation;
	}

	public abstract boolean isTargetLocking(SegmentPiece p);

	@Override
	public int hashCode() {
		return segmentController.getId();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof ManagerContainer<?>) {
			return segmentController.getId() == ((ManagerContainer<?>) obj).segmentController.getId();
		} else {
			return false;
		}
	}

	/**
	 * @return the namedInventoriesClient
	 */
	public Long2ObjectOpenHashMap<StashInventory> getNamedInventoriesClient() {
		return namedInventoriesClient;
	}

	/**
	 * @return the namedInventoriesClientChanged
	 */
	public boolean isNamedInventoriesClientChanged() {
		return namedInventoriesClientChanged;
	}

	/**
	 * @param namedInventoriesClientChanged the namedInventoriesClientChanged to set
	 */
	public void setNamedInventoriesClientChanged(boolean namedInventoriesClientChanged) {
		this.namedInventoriesClientChanged = namedInventoriesClientChanged;
	}

	public void handleBlueprintWireless(String rootUID, List<BBWirelessLogicMarker> wirelessToOwnRail) {

		for(int i = 0; i < wirelessToOwnRail.size(); i++) {
			BBWirelessLogicMarker w = wirelessToOwnRail.get(i);

			BlockMetaDataDummy blockMetaDataDummy = initialBlockMetaData.get(w.fromLocation);
			if(blockMetaDataDummy != null && blockMetaDataDummy instanceof ActivationDestMetaDataDummy) {
				if(((ActivationDestMetaDataDummy) blockMetaDataDummy).dest != null) {

					String uu = new String(rootUID);
					uu = uu.replaceFirst("ENTITY_SPACESTATION", "ENTITY_SHIP");
					uu = uu.replaceFirst("ENTITY_PLANET", "ENTITY_SHIP");
					uu = uu.replaceFirst("ENTITY_FLOATINGROCKMANAGED", "ENTITY_SHIP");
					uu = uu.replaceFirst("ENTITY_FLOATINGROCK", "ENTITY_SHIP");

					((ActivationDestMetaDataDummy) blockMetaDataDummy).dest.marking = uu + w.marking;

					System.err.println("[SERVER][BLUEPRINT][WIRELESS] set blueprint wireless to own: " + segmentController + " -> " + ((ActivationDestMetaDataDummy) blockMetaDataDummy).dest.marking + "; " + rootUID + "; " + w.marking);
				}
			}
		}
	}

	private final SystemTargetContainer targetContainer = new SystemTargetContainer();
	private boolean flagTargetRecalc = true;
	private long lastTargetRecalc;
	private boolean flagElementCollectionChanged;

	private SystemTargetContainer checkTargatableSystems() {
		if(flagTargetRecalc && state.getUpdateTime() - lastTargetRecalc > 5000) {
			targetContainer.initialize(targetableSystems);
			lastTargetRecalc = state.getUpdateTime();
			flagTargetRecalc = false;
		}
		return targetContainer;
	}

	public void transformAimingAt(Vector3f to, Damager from, SimpleGameObject target, Random random, float deviation) {

		//dont apply misshooting when targeting missiles
		to.set((float) ((Math.random() - 0.5f) * deviation), (float) ((Math.random() - 0.5f) * deviation), (float) ((Math.random() - 0.5f) * deviation));
		to.add(target.getClientTransform().origin);

		SystemTargetContainer tSys = checkTargatableSystems();
		if(tSys.isEmpty()) {
			//no valid system target. use center of mass.
			if(factionBlockPos != Long.MIN_VALUE) {
				sPos.set(ElementCollection.getPosX(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosY(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosZ(factionBlockPos) - SegmentData.SEG_HALF);
				target.getClientTransform().basis.transform(sPos);
				to.add(sPos);
				if(ServerConfig.DEBUG_FSM_STATE.isOn()) {
					System.err.println("[FSMDEBUG][TARGET] TARGETING FACTION BLOCK A of " + segmentController);
				}
			} else {
				sPos.set(target.getCenterOfMass(tTmp.origin));
				target.getClientTransform().basis.transform(sPos);
				to.add(sPos);
				if(ServerConfig.DEBUG_FSM_STATE.isOn()) {
					System.err.println("[FSMDEBUG][TARGET] TARGETING COM A of " + segmentController);
				}
			}
		} else {
			//choose a target module at random
			ElementCollection<?, ?, ?> t = tSys.getRandomCollection(random);

			if(t == null) {
				//no valid system target. use center of mass.
				if(factionBlockPos != Long.MIN_VALUE) {
					sPos.set(ElementCollection.getPosX(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosY(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosZ(factionBlockPos) - SegmentData.SEG_HALF);
					target.getClientTransform().basis.transform(sPos);
					to.add(sPos);

					if(ServerConfig.DEBUG_FSM_STATE.isOn()) {
						System.err.println("[FSMDEBUG][TARGET] TARGETING FACTION BLOCK B OF " + segmentController);
					}
				} else {
					sPos.set(target.getCenterOfMass(tTmp.origin));
					target.getClientTransform().basis.transform(sPos);
					to.add(sPos);
					if(ServerConfig.DEBUG_FSM_STATE.isOn()) {
						System.err.println("[FSMDEBUG][TARGET] TARGETING COM B of " + segmentController);
					}
				}
			} else {
				long l = t.getRandomlySelectedFromLastThreadUpdate();
				sPos.set(ElementCollection.getPosX(l) - SegmentData.SEG_HALF, ElementCollection.getPosY(l) - SegmentData.SEG_HALF, ElementCollection.getPosZ(l) - SegmentData.SEG_HALF);
				target.getClientTransform().basis.transform(sPos);
				to.add(sPos);

				if(ServerConfig.DEBUG_FSM_STATE.isOn()) {
					System.err.println("[FSMDEBUG][TARGET] TARGETING MODULE " + t.getName() + " of " + segmentController);
				}
			}
		}

	}

	public void getAimingAtRelative(Vector3f out, Damager from, SimpleGameObject target, Random random, float deviation) {

		//dont apply missshooting when targeting missiles
		out.set((float) ((Math.random() - 0.5f) * deviation), (float) ((Math.random() - 0.5f) * deviation), (float) ((Math.random() - 0.5f) * deviation));

		SystemTargetContainer tSys = checkTargatableSystems();

		if(random.nextInt(10) == 0 || tSys.isEmpty()) {
			//no valid system target. use center of mass.
			if(factionBlockPos != Long.MIN_VALUE) {
				sPos.set(ElementCollection.getPosX(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosY(factionBlockPos) - SegmentData.SEG_HALF, ElementCollection.getPosZ(factionBlockPos) - SegmentData.SEG_HALF);
				out.add(sPos);
			} else {

				out.add(target.getCenterOfMass(tTmp.origin));
			}

		} else {
			//choose a target module at random
			ElementCollection<?, ?, ?> t = tSys.getRandomCollection(random);

			if(t == null) {
				//no valid system target. use center of mass.
				out.add(target.getCenterOfMass(tTmp.origin));
			} else {
				long l = t.getRandomlySelectedFromLastThreadUpdate();

				sPos.set(ElementCollection.getPosX(l) - SegmentData.SEG_HALF, ElementCollection.getPosY(l) - SegmentData.SEG_HALF, ElementCollection.getPosZ(l) - SegmentData.SEG_HALF);

				out.add(sPos);
			}
		}

	}

	public double getMassFromInventories() {
		return segmentController.getConfigManager().apply(StatusEffectType.CARGO_WEIGHT, this.volumeTotal * VoidElementManager.VOLUME_MASS_MULT);
	}

	public abstract ModuleStatistics<E, ? extends ManagerContainer<E>> getStatisticsManager();

	private class SingleControllerStateUnit implements ControllerStateInterface {

		public long block;

		@Override
		public Vector3i getParameter(Vector3i out) {
			return ElementCollection.getPosFromIndex(block, out);
		}

		@Override
		public PlayerState getPlayerState() {
			return null;
		}

		@Override
		public Vector3f getForward(Vector3f out) {
			return GlUtil.getForwardVector(out, getSegmentController().getWorldTransform());
		}

		@Override
		public Vector3f getUp(Vector3f out) {
			return GlUtil.getUpVector(out, getSegmentController().getWorldTransform());
		}

		@Override
		public Vector3f getRight(Vector3f out) {
			return GlUtil.getRightVector(out, getSegmentController().getWorldTransform());
		}

		@Override
		public boolean isUnitInPlayerSector() {
			return true;
		}

		@Override
		public Vector3i getControlledFrom(Vector3i out) {
			getParameter(out);
			return out;
		}

		@Override
		public boolean isFlightControllerActive() {
			return true;
		}

		@Override
		public int getCurrentShipControllerSlot() {
			return 0;
		}

		@Override
		public void handleJoystickDir(Vector3f dir, Vector3f vforwardector3f, Vector3f left, Vector3f up) {
		}

		@Override
		public SimpleTransformableSendableObject getAquiredTarget() {
			return null;
		}

		Vector3f tmpForw = new Vector3f();
		Vector3f tmpUp = new Vector3f();
		Vector3f tmpRight = new Vector3f();
		private SegmentPiece tmp = new SegmentPiece();

		@Override
		public boolean getShootingDir(SegmentController c, ShootContainer shootContainer, float distance, float speed, Vector3i collectionControllerPoint, boolean focused, boolean lead) {
			long curTime = System.currentTimeMillis();

			shootContainer.shootingDirTemp.set(UsableControllableElementManager.getShootingDir(c, getForward(tmpForw), getUp(tmpUp), getRight(tmpRight), shootContainer.shootingForwardTemp, shootContainer.shootingUpTemp, shootContainer.shootingRightTemp, true, collectionControllerPoint, tmp));
			tmp.reset();
			shootContainer.shootingDirStraightTemp.set(UsableControllableElementManager.getShootingDir(c, GlUtil.getForwardVector(tmpForw, c.getWorldTransform()), GlUtil.getUpVector(tmpUp, c.getWorldTransform()), GlUtil.getRightVector(tmpRight, c.getWorldTransform()), shootContainer.shootingStraightForwardTemp, shootContainer.shootingStraightUpTemp, shootContainer.shootingStraightRightTemp, true, collectionControllerPoint, tmp));
			tmp.reset();

			return true;
		}

		@Override
		public boolean isSelected(SegmentPiece controllerElement, Vector3i controlledFrom) {
			return block == controllerElement.getAbsoluteIndex();
		}

		@Override
		public boolean isAISelected(SegmentPiece controllerElement, Vector3i controlledFrom, int controllerIndexForAI, int max, ElementCollectionManager<?, ?, ?> colMan) {
			return true;
		}

		@Override
		public boolean canFlyShip() {
			return true;
		}

		@Override
		public boolean canRotateShip() {
			return true;
		}

		@Override
		public float getBeamTimeout() {
			return 3;//use a 3 second timeout for single activations, so a delay block can keep beams alive
		}

		@Override
		public boolean isSelected(PlayerUsableInterface usable, ManagerContainer<?> con) {
			return false;
		}

		@Override
		public boolean canFocusWeapon() {
			return false;
		}

		@Override
		public void addCockpitOffset(Vector3f camPos) {

		}

		@Override
		public boolean isDown(KeyboardMappings m) {
			return true;
		}

		@Override
		public boolean isTriggered(KeyboardMappings m) {
			return true;
		}
	}

	public abstract ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> getCargo();

	public abstract ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> getLockBox();

	private class DelayedInvProdSet {
		Short2IntOpenHashMap inventoryFilter;
		short inventoryProduction;
		int inventoryProductionLimit;
		Short2IntOpenHashMap inventoryFillUpFilters;

		public DelayedInvProdSet(Vector3i posBuilt, Short2IntOpenHashMap inventoryFilter, Short2IntOpenHashMap inventoryFillUpFilters, short inventoryProduction, int inventoryProductionLimit) {
			super();
			this.inventoryFilter = inventoryFilter;
			this.inventoryProduction = inventoryProduction;
			this.inventoryFillUpFilters = inventoryFillUpFilters;
			this.inventoryProductionLimit = inventoryProductionLimit;
		}

	}

	public void addDelayedProductionAndFilterClientSet(Vector3i posBuilt, Short2IntOpenHashMap inventoryFilter, Short2IntOpenHashMap inventoryFillUpFilters, short inventoryProduction, int inventoryProductionLimit) {
		DelayedInvProdSet d = new DelayedInvProdSet(posBuilt, inventoryFilter, inventoryFillUpFilters, inventoryProduction, inventoryProductionLimit);
		invProdMapDelayed.put(ElementCollection.getIndex(posBuilt), d);
	}

	public abstract ManagerModuleSingle<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> getRailPickup();

	public abstract ManagerModuleCollection<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> getRailSys();

	public abstract ManagerModuleSingle<ArmorHPUnit, ArmorHPCollection, VoidElementManager<ArmorHPUnit, ArmorHPCollection>> getArmorHP();

	public abstract ManagerModuleSingle<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> getFactoryManager();

	public abstract ManagerModuleSingle<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> getFleetManager();

	@Override
	public String toString() {
		return "MANAGER[" + segmentController + "]";
	}

	//	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
	//		as
	//		if(this instanceof PowerManagerInterface){
	//			((PowerManagerInterface)this).getPowerAddOn().handleMouseEvent(this, unit, e);
	//		}
	//	}
	//	public void handleMouseEvent(ControllerStateUnit unit, MouseEvent e) {
	//		if(unit.parameter instanceof Vector3i){
	//			long enterIndex = ElementCollection.getIndex((Vector3i)unit.parameter);
	//			long selectedSlotIndex = getSelectedSlot(unit, enterIndex);
	//
	//
	//			PlayerUsableInterface p = playerUsable.get(selectedSlotIndex);
	//			if(p != null){
	//				p.handleMouseEvent(unit, e);
	//			}
	//
	//			for(long a : PlayerUsableInterface.ALWAYS_SELECTED){
	//				PlayerUsableInterface always = playerUsable.get(a);
	//				if(always != null){
	//					always.handleMouseEvent(unit, e);
	//				}
	//			}
	//		}
	//	}
	public boolean getSelectedSlot(ControllerStateUnit unit, Vector3i output) {
		unit.getParameter(output);

		SlotAssignment shipConfiguration = null;
		SegmentPiece fromPiece = segmentController.getSegmentBuffer().getPointUnsave(output, tmp);

		if(fromPiece != null && fromPiece.getType() == ElementKeyMap.CORE_ID) {
			shipConfiguration = checkShipConfig(unit);
			int currentlySelectedSlot = unit.getCurrentShipControllerSlot();
			if(!shipConfiguration.hasConfigForSlot(currentlySelectedSlot)) {
				return false;
			} else {
				output.set(shipConfiguration.get(currentlySelectedSlot));
			}
		} else {
			return false;
		}
		return true;
	}

	public long getSelectedSlot(ControllerStateUnit unit, long entered) {

		SlotAssignment shipConfiguration = null;
		SegmentPiece fromPiece = segmentController.getSegmentBuffer().getPointUnsave(entered, tmp);

		if(fromPiece != null && fromPiece.getType() == ElementKeyMap.CORE_ID) {
			shipConfiguration = checkShipConfig(unit);
			int currentlySelectedSlot = unit.getCurrentShipControllerSlot();
			if(shipConfiguration == null || !shipConfiguration.hasConfigForSlot(currentlySelectedSlot)) {
				return Long.MIN_VALUE;
			} else {
				return shipConfiguration.getAsIndex(currentlySelectedSlot);
			}
		} else {
			return Long.MIN_VALUE;
		}
	}

	public SlotAssignment checkShipConfig(ControllerStateInterface unit) {
		if(unit.getPlayerState() == null) {
			return null;
		}
		//		getSegmentController().getSlotAssignment().reassignIfNotExists(unit.getParameter());
		return segmentController.getSlotAssignment();
	}

	public void addModuleExplosions(ModuleExplosion m) {
		assert (isOnServer());
		if(!isOnServer()) {
			return;
		}
		moduleExplosions.add(m);
	}

	public boolean isPowerBatteryAlwaysOn() {
		return false;
	}

	public abstract ManagerModuleCollection<SensorUnit, SensorCollectionManager, SensorElementManager> getSensor();

	public Vector4f getColorCore() {
		return colorCore;
	}

	public long getFactionBlockPos() {
		return factionBlockPos;
	}

	public PowerInterface getPowerInterface() {
		return powerInterface;
	}

	public abstract MainReactorCollectionManager getMainReactor();

	public abstract StabilizerCollectionManager getStabilizer();

	public abstract List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> getChambers();

	public abstract ConduitCollectionManager getConduit();

	public LongOpenHashSet getBuildBlocks() {
		return buildBlocks;
	}

	public void flagAllCollectionsDirty() {
		for(ManagerModule<?, ?, ?> mod : modules) {
			if(mod.getElementManager() instanceof UsableControllableSingleElementManager<?, ?, ?>) {
				UsableControllableSingleElementManager<?, ?, ?> voidMan = (UsableControllableSingleElementManager<?, ?, ?>) mod.getElementManager();
				ElementCollectionManager<?, ?, ?> ec = voidMan.getCollection();
				ec.flagDirty();

			} else if(mod.getElementManager() instanceof UsableControllableElementManager<?, ?, ?>) {
				UsableControllableElementManager<?, ?, ?> man = (UsableControllableElementManager<?, ?, ?>) mod.getElementManager();
				for(ControlBlockElementCollectionManager<?, ?, ?> c : man.getCollectionManagers()) {
					c.flagDirty();
				}
			}
		}
		System.err.println("[MANAGER CONTAINER] " + segmentController + " FLAGGED ALL MANAGERS DIRTY");
	}

	public void onFullyLoaded() {
		for(ManagerModule<?, ?, ?> m : modules) {
			m.onFullyLoaded();
		}
	}

	public boolean isUsingPowerReactors() {
		return powerInterface.isUsingPowerReactors();
	}

	public void removeConsumer(PowerConsumer e) {
		powerInterface.removeConsumer(e);
	}

	public void addConsumer(PowerConsumer e) {
		powerInterface.addConsumer(e);
	}

	public void addPlayerUsable(PlayerUsableInterface w) {
		if(w.isAddToPlayerUsable()) {
			if(w.getUsableId() > Long.MIN_VALUE) {
				playerUsable.put(w.getUsableId(), w);

				if(w instanceof RevealingActionListener) {
					revealingActionListeners.put(w.getUsableId(), (RevealingActionListener) w);
				}
			}
		}
	}

	public void removePlayerUsable(PlayerUsableInterface w) {
		playerUsable.remove(w.getUsableId());
		revealingActionListeners.remove(w.getUsableId());

		if(isOnServer()) {
			SlotAssignment shipConfiguration = segmentController.getSlotAssignment();
			shipConfiguration.removeByPosAndSend(w.getUsableId());
		}
	}

	public ObjectCollection<PlayerUsableInterface> getPlayerUsable() {
		return playerUsable.values();
	}

	public PlayerUsableInterface getPlayerUsable(long index) {
		return playerUsable.get(index);
	}

	public void addUpdatable(ManagerUpdatableInterface m) {
		assert (!updatable.contains(m)) : m + "; " + updatable;
		updatable.add(m);
		Collections.sort(updatable, updatableComp);
	}

	public void onRevealingAction() {
		for(RevealingActionListener u : revealingActionListeners.values()) {
			u.onRevealingAction();
		}
	}

	public void addTagUsable(TagModuleUsableInterface m) {
		//check for duplicates
		for(TagModuleUsableInterface tm : tagModules) {

			if(m == tm || (tm.getTagId().equals(m.getTagId()) && tm.getDummyInstance().getClass() != m.getDummyInstance().getClass())) {
				throw new RuntimeException("Duplicate TAG ID: " + tm.getTagId() + ": " + tm.getClass().getSimpleName() + " and " + m.getClass().getSimpleName());
			}
		}
		tagModules.add(m);
	}

	public void addEffectSource(ConfigProviderSource configSource) {
		effectConfigSources.add(configSource);
	}

	public void removeEffectSource(ConfigProviderSource configSource) {
		effectConfigSources.remove(configSource);
	}

	public void registerTransientEffetcs(List<ConfigProviderSource> transientEffectSources) {
		transientEffectSources.addAll(effectConfigSources);
	}

	public EffectAddOnManager getEffectAddOnManager() {
		return effectAddOnManager;
	}

	public void addRechargeSingleModule(RecharchableSingleModule r) {
		rechargableSingleModulesToAdd.enqueue(r);
	}

	public static void onClientStartStatic() {
		if(!startedClientStatic) {
			StabilizerCollectionManager.startStaticThread();
			startedClientStatic = true;
		}
	}

	public boolean isRequestedInitalValuesIfNeeded() {
		if(isOnServer()) {
			return true;
		}
		if(!segmentController.isInClientRange()) {
			notLoadedOnClient = true;
			//dont update (use up initial etc) while object can't be loaded
			return false;
		}
		if(notLoadedOnClient) {
			ServerValueRequestUpdate v = new ServerValueRequestUpdate(ServerValueRequestUpdate.Type.ALL);
			assert (v.getType() == ValTypes.SERVER_UPDATE_REQUEST);
			v.setServer(this);
			((NTValueUpdateInterface) segmentController.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(v, segmentController.isOnServer()));

			assert (!segmentController.isOnServer());
			notLoadedOnClient = false;
		}
		return true;
	}

	public boolean hasActiveReactors() {
		return powerInterface.hasActiveReactors();
	}

	public boolean hasAnyReactors() {
		return powerInterface.hasAnyReactors();
	}

	public ReactorBoostAddOn getReactorBoostAddOn() {
		return reactorBoostAddOn;
	}

	public InterdictionAddOn getInterdictionAddOn() {
		return interdictionBoostAddOn;
	}

	public void modifySlavesAndEffects(long oldElem, long newElem) {
		slavesAndEffects.remove(oldElem);
		if(newElem != Long.MIN_VALUE) {
			slavesAndEffects.add(newElem);
		}
	}

	public LongSet getSlavesAndEffects() {
		return slavesAndEffects;
	}

	private class InventoryDetailRequest {
		NetworkSegmentProvider prov;
		private final LongArrayList requested = new LongArrayList();
	}

	public void handleInventoryDetailRequests(NetworkSegmentProvider prov, RemoteLongBuffer detailrequest) {
		InventoryDetailRequest r = new InventoryDetailRequest();
		r.prov = prov;
		for(int i = 0; i < detailrequest.getReceiveBuffer().size(); i++) {
			r.requested.add(detailrequest.getReceiveBuffer().getLong(i));
		}
		inventoryDetailRequests.enqueue(r);
	}

	public void handleInventoryDetailAnswer(NetworkSegmentProvider networkObject, RemoteInventoryFilterBuffer inventoryDetailAnswers) {
		for(int i = 0; i < inventoryDetailAnswers.getReceiveBuffer().size(); i++) {
			InventoryFilter inventoryFilter = inventoryDetailAnswers.getReceiveBuffer().get(i).get();
			Inventory inventory = getInventory(inventoryFilter.inventoryId);
			assert (inventory != null) : inventoryFilter.inventoryId;
			if(inventory != null) {
				inventory.receivedFilterAnswer(inventoryFilter);
			}

		}
	}

	public void unregisterGraphicsListener() {
		this.graphicsListener = null;
	}

	public void registerGraphicsListener(MContainerDrawJob graphicsListener) {
		this.graphicsListener = graphicsListener;
	}

	public void flagElementChanged() {
		if(this.graphicsListener != null) {
			this.graphicsListener.flagChanged();
		}
		lastChangedElement = state.getUpdateTime();
		flagElementCollectionChanged = true;
	}

	public void onPlayerDetachedFromThisOrADock(ManagedUsableSegmentController<?> originalCaller, PlayerState pState, PlayerControllable newAttached) {
		for(PlayerUsableInterface p : playerUsable.values()) {
			p.onPlayerDetachedFromThisOrADock(originalCaller, pState, newAttached);
		}
	}

	public static class ManualMouseEvent implements SerializationInterface {
		public boolean sent;

		public class ControllerStateManualUnit extends ControllerStateUnit {

			@Override
			public boolean isFlightControllerActive() {
				return true;
			}
		}

		long usableId;
		KeyboardMappings eventButton;
		int playerId;
		private long firstExecution;

		public void send(NetworkSegmentProvider s) {
			s.manualMouseEventBuffer.add(new RemoteManualMouseEvent(this, s.isOnServer()));
		}

		public boolean execute(ManagerContainer<?> c, Timer timer) {

			Sendable sendable = c.getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(playerId);
			if(sendable instanceof PlayerState) {
				PlayerState p = (PlayerState) sendable;
				ControllerStateManualUnit cc = new ControllerStateManualUnit();
				cc.playerState = p;
				cc.playerControllable = (PlayerControllable) c.getSegmentController();
				cc.parameter = new Vector3i(Ship.core);

				PlayerUsableInterface pl = c.playerUsable.get(usableId);
				if(pl != null) {
					if(timer.currentTime > firstExecution + 500) {
						pl.handleKeyEvent(cc, eventButton, timer);
					}
					if(firstExecution == 0) {
						firstExecution = timer.currentTime;
					}
				} else {
					System.err.println("[MANUAL EVENT] " + this + " " + c + " USABLE NULL: " + usableId);
				}

				return false;
			} else {
				System.err.println("[MANUAL EVENT] " + this + " " + c + " PLAYER STATE NULL");
			}

			return true;
		}

		@Override
		public void serialize(DataOutput b, boolean isOnServer) throws IOException {
			b.writeLong(usableId);
			b.writeShort(eventButton.ordinal());
			b.writeInt(playerId);
		}

		@Override
		public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
			usableId = b.readLong();
			eventButton = KeyboardMappings.values()[b.readShort()];
			playerId = b.readInt();
		}

		@Override
		public String toString() {
			return "ManualMouseEvent [usableId=" + usableId + ", eventButton=" + eventButton + ", playerId=" + playerId + "]";
		}

	}

	public void triggerEventFromMouseCallback(int playerId, PlayerUsableInterface f, KeyboardMappings event) {
		ManualMouseEvent m = new ManualMouseEvent();
		m.playerId = playerId;
		m.usableId = f.getUsableId();
		m.eventButton = event;
		triggeredManualMouseEvent(m);

	}

	public void triggeredManualMouseEvent(ManualMouseEvent mme) {
		manualEvents.add(mme);

	}

	public boolean existsExplosion(Damager from, short type, long pos) {
		List<ModuleExplosion> l = moduleExplosions;
		final int size = l.size();
		for(int i = 0; i < size; i++) {
			ModuleExplosion moduleExplosion = l.get(i);
			if(moduleExplosion.getModuleBB().isInside(ElementCollection.getPosX(pos), ElementCollection.getPosY(pos), ElementCollection.getPosZ(pos))) {
				//don't add explosion, since there is one ongoing in the area
				return true;
			}
		}
		return false;
	}

	public List<ModuleExplosion> getModuleExplosions() {
		return moduleExplosions;
	}

	public InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType) {
		PlayerUsableInterface usable = getPlayerUsable(weaponId);
		if(usable != null && usable instanceof DamageDealer) {
			DamageDealer d = ((DamageDealer) usable);
			assert (d.getDamageDealerType() == damageDealerType);
			return d.getAttackEffectSet();
		}
		return null;
	}

	public MetaWeaponEffectInterface getMetaWeaponEffect(long weaponId, DamageDealerType damageDealerType) {
		PlayerUsableInterface usable = getPlayerUsable(weaponId);
		if(usable != null && usable instanceof DamageDealer) {
			DamageDealer d = ((DamageDealer) usable);
			assert (d.getDamageDealerType() == damageDealerType);
			return d.getMetaWeaponEffect();
		}
		return null;
	}

	public static class ReceivedBeamLatch {
		public long beamId;
		public int objId;
		public long blockPos;
	}

	public void addReceivedBeamLatch(long beamId, int objId, long blockPos) {
		ReceivedBeamLatch b = new ReceivedBeamLatch();
		b.beamId = beamId;
		b.objId = objId;
		b.blockPos = blockPos;

		this.receivedBeamLatches.enqueue(b);
	}

	public List<BeamHandlerContainer<?>> getBeamInterfacesSingle() {
		return beamInteracesSingle;
	}

	public List<BeamElementManager<?, ?, ?>> getBeamManagers() {
		return beamModules;
	}

	public abstract float getAmmoCapacity(WeaponType type);

	public abstract float getAmmoCapacityMax(WeaponType type);

	public abstract float getAmmoCapacityTimer(WeaponType type);

	public abstract float getAmmoCapacityReloadTime(WeaponType type);

	public abstract void setAmmoCapacity(WeaponType to, float val, float timer, boolean send);

	public abstract AcidFormulaType getAcidType(long weaponId);

	public void queueOnEffectChange(EffectChangeHanlder mm) {
		effectChangeQueue.enqueue(mm);
	}

	public void flagUpdatableCheckFor(UsableElementManager<?, ?, ?> man) {
		this.checkUpdatableQueue.enqueue(man);
	}

	public float getIntegrityUpdateDelay() {
		return integrityUpdateDelay;
	}

	public void setIntegrityUpdateDelay(float integrityUpdateDelay) {
		this.integrityUpdateDelay = integrityUpdateDelay;
	}

	public float getRepairDelay() {
		return repairDelay;
	}

	public void onDockingChanged(boolean docked) {
		for(RailDockingListener r : railDockingListeners) {
			r.dockingChanged(segmentController, docked);
		}
	}

	public float getSelectedWeaponZoom(PlayerState player) {
		for(ControllerStateUnit u : player.getControllerState().getUnits()) {
			if(u.playerControllable == segmentController && u.parameter instanceof Vector3i) {

				long wepId = getSelectedSlot(u, ElementCollection.getIndex((Vector3i) u.parameter));
				PlayerUsableInterface playerUsable = getPlayerUsable(wepId);

				if(playerUsable instanceof ZoomableUsableModule) {
					return ((ZoomableUsableModule) playerUsable).getPossibleZoom();
				}
			}
		}
		return -1f;
	}

	public void sendAllFireModes() {
		for(WeaponElementManagerInterface e : weapons) {
			if(e instanceof UsableControllableFiringElementManager<?, ?, ?>) {
				for(ControlBlockElementCollectionManager<?, ?, ?> c : ((UsableControllableFiringElementManager<?, ?, ?>) e).getCollectionManagers()) {
					if(c instanceof FocusableUsableModule && ((FocusableUsableModule) c).getFireMode() != FireMode.getDefault(c.getClass())) {
						((FocusableUsableModule) c).sendFireMode();
					}
				}
			}
		}
	}

	public ObjectArrayList<WeaponElementManagerInterface> getWeapons() {
		return weapons;
	}

	public void resetIntegrityDelay() {
		integrityUpdateDelay = 0;
		sendDelayUpdate();
	}

	private void sendDelayUpdate() {
		segmentController.getNetworkObject().blockDelayTimers.add(integrityUpdateDelay);
		segmentController.getNetworkObject().blockDelayTimers.add(repairDelay);
	}

	public void resetRepairDelay() {
		repairDelay = 0;
		sendDelayUpdate();
	}

}
