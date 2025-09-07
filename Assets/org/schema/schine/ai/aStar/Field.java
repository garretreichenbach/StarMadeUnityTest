package org.schema.schine.ai.aStar;

import java.util.Collection;

import org.schema.schine.network.Identifiable;

public interface Field {

	Collection<Identifiable> getEntities();

	int getNeighborCount();

	Field[] getNeighbors();

	int getWeight();

	boolean isOccupied(Identifiable entity);

}
