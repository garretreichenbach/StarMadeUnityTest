package org.schema.game.common.controller.rules.rules.actions.seg;

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
import org.schema.game.common.controller.rules.rules.actions.faction.FactionActionList;
import org.schema.game.common.controller.rules.rules.actions.player.PlayerActionList;
import org.schema.game.common.controller.rules.rules.actions.sector.SectorAction;
import org.schema.game.common.controller.rules.rules.actions.sector.SectorActionList;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
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

public class SegmentController2PlayerAction extends SegmentControllerAction{

	
	@RuleValue(tag = "Actions")
	public PlayerActionList actions = new PlayerActionList();
	
	
	public SegmentController2PlayerAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Execute %s actions for player of entity", actions.size());
	}

	@Override
	public void onTrigger(SegmentController s) {
		if(s.isOnServer()) {
			
			if(s.getOwnerState() instanceof PlayerState) {
				actions.onTrigger((PlayerState)s.getOwnerState());
			}
		}
	}

	@Override
	public void onUntrigger(SegmentController s) {
		if(s.isOnServer()) {
			
			if(s.getOwnerState() instanceof PlayerState) {
				actions.onUntrigger((PlayerState)s.getOwnerState());
			}
		}
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.SEG_2_PLAYER;
	}
}
