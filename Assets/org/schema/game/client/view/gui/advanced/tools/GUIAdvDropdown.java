package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIDropDownList;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.input.InputState;

public class GUIAdvDropdown extends GUIAdvTool<DropdownResult>{
	private GUIDropDownList dd;


	public GUIAdvDropdown(InputState state, GUIElement dependent, DropdownResult r) {
		super(state, dependent, r);
		
		
		dd = new GUIDropDownList(state, 30, r.getDropdownHeight(), r.getDropdownExpendedHeight(), element -> r.change(element.getContent().getUserPointer()), r.getDropdownElements(dependent));
		dd.dependend = dependent;
		dd.dependentWidthOffset = -4;
		dd.setPos(2, 0, 0);
		attach(dd);
	}
	

	@Override
	public void draw() {
		if(getRes().needsListUpdate()){
			refreshElements();
			getRes().flagListNeedsUpdate(false);
		}
		super.draw();
	}
	@Override
	public void drawToolTip(long time) {
		super.drawToolTip(time);
		dd.drawToolTip(time);
	}

	@Override
	public int getElementHeight() {
		return (int) dd.getHeight();
	}
	public void refreshElements(){
		dd.clear();
		for(GUIElement e : getRes().getDropdownElements(dd.dependend)){
			dd.add(new GUIListElement(e, e, getState()));
		}
		getRes().change(getRes().getDefault());
	}
}
