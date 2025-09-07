package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SegmentData;

import junit.framework.TestCase;

public class SegmentBufferManagerTest extends TestCase {
	final int segMax = SegmentData.SEG * SegmentBufferManager.DIMENSION;
	Vector3i exp = new Vector3i();
	Vector3i test = new Vector3i();

	private void equ(Vector3i expected, Vector3i from) {
		//		fromInt.set(from);
		//		Vector3i res = SegmentBufferManager.getBufferPosition(fromInt, resOut);
		//		assertEquals(expected, res);
		//
		//		fromInt.set(from);
		//		int index = SegmentBuffer.getIndex(fromInt.x, fromInt.y, fromInt.z, res);
		////		if(fromInt.z < 0){
		////			System.err.println(fromInt);
		////		}
		//		assertTrue(index >= 0 && index < SegmentBuffer.BUFFER_LENGTH);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNegSegmentCalculations() {

		for (int x = -segMax; x < 0; x++) {
			for (int y = 0; y < SegmentData.SEG * SegmentBufferManager.DIMENSION; y++) {
				for (int z = 0; z < SegmentData.SEG * SegmentBufferManager.DIMENSION; z++) {
					exp.set(-8, 0, 0);
					test.set(x, y, z);
					equ(exp, test);
				}
			}
		}
		for (int x = -segMax; x < 0; x++) {
			for (int y = -segMax; y < 0; y++) {
				for (int z = -segMax; z < 0; z++) {
					exp.set(-SegmentData.SEG_HALF, -SegmentData.SEG_HALF, -SegmentData.SEG_HALF);
					test.set(x, y, z);
					equ(exp, test);
				}
			}
		}
		tNeg(-33, -10, -10);
		tNeg(-10, 2, -10);
		tNeg(-10, 12, 10);
		tNeg(10, -89, 23);
		tNeg(10, 10, -12);
	}

	public void testPosSegmentCalculations() {

		for (int x = 0; x < segMax; x++) {
			for (int y = 0; y < segMax; y++) {
				for (int z = 0; z < segMax; z++) {
					exp.set(0, 0, 0);
					test.set(x, y, z);
					equ(exp, test);
				}
			}
		}
		for (int x = segMax; x < 2 * segMax; x++) {
			for (int y = 0; y < segMax; y++) {
				for (int z = 0; z < segMax; z++) {
					exp.set(8, 0, 0);
					test.set(x, y, z);
					equ(exp, test);
				}
			}
		}
		tNeg(0, 0, 0);
		tNeg(1, 0, 0);
		tNeg(0, 1, 1);

	}

	private void tNeg(int rangeX, int rangeY, int rangeZ) {
		for (int x = rangeX * segMax; x < (rangeX + 1) * segMax; x++) {
			for (int y = rangeY * segMax; y < (rangeY + 1) * segMax; y++) {
				for (int z = rangeZ * segMax; z < (rangeZ + 1) * segMax; z++) {
					exp.set(rangeX * SegmentData.SEG_HALF, rangeY * SegmentData.SEG_HALF, rangeZ * SegmentData.SEG_HALF);
					test.set(x, y, z);
					equ(exp, test);
				}
			}
		}
	}

}
