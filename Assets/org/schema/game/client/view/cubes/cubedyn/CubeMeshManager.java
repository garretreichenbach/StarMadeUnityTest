package org.schema.game.client.view.cubes.cubedyn;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager.ManagedMemoryChunk;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.CubeBufferFloat;
import org.schema.game.client.view.cubes.CubeInfo;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshInterface;
import org.schema.game.client.view.cubes.CubeMeshNormal;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
@Deprecated
public class CubeMeshManager {

	private static final boolean DEBUG = false;
	public static int MAX_BYTES = (EngineSettings.G_VBO_BULKMODE_SIZE.getInt()) * 1024 * 1024; //4MB
	public final ObjectArrayList<VBOSeg> vboSegs = new ObjectArrayList<VBOSeg>();

	public CubeMeshManager() {
		super();
		System.err.println("[GRAPHICS] TRY USING VBO MAPPED BUFFER: " + EngineSettings.G_USE_VBO_MAP.isOn());
	}

	public VBOCell getFreeSegment(int sizeNeeded, CubeMeshDyn mesh) {
		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		int receiveMethod;
		VBOCell seg = null;

		for (int i = 0; i < vboSegs.size(); i++) {
			seg = vboSegs.get(i).getFree(sizeNeeded, mesh);
			if (seg != null) {
				return seg;
			}
		}

		if (seg == null) {
			//no free cells in any current vbo
			VBOSeg newSeg = new VBOSeg();
			newSeg.init();
			vboSegs.add(newSeg);
			seg = newSeg.getFree(sizeNeeded, mesh);
		}

		if (DEBUG) {
			GlUtil.printGlErrorCritical();
		}
		assert (seg != null);
		return seg;
	}

	public CubeMeshInterface getInstance() {
		CubeMeshInterface f = new CubeMeshDyn();
		return f;
	}

	private class VBOCell implements Comparable<VBOCell> {
		final int initialStart;
		final int initialEnd;
		final int bufferId;
		public int blendBufferPos;
		boolean free = true;
		int startPositionByte;
		int endPositionByte;
		public VBOCell(int start, int end, int bufferId) {
			this.initialStart = start;
			this.initialEnd = end;
			this.bufferId = bufferId;
		}

		public boolean fits(int sizeNeeded, CubeMeshDyn mesh) {
			if (free && sizeNeeded <= sizeInitial()) {
				startPositionByte = initialStart;
				endPositionByte = initialStart + sizeNeeded;
				assert (endPositionByte <= initialEnd);
				free = false;
				return true;
			}
			return false;
		}

		private int sizeInitial() {
			return initialEnd - initialStart;
		}

		@Override
		public int compareTo(VBOCell o) {
			return initialStart - o.initialStart;
		}

		public int size() {
			return endPositionByte - startPositionByte;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "VBOCell [initialStart=" + initialStart + ", initialEnd="
					+ initialEnd + ", bufferId=" + bufferId + ", free=" + free
					+ ", startPositionByte=" + startPositionByte
					+ ", endPositionByte=" + endPositionByte + "]";
		}

	}

	private class VBOSeg {
		private final ObjectAVLTreeSet<VBOCell> cells = new ObjectAVLTreeSet<VBOCell>();
		int bufferId;
		int currentSize;

		public void init() {
			if (bufferId == 0) {
				createBuffer();
			}
		}

		public void createBuffer() {
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}

			bufferId = GL15.glGenBuffers();
			CubeMeshNormal.initializedBuffers++;
			Controller.loadedVBOBuffers.add(bufferId);
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId); // Bind
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, MAX_BYTES, CubeMeshNormal.BUFFER_FLAG); // Load The Data

			GameClientState.realVBOSize += MAX_BYTES;
			GameClientState.prospectedVBOSize += (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents)
					* ByteUtil.SIZEOF_FLOAT;

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
		}

		public VBOCell getFree(int sizeNeeded, CubeMeshDyn mesh) {
			ObjectBidirectionalIterator<VBOCell> iterator = cells.iterator();
			int lastEnd = 0;
			while (iterator.hasNext()) {
				VBOCell cell = iterator.next();
				assert (cell.initialStart == lastEnd);
				lastEnd = cell.initialEnd;

				//				System.err.println("CHECKING "+cell);

				if (cell.fits(sizeNeeded, mesh)) {
					return cell;
				}
			}
			if (currentSize + sizeNeeded < MAX_BYTES) {
				VBOCell cell = new VBOCell(currentSize, currentSize + sizeNeeded, bufferId);
				currentSize += sizeNeeded;
				boolean fits = cell.fits(sizeNeeded, mesh);

				cells.add(cell);
				//				System.err.println("NEW CELL: "+cell);
				assert (fits);
				assert (!cell.free);
				return cell;
			}
			return null;
		}
	}

	public class CubeMeshDyn implements CubeMeshInterface {

		private boolean initialized;
		private VBOCell currentVBOSeg;

		@Override
		public void cleanUp() {

		}

		@Override
		public void contextSwitch(CubeMeshBufferContainer container, int[][] opaqueRanges, int[][] blendedRanges, int blendBufferPos, int totalDrawnBlockCount, DrawableRemoteSegment segment) {
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			if (!CubeMeshNormal.checkedForRangeMap) {
				CubeMeshNormal.setGl_ARB_map_buffer_range(GraphicsContext.current.getCapabilities().GL_ARB_map_buffer_range);
				System.err.println("USE BUFFER RANGE: " + CubeMeshNormal.isGl_ARB_map_buffer_range());
				CubeMeshNormal.checkedForRangeMap = true;
			}
			boolean init = false;

			container.dataBuffer.make();
			container.dataBuffer.getTotalBuffer().flip();

			if (!initialized) {
				prepare(null);
				init = true;
			}

			if (container.dataBuffer.getTotalBuffer().limit() != 0) {

				long tPut = 0;
				long tOrder = 0;
				long tUnmap = 0;

				int sizeNeededInBytes = container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT;
				if (currentVBOSeg != null) {
					currentVBOSeg.free = true;
				}
				if (currentVBOSeg != null && currentVBOSeg.fits(sizeNeededInBytes, this)) {
					// nothing to do, we can use the old one
				} else {
					currentVBOSeg = getFreeSegment(sizeNeededInBytes, this);
				}

				assert (currentVBOSeg.bufferId != 0);

				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOSeg.bufferId); // Bind
				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}
				long tStart = System.nanoTime();
				if (CubeMeshNormal.USE_MAP_BUFFER) {
					if (CubeMeshNormal.isGl_ARB_map_buffer_range()) {
						CubeMeshNormal.oldHelpBuffer = ARBMapBufferRange.glMapBufferRange(
								GL15.GL_ARRAY_BUFFER,
								currentVBOSeg.startPositionByte,
								currentVBOSeg.endPositionByte,
								CubeMeshNormal.mappingByte,
								CubeMeshNormal.oldHelpBuffer == null ? null : CubeMeshNormal.oldHelpBuffer);

						assert ((currentVBOSeg.startPositionByte + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT) < MAX_BYTES);
						if (DEBUG) {
							GlUtil.printGlErrorCritical("STARTPOS: " + currentVBOSeg.startPositionByte + "; len: " + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT + "; TOTAL END: " + (currentVBOSeg.startPositionByte + container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT) + " / " + MAX_BYTES);
						}
						//						System.err.println("STARTPOS: "+startPos+"; len: "+container.dataBuffer.limit() * ByteUtil.SIZEOF_FLOAT+"; TOTAL END: "+(startPos + container.dataBuffer.limit() * ByteUtil.SIZEOF_FLOAT)+" / "+MAX_BYTES);
					} else {

						CubeMeshNormal.oldHelpBuffer = GL15.glMapBuffer(
								GL15.GL_ARRAY_BUFFER,
								GL15.GL_WRITE_ONLY,
								CubeMeshNormal.oldHelpBuffer == null ? null : CubeMeshNormal.oldHelpBuffer);
						if (DEBUG) {
							GlUtil.printGlErrorCritical();
						}
					}
					if (CubeMeshNormal.oldHelpBuffer == null && CubeMeshNormal.USE_MAP_BUFFER) {
						EngineSettings.G_USE_VBO_MAP.setOn(false);
						CubeMeshNormal.USE_MAP_BUFFER = false;
						System.err.println("[Exception]WARNING: MAPPED BUFFER HAS BEEN TURNED OFF " + GlUtil.getGlError());
					}
				}
				long tBindAD = System.nanoTime() - tStart;
				boolean glUnmapBuffer = false;
				if (CubeMeshNormal.USE_MAP_BUFFER) {
					long t = System.nanoTime();
					FloatBuffer mapBuffer = CubeMeshNormal.oldHelpBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
					;
					tOrder = (System.nanoTime() - t) / 1000000;
					;

					t = System.nanoTime();
					if (CubeMeshNormal.isGl_ARB_map_buffer_range()) {
						mapBuffer.put(((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());
					} else {
						mapBuffer.position(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT);
						mapBuffer.put(((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());
					}
					tPut = (System.nanoTime() - t) / 1000000;

					//					mapBuffer.flip();
					t = System.nanoTime();
					glUnmapBuffer = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
					tUnmap = (System.nanoTime() - t) / 1000000;
					if (DEBUG) {
						GlUtil.printGlErrorCritical();
					}
				} else {
					//DO BUFFER SUB DATA UPDATE
					GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, currentVBOSeg.startPositionByte, ((CubeBufferFloat)container.dataBuffer).totalBuffer.getByteBackingToCurrent());// Load The Data
					if (DEBUG) {
						GlUtil.printGlErrorCritical();
					}
				}

				//		System.err.println("DDDD !!!!!!!!!! iB: "+container.indexBuffer.position()+" / "+container.indexBuffer.limit()+"; aB: "+container.attibuteBuffer.position()+" / "+container.attibuteBuffer.limit()+";buID "+getIndexBufferId(0)+"; atID: "+getAttributeBufferId(0));
				CubeMeshNormal.bufferContextSwitchTime = System.nanoTime() - tStart;

				CubeMeshNormal.bufferContextSwitchTime /= 1000000;
				if (CubeMeshNormal.bufferContextSwitchTime > 10) {
					System.err.println("[CUBE] WARNING: context switch time: " + CubeMeshNormal.bufferContextSwitchTime + " ms : " + tBindAD / 1000000 + "ms: O " + tOrder + "; P " + tPut + "; U " + tUnmap + "::; map " + CubeMeshNormal.USE_MAP_BUFFER + "; range " + CubeMeshNormal.isGl_ARB_map_buffer_range() + "; init " + init + "  unmap " + glUnmapBuffer);
				}

				assert (container.dataBuffer.getTotalBuffer().limit() / CubeMeshBufferContainer.vertexComponents == (currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents) : container.dataBuffer.getTotalBuffer().limit() / CubeMeshBufferContainer.vertexComponents + "; " + (currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents;
				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}
				currentVBOSeg.blendBufferPos = blendBufferPos;
			} else {
				//			System.err.println("[CUBEMESH] WARNING: tried to switch to empty mesh");
				if (currentVBOSeg != null) {
					currentVBOSeg.free = true;
					currentVBOSeg = null;
				}
			}

		}

		@Override
		public void draw(int blended, int vis) {
			drawMesh(blended, vis);
		}

		@Override
		public void drawMesh(int blended, int vis) {

			if (currentVBOSeg == null) {
				//				System.err.println("SEGMENT NULL");
				return;
			}

			assert (currentVBOSeg.bufferId != 0);

			boolean glBindBuffer = GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOSeg.bufferId);
			if (glBindBuffer) {
				GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
			}
			if (CubeMeshBufferContainer.isTriangle()) {
				//				System.err.println("DRAW TRIANGLE");
				if (blended == BOTH) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == OPAQUE) {
					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == BLENDED) {

					GL11.glDrawArrays(GL11.GL_TRIANGLES,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents + (currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT - currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents
					);

				}

			} else {
				if (blended == BOTH) {
					GL11.glDrawArrays(GL11.GL_QUADS,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents);

				} else if (blended == OPAQUE) {
					GL11.glDrawArrays(GL11.GL_QUADS,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents);
				} else if (blended == BLENDED) {
					GL11.glDrawArrays(GL11.GL_QUADS,
							(currentVBOSeg.startPositionByte / ByteUtil.SIZEOF_FLOAT) / CubeMeshBufferContainer.vertexComponents + (currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents,
							(currentVBOSeg.size() / ByteUtil.SIZEOF_FLOAT - currentVBOSeg.blendBufferPos) / CubeMeshBufferContainer.vertexComponents
					);
				}
			}

		}

		//	private void drawSeperateOpaque(){
		//
		//		GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
		//		GL11.glDrawArrays(GL11.GL_QUADS, 0, blendBufferPos/CubeMeshBufferContainer.vertexComponents);
		//
		//	}
		//	private void drawSeperateBlend(){
		//
		//		GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, blendBufferPos * 4);
		//		GL11.glDrawArrays(GL11.GL_QUADS, 0, (endBufferPos-blendBufferPos)/CubeMeshBufferContainer.vertexComponents);
		//	}

		@Override
		public boolean isInitialized() {
			return initialized;
		}

		@Override
		public void setInitialized(boolean initialized) {
			this.initialized = initialized;
		}

		@Override
		public void prepare(ManagedMemoryChunk buffer) {

			initialized = true;
		}

		@Override
		public void released() {
			if (currentVBOSeg != null) {
				currentVBOSeg.free = true;
				currentVBOSeg = null;
			}
		}

	}

}
