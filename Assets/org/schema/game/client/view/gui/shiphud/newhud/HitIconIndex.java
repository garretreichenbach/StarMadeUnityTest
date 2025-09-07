package org.schema.game.client.view.gui.shiphud.newhud;

import org.schema.game.common.controller.elements.effectblock.EffectElementManager.OffensiveEffects;

public class HitIconIndex implements IconInterface {
	public final OffensiveEffects type;
	private final int index;
	private final String text;
	private final boolean buff;

	public HitIconIndex(int index, OffensiveEffects type, boolean buff, String txt) {
		super();
		this.index = index;
		this.type = type;
		this.buff = buff;
		this.text = txt;
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
	 * @return the text
	 */
	@Override
	public String getText() {
		return text;
	}

}
