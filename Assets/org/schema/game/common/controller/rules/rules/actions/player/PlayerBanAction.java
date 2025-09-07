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
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.PlayerState;
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

public class PlayerBanAction extends PlayerAction{

	@RuleValue(tag = "Minutes")
	public int minutes = -1; 
	
	
	public PlayerBanAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Ban Player");
	}

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer()) {
			((GameServerState)s.getState()).getController().addBannedName("<RULE>", s.getName(), minutes < 0 ? -1 : (System.currentTimeMillis() + (long)minutes*60L*1000L));
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_BAN;
	}
}
