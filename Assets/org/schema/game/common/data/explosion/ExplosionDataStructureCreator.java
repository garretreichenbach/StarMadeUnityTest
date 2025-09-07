package org.schema.game.common.data.explosion;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import javax.vecmath.Vector3f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.physics.RayCubeGridSolver;
import org.schema.game.common.data.physics.RayTraceGridTraverser;
import org.schema.game.common.data.physics.SegmentTraversalInterface;
import org.schema.schine.resource.FileExt;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ExplosionDataStructureCreator {

	static int maxExplosionDiameter = 128;
	static int maxExplosionRadius = maxExplosionDiameter / 2;
	static int maxRadius = 8;
	static int maxRadiusHalf = maxRadius / 2;
	RayCubeGridSolver raySolver = new RayCubeGridSolver();
	short[][][] explosionAggegation = new short[maxExplosionDiameter][maxExplosionDiameter][maxExplosionDiameter];
	/**
	 * x,y,z, dist, index of parent in same array
	 */
	int[] explosionOrder = new int[maxExplosionDiameter * maxExplosionDiameter * maxExplosionDiameter * 5];
	int currentRay;
	private ExplosionRayInfo[][][] rayInfo;

	public static void main(String[] args) {
		ExplosionDataStructureCreator ds = new ExplosionDataStructureCreator();

		System.err.println("CREATING RAY DATA");

		ds.createData();

		ObjectArrayList<Vector3f> loadData = ds.loadData();

		System.err.println("RAY DATA LOADED: " + loadData.size() + ": CREATING EXPLOSION RAYS");

		ds.createExplosionRays(loadData);

		System.err.println("EXPLOSION RAYS CREATED: EXPANDING EXPLOSIION");

		ds.expandExplosion();

		System.err.println("EXPLOSION EXPANDED: CREATING BLOCK ORDER");

		ds.createExplosionBlockOrder();

		System.err.println("BLOCK ORDER CREATED: WRITING DATA");

		ds.write();

		ds.printSmallGrid();

		ds.printGrid();
	}

	public void createEllipsoid(int uiStacks, int uiSlices, float fA, float fB, float fC, ObjectOpenHashSet<Vector3f> poses) {
		poses.clear();

		float tStep = (FastMath.PI) / uiSlices;
		float sStep = (FastMath.PI) / uiStacks;

		float totalSteps = ((((FastMath.HALF_PI * 2 + FastMath.HALF_PI) + .0001f) / tStep) + 1) * ((((FastMath.HALF_PI * 2 + FastMath.HALF_PI) + .0001f) / sStep) + 1);

		float step = 0;
		for (float t = -FastMath.HALF_PI; t <= (FastMath.HALF_PI) + .0001f; t += tStep) {
			for (float s = -FastMath.PI; s <= FastMath.PI + .0001f; s += sStep) {
				float x0 = fA * FastMath.cos(t) * FastMath.cos(s);
				float y0 = fB * FastMath.cos(t) * FastMath.sin(s);
				float z0 = fC * FastMath.sin(t);

				float x1 = fA * FastMath.cos(t + tStep) * FastMath.cos(s);
				float y1 = fB * FastMath.cos(t + tStep) * FastMath.sin(s);
				float z1 = fC * FastMath.sin(t + tStep);

//				poses.add(ElementCollection.getIndex(FastMath.round(x0), FastMath.round(y0), FastMath.round(z0)));
//				poses.add(ElementCollection.getIndex(FastMath.round(x1), FastMath.round(y1), FastMath.round(z1)));
				poses.add(new Vector3f((short) FastMath.round(x0), (short) FastMath.round(y0), (short) FastMath.round(z0)));
				poses.add(new Vector3f((short) FastMath.round(x1), (short) FastMath.round(y1), (short) FastMath.round(z1)));

				step++;
			}
		}
	}

	public void createData() {
		ObjectOpenHashSet<Vector3f> sphereData = new ObjectOpenHashSet<Vector3f>(1024 * 1024);
		createEllipsoid(maxRadius * 20, maxRadius * 20, maxRadius, maxRadius, maxRadius, sphereData);

		System.err.println("Created: " + sphereData.size()); //Created: 1599902

		try {
			DataOutputStream sphereOPF = new DataOutputStream(new FileOutputStream(new FileExt("./data/sphereExplosionHelper.dat")));
			sphereOPF.writeInt(sphereData.size());
			for (Vector3f f : sphereData) {
//				System.err.println("MM: "+f);

				sphereOPF.writeShort((short) f.x);
				sphereOPF.writeShort((short) f.y);
				sphereOPF.writeShort((short) f.z);
			}
			sphereOPF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ObjectArrayList<Vector3f> loadData() {

		DataInputStream sIn;
		try {
			sIn = new DataInputStream(new BufferedInputStream(new FileInputStream(new FileExt("./data/sphereExplosionHelper.dat"))));

			final int size = sIn.readInt();
			ObjectArrayList<Vector3f> sphereData = new ObjectArrayList<Vector3f>(size);
			for (int i = 0; i < size; i++) {
				sphereData.add(new Vector3f(sIn.readShort(), sIn.readShort(), sIn.readShort()));
			}

			sIn.close();

			return sphereData;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private void createExplosionRays(ObjectArrayList<Vector3f> sphere) {

		Transform currentTrans = new Transform();
		currentTrans.setIdentity();
		Vector3f start = new Vector3f(0, 0, 0);

		rayInfo = new ExplosionRayInfo[maxRadius * 2][maxRadius * 2][maxRadius * 2];

		final int size = sphere.size();
		SmallTrav tr = new SmallTrav();
		for (int i = 0; i < size; i++) {
			currentRay = i;
			Vector3f spherePoint = sphere.get(i);

			raySolver.initializeBlockGranularity(spherePoint, start, currentTrans);
			tr.first = true;
			tr.sp.set(spherePoint);
			raySolver.traverseSegmentsOnRay(tr);

			if (i % 100 == 0) {
				System.err.println("DONE: " + i + "/" + size);
			}
		}

	}

	private void expandExplosion() {

		Transform currentTrans = new Transform();
		currentTrans.setIdentity();
		Vector3f start = new Vector3f(0, 0, 0);

		BigTrav bt = new BigTrav();
		Vector3f from = new Vector3f();
		for (int z = 0; z < maxExplosionDiameter; z++) {
			for (int y = 0; y < maxExplosionDiameter; y++) {
				for (int x = 0; x < maxExplosionDiameter; x++) {
					bt.currentBigSphereArrayCoords.set(x, y, z);

					//grid coordinate in big sphere
					int bX = x - maxExplosionRadius;
					int bY = y - maxExplosionRadius;
					int bZ = z - maxExplosionRadius;

					from.set(bX, bY, bZ);

					//array coordinate in small sphere
					int xA = bX + maxRadius;
					int yA = bY + maxRadius;
					int zA = bZ + maxRadius;

					if (xA >= 0 && xA < rayInfo.length &&
							yA >= 0 && yA < rayInfo.length &&
							zA >= 0 && zA < rayInfo.length && rayInfo[zA][yA][xA] != null) {
						//this block is still within presicion radius
					} else {
						//traverse to the first block of presicion which will have exactly
						//one ray indicator. if taht indicator gets taken out
						//all the area pointing to that sphere segment is out
						raySolver.initializeBlockGranularity(from, start, currentTrans);
						raySolver.traverseSegmentsOnRay(bt);
					}

				}
			}
		}
	}

	private void createExplosionBlockOrder() {
		ObjectArrayList<int[]> order = new ObjectArrayList<int[]>(maxExplosionDiameter * maxExplosionDiameter * maxExplosionDiameter);

		int j = 0;
		for (int z = 0; z < maxExplosionDiameter; z++) {
			for (int y = 0; y < maxExplosionDiameter; y++) {
				for (int x = 0; x < maxExplosionDiameter; x++) {
					//grid coordinate in big sphere
					int bX = x - maxExplosionRadius;
					int bY = y - maxExplosionRadius;
					int bZ = z - maxExplosionRadius;

					int[] o1 = new int[]{bX, bY, bZ, FastMath.round(FastMath.carmackLength(bX, bY, bZ))};
					order.add(o1);

					if (x != 0 || y != 0 || z != 0)

						j++;

				}
			}
		}

		System.err.println("initial block order created. sorting...");

		Collections.sort(order, (o1, o2) -> {
			float c1 = FastMath.carmackLength(o1[0], o1[1], o1[2]);
			float c2 = FastMath.carmackLength(o2[0], o2[1], o2[2]);
			return Float.compare(c1, c2);
		});

		System.err.println("sorting block order finished. calculating parents");

		Vector3f center = new Vector3f(0, 0, 0);
		Transform currentTrans = new Transform();
		currentTrans.setIdentity();
		OneTrav tr = new OneTrav();
		tr.order = order;
		final int step = 5;
		tr.step = step;
		tr.startIndex = 0;
		int currentDist = -1;
		for (int i = 0; i < order.size(); i++) {

			explosionOrder[i * step] = order.get(i)[0];
			explosionOrder[i * step + 1] = order.get(i)[1];
			explosionOrder[i * step + 2] = order.get(i)[2];
			explosionOrder[i * step + 3] = order.get(i)[3];

			tr.r = 0;
			tr.indexA = i;
			tr.startIndex = i;
			if (i > 0) {
				raySolver.initializeBlockGranularity(new Vector3f(order.get(i)[0], order.get(i)[1], order.get(i)[2]), center, currentTrans);
				raySolver.traverseSegmentsOnRay(tr);

				if (i % 10000 == 0) {
					System.err.println("...done " + i + "/" + order.size() + "   CC " + i + " / " + tr.startIndex);
				}
			}
		}
	}

	public void write() {

		try {
			DataOutputStream sphereOPF = new DataOutputStream(new FileOutputStream(new FileExt("./data/sphereExplosionData.dat")));
			sphereOPF.writeInt(explosionAggegation.length);
			for (int z = 0; z < explosionAggegation.length; z++) {
				for (int y = 0; y < explosionAggegation.length; y++) {
					for (int x = 0; x < explosionAggegation.length; x++) {
						sphereOPF.writeShort(explosionAggegation[z][y][x]);
					}
				}
			}

			sphereOPF.writeInt(rayInfo.length);
			for (int z = 0; z < rayInfo.length; z++) {
				for (int y = 0; y < rayInfo.length; y++) {
					for (int x = 0; x < rayInfo.length; x++) {
						if (rayInfo[z][y][x] != null) {
							rayInfo[z][y][x].write(sphereOPF);
						} else {
							ExplosionRayInfo.writeNull(sphereOPF);
						}
					}
				}
			}
			sphereOPF.writeInt(explosionOrder.length);
			for (int i = 0; i < explosionOrder.length; i++) {
				sphereOPF.writeInt(explosionOrder[i]);
			}

			sphereOPF.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void printGrid() {
		for (int i = 0; i < maxExplosionDiameter; i++) {
			for (int j = 0; j < maxExplosionDiameter; j++) {
				System.out.printf("%5d ", explosionAggegation[maxExplosionRadius][i][j]);
			}
			System.out.println();
		}
	}

	public void printSmallGrid() {
		for (int i = 0; i < maxRadius * 2; i++) {
			for (int j = 0; j < maxRadius * 2; j++) {
				System.out.printf("%5d ", rayInfo[maxRadius][i][j] != null ? (rayInfo[maxRadius][i][j].size() == 1 ? rayInfo[maxRadius][i][j].iterator().nextShort() : -rayInfo[maxRadius][i][j].size()) : 00);
			}
			System.out.println();
		}
	}

	class OneTrav implements SegmentTraversalInterface<OneTrav> {

		public int startIndex;
		public int step;
		public int indexA;
		int r = 0;
		ObjectArrayList<int[]> order;

		@Override
		public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {
//			System.err.println("CHECK CCC  "+x+", "+y+", "+z+"; "+r);

			if (r > 0) {

				for (int m = startIndex; m >= 0; m--) {
					int[] js = order.get(m);

//					System.err.println("CHECK "+Arrays.toString(js)+" "+x+", "+y+", "+z);
					if (js[0] == x && js[1] == y && js[2] == z) {
						explosionOrder[indexA * step + 4] = m;

						return false;
					}
				}

				assert (false) : startIndex + "; " + x + ", " + y + ", " + z;
			}
			r++;
			return true;
		}

		@Override
		public OneTrav getContextObj() {
			return this;
		}
	}

	class SmallTrav implements SegmentTraversalInterface<SmallTrav> {

		public Vector3f sp = new Vector3f();
		public boolean first;

		@Override
		public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {

			float xx = sp.x - x;
			float yy = sp.y - y;
			float zz = sp.z - z;

			float len = FastMath.carmackSqrt(xx * xx + yy * yy + zz * zz);

			if (len < 0.1) {
				return true;
			}

			//		System.err.println("RAY::: "+x+", "+y+", "+z);
			int xA = x + maxRadius;
			int yA = y + maxRadius;
			int zA = z + maxRadius;

			assert (xA >= 0 && xA < maxRadius * 2) : xA + "; " + x + " + " + maxRadius + "; len: " + len + "; " + x + ", " + y + ", " + z;
			assert (yA >= 0 && xA < maxRadius * 2) : yA + "; " + y + " + " + maxRadius + "; len: " + len + "; " + x + ", " + y + ", " + z;
			assert (zA >= 0 && xA < maxRadius * 2) : zA + "; " + z + " + " + maxRadius + "; len: " + len + "; " + x + ", " + y + ", " + z;

			if (rayInfo[zA][yA][xA] == null) {
				rayInfo[zA][yA][xA] = new ExplosionRayInfo();
				if (first) {
					rayInfo[zA][yA][xA].unique = true;
				}
			} else if (first) {
				rayInfo[zA][yA][xA].clear();
				rayInfo[zA][yA][xA].unique = true;
			}
			if (first || !rayInfo[zA][yA][xA].unique) {
				rayInfo[zA][yA][xA].addRay(currentRay);
			}
			first = false;

			if (x == 0 && y == 0 && z == 0) {
				return false;
			}

			return true;
		}

		@Override
		public SmallTrav getContextObj() {
			return this;
		}
	}

	class BigTrav implements SegmentTraversalInterface<BigTrav> {

		public Vector3i currentBigSphereArrayCoords = new Vector3i();

		@Override
		public boolean handle(int x, int y, int z, RayTraceGridTraverser traverser) {

			float len = FastMath.carmackSqrt(x * x + y * y + z * z);

//			if(len > maxExplosionRadius-1){
//				return false;
//			}
			int xA = x + maxRadius;
			int yA = y + maxRadius;
			int zA = z + maxRadius;

			if (xA >= 0 && xA < rayInfo.length &&
					yA >= 0 && yA < rayInfo.length &&
					zA >= 0 && zA < rayInfo.length) {
//				System.err.println("CHECKING::: "+xA+", "+yA+", "+zA+"; "+rayInfo[zA][yA][xA]);
				if (rayInfo[zA][yA][xA] != null) {
					assert (rayInfo[zA][yA][xA].size() == 1) : rayInfo[zA][yA][xA] + "; " + x + ", " + y + ", " + z;

					short rayCode = rayInfo[zA][yA][xA].iterator().nextShort();

//					System.err.println("RAYCODE: "+rayCode);

					//we found the sphere segment this ray belongs to
					//and can mark it as a parent for the big array

					explosionAggegation[currentBigSphereArrayCoords.z][currentBigSphereArrayCoords.y][currentBigSphereArrayCoords.x]
							= rayCode;
					return false;
				}
			}

			return true;
		}

		@Override
		public BigTrav getContextObj() {
			return this;
		}
	}
}
