package org.schema.game.server.data.simulation.npc.geo;

import java.util.Arrays;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.VoidSystem;
import theleo.jstruct.Struct;

@Struct
public class StarSystemResourceRequestContainer {
	/**
	 * Array of resource densities. Indices are based on ElementKeyMap.resources.
	 */
	public final byte[] res = new byte[VoidSystem.RESOURCES];
	public int factionId = 0;

	public static void reset(StarSystemResourceRequestContainer c){ //jstruct/junion apparently doesn't support nonstatic methods
		Arrays.fill(c.res, (byte)0);
		c.factionId = 0;
	}
}
