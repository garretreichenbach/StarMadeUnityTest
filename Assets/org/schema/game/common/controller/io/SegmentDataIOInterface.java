package org.schema.game.common.controller.io;

import java.io.IOException;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public interface SegmentDataIOInterface {

	public void releaseFileHandles() throws IOException;
	
	public boolean write(RemoteSegment s, long lastChanged, boolean writeHeader, boolean debug) throws IOException;
	
	public IOFileManager getManager();
	
	public int getSize(int x, int y, int z) throws IOException;
	
	public long getTimeStamp(int x, int y, int z) throws IOException;
	
	public int request(int x, int y, int z, RemoteSegment seg) throws IOException, DeserializationException;
	
	public EWAHCompressedBitmap requestSignature(int xs, int ys, int zs) throws IOException, DeserializationException;
	
	public void writeEmpty(int x, int y, int z, SegmentController c, long lastChanged, boolean writeHeader) throws IOException;
	
}
