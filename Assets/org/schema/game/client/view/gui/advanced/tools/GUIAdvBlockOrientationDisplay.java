package org.schema.game.client.view.gui.advanced.tools;

import org.schema.game.client.view.gui.buildtools.GUIOrientationSettingElementNew;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.input.InputState;

public class GUIAdvBlockOrientationDisplay extends GUIAdvTool<BlockOrientationResult>{
private final GUIOrientationSettingElementNew blockPreview;

	
	public GUIAdvBlockOrientationDisplay(InputState state, GUIElement dependent, BlockOrientationResult r) {
		super(state, dependent, r);
		blockPreview = new GUIOrientationSettingElementNew(getState(), r);
		
		attach(blockPreview);
	}
	@Override
	public int getElementHeight() {
		return 64;
	}
	@Override
	public boolean isInsideForTooltip() {
		return super.isInsideForTooltip() && blockPreview.isMouseInsideBlock();
	}
	
}
