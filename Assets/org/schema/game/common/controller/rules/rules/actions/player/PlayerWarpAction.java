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

public class PlayerWarpAction extends PlayerAction{

	
	@RuleValue(tag = "SectorX")
	public int sectorX;
	@RuleValue(tag = "SectorY")
	public int sectorY;
	@RuleValue(tag = "SectorZ")
	public int sectorZ;
	
	
	@RuleValue(tag = "WarpAsCopy")
	public boolean copy;
	
	public PlayerWarpAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Warp to sector %s %s %s  ", sectorX, sectorY, sectorZ);
	}

	@Override
	public void onTrigger(PlayerState s) {
		if(s.isOnServer()) {
			Vector3i sec = new Vector3i(sectorX, sectorY, sectorZ);
			Sector sector;
			try {
				sector = ((GameServerState)s.getState()).getUniverse().getSector(sec);
			
				if (sector != null) {
					for (ControllerStateUnit u : s
							.getControllerState().getUnits()) {
						if (u.playerControllable instanceof SimpleTransformableSendableObject) {
							
							
							((GameServerState)s.getState()).getController()
									.queueSectorSwitch(
											AdminCommandQueueElement.getControllerRoot((SimpleTransformableSendableObject) u.playerControllable),
											sector.pos, SectorSwitch.TRANS_JUMP, copy, true, true);
	
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onUntrigger(PlayerState s) {
		
	}
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_WARP;
	}
}
