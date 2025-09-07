package org.schema.game.common.data.blockeffects.config.parameter;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.data.blockeffects.config.annotations.Stat;
import org.schema.game.common.data.blockeffects.config.elements.Vector3fModifier;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;

public class StatusEffectVector3fValue extends StatusEffectParameter{

	@Stat(id="value")
	public Vector3fModifier value = new Vector3fModifier();
	
	public StatusEffectVector3fValue() {
		super(StatusEffectParameterNames.VALUE, StatusEffectParameterType.VECTOR3f);
	}
	
	
	public Vector3f getValue(){
		return value.getValue();
	}
	@Override
	public String toString() {
		return "Vec3f";
	}
	@Override
	public void apply(StatusEffectParameter v) {
		this.value.set(((StatusEffectVector3fValue)v).value.getValue());
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
				Vector3f v;
				try{
					v = Vector3fTools.read(t);
					value.set(v);
					setTextWithoutCallback(t.trim());
				}catch(Exception e){
					setTextWithoutCallback(Vector3fTools.toStringRaw(value.getValue()));
				}
				
			}
			
		};
		t.setDeleteOnEnter(false);
		t.setTextWithoutCallback(String.valueOf(value.getValue()));
		return t;
	}
}
