package org.schema.game.client.data.terrain.geimipmap;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.game.client.data.terrain.BufferGeomap;
import org.schema.game.client.data.terrain.LODLoadableInterface;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.Mesh;

/**
 * Produces the cubeMeshes for the TerrainPatch.
 * This LOD algorithm generates a single triangle strip by first building the center of the
 * cubeMeshes, minus one outer edge around it. Then it builds the edges in counter-clockwise order,
 * starting at the bottom right and working up, then left across the top, then down across the
 * left, then right across the bottom.
 * It needs to know what its neighbour's LOD's are so it can stitch the edges.
 * It creates degenerate polygons in order to keep the winding order of the polygons and to move
 * the strip to a new position while still maintaining the continuity of the overall cubeMeshes. These
 * degenerates are removed quickly by the video card.
 *
 * @author Brent Owens
 */
public class LODGeomap extends BufferGeomap {

	public static boolean LOAD_IMIDIATELY = false;
	private static int count;
	@SuppressWarnings("unused")
	private final int maxLod;
	private TerrainPatchLoader terrainLoader;
	private LODLoadableInterface terrain;

	public LODGeomap(int size, FloatBuffer heightMap, LODLoadableInterface terrain) {
		super(heightMap, null, size, size, 1);
		this.terrain = terrain;
		maxLod = Math.max(1, (int) (FastMath.log(size - 1) / FastMath.log(2)) - 1);
	}

	/**
	 * calculate how many indexes there will be.
	 * This isn'transformationArray that precise and there might be a couple extra.
	 */
	private int calculateNumIndexesLodDiff(int lod) {

		int length = getWidth() - 1; // make it even for lod calc
		int side = (length / lod) + 1 - (2);
		//System.out.println("side: "+side);
		int num = side * side * 2;
		//System.out.println("num: "+num);
		num -= 2 * side;    // remove one first row and one last row (they are only hit once each)
		//System.out.println("num2: "+num);
		// now get the degenerate indexes that exist between strip rows
		int degenerates = 2 * (side - (2)); // every row except the first and last
		num += degenerates;
		//System.out.println("degenerates: "+degenerates);

		//System.out.println("center, before edges: "+num);

		num += (getWidth() / lod) * 2 * 4;
		num++;

		num += 10;// TODO remove me: extra
		//System.out.println("Index bufferList size: "+num);
		return num;
	}

	public Mesh createMesh(Vector3f scale, Vector2f tcScale, Vector2f tcOffset, Vector3f stepScale, float offsetAmount, int totalSize, boolean center, int lod, boolean rightLod, boolean topLod, boolean leftLod, boolean bottomLod, TerrainPatch patch) {
		System.err.println("loading cubeMeshes for patch " + count++);
		Mesh m = new Mesh();
		if (LOAD_IMIDIATELY) {

			this.terrainLoader = new TerrainPatchLoader(scale, tcScale, tcOffset, stepScale, offsetAmount, totalSize, center, lod, rightLod, topLod, leftLod, bottomLod, m, patch);
			this.terrainLoader.loadMesh();
		} else {
			this.terrainLoader = new TerrainPatchLoader(scale, tcScale, tcOffset, stepScale, offsetAmount, totalSize, center, lod, rightLod, topLod, leftLod, bottomLod, m, patch);
			terrain.getGeoMapsToLoad().add(this);
		}
		return m;
	}

	public Mesh createMesh(Vector3f scale, Vector2f tcScale, Vector2f tcOffset, Vector3f stepScale, float offsetAmount, int totalSize, boolean center, TerrainPatch patch) {
		return this.createMesh(scale, tcScale, tcOffset, stepScale, offsetAmount, totalSize, center, 1, false, false, false, false, patch);
	}

	public void doLoadStep() {
		terrainLoader.loadMesh();
		//		if(terrainLoader.m.isLoaded()){
		//			terrainLoader = null;
		//		}
	}

	public Vector2f getUV(int x, int y, Vector2f store, Vector2f offset, float offsetAmount, int totalSize, Vector3f stepScale) {

		float offsetX = offset.x / stepScale.x + (offsetAmount); //*stepScale.x
		float offsetY = offset.y / stepScale.z + (offsetAmount); //*stepScale.y

		store.set(((x) + offsetX) / totalSize, // calculates percentage of texture here
				((y) + offsetY) / totalSize);
		//        System.err.println("("+x+" + ("+offset.x/stepScale.x+"+"+offsetAmount+")) / "+totalSize+" = "+store.x);
		return store;
	}

	public boolean isMeshLoaded() {
		return terrainLoader.m.isLoaded();
	}

	/**
	 * Create the LOD index array that will seam its edges with its neighbour's LOD.
	 * This is a scary method!!! It will break your mind.
	 *
	 * @param store     to store the index bufferList
	 * @param lod       level of detail of the cubeMeshes
	 * @param rightLod  LOD of the right neighbour
	 * @param topLod    LOD of the top neighbour
	 * @param leftLod   LOD of the left neighbour
	 * @param bottomLod LOD of the bottom neighbour
	 * @return the LOD-ified index bufferList
	 */
	public IntBuffer writeIndexArrayLodDiff(IntBuffer store, int lod, boolean rightLod, boolean topLod, boolean leftLod, boolean bottomLod) {

		IntBuffer buffer2 = store;
		int numIndexes = calculateNumIndexesLodDiff(lod);
		if (store == null) {
			buffer2 = MemoryUtil.memAllocInt(numIndexes);
		}
		VerboseIntBuffer buffer = new VerboseIntBuffer(buffer2);

		// generate center squares minus the edges
		//System.out.println("for (x="+lod+"; x<"+(getWidth()-(2*lod))+"; x+="+lod+")");
		//System.out.println("	for (z="+lod+"; z<"+(getWidth()-(1*lod))+"; z+="+lod+")");
		for (int r = lod; r < getWidth() - (2 * lod); r += lod) { // row
			int rowIdx = r * getWidth();
			int nextRowIdx = (r + 1 * lod) * getWidth();
			for (int c = lod; c < getWidth() - (1 * lod); c += lod) { // column
				int idx = rowIdx + c;
				buffer.put(idx);
				idx = nextRowIdx + c;
				buffer.put(idx);
			}

			// add degenerate triangles
			if (r < getWidth() - (3 * lod)) {
				int idx = nextRowIdx + getWidth() - (1 * lod) - 1;
				buffer.put(idx);
				idx = nextRowIdx + (1 * lod); // inset by 1
				buffer.put(idx);
				//System.out.println("");
			}
		}
		//System.out.println("\nright:");

		//int runningBufferCount = bufferList.getCount();
		//System.out.println("bufferList start: "+runningBufferCount);

		// right
		int br = getWidth() * (getWidth() - lod) - 1 - lod;
		buffer.put(br); // bottom right -1
		int corner = getWidth() * getWidth() - 1;
		buffer.put(corner);    // bottom right corner
		if (rightLod) { // if lower LOD
			for (int row = getWidth() - lod; row >= 1 + lod; row -= 2 * lod) {
				int idx = (row) * getWidth() - 1 - lod;
				buffer.put(idx);
				idx = (row - lod) * getWidth() - 1;
				buffer.put(idx);
				if (row > lod + 1) { //if not the last one
					idx = (row - lod) * getWidth() - 1 - lod;
					buffer.put(idx);
					idx = (row - lod) * getWidth() - 1;
					buffer.put(idx);
				} else {

				}
			}
		} else {
			buffer.put(corner);//br+1);//degenerate to flip winding order
			for (int row = getWidth() - lod; row > lod; row -= lod) {
				int idx = row * getWidth() - 1; // mult to get row
				buffer.put(idx);
				buffer.put(idx - lod);
			}

		}

		buffer.put(getWidth() - 1);

		//System.out.println("\nbuffer right: "+(bufferList.getCount()-runningBufferCount));
		//runningBufferCount = bufferList.getCount();

		//System.out.println("\ntop:");

		// top 			(the order gets reversed here so the diagonals line up)
		if (topLod) { // if lower LOD
			if (rightLod) {
				buffer.put(getWidth() - 1);
			}
			for (int col = getWidth() - 1; col >= lod; col -= 2 * lod) {
				int idx = (lod * getWidth()) + col - lod; // next row
				buffer.put(idx);
				idx = col - 2 * lod;
				buffer.put(idx);
				if (col > lod * 2) { //if not the last one
					idx = (lod * getWidth()) + col - 2 * lod;
					buffer.put(idx);
					idx = col - 2 * lod;
					buffer.put(idx);
				} else {

				}
			}
		} else {
			if (rightLod) {
				buffer.put(getWidth() - 1);
			}
			for (int col = getWidth() - 1 - lod; col > 0; col -= lod) {
				int idx = col + (lod * getWidth());
				buffer.put(idx);
				idx = col;
				buffer.put(idx);
			}
			buffer.put(0);
		}
		buffer.put(0);

		//System.out.println("\nbuffer top: "+(bufferList.getCount()-runningBufferCount));
		//runningBufferCount = bufferList.getCount();

		//System.out.println("\nleft:");

		// left
		if (leftLod) { // if lower LOD
			if (topLod) {
				buffer.put(0);
			}
			for (int row = 0; row < getWidth() - lod; row += 2 * lod) {
				int idx = (row + lod) * getWidth() + lod;
				buffer.put(idx);
				idx = (row + 2 * lod) * getWidth();
				buffer.put(idx);
				if (row < getWidth() - lod - 2 - 1) { //if not the last one
					idx = (row + 2 * lod) * getWidth() + lod;
					buffer.put(idx);
					idx = (row + 2 * lod) * getWidth();
					buffer.put(idx);
				} else {

				}
			}
		} else {
			if (!topLod) {
				buffer.put(0);
			}
			//bufferList.put(getWidth()+1); // degenerate
			//bufferList.put(0); // degenerate winding-flip
			for (int row = lod; row < getWidth() - lod; row += lod) {
				int idx = row * getWidth();
				buffer.put(idx);
				idx = row * getWidth() + lod;
				buffer.put(idx);
			}

		}
		buffer.put(getWidth() * (getWidth() - 1));

		//System.out.println("\nbuffer left: "+(bufferList.getCount()-runningBufferCount));
		//runningBufferCount = bufferList.getCount();

		//if (true) return bufferList.delegate;
		//System.out.println("\nbottom");

		// bottom
		if (bottomLod) { // if lower LOD
			if (leftLod) {
				buffer.put(getWidth() * (getWidth() - 1));
			}
			// there was a slight bug here when really high LOD near maxLod
			// far right has extra index one row up and all the way to the right, need to skip last index entered
			// seemed to be fixed by making "getWidth()-1-2-lod" this: "getWidth()-1-2*lod", which seems more correct
			for (int col = 0; col < getWidth() - lod; col += 2 * lod) {
				int idx = getWidth() * (getWidth() - 1 - lod) + col + lod;
				buffer.put(idx);
				idx = getWidth() * (getWidth() - 1) + col + 2 * lod;
				buffer.put(idx);
				if (col < getWidth() - 1 - 2 * lod) { //if not the last one
					idx = getWidth() * (getWidth() - 1 - lod) + col + 2 * lod;
					buffer.put(idx);
					idx = getWidth() * (getWidth() - 1) + col + 2 * lod;
					buffer.put(idx);
				} else {

				}
			}
		} else {
			if (leftLod) {
				buffer.put(getWidth() * (getWidth() - 1));
			}
			for (int col = lod; col < getWidth() - lod; col += lod) {
				int idx = getWidth() * (getWidth() - 1 - lod) + col; // up
				buffer.put(idx);
				idx = getWidth() * (getWidth() - 1) + col; // down
				buffer.put(idx);
			}
			//bufferList.put(getWidth()*getWidth()-1-lod); // <-- THIS caused holes at the end!
		}

		buffer.put(getWidth() * getWidth() - 1);

		//System.out.println("\nbuffer bottom: "+(bufferList.getCount()-runningBufferCount));
		//runningBufferCount = bufferList.getCount();

		//System.out.println("\nBuffer size: "+bufferList.getCount());

		// fill in the rest of the bufferList with degenerates, there should only be a couple
		for (int i = buffer.getCount(); i < numIndexes; i++) {
			buffer.put(getWidth() * getWidth() - 1);
		}

		return buffer.delegate;
	}

	public FloatBuffer writeTexCoordArray(FloatBuffer store, Vector2f offset, Vector2f scale, float offsetAmount, int totalSize, Vector3f stepScale) {
		if (store != null) {
			if (store.remaining() < getWidth() * getHeight() * 2) {
				throw new BufferUnderflowException();
			}
		} else {
			store = MemoryUtil.memAllocFloat(getWidth() * getHeight() * 2);
		}

		if (offset == null) {
			offset = new Vector2f();
		}

		Vector2f tcStore = new Vector2f();
		System.err.println(offset + ", " + offsetAmount + ", " + scale);
		for (int y = 0; y < getHeight(); y++) {

			for (int x = 0; x < getWidth(); x++) {
				getUV(x, y, tcStore, offset, offsetAmount, totalSize, stepScale);
				//				System.err.println(tcStore);
				float tx = tcStore.x * scale.x;
				float ty = tcStore.y * scale.y;
				store.put(tx);
				store.put(ty);
			}
		}

		return store;
	}


	/*private int calculateNumIndexesNormal(int lod) {
		int length = getWidth()-1;
		int num = ((length/lod)+1)*((length/lod)+1)*2;
		System.out.println("num: "+num);
		num -= 2*((length/lod)+1);
		System.out.println("num2: "+num);
		// now get the degenerate indexes that exist between strip rows
		num += 2*(((length/lod)+1)-2); // every row except the first and last
		System.out.println("Index bufferList size: "+num);
		return num;
	}*/

	private class TerrainPatchLoader {

		int phase = 0;
		private Mesh m;
		private FloatBuffer pb;
		private FloatBuffer tb;
		private FloatBuffer nb;
		private IntBuffer ib;
		private TerrainPatch patch;
		private Vector3f scale;
		private Vector2f tcScale;
		private Vector2f tcOffset;
		private Vector3f stepScale;
		private float offsetAmount;
		private int totalSize;
		private boolean center;
		private int lod;
		private boolean rightLod;
		private boolean topLod;
		private boolean leftLod;
		private boolean bottomLod;

		public TerrainPatchLoader(Vector3f scale, Vector2f tcScale,
		                          Vector2f tcOffset, Vector3f stepScale, float offsetAmount,
		                          int totalSize, boolean center, int lod, boolean rightLod,
		                          boolean topLod, boolean leftLod, boolean bottomLod, Mesh m, TerrainPatch patch) {
			super();
			this.scale = scale;
			this.tcScale = tcScale;
			this.tcOffset = tcOffset;
			this.stepScale = stepScale;
			this.offsetAmount = offsetAmount;
			this.totalSize = totalSize;
			this.center = center;
			this.lod = lod;
			this.rightLod = rightLod;
			this.topLod = topLod;
			this.leftLod = leftLod;
			this.bottomLod = bottomLod;
			this.m = m;
			this.patch = patch;

		}

		public void loadMesh() {
			//			FloatBuffer pb = writeVertexArray(null, scale, center);
			//			FloatBuffer tb = writeTexCoordArray(null, tcOffset, tcScale, offsetAmount, totalSize, stepScale );
			//			FloatBuffer nb = writeNormalArray(null, scale);
			//			IntBuffer ib = writeIndexArrayLodDiff(null, lod, rightLod, topLod, leftLod, bottomLod);
			//
			//			m.setBuffer(Mesh.BUFFERTYPE_Position, 3, pb);
			//			m.setBuffer(Mesh.BUFFERTYPE_Normal, 3, nb);
			//			m.setBuffer(Mesh.BUFFERTYPE_TexCoord, 2, tb);
			//			m.setBuffer(Mesh.BUFFERTYPE_Index, 3, ib);
			//			m.setFaceCount(ib.capacity()/3);
			//			m.updateBound();
			//			m.setType(Mesh.TYPE_VERTEX_BUFFER_OBJ);
			//			m.setDrawMode(GL11.GL_TRIANGLE_STRIP);
			//			m.setLoaded(true);

			//			Mesh m = new Mesh();

			//			m.setMode(Mode.TriangleStrip);

			//			m.setStatic();
			switch(phase) {
				case (0) -> {
					System.err.println("writingToDiskLock vertex array");
					pb = writeVertexArray(null, scale, center);
					break;
				}
				case (1) -> {
					System.err.println("writingToDiskLock noiseVolume array");
					tb = writeTexCoordArray(null, tcOffset, tcScale, offsetAmount, totalSize, stepScale);
					break;
				}
				case (2) -> {
					System.err.println("writingToDiskLock normal array");
					nb = writeNormalArray(null, scale);
					break;
				}
				case (3) -> {
					System.err.println("writingToDiskLock index array");
					ib = writeIndexArrayLodDiff(null, lod, rightLod, topLod, leftLod, bottomLod);
					break;
				}
				case (4) -> {
					System.err.println("setting vertex array");
					m.setBuffer(Mesh.BUFFERTYPE_Position, 3, pb);
					break;
				}
				case (5) -> {
					System.err.println("setting normal array");
					m.setBuffer(Mesh.BUFFERTYPE_Normal, 3, nb);
					break;
				}
				case (6) -> {
					System.err.println("setting noiseVolume array");
					m.setBuffer(Mesh.BUFFERTYPE_TexCoord, 2, tb);
					break;
				}
				case (7) -> {
					System.err.println("setting index array");
					m.setBuffer(Mesh.BUFFERTYPE_Index, 3, ib);
					break;
				}
				case (8) -> {
					System.err.println("updating bounds");
					m.updateBound();
					m.setFaceCount(ib.capacity() / 3);
					m.setType(Mesh.TYPE_VERTEX_BUFFER_OBJ);
					m.setDrawMode(GL11.GL_TRIANGLE_STRIP);
					m.setLoaded(true);
					hdata = null;
					ndata = null;
					pb = null;
					nb = null;
					tb = null;
					ib = null;
					patch.updateModelBound();
					GlUtil.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
					break;
				}
			}
			phase++;
		}

	}

	/**
	 * Keeps a count of the number of indexes, good for debugging
	 */
	public class VerboseIntBuffer {
		int count = 0;
		private IntBuffer delegate;

		public VerboseIntBuffer(IntBuffer d) {
			delegate = d;
		}

		public int getCount() {
			return count;
		}

		public void put(int value) {
			try {
				//System.out.print(value+",");
				delegate.put(value);
				count++;
			} catch (BufferOverflowException e) {
				//System.out.println("err bufferList size: "+delegate.capacity());
			}
		}
	}

}

