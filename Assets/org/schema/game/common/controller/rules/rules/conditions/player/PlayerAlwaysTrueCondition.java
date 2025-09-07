package org.schema.game.common.controller.rules.rules.conditions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerAlwaysTrueCondition extends PlayerCondition{

	@RuleValue(tag = "AlwaysTrue")
	public boolean alwaysTrue = true;
	

	
	
	
	
	
	public PlayerAlwaysTrueCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_RULE_CHANGE;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.PLAYER_ALWAYS_TRUE;
	}


	@Override
	public String getDescriptionShort() {
		return alwaysTrue ? Lng.str("This condition is always true") : Lng.str("This condition is always false");
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, PlayerState a, long trigger,
			boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		return alwaysTrue;
	}
	
	
}
