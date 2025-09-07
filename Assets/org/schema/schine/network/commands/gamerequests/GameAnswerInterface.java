package org.schema.schine.network.commands.gamerequests;

import org.schema.common.SerializationInterface;

public interface GameAnswerInterface extends SerializationInterface{
	public GameRequestAnswerFactory getFactory();

	public void free();
}
