package org.schema.game.client.view.cubes.noise;

import java.util.Arrays;
import java.util.Random;

public class Simplex {

	// [12][3]
	private static final float[][] grad = new float[][]{
			{1.0f, 1.0f, 0.0f},
			{-1.0f, 1.0f, 0.0f},
			{1.0f, -1.0f, 0.0f},
			{-1.0f, -1.0f, 0.0f},
			{1.0f, 0.0f, 1.0f},
			{-1.0f, 0.0f, 1.0f},
			{1.0f, 0.0f, -1.0f},
			{-1.0f, 0.0f, -1.0f},
			{0.0f, 1.0f, 1.0f},
			{0.0f, -1.0f, 1.0f},
			{0.0f, 1.0f, -1.0f},
			{0.0f, -1.0f, -1.0f}};

	// [512]
	private static final int permutations[] = new int[]{151, 160, 137, 91, 90, 15,
			131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69,
			142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0,
			26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88,
			237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165,
			71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
			60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102,
			143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208,
			89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109,
			198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147,
			118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
			189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163,
			70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98,
			108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
			251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51,
			145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
			184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236,
			205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215,
			61, 156, 180, 151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53,
			194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21,
			10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252,
			219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174,
			20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27,
			166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220,
			105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161,
			1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135,
			130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52,
			217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85,
			212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183,
			170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155,
			167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224,
			232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238,
			210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239,
			107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115,
			121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29,
			24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180};

	private static final int lookup[] = new int[permutations.length];
	private static final float ONE_THIRD = 1f / 3f;
	//	private static final IntBuffer lookup2 = MemoryUtil.memAllocInt(permutations.length);
	//	static{
	//		for(int i = 0; i < permutations.length; i++){
	//			lookup2.put(i % 12);
	//		}
	//	}
	//	static Unsafe unsafe = Unsafe.getUnsafe();
	//	static long pointer;
	//	static{
	//		pointer = unsafe.allocateMemory(permutations.length*4);
	//		for(int i = 0; i < permutations.length; i++){
	//			unsafe.putInt(pointer+i*4, i%12);
	//		}
	//	}
	//
	private static final float ONE_SIXTH = 1f / 6f;
	private static final float TWO_THIRD = ONE_SIXTH * 2f;
	private static final float G3x3 = ONE_SIXTH * 3f;
	private static final float MINUS_HALF = ONE_SIXTH * 3f - 1.0f;
	private static final int[][] c = new int[][]{
			{1, 0, 0, 1, 1, 0},
			{1, 0, 0, 1, 0, 1},
			{0, 0, 1, 1, 0, 1},
			{0, 0, 1, 0, 1, 1},
			{0, 1, 0, 0, 1, 1},
			{0, 1, 0, 1, 1, 0},
	};
	private static final int[] c2 = new int[]{
			1, 0, 0, 1, 1, 0,
			1, 0, 0, 1, 0, 1,
			0, 0, 1, 1, 0, 1,
			0, 0, 1, 0, 1, 1,
			0, 1, 0, 0, 1, 1,
			0, 1, 0, 1, 1, 0,
	};

	static {
		for (int i = 0; i < lookup.length; i++) {
			lookup[i] = i % 12;
		}
	}

	static float fade(float t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	static float grad(int hash, float x, float y, float z) {
		int h = hash & 15; // CONVERT LO 4 BITS OF HASH CODE
		float u = h < 8 ? x : y, // INTO 12 GRADIENT DIRECTIONS.
				v = h < 4 ? y : h == 12 || h == 14 ? x : z;
		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}

	static float lerp(float t, float a, float b) {
		return a + t * (b - a);
	}

	public static void main(String[] args) {
		Perlin.noise(0, 0, 0); //static init
		System.err.println("PERM: " + permutations.length);

		long currentTimeMillis = System.currentTimeMillis();
		int size = 500;
		for (int z = 0; z < size; z++) {
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					noise(x, y, z, permutations);

				}
			}
		}
		System.err.println("1TIME: " + ((System.currentTimeMillis() - currentTimeMillis) / 1000f));

		currentTimeMillis = System.currentTimeMillis();
		for (int z = 0; z < size; z++) {
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					Perlin.noise(x, y, z);
				}
			}
		}
		System.err.println("2TIME: " + ((System.currentTimeMillis() - currentTimeMillis) / 1000f));

		//		currentTimeMillis = System.currentTimeMillis();
		//		for(int z = 0; z < size; z++){
		//			for(int y = 0; y < size; y++){
		//				for(int x = 0; x < size; x++){
		//					noise(x, y, z, permutations);
		//				}
		//			}
		//		}
		//		System.err.println("3TIME: "+((float)(System.currentTimeMillis()-currentTimeMillis)/1000f));

	}

	public static float noise(float xin, float yin, float zin, int[] permutations) {
		float t, X0, Y0, Z0, x0, y0, z0, s, x1, y1, z1, x2, y2, z2, x3, y3, z3, t0, t1, t2, t3, n0, n1, n2, n3;
		int i, j, k, ii, jj, kk, gi0, gi1, gi2, gi3;
		int a;
		//i1, j1, k1, i2, j2, k2,
		s = (xin + yin + zin) * ONE_THIRD;
		i = (int) (xin + s);
		j = (int) (yin + s);
		k = (int) (zin + s);
		t = (i + j + k) * ONE_SIXTH;
		X0 = i - t;
		Y0 = j - t;
		Z0 = k - t;
		x0 = xin - X0;
		y0 = yin - Y0;
		z0 = zin - Z0;

		if (x0 >= y0) {
			if (y0 >= z0) {
				a = 0;
			} else if (x0 >= z0) {
				a = 6;
			} else {
				a = 12;
			}
		} else {
			if (y0 < z0) {
				a = 18;
			} else if (x0 < z0) {
				a = 24;
			} else {
				a = 30;
			}
		}

		//		x1 = x0 - i1 + G3;
		//		y1 = y0 - j1 + G3;
		//		z1 = z0 - k1 + G3;
		//		x2 = x0 - i2 + 2.0f * G3;
		//		y2 = y0 - j2 + 2.0f * G3;
		//		z2 = z0 - k2 + 2.0f * G3;
		//		x3 = x0 - 1.0f + 3.0f * G3;
		//		y3 = y0 - 1.0f + 3.0f * G3;
		//		z3 = z0 - 1.0f + 3.0f * G3;

		x1 = x0 - c2[a] + ONE_SIXTH;
		y1 = y0 - c2[a + 1] + ONE_SIXTH;
		z1 = z0 - c2[a + 2] + ONE_SIXTH;
		x2 = x0 - c2[a + 3] + TWO_THIRD;
		y2 = y0 - c2[a + 4] + TWO_THIRD;
		z2 = z0 - c2[a + 5] + TWO_THIRD;

		x3 = x0 + MINUS_HALF;
		y3 = y0 + MINUS_HALF;
		z3 = z0 + MINUS_HALF;

		ii = i & 255;
		jj = j & 255;
		kk = k & 255;

		//		gi1 = permutations[ii + i1 + permutations[jj + j1 + permutations[kk + k1]]] % 12;
		//		gi2 = permutations[ii + i2 + permutations[jj + j2 + permutations[kk + k2]]] % 12;

		t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
		if (t0 < 0f) {
			n0 = 0.0f;
		} else {
			gi0 = lookup[permutations[ii + permutations[jj + permutations[kk]]]];
			t0 *= t0;
			//			n0 = t0 * t0 * dot(x0, y0, z0, grad[gi0]);
			n0 = t0 * t0 * (x0 * grad[gi0][0] + y0 * grad[gi0][1] + z0 * grad[gi0][2]);
		}

		t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
		if (t1 < 0f) {
			n1 = 0.0f;
		} else {
			gi1 = lookup[permutations[ii + c2[a] + permutations[jj + c2[a + 1] + permutations[kk + c2[a + 2]]]]];
			t1 *= t1;
			//			n1 = t1 * t1 * dot(x1, y1, z1, grad[gi1]);
			n1 = t1 * t1 * (x1 * grad[gi1][0] + y1 * grad[gi1][1] + z1 * grad[gi1][2]);
		}

		t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
		if (t2 < 0f) {
			n2 = 0.0f;
		} else {
			gi2 = lookup[permutations[ii + c2[a + 3] + permutations[jj + c2[a + 4] + permutations[kk + c2[a + 5]]]]];
			t2 *= t2;
			//			n2 = t2 * t2 * dot(x2, y2, z2, grad[gi2]);
			n2 = t2 * t2 * (x2 * grad[gi2][0] + y2 * grad[gi2][1] + z2 * grad[gi2][2]);
		}

		t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
		if (t3 < 0f) {
			n3 = 0.0f;
		} else {
			gi3 = lookup[permutations[ii + 1 + permutations[jj + 1 + permutations[kk + 1]]]];
			t3 *= t3;
			//			n3 = t3 * t3 * dot(x3, y3, z3, grad[gi3]);
			n3 = t3 * t3 * (x3 * grad[gi3][0] + y3 * grad[gi3][1] + z3 * grad[gi3][2]);
		}

		return 16.0f * (n0 + n1 + n2 + n3) + 1.0f;
	}

	public static float noiseNew(float x, float y, float z, int[] permutations) {
		int ix, iy, iz, gx, gy, gz;
		int a0, b0, aa, ab, ba, bb;

		int aa0, ab0, ba0, bb0;
		int aa1, ab1, ba1, bb1;
		float a1, a2, a3, a4, a5, a6, a7, a8;
		float u, v, w, a8_5, a4_1;

		ix = (int) x;
		x -= ix;
		iy = (int) y;
		y -= iy;
		iz = (int) z;
		z -= iz;

		gx = ix & 0xFF;
		gy = iy & 0xFF;
		gz = iz & 0xFF;

		a0 = gy + permutations[gx];
		b0 = gy + permutations[gx + 1];
		aa = gz + permutations[a0];
		ab = gz + permutations[a0 + 1];
		ba = gz + permutations[b0];
		bb = gz + permutations[b0 + 1];

		aa0 = permutations[aa];
		aa1 = permutations[aa + 1];
		ab0 = permutations[ab];
		ab1 = permutations[ab + 1];
		ba0 = permutations[ba];
		ba1 = permutations[ba + 1];
		bb0 = permutations[bb];
		bb1 = permutations[bb + 1];

		a1 = grad(bb1, x - 1, y - 1, z - 1);
		a2 = grad(ab1, x, y - 1, z - 1);
		a3 = grad(ba1, x - 1, y, z - 1);
		a4 = grad(aa1, x, y, z - 1);
		a5 = grad(bb0, x - 1, y - 1, z);
		a6 = grad(ab0, x, y - 1, z);
		a7 = grad(ba0, x - 1, y, z);
		a8 = grad(aa0, x, y, z);

		u = fade(x);
		v = fade(y);
		w = fade(z);

		a8_5 = lerp(v, lerp(u, a8, a7), lerp(u, a6, a5));
		a4_1 = lerp(v, lerp(u, a4, a3), lerp(u, a2, a1));
		return lerp(w, a8_5, a4_1);
	}

	public static float noiseOld(float xin, float yin, float zin, int[] permutations) {
		float t, X0, Y0, Z0, x0, y0, z0, s, x1, y1, z1, x2, y2, z2, x3, y3, z3, t0, t1, t2, t3, n0, n1, n2, n3;
		int i, j, k, ii, jj, kk, gi0, gi1, gi2, gi3;
		final int[] a;
		//i1, j1, k1, i2, j2, k2,
		s = (xin + yin + zin) * ONE_THIRD;
		i = (int) (xin + s);
		j = (int) (yin + s);
		k = (int) (zin + s);
		t = (i + j + k) * ONE_SIXTH;
		X0 = i - t;
		Y0 = j - t;
		Z0 = k - t;
		x0 = xin - X0;
		y0 = yin - Y0;
		z0 = zin - Z0;

		if (x0 >= y0) {
			if (y0 >= z0) {
				a = c[0];
			} else if (x0 >= z0) {
				a = c[1];
			} else {
				a = c[2];
			}
		} else {
			if (y0 < z0) {
				a = c[3];
			} else if (x0 < z0) {
				a = c[4];
			} else {
				a = c[5];
			}
		}

		//		x1 = x0 - i1 + G3;
		//		y1 = y0 - j1 + G3;
		//		z1 = z0 - k1 + G3;
		//		x2 = x0 - i2 + 2.0f * G3;
		//		y2 = y0 - j2 + 2.0f * G3;
		//		z2 = z0 - k2 + 2.0f * G3;
		//		x3 = x0 - 1.0f + 3.0f * G3;
		//		y3 = y0 - 1.0f + 3.0f * G3;
		//		z3 = z0 - 1.0f + 3.0f * G3;

		x1 = x0 - a[0] + ONE_SIXTH;
		y1 = y0 - a[1] + ONE_SIXTH;
		z1 = z0 - a[2] + ONE_SIXTH;
		x2 = x0 - a[3] + TWO_THIRD;
		y2 = y0 - a[4] + TWO_THIRD;
		z2 = z0 - a[5] + TWO_THIRD;

		x3 = x0 + G3x3;
		y3 = y0 + G3x3;
		z3 = z0 + G3x3;

		ii = i & 255;
		jj = j & 255;
		kk = k & 255;

		gi0 = permutations[ii + permutations[jj + permutations[kk]]] % 12;
		//		gi1 = permutations[ii + i1 + permutations[jj + j1 + permutations[kk + k1]]] % 12;
		//		gi2 = permutations[ii + i2 + permutations[jj + j2 + permutations[kk + k2]]] % 12;
		gi1 = permutations[ii + a[0] + permutations[jj + a[1] + permutations[kk + a[2]]]] % 12;
		gi2 = permutations[ii + a[3] + permutations[jj + a[4] + permutations[kk + a[5]]]] % 12;
		gi3 = permutations[ii + 1 + permutations[jj + 1 + permutations[kk + 1]]] % 12;

		t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
		if (t0 < 0f) {
			n0 = 0.0f;
		} else {
			t0 *= t0;
			//			n0 = t0 * t0 * dot(x0, y0, z0, grad[gi0]);
			n0 = t0 * t0 * (x0 * grad[gi0][0] + y0 * grad[gi0][1] + z0 * grad[gi0][2]);
		}

		t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
		if (t1 < 0f) {
			n1 = 0.0f;
		} else {
			t1 *= t1;
			//			n1 = t1 * t1 * dot(x1, y1, z1, grad[gi1]);
			n1 = t1 * t1 * (x1 * grad[gi1][0] + y1 * grad[gi1][1] + z1 * grad[gi1][2]);
		}

		t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
		if (t2 < 0f) {
			n2 = 0.0f;
		} else {
			t2 *= t2;
			//			n2 = t2 * t2 * dot(x2, y2, z2, grad[gi2]);
			n2 = t2 * t2 * (x2 * grad[gi2][0] + y2 * grad[gi2][1] + z2 * grad[gi2][2]);
		}

		t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
		if (t3 < 0f) {
			n3 = 0.0f;
		} else {
			t3 *= t3;
			//			n3 = t3 * t3 * dot(x3, y3, z3, grad[gi3]);
			n3 = t3 * t3 * (x3 * grad[gi3][0] + y3 * grad[gi3][1] + z3 * grad[gi3][2]);
		}

		return 16.0f * (n0 + n1 + n2 + n3) + 1.0f;
	}

	public static int[] randomize(Random random) {
		int[] copy = Arrays.copyOf(permutations, permutations.length);
		shuffleArray(copy, random);

		return copy;

	}

	public static void shuffleArray(int[] array, Random random) {
		int n = array.length;
		for (int i = 0; i < n; i++) {
			int change = i + random.nextInt(n - i);
			swap(array, i, change);
		}
	}

	public static float simplex_noise(int octaves, float x, float y, float z, int[] permutations) {
		float value = 0.0f;
		int i;
		for (i = 1; i <= octaves; i++) {
			int pow = (i * i);
			value += noise(x * pow, y * pow, z * pow, permutations);
		}
		return value;
	}

	//	   static float fade(float t)
	//	   {
	//	      return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
	//	   }
	//
	//	   static float lerp(float t, float a, float b)
	//	   {
	//	      return a + t * (b - a);
	//	   }

	//	   static float grad(int hash, float x, float y, float z)
	//	   {
	//	 //float u = (h < 8) ? x : y;
	//	 //float v = (h < 4) ? y : ((h == 12 || h == 14) ? x : z);
	//	 //return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	//
	//	 switch(hash & 0xF)
	//	 {
	//	  case 0x0: return  x + y;
	//	  case 0x1: return -x + y;
	//	  case 0x2: return  x - y;
	//	  case 0x3: return -x - y;
	//	  case 0x4: return  x + x;
	//	  case 0x5: return -x + x;
	//	  case 0x6: return  x - x;
	//	  case 0x7: return -x - x;
	//	  case 0x8: return  y + x;
	//	  case 0x9: return -y + x;
	//	  case 0xA: return  y - x;
	//	  case 0xB: return -y - x;
	//	  case 0xC: return  y + z;
	//	  case 0xD: return -y + x;
	//	  case 0xE: return  y - x;
	//	  case 0xF: return -y - z;
	//	  default: return 0; // never happens
	//	 }
	//	   }

	private static void swap(int[] array, int a, int b) {
		int helper = array[a];
		array[a] = array[b];
		array[b] = helper;
	}

}
