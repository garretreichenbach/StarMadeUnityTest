package org.schema.game.common.controller.elements.power.reactor.tree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Matrix3f;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.power.reactor.MainReactorUnit;
import org.schema.game.common.controller.elements.power.reactor.PowerInterface;
import org.schema.game.common.controller.elements.power.reactor.chamber.ReactorChamberUnit;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class ReactorSet implements SerializationInterface{
	private static byte TAG_VERSION = 0; 
	private final PowerInterface pw;
	private final List<ReactorTree> trees = new ObjectArrayList<ReactorTree>();
	private boolean damaged; 
	private final ReactorSectorProjection reactorSectorProjection = new ReactorSectorProjection();
	private final Long2ObjectOpenHashMap<ReactorTree> treeMap = new Long2ObjectOpenHashMap<ReactorTree>();
	private final Long2ObjectOpenHashMap<Matrix3f> bonusCache = new Long2ObjectOpenHashMap<Matrix3f>();
	private boolean updateBooted = true;
	public boolean dischargeAll;
	private float accumulatedBootUp;
	
	private final List<ReactorTreeChangeListener> reactorListeners = new ObjectArrayList<ReactorTreeChangeListener>();
	private boolean receivedTree;
	
	public ReactorSet(PowerInterface pw){
		this.pw = pw;
	}
	public void build(){
		
		ReactorTree activeReactor = getActiveReactor();
		double bootTime;
		if(activeReactor != null && activeReactor.isAnyChamberBootingUp()){
			this.accumulatedBootUp = activeReactor.getAccumulatedBootUp();
		}
		int sizeBef = trees.size();
		for(ReactorTree t : trees){
			if(t.hasModifiedBonusMatrix()){
				bonusCache.put(t.getId(), t.getBonusMatrix());
			}
		}
		trees.clear();
		treeMap.clear();
		for(MainReactorUnit r : pw.getMainReactors()){
			ReactorTree t = new ReactorTree(pw);
			t.build(r);
			float f = t.getSpecificCountRec();
			if(f > 0 && this.accumulatedBootUp > 0.9){
				float bootUpPerSpecific = this.accumulatedBootUp / f;
				t.distributeBootUp(bootUpPerSpecific);
			}
			if(bonusCache.containsKey(t.getId())){
				t.getBonusMatrix().set(bonusCache.remove(t.getId()));
			}
			trees.add(t);
			treeMap.put(t.getId(), t);
			
			
		}
		if(sizeBef > 0 && trees.size() == 0){
			//remove all paths
			pw.onLastReactorRemoved();
		}
		
	}
	
	public boolean onBlockKilledServer(Damager from, short type, long index, Long2IntMap changedModuleSet) {
		
		for(ReactorTree t : trees){
			boolean hit = t.onBlockKilledServer(from, type, index, changedModuleSet);
			if(hit){
				
				damaged = true;
				
				if(t.isActiveTree() && pw.getSegmentController().getConfigManager().apply(StatusEffectType.REACTOR_FAILSAFE, false)){
					float threshold = pw.getSegmentController().getConfigManager().apply(StatusEffectType.REACTOR_FAILSAFE_THRESHOLD, 1.0f);
					if(t.getHpPercent() < threshold){
						pw.switchActiveReacorToMostHp(t);
					}
				}
				return true;
			}
		}
		return false;
		
	}
	public void print(){
//		if(trees.size() > 0){
		System.err.println("Printing all Reactor Trees: "+trees.size());
		System.err.println("---------------------------------");
		for(ReactorTree t : trees){
			t.print();
			System.err.println("---------------------------------");
		}
	}
	public List<ReactorTree> getTrees() {
		return trees;
	}
	public boolean isInAnyTree(ReactorChamberUnit e) {
		for(ReactorTree t : trees){
			if(t.isUnitPartOfTree(e)){
				return true;
			}
		}
		return false;
	}
	public int size() {
		return trees.size();
	}
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeBoolean(dischargeAll);
		
		b.writeShort(trees.size());
		
		for(int i = 0; i < trees.size(); i++){
			trees.get(i).serialize(b, isOnServer);
		}
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		trees.clear();
		treeMap.clear();
		this.dischargeAll = b.readBoolean();
		int cSize = b.readShort();
		
		for(int i = 0; i < cSize; i++){
			ReactorTree e = new ReactorTree(pw);
			e.deserialize(b, updateSenderStateId, isOnServer);
			trees.add(e);
			treeMap.put(e.getId(), e);
		}
		receivedTree = true;
	}
	public void fromTagStructure(Tag iTag) {
		Tag[] t = iTag.getStruct();
		byte version = t[0].getByte();
		Tag[] cTags = t[1].getStruct();
		for(int i = 0; i < cTags.length-1; i++){
			ReactorTree c = new ReactorTree(pw);
			c.fromTagStructure(cTags[i]);
			trees.add(c);
			treeMap.put(c.getId(), c);
		}
		damaged = calcAnyMainDamaged();
	}
	public Tag toTagStructure(){
		Tag vTag = new Tag(Type.BYTE, null, TAG_VERSION);
		
		if(pw.isOnServer()){
			Tag[] childTags = new Tag[trees.size()+1];
			childTags[childTags.length-1] = FinishTag.INST;
			for(int i = 0; i < childTags.length-1; i++){
				childTags[i] = trees.get(i).toTagStructure();
			}
			return new Tag(Type.STRUCT, null, new Tag[]{
				vTag,
				new Tag(Type.STRUCT, null, childTags),
				FinishTag.INST
			});
		}else{
			//local save (can't save tree, since its only available on server)
			return new Tag(Type.STRUCT, null, new Tag[]{
					vTag,
					new Tag(Type.STRUCT, null, new Tag[]{FinishTag.INST}),
					FinishTag.INST
				});
		}
	}
	public void apply(ReactorSet reactorSet) {
		trees.clear();
		treeMap.clear();
		trees.addAll(reactorSet.trees);
		for(ReactorTree e : trees){
			treeMap.put(e.getId(), e);
		}
		damaged = calcAnyMainDamaged();
		receivedTree = true;
	}
	public boolean calcAnyMainDamaged() {
		for(ReactorTree t : trees){
			if(t.isDamagedAny()){
				return true;
			}
		}
		return false;
	}
	public boolean isAnyMainDamaged() {
		for(ReactorTree t : trees){
			if(t.isDamagedMain()){
				return true;
			}
		}
		return false;
	}
	public boolean isAnyDamaged() {
		return damaged;
	}
	public boolean applyReceivedSizeChange(long moduleId, int actualSize) {
		for(ReactorTree t : trees){
			boolean changed = t.applyReceivedSizeChange(moduleId, actualSize);
			if(changed){
				damaged = calcAnyMainDamaged();
				
				if(receivedTree) {
					for(ReactorTreeChangeListener r : reactorListeners) {
						r.onReactorSizeChanged(t, damaged);
					}
				}
				return damaged;
			}
			
		}
		return false;
	}
	public void getAppliedConfigGroups(ShortList out) {
		for(ReactorTree t : trees){
			if(t.isActiveTree()){
				t.getAppliedConfigGroups(out);
			}
		}
	}
	public void getAppliedConfigSectorGroups(ShortList out) {
		for(ReactorTree t : trees){
			if(t.isActiveTree()){
				t.getAppliedConfigSectorGroups(out);
			}
		}
	}
	public ConfigProviderSource getSectorConfigProjection() {
			return reactorSectorProjection;
	}
	public Long2ObjectOpenHashMap<ReactorTree> getTreeMap() {
		return treeMap;
	}
	private class ReactorSectorProjection implements ConfigProviderSource{

		@Override
		public ShortList getAppliedConfigGroups(ShortList out) {
			getAppliedConfigSectorGroups(out);
			return out;
		}

		@Override
		public long getSourceId() {
			return -1;
		}
		
	}
	public ReactorElement getChamber(long reactorIdPos) {
		for(ReactorTree t : trees){
			ReactorElement e = t.getChamber(reactorIdPos);
			if(e != null){
				return e;
			}
		}
		return null;
	}
	public void update(Timer timer, long selectedReactor) {
		if(receivedTree) {
			for(ReactorTreeChangeListener r : reactorListeners) {
				r.onReceivedTree();
			}
			receivedTree = false;
		}
		for(ReactorTree t : trees){
			if(t.getId() == selectedReactor){
				updateBooted = t.updateBooted(timer);
				
				//use to test
//				if(updateBooted){
//					System.err.println("RESET BOOT");
//					t.resetBootedRecursive();
//				}
				
			}
		}
	}
	public ReactorTree getActiveReactor() {
		return treeMap.get(pw.getActiveReactorId());
	}
	public void getAllReactorElementsWithConfig(ConfigPool configPool, StatusEffectType t, Collection<ConfigGroup> out) {
		ReactorTree activeReactor = getActiveReactor();
		if(activeReactor != null){
			activeReactor.getAllReactorElementsWithConfig(configPool, t, out);
		}
	}
	public void removeReactorTreeListener(ReactorTreeChangeListener c) {
		reactorListeners.remove(c);
	}
	public void addReactorTreeListener(ReactorTreeChangeListener c) {
		reactorListeners.add(c);
	}
}
