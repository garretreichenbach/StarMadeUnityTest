package org.schema.game.common.controller;

import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.BuildCallback;

public class DelayedBuildCallback {

	public final Vector3i absOut;
	public boolean received;
	private BuildCallback callback;
	private Vector3i absOnOut;
	private short type;

	public DelayedBuildCallback(BuildCallback callback, Vector3i absOut,
	                            Vector3i absOnOut, short type) {
		this.callback = callback;
		this.absOut = absOut;
		this.absOnOut = absOnOut;
		this.type = type;
	}

	public void execute() throws IOException, InterruptedException {
		callback.onBuild(absOut, absOnOut, type);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return absOut.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
				if (obj instanceof DelayedBuildCallback) {
			return absOut.equals(((DelayedBuildCallback) obj).absOut);
		} else {
			return absOut.equals(obj);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DelayedBuildCallback [callback=" + callback + ", absOut="
				+ absOut + ", type=" + type + ", received=" + received + "]";
	}

}
