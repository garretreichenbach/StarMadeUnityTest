package org.schema.game.common.controller.generator;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.TeamDeathStar;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.world.factory.WorldCreatorDeathStarFactory;

public class DeathStarCreatorThread extends CreatorThread {

	private WorldCreatorDeathStarFactory creator;

	public DeathStarCreatorThread(TeamDeathStar teamDeathStar) {
		super(teamDeathStar);
		this.creator = new WorldCreatorDeathStarFactory(teamDeathStar.getSeed(), new Vector3i(Ship.core));

	}

	@Override
	public int isConcurrent() {
		return LOCAL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		//		System.err.println("searching for "+minThis+" to "+maxThis);
		//		PlanetSurface w = ((PlanetSurface)getSegmentController());
		//		long t = System.currentTimeMillis();
		//		String query = "SELECT elements FROM " +
		//				"GALAXY_"+w.getPlanet().getStarSystem().getGalaxy().getName()+"_SYSTEM_"+w.getPlanet().getStarSystem().getName()+"_DATA "+
		//				"WHERE " +
		//				"(x = "+ws.pos.x+") AND" +
		//				"(y = "+ws.pos.y+") AND" +
		//				"(z = "+ws.pos.z+")";
		//		EmbedResultSet40 result = ((GameClientState)getSegmentController().getState()).getSqlConnection().executeQuery(query);
		////		System.err.println("searching for "+minThis+" to "+maxThis+"   "+query);
		////		System.err.println("returned "+result.size()+" results ");
		//		try {
		//		if(result.next()){
		//			Blob blob = result.getBlob(1);
		//			byte[] dataBytes = blob.getBytes(1L, (int)blob.length());
		//			ws.getSegmentData().createFromByteBuffer(dataBytes, getSegmentController().getState());
		//			return 1;
		//		}
		//		System.err.println("DB QUERY: nothing found at "+ws.pos+",   Time "+(System.currentTimeMillis()-t)+" mass");
		//		} catch (SQLException e1) {
		//
		//			e1.printStackTrace();
		//		}
		return -1;
	}

	@Override
	public void onNoExistingSegmentFound(Segment ws, RequestData requestData) {

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

}
