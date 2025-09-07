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
import org.schema.game.common.data.element.ElementKeyMap;
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

public class PlayerModBlockTypeRecurringAction extends PlayerRecurringAction{

	
	@RuleValue(tag = "BlockType")
	public int blockType = 0;

	@RuleValue(tag = "Amount")
	public int amount = 0;
	
	@RuleValue(tag = "Seconds")
	public int seconds;
	
	public PlayerModBlockTypeRecurringAction() {
		super();
	}
	@Override
	public String getDescriptionShort() {
		return Lng.str("Add %s %s every %s seconds", amount, ElementKeyMap.toString(blockType), seconds);
	}

	
	@Override
	public ActionTypes getType() {
		return ActionTypes.PLAYER_MOD_BLOCK_TYPE_RECURRING;
	}
	@Override
	public long getCheckInterval() {
		return (long)seconds*1000L;
	}
	@Override
	public void onActive(PlayerState s) {
		if(s.isOnServer() && ElementKeyMap.isValidType(blockType)) {
			int slot = s.getInventory()
					.incExistingOrNextFreeSlot((short)blockType, amount);
			s.sendInventoryModification(slot, Long.MIN_VALUE);
		}
	}
}
