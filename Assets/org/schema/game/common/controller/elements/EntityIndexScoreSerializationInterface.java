package org.schema.game.common.controller.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface EntityIndexScoreSerializationInterface {
	public void serialize(EntityIndexScore s, DataOutput b, boolean isOnServer) throws IOException;

	public void deserialize(EntityIndexScore s, DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException;
}
