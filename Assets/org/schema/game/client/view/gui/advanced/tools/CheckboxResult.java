package org.schema.game.client.view.gui.advanced.tools;


public abstract class CheckboxResult  extends AdvResult<CheckboxCallback>{
	
	public abstract boolean getCurrentValue();
	public abstract void setCurrentValue(boolean b);
	
	public CheckboxResult(){
	}
	@Override
	protected void initDefault() {
		change(getDefault());
	}
	
	public void switchIt(){
		change(!getCurrentValue());
	}
	public void change(boolean to){
		boolean val = getCurrentValue();
		
		setCurrentValue(to);
		
		if(val != getCurrentValue()){
			if(callback != null){
				callback.onValueChanged(getCurrentValue());
			}
		}
	}
	public abstract boolean getDefault();
	@Override
	public void refresh(){
		change(getDefault());
	}
	@Override
	public int getInsetLeft() {
		return 4;
	}
	
}
