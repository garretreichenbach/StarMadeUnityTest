package org.schema.game.common.controller.elements;

import api.element.block.Blocks;
import api.listener.events.register.ManagerContainerRegisterEvent;
import api.mod.StarLoader;
import api.utils.game.module.ModManagerContainerModule;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.common.util.linAlg.Vector4i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.damage.acid.AcidDamageFormula.AcidFormulaType;
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
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorBeamCollectionManager;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorElementManager;
import org.schema.game.common.controller.elements.beam.tractorbeam.TractorUnit;
import org.schema.game.common.controller.elements.cannon.CannonCollectionManager;
import org.schema.game.common.controller.elements.cannon.CannonElementManager;
import org.schema.game.common.controller.elements.cannon.CannonUnit;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.controller.elements.cargo.CargoElementManager;
import org.schema.game.common.controller.elements.cargo.CargoUnit;
import org.schema.game.common.controller.elements.dockingBeam.ActivationBeamElementManager;
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
import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.factory.FactoryElementManager;
import org.schema.game.common.controller.elements.factory.FactoryUnit;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerCollection;
import org.schema.game.common.controller.elements.factorymanager.FactoryManagerUnit;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerCollectionManager;
import org.schema.game.common.controller.elements.fleetmanager.FleetManagerUnit;
import org.schema.game.common.controller.elements.gasMiner.GasHarvesterCollectionManager;
import org.schema.game.common.controller.elements.gasMiner.GasHarvesterElementManager;
import org.schema.game.common.controller.elements.gasMiner.GasHarvesterUnit;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveCollectionManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveElementManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpDriveUnit;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorCollectionManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorElementManager;
import org.schema.game.common.controller.elements.jumpprohibiter.JumpInhibitorUnit;
import org.schema.game.common.controller.elements.mines.MineLayerCollectionManager;
import org.schema.game.common.controller.elements.mines.MineLayerElementManager;
import org.schema.game.common.controller.elements.mines.MineLayerUnit;
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
import org.schema.game.common.controller.elements.railbeam.RailBeamElementManager;
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
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerCollectionManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerElementManager;
import org.schema.game.common.controller.elements.spacescanner.LongRangeScannerUnit;
import org.schema.game.common.controller.elements.stealth.StealthCollectionManager;
import org.schema.game.common.controller.elements.stealth.StealthElementManager;
import org.schema.game.common.controller.elements.stealth.StealthUnit;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerCollectionManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerElementManager;
import org.schema.game.common.controller.elements.structurescanner.StructureScannerUnit;
import org.schema.game.common.controller.elements.thrust.ThrusterCollectionManager;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.common.controller.elements.thrust.ThrusterUnit;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterElementManager;
import org.schema.game.common.controller.elements.transporter.TransporterUnit;
import org.schema.game.common.controller.elements.trigger.TriggerCollectionManager;
import org.schema.game.common.controller.elements.trigger.TriggerElementManager;
import org.schema.game.common.controller.elements.trigger.TriggerUnit;
import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.physics.RepulseHandler;
import org.schema.game.common.data.player.inventory.*;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Segment;
import org.schema.game.network.objects.NetworkDoorInterface;
import org.schema.game.network.objects.NetworkShip;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.remote.RemoteInventoryMultMod;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteVector4i;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.*;

public class ShipManagerContainer extends ManagerContainer<Ship> implements EffectManagerContainer, ShieldContainerInterface, DockingBlockManagerInterface, ShipyardManagerContainerInterface, SalvageManagerContainer, ScannerManagerInterface, TriggerManagerInterface, ActivationManagerInterface, DoorContainerInterface, ExplosiveManagerContainerInterface, PulseManagerInterface, WeaponManagerInterface, ManagerThrustInterface, RailManagerInterface, JumpProhibiterModuleInterface, TransporterModuleInterface, MissileModuleInterface, ShopInterface {

	private PowerAddOn powerAddOn;
	private ShieldAddOn shieldAddOn;
	//	private ManagerModuleCollection<HeatMissileUnit, HeatMissileCollectionManager, HeatMissileElementManager> heatMissile;
	//	private ManagerModuleCollection<FafoMissileUnit, FafoMissileCollectionManager, FafoMissileElementManager> fafoMissile;

	private final ShipManagerModuleStatistics statisticsManager;
	private ActivationBeamElementManager dockingBeam;
	private RailBeamElementManager railBeam;
	private ManagerModuleSingle<ArmorHPUnit, ArmorHPCollection, VoidElementManager<ArmorHPUnit, ArmorHPCollection>> armorHp;
	private ManagerModuleSingle<ShieldRegenUnit, ShieldRegenCollectionManager, VoidElementManager<ShieldRegenUnit, ShieldRegenCollectionManager>> shields;
	private ManagerModuleSingle<ShieldCapacityUnit, ShieldCapacityCollectionManager, VoidElementManager<ShieldCapacityUnit, ShieldCapacityCollectionManager>> shieldCapacity;
	private ManagerModuleSingle<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> thrust;
	private ManagerModuleCollection<StealthUnit, StealthCollectionManager, StealthElementManager> stealth;
	private ManagerModuleSingle<ExplosiveUnit, ExplosiveCollectionManager, ExplosiveElementManager> explosive;
	private ManagerModuleSingle<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> railPickup;
	private ManagerModuleCollection<ActivationGateUnit, ActivationGateCollectionManager, ActivationGateElementManager> activationgate;
	private ManagerModuleCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> transporter;
	private ManagerModuleSingle<FactoryManagerUnit, FactoryManagerCollection, VoidElementManager<FactoryManagerUnit, FactoryManagerCollection>> factoryManager;
	private ManagerModuleSingle<FleetManagerUnit, FleetManagerCollectionManager, VoidElementManager<FleetManagerUnit, FleetManagerCollectionManager>> fleetManager;
	private ManagerModuleSingle<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> power;
	private ManagerModuleSingle<MainReactorUnit, MainReactorCollectionManager, MainReactorElementManager> reactor;
	private ManagerModuleSingle<StabilizerUnit, StabilizerCollectionManager, StabilizerElementManager> stabilizer;
	private ManagerModuleSingle<ConduitUnit, ConduitCollectionManager, ConduitElementManager> conduit;
	private ManagerModuleSingle<RailMassEnhancerUnit, RailMassEnhancerCollectionManager, VoidElementManager<RailMassEnhancerUnit, RailMassEnhancerCollectionManager>> railMassEnhancer;
	private ManagerModuleSingle<PowerCapacityUnit, PowerCapacityCollectionManager, VoidElementManager<PowerCapacityUnit, PowerCapacityCollectionManager>> powerCapacity;
	private ManagerModuleSingle<PowerBatteryUnit, PowerBatteryCollectionManager, VoidElementManager<PowerBatteryUnit, PowerBatteryCollectionManager>> powerBattery;
	private ManagerModuleSingle<DoorUnit, DoorCollectionManager, VoidElementManager<DoorUnit, DoorCollectionManager>> door;
	private ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> salvage;
	private ManagerModuleCollection<GasHarvesterUnit, GasHarvesterCollectionManager, GasHarvesterElementManager> gasHarvest;
	private ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> activation;
	private ManagerModuleCollection<RailSpeedUnit, RailSpeedCollectionManager, RailSpeedElementManager> railSpeed;
	private ManagerModuleCollection<TurretDockingBlockUnit, TurretDockingBlockCollectionManager, TurretDockingBlockElementManager> turretDockingBlock;
	private ManagerModuleCollection<FixedDockingBlockUnit, FixedDockingBlockCollectionManager, FixedDockingBlockElementManager> fixedDockingBlock;
	private ManagerModuleCollection<RepairUnit, RepairBeamCollectionManager, RepairElementManager> repair;
	private ManagerModuleCollection<TractorUnit, TractorBeamCollectionManager, TractorElementManager> tractorBeam;
	private ManagerModuleCollection<CannonUnit, CannonCollectionManager, CannonElementManager> weapon;
	private ManagerModuleCollection<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> jumpDrive;
	private ManagerModuleCollection<JumpInhibitorUnit, JumpInhibitorCollectionManager, JumpInhibitorElementManager> jumpProhibiter;
	private ManagerModuleCollection<LongRangeScannerUnit, LongRangeScannerCollectionManager, LongRangeScannerElementManager> spaceScanner;
	private ManagerModuleCollection<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> structureScanner;
	private ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> shipyard;
	private ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> cargo;
	private ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> lockBox;
	private ManagerModuleCollection<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> pushPulse;
	private ManagerModuleCollection<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> damageBeam;
	private ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> trigger;
	private ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> dumbMissile;
	private ManagerModuleSingle<CannonCapacityUnit, CannonCapacityCollectionManager, CannonCapacityElementManager> cannonCapacity;
	private ManagerModuleSingle<DamageBeamCapacityUnit, DamageBeamCapacityCollectionManager, DamageBeamCapacityElementManager> beamCapacity;
	private ManagerModuleSingle<MissileCapacityUnit, MissileCapacityCollectionManager, MissileCapacityElementManager> missileCapacity;
	private ManagerModuleSingle<RepairCapacityUnit, RepairCapacityCollectionManager, RepairCapacityElementManager> repairCapacity;
	private ManagerModuleCollection<SensorUnit, SensorCollectionManager, SensorElementManager> sensor;
	private ManagerModuleCollection<RailConnectionUnit, RailConnectionCollectionManager, RailConnectionElementManager> railSys;
	private ManagerModuleCollection<MineLayerUnit, MineLayerCollectionManager, MineLayerElementManager> mineLayer;

	private ManagerModuleCollection<HeatEffectUnit, HeatEffectCollectionManager, HeatEffectElementManager> heatEffect;
	private ManagerModuleCollection<KineticEffectUnit, KineticEffectCollectionManager, KineticEffectElementManager> kineticEffect;
	private ManagerModuleCollection<EmEffectUnit, EmEffectCollectionManager, EmEffectElementManager> emEffect;

	private List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers;
	public final ThrustConfiguration thrustConfiguration;
	private RepulseHandler repulseManager;
	private boolean flagSendMissileCapacity;
	private boolean flagSendCannonCapacity;
	private boolean flagSendBeamCapacity;
	private boolean flagSendRepairCapacity;

	private final CockpitManager cockpitManager;
	private static boolean[] specialBlocksStatic;
	private FactoryAddOn factory;

	public ShipManagerContainer(StateInterface state, Ship ship) {
		super(state, ship);

		statisticsManager = new ShipManagerModuleStatistics(ship, this);

		this.thrustConfiguration = new ThrustConfiguration(ship);

		cockpitManager = new CockpitManager(this);
	}

	@Override
	protected Tag getExtraTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.LONG, null, getSegmentController().lastPickupAreaUsed),
				thrustConfiguration.toTag(),
				missileCapacity.getElementManager().toTag(),
				cockpitManager.toTagStructure(),
				shopInventory.toTagStructure(),
				shoppingAddOn.toTagStructure(),
				cannonCapacity.getElementManager().toTag(),
				beamCapacity.getElementManager().toTag(),
				repairCapacity.getElementManager().toTag(),
				FinishTag.INST
		});

	}

	@Override
	protected void fromExtraTag(Tag tag) {
		if(tag.getType() == Type.STRUCT) {
			Tag[] t = (Tag[]) tag.getValue();
			if(t[0].getType() == Type.LONG) {
				getSegmentController().lastPickupAreaUsed = (Long) t[0].getValue();
				if(getSegmentController().lastPickupAreaUsed != Long.MIN_VALUE && getSegmentController().isLoadedFromChunk16()) {
					getSegmentController().lastPickupAreaUsed = ElementCollection.shiftIndex(getSegmentController().lastPickupAreaUsed, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
				}
			}

			if(t.length > 1 && t[1].getType() == Type.STRUCT) {
				thrustConfiguration.readFromTag(t[1]);
			}

			if(t.length > 2 && t[2].getType() == Type.STRUCT) {
				missileCapacity.getElementManager().readFromTag(t[2]);
			}
			if(t.length > 3 && t[3].getType() == Type.STRUCT) {
				cockpitManager.fromTagStructure(t[3]);
			}

			if(t.length > 4 && t[4].getType() == Type.STRUCT) shopInventory.fromTagStructure(t[4]);
			if(t.length > 5 && t[5].getType() == Type.STRUCT) shoppingAddOn.fromTagStructure(t[5]);

			if(t.length > 6 && t[6].getType() == Type.STRUCT) {
				cannonCapacity.getElementManager().readFromTag(t[6]);
			}
			if(t.length > 7 && t[7].getType() == Type.STRUCT) {
				beamCapacity.getElementManager().readFromTag(t[7]);
			}
			if(t.length > 8 && t[8].getType() == Type.STRUCT) repairCapacity.getElementManager().readFromTag(t[8]);
		}
	}

	public StealthElementManager getStealthElementManager() {
		return stealth.getElementManager();
	}

	@Override
	/**
	 * @return the stealth system collection
	 */
	public ManagerModuleCollection<StealthUnit, StealthCollectionManager, StealthElementManager> getStealth() {
		return stealth;
	}

	@Override
	/**
	 * @return the short range scan system collection
	 */
	public ManagerModuleCollection<StructureScannerUnit, StructureScannerCollectionManager, StructureScannerElementManager> getShortRangeScanner() {
		return structureScanner;
	}

	/**
	 * @return the dockingBeam
	 */
	public ActivationBeamElementManager getDockingBeam() {
		return dockingBeam;
	}

	public RailBeamElementManager getRailBeam() {
		return railBeam;
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
		return getSegmentController().getNetworkObject();
	}
	//	@Override
	//	public Tag toTagStructure() {
	//		Tag weapons = weapon.getElementManager().toTagStructure();
	//		return new Tag(Type.STRUCT, "shipMan0", new Tag[]{super.toTagStructure(), weapons, FinishTag.INST});
	//	}

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

	/**
	 * @return the dumbMissile
	 */
	public ManagerModuleCollection<DumbMissileUnit, DumbMissileCollectionManager, DumbMissileElementManager> getMissile() {
		return dumbMissile;
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
		return getSegmentController().getNetworkObject();
	}

	@Override
	public int getId() {
		return getSegmentController().getId();
	}

	/**
	 * This is no longer useful, and preserved only for backwards compatibility.
	 * @return The old power manager module.
	 */
	@Deprecated
	public ManagerModuleSingle<PowerUnit, PowerCollectionManager, VoidElementManager<PowerUnit, PowerCollectionManager>> getPower() {
		return power;
	}

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

	/**
	 * @return the push pulse collection
	 */
	@Override
	public ManagerModuleCollection<PushPulseUnit, PushPulseCollectionManager, PushPulseElementManager> getPushPulse() {
		return pushPulse;
	}

	/**
	 * @return the repair (astrotech) beam collection
	 */
	public ManagerModuleCollection<RepairUnit, RepairBeamCollectionManager, RepairElementManager> getRepair() {
		return repair;
	}

	/**
	 * @return the salvage beam collection
	 */
	public ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> getSalvage() {
		return salvage;
	}

	/**
	 * @return the gas harvester collection
	 */
	public ManagerModuleCollection<SalvageUnit, SalvageBeamCollectionManager, SalvageElementManager> getGasHarvester() {
		return salvage;
	}

	@Override
	public ShieldRegenCollectionManager getShieldRegenManager() {
		assert (shields != null);
		assert (shields.getCollectionManager() != null);
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
	//
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
	 * @return the thrust
	 */
	@Override
	public ManagerModuleSingle<ThrusterUnit, ThrusterCollectionManager, ThrusterElementManager> getThrust() {
		return thrust;
	}

	@Override
	public ThrusterElementManager getThrusterElementManager() {
		return thrust.getElementManager();
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

	//	@Override
	//	public void handle(ControllerStateUnit unit, Timer timer) {
	//		super.handle(unit, timer);
	//
	//		if (unit.playerState.isMouseButtonDown(0) || unit.playerState.isMouseButtonDown(1)) {
	//			dockingBeam.handle(unit, timer);
	//		}
	//		if (unit.playerState.isMouseButtonDown(0) || unit.playerState.isMouseButtonDown(1)) {
	//			railBeam.handle(unit, timer);
	//		}
	//	}

	@Override
	public void initialize(StateInterface state) {
		shoppingAddOn = new ShoppingAddOn(this);
		powerAddOn = new PowerAddOn(this, getSegmentController());
		shieldAddOn = new ShieldAddOn(this, getSegmentController());
		this.repulseManager = new RepulseHandler(getSegmentController());
		addConsumer(repulseManager);

		dockingBeam = new ActivationBeamElementManager(getSegmentController());
		addUpdatable(dockingBeam);
		railBeam = new RailBeamElementManager(getSegmentController());
		//add beam interface manually since its not part of any module
		beamInteracesSingle.add(this.railBeam.getBeamHandler());
		addUpdatable(railBeam);
		modules.add(shields = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), ShieldRegenCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.SHIELD_REGEN_ID));
		modules.add(armorHp = new ManagerModuleSingle<>(new VoidElementManager<>(getSegmentController(), ArmorHPCollection.class), ElementKeyMap.CORE_ID, ElementKeyMap.CORE_ID));
		modules.add(shieldCapacity = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), ShieldCapacityCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.SHIELD_CAP_ID));

		modules.add(railPickup = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), RailPickupCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.PICKUP_AREA));

		modules.add(thrust = new ManagerModuleSingle(new ThrusterElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.THRUSTER_ID));

		modules.add(missileCapacity = new ManagerModuleSingle(new MissileCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.MISSILE_CAPACITY_MODULE));

		modules.add(cannonCapacity = new ManagerModuleSingle(new CannonCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.CANNON_CAPACITY_MODULE));

		modules.add(beamCapacity = new ManagerModuleSingle(new DamageBeamCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.BEAM_CAPACITY_MODULE));

		modules.add(repairCapacity = new ManagerModuleSingle(new RepairCapacityElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.REPAIR_PASTE_MODULE));

		modules.add(door = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), DoorCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.DOOR_ELEMENT));

		modules.add(explosive = new ManagerModuleSingle(new ExplosiveElementManager(getSegmentController()), ElementKeyMap.CORE_ID, ElementKeyMap.EXPLOSIVE_ID));

		modules.add(power = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_ID_OLD));

		modules.add(reactor = new ManagerModuleSingle(new MainReactorElementManager(getSegmentController(), MainReactorCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_MAIN));

		modules.add(stabilizer = new ManagerModuleSingle(new StabilizerElementManager(getSegmentController(), StabilizerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_STABILIZER));

		modules.add(conduit = new ManagerModuleSingle(new ConduitElementManager(getSegmentController(), ConduitCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.REACTOR_CONDUIT));

		modules.add(railMassEnhancer = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), RailMassEnhancerCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.RAIL_MASS_ENHANCER));

		modules.add(powerCapacity = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerCapacityCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_CAP_ID));

		modules.add(powerBattery = new ManagerModuleSingle(new VoidElementManager(getSegmentController(), PowerBatteryCollectionManager.class), Element.TYPE_NONE, ElementKeyMap.POWER_BATTERY));

		modules.add(salvage = new ManagerModuleCollection(new SalvageElementManager(getSegmentController()), ElementKeyMap.SALVAGE_CONTROLLER_ID, ElementKeyMap.SALVAGE_ID));

		modules.add(gasHarvest = new ManagerModuleCollection(new GasHarvesterElementManager(getSegmentController()), ElementKeyMap.GAS_SCOOP_CONTROLLER, ElementKeyMap.GAS_SCOOP_MODULE));

		modules.add(activation = new ManagerModuleCollection(new ActivationElementManager(getSegmentController()), Element.TYPE_SIGNAL, Element.TYPE_ALL));

		modules.add(railSpeed = new ManagerModuleCollection(new RailSpeedElementManager(getSegmentController()), ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER, Element.TYPE_RAIL_TRACK));

		modules.add(activationgate = new ManagerModuleCollection(new ActivationGateElementManager(getSegmentController()), ElementKeyMap.ACTIVATION_GATE_CONTROLLER, ElementKeyMap.ACTIVATION_GATE_MODULE));

		modules.add(transporter = new ManagerModuleCollection(new TransporterElementManager(getSegmentController()), ElementKeyMap.TRANSPORTER_CONTROLLER, ElementKeyMap.TRANSPORTER_MODULE));

		modules.add(turretDockingBlock = new ManagerModuleCollection(new TurretDockingBlockElementManager(getSegmentController()), ElementKeyMap.TURRET_DOCK_ID, ElementKeyMap.TURRET_DOCK_ENHANCE_ID));

		modules.add(shipyard = new ManagerModuleCollection(new ShipyardElementManager(getSegmentController()), ElementKeyMap.SHIPYARD_COMPUTER, ElementKeyMap.SHIPYARD_MODULE));

		modules.add(fixedDockingBlock = new ManagerModuleCollection(new FixedDockingBlockElementManager(getSegmentController()), ElementKeyMap.FIXED_DOCK_ID, ElementKeyMap.FIXED_DOCK_ID_ENHANCER));

		modules.add(repair = new ManagerModuleCollection(new RepairElementManager(getSegmentController()), ElementKeyMap.REPAIR_CONTROLLER_ID, ElementKeyMap.REPAIR_ID));

		modules.add(tractorBeam = new ManagerModuleCollection(new TractorElementManager(getSegmentController()), ElementKeyMap.TRACTOR_BEAM_COMPUTER, ElementKeyMap.TRACTOR_BEAM));

		modules.add(weapon = new ManagerModuleCollection(new CannonElementManager(getSegmentController()), ElementKeyMap.WEAPON_CONTROLLER_ID, ElementKeyMap.WEAPON_ID));

		modules.add(jumpDrive = new ManagerModuleCollection(new JumpDriveElementManager(getSegmentController()), ElementKeyMap.JUMP_DRIVE_CONTROLLER, ElementKeyMap.JUMP_DRIVE_MODULE));

		modules.add(jumpProhibiter = new ManagerModuleCollection(new JumpInhibitorElementManager(getSegmentController()), ElementKeyMap.JUMP_INHIBITOR_COMPUTER, ElementKeyMap.JUMP_INHIBITOR_MODULE));

		modules.add(spaceScanner = new ManagerModuleCollection(new LongRangeScannerElementManager(getSegmentController()), ElementKeyMap.SCANNER_COMPUTER, ElementKeyMap.SCANNER_MODULE));

		modules.add(structureScanner = new ManagerModuleCollection(new StructureScannerElementManager(getSegmentController()), ElementKeyMap.INTELL_COMPUTER, ElementKeyMap.INTELL_ANTENNA));

		modules.add(stealth = new ManagerModuleCollection(new StealthElementManager(getSegmentController()), ElementKeyMap.STEALTH_COMPUTER, ElementKeyMap.STEALTH_MODULE));

		modules.add(cargo = new ManagerModuleCollection<>(new CargoElementManager(getSegmentController(), ElementKeyMap.STASH_ELEMENT), Blocks.STORAGE.getId(), ElementKeyMap.CARGO_SPACE));

		modules.add(lockBox = new ManagerModuleCollection<>(new CargoElementManager(getSegmentController(), Blocks.LOCK_BOX.getId()), Blocks.LOCK_BOX.getId(), ElementKeyMap.CARGO_SPACE));

		modules.add(pushPulse = new ManagerModuleCollection(new PushPulseElementManager(getSegmentController()), ElementKeyMap.PUSH_PULSE_CONTROLLER_ID, ElementKeyMap.PUSH_PULSE_ID));

		modules.add(damageBeam = new ManagerModuleCollection(new DamageBeamElementManager(getSegmentController()), ElementKeyMap.DAMAGE_BEAM_COMPUTER, ElementKeyMap.DAMAGE_BEAM_MODULE));

		modules.add(trigger = new ManagerModuleCollection(new TriggerElementManager(getSegmentController()), ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER, ElementKeyMap.SIGNAL_TRIGGER_AREA));

		modules.add(sensor = new ManagerModuleCollection(new SensorElementManager(getSegmentController()), ElementKeyMap.SIGNAL_SENSOR, ElementKeyMap.ACTIVAION_BLOCK_ID));

		modules.add(dumbMissile = new ManagerModuleCollection(new DumbMissileElementManager(getSegmentController()), ElementKeyMap.MISSILE_DUMB_CONTROLLER_ID, ElementKeyMap.MISSILE_DUMB_ID));

		//		modules.add(fafoMissile = new ManagerModuleCollection<FafoMissileUnit, FafoMissileCollectionManager, FafoMissileElementManager>(new FafoMissileElementManager(getSegmentController()), ElementKeyMap.MISSILE_FAFO_CONTROLLER_ID, ElementKeyMap.MISSILE_FAFO_ID));
		//		modules.add(heatMissile = new ManagerModuleCollection<HeatMissileUnit, HeatMissileCollectionManager, HeatMissileElementManager>(new HeatMissileElementManager(getSegmentController()), ElementKeyMap.MISSILE_HEAT_CONTROLLER_ID, ElementKeyMap.MISSILE_HEAT_ID));

		modules.add(heatEffect = new ManagerModuleCollection(new HeatEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_HEAT_COMPUTER, ElementKeyMap.EFFECT_HEAT));
		modules.add(kineticEffect = new ManagerModuleCollection(new KineticEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_KINETIC_COMPUTER, ElementKeyMap.EFFECT_KINETIC));
		modules.add(emEffect = new ManagerModuleCollection(new EmEffectElementManager(getSegmentController()), ElementKeyMap.EFFECT_EM_COMPUTER, ElementKeyMap.EFFECT_EM));

		modules.add(railSys = (new ManagerModuleCollection(new RailConnectionElementManager(getSegmentController()), Element.TYPE_RAIL_INV, Element.TYPE_ALL)));

		modules.add(mineLayer = (new ManagerModuleCollection(new MineLayerElementManager(getSegmentController()), ElementKeyMap.MINE_LAYER, ElementKeyMap.MINE_CORE)));

		chambers = new ObjectArrayList<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>>();

		short[] typeList = ElementKeyMap.typeList();

		for(int i = 0; i < ElementKeyMap.chamberAnyTypes.size(); i++) {
			short type = ElementKeyMap.chamberAnyTypes.getShort(i);
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> m = new ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>(new ReactorChamberElementManager(type, getSegmentController(), ReactorChamberCollectionManager.class), Element.TYPE_NONE, type);

			chambers.add(m);
			modules.add(m);
		}

		if(((GameStateInterface) getState()).getGameState() != null && ((GameStateInterface) getState()).getGameState().isAllowFactoryOnShips()) {
			factory = new FactoryAddOn();
			factory.initialize(getModules(), getSegmentController());
		}

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
	public void onAction() {
		if(getSegmentController().railController.getRoot() != getSegmentController() && getSegmentController().railController.getRoot() instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) getSegmentController().railController.getRoot()).getManagerContainer().onAction();
		}
		if(StealthElementManager.REUSE_DELAY_ON_ACTION_MS >= 0) {
			stealth.getElementManager().stopStealth(StealthElementManager.REUSE_DELAY_ON_ACTION_MS);
		}
		if(StealthElementManager.REUSE_DELAY_ON_ACTION_MS >= 0) {
			stealth.getElementManager().stopStealth(StealthElementManager.REUSE_DELAY_ON_ACTION_MS);
		}

		onRevealingAction();
	}

	@Override
	public void onAddedElementSynched(short type, Segment segment, long absIndex, long time, boolean revalidate) {

		super.onAddedElementSynched(type, segment, absIndex, time, revalidate);
		switch(type) {
			case (ElementKeyMap.COCKPIT_ID) -> {
				cockpitManager.addCockpit(absIndex);
				break;
			}
			case (ElementKeyMap.REPULSE_MODULE) -> {
				int lX = ByteUtil.modUSeg(ElementCollection.getPosX(absIndex));
				int lY = ByteUtil.modUSeg(ElementCollection.getPosY(absIndex));
				int lZ = ByteUtil.modUSeg(ElementCollection.getPosZ(absIndex));
				byte orientation = segment.getSegmentData().getOrientation((byte) lX, (byte) lY, (byte) lZ);
				//ecode orientation onto long pos
				repulseManager.add(absIndex, orientation);
				break;
			}
			case (ElementKeyMap.RAIL_BLOCK_DOCKER) -> {
				railBeam.addRailDocker(ElementCollection.getIndex4(absIndex, type));
				break;
			}
			case (ElementKeyMap.AI_ELEMENT) -> {
				getSegmentController().getAiConfiguration().setControllerBlock(new SegmentPiece(segment, (byte) ByteUtil.modUSeg(ElementCollection.getPosX(absIndex)), (byte) ByteUtil.modUSeg(ElementCollection.getPosY(absIndex)), (byte) ByteUtil.modUSeg(ElementCollection.getPosZ(absIndex))));
				break;
			}
		}
	}

	@Override
	public void onRemovedElementSynched(short type, int oldSize, byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		super.onRemovedElementSynched(type, oldSize, x, y, z, segment, preserveControl);
		switch(type) {
			case (ElementKeyMap.COCKPIT_ID) -> {
				cockpitManager.removeCockpit(segment.getAbsoluteIndex(x, y, z));
				break;
			}
			case (ElementKeyMap.REPULSE_MODULE) -> {
				repulseManager.remove(segment.getAbsoluteIndex(x, y, z));
				break;
			}
			case (ElementKeyMap.RAIL_BLOCK_DOCKER) -> {
				railBeam.removeRailDockers(ElementCollection.getIndex4(segment.getAbsoluteIndex(x, y, z), type));
				break;
			}
			case (ElementKeyMap.AI_ELEMENT) -> {
				getSegmentController().getAiConfiguration().setControllerBlock(null);
				break;
			}
		}
	}

	@Override
	public void onHitNotice() {
		if(getSegmentController().isOnServer()) {
			if(isCloaked() || isJamming()) {
				if(getSegmentController().getNetworkObject().onHitNotices.isEmpty()) {
					stealth.getElementManager().onHit();
					stealth.getElementManager().onHit();
					getSegmentController().getNetworkObject().onHitNotices.add(new RemoteBoolean(true, getSegmentController().getNetworkObject()));
				}
			}
		} else {
			stealth.getElementManager().onHit();
			stealth.getElementManager().onHit();
		}

	}

	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(getSegmentController(), "ShipManagerContainerUpdate");
		//power first
		powerAddOn.update(timer);
		super.updateLocal(timer);
		thrustConfiguration.updateLocal(timer);

		shoppingAddOn.setActive(getSegmentController().getElementClassCountMap().get(ElementKeyMap.SHOP_BLOCK_ID) > 0);
		shoppingAddOn.update(timer.currentTime);

		if(factory != null) {
			factory.update(timer, isOnServer());
		} else {
			if(((GameStateInterface) getState()).getGameState().isAllowFactoryOnShips()) {
				factory = new FactoryAddOn();
				factory.initialize(getModules(), getSegmentController());
				for(ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager> m : factory.map.values()) {
					getModulesMap().put(m.getElementID(), m);
				}
			}
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
		repulseManager.handle(timer);

		if(armorHp != null) armorHp.getCollectionManager().update(timer);

		getState().getDebugTimer().end(getSegmentController(), "ShipManagerContainerUpdate");
	}

	@Override
	public boolean isTargetLocking(SegmentPiece p) {
		return dumbMissile.getElementManager().isTargetLocking(p);
	}

	@Override
	public ShipManagerModuleStatistics getStatisticsManager() {
		return statisticsManager;
	}

	public boolean isCloaked() {
		return getStealthElementManager().isActive() && getStealthElementManager().hasCloakCapability();
	}

	public boolean isJamming() {
		return getStealthElementManager().isActive() && getStealthElementManager().hasJamCapability();
	}

	public boolean hasRepulsors() {
		return repulseManager.hasRepulsors();
	}

	/**
	 * @return the activation
	 */
	@Override
	public ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> getActivation() {
		return activation;
	}

	/**
	 * @param activation the activation to set
	 */
	public void setActivation(ManagerModuleCollection<AbstractUnit, ActivationCollectionManager, ActivationElementManager> activation) {
		this.activation = activation;
	}

	/**
	 * @return the trigger
	 */
	@Override
	public ManagerModuleCollection<TriggerUnit, TriggerCollectionManager, TriggerElementManager> getTrigger() {
		return trigger;
	}

	@Override
	public EffectElementManager<?, ?, ?> getEffect(short effectType) {
		if(effectType == 0) {
			return null;
		}
		if(!effectMap.containsKey(effectType)) {
			throw new RuntimeException("CRITICAL: invalid weapon effect referenced " + ElementKeyMap.toString(effectType) + ": " + effectMap + "\non entity " + getSegmentController() + " (" + getState() + ")");
		}
		return (EffectElementManager<?, ?, ?>) effectMap.get(effectType).getElementManager();
	}

	/**
	 * @return the jumpDrive
	 */
	public ManagerModuleCollection<JumpDriveUnit, JumpDriveCollectionManager, JumpDriveElementManager> getJumpDrive() {
		return jumpDrive;
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
	public ManagerModuleSingle<RailPickupUnit, RailPickupCollectionManager, VoidElementManager<RailPickupUnit, RailPickupCollectionManager>> getRailPickup() {
		return railPickup;
	}

	@Override
	public ManagerModuleCollection<SensorUnit, SensorCollectionManager, SensorElementManager> getSensor() {
		return sensor;
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

	@Deprecated
	public JumpAddOn getJumpAddOn() {
		return null;
	}

	public RepulseHandler getRepulseManager() {
		return repulseManager;
	}

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		NetworkShip s = (NetworkShip) from;
		thrustConfiguration.initFromNetworkObject(s);
		cockpitManager.initFromNetworkObject(s);
		shoppingAddOn.initFromNetwokObject(from);
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		thrustConfiguration.updateFromNetworkObject((NetworkShip) o, senderId);
		cockpitManager.updateFromNetworkObject();
		shoppingAddOn.receivePrices(false);
	}

	@Override
	public void updateToFullNetworkObject(NetworkObject o) {
		super.updateToFullNetworkObject(o);
		thrustConfiguration.updateToFullNetworkObject((NetworkShip) o);
		cockpitManager.updateToFullNetworkObject((NetworkShip) o);
		shopInventory.sendAll();
	}

	@Override
	public void updateToNetworkObject(NetworkObject o) {
		super.updateToNetworkObject(o);
		thrustConfiguration.updateToNetworkObject((NetworkShip) o);
	}

	@Override
	protected void afterInitialize() {
		if(specialBlocksStatic == null || specialBlocksStatic.length != ElementKeyMap.highestType) {
			specialBlocksStatic = new boolean[ElementKeyMap.highestType + 1];
			getSpecialMap(specialBlocksStatic);
		}
		specialBlocks = specialBlocksStatic;
	}

	public ManagerModuleCollection<MineLayerUnit, MineLayerCollectionManager, MineLayerElementManager> getMineLayer() {
		return mineLayer;
	}

	public ManagerModuleCollection<TractorUnit, TractorBeamCollectionManager, TractorElementManager> getTractorBeam() {
		return tractorBeam;
	}

	public void setTractorBeam(ManagerModuleCollection<TractorUnit, TractorBeamCollectionManager, TractorElementManager> tractorBeam) {
		this.tractorBeam = tractorBeam;
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

	public CockpitManager getCockpitManager() {
		return cockpitManager;
	}

	@Override
	public ManagerModuleCollection<DamageBeamUnit, DamageBeamCollectionManager, DamageBeamElementManager> getBeam() {
		return damageBeam;
	}

	@Override
	public ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> getShipyard() {
		return shipyard;
	}

	private ShoppingAddOn shoppingAddOn;
	private ShopInventory shopInventory;

	@Override
	public void fillInventory(boolean send, boolean full) throws NoSlotFreeException {

	}

	@Override
	public long getCredits() {
		return shoppingAddOn.getCredits();
	}

	@Override
	public ShopNetworkInterface getNetworkObject() {
		return (ShopNetworkInterface) getSegmentController().getNetworkObject();
	}

	@Override
	public int getPriceString(ElementInformation info, boolean purchasePrice) {
		return shoppingAddOn.getPriceString(info, purchasePrice);
	}

	@Override
	public long getPermissionToPurchase() {
		return shoppingAddOn.getPermissionToPurchase();
	}

	@Override
	public long getPermissionToTrade() {
		return shoppingAddOn.getPermissionToTrade();
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
		return shopInventory;
	}

	@Override
	public Set<String> getShopOwners() {
		return shoppingAddOn.getOwnerPlayers();
	}

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
	public boolean isTradeNode() {
		return false; //Don't allow trade nodes on ships, but mobile caravans is fine
	}

	@Override
	public TradeNode getTradeNode() {
		return null;
	}

	@Override
	public boolean isValidShop() {
		return shopBlockIndex != Long.MIN_VALUE;
	}

	@Override
	public boolean isNPCHomeBase() {
		return false;
	}

	@Override
	public boolean wasValidTradeNode() {
		return false;
	}

	@Override
	public void onBlockDamage(long pos, short type, int damage, DamageDealerType damageType, Damager from) {
		super.onBlockDamage(pos, type, damage, damageType, from);
		shoppingAddOn.onHit(from);
	}

	@Override
	protected void onSpecialTypesRemove(short type, byte x, byte y, byte z, Segment segment, boolean preserveControl) {
		super.onSpecialTypesRemove(type, x, y, z, segment, preserveControl);
		if(type == ElementKeyMap.SHOP_BLOCK_ID && !preserveControl) {
			System.err.println(getState() + " RESETTING SHOP!");
			shoppingAddOn.reset();
		}
	}

	@Override
	public void sendInventoryModification(IntCollection slots, long parameter) {
		if(parameter == Long.MIN_VALUE) {
			InventoryMultMod m = new InventoryMultMod(slots, shopInventory, parameter);
			getInventoryInterface().getInventoryMultModBuffer().add(new RemoteInventoryMultMod(m, getSegmentController().getNetworkObject()));
		} else super.sendInventoryModification(slots, parameter);
	}

	@Override
	protected void handleGlobalInventory(InventoryMultMod a) {
		shopInventory.handleReceived(a, getInventoryInterface());
	}

	@Override
	protected void handleGlobalInventorySlotRemove(InventorySlotRemoveMod a) {
		shopInventory.removeSlot(a.slot, isOnServer());
	}

	@Override
	public Inventory getInventory(long pos) {
		if(pos == shopBlockIndex) return shopInventory;
		else return super.getInventory(pos);
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
			if(!cargo.getCollectionManagersMap().containsKey(index) && !lockBox.getCollectionManagersMap().containsKey(index)) return CargoElementManager.INVENTORY_BASE_CAPACITY_STATION;
		} else index = inventory.getParameterIndex();
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
}
