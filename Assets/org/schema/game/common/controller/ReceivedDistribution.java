package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;

public class ReceivedDistribution {
	public Vector3i controllerPos;
	public long idPos;
	public int id;
	public int dist;
	public int stateId;

	public ReceivedDistribution(Vector3i controllerPos, long idPos, int id, int dist, int stateId) {
		super();
		this.controllerPos = controllerPos;
		this.idPos = idPos;
		this.id = id;
		this.dist = dist;
		this.stateId = stateId;
	}

}