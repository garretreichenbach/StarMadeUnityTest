package org.schema.game.client.view.gui.buildtools;

import org.schema.game.client.controller.manager.ingame.PlayerInteractionControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

public class BuildConstructionCommand {
	/**
	 * this is set when the first command is first added to queue.
	 * It will prevent advanced build more on holding control 
	 * (until player unpresses and presses control again)
	 */
	public static boolean issued;
	
	public final BuildConstructionManager manager;
	public final short type;
	public final int amountToBuild;
	private int amountBuilt;
	private String instruction;
	private boolean canceled;

	
	public BuildConstructionCommand(BuildConstructionManager manager, short type, int amountToBuild) {
		this.manager = manager;
		this.type = type;
		this.amountToBuild = amountToBuild;
	}
	
	
	public String getInstruction() {
		if(instruction != null) {
			return instruction;
		}else {
			return Lng.str("Place %s %s. (%s/%s)", amountToBuild, getInfo().getName(), amountBuilt, amountToBuild);
		}
	}
	
	public ElementInformation getInfo() {
		return ElementKeyMap.getInfo(type);
	}
	
	public boolean isFinished() {
		return canceled || amountBuilt >= amountToBuild; 
	}
	
	public void cancel() {
		manager.onCanceled(this);
		canceled = true;
	}


	public void onStart(GameClientState state) {
		Inventory inv = state.getPlayer().getInventory();
		int quant = inv.getOverallQuantity(type);
		if(quant < amountToBuild) {
			state.getController().popupAlertTextMessage(Lng.str("You don't have enough %s. %s/%s available.", getInfo().getName(), quant, amountToBuild));
			cancel();
		}
		
		
		int firstSlot = inv.getFirstSlot(type, true);
		if(firstSlot < 0) {
			System.err.println("[BUILDCOMMANDQUEUE] canceled "+this+"; no slot found: "+firstSlot);
			canceled = true;
			return; 
		}
		
		InventorySlot slot = inv.getSlot(firstSlot);
		if(slot == null) {
			canceled = true;
			return;
		}
		int subslot = -1;
		if(slot.isMultiSlot()) {
			for(int i = 0; i < slot.getSubSlots().size(); i++) {
				InventorySlot s = slot.getSubSlots().get(i);
				if(s.getType() == type) {
					subslot = i;
					break;
				}
			}
		}
		
		if(firstSlot != 0) {
			
			System.err.println("[BUILDCOMMANDQUEUE] found slot for "+this+"; putting "+firstSlot+" into first slot");
			//put the slot with the type in the first slot
			if(inv.getSlot(0) == null || inv.getSlot(0).isEmpty()) {
				//for creative mode, the slot to copy to has to be first
				inv.switchSlotsOrCombineClient(0, firstSlot, inv, -1);
			}else {
				//if the slot is filled its better to do it the other way around so we dont have to look for the sub slot.
				inv.switchSlotsOrCombineClient(0, firstSlot, subslot, inv, slot.count());
			}
		}
		
		
		//select first slot and make the type the selected subslot if possible
		PlayerInteractionControlManager pi = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		pi.setSelectedSlotForced(0, type);
		
		
		//issue command (put player out of advanced build mode until ctrl is repressed)
		issued = true;
		pi.hinderInteraction(300);
		
	}

	public void update(Timer timer, GameClientState state) {
		if(canceled || amountBuilt >= amountToBuild) {
			//satisfied
			return;
		}
		
		PlayerInteractionControlManager pi = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
		
		if(pi.getSelectedSlot() != 0) {
			cancel();
			return;
		}
		if(!pi.isInAnyStructureBuildMode()) {
			cancel();
			return;
		}
		int quant = state.getPlayer().getInventory().getOverallQuantity(type);
		if(quant < amountToBuild) {
			state.getController().popupAlertTextMessage(Lng.str("You don't have enough %s. %s/%s available.", getInfo().getName(), quant, amountToBuild));
			cancel();
			return;
		}
		
	}

	public boolean isExecutable(GameClientState state) {
		int quant = state.getPlayer().getInventory().getOverallQuantity(type);
		if(quant < amountToBuild) {
			return false;
		}else {
			return true;
		}
	}

	public void onEnd(GameClientState state) {
		
	}


	public void onBuiltBlock(short type) {
		if(type == this.type) {
			amountBuilt++;
		}
	}


	public void onRemovedBlock(short type) {
		if(type == this.type) {
			amountBuilt = Math.max(0, amountBuilt-1);
		}
	}


	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}


	@Override
	public String toString() {
		return "BuildConstructionCommand [type=" + ElementKeyMap.toString(type) + ", amountToBuild=" + amountToBuild + ", instruction="
				+ instruction + "]";
	}



	
	
}
