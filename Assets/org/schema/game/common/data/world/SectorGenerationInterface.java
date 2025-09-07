package org.schema.game.common.data.world;

import java.util.Random;

import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;

public interface SectorGenerationInterface {
	public boolean staticSectorGeneration(GameServerState state, int secX, int secY, int secZ, StellarSystem system, int index, Galaxy galaxy, Random rand);

	public void generate(GameServerState state, int secX, int secY, int secZ,
			StellarSystem system, int index, Galaxy galaxy, Random rand);

	public boolean orbitTakenByGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand);

	public void definitePlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand);

	public boolean onOrbitButNoPlanet(GameServerState state, int secX, int secY,
			int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand);

	public void onAsteroidBelt(GameServerState state, int secX, int secY,
			int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand);
	
	
}
