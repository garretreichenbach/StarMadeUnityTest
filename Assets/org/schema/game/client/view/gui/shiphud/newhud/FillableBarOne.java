package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.schine.input.InputState;

public abstract class FillableBarOne extends FillableBar {

	public FillableBarOne(InputState state) {
		super(state);
	}

	@Override
	public float[] getFilled() {
		return new float[]{getFilledOne()};
	}

	public abstract float getFilledOne();
}
