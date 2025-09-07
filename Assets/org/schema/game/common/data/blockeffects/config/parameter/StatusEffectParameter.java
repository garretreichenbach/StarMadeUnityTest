package org.schema.game.common.data.blockeffects.config.parameter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.blockeffects.config.annotations.Stat;
import org.schema.game.common.data.blockeffects.config.elements.EffectModifier;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.input.InputState;

import java.lang.reflect.Field;
import java.util.List;

public abstract class StatusEffectParameter {
	
	public final StatusEffectParameterNames name;
	public final StatusEffectParameterType type;
	
	public StatusEffectParameter(StatusEffectParameterNames name, StatusEffectParameterType type){
		this.name = name;
		this.type = type;
	}
	
	public StatusEffectParameterNames getName(){
		return name;
	}
	public StatusEffectParameterType getType(){
		return type;
	}
	public abstract GUIElement createEditBar(InputState state, GUIElement dep);
	public long valueHash() {
		long l = 0;
		Field[] fields = getClass().getFields();
		for(Field f : fields){
			Stat anno = f.getAnnotation(Stat.class);
			if(anno != null){
				try {
					l+=anno.id().hashCode()*((EffectModifier)f.get(this)).valueHash();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} 
			}
		}
		return l;
	}
	public String calcName(){
		Field[] fields = getClass().getFields();
		for(Field f : fields){
			Stat anno = f.getAnnotation(Stat.class);
			if(anno != null){
				try {
					return anno.id();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} 
			}
		}
		throw new NullPointerException();
	}

	public abstract void apply(StatusEffectParameter to);

	public GUIElement getWeaponDropdown(InputState state) {
		assert(type == StatusEffectParameterType.INT);
		assert(this instanceof StatusEffectWeaponType);

		int selectedIndex = 0;
		List<GUIElement> ruleTypes = new ObjectArrayList<GUIElement>();

		int cc = 0;
		for(DamageDealerType t : DamageDealerType.values()) {
			GUITextOverlay l = new GUITextOverlay(FontLibrary.FontSize.MEDIUM_15, state);
			l.setTextSimple(t.getName());
			GUIAnchor c = new GUIAnchor(state, 300, 24);
			c.attach(l);
			l.setPos(3, 2, 0);
			ruleTypes.add(c);
			c.setUserPointer(t);

			if(t.ordinal() == ((StatusEffectWeaponType)this).getWeaponType()) {
				selectedIndex = cc;
			}
			cc++;
		}

		GUIDropDownList list = new GUIDropDownList(state, 300, 24, 100, element -> ((StatusEffectWeaponType)StatusEffectParameter.this).value.set(((DamageDealerType) element.getContent().getUserPointer()).ordinal()), ruleTypes);
		list.setSelectedIndex(selectedIndex);


		return list;
	}


}
