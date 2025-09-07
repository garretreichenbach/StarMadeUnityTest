package org.schema.game.client.view.gui.advanced.tools;


public abstract class TextBarResult  extends AdvResult<TextBarCallback>{
	
	
	public TextBarResult(){
	}
	@Override
	protected void initDefault() {
	}
	
	public abstract String onTextChanged(String text);
}
