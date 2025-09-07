package org.schema.game.server.data.simulation.npc.diplomacy;

import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity.DiplStatusType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class NPCDiplStaticModifier extends NPCDiplModifier{

	public int value;
	public long elapsedTimeInactive;
	public DiplStatusType type;
	public long totalTimeApplied;
	
	public NPCDiplStaticModifier() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Tag toTag() {
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, (byte)0),
			new Tag(Type.INT, null, value),
			new Tag(Type.LONG, null, elapsedTimeInactive),
			new Tag(Type.BYTE, null, (byte)type.ordinal()),
			FinishTag.INST,
		});
	}
	
	public void fromTag(Tag tag){
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
		
		value = t[1].getInt();
		elapsedTimeInactive = t[2].getLong();
		type = DiplStatusType.values()[t[3].getByte()];
	}

	@Override
	public String getName() {
		return type.getDescription();
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public int getValue() {
		return value;
	}

}
