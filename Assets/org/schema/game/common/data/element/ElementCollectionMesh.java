package org.schema.game.common.data.element;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.FastEntrySet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.Test;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.shader.CubeMeshQuadsShader13;
import org.schema.game.common.data.world.Segment;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;
import org.schema.schine.input.Keyboard;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ElementCollectionMesh{
	public static final int FACE_COUNT = 6;
	public static final int VERTS_PER_FACE = 6; //2 triangles
	public static final int VERT_DIMENSION = USE_INT_ATT() ? 2 : 3;
	public static final int TYPE_BYTE_COUNT = 4;
	public static final int VERT_ATTRIB_INDEX = ShaderLibrary.ElementCollectionMesh_VERT_ATTRIB_INDEX;
	private static final Vector4f DEFAULT_COLOR = new Vector4f(1f,1f,1f,0.2f);
	private static final int THREADS = 5;
	private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS, r -> new Thread(r, "CollectionMeshCreationThread"));
	public static final List<LongOpenHashSet> setPool = new ObjectArrayList<LongOpenHashSet>();
	static{
		for(int i = 0; i < 64; i++){
			setPool.add(new LongOpenHashSet(1024));
		}
		//preheat
		for(int i = 0; i < THREADS; i++) {
			threadPool.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}
	public static void freeSet(LongOpenHashSet s){
		s.clear();
		synchronized(setPool){
			setPool.add(s);
			setPool.notifyAll();
		}
		
	}
	public static LongOpenHashSet getSet(){
		synchronized(setPool){
			while(setPool.isEmpty()){
				try {
					setPool.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return setPool.remove(setPool.size()-1);
		}
		
	}
	private ByteBuffer buffer;
	private final ByteArrayList vises = new ByteArrayList();
	private final LongArrayList poses = new LongArrayList();
	private float[] triangles;
	private Vector3i posTmp = new Vector3i();
	private boolean init;
	private boolean startedInit;
	private boolean inInit;
	private int rawDataSize;
	private int bufferId;
	private int rawVerticesCount;
	private Vector3f localMin = new Vector3f();
	private Vector3f localMax = new Vector3f();
	private Vector3f min = new Vector3f();
	private Vector3f max = new Vector3f();
	private final LongOpenHashSet filled = new LongOpenHashSet();
	private final Long2ByteOpenHashMap pContain = new Long2ByteOpenHashMap();
	private boolean draw;
	private final Vector4f color = new Vector4f(DEFAULT_COLOR);
	private OptimizedMesh optimizedMesh;
	private int optTriangleFloatCount;
	private Vector3f firstVertex;
	public static final DrawMode DRAW_MODE = DrawMode.OPTIMIZED;
	public static DebugModes dbm = new DebugModes();
	public static int meshesInUse;
	public static class DebugModes{
		public boolean removeInner = true;
		public boolean removeColinear = true;
		public boolean triangulate = true;
		public boolean quadrulate = true;
		public boolean retriangulate = true;
		private int step;
		
		public boolean step(int step){
			boolean changed = this.step != step; 
			this.step = step;
			switch(step) {
				case 0 -> step0();
				case 1 -> step1();
				case 2 -> step2();
				case 3 -> step3();
				case 4 -> step4();
				case 5 -> step5();
			}

			return changed;
		}
		
		public void step0(){
			removeInner = false;
			removeColinear = false;
			triangulate = false;
			quadrulate = false;
			retriangulate = false;
		}
		public void step1(){
			removeInner = true;
			removeColinear = false;
			triangulate = false;
			quadrulate = false;
			retriangulate = false;
		}
		public void step2(){
			removeInner = true;
			removeColinear = true;
			triangulate = false;
			quadrulate = false;
			retriangulate = false;
		}
		public void step3(){
			removeInner = true;
			removeColinear = true;
			triangulate = true;
			quadrulate = false;
			retriangulate = false;
		}
		public void step4(){
			removeInner = true;
			removeColinear = true;
			triangulate = true;
			quadrulate = true;
			retriangulate = false;
		}
		public void step5(){
			removeInner = true;
			removeColinear = true;
			triangulate = true;
			quadrulate = true;
			retriangulate = true;
		}
	}
	
	public static final List<Side> sidePool = new ObjectArrayList<Side>();
	
	public static void freeSideInstance(Side s){
		s.clear();
		synchronized(sidePool){
			sidePool.add(s);
		}
	}
	public static Side getSideInstance(){
		synchronized(sidePool){
			if(sidePool.isEmpty()){
				return new Side();
			}else{
				return sidePool.remove(sidePool.size()-1);
			}
		}
	}
	private static class VSet{
		
		public static List<VSet> pool = new ObjectArrayList<VSet>();
		public static void freeInst(VSet inst){
			inst.clear();
			synchronized(pool){
				pool.add(inst);
			}
		}
		private void clear() {
			set.clear();
			diagonals.clear();
			groupId = -1;
		}
		public static VSet getInst(){
			synchronized(pool){
				if(!pool.isEmpty()){
					return pool.remove(pool.size()-1);
				}
			}
			return new VSet();
		}
		public static VSet getInst(VSet vSet){
			VSet remove = null;
			synchronized(pool){
				if(!pool.isEmpty()){
					remove = pool.remove(pool.size()-1);
				}
			}
			if(remove == null){
				remove = new VSet();
			}
			remove.set(vSet);
			return remove;
		}
		public void set(VSet vSet) {
			this.set.addAll(vSet.set);
			this.diagonals.addAll(vSet.diagonals);
			this.groupId = vSet.groupId;
		}
		private VSet(VSet vSet) {
			set(vSet);
		}
		private VSet() {
		}
		byte groupId = -1;
		final LongOpenHashSet set = new LongOpenHashSet(4);
		final LongOpenHashSet diagonals = new LongOpenHashSet(4);
	}
	public static boolean isCanceled(Cancelable cancelable, long updateSignture){
		return cancelable != null && cancelable.isCancelled(updateSignture);
	}
	private static class Side{
		private int sideId;
		private int index;
		private final LongArrayList inner = new LongArrayList();
		private final LongArrayList innerCon = new LongArrayList();
		private final LongArrayList open = new LongArrayList();
		private final LongOpenHashSet closed = new LongOpenHashSet();
		private final LongArrayList closedSubSet = new LongArrayList();
		private final long[] lTmp = new long[]{Long.MAX_VALUE, Long.MAX_VALUE};
		private final Long2ObjectOpenHashMap<VSet> pSet = new Long2ObjectOpenHashMap<VSet>();
		
		private GridTriangulator triangulator = new GridTriangulator();
		public void clear() {
			pSet.clear();
			for(VSet s : pSet.values()){
				VSet.freeInst(s);
			}
			inner.clear();
			innerCon.clear();
			closed.clear();
			closedSubSet.clear();
			
			triangulator.clear();
		}
		public static long getIndex(int x, int y){
			return (x & 0xFFFFFFFFL) + ((y & 0xFFFFFFFFL) << 32L);
		}
		public static int getPosX(long index) {
			int x = (int) (index & 0xFFFFFFFFL);
			return x;
		}

		public static int getPosY(long index) {
			int y = (int) (index >> 32L & 0xFFFFFFFFL);
			return y;
		}
		@Test
		public static void test(){
			for(int x = -32000; x < 32000; x++){
				for(int y = -32000; y < 32000; y++){
					long index = getIndex(x, y);
					
					assert(x == getPosX(index)):"X "+x+" != "+getPosX(index)+"; "+index+"; ("+x+", "+y+")";
					assert(y == getPosY(index)):"Y "+y+" != "+getPosY(index)+"; "+index+"; ("+x+", "+y+")";
				}
			}
		}
		private void markInnerVertices() {
			LongIterator iterator = pSet.keySet().iterator();
			while(iterator.hasNext()){
				long point = iterator.nextLong();
				VSet vSet = pSet.get(point);
				LongOpenHashSet neigh = vSet.set;
				assert(neigh.size() <= 4);
				//has all neighbors. now check if we are orphaned
				
				int x = getPosX(point);
				int y = getPosY(point);
				
				long right = getIndex(x+1, y);
				long left = getIndex(x-1, y);
				long top = getIndex(x, y+1);
				long bottom = getIndex(x, y-1);
				
				long rightTop = getIndex(x+1, y+1);
				long rightBottom = getIndex(x+1, y-1);
				
				long leftTop = getIndex(x-1, y+1);
				long leftBottom = getIndex(x-1, y-1);
				
				
				long topRight = rightTop;
				long topLeft = leftTop;

				long bottomRight = rightBottom;
				long bottomLeft = leftBottom;
				
				
				VSet rSet = pSet.get(right);
				VSet lSet = pSet.get(left);

				VSet tSet = pSet.get(top);
				VSet bSet = pSet.get(bottom);
				
				if(vSet.diagonals.size() == 4){
					//all blocks around this vertex are soild
					inner.add(point);
					
				}else{
					//this is a corner
					
					//mark any conncetions that would be troublesome otherwise
					
					lTmp[0] = Long.MAX_VALUE;
					lTmp[1] = Long.MAX_VALUE;
					/*
					 * do this by checking the neighbors in the same
					 * direction as the original direction. So for e.g.
					 * right we go top then right, as well bottom then
					 * right. if both connections exist the original right
					 * connection is not needed. This removes connections
					 * inbetween 1m wide or long stripes
					 */
					int p = 0;
					//check right
					if(vSet.diagonals.contains(topRight) && vSet.diagonals.contains(bottomRight)){ // check in a C-directioin for right
						p = putInnerCon(point, right, p);
					}
					//check left
					if(vSet.diagonals.contains(topLeft) && vSet.diagonals.contains(bottomLeft)){
						p = putInnerCon(point, left, p);
					}
					//check top
					if(vSet.diagonals.contains(topRight) && vSet.diagonals.contains(topLeft)){
						p = putInnerCon(point, top, p);
					}
					//check bottom
					if(vSet.diagonals.contains(bottomRight) && vSet.diagonals.contains(bottomLeft)){
						p = putInnerCon(point, bottom, p);
					}
					if(p > 0){
						innerCon.add(point);
						innerCon.add(lTmp[0]);
						innerCon.add(lTmp[1]);
					}
				}
			}
		}
		private int putInnerCon(long point, long val, int p) {
			assert(p < 2):p;
			lTmp[p] = val;
			p++;
			return p;
		}
		private void removeColinearVertices() {
			LongIterator iterator = pSet.keySet().iterator();
			while(iterator.hasNext()){
				long point = iterator.nextLong();
				
				int x = getPosX(point);
				int y = getPosY(point);
				VSet vSet = pSet.get(point);
				LongOpenHashSet neigh = vSet.set;
				
				long invalid = Long.MAX_VALUE;
				long top = invalid;
				long bottom = invalid;
				long right = invalid;
				long left = invalid;
				
				for(long s : neigh){
					int nx = getPosX(s);
					int ny = getPosY(s);
					if(nx == x && ny > y){
						top = s;
					}else if(nx == x && ny < y){
						bottom = s;
					}else if(nx > x && ny == y){
						right = s;
					}else if(nx < x && ny == y){
						left = s;
					}
				}
				
				if(top != invalid && bottom != invalid &&
						(left == invalid || right == invalid) ){
					
					//remove from all neighbor sets
					for(long s : neigh){
						LongOpenHashSet otherNeigh = pSet.get(s).set;
						if(otherNeigh != null){
							otherNeigh.remove(point);
						}
					}
					
					//remove the point
					iterator.remove();
					VSet.freeInst(vSet);
					
					//connect top and bottom
					VSet tSet = pSet.get(top);
					if(tSet != null){
						tSet.set.add(bottom);
					}
					VSet bSet = pSet.get(bottom);
					if(bSet != null){
						bSet.set.add(top);
					}
				}else if(left != invalid && right != invalid &&
						(top == invalid || bottom == invalid) ){
					
					//remove from all neighbor sets
					for(long s : neigh){
						VSet otherNeigh = pSet.get(s);
						if(otherNeigh != null){
							otherNeigh.set.remove(point);
						}
					}
					//remove the point
					iterator.remove();
					VSet.freeInst(vSet);
					
					//connect top and bottom
					VSet tSet = pSet.get(right);
					if(tSet != null){
						tSet.set.add(left);
					}
					VSet bSet = pSet.get(left);
					if(bSet != null){
						bSet.set.add(right);
					}
				}
			}
		}
		public void optimize(Cancelable cancelable, long updateSignture) {
			groupVertices(triangulator);
			if(isCanceled(cancelable, updateSignture)){
				return;
			}
			if(dbm.removeInner){
				markInnerVertices();
				if(isCanceled(cancelable, updateSignture)){
					return;
				}
				removeInnerVertices();
				if(isCanceled(cancelable, updateSignture)){
					return;
				}
			}
			if(dbm.removeColinear){
				removeColinearVertices();
				if(isCanceled(cancelable, updateSignture)){
					return;
				}
			}
			if(dbm.triangulate){
				triangulate();
			}
		}
		
		private void groupVertices(GridTriangulator triangulator) {
			closed.clear();
			open.clear();
			open.addAll(pSet.keySet());
			closedSubSet.ensureCapacity(open.size());
			byte id = 1;
			while(!open.isEmpty()){
				
				long p = open.removeLong(open.size()-1);
				if(closed.contains(p)){
					continue;
				}
				closedSubSet.add(p);
				while(!closedSubSet.isEmpty()){
					long sp = closedSubSet.removeLong(closedSubSet.size()-1);
					closed.add(sp);
					VSet vSet = pSet.get(sp);
					if(vSet != null){
						triangulator.pFull.put(sp, VSet.getInst(vSet));
						vSet.groupId = id;
						for(long n : vSet.set){
							if(!closed.contains(n)){
								closedSubSet.add(n);
							}
						}
					}
				}
				id++;
				closedSubSet.clear();
			}
			
			open.clear();
			closed.clear();
		}
		private void triangulate() {
			for(Entry<VSet> s : pSet.long2ObjectEntrySet()){
				triangulator.addPoint(s.getLongKey(), s.getValue());
			}
			
			triangulator.triangulate(sideId, pSet);
			
			
			
		}
		
		private void removeInnerVertices() {
			for(long l : inner){
				//remove the point
				VSet removed = pSet.remove(l);	
				
				if(removed != null){
					//remove from all neighbor sets
					for(long s : removed.set){
						VSet otherNeigh = pSet.get(s);
						if(otherNeigh != null){
							otherNeigh.set.remove(l);
						}
					}
					VSet.freeInst(removed);
				}
			}
			inner.clear();
			
			
			final int iConSize = innerCon.size();
			for(int i = 0; i < iConSize; i+=3){
				long key = innerCon.getLong(i);
				VSet set = pSet.get(key);
				if(set != null){
					set.set.remove(innerCon.getLong(i+1));
					set.set.remove(innerCon.getLong(i+2));
				}
			}
			innerCon.clear();
		}
		
	}
	private static class GridTriangulator{
		private final static boolean CLOCK_WISE[] = new boolean[]{true, false, false, true, true, false}; 
		
		private Long2ObjectOpenHashMap<VSet> pFull = new Long2ObjectOpenHashMap<VSet>();
		private Byte2ObjectOpenHashMap<Long2ObjectOpenHashMap<VSet>> groups = new Byte2ObjectOpenHashMap<Long2ObjectOpenHashMap<VSet>>();
		private LongArrayList pointsToInsert = new LongArrayList();
		private LongArrayList edgesToInsert = new LongArrayList();
		private LongOpenHashSet closed = new LongOpenHashSet();
		private LongArrayList triangles = new LongArrayList();
		public void triangulate(int sideId, Long2ObjectOpenHashMap<VSet> pSet){
			for(Long2ObjectOpenHashMap<VSet> g : groups.values()){
//				assert(checkSanity(g));
				if(dbm.quadrulate){
					quadrulate(g, pSet);
				}
//				assert(checkSanity(g));
				if(dbm.retriangulate){
					triangulateQuads(sideId, g);
				}
			}
		}
		private void triangulateQuads(int sideId, Long2ObjectOpenHashMap<VSet> g) {
			FastEntrySet<VSet> mp = g.long2ObjectEntrySet();
			for(Entry<VSet> e : mp){
				long point = e.getLongKey();
				if(closed.contains(point)){
					continue;
				}
				VSet set = e.getValue();
				
				int x = Side.getPosX(point);
				int y = Side.getPosY(point);
				boolean right = false;
				boolean down = false;
				//check for top left corners
				for(long n : set.set){
					int nx = Side.getPosX(n);
					int ny = Side.getPosY(n);
					
					if(nx == x && ny < y){
						assert(!down);
						down = true;
					}
					if(nx > x && ny == y){
						assert(!right);
						right = true;
					}
				}
				assert(pFull.containsKey(point)):pFull.size();
				if(right && down && !pFull.get(point).diagonals.contains(Side.getIndex(x-1, y-1))){
					//found corner. triangulate clockwise 
					long A = point;
					long B = find(g, A, 1, 0, true, 0, -1);
					long C = find(g, B, 0, -1, true, -1, 0);
					long D = find(g, C, -1, 0, true, 0, 1);
					
					if(CLOCK_WISE[sideId]){
					
						triangles.add(C);
						triangles.add(B);
						triangles.add(A);
						
						triangles.add(D);
						triangles.add(C);
						triangles.add(A);
					}else{
						triangles.add(A);
						triangles.add(B);
						triangles.add(C);
						
						triangles.add(A);
						triangles.add(C);
						triangles.add(D);
					}
				}
			}
		}
		private long find(LongCollection g, int x, int y, int xd, int yd) {
			
			for(long n : g){
				int nx = Side.getPosX(n);
				int ny = Side.getPosY(n);
				if(
						(xd > 0 && nx > x && ny == y) || (xd < 0 && nx < x && ny == y) || 
						(yd > 0 && ny > y && nx == x) || (yd < 0 && ny < y && nx == x)){
					return n;
				}
				
			}
			
			return Long.MAX_VALUE;
		}
		private long find(Long2ObjectOpenHashMap<VSet> g, long from, int xd, int yd, boolean stopAtIntersection, int stopX, int stopY) {
			
			VSet current = g.get(from);
			
			long s = from;
			
			long next;
			int x = Side.getPosX(s);
			int y = Side.getPosY(s);
			while(((next = find(current.set, x, y, xd, yd)) != Long.MAX_VALUE)){
				s = next;
				current = g.get(s);
				x = Side.getPosX(s);
				y = Side.getPosY(s);
				assert(current != null):s+"; "+x+", "+y;
				
				if(stopAtIntersection){
					for(long n : current.set){
						int nx = Side.getPosX(n);
						int ny = Side.getPosY(n);
						if(
							(stopX > 0 && nx > x && ny == y) || (stopX < 0 && nx < x && ny == y) || 
							(stopY > 0 && ny > y && nx == x) || (stopY < 0 && ny < y && nx == x)){
							
							return s;
						}
						
					}
				}
			}
			
			return s;
		}
		private void quadrulate(Long2ObjectOpenHashMap<VSet> g, Long2ObjectOpenHashMap<VSet> pSet) {
			byte grp = -1;
			/*
			 * for all vertices
			 * 		find concave corner
			 * 		move along grid in horizontal position until we hit end
			 * 		add point
			 * 		move up and down until next vertex reached
			 * 		insert point
			 * for all vertices
			 * 		find convex corner
			 * 		move clockwise until quad is complete
			 * 		create triangles
			 */
			FastEntrySet<VSet> mp = g.long2ObjectEntrySet();
			for(Entry<VSet> e : mp){
				long point = e.getLongKey();
				if(closed.contains(point)){
					continue;
				}
				VSet set = e.getValue();
				assert(grp == -1 || grp == set.groupId);
				grp = set.groupId;
				
				if(set.diagonals.size() == 3){
					
					closed.add(point);
					//concave corner detected.
					
					//check left and right
					
					int x = Side.getPosX(point);
					int y = Side.getPosY(point);
					
					assert(set.set.size() == 2);
					for(long n : set.set){
						int nx = Side.getPosX(n);
						int ny = Side.getPosY(n);
						if(y == ny){
							//connection in this x direction.
							//that means, splice must go in opposite direction
							int dirX = nx > x ? -1 : 1;
							
							int curX = x;
							int dx = curX+dirX;
							
							boolean corner = false;
							long other = Side.getIndex(curX, y);
							//follow neightbors into x dir until edge or another corner is reached
							while(!corner && pFull.containsKey(other) && pFull.get(other).set.contains(Side.getIndex(dx, y))){
								curX = dx;
								dx = curX+dirX;
								
								other = Side.getIndex(curX, y);
								if(g.containsKey(other)){
									//found an existing concave corner
									closed.add(other);
									
									edgesToInsert.add(point);
									edgesToInsert.add(other);
									
									corner = true;
								}
							}
							
							if(!corner){
								//curX is now on an edge intersection and must be inserted
								pointsToInsert.add(other);
								edgesToInsert.add(point);
								edgesToInsert.add(other);
								
								
							}
						}
					}
				}
			}
			assert(grp != -1);
			for(long in : pointsToInsert){
				VSet vSet = VSet.getInst();
				vSet.groupId = grp;
				g.put(in, vSet);
				VSet put = pSet.put(in, vSet);
				assert(put == null);
				
				int curX = Side.getPosX(in);
				int y = Side.getPosY(in);
				assert(Side.getPosX(in) == curX);
				assert(Side.getPosY(in) == y);
				//go up and down along the edge to find corner points to connect to
				int curY = y+1;
				long vertIndex = Side.getIndex(curX, curY);
				while(!g.containsKey(vertIndex)){
					assert(pFull.containsKey(vertIndex)):curX+"; "+curY;
					long oldIndex = vertIndex;
					vertIndex = Side.getIndex(curX, ++curY);
					assert(pFull.get(oldIndex).set.contains(vertIndex));
				}
				long upIndex = vertIndex;
				
				curY = y-1;
				vertIndex = Side.getIndex(curX, curY);
				while(!g.containsKey(vertIndex)){
					assert(pFull.containsKey(vertIndex)):curX+"; "+curY;
					long oldIndex = vertIndex;
					vertIndex = Side.getIndex(curX, --curY);
					assert(pFull.get(oldIndex).set.contains(vertIndex));
				}
				long downIndex = vertIndex;
				
				g.get(upIndex).set.remove(downIndex);
				g.get(downIndex).set.remove(upIndex);

				g.get(in).set.add(downIndex);
				g.get(in).set.add(upIndex);
				g.get(upIndex).set.add(in);
				g.get(downIndex).set.add(in);
				
			}
			for(int i = 0; i < edgesToInsert.size(); i+=2){
				long r0 = edgesToInsert.getLong(i);
				long r1 = edgesToInsert.getLong(i+1);
				assert(g.containsKey(r1));
				assert(g.containsKey(r0));
				
				g.get(r0).set.add(r1);
				g.get(r1).set.add(r0);
				
			}
			
			
			edgesToInsert.clear();
			pointsToInsert.clear();
			closed.clear();
			
		}
		public void addPoint(long key, VSet value) {
			Long2ObjectOpenHashMap<VSet> m = groups.get(value.groupId);
			if(m == null){
				m = new Long2ObjectOpenHashMap();
				groups.put(value.groupId, m);
			}
			m.put(key, value);
		}
		public void clear(){
			pFull.clear();
			for(VSet s : pFull.values()){
				VSet.freeInst(s);
			}
			groups.clear();
			pointsToInsert.clear();
			closed.clear();
			edgesToInsert.clear();
			triangles.clear();
		}
	}
	private static class OptimizedMesh{
		private final long[] tmpSc = new long[4];
		private Int2ObjectOpenHashMap<Side>[] sArray = new Int2ObjectOpenHashMap[]{
			new Int2ObjectOpenHashMap<Side>(),
			new Int2ObjectOpenHashMap<Side>(),
			new Int2ObjectOpenHashMap<Side>(),
			new Int2ObjectOpenHashMap<Side>(),
			new Int2ObjectOpenHashMap<Side>(),
			new Int2ObjectOpenHashMap<Side>(),
		};
		public int triangleFloatCount;
		
		
		
		public void optimize(Cancelable cancelable, long updateSignture){
			triangleFloatCount = 0;
			for(int sideId = 0; sideId < 6; sideId++){
				for(Side side : sArray[sideId].values()){
					if(isCanceled(cancelable, updateSignture)){
						return;
					}
					side.optimize(cancelable, updateSignture);
					triangleFloatCount += side.triangulator.triangles.size()*3;
				}
			}
		}
		public void fill(LongArrayList poses, ByteArrayList vises){
			for(int h = 0; h < poses.size(); h++){
				long p = poses.getLong(h);
				byte vis = vises.getByte(h);
				float x = ElementCollection.getPosX(p) - Segment.HALF_DIM;
				float y = ElementCollection.getPosY(p) - Segment.HALF_DIM;
				float z = ElementCollection.getPosZ(p) - Segment.HALF_DIM;
				
				for(int sideId = 0; sideId < 6; sideId++){
					
					int normalIndex = sideId;
					Vector3f qpm = CubeMeshQuadsShader13.quadPosMark[normalIndex];
					Vector3f normalPos = Element.DIRECTIONSf[sideId];
					
					Int2ObjectOpenHashMap<Side> map = sArray[sideId];
					
					
					int sideFlag = Element.SIDE_FLAG[sideId];
					if ((vis & sideFlag) == sideFlag) {
						int sig = Element.SIGNIFICANT_COORD[sideId];
						int index = switch(sig) {
							case 0 -> (int) x;
							case 1 -> (int) y;
							case 2 -> (int) z;
							default -> 0;
						};
						index += (Element.COORD_DIR[sideId]+1)/2;
								
						Side side = map.get(index);
						if(side == null){
							side = getSideInstance();
							side.index = index;
							side.sideId = sideId;
							map.put(index, side);
						}
						
						
						for (short j = 0; j < 4; j++) {
							
							
							float mVertNumQuarters = j * 0.25f;
							
							float fx = x;
							float fy = y;
							float fz = z;
							
							float mmx = (int)(mVertNumQuarters * qpm.x) & 1;
							float mmy = (int)(mVertNumQuarters * qpm.y) & 1;
							float mmz = (int)(mVertNumQuarters * qpm.z) & 1;
							
							
							float px = 	((((-0.5f) - (Math.abs(normalPos.x) * -0.5f)))  + mmx);
							float py =	((((-0.5f) - (Math.abs(normalPos.y) * -0.5f)))  + mmy);
							float pz =  ((((-0.5f) - (Math.abs(normalPos.z) * -0.5f)))  + mmz);;
						
							fx += px + ((normalPos.x * 0.5f));	
							fy += py + ((normalPos.y * 0.5f));	
							fz += pz + ((normalPos.z * 0.5f));	
							
							//shift to full ints to be on a grid
							fx += 0.5f;	
							fy += 0.5f;	
							fz += 0.5f;
							
							
							int xLocal = 0;
							int yLocal = 0;
							switch(sig) {
								case 0 -> {
									xLocal = FastMath.round(fy);
									yLocal = FastMath.round(fz);
								}
								case 1 -> {
									xLocal = FastMath.round(fx);
									yLocal = FastMath.round(fz);
								}
								case 2 -> {
									xLocal = FastMath.round(fx);
									yLocal = FastMath.round(fy);
								}
							}
							
							
							tmpSc[j] = Side.getIndex(xLocal, yLocal);
						}
						for (short j = 0; j < 4; j++) {
							long c = tmpSc[j];
							int xLocal = Side.getPosX(c);
							int yLocal = Side.getPosY(c);
							VSet ls = side.pSet.get(c);
							if(ls == null){
								ls = VSet.getInst();
								side.pSet.put(c, ls);
							}
							//add neighbors
							for (short m = 0; m < 4; m++) {
								
								long l = tmpSc[m];
								
								int xx = Side.getPosX(l);
								int yy = Side.getPosY(l);
								
								if(l != c && 
									((xLocal == xx && yLocal != yy) || (xLocal != xx && yLocal == yy))){
									ls.set.add(l);
//									System.err.println("ADDING TO "+xLocal+", "+yLocal+" :: "+xx+", "+yy);
									assert(ls.set.size() <= 4);
								}else if(l != c ){
									ls.diagonals.add(l);
								}
							}
						}
					}
				}
			}
		}
		public void drawItermediateTriangledSides() {
			GL11.glPointSize(4);
			GL11.glBegin(GL11.GL_POINTS);
			for(int sideId = 0; sideId < 6; sideId++){
				
				if(sideId != 0){
					continue;
				}
				
				Int2ObjectOpenHashMap<Side> m = sArray[sideId];
				int sig = Element.SIGNIFICANT_COORD[sideId];
				for(Side s : m.values()){
					
					for(long k : s.triangulator.triangles){
						
						
						int x = Side.getPosX(k);
						int y = Side.getPosY(k);
						
						float a = Element.COORD_DIR[sideId] * 0.03f;
						
						GlUtil.glColor4f(Element.SIDE_COLORS[sideId]);
						
						if(sig == 0){
							GL11.glVertex3f(s.index+a, x, y);
						}else if(sig == 1){
							GL11.glVertex3f(x, s.index+a, y);
						}else{
							GL11.glVertex3f(x, y, s.index+a);
						}
					}
				}
			}
			
			GL11.glEnd();
			
			GL11.glBegin(GL11.GL_LINES);
			for(int sideId = 0; sideId < 6; sideId++){
				
				if(sideId != 0){
					continue;
				}
				
				Int2ObjectOpenHashMap<Side> m = sArray[sideId];
				int sig = Element.SIGNIFICANT_COORD[sideId];
				for(Side s : m.values()){
					
					for(int i = 0; i < s.triangulator.triangles.size(); i+=3){
						long a = s.triangulator.triangles.getLong(i);
						long b = s.triangulator.triangles.getLong(i+1);
						long c = s.triangulator.triangles.getLong(i+2);
						
						int x0 = Side.getPosX(a);
						int y0 = Side.getPosY(a);
						
						int x1 = Side.getPosX(b);
						int y1 = Side.getPosY(b);
						
						int x2 = Side.getPosX(c);
						int y2 = Side.getPosY(c);
						GlUtil.glColor4f(Element.SIDE_COLORS[sideId]);
						
						if(sig == 0){
							GL11.glVertex3f(s.index, x0, y0);
							GL11.glVertex3f(s.index, x1, y1);
						}else if(sig == 1){
							GL11.glVertex3f(x1, s.index, y1);
							GL11.glVertex3f(x2, s.index, y2);
						}else{
							GL11.glVertex3f(x2, y2, s.index);
							GL11.glVertex3f(x0, y0, s.index);
						}
					}
				}
			}
			GL11.glEnd();
		}
		public void drawItermediateSides() {
			GL11.glPointSize(4);
			GL11.glBegin(GL11.GL_POINTS);
			for(int sideId = 0; sideId < 6; sideId++){
				
				if(sideId != 0){
					continue;
				}
				
				Int2ObjectOpenHashMap<Side> m = sArray[sideId];
				int sig = Element.SIGNIFICANT_COORD[sideId];
				for(Side s : m.values()){
					
					for(Entry<VSet> e : s.pSet.long2ObjectEntrySet()){
						
						long k = e.getLongKey();
						
						int x = Side.getPosX(k);
						int y = Side.getPosY(k);
						
						float a = Element.COORD_DIR[sideId] * 0.03f;
						
						GlUtil.glColor4f(Element.SIDE_COLORS[sideId]);
						
						if(sig == 0){
							GL11.glVertex3f(s.index+a, x, y);
						}else if(sig == 1){
							GL11.glVertex3f(x, s.index+a, y);
						}else{
							GL11.glVertex3f(x, y, s.index+a);
						}
					}
				}
			}
			
			GL11.glEnd();
			
			GL11.glBegin(GL11.GL_LINES);
			for(int sideId = 0; sideId < 6; sideId++){
				
				if(sideId != 0){
					continue;
				}
				
				Int2ObjectOpenHashMap<Side> m = sArray[sideId];
				int sig = Element.SIGNIFICANT_COORD[sideId];
				for(Side s : m.values()){
					
					for(Entry<VSet> e : s.pSet.long2ObjectEntrySet()){
						
						long k = e.getLongKey();
						
						int x = Side.getPosX(k);
						int y = Side.getPosY(k);
						
						float a = 0;//Element.COORD_DIR[sideId] * 0.03f;
						
						GlUtil.glColor4f(Element.SIDE_COLORS[e.getValue().groupId%6]);
						
						for(long n : e.getValue().set){
							int nx = Side.getPosX(n);
							int ny = Side.getPosY(n);
							if(sig == 0){
								GL11.glVertex3f(s.index+a, x, y);
								GL11.glVertex3f(s.index+a, nx, ny);
							}else if(sig == 1){
								GL11.glVertex3f(x, s.index+a, y);
								GL11.glVertex3f(nx, s.index+a, ny);
							}else{
								GL11.glVertex3f(x, y, s.index+a);
								GL11.glVertex3f(nx, ny, s.index+a);
							}
						}
					}
				}
			}
			GL11.glEnd();
		}
		public void clear() {
			for(Int2ObjectOpenHashMap<Side> e : sArray){
				for(Side s : e.values()){
					freeSideInstance(s);
				}
				e.clear();
			}
		}
		private static int aTri(float x, float y, float z, float[] triangles, int tri){
			triangles[tri] = x - 0.5f; //move back to correct previous offset
			triangles[tri+1] = y - 0.5f;
			triangles[tri+2] = z - 0.5f;
			return tri+3;
		}
		public int apply(float[] triangles) {
			int tri = 0;
			for(int sideId = 0; sideId < 6; sideId++){
				Int2ObjectOpenHashMap<Side> m = sArray[sideId];
				int sig = Element.SIGNIFICANT_COORD[sideId];
				for(Side s : m.values()){
					
					for(int i = 0; i < s.triangulator.triangles.size(); i+=3){
						long a = s.triangulator.triangles.getLong(i);
						long b = s.triangulator.triangles.getLong(i+1);
						long c = s.triangulator.triangles.getLong(i+2);
						
						int x0 = Side.getPosX(a);
						int y0 = Side.getPosY(a);
						
						int x1 = Side.getPosX(b);
						int y1 = Side.getPosY(b);
						
						int x2 = Side.getPosX(c);
						int y2 = Side.getPosY(c);
						
						if(sig == 0){
							tri = aTri(s.index, x0, y0, triangles, tri);
							tri = aTri(s.index, x1, y1, triangles, tri);
							tri = aTri(s.index, x2, y2, triangles, tri);
						}else if(sig == 1){
							tri = aTri(x0, s.index, y0, triangles, tri);
							tri = aTri(x1, s.index, y1, triangles, tri);
							tri = aTri(x2, s.index, y2, triangles, tri);
						}else{
							tri = aTri(x0, y0, s.index, triangles, tri);
							tri = aTri(x1, y1, s.index, triangles, tri);
							tri = aTri(x2, y2, s.index, triangles, tri);
						}
					}
				}
			}
			return tri;
		}
		
	}
	public enum DrawMode {
		RAW,
		OPTIMIZED
	}
	public ElementCollectionMesh(){
	}
	public void calculate(Cancelable cancelable, long updateSignture, LongList col) {
		long t = System.currentTimeMillis();
		LongOpenHashSet set = getSet();
		set.addAll(col);
		localMin.set(10000000, 10000000, 10000000);
		localMax.set(-10000000, -10000000, -10000000);
		assert(col.size() > 0);
		
		final int cSize = col.size();
		for(int i = 0; i < cSize; i++){
			long p = col.getLong(i);
			addFill(p, set);
			if(isCanceled(cancelable, updateSignture)){
				break;
			}
		}
		set.addAll(filled);
		
		
		for(int i = 0; i < cSize; i++){
			long p = col.getLong(i);
			addCheck(p, set);
			if(isCanceled(cancelable, updateSignture)){
				break;
			}
		}
		
		LongIterator longIterator = filled.iterator();
		while(longIterator.hasNext()){
			addCheck(longIterator.nextLong(), set);
			if(isCanceled(cancelable, updateSignture)){
				break;
			}
		}
		long added = System.currentTimeMillis() - t;
		rawVerticesCount = poses.size() *  VERTS_PER_FACE * FACE_COUNT ; 
		rawDataSize = rawVerticesCount * TYPE_BYTE_COUNT * VERT_DIMENSION ;  
		pContain.clear();
		filled.clear();
		freeSet(set);
		if(optimizedMesh == null){
			optimizedMesh = new OptimizedMesh();
		}else{
			optimizedMesh.clear();
		}
		optimizedMesh.fill(poses, vises);
		optimizedMesh.optimize(cancelable, updateSignture);
		
		int size = 256;
		
		while(size < optimizedMesh.triangleFloatCount){
			size *= 2;
		}
		if(triangles == null || triangles.length < size){
			triangles = new float[size];
		}
		optTriangleFloatCount = optimizedMesh.apply(triangles);
		firstVertex = new Vector3f(triangles[0], triangles[1], triangles[2]);
		assert(optTriangleFloatCount == optimizedMesh.triangleFloatCount);
		optimizedMesh.clear();
		
		
		long taken = System.currentTimeMillis() - t;
		
		if(taken > 30) {
			System.err.println("[ElementCollectionMesh] WARNING: Mesh calculation took long: "+taken+"ms; Adding took: "+added+"ms");
		}
	}
	private void addFill(long p, LongOpenHashSet set){
		ElementCollection.getPosFromIndex(p, posTmp);
		for(int i = 0; i < 6; i++){
			Vector3i dir = Element.DIRECTIONSi[i];
			int x = posTmp.x + dir.x;
			int y = posTmp.y + dir.y;
			int z = posTmp.z + dir.z;
			
			
			long nPos = ElementCollection.getIndex(x,y,z);
			if(!set.contains(nPos)){
				byte old = pContain.addTo(nPos, (byte) 1);
				old ++;
				if(old >= 5){
					filled.add(nPos);
				}
			}
		}
	}
	private void addCheck(long p, LongOpenHashSet set){
		ElementCollection.getPosFromIndex(p, posTmp);
		byte vis = Element.FULLVIS;
		for(int i = 0; i < 6; i++){
			Vector3i dir = Element.DIRECTIONSi[i];
			int x = posTmp.x + dir.x;
			int y = posTmp.y + dir.y;
			int z = posTmp.z + dir.z;
			
			localMin.x = Math.min(localMin.x, x-Segment.HALF_DIM);
			localMin.y = Math.min(localMin.y, y-Segment.HALF_DIM);
			localMin.z = Math.min(localMin.z, z-Segment.HALF_DIM);

			localMax.x = Math.max(localMax.x, x-Segment.HALF_DIM);
			localMax.y = Math.max(localMax.y, y-Segment.HALF_DIM);
			localMax.z = Math.max(localMax.z, z-Segment.HALF_DIM);
			
			
			long nPos = ElementCollection.getIndex(x,y,z);
			if(set.contains(nPos)){
				vis -= Element.SIDE_FLAG[i];
			}
		}
		if(vis > 0){
			this.vises.add(vis);
			this.poses.add(p);
		}
	}
	public void clear(){
		while(inInit){
			System.err.println("THREAD WAS IN INIT. CLEAN UP HAS TO WAIT");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		color.set(DEFAULT_COLOR);
		draw = false;
		init = false;
		startedInit = false;
		vises.clear();
		poses.clear();
		optTriangleFloatCount = 0;
		if(buffer != null){
			buffer.clear();
		}
		if(bufferId != 0){
			GL15.glDeleteBuffers(bufferId);
			bufferId = 0;
		}
		optimizedMesh = null;
	}
	public void destroyBuffer() {
		if(buffer != null){
				GlUtil.destroyDirectByteBuffer(buffer);
		}
	}
	public boolean isVisibleFrustum(Transform t){
		AabbUtil2.transformAabb(localMin, localMax, 3, t, min, max);
		return Controller.getCamera().isAABBInFrustum(min, max);
	}
//	public void drawOptimized(){
//		if(!draw){
//			return;
//		}
//		GlUtil.glColor4f(1,1,1,1);
//		optimizedMesh.drawItermediate();
//		optimizedMesh.drawItermediateSides();
//		optimizedMesh.drawItermediateTriangledSides();
//		
//	}
	private OptimizedMesh debug;

	public void drawDebug() {
		if(!draw){
			return;
		}
		draw = false;
		
		
		
		int nr = Keyboard.getNumberPressed();
		boolean changed = false;
		if(nr >= 0){
			changed = dbm.step(nr);
		}
		System.err.println("DRAW DEBUG "+dbm.step);
		if(debug == null || changed){
			debug = new OptimizedMesh();
			debug.fill(poses, vises);
			debug.optimize(null, 0);
		}
		
		debug.drawItermediateSides();
		debug.drawItermediateTriangledSides();
	}
	public void draw(){
		try{
			if(!draw){
				return;
			}
			wtc.storeCurrentModelviewProjection();
			
			
			if(debug != null){
				debug.clear();
				debug = null;
			}
			
			draw = false;
			if(!init){
				if(!startedInit){
//					System.err.println("MESH INIT");
					initializeMeshThreaded();
					startedInit = true;
				}
				return;
			}
			
			GL11.glColor4f(color.x, color.y, color.z, color.w);
		
			if(bufferId == 0){
				bufferId = GL15.glGenBuffers();
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId); // Bind
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
				
			}else{
				GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
			}
			if(ElementCollectionMesh.USE_INT_ATT()){
				GlUtil.glVertexAttribIPointer(ElementCollectionMesh.VERT_ATTRIB_INDEX, ElementCollectionMesh.VERT_DIMENSION, GL11.GL_INT, 0, 0);
			}else{
				if(DRAW_MODE == DrawMode.RAW){
					GL11.glVertexPointer(ElementCollectionMesh.VERT_DIMENSION, GL11.GL_FLOAT, 0, 0);
				}else{
					GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);	
				}
			}
			
			if(DRAW_MODE == DrawMode.RAW){
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rawVerticesCount);
			}else{
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, optTriangleFloatCount/3);
			}
			
			GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}finally{
		}
	}
	public int getByteCount() {
		return optTriangleFloatCount * ByteUtil.SIZEOF_FLOAT;
	}
	public void initializeMesh() {
		int neededSize = 256;
		
		int dataSize = DRAW_MODE == DrawMode.RAW ? rawDataSize : getByteCount();
		
		while(neededSize < dataSize){
			neededSize *= 2;
		}
		neededSize *= 2;
		if(buffer == null || buffer.capacity() < neededSize){
			long takenDestruction = 0;
			if(buffer != null){
				try {
					long t = System.currentTimeMillis();
					//clean up mesh 
					GlUtil.destroyDirectByteBuffer(buffer);
					takenDestruction = System.currentTimeMillis()-t;
					
				} catch (IllegalArgumentException | SecurityException e) {
					e.printStackTrace();
				} 
			}
			long t = System.currentTimeMillis();
			buffer = MemoryUtil.memAlloc(neededSize);
			
			long takenCreation = System.currentTimeMillis() - t;
			
			long taken = takenDestruction + takenCreation;
			if(taken > 10) {
				System.err.println("[ElementCollectionMesh] WARNING: REFRESH: Mesh buffer data destruction->recreation took long: "+taken+" ms (destr: "+takenDestruction+"ms; create: "+takenCreation+"ms)");
			}
		}
		buffer.clear();
		fillBuffer(buffer);
		
		buffer.flip();
//		System.err.println("BUFFER LIM :::: "+buffer.limit());
		init = true;
		inInit = false;		
	}
	private void initializeMeshThreaded() {
		inInit = true;
		threadPool.execute(this::initializeMesh);
		
		
	}
	public static boolean USE_INT_ATT(){
		return DRAW_MODE == DrawMode.RAW && EngineSettings.G_ELEMENT_COLLECTION_INT_ATT.isOn() && GraphicsContext.INTEGER_VERTICES;
	}
	public void fillBuffer(ByteBuffer b) {
//		for(int i = 0; i < optTriangleFloatCount; i++) {
//			b.putFloat(triangles[i]);
//		}
		b.asFloatBuffer().put(triangles, 0, optTriangleFloatCount);
		//needs to manually set position since asFloatBuffer is a view with an independent position/limit
		b.position(optTriangleFloatCount*ByteUtil.SIZEOF_FLOAT);
		
//		assert(false):b.limit()+"; "+optTriangleFloatCount;
	}
	public void markDraw() {
		this.draw = true;
	}
	public void setColor(float r, float g, float b, float a){
		this.color.set(r,g,b,a);
	}
	public void setColor(Vector4f c){
		this.color.set(c);
	}
	public boolean isDraw() {
		return draw;
	}
	private WorldToScreenConverter wtc = new WorldToScreenConverter();
	public Vector3f getFirstVertexScreenCoord(Transform world, Vector3f out) {
		Vector3f worldPos = new Vector3f(firstVertex);
		world.transform(worldPos);
		wtc.convert(worldPos, out, true);
		return out;
	}
	public Vector3f getFirstVertex() {
		return firstVertex;
	}
	public void setFirstVertex(Vector3f firstVertex) {
		this.firstVertex = firstVertex;
	}
	public int getVertexCount() {
		if(DRAW_MODE == DrawMode.RAW){
			return rawVerticesCount;
		}else{
			return optTriangleFloatCount/3;
		}
		
	}
	
	

}
