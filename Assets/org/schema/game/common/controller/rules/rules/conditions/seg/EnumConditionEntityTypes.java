package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.util.Set;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public enum EnumConditionEntityTypes {
	SHIP(EntityType.SHIP),
	TURRET(EntityType.SHIP),
	STATION(EntityType.SPACE_STATION),
	PLANET(EntityType.PLANET_SEGMENT, EntityType.PLANET_CORE, EntityType.PLANET_ICO),
	SHOP(EntityType.SHOP),
	ASTEROID(EntityType.ASTEROID),
	;
	
	private final Set<EntityType> e = new ObjectOpenHashSet<EntityType>();

	private EnumConditionEntityTypes(EntityType ... ee) {
		for(EntityType e : ee) {
			this.e.add(e);
		}
	}
	
	public boolean isType(SimpleTransformableSendableObject<?> s) {
		if(!e.contains(s.getType())) {
			return false;
		}
		if(s instanceof SegmentController) {
			SegmentController seg = (SegmentController)s;
			if(this == TURRET && seg.railController.isRoot()) {
				return false;
			}else if(!seg.railController.isRoot()) {
				return false;
			}
		}
		return true;
	}
}
