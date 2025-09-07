package org.schema.schine.graphicsengine.forms;

public class SimplePosElement {
	public static final int FRONT = 0;
	public static final int BACK = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	public static final int RIGHT = 4;
	public static final int LEFT = 5;

	public static final int FLAG_FRONT = 1;
	public static final int FLAG_BACK = 2;
	public static final int FLAG_TOP = 4;
	public static final int FLAG_BOTTOM = 8;
	public static final int FLAG_RIGHT = 16;
	public static final int FLAG_LEFT = 32;

	public static int[] SIDE_FLAG = new int[]{FLAG_FRONT, FLAG_BACK, FLAG_TOP, FLAG_BOTTOM, FLAG_RIGHT, FLAG_LEFT};

//	public static final int CUBE_SIDE_VERT_COUNT 		= 4;

//	public static final int INDEX_BOTTOM 			= 0;
//	public static final int INDEX_TOP 				= CUBE_SIDE_VERT_COUNT;
//	public static final int INDEX_FRONT 			= CUBE_SIDE_VERT_COUNT*2;
//	public static final int INDEX_BACK 				= CUBE_SIDE_VERT_COUNT*4;
//	public static final int INDEX_LEFT 				= CUBE_SIDE_VERT_COUNT*3;
//	public static final int INDEX_RIGHT 			= CUBE_SIDE_VERT_COUNT*5;
	/**
	 * value between 0 and 63 to determine which sides are visible
	 */
	public static byte FULLVIS = 63;
	public byte x = 0;
	public byte y = 0;
	public byte z = 0;
	public byte visMask = FULLVIS;

	public static int countBits(int x) {
		// collapsing partial parallel sums method
		// collapse 32x1 bit counts to 16x2 bit counts, mask 01010101
		x = (x >>> 1 & 0x55555555) + (x & 0x55555555);
		// collapse 16x2 bit counts to 8x4 bit counts, mask 00110011
		x = (x >>> 2 & 0x33333333) + (x & 0x33333333);
		// collapse 8x4 bit counts to 4x8 bit counts, mask 00001111
		x = (x >>> 4 & 0x0f0f0f0f) + (x & 0x0f0f0f0f);
		// collapse 4x8 bit counts to 2x16 bit counts
		x = (x >>> 8 & 0x00ff00ff) + (x & 0x00ff00ff);
		// collapse 2x16 bit counts to 1x32 bit count
		return (x >>> 16) + (x & 0x0000ffff);
	}

	@Override
	public String toString() {
		return "SPE(" + x + ", " + y + ", " + z + " [v" + visMask + "])";
	}
}
