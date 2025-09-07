package org.schema.game.common.test;

import java.util.Arrays;

import org.schema.game.common.data.world.DrawableRemoteSegment;

import junit.framework.TestCase;

public class ByteEncodingTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSegmentCalculations() {
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 4; z++) {
					for (int w = 0; w < 4; w++) {
						int[] b = new int[]{w, z, y, x};
						byte encodeAmbient = DrawableRemoteSegment.encodeAmbient(b);
						int[] decodeAmbient = DrawableRemoteSegment.decodeAmbient(encodeAmbient, new int[4]);

						//							System.out.println(decodeAmbient+" == "+b+" ?"+Arrays.equals(decodeAmbient, b));
						assertTrue(Arrays.equals(decodeAmbient, b));
					}
				}
			}
		}

	}

}
