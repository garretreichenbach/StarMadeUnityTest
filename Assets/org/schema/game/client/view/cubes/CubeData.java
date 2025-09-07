package org.schema.game.client.view.cubes;

import org.schema.game.client.view.cubes.cubedyn.CubeMeshManagerBulkOptimized;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.SimplePosElement;

public class CubeData {

	//	static CubeMeshManager manager = new CubeMeshManager();
	public static final CubeMeshManagerBulkOptimized manager = new CubeMeshManagerBulkOptimized();
	public static int drawnMeshes;
	public static int cubeMeshes;
	public static boolean useBulkMode = true;
	public int totalDrawnBlockCount;
	public DrawableRemoteSegment lastTouched;
	public CubeMeshInterface cubeMesh;
	public boolean generated;
	private int blendedElementsCount;
	private int endBufferPos;
	private int blendBufferPos;
	public int[] opaqueSizes = new int[7];
	public int[][] opaqueRanges = new int[7][2];
	public int[][] blendedRanges = new int[7][2];
	
	public static void resetDrawn() {
	}
	public CubeData(){
		
	}
	public void contextSwitch(CubeMeshBufferContainer container,
	                          DrawableRemoteSegment segment, int c) {

		if (cubeMesh == null) {
			if (useBulkMode) {
				cubeMesh = manager.getInstance();
			} else {
				cubeMesh = new CubeMeshNormal();
			}
		}
		cubeMesh.contextSwitch(container, opaqueRanges, blendedRanges, blendBufferPos, totalDrawnBlockCount, segment);
	}

	public void released() {
		if (cubeMesh != null) {
			cubeMesh.released();
		}
	}

	public void createIndex(SegmentData segmentData, CubeMeshBufferContainer container) {

		container.dataBuffer.clearBuffers();
		totalDrawnBlockCount = 0;

		blendedElementsCount = 0;

		for (int dataIndex = 0, index = 0; index < SegmentData.BLOCK_COUNT; index++, dataIndex ++) {
			
			short type = segmentData.getType(dataIndex);

			if (type != 0 && (ElementInformation.isPhysical(type, segmentData, dataIndex)) ) {
				
				totalDrawnBlockCount++;
				if (ElementKeyMap.getInfo(type).isBlended()) {
					//only calc blended for one side
					container.blendedElementBuffer.put(blendedElementsCount, index);
					container.blendedElementTypeBuffer.put(blendedElementsCount, type);
					blendedElementsCount++;
				} else {
					container.p.fresh = true;
					for (int sideId = 0; sideId < 6; sideId++) {

						CubeMeshBufferContainer.putIndex(container, index, dataIndex, segmentData,
								SimplePosElement.SIDE_FLAG[sideId], sideId, type);
					}
				}
			}

		}
		container.dataBuffer.createOpaqueSizes(opaqueRanges);
		blendBufferPos = container.dataBuffer.totalPosition();
		for (int k = 0; k < blendedElementsCount; k++) {
			int index = container.blendedElementBuffer.get(k);
			int e = index ;
			container.p.fresh = true;
			for (int sideId = 0; sideId < 6; sideId++) {
				CubeMeshBufferContainer.putIndex(container, index, e, segmentData,
						SimplePosElement.SIDE_FLAG[sideId], sideId, container.blendedElementTypeBuffer.get(k));
			}
		}
		container.dataBuffer.createBlendedRanges(opaqueRanges, blendedRanges);
		endBufferPos = container.dataBuffer.totalPosition();
	}

	public void draw(int blended, int vismask) {
		cubeMesh.draw(blended, vismask);
		drawnMeshes++;
	}

	/**
	 * @return the blendBufferPos
	 */
	public int getBlendBufferPos() {
		return blendBufferPos;
	}

	/**
	 * @param blendBufferPos the blendBufferPos to set
	 */
	public void setBlendBufferPos(int blendBufferPos) {
		this.blendBufferPos = blendBufferPos;
	}

	public int getBlendedElementsCount() {
		return blendedElementsCount;
	}

}
