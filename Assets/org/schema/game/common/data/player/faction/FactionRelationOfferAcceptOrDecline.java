package org.schema.game.common.data.player.faction;

public class FactionRelationOfferAcceptOrDecline {
	public final String initiator;
	public final long code;
	public final boolean accept;

	public FactionRelationOfferAcceptOrDecline(String initiator, long code,
	                                           boolean accept) {
		super();
		this.initiator = initiator;
		this.code = code;
		this.accept = accept;
	}
}
