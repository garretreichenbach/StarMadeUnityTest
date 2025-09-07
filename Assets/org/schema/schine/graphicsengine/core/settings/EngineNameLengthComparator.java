package org.schema.schine.graphicsengine.core.settings;

import java.util.Comparator;

public class EngineNameLengthComparator implements Comparator<EngineSettings> {
	@Override
	public int compare(EngineSettings o1, EngineSettings o2) {
		if (o1.getDescription().length() < o2.getDescription().length()) {
			return -1;
		} else if (o1.getDescription().length() > o2.getDescription().length()) {
			return 1;
		} else {
			return 0;
		}
	}
}
