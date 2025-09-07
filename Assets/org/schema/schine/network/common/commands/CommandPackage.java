package org.schema.schine.network.common.commands;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.schema.common.SerializationInterface;
import org.schema.schine.network.common.NetworkProcessor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class CommandPackage implements SerializationInterface {
	public boolean pooled;

	public abstract Commandable getType();

	public abstract void reset();

	public void copyTo(CommandPackage p) throws IOException {

		FastByteArrayOutputStream out = new FastByteArrayOutputStream();
		serialize(new DataOutputStream(out), false);

		FastByteArrayInputStream in = new FastByteArrayInputStream(out.array, 0, (int) out.length());
		p.reset();
		p.deserialize(new DataInputStream(in), 0, false);

	}

	public void send(NetworkProcessor recipient) throws IOException {
		getType().getCommand().send(recipient, this);
	}

}
