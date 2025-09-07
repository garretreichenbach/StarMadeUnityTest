package org.schema.game.client.view.gui.advanced.tools;

import java.util.Collection;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;

public abstract class DropdownResult  extends AdvResult<DropdownCallback>{
	
	private Object currentValue;
	public Object getCurrentValue(){
		return currentValue;
	}
	public void setCurrentValue(Object b){
		currentValue = b;
	}
	
	public DropdownResult(){
	}
	@Override
	protected void initDefault() {
		change(getDefault());
	}
	
	public void change(Object to){
		Object val = currentValue;
		
		currentValue = to;
		
		if(val != currentValue){
			if(callback != null){
				callback.onChanged(currentValue);
			}
		}
	}
	public abstract Object getDefault();
	@Override
	public void refresh(){
		change(getDefault());
	}
	
	public abstract Collection<? extends GUIElement> getDropdownElements(GUIElement dependent);
	
	public abstract boolean needsListUpdate();
	public abstract void flagListNeedsUpdate(boolean b);
	public int getDropdownExpendedHeight() {
		return 160;
	}
	public int getDropdownHeight() {
		return 24;
	}
}
