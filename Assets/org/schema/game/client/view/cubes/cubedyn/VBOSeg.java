package org.schema.game.client.view.cubes.cubedyn;

import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.CubeInfo;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshNormal;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public class VBOSeg {

	private Int2ObjectOpenHashMap<ObjectArrayList<VBOSeg>> reservedVBOSegs;

	public VBOSeg(Int2ObjectOpenHashMap<ObjectArrayList<VBOSeg>> reservedVBOSegs) {
		this.reservedVBOSegs = reservedVBOSegs;
	}
	
	final ObjectArrayList<DrawMarker> markers = new ObjectArrayList<DrawMarker>();
	private final ObjectAVLTreeSet<VBOCell> cells = new ObjectAVLTreeSet<VBOCell>();
	public int takenCount;
	public int reserved;
	int bufferId;
	int currentSize;
	int maxBytes;

	public void init(int sizeNeeded) {
		if (bufferId == 0) {
			createBuffer(sizeNeeded);
		}
	}

	public void checkAllFree() {
		if (takenCount == 0) {
			cells.clear();
			currentSize = 0;
			if (reserved > 0) {
				ObjectArrayList<VBOSeg> objectArrayList = reservedVBOSegs.get(reserved);
				objectArrayList.remove(this);
				if (objectArrayList.isEmpty()) {
					reservedVBOSegs.remove(reserved);
				}
				reserved = 0;
			}
		}
	}

	public void createBuffer(int sizeNeeded) {
		if (VBOManagerBulkBase.DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		int max = Math.max(sizeNeeded+1, CubeMeshManagerBulkOptimized.MAX_BYTES);
		this.maxBytes = max;
		bufferId = GL15.glGenBuffers();
		CubeMeshNormal.initializedBuffers++;
		Controller.loadedVBOBuffers.add(bufferId);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId); // Bind
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, max, CubeMeshNormal.BUFFER_FLAG); // Load The Data

		GameClientState.realVBOSize += max;
		GameClientState.prospectedVBOSize += (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents)
				* ByteUtil.SIZEOF_FLOAT;

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
		if (VBOManagerBulkBase.DEBUG) {
			GlUtil.printGlErrorCritical();
		}
	}

	public VBOCell getFree(int sizeNeeded) {
		ObjectBidirectionalIterator<VBOCell> iterator = cells.iterator();
		int lastEnd = 0;
		while (iterator.hasNext()) {
			VBOCell cell = iterator.next();
			assert (cell.initialStart == lastEnd) : cell.initialStart + " : " + lastEnd;
			lastEnd = cell.initialEnd;

			//				System.err.println("CHECKING "+cell);

			if (cell.fitIn(sizeNeeded)) {
				return cell;
			}
		}
		if (currentSize + sizeNeeded < maxBytes) {
			VBOCell cell = new VBOCell(currentSize, currentSize + sizeNeeded, VBOSeg.this);
			currentSize += sizeNeeded;
			boolean fits = cell.fitIn(sizeNeeded);

			cells.add(cell);
			//				System.err.println("NEW CELL: "+cell);
			assert (fits);
			assert (!cell.free);
			return cell;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return bufferId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((VBOSeg) obj).bufferId == bufferId;
	}

}
