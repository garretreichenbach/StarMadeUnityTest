package org.schema.game.common.data.blockeffects.config.parameter;

import org.schema.game.common.data.blockeffects.config.annotations.Stat;
import org.schema.game.common.data.blockeffects.config.elements.BooleanModifier;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUICheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class StatusEffectBooleanValue extends StatusEffectParameter{

	@Stat(id="value")
	public BooleanModifier value = new BooleanModifier();
	
	public StatusEffectBooleanValue() {
		super(StatusEffectParameterNames.VALUE, StatusEffectParameterType.BOOLEAN);
	}
	@Override
	public void apply(StatusEffectParameter v) {
		this.value.set(((StatusEffectBooleanValue)v).value.getValue());
	}
	
	public boolean getValue(){
		return value.getValue();
	}
	@Override
	public String toString() {
		return "Boolean";
	}
	@Override
	public GUIElement createEditBar(InputState state, GUIElement dep) {
//		GUITextOverlay trueText = new GUITextOverlay(70, 24, state);
//		trueText.setTextSimple(Lng.str("true"));
//		GUITextOverlay falseText = new GUITextOverlay(70, 24, state);
//		falseText.setTextSimple(Lng.str("false"));
//		
//		GUIAncor trueAnc = new GUIAncor(state, 70, 24);
//		trueAnc.attach(trueText);
//		GUIAncor falseAnc = new GUIAncor(state, 70, 24);
//		trueAnc.attach(trueText);
		
		GUICheckBox cb = new GUICheckBox(state) {
			
			@Override
			protected boolean isActivated() {
				return value.getValue();
			}
			
			@Override
			protected void deactivate() throws StateParameterNotFoundException {
				value.set(false);
			}
			
			@Override
			protected void activate() throws StateParameterNotFoundException {
				value.set(true);				
			}
		};
		return cb;
	}


	
}
