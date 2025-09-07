package org.schema.game.common.controller.rules.rules.actions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.actions.RecurringAction;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerModCreditsRecurringAction extends PlayerRecurringAction{

	
	@RuleValue(tag = "Credits")
	public int credits = 0;
	
	@RuleValue(tag = "Seconds")
	public int seconds;
	
	public PlayerModCreditsRecurringAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Add %s credits every %s seconds", credits, seconds);
	}

	
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_MOD_CREDITS_RECURRING;
	}
	@Override
	public long getCheckInterval() {
		return (long)seconds*1000L;
	}
	@Override
	public void onActive(PlayerState s) {
		if(s.isOnServer()) {
			s.modCreditsServer(credits);
		}
	}
}
