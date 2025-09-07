package org.schema.game.common.data.physics;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.physics.ClosestRayCastResultExt;

import javax.vecmath.Vector3f;

public class CubeRayCastResult extends ClosestRayCastResultExt {

	private boolean recordAllBlocks;
	private Int2ObjectOpenHashMap<BlockRecorder> recordedBlocks;
	private Int2ObjectOpenHashMap<LongOpenHashSet> collidingBlocks;
	private boolean hasCollidingBlockFilter = false;
	private boolean ignoereNotPhysical = false;
	private boolean onlyCubeMeshes;

	private boolean debug;
	private boolean ignoreDebris;
	private int blockDeepness;
	private boolean zeroHpPhysical = true;

	private SegmentController[] filter;
	private Segment segment;
	private boolean damageTest;
	private boolean simpleRayTest;
	private boolean cubesOnly;
	private NonBlockHitCallback nonBlockHitCallback;
	private boolean checkStabilizerPath = true;
	public float power;
	public boolean filterModeSingleNot;
	private boolean recordArmor;

	
	public SegmentTraversalInterface<?> newInnerSegmentIterator(){
		return new InnerSegmentIterator();
	}
	public CubeRayCastResult(Vector3f arg0, Vector3f arg1, Object owner, SegmentController ... filter) {
		super(arg0, arg1);
		this.setOwner(owner);
		this.filter = filter;
	}

	public Vector3i getNextToAbsolutePosition() {
		assert (segment != null && hasHit());

		Vector3f hitPointWorldInv = new Vector3f(this.hitPointWorld);

		segment.getSegmentController().getWorldTransformInverse().transform(hitPointWorldInv);

		Vector3i p = new Vector3i(segment.pos.x, segment.pos.y, segment.pos.z);

		p.x += (getCubePos().x - SegmentData.SEG_HALF);
		p.y += (getCubePos().y - SegmentData.SEG_HALF);
		p.z += (getCubePos().z - SegmentData.SEG_HALF);
		IntSet disabledSides = new IntOpenHashSet();
		for (int i = 0; i < 6; ++i) {
			Vector3i dir0 = Element.DIRECTIONSi[i];
			SegmentPiece piece = segment
					.getSegmentController()
					.getSegmentBuffer()
					.getPointUnsave(new Vector3i(p.x + dir0.x, p.y + dir0.y, p.z + dir0.z));
			if (piece != null && piece.getType() != Element.TYPE_NONE) {
				disabledSides.add(i);
			}
		}
		SegmentPiece piece = segment
				.getSegmentController()
				.getSegmentBuffer()
				.getPointUnsave(new Vector3i(p.x, p.y, p.z)); //autorequest true previously
		int side = Element.getSide(hitPointWorldInv, piece == null ? null : piece.getAlgorithm(), p, piece != null ? piece.getType() : (short)0, piece != null ? piece.getOrientation() : 0, disabledSides);

		System.err.println("[GETNEXTTONEAREST] SIDE: " + Element.getSideString(side) + ": " + hitPointWorldInv + "; " + p);
		switch(side) {
			case (Element.RIGHT) -> p.x += 1f;
			case (Element.LEFT) -> p.x -= 1f;
			case (Element.TOP) -> p.y += 1f;
			case (Element.BOTTOM) -> p.y -= 1f;
			case (Element.FRONT) -> p.z += 1f;
			case (Element.BACK) -> p.z -= 1f;
			//		default:	System.err.println("[BUILDMODEDRAWER] WARNING: NO SIDE recognized!!!");
		}
		p.add(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);

		return p;
	}

	/**
	 * @param segment the segment to set
	 */
	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	/**
	 * @return the recordAllBlocks
	 */
	@Override
	public boolean isRecordAllBlocks() {
		return recordAllBlocks;
	}

	/**
	 * @param recordAllBlocks the recordAllBlocks to set
	 */
	public void setRecordAllBlocks(boolean recordAllBlocks) {
		this.recordAllBlocks = recordAllBlocks;
	}

	@Override
	public int getBlockDeepness() {
		return blockDeepness;
	}

	/**
	 * @return the recordedBlocks
	 */
	@Override
	public Int2ObjectOpenHashMap<BlockRecorder> getRecordedBlocks() {
		return recordedBlocks;
	}

	/**
	 * @return the recordAllCubes
	 */
	@Override
	public boolean isHasCollidingBlockFilter() {
		return hasCollidingBlockFilter;
	}

	/**
	 * @param recordAllCubes the recordAllCubes to set
	 */
	public void setHasCollidingBlockFilter(boolean recordAllCubes) {
		this.hasCollidingBlockFilter = recordAllCubes;
	}

	/**
	 * @return the collidingBlocks
	 */
	@Override
	public Int2ObjectOpenHashMap<LongOpenHashSet> getCollidingBlocks() {
		return collidingBlocks;
	}

	/**
	 * @param collidingBlocks the collidingBlocks to set
	 */
	public void setCollidingBlocks(Int2ObjectOpenHashMap<LongOpenHashSet> collidingBlocks) {
		this.collidingBlocks = collidingBlocks;
	}

	/**
	 * @return the ignoreDebris
	 */
	@Override
	public boolean isIgnoreDebris() {
		return ignoreDebris;
	}

	/**
	 * @return the ignoereNotPhysical
	 */
	@Override
	public boolean isIgnoereNotPhysical() {
		return ignoereNotPhysical;
	}

	/**
	 * @return the segment
	 */
	@Override
	public Segment getSegment() {
		return segment;
	}

	@Override
	public void setSegment(Object segment) {
		this.segment = (Segment) segment;
	}

	@Override
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public SegmentController[] getFilter() {
		return filter;
	}

	public void setFilter(SegmentController ... filter) {
		if(filter != null) {
			this.filter = filter;
		}else {
			this.filter =  new SegmentController[0];
		}
	}

	@Override
	public boolean isZeroHpPhysical() {
		return zeroHpPhysical;
	}

	public void setZeroHpPhysical(boolean zeroHpPhysical) {
		this.zeroHpPhysical = zeroHpPhysical;
	}

	@Override
	public boolean isOnlyCubeMeshes() {
		return onlyCubeMeshes;
	}

	public void setOnlyCubeMeshes(boolean onlyCubeMeshes) {
		this.onlyCubeMeshes = onlyCubeMeshes;
	}

	/**
	 * @param ignoereNotPhysical the ignoereNotPhysical to set
	 */
	public void setIgnoereNotPhysical(boolean ignoereNotPhysical) {
		this.ignoereNotPhysical = ignoereNotPhysical;
	}

	/**
	 * @param ignoreDebris the ignoreDebris to set
	 */
	public void setIgnoreDebris(boolean ignoreDebris) {
		this.ignoreDebris = ignoreDebris;
	}

	/**
	 * @param recordedBlocks the recordedBlocks to set
	 * @param blockDeepness
	 */
	public void setRecordedBlocks(Int2ObjectOpenHashMap<BlockRecorder> recordedBlocks, int blockDeepness) {
		this.recordedBlocks = recordedBlocks;
		this.blockDeepness = blockDeepness;
	}

	@Override
	public boolean isDamageTest() {
		return damageTest;
	}

	public void setDamageTest(boolean damageTest) {
		this.damageTest = damageTest;
	}
	@Override
	public boolean isSimpleRayTest() {
		return this.simpleRayTest;
	}

	public void setSimpleRayTest(boolean simpleRayTest) {
		this.simpleRayTest = simpleRayTest;
	}

	@Override
	public boolean isCubesOnly() {
		return cubesOnly;
	}

	public void setCubesOnly(boolean cubesOnly) {
		this.cubesOnly = cubesOnly;
	}

	public void setHitNonblockCallback(NonBlockHitCallback nonBlockHitCallback) {
		this.nonBlockHitCallback = nonBlockHitCallback;
	}
	public NonBlockHitCallback getHitNonblockCallback(){
		return this.nonBlockHitCallback;
	}

	public void setCheckStabilizerPaths(boolean checkStabilizerPath) {
		this.checkStabilizerPath = checkStabilizerPath;
	}

	public boolean isCheckStabilizerPath() {
		return checkStabilizerPath;
	}
	public boolean isFiltered(SegmentController segmentController) {
		//filter mode: only process filtered
		if(filter == null || filter.length == 0) {
			return false;
		}
		if(filterModeSingleNot) {
			//filter mode: ignore list
			for(int i = 0; i < filter.length; i++) {
				if(filter[i] == segmentController) {
					return true;
				}
			}
			return false;
		}
		for(int i = 0; i < filter.length; i++) {
			if(filter[i] == segmentController) {
				return false;
			}
		}
		return true;
	}
	public boolean isFilteredRoot(SegmentController segmentController) {
		if(filter == null || filter.length == 0) {
			return false;
		}
		if(filterModeSingleNot) {
			//filter mode: ignore list
			for(int i = 0; i < filter.length; i++) {
				if(filter[i] == segmentController) {
					return true;
				}
			}
			return false;
		}
		//filter mode: only process filtered
		for(int i = 0; i < filter.length; i++) {
			if(filter[i].railController.getRoot() == segmentController) {
				return false;
			}
		}
		return true;
	}

}
