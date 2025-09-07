package org.schema.game.client.controller.element.world;

import org.schema.game.common.data.world.RemoteSegment;

public class LocalSegmentRequest {
	public final ClientSegmentProvider provider;
	public final RemoteSegment segment;

	public LocalSegmentRequest(ClientSegmentProvider provider,
	                           RemoteSegment segment) {
		super();
		this.provider = provider;
		this.segment = segment;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return segment.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return segment.pos.equals(((LocalSegmentRequest) obj).segment.pos) && segment.getSegmentController().equals(((LocalSegmentRequest) obj).segment.getSegmentController());
	}

}
