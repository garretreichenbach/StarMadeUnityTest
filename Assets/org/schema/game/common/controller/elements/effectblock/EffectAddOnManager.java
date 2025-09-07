package org.schema.game.common.controller.elements.effectblock;

import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.RecharchableActivatableDurationSingleModule;
import org.schema.game.common.data.blockeffects.BlockEffectTypes;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class EffectAddOnManager {

	private final ManagerContainer<?> managerContainer;

	private final Long2ObjectOpenHashMap<EffectAddOn> effects = new Long2ObjectOpenHashMap<EffectAddOn>();
	
	public EffectAddOnManager(ManagerContainer<?> managerContainer) {
		this.managerContainer = managerContainer;
		init();
	}

//	public void receivedCharge(long parameter, float val) {
//		EffectAddOn effectAddOn = effects.get(parameter);
//		if(effectAddOn != null){
//			effectAddOn.setCharge(RecharchableSingleModule.decodeCharge(val));
//			effectAddOn.setCharges(RecharchableSingleModule.decodeCharges(val));
//		}else{
//			throw new NullPointerException("Effect not found: "+parameter);
//		}
//	}

	public float getCharge(long parameter) {
		EffectAddOn effectAddOn = effects.get(parameter);
		if(effectAddOn != null){
			return effectAddOn.getCharge();
		}else{
			throw new NullPointerException("Effect not found: "+parameter);
		}
	}
	
	public void init(){
//		registerEffect(BlockEffectTypes.EVADE);
		registerEffect(BlockEffectTypes.TAKE_OFF);
	}
	public void registerEffect(BlockEffectTypes type){
		EffectAddOn e = new EffectAddOn(managerContainer, type);
		effects.put(e.getUsableId(), e);
	}

	public Long2ObjectOpenHashMap<EffectAddOn> getEffects() {
		return effects;
	}

	public int getCharges(long parameter) {
		EffectAddOn effectAddOn = effects.get(parameter);
		if(effectAddOn != null){
			return effectAddOn.getCharges();
		}else{
			throw new NullPointerException("Effect not found: "+parameter);
		}
	}

	public RecharchableActivatableDurationSingleModule get(long parameter) {
		return effects.get(parameter);
	}

	public void sendChargeUpdate() {
		for(EffectAddOn e : effects.values()){
			e.sendChargeUpdate();
		}
	}

}
