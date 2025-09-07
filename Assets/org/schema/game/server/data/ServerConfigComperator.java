package org.schema.game.server.data;

import java.util.Comparator;

public class ServerConfigComperator implements Comparator<ServerConfig> {
	@Override
	public int compare(ServerConfig o1, ServerConfig o2) {
		if (o1.getDescription().length() < o2.getDescription().length()) {
			return -1;
		} else if (o1.getDescription().length() > o2.getDescription().length()) {
			return 1;
		} else {
			return 0;
		}
	}
}