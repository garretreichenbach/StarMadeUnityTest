package org.schema.schine.network;

import java.io.IOException;

import org.schema.schine.network.objects.Sendable;

public interface SendableType {
	public Sendable getInstance(StateInterface state) throws IOException;
	public byte getTypeCode();
}
