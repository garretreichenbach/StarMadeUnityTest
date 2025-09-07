package org.schema.game.server.controller;

import java.util.Random;

import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.controller.world.factory.planet.FastNoiseSIMD;

public class RequestDataAsteroid extends RequestDataStructureGen {
	public final float[] noiseSetBase = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG);
	public final float[] noiseSetVeins = FastNoiseSIMD.GetEmptyNoiseSet(SegmentData.SEG, SegmentData.SEG, SegmentData.SEG);
	public Random random = new Random(0);
}
