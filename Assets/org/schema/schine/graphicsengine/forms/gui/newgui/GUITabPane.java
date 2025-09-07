package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.input.InputState;

public class GUITabPane extends GUIHorizontalTabs {

	private GUITabInterface p;

	public GUITabPane(InputState state, GUITabInterface p) {
		super(state);
		this.p = p;
	}




	@Override
	public boolean isOccluded() {
		return !p.isActive();
	}

	@Override
	public void setTabTextColor(int tab, boolean selected) {
		p.getTabs().get(tab).getTabNameText().setColor(selected ? p.getTabs().get(tab).getTextColorSelected() : p.getTabs().get(tab).getTextColorUnselected());		
	}

	@Override
	public int getSelectedTab() {
		return p.getSelectedTab();
	}

	@Override
	public int getTabCount() {
		return p.getTabs().size();
	}



	@Override
	protected int getTabTextWidth(int i) {
		return p.getTabs().get(i).getTextWidth();
	}



	@Override
	protected int getInnerWidthTab() {
		return p.getInnerWidthTab();
	}



	@Override
	protected void setTabTextPos(int i, int x, int y) {
		p.getTabs().get(i).getTabNameText().setPos(x, y, 0);		
	}

	@Override
	protected GUIAnchor getTabAnchor(int i) {
		return p.getTabs().get(i).tabAnchor;
	}

	@Override
	protected void drawTabText(int i) {
		p.getTabs().get(i).getTabNameText().draw();		
	}

	@Override
	protected int getTabContentWidth() {
		return (int) p.getWidth();
	}

	@Override
	protected void onLeftMouse(int index) {
		if(p.getTabs().get(index).tabAnchor.isActive()){
			p.setSelectedTab(index);
		}		
	}

}
