package org.schema.game.common.controller.rules;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.schine.network.TopLevelType;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

public class RuleProperty implements TagSerializable, SerializationInterface{

	
	public static final byte ALL_SUBTYPES = -1;

	public byte VERSION = 0;
	
	public boolean global;
	public byte subType = ALL_SUBTYPES; 
	public RuleSet ruleSet;
	public String receivedRuleSetUID;
	
	public RuleProperty() {
		
	}
	public RuleProperty(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}
	
	public List<Enum> getAvailableSubtypes(RuleSet set, List<Enum> out) {
		TopLevelType entityType = set.getEntityType();
		out.clear();
		for(EntityType t : EntityType.values()) {
			if(t.used && t.topLevelType == entityType) {
				out.add(t);
			}
		}
		return out;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(ruleSet.getUniqueIdentifier());
		b.writeBoolean(global);
		b.writeByte(subType);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		this.receivedRuleSetUID = b.readUTF();
		this.global = b.readBoolean();
		this.subType = b.readByte();
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] s = tag.getStruct();
		byte version = s[0].getByte();
		this.receivedRuleSetUID = s[1].getString();
		this.global = s[2].getByte() == 1;
		this.subType = s[3].getByte();
	}

	@Override
	public Tag toTagStructure() {
		return new Tag(Type.STRUCT, null, new Tag[] {
			new Tag(Type.BYTE, null, VERSION),
			new Tag(Type.STRING, null, ruleSet.uniqueIdentifier),
			new Tag(Type.BYTE, null, global ? (byte)1 : (byte)0),
			new Tag(Type.BYTE, null, subType),
			FinishTag.INST,
		});
	}

}
