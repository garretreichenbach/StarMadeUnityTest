package org.schema.game.common.data.event;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.damage.DamageDealerType;

public class EventFactory {
	
	public EventFactory(GameClientState state) {
	}
	public void fireProjectileFiredEvent(int damagerId, float damage, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, DamageDealerType damageType){
		
	}
	public void fireProjectileHitEvent(int damagerId, int hitId, float damage, float posX, float posY, float posZ, DamageDealerType damageType){
		
	}
}
