package org.schema.game.client.controller.manager.ingame;

import org.schema.schine.input.KeyEventInterface;

public class BuildSelectionCopy extends BuildSelection{
	@Override
	protected void callback(PlayerInteractionControlManager pim, KeyEventInterface e) {
		BuildToolsManager buildToolsManager = pim.getBuildToolsManager();
		SegmentControlManager segmentControlManager = pim.getSegmentControlManager();
		buildToolsManager.setCopyArea2vectors(segmentControlManager.getSegmentController(),
				selectionBoxA, selectionBoxB, (int) pim.getState().getMaxBuildArea());
			buildToolsManager.setCopyMode(false);
	}
	@Override
	protected DrawStyle getDrawStyle(){
		return DrawStyle.BOX;
	}
	@Override
	protected boolean canExecute(PlayerInteractionControlManager pim) {
		BuildToolsManager buildToolsManager = pim.getBuildToolsManager();
		return buildToolsManager.isSelectMode();
	}
	@Override
	protected boolean isSingleSelect() {
		return false;
	}
}
