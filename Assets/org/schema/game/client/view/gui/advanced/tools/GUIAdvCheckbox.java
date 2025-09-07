package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUICheckBoxTextPairNew;
import org.schema.schine.input.InputState;

public class GUIAdvCheckbox extends GUIAdvTool<CheckboxResult>{
	
	private final GUICheckBoxTextPairNew chk;
	public GUIAdvCheckbox(InputState state, GUIElement dependent, final CheckboxResult r) {
		super(state, dependent, r);
		chk = new GUICheckBoxTextPairNew(getState(), new Object(){
			@Override
			public String toString(){return getRes().getName();}
			}, r.getFontSize()) {
			
			@Override
			public boolean isChecked() {
				return r.getCurrentValue();
			}
			
			@Override
			public void deactivate() {
					r.change(false);
			}
			
			@Override
			public void activate() {
					r.change(true);
			}
		};
		chk.activeInterface = GUIAdvCheckbox.this::isActive;
		attach(chk);
	}


	@Override
	public int getElementHeight() {
		return (int) chk.getHeight();
	}

}
