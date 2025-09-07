package org.schema.game.common.data.blockeffects.config.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class EffectModifier {
	
	
	public EffectModifier() {
		super();
	}

	public abstract StatusEffectParameterType getType();
	
	public abstract void parseValue(Node node);
	public void parse(Node node){
		parseValue(node);
	}
	
	public void writeToNode(Document doc, Element mod) {
		writeValueToNode(doc, mod);
	}
	protected abstract void writeValueToNode(Document doc, Element mod);

	public abstract void serialize(DataOutput d) throws IOException;
	public abstract void deserialize(DataInput d) throws IOException;

	public abstract long valueHash();

	
}
