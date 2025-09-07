package org.schema.game.common.data.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ChannelFactory<E extends ChatChannel> {
	public E instantiate(DataInputStream b) throws IOException;

	public void serialize(E form, DataOutputStream b) throws IOException;
}
