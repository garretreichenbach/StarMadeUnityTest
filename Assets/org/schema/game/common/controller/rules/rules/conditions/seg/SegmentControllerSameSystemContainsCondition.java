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
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerSameSystemContainsCondition extends SegmentControllerMoreLessCondition{

	

	@RuleValue(tag = "Type")
	public EnumConditionEntityTypes type = EnumConditionEntityTypes.SHIP;
	
	@RuleValue(tag = "Amount")
	public int count;
	
	@RuleValue(tag = "Relationship")
	public RType relation = RType.NEUTRAL;
	
	
	
	
	public SegmentControllerSameSystemContainsCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_ANY_SECTOR_SWITCHED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_SAME_SYSTEM_CONTAINS;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		
		GameServerState st = (GameServerState)a.getState();
		Vector3i system = a.getSystem(new Vector3i());
		Vector3i other = new Vector3i();
		int c = 0;
		for(Sendable sendable : st.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
			if(sendable instanceof SimpleTransformableSendableObject<?>) {
				SimpleTransformableSendableObject<?> e = (SimpleTransformableSendableObject<?>)sendable;
				
				
				
				if(e != a && type.isType(e) && system.equals(e.getSystem(other))) {
					RType relationTo = a.getRelationTo(e);
					if(relationTo == relation) {
						c++;
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
		return Lng.str("%s Loaded Entities (%s) in entity System", type.name(), relation.name());
	}
}
