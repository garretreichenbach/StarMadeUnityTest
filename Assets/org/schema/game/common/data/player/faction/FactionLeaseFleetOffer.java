package org.schema.game.common.data.player.faction;

/**
 * [Description]
 *
 * @author TheDerpGamer (MrGoose#0027)
 */
public class FactionLeaseFleetOffer extends FactionRelationOffer {

	private final long price;
	private final long duration;
	private final long fleetId;

	public FactionLeaseFleetOffer(int fromId, int toId, long price, long duration, long fleetId) {
		a = fromId;
		b = toId;
		this.price = price;
		this.duration = duration;
		this.fleetId = fleetId;
	}

	public long getPrice() {
		return price;
	}

	public long getDuration() {
		return duration;
	}

	public int getDurationDays() {
		return (int) (duration / 86400000);
	}

	public long getFleetId() {
		return fleetId;
	}
}
