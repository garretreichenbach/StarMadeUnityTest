package org.schema.game.common.data.gamemode.battle;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class BattleCondition {

	public ObjectArrayList<BattleCondition> childs = new ObjectArrayList<BattleCondition>();

	public boolean isMet() {
		boolean met = isMetCondition();
		for (int i = 0; i < childs.size(); i++) {
			met = met && childs.get(i).isMet();
		}
		return met;
	}

	protected abstract boolean isMetCondition();

	protected void onMetCondition() {
	}

	;

	protected void onNotMetCondition() {
	}

	;

	protected abstract String getDescription();
}
