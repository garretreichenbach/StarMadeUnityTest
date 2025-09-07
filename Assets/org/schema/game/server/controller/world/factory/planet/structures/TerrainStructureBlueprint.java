package org.schema.game.server.controller.world.factory.planet.structures;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.ByteBufferInputStream;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.io.IOFileManager;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.controller.io.SegmentHeader;
import org.schema.game.common.controller.io.SegmentRegionFileNew;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.*;
import org.schema.game.server.controller.GenerationElementMap;
import org.schema.game.server.controller.RequestDataStructureGen;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class TerrainStructureBlueprint extends TerrainStructure {

	public static final byte PLACEMENT_FORCED = 0;
	public static final byte PLACEMENT_EMPTY = 1;
	public static final byte PLACEMENT_NOTEMPTY = 2;

	private byte[] byteArray;
	private int[] intArray;
	public final String path;

	private static HashMap<String, TerrainStructureBlueprint> blueprintMap = new HashMap<String, TerrainStructureBlueprint>();

	public static TerrainStructureBlueprint get(String path) {
		TerrainStructureBlueprint bp = blueprintMap.get(path);

		if (bp == null)
			blueprintMap.put(path, bp = new TerrainStructureBlueprint(path));

		return bp;
	}

	private TerrainStructureBlueprint(String path) {

		this.path = path;
	}

	public void loadFromFile() {
		/*int lastSlash = path.lastIndexOf('/') + 1;
		String folder = "blueprints-terrain/" + path.substring(0, lastSlash);
		String file = path.substring(lastSlash);*/
		System.err.println("[TERRAIN_STRUCTURE] Building terrain structure blueprint: " + path);
		ByteArrayList byteList = new ByteArrayList();
		IntArrayList intList = new IntArrayList();

		File f = new FileExt("blueprints-terrain/" + path + SegmentDataFileUtils.BLOCK_FILE_EXT);

		if (!f.exists()) {
			System.err.println("[TERRAIN_STRUCTURE] NO FILE: " + f.getAbsolutePath());

		} else {
			try {
				SpaceStation s = new SpaceStation(GameServerState.instance);
				RemoteSegment seg = new RemoteSegment(s);
				SegmentData segData = new SegmentData4Byte(false);
				segData.assignData(seg);

				SegmentRegionFileNew rf = null;
				BufferedInputStream byteArrayInputStream = null;
				DataInputStream in = null;
				ByteBuffer dataByteBuffer = MemoryUtil.memAlloc(SegmentData.SEG_TIMES_SEG_TIMES_SEG * SegmentData.BYTES_USED);

				rf = (SegmentRegionFileNew) IOFileManager.getFromFile(f);

				bbMin.set(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);
				bbMax.set(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);

				final int minSeg = -SegmentData.SEG;
				final int maxSeg = SegmentData.SEG;
				for (int xSeg = minSeg; xSeg <= maxSeg; xSeg += SegmentData.SEG) {
					for (int ySeg = minSeg; ySeg <= maxSeg; ySeg += SegmentData.SEG) {
						for (int zSeg = minSeg; zSeg <= maxSeg; zSeg += SegmentData.SEG) {

							if (rf.getHeader().isEmptyOrNoData(xSeg, ySeg, zSeg))
								continue;

							int length = rf.getHeader().getSize(xSeg, ySeg, zSeg);

							short offset = SegmentHeader.convertToDataOffset(rf.getHeader().getOffset(xSeg, ySeg, zSeg));

							long dataPosition = SegmentHeader.getAbsoluteFilePos(offset);

							dataByteBuffer.rewind();
							dataByteBuffer.limit(length);

							rf.getFile().getChannel().read(dataByteBuffer, dataPosition);
							dataByteBuffer.rewind();

							byteArrayInputStream = new BufferedInputStream(new ByteBufferInputStream(
								dataByteBuffer));
							in = new DataInputStream(
								byteArrayInputStream);

							//System.err.println("::: READING: " + xSeg + ", " + ySeg + ", " + zSeg + "; off: " + offset + " -> " + dataPosition + "; size " + length + "; " + s);
							//System.err.println(s+" ::: READING: "+x+", "+y+", "+z+"; off: "+offset+" -> "+dataPosition+"; size "+length);
							//seg.pos.set(xSeg, ySeg, zSeg);
							seg.compressionCheck = false;
							seg.deserialize(in, length, true, false, 0);
						
						/*try {
							status = SegmentDataIONew.requestStatic(xSeg * SegmentData.SEG, ySeg * SegmentData.SEG, zSeg * SegmentData.SEG, file, dataByteBuffer, folder, seg);
						} catch (DeserializationException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						if (status != SegmentDataFileUtils.READ_DATA)
							continue;*/

							for (byte x = 0; x < Segment.DIM; x++) {
								for (byte y = 0; y < Segment.DIM; y++) {
									for (byte z = 0; z < Segment.DIM; z++) {

										int xLocal = (x - SegmentData.SEG_HALF) + xSeg;
										int yLocal = (y - SegmentData.SEG_HALF) + ySeg;
										int zLocal = (z - SegmentData.SEG_HALF) + zSeg;

										if (xLocal < -SegmentData.SEG || xLocal > SegmentData.SEG ||
											yLocal < -SegmentData.SEG || yLocal > SegmentData.SEG ||
											zLocal < -SegmentData.SEG || zLocal > SegmentData.SEG)
											continue;

										int index = SegmentData.getInfoIndex(x, y, z);
										short type = segData.getType(index);

										if (type == Element.TYPE_NONE ||
											type == ElementKeyMap.BUILD_BLOCK_ID ||
											type == ElementKeyMap.CORE_ID)
											continue;

										short hitpoints = segData.getHitpointsByte(index);

										if (type == ElementKeyMap.BLUEPRINT_EMPTY) {
											segData.setType(index, Element.TYPE_NONE);
											hitpoints = 0;
										}

										byte placementType = PLACEMENT_FORCED;

										if (hitpoints == PLACEMENT_EMPTY) {
											placementType = PLACEMENT_EMPTY;
											hitpoints = ElementKeyMap.MAX_HITPOINTS;
										} else if (hitpoints == PLACEMENT_NOTEMPTY) {
											placementType = PLACEMENT_NOTEMPTY;
											hitpoints = ElementKeyMap.MAX_HITPOINTS;
										}

										segData.setHitpointsByte(index, hitpoints);

										int dataInt = segData.getDataAt(index);

										int dataIndex = GenerationElementMap.getBlockDataIndex(dataInt);

										byteList.add((byte) xLocal);
										byteList.add((byte) yLocal);
										byteList.add((byte) zLocal);
										byteList.add(placementType);
										intList.add(dataIndex);

										bbMin.x = Math.min(xLocal, bbMin.x);
										bbMin.y = Math.min(yLocal, bbMin.y);
										bbMin.z = Math.min(zLocal, bbMin.z);

										bbMax.x = Math.max(xLocal, bbMax.x);
										bbMax.y = Math.max(yLocal, bbMax.y);
										bbMax.z = Math.max(zLocal, bbMax.z);
									}
								}
							}
						}
					}
				}
			} catch (SegmentDataWriteException e) {
				e.printStackTrace();
				throw new RuntimeException("this should be never be thrown, as generation should use a normal segment data", e);
			} catch (DeserializationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		byteArray = byteList.toByteArray();
		intArray = intList.toIntArray();
	}

	@Override
	public void build(Segment seg, RequestDataStructureGen reqData, int x, int y, int z, short metaData0, short metaData1, short metaData2) throws SegmentDataWriteException {

		SegmentData segData = seg.getSegmentData();
		int byteIndex = -1;
		int intIndex = -1;

		while (++byteIndex < byteArray.length) {

			intIndex++;
			byte xLocal = (byte) (x + byteArray[byteIndex++]);
			byte yLocal = (byte) (y + byteArray[byteIndex++]);
			byte zLocal = (byte) (z + byteArray[byteIndex++]);

			if (xLocal < 0 || xLocal >= SegmentData.SEG ||
				yLocal < 0 || yLocal >= SegmentData.SEG ||
				zLocal < 0 || zLocal >= SegmentData.SEG)
				continue;

			byte placementType = byteArray[byteIndex];

			short type;

			switch (placementType) {
				case PLACEMENT_EMPTY:
					if ((type = segData.getType(xLocal, yLocal, zLocal)) != 0 &&
						(ElementKeyMap.getInfoFast(type).isPhysical() || type == ElementKeyMap.WATER))
						continue;
					break;

				case PLACEMENT_NOTEMPTY:
					if ((type = segData.getType(xLocal, yLocal, zLocal)) == 0 ||
						(!ElementKeyMap.getInfoFast(type).isPhysical() && type != ElementKeyMap.WATER))
						continue;
					break;
			}
			reqData.currentChunkCache.placeBlock(intArray[intIndex], xLocal, yLocal, zLocal, segData);
		}
	}

}
