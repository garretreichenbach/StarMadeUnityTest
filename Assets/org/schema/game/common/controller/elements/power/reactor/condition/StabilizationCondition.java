package org.schema.game.common.controller.elements.power.reactor.condition;


public abstract class StabilizationCondition {
	public abstract double getStart();
	public abstract double getEnd();

	public abstract double getMinEffect();
	public abstract double getMaxEffect();
	
	public double getEffect(final double stabilization){
		
		if(stabilization < getStart()){
			double d = getStart() - getEnd();
			double stabD = stabilization - getEnd();
			
			double eff = stabD / d;
			
			double effectRange = getMaxEffect() - getMinEffect();
			
			return getMinEffect() + eff*effectRange;
		}
		return 0;
	}
	
}
