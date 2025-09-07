package org.schema.game.server.controller.world.factory;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameCreatureConfiguration;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.data.CreatureSpawn;
import org.schema.game.server.data.CreatureType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import com.bulletphysics.linearmath.Transform;

public class WorldCreatorShopMannedFactory extends WorldCreatorFactory {
	public static Vector3i shopkeepSpawner = new Vector3i(8, 267, 16); //Todo: Update for new shops
	public static final int HEIGHT = 15;
	private static final byte innerRadius = 3;
	private final long seed;

	public WorldCreatorShopMannedFactory(long seed) {
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
					if ((w.pos.y + y < 8 && w.pos.y + y > 0)) {
						continue;
					}
					placeSolid(x, y, z, w, ElementKeyMap.HULL_ID);
				}
			}
			world.getSegmentBuffer().updateBB(w);
		}
		byte yStart = (byte) (w.pos.equals(0, 0, 0) ? 1 : 0);
		for (byte y = (byte) (yStart-8); y < 24; y++) {
			if (random.nextFloat() < 0.1f && (y > 7 || y < -7)) {
				placeRing(y, w, t, random, v);
			}
		}
		if (w.pos.equals(0, 0, 0)) {
			placeRingSolid((byte) 0, w, t, random, v);
			placeGlassRing((byte) 4, w, t, random, v);
			placeRingSolid((byte) 8, w, t, random, v);
			placeSolid(2, 1, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(2, 1, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(13, 1, 8, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(8, 1, 2, w, ElementKeyMap.PLAYER_SPAWN_MODULE);
			placeSolid(8, 1, 13, w, ElementKeyMap.PLAYER_SPAWN_MODULE);

			placeSolid(9, 1, 8, w, ElementKeyMap.DECORATIVE_PANEL_1);
			placeSolid(9, 2, 8, w, ElementKeyMap.DECORATIVE_PANEL_2);

			if (world.isNewlyCreated()) {

				GameServerState state = ((GameServerState) world.getState());

				Sector sector = state.getUniverse().getSector(world.getSectorId());
				if (sector != null) {
					Transform transform = new Transform(world.getWorldTransform());
					Vector3f relPos = new Vector3f(0, 1 - 8, 0);
					transform.basis.transform(relPos);
					transform.origin.add(relPos);
					CreatureSpawn s = new CreatureSpawn(new Vector3i(sector.pos), transform, "A. Trader, the " + Math.abs(sector.pos.code()) + "th", CreatureType.CHARACTER) {
						@Override
						public void initAI(
								AIGameCreatureConfiguration<?, ?> aiConfiguration) {
							try {
								assert (aiConfiguration != null);
								aiConfiguration.get(Types.ORIGIN_X).switchSetting("16", false);
								aiConfiguration.get(Types.ORIGIN_Y).switchSetting("-7", false);
								aiConfiguration.get(Types.ORIGIN_Z).switchSetting("16", false);

								aiConfiguration.get(Types.ROAM_X).switchSetting("-3", false);
								aiConfiguration.get(Types.ROAM_Y).switchSetting("1", false);
								aiConfiguration.get(Types.ROAM_Z).switchSetting("-3", false);

								aiConfiguration.get(Types.ORDER).switchSetting(AIGameCreatureConfiguration.BEHAVIOR_ROAMING, false);

								aiConfiguration.getAiEntityState().getEntity().setFactionId(FactionManager.TRAIDING_GUILD_ID);
							} catch (StateParameterNotFoundException e) {
								e.printStackTrace();
							}
						}
					};
					state.getController().queueCreatureSpawn(s);
				}

			}
		}
		if (w.pos.equals(0, 0, 0)) {
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

	private void placeRingSolid(byte y, Segment w, Transform t, Random random, Vector3f v) throws SegmentDataWriteException {
		short type = random.nextFloat() < 0.5f ? ElementKeyMap.HULL_ID : ElementKeyMap.HULL_COLOR_BLACK_ID;
		t.setIdentity();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				v.set(
						x - (8f - 0.5f),
						0,
						z - (8f - 0.5f));

				if (v.length() < 7) {
					placeSolid(x, y, z, w, type);
				}
			}

		}

		w.getSegmentController().getSegmentBuffer().updateBB(w);
	}

	private void placeGlassRing(byte yB, Segment w, Transform t, Random random, Vector3f v) throws SegmentDataWriteException {
		short type = ElementKeyMap.GLASS_ID;
		t.setIdentity();

		for (float a = 0; a < FastMath.TWO_PI; a += 0.05f) {
			for (int y = yB - 4; y < yB + 4; y++) {
				v.set(1, 0, 0);
				t.setIdentity();
				t.basis.rotY(a);
				t.transform(v);
				v.scale((16 / 2 - 1));
				int x = 16 / 2 + (int) (v.x - Element.BLOCK_SIZE / 2f);
				int z = 16 / 2 + (int) (v.z - Element.BLOCK_SIZE / 2f);
				if (x != 1 && x != 14) {
					placeSolid(x, y, z, w, type);
				}
			}
		}
		w.getSegmentController().getSegmentBuffer().updateBB(w);
	}

}
