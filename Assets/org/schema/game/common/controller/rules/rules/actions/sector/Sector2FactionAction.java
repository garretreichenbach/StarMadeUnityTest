package org.schema.game.common.controller.rules.rules.actions.sector;

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

public class Sector2FactionAction extends SectorAction{

	
	@RuleValue(tag = "Actions")
	public FactionActionList actions = new FactionActionList();
	
	
	public Sector2FactionAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Execute %s faction actions for factions in sector", actions.size());
	}

	@Override
	public void onTrigger(RemoteSector s) {
		if(s.isOnServer()) {
			
			GameServerState state = (GameServerState) s.getState();

			Set<Faction> facs =new ObjectOpenHashSet<Faction>();
			for(PlayerState p : state.getPlayerStatesByName().values()) {
				
				if(p.getSectorId() == s.getId() && state.getFactionManager().existsFaction(p.getFactionId())) {
					facs.add(state.getFactionManager().getFaction(p.getFactionId()));
				}
			}
			
			for(Faction f : facs) {
				actions.onTrigger(f);
			}
		}
	}

	@Override
	public void onUntrigger(RemoteSector s) {
		if(s.isOnServer()) {
			
			GameServerState state = (GameServerState) s.getState();

			Set<Faction> facs =new ObjectOpenHashSet<Faction>();
			for(PlayerState p : state.getPlayerStatesByName().values()) {
				
				if(p.getSectorId() == s.getId() && state.getFactionManager().existsFaction(p.getFactionId())) {
					facs.add(state.getFactionManager().getFaction(p.getFactionId()));
				}
			}
			
			for(Faction f : facs) {
				actions.onUntrigger(f);
			}
		}
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.SECTOR_2_FACTION;
	}
}
