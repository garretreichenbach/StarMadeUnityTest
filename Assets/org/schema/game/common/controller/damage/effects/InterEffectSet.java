package org.schema.game.common.controller.damage.effects;

import api.listener.fastevents.ApplyAddConfigEventListener;
import api.listener.fastevents.FastListenerCommon;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.config.ConfigParserException;
import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.blockeffects.config.ConfigEntityManager;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementInformation;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Locale;

public class InterEffectSet {
	public static final int length = InterEffectType.values().length;
	private float[] strength = new float[length];
	
	public InterEffectSet() {
		
	}
	public InterEffectSet(InterEffectSet from) {
		setEffect(from);
	}

	public float getStrength(InterEffectType t){
		return strength[t.ordinal()];
	}
	
	public void reset(){
		Arrays.fill(strength, 0f);
	}
	
	public void setEffect(final InterEffectSet s){
		for(int i = 0; i < length; i++){
			strength[i] = s.strength[i];
		}
	}
	public void setStrength(final InterEffectType t, float val){
		strength[t.ordinal()] = val;
	}
	public void scaleAdd(final InterEffectSet s, final float v) {
		for(int i = 0; i < length; i++){
			strength[i] += s.strength[i] * v;
		}
	}
	public boolean hasEffect(InterEffectType t){
		return getStrength(t) > 0f;
	}

	public void parseXML(Node r) throws ConfigParserException {
		ObjectOpenHashSet<InterEffectType> set = new ObjectOpenHashSet<InterEffectType>();
		InterEffectType[] types = InterEffectType.values();
		NodeList childs = r.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++) {
			Node item = childs.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				String nm = item.getNodeName().toLowerCase(Locale.ENGLISH);
				boolean found = false;
				for(InterEffectType t : types ) {
					if(t.id.toLowerCase(Locale.ENGLISH).equals(nm)) {
						try {
							strength[t.ordinal()] = Float.parseFloat(item.getTextContent());
							
						}catch(NumberFormatException f) {
							throw new ConfigParserException(item.getParentNode().getParentNode().getNodeName()+"->"+item.getParentNode().getNodeName()+"->"+item.getNodeName()+" must be floating point value", f);
						}
						found = true;
						set.add(t);
						break;
					}
				}
				if(!found) {
					throw new ConfigParserException(item.getParentNode().getParentNode().getNodeName()+"->"+item.getParentNode().getNodeName()+"->"+item.getNodeName()+" no effect found for '"+nm+"'; must be one of: "+Arrays.toString(types));
				}
			}
		}
		if(set.size() < types.length) {
			throw new ConfigParserException(r.getParentNode().getNodeName()+"->"+r.getNodeName()+" missing effects element! set: "+set+";. Must contain all of "+Arrays.toString(types));
		}
	}
	public boolean isZero() {
		for(float s : strength) {
			if(s != 0) {
				return false;
			}
		}
		return true;
	}
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		InterEffectType[] types = InterEffectType.values();
		b.append("EFFECT[");
		for(int i = 0; i < length; i++) {
			b.append("(");
			b.append(types[i].id);
			b.append(" = ");
			b.append(strength[i]);
			b.append(")");
			if(i < length-1) {
				b.append(", ");
			}
		}
		b.append("]");
		return b.toString();
	}
	public void applyAddEffectConfig(ConfigEntityManager c, StatusEffectType t, InterEffectType em) {
		int index = em.ordinal();
		//INSERTED CODE
		//by Ithirahad
		//ORIGINAL: float result = c.apply(t, this.strength[index]);
		float result = c.getAsDefense(t);
		//BUG FIX: Strength always started at zero, and the apply function multiplies the second argument by the chamber value,
		//so the resulting resistance would always be zero.
		//because of how the damage formula works, multiplying by 1 instead should give the expected behavior. (change this if the damage formula gets modified)
		for (ApplyAddConfigEventListener listener : FastListenerCommon.applyAddEffectConfigEventListeners) {
			result = listener.onApplyEffectDefense(c, t, em, result, this.strength);
		}
		this.strength[index] = result;
		///
	}
	public void mul(InterEffectSet o) {
		for(int i = 0; i < length; i++) {
			strength[i] *= o.strength[i];
		}
	}
	public void add(InterEffectSet o) {
		for(int i = 0; i < length; i++) {
			strength[i] += o.strength[i];
		}
	}
	public void setDefenseFromInfo(ElementInformation info) {
		setAdd(info.effectArmor, info.isArmor() ? VoidElementManager.armorEffectConfiguration : VoidElementManager.basicEffectConfiguration);
	}
	public void setAdd(InterEffectSet a, InterEffectSet b) {
		setEffect(a);
		add(b);
	}

}
