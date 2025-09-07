package org.schema.game.client.view.cubes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.ARBMapBufferRange;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager.ManagedMemoryChunk;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.FBOFlag;
import org.schema.schine.graphicsengine.forms.simple.Box;

public class CubeMeshNormal implements CubeMeshInterface {
	public static final int BUFFER_FLAG =
			EngineSettings.G_VBO_FLAG.getObject() == FBOFlag.DYNAMIC ? GL15.GL_DYNAMIC_DRAW :
				EngineSettings.G_VBO_FLAG.getObject() == FBOFlag.STATIC ? GL15.GL_STATIC_DRAW : GL15.GL_STREAM_DRAW;

	

	//	private static FloatBuffer normals; // TextureNew Coordinates

	//	private IntBuffer attibuteBufferIds;

	//	private ObjectArrayList<CubeData> attached = new ObjectArrayList<CubeData>();
	public static int initializedBuffers;
	public static int occludedMeshes;
	public static int visibleMeshes;
	public static boolean withOcclusionCulling = false;
	public static int maxIndex = 0;
	/**
	 * this is a little waste of space, but the buffer is only needed for element types anyway
	 */
	//	public static final int first = 4096;
	//	public static final int second = 8192;
	//	public static final int third = 16384;
	//	public static final int fourth = 32768;

	//	public static FloatBuffer container.dataBuffer =
	//			MemoryUtil.memAllocFloat(CubeData.MAX_SIZE*CubeMeshBufferContainer.vertexComponents);
	public static ByteBuffer oldHelpBuffer;
	public static boolean USE_MAP_BUFFER = EngineSettings.G_USE_VBO_MAP.isOn();
	public static boolean checkedForRangeMap;
	public static boolean showBoundingBoxes = false;
	public static boolean showOccludedBoundingBoxes;
	public static long bufferContextSwitchTime;
	public static int mappingByte =
			ARBMapBufferRange.GL_MAP_WRITE_BIT |
					ARBMapBufferRange.GL_MAP_UNSYNCHRONIZED_BIT |
					ARBMapBufferRange.GL_MAP_INVALIDATE_BUFFER_BIT;
	private static int IDGEN;
	private static boolean gl_ARB_map_buffer_range;
	public int timeForQuery;

	//	public static final int RIGHT 	= 0;
	//	public static final int LEFT 	= 1;
	//	public static final int TOP 	= 2;
	//	public static final int BOTTOM = 3;
	//	public static final int FRONT 	= 4;
	//	public static final int BACK 	= 5;
	public int timeForDrawVisible;

	//	private static FloatBuffer singleAttributeBufferPerSide =
	//			MemoryUtil.memAllocFloat(SimplePosElement.CUBE_SIDE_VERT_COUNT );
	public int timeForDrawOccluded;
	public int timeForContextSwitch;
	public int timeForContextUniforms;
	public int timeForActualDraw;
	public long createContextSwitchTime;
	public long statePrepareTime;
	//	private static void generateNormals(){
	//		normals = new Vector3f[6];
	//		normals[SimplePosElement.LEFT] = new Vector3f(-1,0,0);
	//		normals[SimplePosElement.RIGHT] = new Vector3f(1,0,0);
	//		normals[SimplePosElement.BOTTOM] = new Vector3f(0,-1,0);
	//		normals[SimplePosElement.TOP] = new Vector3f(0,1,0);
	//		normals[SimplePosElement.BACK] = new Vector3f(0,0,-1);
	//		normals[SimplePosElement.FRONT] = new Vector3f(0,0,1);
	//	}
	//
	public boolean generated;
	boolean occlusionTest = false;

	//	public Segment lastTouched;
	int readWrite = GL15.GL_WRITE_ONLY;
	int id;
	boolean first = true;
	private long time;
	private boolean initialized;
	private int currentVBOSize;
	private int vertexBufferEndPos;
	private int attibuteBufferId;

	public CubeMeshNormal() {
		id = IDGEN++;
		//		this.setName(cubeName);
		//		this.setFaceCount(CubeInfo.CUBE_VERTICES_COUNT_SIDE);
		//		this.setType(Mesh.TYPE_VERTEX_BUFFER_OBJ);

	}

	public static void buildBox(Vector3f min, Vector3f max, Vector3f offset, FloatBuffer vertices) {
		Vector3f[][] box = Box.getVertices(new Vector3f(min.x, min.y, min.z), new Vector3f(max.x, max.y, max.z));
		for (int i = 0; i < 6; i++) {
			for (int faceVertex = 0; faceVertex < 4; faceVertex++) {
				// Vertices
				Vector3f vert = box[i][faceVertex];
				vertices.put(vert.x);
				vertices.put(vert.y);
				vertices.put(vert.z);
			}
		}
	}

	public static Vector3b[] getSurrounding(Vector3b pos, Vector3b[] out) {
		byte x = pos.x;
		byte y = pos.y;
		byte z = pos.z;
		pos.x -= 1;
		out[0].set(pos);
		pos.x += 2;
		out[1].set(pos);

		pos.x = x;
		pos.y -= 1;
		out[2].set(pos);
		pos.y += 2 * Element.BLOCK_SIZE;
		out[3].set(pos);

		pos.y = y;
		pos.z -= 1;
		out[4].set(pos);
		pos.z += 2;
		out[5].set(pos);

		pos.set(x, y, z);

		return out;
	}


	//	public void attachContext(CubeData cubeData) {
	//		if(!attached.contains(cubeData)){
	//			attached.add(cubeData);
	//		}else{
	//		}
	//	}

	public static void main(String[] args) {
		float[] t = new float[8];

		//		vec2 quad = vec2(mod(floor(mTex * 4.0), 2.0), mod(floor(mTex * 2.0), 2.0));
		for (int i = 0; i < 8; i++) {
			t[i] = i / 8f;
			System.err.println("FF: " + t[i]);
			//			System.err.println("MM "+(((int)(t[i]*4 )%2)*-()));
			//			System.err.println("MM "+(int)(t[i]*4 + 4));
			//			System.err.println("M8 "+((int)(t[i]*8))%3);
			//			System.err.println("M8 "+((int)(t[i]*8))%2);
			//			System.err.println("M8 "+((int)(t[i]*4))%2);
		}

	}
	//	public void contextRefit() {
	//		container.dataBuffer.limit(container.dataBuffer.capacity());
	//		container.dataBuffer.rewind();
	//		for(int i = 0; i < attached.size(); i++){
	//			for(float f : attached.get(i).currentData){
	//				container.dataBuffer.put(f);
	//			}
	//		}
	//
	//		contextSwitch(container.dataBuffer);
	//
	//		//		System.err.println(id+" REFIT: "+container.dataBuffer.limit()+"; ATTACHED "+attached.size());
	//	}

	/**
	 * @return the gl_ARB_map_buffer_range
	 */
	public static boolean isGl_ARB_map_buffer_range() {
		return gl_ARB_map_buffer_range;
	}

	/**
	 * @param gl_ARB_map_buffer_range the gl_ARB_map_buffer_range to set
	 */
	public static void setGl_ARB_map_buffer_range(boolean gl_ARB_map_buffer_range) {
		CubeMeshNormal.gl_ARB_map_buffer_range = gl_ARB_map_buffer_range;
	}

	/**
	 * Clean up.
	 *
	 * @param gl the gl
	 * @
	 */
	@Override
	public void cleanUp() {
		//		super.cleanUp( );

		GL15.glDeleteBuffers(attibuteBufferId);
		CubeMeshNormal.initializedBuffers--;
		initialized = false;
	}

	@Override
	public void contextSwitch(CubeMeshBufferContainer container, int[][] opaqueRanges, int[][] blendedRanges, int blendedElementsCount, int totalDrawnBlockCount, DrawableRemoteSegment segment) {
		if (!checkedForRangeMap) {
			gl_ARB_map_buffer_range = GraphicsContext.current.getCapabilities().GL_ARB_map_buffer_range;
			System.err.println("USE BUFFER RANGE: " + gl_ARB_map_buffer_range);
			checkedForRangeMap = true;
		}
		boolean init = false;

		container.dataBuffer.make();
		container.dataBuffer.getTotalBuffer().flip();

		if (!initialized) {
			prepare(((CubeBufferFloat)container.dataBuffer).totalBuffer);
			init = true;
		}

		if (container.dataBuffer.getTotalBuffer().limit() != 0) {

			long tPut = 0;
			long tOrder = 0;
			long tUnmap = 0;
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attibuteBufferId); // Bind

			if (currentVBOSize != container.dataBuffer.getTotalBuffer().limit()) {
				GameClientState.realVBOSize -= currentVBOSize * 4;
				GameClientState.realVBOSize += container.dataBuffer.getTotalBuffer().limit() * 4;
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, container.dataBuffer.getTotalBuffer().limit() * 4, BUFFER_FLAG); // Load The Data
				currentVBOSize = container.dataBuffer.getTotalBuffer().limit();
			}

			long tStart = System.nanoTime();
			if (CubeMeshNormal.USE_MAP_BUFFER) {
				if (gl_ARB_map_buffer_range) {
					oldHelpBuffer = ARBMapBufferRange.glMapBufferRange(
							GL15.GL_ARRAY_BUFFER,
							0,
							container.dataBuffer.getTotalBuffer().limit() * ByteUtil.SIZEOF_FLOAT,
							mappingByte,
							oldHelpBuffer == null ? null : oldHelpBuffer);
				} else {
					oldHelpBuffer = GL15.glMapBuffer(
							GL15.GL_ARRAY_BUFFER,
							GL15.GL_WRITE_ONLY,
							oldHelpBuffer == null ? null : oldHelpBuffer);
				}
				if (oldHelpBuffer == null && CubeMeshNormal.USE_MAP_BUFFER) {
					CubeMeshNormal.USE_MAP_BUFFER = false;
					System.err.println("[Exception]WARNING: MAPPED BUFFER HAS BEEN TURNED OFF " + GlUtil.getGlError());
				}
			}
			long tBindAD = System.nanoTime() - tStart;
			boolean glUnmapBuffer = false;
			if (CubeMeshNormal.USE_MAP_BUFFER) {
				long t = System.nanoTime();
				FloatBuffer mapBuffer = oldHelpBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
				;
				tOrder = (System.nanoTime() - t) / 1000000;
				;

				t = System.nanoTime();
				mapBuffer.put(((CubeBufferFloat)container.dataBuffer).totalBuffer.getBackingToCurrent());
				tPut = (System.nanoTime() - t) / 1000000;

				mapBuffer.flip();
				t = System.nanoTime();
				glUnmapBuffer = GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
				tUnmap = (System.nanoTime() - t) / 1000000;

			} else {
				//DO BUFFER SUB DATA UPDATE
				GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, ((CubeBufferFloat)container.dataBuffer).totalBuffer.getByteBackingToCurrent());// Load The Data
			}

			//		System.err.println("DDDD !!!!!!!!!! iB: "+container.indexBuffer.position()+" / "+container.indexBuffer.limit()+"; aB: "+container.attibuteBuffer.position()+" / "+container.attibuteBuffer.limit()+";buID "+getIndexBufferId(0)+"; atID: "+getAttributeBufferId(0));
			CubeMeshNormal.bufferContextSwitchTime = System.nanoTime() - tStart;

			CubeMeshNormal.bufferContextSwitchTime /= 1000000;
			if (CubeMeshNormal.bufferContextSwitchTime > 10) {
				System.err.println("[CUBE] WARNING: context switch time: " + CubeMeshNormal.bufferContextSwitchTime + " ms : " + tBindAD / 1000000 + "ms: O " + tOrder + "; P " + tPut + "; U " + tUnmap + "::; map " + CubeMeshNormal.USE_MAP_BUFFER + "; range " + gl_ARB_map_buffer_range + "; init " + init + "  unmap " + glUnmapBuffer);
			}
		} else {
			//			System.err.println("[CUBEMESH] WARNING: tried to switch to empty mesh");
		}

		vertexBufferEndPos = container.dataBuffer.getTotalBuffer().limit() / CubeMeshBufferContainer.vertexComponents;

		//		System.err.println("CONTEXT SWITCH: "+vertexBufferEndPos+": "+container.dataBuffer.limit());
	}

	@Override
	public void draw(int blended, int vis) {
		timeForQuery = 0;
		statePrepareTime = 0;
		timeForDrawVisible = 0;
		timeForDrawOccluded = 0;
		timeForContextSwitch = 0;
		timeForContextUniforms = 0;
		createContextSwitchTime = 0;
		bufferContextSwitchTime = 0;
		timeForActualDraw = 0;
		time = System.nanoTime();
		drawMesh(blended, vis);

	}

	@Override
	public void drawMesh(int blended, int vis) {
		if (!initialized) {
			return;
		}

		visibleMeshes++;
		drawCubeVBO();
		timeForDrawVisible = (int) (System.nanoTime() - time);

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

	//	private void drawSide(int side, boolean blended , DrawableRemoteSegment seg){

	//
	//
	//		long time = System.nanoTime();
	////		GlUtil.updateShaderInt(ShaderLibrary.cubeShader13, "sideId", side);
	//		timeForContextUniforms += System.nanoTime()-time;
	//
	//
	//		time = System.nanoTime();
	//
	//		int bufferOffset = 0;
	//		if(!blended){
	//			bufferOffset = vertexStartPerSide[side];
	//		}else{
	//			bufferOffset = blendedvertexStartPerSide[side];
	//		}
	//		GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, bufferOffset * ByteUtil.SIZEOF_FLOAT);
	//
	//		timeForDrawVisible += System.nanoTime()-time;
	//
	//		time = System.nanoTime();
	//
	//		if(blended){
	//			GL11.glDrawArrays(GL11.GL_QUADS, 0, blendedvertexCountPerSide[side]);
	//		}else{
	//			GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCountPerSide[side]);
	//		}
	//		timeForActualDraw += System.nanoTime()-time;

	//	}

	/**
	 * @return the initialized
	 */
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	//	static ShortBuffer sb = BufferUtils.createShortBuffer(Short.MAX_VALUE);
	//	static FloatBuffer fb = MemoryUtil.memAllocFloat(Short.MAX_VALUE);
	@Override
	public void prepare(ManagedMemoryChunk buffer) {
		//		this.setType(Mesh.TYPE_VERTEX_BUFFER_OBJ);
		IntBuffer attibuteBufferIds = MemoryUtil.memAllocInt(1);

		GlUtil.getIntBuffer1();

		CubeMeshNormal.initializedBuffers++;

		attibuteBufferIds.rewind();
		GL15.glGenBuffers(attibuteBufferIds); // Get A Valid Name

		attibuteBufferId = attibuteBufferIds.get(0);

		Controller.loadedVBOBuffers.add(attibuteBufferId);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attibuteBufferId); // Bind
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.limit() * 4, BUFFER_FLAG); // Load The Data

		GameClientState.realVBOSize += buffer.limit() * 4;
		GameClientState.prospectedVBOSize += (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents)
				* ByteUtil.SIZEOF_FLOAT;

		currentVBOSize = buffer.limit();

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind

		initialized = true;
	}

	/**
	 * @param initialized the initialized to set
	 */
	@Override
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public void released() {
		//nothing to do
	}
	//	private int getOpaqueSize() {
	//		int s = 0;
	//		for(int i = 0; i < attached.size(); i++){
	//			s += attached.get(i).getBlendBufferPos();
	//		}
	//		return s;
	//	}

	/*
	private void drawAllSides(boolean blended, DrawableRemoteSegment seg){
		int side = -1;
		int segmentHalfDim = SegmentData.SEG_HALF;
		if(currentCamPos.x < segPos.x + segmentHalfDim){
			side = SimplePosElement.LEFT;
			drawSide(side,blended, seg);
		}
		if(currentCamPos.x > segPos.x - segmentHalfDim){
			side = SimplePosElement.RIGHT;
			drawSide(side,blended, seg);
		}
		if(currentCamPos.y < segPos.y + segmentHalfDim){
			side = SimplePosElement.BOTTOM;
			drawSide(side,blended, seg);
		}
		if(currentCamPos.y > segPos.y - segmentHalfDim){
			side = SimplePosElement.TOP;
			drawSide(side,blended, seg);
		}
		if(currentCamPos.z < segPos.z + segmentHalfDim){
			side = SimplePosElement.BACK;
			drawSide(side,blended, seg);
		}
		if(currentCamPos.z > segPos.z - segmentHalfDim){
			side = SimplePosElement.FRONT;
			drawSide(side,blended, seg);
		}
	}
	 */
	public void drawAsBoundingBox() {
		GlUtil.glEnableClientState(GL11.GL_VERTEX_ARRAY); // Enable
		// Vertex
		// Arrays

	}

	public void drawBB() {
	}
	//	public void orphan() {
	//		//		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, getIndexBufferId(0)); // Bind
	//		//		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, CubeInfo.INDEX_BUFFER_SIZE
	//		//				* ByteUtil.SIZEOF_SHORT, GL15.GL_DYNAMIC_DRAW);// Load The Data
	//		//		GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); // Bind
	//
	//		GlUtil.printGlErrorCritical();
	//
	//		GL15.glGenBuffers(attibuteBufferIds); // Get A Valid Name
	//		Controller.loadedVBOBuffers.add(attibuteBufferId);
	//		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attibuteBufferId); // Bind
	//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER,  (CubeInfo.INDEX_BUFFER_SIZE * CubeMeshBufferContainer.vertexComponents)
	//				* ByteUtil.SIZEOF_FLOAT, BUFFER_FLAG); // Load The Data
	//		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Bind
	//
	//	}

	private void drawComplete() {

		//
		//		long time = System.nanoTime();
		//		timeForContextUniforms += System.nanoTime()-time;
		//
		//
		//		time = System.nanoTime();
		if(GraphicsContext.INTEGER_VERTICES){
			GlUtil.glVertexAttribIPointer(0, CubeMeshBufferContainer.vertexComponents, GL11.GL_INT, 0, 0);
		}else{
			GL11.glVertexPointer(CubeMeshBufferContainer.vertexComponents, GL11.GL_FLOAT, 0, 0);
		}

		//		timeForDrawVisible += System.nanoTime()-time;
		//
		//		time = System.nanoTime();

		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexBufferEndPos);

		//		timeForActualDraw += System.nanoTime()-time;

	}
	//	public void removeContext(CubeData cubeData) {
	//		attached.remove(cubeData);
	//		if(attached.isEmpty()){
	//			segController = null;
	//		}
	//	}

	private void drawCubeVBO() {

		long time = System.nanoTime();

		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, attibuteBufferId);

		//		if(SegmentDrawer.seperateDrawing){
		//			if(SegmentDrawer.opaquePass){
		//				drawSeperateOpaque();
		//				//TODO blendedElementsCount > 0 mark to minimize re-render queue
		//			}else if(blendedElementsCount > 0){
		//				drawSeperateBlend();
		//			}
		//		}else{
		drawComplete();
		//		}
		this.statePrepareTime += (System.nanoTime() - time);

	}

	//	public boolean fits() {
	//		int oldSize = getOpaqueSize();
	//		//		System.err.println("OLD SIZE: "+oldSize+" / "+MAX_SIZE);
	//		return oldSize < CubeData.MAX_SIZE;
	//	}
	//	public boolean fitsAdditional(CubeData data) {
	//
	//		if(attached.size() >= CubeData.MAX_ATTACHED){
	//			return false;
	//		}
	//
	//		int oldSize = getOpaqueSize();
	//
	//
	//		return oldSize+data.getBlendBufferPos() < CubeData.MAX_SIZE;
	//	}
	public int getAttributeBufferId() {
		return attibuteBufferId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return id == ((CubeMeshNormal) obj).id;
	}

	@Override
	public String toString() {
		return super.toString() + ":" + hashCode();
	}

}
