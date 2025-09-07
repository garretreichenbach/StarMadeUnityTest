package org.schema.game.server.data.admin;

import java.util.Comparator;

public class AdminCommandLengthComparator implements Comparator<AdminCommands> {
	@Override
	public int compare(AdminCommands o1, AdminCommands o2) {
		if (o1.getDescription().length() < o2.getDescription().length()) {
			return -1;
		} else if (o1.getDescription().length() > o2.getDescription().length()) {
			return 1;
		} else {
			return 0;
		}
	}
}
