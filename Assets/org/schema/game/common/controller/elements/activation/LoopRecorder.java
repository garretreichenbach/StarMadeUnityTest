package org.schema.game.common.controller.elements.activation;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LoopRecorder {
	private LongLinkedOpenHashSet trail = new LongLinkedOpenHashSet();

	public boolean hasLoop(long l) {
		return trail.contains(l);
	}

	public void add(long l) {
		trail.add(l);
	}
}
