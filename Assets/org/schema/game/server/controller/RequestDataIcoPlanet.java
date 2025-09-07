package org.schema.game.server.controller;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;
import org.schema.game.server.controller.world.factory.planet.terrain.BlockBiomeData;

public class RequestDataIcoPlanet extends RequestDataStructureGen {

	public final float[] noiseSet0 = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG+1);
	public final float[] noiseSet1 = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG+1);
	public final float[] noiseSet2 = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG+1);
	
	public BlockBiomeData blockBiomeData = new BlockBiomeData();
	public Random random = new Random(0);
	public Vector3f vector3f = new Vector3f();
}
