package org.schema.game.common.data.blockeffects.config;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.blockeffects.config.annotations.Stat;
import org.schema.game.common.data.blockeffects.config.elements.EffectModifier;
import org.schema.game.common.data.blockeffects.config.elements.ModifierStackType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectBooleanValue;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectFloatValue;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectIntValue;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameter;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectVector3fValue;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectWeaponType;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EffectConfigElement implements Comparable<EffectConfigElement>{
	private StatusEffectType type;
	public StatusEffectType getType(){
		return type;
	}
	
	public StatusEffectParameter value;
	public StatusEffectParameter weaponType;
	public ModifierStackType stackType = ModifierStackType.SET;
	public byte priority;
	
	
	public EffectConfigElement(){
	}
	public EffectConfigElement(EffectConfigElement e){
		this.type = e.type;
		this.stackType = e.stackType;
		this.priority = e.priority;
		
		init(type);
		
		duplicateValue(e.value, this.value);
		duplicateValue(e.weaponType, this.weaponType);
	}
	private void duplicateValue(StatusEffectParameter from, StatusEffectParameter to) {
		if(from == null || to == null){
			return;
		}else{
			to.apply(from);
		}
	}
	public void init(StatusEffectType type){
		this.type = type;
		for( Class<? extends StatusEffectParameter> a : this.type.effectParameters){
			
			StatusEffectParameter inst;
			try {
				inst = a.newInstance();//need new instance here
				switch(inst.name) {
					case VALUE -> value = inst;
					case WEAPON_TYPE -> weaponType = inst;
					default -> throw new EffectException("Unknown name: " + inst.name.name());
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	public String toString() {
		return "EffectConfigElement(type: "+type+"; val: "+value+"; wep: "+weaponType+")";
	}
	public void readConfig(Node node) throws IllegalArgumentException, IllegalAccessException {
		readConfig(value, node);
		readConfig(weaponType, node);
		parseStack(node);
	}
	private void readConfig(StatusEffectParameter inst, Node node) throws IllegalArgumentException, IllegalAccessException {
		if(inst == null){
			return;
		}
		NodeList childNodesP = node.getChildNodes();
		boolean found = false;
		for(int j = 0; j < childNodesP.getLength(); j++){
			Node itemP = childNodesP.item(j);
			if(itemP.getNodeType() == Node.ELEMENT_NODE){
				
				Node namedItem = itemP.getAttributes().getNamedItem("name");
				
				if(namedItem != null && namedItem.getNodeValue().toLowerCase(Locale.ENGLISH).equals(inst.name.name().toLowerCase(Locale.ENGLISH))){
					
					
					NodeList childNodes = itemP.getChildNodes();
					for(int i = 0; i < childNodes.getLength(); i++){
						Node item = childNodes.item(i);
						if(item.getNodeType() == Node.ELEMENT_NODE){
							Field[] fields = inst.getClass().getFields();
							for(Field f : fields){
								f.setAccessible(true);
								Stat annotation = f.getAnnotation(Stat.class);
								if(annotation != null){
									if(item.getNodeName().toLowerCase(Locale.ENGLISH).equals(annotation.id().toLowerCase(Locale.ENGLISH))){
										found = true;
										((EffectModifier)f.get(inst)).parse(itemP);
									}
								}
							}
						}
					}
				}
			}
		}
		if(!found){
			throw new EffectException("Not found for "+inst.name.name()+":"+inst.type.name()+"  "+node.getParentNode().getNodeName()+"->"+node.getNodeName());
		}
	}
	public void checkValue(StatusEffectParameterType type){
		if(value == null){
			throw new EffectException("Not a value ");
		}else if(value.type != type){
			throw new EffectException("Invalid Type: "+type.name()+" != "+value.type.name());
		}
	}
	
	public float getFloatValue(){
		checkValue(StatusEffectParameterType.FLOAT);
		return ((StatusEffectFloatValue)value).getValue();
	}
	public int getIntValue(){
		checkValue(StatusEffectParameterType.INT);
		return ((StatusEffectIntValue)value).getValue();
	}
	public boolean getBooleanValue(){
		checkValue(StatusEffectParameterType.BOOLEAN);
		return ((StatusEffectBooleanValue)value).getValue();
	}
	public Vector3f getVector3fValue(){
		checkValue(StatusEffectParameterType.VECTOR3f);
		return ((StatusEffectVector3fValue)value).getValue();
	}
	public DamageDealerType getWeaponType(){
		if(!(weaponType instanceof StatusEffectWeaponType) ){
			throw new EffectException("Not a weapon type");
		}
		return DamageDealerType.values()[((StatusEffectWeaponType)weaponType).getWeaponType()];
//		return DamageDealerType.values()[((StatusEffectIntValue)weaponType).getValue()];
	}
	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeByte((byte)stackType.ordinal());
		buffer.writeByte(priority);
		serialize(value, buffer);
		serialize(weaponType, buffer);
	}
	public void deserialize(DataInput buffer) throws IOException {
		stackType = ModifierStackType.values()[buffer.readByte()];
		priority = buffer.readByte();
//		System.err.println("[Config] Stack: "+stackType.name()+"; prio: "+priority+"; value "+value+"; weaponType: "+weaponType);
		deserialize(value, buffer);
		deserialize(weaponType, buffer);
	}
	private void deserialize(StatusEffectParameter param, DataInput buffer) throws IOException {
		if(param == null){
			return;
		}
		
		Field[] fields = param.getClass().getFields();
		for(Field f : fields){
			Stat anno = f.getAnnotation(Stat.class);
			if(anno != null){
				try {
					((EffectModifier)f.get(param)).deserialize(buffer);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
	public void serialize(StatusEffectParameter param, DataOutput buffer) throws IOException {
		if(param == null){
			return;
		}
		
		Field[] fields = param.getClass().getFields();
		for(Field f : fields){
			Stat anno = f.getAnnotation(Stat.class);
			if(anno != null){
				try {
					((EffectModifier)f.get(param)).serialize(buffer);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	public Node write(Document doc) {
		Element elem = doc.createElement("E");
		addNode(elem, doc, value);
		addNode(elem, doc, weaponType);
		elem.setAttribute("type", type.name().toLowerCase(Locale.ENGLISH));
		elem.setAttribute("modtype", stackType.name().toLowerCase(Locale.ENGLISH));
		elem.setAttribute("priority", String.valueOf(priority));
		return elem;
	}
	private void addNode(Element parent, Document doc, StatusEffectParameter v) {
		if(v == null){
			return;
		}
		Element elem = doc.createElement("P");
		elem.setAttribute("name", v.name.name().toLowerCase(Locale.ENGLISH));
		elem.setAttribute("type", v.type.name().toLowerCase(Locale.ENGLISH));
		Field[] fields = v.getClass().getFields();
		for(Field f : fields){
			System.err.println("FIELD::: "+v.getClass().getSimpleName()+" -> "+f.getName()+"; "+f.getAnnotation(Stat.class));
			Stat anno = f.getAnnotation(Stat.class);
			if(anno != null){
				try {
					Element mod = doc.createElement(anno.id());
					EffectModifier em = ((EffectModifier)f.get(v));
					em.writeToNode(doc, mod);
					elem.appendChild(mod);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} 
			}
		}
		
		
		
		parent.appendChild(elem);
	}
	private void parseStack(Node node){
		try{
			Node namedItem = node.getAttributes().getNamedItem("modtype");
			if(namedItem == null){
				throw new IllegalArgumentException("No 'type' attribute found");
			}
			stackType = ModifierStackType.valueOf(namedItem.getNodeValue().toUpperCase(Locale.ENGLISH));
			if(stackType == null){
				throw new IllegalArgumentException("Unknown type: "+namedItem.getNodeValue()+"; Must be "+Arrays.toString(ModifierStackType.values()));
			}
		}catch(Exception e){
			throw new EffectException(node.getParentNode().getNodeName()+"->"+node.getNodeName(), e);
		}
		try{
			Node namedItem = node.getAttributes().getNamedItem("priority");
			if(namedItem == null){
				throw new IllegalArgumentException("No 'priority' attribute found");
			}
			priority = Byte.parseByte(namedItem.getNodeValue());
		}catch(Exception e){
			throw new EffectException(node.getParentNode().getNodeName()+"->"+node.getNodeName(), e);
		}
	}
	public long addHash() {
		long s = (type.ordinal()+1) * 234234L;
		s += addHash(value)* 938283L;
		s += addHash(weaponType)* 6924L;
		s += priority * 62222924L;
		return s;
	}
	private long addHash(StatusEffectParameter v) {
		if(v != null){
			return ((v.name.ordinal()+v.type.ordinal()*100008829L+v.valueHash()));
		}
		return 0;
	}
	public String getParamString(StatusEffectParameter v) {
		if(v == null){
			return "-";
		}else{
			return v.toString();
		}
	}
	public GUIElement createPriorityBar(InputState state, GUIElement dep) {
		GUIActivatableTextBar t = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, 10, 1, "prio", dep, new TextCallback() {
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				
			}
			
			@Override
			public void onFailedTextCheck(String msg) {
				
			}
			
			@Override
			public void newLine() {
			}
			
			@Override
			public String handleAutoComplete(String s, TextCallback callback, String prefix) throws PrefixNotFoundException {
				return null;
			}
			
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t1 -> t1){

			@Override
			protected void onBecomingInactive() {
				
				String t = getText();
				long v = 0;
				while(t.length() > 0){
					try{
						v = Long.parseLong(t);
						v = Math.max(-127, Math.min(v, 127));
						priority = (byte) v;
						setTextWithoutCallback(String.valueOf(priority));
						return;
					}catch(NumberFormatException e){
						t = t.substring(0, t.length()-1);
					}
				}
				setTextWithoutCallback(String.valueOf(priority));
			}
			
		};
		t.setDeleteOnEnter(false);
		t.setTextWithoutCallback(String.valueOf(priority));
		return t;
	}
	@Override
	public int compareTo(EffectConfigElement o) {
		return priority - o.priority;
	}
	public String getEffectDescription() {
		StringBuffer b = new StringBuffer();
		if(value != null){
			switch(value.type){
			case BOOLEAN:
				b.append(stackType.getVerbBool(getBooleanValue(), type.getFullName()));
				break;
			case BYTE:
				break;
			case DOUBLE:
				break;
			case FLOAT:
				b.append(stackType.getVerbFloat(getFloatValue(), type.getFullName(), type.isPercentage(), type.isTimed(), type.respectOnePointZero()));
				break;
			case INT:
				break;
			case LONG:
//				b.append(stackType.getVerb(String.valueOf(getLongValue()), type.getFullName()));
				break;
			case SHORT:
//				b.append(stackType.getVerb(String.valueOf(getShortValue()), type.getFullName()));
				break;
			case VECTOR2f:
				break;
			case VECTOR3f:
				b.append(stackType.getVerb(String.valueOf(getVector3fValue()), type.getFullName()));
				break;
			case VECTOR3i:
//				b.append(stackType.getVerb(String.valueOf(getVector3iValue()), type.getFullName()));
				break;
			case VECTOR4f:
//				b.append(stackType.getVerb(String.valueOf(getVector4fValue()), type.getFullName()));
				break;
			case WEAPON_TYPE:
				throw new RuntimeException("Illegal value");
			default:
				break;
			
			}
		}
		if(weaponType != null){
			if(weaponType.type != StatusEffectParameterType.WEAPON_TYPE){
				throw new RuntimeException("Illegal type "+weaponType.type);
			}
			switch(getWeaponType()) {
				case BEAM -> b.append(Lng.str("for beam damage"));
				case EXPLOSIVE -> b.append(Lng.str("for explosive damage"));
				case GENERAL -> b.append(Lng.str("for general damage"));
				case MISSILE -> b.append(Lng.str("for missile damage"));
				case PROJECTILE -> b.append(Lng.str("for projectile damage"));
				case PULSE -> b.append(Lng.str("for pulse damage"));
				default -> b.append(Lng.str("for unknown damage"));
			}
		}
		return b.toString();
	}
	
}
