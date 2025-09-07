package org.schema.game.common.controller.pathfinding;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.pathfinding.AbstractPathFindingHandler;
import org.schema.game.server.controller.pathfinding.AbstractPathRequest;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugPoint;

import it.unimi.dsi.fastutil.floats.Float2LongRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class AbstractAStarCalculator<A extends AbstractPathRequest> {

	public static final long MAX_CALCULATION_TIME = 5000;
	protected final boolean recordPath;
	protected final LongArrayList path = new LongArrayList();
	protected final Long2ObjectOpenHashMap<Node> pathMap = new Long2ObjectOpenHashMap<Node>();
	private final Vector3i dirA = new Vector3i();
	private final Vector3i dirB = new Vector3i();
	private final Vector3f bbTest = new Vector3f();
	protected SegmentController controller;
	protected Vector3i search;
	protected LongOpenHashSet currentPart;
	protected FloatingRock to;
	protected Vector3i min = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	protected Vector3i max = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	protected Vector3i center = new Vector3i();
	protected long currentStart;
	protected Vector3i origin;
	protected BoundingBox roam;
	private Float2LongRBTreeMap openCollection = new Float2LongRBTreeMap();
	private LongOpenHashSet closedCollection = new LongOpenHashSet();
	private ObjectArrayList<LongOpenHashSet> partCollection = new ObjectArrayList<LongOpenHashSet>();
	private Vector3i absPosDist = new Vector3i();
	private Vector3i nextPosTmp = new Vector3i();
	private Vector3i absPosBase = new Vector3i();
	private Vector3i absPos = new Vector3i();
	private Vector3i absPosBefore = new Vector3i();
	public AbstractAStarCalculator(boolean recordPath) {
		super();
		this.recordPath = recordPath;
	}

	public void optimizePath(LongArrayList path) {

	}

	public void init(A cr) {
		this.controller = cr.getSegmentController();
		min.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		max.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		openCollection.clear();
		closedCollection.clear();
		partCollection.clear();
		pathMap.clear();
		path.clear();
		currentPart = null;
	}

	public boolean calculateDir(int x, int y, int z, Vector3i origin, BoundingBox roam, Vector3i preferredDir) {

		this.origin = origin;
		this.roam = new BoundingBox(roam);

		assert (origin.x > Integer.MIN_VALUE && origin.y > Integer.MIN_VALUE && origin.z > Integer.MIN_VALUE);
		this.roam.min.x += origin.x;
		this.roam.min.y += origin.y;
		this.roam.min.z += origin.z;

		this.roam.max.x += origin.x;
		this.roam.max.y += origin.y;
		this.roam.max.z += origin.z;

		//		System.err.println("Calculating path from "+x+", "+y+", "+z);

		closedCollection.add(ElementCollection.getIndex(x, y, z));

		float xxMax = roam.max.x - roam.min.x;
		float yyMax = roam.max.y - roam.min.y;
		float zzMax = roam.max.z - roam.min.z;

		float mmm = Math.max(Math.max(xxMax, yyMax), zzMax);
		int length = Universe.getRandom().nextInt((int) Math.ceil(mmm)) + 1;

//		System.err.println("LENGTH::::::::::::::::::::::::::::::::::::: "+length);
		int steps = 0;
		while (!closedCollection.isEmpty()) {
			if (openCollection.isEmpty()) {
				//				if(lastElementCollection != null){
				//					System.err.println(lastElementCollection+" FOUND SIZE: "+lastElementCollection.size());
				//				}
				assert (!closedCollection.isEmpty());
				// find new start position
				LongIterator iterator = closedCollection.iterator();
				long nextNotConsumedPoint = iterator.nextLong();
				iterator.remove();
				float weight = 1;
				openCollection.put(weight, nextNotConsumedPoint);
				currentPart = new LongOpenHashSet();
				partCollection.add(currentPart);

				currentStart = nextNotConsumedPoint;
				currentPart.add(nextNotConsumedPoint);
				ElementCollection.getPosFromIndex(nextNotConsumedPoint, absPos);
				min.x = Math.min(absPos.x, min.x);
				min.y = Math.min(absPos.y, min.y);
				min.z = Math.min(absPos.z, min.z);

				max.x = Math.max(absPos.x + 1, max.x);
				max.y = Math.max(absPos.y + 1, max.y);
				max.z = Math.max(absPos.z + 1, max.z);

				if (recordPath) {
					pathMap.put(nextNotConsumedPoint, new Node(0, weight));
				}
				//				E collectionInstance = getCollectionInstance();
				//
				//				collectionInstance.addElement(nextNotConsumedPoint);
				//
				//				getCollections().add(collectionInstance);
				//
				//				lastElementCollection = collectionInstance;
			}
			while (!openCollection.isEmpty()) {
				steps++;

				float smallest = openCollection.firstFloatKey();

				long lastPoint = openCollection.remove(smallest);
				absPosBefore.set(absPos);
				ElementCollection.getPosFromIndex(lastPoint, absPos);
//				System.err.println("NEXT: "+absPos);

				int neighbor;
				for (int i = 0; i < 6; i++) {
					long index = ElementCollection.getSide(lastPoint, i);

					if (!currentPart.contains(index)) {
						nextPosTmp.set(absPos);
						nextPosTmp.add(Element.DIRECTIONSi[i]);
						absPosDist.set(x, y, z);
						absPosDist.sub(nextPosTmp);
						if ((absPosDist.length() >= length) && canTravelPoint(nextPosTmp, absPos, controller)) {
							long end = ElementCollection.getIndex(nextPosTmp);

							if (recordPath) {
								pathMap.put(index, new Node(lastPoint, 0));
								long cur = end;
								long last = -1;
								while (cur != currentStart) {
									Node node = pathMap.get(cur);
									//									if(node != null){
									assert (node != null) : currentStart + "::: " + last + " -> " + cur + " :::" + end + "\n" + pathMap;
									path.add(cur);
									last = cur;
									cur = node.parent;
									//									}else{
									//										cur = currentStart;
									//										System.err.println("[PATH] PATH ONLY ONE WIDE");
									//									}
								}
								path.add(currentStart);

								//								System.err.println("[ASTAR] path length: "+path.size());
								if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
									for (long in : path) {
										ElementCollection.getPosFromIndex(in, absPosBase);
										Vector3f vv = new Vector3f(absPosBase.x - SegmentData.SEG_HALF, absPosBase.y - SegmentData.SEG_HALF, absPosBase.z - SegmentData.SEG_HALF);
										if (controller != null) {
											controller.getWorldTransform().transform(vv);
										}
										DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(0, 0, 1, 1));
										debugPoint.size = 0.6f;
										debugPoint.LIFETIME = 10000;
										DebugDrawer.points.add(debugPoint);
									}

								}
							}

							//							System.err.println("FOUND CORE CONNECTION; steps: "+steps+"; setSize: "+currentPart.size());
							//							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//								for(long in : currentPart){
							//									ElementCollection.getPosFromIndex(in, absPosBase);
							//									Vector3f vv = new Vector3f(absPosBase.x-8, absPosBase.y-8, absPosBase.z-8);
							//									controller.getWorldTransform().transform(vv);
							//									DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1,1,1,1));
							//									debugPoint.LIFETIME = 5000;
							//									debugPoint.size = 1.1f;
							//									DebugDrawer.points.add(debugPoint);
							//								}
							//
							//
							//							}

							return true;
						}
						if (canTravelPoint(nextPosTmp, absPos, controller)) {

							//							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//									ElementCollection.getPosFromIndex(index, absPosBase);
							//									Vector3f vv = new Vector3f(absPosBase.x-8, absPosBase.y-8, absPosBase.z-8);
							//									controller.getWorldTransform().transform(vv);
							//									DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1,1,0,1));
							//									debugPoint.LIFETIME = 5000;
							//									debugPoint.size = 1.1f;
							//									DebugDrawer.points.add(debugPoint);
							//
							//
							//							}

							min.x = Math.min(nextPosTmp.x, min.x);
							min.y = Math.min(nextPosTmp.y, min.y);
							min.z = Math.min(nextPosTmp.z, min.z);

							max.x = Math.max(nextPosTmp.x, max.x);
							max.y = Math.max(nextPosTmp.y, max.y);
							max.z = Math.max(nextPosTmp.z, max.z);

							currentPart.add(index);

							float weight = getWeightByBestDir(absPosBefore, absPos, nextPosTmp, preferredDir);
							assert (weight < Float.MAX_VALUE);
							float moveWeight = 0.5f;
							if (recordPath) {
								Node last = pathMap.get(lastPoint);
								Node next = pathMap.get(index);
								if (next == null) {
									next = new Node();
									pathMap.put(index, next);
									next.parent = lastPoint;
									next.costTo = last.costTo + moveWeight;
								} else {
									if (last.costTo + moveWeight < next.costTo) {
										next.parent = lastPoint;
										next.costTo = last.costTo + moveWeight;
									}
								}

								weight = next.costTo + weight;

								//update new weight if necessary
								openCollection.values().remove(index);
							}

							while (openCollection.containsKey(weight)) {
								float wBef = weight;
								weight += 0.001f;
								if (wBef == weight) {
									System.err.println("ROUNDING ERROR: " + weight);
									assert (false);
									weight = 0;
								}
//								System.err.println("CONTAINS: "+weight+"; "+Float.MAX_VALUE+"; "+(weight<Float.MAX_VALUE));
							}
							long old = openCollection.put(weight, index);
						}
					}
				}

			}

		}
		//		System.err.println("NOT FOUND CORE CONNECTION; steps: "+steps+"; setSize: "+currentPart.size());
		System.err.println("[PATH] CALC MINMAX " + min + ", " + max);
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			for (long index : currentPart) {
				ElementCollection.getPosFromIndex(index, absPosBase);
				Vector3f vv = new Vector3f(absPosBase.x - SegmentData.SEG_HALF, absPosBase.y - SegmentData.SEG_HALF, absPosBase.z - SegmentData.SEG_HALF);
				controller.getWorldTransform().transform(vv);
				DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1, 0, 0, 1));
				debugPoint.LIFETIME = 8000;
				debugPoint.size = 0.6f;
				DebugDrawer.points.add(debugPoint);
			}
		}
		return false;
	}

	protected boolean isTurn(Vector3i before, Vector3i from, Vector3i to) {
		if (before.equals(from)) {
			return false;
		} else {
			dirA.sub(from, before);
			dirB.sub(to, from);

			return (((dirA.x != 0 && dirB.x == 0) || (dirB.x != 0 && dirA.x == 0) || (dirA.z != 0 && dirB.z == 0) || (dirB.z != 0 && dirA.z == 0)));
		}
	}

	protected boolean isInbound(Vector3i pos) {
		bbTest.set(pos.x - SegmentData.SEG_HALF, pos.y - SegmentData.SEG_HALF, pos.z - SegmentData.SEG_HALF);
		boolean inside = roam.isInside(bbTest);
		return inside;
	}

	public boolean calculate(int x, int y, int z, int toX, int toY, int toZ) throws CalculationTookTooLongException {
		long tStarted = System.currentTimeMillis();

		search = new Vector3i(toX, toY, toZ);

		if (x == toX && y == toY && z == toZ) {
			return false;
		}
		//		System.err.println("Calculating path from "+x+", "+y+", "+z+" to "+toX+", "+toY+", "+toZ);

		closedCollection.add(ElementCollection.getIndex(x, y, z));

		int steps = 0;
		while (!closedCollection.isEmpty()) {
			if (openCollection.isEmpty()) {
				//				if(lastElementCollection != null){
				//					System.err.println(lastElementCollection+" FOUND SIZE: "+lastElementCollection.size());
				//				}
				assert (!closedCollection.isEmpty());
				// find new start position
				LongIterator iterator = closedCollection.iterator();
				long nextNotConsumedPoint = iterator.nextLong();
				iterator.remove();
				float weight = getWeight(search, search, ElementCollection.getPosFromIndex(nextNotConsumedPoint, absPosBase));
				openCollection.put(weight, nextNotConsumedPoint);
				currentPart = new LongOpenHashSet();
				partCollection.add(currentPart);

				currentStart = nextNotConsumedPoint;
				currentPart.add(nextNotConsumedPoint);
				ElementCollection.getPosFromIndex(nextNotConsumedPoint, absPos);
				min.x = Math.min(absPos.x, min.x);
				min.y = Math.min(absPos.y, min.y);
				min.z = Math.min(absPos.z, min.z);

				max.x = Math.max(absPos.x + 1, max.x);
				max.y = Math.max(absPos.y + 1, max.y);
				max.z = Math.max(absPos.z + 1, max.z);

				if (recordPath) {
					pathMap.put(nextNotConsumedPoint, new Node(0, weight));
				}
				//				E collectionInstance = getCollectionInstance();
				//
				//				collectionInstance.addElement(nextNotConsumedPoint);
				//
				//				getCollections().add(collectionInstance);
				//
				//				lastElementCollection = collectionInstance;
			}
			while (!openCollection.isEmpty()) {

				AbstractPathFindingHandler.currentIt++;
				steps++;

				if (steps % 1000 == 0 && System.currentTimeMillis() - tStarted > MAX_CALCULATION_TIME) {
					throw new CalculationTookTooLongException("Calculated Steps: " + steps);
				}

				float smallest = openCollection.firstFloatKey();

				long lastPoint = openCollection.remove(smallest);

				ElementCollection.getPosFromIndex(lastPoint, absPos);
				Node back = pathMap.get(lastPoint);
				if (back != null) {
					ElementCollection.getPosFromIndex(back.parent, absPosBefore);
				} else {
					absPosBefore.set(absPos);
				}

				int neighbor;
				for (int i = 0; i < 6; i++) {
					long index = ElementCollection.getSide(lastPoint, i);

					if (!currentPart.contains(index)) {
						nextPosTmp.set(absPos);
						nextPosTmp.add(Element.DIRECTIONSi[i]);
						if (nextPosTmp.equals(search) && canTravelPoint(nextPosTmp, absPos, controller)) {
							long end = ElementCollection.getIndex(search);

							if (recordPath) {
								pathMap.put(index, new Node(lastPoint, 0));
								long cur = end;
								long last = -1;
								while (cur != currentStart) {
									Node node = pathMap.get(cur);
									//									if(node != null){
									assert (node != null) : currentStart + "::: " + last + " -> " + cur + " :::" + end + "\n" + pathMap;
									path.add(cur);
									last = cur;
									cur = node.parent;
									//									}else{
									//										cur = currentStart;
									//										System.err.println("[PATH] PATH ONLY ONE WIDE");
									//									}
								}
								path.add(currentStart);

								//								System.err.println("[ASTAR] path length: "+path.size());
								//								if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
								//									for(long in : path){
								//										ElementCollection.getPosFromIndex(in, absPosBase);
								//										Vector3f vv = new Vector3f(absPosBase.x-8, absPosBase.y-8, absPosBase.z-8);
								//										if(controller != null){
								//											controller.getWorldTransform().transform(vv);
								//										}
								//										DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(0,0,1,1));
								//										debugPoint.size = 0.6f;
								//										debugPoint.LIFETIME = 10000;
								//										DebugDrawer.points.add(debugPoint);
								//									}
								//
								//
								//								}
							}

							//							System.err.println("FOUND CORE CONNECTION; steps: "+steps+"; setSize: "+currentPart.size());
							//							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//								for(long in : currentPart){
							//									ElementCollection.getPosFromIndex(in, absPosBase);
							//									Vector3f vv = new Vector3f(absPosBase.x-8, absPosBase.y-8, absPosBase.z-8);
							//									controller.getWorldTransform().transform(vv);
							//									DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1,1,1,1));
							//									debugPoint.LIFETIME = 5000;
							//									debugPoint.size = 1.1f;
							//									DebugDrawer.points.add(debugPoint);
							//								}
							//
							//
							//							}

							return true;
						}
						if (canTravelPoint(nextPosTmp, absPos, controller)) {

							//							if(EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()){
							//									ElementCollection.getPosFromIndex(index, absPosBase);
							//									Vector3f vv = new Vector3f(absPosBase.x-8, absPosBase.y-8, absPosBase.z-8);
							//									if(controller != null){
							//										controller.getWorldTransform().transform(vv);
							//									}
							//									DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1,1,0,1));
							//									debugPoint.LIFETIME = 5000;
							//									debugPoint.size = 1.1f;
							//									DebugDrawer.points.add(debugPoint);
							//
							//
							//							}

							min.x = Math.min(nextPosTmp.x, min.x);
							min.y = Math.min(nextPosTmp.y, min.y);
							min.z = Math.min(nextPosTmp.z, min.z);

							max.x = Math.max(nextPosTmp.x, max.x);
							max.y = Math.max(nextPosTmp.y, max.y);
							max.z = Math.max(nextPosTmp.z, max.z);

							currentPart.add(index);

							float weight = getWeight(absPosBefore, absPos, nextPosTmp);
							float moveWeight = getMoveWeight(absPosBefore, absPos, nextPosTmp);
							if (recordPath) {
								Node last = pathMap.get(lastPoint);
								Node next = pathMap.get(index);
								if (next == null) {
									next = new Node();
									pathMap.put(index, next);
									next.parent = lastPoint;
									next.costTo = last.costTo + moveWeight;
								} else {
									if (last.costTo + moveWeight < next.costTo) {
										next.parent = lastPoint;
										next.costTo = last.costTo + moveWeight;
									}
								}

								weight = next.costTo + weight;

								//update new weight if necessary
								openCollection.values().remove(index);
							}

							while (openCollection.containsKey(weight)) {
								weight += 0.001f;
							}
							long old = openCollection.put(weight, index);
						}
					}
				}

			}

		}
		//		System.err.println("NOT FOUND CORE CONNECTION; steps: "+steps+"; setSize: "+currentPart.size());
		System.err.println("CALC MINMAX " + min + ", " + max);
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			for (long index : currentPart) {
				ElementCollection.getPosFromIndex(index, absPosBase);
				Vector3f vv = new Vector3f(absPosBase.x - SegmentData.SEG_HALF, absPosBase.y - SegmentData.SEG_HALF, absPosBase.z - SegmentData.SEG_HALF);
				if (controller != null) {
					controller.getWorldTransform().transform(vv);
				}
				DebugPoint debugPoint = new DebugPoint(vv, new Vector4f(1, 0, 0, 1));
				debugPoint.LIFETIME = 8000;
				debugPoint.size = 0.6f;
				DebugDrawer.points.add(debugPoint);
			}
		}
		return false;
	}

	protected float getMoveWeight(Vector3i before, Vector3i from, Vector3i to) {
		return 0.5f;
	}

	public abstract boolean canTravelPoint(Vector3i point, Vector3i from, SegmentController controller);

	protected abstract float getWeight(Vector3i before, Vector3i from, Vector3i to);

	protected abstract float getWeightByBestDir(Vector3i before, Vector3i from, Vector3i to, Vector3i prefferedDir);

	protected float getDistToSearchPos(Vector3i from) {
		//		ElementCollection.getPosFromIndex(index, absPosBase);
		absPosBase.set(from);
		absPosBase.x -= search.x;
		absPosBase.y -= search.y;
		absPosBase.z -= search.z;
		return absPosBase.length();
	}

	protected float getDistToSearchPos(int index) {
		ElementCollection.getPosFromIndex(index, absPosBase);
		absPosBase.x -= search.x;
		absPosBase.y -= search.y;
		absPosBase.z -= search.z;
		return absPosBase.length();
	}

	/**
	 * @return the path
	 */
	public LongArrayList getPath() {
		return path;
	}

	public int getCurrentPartSize() {
		return currentPart.size();
	}

	class Node {

		long parent;
		float costTo;

		public Node(long parent, float costTo) {
			super();
			this.parent = parent;
			this.costTo = costTo;
		}
		public Node() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Node [parent=" + parent + ", costTo=" + costTo + "]";
		}

	}

}
