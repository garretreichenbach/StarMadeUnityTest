package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentDataInterface;
import org.schema.game.common.data.world.SegmentDataWriteException;

public class GeneratorFloraPlugin extends TerrainDeco {
	private short floraID;
	private short[] growthOn;
	public GeneratorFloraPlugin(Integer frequency, short grassID, short... growthOn) {
		this.growthOn = growthOn;
		floraID = grassID;
	}

	public GeneratorFloraPlugin(short grassID, short... growthOn) {
		this(4, grassID, growthOn);
	}

	public boolean canGrowOn(short type) {
		for (int i = 0; i < growthOn.length; i++) {
			if (type == growthOn[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean generate(SegmentDataInterface data, int x, int y, int z, Random randomContext) throws SegmentDataWriteException {

		int segPosY = Math.abs(data.getSegmentPos().y);
		int absX = Math.abs(x);
		int absY = Math.abs(y); 
		int absZ = Math.abs(z);
		for (int i = 0; ((i = data.getType((byte) (absX % 16), (byte) (absY % 16), (byte) (absZ % 16))) == 0
				|| i == ElementKeyMap.TERRAIN_TREE_LEAF_ID) && absY > 0 && absY > segPosY; absY--) {
		}

		if (absY == segPosY) {
			//TODO: handle this case when all heigth segments are available first
			return false;
		}
		for (int tries = 0; tries < 4; tries++) {
			int xPos = Math.abs((absX + randomContext.nextInt(8)) - randomContext.nextInt(8));
			int yPos = Math.abs((absY + randomContext.nextInt(4)) - randomContext.nextInt(4));
			int zPos = Math.abs((absZ + randomContext.nextInt(8)) - randomContext.nextInt(8));

			if (yPos > segPosY) {
				short type = data.getType((byte) (xPos % 16), (byte) (yPos % 16), (byte) (zPos % 16));
				byte under = (byte) ((yPos % 16) - 1);
				if (under >= 0) {

					short typeUnder = data.getType((byte) (xPos % 16), under, (byte) (zPos % 16));
					if (type == 0 && typeUnder > 0 && canGrowOn(typeUnder)) {
						data.setInfoElementForcedAddUnsynched((byte) (xPos % 16), (byte) (yPos % 16), (byte) (zPos % 16), floraID, (byte) Element.TOP, ElementInformation.activateOnPlacement(floraID), false);
					}
				}
			}
		}

		return true;
	}
}
