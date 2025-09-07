package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.Set;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SegmentControllerIsTypeCondition extends SegmentControllerCondition{

	

	
	

	@RuleValue(tag = "IsShip")
	public boolean isShip;
	
	@RuleValue(tag = "IsDock")
	public boolean isDock;

	@RuleValue(tag = "IsTurret")
	public boolean isTurret;
	
	@RuleValue(tag = "IsStation")
	public boolean isStation;
	
	@RuleValue(tag = "IsAsteroid")
	public boolean isAsteroid;
	
	@RuleValue(tag = "IsPlanet")
	public boolean isPlanet;


	
	public SegmentControllerIsTypeCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_RULE_CHANGE | TRIGGER_ON_DOCKING_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_IS_TYPE;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return getAllowedTypes().contains(a.getType()) && checkDockTurret(a);
	}

	private boolean checkDockTurret(SegmentController a) {
		if(a.getType() == EntityType.SHIP && (isShip || isTurret || isDock)) {
			
			if(isShip && a.railController.isRoot()) {
				return true;
			}
			if(isTurret || isDock) {
				if(a.railController.isRoot()) {
					return false;
				}
				if(isTurret) {
					return a.railController.isTurretDocked();
				} else{
					assert(isDock);
					return !a.railController.isTurretDocked();
				}
			}
			return false;
			
		}
		return true;
	}

	public Set<EntityType> getAllowedTypes() {
		Set<EntityType> b = new ObjectOpenHashSet<EntityType> ();
		if(isShip || isDock || isTurret) {
			b.add(EntityType.SHIP);
		}
		if(isStation) {
			b.add(EntityType.SPACE_STATION);
		}
		if(isAsteroid) {
			b.add(EntityType.ASTEROID);
			b.add(EntityType.ASTEROID_MANAGED);
		}
		if(isPlanet) {
			b.add(EntityType.PLANET_SEGMENT);
			b.add(EntityType.PLANET_ICO);
			b.add(EntityType.PLANET_CORE);
			b.add(EntityType.GAS_PLANET);
		}
		return b;
	}
	public String getAllowed() {
		StringBuffer b = new StringBuffer();
		if(isShip) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(EntityType.SHIP.getName());
		}
		if(isDock) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(Lng.str("Dock"));
		}
		if(isTurret) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(Lng.str("Turret"));
		}
		if(isStation) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(EntityType.SPACE_STATION.getName());
		}
		if(isAsteroid) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(EntityType.ASTEROID.getName());
		}
		if(isPlanet) {
			if(b.length() > 0) {
				b.append(", ");
			}
			b.append(EntityType.PLANET_SEGMENT.getName());
		}
		return b.toString();
	}
	@Override
	public String getDescriptionShort() {
		
		return Lng.str("Is entity of type: %s", getAllowed());
	}
}
