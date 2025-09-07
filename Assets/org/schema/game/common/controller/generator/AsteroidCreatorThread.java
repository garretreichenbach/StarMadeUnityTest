package org.schema.game.common.controller.generator;

import api.listener.events.controller.asteroid.AsteroidSegmentGenerateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataBitMap;
import org.schema.game.common.data.world.SegmentDataSingle;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataAsteroid;
import org.schema.game.server.controller.TerrainChunkCacheElement;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.asteroid.old.*;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public class AsteroidCreatorThread extends CreatorThread {
	public static final ObjectArrayFIFOQueue<RequestData> dataPool = new ObjectArrayFIFOQueue<RequestData>(ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt());
	private static final boolean OPTIMIZE_DATA = true;

	static {
		for (int i = 0; i < ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt(); i++) {
			dataPool.enqueue(new RequestDataAsteroid());
		}
	}
	
	@Override
	public RequestData allocateRequestData(int x, int y, int z) {
		synchronized (dataPool) {
			//lock column
			while (dataPool.isEmpty()) {
				try {
					dataPool.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return dataPool.dequeue();

		}
	}

	@Override
	public void freeRequestData(RequestData data, int x, int y, int z) {
		assert (data != null);
		data.reset();
		synchronized (dataPool) {
			//unlock column
			dataPool.enqueue(data);
			dataPool.notify();
		}
	}
	protected WorldCreatorFloatingRockFactory creator;
	public AsteroidCreatorThread(FloatingRock world, AsteroidTypeOld type) {
		super(world);
		//this.creator = new WorldCreatorFloatingRockLavaFactory(world.getSeed());
		//if (true) return;
		switch(type) {
			case ICY -> this.creator = new WorldCreatorFloatingRockIcyFactory(world.getSeed());
			case LAVA -> this.creator = new WorldCreatorFloatingRockLavaFactory(world.getSeed());
			case MINERAL -> this.creator = new WorldCreatorFloatingRockMineralFactory(world.getSeed());
			case GOLDY -> this.creator = new WorldCreatorFloatingRockGoldyFactory(world.getSeed());
			case ICE_CORE -> this.creator = new WorldCreatorFloatingRockRockyIceCoreFactory(world.getSeed());
			case ROCKY_PURPLE -> this.creator = new WorldCreatorFloatingRockRockyPurpleFactory(world.getSeed());
			case ICE_HEAVY -> this.creator = new WorldCreatorFloatingRockRockyMoreIceFactory(world.getSeed());
			default -> this.creator = new WorldCreatorFloatingRockRockyTempoFactory(world.getSeed());
		}
	}

	protected AsteroidCreatorThread(FloatingRock world){ //do not call this unless subclassing. creator must be set in child initializer
		super(world);
	}

	@Override
	public int isConcurrent() {
		return FULL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return -1;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {
		//INSERTED CODE @112
		AsteroidSegmentGenerateEvent event = new AsteroidSegmentGenerateEvent(this, ws, (RequestDataAsteroid) requestData, this.creator);
		StarLoader.fireEvent(event, true);

		this.creator = event.getWorldCreatorFloatingRockFactory();
		///
		RequestDataAsteroid requestDataAsteroid = (RequestDataAsteroid)requestData;

		creator.createWorld(getSegmentController(), ws, requestDataAsteroid);

		// Disable optimised asteroids
		if (!OPTIMIZE_DATA)
			return;

		TerrainChunkCacheElement tcce = requestDataAsteroid.currentChunkCache;

		if (tcce.isEmpty())
			return;

		SegmentData optimisedData = null;

		if (tcce.isFullyFilledWithOneType()){
			optimisedData = new SegmentDataSingle(false, tcce.generationElementMap.getBlockDataFromList(0));

		} else if (tcce.generationElementMap.containsBlockIndexList.size() <= 16) {
			int[] blockTypes = new int[tcce.generationElementMap.containsBlockIndexList.size()];

			for (int i = 0; i < blockTypes.length; i++) {
				blockTypes[i] = tcce.generationElementMap.getBlockDataFromList(i);
			}

			optimisedData = new SegmentDataBitMap(false, blockTypes, ws.getSegmentData());
		}

		if (optimisedData != null) {
			optimisedData.setSize(ws.getSize());

			ws.getSegmentData().setBlockAddedForced(false);
			ws.getSegmentController().getSegmentProvider().getSegmentDataManager()
				.addToFreeSegmentData(ws.getSegmentData(), true, true);

			optimisedData.setBlockAddedForced(true);
			optimisedData.assignData(ws);

		}
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
				return false;
	}

	@Deprecated
	public enum AsteroidTypeOld {
		ROCKY(0.65f, en -> {
			return Lng.str("Varat, Zercaner");
		}),
		ICY(0.483f, en -> {
			return Lng.str("Hattel, Hylat");
		}),
		LAVA(0.9f, en -> {
			return Lng.str("Mattise, Jisper");
		}),
		MINERAL(0.566f, en -> {
			return Lng.str("Nocx, Macet");
		}),
		GOLDY(0.733f, en -> {
			return Lng.str("Bastyn, Fertikeen");
		}),
		ICE_CORE(0.483f, en -> {
			return Lng.str("Parseen, Sapsun");
		}),
		ROCKY_PURPLE(0.816f, en -> {
			return Lng.str("Sintyr, Threns");
		}),
		ICE_HEAVY(0.4f, en -> {
			return Lng.str("Rammet, Sertise");
		}),;

		private final Translatable translation;
		public final float temperature;

		private AsteroidTypeOld(final float temp, Translatable translation) {
			this.temperature = temp;
			this.translation = translation;
		}

		public String getTranslation(){
			return translation.getName(this);
		}



	}

}
