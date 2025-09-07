package org.schema.game.common.controller.rules.rules.actions.player;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.rails.RailController.RailTarget;
import org.schema.game.common.controller.rules.RuleStateChange;
import org.schema.game.common.controller.rules.rules.RuleValue;
import org.schema.game.common.controller.rules.rules.actions.ActionTypes;
import org.schema.game.common.controller.rules.rules.actions.sector.SectorAction;
import org.schema.game.common.controller.rules.rules.actions.sector.SectorActionList;
import org.schema.game.common.controller.rules.rules.actions.seg.SegmentControllerActionList;
import org.schema.game.common.controller.rules.rules.conditions.ConditionTypes;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
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

public class Player2EnteredSegmentControllerAction extends PlayerAction{

	
	@RuleValue(tag = "Actions")
	public SegmentControllerActionList actions = new SegmentControllerActionList();
	
	@RuleValue(tag = "Target")
	public RailTarget target = RailTarget.EVERYTHING; 

	@RuleValue(tag = "PilotOnly")
	public boolean pilot = false; 

	private Set<SegmentController> targets = new ObjectOpenHashSet<SegmentController>(); 
	public Player2EnteredSegmentControllerAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Execute %s entity actions for entered entity of player", actions.size());
	}

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer()) {
			
			SimpleTransformableSendableObject<?> c = s.getFirstControlledTransformableWOExc();
			if (c instanceof SegmentController && (!pilot || c.getOwnerState() == s)) {
				targets.clear();
				target.getTargets((SegmentController)c, targets);
				
				for(SegmentController seg : targets) {
					actions.onTrigger(seg);
				}
			}
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		if(s.isOnServer()) {
			
			SimpleTransformableSendableObject<?> c = s.getFirstControlledTransformableWOExc();
			if (c instanceof SegmentController && (!pilot || c.getOwnerState() == s)) {
				targets.clear();
				target.getTargets((SegmentController)c, targets);
				
				for(SegmentController seg : targets) {
					actions.onUntrigger(seg);
				}
			}
		}
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_2_SEG;
	}
}
