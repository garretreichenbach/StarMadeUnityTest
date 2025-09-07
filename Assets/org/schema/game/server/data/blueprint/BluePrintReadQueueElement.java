package org.schema.game.server.data.blueprint;

import com.bulletphysics.linearmath.Transform;

public class BluePrintReadQueueElement {
	public String catalogname;
	public String name;
	public Transform t;
	public boolean activeAI;

	public BluePrintReadQueueElement(String catalogname, String name,
	                                 Transform t, boolean activeAI) {
		super();
		this.catalogname = catalogname;
		this.name = name;
		this.t = t;
		this.activeAI = activeAI;
	}

}
