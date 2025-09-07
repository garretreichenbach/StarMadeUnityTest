package org.schema.game.server.controller.world.factory;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;

import com.bulletphysics.linearmath.Transform;

public class WorldCreatorDeathStarFactory extends WorldCreatorFactory {
	Vector3i cPos = new Vector3i();
	Vector3i cCenterDist = new Vector3i();
	Vector3f corridorDist = new Vector3f();
	Vector3f corridorTestStart = new Vector3f();
	Vector3f corridorTestElement = new Vector3f();
	Transform t = new Transform();
	private int centerRadius = 16;
	private int maxRadius = 3 * 16;
	private int cooridorRadius = 4;
	private Vector3i center;
	private Vector3i corridorStart;

	public WorldCreatorDeathStarFactory(long seed, Vector3i center) {
		this.center = center;
		this.corridorStart = new Vector3i(maxRadius, maxRadius, maxRadius);
	}

	private void createFromCorner(Segment w, SegmentController world) throws SegmentDataWriteException {
		short type = ElementKeyMap.DEATHSTAR_CORE_ID;
		for (byte z = 0; z < SegmentData.SEG; z++) {
			for (byte y = 0; y < SegmentData.SEG; y++) {
				for (byte x = 0; x < SegmentData.SEG; x++) {
					cPos.set(x + w.pos.x, y + w.pos.y, z + w.pos.z);
					cCenterDist.sub(center, cPos);

					corridorTestStart.set(corridorStart.x, corridorStart.y, corridorStart.z);
					corridorDist.set(center.x, center.y, center.z);
					corridorDist.x -= corridorStart.x;
					corridorDist.y -= corridorStart.y;
					corridorDist.z -= corridorStart.z;

					float l = corridorDist.length();

					corridorDist.normalize();

					float start = 0;
					boolean corridor = false;
					while (start < l) {
						corridorTestStart.add(corridorDist);
						corridorTestElement.set(corridorTestStart);
						corridorTestElement.x -= cPos.x;
						corridorTestElement.y -= cPos.y;
						corridorTestElement.z -= cPos.z;
						if (corridorTestElement.length() < cooridorRadius) {
							corridor = true;
							break;
						}
						start++;
					}
					if (cPos.equals(center)) {
						System.err.println("PLACED DEATH START CENTER!");
						placeSolid(x, y, z, w, type);
					}
					if (corridor) {
						continue;
					}

					if (cCenterDist.length() > centerRadius + 1 && cCenterDist.length() < maxRadius) {
						placeSolid(x, y, z, w, ElementKeyMap.HULL_ID);
					} else if (cCenterDist.length() == centerRadius + 1) {
						placeSolid(x, y, z, w, ElementKeyMap.TERRAIN_LAVA_ID);
					}

				}
			}
		}

		world.getSegmentBuffer().updateBB(w);

	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		try {
			createFromCorner(w, world);
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

}
