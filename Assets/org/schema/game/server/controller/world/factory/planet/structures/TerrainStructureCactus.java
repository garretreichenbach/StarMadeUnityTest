package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataStructureGen;

/**
 * Created by Jordan on 04/03/2017.
 */
public class TerrainStructureCactus extends TerrainStructure {

	static private final int CACTUS = registerBlock(ElementKeyMap.TERRAIN_CACTUS_ID);

	public TerrainStructureCactus(){
		bbMin.set(-1,1,-1);
		bbMax.set(1,16,1);
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short randomShort, short metaData1, short metaData2) throws SegmentDataWriteException {

		y++;

		assert (randomShort >= 0);

		SegmentData segData = seg.getSegmentData();

		int height = 4 + (randomShort & 3);

		if (x >= 0 && x < SegmentData.SEG &&
			z >= 0 && z < SegmentData.SEG) {

			byte max = (byte) Math.min(SegmentData.SEG, y + height);

			for (byte iY = (byte) Math.max(0, y); iY < max; iY++) {

				if (segData.getType((byte)x, iY, (byte)z) == Element.TYPE_NONE)
					reqData.currentChunkCache.placeBlock(
						CACTUS,
						(byte) x, iY, (byte) z,
						segData);
			}
		}

		int branchSide = (randomShort >> 2) & 7;

		if (branchSide < 4){
			buildBranch(segData, reqData, x, y, z, branchSide, height, randomShort >> 6);

			// double branch
			if (((randomShort >> 5) & 1) == 1){
				buildBranch(segData, reqData, x, y, z, (branchSide + 1) & 3, height, randomShort >> 8);
			}
		}
	}

	private void buildBranch(SegmentData segData, RequestDataStructureGen reqData, int x, int y, int z, int side, int cactusHeight, int randomShort) throws SegmentDataWriteException {

		int branchHeight = randomShort & 3;

		if (branchHeight > 0)
			branchHeight++;

		int branchStartHeight = ((randomShort >> 2) % (cactusHeight - 2)) + 1;

		// Branch first outward block

		switch (side) {
			case 0:
				x++;
				break;
			case 1:
				z++;
				break;
			case 2:
				x--;
				break;
			case 3:
				z--;
				break;
			default:
				return;
		}

		y += branchStartHeight;

		if (x >= 0 && x < SegmentData.SEG &&
			y >= 0 && y < SegmentData.SEG &&
			z >= 0 && z < SegmentData.SEG) {

			if (segData.getType((byte)x, (byte)y, (byte)z) == Element.TYPE_NONE)
				reqData.currentChunkCache.placeBlock(
					CACTUS,
					(byte) x, (byte)y, (byte) z,
					segData);
		}

		// Upwards of branch

		switch (side) {
			case 0:
				x++;
				break;
			case 1:
				z++;
				break;
			case 2:
				x--;
				break;
			case 3:
				z--;
				break;
			default:
				return;
		}

		if (x >= 0 && x < SegmentData.SEG &&
			z >= 0 && z < SegmentData.SEG) {

			byte max = (byte) Math.min(y + branchHeight, SegmentData.SEG);

			for (byte iY = (byte) Math.max(0, y); iY < max; iY++) {

				if (segData.getType((byte)x, iY, (byte)z) == Element.TYPE_NONE)
					reqData.currentChunkCache.placeBlock(
						CACTUS,
						(byte) x, iY, (byte) z,
						segData);
			}
		}
	}
}
