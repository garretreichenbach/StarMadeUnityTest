package org.schema.game.client.view.gui.advanced.tools;


public abstract class StatLabelResult extends AdvResult<AdvCallback>{

	@Override
	public AdvCallback initCallback() {
		return null;
	}
	@Override
	protected void initDefault() {
	}
	@Override
	public String getToolTipText() {
		return getValue();
	}
	
	@Override
	public long getToolTipDelayMs() {
		return 300;
	}
	public abstract String getValue();
	public abstract int getStatDistance();
	
	
}
