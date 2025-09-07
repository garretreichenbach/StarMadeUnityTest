package org.schema.game.common.controller.rules.rules.conditions.sector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.FactionRange;
import org.schema.game.common.controller.rules.rules.conditions.seg.EnumConditionEntityTypes;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class SectorContainsCondition extends SectorMoreLessCondition{

	@RuleValue(tag = "Type")
	public EnumConditionEntityTypes type = EnumConditionEntityTypes.SHIP;
	
	@RuleValue(tag = "Amount")
	public int count;
	
	@RuleValue(tag = "FactionRange")
	public FactionRange factionRange = new FactionRange();
	
	
	
	public SectorContainsCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_SECTOR_CHMOD;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SECTOR_CHMOD;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, RemoteSector a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		int cc = 0;
		if(a.isOnServer()) {
			Set<SimpleTransformableSendableObject<?>> entities = a.getServerSector().getEntities();
			for(SimpleTransformableSendableObject<?> s : entities) {
				if(factionRange.isInRange(s.getFactionId()) && s.getSectorId() == a.getId()) {
					cc++;
				}
			}
		}else {
			for(Sendable se : a.getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
				if(se instanceof SimpleTransformableSendableObject<?>) {
					SimpleTransformableSendableObject<?> s = ((SimpleTransformableSendableObject<?>)se);
					if(factionRange.isInRange(s.getFactionId()) && s.getSectorId() == a.getId()) {
						cc++;
					}
				}
			}
		}
		return moreThan ? (cc > count) : (cc <= count);
	}
	

	@Override
	public String getCountString() {
		return String.valueOf(count);
	}

	@Override
	public String getQuantifierString() {
		return Lng.str("%s Entities (%s) in sector", type.name(), factionRange);
	}
}
