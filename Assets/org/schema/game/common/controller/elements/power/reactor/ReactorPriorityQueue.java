package org.schema.game.common.controller.elements.power.reactor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.schema.common.SerializationInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer.PowerConsumerCategory;
import org.schema.game.network.objects.PowerInterfaceNetworkObject;
import org.schema.game.network.objects.remote.RemoteReactorPriorityQueue;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ReactorPriorityQueue extends Observable implements SerializationInterface, Comparator<PowerConsumer>{
	private final List<PowerConsumerCategory> queue;
	private final Object2IntOpenHashMap<PowerConsumerCategory> prioMap = new Object2IntOpenHashMap<PowerConsumerCategory>();
	private final Object2IntOpenHashMap<PowerConsumerCategory> amountMap = new Object2IntOpenHashMap<PowerConsumerCategory>();
	private final Object2DoubleOpenHashMap<PowerConsumerCategory> consumptionMap = new Object2DoubleOpenHashMap<PowerConsumerCategory>();
	private final Object2DoubleOpenHashMap<PowerConsumerCategory> percentMap = new Object2DoubleOpenHashMap<PowerConsumerCategory>();
	private boolean changed;
	private boolean received;
	private float totalPowered;
	private float totalAmount;
	private final PowerInterface pi;
	public ReactorPriorityQueue(PowerInterface pi) {
		super();
		this.pi = pi;
		ObjectArrayList<PowerConsumerCategory> q = new ObjectArrayList<PowerConsumerCategory>(PowerConsumerCategory.values());
		q.trim();
		queue = q;
	}
	public List<PowerConsumerCategory> getQueue(){
		return queue;
	}
	public int getAmount(PowerConsumerCategory f) {
		return amountMap.getInt(f);
	}
	public double getConsumption(PowerConsumerCategory f) {
		return consumptionMap.getDouble(f);
	}
	public double getConsumptionPercent(PowerConsumerCategory f) {
		return consumptionMap.getDouble(f) / pi.getRechargeRatePowerPerSec();
	}
	public double getPercent(PowerConsumerCategory f, double defaultRet) {
		if(!amountMap.containsKey(f)){
			return defaultRet;
		}
		double p = getAmount(f);
		if(p == 0){
			return 0;
		}
		return percentMap.getDouble(f) / p;
	}
	public double getPercent(PowerConsumerCategory f) {
		return getPercent(f, 1d);
	}
	
	public void resetStats(){
		percentMap.clear();
		amountMap.clear();
		consumptionMap.clear();
	}
	
	public void addPercent(PowerConsumerCategory f, double v){
		percentMap.addTo(f, v);
	}
	public void addConsumption(PowerConsumerCategory f, double v){
		consumptionMap.addTo(f, v);
	}
	public void addAmount(PowerConsumerCategory f){
		amountMap.addTo(f, 1);
	}
	public float getTotalPercent() {
		return totalAmount == 0 ? 0f : totalPowered / totalAmount; 
	}
	public void addTotalPercent(float powered) {
		totalPowered += powered;
		totalAmount ++;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		boolean defaultOrder = isDefaultOrder();
		b.writeBoolean(defaultOrder);
		if(!defaultOrder){
			for(int i = 0; i < queue.size(); i++){
				PowerConsumerCategory p = queue.get(i);
				b.writeByte((byte)p.ordinal());
			}
		}
	}
	
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		boolean defaultOrder = b.readBoolean();
		PowerConsumerCategory[] values = PowerConsumerCategory.values();
		if(defaultOrder){
			queue.clear();
			for(int i = 0; i < values.length; i++){
				queue.add(values[i]);
			}
		}else{
			queue.clear();
			for(int i = 0; i < values.length; i++){
				queue.add(values[b.readByte()]);
			}
		}
		flagChanged();
	}
	private boolean isDefaultOrder() {
		PowerConsumerCategory[] values = PowerConsumerCategory.values();
		for(int i = 0; i < queue.size(); i++){
			if(queue.get(i) != values[i]){
				return false;
			}
		}
		return true;
	}
	
	public Tag toTagStructure(){
		if(isDefaultOrder()){
			return new Tag(Type.BYTE, null, (byte)1);
		}
		Tag[] t = new Tag[queue.size()+1];
		t[t.length-1] = FinishTag.INST;
		for(int i = 0; i < queue.size(); i++){
			t[i] = new Tag(Type.BYTE, null, (byte)queue.get(i).ordinal());
		}
		return new Tag(Type.STRUCT, null, t);
	}
	public void fromTagStructure(Tag tag){
		if(tag.getType() == Type.STRUCT){
			//otherwise it's default order (which is already set)
			Tag[] t = tag.getStruct();
			final PowerConsumerCategory[] values = PowerConsumerCategory.values();
			Set<PowerConsumerCategory> s = new ObjectOpenHashSet<PowerConsumerCategory>(values);
			queue.clear();
			for(int i = 0; i < t.length-1; i++){
				byte b = t[i].getByte();
				if(b < values.length){
					queue.add(values[b]);
					s.remove(values[b]);
				}
			}
			//add rest (in case there are new ones added between versions)
			queue.addAll(s);
			
			flagChanged();
		}
	}
	private void flagChanged() {
		this.changed = true;
	}
	@Override
	public boolean hasChanged() {
		return changed;
	}
	public void updateLocal(Timer t, PowerInterface pw){
		if(received){
			if(pw.isOnServer()){
				//deligate to clients
				send(pw.getNetworkObject());
			}
			received = false;
			setChanged();
			notifyObservers();
		}
		
		if(changed){
			prioMap.clear();
			for(int i = 0; i < queue.size(); i++){
				prioMap.put(queue.get(i), i);
			}
			pw.flagConsumersChanged();
			changed = false;
			setChanged();
			notifyObservers();
//			System.err.println((pw.isOnServer() ? "SERVER" : "CLIENT")+" CHANGE GOTTEN ::: "+queue);
		}
	}
	public void receive(PowerInterfaceNetworkObject n){
		ObjectArrayList<RemoteReactorPriorityQueue> r = n.getReactorPrioQueueBuffer().getReceiveBuffer();
		if(r.size() > 0){
			received = true;
		}
	}
	public void send(PowerInterfaceNetworkObject n){
		//doesn't need receiving function as this is directed and applied on received
		n.getReactorPrioQueueBuffer().add(new RemoteReactorPriorityQueue(this, n.getReactorPrioQueueBuffer().onServer));
	}
	public void move(PowerInterface pw, PowerConsumerCategory f, int dir) {
		int i = queue.indexOf(f);
		final int old = i; 
		int am = Math.abs(dir);
		int d = dir > 0 ? 1 : -1;
		while(am > 0 && ( d > 0 || i > 0) && (i < queue.size()-1 || d < 0)){
			i += d;
			am--;
		}
		moveAbs(pw, f, i);
		
	}
	public int getPriority(PowerConsumerCategory p) {
		return prioMap.getInt(p);
	}
	public void moveAbs(PowerInterface pw, PowerConsumerCategory f, int to) {
		queue.remove(f);
		queue.add(to, f);
		flagChanged();
		send(pw.getNetworkObject());
	}
	@Override
	public int compare(PowerConsumer o1, PowerConsumer o2) {
		return getPriority(o1.getPowerConsumerCategory())- getPriority(o2.getPowerConsumerCategory());
	}
	
}
