package org.schema.game.common.test;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Segment;

import junit.framework.TestCase;

public class SegmentStaticTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSegmentCalculations() {
		for (int i = 0; i < 64; i++) {
			Vector3i p = new Vector3i();
			Segment.getSegmentIndexFromSegmentElement(i, 0, 0, p);
			if (i == 15) {
				assertEquals(0, p.x);
			}
			if (i == 16) {
				assertEquals(1, p.x);
			}
			if (i == 31) {
				assertEquals(1, p.x);
			}
			if (i == 32) {
				assertEquals(2, p.x);
			}
		}

	}

	public void testSegmentCalculationsNeg() {
		for (int i = 0; i > -64; i--) {
			Vector3i p = new Vector3i();
			Segment.getSegmentIndexFromSegmentElement(i, 0, 0, p);
			if (i == 0) {
				assertEquals(0, p.x);
			}
			if (i == -1) {
				System.out.println(i + ": " + p);
				assertEquals(-1, p.x);
			}

			if (i == -15) {
				System.out.println(i + ": " + p);
				assertEquals(-1, p.x);
			}
			if (i == -16) {
				System.out.println(i + ": " + p);
				assertEquals(-1, p.x);
			}
			if (i == -17) {
				System.out.println(i + ": " + p);
				assertEquals(-2, p.x);
			}
			if (i == -32) {
				assertEquals(-2, p.x);
			}
			if (i == -33) {
				assertEquals(-3, p.x);
			}
		}
		Vector3i p = new Vector3i();
		Segment.getSegmentIndexFromSegmentElement(8, 8, -16, p);
		System.out.println(p + "equals" + new Vector3i(0, 0, -1) + " ?");
		assertEquals(p, new Vector3i(0, 0, -1));

	}

}
