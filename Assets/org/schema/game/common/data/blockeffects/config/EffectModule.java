package org.schema.game.common.data.blockeffects.config;

import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameter;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterNames;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class EffectModule {

	
	private float floatValue;
	private int intValue;
	private boolean booleanValue;
	private Vector3f vector3fValue;
	
	private final Object2ObjectMap<DamageDealerType, EffectModule> weaponType = new Object2ObjectOpenHashMap<DamageDealerType, EffectModule>();
	private StatusEffectType type;
	private StatusEffectParameterType valueType;
	private EffectModule parent;
	private DamageDealerType ownWeaponType;

	public void create(StatusEffectType key, List<EffectConfigElement> list) {
		this.type = key; 
		assert(type != null);
		for( Class<? extends StatusEffectParameter> a : key.effectParameters){
			StatusEffectParameter inst = key.getInstance(a);;
			switch(inst.name){
			case VALUE:
				break;
			case WEAPON_TYPE:
				final Object2ObjectMap<DamageDealerType, List<EffectConfigElement>> cc = new Object2ObjectOpenHashMap<DamageDealerType, List<EffectConfigElement>>();

				for(EffectConfigElement e : list){
					List<EffectConfigElement> l = cc.get(e.getWeaponType());
					if(l == null){
						l = new ObjectArrayList<EffectConfigElement>();
						cc.put(e.getWeaponType(), l);
					}
					l.add(e);
				}
				
				//subsort into weapon types
				for(Entry<DamageDealerType, List<EffectConfigElement>> e : cc.entrySet()){
					EffectModule m = new EffectModule();
					m.setParent(this, e.getKey());
					Collections.sort(e.getValue());
					m.calculateValue(key, e.getValue());
					weaponType.put(e.getKey(), m);
				}
				//return as there is nothing else to do
				return;
			default:
				throw new EffectException("Unknown name: "+inst.name.name());
			}
		}
		
		calculateValue(key, list);
		
	}


	private void calculateValue(StatusEffectType type, List<EffectConfigElement> list) {
		for( Class<? extends StatusEffectParameter> a : type.effectParameters){
			StatusEffectParameter inst = type.getInstance(a);
			if(inst.name == StatusEffectParameterNames.VALUE){
				calculateValue(inst.type, list);
			}
		}
	}


	private void calculateValue(StatusEffectParameterType type, List<EffectConfigElement> list) {
		this.valueType = type;
		type.calculator.calculate(this, this.type, this.valueType, list);

//		if(parent != null) {
//			assert(false):valueType+"; "+type+"; "+list;
//		}

	}

	private void checkValueType(StatusEffectParameterType p){
		if(valueType != p){
			throw new IllegalArgumentException("Illegal value type. Expected "+p.name()+", but got "+valueType.name());
		}
	}
	public void setIntValue(int v) {
		checkValueType(StatusEffectParameterType.INT);
		intValue = v;
	}


	public void setFloatValue(float v) {
		checkValueType(StatusEffectParameterType.FLOAT);
		floatValue = v;
	}


	public void setBooleanValue(boolean v) {
		checkValueType(StatusEffectParameterType.BOOLEAN);
		booleanValue = v;
	}


	public void setVector3fValue(Vector3f v) {
		checkValueType(StatusEffectParameterType.VECTOR3f);
		vector3fValue = v;
	}


	public float getFloatValue() {
		checkValueType(StatusEffectParameterType.FLOAT);
		return floatValue;
	}


	public int getIntValue() {
		checkValueType(StatusEffectParameterType.INT);
		return intValue;
	}


	public boolean getBooleanValue() {
		checkValueType(StatusEffectParameterType.BOOLEAN);
		return booleanValue;
	}


	public Vector3f getVector3fValue() {
		checkValueType(StatusEffectParameterType.VECTOR3f);
		return vector3fValue;
	}


	public Object2ObjectMap<DamageDealerType, EffectModule> getWeaponType() {
		return weaponType;
	}


	public StatusEffectType getType() {

		return type != null ? type : parent.type ;
	}


	public StatusEffectParameterType getValueType() {
		return valueType;
	}


	public String getValueString() {
		switch(valueType){
		case BOOLEAN:
			return String.valueOf(getBooleanValue());
		case BYTE:
			break;
		case DOUBLE:
			break;
		case FLOAT:
			return String.valueOf(getFloatValue());
		case INT:
			return String.valueOf(getIntValue());
		case LONG:
			break;
		case SHORT:
			break;
		case VECTOR2f:
			break;
		case VECTOR3f:
			return String.valueOf(getVector3fValue());
		case VECTOR3i:
			break;
		case VECTOR4f:
			break;
		case WEAPON_TYPE:
			break;
		default:
			break;
		
		}
		return "UNDEF";
	}


	@Override
	public String toString() {
		return "EffectModule["+type.name()+" "+type.getName()+" -> "+getValueString()+"]";
	}


	public EffectModule getParent() {
		return parent;
	}


	public void setParent(EffectModule parent, DamageDealerType wepType) {
		this.parent = parent;
		this.ownWeaponType = wepType;
	}


	public String getOwnWeaponTypeString() {
		assert(parent != null);
		assert(ownWeaponType != null);
		return ownWeaponType.getName();
	}

}
