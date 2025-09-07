package org.schema.game.common.data.mines.handler;

import java.util.List;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.data.SimpleGameObject;
import org.schema.game.common.data.mines.Mine;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.faction.FactionRelation.AttackType;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.server.data.FactionState;
import org.schema.schine.graphicsengine.core.Timer;

public abstract class MineHandler {

	
	public final Mine mine;

	
	
	public static boolean isAttacking(AttackType t, Mine mine) {
		return AttackType.isAttacking(mine.getFiringMode(), t);
	}
	public boolean isAttacking(SimpleGameObject obj) {
		return isAttacking(mine, obj);
	}
	/**
	 * 
	 * @param target
	 * @return if this mine attacks this object
	 */
	public static boolean isAttacking(Mine attacker, SimpleGameObject target) {
		if(target.getOwnerState() != null && target.getOwnerState().isSpawnProtected()) {
			return false;
		}
		if(target.getOwnerId() == attacker.getOwnerId()) {
			if(isAttacking(AttackType.OWNER, attacker)) {
				return true;
			}
		}
		
		FactionManager factionManager = ((FactionState)target.getState()).getFactionManager();

		if(target.getFactionId() == attacker.getFactionId()) {
			if(isAttacking(AttackType.FACTION, attacker)) {
				return true;
			}
		}
		
		Faction faction = factionManager.getFaction(attacker.getFactionId());
		if(faction != null) {
			RType relation = faction.getRelationshipWithFactionOrPlayer(target.getFactionId());
			switch(relation) {
			case ENEMY:
				if(isAttacking(AttackType.ENEMY, attacker)) {
					return true;
				}
				break;
			case FRIEND:
				if(isAttacking(AttackType.ALLY, attacker)) {
					//faction id equals is checked before, so this has to be an ally
					return true;
				}
				break;
			case NEUTRAL:
				if(isAttacking(AttackType.NEUTRAL, attacker)) {
					return true;
				}
				break;
			default:
				throw new RuntimeException("Relation not found: "+relation.name());
			}
		}else {
			if(isAttacking(AttackType.NEUTRAL, attacker)) {
				return true;
			}
		}
		return false;
	}
	
	public MineHandler(Mine mine) {
		super();
		this.mine = mine;
	}
	
	public abstract void handleActive(Timer timer, List<SimpleGameObject> detected);
	public abstract void onBecomingActive(List<SimpleGameObject> detected);
	public abstract void handleInactive(Timer timer);
	public abstract void onBecomingInactive();

	public abstract InterEffectSet getAttackEffectSet(long weaponId, DamageDealerType damageDealerType);

	public boolean isPointDefense() {
		return false;
	}
}
