package org.schema.game.common.controller.rules.rules.conditions.seg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerAdjacentSectorContainsCondition extends SegmentControllerMoreLessCondition{

	@RuleValue(tag = "Type")
	public EnumConditionEntityTypes type = EnumConditionEntityTypes.SHIP;
	
	@RuleValue(tag = "Amount")
	public int count;
	
	@RuleValue(tag = "Relationship")
	public RType relation = RType.NEUTRAL;
	
//	@RuleValue(tag = "SectorX")
//	public int x;
//	@RuleValue(tag = "SectorY")
//	public int y;
//	@RuleValue(tag = "SectorZ")
//	public int z;
	
	
	
	public SegmentControllerAdjacentSectorContainsCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_SECTOR_SWITCHED | TRIGGER_ON_SECTOR_ENTITIES_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_ADJACENT_SECTOR_CONTAINS;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		
		GameServerState s = (GameServerState)a.getState();
		
		int c = 0;
		Vector3i own = a.getSector(new Vector3i());
		if(own == null) {
			return false;
		}
		Vector3i add = new Vector3i();
		for(int z = -1; z < 1; z++) {
			for(int y = -1; y < 1; y++) {
				for(int x = -1; x < 1; x++) {
					if(x != 0 || y != 0 || z != 0) {
						add.set(own);
						add.add(x, y, z);
						
						Sector sec = s.getUniverse().getSectorWithoutLoading(add);
						if(sec != null) {
							Set<SimpleTransformableSendableObject<?>> entities = sec.getEntities();
							for(SimpleTransformableSendableObject<?> e : entities) {
								if(e != a && type.isType(e)) {
									RType relationTo = a.getRelationTo(e);
									if(relationTo == relation) {
										c++;
									}
								}
							}
						}
					}
				}
			}
		}
		
		return moreThan ? (c > count) : (c <= count);
	}
	@Override
	public String getCountString() {
		return String.valueOf(count);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s Entities (%s) adjacent to entity sector", type.name(), relation.name());
	}
}
