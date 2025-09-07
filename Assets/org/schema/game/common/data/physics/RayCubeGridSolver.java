package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.common.data.world.SegmentData;

import com.bulletphysics.linearmath.Transform;

public class RayCubeGridSolver {

	private final Vector3f inputStart = new Vector3f();
	private final Vector3f inputEnd = new Vector3f();

	private final Vector3f dir = new Vector3f();

	private final RayTraceGridTraverser tra = new RayTraceGridTraverser();

	private final Ray r = new Ray();
	private final Transform inv = new Transform();
	public boolean ok;
	private float MIN_DEPTH;
	private Granularity gran;
	private static long lastExceptionMinS;
	private static long lastExceptionMin;
	private static long lastException;

	
	
	private final static float oneSegInv = 1f/SegmentData.SEGf;
	public static float sectorInv;
	public static float sectorHalf;
	
	public enum Granularity{
		BLOCK,
		SEGMENT,
		SECTOR
	}
	
	private void initialize(Vector3f start, Vector3f end, Transform currentTrans, Granularity segGranularity) {
		ok = true;
		this.gran = segGranularity;
		if (segGranularity == Granularity.SEGMENT) {
			MIN_DEPTH = 3;
		} else {
			MIN_DEPTH = 0.01f;
		}
//			System.err.println("RAY SCALE "+Vector3fTools.diffLength(start, end));
		inputStart.set(start);
		inputEnd.set(end);

		inv.set(currentTrans);
		inv.inverse();
		inv.transform(inputStart);
		inv.transform(inputEnd);

		//add EXACT block position
		float centerAdd = 0.0f;
		if(segGranularity == Granularity.BLOCK){
			centerAdd = 0.5f;	
		}else if (segGranularity == Granularity.SEGMENT) {
			centerAdd = SegmentData.SEG_HALF + 0.5f;
		}else if (segGranularity == Granularity.SECTOR) {
			assert(sectorHalf > 0);
			centerAdd = sectorHalf;
		}
		inputStart.x += centerAdd;
		inputStart.y += centerAdd;
		inputStart.z += centerAdd;

		inputEnd.x += centerAdd;
		inputEnd.y += centerAdd;
		inputEnd.z += centerAdd;

		//		if(SubsimplexRayCubesCovexCast.debug){
		//			System.err.println("START OF TRAVERSE +888 IS "+start+" to "+end+" inversed-local: "+inputStart+" to "+inputEnd);
		//		}

		if (segGranularity == Granularity.SEGMENT) {
			inputStart.x *= oneSegInv;
			inputStart.y *= oneSegInv;
			inputStart.z *= oneSegInv;

			inputEnd.x *= oneSegInv;
			inputEnd.y *= oneSegInv;
			inputEnd.z *= oneSegInv;
		}else if(segGranularity == Granularity.SECTOR){
			assert(sectorInv != 0);
			inputStart.x *= sectorInv;
			inputStart.y *= sectorInv;
			inputStart.z *= sectorInv;

			inputEnd.x *= sectorInv;
			inputEnd.y *= sectorInv;
			inputEnd.z *= sectorInv;
		}

	

		dir.sub(inputEnd, inputStart);
		
		float len = dir.length()+1;
		int maxDepth = FastMath.fastCeil(len);
		if (maxDepth > 10000) {
			if(System.currentTimeMillis() > lastExceptionMinS+2000) {
				try {
					throw new Exception("RAYTRACE [WARNING] Trace > 10000:::  DIR: " + dir + ";; LEN/MAX " + len + "/" + maxDepth + "; start " + inputStart + "; end " + inputEnd + "; ORIG: "+start+" -> "+end);
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastExceptionMinS = System.currentTimeMillis();
			}
		}
		
		
		if (dir.lengthSquared() == 0) {
			System.err.println("[TRAVERSE] Exception: Tried to traverse on zero length direction from " + inputStart + " -> " + inputEnd);
			ok = false;
			return;
		}
//		dir.scale(3.2f);
//		if(dir.length() < 30) {
			dir.scale(1.2f); //for some reason this is necessary, or blocks will not be detected in front on some angles
//		}

		if (dir.length() < MIN_DEPTH) {
			dir.normalize();
			dir.scale(MIN_DEPTH+0.5f);
		}

		r.direction.set(dir);
		r.position.set(inputStart);
	}

	public void initializeBlockGranularity(Vector3f start, Vector3f end, Transform currentTrans) {
		initialize(start, end, currentTrans, Granularity.BLOCK);
	}

	public void initializeSegmentGranularity(Vector3f start, Vector3f end, Transform currentTrans) {
		initialize(start, end, currentTrans, Granularity.SEGMENT);
	}

	public void initializeSectorGranularity(Vector3f start, Vector3f end, Transform currentTrans) {
		initialize(start, end, currentTrans, Granularity.SECTOR);
	}

	public void traverseSegmentsOnRay(SegmentTraversalInterface<?> callback) {

		if (!ok) {
			return;
		}

		float len = dir.length()+1;
//				System.err.println("TRAVERSING: "+dir.length());

		int maxDepth = FastMath.fastCeil(len);

		if (Float.isInfinite(maxDepth) || Float.isNaN(maxDepth)) {
			System.err.println("RAYTRACE [ERROR]::: Not calculating: DIR not a number " + dir + ";; " + len + "/" + maxDepth + "; " + inputStart + "; " + inputEnd + "; ");
			return;
		}
		if (maxDepth > 10000) {
			if(System.currentTimeMillis() > lastExceptionMin+200) {
				System.err.println("RAYTRACE [WARNING] Trace > 10000:::  DIR: " + dir + ";; LEN/MAX " + len + "/" + maxDepth + "; start " + inputStart + "; end " + inputEnd + "; ");
				lastExceptionMin = System.currentTimeMillis();
			}
			if(System.currentTimeMillis() > lastException+4000) {
				try {
					throw new Exception("RAYTRACE [WARNING] Trace > 10000:::  DIR: " + dir + ";; LEN/MAX " + len + "/" + maxDepth + "; start " + inputStart + "; end " + inputEnd + "; ");
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastException = System.currentTimeMillis();
			}
			maxDepth = 10000;
		}

		tra.getCellsOnRay(r, (int) Math.max(MIN_DEPTH, maxDepth), callback);

	}
}
