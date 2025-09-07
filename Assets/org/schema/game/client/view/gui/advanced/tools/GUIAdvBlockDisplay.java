package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.IconDatabase;
import org.schema.schine.input.InputState;

public class GUIAdvBlockDisplay extends GUIAdvTool<BlockDisplayResult>{

	
	
	private final GUIOverlay blockOverlay;
	
	public GUIAdvBlockDisplay(InputState state, GUIElement dependent, BlockDisplayResult res) {
		super(state, dependent, res);
		blockOverlay = new GUIOverlay(IconDatabase.getBuildIconsSprite(res.getCurrentBuildIconNum()), state){
			@Override
			public void draw() {
				setScale(getIconScale(), getIconScale(), 0);
				IconDatabase.getBuildIcons(this, GUIAdvBlockDisplay.this.getRes().getCurrentBuildIconNum());
				if(GUIAdvBlockDisplay.this.getRes().isBlockInit()){
					GUIAdvBlockDisplay.this.getRes().beforeBlockDraw(this);
					super.draw();
					GUIAdvBlockDisplay.this.getRes().afterBlockDraw(this);
				}
			}
		};
		
		
		attach(blockOverlay);
		
		getRes().afterInit(blockOverlay);
	}
	@Override
	public int getElementHeight() {
		return (int) (blockOverlay.getHeight() * getIconScale());
	}
	protected float getIconScale() {
		return getRes().getIconScale();
	}
}
