package org.schema.game.common.data.physics.sweepandpruneaabb;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.schema.game.common.data.physics.AABBVarSet;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class Sweeper<E, C> {

	public final ObjectArrayList<OverlappingSweepPair<E>> pairs = new ObjectArrayList<OverlappingSweepPair<E>>(512);
	private final ObjectLinkedOpenHashSet<OverlappingSweepPair<E>> xPairs = new ObjectLinkedOpenHashSet<>(512);
	private final IntOpenHashSet yPairs = new IntOpenHashSet(512);
	private final IntOpenHashSet zPairs = new IntOpenHashSet(512);
	public boolean debug;
	int pointPointer = 0;
	int pairPointer = 0;
	int xAxisAPointer = 0;
	int yAxisAPointer = 0;
	int zAxisAPointer = 0;
	int xAxisBPointer = 0;
	int yAxisBPointer = 0;
	int zAxisBPointer = 0;
	AABBVarSet varSet = new AABBVarSet();
	private SweepPoint<E>[] pool = new SweepPoint[10000];
	private OverlappingSweepPair<E>[] poolPairs = new OverlappingSweepPair[10000];
	
	private SweepPoint<E>[] xAxisA = new SweepPoint[5000];
	private SweepPoint<E>[] yAxisA = new SweepPoint[5000];
	private SweepPoint<E>[] zAxisA = new SweepPoint[5000];
	
	private SweepPoint<E>[] xAxisB = new SweepPoint[5000];
	private SweepPoint<E>[] yAxisB = new SweepPoint[5000];
	private SweepPoint<E>[] zAxisB = new SweepPoint[5000];

	private final XComp xComp = new XComp();
	private final YComp yComp = new YComp();
	private final ZComp zComp = new ZComp();
	protected SweepPoint<E>[] ensureSize(SweepPoint<E>[] axisToFill, int size) {
		
		if(size >= axisToFill.length){
			final SweepPoint<E>[] old = axisToFill;
			final int oldSize = axisToFill.length;
			axisToFill = new SweepPoint[Math.max(size, oldSize * 2)];
			
			if(old == xAxisA){
				xAxisA = axisToFill;
			}else if(axisToFill == yAxisA){
				yAxisA = axisToFill;
			}else if(axisToFill == zAxisA){
				zAxisA = axisToFill;
			}else if(axisToFill == xAxisB){
				xAxisB = axisToFill;
			}else if(axisToFill == yAxisB){
				yAxisB = axisToFill;
			}else if(axisToFill == zAxisB){
				zAxisB = axisToFill;
			}
			
		}
		return axisToFill;
		
	}
	protected SweepPoint<E> getPoint(E s, Vector3f outOuterMin, Vector3f outOuterMax, int hash) {
		if(pointPointer == pool.length){
			final SweepPoint<E>[] old = pool;
			final int oldSize = pool.length;
			pool = new SweepPoint[pool.length * 2];
			System.arraycopy(old, 0, pool, 0, oldSize);
			//initialize rest new
			for (int i = oldSize; i < pool.length; i++) {
				pool[i] = new SweepPoint();
			}
		}

		SweepPoint<E> p = pool[pointPointer++];
		p.set(s, outOuterMin, outOuterMax, hash);

		return p;
	}
	public Sweeper() {
		super();

		for (int i = 0; i < pool.length; i++) {
			pool[i] = new SweepPoint();
		}

		for (int i = 0; i < poolPairs.length; i++) {
			poolPairs[i] = new OverlappingSweepPair();
		}
	}
	
	public void fill(C cubeShape0, Transform tmpTrans0, C cubeShape1, Transform tmpTrans1, List<E> aList, List<E> bList) {

		xAxisAPointer = 0;
		yAxisAPointer = 0;
		zAxisAPointer = 0;

		xAxisBPointer = 0;
		yAxisBPointer = 0;
		zAxisBPointer = 0;

		pointPointer = 0;
		pairPointer = 0;
		xPairs.clear();
		yPairs.clear();
		zPairs.clear();

		assert (aList.size() * 3 + bList.size() * 3 < pool.length) : aList.size() * 3 + "; " + bList.size() * 3;

		// create once and then copy over for the other axes

		xAxisAPointer = fillAxis(cubeShape0, tmpTrans0, xAxisA, aList, xComp, 0);
		yAxisAPointer = fillAxis(yAxisA, xAxisA, xAxisAPointer, yComp);
		zAxisAPointer = fillAxis(zAxisA, xAxisA, xAxisAPointer, zComp);

		xAxisBPointer = fillAxis(cubeShape1, tmpTrans1, xAxisB, bList, xComp, xAxisAPointer);
		yAxisBPointer = fillAxis(yAxisB, xAxisB, xAxisBPointer, yComp);
		zAxisBPointer = fillAxis(zAxisB, xAxisB, xAxisBPointer, zComp);

	}

	public void getOverlapping() {

		pairs.clear();
		
		overlappingX(xAxisA, xAxisAPointer, xAxisB, xAxisBPointer, xPairs);
		overlappingY(yAxisA, yAxisAPointer, yAxisB, yAxisBPointer, yPairs);
		overlappingZ(zAxisA, zAxisAPointer, zAxisB, zAxisBPointer, zPairs);

		//		System.err.println("HIGHEST: "+overlappingX+", "+overlappingY+", "+overlappingZ);

        for (OverlappingSweepPair<E> next : xPairs) {
            int hashCode = next.hashCode();
            if (yPairs.contains(hashCode) && zPairs.contains(hashCode)) {
                pairs.add(next);
//				it.remove();
            }
        }
			if(debug && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
                for (OverlappingSweepPair<E> next : xPairs) {
                    int hashCode = next.hashCode();
                    if (yPairs.contains(hashCode) && zPairs.contains(hashCode)) {
                        drawPair(0.1f, next, 1, 0, 0, 0, 0, 1);
                    } // else {
//						drawPair(0.1f, next, 1, 1, 0, 1, 0, 1);
                    //}

                }
			}
//			if(debug && EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
//				System.err.println("PAIRS::: "+pairs.size());
//				it = pairs.iterator();
//				while(it.hasNext()){
//					OverlappingSweepPair next = it.next();
//						drawPair(0.1f, next, 0, 1, 0, 0, 0, 1);
//					
//				}
//			}
		
		
		//		for(int i = 0; i < indices.size(); i++){
		//			int index = indices.getInt(i);
		//
		//			if(!(yPairs[index]  && zPairs[index] )){
		//				indices.remove(i);
		//				i--;
		//			}
		//		}

		//		System.err.println("PREVIOUS PAIRS: "+pairPointer+"; "+xAxisA.size()+" vs "+yAxisB.size()+": "+xPairs.size());
	}

	private void drawPair(float sc, OverlappingSweepPair<E> next, float ar, float ag, float ab, float br, float bg, float bb) {
		DebugDrawer.points.add(new DebugPoint(new Vector3f(
				next.a.minX + sc,
				next.a.minY + sc,
				next.a.minZ + sc
		), new Vector4f(ar, ag, ab, 1), 0.2f));

		DebugDrawer.boundingBoxes.add(new DebugBoundingBox(new Vector3f(
				next.a.minX + sc,
				next.a.minY + sc,
				next.a.minZ + sc
		),
				new Vector3f(
						next.a.maxX - sc,
						next.a.maxY - sc,
						next.a.maxZ - sc
				), ar, ag, ab, 1));

		DebugDrawer.points.add(new DebugPoint(new Vector3f(
				next.b.minX + sc,
				next.b.minY + sc,
				next.b.minZ + sc
		), new Vector4f(br, bg, bb, 1), 0.2f));

		DebugDrawer.boundingBoxes.add(new DebugBoundingBox(new Vector3f(
				next.b.minX + sc,
				next.b.minY + sc,
				next.b.minZ + sc
		),
				new Vector3f(
						next.b.maxX - sc,
						next.b.maxY - sc,
						next.b.maxZ - sc
				), br, bg, bb, 1));
	}

	private int overlappingX(SweepPoint<E>[] axisA, int sizeA, SweepPoint<E>[] axisB, int sizeB, ObjectSet<OverlappingSweepPair<E>> pairs) {
		int i = 0;
		int j = 0;
		while (i < sizeA && j < sizeB) {

			SweepPoint<E> currentA = axisA[i];
			SweepPoint<E> currentB = axisB[j];

			if (currentA.minX < currentB.minX) {
				// A min point is smaller then B min
				int k = j;
				while (currentB.minX <= currentA.maxX && k < sizeB) {
					currentA.hasXPair = true;
					currentB.hasXPair = true;
					currentB = axisB[k];
					k++;
					OverlappingSweepPair<E> pair = getPair(currentA, currentB);
					boolean add = pairs.add(pair);
					if (!add) {
						//object is not needed because an equal is already is in the list
						pairPointer--;
					}
				}

				i++;

			} else {
				// B min point is smaller then A min
				int k = i;
				// include all A's as overlapping that have a lower begin then the end of B
				while (currentA.minX <= currentB.maxX && k < sizeA) {
					currentA.hasXPair = true;
					currentB.hasXPair = true;
					currentA = axisA[k];
					k++;
					OverlappingSweepPair<E> pair = getPair(currentA, currentB);
					boolean add = pairs.add(pair);
					if (!add) {
						//object is not needed because an equal is already is in the list
						pairPointer--;
					}// else {
						//						drawPair(0.2f, pair, 1, 1, 1, 1, 1, 1);
					//}

				}
				j++;
			}
		}
		return 0;
	}

	private int overlappingY(SweepPoint<E>[] axisA, int sizeA, SweepPoint<E>[] axisB, int sizeB, IntOpenHashSet pairs) {
		int i = 0;
		int j = 0;
		while (i < sizeA && j < sizeB) {
			SweepPoint<E> currentA = axisA[i];
			SweepPoint<E> currentB = axisB[j];
			//			if(!currentA.hasXPair){
			//				i++;
			//				continue;
			//			}
			//			if(!currentB.hasXPair){
			//				j++;
			//				continue;
			//			}
			if (currentA.minY < currentB.minY) {

				// A min point is smaller then B min
				int k = j;
				while (currentB.minY <= currentA.maxY && k < sizeB) {
					currentB = axisB[k];
					if (!currentB.hasXPair && !currentA.hasXPair) {
						k++;
						continue;
					}
					currentA.hasYPair = true;
					currentB.hasYPair = true;
					k++;
					pairs.add(OverlappingSweepPair.getHashCode(currentA, currentB));

				}
				i++;

			} else {
				// B min point is smaller then A min
				int k = i;
				// include all A's as overlapping that have a lower begin then the end of B
				while (currentA.minY <= currentB.maxY && k < sizeA) {
					currentA = axisA[k];
					if (!currentA.hasXPair && !currentB.hasXPair) {
						k++;
						continue;
					}
					currentA.hasYPair = true;
					currentB.hasYPair = true;
					k++;
					pairs.add(OverlappingSweepPair.getHashCode(currentA, currentB));

				}
				j++;
			}
		}
		return 0;
	}

	private int overlappingZ(SweepPoint<E>[] axisA, int sizeA, SweepPoint<E>[] axisB, int sizeB, IntOpenHashSet pairs) {
		int i = 0;
		int j = 0;
		while (i < sizeA && j < sizeB) {
			SweepPoint<E> currentA = axisA[i];
			SweepPoint<E> currentB = axisB[j];
			//			if(!currentA.hasXPair){
			//				i++;
			//				continue;
			//			}
			//			if(!currentB.hasXPair){
			//				j++;
			//				continue;
			//			}
			if (currentA.minZ < currentB.minZ) {

				// A min point is smaller then B min
				int k = j;
				while (currentB.minZ <= currentA.maxZ && k < sizeB) {
					currentB = axisB[k];
					if (!(currentB.hasXPair && currentB.hasYPair) && !(currentA.hasXPair && currentA.hasYPair)) {
						k++;
						continue;
					}
					currentA.hasZPair = true;
					currentB.hasZPair = true;
					k++;

					pairs.add(OverlappingSweepPair.getHashCode(currentA, currentB));

				}
				i++;

			} else {
				// B min point is smaller then A min
				int k = i;
				// include all A's as overlapping that have a lower begin then the end of B
				while (currentA.minZ <= currentB.maxZ && k < sizeA) {
					currentA = axisA[k];
					if (!(currentA.hasXPair && currentA.hasYPair) && !(currentB.hasXPair && currentB.hasYPair)) {
						k++;
						continue;
					}
					currentA.hasZPair = true;
					currentB.hasZPair = true;
					k++;
					pairs.add(OverlappingSweepPair.getHashCode(currentA, currentB));
				}
				j++;
			}
		}
		return 0;
	}

	private int fillAxis(SweepPoint<E>[] axisToFill, SweepPoint<E>[] toCopyAxis, int pointer, Comparator<SweepPoint<E>> comp) {

		int i;
		int size = pointer;
		for (i = 0; i < size; i++) {
			axisToFill[i] = toCopyAxis[i];
		}
		Arrays.parallelSort(axisToFill, 0, i, comp);
		return i;
	}

	protected abstract int fillAxis(C shape,
	                     Transform trans, SweepPoint<E>[] axisToFill, List<E> aList, Comparator<SweepPoint<E>> comp, int startHash);



	private OverlappingSweepPair<E> getPair(SweepPoint<E> a, SweepPoint<E> b) {
		if (poolPairs.length <= pairPointer + 1) {
			OverlappingSweepPair<E>[] old = poolPairs;
			final int oldSize = poolPairs.length;
			poolPairs = new OverlappingSweepPair[poolPairs.length * 2];
			System.arraycopy(old, 0, poolPairs, 0, oldSize);
			
			//initialize rest new
			for (int i = oldSize; i < poolPairs.length; i++) {
				poolPairs[i] = new OverlappingSweepPair();
			}
		}
		OverlappingSweepPair<E> p = poolPairs[pairPointer++];
		p.set(a, b);

		return p;
	}

	private class XComp implements Comparator<SweepPoint<E>> {
		@Override
		public int compare(SweepPoint<E> o1, SweepPoint<E> o2) {
			return Float.compare(o1.minX, o2.minX);
		}
	}

	private class YComp implements Comparator<SweepPoint<E>> {
		@Override
		public int compare(SweepPoint<E> o1, SweepPoint<E> o2) {
			return Float.compare(o1.minY, o2.minY);
		}
	}

	private class ZComp implements Comparator<SweepPoint<E>> {
		@Override
		public int compare(SweepPoint<E> o1, SweepPoint<E> o2) {
			return Float.compare(o1.minZ, o2.minZ);
		}
	}
}
