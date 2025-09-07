package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import org.schema.schine.input.InputState;

public class ReactorGraphBackgroundSmall extends ReactorGraphBackground{


	public ReactorGraphBackgroundSmall(InputState state) {
		super(state);
	}
	@Override
	protected int getLeftTop() {
		return ((getColorEnum().start/2)*4)+0;
	}

	@Override
	protected int getRightTop() {
		return ((getColorEnum().start/2)*4)+1;
	}

	@Override
	protected int getBottomLeft() {
		return ((getColorEnum().start/2)*4)+2;
	}

	@Override
	protected int getBottomRight() {
		return ((getColorEnum().start/2)*4)+3;
	}
	@Override
	protected String getCorners() {
		return "UI 16px SubChamberCorner-8x8-gui-";
	}

	@Override
	protected String getVertical() {
		return "UI 16px SubChamberVertical-16x1-gui-";
	}

	@Override
	protected String getHorizontal() {
		return "UI 16px SubChamberHorizontal-1x16-gui-";
	}

	@Override
	protected String getBackground() {
		return "UI 16px SubChamberCenter-gui-";
	}
	@Override
	protected float getTopOffset() {
		return (((getColorEnum().start/2)*2f)+0f) * 0.0625f;
	}

	@Override
	protected float getBottomOffset() {
		return (((getColorEnum().start/2)*2f)+1f) * 0.0625f;
	}

	@Override
	protected float getLeftOffset() {
		return (((getColorEnum().start/2)*2f)+0f) * 0.0625f;
	}

	@Override
	protected float getRightOffset() {
		return (((getColorEnum().start/2)*2f)+1f) * 0.0625f;
	}
}
