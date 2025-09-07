package org.schema.schine.network.common;

import java.io.IOException;

import org.schema.schine.network.common.commands.UnknownCommandException;


public interface Updatable {
	public void update() throws IOException, UnknownCommandException;
}
