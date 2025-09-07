package org.schema.game.common.controller.rules.rules.actions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.admin.AdminCommandQueueElement;
import org.schema.schine.common.language.Lng;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerSetFactionPointsAction extends PlayerAction{

	
	@RuleValue(tag = "FactionPoints")
	public int points;
	
	public PlayerSetFactionPointsAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Sets player's faction %s faction points", points);
	}

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer()) {
			FactionManager factionManager = ((GameServerState)s.getState()).getFactionManager();
			Faction faction = factionManager.getFaction(s.getFactionId());
			if(faction != null) {
				faction.factionPoints = points;
				faction.sendFactionPointUpdate(((GameServerState)s.getState()).getGameState());
			}
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_SET_FACTION_POINTS;
	}
}
