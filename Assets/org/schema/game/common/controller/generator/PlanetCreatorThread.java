package org.schema.game.common.controller.generator;

import api.listener.events.controller.planet.PlanetSegmentGenerateEvent;
import api.mod.StarLoader;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.*;
import org.schema.game.server.data.ServerConfig;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class PlanetCreatorThread extends CreatorThread {

	public static final ObjectArrayFIFOQueue<RequestDataPlanet> dataPool = new ObjectArrayFIFOQueue<RequestDataPlanet>(ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt());
	private static final float DEFAULT_RADIUS = 100f;

	static {
		for (int i = 0; i < ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt(); i++) {
			dataPool.enqueue(new RequestDataPlanet());
		}
	}

	public final Vector3f[] polygon;
	private final Dodecahedron s;
	public LongOpenHashSet locked = new LongOpenHashSet(128);
	public WorldCreatorFactory creator;

	public PlanetCreatorThread(Planet planet, PlanetType type) {
		super(planet);
		assert (planet.getCore() != null || planet.getPlanetCoreUID().equals("none"));

		s = new Dodecahedron(planet.getCore() != null ? planet.getCore().getRadius() : DEFAULT_RADIUS);
		s.create();
		Transform transform = s.getTransform(Math.max(0, planet.fragmentId), new Transform(), -0.5f, -0.5f);
		transform.inverse();
		polygon = s.getPolygon(Math.max(0, planet.fragmentId));
		Matrix3f m = new Matrix3f();
		m.rotY(FastMath.PI);
		for (int i = 0; i < polygon.length; i++) {
			transform.transform(polygon[i]);
			polygon[i].y = 0;
			m.transform(polygon[i]);
		}
		switch(type) {
			case EARTH -> creator = new WorldCreatorPlanetEarthFactory(planet.getSeed(), polygon, s.radius);
			case DESERT -> creator = new WorldCreatorPlanetDesertFactory(planet.getSeed(), polygon, s.radius);
			case CORRUPTED -> creator = new WorldCreatorPlanetColumnyFactory(planet.getSeed(), polygon, s.radius);
			case FROZEN -> creator = new WorldCreatorPlanetIceFactory(planet.getSeed(), polygon, s.radius);
			default -> creator = new WorldCreatorPlanetMarsFactory(planet.getSeed(), polygon, s.radius);
		}

	}

	@Override
	public int isConcurrent() {
		return NO_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return -1;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {
		//INSERTED CODE
		PlanetSegmentGenerateEvent event = new PlanetSegmentGenerateEvent(this, ws, (RequestDataPlanet) requestData, (WorldCreatorPlanetFactory) creator);
		StarLoader.fireEvent(event, true);
		if(event.isCanceled()) return;
//		if(getSegmentController() instanceof PlanetIco) {
//			int x = ws.pos.x;// / Segment.HALF_DIM;
//			int y = ws.pos.y;// / Segment.HALF_DIM;
//			int z = ws.pos.z;// / Segment.HALF_DIM;
//			if(((PlanetIco) getSegmentController()).isInPlanetCore(x, y, z)) {
//				return;
//			}
//		}
		//
		if (creator instanceof WorldCreatorPlanetFactory) {
			if (ws.pos.y < 0) {
				return;
			}
			if (!Dodecahedron.pnpoly(polygon, ws.pos.x, ws.pos.z, 48)) {
				return;
			}
		} 
		creator.createWorld(getSegmentController(), ws, requestData);
		//		System.out.println("[CREATOR] NEW SEGMENT from factory. local segID:" + ws.getId()
		//				+ ", pos (" + ws.x + ", " + ws.y + ", " + ws.z
		//				+ ")");

		//		((PlanetSurface)getSegmentController()).getWorldCreatorFactory().createWorld(((PlanetSurface)getSegmentController()), ws);
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
		return false;
	}

	@Override
	public RequestData allocateRequestData(int x, int y, int z) {
		synchronized (dataPool) {
			//lock column
			long index = ElementCollection.getIndex(z, 0, x);
			while (dataPool.isEmpty() || locked.contains(index)) {
				try {
					dataPool.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			locked.add(index);
			RequestDataPlanet dequeue = dataPool.dequeue();
			dequeue.cachePos.set(x, y, z);
			
			return dequeue; 

		}
	}

	@Override
	public void freeRequestData(RequestData data, int x, int y, int z) {
		assert (data != null);
		synchronized (dataPool) {
			((RequestDataPlanet) data).reset();
			//unlock column
			locked.remove(ElementCollection.getIndex(z, 0, x));
			dataPool.enqueue((RequestDataPlanet) data);
			dataPool.notify();
		}
	}

	public int margin(int input) {
		return input < 0 ? Math.min(0, input + 48) : Math.max(0, input - 48);
	}
}
