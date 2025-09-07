package org.schema.game.client.view.textbox;

public abstract class Replacement {
	public final int where;
	public int index;

	public Replacement(int where, int index) {
		super();
		this.where = where;
		this.index = index;
	}

	public abstract String get();
}
