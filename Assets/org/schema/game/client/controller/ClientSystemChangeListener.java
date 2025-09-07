package org.schema.game.client.controller;

import org.schema.common.util.linAlg.Vector3i;

public interface ClientSystemChangeListener {
	public void onSystemChanged(Vector3i lastSystem, Vector3i currentSystem);
}
