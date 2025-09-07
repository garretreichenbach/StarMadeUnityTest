package org.schema.game.common.controller.activities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class RaceModification implements SerializationInterface{

	public enum RacemodType{
		TYPE_ENTER,
		TYPE_LEFT,
		TYPE_FINISHED,
		TYPE_ENTRANT_GATE,
		TYPE_ENTRANT_FOREFEIT,
		TYPE_RACE_START, TYPE_CREATE_RACE,
		;
	}
	public int raceId;
	public RacemodType type;
	public int entrantId;
	public int gate;
	public long timeAtGate;
	public long raceStart;
	public int createRaceSegContId;
	public long createRaceBlockIndex;
	public String createRaceName;
	public int laps;
	public int buyIn;
	public String creatorName;
	
	
	
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeByte(type.ordinal());
		b.writeInt(raceId);
		
		switch(type){
		case TYPE_ENTER:
			assert(entrantId != 0);
			b.writeInt(entrantId);
			break;
		case TYPE_ENTRANT_FOREFEIT:
			assert(entrantId != 0);
			b.writeInt(entrantId);
			break;
		case TYPE_ENTRANT_GATE:
			assert(entrantId != 0);
			b.writeInt(entrantId);
			b.writeInt(gate);
			b.writeLong(timeAtGate);
			break;
		case TYPE_FINISHED:
			break;
		case TYPE_LEFT:
			assert(entrantId != 0);
			b.writeInt(entrantId);
			break;
		case TYPE_RACE_START:
			b.writeLong(raceStart);
			break;
		case TYPE_CREATE_RACE:
		{
			b.writeUTF(createRaceName);
			b.writeUTF(creatorName);
			b.writeInt(createRaceSegContId);
			b.writeInt(laps);
			b.writeInt(buyIn);
			b.writeLong(createRaceBlockIndex);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		this.type = RacemodType.values()[b.readByte()];
		this.raceId = b.readInt();
		switch(type){
		case TYPE_ENTER:
			entrantId = b.readInt();
			break;
		case TYPE_ENTRANT_FOREFEIT:
			entrantId = b.readInt();
			break;
		case TYPE_ENTRANT_GATE:
			entrantId = b.readInt();
			gate = b.readInt();
			timeAtGate = b.readLong();
			break;
		case TYPE_FINISHED:
			break;
		case TYPE_LEFT:
			entrantId = b.readInt();
			break;
		case TYPE_RACE_START:
			raceStart = b.readLong();
			break;
			
		case TYPE_CREATE_RACE:
		{
			createRaceName = b.readUTF();
			creatorName = b.readUTF();
			createRaceSegContId = b.readInt();
			laps = b.readInt();
			buyIn = b.readInt();
			createRaceBlockIndex = b.readLong();
			break;
		}
		default:
			break;
		
		}
	}

	@Override
	public String toString() {
		return "RaceModification [raceId=" + raceId + ", type=" + type
				+ ", entrantId=" + entrantId + ", gate=" + gate
				+ ", timeAtGate=" + timeAtGate + ", raceStart=" + raceStart
				+ ", createRaceSegContId=" + createRaceSegContId
				+ ", createRaceBlockIndex=" + createRaceBlockIndex
				+ ", createRaceName=" + createRaceName + "]";
	}

}
