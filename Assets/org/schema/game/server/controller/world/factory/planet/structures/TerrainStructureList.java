package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataStructureGen;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class TerrainStructureList {

	private static TerrainStructure.Type[] typeArray = TerrainStructure.Type.values();

	private ShortArrayList shortArray = new ShortArrayList(7);
	//ArrayList<TerrainStructure> typeArray = new ArrayList<TerrainStructure>(1);

	public void add(short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {

		shortArray.add((short) structureType.ordinal());
		shortArray.add(x);
		shortArray.add(y);
		shortArray.add(z);
		shortArray.add(metaData0);
		shortArray.add(metaData1);
		shortArray.add(metaData2);
	}

	public void add(short x, short y, short z, TerrainStructure.Type structureType, short metaData0, short metaData1) {
		add(x, y, z, structureType, metaData0, metaData1, (short) 0);
	}

	public void add(short x, short y, short z, TerrainStructure.Type structureType, short metaData0) {
		add(x, y, z, structureType, metaData0, (short) 0, (short) 0);
	}

	public void add(short x, short y, short z, TerrainStructure.Type structureType) {
		add(x, y, z, structureType, (short) 0, (short) 0, (short) 0);
	}

	public void addNotNear(short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1, short metaData2) {

		minNearDistance *= minNearDistance;

		for (int i = 1; i < shortArray.size(); i += 5) {
			int dx = x - shortArray.getShort(i);
			i += 2;
			int dz = z - shortArray.getShort(i);

			if (dx * dx + dz * dz < minNearDistance)
				return;
		}

		shortArray.add((short) structureType.ordinal());
		shortArray.add(x);
		shortArray.add(y);
		shortArray.add(z);
		shortArray.add(metaData0);
		shortArray.add(metaData1);
		shortArray.add(metaData2);
	}

	public void addNotNear(short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0, short metaData1) {
		addNotNear(x, y, z, minNearDistance, structureType, metaData0, metaData1, (short) 0);
	}

	public void addNotNear(short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType, short metaData0) {
		addNotNear(x, y, z, minNearDistance, structureType, metaData0, (short) 0, (short) 0);
	}

	public void addNotNear(short x, short y, short z, int minNearDistance, TerrainStructure.Type structureType) {
		addNotNear(x, y, z, minNearDistance, structureType, (short) 0, (short) 0, (short) 0);
	}


	public void buildAll(Segment s, RequestDataStructureGen reqData) throws SegmentDataWriteException {
		buildAll(s, reqData, 0, 0, 0);
	}

	public void buildAll(Segment s, RequestDataStructureGen reqData, int xOffset, int yOffset, int zOffset) throws SegmentDataWriteException {

		int i = 0;

		while (i < shortArray.size()) {

			TerrainStructure ts = typeArray[shortArray.getShort(i++)].terrainStructure;
			int x = shortArray.getShort(i++) + xOffset;
			int y = shortArray.getShort(i++) + yOffset;
			int z = shortArray.getShort(i++) + zOffset;

			if (x + ts.bbMin.x < SegmentData.SEG && x + ts.bbMax.x >= 0 &&
				y + ts.bbMin.y < SegmentData.SEG && y + ts.bbMax.y >= 0 &&
				z + ts.bbMin.z < SegmentData.SEG && z + ts.bbMax.z >= 0) {

				ts.build(s, reqData, x, y, z,
						 shortArray.getShort(i++),
						 shortArray.getShort(i++),
						 shortArray.getShort(i++));
			} else
				i += 3;
		}
	}

	public void clear() {
		shortArray.clear();
	}
}