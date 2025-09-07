package org.schema.game.client.controller;

import org.schema.schine.network.objects.Sendable;

public interface SendableAddedRemovedListener {
	public void onAddedSendable(Sendable s);
	public void onRemovedSendable(Sendable s);
}
