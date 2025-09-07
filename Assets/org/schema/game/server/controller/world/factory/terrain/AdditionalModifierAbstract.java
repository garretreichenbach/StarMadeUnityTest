package org.schema.game.server.controller.world.factory.terrain;

import java.util.Random;

import org.schema.common.FastMath;

public abstract class AdditionalModifierAbstract {
	protected static final int SEGMENT_HEIGHT = 64;
	protected static final int SEGMENT_HEIGHT_PARTS = 4;
	protected static final float SEGMENT_HEIGHT_PARTS_INV = 1f / SEGMENT_HEIGHT_PARTS;
	protected static final double MARGIN = -0.6932488288173d;
	protected int range;

	public AdditionalModifierAbstract() {
		range = 8;
	}

	protected static double getRandX(int xLocal, Random rand) {
		return xLocal * 16 + rand.nextInt(16);
	}

	protected static double getRandY(Random rand) {
		return rand.nextInt(rand.nextInt(60) + 4);
	}

	protected static double getRandZ(int zLocal, Random rand) {
		return zLocal * 16 + rand.nextInt(16);
	}

	protected static float getDist1(Random rand) {
		return FastMath.TWO_PI * rand.nextFloat();
	}

	protected static float getDist2(Random rand) {
		return ((rand.nextFloat() - 0.5f) * 2.0f) * SEGMENT_HEIGHT_PARTS_INV;
	}

	protected static float getDist0(Random rand) {

		float ret = rand.nextFloat() * 2.0f + rand.nextFloat();

		if (rand.nextInt(10) == 0) {
			ret *= rand.nextFloat() * rand.nextFloat() * 3f + 1f;
		}

		return ret;
	}

	public void generate(long seed, int width, int height, int depth,
	                     short typeArray[], Random rand) {
		rand.setSeed(seed);
		long lA = rand.nextLong();
		long lB = rand.nextLong();

		for (int xLocal = width - range; xLocal <= width + range; xLocal++) {
			for (int zLocal = depth - range; zLocal <= depth + range; zLocal++) {
				rand.setSeed(xLocal * lA ^ zLocal * lB ^ seed);
				generate(xLocal, zLocal, width, depth,
						typeArray, rand);
			}
		}
	}

	protected abstract boolean isGroundType(short type);

	protected int getAdditionalPoints(Random rand) {
		return rand.nextInt(4);
	}

	protected boolean isBigger(Random rand) {
		return rand.nextInt(4) == 0;
	}

	protected boolean isZeroHeight(Random rand) {
		return rand.nextInt(15) != 0;
	}

	protected int genHeight(Random rand) {
		return rand.nextInt(rand.nextInt(rand.nextInt(40) + 1) + 1);
	}

	protected abstract void generate(int xLocal, int zLocal, int width, int depth, short typeArray[], Random rand);
}
