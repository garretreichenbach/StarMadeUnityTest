package org.schema.game.common.controller.elements;

import java.util.Random;

import org.schema.game.common.data.element.ElementCollection;

public interface TargetableSystemInterface {
	public int getPriority();

	public ElementCollection<?, ?, ?> getRandomCollection(Random r);

	public boolean hasAnyBlock();
}
