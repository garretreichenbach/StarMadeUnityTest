package org.schema.schine.network.common.commands;

import java.io.IOException;
import java.util.Arrays;



public class UnknownCommandException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6524224168535752632L;
	public final byte id;
	public UnknownCommandException(byte b) {
		super("An unknown command tried to be deserialized "+b+"; Available: "+Arrays.toString(Command.commandIdMap)+"\nThis could be due to a bug in the netcode too. Check the serialization and deserialization");
		this.id = b;
	}
}
