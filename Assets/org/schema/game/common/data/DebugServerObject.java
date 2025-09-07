package org.schema.game.common.data;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.network.objects.remote.RemoteSerializable;

public abstract class DebugServerObject implements RemoteSerializable {
	public static final byte PHYSICAL = 0;
	public final byte type;

	public DebugServerObject() {
		super();
		this.type = getType();
	}

	public abstract byte getType();

	public abstract void draw(GameClientState state);
}
