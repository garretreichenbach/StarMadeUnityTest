package org.schema.game.server.data.simulation.npc.diplomacy;

import org.schema.common.util.StringTools;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class DiplomacyAction {
	public enum DiplActionType {
		ATTACK(en -> {
			return Lng.str("Attacked them");
		}),
		ATTACK_ENEMY(en -> {
			return Lng.str("Attacked their enemies");
		}),
		MINING(en -> {
			return Lng.str("Mined their resources");
		}),
		TERRITORY(en -> {
			return Lng.str("Violated their territory");
		}),
		PEACE_OFFER(en -> {
			return Lng.str("Peace offering made");
		}),
		DECLARATION_OF_WAR(en -> {
			return Lng.str("Declared war");
		}),
		ALLIANCE_REQUEST(en -> {
			return Lng.str("Requested Alliance");
		}),
		ALLIANCE_CANCEL(en -> {
			return Lng.str("Canceled Alliance");
		}),
		TRADING_WITH_US(en -> {
			return Lng.str("Traded with them");
		}),
		TRADING_WITH_ENEMY(en -> {
			return Lng.str("Traded with their enemies");
		}),
		ALLIANCE_WITH_ENEMY(en -> {
			return Lng.str("In alliance with their enemies");
		}),;
		private Translatable description;

		private DiplActionType(Translatable description){
			this.description = description;
		}

		public String getDescription() {
			return description.getName(this);
		}

		public static String list() {
			return StringTools.listEnum(DiplActionType.values());
		}
	
	}
	
	public DiplActionType type;
	
	public int counter;
	
	public long timeDuration;

	public Tag toTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, (byte)0),
			new Tag(Type.INT, null, counter),
			new Tag(Type.LONG, null, timeDuration),
			new Tag(Type.BYTE, null, (byte)type.ordinal()),
			FinishTag.INST,
		});
	}
	
	public void fromTag(Tag tag){
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
		
		counter = t[1].getInt();
		timeDuration = t[2].getLong();
		type = DiplActionType.values()[t[3].getByte()];
	}
	
}
