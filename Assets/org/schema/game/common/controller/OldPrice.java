package org.schema.game.common.controller;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class OldPrice implements SerializationInterface{

	public static final byte SERIALIZATION_NO_PRICE = 1;
	public static final byte SERIALIZATION_VALUE_ONLY = 2;
	public static final byte SERIALIZATION_FULL = 3;
	public short priceTypeBuy;
	public int amountBuy = 1;

	public short priceTypeSell;
	public int amountSell = 1;
	
	public int buyDownTo = -1;
	public int sellUpTo = -1;

	/**
	 * only used for shopOwnership
	 */
	public short priceForType = -1;
	/**
	 * only used for shopOwnership
	 */
	public String modName;
	/**
	 * only used for shopOwnership
	 */
	public boolean add;
	/**
	 * only used for shopOwnership
	 */
	public int senderId = -1;
	public long permission;
	long tradePermission;

	public OldPrice() {
		super();
	}

	public OldPrice(short priceForType, short typeBuy, int amountBuy, short typeSell, int amountSell) {
		super();
		this.priceForType = priceForType;
		this.priceTypeBuy = typeBuy;
		this.amountBuy = amountBuy;
		this.priceTypeSell = typeSell;
		this.amountSell = amountSell;
	}

	public void buy(PlayerState player, short buyItem, int quantity, ShopInterface shop, IntOpenHashSet invMod, IntOpenHashSet shopHash) throws NoSlotFreeException {
		assert(shop.getShopInventory().checkVolume());
		boolean ok = true;

		int overallQuantity = shop.getShopInventory().getOverallQuantity(buyItem);
		if (overallQuantity < quantity && !shop.getShoppingAddOn().isInfiniteSupply()) {
			quantity = Math.min(overallQuantity, quantity);
			player.sendServerMessage(new ServerMessage(Lng.astr("Shop only had %s of this item",  overallQuantity), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
		}
		int toBuy = quantity;
		
		if(!(player.getInventory().canPutIn(buyItem, toBuy))){
			player.sendServerMessage(new ServerMessage(Lng.astr("Inventory full!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
			ok = false;
		}else{
			if ((!(shop.isAiShop()) && (shop.getShopOwners().isEmpty() || shop.getShopOwners().contains(player.getName().toLowerCase(Locale.ENGLISH)))) || shop.getShoppingAddOn().isInfiniteSupply()) {
				//dont let the player pay
				ok = true;
			} else {
	
				toBuy = canAfford(player, quantity, shop);
				
				if (!ElementKeyMap.isValidType(priceTypeBuy)) {
					if (player.getCredits() >= toBuy * amountBuy) {
						player.modCreditsServer(-toBuy * amountBuy);
						shop.modCredits(toBuy * amountBuy);
					} else {
						ok = false;
					}
				} else {
					
					if (player.getInventory().getOverallQuantity(priceTypeBuy) >= toBuy * amountBuy) {
						//remove price from player
						player.getInventory().decreaseBatch(priceTypeBuy, toBuy * amountBuy, invMod);
						//add price to shop
						shopHash.add(shop.getShopInventory().incExistingOrNextFreeSlot(priceTypeBuy, toBuy * amountBuy));
						assert(shop.getShopInventory().checkVolume());
						assert(player.getInventory().checkVolume());
						assert (shop.getShopInventory().slotsContaining(priceTypeBuy) <= 1);
					} else {
						
						ok = false;
					}
				}
	
			}
		}

		if (ok) {
			//subtract items from shop
			shop.getShopInventory().decreaseBatch(buyItem, toBuy, shopHash);
			//add items to player
			int slot = player.getInventory().incExistingOrNextFreeSlot(buyItem, toBuy);
			invMod.add(slot);
			assert(shop.getShopInventory().checkVolume());
			assert(player.getInventory().checkVolume());
			assert (shop.getShopInventory().slotsContaining(buyItem) <= 1);
		}
		assert(shop.getShopInventory().checkVolume());
	}

	public int canAfford(PlayerState player, int quantity, ShopInterface shop) {

		if (!(shop.isAiShop()) && (shop.getShopOwners().isEmpty() || shop.getShopOwners().contains(player.getName().toLowerCase(Locale.ENGLISH)))) {
			//dont let the player pay
			return quantity;
		}
		if (shop.getShoppingAddOn().isInfiniteSupply()) {
			return quantity;
		}
		if (!shop.getShoppingAddOn().hasPermission(player)) {
			player.sendServerMessage(new ServerMessage(Lng.astr("You are not allowed\nto buy at this shop!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
			return 0;
		}
		if (amountBuy <= 0) {
			//do not divide by zero
			return 0;
		}
		if (!ElementKeyMap.isValidType(priceTypeBuy)) {
			
			int amountForCredits = Math.min(quantity, player.getCreditsInt() / amountBuy);
			
			int has = shop.getShopInventory().getOverallQuantity(priceForType);
			int possible = has - buyDownTo;
			if(priceForType == ElementKeyMap.FACTION_BLOCK){
				System.err.println("AMMM "+quantity+" CAN:" +amountForCredits+" ;;; has: "+has+"; DOWN "+buyDownTo+"; UP: " +sellUpTo+"; possible: "+possible);
			}
			
			return Math.max(0, Math.min(possible, amountForCredits));
		} else {
			return Math.min(quantity, player.getInventory(null).getOverallQuantity(priceTypeBuy) / amountBuy);
		}
	}

	public int canShopAfford(ShopInterface currentClosestShop, int quantity) {
		if(amountSell <= 0){
			//do not divide by zero
			return 0;
		}
		if (!ElementKeyMap.isValidType(priceTypeSell)) {
			
			int amountForCredits = (int)Math.min(quantity, currentClosestShop.getCredits() / amountSell);
			int has = currentClosestShop.getShopInventory().getOverallQuantity(priceForType);
			int possible = sellUpTo - has;
			return Math.max(0, Math.min(possible, amountForCredits));
			
		} else {
			return Math.min(quantity, currentClosestShop.getShopInventory().getOverallQuantity(priceTypeSell) / amountSell);
		}
	}
	@Override
	public void deserialize(DataInput stream, int senderId, boolean isOnServer) throws IOException {
		this.senderId = senderId;

		priceTypeBuy = stream.readShort();

		if (priceTypeBuy == 0) {
			modName = stream.readUTF();
			add = stream.readBoolean();
			permission = stream.readLong();
			tradePermission = stream.readLong();
		} else {
			priceForType = stream.readShort();
			amountBuy = stream.readInt();
			buyDownTo = stream.readInt();
		}

		
		
		
		priceTypeSell = stream.readShort();

		if (priceTypeSell == 0) {
		} else {
			priceForType = stream.readShort();
			amountSell = stream.readInt();
			sellUpTo = stream.readInt();
		}
	}
	@Override
	public void serialize(DataOutput buffer, boolean isOnServer) throws IOException {
		buffer.writeShort(priceTypeBuy);
		if (priceTypeBuy == 0) {
			buffer.writeUTF(modName);
			buffer.writeBoolean(add);
			buffer.writeLong(permission);
			buffer.writeLong(tradePermission);
		} else {
			buffer.writeShort(priceForType);
			buffer.writeInt(amountBuy);
			buffer.writeInt(buyDownTo);
		}
		
		
		
		
		buffer.writeShort(priceTypeSell);
		if (priceTypeSell == 0) {
		} else {
			buffer.writeShort(priceForType);
			buffer.writeInt(amountSell);
			buffer.writeInt(sellUpTo);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		long bits = 1L;
		bits = 31L * bits + priceTypeBuy;
		bits = 31L * bits + amountBuy;
		bits = 31L * bits + priceTypeSell;
		bits = 31L * bits + amountSell;
		return (int) (bits ^ (bits >> 32));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return priceTypeBuy == ((OldPrice) obj).priceTypeBuy && amountBuy == ((OldPrice) obj).amountBuy && priceTypeSell == ((OldPrice) obj).priceTypeSell && amountSell == ((OldPrice) obj).amountSell;
	}


	@Override
	public String toString() {
		return "Price [priceForType=" + priceForType + ", priceTypeBuy="
				+ priceTypeBuy + ", amountBuy=" + amountBuy + ", buyDownTo="
				+ buyDownTo + ", priceTypeSell=" + priceTypeSell
				+ ", amountSell=" + amountSell + ", sellUpTo=" + sellUpTo
				+ ", add=" + add + ", permission=" + permission + ", modName="
				+ modName + "]";
	}

	public void sell(PlayerState player, short sellItem, int quantity, ShopInterface shop, IntOpenHashSet invMod, IntOpenHashSet shopHash) throws NoSlotFreeException {
		assert(shop.getShopInventory().checkVolume());
		boolean ok = true;
		int overallQuantity = player.getInventory(null).getOverallQuantity(sellItem);
		int toSell = quantity;
		if (overallQuantity < quantity) {
			toSell = overallQuantity;
			player.sendServerMessage(new ServerMessage(Lng.astr("SERVER: you only had %s",   overallQuantity), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
		}

		if (!(shop.isAiShop()) && (shop.getShopOwners().isEmpty() || shop.getShopOwners().contains(player.getName().toLowerCase(Locale.ENGLISH)))) {
			//dont let the player pay
			ok = true;
		} else {

			if (!ElementKeyMap.isValidType(priceTypeSell)) {

				int cost = toSell * amountSell;
				if (shop.getCredits() < toSell * amountSell) {
					player.sendServerMessage(new ServerMessage(Lng.astr("Shop cannot pay you (left: %s Credits)", shop.getCredits()), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
					ok = false;
				} else {
					player.modCreditsServer(cost);
					shop.modCredits(-cost);
				}
			} else {
				int shopAmount = shop.getShopInventory().getOverallQuantity(priceTypeSell);

				if (shopAmount < toSell * amountSell) {
					player.sendServerMessage(new ServerMessage(Lng.astr("Shop cannot pay you \n%s (has %s) of %s\n(out of stock for price)", toSell * amountSell,  shopAmount,  ElementKeyMap.getInfo(priceTypeSell).getName()) , ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
					ok = false;
				} else {
					//decrease price from shop
					shop.getShopInventory().decreaseBatch(priceTypeSell, toSell * amountSell, shopHash);

					//add price to player
					invMod.add(player.getInventory(null).incExistingOrNextFreeSlot(priceTypeSell, toSell * amountSell));
					assert (shop.getShopInventory().slotsContaining(priceTypeSell) <= 1);
				}
			}
		}
		if (ok) {
			assert (shop.getShopInventory().slotsContaining(sellItem) <= 1);
			//decrease item to sell from player
			player.getInventory(null).decreaseBatch(sellItem, toSell, invMod);

			//add item to sell to shop
			shopHash.add(shop.getShopInventory().incExistingOrNextFreeSlot(sellItem, toSell));

			assert (shop.getShopInventory().slotsContaining(sellItem) <= 1);
			assert(shop.getShopInventory().checkVolume());
		}
	}

	public String toStringBuy(int wantedQuantity) {
		if (!ElementKeyMap.isValidType(priceTypeBuy)) {
			return amountBuy <= 0 ? Lng.str("not set") : Lng.str("%s Credits", wantedQuantity * amountBuy);
		} else {
			return Lng.str("%s of %s", wantedQuantity * amountBuy,  ElementKeyMap.getInfo(priceTypeBuy).getName());
		}
	}

	public String toStringBuySimple(int wantedQuantity) {
		if (!ElementKeyMap.isValidType(priceTypeBuy)) {
			return amountBuy <= 0 ? Lng.str("not set") : String.valueOf(wantedQuantity * amountBuy);
		} else {
			return Lng.str("%s of %s",  String.valueOf(wantedQuantity * amountBuy),  ElementKeyMap.getInfo(priceTypeBuy).getName());
		}
	}

	public String toStringSellSimple(int wantedQuantity) {
		if (!ElementKeyMap.isValidType(priceTypeSell)) {
			return amountSell <= 0 ? Lng.str("not set") : Lng.str("%sc", wantedQuantity * amountSell);
		} else {
			return Lng.str("%s of %s", wantedQuantity * amountSell,  ElementKeyMap.getInfo(priceTypeSell).getName());
		}
	}
	public String toStringSell(int wantedQuantity) {
		if (!ElementKeyMap.isValidType(priceTypeSell)) {
			return amountSell <= 0 ? Lng.str("not set") : Lng.str("%sc", wantedQuantity * amountSell);
		} else {
			return Lng.str("%s of %s", wantedQuantity * amountSell,  ElementKeyMap.getInfo(priceTypeSell).getName());
		}
	}

	public boolean isCreditBuyPrice() {
		return !ElementKeyMap.isValidType(priceTypeBuy);
	}
	public boolean isCreditSellPrice() {
		return !ElementKeyMap.isValidType(priceTypeSell);
	}

	
}
