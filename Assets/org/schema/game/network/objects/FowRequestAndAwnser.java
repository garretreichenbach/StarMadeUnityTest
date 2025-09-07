package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.game.client.controller.ClientChannel;

public class FowRequestAndAwnser implements SerializationInterface{

	public int sysX;
	public int sysY;
	public int sysZ;
	public boolean visible = false;
	public ClientChannel receivedClientChannel;

	@Override
	public void deserialize(DataInput b, int id, boolean onServer) throws IOException {
		if (onServer) {
			sysX = b.readInt();
			sysY = b.readInt();
			sysZ = b.readInt();
		} else {
			sysX = b.readInt();
			sysY = b.readInt();
			sysZ = b.readInt();
			visible = b.readBoolean();
		}
	}

	@Override
	public void serialize(DataOutput b, boolean onServer) throws IOException {
		if (onServer) {
			b.writeInt(sysX);
			b.writeInt(sysY);
			b.writeInt(sysZ);
			b.writeBoolean(visible);
		} else {
			b.writeInt(sysX);
			b.writeInt(sysY);
			b.writeInt(sysZ);
		}
	}


}
