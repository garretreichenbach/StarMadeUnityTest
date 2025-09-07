package org.schema.game.common.controller.io;

import java.nio.ByteBuffer;

public interface UniqueIdentifierInterface {
	public String getReadUniqueIdentifier();

	public String getUniqueIdentifier();
	
	public boolean isOnServer();

	public boolean isLoadByBlueprint();

	public String getBlueprintSegmentDataPath();

	public String getObfuscationString();

	public ByteBuffer getDataByteBuffer();

	public void releaseDataByteBuffer(ByteBuffer dataByteBuffer);

	public long getUpdateTime();

	public String getWriteUniqueIdentifier();
}
