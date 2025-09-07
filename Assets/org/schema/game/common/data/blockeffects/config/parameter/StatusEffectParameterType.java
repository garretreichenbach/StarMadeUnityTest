package org.schema.game.common.data.blockeffects.config.parameter;

import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.blockeffects.config.EffectConfigElement;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;

public enum StatusEffectParameterType {
	INT((effectModule, type, valueType, list) -> {
		int v = 0;
		for(EffectConfigElement e : list){
			switch(e.stackType) {
				case ADD -> v += e.getIntValue();
				case MULT -> v *= e.getIntValue();
				case SET -> v = e.getIntValue();
				default -> throw new RuntimeException("Unknown StackType: " + e.stackType.name());
			}
		}
		effectModule.setIntValue(v);
	}),
	FLOAT((effectModule, type, valueType, list) -> {
		float v = 0;
		for(EffectConfigElement e : list){
			switch(e.stackType) {
				case ADD -> v += e.getFloatValue();
				case MULT -> v *= e.getFloatValue();
				case SET -> v = e.getFloatValue();
				default -> throw new RuntimeException("Unknown StackType: " + e.stackType.name());
			}
		}
		effectModule.setFloatValue(v);
	}),
	WEAPON_TYPE((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Illegal calc type");
	}),
	BOOLEAN((effectModule, type, valueType, list) -> {
		boolean v = false;
		for(EffectConfigElement e : list){
			switch(e.stackType) {
				case ADD -> v |= e.getBooleanValue();
				case MULT -> v &= e.getBooleanValue();
				case SET -> v = e.getBooleanValue();
				default -> throw new RuntimeException("Unknown StackType: " + e.stackType.name());
			}
		}
		effectModule.setBooleanValue(v);
	}),
	LONG((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	DOUBLE((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	SHORT((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	BYTE((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	VECTOR3f((effectModule, type, valueType, list) -> {
		Vector3f v = new Vector3f();
		for(EffectConfigElement e : list){
			switch(e.stackType) {
				case ADD -> v.add(e.getVector3fValue());
				case MULT -> {
					v.x *= e.getVector3fValue().x;
					v.y *= e.getVector3fValue().y;
					v.z *= e.getVector3fValue().z;
				}
				case SET -> v.set(e.getVector3fValue());
				default -> throw new RuntimeException("Unknown StackType: " + e.stackType.name());
			}
		}
		effectModule.setVector3fValue(v);
	}),
	VECTOR3i((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	VECTOR4f((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	VECTOR2f((effectModule, type, valueType, list) -> {
		throw new RuntimeException("Type not yet implemented");
	}),
	;
	
	public final StatusEffectParameterCalculator calculator;
	
	private StatusEffectParameterType(StatusEffectParameterCalculator calc){
		this.calculator = calc;
	}
}
