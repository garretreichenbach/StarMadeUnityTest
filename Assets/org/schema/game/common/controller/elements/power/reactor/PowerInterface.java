package org.schema.game.common.controller.elements.power.reactor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix3f;

import org.schema.game.client.view.gui.reactor.ReactorTreeListener;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.ManagerUpdatableInterface;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.network.objects.PowerInterfaceNetworkObject;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.resource.tag.Tag;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

public interface PowerInterface extends ManagerUpdatableInterface, ConfigProviderSource{

	public double getPower();
	public double getMaxPower();
	public void flagStabilizersDirty();
	public List<MainReactorUnit> getMainReactors();
	public abstract List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> getChambers();
	public ReactorChamberUnit getReactorChamber(long reactorIndex);
	public ReactorElement getChamber(long reactorIdPos);
	public MainReactorUnit getReactor(long reactorIndex);
	public ConduitCollectionManager getConduits();
	public Set<ReactorChamberUnit> getConnectedChambersToConduit(long index);
	public void createPowerTree();
	public ReactorSet getReactorSet();
	public void onFinishedStabilizerChange();
	public void calcBiggestAndActiveReactor();
	public double getReactorOptimalDistance();
	public StabilizerCollectionManager getStabilizerCollectionManager();
	public double getPowerAsPercent();
	public double calcStabilization(double reactorOptimalDist, float reactorDist);
	@Override
	public void update(Timer timer);
	public void sendBonusMatrixUpdate(ReactorTree t, Matrix3f mat);
	public boolean isUsingPowerReactors();
	public void addConsumer(PowerConsumer e);
	public void removeConsumer(PowerConsumer e);
	public double getCurrentConsumption();
	public double getCurrentLocalConsumption();
	public double getCurrentLocalConsumptionPerSec();
	public double getRechargeRatePercentPerSec();
	public double getRechargeRatePowerPerSec();
	public double getCurrentPowerGain();
	public double getCurrentConsumptionPerSec();
	public void convertRequest(long chamSig, short typeTo);
	public LongArrayFIFOQueue getChamberConvertRequests();
	public LongArrayFIFOQueue getTreeBootRequests();
	public void deleteObserver(ReactorTreeListener guiReactorTree);
	public void addObserver(ReactorTreeListener guiReactorTree);
	public boolean isInAnyTree(ReactorChamberUnit e);
	public void boot(ReactorTree reactorTree);
	public boolean isActiveReactor(ReactorTree reactorTree);
	public float getReactorSwitchCooldown();
	public double getActiveReactorIntegrity();
	public float getReactorRebootCooldown();
	public void fromTagStructure(Tag tag);
	public Tag toTagStructure();
	public void updateFromNetworkObject(NetworkObject o);
	public void updateToFullNetworkObject(NetworkObject o);
	public void updateToNetworkObject(NetworkObject o);
	public void initFromNetworkObject(NetworkObject o);
	public void onBlockKilledServer(Damager from, short type, long index);
	public float getRebootTimeSec();
	public boolean isAnyDamaged();
	public void requestRecalibrate();
	public void onAnyReactorModulesChanged();
	public List<PowerConsumer> getPowerConsumerList();
	public ReactorPriorityQueue getPowerConsumerPriorityQueue();
	public boolean isOnServer();
	public PowerInterfaceNetworkObject getNetworkObject();
	public void flagConsumersChanged();
	public void consumePower(Timer timer, PowerImplementation consumedFrom);
	public float getReactorToChamberSizeRelation();
	public boolean isChamberValid(int reactorSize, int chamberSize);
	public int getNeededMinForReactorLevel(int reactorSize);
	public int getNeededMaxForReactorLevel(int reactorSize);
	public ConfigPool getConfigPool();
	public void reactorTreeReceived(ReactorTree reactorTree);
	public void checkRemovedChamber(List<ReactorChamberUnit> currentChambers);
	
	/**
	 * 
	 * @return stabilization percent ranging from 0 to 1
	 */
	public double getStabilizerEfficiencyTotal();
	public long getCurrentHp();
	public long getCurrentMaxHp();
	public SegmentController getSegmentController();
	public double getPowerConsumptionAsPercent();
	public boolean hasActiveReactors();
	public ReactorTree getActiveReactor();
	public float getChamberCapacity();
	public long getActiveReactorId();
	public boolean hasAnyReactors();
	public ManagerContainer<? extends SegmentController> getManagerContainer();
	public void switchActiveReacorToMostHp(ReactorTree currentActive);
	public void setReactorBoost(float boost);
	public float getReactorBoost();
	public boolean isInstable();
	public void registerProjectionConfigurationSource(ConfigProviderSource source);
	public void unregisterProjectionConfigurationSource(ConfigProviderSource source);
	public void addSectorConfigProjection(Collection<ConfigProviderSource> to);
	public boolean isActiveReactor(long idPos);
	public boolean isAnyRebooting();
	public double getStabilizerIntegrity();
	public List<StabilizerPath> getStabilizerPaths();
	public void onPhysicsAdd();
	public void onPhysicsRemove();
	public void drawDebugEnergyStream();
	public float getStabilzerPathRadius();
	public void flagStabilizerPathCalc();
	/**
	 * called on both client and server
	 */
	public void onLastReactorRemoved();
	public void onBlockDamageServer(Damager from, int damage, short type, long pos);
	public void onShieldDamageServer(double damage);
	public void consumePowerTick(float tickTime, Timer tm, PowerImplementation consumedFrom, float powerMod);
	public void destroyStabilizersBasedOnReactorSize(Damager damager);
	public double getStabilizerEfficiencyExtra();
	public float getExtraDamageTakenFromStabilization();
	public void doEnergyStreamCooldownOnHit(Damager damager, float damage, long hitTime);
	public float getCurrentEnergyStreamDamageCooldown();
	public void injectPower(Damager from, double power);
	public void setEnergyStreamEnabled(boolean b);
	
	
}
