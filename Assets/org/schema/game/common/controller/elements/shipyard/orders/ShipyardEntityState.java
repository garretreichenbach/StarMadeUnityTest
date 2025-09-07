package org.schema.game.common.controller.elements.shipyard.orders;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardCommandType;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryChangeMap;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class ShipyardEntityState extends AiEntityState{

	private static final byte VERSION = 3;



	private final ShipyardCollectionManager shipyardCollectionManager;
	
	
	
	public ElementCountMap currentMapFrom = new ElementCountMap();
	public ElementCountMap currentMapTo = new ElementCountMap();

	public String currentName;
	public BlueprintClassification currentClassification;



	public int designToLoad = -1;



	public String currentBlueprintName;



	public boolean spawnDesignWithoutBlocks;



	public int playerStateSent;



	private Tag invs;



	public boolean isInRepair;



	public boolean wasUndockedManually;



	public int lastDeconstructedBlockCount;



	public int designToBlueprintOwner;



	public int lastOrderFactionId;



	public VirtualBlueprintMetaItem lastConstructedDesign;




	
	public ShipyardEntityState(String name, ShipyardCollectionManager shipYardCollectionManager, StateInterface state) {
		super(name, state);
		
		this.shipyardCollectionManager = shipYardCollectionManager;
		
	}

	//@Override
	//public void afterUpdate(Timer timer)  {
	//	getShipyardCollectionManager().getCurrentCommand().getCallback().onFinished(this, getShipyardCollectionManager());
	//}
	
	public SegmentController getSegmentController() {
		return shipyardCollectionManager.getSegmentController();
	}
	public ShipyardCollectionManager getShipyardCollectionManager() {
		return shipyardCollectionManager;
	}
	public SegmentController getCurrentDocked(){
		return shipyardCollectionManager.getCurrentDocked();
	}
	public Inventory getInventory(){
		return shipyardCollectionManager.getInventory();
	}

	public VirtualBlueprintMetaItem getCurrentDesign() {
		return shipyardCollectionManager.getCurrentDesignObject();
	}



	public boolean isLoadedDesignValid() {
		return shipyardCollectionManager.isLoadedDesignValid();
	}



	public void sendShipyardStateToClient() {
		shipyardCollectionManager.sendShipyardStateToClient();
	}



	public void sendShipyardErrorToClient(String error) {
		shipyardCollectionManager.sendShipyardErrorToClient(error);
	}



	public void sendShipyardCommandToServer(int factionId, ShipyardCommandType t,
			Object... args) {
		shipyardCollectionManager.sendShipyardCommandToServer(factionId, t, args);
	}



	public void handle(ShipyardCommandType t, Object[] args) {
		this.currentName = null; 
		this.currentBlueprintName = null; 
		this.isInRepair = false;
		this.spawnDesignWithoutBlocks = false;
		this.playerStateSent = -1;
		
		LogUtil.sy().fine(getSegmentController()+": Command on server "+t.name()+"; args: "+Arrays.toString(args));
		switch(t) {
			case CREATE_NEW_DESIGN -> this.currentName = (String) args[0];
			case DECONSTRUCT -> this.currentName = (String) args[0];
			case SPAWN_DESIGN -> this.currentName = (String) args[0];
			case DESIGN_TO_BLUEPRINT -> {
				this.currentName = (String) args[0];
				this.designToBlueprintOwner = (Integer) args[1];
				this.currentClassification = BlueprintClassification.values()[(Integer) args[2]];
			}
			case BLUEPRINT_TO_DESIGN -> {
				this.currentName = (String) args[0];
				this.currentBlueprintName = (String) args[1];
			}
			case CATALOG_TO_DESIGN -> {
				this.currentName = (String) args[0];
				this.currentBlueprintName = (String) args[1];
			}
			case REPAIR_FROM_DESIGN -> {
				this.isInRepair = true;
				this.designToLoad = (Integer) args[0];
				if(getCurrentDocked() != null) {
					this.currentName = getCurrentDocked().getRealName();
				}
			}
			case TEST_DESIGN -> {
				this.playerStateSent = (Integer) args[0];
				Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(this.playerStateSent);
				if(sendable != null && sendable instanceof PlayerState) {
					currentName = ((PlayerState) sendable).getName() + "_" + System.currentTimeMillis();
				} else {
					currentName = "SERVER_ERROR_NO_PLAYER_" + System.currentTimeMillis();
				}
				this.spawnDesignWithoutBlocks = true;
			}
			case LOAD_DESIGN -> this.designToLoad = (Integer) args[0];
			default -> {
			}
		}
	}



	public boolean setCompletionOrderPercentAndSendIfChanged(double f) {
		return shipyardCollectionManager.setCompletionOrderPercentAndSendIfChanged(f);
	}



	public void unloadCurrentDockedVolatile() {
		LogUtil.sy().fine(getSegmentController()+" unload docked volatile");
		shipyardCollectionManager.unloadCurrentDockedVolatile();
	}



	public SegmentController loadDesign(VirtualBlueprintMetaItem o) {
		LogUtil.sy().fine(getSegmentController()+" load design from meta item: "+o);
		return shipyardCollectionManager.loadDesign(o);
	}



	public void fromTagStructure(Tag tag) {
		Tag[] v = (Tag[])tag.getValue();
		byte version = (Byte)v[0].getValue();
		currentMapTo.readByteArray((byte[]) v[1].getValue());
		currentName = v[2].getType() == Type.BYTE ? null : (String)v[2].getValue();
		designToLoad = (Integer)v[3].getValue();
		
		if(version > 0){
			isInRepair = (Byte)v[4].getValue() > 0;
			lastDeconstructedBlockCount = (Integer)v[5].getValue();
		}
		if(version > 1){
			this.lastOrderFactionId = (Integer)v[6].getValue();
		}
		if(version > 2){
			this.currentClassification = BlueprintClassification.values()[(Byte)v[7].getValue()];
		}
		LogUtil.sy().fine(getSegmentController()+" from tag: current name: currentName: "+currentName+"; designToLoad: "+designToLoad);
	}
	public Tag toTagStructure() {
		Tag version = new Tag(Type.BYTE, null, VERSION);
		Tag elementCountMapTag = new Tag(Type.BYTE_ARRAY, null, currentMapTo.getByteArray());
		Tag nameTag = currentName != null ? new Tag(Type.STRING, null, currentName) : new Tag(Type.BYTE, null, (byte)0);
		Tag designToLoadTag = new Tag(Type.INT, null, designToLoad);
		Tag isInRepairTag = new Tag(Type.BYTE, null, this.isInRepair ? (byte) 1 : (byte) 0);
		Tag lastDeconstructedBlockCountTag = new Tag(Type.INT, null, lastDeconstructedBlockCount);
		Tag lastOrderFactionId = new Tag(Type.INT, null, this.lastOrderFactionId);
		Tag classTag = new Tag(Type.BYTE, null, currentClassification != null ? (byte)this.currentClassification.ordinal() : (byte)0);
		
		return new Tag(Type.STRUCT, null, new Tag[]{
				version, 
				elementCountMapTag,
				nameTag,
				designToLoadTag,
				isInRepairTag,
				lastDeconstructedBlockCountTag,
				lastOrderFactionId,
				classTag,
				FinishTag.INST});
	}


	private void putIn(short type, int count, int meta, Inventory fromInventory, int slot, FastCopyLongOpenHashSet connectedStashes, InventoryChangeMap cMap){
		

		
		short origType = type;
		
		if(type != InventorySlot.MULTI_SLOT && count < 1){
			return;
		}
		if(type == InventorySlot.MULTI_SLOT){
			InventorySlot iSlot = fromInventory.getSlot(slot);
			System.err.println("[SHIPYARD] DECONSTRUCTING MULTISLOT: "+iSlot);
			for(InventorySlot s : iSlot.getSubSlots()){
				System.err.println("[SHIPYARD] DECONSTRUCTING MULTISLOT "+s+";");
				putIn(s.getType(), s.count(), s.metaId, fromInventory, slot, connectedStashes, cMap);
			}
		}else{
			
			if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).getSourceReference() != 0){
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}
			
			Inventory inventory = getInventory();
			boolean foundStashToPutIn = false;
			if(getInventory().canPutIn(type, count)){
				cMap.getInv(inventory).add(inventory.incExistingOrNextFreeSlotWithoutException(type, count, meta));
				foundStashToPutIn = true;
			}else if(connectedStashes != null){
				
				for(long s : connectedStashes){
					inventory = shipyardCollectionManager.getContainer().getInventory(ElementCollection.getPosIndexFrom4(s));
					if(inventory.canPutIn(type, count)){
						cMap.getInv(inventory).add(inventory.incExistingOrNextFreeSlotWithoutException(type, count, meta));
						foundStashToPutIn = true;
						break;
					}
				}
			}
			
			if(!foundStashToPutIn){
				Sector s = ((GameServerState)getState()).getUniverse().getSector(getSegmentController().getSectorId());
				if(s != null){
					Vector3i d = Element.DIRECTIONSi[Math.min(Math.max(0,shipyardCollectionManager.getControllerElement().getOrientation()), 5)];
					Vector3f sPos = shipyardCollectionManager.getControllerElement().getAbsolutePos(new Vector3f());
					sPos.x -= SegmentData.SEG_HALF;
					sPos.y -= SegmentData.SEG_HALF;
					sPos.z -= SegmentData.SEG_HALF;
					sPos.x += d.x;
					sPos.y += d.y;
					sPos.z += d.z;
					
					sPos.x += Math.random() - 0.5;
					sPos.y += Math.random() - 0.5;
					sPos.z += Math.random() - 0.5;
					
					getSegmentController().getWorldTransform().transform(sPos);
					
					System.err.println("[INVENTORY][SPAWNING] TO spawning inventory at " + ElementKeyMap.toString(type) +" count: "+ count + " -> " + sPos);
					s.getRemoteSector().addItem(sPos, type, -1, count);
				}
			}
		}
	}
	private void putInInventoryAndConnected(Inventory m) {
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> pp = getSegmentController().getControlElementMap().getControllingMap().get(shipyardCollectionManager.getControllerElement().getAbsoluteIndex());
		FastCopyLongOpenHashSet connectedStashes = null;
		if(pp != null){
			connectedStashes = pp.get(ElementKeyMap.STASH_ELEMENT);
		}
		
		InventoryChangeMap cMap = new InventoryChangeMap();
		for(int slot : m.getSlots()){
			short type = m.getType(slot);
			int count = m.getCount(slot, type);
			int meta = m.getMeta(slot);
			putIn(type, count, meta, m, slot, connectedStashes, cMap);
			
		}
		cMap.sendAll();
	}
	public void putInInventoryAndConnected(ElementCountMap m, boolean derelict) {
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> pp = getSegmentController().getControlElementMap().getControllingMap().get(shipyardCollectionManager.getControllerElement().getAbsoluteIndex());
		FastCopyLongOpenHashSet connectedStashes = null;
		if(pp != null){
			connectedStashes = pp.get(ElementKeyMap.STASH_ELEMENT);
		}
		Random r = new Random(getSegmentController().getName().hashCode());
		InventoryChangeMap cMap = new InventoryChangeMap();
		for(short type : ElementKeyMap.keySet){
			short origType = type;
			int count = m.get(origType);
			if(count < 1){
				continue;
			}
			if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).getSourceReference() != 0){
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}
			if(derelict){
				type = r.nextBoolean() ? ElementKeyMap.SCRAP_ALLOYS : ElementKeyMap.SCRAP_COMPOSITE;
			}
			Inventory inventory = getInventory();
			boolean foundStashToPutIn = false;
			if(getInventory().canPutIn(type, count)){
				cMap.getInv(inventory).add(inventory.incExistingOrNextFreeSlotWithoutException(type, count));
				foundStashToPutIn = true;
			}else if(connectedStashes != null){
				
				for(long s : connectedStashes){
					inventory = shipyardCollectionManager.getContainer().getInventory(ElementCollection.getPosIndexFrom4(s));
					if(inventory.canPutIn(type, count)){
						cMap.getInv(inventory).add(inventory.incExistingOrNextFreeSlotWithoutException(type, count));
						foundStashToPutIn = true;
						break;
					}
				}
			}
			
			if(!foundStashToPutIn){
				Sector s = ((GameServerState)getState()).getUniverse().getSector(getSegmentController().getSectorId());
				if(s != null){
					Vector3i d = Element.DIRECTIONSi[Math.min(Math.max(0,shipyardCollectionManager.getControllerElement().getOrientation()), 5)];
					Vector3f sPos = shipyardCollectionManager.getControllerElement().getAbsolutePos(new Vector3f());
					sPos.x -= SegmentData.SEG_HALF;
					sPos.y -= SegmentData.SEG_HALF;
					sPos.z -= SegmentData.SEG_HALF;
					sPos.x += d.x;
					sPos.y += d.y;
					sPos.z += d.z;
					
					sPos.x += Math.random() - 0.5;
					sPos.y += Math.random() - 0.5;
					sPos.z += Math.random() - 0.5;
					
					getSegmentController().getWorldTransform().transform(sPos);
					
					System.err.println("[INVENTORY][SPAWNING] TE spawning inventory at " + ElementKeyMap.toString(type) +" count: "+ count + " -> " + sPos);
					s.getRemoteSector().addItem(sPos, type, -1, count);
				}
			}
		}
		cMap.sendAll();
	}



	public void saveInventories(SegmentController currentDocked) {
		LogUtil.sy().fine(getSegmentController()+" from tag: current name: Deconstructing: saving inventories of: "+currentDocked);
		ObjectArrayList<Tag> list = new ObjectArrayList<Tag>();
		saveInventoriesRecursive(currentDocked, list);
		Tag[] t = new Tag[list.size()+1];
		for(int i = 0; i < list.size(); i++){
			t[i] = list.get(i);
		}
		t[t.length-1] = FinishTag.INST;
		this.invs = new Tag(Type.STRUCT, null, t);
		
		LogUtil.sy().fine(getSegmentController()+" from tag: current name: Deconstructing: DONE saving inventories of: "+currentDocked+": "+list.size());
	}
	private void saveInventoriesRecursive(SegmentController currentDocked, List<Tag> list) {
		if(currentDocked instanceof ManagedSegmentController<?>){
			ManagerContainer<SegmentController> c = (ManagerContainer<SegmentController>) ((ManagedSegmentController<?>)currentDocked).getManagerContainer();
			InventoryMap inventories = c.getInventories();
			for(Inventory i : inventories.values()){
				Tag t = new Tag(Type.STRUCT, null, new Tag[]{
					new Tag(Type.STRING, null, currentDocked.getUniqueIdentifier()	),
					new Tag(Type.LONG, null, i.getParameter()),
					i.toTagStructure(),
					FinishTag.INST
				});
				list.add(t);
				putInInventoryAndConnected(i);
			}
		}
		
		for(RailRelation r : currentDocked.railController.next){
			saveInventoriesRecursive(r.docked.getSegmentController(), list);
		}
	}



	



	
}
