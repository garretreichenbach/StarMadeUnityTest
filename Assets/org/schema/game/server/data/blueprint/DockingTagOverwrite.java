package org.schema.game.server.data.blueprint;

import javax.vecmath.Quat4f;

import org.schema.common.util.linAlg.Vector3i;

public class DockingTagOverwrite {

	public final String dockTo;
	public final Vector3i pos;
	public final byte dockingOrientation;
	public final Quat4f rot;

	public DockingTagOverwrite(String dockTo, Vector3i vector3i,
	                           Quat4f delayedDockLocalRot, byte dockingOrientation) {
		this.dockTo = dockTo;
		this.pos = vector3i;
		this.dockingOrientation = dockingOrientation;
		this.rot = delayedDockLocalRot;
	}

}
