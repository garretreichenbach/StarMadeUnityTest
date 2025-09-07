package org.schema.game.server.controller.world.factory;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.data.ServerConfig;

import com.bulletphysics.linearmath.Transform;

public class WorldCreatorShopFactory extends WorldCreatorFactory {
	public static final int HEIGHT = 15;
	private static final byte innerRadius = 3;
	private final long seed;

	public WorldCreatorShopFactory(long seed) {
		this.seed = seed;

	}
	@Override
	public boolean placeSolid(int x, int y, int z, Segment segToCreate, short type) throws SegmentDataWriteException {
		if (type == Element.TYPE_NONE) {
			return false;
		}
		if (!segToCreate.getSegmentData().containsUnsave((byte) x+8, (byte) y+8, (byte) z+8)) {
			segToCreate.getSegmentData().setInfoElementForcedAddUnsynched((byte) (x+8), (byte) (y+8), (byte) (z+8), type, false);
			return true;
		}
		return false;
	}
	@Override
	public boolean placeSolid(int x, int y, int z, Segment segToCreate, short type, byte orientation, boolean active) throws SegmentDataWriteException {
		if (type == Element.TYPE_NONE) {
			return false;
		}
		if (!segToCreate.getSegmentData().containsUnsave((byte) x+8, (byte) y+8, (byte) z+8)) {
			if (orientation == Element.LEFT) {
				orientation = Element.RIGHT;
			} else if (orientation == Element.RIGHT) {
				orientation = Element.LEFT;
				//FIXME :( has to switch orientation
			}
			segToCreate.getSegmentData().setInfoElementForcedAddUnsynched((byte) (x+8), (byte) (y+8), (byte) (z+8), type, orientation, active ? (byte)1 : (byte)0, false);
			return true;
		}
		return false;
	}
	private void createFromCorner(Segment w, SegmentController world, Random random, Vector3f v) throws SegmentDataWriteException {
		Vector3f d = new Vector3f();
		Transform t = new Transform();

		byte start = (byte) (16 / 2 - innerRadius);
		byte end = (byte) (16 / 2 + innerRadius);

		for (byte z = start; z < end; z++) {
			for (byte y = -8; y < 24; y++) {
				for (byte x = start; x < end; x++) {

					//					System.err.println(d.length() +";;; "+innerRadius+" "+(d.length() < (float)innerRadius));
					if ((x <= start && z <= start) ||
							(x >= end - 1 && z >= end - 1) ||
							(x <= start && z >= end - 1) ||
							(x >= end - 1 && z <= start)
							) {
						continue;
					}
					placeSolid(x, y, z, w, ElementKeyMap.HULL_ID);
				}
			}
			world.getSegmentBuffer().updateBB(w);
		}
		byte yStart = (byte) (w.pos.equals(0, 0, 0) ? 1 : 0);
		for (byte y = (byte) (yStart-8); y < 24; y++) {
			if (random.nextFloat() < 0.1f) {
				placeRing(y, w, t, random, v);
				//				if(Math.random() < 0.01f){
				//					placeSolid(2, y, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
				//					placeSolid(13, y, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
				//					placeSolid(8, y, 2, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
				//					placeSolid(8, y, 13, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
				//				}
			}
		}
		if (w.pos.equals(0, 0, 0)) {
			placeRing((byte) 0, w, t, random, v);
			placeSolid(0, 0, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(15, 0, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(8, 0, 0, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(8, 0, 15, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			
			
		}
		if (ServerConfig.SHOP_RAILS_ON_NORMAL.isOn() && w.pos.equals(0, 0, 0)) {
			placeRing((byte) 16, w, t, random, v);
			
			placeSolid(0, 16, 8, w, ElementKeyMap.RAIL_BLOCK_BASIC, (byte)1, false);
			placeSolid(15, 16, 8, w, ElementKeyMap.RAIL_BLOCK_BASIC, (byte)4, false);
			placeSolid(8, 16, 0, w, ElementKeyMap.RAIL_BLOCK_BASIC, (byte)5, true);
			placeSolid(8, 16, 15, w, ElementKeyMap.RAIL_BLOCK_BASIC, (byte)0, true);
		}
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {

		Random random = new Random(seed + ElementCollection.getIndex((short) w.pos.x, (short) w.pos.y, (short) w.pos.z));
		Vector3f v = new Vector3f(1, 0, 0);
		if (w.pos.y < HEIGHT * 16 && w.pos.y > -HEIGHT * 16 &&
				w.pos.x == 0 && w.pos.z == 0) {
			try {
				createFromCorner(w, world, random, v);
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

	private void placeRing(byte y, Segment w, Transform t, Random random, Vector3f v) throws SegmentDataWriteException {
		short type = random.nextFloat() < 0.5f ? ElementKeyMap.HULL_ID : ElementKeyMap.HULL_COLOR_BLACK_ID;
		t.setIdentity();

		for (float a = 0; a < FastMath.TWO_PI; a += 0.05f) {
			v.set(1, 0, 0);
			t.setIdentity();
			t.basis.rotY(a);
			t.transform(v);
			v.scale((16 / 2 - 1));
			int x = 16 / 2 + (int) (v.x - Element.BLOCK_SIZE / 2f);
			int z = 16 / 2 + (int) (v.z - Element.BLOCK_SIZE / 2f);

			placeSolid(x, y, z, w, type);
		}
		w.getSegmentController().getSegmentBuffer().updateBB(w);
	}

}
