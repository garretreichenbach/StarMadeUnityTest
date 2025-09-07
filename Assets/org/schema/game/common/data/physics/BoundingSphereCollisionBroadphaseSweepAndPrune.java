package org.schema.game.common.data.physics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.schema.common.util.CompareTools;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BoundingSphereCollisionBroadphaseSweepAndPrune<E extends BoundingSphereObject> extends BoundingSphereCollisionBroadphase<E>{

	public BoundingSphereCollisionBroadphaseSweepAndPrune(
			BoundingSphereCollisionManagerLocal<E> man) {
		super(man);
	}

	public List<E> xList = new ObjectArrayList<E>();
	
	private class XComp implements Comparator<E>{
		@Override
		public int compare(E o1, E o2) {
			//sort by lower bound
			return CompareTools.compare(
					o1.getWorldTransform().origin.x - o1.getBoundingSphereTotal().radius, 
					o2.getWorldTransform().origin.x - o2.getBoundingSphereTotal().radius);
		}
	}
	
	private XComp xComp = new XComp();
	
	@Override
	protected void setupPhase(List<E> bbs) {
		xList.clear();
		
		xList.addAll(bbs);
		
		Collections.sort(xList, xComp);
	}

	@Override
	protected List<BoundingSpherePair<E>> broadPhase(BoundingSphereCollisionManagerLocal<E> man, List<BoundingSpherePair<E>> out) {
		out.clear();
		for(int j = 0; j < xList.size()-1; j++){
			
			E A = xList.get(j);
			
			for(int i = j+1; i < xList.size(); i++){
				E B = xList.get(j);
				if(B.getWorldTransform().origin.x - B.getBoundingSphereTotal().radius <=
					A.getWorldTransform().origin.x + A.getBoundingSphereTotal().radius){
					/*
					 * spheres are overlapping, since the lower bound of B is
					 * bigger than the lower bound of A and also smaller than
					 * the upper bound of A
					 */
					BoundingSpherePair<E> pair = man.getPair();
					
				}else{
					//no more overlaps on this sphere
					break;
				}
				
			}
		}
		
		for(int i = 0; i < out.size(); i++){
			BoundingSpherePair<E> pair = out.get(i);
			if(!pair.overlapY() || !pair.overlapZ()){
				BoundingSpherePair<E> remove = out.remove(i--);
				man.freePair(remove);
			}
		}
		return out;
	}

	
	
	
}
