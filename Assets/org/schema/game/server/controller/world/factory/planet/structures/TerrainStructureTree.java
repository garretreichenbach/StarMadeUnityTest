package org.schema.game.server.controller.world.factory.planet.structures;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoise.FractalType;

public class TerrainStructureTree extends TerrainStructure {

	final static int TRUNK = registerBlock(ElementKeyMap.TERRAIN_TREE_TRUNK_ID);
	final static int LEAF = registerBlock(ElementKeyMap.TERRAIN_TREE_LEAF_ID);

	private static FastNoise leafNoise;
	private static FastNoise trunkNoise;

	static {
		leafNoise = new FastNoise();
		leafNoise.SetFrequency(0.05f);
		leafNoise.SetFractalType(FractalType.Billow);

		trunkNoise = new FastNoise();
		trunkNoise.SetFrequency(0.02f);
		trunkNoise.SetFractalType(FractalType.Billow);
	}

	private float trunkHeight;
	private float trunkWidth;
	private float sizeLeaves;

	public TerrainStructureTree(float trunkHeight, float trunkWidth, float sizeLeaves) {
		this.trunkHeight = trunkHeight;
		this.trunkWidth = trunkWidth;
		this.sizeLeaves = sizeLeaves;

		int maxSize = FastMath.fastCeil(Math.max(trunkWidth, sizeLeaves));

		bbMin.set(-maxSize, 0, -maxSize);
		bbMax.set(maxSize, FastMath.fastCeil(trunkHeight + sizeLeaves), maxSize);
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short metaData0, short metaData1, short metaData2) throws SegmentDataWriteException {

		SegmentData segData = seg.getSegmentData();

		int trunkHeightI = FastMath.fastRound(trunkHeight);
		int trunkWidthI = FastMath.fastCeil(trunkWidth);
		int sizeLeavesI = FastMath.fastCeil(sizeLeaves);

		float fx, fy, fz, mag;

		for (byte iX = (byte) Math.max(0, x - sizeLeavesI); iX < Math.min(SegmentData.SEG, x + sizeLeavesI + 1); iX++) {
			for (byte iY = (byte) Math.max(0, y - sizeLeavesI + trunkHeightI); iY < Math.min(SegmentData.SEG, y + sizeLeavesI + trunkHeightI + 1); iY++) {
				for (byte iZ = (byte) Math.max(0, z - sizeLeavesI); iZ < Math.min(SegmentData.SEG, z + sizeLeavesI + 1); iZ++) {

					fx = (iX - x) * 1.2f;
					fy = iY - y - trunkHeightI;
					fz = (iZ - z) * 1.2f;
					mag = FastMath.carmackInvSqrt(fx * fx + fy * fy + fz * fz) * sizeLeaves;
					fx *= mag;
					fy *= mag;
					fz *= mag;
					mag = sizeLeaves / mag;
					mag += leafNoise.GetSimplexFractal(fx + seg.pos.x, fy + seg.pos.y, fz + seg.pos.z) * 1.5f;

					if (mag < sizeLeaves && segData.getType(iX, iY, iZ) == Element.TYPE_NONE)
						reqData.currentChunkCache.placeBlock(
							LEAF,
							iX, iY, iZ,
							segData);
				}
			}
		}

		for (byte iX = (byte) Math.max(0, x - trunkWidthI); iX < Math.min(SegmentData.SEG, x + trunkWidthI + 1); iX++) {
			for (byte iY = (byte) Math.max(0, y); iY < Math.min(SegmentData.SEG, y + trunkHeightI + 1); iY++) {
				for (byte iZ = (byte) Math.max(0, z - trunkWidthI); iZ < Math.min(SegmentData.SEG, z + trunkWidthI + 1); iZ++) {

					fx = iX - x;
					fy = iY - y;
					fz = iZ - z;
					mag = FastMath.carmackInvSqrt(fx * fx + fz * fz) * trunkWidth;
					fx *= mag;
					fy *= mag;
					fz *= mag;
					mag = trunkWidth / mag;
					//mag += trunkNoise.GetSimplexFractal(x + sli.segment.pos.x, y + sli.segment.pos.y, z + sli.segment.pos.z) * 0.5f;

					if (mag < trunkWidth)
						reqData.currentChunkCache.placeBlock(
							TRUNK,
							iX, iY, iZ,
							segData);
				}
			}
		}
	}
}
