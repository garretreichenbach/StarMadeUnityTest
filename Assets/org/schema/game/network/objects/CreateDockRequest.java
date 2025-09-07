package org.schema.game.network.objects;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.data.VoidUniqueSegmentPiece;

public class CreateDockRequest {
	public VoidUniqueSegmentPiece core;
	public VoidUniqueSegmentPiece docker;
	public VoidUniqueSegmentPiece rail;
	public String name;
	
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		core.serialize(buffer);
		docker.serialize(buffer);
		rail.serialize(buffer);
		buffer.writeUTF(name);
	}
	public void deserialize(DataInputStream stream, int updateSenderStateId,
			boolean onServer) throws IOException {
		core = new VoidUniqueSegmentPiece();
		core.deserialize(stream);
		
		docker = new VoidUniqueSegmentPiece();
		docker.deserialize(stream);
		
		rail = new VoidUniqueSegmentPiece();
		rail.deserialize(stream);
		
		name = stream.readUTF();
	}
	
	
}
