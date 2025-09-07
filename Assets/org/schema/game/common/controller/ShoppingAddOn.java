package org.schema.game.common.controller;

import com.bulletphysics.linearmath.AabbUtil2;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.vecmath.Vector3f;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ShopOption.ShopOptionType;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.common.controller.trade.TradeManager;
import org.schema.game.common.controller.trade.TradeManager.TradePerm;
import org.schema.game.common.controller.trade.TradeNodeClient;
import org.schema.game.common.controller.trade.TradeNodeStub;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.player.inventory.TradePricePair;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Universe;
import org.schema.game.network.objects.TradePrice;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.game.network.objects.TradePriceSingle;
import org.schema.game.network.objects.TradePrices;
import org.schema.game.network.objects.remote.RemoteShopOption;
import org.schema.game.network.objects.remote.RemoteTradePrice;
import org.schema.game.network.objects.remote.RemoteTradePriceSingle;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class ShoppingAddOn implements SerializationInterface{

	private static final float SHOP_DISTANCE = 64;
	private static final int SHOP_CHECK_PERIOD = 500;
	private static long SHOP_PRICE_CHECK_PERIOD = 60000 * 15;
	final ShopInterface shop;
	
	final Short2ObjectOpenHashMap<TradePricePair> priceList = new Short2ObjectOpenHashMap<TradePricePair>(ElementKeyMap.highestType);
	
	private final Vector3f sMin = new Vector3f();
	private final Vector3f sMax = new Vector3f();
	private final Vector3f oMin = new Vector3f();
	private final Vector3f oMax = new Vector3f();
	private final Set<String> ownerPlayers = new ObjectOpenHashSet<String>();
	private long lastShopCheck;
	private long lastPriceCheck;
	private double basePriceMult = 1;
	private boolean active = true;
	private boolean flagForcedUpdate;
	private long permissionToPurchase = TradeManager.PERM_ALL_BUT_ENEMY;
	private long permissionToTrade = TradeManager.PERM_FACTION;
	private long lastHit;
	private long lastFleetSent;
	private Int2IntOpenHashMap strikes = new Int2IntOpenHashMap();
	private long credits = 0;
	private boolean infiniteSupply;
	private boolean tradeNodeOn;
	private boolean oldVersionClearFlag;

	public ShoppingAddOn(ShopInterface shop) {
		this.shop = shop;
		if (isAIShop()) {
			credits = (ServerConfig.SHOP_NPC_STARTING_CREDITS.getInt());
		}
		
	}

	public void onHit(Damager from) {
		if(!isOnServer()) {
			return;
		}
		if (isAIShop() && active && from != null && from.getOwnerState() != null && from.getOwnerState() instanceof PlayerState) {

			PlayerState player = (PlayerState) from.getOwnerState();

			if (from.getState() instanceof GameServerState) {
				Sector sector = ((GameServerState) from.getState()).getUniverse().getSector(shop.getSectorId());
				if (sector != null && sector.isProtected()) {
					player.sendServerMessage(new ServerMessage(
							Lng.astr("This is a protected area, weapons\ndisabled."),
							ServerMessage.MESSAGE_TYPE_INFO, player.getId()));
					return;
				}
			}

			if (System.currentTimeMillis() - lastHit > 5000) {

				int strikesCount = 0;

				if (strikes.containsKey(player.getId())) {
					strikesCount = strikes.get(player.getId());
				}
				strikesCount++;
				if (strikesCount < 3) {
					System.err.println("STRIKES: " + strikesCount);
					if (strikesCount <= 1) {
						player.sendServerMessage(new ServerMessage(
								Lng.astr("####### Transmission Start\n\nCease fire immediately!\n\n####### Transmission End"),
								ServerMessage.MESSAGE_TYPE_WARNING, player.getId()));
					} else {
						player.sendServerMessage(new ServerMessage(
								Lng.astr("####### Transmission Start\n\nCease fire immediately!\nThis is your last warning!\n\n####### Transmission End"),
								ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
					}

					strikes.put(player.getId(), strikesCount);
				} else {

					if (System.currentTimeMillis() - lastFleetSent > 40000) {
						player.sendServerMessage(new ServerMessage(
								Lng.astr("####### Transmission Start\n\nFleet dispatched!\n\n####### Transmission End"),
								ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
						lastFleetSent = System.currentTimeMillis();

						//prepare to die
						try {
							((GameServerState) shop.getState()).getSimulationManager().sendToAttackSpecific(
									player.getFirstControlledTransformable(), FactionManager.TRAIDING_GUILD_ID, 6);
						} catch (PlayerControlledTransformableNotFound e) {
							e.printStackTrace();
						}
					} else {
						int rMsg = Universe.getRandom().nextInt(7);
						switch(rMsg) {
							case (0) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nOur Fleet is on the way to kill you!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (1) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nThe Trading Guild will\neliminate you!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (2) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nAre you suicidal?!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (3) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nIn space, nobody can hear you scream!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (4) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nFreeze!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (5) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nPlease pull over, sir!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
							case (6) -> player.sendServerMessage(new ServerMessage(Lng.astr("####### Transmission Start\n\nNo soup for YOU!\n\n####### Transmission End"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
						}
					}
					//					strikes.put(player.getId(), 0);
				}

				lastHit = System.currentTimeMillis();
			}
		}

	}

	public boolean hasPermission(AbstractOwnerState ps) {
		if(getOwnerPlayers().isEmpty() || getOwnerPlayers().contains(ps.getName().toLowerCase(Locale.ENGLISH))){
			return true;
		}
		
		if (TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.ENEMY)) {
			return true;
		}
		FactionState fs = (FactionState) shop.getState();
		FactionManager factionManager = fs.getFactionManager();
		
//		System.err.println(shop.getState()+" PERMISSION :"
//				+TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.ENEMY)+" "
//				+TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.FACTION)+" "
//				+TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.ALLY)+" "
//				+TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.NEUTRAL)+" "
//				+factionManager.isEnemy(shop.getFactionId(), ps)
//				
//				);
		if (TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.FACTION)) {
			if (ps.getFactionId() == shop.getFactionId()) {
				return true;
			}
		} 
		if (TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.ALLY)) {
			if (ps.getFactionId() == shop.getFactionId() || factionManager.isFriend(ps.getFactionId(), shop.getFactionId())) {
				return true;
			}
		} 
		if (TradeManager.isPermission(shop.getPermissionToPurchase(), TradePerm.NEUTRAL)) {

			if (!factionManager.isEnemy(shop.getFactionId(), ps)) {
				return true;
			}

		}

		return false;
	}

	public boolean isAIShop() {
		return shop instanceof ShopSpaceStation || 
				shop.getFactionId() == FactionManager.TRAIDING_GUILD_ID; 
	}
	

	private int calculatePrice(ElementInformation info) {
		assert (shop.isOnServer());
		//retrieve inventory slot for the item

		double lowerLimit = ServerConfig.SHOP_SELL_BUY_PRICES_LOWER_LIMIT.getFloat();
		double upperLimit = ServerConfig.SHOP_SELL_BUY_PRICES_UPPER_LIMIT.getFloat();

		//inventory slot < 0 if it doesnt exist
		int count = shop.getShopInventory().getOverallQuantity(info.getId());
		long basePrice = info.getPrice(((GameStateInterface) shop.getState()).getMaterialPrice());
		double v;
		double b;
		if (!ServerConfig.SHOP_USE_STATIC_SELL_BUY_PRICES.isOn()) {
			v = Math.pow(Math.max(1, shop.getShopInventory().getMaxStock() - count), 0.35) * 0.1f * basePrice;

			b = Math.max(basePrice, v * basePriceMult);
			b = Math.min(basePrice * upperLimit, Math.max(b, basePrice * lowerLimit));
		} else {
			v = basePrice;
			b = Math.max(basePrice, v * basePriceMult);
		}

		//add both for final price
		int finalPrice = (int) Math.min(Integer.MAX_VALUE, (long) b);

		return finalPrice;
	}

	private void checkShopDistance(long time, boolean forced) {

		if (forced || time > lastShopCheck + SHOP_CHECK_PERIOD) {
			synchronized (shop.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : shop.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
					if (s instanceof ShopperInterface) {
						if (active && isInShopDistance((ShopperInterface) s) && ((ShopperInterface) s).getSectorId() == shop.getSectorId()) {
							((ShopperInterface) s).getShopsInDistance().add(shop);
						} else {
							((ShopperInterface) s).getShopsInDistance().remove(shop);
						}
					}
				}
				lastShopCheck = time;
			}
		}
	}
	public static boolean isTradePermShop(PlayerState p,
			ShopInterface currentClosestShop) {
		if(currentClosestShop != null && !(currentClosestShop.isAiShop())){
			if((currentClosestShop.getShopOwners().isEmpty() || 
						currentClosestShop.getShopOwners().contains(p.getName().toLowerCase(Locale.ENGLISH)))){
				return true;
			}
			long t = currentClosestShop.getPermissionToTrade();
			if(t == TradeManager.PERM_ALL){
				return true;
			}
			int sId = currentClosestShop.getFactionId();
			int pId = p.getFactionId();
			if(TradeManager.isPermission(t, TradePerm.FACTION) && sId != 0 && pId == sId){
				return true;
			}
			if(TradeManager.isPermission(t, TradePerm.ALLY) && ((FactionState)p.getState()).getFactionManager().isFriend(pId, sId)){
				return true;
			}
		
		}
		return false;
	}
	public void clientRequestCredits(PlayerState requestingPlayer, int amount, boolean withdrawal) {
		assert (requestingPlayer.isClientOwnPlayer());

		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);
		} else {
			
			if (isTradePermShop(requestingPlayer, this.shop)) {
				ShopOption t =new ShopOption();
				t.type = withdrawal ?  ShopOptionType.CREDIT_WITHDRAWAL : ShopOptionType.CREDIT_DEPOSIT;
				t.credits = withdrawal ? -Math.abs(amount) : Math.abs(amount);
				shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, isOnServer()));
			} else {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
			}
		}
	}
	public static String toStringPrice(TradePriceInterface price, int wantedQuantity){
		if(price == null){
			return Lng.str("not set");
		}else{
			return Lng.str("%sc", wantedQuantity * price.getPrice());
		}
	}
	public String toStringForPurchase(short type, int wantedQuantity) {
		TradePriceInterface price = getPrice(type, false);
		return toStringPrice(price, wantedQuantity);
	}

	public int canAfford(PlayerState player, short type, int quantity) {

		
		
		if (!(shop.isAiShop()) && (shop.getShopOwners().isEmpty() || shop.getShopOwners().contains(player.getName().toLowerCase(Locale.ENGLISH)))) {
			//dont let the player pay
			return quantity;
		}
		if (shop.getShoppingAddOn().infiniteSupply) {
			return quantity;
		}
		if (!shop.getShoppingAddOn().hasPermission(player)) {
			player.sendServerMessage(new ServerMessage(Lng.astr("You are not allowed\nto buy at this shop!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
			return 0;
		}
		TradePriceInterface price = getPrice(type, false);
		if(price == null){
			return 0;//no price set
		}
		if(price.getPrice() == 0){
			return 0;
		}
		
		int avail = price.getAmount();
		if( price.getLimit() >= 0){
			avail = price.getAmount() - price.getLimit();
		}
			
		int amountForCredits = Math.min(avail, Math.min(quantity, player.getCreditsInt() / price.getPrice()));
		
		return Math.max(0,  amountForCredits);
	}

	public int canShopAfford(short type,int quantity) {
		
		TradePriceInterface price = getPrice(type, true);
		
		if (shop.getShoppingAddOn().infiniteSupply) {
			return quantity;
		}
		if(price == null){
			return 0;//no price set
		}
		if(price.getPrice() == 0){
			return 0;
		}
		int avail = quantity;
		if( price.getLimit() >= 0){
			avail = price.getLimit() - price.getAmount();
		}

		int amountForCredits = (int)Math.min(avail, Math.min(quantity, credits / price.getPrice()));
		return Math.max(0, amountForCredits);
			
	}
	
	public String toStringForSale(short type, int wantedQuantity) {
		TradePriceInterface price = getPrice(type, true);
		return toStringPrice(price, wantedQuantity);
	}
	public static boolean isSelfOwnedShop(InputState state, ShopInterface currentClosestShop) {
		return (currentClosestShop != null && !(currentClosestShop.isAiShop()) && (
				(currentClosestShop.getShopOwners().isEmpty() || currentClosestShop.getShopOwners().contains(((GameClientState) state).getPlayer().getName().toLowerCase(Locale.ENGLISH)))));
	}
	public static boolean isTradePermShop(GameClientState state,
			ShopInterface currentClosestShop) {
		if(currentClosestShop != null && !(currentClosestShop.isAiShop())){
			if((currentClosestShop.getShopOwners().isEmpty() || 
						currentClosestShop.getShopOwners().contains(state.getPlayer().getName().toLowerCase(Locale.ENGLISH)))){
				return true;
			}
			long t = currentClosestShop.getPermissionToTrade();
			if(t == TradeManager.PERM_ALL){
				return true;
			}
			int sId = currentClosestShop.getFactionId();
			int pId = state.getPlayer().getFactionId();
			if(TradeManager.isPermission(t, TradePerm.FACTION) && pId == sId){
				return true;
			}
			if(TradeManager.isPermission(t, TradePerm.ALLY) && state.getFactionManager().isFriend(pId, sId)){
				return true;
			}
		
		}
		return false;
	}
	public int getPriceString(ElementInformation info, boolean weWantToPurchase){
		//if we want to purchase, return the price at which this shop sells
		TradePriceInterface price = getPrice(info.getId(), !weWantToPurchase);
		
		return price != null ? price.getPrice() : -1 ;
	}
	public static TradePrice getPriceInstanceIfExisting(ShopInterface currentClosestShop, short type, boolean buy){
		TradePriceInterface f = currentClosestShop.getPrice(type, buy);
		if(f != null){
			assert(f.getType() == type);
			return new TradePrice(f.getType(), f.getAmount(), f.getPrice(), f.getLimit(), f.isSell());
		}else{
			return null;
		}
	}
	public static TradePrice getPriceInstance(ShopInterface currentClosestShop, short type, boolean buy){
		boolean sell = !buy;
		TradePriceInterface f = currentClosestShop.getPrice(type, buy);
		TradePrice s;
		if(f != null){
			assert(f.getType() == type);
			s = new TradePrice(f.getType(), f.getAmount(), f.getPrice(), f.getLimit(), f.isSell());
		}else{
			s = new TradePrice(type, 0, -1, -1, sell);
		}
		return s;
	}
	public void clientRequestDeposit(PlayerState requestingPlayer, int amount) {
		clientRequestCredits(requestingPlayer, amount, false);
	}

	public void clientRequestLocalPermission(PlayerState requestingPlayer, long localPermission) {
		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);
		} else {
			if (getOwnerPlayers().isEmpty() || getOwnerPlayers().contains(requestingPlayer.getName().toLowerCase(Locale.ENGLISH))) {
				ShopOption t = new ShopOption();
				t.permission = localPermission;
				t.type = ShopOptionType.LOCAL_PERMISSION;
				shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, isOnServer()));
			} else {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
			}
		}
	}
	public void clientRequestTradePermission(PlayerState requestingPlayer, long tradePermission) {
		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);
		} else {
			if (getOwnerPlayers().isEmpty() || getOwnerPlayers().contains(requestingPlayer.getName().toLowerCase(Locale.ENGLISH))) {
				ShopOption t = new ShopOption();
				t.permission = tradePermission;
				t.type = ShopOptionType.TRADE_PERMISSION;
				shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, isOnServer()));
			} else {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
			}
		}
	}
	public void clientRequestPlayerRemove(PlayerState requestingPlayer, String name) {
		assert (requestingPlayer.isClientOwnPlayer());
		assert (name != null);
		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);
		} else {
			if (getOwnerPlayers().isEmpty() || getOwnerPlayers().contains(requestingPlayer.getName().toLowerCase(Locale.ENGLISH))) {
				ShopOption t = new ShopOption();
				t.playerName = name;
				t.type = ShopOptionType.USER_REMOVE;
				shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, isOnServer()));
			} else {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
			}
		}
	}
	public void clientRequestPlayerAdd(PlayerState requestingPlayer, String name) {
		assert (requestingPlayer.isClientOwnPlayer());
		assert (name != null);

		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);

		} else {
			if (name.length() < 1) {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Invalid player name!"), 0);
			} else {
				if (getOwnerPlayers().isEmpty() || getOwnerPlayers().contains(requestingPlayer.getName().toLowerCase(Locale.ENGLISH))) {
					ShopOption t = new ShopOption();
					t.playerName = name;
					t.type = ShopOptionType.USER_ADD;
					shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, isOnServer()));
				} else {
					((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
				}
			}
		}
	}

	public void clientRequestSetPrice(PlayerState requestingPlayer, TradePriceInterface tradePrice) {
		assert (requestingPlayer.isClientOwnPlayer());
		if (isAIShop()) {
			((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("Not allowed to change NPC shop!"), 0);
		} else {

			if (isTradePermShop(requestingPlayer, this.shop)) {
				
				TradePriceSingle p = new TradePriceSingle();
				p.tp = tradePrice;
				shop.getNetworkObject().getPriceModifyBuffer().add(new RemoteTradePriceSingle(p, false));
//				((GameClientState) requestingPlayer.getState()).getController().popupInfoTextMessage(Lng.str("Price for %s changed.", ElementKeyMap.toString(priceFor)), 0);
			} else {
				((GameClientState) requestingPlayer.getState()).getController().popupAlertTextMessage(Lng.str("You don't have permission\nto do that!"), 0);
			}
		}
	}

	public void clientRequestWithdrawal(PlayerState requestingPlayer, int amount) {
		clientRequestCredits(requestingPlayer, amount, true);
	}

	public void clearInventory(boolean send) throws NoSlotFreeException {
		IntOpenHashSet changed = new IntOpenHashSet();
		shop.getShopInventory().clear(changed);
		if (send) {
			shop.getShopInventory().sendInventoryModification(changed);
		}
	}

	public void fillInventory(boolean send, boolean full) throws NoSlotFreeException {
		IntOpenHashSet changed = new IntOpenHashSet();
		boolean mineralStacked = Universe.getRandom().nextInt(5) == 0;
		if (full) {
			shop.getShopInventory().clear(changed);
		}
		for (Short s : ElementKeyMap.typeList()) {
			ElementInformation info = ElementKeyMap.getInfo(s);
			int cS = shop.getShopInventory().getOverallQuantity(s);
			int max = shop.getShopInventory().getMaxStock();
			int m = 0;
			if (full) {
				m = shop.getShopInventory().getMaxStock();
			} else {

				if (info.getType().hasParent("ship")) {
					if (Universe.getRandom().nextInt(10) == 0) {
						m = Universe.getRandom().nextInt(100);
					} else {
						m = 100 + Universe.getRandom().nextInt(10000);
					}
				} else if (info.getType().hasParent("terrain")) {
					if (info.getType().hasParent("mineral")) {
						if (mineralStacked) {
							m = 2000 + Universe.getRandom().nextInt(10000);
						} else if (Universe.getRandom().nextInt(3) == 0) {
							if (Universe.getRandom().nextInt(10) == 0) {
								m = 500 + Universe.getRandom().nextInt(5000);
							} else {
								m = Universe.getRandom().nextInt(100);
							}
						}
					} else {
						m = Universe.getRandom().nextInt(10000);
					}
				} else if (info.getType().hasParent("spacestation")) {
					m = 100 + Universe.getRandom().nextInt(5000);

				} else {
					m = Universe.getRandom().nextInt(1000);

				}

			}
			int toFillFull = max - cS;

			m = Math.min(toFillFull, m);
			if (m > 0) {
				changed.add(shop.getShopInventory().incExistingOrNextFreeSlot(s, m));
			}
		}
		if (send) {
			shop.getShopInventory().sendInventoryModification(changed);
		}
	}
	
	public void fromTagStructure(Tag tag) {
		Tag[] v = tag.getStruct();
		if(v[0].getType() == Type.BYTE){
			byte version = v[0].getByte();
			basePriceMult = v[1].getDouble();
			lastPriceCheck = v[2].getLong();
			byte[] pricesBytes = v[3].getByteArray();
			permissionToPurchase = v[4].getLong();
			credits = v[5].getLong();
			
			ObjectArrayList<String> l = new ObjectArrayList<String>();
			Tag.listFromTagStruct(l, (Tag[]) v[6].getValue());
			for(int i = 0; i < l.size(); i++){
				getOwnerPlayers().add(l.get(i).toLowerCase(Locale.ENGLISH));
			}
			infiniteSupply = (Byte) v[7].getValue() == 1 ? true : false;
			setTradeNodeOn(((Byte) v[8].getValue() == (byte)1 ));
			
			permissionToTrade = v[9].getLong();
			
			try {
				TradePrices pc = deserializeTradePrices(new DataInputStream(new FastByteArrayInputStream(pricesBytes)), true);
				putAllPrices(pc.getPrices());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}else{
			fromOldTag(tag);
		}
		
		
		
	}

	public Tag toTagStructure() {
		
		byte[] prices = null;
		try {
			prices = getTradePrices().getPricesBytesCompressed(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		Tag basePriceTag = new Tag(Type.DOUBLE, null, basePriceMult);
		Tag lastPriceCheckTag = new Tag(Type.LONG, null, lastPriceCheck);
		Tag pricesTag = new Tag(Type.BYTE_ARRAY, null, prices);
		Tag allowedPurchaseTag = new Tag(Type.LONG, null, permissionToPurchase);
		Tag allowedTradeTag = new Tag(Type.LONG, null, permissionToTrade);
		Tag creditsTag = new Tag(Type.LONG, null, credits);
		
		ObjectOpenHashSet<String> ow = new ObjectOpenHashSet<String>(getOwnerPlayers());
		if(FactionManager.isNPCFaction(shop.getFactionId())){
			ow.removeAll(((GameStateInterface)getState()).getGameState().getNPCShopOwnersDebugSet());
		}
		Tag ownersTag = Tag.listToTagStruct(ow, Type.STRING, null);
		Tag infiniteSupplyTag = new Tag(Type.BYTE, null, infiniteSupply ? (byte) 1 : (byte) 0);
		
		assert(!shop.isNPCHomeBase() || isTradeNode()):shop+". NPC homebase should be active trade node";
		Tag tradeNodeTag = new Tag(Type.BYTE, null, isTradeNode() ? (byte) 1 : (byte) 0);
		return new Tag(Type.STRUCT, null, new Tag[]{
				new Tag(Type.BYTE, null, VERSION),
				basePriceTag,
				lastPriceCheckTag,
				pricesTag,
				allowedPurchaseTag,
				creditsTag,
				ownersTag,
				infiniteSupplyTag,
				tradeNodeTag,
				allowedTradeTag,
				FinishTag.INST});
	}

	private void fromOldTag(Tag tag) {
		Tag[] v = tag.getStruct();
		fromTagStructure(v[0], v[1], v[2]);
		if(v[3].getType() == Type.BYTE){
			final byte ALL = 0;
			final byte NEUTRAL = 1;
			final byte ALLIES = 2;
			final byte FACTION = 3;
			final byte val = (Byte) v[3].getValue();
			permissionToPurchase = TradeManager.PERM_ALL;
			switch(val) {
				case (ALL) -> permissionToPurchase = TradeManager.PERM_ALL;
				case (NEUTRAL) -> permissionToPurchase = TradeManager.PERM_ALL_BUT_ENEMY;
				case (ALLIES) -> permissionToPurchase = TradeManager.PERM_ALLIES_AND_FACTION;
				case (FACTION) -> permissionToPurchase = TradeManager.PERM_FACTION;
			}
		}else{
			permissionToPurchase = (Long) v[3].getValue();
		}
		credits = v[4].getType() == Type.LONG ? (Long) v[4].getValue() : ((Integer) v[4].getValue()).longValue();
		
		ObjectArrayList<String> l = new ObjectArrayList<String>();
		Tag.listFromTagStruct(l, (Tag[]) v[5].getValue());
		for(int i = 0; i < l.size(); i++){
			getOwnerPlayers().add(l.get(i).toLowerCase(Locale.ENGLISH));
		}
		if (v[6].getType() != Type.FINISH) {
			infiniteSupply = (Byte) v[6].getValue() == 1 ? true : false;
			if (infiniteSupply) {
				System.err.println("[SERVER][TAG] set infinite supply on shop " + this);
			}
		}
		if (v.length > 7 && v[7].getType() != Type.FINISH) {
			setTradeNodeOn(((Byte) v[7].getValue() == (byte)1 ));
			if (isTradeNodeOn()) {
				System.err.println("[SERVER][TAG] set trade node on shop " + this);
			}
		}
		if (v.length > 8 && v[8].getType() != Type.FINISH) {
			permissionToTrade = (Long) v[8].getValue();
		}
		
	}

	public void fromTagStructure(Tag basePriceTag, Tag lastCheckTag, Tag pricesTag) {

		basePriceMult = (Double) basePriceTag.getValue();
		lastPriceCheck = (Long) lastCheckTag.getValue();

		Tag[] pTags = (Tag[]) pricesTag.getValue();
		for (int i = 0; i < pTags.length - 1; i++) {
			Tag[] v = (Tag[]) pTags[i].getValue();

			short type;
			OldPrice price;
			if (v[1].getType() == Type.INT) {
				//version0
				type = (Short) v[0].getValue();
				price = new OldPrice(type, (short) -1, (Integer) v[1].getValue(), (short) -1, (Integer) v[1].getValue());
				this.oldVersionClearFlag = true;
			} else {
				Tag[] ptag = (Tag[]) v[1].getValue();

				if(ptag.length > 5){
					//version3
					type = (Short) v[0].getValue();
					price = new OldPrice(type, (Short) ptag[0].getValue(), (Integer) ptag[1].getValue(), (Short) ptag[2].getValue(), (Integer) ptag[3].getValue());
					price.buyDownTo = (Integer)ptag[4].getValue();
					price.sellUpTo = (Integer)ptag[5].getValue();


					
					//this is a sell price!
					TradePrice sell = new TradePrice(type, 0, price.amountBuy, price.buyDownTo, true);
					
					//this is a buy price
					TradePrice buy = new TradePrice(type, 0, price.amountSell, price.sellUpTo, true);
					
					if(price.priceTypeBuy == -1){
						putPrice(sell);
					}
					if(price.priceTypeSell == -1){
						putPrice(buy);
					}
					
					
				}else{
					if (ptag[2].getType() == Type.SHORT) {
						//version2
						type = (Short) v[0].getValue();
						price = new OldPrice(type, (Short) ptag[0].getValue(), (Integer) ptag[1].getValue(), (Short) ptag[2].getValue(), (Integer) ptag[3].getValue());
						this.oldVersionClearFlag = true;
					} else {
						//version1
						type = (Short) v[0].getValue();
						price = new OldPrice(type, (Short) ptag[0].getValue(), (Integer) ptag[1].getValue(), (Short) ptag[0].getValue(), (Integer) ptag[1].getValue());
						this.oldVersionClearFlag = true;
					}
				}

			}
		}
		
	}

	/**
	 * @return the basePriceMult
	 */
	public double getBasePriceMult() {
		return basePriceMult;
	}

	/**
	 * @param basePriceMult the basePriceMult to set
	 */
	public void setBasePriceMult(double basePriceMult) {
		this.basePriceMult = basePriceMult;
	}

	/**
	 * @return the credits
	 */
	public long getCredits() {
		return credits;
	}

	/**
	 * @param credits the credits to set
	 */
	public void setCredits(long credits) {
		this.credits = credits;
	}

	/**
	 * @return the ownerPlayers
	 */
	public Set<String> getOwnerPlayers() {
		if(ownerPlayers.isEmpty() && FactionManager.isNPCFaction(shop.getFactionId())){
			ownerPlayers.add(("#NPC_"+shop.getFactionId()).toLowerCase(Locale.ENGLISH));
			ownerPlayers.addAll(((GameStateInterface)getState()).getGameState().getNPCShopOwnersDebugSet());
		}
		return ownerPlayers;
	}

	/**
	 * @return the permissionToPurchase
	 */
	public long getPermissionToPurchase() {
		return permissionToPurchase;
	}

	/**
	 * @param permissionToPurchase the permissionToPurchase to set
	 */
	public void setPermissionToPurchase(long permissionToPurchase) {
		this.permissionToPurchase = permissionToPurchase;
	}
	private boolean checkedDataBaseNode;
	private ObjectArrayFIFOQueue<TradePriceSingle> priceModifyBuffer = new ObjectArrayFIFOQueue<TradePriceSingle>();
	private ObjectArrayFIFOQueue<ShopOption> optionBuffer = new ObjectArrayFIFOQueue<ShopOption>();
	private ObjectArrayFIFOQueue<TradePrices> pricesBuffer = new ObjectArrayFIFOQueue<TradePrices>();
	public static final byte VERSION = 0;
	

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		flagForcedUpdate = active != this.active;
		this.active = active;
	}

	public boolean isInShopDistance(ShopperInterface other) {
		if (other.getSectorId() != shop.getSectorId()) {
			return false;
		}
		other.getTransformedAABB(oMin, oMax, 0, new Vector3f(), new Vector3f(), null);
		if (shop.getSegmentBuffer().isEmpty()) {
			oMax.sub(oMin);
			oMax.scale(0.5f);
			oMin.add(oMax);
			oMin.sub(shop.getWorldTransform().origin);

			return oMin.length() < SHOP_DISTANCE;
		}
		Vector3f min = new Vector3f(shop.getSegmentBuffer().getBoundingBox().min);
		Vector3f max = new Vector3f(shop.getSegmentBuffer().getBoundingBox().max);

		if (!GlUtil.checkAABB(min, max)) {
			shop.getSegmentBuffer().restructBB();
			return false;
		}
		AabbUtil2.transformAabb(min, max, SHOP_DISTANCE, shop.getWorldTransform(), sMin, sMax);

		return AabbUtil2.testAabbAgainstAabb2(sMin, sMax, oMin, oMax);
	}

	public void modCredits(long i) {

		long c = credits + i;

		if (c < 0) {
			c = 0;
		} else if (c > Integer.MAX_VALUE) {
			c = Integer.MAX_VALUE;
		}

		credits =  c;
	}

	public void onSectorInactiveClient() {
//		System.err.println("INACTIVE FOR SHOP: "+shop+"; "+shop.getSectorId());
		synchronized (shop.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : shop.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof ShopperInterface) {

					boolean add = isInShopDistance((ShopperInterface) s) && ((ShopperInterface) s).getSectorId() == shop.getSectorId();

//					System.err.println("[SECTORCHANGE] "+(add ? "add" : "remove")+" "+ shop+" "+(add ? "to" : "from")+" shops in distance of "+s+"; "+shop.getSectorId()+"; "+((ShopperInterface)s).getSectorId());

					if (add) {
						((ShopperInterface) s).getShopsInDistance().add(shop);
					} else {
						((ShopperInterface) s).getShopsInDistance().remove(shop);

					}
				}
			}
		}
	}
	private void handleOptions(){
		if (!optionBuffer.isEmpty()) {
			synchronized (optionBuffer) {
				while (!optionBuffer.isEmpty()) {
					ShopOption p = optionBuffer.dequeue();
					executeShopOption(p);
				}
			}
		}
	}
	private void handlePrices(){
		if (!pricesBuffer.isEmpty()) {
			synchronized (pricesBuffer) {
				while (!pricesBuffer.isEmpty()) {
					TradePrices p = pricesBuffer.dequeue();
					//System.err.println("RECEIVED PRICES :::: "+shop.getSegmentController().getName()+": "+p.getBuyAndSellNumbers());
					executePricesUpdate(p);
				}
			}
		}
	}
	private void executePricesUpdate(TradePrices p) {
		putAllPrices(p.getPrices());
		if(!isOnServer()){
			tradeNodeSettingChangedClient();
		}
	}

	private void executeShopOption(ShopOption p) {
		PlayerState player = null;
		if(isOnServer()){
			try {
				player = ((GameServerState) shop.getState()).getPlayerFromStateId(p.senderId);
			} catch (PlayerNotFountException e) {
				e.printStackTrace();
			}
			//check permission
			if (player == null ||
					isAIShop() ||
					(getOwnerPlayers().size() > 0 && !getOwnerPlayers().contains(player.getName().toLowerCase(Locale.ENGLISH)))) {
				if (player != null) {
					player.sendServerMessage(new ServerMessage(Lng.astr("SERVER: permission denied!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
				}
				return;
			}
		}
		
		switch(p.type){
		case LOCAL_PERMISSION:
			System.err.println(getState()+" Executed local permission "+shop+": "+p.permission);
			permissionToPurchase = p.permission;
			break;
		case TRADE_PERMISSION:
			System.err.println(getState()+" Executed trade permission "+shop+": "+p.permission);
			permissionToTrade = p.permission;
			break;
		case USER_ADD:
			getOwnerPlayers().add(p.playerName.toLowerCase(Locale.ENGLISH));
//			System.err.println("[SHOP] "+getState()+" "+this+" HAS ADDED OWNER "+p.playerName+" -> "+getOwnerPlayers());
			break;
		case USER_REMOVE:
			getOwnerPlayers().remove(p.playerName.toLowerCase(Locale.ENGLISH));
//			System.err.println("[SHOP] "+getState()+" "+this+" HAS REMOVED OWNER "+p.playerName+" -> "+getOwnerPlayers());
			break;
		case CREDIT_WITHDRAWAL:
			if(isOnServer()){
				if (credits >= Math.abs(p.credits)) {
					player.modCreditsServer(Math.abs(p.credits));
					modCredits(-Math.abs(p.credits));
				} else {
					player.sendServerMessage(new ServerMessage(Lng.astr("Shop doesn't have enough credits!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
				}
			}
			break;
		case CREDIT_DEPOSIT:
			if(isOnServer()){
				if (player.getCredits() >= Math.abs(p.credits)) {
					player.modCreditsServer(-Math.abs(p.credits));
					modCredits(Math.abs(p.credits));
				} else {
					player.sendServerMessage(new ServerMessage(Lng.astr("You don't have enough credits!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
				}
			}
			break;
		default:
			break;
		
		}
		
		if (shop.isOnServer()) {
			//deligate
			shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(p, shop.isOnServer()));
		}else{
			notifyClientPriceChange();
			tradeNodeSettingChangedClient();
		}
	}
	private void executePriceMod(TradePriceSingle p) {
		TradePricePair pair = priceList.get(p.getType());
		
		if(pair == null){
			pair = new TradePricePair();
			priceList.put(p.getType(), pair);
		}
		if(p.isBuy()){
			pair.buy = new PriceRep(p);
		}else{
			pair.sell = new PriceRep(p);
		}
		
		
		
		if (shop.isOnServer()) {
			//deligate
			shop.getNetworkObject().getPriceModifyBuffer().add(new RemoteTradePriceSingle(p, shop.isOnServer()));
		}else{
			notifyClientPriceChange();
			tradeNodeSettingChangedClient();
		}
	}
	private void handlePriceMods(){
		if (!priceModifyBuffer.isEmpty()) {
			synchronized (priceModifyBuffer) {
				while (!priceModifyBuffer.isEmpty()) {
					TradePriceSingle p = priceModifyBuffer.dequeue();
					executePriceMod(p);
					

					
				}
			}
		}
	}
	

	public void receivePrices(boolean init) {
		infiniteSupply = shop.getNetworkObject().getInfiniteSupply().get();
		
		
		
		if (!shop.isOnServer()) {
			boolean tradeNodeBef = tradeNodeOn; //dont use getter as it might assert false for client receiving for the first time
			setTradeNodeOn(shop.getNetworkObject().getTradeNodeOn().getBoolean());
			if(!init && isTradeNodeOn() != tradeNodeBef){
				tradeNodeSettingChangedClient();
			}
			long credsBef = credits;
			credits = shop.getNetworkObject().getShopCredits().getLong();
			
			if((credsBef != credits)){
				notifyClientPriceChange();
			}
		}else{
			
			//ON SERVER
			ByteArrayList receiveBuffer = shop.getNetworkObject().getTradeNodeOnRequest().getReceiveBuffer();
			
			for( byte b  : receiveBuffer){
				boolean sta = b == (byte)1;
				if(sta != isTradeNodeOn()){
					setTradeNodeOn(sta);
					
					tradeNodeSettingChangedServer();
				}
			}
		}
		for (int i = 0; i < shop.getNetworkObject().getPricesUpdateBuffer().getReceiveBuffer().size(); i++) {
			TradePrices p = shop.getNetworkObject().getPricesUpdateBuffer().getReceiveBuffer().get(i).get();
			synchronized (pricesBuffer) {
				pricesBuffer.enqueue(p);
			}
		}
		for (int i = 0; i < shop.getNetworkObject().getShopOptionBuffer().getReceiveBuffer().size(); i++) {
			ShopOption p = shop.getNetworkObject().getShopOptionBuffer().getReceiveBuffer().get(i).get();
//			try {
//				throw new Exception("[SHOP] " + shop.getSegmentController() + " " + shop.getState() + " received option: " + p);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.err.println("[SHOP] " + shop.getSegmentController() + " " + shop.getState() + " received option: " + p);
			synchronized (optionBuffer) {
				optionBuffer.enqueue(p);
			}
		}
		for (int i = 0; i < shop.getNetworkObject().getPriceModifyBuffer().getReceiveBuffer().size(); i++) {
			TradePriceSingle p = shop.getNetworkObject().getPriceModifyBuffer().getReceiveBuffer().get(i).get();
			System.err.println("[SHOP] " + shop.getSegmentController() + " " + shop.getState() + " received price mod: " + p);
			synchronized (priceModifyBuffer) {
				priceModifyBuffer.enqueue(p);
			}
		}
	
	}

	private void notifyClientPriceChange(){
		assert(!isOnServer());
		ClientChannel clientChannel = ((GameClientState)shop.getState()).getController().getClientChannel();
		if(clientChannel != null){
			TradeNodeClient tradeNodeStub = 
					(TradeNodeClient)clientChannel.getGalaxyManagerClient()
					.getTradeNodeDataById().get(shop.getSegmentController().getDbId());
			if(tradeNodeStub != null){
				tradeNodeStub.priceChangeListener.onChanged();
			}
		}
	}
	public void tradeNodeSettingChangedServer() {
		assert(isOnServer());
		Vector3i system = shop.getSegmentController().getSystem(new Vector3i());
		if(system != null){
			if(!isTradeNodeOn()){
				((GameServerState)shop.getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().removeTradeNode(shop.getSegmentController().dbId);
			}else{
				try {
					((GameServerState)shop.getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().insertOrUpdateTradeNode(shop.getTradeNode());
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			((GameServerState)shop.getState()).getUniverse().tradeNodesDirty.enqueue(shop.getSegmentController().getDbId());
		}else{
			System.err.println("[SHOP] Error: System could not be retrieved");
		}
	}
	public void tradeNodeSettingChangedClient() {
		assert(!isOnServer());
		
	}

	public void reset() {
		System.err.println("[SHOPADDON] resetting shop " + shop);
		getOwnerPlayers().clear();
		permissionToPurchase = TradeManager.PERM_ALL;
		credits = 0;

		if (shop.isOnServer()) {
			IntOpenHashSet c = new IntOpenHashSet();
			shop.getShopInventory().clear(c);
			shop.sendInventoryModification(c, Long.MIN_VALUE);

			priceList.clear();
			updateServerPrices(true, false);
		}
		flagForcedUpdate = true;
	}
	@Override
	public void deserialize(DataInput inputStream, int updateSenderStateId, boolean onServer) throws IOException {
		deserialize(this, isOnServer(), inputStream, updateSenderStateId);
	}
	public static void deserialize(ShoppingAddOn addOn, boolean onServer, DataInput inputStream, int updateSenderStateId) throws IOException {

		TradePrices p = new TradePrices(100);
		p.deserialize(inputStream, updateSenderStateId, onServer);
		List<TradePriceInterface> prices = p.getPrices();
		addOn.putAllPrices(prices);
		
	}
	public void putAllPrices(List<TradePriceInterface> prices){
		for(TradePriceInterface m : prices){
			putPrice(m);
		}
	}
	
	public void buy(PlayerState player, short buyItem, int quantity, ShopInterface shop, IntOpenHashSet invMod, IntOpenHashSet shopHash) throws NoSlotFreeException {
		assert(shop.getShopInventory().checkVolume());
		boolean ok = true;

		
		
		int overallQuantity = shop.getShopInventory().getOverallQuantity(buyItem);
		if (overallQuantity < quantity && !shop.getShoppingAddOn().infiniteSupply) {
			quantity = Math.min(overallQuantity, quantity);
			player.sendServerMessage(new ServerMessage(Lng.astr("Shop only had %s of this item",  overallQuantity), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
		}
		int toBuy = quantity;
		
		if(!(player.getInventory().canPutIn(buyItem, toBuy))){
			player.sendServerMessage(new ServerMessage(Lng.astr("Inventory full!"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
			ok = false;
		}else{
			if (!(shop.isAiShop()) && (shop.getShopOwners().isEmpty() || shop.getShopOwners().contains(player.getName().toLowerCase(Locale.ENGLISH)))) {
				//dont let the player pay
				ok = true;
			} else {
				TradePriceInterface price = getPrice(buyItem, false);
				if(price == null){
					player.sendServerMessage(new ServerMessage(Lng.astr("Is not trading this item"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
					return;
				}
				toBuy = canAfford(player, buyItem, quantity);
				
				if (player.getCredits() >= toBuy * price.getPrice()) {
					player.modCreditsServer(-toBuy * price.getPrice());
					shop.modCredits(toBuy * price.getPrice());
				} else {
					ok = false;
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

			TradePriceInterface price = getPrice(sellItem, true);
			if(price == null){
				player.sendServerMessage(new ServerMessage(Lng.astr("Is not trading this item"), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
				return;
			}
			
			int cost = toSell * price.getPrice();
			if (shop.getCredits() < toSell * price.getPrice()) {
				player.sendServerMessage(new ServerMessage(Lng.astr("Shop cannot pay you (left: %s Credits)", shop.getCredits()), ServerMessage.MESSAGE_TYPE_ERROR, player.getId()));
				ok = false;
			} else {
				player.modCreditsServer(cost);
				shop.modCredits(-cost);
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
	
	private void putPrice(TradePriceInterface m){
		TradePricePair tradePricePair = priceList.get(m.getType());
		if(tradePricePair == null){
			tradePricePair = new TradePricePair();
			priceList.put(m.getType(), tradePricePair);
		}
		if(m.isBuy()){
			tradePricePair.buy = new PriceRep(m);
		}else{
			tradePricePair.sell = new PriceRep(m);
		}
	}
	
	@Override
	public void serialize(DataOutput outputStream, boolean onServer) throws IOException {
			
		TradePrices p = getTradePrices();
		p.serialize(outputStream, onServer);
	}

	public boolean isOnServer() {
		return shop.isOnServer();
	}
	public void requestTradeNodeStateOnClient(boolean on){
		shop.getNetworkObject().getTradeNodeOnRequest().add(on ? (byte) 1 : (byte) 0);
	}
	public void sendPrices() {
		
		TradePrices tp = getTradePrices();
		//FIXME dont send initally empty 
//		System.err.println("SEND PRICES:::::: "+shop.getSegmentController()+" "+tp.getBuyAndSellNumbers());
		
		RemoteTradePrice p = new RemoteTradePrice(tp, isOnServer());
		shop.getNetworkObject().getPricesUpdateBuffer().add(p);
	}

	public void update(long time) {
		if(oldVersionClearFlag){
			assert(isOnServer());
			//clearing all prices, because saved data is pre-trading
			priceList.clear();
			updateServerPrices(true, false);
			assert(isAIShop() || priceList.isEmpty());
			oldVersionClearFlag = false;
			
		}
		
		
		if(!checkedDataBaseNode){
			
			if(shop.isNPCHomeBase()){
				setTradeNodeOn(true);
			}
			if(isOnServer() && isTradeNodeOn()){
				
				long credits = ((GameServerState)getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradeNodeCredits(shop.getSegmentController().getDbId());
				
				if(credits >= 0){
					this.credits = credits;
				}
				
				try {
					TradeNodeStub tradeNode = ((GameServerState)getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradeNode(shop.getSegmentController().getDbId());
					if(tradeNode != null && this.permissionToTrade != tradeNode.getTradePermission()){
						this.permissionToTrade = tradeNode.getTradePermission();
						ShopOption p = new ShopOption();
						p.type = ShopOptionType.TRADE_PERMISSION;
						p.permission = this.permissionToTrade;
						shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(p, shop.isOnServer()));
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
				DataInputStream tpStream = ((GameServerState)getState()).getDatabaseIndex().getTableManager().getTradeNodeTable().getTradePricesAsStream(shop.getSegmentController().getDbId());
				
				if(tpStream != null){
					try {
						TradePrices pc = ShoppingAddOn.deserializeTradePrices(tpStream, true);
						tpStream.close();
						List<TradePriceInterface> prices = pc.getPrices();
						IntOpenHashSet changed = new IntOpenHashSet();
						for(TradePriceInterface p : prices){
							int overallQuantity = shop.getShopInventory().getOverallQuantity(p.getType());
							int current = p.getAmount();
//							if(shop.getSegmentController() instanceof SpaceStation){
//								System.err.println("PRICE ::: "+current+" ::: "+overallQuantity);
//							}
							if(current != overallQuantity){
								shop.getShopInventory().deleteAllSlotsWithType(p.getType(), changed);
								shop.getShopInventory().putNextFreeSlotWithoutException(p.getType(), current, -1);
								System.err.println("[SERVER][SHOP] applied changed inventory from shopping: "+ElementKeyMap.toString(p.getType())+" "+overallQuantity+" -> "+current);
							}
						}
						if(changed.size() > 0){
							shop.getShopInventory().sendInventoryModification(changed);
						}
						if(FactionManager.isNPCFaction(shop.getFactionId())){
							priceList.clear();
						}
						putAllPrices(prices);
						sendAllPrices();
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else if(!isOnServer() && isTradeNodeOn()){
				TradeNodeStub tradeNodeStub = ((GameClientState)getState()).getController().getClientChannel().getGalaxyManagerClient()
				.getTradeNodeDataById().get(shop.getSegmentController().getDbId());
				if(tradeNodeStub != null){
					((TradeNodeClient)tradeNodeStub).priceChangeListener.onChanged();
				}
			}
			
			checkedDataBaseNode = true;
		}
		handlePriceMods();
		handleOptions();
		handlePrices();

		if (shop.isOnServer()) {
			shop.getNetworkObject().getShopCredits().set(credits);
			shop.getNetworkObject().getInfiniteSupply().set(infiniteSupply);
			shop.getNetworkObject().getTradeNodeOn().set(isTradeNodeOn());
		}

		if (active || flagForcedUpdate) {
			checkShopDistance(time, flagForcedUpdate);

			if (shop.isOnServer() && (GameServerState.updateAllShopPricesFlag ||
					time > lastPriceCheck + SHOP_PRICE_CHECK_PERIOD)) {
				long t = System.currentTimeMillis();
				if(isAIShop()){
					updateServerPrices(true, false);
				}
				long took = System.currentTimeMillis() - t;
				if (took > 3) {
					System.err.println("[SERVER] updating prices for: " + this + " took " + took);
				}
				if (isAIShop()) {
					if (credits < Integer.MAX_VALUE - 10000) {
						credits += ServerConfig.SHOP_NPC_RECHARGE_CREDITS.getInt();
					}
				}

				SHOP_PRICE_CHECK_PERIOD = (long) (5 * 60000 + (Math.random() * 10) * 60000);
				lastPriceCheck = time;

			}
			flagForcedUpdate = false;
		}
	}
	public StateInterface getState(){
		return shop.getState();
	}
	public void updateToFullNT() {
		if (shop.isOnServer()) {
			shop.getNetworkObject().getInfiniteSupply().set(infiniteSupply);
			shop.getNetworkObject().getTradeNodeOn().set(isTradeNodeOn());
			shop.getNetworkObject().getTradePermission().set(permissionToTrade);
			shop.getNetworkObject().getLocalPermission().set(permissionToPurchase);
			for (String owner : getOwnerPlayers()) {
				
				ShopOption t = new ShopOption();
				t.type = ShopOptionType.USER_ADD;
				t.playerName = owner;
				
				shop.getNetworkObject().getShopOptionBuffer().add(new RemoteShopOption(t, shop.isOnServer()));
			}

			sendAllPrices();
		}
	}

	void updateServerPrices(boolean send, boolean missingOnly) {
		assert (shop.isOnServer());
		for (short infoId : ElementKeyMap.typeList()) {
			ElementInformation info = ElementKeyMap.getInfo(infoId);
			if (isAIShop()) {
				//initial add for all
				//continous update only for npc shops
				int calculatePrice = calculatePrice(info);
				
				int amount = shop.getShopInventory().getOverallQuantity(infoId);
				TradePrice sell = new TradePrice(infoId, amount, calculatePrice, -1, true); 
				TradePrice buy = new TradePrice(infoId, amount, calculatePrice, -1, false); 
				
				TradePricePair tradePricePair = priceList.get(infoId);
				if(missingOnly){
					if(tradePricePair == null){
						tradePricePair = new TradePricePair();
						priceList.put(infoId, tradePricePair);
					}
					if(!missingOnly || tradePricePair.buy == null){
						tradePricePair.buy = new PriceRep(buy);
					}
					if(!missingOnly || tradePricePair.sell == null){
						tradePricePair.sell = new PriceRep(sell);
					}
				}
			}
		}
		if (send) {
			sendAllPrices();
		}
	}

	private void sendAllPrices() {
		assert (isOnServer());
		sendPrices();
	}

	/**
	 * @return the infiniteSupply
	 */
	public boolean isInfiniteSupply() {
		return infiniteSupply;
	}

	/**
	 * @param infiniteSupply the infiniteSupply to set
	 */
	public void setInfiniteSupply(boolean infiniteSupply) {
		this.infiniteSupply = infiniteSupply;
	}

	public void cleanUp() {

		synchronized (shop.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
			for (Sendable s : shop.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof ShopperInterface) {
					((ShopperInterface) s).getShopsInDistance().remove(shop);
				}
			}
		}
	}

	public boolean isTradeNode() {
		return isTradeNodeOn();
	}

	public TradePrices getTradePrices() {
		TradePrices p = new TradePrices(priceList.size()*2);
		p.entDbId = shop.getSegmentController().getDbId();
		for(Entry<Short, TradePricePair> e : priceList.entrySet()){
			TradePricePair value = e.getValue();
			if(value.buy != null){
				p.addBuy(value.buy.getType(), value.buy.getAmount(), value.buy.getPrice(), value.buy.getLimit());
			}
			if(value.sell != null){
				p.addSell(value.sell.getType(), value.sell.getAmount(), value.sell.getPrice(), value.sell.getLimit());
			}
		}
		return p;
	}
	public List<TradePriceInterface> getPricesRep(){
		List<TradePriceInterface> r = new ObjectArrayList<TradePriceInterface>();
		
		for(TradePricePair p : priceList.values()){
			if(p.buy != null){
				r.add(p.buy);
			}
			if(p.sell != null){
				r.add(p.sell);
			}
		}
		
		return r;
	}
	public class PriceRep implements TradePriceInterface{
		final TradePriceInterface p;
		
		public PriceRep(TradePriceInterface p) {
			super();
			assert(p != null);
			this.p = p;
		}
		@Override
		public short getType() {
			return p.getType();
		}
		@Override
		public int getAmount() {
			return shop.getShopInventory().getOverallQuantity(getType());
		}
		@Override
		public int getPrice() {
			return p.getPrice();
		}
		@Override
		public boolean isSell() {
			return p.isSell();
		}
		@Override
		public int hashCode() {
			return (isSell() ? 4000 : 1) * getType();
		}
		@Override
		public boolean equals(Object obj) {
			return ((TradePriceInterface)obj).isSell() == isSell() && 
					((TradePriceInterface)obj).getType() == getType() &&
					getPrice() == ((TradePriceInterface)obj).getPrice() && 
					getLimit() == ((TradePriceInterface)obj).getLimit();
		}
		@Override
		public int getLimit() {
			return p.getLimit();
		}
		@Override
		public ElementInformation getInfo() {
			return ElementKeyMap.getInfoFast(getType());
		}
		@Override
		public void setAmount(int amount) {
			IntOpenHashSet h = new IntOpenHashSet();
			
			shop.getShopInventory().deleteAllSlotsWithType(getType(), h);
			shop.getShopInventory().incExistingOrNextFreeSlot(getType(), amount, -1);
			shop.getShopInventory().sendInventoryModification(h);
			p.setAmount(amount);
		}
		@Override
		public boolean isBuy() {
			return p.isBuy();
		}
		@Override
		public String toString() {
			return "PriceRep "+(isBuy() ? "BUY" : "SELL")+"[type: "+getType()+"; Price: "+getPrice()+"; Stock: "+getAmount()+"]";
		}
		
		
	}
	public static TradePrices deserializeTradePrices(DataInputStream inputStream, boolean onServer) throws IOException {
		
		TradePrices prices;
		
		final SegmentSerializationBuffersGZIP bb = SegmentSerializationBuffersGZIP.get();
		
		
		int size = inputStream.readInt();
		int deflatedSize = inputStream.readInt();
		
		final byte[] buffer = bb.SEGMENT_BUFFER;
		final byte[] byteArrayStream = bb.getStaticArray(size);
		final Inflater inflater = bb.inflater;
		try {

			assert (deflatedSize <= buffer.length) : deflatedSize + "/" + buffer.length;
			int read = inputStream.read(buffer, 0, deflatedSize);

			assert (read == deflatedSize) : read + "/" + deflatedSize;
			inflater.reset();

			inflater.setInput(buffer, 0, deflatedSize);

			int inflate;
			
			inflate = inflater.inflate(byteArrayStream, 0, size);

			assert (inflate == size) : inflate + " / " + size;

			DataInputStream ds = new DataInputStream(new FastByteArrayInputStream(byteArrayStream, 0, size));
			
			prices = new TradePrices(deflatedSize / (2*4*4*4));
			prices.deserialize(ds, 0, onServer);
			
			ds.close();
			if (inflate == 0) {
				System.err.println("[PRICES] WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
			}

		} catch (DataFormatException e) {
			e.printStackTrace();
			throw new IOException(e);
		} catch (IOException e) {
			throw e;
		}finally {
			SegmentSerializationBuffersGZIP.free(bb);
		}

		return prices;
	}
	public void serializeTradePrices(DataOutputStream outputStream) throws IOException {
		serializeTradePrices(outputStream, isOnServer(), getTradePrices(), shop.getSegmentController().getDbId());
	}
	public static void serializeTradePrices(DataOutputStream outputStream, boolean onServer, TradePrices tradePrices, long dbId) throws IOException {
		int zipSize = 0;
//		byte[] buffer;
//		Deflater deflater;
//		if (onServer) {
//			buffer = GameServerState.SEGMENT_DEFLATE_BUFFER;
//			deflater = GameServerState.deflater;
//		} else {
//			buffer = GameClientState.SEGMENT_DEFLATE_BUFFER;
//			deflater = GameClientState.deflater;
//		}
		
		final SegmentSerializationBuffersGZIP b = SegmentSerializationBuffersGZIP.get();
		try {
			byte[] buffer = b.SEGMENT_BUFFER;
			FastByteArrayOutputStream byteArrayStream = b.getStaticArrayOutput();
			DataOutputStream ds = new DataOutputStream(byteArrayStream);
			
			tradePrices.entDbId = dbId;
			tradePrices.serialize(ds, onServer);
			b.deflater.reset();
			b.deflater.setInput(byteArrayStream.array, 0, (int) byteArrayStream.position());
			b.deflater.finish();
			zipSize = b.deflater.deflate(buffer);
			
			outputStream.writeInt(tradePrices.byteCount);
			outputStream.writeInt(zipSize);
			
			outputStream.write(buffer, 0, zipSize);
		}finally{
			SegmentSerializationBuffersGZIP.free(b);
		}
	}

	public boolean isTradeNodeOn() {
		assert(!isOnServer() || !shop.isNPCHomeBase() || tradeNodeOn ):shop;
//		if(shop.getSegmentController().getUniqueIdentifier().contains("NPC-HOMEBASE")){
//			assert(false):tradeNodeOn+"; "+shop.isNPCHomeBase();
//		}
		return tradeNodeOn;
	}

	public void setTradeNodeOn(boolean tradeNodeOn) {
		assert(!isOnServer() || !shop.isNPCHomeBase() || tradeNodeOn):shop+" tried to deactive NPC trade node";
		this.tradeNodeOn = tradeNodeOn;
	}

	public long getPermissionToTrade() {
		return permissionToTrade;
	}

	public void setPermissionToTrade(long permissionToTrade) {
		this.permissionToTrade = permissionToTrade;
	}

	public static boolean isPurchasePermission(PlayerState p,
			ShopInterface c) {
		return c.getShoppingAddOn().hasPermission(p);
	}

	public TradePriceInterface getPrice(short type, boolean buy) {
		TradePricePair tradePricePair = priceList.get(type);
		if(tradePricePair != null){
			if(buy){
				return tradePricePair.buy;
			}else{
				return tradePricePair.sell;
			}
		}
		return null;
	}

	public void initFromNetwokObject(NetworkObject from) {
		receivePrices(true);
		permissionToTrade =	shop.getNetworkObject().getTradePermission().getLong();
		permissionToPurchase = shop.getNetworkObject().getLocalPermission().getLong();
	}

//	public void modifyPriceServer(TradePriceInterface pr) {
//		synchronized (priceModifyBuffer) {
//			priceModifyBuffer.enqueue(pr);
//		}
//	}


}
