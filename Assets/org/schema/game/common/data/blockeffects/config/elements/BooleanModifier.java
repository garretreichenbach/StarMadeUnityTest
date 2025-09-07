package org.schema.game.common.data.blockeffects.config.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.data.blockeffects.config.EffectException;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BooleanModifier extends EffectModifier{

	private boolean value;

	public boolean getValue(){
		return value;
	}

	@Override
	public StatusEffectParameterType getType() {
		return StatusEffectParameterType.BOOLEAN;
	}

	@Override
	public void parseValue(Node node) {
		try{
			value = Boolean.parseBoolean(node.getTextContent().trim());
		}catch(NumberFormatException e){
			throw new EffectException(e);
		}
	}

	@Override
	public void serialize(DataOutput d) throws IOException {
		d.writeBoolean(value);
	}

	@Override
	public void deserialize(DataInput d) throws IOException {
		value = d.readBoolean();		
	}

	@Override
	protected void writeValueToNode(Document doc, Element mod) {
		mod.setTextContent(String.valueOf(value));
	}

	@Override
	public long valueHash() {
		return value ? 1231 : 214305;
	}
	public void set(boolean v) {
		value = v;
	}
}
