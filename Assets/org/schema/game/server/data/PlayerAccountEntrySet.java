package org.schema.game.server.data;

import org.schema.common.util.StringTools;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerAccountEntrySet extends ObjectOpenHashSet<PlayerAccountEntry>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	

	@SuppressWarnings("unlikely-arg-type")
	public boolean containsAndIsValid(String s){
		if(contains(s)){
			
			for(PlayerAccountEntry e : this){
				if(e.isValid(System.currentTimeMillis()) && e.equals(s)){
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean containsAndIsValid(String s, StringBuffer failReason) {
		if(contains(s)){
			long currentTime = System.currentTimeMillis();
			for(PlayerAccountEntry e : this){
				if(e.isValid(currentTime) && e.equals(s)){
					
					if(e.validUntil > 0 && currentTime < e.validUntil){
						
						long tBanned = e.validUntil - currentTime;
						failReason.append(" (Ban will be lifted in "+StringTools.formatTimeFromMS(tBanned)+")");
					}else if(e.validUntil <= 0){
						failReason.append(" (permanently banned)");
					}
					
					return true;
				}
			}
		}
		return false;
	}
}
