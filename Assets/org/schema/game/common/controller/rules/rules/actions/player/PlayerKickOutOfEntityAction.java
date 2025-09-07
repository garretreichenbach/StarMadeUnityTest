package org.schema.game.common.controller.rules.rules.actions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rails.RailRelation;
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

public class PlayerKickOutOfEntityAction extends PlayerAction{

	
	
	public PlayerKickOutOfEntityAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Kick Player");
	}

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer() && s.getFirstControlledTransformableWOExc() instanceof SegmentController) {
			kickPlayersOut((GameServerState)s.getState(), (SegmentController)s.getFirstControlledTransformableWOExc(), true, s);
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_KICK_OUT_OF_ENTITY;
	}
	
	public void kickPlayersOut(GameServerState s, SegmentController c, boolean dock, PlayerState playerFilter) {
		if(playerFilter == null) {
			c.kickAllPlayersOutServer();
		}else {
			c.kickPlayerOutServer(playerFilter);
		}
		
		if(dock) {
			for(RailRelation r : c.railController.next) {
				kickPlayersOut(s, r.docked.getSegmentController(), dock, playerFilter);
			}
		}
	}
}
