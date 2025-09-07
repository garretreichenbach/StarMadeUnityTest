package org.schema.game.server.data.simulation.npc.geo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.schema.common.SerializationInterface;
import org.schema.common.util.LogInterface;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContainer.NPCEntity;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCEntityContingent implements SerializationInterface, LogInterface{
	
	private static final byte VERSION = 0;
	public final List<NPCEntitySpecification> entities = new ObjectArrayList<NPCEntitySpecification>();
	
	public final NPCEntityContainer spawnedEntities;
	public final NPCSystemStub system;
	public final ElementCountMap contingentValueServer = new ElementCountMap();
	private double minerScore = -1;
	private float consumed = 0;;
	
	public class NPCEntitySpecification implements SerializationInterface, Comparable<NPCEntitySpecification>{
		public String bbName;
		public EntityType type;
		public BlueprintClassification c;
		public int count;
		public float mass;
		@Override
		public void serialize(DataOutput b, boolean isOnServer)
				throws IOException {
			assert(bbName != null);
			b.writeUTF(bbName);
			b.writeByte(type.ordinal());
			b.writeByte(c.ordinal());
			b.writeInt(count);
			b.writeFloat(mass);
		}
		@Override
		public void deserialize(DataInput b, int updateSenderStateId,
				boolean isOnServer) throws IOException {
			bbName = b.readUTF();
			type = EntityType.values()[b.readByte()];
			c = BlueprintClassification.values()[b.readByte()];
			count = b.readInt();
			mass = b.readFloat();
		}
		public boolean hasLeft() {
			return getLeft() > 0;
		}
		public int getLeft() {
			return count - getSpawned(this);
		}
		@Override
		public String toString() {
			return "NPCEntitySpecification [bbName=" + bbName + ", type="
					+ type + ", c=" + c + ", count=" + count + ", mass=" + mass
					+ "]";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((bbName == null) ? 0 : bbName.hashCode());
			result = prime * result + ((c == null) ? 0 : c.hashCode());
			result = prime * result + count;
			result = prime * result + Float.floatToIntBits(mass);
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof NPCEntitySpecification)) {
				return false;
			}
			NPCEntitySpecification other = (NPCEntitySpecification) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (bbName == null) {
				if (other.bbName != null) {
					return false;
				}
			} else if (!bbName.equals(other.bbName)) {
				return false;
			}
			if (c != other.c) {
				return false;
			}
			if (count != other.count) {
				return false;
			}
			if (Float.floatToIntBits(mass) != Float.floatToIntBits(other.mass)) {
				return false;
			}
			if (type != other.type) {
				return false;
			}
			return true;
		}
		private NPCEntityContingent getOuterType() {
			return NPCEntityContingent.this;
		}
		@Override
		public int compareTo(NPCEntitySpecification o) {
			int bn = bbName.compareTo(o.bbName);
			if(bn == 0){
				int tp = c.name().compareTo(o.c.name());
				if(tp == 0){
					return count - o.count;
				}
				return tp;
			}
			return bn;
		}
		
	}
	
	public NPCEntityContingent(NPCSystemStub system) {
		super();
		spawnedEntities = new NPCEntityContainer(this);
		this.system = system;
	}

	public int getSpawned(NPCEntitySpecification npcEntitySpecification) {
		return getSpawned(npcEntitySpecification.bbName);
	}
	public int getSpawned(String bbName) {
		return spawnedEntities.getSpanwed(bbName);
	}
	public void spawn(NPCEntitySpecification spec, long dbId){
		spawnedEntities.spawn(spec, dbId);
		((NPCSystem)system).setChangedNT();
	}
	public NPCEntitySpecification getMainStation(){
		NPCEntitySpecification d = null;
		float maxMass = -1;
		assert(!entities.isEmpty());
		for(NPCEntitySpecification s : entities){
			if(s.hasLeft() && s.type == EntityType.SPACE_STATION && s.mass > maxMass){
				d = s;
			}
		}
		assert(d != null);
		return d;
	}
	public NPCEntitySpecification getStation(long seed, Random r) {
		r.setSeed(seed);
		List<NPCEntitySpecification> randList = new ObjectArrayList<NPCEntitySpecification>(entities);
		Collections.shuffle(randList, r);
		NPCEntitySpecification d = null;
		for(NPCEntitySpecification s : randList){
			if(s.hasLeft() && s.type == EntityType.SPACE_STATION){
				d = s;
				break;
			}
		}
		return d;
	}
	public List<NPCEntitySpecification> getShips(Fleet fleet, BlueprintClassification ... classes) {
		List<NPCEntitySpecification> list = new ObjectArrayList<NPCEntitySpecification>(entities.size());
		NPCEntitySpecification d = null;
		
		for(NPCEntitySpecification s : entities){
			if(s.type == EntityType.SHIP){
				if(classes == null || classes.length == 0){
					list.add(s);
				}else{
					for(int i = 0; i < classes.length; i++){
						BlueprintClassification c = classes[(fleet.getMembers().size()+i)%classes.length];
						if(s.c == c){
							list.add(s);
							break;
						}
					}
				}
			}
		}
		if(classes == null || classes.length == 0){
			Collections.shuffle(list);
		}
		return list;
	}
	public void add(String name, EntityType type, float mass, BlueprintClassification c, int count, ElementCountMap elementCountMap){
		NPCEntitySpecification s = new NPCEntitySpecification();
		s.bbName = name;
		s.type = type;
		s.c = c;
		s.mass = mass;
		s.count = count;
		
		entities.add(s);
		
		contingentValueServer.add(elementCountMap);
		
		
		if(isOnServer()){
			minerScore = -1;
		}
	}
	
	@Override
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		for(NPCEntitySpecification e : entities){
			b.append(e.bbName +"; "+e.type.name()+"; "+e.c.name()+" x "+e.count+"; Spawned("+spawnedEntities.getSpanwed(e.bbName)+")\n");
		}
		
		b.append("SPAWNED: \n"+spawnedEntities.toString());
		return b.toString();
	}


	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeInt(entities.size());
		for(NPCEntitySpecification s : entities){
			s.serialize(b, isOnServer);
		}
		spawnedEntities.serialize(b);
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		entities.clear();
		int size = b.readInt();
		for(int i = 0; i < size; i++){
			NPCEntitySpecification e = new NPCEntitySpecification();
			e.deserialize(b, updateSenderStateId, isOnServer);
			entities.add(e);
		}
		
		spawnedEntities.deserialize(b);
	}


	public int getTotalAmountClass(BlueprintClassification ... c) {
		int amount = 0;
		for(NPCEntitySpecification e : entities){
			for(BlueprintClassification s : c){
				if(e.c == s){
					amount += e.count;
					break;
				}
			}
		}
		return amount;
	}
	
	public Tag toTag(){
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, VERSION),
			spawnedEntities.toTag(),
			new Tag(Type.FLOAT, null, consumed),
			
			FinishTag.INST}
		);
	}
	public void fromTag(Tag tag){
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
		spawnedEntities.fromTag(t[1]);
		consumed = t[2].getFloat();
	}

	@Override
	public void log(String m, LogLevel lvl) {
		system.log(m, lvl);
	}

	public void clearContignet() {
		entities.clear();
	}

	public NPCEntitySpecification getSpec(String mainStationBB) {
		for(NPCEntitySpecification e : entities){
			if(e.bbName.toLowerCase(Locale.ENGLISH).equals(mainStationBB.toLowerCase(Locale.ENGLISH))){
				return e;
			}
		}
		return null;
	}

	public NPCEntity killRandomSpawned(){
		assert(isOnServer());
		Iterator<NPCEntity> iterator = spawnedEntities.getSpanwed().iterator();
		if(iterator.hasNext()){
			NPCEntity next = iterator.next();
			if(next.entityId != ((NPCSystem)system).getFaction().getTradeNode().getEntityDBId()){
				((NPCSystem)system).state.destroyEntity(next.entityId);
				return next;
			}else{
				System.err.println("[SERVER][NPC][KILLRANDOM] not killing homebase");
			}
		}
		return null;
	}

	private boolean isOnServer() {
		return system instanceof NPCSystem;
	}

	public void despawnAllSpawned(NPCSystem system) {
		spawnedEntities.onRemovedCleanUp(system);
	}

	public void killRandomNonSpanwed() {
		spawnedEntities.loseRandomResources();
	}

	public double getMinerScore() {
		if(isOnServer() && minerScore < 0){
			double mScoreBef = minerScore;
			minerScore = recalcMinerScore();
			if(mScoreBef != this.minerScore){
				((NPCSystem)system).setChangedNT();
			}
		}
		return minerScore;
	}

	private double recalcMinerScore() {
		assert(isOnServer());
		double minerScore = 0;
		BluePrintController bbc = ((NPCSystem)system).getFaction().getConfig().getPreset().blueprintController;
		
		minerScore += ((NPCSystem)system).getFaction().getConfig().basicMiningScorePerSystem;
		
		for(NPCEntitySpecification sp : entities){
			if(sp.c == BlueprintClassification.MINING){
				try {
					BlueprintEntry blueprint = bbc.getBlueprint(sp.bbName);
					double support = 0;
					if(blueprint.getScore() != null) {
						support = blueprint.getScore().miningIndex;
					}else {
						System.err.println("[NPCFACTION] ERROR: no score attached for blueprint: "+blueprint);
						log("ERROR: no score attached for blueprint: "+blueprint, LogLevel.ERROR);
					}
					
					minerScore += support * sp.count;
				} catch (EntityNotFountException e) {
					e.printStackTrace();
				}
			}else if(sp.c == BlueprintClassification.MINING_STATION){
				minerScore += ((NPCSystem)system).getFaction().getConfig().miningStationMiningScore;
			}
		}
		
		return minerScore;
	}

	public void resupply(NPCSystem system, long time) {
		spawnedEntities.resupply(system, time);
	}

	public void consume(NPCSystem system, long time) {
		
		
		if(consumed < 1f){
			BluePrintController bbc = system.getFaction().getConfig().getPreset().blueprintController;
			IntOpenHashSet ss = new IntOpenHashSet();
			ElementCountMap rm = new ElementCountMap();
			for(NPCEntitySpecification sp : entities){
				try {
					double fac = 
							system.getFaction().getConfig().resourcesConsumeStep *
							system.distanceFactor * 
							system.getFaction().getConfig().resourcesConsumedPerDistance;
					BlueprintEntry blueprint = bbc.getBlueprint(sp.bbName);
					
					ElementCountMap elementCountMapWith = blueprint.getElementCountMapWithChilds();
					
					for(short type : ElementKeyMap.typeList()){
						
						int consumed = (int)((double)elementCountMapWith.get(type) * (double)sp.count * fac);
						
						rm.inc(type, consumed);
						
						
					}
					
				} catch (EntityNotFountException e) {
					e.printStackTrace();
				}
			}
			if(!rm.isEmpty()){
				system.getFaction().getInventory().decreaseBatchIgnoreAmount(rm, ss);
//					int available = system.getFaction().getInventory().getOverallQuantity(type);
//					system.getFaction().getInventory().decreaseBatch(type, consumed, ss);
//					consumed = Math.min(available, consumed);
			}
			
			if(ss.size() > 0){
				system.getFaction().getInventory().sendInventoryModification(ss);
			}
			consumed += system.getFaction().getConfig().resourcesConsumeStep;
			
			log("CONSUME: consumed: "+rm.getTotalAmount()+" Blocks (maintenance); consumed status: "+consumed+" / "+1, LogLevel.DEBUG);
		}
	}

	public int getTotalAmount() {
		int count = 0;
		for(NPCEntitySpecification c : entities){
			count += c.count;
		}
		return count;
	}

	public boolean isEqualTo(List<NPCEntitySpecification> oldCont) {
		if(oldCont.size() != entities.size()){
			return false;
		}
		Collections.sort(oldCont);
		Collections.sort(entities);
		
		for(int i = 0; i < oldCont.size(); i++){
			if(oldCont.get(i).hashCode() != entities.hashCode() || !entities.equals(oldCont.get(i))){
				return false;
			}
		}
		return true;
	}
	
}
