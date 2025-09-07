package org.schema.game.server.controller;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.world.factory.terrain.TerrainGenerator;

import java.util.Arrays;
import java.util.Random;

public class RequestDataPlanet extends RequestData {

	public final Vector3i cachePos = new Vector3i();
	
	public Seg[] segs = new Seg[ByteUtil.Chunk32 ? 9 : 1];
	
	private int[] total = new int[48 * 128 * 48];
	
	public int get48InfoIndex(int x, int y, int z){
		int i = ((z * (48*128)) + (y * 48) + x);
		return i;
	}
	
	
	public int index;
	
	public boolean done;

	public Seg getR(){
		return segs[index];
	}
	
	public RequestDataPlanet(){
		for(int i = 0; i < segs.length; i++){
			segs[i] = new Seg();
		}
	}
	@Override
	public void reset(){
		for(int i = 0; i < segs.length; i++){
			segs[i].reset();
		}
		Arrays.fill(total, (byte)0);
		done = false;
	}
	public class Seg{
		
		
		public Chunk16SegmentData[] segmentData = new Chunk16SegmentData[8];
		public Seg(){
			for(int i = 0; i < segmentData.length; i++){
				segmentData[i] = new Chunk16SegmentData();
			}
		}
		
		
		public final Vector3i cachePos = new Vector3i();
		public short[] data = new short[32768 / (8 / TerrainGenerator.plateauHeight)];
		public boolean created = false;
		public Random rand = new Random();
		public double noiseSmall8[];
		public double noise1Big16[];
		public double noise2Big16[];
		public double noise2DMid16[];
		public final Vector3i p = new Vector3i();
		public final Vector3i pFac = new Vector3i();
		public double noiseArray[];
		public float mar;
		public int miniblock;
		public double rScale = 1;
		public float normMax = 4;
		public float normMin = -0.55f;
		public float radius;
		public void reset() {
			created = false;
			for(int i = 0; i < segmentData.length; i++){
				segmentData[i].resetFast();
			}
		}
		
		public void set(int x, int y, int z){
			cachePos.set(x, y, z);
			for(int i = 0; i < segmentData.length; i++){
				segmentData[i].segmentPos.set(x, i * 16, z);
				segmentData[i].setBlockAddedForced(false);
			}
		}
	}
	
	public void initWith(Segment w) {
		if(ByteUtil.Chunk32){
			segs[0].set(w.pos.x-16, 	0, w.pos.z-16);
			segs[1].set(w.pos.x, 		0, w.pos.z-16);
			segs[2].set(w.pos.x+16, 	0, w.pos.z-16);
			
			segs[3].set(w.pos.x-16, 	0, w.pos.z);
			segs[4].set(w.pos.x,	  	0, w.pos.z);
			segs[5].set(w.pos.x+16, 	0, w.pos.z);
			
			segs[6].set(w.pos.x-16,  	0, w.pos.z+16);
			segs[7].set(w.pos.x,  		0, w.pos.z+16);
			segs[8].set(w.pos.x+16,  	0, w.pos.z+16);
		}else{
			segs[0].set(w.pos.x, w.pos.y, w.pos.z);
		}
	}
	public void applyTo(Segment w) throws SegmentDataWriteException {
		if(w.pos.y >= 128-32){
			return;
		}

		done = true;

//		if(ByteUtil.Chunk32){
			
			for(int sub = 0; sub < 8; sub++){
				fillColumn(w, 0, sub, 0, 0);
				fillColumn(w, 1, sub, 16, 0);
				fillColumn(w, 2, sub, 32, 0);
				
				fillColumn(w, 3, sub, 0, 16);
				fillColumn(w, 4, sub, 16, 16);
				fillColumn(w, 5, sub, 32, 16);
				
				fillColumn(w, 6, sub, 0, 32);
				fillColumn(w, 7, sub, 16, 32);
				fillColumn(w, 8, sub, 32, 32);
			}
			
			create32Chunk(w);
//		}else{
//			int sub = w.pos.y / 16;
//			Chunk16SegmentData c = segs[0].segmentData[sub];
//			System.arraycopy(c.getAsIntBuffer(), 0, w.getSegmentData().getAsIntBuffer(), 0, SegmentData.TOTAL_SIZE);
//			w.getSegmentData().setBlockAddedForced(
//					c.isBlockAddedForced());
//			c.setBlockAddedForced(false);
//		}
	}
	private void create32Chunk(Segment w) throws SegmentDataWriteException {
		
		
		boolean bot = w.pos.y == 0;
		int maxY = bot ? 24 : 32;
		int mShift = (bot ? 0 : -8);
		int lShift = (bot ? 8 : 0);
		w.getSegmentData().checkWritable();
		for(int z = 0; z < 32; z++){
			for(int y = 0; y < maxY; y++){
				for(int x = 0; x < 32; x++){
					
					int coX = x+8;
					int coY = w.pos.y+y +mShift;
					int coZ = z+8;
					
					int info48 = get48InfoIndex(coX, coY, coZ);
					
					assert(info48 >= 0 && info48 < total.length):info48+"; "+coX+", "+coY+", "+coZ+"; "+w.pos;
					
					
					int infoIndex = SegmentData.getInfoIndex(x, y+lShift, z);

					try {
						w.getSegmentData().setDataAt(infoIndex, total[info48]);
					} catch (Exception exception) {
						exception.printStackTrace();
					}

					if(!w.getSegmentData().isBlockAddedForced() && w.getSegmentData().getType(infoIndex) > 0){
						w.getSegmentData().setBlockAddedForced(true);
					}
				}
			}
		}
	}

	private void fillColumn(Segment w, int yInd, int sub, int x, int z){
		Seg seg = segs[yInd];
		int y = sub * 16;
		fillRange(w, seg.segmentData[sub],   x, y,  z,     x+16, y+16, z+16);
	}
	
	private void fillRange(Segment w, Chunk16SegmentData f, 
			int fromX, int fromY, int fromZ, 
			int toX,   int toY,   int toZ){
		int dataIndex = 0;
		
		for(int z = fromZ; z < toZ; z++){
			for(int y = fromY; y < toY; y++){
				for(int x = fromX; x < toX; x++){
					
					
					int info48 = get48InfoIndex(x, y, z);
					
					;
					
					total[info48] = SegmentData.convertIntValue(f, dataIndex);
					
					dataIndex ++;
				}
			}
		}
//		w.getSegmentData().setBlockAddedForced(
//				w.getSegmentData().isBlockAddedForced() || 
//				f.isBlockAddedForced());
		f.setBlockAddedForced(false);
	}
	
	
	

}
