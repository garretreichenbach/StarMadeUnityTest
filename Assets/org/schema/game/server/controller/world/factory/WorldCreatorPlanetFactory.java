package org.schema.game.server.controller.world.factory;

import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;
import org.schema.game.server.controller.world.factory.terrain.TerrainGenerator;
import org.schema.game.server.data.ServerConfig;

import javax.vecmath.Vector3f;
import java.util.Random;

public abstract class WorldCreatorPlanetFactory extends WorldCreatorFactory {
	public final Vector3f[] poly;
	public float radius;
	protected long seed;
	protected boolean initialized = false;
	//	private void setOcclusion(SegmentData data, int index, float occlusion, int sideId){
	//		data.setOcclusion(index, (byte)( occlusion <= 0.05 ? 5 : occlusion >= 0.95 ? 95 : occlusion * 100f), sideId);
	//	}
	public TerrainGenerator generator;

	public WorldCreatorPlanetFactory(long seed, Vector3f[] poly, float radius) {
		//		repositoryDim = new Vector3i(128,128,128);
		this.seed = seed;
		this.poly = poly;
		this.radius = radius;
	}

	public abstract void createAdditionalRegions(Random r);

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		synchronized (this) {
			//this must only happen once
			if (!initialized) {
				initialize(world);
			}
		}

		if(w.getSegmentController() instanceof PlanetIco) return;
		try {
			gen(w, (RequestDataPlanet) requestData);
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
		//		createFromCornerOnTheFly(w, world);
		//		applyOcclusion(w);
	}
	public void initialize(SegmentController world){
		generator = new TerrainGenerator(((Planet) world).getSeed());
		generator.setWorldCreator(this);
		init(world);

		initialized = true;
	}
	@Override
	public boolean predictEmpty() {
				return false;
	}

	protected void gen(Segment w, RequestDataPlanet requestData) throws SegmentDataWriteException {
		
		
		if(!requestData.done){
			requestData.initWith(w);
		
			for(int in = 0; in < requestData.segs.length; in++){
				requestData.index = in;
				
				for(int i = 0; i < requestData.getR().segmentData.length; i++){
					Chunk16SegmentData c = requestData.getR().segmentData[i];
					
					generator.generateSegment(c, c.getSegmentPos(),
							64 + (c.getSegmentPos().x / TerrainGenerator.SEG), 
							(Math.abs(c.getSegmentPos().y) / TerrainGenerator.SEG), 
							64 + (c.getSegmentPos().z / TerrainGenerator.SEG), (c.getSegmentPos().y < 0), requestData);
			
//					System.err.println("RETRIVING ::: "+w.pos+" -------> "+c.getSegmentPos()+" ----> "+c.getSize());
					
					generator.checkRegionHooks(w, requestData);
				}
			
			}
			requestData.done = true;
		}
		requestData.applyTo(w);

	}

	public short getCaveBottom() {
		return ElementKeyMap.TERRAIN_LAVA_ID;
	}

	public abstract short getFiller();

	public abstract TerrainDeco[] getGen();

	public abstract short getSolid();

	public abstract short getTop();

	protected void init(SegmentController world) {
		Random r = new Random(seed);
		if (r.nextInt(ServerConfig.PLANET_SPECIAL_REGION_PROBABILITY.getInt()) == 0 || world.forceSpecialRegion) {
			createAdditionalRegions(r);
		}

	}

}
