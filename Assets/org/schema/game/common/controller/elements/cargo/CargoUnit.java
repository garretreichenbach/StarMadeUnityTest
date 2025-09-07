package org.schema.game.common.controller.elements.cargo;

import java.util.Collections;
import java.util.Random;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentDataWriteException;

import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class CargoUnit extends ElementCollection<CargoUnit, CargoCollectionManager, CargoElementManager> {

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CargoUnit " + super.toString();
	}

	@Override
	public ControllerManagerGUI createUnitGUI(GameClientState state, ControlBlockElementCollectionManager<?, ?, ?> supportCol, ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
		return elementCollectionManager.getElementManager().getGUIUnitValues(this, elementCollectionManager, supportCol, effectCol);
	}
	private final ObjectOpenHashSet<Segment> affected = new ObjectOpenHashSet<Segment>();
	private SegmentPiece tmp = new SegmentPiece();
	private final Random r = new Random();
	public double updateBlocks(double volume, double extraCapacity){
		
		r.setSeed(ElementCollection.getIndex(elementCollectionManager.getControllerPos()));
		
		float size = size();
		
		
		
		
		byte fill = 0;
		int c = 0;
		
		
		double totalCapacity = Math.max(0.0000001, getCapacity() + extraCapacity);
		
		double volPerBlock =  totalCapacity / size();
		
		for(int i = 0; i < countsPerY.size(); i++){
			int count = countsPerY.get(i);
			
			
//			if(!getSegmentController().isOnServer()){
//				System.err.println("FILL row#"+i+" (count: "+count+"): "+fill+"; volume: "+volume);
//			}
			short cache = -1;
			boolean full = false;
			for(int j = 0; j < count; j++){
				
				if(volume <= 0){
					if(cache > -2){
						cache = 4;
					}
					fill = (byte) 4; //empty: transparent
				}else if(!full && count * volPerBlock > volume){
					cache = -2;
					fill = (byte) (r.nextInt(3)+1); //more space than inventory volume
				}else{
					cache = 0;
					full = true;
					fill = 0; //full 
				}
				if((cachedRow.size() > i && cachedRow.getShort(i) < 0) || countsPerY.size() == 1 && count == 1){
					
					if(getNeighboringCollection() == null){
						System.err.println("Exception: [CARGO] NCOL NULL");
					}
					if(c >= getNeighboringCollection().size()){
						System.err.println("Exception: [CARGO] OUT OF BOUNDS");
						return 0;
					}
					long blockIndex = getNeighboringCollection().getLong(c);
					if(getSegmentController() == null){
						System.err.println("Exception: [CARGO] SEG CON NULL");
					}
					if(getSegmentController().getSegmentBuffer() == null){
						System.err.println("Exception: [CARGO] SEG BUFF NULL");
					}
					SegmentPiece block = getSegmentController().getSegmentBuffer().getPointUnsave(blockIndex, tmp);
					
					if(block != null && block.getOrientation() != fill && block.getSegment() != null && block.getSegment().getSegmentData() != null){
						try{
							block.getSegment().getSegmentData().setOrientation(block.getInfoIndex(), fill);
						} catch (SegmentDataWriteException e) {
							SegmentDataWriteException.replaceData(block.getSegment());
							try {
								block.getSegment().getSegmentData().setOrientation(block.getInfoIndex(), fill);
							} catch (SegmentDataWriteException e1) {
								e1.printStackTrace();
								throw new RuntimeException(e1);
							}
						}
						if(!getSegmentController().isOnServer()){
							affected.add(block.getSegment());
						}
					}
				}
				c++;
				volume -= volPerBlock;
			}
			cachedRow.set(i, cache);
		}
		
		if(!getSegmentController().isOnServer()){
			for(Segment s : affected){
				((DrawableRemoteSegment)s).dataChanged(true);
			}
			affected.clear();
		}
		
		return volume;
	}
	
	public void resetBlocks() {
		for(long blockIndex : getNeighboringCollection()){
			SegmentPiece block = getSegmentController().getSegmentBuffer().getPointUnsave(blockIndex, tmp);
			ObjectOpenHashSet<Segment> affected = new ObjectOpenHashSet<Segment>();
			if(block != null && block.getSegment() != null){
				SegmentData segmentData = block.getSegment().getSegmentData();
				if(segmentData != null){
					try{
						block.getSegment().getSegmentData().setOrientation(block.getInfoIndex(), (byte)4);
					} catch (SegmentDataWriteException e) {
						SegmentDataWriteException.replaceDataOnClient(block.getSegment().getSegmentData());
						try {
							block.getSegment().getSegmentData().setOrientation(block.getInfoIndex(), (byte)4);
						} catch (SegmentDataWriteException e1) {
							e1.printStackTrace();
							throw new RuntimeException(e1);
						}
					}
					affected.add(block.getSegment());
				}
			}
			if(!getSegmentController().isOnServer()){
				for(Segment s : affected){
					((DrawableRemoteSegment)s).dataChanged(true);
				}
			}
		}
	}
	private final ShortArrayList countsPerY = new ShortArrayList();
	private final ShortArrayList cachedRow = new ShortArrayList();
	@Override
	public void calculateExtraDataAfterCreationThreaded(long updateSignture, LongOpenHashSet totalCollectionSet){
		Collections.sort(getNeighboringCollection(), comp);
		countsPerY.clear();
		short curCount = 0;
		int curY = Integer.MIN_VALUE;
		
		final int size = getNeighboringCollection().size();
		for(int i = 0; i < size; i++){
			long l = getNeighboringCollection().getLong(i);
			int y = ElementCollection.getPosY(l);
			if(curY == Integer.MIN_VALUE){
				curY = y;
			}
//			if(!getSegmentController().isOnServer()){
//				System.err.println("CC "+i+": ::  Y: "+y);
//			}
			
			if(y != curY){
				countsPerY.add(curCount);
				curY = y;
				curCount = 0;
			}
			
			curCount++;
		}
		countsPerY.add(curCount);
		
		for(int i = 0; i < countsPerY.size(); i++){
			cachedRow.add((short)-1);
		}
		if(!countsPerY.isEmpty()){
			cachedRow.trim();
			countsPerY.trim();
		}
		
//		System.err.println("CCCCOOCOOOCOO "+countsPerY);
	}
	
	private static final LongComparator comp = new LongComparator() {
		
		@Override
		public int compare(Long o1, Long o2) {
			return compare(o1.longValue(), o2.longValue());
		}
		
		@Override
		public int compare(long arg0, long arg1) {
			int posY0 = ElementCollection.getPosY(arg0);
			int posY1 = ElementCollection.getPosY(arg1);
			return posY0 - posY1;
		}
	};
	public double getCapacity() {
		
		return Math.pow((size() * elementCollectionManager.getElementManager().getCapacityPerBlockMult()), elementCollectionManager.getElementManager().getCapacityPerGroupQuadratic());
	}

	
}
