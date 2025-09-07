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

public class PlayerInSectorCondition extends PlayerCondition{

	@RuleValue(tag = "FromX")
	public int xFrom;
	
	
	@RuleValue(tag = "ToX")
	public int xTo;
	
	
	@RuleValue(tag = "FromY")
	public int yFrom;
	
	
	@RuleValue(tag = "ToY")
	public int yTo;
	
	
	@RuleValue(tag = "FromZ")
	public int zFrom;
	
	
	@RuleValue(tag = "ToZ")
	public int zTo;
	
	
	
	
	
	public PlayerInSectorCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_SECTOR_ENTITIES_CHANGED;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.PLAYER_IN_SECTOR;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, PlayerState a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		Vector3i pos;
			pos = a.getCurrentSector();
		
			return pos.x >= xFrom  && pos.x <= xTo   &&
					pos.y >= yFrom   && pos.y <= yTo  &&
					pos.z >= zFrom && pos.z <= zTo 
					;
	}
	
	@Override
	public String getDescriptionShort() {
		return Lng.str("Player is in sector X[%s, %s], Y[%s, %s], Z[%s, %s]", xFrom, xTo, yFrom, yTo, zFrom, zTo);
	}
}
