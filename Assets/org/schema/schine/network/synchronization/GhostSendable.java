package org.schema.schine.network.synchronization;

import org.schema.schine.network.objects.Sendable;

public class GhostSendable {

	public final long timeDeleted;

	public final Sendable sendable;

	public GhostSendable(long timeDeleted, Sendable sendable) {
		super();
		this.timeDeleted = timeDeleted;
		this.sendable = sendable;
	}

}
