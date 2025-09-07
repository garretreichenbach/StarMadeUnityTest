package org.schema.game.client.controller.element.world;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class FastValidationContainer {
	public final LongArrayList l = new LongArrayList(4096);
	public final ShortArrayList a = new ShortArrayList(4096*4);
	public void clear() {
		l.clear();
		a.clear();
	}
}
