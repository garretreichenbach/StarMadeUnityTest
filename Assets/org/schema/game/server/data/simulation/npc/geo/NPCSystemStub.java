package org.schema.game.server.data.simulation.npc.geo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class NPCSystemStub implements SerializationInterface, LogInterface{
	int id = -1;
	private final static byte VERSION = 0;

	public long minedResources;
	
	protected long seed;
	
	public Vector3i system;
	
	public Vector3i systemBase;
	public short totalAsteroidSectors;
	public short totalPlanetSectors;

	protected int lastContingentMaxLevel;
	protected float lastContingentTotalLevelFill;
	
	public float distanceFactor = -1;
	
	public long takenTime;

	public long lastUpdate;

	public long lastApplied = -1;

	public String systemBaseUID;
	
	public boolean abandoned;
	
	public short spawedStations = 1;
	
	public final StarSystemResourceRequestContainer resources = new StarSystemResourceRequestContainer();

	
	public float resourcesAvailable = 1f;

	public final Short2ObjectOpenHashMap<String> stationMap = new Short2ObjectOpenHashMap<String>();

	public boolean markedChangedContingent;
	private final NPCEntityContingent contingent;
	
	public double status = 1;
	
	public NPCSystemStub() {
		super();
		contingent = new NPCEntityContingent(this);
	}
	/**
	 * WARNING: only for client use
	 */
	private int clientFactionId;
	private float clientWeight;
	private short clientLevel;
	
	/** HP if you will (between 0 and 1) **/
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		
		
		b.writeBoolean(abandoned);
		if(abandoned){
			system.serialize(b);
			return;
		}
		
		Tag t = toTagStructure();
		assert(t.getType() == Type.STRUCT);
		
		t.serializeNT(b);
		
		contingent.serialize(b, isOnServer);
		b.writeDouble(status);
		
//		b.writeBoolean(lostResources != null);
//		if(lostResources != null){
//			lostResources.serialize(b);
//		}
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		
		abandoned = b.readBoolean();
		if(abandoned){
			system = Vector3i.deserializeStatic(b);
			return;
		}
		
		Tag readFromZipped = Tag.deserializeNT(b);
		fromTagStructure(readFromZipped, getLevel());
		
		
		contingent.deserialize(b, updateSenderStateId, isOnServer);
		status = b.readDouble();
//		boolean lr = b.readBoolean();
//		if(lr){
//			lostResources = new ElementCountMap();
//			lostResources.deserialize(b);
//		}else{
//			lostResources = null;
//		}
	}
	public Tag toTagStructure() {
		
		return new Tag(Type.STRUCT, null,
		new Tag[]{
		new Tag(Type.BYTE, null, VERSION),	
		new Tag(Type.INT, null, id),	
		new Tag(Type.SHORT, null, getLevel()),	
		new Tag(Type.INT, null, getFactionId()),	
		new Tag(Type.VECTOR3i, null, systemBase),	
		new Tag(Type.STRING, null, systemBaseUID),	
		new Tag(Type.FLOAT, null, distanceFactor),
		new Tag(Type.INT, null, lastContingentMaxLevel),	
		new Tag(Type.FLOAT, null, lastContingentTotalLevelFill),	
		
		new Tag(Type.LONG, null, takenTime),
		new Tag(Type.LONG, null, lastUpdate),
		new Tag(Type.LONG, null, lastApplied),
		new Tag(Type.VECTOR3i, null, system),
		new Tag(Type.BYTE_ARRAY, null, resources.res),
		new Tag(Type.FLOAT, null, resourcesAvailable),
		contingent.toTag(),
		new Tag(Type.BYTE, null, markedChangedContingent ? (byte) 1 : (byte) 0),
		new Tag(Type.FLOAT, null, getWeight()),
		Tag.short2StringMapToTagStruct(stationMap),
		new Tag(Type.SHORT, null, totalAsteroidSectors),	
		new Tag(Type.SHORT, null, totalPlanetSectors),	
		new Tag(Type.SHORT, null, spawedStations),	
		new Tag(Type.LONG, null, minedResources),	
		(this instanceof NPCSystem ? ((NPCSystem)this).getFleetManager().toTag() : new Tag(Type.BYTE, null, (byte)0)),
		
		
		FinishTag.INST}
		);
	}

	public void fromTagStructure(Tag tag, int level) {
		assert(tag != null);
		assert(tag.getType() == Type.STRUCT);
		Tag[] t = tag.getStruct();
		assert(t != null);
		byte version = t[0].getByte();
		id = t[1].getInt();
		clientLevel = t[2].getShort();
		clientFactionId = t[3].getInt();
		systemBase = t[4].getVector3i();
		systemBaseUID = t[5].getString();
		distanceFactor = t[6].getFloat();
		lastContingentMaxLevel = t[7].getInt();
		lastContingentTotalLevelFill = t[8].getFloat();
		
		takenTime = t[9].getLong();
		lastUpdate = t[10].getLong();
		lastApplied = t[11].getLong();
		system = t[12].getVector3i();
		seed = calcSeed();
		assert(t[13].getByteArray() != null);
		assert(resources != null);
		assert(resources.res != null);
		System.arraycopy(t[13].getByteArray(), 0, resources.res, 0, resources.res.length);
		resourcesAvailable = t[14].getFloat();
		
		//racalculate here for
		if(this instanceof NPCSystem){
			((NPCSystem)this).calculateContingent(level, lastContingentMaxLevel, lastContingentTotalLevelFill);
		}
		
		contingent.fromTag(t[15]);
		markedChangedContingent = t[16].getByte() == (byte)1;
		clientWeight = t[17].getFloat();
		Tag.fromShort2StringMapToTagStruct(t[18], stationMap);
		totalAsteroidSectors = t[19].getShort();
		totalPlanetSectors = t[20].getShort();
		spawedStations = t[21].getShort();
		minedResources = t[22].getLong();

		if(this instanceof NPCSystem){
			((NPCSystem)this).getFleetManager().fromTag(t[23]);
		}
		
	}
	protected long calcSeed(){
		return system.hashCode() + getFactionId() * getLevel();
	}
	public int getFactionId(){
		return clientFactionId;
	}

	public NPCEntityContingent getContingent() {
		return contingent;
	}

	public float getWeight() {
		return clientWeight;
	}
	public short getLevel() {
		return clientLevel;
	}
	@Override
	public void log(String m, LogLevel lvl) {
	}
	
}
