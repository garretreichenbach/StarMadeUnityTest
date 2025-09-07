package org.schema.game.common.data.blockeffects.config.elements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.data.blockeffects.config.EffectException;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Vector3fModifier extends EffectModifier{
	
	
	

	private final Vector3f value = new Vector3f();

	public Vector3f getValue(){
		return value;
	}

	@Override
	public StatusEffectParameterType getType() {
		return StatusEffectParameterType.VECTOR3f;
	}

	@Override
	public void parseValue(Node node) {
		try{
			value.set(Vector3fTools.read(node.getTextContent().trim()));
		}catch(Exception e){
			throw new EffectException(e);
		}
	}
	@Override
	public void serialize(DataOutput d) throws IOException {
		Vector3fTools.serialize(value, d);
	}

	@Override
	public void deserialize(DataInput d) throws IOException {
		Vector3fTools.deserialize(value, d);		
	}
	@Override
	protected void writeValueToNode(Document doc, Element mod) {
		mod.setTextContent(Vector3fTools.toStringRaw(value));
	}

	@Override
	public long valueHash() {
		return value.hashCode();
	}
	public void set(Vector3f v) {
		value.set(v);
	}
}
