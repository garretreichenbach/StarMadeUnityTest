package org.schema.game.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Header {

	public static final byte packetByte = 42;
	public static final byte pingByte = 23;
	public static final byte testByte = 100;
	public static final byte logoutByte = 65;
	public static final byte TYPE_PARAMETRIZED_COMMAND = 111;
	public static final byte TYPE_STREAM_COMMAND = 123;
	public short packetId;
	byte commandType;
	private byte commandId;
	private byte type;

	public Header() {
		super();
	}

	public Header(byte commandId, short packetId, byte type) {
		super();
		this.commandId = (commandId);
		this.type = (type);
		this.packetId = packetId;
	}

	/**
	 * @return the commandId
	 */
	public byte getCommandId() {
		return commandId;
	}

	/**
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	public void read(DataInputStream inputStream) throws IOException {
		//signature 42 has already been read
		this.packetId = inputStream.readShort();
		this.commandId = (inputStream.readByte()); //  command ID
		this.type = (inputStream.readByte());
	}

	@Override
	public String toString() {
		return "\n||commandId: " + this.commandId + "; \n" +
				"||type: " + this.type + "; \n" +
				"||packetId: #" + this.packetId;
	}

	public void write(DataOutputStream outputStream) throws IOException {
		outputStream.write(packetByte);                //  signature
		outputStream.writeShort(packetId);
		outputStream.writeByte(commandId);        //  command ID
		outputStream.writeByte(type);                //  type

	}

//	public void writeToArray(byte[] array){
//		assert(array.length >= 5);
//		array[0] = packetByte; //0
//		ByteUtil.shortWriteByteArray(packetId, array, 1);//1,2
//		array[3] = getCommandId();
//		array[4] = getType();
//	}

}
