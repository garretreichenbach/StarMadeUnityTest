package org.schema.game.client.controller.manager.ingame;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.input.KeyEventInterface;

public class BuildSelectionFillHelper extends BuildSelection{
	public BuildSelectionFillHelper() {
	}
	@Override
	protected void callback(PlayerInteractionControlManager pim, KeyEventInterface e) {
		Vector3i selectedBlock = new Vector3i(selectionBoxA);
		BuildToolsManager buildToolsManager = pim.getBuildToolsManager();
		
		SegmentPiece p = pim.getSegmentControlManager().getSegmentController().getSegmentBuffer().getPointUnsave(selectedBlock);
		
		if(p != null){
			buildToolsManager.setFillTool(new FillTool(
					pim.getSegmentControlManager().getSegmentController(), selectedBlock, p.getType()));
		}
	}
	@Override
	protected DrawStyle getDrawStyle(){
		return DrawStyle.LINE;
	}
	@Override
	protected boolean canExecute(PlayerInteractionControlManager pim) {
		return true;
	}
	@Override
	protected boolean isSingleSelect() {
		return true;
	}
}
