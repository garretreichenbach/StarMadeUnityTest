package org.schema.game.common.data.explosion;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.FileExt;

public class ExplosionDataHandler {
	public static final int spade = 5;
	public int bigLength;
	public int bigLengthHalf;
	public int bigLengthQuad;
	private short[] explosionAggegation;
	private int[] explosionOrder;
//	private ExplosionRayInfo[] rayInfo;
	private int sizeAggr;
	private int sizeAggr2;
	private int sizeRayInfo;
	private int sizeRayInfo2;
//	private ExplosionRayInfoArray explosionRayData;
//	private ExplosionRayInfo[] rayInfo;
private boolean loaded;
private ExplosionRayInfoArray explosionRayData;

	public static void main(String[] ar) {
		ExplosionDataHandler s = new ExplosionDataHandler();

		s.loadData();

		ExplosionCollisionSegmentCallback explosionCollisionSegmentCallback = new ExplosionCollisionSegmentCallback(s);
		long l = System.currentTimeMillis();
		s.applyDamage(explosionCollisionSegmentCallback, 16, 1000);

		System.err.println("TOOK:: " + (System.currentTimeMillis() - l));

		s.printGrid(explosionCollisionSegmentCallback);
	}
	public int getIndex(int x, int y, int z){
		return z * sizeAggr2 + y * sizeAggr + x;
	}
	int getIndexRay(int x, int y, int z) {
		return z * sizeRayInfo2 + y * sizeRayInfo + x;
	}
	public void loadData() {
		if(loaded){
			return;
		}
		DataInputStream sIn;
		try {
			final ZipInputStream zis;
			sIn = new DataInputStream(zis = new ZipInputStream( new BufferedInputStream(new FileInputStream(new FileExt("./data/sphereExplosionData.zip")), (4096*2)*2)));
			ZipEntry ze = zis.getNextEntry();
			
			sizeAggr = sIn.readInt();
			sizeAggr2 = sizeAggr * sizeAggr;
			bigLength = sizeAggr;
			bigLengthQuad = bigLength * bigLength;
			bigLengthHalf = bigLength / 2;
			
			if(sizeAggr != 128){
				sIn.close();
				throw new RuntimeException(Lng.str("The file (./data/sphereExplosionData.zip) seems to be corrupted.\nMaybe something downloaded wrongly.\nBest to force a reinstall from the launcher (make sure to save your 'server-database' and 'blueprints' folder if you fully uninstall to keep your worlds)"));
			}
			System.err.println("EXPLOSION DATA: aggregation data: dim size: "+sizeAggr+"; total Memory used: "+StringTools.readableFileSize(sizeAggr*sizeAggr*sizeAggr*2)+"; max memory: "+StringTools.readableFileSize(Runtime.getRuntime().maxMemory()));
			
			
			
			explosionAggegation = new short[sizeAggr*sizeAggr*sizeAggr];
			for (int z = 0; z < sizeAggr; z++) {
				for (int y = 0; y < sizeAggr; y++) {
					for (int x = 0; x < sizeAggr; x++) {
						explosionAggegation[getIndex(x,y,z)] = sIn.readShort();
					}
				}
			}

			sizeRayInfo = sIn.readInt();
			sizeRayInfo2 = sizeRayInfo * sizeRayInfo;
//			System.err.println("[EXPLOSION] DATA: ray info data: dim size: "+sizeRayInfo+"; total instances used: "+sizeRayInfo*sizeRayInfo*sizeRayInfo);
			
			
			final int totalElem = 13484; //FIXME, should be in the file and not hardcoded
			explosionRayData = ExplosionRayInfoArray.read(sizeRayInfo, totalElem, this, sIn);
			
//			rayInfo = new ExplosionRayInfo[sizeRayInfo*sizeRayInfo*sizeRayInfo];
//			for (int z = 0; z < sizeRayInfo; z++) {
//				for (int y = 0; y < sizeRayInfo; y++) {
//					for (int x = 0; x < sizeRayInfo; x++) {
//						rayInfo[getIndexRay(x,y,z)] = ExplosionRayInfo.read(sIn);
//					}
//				}
//			}

			
			
			final int sizeOrder = sIn.readInt();
//			explosionOrder = new int[sizeOrder];

//			System.err.println("[EXPLOSION] DATA: sizeOrder size: "+sizeOrder+" -> "+StringTools.readableFileSize(sizeOrder*4));
			
			long t = System.nanoTime();
			byte[] b = new byte[sizeOrder*4];
//			for (int i = 0; i < sizeOrder; i++) {
//				explosionOrder[i] = sIn.readInt();
				sIn.readFully(b);
//			}
				IntBuffer intBuf =
				ByteBuffer.wrap(b)
			    	.order(ByteOrder.BIG_ENDIAN)
			    	.asIntBuffer();
				explosionOrder = new int[intBuf.remaining()];
				intBuf.get(explosionOrder);
			long readTook = (System.nanoTime() - t) / 1000000;
//			System.err.println("[EXPLOSION] 'Order' File read took "+readTook+" ms");
			
			sIn.close();
			loaded = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	/**
	 * Basically, we go from inside to radius block by block.
	 * for each block we note the damage left for all rays going through that block,
	 * so the next block in the ray can use that to propagate
	 *
	 * @param closedList
	 * @param cb
	 * @param radius
	 * @param damage
	 */
	public void applyDamage(ExplosionDamageInterface cb, int radius, float damage) {

		cb.resetDamage();

		
		damage = cb.modifyDamageBasedOnBlockArmor(0, 0, 0, damage);
		
		float damageBlock = cb.damageBlock(0, 0, 0, damage);
		cb.setDamage(0, 0, 0, damageBlock);
		if (damageBlock <= 0) {
			return;
		}

		int step = 1;

		final int detail = sizeRayInfo;
		int dist;
		int index;
		//FIXME I don't honestly understand this algorithm, but it was giving arrayindexoutofbounds
		// so I added the index size check as a temp fix. There is a more substantial bug here. ~Ithirahad

		while ((index = step * spade) < explosionOrder.length && (dist = explosionOrder[index + 3]) < radius) {
			int x = explosionOrder[index];
			int y = explosionOrder[index + 1];
			int z = explosionOrder[index + 2];

			int parent = explosionOrder[index + 4];

			if (x + sizeRayInfo / 2 >= 0 && y + sizeRayInfo / 2 >= 0 && z + sizeRayInfo / 2 >= 0 &&
					x + sizeRayInfo / 2 < detail && y + sizeRayInfo / 2 < detail && z + sizeRayInfo / 2 < detail) {
				handleDetail(x, y, z, step, parent, cb, damage);
			} else {
				handleMacro(x, y, z, step, parent, cb, damage);
			}
			step++;
		}

	}

	public void printGrid(ExplosionDamageInterface cb) {
		for (int i = -sizeAggr / 2 + 1; i < sizeAggr / 2 - 1; i++) {
			for (int j = -sizeAggr / 2 + 1; j < sizeAggr / 2 - 1; j++) {
				int dmg = (int) cb.getDamage(0, i, j);
				System.out.printf("%5d ", dmg);
			}
			System.out.println();
		}
	}

	private void handleMacro(int x, int y, int z, int step, int parent,
	                         ExplosionDamageInterface cb, float totalDamage) {

		int xParent = explosionOrder[parent * spade];
		int yParent = explosionOrder[parent * spade + 1];
		int zParent = explosionOrder[parent * spade + 2];

		float parentDamage = cb.getDamage(xParent, yParent, zParent);
		if (parentDamage <= 0) {
			return;
		}

//		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
//			Vector3f start = new Vector3f(-0.5f, -0.5f, -0.5f);
//			Vector3f end = new Vector3f(0.5f, 0.5f, 0.5f);
//			Transform t = new Transform();
//			t.setIdentity();
//			t.origin.set(cb.getExplosionCenter());
//			t.origin.x += x;
//			t.origin.y += y;
//			t.origin.z += z;
//			DebugBox b = new DebugBox(start, end, t, 1, 0.5f, 0.5f, 0.1f + (0.9f * (parentDamage / totalDamage)));
//			b.LIFETIME = 20000f;
//			DebugDrawer.boxes.add(b);
//		}

		short rayId = explosionAggegation[getIndex(x + sizeAggr / 2, y + sizeAggr / 2, z + sizeAggr / 2)];

		if (cb.getClosedList().contains(rayId)) {
			return;
		} else {
			float damageBlock = cb.damageBlock(x, y, z, parentDamage);

			cb.setDamage(x, y, z, damageBlock);
			if (damageBlock <= 0) {
				cb.getClosedList().add(rayId);
			}
		}

	}

	private void handleDetail(int x, int y, int z, int step, int parent,
	                          ExplosionDamageInterface cb, float totalDamage) {

//		ExplosionRayInfo explosionRayInfo = rayInfo[getIndexRay(x + sizeRayInfo / 2, y + sizeRayInfo / 2, z + sizeRayInfo / 2)];

		int indexRay = getIndexRay(x + sizeRayInfo / 2, y + sizeRayInfo / 2, z + sizeRayInfo / 2);
		
		
		
		if (explosionRayData.hasAny(indexRay)/* explosionRayInfo != null*/) {

			int xParent = explosionOrder[parent * spade];
			int yParent = explosionOrder[parent * spade + 1];
			int zParent = explosionOrder[parent * spade + 2];

			float parentDamage = cb.getDamage(xParent, yParent, zParent);
			if (parentDamage <= 0) {
				return;
			}
//			cb.getCpy().addAll(explosionRayInfo);
			explosionRayData.addAllTo(indexRay, cb.getCpy());
			
			cb.getCpy().removeAll(cb.getClosedList());
			int nowSize = cb.getCpy().size();
			cb.getCpy().clear();

//			if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
//				Vector3f start = new Vector3f(-0.5f, -0.5f, -0.5f);
//				Vector3f end = new Vector3f(0.5f, 0.5f, 0.5f);
//				Transform t = new Transform();
//				t.setIdentity();
//				t.origin.set(cb.getExplosionCenter());
//				t.origin.x += x;
//				t.origin.y += y;
//				t.origin.z += z;
////				System.err.println("CENTER::: "+t.origin);
//				DebugBox b = new DebugBox(start, end, t, 1, 0.5f, 0.5f, 0.3f + (0.7f * (parentDamage / totalDamage)));
//				b.LIFETIME = 20000f;
//				DebugDrawer.boxes.add(b);
//			}

			if (nowSize > 0) {
//				int parentSize = rayInfo[getIndexRay(xParent + sizeRayInfo / 2, yParent + sizeRayInfo / 2, zParent + sizeRayInfo / 2)].size();
				int pIndex = getIndexRay(xParent + sizeRayInfo / 2, yParent + sizeRayInfo / 2, zParent + sizeRayInfo / 2);
				int parentSize = explosionRayData.getSize(pIndex);

				float takeAway = (float) nowSize / (float) parentSize;

//				System.err.println("PARENT::: "+x+", "+y+", "+z+", "+parentDamage+"; "+takeAway+"; "+explosionRayInfo.size()+" / "+parentSize);

				float damageBlock = cb.damageBlock(x, y, z, takeAway * parentDamage);

				cb.setDamage(x, y, z, damageBlock);

				assert (cb.getDamage(x, y, z) == damageBlock);

				if (damageBlock <= 0) {
					//this ray has used up its damage
//					cb.getClosedList().addAll(explosionRayInfo);
					explosionRayData.addAllTo(indexRay, cb.getClosedList());
				}
			}
		} else {
			handleMacro(x, y, z, step, parent, cb, totalDamage);
		}
	}
}
