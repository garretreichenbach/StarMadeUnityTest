package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.ItemsToCreditsInventory;
import org.schema.game.common.data.player.inventory.NPCFactionInventory;
import org.schema.game.common.data.player.inventory.ShopInventory;
import org.schema.game.common.data.player.inventory.StashInventory;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteInventory extends RemoteField<Inventory> {

	private boolean add;
	private InventoryHolder holder;

	public RemoteInventory(Inventory entry, InventoryHolder holder, boolean add, boolean synchOn) {
		super(entry, synchOn);
		this.add = add;
		this.holder = holder;
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		int localInventoryType = stream.readByte();

		boolean hasName = false;

		if (localInventoryType >= 16) {
			hasName = true;
			localInventoryType -= 16;
		}
		int x = 0;
		int y = 0;
		int z = 0;
		long parameter = Long.MIN_VALUE;
		if(localInventoryType == Inventory.NPC_FACTION_INVENTORY){
			parameter = stream.readLong();
		}else if(localInventoryType == Inventory.SHOP_INVENTORY){
			
		}else{
			
			x = stream.readShort();
			y = stream.readShort();
			z = stream.readShort();
		}
		short production = stream.readShort();
		int productionLimit = stream.readInt();
			 
//		short filterCount = stream.readShort();
//		Short2IntOpenHashMap map = null;
//		if (filterCount > 0) {
//			map = new Short2IntOpenHashMap(filterCount);
//			for (int i = 0; i < filterCount; i++) {
//				map.put(stream.readShort(), stream.readInt());
//			}
//		}
		String name;
		if (hasName) {
			name = stream.readUTF();
		} else {
			name = "";
		}

		add = stream.readBoolean();

		Inventory inventory = null;
		if (add) {
			switch(localInventoryType) {
				case (1) -> inventory = new ItemsToCreditsInventory(holder, ElementCollection.getIndex(x, y, z));
				case (Inventory.NPC_FACTION_INVENTORY) -> {
					assert (parameter != Long.MIN_VALUE);
					inventory = new NPCFactionInventory((GameStateInterface) holder.getState(), parameter);
					((StashInventory) inventory).setProduction(production);
					((StashInventory) inventory).setProductionLimit(productionLimit);
					((StashInventory) inventory).setCustomName(name);
				}
				case (Inventory.SHOP_INVENTORY) -> {
					inventory = new ShopInventory(holder, Long.MIN_VALUE);
					((StashInventory) inventory).setProduction(production);
					((StashInventory) inventory).setProductionLimit(productionLimit);
					((StashInventory) inventory).setCustomName(name);
				}
				default -> {
					inventory = new StashInventory(holder, ElementCollection.getIndex(x, y, z));
					((StashInventory) inventory).setProduction(production);
					((StashInventory) inventory).setProductionLimit(productionLimit);
					((StashInventory) inventory).setCustomName(name);
				}
			}
			inventory.deserialize(stream);

		} else {
			switch(localInventoryType) {
				case (1) -> inventory = new ItemsToCreditsInventory(holder, ElementCollection.getIndex(x, y, z));
				case (Inventory.NPC_FACTION_INVENTORY) -> {
					assert (parameter != Long.MIN_VALUE);
					inventory = new NPCFactionInventory((GameStateInterface) holder.getState(), parameter);
				}
				case (Inventory.SHOP_INVENTORY) -> inventory = new ShopInventory(holder, ElementCollection.getIndex(x, y, z));
				default -> inventory = new StashInventory(holder, ElementCollection.getIndex(x, y, z));
			}
		}
//		if (map != null && inventory.getFilter() != null) {
//			inventory.getFilter().putMap(map);
//		}
		if(inventory instanceof NPCFactionInventory){
			assert(inventory.getParameter() != 0 && inventory.getParameter() != Long.MIN_VALUE):parameter;
		}
		set(inventory);

	}

	@Override
	public int toByteStream(final DataOutputStream buffer) throws IOException {

		boolean hasName = get().getCustomName() != null && get().getCustomName().length() > 0;

		buffer.writeByte(get().getLocalInventoryType() + (hasName ? 16 : 0));
		
		if(get() instanceof NPCFactionInventory){
			assert(get().getParameter() != Long.MIN_VALUE);
			buffer.writeLong(get().getParameter());
		}else if(get() instanceof ShopInventory){
		}else{
		
			buffer.writeShort(get().getParameterX());
			buffer.writeShort(get().getParameterY());
			buffer.writeShort(get().getParameterZ());
		}
		
		buffer.writeShort(get().getProduction());
		buffer.writeInt(get().getProductionLimit());
//		if (get().getFilter() != null) {
//			final int size = get().getFilter().size();
//			buffer.writeShort(size);
//			
//			get().getFilter().handleLoop(new TypeAmountLoopHandle() {
//				
//				@Override
//				public void handle(short type, int amount) {
//					try {
//						buffer.writeShort(type);
//						buffer.writeInt(amount);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			});
//			
//			
//		} else {
//			buffer.writeShort(0);
//		}

		if (hasName) {
			buffer.writeUTF(get().getCustomName());
		}

		buffer.writeBoolean(add);

		if (add) {

			get().serialize(buffer);
		}

		return 1;
	}

	/**
	 * @return the add
	 */
	public boolean isAdd() {
		return add;
	}

	/**
	 * @param add the add to set
	 */
	public void setAdd(boolean add) {
		this.add = add;
	}

}
