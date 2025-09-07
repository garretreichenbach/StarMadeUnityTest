package org.schema.game.common.controller.elements.factorymanager;

import api.element.block.FactoryType;
import api.utils.game.inventory.ItemStack;

import java.util.List;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public record ProcessingJob(long started, int priority, FactoryType factoryType, List<ItemStack> inputs, List<ItemStack> target, List<ItemStack> progress) implements Comparable<ProcessingJob> {
	
	@Override
	public int compareTo(ProcessingJob o) {
		return Integer.compare(priority, o.priority);
	}
}