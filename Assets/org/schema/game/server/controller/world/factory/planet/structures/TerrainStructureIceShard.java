package org.schema.game.server.controller.world.factory.planet.structures;

import java.util.Random;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.controller.world.factory.planet.FastNoise;
import org.schema.game.server.controller.world.factory.planet.FastNoise.FractalType;

import com.bulletphysics.linearmath.MatrixUtil;

public class TerrainStructureIceShard extends TerrainStructure {

	static int ICE = registerBlock(ElementKeyMap.TERRAIN_ICE_ID);
	static int CRYSTAL = registerBlock(ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL);

	static FastNoise rockNoise;

	static Matrix3f[] randomRotations;

	static {
		rockNoise = new FastNoise();
		rockNoise.SetFrequency(0.03f);
		rockNoise.SetFractalType(FractalType.RigidMulti);
		rockNoise.SetFractalOctaves(2);

		randomRotations = new Matrix3f[64];

		Random r = new Random(1337);

		for (int i = 0; i < randomRotations.length; i++){
			randomRotations[i] = new Matrix3f();
			MatrixUtil.setEulerZYX(randomRotations[i],
								   (r.nextFloat() - 0.5f) * 1.2f,
								   r.nextFloat() * FastMath.TWO_PI, 0);
		}
	}

	public TerrainStructureIceShard(){

	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short _width, short _height, short randomShort) throws SegmentDataWriteException {

		float width = _width;
		float height = _height;

		float falloff = width/height;

		int widthI = FastMath.fastCeil(width) + 1;
		int heightI = FastMath.fastCeil(height);
		int sizeI = Math.max(widthI, heightI);
		SegmentData segData = seg.getSegmentData();

		Matrix3f rotation = randomRotations[randomShort&63];
		Vector3f v3 = new Vector3f();
		float mag;
		
		for (byte iX = (byte) Math.max(0, x - sizeI); iX < Math.min(SegmentData.SEG, x + sizeI + 1); iX++){
			for (byte iY = (byte) Math.max(0, y - sizeI); iY < Math.min(SegmentData.SEG, y + sizeI + 1); iY++){
				for (byte iZ = (byte) Math.max(0, z - sizeI); iZ < Math.min(SegmentData.SEG, z + sizeI + 1); iZ++){

					v3.set(iX - x, iY - y, iZ - z);

					rotation.transform(v3);

					mag = v3.x*v3.x + v3.z*v3.z;
					mag = FastMath.carmackInvSqrt(mag) * mag;

					mag += rockNoise.GetSimplexFractal(iX + seg.pos.x, iY + seg.pos.y, iZ + seg.pos.z) * 1.0f;

					if (mag * 2 < width * 0.5f - Math.abs(v3.y) * falloff)
						reqData.currentChunkCache.placeBlock(
							CRYSTAL,
							iX, iY, iZ,
							segData);

					else if (mag < width - Math.abs(v3.y) * falloff)
						reqData.currentChunkCache.placeBlock(
							ICE,
							iX, iY, iZ,
							segData);
				}
			}
		}
	}

}
