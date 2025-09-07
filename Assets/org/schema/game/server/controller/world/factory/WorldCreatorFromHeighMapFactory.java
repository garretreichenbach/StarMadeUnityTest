package org.schema.game.server.controller.world.factory;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;

@SuppressWarnings("unused")
public class WorldCreatorFromHeighMapFactory extends WorldCreatorFactory {

	private int[] heightMap;
	private int width;
	private int height;
	private int depth;
	private int min;
	private int max;
	private int m_width = 1024;
	private boolean initialized;

	public WorldCreatorFromHeighMapFactory(int[] heightMap) {
		super();
		this.heightMap = heightMap;
	}
	//	/**
	//	 * @param x
	//	 * @param y
	//	 * @return
	//	 */
	//	protected int at(int x, int y) {
	//		while(x < 0) x+=m_width;
	//
	//		y = y & ((m_height << 1) - 1);
	//
	//		if(y > m_heightm1 ) {
	//			y = (m_heightm1 << 1) - y;
	//			x += m_halfwidth;
	//		}
	//
	//		if(y < 0) {
	//			y = -y;
	//			x += m_width >> 1;
	//		}
	//
	//		x = x & m_widthm1;
	//
	//
	//		return (y * m_width) + x;
	//
	//	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	protected int at(int x, int y) {
		return (y * m_width) + x;

	}

	@Override
	public void createWorld(SegmentController world, Segment ws, RequestData requestData) {

		if (!initialized) {
			initialize();
		}
		int xOffset = 0;//ws.x;
		int yOffset = 0;//ws.y;
		int zOffset = 0;//ws.z;

		this.width = SegmentData.SEG;
		this.height = SegmentData.SEG;
		this.depth = SegmentData.SEG;

		System.err.println("creating new info from heightmap: (" + ws.pos + ") - " + width + "x" + height + "x" + depth + " -- " + heightMap.length);

		long time = System.currentTimeMillis();
		long t = System.currentTimeMillis() - time;

		time = System.currentTimeMillis();
		Vector3b posAbsolute = new Vector3b();
		Vector3b posLocal = new Vector3b();
		t = System.currentTimeMillis() - time;
		if (t > 5) {
			System.err.println("!!!map cration percentage " + t);
		}
		time = System.currentTimeMillis();
		for (byte x = 0; x < width; x++) {
			for (byte z = 0; z < depth; z++) {
				//(z+zPos) * width + (x+xPos)
				int absY = heightMap[at(x + xOffset, z + zOffset)];
				byte y = (byte) normalize(absY, height);
				//				System.err.println("height value = "+y+" / "+absY+" -- "+((x+xOffset)+", "+(z+zOffset))+" ---- "+((z+zOffset) * width + (x+xOffset))+", minmax   "+minThis+" <y< "+maxThis);
				byte e = (byte) ElementKeyMap.CORE_ID;
				posLocal.set((x), (y), (z));
				//				ws.getSegmentData().setInfoElement(posLocal, e, true);

				for (byte i = (byte) ((y) - 1); i > 0; i--) {
					byte ed = (byte) ElementKeyMap.CORE_ID;
					posLocal.set((x), (i), (z));
					//					ws.getSegmentData().setInfoElement(posLocal, ed, true);

					Thread.yield();
				}
				for (byte i = (byte) ((y) + 1); i < height; i++) {
					posLocal.set((x), (i), (z));
					try {
						ws.getSegmentData().removeInfoElement(posLocal.x, posLocal.y, posLocal.z);
					} catch (SegmentDataWriteException e1) {
						e1.printStackTrace();
					}

				}
				Thread.yield();
			}
		}
	}

	@Override
	public boolean predictEmpty() {
				return false;
	}

	private void initialize() {
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		for (int i = 0; i < heightMap.length; i++) {
			min = Math.min(min, heightMap[i]);
			max = Math.max(max, heightMap[i]);
		}
		initialized = true;
	}

	public int normalize(int y, float maxHeight) {
		float v = y - min; // subtract minimum
		int nMax = max - min; // new maxThis value
		float fac = maxHeight / nMax;
		float p = v * fac;
		return (int) (Math.floor(p));

	}

}
