package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import org.schema.schine.input.InputState;

public class ReactorGraphBackgroundBig extends ReactorGraphBackground{


	public ReactorGraphBackgroundBig(InputState state) {
		super(state);
	}

	@Override
	protected String getCorners() {
		return "UI 32px ChamberCorners-8x8-gui-";
	}

	@Override
	protected String getVertical() {
		return "UI 32px ChamberVertical-32x1-gui-";
	}

	@Override
	protected String getHorizontal() {
		return "UI 32px ChamberHorizontal-1x16-gui-";
	}

	@Override
	protected String getBackground() {
		return "UI 16px SubChamberCenter-gui-";
	}

	
}
