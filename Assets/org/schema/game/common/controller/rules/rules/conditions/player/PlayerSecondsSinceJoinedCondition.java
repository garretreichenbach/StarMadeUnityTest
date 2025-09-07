package org.schema.game.common.controller.rules.rules.conditions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;

import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.controller.rules.rules.conditions.TimedCondition;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerSecondsSinceJoinedCondition extends PlayerCondition  implements TimedCondition{

	
	@RuleValue(tag = "Seconds")
	public int durationInSecs;
	
	public long firstFired = -1;

	public boolean flagTriggered;

	private boolean flagEndTriggered;
	
	
	
	public PlayerSecondsSinceJoinedCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_TIMED_CONDITION;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.PLAYER_JOINED_AFTER_SECS;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, PlayerState a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		
		if(firstFired == -1) {
			firstFired = a.getState().getUpdateTime();
			a.getRuleEntityManager().addDurationCheck(this);
		}
		long serverTime = a.getState().getUpdateTime();
		return isTimeToFire(serverTime);
		
		
	}
	
	@Override
	public boolean isTimeToFire(long time) {
		return time >= firstFired + durationInSecs*1000;
	}

	@Override
	public String getDescriptionShort() {
		return Lng.str("becomes true after %s secs player joined", durationInSecs);
	}

	@Override
	public void flagTriggeredTimedCondition() {
		this.flagTriggered = true;
	}

	@Override
	public boolean isTriggeredTimedCondition() {
		return flagTriggered;
	}

	@Override
	public boolean isRemoveOnTriggered() {
		return true;
	}
	@Override
	public boolean isTriggeredTimedEndCondition() {
		return flagEndTriggered;
	}

	@Override
	public void flagTriggeredTimedEndCondition() {
		flagEndTriggered = true;		
	}
}
