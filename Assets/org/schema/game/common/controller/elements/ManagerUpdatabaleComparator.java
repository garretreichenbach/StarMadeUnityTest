package org.schema.game.common.controller.elements;

import java.util.Comparator;

public class ManagerUpdatabaleComparator implements Comparator<ManagerUpdatableInterface>{

	@Override
	public int compare(ManagerUpdatableInterface o1, ManagerUpdatableInterface o2) {
		return o2.updatePrio() - o1.updatePrio();
	}

}
