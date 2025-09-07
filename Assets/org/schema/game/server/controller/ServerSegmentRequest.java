package org.schema.game.server.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.network.objects.NetworkSegmentProvider;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class ServerSegmentRequest {

	private final NetworkSegmentProvider sc;
	private final Vector3i v;
	private final SegmentController c;
	private final long localTimestamp;
	private final short sizeOnClient;
	public boolean sigatureOfSegmentBuffer;
	public RemoteSegment segment;
	public EWAHCompressedBitmap bitMap;
	public boolean highPrio;

	public ServerSegmentRequest(SegmentController c, Vector3i v,
	                            NetworkSegmentProvider sc, long localTimestamp, short sizeOnClient) {
		this.c = c;
		this.v = v;
		this.sc = sc;
		this.sizeOnClient = sizeOnClient;
		this.localTimestamp = localTimestamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		ServerSegmentRequest r = (ServerSegmentRequest) obj;
		return sc == r.sc && v.equals(r.v) && c == r.c;
	}

	/**
	 * @return the localTimestamp
	 */
	public long getLocalTimestamp() {
		return localTimestamp;
	}

	/**
	 * @return the sc
	 */
	public NetworkSegmentProvider getNetworkSegmentProvider() {
		return sc;
	}

	/**
	 * @return the c
	 */
	public SegmentController getSegmentController() {
		return c;
	}

	/**
	 * @return the v
	 */
	public Vector3i getSegmentPos() {
		return v;
	}

	/**
	 * @return the sizeOnClient
	 */
	public short getSizeOnClient() {
		return sizeOnClient;
	}

}
