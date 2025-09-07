package org.schema.game.server.controller.world.factory;

import java.util.Arrays;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.client.data.terrain.fractal.FractalHeightMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;

public class WorldCreatorFractalHeighMapFactory extends WorldCreatorFactory {
	final float roughnessFactor = 0.4f;
	final float scale = 90.0f;
	final float tiling = 20.0f;
	final int vertexSpacing = 500;

	int width;
	int height;
	int depth;

	public WorldCreatorFractalHeighMapFactory() {
		super();

	}

	@Override
	public void createWorld(SegmentController world, Segment ws, RequestData requestData) {
		int xPos = ws.pos.x;
		int yPos = ws.pos.y;
		int zPos = ws.pos.z;

		this.width = SegmentData.SEG;
		this.height = SegmentData.SEG;
		this.depth = SegmentData.SEG;

		System.err.println("creating new fractal info: (" + ws.pos + ") - " + width + "x" + height + "x" + depth);

		FractalHeightMap test = new FractalHeightMap(width, height, depth, 1, 1);
		long time = System.currentTimeMillis();
		test.midpointDisplacement(roughnessFactor);
		long t = System.currentTimeMillis() - time;
		if (t > 5) {
			System.err.println("!!!displacement percentage " + t);
		}
		time = System.currentTimeMillis();
		test.boxFilter();
		t = System.currentTimeMillis() - time;
		if (t > 5) {
			System.err.println("!!!box filter percentage " + t);
		}

		time = System.currentTimeMillis();
		Vector3b posAbsolute = new Vector3b();
		Vector3b posLocal = new Vector3b();
		t = System.currentTimeMillis() - time;
		if (t > 5) {
			System.err.println("!!!map cration percentage " + t);
		}
		time = System.currentTimeMillis();
		//		StringBuffer sb = new StringBuffer();
		//		StringBuffer sb1 = new StringBuffer();
		for (byte x = 0; x < width; x++) {
			for (byte z = 0; z < depth; z++) {
				Float heightValue = FractalHeightMap.heightValues[z * width + x];
				byte y = heightValue.byteValue();
				byte e = ElementKeyMap.CORE_ID;
				//				Vector3i posFromStartOffset = world.getPosFromStartOffset(pos, startOffset);
				//				ws.getSegmentData().setInfoElement(posLocal, e, true);
				//				sb.append(pos+",");
				//				String index = "("+posFromStartOffset.x/Element.HALF_SIZE+","+posFromStartOffset.y/Element.HALF_SIZE+","+posFromStartOffset.z/Element.HALF_SIZE+")";
				//				sb1.append("["+pos+"-"+index+"-"+world.getInfoIndex(posFromStartOffset, Element.HALF_SIZE)+"],");

				for (byte i = (byte) ((y) - 1); i > 0; i--) {
					byte ed = ElementKeyMap.CORE_ID;
					assert (false) : "not implemented: use ElementKeyMap to instantiate";
					//					ed.type = e.getType();

					//					ws.getSegmentData().setInfoElement(posLocal, ed, true);

					Thread.yield();
				}
				for (byte i = (byte) ((y) + 1); i < height; i++) {
					posLocal.set((x), (i), (z));
					//					posFromStartOffset = world.getPosFromStartOffset(pos, startOffset);
					try {
						ws.getSegmentData().removeInfoElement(posLocal.x, posLocal.y, posLocal.z);
					} catch (SegmentDataWriteException e1) {
						e1.printStackTrace();
					}

				}
				Thread.yield();
			}
		}
		//		System.err.println("Created Fractal map pos       : "+sb.toString());
		//		System.err.println("Created Fractal map withOffset: "+sb1.toString());
		//		transformationArray = System.currentTimeMillis()-percentage;
		//		if(transformationArray > 5){
		//			System.err.println("!!!map operation percentage "+transformationArray);
		//		}
		//		System.err.println(map);
	}

	/*************************************************************
	 /**  Heightmap
	 /**  This class is where all the hight data is generated and stored.
	 /*************************************************************/

	@Override
	public boolean predictEmpty() {
				return false;
	}

	/*****************************************************************
	 * /** This function joins the edges between heightmaps stored in the current
	 * map
	 * /
	 ****************************************************************/
	void JoinEdges(FractalHeightMap[][] map) {
		int endPoint = width / 10; //* 0.1
		int startPoint = width - endPoint;
		float increment = (100.0f / endPoint) / 100.0f;

		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				int index = j + 1;

				if (index >= map[0].length) {
					index = 0;
				}

				//                       int index_two = i + 1;
				//
				//                       if(index_two >= map.length)
				//                       {
				//                             index_two = 0;
				//                       }

				float[] source = Arrays.copyOf(FractalHeightMap.heightValues, FractalHeightMap.heightValues.length);
				for (int x = 0; x < width; ++x) {
					float percentage = 1.0f;
					for (int y = startPoint; y < depth; ++y) {
						float pointA = FractalHeightMap.heightValues[y * width + x];
						float pointB = source[(width - y) * width + x];

						float distance = pointA - pointB;

						FractalHeightMap.heightValues[y * width + x] = source[(width - y) * width + x] + (percentage * distance);
						percentage -= increment;
					}
				}
			}
		}

		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				int index_two = i + 1;

				if (index_two >= map.length) {
					index_two = 0;
				}
				float[] source = Arrays.copyOf(FractalHeightMap.heightValues, FractalHeightMap.heightValues.length);

				for (int y = 0; y < depth; ++y) {
					float percentage = 1.0f;
					for (int x = startPoint; x < width; ++x) {

						float pointA = FractalHeightMap.heightValues[y * width + x];
						float pointB = source[y * width + (width - x)];

						float distance = pointA - pointB;

						FractalHeightMap.heightValues[y * width + x] = source[y * width + (width - x)] + (percentage * distance);
						percentage -= increment;
					}
				}
			}
		}
	}

}
