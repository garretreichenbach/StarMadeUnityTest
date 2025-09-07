package org.schema.game.client.view.cubes;

import org.schema.common.util.MemoryManager.ManagedMemoryChunk;
import org.schema.game.common.data.world.DrawableRemoteSegment;

public interface CubeMeshInterface {

	int OPAQUE = 0;
	int BLENDED = 1;
	int BOTH = 2;

	/**
	 * Clean up.
	 *
	 * @param gl the gl
	 * @
	 */
	public void cleanUp();

	public void contextSwitch(CubeMeshBufferContainer container, int[][] opaqueRanges, int[][] blendedRanges, int blendedElementsCount, int totalDrawnBlockCount, DrawableRemoteSegment segment);

	public void draw(int blended, int vis);

	public void drawMesh(int blended, int vis);

	/**
	 * @return the initialized
	 */
	public boolean isInitialized();

	/**
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized);

	public void prepare(ManagedMemoryChunk buffer);

	public void released();

}
