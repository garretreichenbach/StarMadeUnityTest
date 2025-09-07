/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.schema.game.client.controller.manager.ingame;

import api.common.GameClient;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.common.FastMath;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.advancedbuildmode.AdvancedBuildModeHotbar;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.common.language.Lng;
import org.schema.schine.input.Keyboard;
import org.schema.schine.resource.FileExt;

import java.io.*;
import java.util.Map;

public class HotbarLayout {
	
	private final Object2ObjectOpenHashMap<String, InventorySlot[]> hotbars = new Object2ObjectOpenHashMap<>();
	private final GameClientState state;
	private int version = 0;
	public static String PATH = "./hotbarLayouts";
	public String current;

	public HotbarLayout(GameClientState state) {
		this.state = state;
		readHotbars();
	}

	public Object2ObjectOpenHashMap<String, InventorySlot[]> getLayouts() {
		return hotbars;
	}

	private PlayerInteractionControlManager getPIM() {
		return state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().getPlayerIntercationManager();
	}

	//Sets player hotbar to template
	public void setCurrentHotbar(String name) {
		readHotbars();
		InventorySlot[] slots = hotbars.get(name);
		current = name;
		AdvancedBuildModeHotbar.selected = name;
		Inventory inventory = state.getPlayer().getInventory(null);
		if(slots != null) {
			for(int i = 0; i < slots.length; i++) {
				InventorySlot loadedSlot = slots[i];
				if(loadedSlot != null) {
					if(inventory.isInfinite()) {
						InventorySlot newSlot = inventory.put(i, loadedSlot.getType(), loadedSlot.count(), loadedSlot.metaId);
						loadedSlot.copyTo(newSlot);
						inventory.sendInventoryModification(i);
					}
				}
			}
		}
		GameClient.getClientState().getController().popupInfoTextMessage(Lng.str("Loaded Hotbar %s", name), 0);
	}

	/**
	 * Removes hotbarlayout from map
	 *
	 * @param name Name of hotbarlayout to remove
	 */
	public void removeHotbarLayout(String name) {
		hotbars.remove(name);
	}

	public boolean containsHotbar(String name) {
		return hotbars.containsKey(name);
	}

	/**
	 * Adds current hotbar layout to map, does not include meta items
	 *
	 * @param name Name of hotbarlayout
	 */
	public void addHotbarLayout(String name) {
		InventorySlot[] hotbar = new InventorySlot[Keyboard.slotKeys.length];
		for(int i = 0; i < Keyboard.slotKeys.length; i++) {
			InventorySlot slot = state.getPlayer().getInventory().getSlot(i);
			if(slot != null) hotbar[i] = slot;
			else hotbar[i] = new InventorySlot();
		}
		hotbars.put(name, hotbar);
	}

	/**
	 * Writes all the current hotbarlayouts to file at PATH
	 * Overwrites the file if it already exists
	 */
	public void writeHotbars() {
		try {
			File hotbarFile = new FileExt(PATH);
			if(!hotbarFile.exists()) initHotbars(hotbarFile);
			try(ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(hotbarFile)))) {
				out.writeInt(version);
				out.writeInt(hotbars.entrySet().size());
				for(Map.Entry<String, InventorySlot[]> entry : hotbars.entrySet()) {
					out.writeUTF(entry.getKey());
					out.writeInt(entry.getValue().length);
					for(InventorySlot slot : entry.getValue()) slot.serialize(out);
				}
			}
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Initializes all hotbarlayouts from file if it exists
	 */
	private void readHotbars() {
		try {
			File file = new FileExt(PATH);
			if(!file.exists()) initHotbars(file);
			try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
				hotbars.clear();
				int version = in.readInt();
				int hotbarCnt = in.readInt();
				for(int i = 0; i < hotbarCnt; i++) {
					String name = in.readUTF();
					int slotCnt = in.readInt();
					InventorySlot[] slots = new InventorySlot[slotCnt];
					for(int j = 0; j < slotCnt; j++) {
						slots[j] = new InventorySlot();
						slots[j].slot = j;
						slots[j].deserialize(in);
					}
					hotbars.put(name, slots);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			writeHotbars();
		}
	}

	private void initHotbars(File file) {
		try {
			file.createNewFile();
			try(ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
				out.writeInt(version);
				out.writeInt(0);
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public void next() {
		if(hotbars.size() < 2) return;
		int selected = FastMath.cyclicModulo(1, hotbars.keySet().toArray().length);
		setCurrentHotbar((String) hotbars.keySet().toArray()[selected]);
	}

	public void previous() {
		if(hotbars.size() < 2) return;
		int selected = FastMath.cyclicModulo(-1, hotbars.keySet().toArray().length);
		setCurrentHotbar((String) hotbars.keySet().toArray()[selected]);
	}
}
