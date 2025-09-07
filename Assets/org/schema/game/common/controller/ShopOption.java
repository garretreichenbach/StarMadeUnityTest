package org.schema.game.common.controller;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class ShopOption implements SerializationInterface{

	public enum ShopOptionType{
		USER_ADD,
		USER_REMOVE,
		LOCAL_PERMISSION,
		TRADE_PERMISSION,
		CREDIT_WITHDRAWAL,
		CREDIT_DEPOSIT,
	}
	
	public String playerName;
	public int senderId;
	public long permission;
	public long credits;
	public ShopOptionType type;
	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeByte(type.ordinal());
		switch(type) {
			case LOCAL_PERMISSION, TRADE_PERMISSION -> b.writeLong(permission);
			case CREDIT_WITHDRAWAL, CREDIT_DEPOSIT -> b.writeLong(credits);
			case USER_ADD, USER_REMOVE -> b.writeUTF(playerName);
			default -> {
			}
		}
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		type = ShopOptionType.values()[b.readByte()];
		switch(type) {
			case LOCAL_PERMISSION, TRADE_PERMISSION -> permission = b.readLong();
			case CREDIT_WITHDRAWAL, CREDIT_DEPOSIT -> credits = b.readLong();
			case USER_ADD, USER_REMOVE -> playerName = b.readUTF();
			default -> {
			}
		}
		senderId = updateSenderStateId;
	}

	@Override
	public String toString() {
		return "ShopOption [senderId=" + senderId + ", type=" + type
				+ ", playerName=" + playerName + ", permission=" + permission
				+ ", credits=" + credits + "]";
	}

}
