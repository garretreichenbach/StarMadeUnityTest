package org.schema.game.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.server.ServerProcessor;

public class StarMadeServerStats implements SerializationInterface{

	public long totalMemory;
	public long freeMemory;
	public long takenMemory;
	public int totalPackagesQueued;
	public int lastAllocatedSegmentData;
	public int playerCount;
	public long ping;


	public static StarMadeServerStats create(GameServerState s) {
		StarMadeServerStats c = new StarMadeServerStats();
		long totalMemory = (Runtime.getRuntime().totalMemory());
		long freeMemory = (Runtime.getRuntime().freeMemory());
		long takenMemory = totalMemory - freeMemory;
		
		c.totalMemory = totalMemory;
		c.freeMemory = freeMemory;
		c.takenMemory = takenMemory;
		c.totalPackagesQueued = ServerProcessor.totalPackagesQueued;
		c.lastAllocatedSegmentData = GameServerState.lastAllocatedSegmentData;
		c.playerCount = s.getPlayerStatesByName().size();
		
		return c;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(this.totalMemory);
		b.writeLong(this.freeMemory);
		b.writeLong(this.takenMemory);
		b.writeLong(this.totalPackagesQueued);
		b.writeLong(this.lastAllocatedSegmentData);
		b.writeLong(this.playerCount);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		this.totalMemory = b.readLong();
		this.freeMemory = b.readLong();
		this.takenMemory = b.readLong();

		this.totalPackagesQueued = b.readInt();
		this.lastAllocatedSegmentData = b.readInt();
		this.playerCount = b.readInt();		
	}

}
