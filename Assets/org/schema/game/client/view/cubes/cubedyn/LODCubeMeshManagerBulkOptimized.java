package org.schema.game.client.view.cubes.cubedyn;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeMeshNormal;
import org.schema.game.common.data.element.BlockFilter;
import org.schema.game.common.data.element.ElementCollectionMesh;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.DrawableRemoteSegment.LODMeshData;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LODCubeMeshManagerBulkOptimized extends VBOManagerBulkBase{


//	private ThreadPoolExecutor threadPool;

	public static final int VERTEX_COMPONENTS = 4;
	
	public LODCubeMeshManagerBulkOptimized() {
		super();
		System.err.println("[GRAPHICS] TRY USING LOD VBO MAPPED BUFFER: " + EngineSettings.G_USE_VBO_MAP.isOn());
		
//		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool( );
//		threadPool.setThreadFactory(new ThreadFactory() {
//				
//				@Override
//				public Thread newThread(Runnable r) {
//					Thread thread = new Thread(r);
//					thread.setDaemon(true); //all connection threads are daemons
//					return thread;
//				}
//			});
		
		
	}
	public boolean running = false;
	public void Initiailize() {
		running = true;
//		for(int i = 0;i < 2; i++) {
//			Thread t = new Thread(new CalculationThread());
//			t.setDaemon(true);
//			t.start();
//		}
	}

	
//
//	public LODCubeMeshDynOpt getInstance() {
//		LODCubeMeshDynOpt f = new LODCubeMeshDynOpt(this);
//		return f;
//	}
	private final ObjectArrayFIFOQueue<DrawableRemoteSegment> queue = new ObjectArrayFIFOQueue<>();
	private final ObjectArrayFIFOQueue<DrawableRemoteSegment> contextSwitchQueue = new ObjectArrayFIFOQueue<>();
	public static class Mesher{
		private final LongArrayList cache = new LongArrayList(SegmentData.BLOCK_COUNT);
		public final ElementCollectionMesh mesh = new ElementCollectionMesh();
		public final Vector4f color;
		public final BlockFilter filter;
		public Mesher(Vector4f color, BlockFilter filter) {
			super();
			this.filter = filter;
			this.color = color;
		}
		public void initializeWithSegmentData(SegmentData data, Vector3i pos) {
			data.fill(cache, pos.x, pos.y, pos.z);
		}
		public void calculate() {
			mesh.calculate(null, 0, cache);
		}
	}
	private final List<ByteBuffer> bufferPool = new ObjectArrayList<>();
	private ByteBuffer oldBufferHelper;
	
	public void freeByteBuffer(ByteBuffer b) {
		synchronized(bufferPool) {
			b.clear();
			bufferPool.add(b);
		}
	}
	private ByteBuffer getByteBuffer(int sizeNeeded) {
		ByteBuffer b = null;
		synchronized(bufferPool) {
			if(!bufferPool.isEmpty()) {
				b = bufferPool.remove(bufferPool.size()-1);
			}
		}
		if(b != null) {
			if(b.capacity() < sizeNeeded) {
					GlUtil.destroyDirectByteBuffer(b);
			}else {
				return b;
			}
		}
			
		b = MemoryUtil.memAlloc(sizeNeeded);
		
		return b;
	}
//	public class CalculationThread implements Runnable{
//		
//		private final Mesher[] mesher = new Mesher[] {new Mesher(new Vector4f(1,1,1,1), (id) -> true)};
//		
//		@Override
//		public void run() {
//			try {
//				while(running) {
//					DrawableRemoteSegment seg;
//					synchronized(queue) {
//						while(queue.isEmpty()) {
//							try {
//								queue.wait(3000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							if(!running) {
//								return;
//							}
//						}
//						seg = queue.dequeue();
//					}
//					
//					
//					
//					final SegmentData data;
//					
//					synchronized(seg.LODMeshLock) {
//						seg.LODMeshLock.updateBasedOn = seg.LODMeshLock.lastRequest;
//						
//					}
//					data = seg.getSegmentData();
//					if(data != null) {
//						calculateLodMesh(seg, data);
//					}
//					synchronized(seg.LODMeshLock) {
//						if(seg.LODMeshLock.updateBasedOn < seg.LODMeshLock.lastRequest) {
//							//mesh has been updated during update. requeue
//							addToQueue(System.currentTimeMillis(), seg);
//						}else {
//							seg.LODMeshLock.queued = false;
//						}
//					}
//				}
//			}finally {
//				
//			}
//		}
//		
//		private void calculateLodMesh(DrawableRemoteSegment seg, SegmentData data) {
//			//Step 1: initialize the mesher by feeding it the segmentData
//			data.rwl.readLock().lock();
//			try {
//				for(int i = 0; i < mesher.length; i++) {
//					mesher[i].initializeWithSegmentData(data, seg.pos);
//				}
//			}finally {
//				data.rwl.readLock().unlock();
//			}
//			System.err.println("CALCULATING");
//			//step 2: calculate optimized mesh
//			for(int i = 0; i < mesher.length; i++) {
//				mesher[i].calculate();
//			}
//			
//			//step 3: copy temporary mesh data into container to be context switched onto the graphics card later on synched update
//			int totalBytes = 0;
//			for(int i = 0; i < mesher.length; i++) {
//				totalBytes += mesher[i].mesh.getByteCount();
//			}
//			ByteBuffer b = getByteBuffer(totalBytes);
//			b.clear();
//			int position = 0;
//			System.err.println("FILLING BUFFER");
//			for(int i = 0; i < mesher.length; i++) {
//				mesher[i].mesh.fillBuffer(b);
//				
//				seg.LODMeshLock.bufferPosAndSize[i][0] = position;
//				seg.LODMeshLock.bufferPosAndSize[i][1] = b.position();
//				position = b.position();
//				
//				
//				System.err.println("LOD BUFFER MESH BUFFER "+seg.LODMeshLock.bufferPosAndSize[i][0]+" -> "+seg.LODMeshLock.bufferPosAndSize[i][1]);
//				mesher[i].mesh.clear();
//			}
//			b.flip();
//			seg.LODMeshLock.queuedBuffer = b;
//			
//			System.err.println("QUEUE CONTEXT SWITCH ");
//			//step 4: enqueue data to be switched in
//			queueContextSwitch(seg);
//		}
//	}
//	private void queueContextSwitch(DrawableRemoteSegment m) {
//		synchronized(contextSwitchQueue) {
//			if(running) {
//				System.err.println("READY FOR CONTEXT SWITCH "+m);
//				contextSwitchQueue.enqueue(m);
//			}
//		}
//	}
//	public void addToQueue(long time, DrawableRemoteSegment m) {
//		synchronized(m.LODMeshLock) {
//			m.LODMeshLock.lastRequest = time;
//			if(!m.LODMeshLock.queued) {
//				m.LODMeshLock.queued = true;
//				synchronized(queue) {
//					queue.enqueue(m);
//					queue.notifyAll();
//				}
//			}
//		}
//	}
	public void update(Timer timer) {
	}
//	public void doContextSwitches() {
//		if(!contextSwitchQueue.isEmpty()) {
//			while(!contextSwitchQueue.isEmpty()) {
//				DrawableRemoteSegment seg;
//				synchronized(contextSwitchQueue) {
//					seg = contextSwitchQueue.dequeue();
//				}
//				contextSwitch(seg, seg.LODMeshLock);
//			}
//			
//			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//		}
//	}
	public void contextSwitch(DrawableRemoteSegment segment, LODMeshData lODMeshLock, int lodIdex) {
		if (!CubeMeshNormal.checkedForRangeMap) {
			CubeMeshNormal.setGl_ARB_map_buffer_range(GraphicsContext.current.getCapabilities().GL_ARB_map_buffer_range);
			System.err.println("USE BUFFER RANGE: " + CubeMeshNormal.isGl_ARB_map_buffer_range());
			CubeMeshNormal.checkedForRangeMap = true;
		}
		ByteBuffer dataBuffer = lODMeshLock.queuedBuffer[lodIdex];
		lODMeshLock.queuedBuffer[lodIdex] = null;
		
		if (dataBuffer.limit() > 0) {

			long tPut = 0;
			long tOrder = 0;
			long tUnmap = 0;

			int sizeNeededInBytes = dataBuffer.limit();
			if (lODMeshLock.currentVBOCell[lodIdex] != null) {
				lODMeshLock.currentVBOCell[lodIdex].free = true;
			}
			if (lODMeshLock.currentVBOCell[lodIdex] != null && lODMeshLock.currentVBOCell[lodIdex].stillFitsInTaken(sizeNeededInBytes)) {
				// nothing to do, we can use the old one
			} else {
				lODMeshLock.currentVBOCell[lodIdex] = getFreeSegment(sizeNeededInBytes, segment);
			}
			assert (lODMeshLock.currentVBOCell[lodIdex]  != null);
			assert (lODMeshLock.currentVBOCell[lodIdex].getBufferId() != 0);

			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, lODMeshLock.currentVBOCell[lodIdex].getBufferId()); // Bind
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
			long tStart = System.nanoTime();
			
//				System.err.println("PUTTING BYTES INTO "+lODMeshLock.currentVBOCell[lodIdex].getBufferId()+" : "+lODMeshLock.currentVBOCell[lodIdex].startPositionByte+" -> "+dataBuffer.remaining());
				
				assert(lODMeshLock.currentVBOCell[lodIdex].lengthInBytes == dataBuffer.remaining());
				assert(dataBuffer.position() == 0);
				//DO BUFFER SUB DATA UPDATE
				GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, lODMeshLock.currentVBOCell[lodIdex].startPositionByte, dataBuffer);// Load The Data
				
				if (DEBUG) {
					GlUtil.printGlErrorCritical();
				}
			if (DEBUG) {
				GlUtil.printGlErrorCritical();
			}
		} else {
			//			System.err.println("[CUBEMESH] WARNING: tried to switch to empty mesh");
//			if (lODMeshLock.currentVBOCell != null) {
//				lODMeshLock.currentVBOCell.free = true;
//				lODMeshLock.currentVBOCell = null;
//			}
		}
		//buffer no longer needed
		freeByteBuffer(dataBuffer);
	}
	public void cleanUp() {
		
		synchronized(queue) {
			queue.clear();
		}
		synchronized(contextSwitchQueue) {
			while(!contextSwitchQueue.isEmpty()) {
				contextSwitchQueue.dequeue().LODMeshLock.cleanUp(this);
			}
			running = false;
		}
		
	}
	
	public void mark(DrawableRemoteSegment seg, Transform t, int id, int optionBits, int lodIdex) {
		VBOCell cell = seg.LODMeshLock.currentVBOCell[lodIdex];
		if (cell != null) {
			int start;
			int count;

			start = (cell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / VERTEX_COMPONENTS ;
			count = (cell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / VERTEX_COMPONENTS;
			
			if (count > 0) {

				DrawMarker drawMarker;
				boolean exists = !cell.vboSeg.markers.isEmpty() && cell.vboSeg.markers.get(cell.vboSeg.markers.size() - 1).id == id;

				if (exists) {
					drawMarker = cell.vboSeg.markers.get(cell.vboSeg.markers.size() - 1);
				} else {
					drawMarker = CubeMeshManagerBulkOptimized.getMarker();
					drawMarker.t.set(t);
					drawMarker.id = id;
					drawMarker.optionBits = optionBits;
					cell.vboSeg.markers.add(drawMarker);
				}

				drawMarker.start.add(start);
				drawMarker.count.add(count);
			}
		}
	}
	
	public void drawMeshWithTrans(DrawableRemoteSegment seg, int lodIndex) {
		VBOCell currentVBOCell = seg.LODMeshLock.currentVBOCell[lodIndex];
		if (currentVBOCell == null) {
			
			return;
		}
		

		assert (currentVBOCell.getBufferId() != 0);
//		Cube.solidCube(1);
		boolean glBindBuffer = GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVBOCell.getBufferId());
		if (glBindBuffer) {
//				GL11.glVertexPointer(VERTEX_COMPONENTS, GL11.GL_FLOAT, 0, 0);
			GlUtil.glVertexAttribIPointer(ShaderLibrary.CUBE_SHADER_VERT_INDEX, VERTEX_COMPONENTS, GL11.GL_INT, 0, 0);	
		}
		
//		System.err.println("DRAWING VBO "+currentVBOCell.lengthInBytes);
		
		
		GlUtil.glPushMatrix();

		GlUtil.glMultMatrix(seg.getSegmentController().getWorldTransformOnClient());
		GL11.glDrawArrays(GL11.GL_TRIANGLES,
				(currentVBOCell.startPositionByte / ByteUtil.SIZEOF_FLOAT) / VERTEX_COMPONENTS,
				(currentVBOCell.lengthInBytes / ByteUtil.SIZEOF_FLOAT) / VERTEX_COMPONENTS);
			
		GlUtil.glPopMatrix();
	}
	public void drawMulti(final boolean clearMaked, Shader shader) {
		int segSize = vboSegs.size();
		final boolean multiDraw = EngineSettings.USE_GL_MULTI_DRAWARRAYS.isOn();
		
		
		Shader currentShader = shader;
		Shader original = shader;
		
		for (int j = 0; j < segSize; j++) {
			VBOSeg seg = vboSegs.get(j);

			boolean glBindBuffer = GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, seg.bufferId);
			if (glBindBuffer) {
//				GL11.glVertexPointer(VERTEX_COMPONENTS, GL11.GL_FLOAT, 0, 0);
				GlUtil.glVertexAttribIPointer(ShaderLibrary.CUBE_SHADER_VERT_INDEX, VERTEX_COMPONENTS, GL11.GL_INT, 0, 0);	
			}
			int markerSize = seg.markers.size();
			for (int i = 0; i < markerSize; i++) {
				DrawMarker mark = seg.markers.get(i);
				if (mark.start.size() > 0) {
					GlUtil.glPushMatrix();

					GlUtil.glMultMatrix(mark.t);
					
					if(currentShader != original){
						original.load();
						currentShader = original;
					}
					
					if (multiDraw) {
						long t = System.currentTimeMillis();
						IntBuffer multiBufferStarts = GlUtil.getDynamicByteBuffer(mark.start.size() * ByteUtil.SIZEOF_INT, 0).order(ByteOrder.nativeOrder()).asIntBuffer();
						multiBufferStarts.put(mark.start.elements(), 0, mark.start.size());
						multiBufferStarts.flip();

						IntBuffer multiBufferCounts = GlUtil.getDynamicByteBuffer(mark.count.size() * ByteUtil.SIZEOF_INT, 1).order(ByteOrder.nativeOrder()).asIntBuffer();
						multiBufferCounts.put(mark.count.elements(), 0, mark.count.size());
						multiBufferCounts.flip();

						long taken = System.currentTimeMillis() - t;
						if(taken > SegmentDrawer.WARNING_MARGIN) {
							System.err.println("WARNING: MultiDraw Buffer Time: "+taken+"ms");
						}
						
						t = System.currentTimeMillis();
						GL14.glMultiDrawArrays(GL11.GL_TRIANGLES, multiBufferStarts, multiBufferCounts);
						taken = System.currentTimeMillis() - t;
						if(taken > SegmentDrawer.WARNING_MARGIN) {
							System.err.println("WARNING: MultiDraw Draw Time: "+multiBufferStarts+"; "+multiBufferCounts+" "+taken+"ms");
						}
					} else {
						int size = mark.start.size();
						for (int k = 0; k < size; k++) {
							int start = mark.start.getInt(k);
							int count = mark.count.getInt(k);
							GL11.glDrawArrays(GL11.GL_TRIANGLES, start, count);
						}
					}

					GlUtil.glPopMatrix();
				}
				if (clearMaked) {
					releaseMarker(mark);
				}
			}
			if (clearMaked) {
				seg.markers.clear();
			}

		}

	}
	

}
