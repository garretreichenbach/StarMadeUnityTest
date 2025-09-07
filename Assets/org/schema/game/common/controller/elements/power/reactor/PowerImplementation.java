package org.schema.game.common.controller.elements.power.reactor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix3f;

import api.StarLoaderHooks;
import api.listener.events.calculate.CurrentPowerCalculateEvent;
import api.listener.events.calculate.MaxPowerCalculateEvent;
import api.listener.events.systems.ReactorRecalibrateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PowerChangeListener.PowerChangeType;
import org.schema.game.client.view.gui.reactor.ReactorTreeListener;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerModuleSingle;
import org.schema.game.common.controller.elements.ReactorLevelCalcStyle;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ConduitCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberCollectionManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberElementManager;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorBonusMatrixUpdate;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorElement;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorSet;
import org.schema.game.common.controller.elements.power.reactor.tree.ReactorTree;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.PowerInterfaceNetworkObject;
import org.schema.game.network.objects.remote.RemoteReactorBonusMatrix;
import org.schema.game.network.objects.remote.RemoteReactorSet;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.container.UpdateWithoutPhysicsObjectInterface;
import org.schema.schine.network.objects.remote.LongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Matrix3f;
import java.util.*;

public class PowerImplementation implements PowerInterface, UpdateWithoutPhysicsObjectInterface{

	private static final byte TAG_VERSION = 1;
	private double power;
	private final ReactorSet reactorSet;
	private MainReactorUnit biggestReactor;
	public final ManagerContainer<? extends SegmentController> container;
	private final List<PowerConsumer> powerConsumerList = new ObjectArrayList<PowerConsumer>();
	private final Set<PowerConsumer> powerConsumers = new ObjectOpenHashSet<PowerConsumer>();
	private boolean consumersChanged;
	private double currentConsumption;
	private double currentLocalConsumption;
	private double currentPowerGain;
	private double currentConsumptionPerSec;
	private final LongArrayFIFOQueue chamberConvertRequests = new LongArrayFIFOQueue();
	private final LongArrayFIFOQueue treeBootRequests = new LongArrayFIFOQueue();
	private long selectedReactor = Long.MIN_VALUE;
	private float reactorSwitchCooldown;
	private ObjectArrayFIFOQueue<ReactorSet> receivedReactorSets = new ObjectArrayFIFOQueue<ReactorSet>();
	private boolean requestedRecalibrate;
	private final Long2IntOpenHashMap changedModuleSet = new Long2IntOpenHashMap();
	private long lastChangeSend;
	private final ReactorPriorityQueue priorityQueue;
	private final ObjectArrayFIFOQueue<ReactorTree> receivedTrees = new ObjectArrayFIFOQueue<ReactorTree>();
	private int waitingForPool;
	private boolean usingReactorsFromTag;
	private double maxPower;
	private boolean hadReactor;
	private float reactorBoost;
	private final Long2ObjectOpenHashMap<ConfigProviderSource> configProjectionSources = new Long2ObjectOpenHashMap<ConfigProviderSource>();
	private double currentMaxPower;
	private final List<StabilizerPath> stabilizerPaths = new ObjectArrayList<StabilizerPath>();
	private boolean flagStabPathCalc;;
	private final List<ReactorTreeListener> listener = new ObjectArrayList<ReactorTreeListener>();
	
//	private final StabilizationShieldCondition stabShieldCondition = new StabilizationShieldCondition();
//	private final StabilizationSystemCondition stabSystemCondition = new StabilizationSystemCondition();
//	private final StabilizationBlockCondition stabBlockCondition = new StabilizationBlockCondition();
	private float reactorRebootCooldown;
	private long firstTreeCreate = -1;
	private float accumulated;
	private float currentEnergyStreamCooldown;
	private float lastEnergyStreamCooldown;
	private double currentLocalConsumptionPerSec;
	private long lastStabReactor = Long.MIN_VALUE;
	private final ObjectArrayFIFOQueue<ReactorBonusMatrixUpdate> receivedBonusMatrixUpdates = new ObjectArrayFIFOQueue<ReactorBonusMatrixUpdate>();
	private double injectedPowerPerSec;
	private float injectedPowerTimoutSec;
	private long selectedReactorToSet = Long.MIN_VALUE;
	private boolean energyStream;
	
	public PowerImplementation(ManagerContainer<? extends SegmentController> container) {
		super();
		this.container = container;
		this.reactorSet = new ReactorSet(this); 
		priorityQueue = new ReactorPriorityQueue(this);
		container.addUpdatable(this);
		container.addEffectSource(this);
		
		
	}

	@Override
	public double getPower() {
		return power;
	}

	@Override
	public double getMaxPower() {
		//minimum is capacity that one reactor block would provide
		return Math.max(VoidElementManager.REACTOR_POWER_CAPACITY_MULTIPLIER, maxPower);
	}

	@Override
	public void flagStabilizersDirty() {
		getStabilizer().flagDirty();
	}

	public MainReactorCollectionManager getMainReactor(){
		return container.getMainReactor();
	}
	public StabilizerCollectionManager getStabilizer(){
		return container.getStabilizer();
	}
	@Override
	public List<MainReactorUnit> getMainReactors() {
		return getMainReactor().getElementCollections();
	}

	@Override
	public Set<ReactorChamberUnit> getConnectedChambersToConduit(long index) {
		return getConduits().getConnected(index);
	}

	@Override
	public List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> getChambers() {
		return container.getChambers();
	}

	@Override
	public ReactorChamberUnit getReactorChamber(long reactorIndex) {
		List<ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager>> chambers = getChambers();
		for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> cmb : chambers){
			for(ReactorChamberUnit cm : cmb.getCollectionManager().getElementCollections()){
				if(cm.getNeighboringCollection().contains(reactorIndex)){
					return cm;
				}
			}
		}
		return null;
	}
	@Override
	public MainReactorUnit getReactor(long reactorIndex) {
		List<MainReactorUnit> mainReactors = getMainReactors();
			for(MainReactorUnit cm : mainReactors){
				if(cm.getNeighboringCollection().contains(reactorIndex)){
					return cm;
				}
			}
		return null;
	}

	@Override
	public ConduitCollectionManager getConduits() {
		return container.getConduit();
	}

	@Override
	public void createPowerTree() {
		if(!isOnServer()){
			// creation only on server and sent via 
			return;
		}
		
		this.reactorSet.build();
		
		
		boolean foundSelected = false;
		for(ReactorTree t : reactorSet.getTrees()){
			if(t.getId() == selectedReactor){
				foundSelected = true;
				break;
			}
		}
		if(!foundSelected && getSegmentController().isFullyLoaded()){
			if(biggestReactor != null){
				for(ReactorTree t : reactorSet.getTrees()){
					if(t.getId() == biggestReactor.idPos){
						selectedReactor = biggestReactor.idPos;
						foundSelected = true;
						break;
					}
				}
			}else if(reactorSet.size() > 0){
				selectedReactor = reactorSet.getTrees().get(0).getId();
			}else{
				selectedReactor = Long.MIN_VALUE;
			}
		}
		
		getNetworkObject().getReactorSetBuffer().add(new RemoteReactorSet(reactorSet, isOnServer()));
		if(getSegmentController().isFullyLoaded()){
			if(firstTreeCreate > 0 && System.currentTimeMillis() - firstTreeCreate > 10000){
				dischargeAllPowerConsumers();
				this.reactorSet.dischargeAll = true;
			}
			if(firstTreeCreate <= 0){
				firstTreeCreate = System.currentTimeMillis();
			}
		}
		
		for(ReactorTreeListener l : listener) {
			l.onTreeChanged(reactorSet);
		}
	}
	public void dischargeAllPowerConsumers() {
//		System.err.println(getState()+"; "+getSegmentController()+" DISCHARGING ALL POWER CONSUMERS");
		for(PowerConsumer c : powerConsumerList){
			c.dischargeFully();
		}
	}
	@Override
	public PowerInterfaceNetworkObject getNetworkObject(){
		return (PowerInterfaceNetworkObject) ((SendableSegmentController)getSegmentController()).getNetworkObject();
	}
	@Override
	public ReactorSet getReactorSet() {
		return this.reactorSet;
	}
	@Override
	public void calcBiggestAndActiveReactor() {
		maxPower = 0;
		biggestReactor = null;
		int biggest = 0;
		
		
		for(MainReactorUnit u : getMainReactor().getElementCollections()){
			if(u.size() > biggest){
				biggest = u.size();
				biggestReactor = u;
			}
		}
	}
	public static double getStabilization(double stabilization, boolean withFree){
		return ((withFree ? VoidElementManager.REACTOR_STABILIZER_FREE_MAIN_REACTOR_BLOCKS : 0)+stabilization)*VoidElementManager.REACTOR_STABILIZATION_MULTIPLIER;
	}
	public double getStabilization(){
		double stabilization = getStabilization(getStabilizer().getStabilization(), true);
//		if(getSegmentController().railController.isRoot() && getSegmentController().toString().contains("Pillar")) {
//			System.err.println("STAB "+stabilization+"; "+getState());
//		}
		
		return stabilization;
	}
	
	public double getBiggestReactorSize(){
		if(biggestReactor == null){
			return 0;
		}else{
			return (double)biggestReactor.size()*(double)VoidElementManager.REACTOR_MAIN_COUNT_MULTIPLIER;
		}
	}
	public double getActiveReactorInitialSize(){
		ReactorTree reactorTree = reactorSet.getTreeMap().get(selectedReactor);
		if(reactorTree == null){
			return 0;
		}else{
			return (double)reactorTree.getSize()*(double)VoidElementManager.REACTOR_MAIN_COUNT_MULTIPLIER;
		}
	}
	public double getActiveReactorCurrentSize(){
		ReactorTree reactorTree = reactorSet.getTreeMap().get(selectedReactor);
		if(reactorTree == null){
			return 0;
		}else{
			return (double)reactorTree.getActualSize()*(double)VoidElementManager.REACTOR_MAIN_COUNT_MULTIPLIER;
		}
	}
	@Override
	public void onFinishedStabilizerChange() {
		if(getActiveReactor() != null){
			getStabilizerCollectionManager().calculateStabilization(getActiveReactor().getId(), getActiveReactor().getCenterOfMass());
			lastStabReactor = getActiveReactor().getId();
		}else{
			lastStabReactor = Long.MIN_VALUE;
		}
		
		//stabilizers get flagged on every power structure change
		maxPower = calculatInitialMaxPower();
		currentMaxPower = calculatCurrentMaxPower();
		flagStabilizerPathCalc();
		
		if(isOnServer()) {
			getSegmentController().getRuleEntityManager().triggerOnReactorActivityChange();
		}
	}
	

	@Override
	public StabilizerCollectionManager getStabilizerCollectionManager() {
		return container.getStabilizer();
	}

	//REPLACE METHOD
	private double calculatInitialMaxPower() {
		double stabilization = this.getStabilizationPowerEfficiency();
		double v = Math.min(this.getActiveReactorInitialSize(), stabilization) * (double) VoidElementManager.REACTOR_POWER_CAPACITY_MULTIPLIER;
		MaxPowerCalculateEvent event = new MaxPowerCalculateEvent(this, v);
		StarLoader.fireEvent(MaxPowerCalculateEvent.class, event, this.isOnServer());
		v = event.getPower();
		return v;
	}
	//
	//REPLACE METHOD
	private double calculatCurrentMaxPower() {
		double stabilization = this.getStabilizationPowerEfficiency();
		double v = Math.min(this.getActiveReactorCurrentSize(), stabilization) * (double) VoidElementManager.REACTOR_POWER_CAPACITY_MULTIPLIER;
		CurrentPowerCalculateEvent event = new CurrentPowerCalculateEvent(this, v);
		StarLoader.fireEvent(CurrentPowerCalculateEvent.class, event, this.isOnServer());
		v = event.getPower();
		return v;
	}
	//
	public double getStabilizationPowerEfficiency(){
		return getStabilization() * (1.0/VoidElementManager.REACTOR_STABILIZATION_POWER_EFFECTIVE_FULL);
		
		
	}
	public static int getMaxStabilizerCount(){
		return VoidElementManager.REACTOR_STABILIZER_GROUPS_MAX < 0 ? Integer.MAX_VALUE : VoidElementManager.REACTOR_STABILIZER_GROUPS_MAX;
	}
	@Override
	public double getReactorOptimalDistance() {
		int blocks = Math.max(1, ((int)getBiggestReactorSize()-VoidElementManager.REACTOR_STABILIZER_FREE_MAIN_REACTOR_BLOCKS));
		double dist;
		switch(VoidElementManager.REACTOR_CALC_STYLE) {
			case LINEAR -> dist = Math.max(0, VoidElementManager.REACTOR_STABILIZER_STARTING_DISTANCE + VoidElementManager.REACTOR_STABILIZER_DISTANCE_PER_MAIN_REACTOR_BLOCK * (double) blocks);
			case EXP -> {
				dist = Math.max(0, VoidElementManager.REACTOR_STABILIZER_STARTING_DISTANCE + Math.pow(blocks, VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP) * VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP_MULT);
				//softcap
				if(blocks >= VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_BLOCKS_START) {
					dist *= (1 + (Math.pow(Math.max((double) (blocks / VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_BLOCKS_START - 1), 0), VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_EXP) * VoidElementManager.REACTOR_STABILIZER_DISTANCE_EXP_SOFTCAP_MULT));
				}
			}
			case LOG_LEVELED -> dist = getLogLeveled(blocks);
			case LOG -> dist = (Math.max(0, VoidElementManager.REACTOR_STABILIZER_STARTING_DISTANCE + (Math.max(0, Math.max(0, Math.log10(blocks)) + VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_OFFSET)) * VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_FACTOR));
			default -> throw new RuntimeException("Illegal calc style " + VoidElementManager.REACTOR_CALC_STYLE);
		}
		assert(!Double.isNaN(dist));
		dist *= VoidElementManager.REACTOR_STABILIZER_DISTANCE_TOTAL_MULT;
		dist = getSegmentController().getConfigManager().apply(StatusEffectType.POWER_STABILIZER_DISTANCE, dist);
		assert(!Double.isNaN(dist));
		return dist;
	}
	private static double getLogLeveled(int blocks){
		double dist;
		int reactorLevel = getReactorLevel(blocks);
		dist = Math.max(0, 
				VoidElementManager.REACTOR_STABILIZER_STARTING_DISTANCE + 
				Math.pow(reactorLevel, VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_EXP) 
				* VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_MULTIPLIER);
		if(!VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_STEPS){
			double dist0 = dist; 
			double dist1 = Math.max(0, 
					VoidElementManager.REACTOR_STABILIZER_STARTING_DISTANCE + 
					Math.pow(reactorLevel+1, VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_EXP) 
					* VoidElementManager.REACTOR_STABILIZER_DISTANCE_LOG_LEVELED_MULTIPLIER);
			int blocksMin = getMinNeededFromReactorLevel(reactorLevel, VoidElementManager.REACTOR_CHAMBER_BLOCKS_PER_MAIN_REACTOR_AND_LEVEL);
			int blocksMax = getMinNeededFromReactorLevel(reactorLevel+1, VoidElementManager.REACTOR_CHAMBER_BLOCKS_PER_MAIN_REACTOR_AND_LEVEL);
			
			
			
			int m = blocks - blocksMin;
			int max = blocksMax - blocksMin;
			float lin = (float)m / (float)max;
			
			double d = (dist1 - dist0)*lin;
			
			dist = dist0 + d;
			
		}
		return dist;
	}
	public static void main(String[] args){
		int lvl = 0;
		for(int i = 0; i < 250000; i+=1000){
			System.err.println(printReactorLevel(i)+" -> DISTANCE "+getLogLeveled(i));
		}
	}
	@Override
	public double calcStabilization(double reactorOptimalDist, float reactorDist) {
		assert(!Double.isNaN(reactorOptimalDist));
		assert(!Double.isNaN(reactorDist));
		return calcStabilizationStatic(reactorOptimalDist, reactorDist);
	}
	public static double calcStabilizationStatic(double reactorOptimalDist, float reactorDist) {
		if(reactorOptimalDist <= 0d){
			return 1;
		}
		double perc = reactorDist / reactorOptimalDist;
		if(perc >= VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ONE){
			return 1;
		}else if(perc <= VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ZERO){
			return 0;
		}else{
			double d = VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ONE - VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ZERO;
			double n = perc - VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ZERO;
			double val = n / d;
			assert(!Double.isNaN(val)):d+" / "+n+" = NaN"+"; optDist: "+reactorOptimalDist+"; ractorDist: "+reactorDist+"; perc: "+perc;
			return val; 
		}
	}
	public static double calcStabilizationDistanceForStabilizationPercent(double reactorOptimalDist, float stab) {
		double start = reactorOptimalDist * VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ZERO;
		double end = reactorOptimalDist * VoidElementManager.REACTOR_STABILIZER_LINEAR_FALLOFF_ONE;
		double d = end - start;
		double dist = start + (stab * d);
		return dist; 
	}


	@Override
	public double getRechargeRatePercentPerSec(){
		float basic = VoidElementManager.REACTOR_RECHARGE_PERCENT_PER_SECOND;
		float boostedValue = basic + basic * reactorBoost;
		float boostedModified = getSegmentController().getConfigManager()
		.apply(StatusEffectType.POWER_RECHARGE_EFFICIENCY, boostedValue);
		return boostedModified;
	}
	@Override
	public double getRechargeRatePowerPerSec(){
		double recharge = getRechargeRatePercentPerSec();
		if(power <= 0.00000001){
			recharge *= VoidElementManager.REACTOR_RECHARGE_EMPTY_MULTIPLIER;
		}
		if(isStabilizerPathHit()){
			recharge *= VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_HIT_COOLDOWN_REACTOR_EFFICIENCY;
		}
		return recharge * currentMaxPower;
	}
	
	@Override
	public void sendBonusMatrixUpdate(ReactorTree t, Matrix3f mat){
		ReactorBonusMatrixUpdate u = new ReactorBonusMatrixUpdate();
		u.id = t.getId();
		u.bonusMatrix = new Matrix3f(mat);
		
		getNetworkObject().getReactorBonusMatrixUpdateBuffer().add(new RemoteReactorBonusMatrix(u, isOnServer()));
	}
	
	@Override
	public void update(Timer timer) {
		
		if(!isOnServer()){
			selectedReactor = selectedReactorToSet;
		}
		
		if(getConfigPool() == null){
			waitingForPool++;
			if(waitingForPool % 1000 == 0){
				System.err.println("[CONFIGPOOL] "+getState()+" WAITING FOR CONFIG POOL ("+waitingForPool+")");
			}
			return;
		}
		if(flagStabPathCalc){
			calculateStabilizerPaths();
			flagStabPathCalc = false;
		}
		getSegmentController().getPhysicsDataContainer().onPhysicsObjectUpdateEnergyBeamInterface = this;
		for(StabilizerPath p : stabilizerPaths){
			p.update(timer);
		}
		while(!receivedBonusMatrixUpdates.isEmpty()){
			ReactorBonusMatrixUpdate dequeue = receivedBonusMatrixUpdates.dequeue();
			ReactorTree reactorTree = reactorSet.getTreeMap().get(dequeue.id);
			if(reactorTree != null){
				reactorTree.getBonusMatrix().set(dequeue.bonusMatrix);
				if(isOnServer()){
					sendBonusMatrixUpdate(reactorTree, dequeue.bonusMatrix);
				}
				flagStabilizersDirty();
			}
		}
		while(!receivedTrees.isEmpty()){
			receivedTrees.dequeue().onConfigPoolReceived();
		}
		if(getActiveReactor() != null && lastStabReactor != getActiveReactor().getId()){
			getStabilizerCollectionManager().calculateStabilization(getActiveReactor().getId(), getActiveReactor().getCenterOfMass());
			lastStabReactor = getActiveReactor().getId();
		}
		maxPower = calculatInitialMaxPower();
		currentMaxPower = calculatCurrentMaxPower();
		priorityQueue.updateLocal(timer, this);
		
		reactorSwitchCooldown = Math.max(0, reactorSwitchCooldown-timer.getDelta());
		reactorRebootCooldown = Math.max(0, reactorRebootCooldown-timer.getDelta());
		
		if(this.requestedRecalibrate){
			createPowerTree();
			//INSERTED CODE
			ReactorRecalibrateEvent ev = new ReactorRecalibrateEvent(this);
			StarLoader.fireEvent(ev, true);
			StarLoaderHooks.onReactorRecalibrateEvent(ev);
			///
			this.requestedRecalibrate = false;
			
			if(getSegmentController().isOnServer()) {
				getSegmentController().getRuleEntityManager().triggerOnReactorActivityChange();
			}
		}
		
		if(isOnServer()){
			if(currentEnergyStreamCooldown > lastEnergyStreamCooldown || (lastEnergyStreamCooldown > 0 && currentEnergyStreamCooldown <= 0)){
				//send update
				getNetworkObject().getEnergyStreamCooldownBuffer().add(currentEnergyStreamCooldown);
			}
		}
		lastEnergyStreamCooldown = currentEnergyStreamCooldown;
		if(injectedPowerTimoutSec > 0) {
			injectedPowerTimoutSec = Math.max(0, injectedPowerTimoutSec - timer.getDelta());
		}else {
			injectedPowerPerSec = 0;
		}
		if(!isDocked()){
			this.currentPowerGain = timer.getDelta() * (getRechargeRatePowerPerSec()+injectedPowerPerSec);
			if(isStabilizerPathHit()){
				
				currentEnergyStreamCooldown = Math.max(0f, currentEnergyStreamCooldown - timer.getDelta());
				getSegmentController().popupOwnClientMessage("LPESHHIT", Lng.str("---WARNING---\nReactor Energy Stream has been hit!\nReactor efficiency at %s%%!\n(%s sec)",
						StringTools.formatPointZero(VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_HIT_COOLDOWN_REACTOR_EFFICIENCY*100f),
						StringTools.formatPointZero(currentEnergyStreamCooldown)
						), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			consumePower(timer, this);
			
			power = Math.min(getMaxPower(), power+currentPowerGain);
		}else{
			this.currentPowerGain = 0;
		}
		
		if(getSegmentController().getElementClassCountMap().get(ElementKeyMap.REACTOR_MAIN) > 0){
			hadReactor = true;
		}else if(hadReactor){
			if(isOnServer() && !isAnyDamaged()){
				requestRecalibrate();
			}
			hadReactor = false;
		}
		if(isOnServer()){
			if(!changedModuleSet.isEmpty() && timer.currentTime - lastChangeSend > 700){
				for(Entry s : changedModuleSet.long2IntEntrySet()){
					//this buffer is for sending after change has done 
					//timer to prevent from spamming send
					long moduleId = s.getLongKey();
					int actualSize = s.getIntValue();
					LongIntPair p = new LongIntPair();
					p.l = moduleId;
					p.i = actualSize;
					getNetworkObject().getReactorChangeBuffer().add(new RemoteLongIntPair(p, isOnServer()));
				}
				changedModuleSet.clear();
				lastChangeSend = timer.currentTime;
				
				getSegmentController().getRuleEntityManager().triggerOnReactorActivityChange();
			}
		}else{
			if(!changedModuleSet.isEmpty()){
				for(Entry s : changedModuleSet.long2IntEntrySet()){
					//this buffer is for receiving after server change
					
					long moduleId = s.getLongKey();
					int actualSize = s.getIntValue();
					boolean hit = reactorSet.applyReceivedSizeChange(moduleId, actualSize);
					if(hit){
						setReactorRebootCooldown();
					}
				}
				changedModuleSet.clear();
				
			}
		}
			
		
		
		

		while(!receivedReactorSets.isEmpty()){
			ReactorSet set = receivedReactorSets.dequeue();
			
			int sizeBef = reactorSet.getTrees().size();
			reactorSet.apply(set);
			
			if(sizeBef > 0 && reactorSet.getTrees().size() == 0){
				//remove all paths
				onLastReactorRemoved();
			}
			
//			System.err.println("[CLIENT] RECEIVED TREE: ");
//			set.print();
//			reactorSet.print();
			for(ReactorTreeListener l : listener) {
				l.onTreeChanged(reactorSet);
			}
			if(set.dischargeAll){
				//discharge all if server sent newly created reactor (not a loaded one from disk)
				dischargeAllPowerConsumers();
			}
		}
		
		
		while(!treeBootRequests.isEmpty()){
			long l = treeBootRequests.dequeueLong();
			for(ReactorTree t : reactorSet.getTrees()){
				if(t.getId() == l){
					doBoot(t);
					dischargeAllPowerConsumers();
				}
			}
			if(isOnServer()){
				//deligate
				getNetworkObject().getBootRequestBuffer().add(l);
			}
		}
		
		while(!chamberConvertRequests.isEmpty()){
			long req = chamberConvertRequests.dequeueLong();
			long id = ElementCollection.getPosIndexFrom4(req);
			short type = (short) ElementCollection.getType(req);
			System.err.println("REQUEST TO CONVERT RECEIVED: "+id+" -> "+ElementKeyMap.toString(type));
			ReactorChamberUnit unit = null;
			for(ManagerModuleSingle<ReactorChamberUnit, ReactorChamberCollectionManager, ReactorChamberElementManager> c : getChambers()){
				List<ReactorChamberUnit> elementCollections = c.getElementManager().getCollection().getElementCollections();
				for(ReactorChamberUnit u : elementCollections){
					if(u.idPos == id){
						unit = u;
						break;
					}
				}
				if(unit != null){
					break;
				}
			}
			if(unit != null){
				boolean changed = false;
				Set<RemoteSegment> set = new ObjectOpenHashSet<RemoteSegment>();
				for(long l : unit.getNeighboringCollection()){
					SegmentPiece p = ((SendableSegmentController)getSegmentController())
							.getSegmentBuffer().getPointUnsave(l);
					
					if(p.getType() != type){
						changed = true;
						p.setType(type);
						
						try{
							//add the replaced one
							p.getSegment().getSegmentData().applySegmentData(
									p.x, p.y, p.z, p.getData(), 0, false,
									p.getAbsoluteIndex(), false, false, timer.currentTime);
							set.add((RemoteSegment)p.getSegment());
							
						}catch(SegmentDataWriteException e){
							try {
								SegmentDataWriteException.replaceData(p.getSegment());
								p.getSegment().getSegmentData().applySegmentData(
										p.x, p.y, p.z, p.getData(), 0, false,
										p.getAbsoluteIndex(), false, false, timer.currentTime);
								
							} catch (SegmentDataWriteException e1) {
								throw new RuntimeException(e1);
							}
						}
						
						RemoteSegmentPiece n = new RemoteSegmentPiece(p, isOnServer());
						((SendableSegmentController)getSegmentController()).sendBlockMod(n);
					}
				}
				for(RemoteSegment s : set){
					s.setLastChanged(timer.currentTime);
				}
				if(changed){
					unit.clear();
				}
			}else{
				System.err.println("[SERVER][POWER][ERROR] Chamber unit to convert not found! sig "+id+"; to type "+type);
			}
		}
		
		
		reactorSet.update(timer, selectedReactor);
	}
	

	private boolean isDocked() {
		return getSegmentController().railController.isDocked();
	}

	@Override
	public void flagConsumersChanged(){
		consumersChanged = true;
	}
	@Override
	public void consumePower(Timer timer, PowerImplementation consumedFrom){
		
		
		accumulated += timer.getDelta();
		
		float tickRate = 0.05f;
		if(accumulated > tickRate){
			
			int ticks = (int)(accumulated / tickRate);
			
			float tickTime = ticks * tickRate; 
			
			float powerMod = 1;
			consumePowerTick(tickTime, timer, consumedFrom, powerMod);
			
			accumulated -= tickTime;
		}
	}
	@Override
	public void consumePowerTick(float tickTime, Timer timer, PowerImplementation consumedFrom, float powerMod){
		if(consumersChanged){
			powerConsumerList.clear();
			powerConsumerList.addAll(powerConsumers);
			Collections.sort(powerConsumerList, priorityQueue);
			consumersChanged = false;
		}
		currentConsumptionPerSec = 0d;
		currentConsumption = 0d;
		currentLocalConsumption = 0d;
		currentLocalConsumptionPerSec = 0d;
		
		if(getSegmentController().isUsingOldPower()){
			return;
		}
		
		final int cSize = powerConsumerList.size();
		priorityQueue.resetStats();
		for(int i = 0; i < cSize; i++){
			PowerConsumer powerConsumer = powerConsumerList.get(i);
			consumePowerFromConsumer(timer, tickTime, powerConsumer, consumedFrom, powerMod);
		}
	}
	private void consumePowerFromConsumer(Timer tm, float tickTime, PowerConsumer powerConsumer, PowerImplementation consumedFrom, float powerMod){
		if(!powerConsumer.isPowerConsumerActive()){
			return;
		}
		priorityQueue.addAmount(powerConsumer.getPowerConsumerCategory());
		
		if(powerConsumer instanceof RailPowerConsumer){
			for(RailRelation l : getSegmentController().railController.next){
				SegmentController docked = l.docked.getSegmentController();
				if(docked instanceof ManagedSegmentController<?>){
					ManagerContainer<?> dockMan = ((ManagedSegmentController<?>)docked).getManagerContainer();
					float dockedPowerMod = powerMod;
					if(!docked.railController.isPowered()) {
						//unpowered
						dockedPowerMod = 0;
					}
					
					//do rail consumption with reactor from root
					dockMan.getPowerInterface().consumePowerTick(tickTime, tm, consumedFrom, dockedPowerMod);
//					System.err.println("POW::: "+dockMan.getPowerInterface().getCurrentConsumptionPerSec());
					priorityQueue.addConsumption(
							powerConsumer.getPowerConsumerCategory(), 
							dockMan.getPowerInterface().getCurrentConsumptionPerSec());
					powerConsumer.setPowered(
							dockMan.getPowerInterface().getPowerConsumerPriorityQueue().getTotalPercent());
				}
			}
		}else{
			
			double powerConsumedPerSecCharging;
			double powerConsumedPerSecResting;
			double powerConsumedPerSec;
			boolean powerCharging = powerConsumer.isPowerCharging(tm.currentTime);
			
			powerConsumedPerSecCharging = powerConsumer.getPowerConsumedPerSecondCharging();
			powerConsumedPerSecCharging = getSegmentController().getConfigManager().apply(StatusEffectType.POWER_MODULE_CHARGING_RATE_MOD, powerConsumedPerSecCharging);
		
			powerConsumedPerSecResting = powerConsumer.getPowerConsumedPerSecondResting();
			powerConsumedPerSecResting = getSegmentController().getConfigManager().apply(StatusEffectType.POWER_TOP_OFF_RATE_MOD, powerConsumedPerSecResting);
			
			if(powerCharging){
				powerConsumedPerSec = powerConsumedPerSecCharging;
			}else{
				powerConsumedPerSec = powerConsumedPerSecResting;
			}
			priorityQueue.addConsumption(powerConsumer.getPowerConsumerCategory(), powerConsumedPerSec);
			//per second value
			consumedFrom.currentConsumptionPerSec += powerConsumedPerSec;
			if(consumedFrom != this) {
				currentConsumptionPerSec += powerConsumedPerSec;
			}
			currentLocalConsumptionPerSec += powerConsumedPerSec;
			
			double powerConsumedActual = powerConsumedPerSec * tickTime;
			consumedFrom.currentConsumption += powerConsumedActual;
			if(consumedFrom != this) {
				currentConsumption += powerConsumedActual;
			}
			currentLocalConsumption += powerConsumedActual;
			
//			if(!getSegmentController().railController.isRoot() && getSegmentController().railController.getRoot() instanceof SpaceStation){
//				System.err.println("CONSUMED FROM:::: "+getSegmentController()+"; "+powerConsumedActual+"; "+powerConsumer);
//			}
//			System.err.println("POWER ON "+getState()+" "+getSegmentController()+": "+consumedFrom.power+";  POWERMOD: "+powerMod);
			double restingActual = powerConsumedPerSecResting  * tickTime;
			if(consumedFrom.power*powerMod <= 0){
				//out of power
				powerConsumer.setPowered(0);
//				System.err.println("POWER EMPT ON "+getState()+" "+getSegmentController()+": "+consumedFrom.power);
				powerConsumer.reloadFromReactor(0, tm, tickTime, powerCharging, 0);
			}else{
				float poweredResting;
				if(restingActual > 0d && (consumedFrom.power*powerMod) < restingActual){
					poweredResting = (float) ((consumedFrom.power*powerMod)/restingActual);
				}else{
					poweredResting = 1f;
				}
				
				float poweredPart;
				if(powerConsumedActual > 0d && (consumedFrom.power*powerMod) < powerConsumedActual){
					poweredPart = (float) ((consumedFrom.power*powerMod)/powerConsumedActual);
					consumedFrom.power = 0;
				}else{
					consumedFrom.power -= powerConsumedActual;
					poweredPart = 1f;
				}
				powerConsumer.setPowered(poweredPart);
//				System.err.println("POWER ON "+getState()+" "+getSegmentController()+": "+consumedFrom.power+": "+poweredResting);
				powerConsumer.reloadFromReactor(poweredPart * tickTime, tm, tickTime, powerCharging, poweredResting);
			}
			
			
		}
		priorityQueue.addPercent(powerConsumer.getPowerConsumerCategory(), powerConsumer.getPowered());
		priorityQueue.addTotalPercent(powerConsumer.getPowered());
	}
	@Override
	public SegmentController getSegmentController() {
		return container.getSegmentController();
	}
	public boolean isClientOwnObject() {
		return container.getSegmentController().isClientOwnObject();
	}
	@Override
	public boolean isOnServer() {
		return container.isOnServer();
	}

	@Override
	public boolean isUsingPowerReactors(){
		if(!getSegmentController().isFullyLoaded()){
			return usingReactorsFromTag;
		}
		return !getSegmentController().isUsingOldPower();
	}

	@Override
	public double getPowerAsPercent() {
		if(getSegmentController().railController.isDockedAndExecuted()){
			SegmentController root = getSegmentController().railController.getRoot();
			if(root instanceof ManagedSegmentController<?>){
				PowerInterface powerInterface = ((ManagedSegmentController<?>)root).getManagerContainer().getPowerInterface();
				return powerInterface.getPowerAsPercent();
			}
		}
		return maxPower > 0 ? (double) (power / maxPower) : 0.0d;
	}
	

	@Override
	public void addConsumer(PowerConsumer e) {
		powerConsumers.add(e);
		consumersChanged = true;
	}
	@Override
	public void removeConsumer(PowerConsumer e) {
		powerConsumers.remove(e);
		consumersChanged = true;
	}
	@Override
	public double getCurrentConsumption() {
		return currentConsumption;
	}

	@Override
	public double getCurrentLocalConsumption() {
		return currentLocalConsumption;
	}
	@Override
	public double getCurrentLocalConsumptionPerSec() {
		return currentLocalConsumptionPerSec;
	}

	@Override
	public double getCurrentPowerGain() {
		return currentPowerGain;
	}

	@Override
	public double getCurrentConsumptionPerSec() {
		return currentConsumptionPerSec;
	}

	@Override
	public void convertRequest(long chamId, short typeTo) {
		getNetworkObject()
			.getConvertRequestBuffer().add(ElementCollection.getIndex4(chamId, typeTo));
	}

	@Override
	public LongArrayFIFOQueue getChamberConvertRequests() {
		return chamberConvertRequests;
	}
	@Override
	public LongArrayFIFOQueue getTreeBootRequests() {
		return treeBootRequests;
	}

	@Override
	public boolean isInAnyTree(ReactorChamberUnit e) {
		return reactorSet.isInAnyTree(e);
	}
	private void doBoot(ReactorTree reactorTree){
		long oldReactor = selectedReactor;
		selectedReactor = reactorTree.getId();
		reactorSwitchCooldown = VoidElementManager.REACTOR_SWITCH_COOLDOWN_SEC;
		
		reactorTree.resetBootedRecursive();
		flagStabilizerPathCalc();
		System.err.println(getState()+"[REACTOR] Reactor switched: "+oldReactor+" -> "+selectedReactor);
	}
	@Override
	public void flagStabilizerPathCalc() {
		flagStabPathCalc = true;
	}

	public StateInterface getState(){
		return getSegmentController().getState();
	}
	@Override
	public void boot(ReactorTree reactorTree) {
		
		if(isOnServer()){
			doBoot(reactorTree);
		}else{
			getNetworkObject().getBootRequestBuffer().add(reactorTree.getId());
		}
	}
	@Override
	public void onLastReactorRemoved() {
//		System.err.println("[REACTOR] "+getState()+"; "+getSegmentController()+" on last reactor removed!");
		flagStabilizerPathCalc();
	}
	@Override
	public boolean isActiveReactor(ReactorTree reactorTree) {
		if(isDocked()){
			return false;
		}
		return selectedReactor == reactorTree.getId();
	}

	@Override
	public float getReactorSwitchCooldown() {
		return reactorSwitchCooldown;
	}
	@Override
	public float getReactorRebootCooldown() {
		return reactorRebootCooldown;
	}
	@Override
	public double getActiveReactorIntegrity(){
		if(getActiveReactor() != null){
			return getActiveReactor().getIntegrity();
		}else{
			return 0;
		}
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] v = tag.getStruct();
		
		byte version = v[0].getByte();
		selectedReactor = v[1].getLong();
		ReactorSet s = new ReactorSet(this);
		s.fromTagStructure(v[2]);
		reactorSet.apply(s);
		priorityQueue.fromTagStructure(v[3]);
		if(v.length > 4 && v[4].getType() == Type.BYTE){
			usingReactorsFromTag = v[4].getByte() != (byte)0;
		}
		
		if(version >= 1){
			reactorSwitchCooldown = v[5].getFloat();
			reactorRebootCooldown = v[6].getFloat();
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag versionTag = new Tag(Type.BYTE, null, TAG_VERSION);
		Tag activeReactorTag = new Tag(Type.LONG, null, selectedReactor);
		boolean usingReactors = (getSegmentController().isFullyLoaded() ? isUsingPowerReactors() : usingReactorsFromTag);
		return new Tag(Type.STRUCT, null, new Tag[]{
			versionTag,
			activeReactorTag,
			reactorSet.toTagStructure(),
			priorityQueue.toTagStructure(),
			new Tag(Type.BYTE, null, usingReactors ? (byte)1 : (byte)0),
			new Tag(Type.FLOAT, null, reactorSwitchCooldown),
			new Tag(Type.FLOAT, null, reactorRebootCooldown),
			FinishTag.INST
		});
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o) {
		{
			ObjectArrayList<RemoteReactorSet> receiveBuffer = ((PowerInterfaceNetworkObject)o).getReactorSetBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				ReactorSet rs = receiveBuffer.get(i).get();
				
				receivedReactorSets.enqueue(rs);
			}
			
		}
		{
			ShortArrayList receiveBuffer = ((PowerInterfaceNetworkObject)o).getRecalibrateRequestBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				this.requestedRecalibrate = true;
			}
		}
		{
			ObjectArrayList<RemoteReactorBonusMatrix> receiveBuffer = ((PowerInterfaceNetworkObject)o).getReactorBonusMatrixUpdateBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				receivedBonusMatrixUpdates.enqueue(receiveBuffer.get(i).get());
			}
		}
		{
			ObjectArrayList<RemoteLongIntPair> receiveBuffer = getNetworkObject().getReactorChangeBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				LongIntPair change = receiveBuffer.get(i).get();
				changedModuleSet.put(change.l, change.i);
			}
		}
		{
			LongArrayList receiveBuffer = ((PowerInterfaceNetworkObject)o).getConvertRequestBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				chamberConvertRequests.enqueue(receiveBuffer.getLong(i));
			}
		}
		{
			FloatArrayList receiveBuffer = ((PowerInterfaceNetworkObject)o).getReactorCooldownBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i+=2){
				if(i <= receiveBuffer.size()-2){
					reactorSwitchCooldown = receiveBuffer.getFloat(i);
					reactorRebootCooldown = receiveBuffer.getFloat(i+1);
				}
			}
		}
		{
			FloatArrayList receiveBuffer = ((PowerInterfaceNetworkObject)o).getEnergyStreamCooldownBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				currentEnergyStreamCooldown = receiveBuffer.getFloat(i);
			}
		}
		{
			LongArrayList receiveBuffer = ((PowerInterfaceNetworkObject)o).getBootRequestBuffer().getReceiveBuffer();
			for(int i = 0; i < receiveBuffer.size(); i++){
				treeBootRequests.enqueue(receiveBuffer.getLong(i));
			}
		}
		if(!isOnServer()){
			selectedReactorToSet = ((PowerInterfaceNetworkObject)o).getActiveReactor().getLong();
		}
		priorityQueue.receive((PowerInterfaceNetworkObject)o);
	}

	@Override
	public void updateToFullNetworkObject(NetworkObject o) {
		((PowerInterfaceNetworkObject)o).getActiveReactor().set(selectedReactor);
		getNetworkObject().getReactorSetBuffer().add(new RemoteReactorSet(reactorSet, isOnServer()));
		priorityQueue.send((PowerInterfaceNetworkObject)o);
		
		sendCooldowns();
	}
	public void sendCooldowns(){
		if(reactorSwitchCooldown > 0 || reactorRebootCooldown > 0){
			getNetworkObject().getReactorCooldownBuffer().add(reactorSwitchCooldown);
			getNetworkObject().getReactorCooldownBuffer().add(reactorRebootCooldown);
		}
		if(currentEnergyStreamCooldown > 0){
			getNetworkObject().getEnergyStreamCooldownBuffer().add(currentEnergyStreamCooldown);
		}
	}
	@Override
	public void updateToNetworkObject(NetworkObject o) {
		if(isOnServer()){
			((PowerInterfaceNetworkObject)o).getActiveReactor().set(selectedReactor);
		}
	}

	@Override
	public void initFromNetworkObject(NetworkObject o) {
		updateFromNetworkObject(o);
		//set it directly on init to have it available immediately
		selectedReactor = ((PowerInterfaceNetworkObject)o).getActiveReactor().getLong();
	}
	@Override
	public String toString(){
		return "[POWERINTERFACE: "+getSegmentController()+"]";
	}
	static int blocksKilled;
	static int blocksNotKilled;
	@Override
	public void onBlockKilledServer(Damager from, short type, long index) {
		
		assert(isOnServer());
		boolean hit = reactorSet.onBlockKilledServer(from, type, index, changedModuleSet);
		
		if(hit){
			blocksKilled++;
			setReactorRebootCooldown();
//			System.err.println("BLOCKS KILLED: "+blocksKilled);
		}else{
			blocksNotKilled++;
//			System.err.println("BLOCKS NOT KILLED: "+blocksNotKilled);
		}
	}
	
	public void setReactorRebootCooldown(){

		reactorRebootCooldown = getRebootTimeSec();
	}
	@Override
	public float getRebootTimeSec(){
		return VoidElementManager.REACTOR_REBOOT_MIN_COOLDOWN_SEC + 
		(float) Math.max(0, (Math.log10(getSegmentController().getMassWithDocks()) + VoidElementManager.REACTOR_REBOOT_LOG_OFFSET) 
					* VoidElementManager.REACTOR_REBOOT_LOG_FACTOR)
	
		* (float) (1.0 - ((double) getCurrentHp() / (double) getCurrentMaxHp())) * VoidElementManager.REACTOR_REBOOT_SEC_PER_HP_PERCENT;
	}
	@Override
	public void onShieldDamageServer(double damage) {
		
		
	}
	@Override
	public void onBlockDamageServer(Damager from, int damage, short type, long pos) {
		
	}

	@Override
	public boolean isAnyDamaged() {
		return reactorSet.isAnyDamaged();
	}

	@Override
	public void requestRecalibrate() {
		if(isOnServer()){
			requestedRecalibrate = true;
		}else{
			getNetworkObject().getRecalibrateRequestBuffer().add((short)0);
		}
	}

	@Override
	public void onAnyReactorModulesChanged() {
		if(!reactorSet.isAnyDamaged()){
			//only recalc tree when there was no damage to the ship
			requestedRecalibrate = true;
		
		}
	}

	@Override
	public int updatePrio() {
		return 1000;
	}
	public static int getReactorLevel(int reactorBlocks){
		switch(VoidElementManager.REACTOR_LEVEL_CALC_STYLE) {
		case LINEAR:
			
			return Math.max(0,reactorBlocks / (VoidElementManager.REACTOR_LEVEL_CALC_LINEAR_BLOCKS_NEEDED_PER_LEVEL));
		case LOG10:

			int log10 = Math.max(0, (int)Math.log10(reactorBlocks)); //amount of 'zeroes'. 10->1, 100->2, 1000->3
			
			int base = Math.max(10, (int) Math.pow(10, log10));
			int interLevel = reactorBlocks / base;

			int level = Math.max(0, log10) * 10 + interLevel;
			
			return Math.max(0,level-10);
		default:
			throw new RuntimeException("Unknown Reactor Calc Style: "+VoidElementManager.REACTOR_LEVEL_CALC_STYLE.name());
		}
		
	}
	public static int convertLinearLvl(int normalLvl){
		return VoidElementManager.REACTOR_LEVEL_CALC_STYLE == ReactorLevelCalcStyle.LINEAR ? (normalLvl) : (normalLvl - normalLvl/10); //need to convert logarithmic level
	}
	public static String printReactorLevel(int reactorBlocks){
		int lvl = getReactorLevel(reactorBlocks);
		return "blocks: "+reactorBlocks+"; Level: "+convertLinearLvl(lvl)+"; Min Blocks: "+getMinNeededFromReactorLevelRaw(lvl)+"; Max Blocks: "+getReactorMaxFromLevel(lvl);
	}
	public static int getMinNeededFromReactorLevelRaw(int lvl) {
		if(lvl == 0){
			return 1;
		}
		switch(VoidElementManager.REACTOR_LEVEL_CALC_STYLE) {
		case LINEAR:

			return Math.max(0, lvl * (VoidElementManager.REACTOR_LEVEL_CALC_LINEAR_BLOCKS_NEEDED_PER_LEVEL));
		case LOG10:
			int pot = lvl / 10;
			int inter = Math.max(0, lvl%10 - 1);
			int base = (int) Math.pow(10, pot);
			int count = base * 10 + (inter * (base*10));
			return count;
		default:
			throw new RuntimeException("Unknown Reactor Calc Style: "+VoidElementManager.REACTOR_LEVEL_CALC_STYLE.name());
		}


	}
	@Override
	public float getReactorToChamberSizeRelation(){
		return VoidElementManager.REACTOR_CHAMBER_BLOCKS_PER_MAIN_REACTOR_AND_LEVEL;
	}
	@Override
	public boolean isChamberValid(int reactorSize, int chamberSize){
		return isChamberValid(reactorSize, chamberSize, getReactorToChamberSizeRelation());
	}
	@Override
	public int getNeededMinForReactorLevel(int reactorSize){
		return getMinNeededFromReactorLevel(getReactorLevel(reactorSize), getReactorToChamberSizeRelation());
	}
	@Override
	public int getNeededMaxForReactorLevel(int reactorSize){
		return getMinNeededFromReactorLevel(getReactorLevel(reactorSize)+1, getReactorToChamberSizeRelation());
	}
	public int getNeededMinForReactorLevelByLevel(int reactorLevel){
		return getMinNeededFromReactorLevel(reactorLevel, getReactorToChamberSizeRelation());
	}
	public int getNeededMaxForReactorLevelByLevel(int reactorLevel){
		return getMinNeededFromReactorLevel(reactorLevel+1, getReactorToChamberSizeRelation());
	}
	
	public static int getReactorMaxFromLevel(int lvl) {
		return getMinNeededFromReactorLevelRaw(lvl+1);
	}
	public static int getMinNeededFromReactorLevel(int lvl, float mainToChamberRealation){
		int min = getMinNeededFromReactorLevelRaw(lvl);
		return (int)(mainToChamberRealation * min);
	}
	public static boolean isChamberValid(int mainReactorCount, int chamberCount, float mainToChamberRelation){
		int min = getMinNeededFromReactorLevel(getReactorLevel(mainReactorCount), mainToChamberRelation);
		return chamberCount >= min;
	}
	
	@Override
	public ShortList getAppliedConfigGroups(ShortList out) {
		if(isDocked()){
			if(getSegmentController().railController.getRoot() instanceof ManagedSegmentController<?>){
				//use effects of root ship
				ManagerContainer<?> rMan = ((ManagedSegmentController<?>)getSegmentController().railController.getRoot()).getManagerContainer();
				rMan.getPowerInterface().getAppliedConfigGroups(out);
			}
		}else{
			reactorSet.getAppliedConfigGroups(out);
		}
		return out;
	}

	@Override
	public ReactorPriorityQueue getPowerConsumerPriorityQueue() {
		return priorityQueue;
	}

	@Override
	public ConfigPool getConfigPool() {
		return container.getSegmentController().getConfigManager().getConfigPool();
	}

	@Override
	public void reactorTreeReceived(ReactorTree reactorTree) {
		receivedTrees.enqueue(reactorTree);
	}

	@Override
	public void checkRemovedChamber(List<ReactorChamberUnit> currentChambers) {
		getConduits().checkRemovedChamber(currentChambers);
	}
	@Override
	public boolean isInstable() {
		return reactorBoost > 0 || getStabilizerEfficiencyTotal() < VoidElementManager.REACTOR_EXPLOSION_STABILITY;
	}
	@Override
	public double getStabilizerEfficiencyTotal() {
		double stabilization = getStabilization();
		double mainReactor = getActiveReactorCurrentSize();
		return getStabilizerEfficiency(stabilization, mainReactor);
	}
	public static double getStabilizerEfficiency(double stabilization, double mainReactor) {
		if(stabilization > mainReactor){
			return 1;
		}else if(stabilization <= 0){
			return 0;
		}else if (mainReactor <= 0){
			return 0;
		}else{
			return stabilization / mainReactor;
		}
	}
	@Override
	public double getStabilizerEfficiencyExtra() {
		double stabilization = getStabilization();
		double mainReactor = getActiveReactorCurrentSize();
		if(stabilization > mainReactor){
			double extra = stabilization - mainReactor;
			return extra / mainReactor;
		}else {
			return 0;
		}
	}

	@Override
	public long getCurrentHp() {
		if(getSegmentController().railController.isDockedAndExecuted()){
			if(getSegmentController().railController.getRoot() instanceof ShopSpaceStation){
				return 1;
			}
			return ((ManagedSegmentController<?>)getSegmentController().railController.getRoot()).getManagerContainer().getPowerInterface().getCurrentHp();
		}
		return getCurrentHpRaw();
	}
	public long getCurrentHpRaw(){
		ReactorTree activeReactor = getActiveReactor();
		if(activeReactor != null){
			return activeReactor.getHp();
		}
		if(getSegmentController() instanceof Ship) {
			SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(Ship.core);
			if(pointUnsave != null && pointUnsave.getHitpointsByte() == 0) {
				return 0;
			}
		}
		return 1;
	}
	public long getCurrentMaxHpRaw() {
		ReactorTree activeReactor = getActiveReactor();
		if(activeReactor != null){
			return activeReactor.getMaxHp();
		}
		return 1;
	}
	@Override
	public ReactorTree getActiveReactor() {
		return reactorSet.getActiveReactor();
	}

	@Override
	public float getChamberCapacity() {
		if (getActiveReactor() != null) {
			return getActiveReactor().getChamberCapacity();
		}
		return 0f;
	}
	
	

	@Override
	public long getCurrentMaxHp() {
		if(getSegmentController().railController.isDockedAndExecuted()){
			if(getSegmentController().railController.getRoot() instanceof ShopSpaceStation){
				return 1;
			}
			return ((ManagedSegmentController<?>)getSegmentController().railController.getRoot()).getManagerContainer().getPowerInterface().getCurrentMaxHp();
		}
		return getCurrentMaxHpRaw();
	}
	

	@Override
	public ReactorElement getChamber(long reactorIdPos) {
		return reactorSet.getChamber(reactorIdPos);
	}

	@Override
	public double getPowerConsumptionAsPercent() {
		if(getSegmentController().railController.isDockedAndExecuted()){
			SegmentController root = getSegmentController().railController.getRoot();
			if(root instanceof ManagedSegmentController<?>){
				PowerInterface powerInterface = ((ManagedSegmentController<?>)root).getManagerContainer().getPowerInterface();
				return powerInterface.getPowerConsumptionAsPercent();
			}
		}
		if(getRechargeRatePowerPerSec() == 0){
			return 0;
		}
		return currentConsumptionPerSec / getRechargeRatePowerPerSec();
	}

	@Override
	public List<PowerConsumer> getPowerConsumerList() {
		return powerConsumerList;
	}

	@Override
	public boolean hasActiveReactors() {
		return isUsingPowerReactors() && !getSegmentController().railController.isDockedAndExecuted() && getActiveReactor() != null;
	}

	@Override
	public boolean hasAnyReactors() {
		return isUsingPowerReactors() && reactorSet.getTrees().size() > 0;
	}

	@Override
	public ManagerContainer<? extends SegmentController> getManagerContainer() {
		return container;
	}

	@Override
	public void switchActiveReacorToMostHp(ReactorTree currentActive) {
		if(treeBootRequests.isEmpty() && reactorSwitchCooldown == 0){
			ReactorTree switchTo = null;
			for(ReactorTree t : reactorSet.getTrees()){
				if(t != currentActive && (switchTo == null || (t.getHp()> switchTo.getHp() && t.getHpPercent() > getSegmentController().getConfigManager().apply(StatusEffectType.REACTOR_FAILSAFE_HPPERCENT_MIN_TARGET_THRESHOLD, 1.0f)))){
					//switch to highest HP reactor and make sure that one is also a high enough hpPercent (status effect)
					switchTo = t;
				}
			}
			if(switchTo != null){
				System.err.println("[SERVER][REACTOR] FAILSAVE REACTOR SWITCH TO "+switchTo.getName());
				treeBootRequests.enqueue(switchTo.getId());
			}
		}
	}
	@Override
	public boolean canUpdate() {
		return true;
	}
	@Override
	public void onNoUpdate(Timer timer) {
	}

	@Override
	public void setReactorBoost(float boost) {
		this.reactorBoost = boost;
	}
	@Override
	public float getReactorBoost(){
		return reactorBoost;
	}

	@Override
	public long getSourceId() {
		return 0;
	}

	@Override
	public void registerProjectionConfigurationSource(ConfigProviderSource source) {
		configProjectionSources.put(source.getSourceId(), source);
	}

	@Override
	public void unregisterProjectionConfigurationSource(ConfigProviderSource source) {
		configProjectionSources.remove(source.getSourceId());		
	}

	@Override
	public void addSectorConfigProjection(Collection<ConfigProviderSource> to) {
		to.addAll(configProjectionSources.values());
	}

	@Override
	public long getActiveReactorId() {
		return selectedReactor;
	}

	@Override
	public boolean isActiveReactor(long idPos) {
		return idPos == selectedReactor;
	}

	@Override
	public boolean isAnyRebooting() {
		return reactorSet.getTrees().size() > 0 && (reactorSwitchCooldown > 0 || reactorRebootCooldown > 0 || (getActiveReactor() != null && getActiveReactor().isAnyChamberBootingUp()));
	}

	@Override
	public double getStabilizerIntegrity() {
		return getStabilizer().getIntegrity();
	}
	public static boolean hasEnergyStreamDocked(SimpleTransformableSendableObject<?> s){
		return s instanceof ManagedSegmentController<?> && !((SegmentController)s).isUsingOldPower() && 
				!((ManagedSegmentController<?>)s).getManagerContainer().getPowerInterface().getStabilizerPaths().isEmpty();
	}
	public static boolean hasEnergyStream(SimpleTransformableSendableObject<?> s){
		return s instanceof ManagedSegmentController<?> && 
				((SegmentController)s).railController.getRoot() == s && hasEnergyStreamDocked(s);
	}
	public void powerChanged(PowerChangeType t){
		if(!isOnServer()){
			((GameClientState)getState()).onPowerChanged(getSegmentController(), t);
		}
	}
	private void calculateStabilizerPaths() {
		onPhysicsRemove();
		if(!energyStream) {
			stabilizerPaths.clear();
			return;
		}
		stabilizerPaths.clear();
		if(getActiveReactor() != null){
			getStabilizer().calculatePaths(getActiveReactor(), stabilizerPaths);
		}
		powerChanged(PowerChangeType.STABILIZER_PATH);
		if(getActiveReactor() != null){
			onPhysicsAdd();
		}
	}
	@Override
	public List<StabilizerPath> getStabilizerPaths() {
		
		return stabilizerPaths;
	}

	@Override
	public void onPhysicsAdd() {
		PhysicsExt physics;
		
		if(isOnServer()){
			if(((GameServerState)getState()).getUniverse().existsSector(getSegmentController().getSectorId())){
				physics = getSegmentController().getPhysics(); 
			}else{
				return;
			}
		}else if(getSegmentController().isClientSectorIdValidForSpawning(getSegmentController().getSectorId())){
			physics = getSegmentController().getPhysics();
		}else{
			return;
		}
		
		List<StabilizerPath> ps = stabilizerPaths;
		for(StabilizerPath p : ps){
			p.onPhysicsAdd(getSegmentController(), physics);
		}
	}

	@Override
	public void onPhysicsRemove() {
		
		PhysicsExt physics;
		
		if(isOnServer()){
			if(((GameServerState)getState()).getUniverse().existsSector(getSegmentController().getSectorId())){
				physics = getSegmentController().getPhysics(); 
			}else{
				return;
			}
		}else{
			physics = getSegmentController().getPhysics();
		}
		
		List<StabilizerPath> ps = stabilizerPaths;
		for(StabilizerPath p : ps){
			p.onPhysicsRemove(getSegmentController(), physics);
		}
	}

	@Override
	public void updateWithoutPhysicsObject() {
		for(StabilizerPath p : stabilizerPaths){
			if(isOnServer()){
				p.updateTransform(getSegmentController().getWorldTransform());
			}else{
				p.updateTransform(getSegmentController().getWorldTransformOnClient());
			}
		}
			
	}

	@Override
	public void checkRootIntegrity() {
	}

	@Override
	public void drawDebugEnergyStream() {
		for(StabilizerPath p : stabilizerPaths){
			p.drawDebug(getSegmentController());
		}
	}

	@Override
	public float getStabilzerPathRadius() {
		return VoidElementManager.REACTOR_STABILIZER_PATH_RADIUS_DEFAULT+ (getActiveReactorLevel() * VoidElementManager.REACTOR_STABILIZER_PATH_RADIUS_PER_LEVEL);
	}

	private int getActiveReactorLevel() {
		ReactorTree reactorTree = reactorSet.getTreeMap().get(selectedReactor);
		if(reactorTree == null){
			return 0;
		}else{
			return reactorTree.getLevel();
		}
	}

	public boolean isStabilizerPathHit() {
		return currentEnergyStreamCooldown > 0;
	}

	@Override
	public void destroyStabilizersBasedOnReactorSize(Damager damager) {
//		if(getActiveReactor() != null){
//			int level = getActiveReactor().getLevel();
//			int blocks = (int) (level * VoidElementManager.REACTOR_STABILIZATION_BEAM_DAMAGE_STABILIZER_BLOCKS_KILLED_PER_LEVEL);
//			
//			if(blocks > 0){
//				getStabilizer().killRandomBlocks(blocks, damager);
//			}
//		}
	}

	@Override
	public float getExtraDamageTakenFromStabilization() {
		double start = VoidElementManager.REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_START;
		double end = VoidElementManager.REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_END;
		double sDam = VoidElementManager.REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_START_DAMAGE;
		double eDam = VoidElementManager.REACTOR_LOW_STABILIZATION_EXTRA_DAMAGE_END_DAMAGE;
		
		if(start > end && eDam > sDam){
			
			double stabilization = getStabilizerEfficiencyTotal();
			
			if(stabilization < start){
				double dTotal = start - end;
				double stabNorm = 0;
				if(stabilization > end){
					double dStab = stabilization - end;
					stabNorm = dStab / dTotal;
				}
				
				double damDist = eDam - sDam;
				
				double extraDamage = ((1.0 - stabNorm) * damDist) + sDam;
			
				return (float)extraDamage;
			}
		}
		
		
		return 0;
	}

	@Override
	public void doEnergyStreamCooldownOnHit(Damager damager, float damage, long hitTime) {
		if(!energyStream) {
			return;
		}
		if(isOnServer()){
			
			float c = damage * VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_HIT_COOLDOWN_PER_DAMAGE_IN_SEC 
				/ Math.max(1, reactorSet.getActiveReactor().getLevel());
			System.out.println("energyStreamCooldown: level " + reactorSet.getActiveReactor().getLevel() + " damage " + damage + " => " + c);
			c = Math.min(
					Math.max(c, VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_HIT_MIN_COOLDOWN_IN_SEC), 
					VoidElementManager.REACTOR_STABILIZATION_ENERGY_STREAM_HIT_MAX_COOLDOWN_IN_SEC);
			
			this.currentEnergyStreamCooldown = Math.max(c, this.currentEnergyStreamCooldown);
			
//			System.err.println("[REACTOR] "+getSegmentController()+", "+getState()+" ENERGY STREAM HIT COOLDOWN: "+this.currentEnergyStreamCooldown);
		}
	}

	@Override
	public float getCurrentEnergyStreamDamageCooldown() {
		return currentEnergyStreamCooldown;
	}

	@Override
	public void injectPower(Damager from, double power) {
//		System.err.println("INJECT POWER: "+getSegmentController().getState()+"; "+getSegmentController());
		injectedPowerPerSec = power;
		injectedPowerTimoutSec = 5f; 
	}

	@Override
	public void setEnergyStreamEnabled(boolean b) {
		if(energyStream != b) {
			flagStabPathCalc = true;
		}
		this.energyStream = b;
	}

	@Override
	public void deleteObserver(ReactorTreeListener guiReactorTree) {
		listener.remove(guiReactorTree);
	}

	@Override
	public void addObserver(ReactorTreeListener guiReactorTree) {
		listener.add(guiReactorTree);
	}

}
