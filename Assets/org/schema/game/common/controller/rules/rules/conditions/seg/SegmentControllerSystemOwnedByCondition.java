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
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.network.objects.Sendable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SegmentControllerSystemOwnedByCondition extends SegmentControllerCondition{

	

	@RuleValue(tag = "Own")
	public boolean own = false;
	
	@RuleValue(tag = "Relationship")
	public RType relation = RType.NEUTRAL;
	
	
	
	
	public SegmentControllerSystemOwnedByCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_ANY_SECTOR_SWITCHED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.SEG_SYSTEM_RELATIONSHIP;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, SegmentController a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		
		GameServerState st = (GameServerState)a.getState();
		Vector3i system = a.getSystem(new Vector3i());
		Vector3i other = new Vector3i();
		StellarSystem sys;
		try {
			sys = st.getUniverse().getStellarSystemFromStellarPos(system);
			int fid = a.getFactionId();
			if(a.getOwnerState() != null) {
				fid = a.getOwnerState().getFactionId();
			}
			if(own) {
				return sys.getOwnerFaction() != 0 && sys.getOwnerFaction() == fid;
			}else {
				return relation == st.getFactionManager().getRelation(fid, sys.getOwnerFaction());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		return false;
	}

	@Override
	public String getDescriptionShort() {
		return own ? Lng.str("Is current system owned by faction of entity owner") : Lng.str("Is current system owner in %s relation to the entity owner", relation.getName());
	}
}
