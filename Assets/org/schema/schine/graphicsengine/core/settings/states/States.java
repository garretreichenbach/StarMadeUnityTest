package org.schema.schine.graphicsengine.core.settings.states;

import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.SettingsInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;
import org.schema.schine.resource.tag.Tag;

public abstract class States<E extends Object> {
	public abstract boolean contains(E state);

	public abstract E getFromString(String arg) throws StateParameterNotFoundException;

	public abstract String getType();

	public abstract E next() throws StateParameterNotFoundException;

	public abstract E previous() throws StateParameterNotFoundException;

	public abstract Tag toTag();

	public abstract E readTag(Tag tag);

	public abstract E getCurrentState();

	public abstract void setCurrentState(E state);
	
	
	public GUIElement getGUIElement(InputState state, GUIElement dependent, String emptyString, final SettingsInterface stateSetting) {
		
		GUIActivatableTextBar t = new GUIActivatableTextBar(state, FontSize.MEDIUM_15, emptyString, dependent, new TextCallback() {
			
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
			public String handleAutoComplete(String s, TextCallback callback,
					String prefix) throws PrefixNotFoundException {
				return null;
			}
			
			@Override
			public String[] getCommandPrefixes() {
				return null;
			}
		}, t1 -> {
			try {
				setCurrentState(getFromString(t1));
				stateSetting.setObject(getCurrentState());
			} catch (StateParameterNotFoundException e) {
			}
			return t1;
		}){

			@Override
			protected void onBecomingInactive() {
				//revert to last valid state
				setText(getCurrentState().toString());
			}
			
		};
		t.setDeleteOnEnter(false);
		t.setText(stateSetting.getString());
		return t;
	}
	
}
