package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.ShoppingAddOn;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class TradePrices implements SerializationInterface{
	private final ShortArrayList types;
	private final IntArrayList amounts;
	private final IntArrayList prices;
	private final IntArrayList limits;
	public long entDbId = Long.MIN_VALUE;
	public int byteCount;
	
	public List<TradePriceInterface> cachedPrices;
	private int cacheDate = Integer.MIN_VALUE;
	public Short2ObjectOpenHashMap<TradePriceInterface> cachedBuyPrices;
	public Short2ObjectOpenHashMap<TradePriceInterface> cachedSellPrices;
	
	public TradePrices(int expected){
		types = new ShortArrayList(expected);
		amounts = new IntArrayList(expected);
		prices = new IntArrayList(expected);
		limits = new IntArrayList(expected);
	}
	
	public String getBuyAndSellNumbers(){
		List<TradePriceInterface> prices = getPrices();
		int buy = 0;
		int sell = 0;
		for(TradePriceInterface p : prices){
			if(p.isBuy()){
				buy++;
			}else{
				sell++;
			}
		}
		return "BUY COUNT: "+buy+"; SELL COUNT: "+sell;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		assert(entDbId != Long.MIN_VALUE);
		b.writeLong(entDbId);
		b.writeInt(types.size());
		byteCount = (8+4);
		for(int i = 0; i < types.size(); i++){
			b.writeShort(types.get(i));
			b.writeInt(amounts.get(i));
			b.writeInt(prices.get(i));
			b.writeInt(limits.get(i));
			
			byteCount += (2 + 4 + 4 + 4);
		}
	}
	public byte[] getPricesBytesCompressed(boolean onServer) throws IOException {
		FastByteArrayOutputStream b = new FastByteArrayOutputStream(10*1024);
			
		assert(entDbId != Long.MIN_VALUE);
		ShoppingAddOn.serializeTradePrices(new DataOutputStream(b), onServer, this, entDbId);
		
		byte[] bytes = new byte[(int)b.length()];
		
		System.arraycopy(b.array, 0, bytes, 0, bytes.length);
		
		
		
		return bytes;
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		entDbId = b.readLong();
		int size = b.readInt();
		for(int i = 0; i < size; i++){
			types.add(b.readShort());
			amounts.add(b.readInt());
			prices.add(b.readInt());
			limits.add(b.readInt());
		}
	}
	
	public void addBuy(short type, int amount, int price, int limit){
		types.add((short)-type);
		amounts.add(amount);
		prices.add(price);
		limits.add(limit);
	}
	public void addSell(short type, int amount, int price, int limit){
		types.add(type);
		amounts.add(amount);
		prices.add(price);
		limits.add(limit);
	}
	public void changeOrAdd(boolean buy, short type, int amount, int price, int limit){
		int i;
		if(buy){
			i = types.indexOf((short)-type);
		}else{
			i = types.indexOf(type);
		}
		
		if(i >= 0){
			amounts.set(i, amount);
			prices.set(i, price);
			limits.set(i, limit);
		}else{
			if(buy){
				addBuy(type, amount, price, limit);
			}else{
				addSell(type, amount, price, limit);
			}
		}
	}
	public List<TradePriceInterface> getCachedPrices(int cacheDate){
		if(cacheDate != this.cacheDate || cachedPrices == null){
			cachedPrices = getPrices();
			cachedBuyPrices = new Short2ObjectOpenHashMap<TradePriceInterface>();
			cachedSellPrices = new Short2ObjectOpenHashMap<TradePriceInterface>();
			for(TradePriceInterface p : cachedPrices){
				if(p.isBuy()){
					cachedBuyPrices.put(p.getType(), p);
				}else{
					cachedSellPrices.put(p.getType(), p);
				}
			}
			
			this.cacheDate = cacheDate; 
		}
		return cachedPrices;
	}
	public void purgeCache(){
		cachedPrices = null;
		cachedBuyPrices = null;
		cachedSellPrices = null;
		this.cacheDate = Integer.MIN_VALUE;
	}
	public TradePriceInterface getCachedSellPrice(int cacheDate, short type){
		return getCachedPrice(cacheDate, false, type);
	}
	public TradePriceInterface getCachedBuyPrice(int cacheDate, short type){
		return getCachedPrice(cacheDate, true, type);
	}
	public TradePriceInterface getCachedPrice(int cacheDate, boolean buy, short type){
		List<TradePriceInterface> pp = getCachedPrices(cacheDate);
		if(buy){
			return cachedBuyPrices.get(type);
		}else{
			return cachedSellPrices.get(type);
		}
	}
	public List<TradePriceInterface> getPrices(){
		ObjectArrayList<TradePriceInterface> pr = new ObjectArrayList<TradePriceInterface>(types.size());
		
		for(int i = 0; i < types.size(); i++){
			TradePrice p = new TradePrice((short)Math.abs(types.get(i)), amounts.get(i), prices.get(i), limits.get(i), types.get(i) > 0);
			pr.add(p);
		}
		
		return pr;
	}
	public static TradePrices getFromPrices(List<TradePriceInterface> l, long dbId){
		TradePrices p = new TradePrices(l.size());
		p.entDbId = dbId;
		for(TradePriceInterface e : l){
			if(e.isSell()){
				p.addSell(e.getType(), e.getAmount(), e.getPrice(), e.getLimit());
			}else{
				p.addBuy(e.getType(), e.getAmount(), e.getPrice(), e.getLimit());
			}
		}
		
		return p;
	}
	public TradePrices switchBuyAndSell() {
		List<TradePriceInterface> pp = getPrices();
		
		TradePrices oth = new TradePrices(pp.size());
		oth.entDbId = entDbId;
		
		for(TradePriceInterface p : pp){
			if(p.isBuy()){
				oth.addSell(p.getType(), p.getAmount(), p.getPrice(), p.getLimit());
			}else{
				oth.addBuy(p.getType(), p.getAmount(), p.getPrice(), p.getLimit());
			}
		}
		return oth;
	}
}
