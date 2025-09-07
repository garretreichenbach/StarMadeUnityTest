package org.schema.schine.network;

import javax.vecmath.Vector3f;

public class NetworkGravity {

	public int gravityId = -1;
	public int gravityIdReceive = -1;
	public Vector3f gravity = new Vector3f();
	public Vector3f gravityReceive = new Vector3f();

	public boolean gravityReceived;
	public boolean forcedFromServer;
	public boolean central;

	public NetworkGravity() {
	}

	public NetworkGravity(NetworkGravity o) {
		gravityId = o.gravityId;
		gravityIdReceive = o.gravityIdReceive;
		gravity = o.gravity;
		central = o.central;
		gravityReceive = o.gravityReceive;
		gravityReceived = o.gravityReceived;
		forcedFromServer = o.forcedFromServer;
		
	}

	@Override
	public String toString() {
		return "NetworkGravity [gravityId=" + gravityId + ", gravityIdReceive="
				+ gravityIdReceive + ", gravity=" + gravity
				+ ", gravityReceive=" + gravityReceive + ", gravityReceived="
				+ gravityReceived + ", forcedFromServer=" + forcedFromServer
				+ "]";
	}

	
}
