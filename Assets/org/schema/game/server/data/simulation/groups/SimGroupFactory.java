package org.schema.game.server.data.simulation.groups;

import org.schema.game.server.data.GameServerState;

public interface SimGroupFactory<E extends SimulationGroup> {
	public E instantiate(GameServerState s);
}
