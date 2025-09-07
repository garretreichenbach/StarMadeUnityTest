package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;

import com.bulletphysics.linearmath.Transform;

public class RayTraceGridTraverser {

	private Vector3i cell = new Vector3i();
	private Vector3i endcell = new Vector3i();
	private Vector3f tMax = new Vector3f();
	private Vector3f tDelta = new Vector3f();
	private Vector3f cellBoundary = new Vector3f();
	private Vector3f end = new Vector3f();
	private int stepX;
	private int stepY;
	private int stepZ;
	private int tries;


	public void drawDebug(int x, int y, int z, int i, Transform controller) {
//		assert(false);
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			Vector3f f = new Vector3f();
			Vector3f t = new Vector3f();
			float size = 0.5f; // 8 for segments
			f.set(x - size - SegmentData.SEG_HALF, y - size - SegmentData.SEG_HALF, z - size - SegmentData.SEG_HALF);
			t.set(x + size - SegmentData.SEG_HALF, y + size - SegmentData.SEG_HALF, z + size - SegmentData.SEG_HALF);

			System.err.println("TESTING: " + x + ", " + y + ", " + z + "; " + stepX + ", " + stepY + ", " + stepZ + "; MAX " + tMax + "; DELTA " + tDelta);

			Transform tr = new Transform(controller);
			DebugBox b;
			if(i == 0){
				b = new DebugBox(f, t, tr, 1, 0, 0, 1);
			}else{
				b = new DebugBox(f, t, tr, (float)i / 10, (float)i / 10, 1, 1);	
			}
			DebugDrawer.boxes.add(b);
		}

	}

	private Vector3i getCellAt(Vector3f position, Vector3i out) {
		out.set(
				FastMath.fastFloor(position.x),
				FastMath.fastFloor(position.y),
				FastMath.fastFloor(position.z));
		return out;
	}

	public void getCellsOnRay(Ray ray, final int maxDepth, SegmentTraversalInterface<?> callback) {

		if (Float.isNaN(ray.position.x) ||
				Float.isNaN(ray.position.y) ||
				Float.isNaN(ray.position.z) ||
				Float.isInfinite(ray.position.x) ||
				Float.isInfinite(ray.position.y) ||
				Float.isInfinite(ray.position.z) ||

				Float.isNaN(ray.direction.x) ||
				Float.isNaN(ray.direction.y) ||
				Float.isNaN(ray.direction.z) ||
				Float.isInfinite(ray.direction.x) ||
				Float.isInfinite(ray.direction.y) ||
				Float.isInfinite(ray.direction.z)) {
			System.err.println("[RAYTRACE_TRAVERSE][WARNING] traversal is invalid (NaN, or Infinite) ray.pos: " + ray.position + ", ray.dir: " + ray.direction+"; callback class: "+callback.getClass());
			return;
		}

		assert (!Float.isNaN(ray.position.x));
		assert (!Float.isNaN(ray.position.y));
		assert (!Float.isNaN(ray.position.z));
		assert (!Float.isInfinite(ray.position.x));
		assert (!Float.isInfinite(ray.position.y));
		assert (!Float.isInfinite(ray.position.z));

		assert (!Float.isNaN(ray.direction.x));
		assert (!Float.isNaN(ray.direction.y));
		assert (!Float.isNaN(ray.direction.z));
		assert (!Float.isInfinite(ray.direction.x));
		assert (!Float.isInfinite(ray.direction.y));
		assert (!Float.isInfinite(ray.direction.z));

		this.tries = 0;
		

		// NOTES:
		// * This code assumes that the ray's position and direction are in 'cell coordinates', which means
		//   that one unit equals one cell in all directions.
		// * When the ray doesn't start within the voxel grid, calculate the first position at which the
		//   ray could enter the grid. If it never enters the grid, there is nothing more to do here.
		// * Also, it is important to test when the ray exits the voxel grid when the grid isn't infinite.
		// * The Point3D structure is a simple structure having three integer fields (X, Y and Z).

		// The cell in which the ray starts.
		Vector3i start = getCellAt(ray.position, cell); // Rounds the position's X, Y and Z down to the nearest integer values.
		int x = start.x;
		int y = start.y;
		int z = start.z;

		// Determine which way we go.
		stepX = (int) Math.signum(ray.direction.x);
		stepY = (int) Math.signum(ray.direction.y);
		stepZ = (int) Math.signum(ray.direction.z);

		// Calculate cell boundaries. When the step (i.e. direction sign) is positive,
		// the next boundary is AFTER our current position, meaning that we have to add 1.
		// Otherwise, it is BEFORE our current position, in which case we add nothing.
		cellBoundary.set(
				x + (stepX > 0 ? 1 : 0),
				y + (stepY > 0 ? 1 : 0),
				z + (stepZ > 0 ? 1 : 0));

		// NOTE: For the following calculations, the result will be Single.PositiveInfinity
		// when ray.Direction.X, Y or Z equals zero, which is OK. However, when the left-hand
		// value of the division also equals zero, the result is Single.NaN, which is not OK.

		// Determine how far we can travel along the ray before we hit a voxel boundary.
		tMax.set(
				(cellBoundary.x - ray.position.x) / ray.direction.x,    // Boundary is a plane on the YZ axis.
				(cellBoundary.y - ray.position.y) / ray.direction.y,    // Boundary is a plane on the XZ axis.
				(cellBoundary.z - ray.position.z) / ray.direction.z);    // Boundary is a plane on the XY axis.
		if (Float.isNaN(tMax.x) || Float.isInfinite(tMax.x)) tMax.x = Float.POSITIVE_INFINITY;
		if (Float.isNaN(tMax.y) || Float.isInfinite(tMax.y)) tMax.y = Float.POSITIVE_INFINITY;
		if (Float.isNaN(tMax.z) || Float.isInfinite(tMax.z)) tMax.z = Float.POSITIVE_INFINITY;

		// Determine how far we must travel along the ray before we have crossed a gridcell.
		tDelta.set(
				stepX / ray.direction.x,                    // Crossing the width of a cell.
				stepY / ray.direction.y,                    // Crossing the height of a cell.
				stepZ / ray.direction.z);                    // Crossing the depth of a cell.
		if (Float.isNaN(tDelta.x)) tDelta.x = Float.POSITIVE_INFINITY;
		if (Float.isNaN(tDelta.y)) tDelta.y = Float.POSITIVE_INFINITY;
		if (Float.isNaN(tDelta.z)) tDelta.z = Float.POSITIVE_INFINITY;

		//	    System.err.println("MAX: "+tMax+"; DELTA: "+tDelta);

		end.add(ray.position, ray.direction);
		Vector3i endPos = getCellAt(end, endcell);
		int i = 0;
		while (i < maxDepth + 2) {

			
			
			if (!handle(x, y, z, callback, i, maxDepth)) {
				return;
			}

			if (tMax.x < tMax.y) {
				if (tMax.x < tMax.z) {
					x += stepX;
					tMax.x += tDelta.x;
				} else {
					z += stepZ;
					tMax.z += tDelta.z;
				}
			} else {
				if (tMax.y < tMax.z) {
					y += stepY;
					tMax.y += tDelta.y;
				} else {
					z += stepZ;
					tMax.z += tDelta.z;
				}
			}
			i++;
		}
	}

	private boolean handle(int x, int y, int z,
	                       SegmentTraversalInterface<?> callback, int i, int maxDepth) {

		tries++;
		if (tries > 4000 && tries % 2000 == 0) {
			System.err.println("[WARNING] RAYTRACE::: #" + tries + "; " + x + ", " + y + ", " + z + "; " + tMax + "; " + stepX + ", " + stepY + ", " + stepZ + "; i: " + i + "; maxDepth: " + maxDepth+"; CONTEXT "+callback.getContextObj()+"; ");
//			try {
//				throw new Exception("ODKKLDNKLD");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			//			try{
			//				throw new NullPointerException();
			//			}catch(Exception e){
			//				e.printStackTrace();
			//			}
		}

		return callback.handle(x, y, z, this);

	}

}
