package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;

public class DeepStructureChecker {

	private static final int BATCH = 64;

	private SegmentController segmentController;

	private boolean done;
	
	private final LongArrayList toCheck = new LongArrayList(256);
	private final SegmentPiece p = new SegmentPiece();

	private long controller;

	private short controlledType;
	public DeepStructureChecker() {
	}
	public void set(SegmentController segmentController, long controller, short controlledType, int predictedSize){
		this.segmentController = segmentController;
		toCheck.ensureCapacity(predictedSize);
		toCheck.clear();
		this.controller = controller;
		this.controlledType = controlledType;
		this.done = false;
	}
	public boolean isDone() {
		return done;
	}
	public void init(LongCollection l){
		toCheck.addAll(l);
	}
	
	public void update(){
		int size = Math.min(toCheck.size(), BATCH);
		int found = 0;
		for(int i = 0; i < size; i++){
			long index = toCheck.removeLong(toCheck.size()-1);
			
			SegmentPiece point = segmentController.getSegmentBuffer().getPointUnsave(index, p);
			if(point == null && segmentController.isInboundAbs(
					ElementCollection.getPosX(index), 
					ElementCollection.getPosY(index), 
					ElementCollection.getPosZ(index))){
				toCheck.add(0, index);
				break;
			}else if(point == null || point.getType() == 0){
				segmentController.getControlElementMap().removeControllerForElement(controller, index, controlledType);
				found++;
			}else{
			}
		}
		if(found > 0){
			System.err.println("[DEEP STRUCTURE CHECKER] FOUND BAD CONNECTION TO "+found+" MORE BLOCKS; still to check: "+toCheck.size());
		}
		if(toCheck.isEmpty()){
			done = true;
		}
	}

}
