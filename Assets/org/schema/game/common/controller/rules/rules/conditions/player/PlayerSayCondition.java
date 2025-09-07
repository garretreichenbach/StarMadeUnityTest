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

public class PlayerSayCondition extends PlayerCondition{

	@RuleValue(tag = "ChatRegexp")
	public String reg = "";
	
	@RuleValue(tag = "ChannelName")
	public String channel = "general";
	
	
	
	
	public PlayerSayCondition() {
		super();
	}

	@Override
	public long getTrigger() {
		return TRIGGER_ON_PLAYER_CHAT;
	}

	@Override
	public ConditionTypes getType() {
		return ConditionTypes.PLAYER_SAY;
	}

	@Override
	protected boolean processCondition(short conditionIndex, RuleStateChange stateChange, PlayerState a, long trigger, boolean forceTrue) {
		if(forceTrue) {
			return true;
		}
		Pattern pat = Pattern.compile(reg);
		
			for(ChatChannel c : a.getPlayerChannelManager().getJoinedChannels()) {
				if(c.getName().toLowerCase(Locale.ENGLISH).equals(channel)) {
					int m = c.getMessageLog().size()-1;
					while(m >= 0) {
						ChatMessage chatMessage = c.getMessageLog().get(m);
						if(chatMessage.sender.toLowerCase(Locale.ENGLISH).equals(a.getName().toLowerCase(Locale.ENGLISH))) {
							Matcher matcher = pat.matcher(chatMessage.text);
							return matcher.matches();
						}
						m--;
					}
				}
			}
		return false;
	}
	
	@Override
	public String getDescriptionShort() {
		return Lng.str("Player chat in channel %s matches regexp \"%s\"", channel, reg);
	}
}
