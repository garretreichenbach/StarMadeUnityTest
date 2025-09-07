package org.schema.game.server.controller.world.factory;

import java.util.Random;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;

public abstract class WorldCreatorFactory {

	public WorldCreatorFactory() {

	}

	public static short getRandomRockResource(Random random) {
		int ra = random.nextInt(16)+1;
		short s = ElementKeyMap.orientationToResIDMapping[ra];

		return s;
	}
	
	public boolean placeSolid(int x, int y, int z, Segment segToCreate, short type) throws SegmentDataWriteException {
		if (type == Element.TYPE_NONE) {
			return false;
		}
		if (!segToCreate.getSegmentData().containsUnsave((byte) x, (byte) y, (byte) z)) {
			segToCreate.getSegmentData().setInfoElementForcedAddUnsynched((byte) x, (byte) y, (byte) z, type, false);
			return true;
		}
		return false;
	}

	public static boolean placeSolid(int x, int y, int z, Segment segToCreate, short type, byte orientation) throws SegmentDataWriteException {
		if (type == Element.TYPE_NONE) {
			return false;
		}
		if (!segToCreate.getSegmentData().containsUnsave((byte) x, (byte) y, (byte) z)) {
			if (orientation == Element.LEFT) {
				orientation = Element.RIGHT;
			} else if (orientation == Element.RIGHT) {
				orientation = Element.LEFT;
				//FIXME :( has to switch orientation
			}
			segToCreate.getSegmentData().setInfoElementForcedAddUnsynched((byte) x, (byte) y, (byte) z, type, orientation, ElementInformation.activateOnPlacement(type), false);
			return true;
		}
		return false;
	}
	public boolean placeSolid(int x, int y, int z, Segment segToCreate, short type, byte orientation, boolean active) throws SegmentDataWriteException {
		if (type == Element.TYPE_NONE) {
			return false;
		}
		if (!segToCreate.getSegmentData().containsUnsave((byte) x, (byte) y, (byte) z)) {
			if (orientation == Element.LEFT) {
				orientation = Element.RIGHT;
			} else if (orientation == Element.RIGHT) {
				orientation = Element.LEFT;
				//FIXME :( has to switch orientation
			}
			segToCreate.getSegmentData().setInfoElementForcedAddUnsynched((byte) x, (byte) y, (byte) z, type, orientation, active ? (byte)1 : (byte)0, false);
			return true;
		}
		return false;
	}

	public abstract void createWorld(SegmentController world, Segment ws, RequestData requestData);

	public abstract boolean predictEmpty();
}
