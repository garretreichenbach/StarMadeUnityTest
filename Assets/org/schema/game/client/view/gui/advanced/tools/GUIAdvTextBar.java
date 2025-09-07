package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.graphicsengine.core.settings.PrefixNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.input.InputState;

public class GUIAdvTextBar extends GUIAdvTool<TextBarResult>{
	
	private final GUIActivatableTextBar chk;
	public GUIAdvTextBar(InputState state, GUIElement dependent, final TextBarResult r) {
		super(state, dependent, r);
		chk = new GUIActivatableTextBar(getState(), r.getFontSize(), dependent, new TextCallback() {
			
			@Override
			public void onTextEnter(String entry, boolean send, boolean onAutoComplete) {
				chk.deactivateBar();				
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
		}, t -> getRes().onTextChanged(t));
		attach(chk);
	}


	@Override
	public int getElementHeight() {
		return (int) chk.getHeight();
	}

	public String getText() {
		return chk.getText();
	}

	public void setText(String text) {
		chk.setText(text);
	}

	public void setInactiveText(String text) {
		chk.guiSearchInd.setTextSimple(text);
	}
}
