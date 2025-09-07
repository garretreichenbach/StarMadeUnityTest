package org.schema.game.common.data;

import java.io.DataInputStream;

public class UploadState {
	public static final long DELAY = 30;
	public DataInputStream uploadInputStream;
	public long currentUploadLength;
	public long pointer;
	public long lastUploadSegmentTime;
}
