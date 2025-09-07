package org.schema.game.client.controller.manager.ingame;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.buildhelper.Line;
import org.schema.game.client.view.buildhelper.LineBuildHelper;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.input.KeyEventInterface;

public class BuildSelectionLineHelper extends BuildSelection{
	private LineBuildHelper c;
	public BuildSelectionLineHelper(LineBuildHelper c) {
		this.c = c; 
	}
	@Override
	protected void callback(PlayerInteractionControlManager pim, KeyEventInterface e) {
		c.clean();
		c.line = new Line();
		c.line.A = new Vector3i(selectionBoxA);
		c.line.B = new Vector3i(selectionBoxB);
		c.create();
		assert(!c.isFinished());
		c.placedPos.set(selectionBoxA);
		c.placed = true;
		Vector3f lpos = new Vector3f(selectionBoxA.x - SegmentData.SEG_HALF, selectionBoxA.y - SegmentData.SEG_HALF, selectionBoxA.z - SegmentData.SEG_HALF);
		c.localTransform.origin.set(lpos);
		System.err.println("PLACED ----------- "+selectionBoxA+" - "+selectionBoxB);
		
		c.showProcessingDialog(pim.getState(), pim.getBuildToolsManager(), true);
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
		return false;
	}
}
