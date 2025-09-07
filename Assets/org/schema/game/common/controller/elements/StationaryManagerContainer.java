package org.schema.game.common.controller.elements;

import api.element.block.Blocks;
import api.listener.events.register.ManagerContainerRegisterEvent;
import api.mod.StarLoader;
import api.utils.game.module.ModManagerContainerModule;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.elements.activation.AbstractUnit;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.activation.ActivationElementManager;
import org.schema.game.common.controller.elements.activationgate.ActivationGateCollectionManager;
import org.schema.game.common.controller.elements.activationgate.ActivationGateElementManager;
import org.schema.game.common.controller.elements.activationgate.ActivationGateUnit;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.AmmoCapacityElementManager;
import org.schema.game.common.controller.elements.ammo.cannon.CannonCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.cannon.CannonCapacityElementManager;
import org.schema.game.common.controller.elements.ammo.cannon.CannonCapacityUnit;
import org.schema.game.common.controller.elements.ammo.damagebeam.DamageBeamCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.damagebeam.DamageBeamCapacityElementManager;
import org.schema.game.common.controller.elements.ammo.damagebeam.DamageBeamCapacityUnit;
import org.schema.game.common.controller.elements.ammo.missile.MissileCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.missile.MissileCapacityElementManager;
import org.schema.game.common.controller.elements.ammo.missile.MissileCapacityUnit;
import org.schema.game.common.controller.elements.ammo.repair.RepairCapacityCollectionManager;
import org.schema.game.common.controller.elements.ammo.repair.RepairCapacityElementManager;
import org.schema.game.common.controller.elements.ammo.repair.RepairCapacityUnit;
import org.schema.game.common.controller.elements.armorhp.ArmorHPCollection;
import org.schema.game.common.controller.elements.armorhp.ArmorHPUnit;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamElementManager;
import org.schema.game.common.controller.elements.beam.damageBeam.DamageBeamUnit;
import org.schema.game.common.controller.elements.beam.harvest.SalvageBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.harvest.SalvageElementManager;
import org.schema.game.common.controller.elements.beam.harvest.SalvageUnit;
import org.schema.game.common.controller.elements.beam.repair.RepairBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.repair.RepairElementManager;
import org.schema.game.common.controller.elements.beam.repair.RepairUnit;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.controller.elements.cargo.CargoElementManager;
import org.schema.game.common.controller.elements.cargo.CargoUnit;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockElementManager;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockManagerInterface;
import org.schema.game.common.controller.elements.dockingBlock.DockingBlockUnit;
import org.schema.game.common.controller.elements.dockingBlock.fixed.FixedDockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.fixed.FixedDockingBlockElementManager;
import org.schema.game.common.controller.elements.dockingBlock.fixed.FixedDockingBlockUnit;
import org.schema.game.common.controller.elements.dockingBlock.turret.TurretDockingBlockCollectionManager;
import org.schema.game.common.controller.elements.dockingBlock.turret.TurretDockingBlockElementManager;
import org.schema.game.common.controller.elements.dockingBlock.turret.TurretDockingBlockUnit;
import org.schema.game.common.controller.elements.door.DoorCollectionManager;
import org.schema.game.common.controller.elements.door.DoorUnit;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.controller.elements.effectblock.em.EmEffectCollectionManager;
import org.schema.game.common.controller.elements.effectblock.em.EmEffectElementManager;
import org.schema.game.common.controller.elements.effectblock.em.EmEffectUnit;
import org.schema.game.common.controller.elements.effectblock.heat.HeatEffectCollectionManager;
import org.schema.game.common.controller.elements.effectblock.heat.HeatEffectElementManager;
import org.schema.game.common.controller.elements.effectblock.heat.HeatEffectUnit;
import org.schema.game.common.controller.elements.effectblock.kinetic.KineticEffectCollectionManager;
import org.schema.game.common.controller.elements.effectblock.kinetic.KineticEffectElementManager;
import org.schema.game.common.controller.elements.effectblock.kinetic.KineticEffectUnit;
import org.schema.game.common.controller.elements.explosive.ExplosiveCollectionManager;
import org.schema.game.common.controller.elements.explosive.ExplosiveElementManager;
import org.schema.game.common.controller.elements.explosive.ExplosiveUnit;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerCollection;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerUnit;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerCollectionManager;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerUnit;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorCollectionManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorElementManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorUnit;
import org.schema.game.common.controller.elements.lift.LiftCollectionManager;
import org.schema.game.common.controller.elements.lift.LiftUnit;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileCollectionManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileElementManager;
import org.schema.game.common.controller.elements.missile.dumb.DumbMissileUnit;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerCollectionManager;
import org.schema.game.common.controller.elements.power.PowerUnit;
import org.schema.game.common.controller.elements.power.reactor.*;
import org.schema.game.common.controller.elements.power.reactor.chamber.*;
import org.schema.game.common.controller.elements.powerbattery.PowerBatteryCollectionManager;
import org.schema.game.common.controller.elements.powerbattery.PowerBatteryUnit;
import org.schema.game.common.controller.elements.powercap.PowerCapacityCollectionManager;
import org.schema.game.common.controller.elements.powercap.PowerCapacityUnit;
import org.schema.game.common.controller.elements.pulse.push.PushPulseCollectionManager;
import org.schema.game.common.controller.elements.pulse.push.PushPulseElementManager;
import org.schema.game.common.controller.elements.pulse.push.PushPulseUnit;
import org.schema.game.common.controller.elements.racegate.RacegateCollectionManager;
import org.schema.game.common.controller.elements.racegate.RacegateElementManager;
import org.schema.game.common.controller.elements.racegate.RacegateUnit;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionCollectionManager;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionElementManager;
import org.schema.game.common.controller.elements.rail.inv.RailConnectionUnit;
import org.schema.game.common.controller.elements.rail.massenhancer.RailMassEnhancerCollectionManager;
import org.schema.game.common.controller.elements.rail.massenhancer.RailMassEnhancerUnit;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupCollectionManager;
import org.schema.game.common.controller.elements.rail.pickup.RailPickupUnit;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedCollectionManager;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedElementManager;
import org.schema.game.common.controller.elements.rail.speed.RailSpeedUnit;
import org.schema.game.common.controller.elements.sensor.SensorCollectionManager;
import org.schema.game.common.controller.elements.sensor.SensorElementManager;
import org.schema.game.common.controller.elements.sensor.SensorUnit;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityCollectionManager;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityUnit;
import org.schema.game.common.controller.elements.shield.regen.ShieldRegenCollectionManager;
import org.schema.game.common.controller.elements.shield.regen.ShieldRegenUnit;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardUnit;
import org.schema.game.common.controller.elements.shop.ShopCollectionManager;
import org.schema.game.common.controller.elements.shop.ShopElementManager;
import org.schema.game.common.controller.elements.shop.ShopUnit;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerCollectionManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerUnit;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterElementManager;
import org.schema.game.common.controller.elements.transporter.TransporterUnit;
import org.schema.game.common.controller.elements.trigger.TriggerCollectionManager;
import org.schema.game.common.controller.elements.trigger.TriggerElementManager;
import org.schema.game.common.controller.elements.trigger.TriggerUnit;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateElementManager;
import org.schema.game.common.controller.elements.warpgate.WarpgateUnit;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.inventory.*;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.objects.NetworkDoorInterface;
import org.schema.game.network.objects.NetworkLiftInterface;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.remote.RemoteInventoryMultMod;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteVector4i;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class StationaryManagerContainer<E extends SegmentController> extends ManagerContainer<E> implements FactoryAddOnInterface, ShieldContainerInterface, TriggerManagerInterface, LiftContainerInterface, InventoryHolder, DoorContainerInterface, ActivationManagerInterface, SalvageManagerContainer, ScannerManagerInterface, ExplosiveManagerContainerInterface, JumpProhibiterModuleInterface, DockingBlockManagerInterface, RailManagerInterface, WeaponManagerInterface, ShopInterface, EffectManagerContainer, ShipyardManagerContainerInterface, TransporterModuleInterface, MissileModuleInterface {

	private PowerAddOn powerAddOn;
	private ShoppingAddOn shoppingAddOn;
	private ShieldAddOn shieldAddOn;
	private ManagerModuleSingle<ArmorHPUnit, ArmorHPCollection, VoidElementManager<ArmorHPUnit, ArmorHPCollection>> armorHp;
	private ManagerModuleSingle<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> factoryManager;
	private ManagerModuleSingle<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> fleetManager;
	private ManagerModuleSingle<ShieldRegenUnit, ShieldRegenCollectionManager, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager>> shields;
	private ManagerModuleSingle<ShieldCapacityUnit, ShieldCapacityCollectionManager, VoidElementManager<ShieldCapacityUnit, ShieldCapacityCollectionManager>> shieldCapacity;
	private ManagerModuleSingle<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> power;
	private ManagerModuleSingle<LiftUnit, LiftCollectionManager, VoidElementManager<LiftUnit, LiftCollectionManager>> lift;
	private ManagerModuleSingle<DoorUnit, DoorCollectionManager, VoidElementManager<DoorUnit, DoorCollectionManager>> door;
	private ManagerModuleSingle<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> railPickup;
	private ManagerModuleCollection<RepairUnit, RepairBeamCollectionManager, RepairElementManager> repair;
	private ManagerModuleCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> warpgate;
	private ManagerModuleCollection<RacegateUnit, RacegateCollectionManager, RacegateElementManager> racegate;
	private ManagerModuleCollection<ActivationGateUnit, ActivationGateCollectionManager, ActivationGateElementManager> activationgate;
	private ManagerModuleCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> transporter;
	private ManagerModuleSingle<ConduitUnit, ConduitCollectionManager, ConduitElementManager> conduit;
	private ManagerModuleSingle<PowerCapacityUnit, PowerCapacityCollectionManager, VoidElementManager<PowerCapacityUnit, PowerCapacityCollectionManager>> powerCapacity;
	private ManagerModuleSingle<PowerBatteryUnit, PowerBatteryCollectionManager, VoidElementManager<PowerBatteryUnit, PowerBatteryCollectionManager>> powerBattery;
	private ManagerModuleSingle<RailMassEnhancerUnit, RailMassEnhancerCollectionManager, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager>> railMassEnhancer;
	private ManagerModuleCollection<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> jumpProhibiter;
	private ManagerModuleCollection<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> railSpeed;
	private ManagerModuleCollection<CannonUnit, CannonCollectionManager, CannonElementManager> weapon;
	private ManagerModuleSingle<MainReactorUnit, MainReactorCollectionManager, MainReactorElementManager> reactor;
	private ManagerModuleSingle<StabilizerUnit, StabilizerCollectionManager, StabilizerElementManager> stabilizer;
	private ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> dumbMissile;
	private ManagerModuleCollection<TurretDockingBlockUnit, TurretDockingBlockCollectionManager, TurretDockingBlockElementManager> turretDockingBlock;
	private ManagerModuleCollection<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> fixedDockingBlock;
	private ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> trigger;
	private ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> activation;
	private ManagerModuleCollection<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> empEffect;
	private ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> cargo;
	private ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> lockBox;
	private ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> salvage;
	private ManagerModuleCollection<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> pushPulse;
	private ManagerModuleCollection<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> damageBeam;
	private List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers;
	private ManagerModuleSingle<CannonCapacityUnit, CannonCapacityCollectionManager, CannonCapacityElementManager> cannonCapacity;
	private ManagerModuleSingle<DamageBeamCapacityUnit, DamageBeamCapacityCollectionManager, DamageBeamCapacityElementManager> beamCapacity;
	private ManagerModuleSingle<MissileCapacityUnit, MissileCapacityCollectionManager, MissileCapacityElementManager> missileCapacity;
	private ManagerModuleSingle<RepairCapacityUnit, RepairCapacityCollectionManager, RepairCapacityElementManager> repairCapacity;
	private FactoryAddOn factory;
	private ShopInventory shopInventory;
	private ManagerModuleSingle<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> explosive;
	private Long2ObjectOpenHashMap<String> warpDestinationMap;
	private Long2ObjectOpenHashMap<Vector3i> warpDestinationLocalMap;
	private Long2BooleanOpenHashMap warpValidInitialMap = new Long2BooleanOpenHashMap();
	private Long2BooleanOpenHashMap warpActiveInitialMap = new Long2BooleanOpenHashMap(){
		@Override
		public boolean defaultReturnValue() {
			return true; //if it's not there just return true
		}
	};
	private Long2ObjectOpenHashMap<String> raceDestinationMap;
	private Long2ObjectOpenHashMap<Vector3i> raceDestinationLocalMap;
	private Long2BooleanOpenHashMap raceValidInitialMap = new Long2BooleanOpenHashMap();
	private ManagerModuleCollection<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> spaceScanner;
	private ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> shipyard;
	private ManagerModuleCollection<ShopUnit, ShopCollectionManager, ShopElementManager> shopManager;
	private TradeNode tradeNode;
	private ManagerModuleCollection<SensorUnit, SensorCollectionManager, SensorElementManager> sensor;
	private boolean wasValidTradeNode;
	private ManagerModuleCollection<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> railSys;
	private boolean flagSendMissileCapacity;
	private boolean flagSendCannonCapacity;
	private boolean flagSendBeamCapacity;
	private boolean flagSendRepairCapacity;
	private static boolean[] specialBlocksStatic;

	private ManagerModuleCollection<HeatEffectUnit, HeatEffectCollectionManager, HeatEffectElementManager> heatEffect;
	private ManagerModuleCollection<KineticEffectUnit, KineticEffectCollectionManager, KineticEffectElementManager> kineticEffect;
	private ManagerModuleCollection<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> emEffect;

	public StationaryManagerContainer(StateInterface state, E station) {
		super(state, station);
	}

	@Override
	public boolean isPowerBatteryAlwaysOn() {
		return true;
	}

	public static final Vector3i getActiveWarpGate(String completeUid) {

		File entity = new FileExt(GameServerState.ENTITY_DATABASE_PATH + completeUid + ".ent");
		try {
			if(entity.exists() && (entity.getName().startsWith(EntityType.SPACE_STATION.dbPrefix) || entity.getName().startsWith(EntityType.PLANET_SEGMENT.dbPrefix))) {

				System.err.println("[WARPGATE] CHECKING Destination Gate: " + entity.getName());
				Tag tagGlob;

				tagGlob = Tag.readFrom(new BufferedInputStream(new FileInputStream(entity)), true, false);

				Tag tagTop = null;
				if(entity.getName().startsWith(EntityType.SPACE_STATION.dbPrefix)) {
					tagTop = ((Tag[]) tagGlob.getValue())[1];
				} else if(entity.getName().startsWith(EntityType.PLANET_SEGMENT.dbPrefix)) {
					tagTop = ((Tag[]) tagGlob.getValue())[2];
				}
				assert (tagTop != null);
				int shift = 0;
				if(tagTop.getName().equals("sc")) {
					//chunk16 system, we have to shift
					shift = 8;
				}
				Tag[] segControllerValues = ((Tag[]) tagTop.getValue());

				Tag[] simpleTransformableValues = (Tag[]) segControllerValues[6].getValue();
				Tag[] managerController = (Tag[]) segControllerValues[7].getValue();

				Tag tag = null;
				if(managerController.length > 8 && managerController[8].getType() != Type.FINISH) {
					tag = managerController[8];
				} else {
					System.err.println("[WARPGATE] Destination Gate: " + entity.getName() + " has no warpgateInfo");
				}

				if(tag != null && tag.getType() != Type.BYTE) {

					Tag[] t = (Tag[]) tag.getValue();
					for(int i = 0; i < t.length - 1; i++) {
						Tag[] inner = (Tag[]) t[i].getValue();
						Vector3i cPos = new Vector3i();
						if(inner[0].getType() == Type.VECTOR3i) {
							cPos.set(inner[0].getVector3i());
						} else {
							ElementCollection.getPosFromIndex(inner[0].getLong(), cPos);
						}
						cPos.add(shift, shift, shift);
						Tag[] detail = (Tag[]) inner[1].getValue();
						boolean valid = ((Byte) detail[0].getValue()).byteValue() != (byte) 0;
						boolean active = true;
						if(detail.length > 3 && detail[3].getType() == Type.BYTE){
							active = detail[3].getByte() > 0;
						}
						String destination = ((String) detail[1].getValue());

						if (valid) {
							if (active){
								System.err.println("[WARPGATE] Destination Gate: " + i + " is valid");
								return cPos;
							} else {
								System.err.println("[WARPGATE] Destination Gate: " + i + " is inactive.");
							}
						} else {
							System.err.println("[WARPGATE] Destination Gate: " + i + " is not valid");
						}
					}
				} else {
					System.err.println("[WARPGATE] Destination Gate: no data");
				}
			} else {
				System.err.println("[WARPGATE] DESTINATION NOT FOUND: " + completeUid);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void fillInventory(boolean send, boolean full) throws NoSlotFreeException {
		shoppingAddOn.fillInventory(send, full);
	}

	@Override
	public long getCredits() {
		return shoppingAddOn.getCredits();
	}

	@Override
	public ShopNetworkInterface getNetworkObject() {
		return (ShopNetworkInterface) getSegmentController().getNetworkObject();
	}

	/**
	 * @return the permissionToPurchase
	 */
	@Override
	public long getPermissionToPurchase() {
		return shoppingAddOn.getPermissionToPurchase();
	}

	@Override
	public TradePriceInterface getPrice(short type, boolean buy) {
		return shoppingAddOn.getPrice(type, buy);
	}

	@Override
	public int getSectorId() {
		return getSegmentController().getSectorId();
	}

	@Override
	public SegmentBufferInterface getSegmentBuffer() {
		return getSegmentController().getSegmentBuffer();
	}

	@Override
	public ShopInventory getShopInventory() {
		Faction faction = ((FactionState) getState()).getFactionManager().getFaction(getFactionId());

		if(faction != null && faction.isNPC()) {
			if((EntityType.SPACE_STATION.dbPrefix + DatabaseEntry.removePrefixWOException(faction.getHomebaseUID())).equals(getSegmentController().getUniqueIdentifier())) {
				//				System.err.println("-.-.-.- LOADING HOME NPC SHOP INV");
				return (ShopInventory) ((NPCFaction) faction).getInventory();
			} else {
				//				System.err.println("NOT HOMEBASE: "+faction.getHomebaseUID()+"; "+getSegmentController().getUniqueIdentifier());
			}
		} else {
		}
		return shopInventory;
	}

	@Override
	public Set<String> getShopOwners() {
		return shoppingAddOn.getOwnerPlayers();
	}

	/**
	 * @return the shoppingAddOn
	 */
	@Override
	public ShoppingAddOn getShoppingAddOn() {
		return shoppingAddOn;
	}

	@Override
	public Transform getWorldTransform() {
		return getSegmentController().getWorldTransform();
	}

	@Override
	public void modCredits(long i) {
		shoppingAddOn.modCredits(i);
	}

	@Override
	public int getFactionId() {
		return getSegmentController().getFactionId();
	}

	@Override
	public boolean isInfiniteSupply() {
		return shoppingAddOn.isInfiniteSupply();
	}

	@Override
	public boolean isAiShop() {
		return shoppingAddOn.isAIShop();
	}

	@Override
	protected void fromExtraTag(Tag tag) {
		if(tag.getValue() != null && tag != null && tag.getType() == Type.STRUCT) {
			Tag[] v = (Tag[]) tag.getValue();

			shopInventory.fromTagStructure(v[0]);
			shoppingAddOn.fromTagStructure(v[1]);

			if(v.length > 2 && v[2].getType() == Type.STRUCT) {
				missileCapacity.getElementManager().readFromTag(v[2]);
				if(v.length > 4 && v[3].getType() == Type.STRUCT && v[4].getType() == Type.STRUCT) {
					cannonCapacity.getElementManager().readFromTag(v[3]);
					beamCapacity.getElementManager().readFromTag(v[4]);
				}
				if(v.length > 5 && v[5].getType() == Type.STRUCT) repairCapacity.getElementManager().readFromTag(v[5]);
			}
		}
	}

	@Override
	protected Tag getExtraTag() {
		return new Tag(Type.STRUCT, "exS", new Tag[]{
				shopInventory.toTagStructure(),
				shoppingAddOn.toTagStructure(),
				missileCapacity.getElementManager().toTag(),
				cannonCapacity.getElementManager().toTag(),
				beamCapacity.getElementManager().toTag(),
				repairCapacity.getElementManager().toTag(),
				FinishTag.INST
		});

	}

	@Override
	public void sendInventoryModification(IntCollection slots, long parameter) {

		if(parameter == Long.MIN_VALUE) {
			InventoryMultMod m = new InventoryMultMod(slots, shopInventory, parameter);
			getInventoryInterface().getInventoryMultModBuffer().add(new RemoteInventoryMultMod(m, getSegmentController().getNetworkObject()));
		} else {
			super.sendInventoryModification(slots, parameter);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#handleGlobalInventory(org.schema.game.common.data.player.inventory.InventoryMultMod)
	 */
	@Override
	protected void handleGlobalInventory(InventoryMultMod a) {
		shopInventory.handleReceived(a, getInventoryInterface());
	}

	@Override
	protected void handleGlobalInventorySlotRemove(InventorySlotRemoveMod a) {
		boolean send = isOnServer();
		shopInventory.removeSlot(a.slot, send);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#initFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		shoppingAddOn.initFromNetwokObject(from);
	}

	@Override
	public void initialize(StateInterface state) {

		powerAddOn = new PowerAddOn(this, getSegmentController());
		shoppingAddOn = new ShoppingAddOn(this);
		shieldAddOn = new ShieldAddOn(this, getSegmentController());

		modules.add(explosive = new ManagerModuleSingle(new ExplosiveElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.EXPLOSIVE_ID));

		modules.add(lift = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), LiftCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.LIFT_ELEMENT));

		modules.add(door = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), DoorCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.DOOR_ELEMENT));

		modules.add(railPickup = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), RailPickupCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.PICKUP_AREA));

		modules.add(shields = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), ShieldRegenCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.SHIELD_REGEN_ID));
		modules.add(armorHp = new ManagerModuleSingle<>(new VoidElementManager<>(getSegmentController(), ArmorHPCollection.class), ElementKeyMap.CORE_ID, ElementKeyMap.CORE_ID));
		modules.add(shieldCapacity = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), ShieldCapacityCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.SHIELD_CAP_ID));

		modules.add(power = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_ID_OLD));

		modules.add(missileCapacity = new ManagerModuleSingle(new MissileCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.MISSILE_CAPACITY_MODULE));

		modules.add(cannonCapacity = new ManagerModuleSingle(new CannonCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.CANNON_CAPACITY_MODULE));

		modules.add(beamCapacity = new ManagerModuleSingle(new DamageBeamCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.BEAM_CAPACITY_MODULE));

		modules.add(repairCapacity = new ManagerModuleSingle(new RepairCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.REPAIR_PASTE_MODULE));

		modules.add(reactor = new ManagerModuleSingle(new MainReactorElementManager(getSegmentController(), MainReactorCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_MAIN));

		modules.add(stabilizer = new ManagerModuleSingle(new StabilizerElementManager(getSegmentController(), StabilizerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_STABILIZER));

		modules.add(conduit = new ManagerModuleSingle(new ConduitElementManager(getSegmentController(), ConduitCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_CONDUIT));

		modules.add(railMassEnhancer = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), RailMassEnhancerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.RAIL_MASS_ENHANCER));

		modules.add(powerCapacity = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerCapacityCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_CAP_ID));

		modules.add(powerBattery = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerBatteryCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_BATTERY));

		modules.add(turretDockingBlock = new ManagerModuleCollection(new TurretDockingBlockElementManager(getSegmentController()), ElementKeyMap.TURRET_DOCK_ID, ElementKeyMap.TURRET_DOCK_ENHANCE_ID));

		modules.add(fixedDockingBlock = new ManagerModuleCollection(new FixedDockingBlockElementManager(getSegmentController()), ElementKeyMap.FIXED_DOCK_ID, ElementKeyMap.FIXED_DOCK_ID_ENHANCER));

		modules.add(repair = new ManagerModuleCollection(new RepairElementManager(getSegmentController()), ElementKeyMap.REPAIR_CONTROLLER_ID, ElementKeyMap.REPAIR_ID));

		modules.add(warpgate = new ManagerModuleCollection(new WarpgateElementManager(getSegmentController(), (warpDestinationMap = new Long2ObjectOpenHashMap()), (warpDestinationLocalMap = new Long2ObjectOpenHashMap())), ElementKeyMap.WARP_GATE_CONTROLLER, ElementKeyMap.WARP_GATE_MODULE));

		modules.add(racegate = new ManagerModuleCollection(new RacegateElementManager(getSegmentController(), (raceDestinationMap = new Long2ObjectOpenHashMap()), (raceDestinationLocalMap = new Long2ObjectOpenHashMap())), ElementKeyMap.RACE_GATE_CONTROLLER, ElementKeyMap.RACE_GATE_MODULE));

		modules.add(activationgate = new ManagerModuleCollection(new ActivationGateElementManager(getSegmentController()), ElementKeyMap.ACTIVATION_GATE_CONTROLLER, ElementKeyMap.ACTIVATION_GATE_MODULE));

		modules.add(transporter = new ManagerModuleCollection(new TransporterElementManager(getSegmentController()), ElementKeyMap.TRANSPORTER_CONTROLLER, ElementKeyMap.TRANSPORTER_MODULE));

		modules.add(shipyard = new ManagerModuleCollection(new ShipyardElementManager(getSegmentController()), ElementKeyMap.SHIPYARD_COMPUTER, ElementKeyMap.SHIPYARD_MODULE));

		modules.add(cargo = new ManagerModuleCollection(new CargoElementManager(getSegmentController(), ElementKeyMap.STASH_ELEMENT), ElementKeyMap.STASH_ELEMENT, ElementKeyMap.CARGO_SPACE));

		modules.add(lockBox = new ManagerModuleCollection<>(new CargoElementManager(getSegmentController(), Blocks.LOCK_BOX.getId()), Blocks.LOCK_BOX.getId(), ElementKeyMap.CARGO_SPACE));

		modules.add(jumpProhibiter = new ManagerModuleCollection(new JumpInhibitorElementManager(getSegmentController()), ElementKeyMap.JUMP_INHIBITOR_COMPUTER, ElementKeyMap.JUMP_INHIBITOR_MODULE));

		modules.add(weapon = new ManagerModuleCollection(new CannonElementManager(getSegmentController()), ElementKeyMap.WEAPON_CONTROLLER_ID, ElementKeyMap.WEAPON_ID));

		modules.add(dumbMissile = new ManagerModuleCollection(new DumbMissileElementManager(getSegmentController()), ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID, ElementKeyMap.MISSILE_DUMB_ID));

		modules.add(activation = new ManagerModuleCollection(new ActivationElementManager(getSegmentController()), Element.TYPE_SIGNAL, Element.TYPE_ALL));

		modules.add(railSpeed = new ManagerModuleCollection(new RailSpeedElementManager(getSegmentController()), ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER, Element.TYPE_RAIL_TRACK));

		modules.add(spaceScanner = new ManagerModuleCollection(new LongRangeScannerElementManager(getSegmentController()), ElementKeyMap.SCANNER_COMPUTER, ElementKeyMap.SCANNER_MODULE));

		modules.add(salvage = new ManagerModuleCollection(new SalvageElementManager(getSegmentController()), ElementKeyMap.SALVAGE_CONTROLLER_ID, ElementKeyMap.SALVAGE_ID));

		modules.add(pushPulse = new ManagerModuleCollection(new PushPulseElementManager(getSegmentController()), ElementKeyMap.PUSH_PULSE_CONTROLLER_ID, ElementKeyMap.PUSH_PULSE_ID));

		modules.add(damageBeam = new ManagerModuleCollection(new DamageBeamElementManager(getSegmentController()), ElementKeyMap.DAMAGE_BEAM_COMPUTER, ElementKeyMap.DAMAGE_BEAM_MODULE));

		modules.add(trigger = new ManagerModuleCollection(new TriggerElementManager(getSegmentController()), ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER, ElementKeyMap.SIGNAL_TRIGGER_AREA));

		modules.add(sensor = new ManagerModuleCollection(new SensorElementManager(getSegmentController()), ElementKeyMap.SIGNAL_SENSOR, ElementKeyMap.ACTIVAION_BLOCK_ID));

		modules.add(empEffect = new ManagerModuleCollection(new EmEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_EMP_COMPUTER, ElementKeyMap.EFFECT_EMP_MODULE));

		modules.add(shopManager = (new ManagerModuleCollection(new ShopElementManager(getSegmentController()), ElementKeyMap.SHOP_BLOCK_ID, ElementKeyMap.STASH_ELEMENT)));

		modules.add(railSys = (new ManagerModuleCollection(new RailConnectionElementManager(getSegmentController()), Element.TYPE_RAIL_INV, Element.TYPE_ALL)));

		modules.add(heatEffect = new ManagerModuleCollection(new HeatEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_HEAT_COMPUTER, ElementKeyMap.EFFECT_HEAT));
		modules.add(kineticEffect = new ManagerModuleCollection(new KineticEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_KINETIC_COMPUTER, ElementKeyMap.EFFECT_KINETIC));
		modules.add(emEffect = new ManagerModuleCollection(new EmEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_EM_COMPUTER, ElementKeyMap.EFFECT_EM));

		chambers = new ObjectArrayList<>();
		for(short type : ElementKeyMap.keySet) {
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			if(info.isReactorChamberAny()) {
				ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> m = new ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>(new ReactorChamberElementManager(type, getSegmentController(), ReactorChamberCollectionManager.class), Element.TYPE_NONE, type);

				chambers.add(m);
				modules.add(m);
			}
		}

		factory = new FactoryAddOn();

		factory.initialize(getModules(), getSegmentController());

		shopInventory = new ShopInventory(this, Long.MIN_VALUE);
		//INSERTED CODE @599
		ManagerContainerRegisterEvent event = new ManagerContainerRegisterEvent(this);
		StarLoader.fireEvent(ManagerContainerRegisterEvent.class, event, this.isOnServer());

		for(ManagerModule moduleCollection : event.getRegisteredModules()) {
			this.modules.add(moduleCollection);
		}
		this.modModuleMap = new HashMap<>();
		for(Map.Entry<Short, ModManagerContainerModule> entry : event.moduleMap.entrySet()) {
			short s = entry.getKey();
			ModManagerContainerModule value = entry.getValue();
			System.out.println(s + "< >" + value);

			this.modModuleMap.put(s, value);
		}
		for(ModManagerContainerModule value : modModuleMap.values()) {
			addConsumer(value);
		}
		///
	}

	@Override
	public boolean isValidShop() {
		return shopBlockIndex != Long.MIN_VALUE || isNPCHomeBase();
	}

	@Override
	public Inventory getInventory(long pos) {
		if(pos == shopBlockIndex) {
			return shopInventory;
		}
		return super.getInventory(pos);
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		long index;
		if(inventory instanceof ShopInventory) {
			if(!isValidShop()) {
				assert (false);
				return 0;
			}
			index = shopBlockIndex;
			if(!cargo.getCollectionManagersMap().containsKey(index) || !lockBox.getCollectionManagersMap().containsKey(index)) {
				return CargoElementManager.INVENTORY_BASE_CAPACITY_STATION;
			}
		} else {
			index = inventory.getParameterIndex();
		}
		CargoCollectionManager cargoCollectionManager = cargo.getCollectionManagersMap().get(index);

		return cargoCollectionManager != null ? cargoCollectionManager.getCapacity() : 0;
	}

	@Override
	public Inventory getInventory(Vector3i pos) {
		long index = ElementCollection.getIndex(pos);
		if(index == shopBlockIndex) {
			return shopInventory;
		}
		return super.getInventory(pos);
	}

	@Override
	public void onAction() {

	}

	@Override
	public void onBlockDamage(long pos, short type, int damage, DamageDealerType damageType, Damager from) {
		super.onBlockDamage(pos, type, damage, damageType, from);
		shoppingAddOn.onHit(from);
	}

	@Override
	public boolean isOnServer() {
		return getSegmentController().isOnServer();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#onSpecialTypesRemove(short, byte, byte, byte, org.schema.game.common.data.world.Segment, boolean)
	 */
	@Override
	protected void onSpecialTypesRemove(short type, byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		super.onSpecialTypesRemove(type, x, y, z, segment, preserveControl);
		if(type == ElementKeyMap.SHOP_BLOCK_ID && !preserveControl) {
			System.err.println(getState() + " RESETTING SHOP!");
			shoppingAddOn.reset();
		}
	}

	//	protected Tag toTagStructurePriv() {
	//		Tag valid = new Tag(Type.BYTE, null, isValid() ? (byte) 1 : (byte) 0);
	//		Tag wd = new Tag(Type.STRING, null, warpDestination.uid);
	//		Tag ld = new Tag(Type.VECTOR3i, null, this.warpDestination.local);
	//
	//		return new Tag(Type.STRUCT, null, new Tag[]{valid, wd, ld, FinishTag.INST});
	//	}
	@Override
	protected void fromWarpGateTag(Tag tag) {
		if(tag.getType() == Type.BYTE) {
			return;
		}
		Tag[] t = (Tag[]) tag.getValue();
		for(int i = 0; i < t.length - 1; i++) {
			Tag[] warpGate = t[i].getStruct();
			Vector3i cPos;
			if(warpGate[0].getType() == Type.LONG) {
				long controlBlock = warpGate[0].getLong();
				cPos = ElementCollection.getPosFromIndex(controlBlock, new Vector3i());
			} else {
				cPos = warpGate[0].getVector3i();
			}

			if(getSegmentController().isLoadedFromChunk16()) {
				cPos.add(Chunk16SegmentData.SHIFT);
			}
			Tag[] detail = warpGate[1].getStruct();
			boolean valid = detail[0].getByte() != (byte) 0;
			String destination = detail[1].getString();
			Vector3i locPos = new Vector3i();
			if(detail[2].getType() == Type.VECTOR3i) {
				locPos.set((Vector3i) detail[2].getValue());
				if(getSegmentController().isLoadedFromChunk16()) {
					locPos.add(Chunk16SegmentData.SHIFT);
				}
			}
			boolean active = true; //true until proven otherwise
			long index = ElementCollection.getIndex(cPos);
			if(detail.length > 3 && detail[3].getType() == Type.BYTE){
				active = detail[3].getByte() > 0;
			}


			this.warpDestinationMap.put(index, destination);
			this.warpDestinationLocalMap.put(index, locPos);
			this.warpValidInitialMap.put(index, valid);
			this.warpActiveInitialMap.put(index,active);
		}

	}

	@Override
	protected Tag getWarpGateTag() {

		Tag[] warpgateInfo = new Tag[warpgate.getElementManager().getCollectionManagers().size() + warpDestinationMap.size() + 1];
		warpgateInfo[warpgateInfo.length - 1] = FinishTag.INST;
		int i = 0;
		for(i = 0; i < warpgate.getElementManager().getCollectionManagers().size(); i++) {
			warpgateInfo[i] = warpgate.getElementManager().getCollectionManagers().get(i).toTagStructure();
		}

		for(Entry<Long, String> a : warpDestinationMap.entrySet()) {

			Vector3i posFromIndex = ElementCollection.getPosFromIndex(a.getKey(), new Vector3i());
			Tag valid = new Tag(Type.BYTE, null, this.warpValidInitialMap.get(a.getKey()) ? (byte) 1 : (byte) 0);
			Tag warpDest = new Tag(Type.STRING, null, a.getValue());
			Tag localDest = new Tag(Type.VECTOR3i, null, warpDestinationLocalMap.get(a.getKey()));
			Tag active = new Tag(Type.BYTE,null, this.warpActiveInitialMap.get(a.getKey()) ? (byte) 1 : (byte) 0);
			Tag privTag = new Tag(Type.STRUCT, null, new Tag[]{valid, warpDest, localDest, active, FinishTag.INST});
			warpgateInfo[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.VECTOR3i, null, posFromIndex), privTag, FinishTag.INST});

			i++;
		}

		return new Tag(Type.STRUCT, null, warpgateInfo);
	}

	@Override
	protected void fromRaceGateTag(Tag tag) {
		if(tag.getType() == Type.BYTE) {
			return;
		}
		Tag[] t = (Tag[]) tag.getValue();
		for(int i = 0; i < t.length - 1; i++) {
			Tag[] inner = (Tag[]) t[i].getValue();
			Vector3i cPos;
			if(inner[0].getType() == Type.VECTOR3i) {
				cPos = inner[0].getVector3i();
			} else {
				cPos = ElementCollection.getPosFromIndex(inner[0].getLong(), new Vector3i());
			}
			if(getSegmentController().isLoadedFromChunk16()) {
				cPos.add(Chunk16SegmentData.SHIFT);
			}
			Tag[] detail = (Tag[]) inner[1].getValue();
			boolean valid = detail[0].getByte() != (byte) 0;
			String destination = ((String) detail[1].getValue());
			Vector3i locPos = new Vector3i();
			if(detail[2].getType() == Type.VECTOR3i) {
				locPos.set((Vector3i) detail[2].getValue());
				if(getSegmentController().isLoadedFromChunk16()) {
					locPos.add(Chunk16SegmentData.SHIFT);
				}
			}

			this.raceDestinationMap.put(ElementCollection.getIndex(cPos), destination);
			this.raceDestinationLocalMap.put(ElementCollection.getIndex(cPos), locPos);
			this.raceValidInitialMap.put(ElementCollection.getIndex(cPos), valid);
		}

	}

	@Override
	protected Tag getRaceGateTag() {

		Tag[] racegateInfo = new Tag[racegate.getElementManager().getCollectionManagers().size() + raceDestinationMap.size() + 1];
		racegateInfo[racegateInfo.length - 1] = FinishTag.INST;
		int i = 0;
		for(i = 0; i < racegate.getElementManager().getCollectionManagers().size(); i++) {
			racegateInfo[i] = racegate.getElementManager().getCollectionManagers().get(i).toTagStructure();
		}

		for(Entry<Long, String> a : raceDestinationMap.entrySet()) {

			Vector3i posFromIndex = ElementCollection.getPosFromIndex(a.getKey(), new Vector3i());
			Tag valid = new Tag(Type.BYTE, null, this.raceValidInitialMap.get(a.getKey()) ? (byte) 1 : (byte) 0);
			Tag wd = new Tag(Type.STRING, null, a.getValue());
			Tag ld = new Tag(Type.VECTOR3i, null, raceDestinationLocalMap.get(a.getKey()));
			Tag privTag = new Tag(Type.STRUCT, null, new Tag[]{valid, wd, ld, FinishTag.INST});

			racegateInfo[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.VECTOR3i, null, posFromIndex), privTag, FinishTag.INST});

			i++;
		}

		return new Tag(Type.STRUCT, null, racegateInfo);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#updateFromNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		shoppingAddOn.receivePrices(false);
		//		powerAddOn.updateFromNT();
		//		shieldAddOn.updateFromNT(o);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(getSegmentController(), "StationManagerContainerUpdate");
		//power first
		powerAddOn.update(timer);
		super.updateLocal(timer);
		factory.update(timer, getSegmentController().isOnServer());

		shieldAddOn.update(timer);

		shoppingAddOn.setActive(getSegmentController().getElementClassCountMap().get(ElementKeyMap.SHOP_BLOCK_ID) > 0);

		shoppingAddOn.update(timer.currentTime);

		if(isValidShop()) {
			wasValidTradeNode = true;
		}

		if(flagSendMissileCapacity) {
			missileCapacity.getElementManager().sendAmmoCapacity();
			flagSendMissileCapacity = false;
		}
		if(flagSendCannonCapacity) {
			cannonCapacity.getElementManager().sendAmmoCapacity();
			flagSendCannonCapacity = false;
		}
		if(flagSendBeamCapacity) {
			beamCapacity.getElementManager().sendAmmoCapacity();
			flagSendBeamCapacity = false;
		}
		if(flagSendRepairCapacity) {
			repairCapacity.getElementManager().sendAmmoCapacity();
			flagSendRepairCapacity = false;
		}

		if(armorHp != null) armorHp.getCollectionManager().update(timer);
		getState().getDebugTimer().end(getSegmentController(), "StationManagerContainerUpdate");
	}

	@Override
	protected void handleShopInventoryReceived(ShopInventory inventory) {
		System.err.println("[CLIENT] Received shop inventory on " + getName());
		this.shopInventory = inventory;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#updateToFullNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateToFullNetworkObject(NetworkObject o) {
		super.updateToFullNetworkObject(o);
		shopInventory.sendAll();
		shoppingAddOn.updateToFullNT();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.elements.ManagerContainer#updateToNetworkObject(org.schema.schine.network.objects.NetworkObject)
	 */
	@Override
	public void updateToNetworkObject(NetworkObject o) {
		super.updateToNetworkObject(o);
	}

	@Override
	public boolean isTargetLocking(SegmentPiece p) {
		return dumbMissile.getElementManager().isTargetLocking(p);
	}

	@Override
	public ModuleStatistics<E, ? extends ManagerContainer<E>> getStatisticsManager() {
		return null;
	}

	/**
	 * @return the dockingBlock
	 */
	@Override
	public Collection<ManagerModuleCollection<? extends DockingBlockUnit<?, ?, ?>, ? extends DockingBlockCollectionManager<?, ?, ?>, ? extends DockingBlockElementManager<?, ?, ?>>> getDockingBlock() {
		return dockingModules;
	}

	/**
	 * @return the door
	 */
	public ManagerModuleSingle<DoorUnit, DoorCollectionManager, VoidElementManager<DoorUnit, DoorCollectionManager>> getDoor() {
		return door;
	}

	private NetworkDoorInterface getDoorInterface() {
		return (NetworkDoorInterface) getSegmentController().getNetworkObject();
	}

	/**
	 * @return the doorManager
	 */
	@Override
	public DoorCollectionManager getDoorManager() {
		return door.getCollectionManager();
	}

	@Override
	public void handleClientRemoteDoor(Vector3i pos) {
		getDoorInterface().getDoorActivate().forceClientUpdates();
		Vector4i a = new Vector4i(pos);
		a.w = -1; //no percent completed
		getDoorInterface().getDoorActivate().add(new RemoteVector4i(a, getSegmentController().getNetworkObject()));
	}

	public ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> getMissile() {
		return dumbMissile;
	}

	/**
	 * @return the factory
	 */
	@Override
	public FactoryAddOn getFactory() {
		return factory;
	}

	@Override
	public ExplosiveElementManager getExplosiveElementManager() {
		return explosive.getElementManager();
	}

	@Override
	public ExplosiveCollectionManager getExplosiveCollectionManager() {
		return explosive.getCollectionManager();
	}

	/**
	 * @return the explosive
	 */
	@Override
	public ManagerModuleSingle<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> getExplosive() {
		return explosive;
	}

	/**
	 * @return the fixedDockingBlock
	 */
	public ManagerModuleCollection<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> getFixedDockingBlock() {
		return fixedDockingBlock;
	}

	@Override
	public NetworkInventoryInterface getInventoryNetworkObject() {
		return (NetworkInventoryInterface) getSegmentController().getNetworkObject();
	}

	@Override
	public int getId() {
		return getSegmentController().getId();
	}

	/**
	 * @return the lift
	 */
	public ManagerModuleSingle<LiftUnit, LiftCollectionManager, VoidElementManager<LiftUnit, LiftCollectionManager>> getLift() {
		return lift;
	}

	private NetworkLiftInterface getLiftInterface() {
		return (NetworkLiftInterface) getSegmentController().getNetworkObject();
	}

	/**
	 * @return the liftManager
	 */
	@Override
	public LiftCollectionManager getLiftManager() {
		return lift.getCollectionManager();
	}

	@Override
	public void handleClientRemoteLift(Vector3i pos) {
		getLiftInterface().getLiftActivate().forceClientUpdates();
		Vector4i a = new Vector4i(pos);
		a.w = 1;
		getLiftInterface().getLiftActivate().add(new RemoteVector4i(a, getSegmentController().getNetworkObject()));

	}

	/**
	 * This is no longer useful, and preserved only for backwards compatibility.
	 * @return The old power manager module.
	 */
	@Deprecated
	public ManagerModuleSingle<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> getPower() {
		return power;
	}

	/**
	 * @return the powerAddOn
	 */
	@Override
	public PowerAddOn getPowerAddOn() {
		return powerAddOn;
	}

	@Override
	public PowerCapacityCollectionManager getPowerCapacityManager() {
		return powerCapacity.getCollectionManager();
	}

	@Override
	public PowerBatteryCollectionManager getPowerBatteryManager() {
		return powerBattery.getCollectionManager();
	}

	@Override
	public PowerCollectionManager getPowerManager() {
		return power.getCollectionManager();
	}

	public ManagerModuleCollection<RepairUnit, RepairBeamCollectionManager, RepairElementManager> getRepair() {
		return repair;
	}

	@Override
	public ShieldRegenCollectionManager getShieldRegenManager() {
		return shields.getCollectionManager();
	}

	@Override
	public ShieldCapacityCollectionManager getShieldCapacityManager() {
		return shieldCapacity.getCollectionManager();
	}

	//	@Override
	//	public double handleShieldHit(Damager damager, Vector3f hitPoint, DamageDealerType damageType, float damage, short effectType, float effectRatio, float effectSize) {
	//
	//		return shieldAddOn.handleShieldHit(damager, hitPoint, damageType, damage, effectType, effectRatio, effectSize);
	//	}

	@Override
	public ShieldAddOn getShieldAddOn() {
		return shieldAddOn;
	}

	/**
	 * @return the shields
	 */
	public ManagerModuleSingle<ShieldRegenUnit, ShieldRegenCollectionManager, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager>> getShields() {
		return shields;
	}

	/**
	 * @return the turretDockingBlock
	 */
	public ManagerModuleCollection<TurretDockingBlockUnit, TurretDockingBlockCollectionManager, TurretDockingBlockElementManager> getTurretDockingBlock() {
		return turretDockingBlock;
	}

	/**
	 * @return the weapon
	 */
	@Override
	public ManagerModuleCollection<CannonUnit, CannonCollectionManager, CannonElementManager> getWeapon() {
		return weapon;
	}

	/**
	 * @return the trigger
	 */
	@Override
	public ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> getTrigger() {
		return trigger;
	}

	/**
	 * @return the empEffect
	 */
	public ManagerModuleCollection<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> getEmpEffect() {
		return empEffect;
	}

	@Override
	public EffectElementManager<?, ?, ?> getEffect(short effectType) {
		if(effectType == 0) {
			return null;
		}
		if(!effectMap.containsKey(effectType)) {
			throw new RuntimeException("CRITICAL: invalid weapon effect referenced " + effectType + ": " + effectMap);
		}
		return (EffectElementManager<?, ?, ?>) effectMap.get(effectType).getElementManager();
	}

	@Override
	public ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> getActivation() {
		return activation;
	}

	/**
	 * @return the warpgate
	 */
	public ManagerModuleCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> getWarpgate() {
		return warpgate;
	}

	/**
	 * @return the racegate
	 */
	public ManagerModuleCollection<RacegateUnit, RacegateCollectionManager, RacegateElementManager> getRacegate() {
		return racegate;
	}

	/**
	 * @return the scanner
	 */
	@Override
	public ManagerModuleCollection<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> getLongRangeScanner() {
		return spaceScanner;
	}

	/**
	 * @return the railSpeed
	 */
	@Override
	public ManagerModuleCollection<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> getRailSpeed() {
		return railSpeed;
	}

	/**
	 * @return the railMassEnhancer
	 */
	@Override
	public ManagerModuleSingle<RailMassEnhancerUnit, RailMassEnhancerCollectionManager, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager>> getRailMassEnhancer() {
		return railMassEnhancer;
	}

	@Override
	public ManagerModuleCollection<WarpgateUnit, WarpgateCollectionManager, WarpgateElementManager> getWarpGate() {
		return warpgate;
	}

	@Override
	public ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> getShipyard() {
		return shipyard;
	}

	@Override
	public ManagerModuleCollection<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> getJumpProhibiter() {
		return jumpProhibiter;
	}

	@Override
	public ManagerModuleCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> getTransporter() {
		return transporter;
	}

	@Override
	public ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> getCargo() {
		return cargo;
	}

	@Override
	public ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> getLockBox() {
		return lockBox;
	}

	@Override
	public long getPermissionToTrade() {
		return shoppingAddOn.getPermissionToTrade();
	}

	@Override
	public ManagerModuleSingle<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> getRailPickup() {
		return railPickup;
	}

	@Override
	public boolean isTradeNode() {

		return shoppingAddOn.isTradeNode();
	}

	@Override
	public TradeNode getTradeNode() {
		if(tradeNode == null) {
			tradeNode = new TradeNode();
			tradeNode.setFromShop(this);
		}
		return tradeNode;
	}

	public ManagerModuleCollection<ShopUnit, ShopCollectionManager, ShopElementManager> getShopManager() {
		return shopManager;
	}

	@Override
	public ManagerModuleCollection<SensorUnit, SensorCollectionManager, SensorElementManager> getSensor() {
		return sensor;
	}

	@Override
	public boolean isNPCHomeBase() {

		Faction faction = getSegmentController().getFaction();
		//		if(!isOnServer()){
		//			System.err.println("FACT "+faction);
		//			if(faction != null){
		//				String db = getSegmentController().getType().dbPrefix+((NPCFaction)faction).getHomebaseUID();
		//				System.err.println("FACT "+faction+": "+getSegmentController().getUniqueIdentifier()+" ---> "+db);
		//			}
		//		}

		return faction != null && faction.isNPC() && getSegmentController().getUniqueIdentifier().equals(((NPCFaction) faction).getHomebaseUID());
	}

	@Override
	public int getPriceString(ElementInformation info, boolean purchasePrice) {
		return shoppingAddOn.getPriceString(info, purchasePrice);
	}

	@Override
	public boolean wasValidTradeNode() {
		return wasValidTradeNode;
	}

	@Override
	public MainReactorCollectionManager getMainReactor() {
		return reactor.getCollectionManager();
	}

	@Override
	public StabilizerCollectionManager getStabilizer() {
		return stabilizer.getCollectionManager();
	}

	@Override
	public List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> getChambers() {
		return chambers;
	}

	@Override
	public ConduitCollectionManager getConduit() {
		return conduit.getCollectionManager();
	}

	@Override
	public ManagerModuleCollection<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> getRailSys() {
		return railSys;
	}

	@Override
	public ManagerModuleSingle<ArmorHPUnit, ArmorHPCollection, VoidElementManager<ArmorHPUnit, ArmorHPCollection>> getArmorHP() {
		return armorHp;
	}

	@Override
	public ManagerModuleSingle<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> getFactoryManager() {
		return factoryManager;
	}

	@Override
	public ManagerModuleSingle<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> getFleetManager() {
		return fleetManager;
	}

	@Override
	protected void afterInitialize() {
		if(specialBlocksStatic == null || specialBlocksStatic.length != ElementKeyMap.highestType) {
			specialBlocksStatic = new boolean[ElementKeyMap.highestType + 1];
			getSpecialMap(specialBlocksStatic);
		}
		specialBlocks = specialBlocksStatic;
	}

	@Override
	public void flagSendAllAmmo() {
		flagSendBeamCapacity = true;
		flagSendCannonCapacity = true;
		flagSendMissileCapacity = true;
		flagSendRepairCapacity = true;
	}

	@Override
	public AmmoCapacityCollectionManager<?, ?, ?> getAmmoSystem(AmmoCapacityElementManager.WeaponType weaponType) {
		return switch(weaponType) {
			case BEAM -> beamCapacity.getCollectionManager();
			case CANNON -> cannonCapacity.getCollectionManager();
			case MISSILE -> missileCapacity.getCollectionManager();
			case REPAIR -> repairCapacity.getCollectionManager();
		};
	}

	@Override
	public void setAmmoCapacity(AmmoCapacityElementManager.WeaponType to, float val, float timer, boolean send) {
		AmmoCapacityElementManager<?, ?, ?> e = getAmmoSystem(to).getElementManager();
		e.setAmmoCapacity(val);
		e.setAmmoTimer(timer);
		if(send) switch(to) {
			case BEAM -> flagSendBeamCapacity = true;
			case CANNON -> flagSendCannonCapacity = true;
			case MISSILE -> flagSendMissileCapacity = true;
			case REPAIR -> flagSendRepairCapacity = true;
		}
	}

	@Override
	public float getAmmoCapacity(AmmoCapacityElementManager.WeaponType weaponType) {
		return getAmmoSystem(weaponType).getElementManager().getCapacityFilled();
	}

	@Override
	public float getAmmoCapacityMax(AmmoCapacityElementManager.WeaponType weaponType) {
		return getAmmoSystem(weaponType).getElementManager().getCapacityMax();
	}

	@Override
	public float getAmmoCapacityTimer(AmmoCapacityElementManager.WeaponType weaponType) {
		return getAmmoSystem(weaponType).getElementManager().getAmmoTimer();
	}

	@Override
	public float getAmmoCapacityReloadTime(AmmoCapacityElementManager.WeaponType weaponType) {
		return getAmmoSystem(weaponType).getElementManager().getAmmoCapacityReloadTime();
	}

	@Override
	public AcidFormulaType getAcidType(long weaponId) {
		CannonCollectionManager wepColManager = weapon.getCollectionManagersMap().get(weaponId);
		if(wepColManager != null) {
			return wepColManager.getAcidFormula();
		}
		return AcidFormulaType.EQUAL_DIST;
	}

	@Override
	public ManagerModuleCollection<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> getBeam() {
		return damageBeam;
	}

	@Override
	public ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> getSalvage() {
		return salvage;
	}

	public boolean getWarpGateInitialActivation(long controllerPos) {
		return warpActiveInitialMap.get(controllerPos);
	}

	public void setWarpGateInitialActivation(long to, boolean val){
		warpActiveInitialMap.put(to,val);
	}

	public void resetWarpGateInitialActivation(long controllerPos){
		warpActiveInitialMap.remove(controllerPos);
	}
}
