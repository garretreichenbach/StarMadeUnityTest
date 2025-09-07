package org.schema.game.server.controller.world.factory.planet.structures;

import java.util.ArrayList;

import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.schine.resource.FileExt;

/**
 * Created by Jordan on 06/03/2017.
 */
public class TerrainStructureMulti extends TerrainStructure {

	private TerrainStructure[] randomList;
	public String path;

	public TerrainStructureMulti(String path) {
		this.path = path;
	}

	public void loadAllBlueprints(){

		ArrayList<TerrainStructure> tsList = new ArrayList<TerrainStructure>();

		int index = 0;

		while ((new FileExt("blueprints-terrain/" + path + index + SegmentDataFileUtils.BLOCK_FILE_EXT).exists())) {
			tsList.add(TerrainStructureBlueprint.get(path + index));
			index++;
		}

		System.err.println("[TERRAIN_STRUCTURE] Multi blueprint structure \"" + path + "\" found " +tsList.size() + " blueprints");

		if (tsList.size() == 0)
			return;

		randomList = tsList.toArray(new TerrainStructure[tsList.size()]);

		for (TerrainStructure t : tsList){
			if (t instanceof TerrainStructureBlueprint){
				((TerrainStructureBlueprint)t).loadFromFile();
			}
		}
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short metaData0, short metaData1, short metaData2) throws SegmentDataWriteException {

		if (randomList == null)
			return;

		int index = (
			(x & SegmentData.SEG_MINUS_ONE) * 1619 +
			(y & SegmentData.SEG_MINUS_ONE) * 31337 +
			(z & SegmentData.SEG_MINUS_ONE) * 6971) % randomList.length;

		randomList[index].build(seg, reqData, x, y, z, metaData0, metaData1, metaData2);
	}
}
