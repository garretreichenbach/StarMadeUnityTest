package org.schema.game.server.controller.world.factory.asteroid;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataAsteroid;
import org.schema.game.server.controller.TerrainChunkCacheElement;
import org.schema.game.server.controller.world.factory.WorldCreatorFactory;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD.FractalType;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD.NoiseType;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;
import org.schema.game.server.data.ServerConfig;

public abstract class WorldCreatorFloatingRockFactory extends WorldCreatorFactory {
	
	protected final long seed;
	protected TerrainDeco[] minable;
	private boolean initialized = false;
	private boolean generateVeins = false;
	
	private FastNoiseSIMD baseNoise;
	private FastNoiseSIMD veinNoise;
	
	private Vector3i minPos;
	private Vector3i maxPos;
	private int sizeX, sizeY, sizeZ;
	
	protected static short defaultResourceSize = TerrainStructure.toHalfFloat(ServerConfig.ASTEROID_RESOURCE_SIZE.getFloat());
	protected static float defaultResourceChance = ServerConfig.ASTEROID_RESOURCE_CHANCE.getFloat();
	
	public WorldCreatorFloatingRockFactory(long seed) {
		this.seed = seed;
		
	}
	
	public boolean isInboundAbs(int segmentPosX, int segmentPosY, int segmentPosZ) {
		
		int x = ByteUtil.divSeg(segmentPosX);
		int y = ByteUtil.divSeg(segmentPosY);
		int z = ByteUtil.divSeg(segmentPosZ);
		return (
			x <= this.maxPos.x &&
				y <= this.maxPos.y &&
				z <= this.maxPos.z)
			&&
			(
				x >= this.minPos.x &&
					y >= this.minPos.y &&
					z >= this.minPos.z);
	}
	
	@Override
	public void createWorld(SegmentController segmentController, Segment segment, RequestData requestData) {

		RequestDataAsteroid reqDataAst = ((RequestDataAsteroid) requestData);
		Random rand = reqDataAst.random;
		float[] noiseSetBase = reqDataAst.noiseSetBase;
		float[] noiseSetVeins = reqDataAst.noiseSetVeins;
		//prevent more threads from creating different permutations at once
		//check two times to prevent unnecessary monitor check
		if (!initialized) {
			synchronized (this) {
				if (!initialized) {
					Random rr = new Random(seed);
					setMinable(rr);
					this.minPos = new Vector3i(((FloatingRock) segmentController).getLoadedOrGeneratedMinPos());
					this.maxPos = new Vector3i(((FloatingRock) segmentController).getLoadedOrGeneratedMaxPos());
					this.sizeX = ((FloatingRock) segmentController).getLoadedOrGeneratedSizeGen().x;
					this.sizeY = ((FloatingRock) segmentController).getLoadedOrGeneratedSizeGen().y;
					this.sizeZ = ((FloatingRock) segmentController).getLoadedOrGeneratedSizeGen().z;
					
					baseNoise = new FastNoiseSIMD((int) seed);
					baseNoise.SetNoiseType(NoiseType.SimplexFractal);
					baseNoise.SetFrequency(0.008f);
					baseNoise.SetFractalOctaves(4);
					baseNoise.SetFractalGain(0.5f);
					baseNoise.SetFractalLacunarity(2f);
					baseNoise.SetFractalType(FractalType.RigidMulti);
					
					if (generateVeins = (getVeinBlock(0, rr) != -1)) {
						veinNoise = new FastNoiseSIMD((int) seed);
						veinNoise.SetNoiseType(NoiseType.Gradient);
						veinNoise.SetFrequency(0.05f);
					}
					
					initialized = true;
				}
			}
		}
		if (!isInboundAbs(segment.pos.x, segment.pos.y, segment.pos.z)) {
			return;
		}
		
		rand.setSeed(seed + segment.pos.hashCode());
		/*
		 * An offset that sets the minThis to
		 * zero position of the noise
		 */
		Vector3i oSet = new Vector3i();
		Segment.getSegmentIndexFromSegmentElement(
			segment.pos.x,
			segment.pos.y,
			segment.pos.z,
			oSet);
		oSet.x *= SegmentData.SEG;
		oSet.y *= SegmentData.SEG;
		oSet.z *= SegmentData.SEG;
		
		TerrainStructureList structureList = new TerrainStructureList();
		TerrainChunkCacheElement tcce = reqDataAst.currentChunkCache;
		SegmentData segData = segment.getSegmentData();
		
		baseNoise.FillSampledNoiseSet(noiseSetBase, oSet.x + 1337, oSet.y - 268, oSet.z + 1282, SegmentData.SEG, SegmentData.SEG, SegmentData.SEG, 1);
		boolean veinsGenerated = false;
		int index = 0;
		
		Vector3f sizeMult = new Vector3f(2f / sizeX, 2f / sizeY, 2f / sizeZ);
		float noiseScale = FastMath.carmackInvSqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) * 120.f;
		
		try {
			
			for (byte x = 0; x < SegmentData.SEG; x++) {
				for (byte y = 0; y < SegmentData.SEG; y++) {
					for (byte z = 0; z < SegmentData.SEG; z++) {
						
						float density = -FastMath.carmackLength(
							(x + oSet.x) * sizeMult.x,
							(y + oSet.y) * sizeMult.y,
							(z + oSet.z) * sizeMult.z);
						
						density += 1.1f;
						
						density -= (noiseSetBase[index] + 1.0f) * noiseScale;
						
						if (density > 0f) {
							int blockIndex;
							if (generateVeins) {
								if (!veinsGenerated) {
									veinNoise.FillSampledNoiseSet(noiseSetVeins, oSet.x + 1336, oSet.y - 268, oSet.z + 12398, SegmentData.SEG, SegmentData.SEG, SegmentData.SEG, 1);
									veinsGenerated = true;
								}
								
								if (noiseSetVeins[index] > 0.4f)
									blockIndex = getVeinBlock(density, rand);
								else
									blockIndex = getRandomSolidType(density, rand);
							} else
								blockIndex = getRandomSolidType(density, rand);
							
							tcce.placeBlock(blockIndex, x, y, z, segData);
							terrainStructurePlacement(x, y, z, density, structureList, rand);
						}
						else
							tcce.placeAir();
						
						index++;
					}
				}
			}
			structureList.buildAll(segment, reqDataAst);

		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
		
		segment.getSegmentController().getSegmentBuffer().updateBB(segment);
	}
	
	@Override
	public boolean predictEmpty() {
		return false;
	}
	
	protected abstract void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand);
	
	protected abstract int getRandomSolidType(float density, Random rand);
	
	public abstract void setMinable(Random rand);
	
	protected int getVeinBlock(float density, Random rand) {
		return -1;
	}

	public static int registerBlock(short block) {
		return GenerationElementMap.getBlockDataIndex(SegmentData.makeDataInt(block));
	}
	
}
