package org.schema.game.server.controller.world.factory;

import java.util.Random;

import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.terrain.CubeTerrainGenerator;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public abstract class WorldCreatorCubePlanetFactory extends WorldCreatorFactory {
	public static final int sideSize = 4;
	public int side;
	protected long seed;
	protected boolean initialized = false;
	//	private void setOcclusion(SegmentData data, int index, float occlusion, int sideId){
	//		data.setOcclusion(index, (byte)( occlusion <= 0.05 ? 5 : occlusion >= 0.95 ? 95 : occlusion * 100f), sideId);
	//	}
	protected CubeTerrainGenerator generator;

	public WorldCreatorCubePlanetFactory(long seed) {
		//		repositoryDim = new Vector3i(128,128,128);
		this.seed = seed;

	}

	public abstract void createAdditionalRegions(Random r);

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		if (!initialized) {
			//			permutations = Simplex.randomize(seed);
			//			Simplex.randomize();
			//			createRepository(repositoryDim);
			//			sample_test();
			generator = new CubeTerrainGenerator(((Planet) world).getSeed());
			generator.setWorldCreator(this);
			init();

			initialized = true;
		}

		try {
			gen(w);
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
		//		createFromCornerOnTheFly(w, world);
		//		applyOcclusion(w);
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

	protected void gen(Segment w) throws SegmentDataWriteException {

		int xB = w.pos.x;
		int yB = w.pos.y;
		int zB = w.pos.z;

		if (Math.abs(w.pos.x / 16) < sideSize && Math.abs(w.pos.z / 16) < sideSize) {
			if (w.pos.y > 0) {
				this.side = Element.TOP;

			} else {

				w.pos.y = -w.pos.y;

				this.side = Element.BOTTOM;
			}
		} else if (Math.abs(w.pos.y / 16) < sideSize && Math.abs(w.pos.z / 16) < sideSize) {
			if (w.pos.x > 0) {
				w.pos.y = w.pos.x;
				w.pos.x = yB;
				this.side = Element.RIGHT;
			} else {
				w.pos.y = -w.pos.x;
				w.pos.x = yB;
				this.side = Element.LEFT;
			}
		} else if (Math.abs(w.pos.y / 16) < sideSize && Math.abs(w.pos.x / 16) < sideSize) {
			if (w.pos.z > 0) {
				w.pos.y = w.pos.z;
				w.pos.z = yB;
				this.side = Element.FRONT;
			} else {
				w.pos.y = -w.pos.z;
				w.pos.z = yB;
				this.side = Element.BACK;
			}
		} else {
			//not on a cube size
			return;
		}
		w.pos.y -= sideSize * 16;

		int y = (side == Element.TOP || side == Element.BOTTOM) ? Math.max(0, (Math.abs(w.pos.y) / 16) - 3) % 4 : (Math.abs(w.pos.y) / 16) % 4;
		generator.generateSegment(
				w.getSegmentData(),
				64 + (w.pos.x / 16),
				y,
				64 + (w.pos.z / 16), false);

		w.pos.x = xB;
		w.pos.y = yB;
		w.pos.z = zB;
		generator.checkRegionHooks(w);

	}

	public short getCaveBottom() {
		return ElementKeyMap.TERRAIN_LAVA_ID;
	}

	public abstract short getFiller();

	public abstract TerrainDeco[] getGen();

	public abstract short getSolid();

	public abstract short getTop();

	protected void init() {
		Random r = new Random(seed);
		if (r.nextInt(20) == 0) {
			createAdditionalRegions(r);
		}

	}

}
