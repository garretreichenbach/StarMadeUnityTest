package org.schema.game.network.objects.remote;

import org.schema.game.common.controller.SendableSegmentProvider;

public class TextBlockPair {
	//only used on client
	public SendableSegmentProvider provider;
	public long block;
	public String text;

	@Override
	public String toString() {
		return "TextBlockPair [provider=" + provider + ", block=" + block
				+ ", text=" + text + "]";
	}

}
