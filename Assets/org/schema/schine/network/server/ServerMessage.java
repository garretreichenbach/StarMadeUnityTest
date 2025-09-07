package org.schema.schine.network.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.schema.common.SerializationInterface;
import org.schema.schine.network.common.commands.Command;

public class ServerMessage implements SerializationInterface{
	public static final byte MESSAGE_TYPE_SIMPLE = 0;
	public static final byte MESSAGE_TYPE_INFO = 1;
	public static final byte MESSAGE_TYPE_WARNING = 2;
	public static final byte MESSAGE_TYPE_ERROR = 3;
	public static final byte MESSAGE_TYPE_ERROR_BLOCK = 4;
	public static final byte MESSAGE_TYPE_DIALOG = 5;
	private Object[] message;
	public byte type;
	public int receiverPlayerId;
	public String prefix;
	public boolean adminOnly;
	public long block;

	public ServerMessage() {
		
	}
	public ServerMessage(Object[] message, byte type) {
		super();
		this.message = message;
		this.type = type;
		this.receiverPlayerId = 0;
	}

	public ServerMessage(Object[] message, byte type, boolean adminOnly) {
		this(message, type);
		this.adminOnly = adminOnly;
	}

	public ServerMessage(Object[] message, byte type, int receiver) {
		super();
		this.message = message;
		this.type = type;
		this.receiverPlayerId = receiver;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[SERVERMSG (type " + type + "): " + Arrays.toString(message) + "]";
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeByte(type);
		if(type == MESSAGE_TYPE_ERROR_BLOCK){
			b.writeLong(block);
		}
		b.writeInt(receiverPlayerId);
		Command.serialize(message, b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		this.type = b.readByte();
		if(type == MESSAGE_TYPE_ERROR_BLOCK){
			block = b.readLong();
		}
		this.receiverPlayerId = b.readInt();
		message = Command.deserialize(b);
	}
	public Object[] getMessage() {
		return message;
	}
	public void setMessage(Object[] message) {
		this.message = message;
	}

}
