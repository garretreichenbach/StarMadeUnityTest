package org.schema.game.client.view.gui.crew.quarters;

import org.schema.game.client.controller.manager.ingame.AbstractSizeSetting;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class QuarterAreaSetting extends AbstractSizeSetting {

	private final int min;
	private final int max;
	private final AreaSettingCallback callback;

	public QuarterAreaSetting(AreaSettingCallback callback, int min, int max) {
		this.callback = callback;
		this.min = min;
		this.max = max;
	}

	@Override
	public int getMin() {
		return min;
	}

	@Override
	public int getMax() {
		return max;
	}

	@Override
	public void dec() {
		super.dec();
		callback.activate(this);
	}

	@Override
	public void inc() {
		super.inc();
		callback.activate(this);
	}

	@Override
	public void reset() {
		super.reset();
		callback.activate(this);
	}
}
