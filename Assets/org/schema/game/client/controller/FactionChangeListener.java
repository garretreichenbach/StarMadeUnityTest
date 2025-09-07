package org.schema.game.client.controller;

public interface FactionChangeListener {
	public void onFactionChanged();

	public void onRelationShipOfferChanged();

	public void onFactionNewsDeleted();

	public void onInvitationsChanged();
}
