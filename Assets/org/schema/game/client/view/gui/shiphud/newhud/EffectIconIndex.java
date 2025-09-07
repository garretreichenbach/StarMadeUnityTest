package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.game.common.data.blockeffects.BlockEffectTypes;

public class EffectIconIndex implements IconInterface {
	public final BlockEffectTypes type;
	private final int index;
	private final boolean buff;
	private final String string;

	public EffectIconIndex(int index, BlockEffectTypes type, boolean buff, String string) {
		super();
		this.index = index;
		this.type = type;
		this.buff = buff;
		this.string = string;
	}

	/**
	 * @return the index
	 */
	@Override
	public int getIndex() {
		return index;
	}

	/**
	 * @return the buff
	 */
	@Override
	public boolean isBuff() {
		return buff;
	}

	/**
	 * @return the string
	 */
	@Override
	public String getText() {
		return string;
	}

}
