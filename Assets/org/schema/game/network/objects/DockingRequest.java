package org.schema.game.network.objects;

import org.schema.common.util.linAlg.Vector3i;

public class DockingRequest {
	public boolean dock;
	public String id;
	public Vector3i pos;

	public DockingRequest() {
	}

	public DockingRequest(boolean dock, String id, Vector3i pos) {
		super();
		set(dock, id, pos);
	}

	public void set(boolean dock, String id, Vector3i pos) {
		this.dock = dock;
		this.id = id;
		this.pos = pos;
	}

}
