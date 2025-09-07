package org.schema.game.common.controller.elements.power.reactor.tree.graph;

import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElementBackground;
import org.schema.schine.input.InputState;

public abstract class ReactorGraphBackground extends GUIGraphElementBackground{
	private ColorEnum colorEnum = ColorEnum.GREY;

	public ReactorGraphBackground(InputState state) {
		super(state, 10, 10);
	}

	@Override
	protected int getLeftTop() {
		return (colorEnum.start*4)+0;
	}

	@Override
	protected int getRightTop() {
		return (colorEnum.start*4)+1;
	}

	@Override
	protected int getBottomLeft() {
		return (colorEnum.start*4)+2;
	}

	@Override
	protected int getBottomRight() {
		return (colorEnum.start*4)+3;
	}
	public void setColorEnum(ColorEnum c) {
		this.colorEnum = c;
	}
	public ColorEnum getColorEnum() {
		return colorEnum;
	}
	public static enum ColorEnum{
		GREEN(0),
		GREEN_OFF(1),
		CYAN(2),
		CYAN_OFF(3),
		BLUE(4),
		BLUE_OFF(5),
		MAGENTA(6),
		MAGENTA_OFF(7),
		RED(8),
		RED_OFF(9),
		ORANGE(10),
		ORANGE_OFF(11),
		YELLOW(12),
		YELLOW_OFF(13),
		GREY(14),
		GREY_OFF(15),
		
		;
		final int start;

		private ColorEnum(int start){
			this.start = start;
		}
	}
	

	@Override
	protected float getTopOffset() {
		return (((colorEnum.start/2)*2f)+0f) * 0.0625f;
	}

	@Override
	protected float getBottomOffset() {
		return (((colorEnum.start/2)*2f)+1f) * 0.0625f;
	}

	@Override
	protected float getLeftOffset() {
		return ((colorEnum.start*2f)+0f) * 0.03125f;
	}

	@Override
	protected float getRightOffset() {
		return ((colorEnum.start*2f)+1f) * 0.03125f;
	}
}