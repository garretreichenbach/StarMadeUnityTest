package org.schema.game.network.objects.remote;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.schine.network.common.commands.Command;


public abstract class SimpleCommand<E extends Enum<?>> implements SerializationInterface{

	private Object[] args;
	private int command;
	private int updateSenderStateId;

	public SimpleCommand(E command, Object[] args) {
		this.args = args;
		this.command = command.ordinal();
		
		checkMatches(command, args);
		
	}

	public SimpleCommand() {
	}
	protected abstract void checkMatches(E command, Object[] args);
	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeInt(command);
		Command.serialize(getArgs(), buffer);
	}

	public void deserialize(DataInput buffer, int updateSenderStateId) throws IOException {
		command = buffer.readInt();
		args = Command.deserialize(buffer);
		this.updateSenderStateId = updateSenderStateId;
	}

	/**
	 * @return the command
	 */
	public int getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(int command) {
		this.command = command;
	}

	/**
	 * @return the args
	 */
	public Object[] getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Object[] args) {
		this.args = args;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		serialize(b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		deserialize(b, updateSenderStateId);
	}

	public int getUpdateSenderStateId() {
		return updateSenderStateId;
	}

	
	
	
}
