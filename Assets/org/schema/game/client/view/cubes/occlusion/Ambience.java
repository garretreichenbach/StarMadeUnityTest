package org.schema.game.client.view.cubes.occlusion;

import java.util.Arrays;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

public class Ambience {

	int[] vTmp = new int[4];
	Vector3b helperPos = new Vector3b();

	private Vector3i outSegPos = new Vector3i();

	private byte encodeAmbient(int[] input) {
		//1
		//2

		//4
		//8

		//16
		//32

		//64
		//128
		int one = (input[0]);
		int two = (4 * input[1]);
		int three = (16 * input[2]);
		int four = (64 * input[3]);
		return (byte) (Byte.MIN_VALUE + (one + two + three + four));
	}

	private boolean existsUnblended(SegmentData repository, byte x, byte y, byte z) {
		if (SegmentData.valid(x, y, z)) {
			return repository.getType(x, y, z) > Element.TYPE_NONE;
		} else {
			helperPos.set(x, y, z);
			Segment outSegment = repository.getSegmentController()
					.getNeighboringSegment(helperPos, repository.getSegment(), outSegPos);

			return outSegment != null && !outSegment.isEmpty() &&
					existsUnblended(outSegment.getSegmentData(), helperPos.x, helperPos.y, helperPos.z);
		}
	}

	public byte getAmbient(Vector3b pos, SegmentData repository, int dir) {
		byte x = pos.x;
		byte y = pos.y;
		byte z = pos.z;

		Arrays.fill(vTmp, 0);

		int x0 = 0;
		int x1 = 1;
		int x2 = 2;
		int x3 = 3;
		switch(dir) {
			case Element.LEFT -> {
				if(existsUnblended(repository, (byte) (x - 1), (byte) (y + 1), (z))) {
					//b
					vTmp[x1]++;
					vTmp[x2]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (byte) (y - 1), (z))) {
					//a
					vTmp[x0]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (y), (byte) (z + 1))) {
					//c
					vTmp[x0]++;
					vTmp[x1]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (y), (byte) (z - 1))) {
					//d
					vTmp[x2]++;
					vTmp[x3]++;
				}
			}
			case Element.RIGHT -> {
				if(existsUnblended(repository, (byte) (x + 1), (byte) (y + 1), (z))) {
					//b
					vTmp[x1]++;
					vTmp[x2]++;
				}
				if(existsUnblended(repository, (byte) (x + 1), (byte) (y - 1), (z))) {
					//a
					vTmp[x0]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (byte) (x + 1), (y), (byte) (z + 1))) {
					//d
					vTmp[x2]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (byte) (x + 1), (y), (byte) (z - 1))) {
					//c
					vTmp[x0]++;
					vTmp[x1]++;
				}
			}
			case Element.TOP -> {
				// xmin zmin = 0
				// xmax zmin = 1
				// xmax zmax = 2
				// xmin zmax = 3


				/*     _____
				 *     |    |
				 * ___0|_c__|1____
				 * |  a|    |b   |
				 * |___|____|____|
				 * 	  3| d  |2
				 *     |____|
				 */
				if(existsUnblended(repository, (byte) (x + 1), (byte) (y + 1), (z))) {
					//b
					vTmp[1]++;
					vTmp[2]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (byte) (y + 1), (z))) {
					//a
					vTmp[0]++;
					vTmp[3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y + 1), (byte) (z + 1))) {
					//c
					vTmp[0]++;
					vTmp[1]++;
				}
				if(existsUnblended(repository, (x), (byte) (y + 1), (byte) (z - 1))) {
					//d
					vTmp[2]++;
					vTmp[3]++;
				}
			}
			case Element.BOTTOM -> {
				if(existsUnblended(repository, (byte) (x + 1), (byte) (y - 1), (z))) {
					//b
					vTmp[x1]++;
					vTmp[x2]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (byte) (y - 1), (z))) {
					//a
					vTmp[x0]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y - 1), (byte) (z + 1))) {
					//d
					vTmp[x2]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y - 1), (byte) (z - 1))) {
					//c
					vTmp[x0]++;
					vTmp[x1]++;
				}
			}
			case Element.FRONT -> {
				if(existsUnblended(repository, (byte) (x + 1), (y), (byte) (z + 1))) {
					//b
					vTmp[x1]++;
					vTmp[x2]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (y), (byte) (z + 1))) {
					//a
					vTmp[x0]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y + 1), (byte) (z + 1))) {
					//d
					vTmp[x2]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y - 1), (byte) (z + 1))) {
					//c
					vTmp[x0]++;
					vTmp[x1]++;
				}
			}
			case Element.BACK -> {
				if(existsUnblended(repository, (byte) (x + 1), (y), (byte) (z - 1))) {
					//b
					vTmp[x1]++;
					vTmp[x2]++;
				}
				if(existsUnblended(repository, (byte) (x - 1), (y), (byte) (z - 1))) {
					//a
					vTmp[x0]++;
					vTmp[x3]++;
				}
				if(existsUnblended(repository, (x), (byte) (y + 1), (byte) (z - 1))) {
					//c
					vTmp[x0]++;
					vTmp[x1]++;
				}
				if(existsUnblended(repository, (x), (byte) (y - 1), (byte) (z - 1))) {
					//d
					vTmp[x2]++;
					vTmp[x3]++;
				}
			}
		}
		assert (vTmp[0] < 4);
		assert (vTmp[1] < 4);
		assert (vTmp[2] < 4);
		assert (vTmp[3] < 4);

		return encodeAmbient(vTmp);
	}
}
