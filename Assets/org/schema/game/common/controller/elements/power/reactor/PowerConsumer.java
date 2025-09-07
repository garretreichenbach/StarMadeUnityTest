package org.schema.game.common.controller.elements.power.reactor;

import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.graphicsengine.core.Timer;

public interface PowerConsumer {
	public enum PowerConsumerCategory{
		THRUST(en -> {
			return Lng.str("Thrusters");
		}),
		SHIELDS(en -> {
			return Lng.str("Shields");
		}),
		JUMP_DRIVE(en -> {
			return Lng.str("Jump Drive");
		}),
		CANNONS(en -> {
			return Lng.str("Cannons");
		}),
		BEAMS(en -> {
			return Lng.str("Beams");
		}),
		MISSILES(en -> {
			return Lng.str("Missile");
		}),
		PULSE(en -> {
			return Lng.str("Pulses");
		}),
		
		
		SCANNER(en -> {
			return Lng.str("Scanner");
		}),
		STEALTH(en -> {
			return Lng.str("Stealth");
		}),
		MINING_BEAMS(en -> {
			return Lng.str("Mining Beams");
		}),
		SUPPORT_BEAMS(en -> {
			return Lng.str("Support Beams");
		}),
		WARP_GATE(en -> {
			return Lng.str("Warp Gates");
		}),
		SHIPYARD(en -> {
			return Lng.str("Shipyard");
		}),
		DOCKS(en -> {
			return Lng.str("Docks");
		}),
		TURRETS(en -> {
			return Lng.str("Turrets");
		}),
		FACTORIES(en -> {
			return Lng.str("Factories");
		}),
		OTHERS(en -> {
			return Lng.str("Others");
		}),
		MINES(en -> {
			return Lng.str("Mines");
		}),
		
		
		;
		private final Translatable t;

		private PowerConsumerCategory(Translatable t){
			this.t = t;
		}
		public String getName(){
			return t.getName(this);
		}
		
	}
	/**
	 * 
	 * @return power consumed when not charging
	 */
	public double getPowerConsumedPerSecondResting();
	
	/**
	 * 
	 * @return power consumed while charging (reloading)
	 */
	public double getPowerConsumedPerSecondCharging();
	
	/**
	 * 
	 * @param curTime TODO
	 * @return module is currently charging (reloading)
	 */
	public boolean isPowerCharging(long curTime);
	/**
	 * percentage of module powered
	 * @param powered: value between 0 and 1
	 * 
	 */
	public void setPowered(float powered);
	public float getPowered();

	public PowerConsumerCategory getPowerConsumerCategory();
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting);

	public boolean isPowerConsumerActive();

	public String getName();

	public void dischargeFully();
	
}
