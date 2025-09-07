package org.schema.game.common.data.blockeffects.config.parameter;

import org.schema.game.common.data.blockeffects.config.annotations.Stat;
import org.schema.game.common.data.blockeffects.config.elements.FloatModifier;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;

public class StatusEffectFloatValue extends StatusEffectParameter{

	@Stat(id="value")
	public FloatModifier value = new FloatModifier();
	
	public StatusEffectFloatValue() {
		super(StatusEffectParameterNames.VALUE, StatusEffectParameterType.FLOAT);
	}
	
	
	public float getValue(){
		return value.getValue();
	}
	@Override
	public String toString() {
		return "Float";
	}
	@Override
	public void apply(StatusEffectParameter v) {
		this.value.set(((StatusEffectFloatValue)v).value.getValue());
	}

	@Override
	public GUIElement createEditBar(InputState state, GUIElement dep) {
		GUIActivatableTextBar t = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, 10, 1, "setting", dep, new TextCallback() {
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
				float v = 0;
				while(t.length() > 0){
					try{
						v = Float.parseFloat(t);
						value.set(v);
						setTextWithoutCallback(String.valueOf(value.getValue()));
						return;
					}catch(NumberFormatException e){
						t = t.substring(0, t.length()-1);
					}
				}
				setTextWithoutCallback(String.valueOf(value.getValue()));
			}
			
		};
		t.setDeleteOnEnter(false);
		t.setTextWithoutCallback(String.valueOf(value.getValue()));
		return t;
	}
}
