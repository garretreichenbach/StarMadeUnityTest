package org.schema.game.common.controller.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector3f;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.ShieldDrawer;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.beam.ShieldConditionInterface;
import org.schema.game.common.controller.elements.shield.capacity.ShieldCapacityUnit;
import org.schema.game.common.controller.elements.shield.regen.ShieldRegenUnit;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.world.SectorNotFoundException;
import org.schema.game.network.objects.remote.RemoteValueUpdate;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.game.network.objects.valueUpdate.ShieldLocalFullValueUpdate;
import org.schema.game.network.objects.valueUpdate.ShieldLocalSingleValueUpdate;
import org.schema.game.network.objects.valueUpdate.ValueUpdate.ValTypes;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ShieldLocalAddOn implements ManagerUpdatableInterface, TagSerializable {
	private boolean flagLocalShieldRecalc;
	private final ShieldContainerInterface sc;
	private final SegmentController controller;
	private final List<ShieldLocal> activeShieldList = new ObjectArrayList<ShieldLocal>();
	private final List<ShieldLocal> inactiveShieldList = new ObjectArrayList<ShieldLocal>();
	private final Long2ObjectOpenHashMap<ShieldLocal> localShieldMap = new Long2ObjectOpenHashMap<ShieldLocal>();
	private ShieldHitCallback hit = new ShieldHitCallback();
	private ObjectArrayFIFOQueue<ShieldLocal> shieldsToAdd = new ObjectArrayFIFOQueue<ShieldLocal>();
	private long lastHitShieldId;
	private List<ShieldConditionInterface> shieldConditions = new ObjectArrayList<ShieldConditionInterface>();
	private boolean shieldWorking;
	private static final byte VERSION = 0;
	public ShieldLocalAddOn(ShieldContainerInterface sc, SegmentController controller) {
		sc.addUpdatable(this);
		this.sc = sc;
		this.controller = controller;
	}

	public void flagCalcLocalShields() {
		//FIXME !!!! method gets called when element collections not filled yet!
		// the "isFullyLoaded" call doesn't cover this case because isFullyLoaded only covers segment data, not metadata like systems status
		// Anyway, it shouldn't be this class's job to determine whether or not shields should be calculated - that's what the external flagging is for!
		flagLocalShieldRecalc = true;
	}
	public StateInterface getState(){
		return controller.getState();
	}
	@Override
	public void update(Timer timer) {
//		if(controller.getName().contains("schema_1505231085958")){
//			System.err.println("UPDATE:: "+getState()+": "+activeShieldList);
//		}
		this.shieldWorking = true;
		for(int i = 0; i < shieldConditions.size(); i++) {
			ShieldConditionInterface sh = shieldConditions.get(i);
			if(!sh.isShieldUsable()) {
				this.shieldWorking = false;
				controller.popupOwnClientMessage("shieldNotWork", Lng.str("WARNING: Shield matrix is currently disrupted!\nUnable to stop any damage."), ServerMessage.MESSAGE_TYPE_ERROR);
			}
			if(!sh.isActive()) {
				
				
				shieldConditions.remove(i);
				i--;
				
				if(!shieldWorking && shieldConditions.isEmpty()) {
					controller.popupOwnClientMessage("shieldWorkAgain", Lng.str("Shield matrix functioning normally again."), ServerMessage.MESSAGE_TYPE_INFO);
				}
			}
		}
		while(!shieldsToAdd.isEmpty()){
			ShieldLocal l = shieldsToAdd.dequeue();
			ShieldLocal contains = localShieldMap.get(l.mainId);
			if(contains != null){
				if(contains.active){
					activeShieldList.remove(contains);
				}else{
					inactiveShieldList.remove(contains);
				}
				localShieldMap.remove(contains.mainId);
				onRemovedShield(contains);
			}
			localShieldMap.put(l.mainId, l);
			if(l.active){
				activeShieldList.add(l);
			}else{
				inactiveShieldList.add(l);
			}
			onAddedShield(l);
		}
		if(controller.isFullyLoaded()){
			//only recalculate on fully loaded. before that, the staticly loaded shields are used
			if(flagLocalShieldRecalc){
				recalculateLocalShields();
				flagLocalShieldRecalc = false;
			}
		}
	}


	public void processShieldHit(ShieldHitCallback hit) throws SectorNotFoundException{
		hit.nowHitEntity = controller;
		if(controller.railController.isDockedAndExecuted()){
			//delegate hit to the root
			SegmentController c = controller.railController.getRoot();
			if(c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)c).getManagerContainer() instanceof ShieldContainerInterface){
				ShieldContainerInterface s = ((ShieldContainerInterface)((ManagedSegmentController<?>)c).getManagerContainer());
				hit.convertWorldHitPoint(c);
				s.getShieldAddOn().getShieldLocalAddOn().processShieldHit(hit);
			}
			return;
		}
		
		final int size = this.shieldWorking ? activeShieldList.size() : 0;
		for(int i = 0; i < size; i++){
			ShieldLocal shieldLocal = activeShieldList.get(i);
			boolean hadHit = hit.hasHit;
			shieldLocal.process(hit);
			if(hit.hasHit){
				
				if(!hadHit){
					lastHitShieldId = shieldLocal.mainId;
					if(shieldLocal.getIntegrity() < VoidElementManager.INTEGRITY_MARGIN){
						sc.getShieldRegenManager().shieldHit(hit);
						sc.getShieldCapacityManager().shieldHit(hit);
					}
					
				}
				if(!controller.isOnServer()){
					
					GameClientState s = (GameClientState)getState();
					ShieldDrawer shieldDrawer = s.getWorldDrawer().getShieldDrawerManager().get(controller);
					if (shieldDrawer != null) {
						shieldDrawer.addHit(hit.xWorld, hit.yWorld, hit.zWorld, (float)hit.getDamage(), shieldLocal.getPercentOne());
					}
				}else {
					sc.getSegmentController().onShieldDamageServer(hit);
				}
				if(!VoidElementManager.SHIELD_LOCAL_HIT_ALL_OVERLAPPING){
					break;
				}
			}
		}
	}
	private void recalculateLocalShields() {
		Long2ObjectMap<ShieldLocal> localMap = getLocalShieldMap();
		//reset all existing to active
		for(ShieldLocal l : localShieldMap.values()){
			l.active = true;
		}
		Set<ShieldLocal> set = new ObjectOpenHashSet<ShieldLocal>();
		set.addAll(localShieldMap.values());
		List<ShieldRegenUnit> elementCollections = sc.getShieldRegenManager().getElementCollections();
		for(ShieldRegenUnit u : elementCollections){
			ShieldLocal local = new ShieldLocal(this);
			local.createFrom(u);
			boolean removed = set.remove(local); //already tracked shield
			if(!removed){
				localShieldMap.put(local.mainId, local);
				onAddedShield(local);
			}else{
				localShieldMap.get(local.mainId).createFrom(u);
			}
		}
		
		
		//anything remaining in set are shields that no longer exist
		for(ShieldLocal l : set){
			onRemovedShield(l);
		}
		localShieldMap.values().removeAll(set);
		
		inactiveShieldList.clear();
		activeShieldList.clear();
		activeShieldList.addAll(localShieldMap.values());
		Collections.sort(activeShieldList);
		
		List<ShieldCapacityUnit> capCollections = new ObjectArrayList<ShieldCapacityUnit>(sc.getShieldCapacityManager().getElementCollections());
		Collections.sort(capCollections); //sort to stay deterministic
		for(int s = 0; s < activeShieldList.size(); s++){
			ShieldLocal shieldLocal = activeShieldList.get(s);
			for(int v = s+1; v < activeShieldList.size(); v++){
				ShieldLocal other = activeShieldList.get(v);
				if(shieldLocal.containsInRadius(other)){
					other.active = false;
					activeShieldList.remove(v);
					inactiveShieldList.add(other);
					v--;
				}
			}
			shieldLocal.resetCapacity();
			for(int i = 0; i < capCollections.size(); i++){
				ShieldCapacityUnit cap = capCollections.get(i);
				if(shieldLocal.addCapacityUnitIfContains(cap)){
					capCollections.remove(i);
					i--;
					if(shieldLocal.supportIds.size() >= VoidElementManager.SHIELD_LOCAL_MAX_CAPACITY_GROUPS_PER_LOCAL_SHIELD){
						break;
					}
				}
				
			}
		}
		
//		System.err.println(controller.getState()+" LOCAL SHIELD::: "+controller+": "+getLocalShieldMap().size()+"; INACTIVE: "+inactiveShieldList.size()+"; ACTIVE: "+activeShieldList.size());
	}

	public ManagerContainer<? extends SegmentController> getManagerContainer(){
		return ((ManagedSegmentController<?>)controller).getManagerContainer();
	}
	private void onRemovedShield(ShieldLocal l) {
		getManagerContainer().getPowerInterface().removeConsumer(l);
//		try {
//			throw new Exception("REMOVED SHIELD: "+controller+"; "+controller.getState()+": "+l);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	public ShieldLocal getContainingShield(final ShieldContainerInterface sc, final long absoluteIndex){
		for(ShieldLocal l : localShieldMap.values()){
			if(l.containsBlock(sc, absoluteIndex)){
				return l;
			}
		}
		return null;
	}
	public ShieldLocal getContainingShieldByIdPos(final ShieldContainerInterface sc, final long absoluteIndex){
		for(ShieldLocal l : localShieldMap.values()){
			if(l.mainId == absoluteIndex || l.supportCoMIds.contains(absoluteIndex)){
				return l;
			}
		}
		return null;
	}
	public ShieldLocal getShieldInRadius(ShieldContainerInterface m, Vector3f whereLocal) {
		for(ShieldLocal l : localShieldMap.values()){
			if(l.containsInRadius(whereLocal.x, whereLocal.x, whereLocal.x)){
				return l;
			}
		}
		return null;
	}
	private void onAddedShield(ShieldLocal l) {
		getManagerContainer().getPowerInterface().addConsumer(l);	
//		try {
//			throw new Exception("ADDED SHIELD: "+controller+"; "+controller.getState()+": "+l);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public int updatePrio() {
		return 0;
	}

	public void sendShieldUpdate(ShieldLocal single) {
		if(single == null){
			//send full update
			assert (controller.isOnServer());
			ShieldLocalFullValueUpdate shieldValueUpdate = new ShieldLocalFullValueUpdate();
			assert (shieldValueUpdate.getType() == ValTypes.SHIELD_LOCAL_FULL);
			shieldValueUpdate.setServer(
					((ManagedSegmentController<?>) controller).getManagerContainer(), this);
			((NTValueUpdateInterface) controller.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(shieldValueUpdate, controller.isOnServer()));
		}else{
			assert (controller.isOnServer());
			ShieldLocalSingleValueUpdate shieldValueUpdate = new ShieldLocalSingleValueUpdate();
			assert (shieldValueUpdate.getType() == ValTypes.SHIELD_LOCAL);
			shieldValueUpdate.setServer(((ManagedSegmentController<?>) controller).getManagerContainer(), single.mainId, single.getShields());
			((NTValueUpdateInterface) controller.getNetworkObject()).getValueUpdateBuffer().add(new RemoteValueUpdate(shieldValueUpdate, controller.isOnServer()));
		}
	}

	public SegmentController getSegmentController() {
		return controller;
	}

	public void sendRegenEnabledUpdate() {
		
	}

	public double handleShieldHit(Damager damager, Vector3f hitPoint, int projectileSectorId, DamageDealerType damageType, double damage, long weaponId) throws SectorNotFoundException {
		hit.reset();
		hit.originalHitEntity = controller;
		hit.damager = damager;
		hit.xWorld = hitPoint.x;
		hit.yWorld = hitPoint.y;
		hit.zWorld = hitPoint.z;
		hit.projectileSectorId = projectileSectorId;
		hit.damageType = damageType;
		hit.setDamage(damage);
		hit.weaponId = weaponId;
		
		hit.convertWorldHitPoint(controller);
		processShieldHit(hit);
		return hit.getDamage();
	}

	
	public List<ShieldLocal> getActiveShields() {
		return activeShieldList;
	}
	public List<ShieldLocal> getInactiveShields() {
		return inactiveShieldList;
	}

	public boolean isAtLeastOneActive() {
		return activeShieldList.size() > 0;
	}

	public void markDrawCollectionByBlock(long absoluteIndex) {
		for(ShieldLocal l : localShieldMap.values()){
			if(l.markDrawCollectionByBlock(sc, absoluteIndex)){
				return;
			}
		}
	}


	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] s = tag.getStruct();
		byte version = s[0].getByte();
		Tag[] lc = s[1].getStruct();
		
		for(int i = 0; i < lc.length-1; i++){
			ShieldLocal l = new ShieldLocal(this);
			l.fromTagStructure(lc[i]);
			localShieldMap.put(l.mainId, l);
			if(l.active){
				activeShieldList.add(l);
			}else{
				inactiveShieldList.add(l);
			}
			onAddedShield(l);
		}
	}

	@Override
	public Tag toTagStructure() {
		
		Tag[] lc = new Tag[localShieldMap.size()+1];
		int c = 0;
		for(ShieldLocal l : localShieldMap.values()){
			lc[c] = l.toTagStructure();
			c++;
		}
		assert(c == lc.length-1);
		lc[lc.length-1] = FinishTag.INST;
		
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				new Tag(Type.STRUCT, null, lc),
				FinishTag.INST
			}
		);
	}

//	@Override
//	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
//		b.writeShort(localShieldMap.size());
//		for(ShieldLocal l : localShieldMap.values()){
//			l.serialize(b, isOnServer);
//		}
//	}
//
//	@Override
//	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
//		final short size = b.readShort();
//		for(int i = 0; i < size; i++){
//			ShieldLocal l = new ShieldLocal();
//			l.deserialize(b, updateSenderStateId, isOnServer);
//			receivedShields.enqueue(l);
//		}
//	}

	public Collection<ShieldLocal> getAllShields() {
		return localShieldMap.values();
	}

	public void receivedShields(List<ShieldLocal> shields) {
		for(ShieldLocal l : shields){
			l.shieldLocalAddOn = this;
			shieldsToAdd.enqueue(l);		
		}
	}

	public void receivedShieldSingle(long shieldId, double val) {
		ShieldLocal shieldLocal = localShieldMap.get(shieldId);
		if(shieldLocal != null){
			shieldLocal.receivedShields(val);
		}
	}

	public ShieldLocal getLastHitShield() {
		ShieldLocal ret = null;
		for(ShieldLocal l : activeShieldList){
			if(l.mainId == lastHitShieldId){
				return l;
			}
			ret = l;
		}
		return ret;
	}
	@Override
	public boolean canUpdate() {
		return true;
	}
	@Override
	public void onNoUpdate(Timer timer) {
	}

	public int getActiveAvailableShields() {
		int i = 0;
		for(ShieldLocal l : activeShieldList){
			if(l.getShields() > 0){
				i++;
			}
		}
		return i;
	}
	public double getTotalShields() {
		double d = 0;
		for(ShieldLocal l : activeShieldList){
			d += l.getShields();
		}
		return d;
	}

	public boolean incShieldsAt(long absoluteIndex, double shields) {
		for(ShieldLocal l : activeShieldList){
			if(l.containsLocalBlockInRadius(absoluteIndex)){
				l.setShieldsAsAction(Math.min(l.getShieldCapacity(), l.getShields()+shields));
				sendShieldUpdate(l);
				return true;
			}
		}
		return false;
	}
	private static ObjectOpenHashSet<ShieldLocal> tmp = new ObjectOpenHashSet<ShieldLocal>();
	public void reduceShieldsDistributed(double shields) {
		if(activeShieldList.isEmpty()){
			System.err.println("WANING: no shields to consume on "+controller);
			return;
		}
		tmp.clear();
		double shieldsLeft = shields;
		
		while(shieldsLeft > 0.1f && getTotalShields() > 0.1){
			double onesy = shieldsLeft/getActiveAvailableShields();
			for(ShieldLocal l : activeShieldList){
				if(l.getShields() > 0){
				
					if(l.getShields() < onesy){
						shieldsLeft = Math.max(0, shieldsLeft-l.getShields());
						l.setShieldsAsAction(0);
						tmp.add(l);
					}else{
						shieldsLeft = Math.max(0, shieldsLeft-onesy);
						l.setShieldsAsAction(l.getShields() - onesy);
						tmp.add(l);
					}
				}
				
			}
		}
		
		for(ShieldLocal l : tmp){
			sendShieldUpdate(l);
		}
		tmp.clear();
	}

	public void fillForExplosion(Vector3f hitWorld, int projectileSectorId, Int2DoubleOpenHashMap shieldMap, Int2DoubleOpenHashMap shieldMapBef,
			Int2DoubleOpenHashMap shieldMapPercent, Int2LongOpenHashMap shieldLocalMap) throws SectorNotFoundException {

		if(controller.railController.isDockedAndExecuted()){
			SegmentController c = controller.railController.getRoot();
			if(c instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)c).getManagerContainer() instanceof ShieldContainerInterface){
				ShieldContainerInterface s = ((ShieldContainerInterface)((ManagedSegmentController<?>)c).getManagerContainer());
				hit.convertWorldHitPoint(c);
				s.getShieldAddOn().getShieldLocalAddOn().fillForExplosion( hitWorld, projectileSectorId, shieldMap, shieldMapBef, shieldMapPercent, shieldLocalMap);
			}
			return;
		}
		hit.reset();
	
		hit.xWorld = hitWorld.x;
		hit.yWorld = hitWorld.y;
		hit.zWorld = hitWorld.z;
		hit.projectileSectorId = projectileSectorId;
		hit.convertWorldHitPoint(controller);
		final int size = activeShieldList.size();
		for(int i = 0; i < size; i++){
			ShieldLocal shieldLocal = activeShieldList.get(i);
			
			if(shieldLocal.containsInRadius(hit)){
				shieldMap.put(controller.getId(), shieldLocal.getShields());
				shieldMapBef.put(controller.getId(), shieldLocal.getShields());
				shieldMapPercent.put(controller.getId(),
						(shieldLocal.getShields() > 0 && shieldLocal.getShieldCapacity() > 0) ?
								(shieldLocal.getShields() / shieldLocal.getShieldCapacity()) : 0);
				shieldLocalMap.put(controller.getId(), shieldLocal.mainId);
				hit.reset();
				return;
			}
		}
		
		
		
	}

	public Long2ObjectOpenHashMap<ShieldLocal> getLocalShieldMap() {
		return localShieldMap;
	}

	public void dischargeAllShields() {
		for(ShieldLocal l : activeShieldList){
			l.setShieldsAsAction(0);
		}
	}

	public void hitAllShields(double damage) {
		for(ShieldLocal l : activeShieldList){
			l.setShieldsAsAction(Math.max(0, l.getShields()-damage));
		}		
	}

	private static void addShieldRecursive(ShieldConditionInterface sh, SegmentController root) {
		if(root instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>)root).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldLocalAddOn shieldLocalAddOn = ((ShieldContainerInterface)((ManagedSegmentController<?>)root).getManagerContainer()).getShieldAddOn().getShieldLocalAddOn();
			shieldLocalAddOn.addShieldCondition(sh);
		}
		
		for(RailRelation r : root.railController.next) {
			addShieldRecursive(sh, r.docked.getSegmentController());
		}
	}
	private void addShieldCondition(ShieldConditionInterface sh) {
		shieldConditions.add(sh);
	}
	public void addShieldCondition(ShieldConditionInterface sh, boolean onCompleteStructure) {
		if(onCompleteStructure) {
			SegmentController root = controller.railController.getRoot();
			addShieldRecursive(sh, root);
		}else {
			addShieldCondition(sh);
		}
	}
	

}
