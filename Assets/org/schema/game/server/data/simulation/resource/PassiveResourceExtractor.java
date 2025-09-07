package org.schema.game.server.data.simulation.resource;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A virtual representation of a passive resource gathering system.
 * <p>
 * Based on IR0NS1GHT's modified {@code Extractor} class, from Ithirahad's Resources ReSourced mod.
 */
public class PassiveResourceExtractor implements Serializable {

	private static final boolean DEBUG_LOGGING = false;
	private final String UID;
	public float strength;
	/**
	 * Number of collection rounds the extractor can still hold.
	 * In other words: (remaining inventory capacity/total inventory volume of one collection tick)
	 */
	private int capacity;
	public int faction; //faction ID of containing entity
	/**
	 * Number of collection rounds completed whilst unloaded.
	 */
	private float collected;
	public Vector3i sector;
	public final ShortOpenHashSet sourceTypesExtractable; //what
	public short oreId;
	private long lastEmptied = -1;
	private int entityMiningBonus = 1; //bonus from chambers or other effectgroup appliers. Does not include faction bonus (calculated on the fly)
	private transient PassiveResourceProvider supplier;

	private static void print(String s) {
		System.out.println(s);
	}

	public PassiveResourceExtractor(String UID, short[] type, Vector3i sector, short OreId) {
		this.UID = UID;
		sourceTypesExtractable = new ShortOpenHashSet(type);
		this.sector = sector;
		oreId = OreId;
	}

	public void setSupplier(PassiveResourceProvider supplier) {
		this.supplier = supplier;
	}

	protected void incCollected(float amount) {
		//collected += amount;
		collected = Math.min(capacity, collected + amount);
		if(DEBUG_LOGGING) System.out.println("[SIM][Passive Resource System] Increased extractor " + UID + " collected item quantity by " + amount + " to " + collected);
	}

	/**
	 * Withdraw up to max units of collected items. Withdraw amount is subtracted from collected quantity logged within object.
	 * Collected gets rounded down; rounding errors remain as "collected" fractional item logged in the object.
	 *
	 * @param max -1 for unlimited
	 * @return quantity that is withdrawn
	 */
	protected int withdrawCollected(double max) {
		int out;
		if(max != -1) out = (int) Math.min(max, collected);
		else out = (int) collected;
		collected -= out; //keep fractions, return rounded down amount
		if(DEBUG_LOGGING) print("[SIM][Passive Resource System] Withdrew " + out + " from extractor " + UID);
		lastEmptied = System.currentTimeMillis();
		return out;
	}

	public void setCapacityVolume(double cargoVolume) {
		float capacityPerItem = ElementKeyMap.getInfo(oreId).getVolume();
		capacity = (int) (cargoVolume / capacityPerItem);
	}

	public void setFaction(int factionId) {
		faction = factionId;
	}

	public String getUID() {
		return UID;
	}

	public float getStrength() {
		return strength;
	}

	public int getCapacity() {
		return capacity;
	}

	public float getCollected() {
		return collected;
	}

	public Vector3i getSector() {
		return sector;
	}

	public ShortSet getExtractableTypes() {
		return sourceTypesExtractable;
	}

	public void setStrength(float strength) {
		if(DEBUG_LOGGING) System.out.println("[SIM][Passive Resource System] Set strength of extractor " + UID + " from " + this.strength + " to " + strength);
		float old = this.strength;
		this.strength = strength;
		if(strength != old && supplier != null) supplier.onExtractorPowerChanged(this, old);
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void setEntityMiningBonus(int miningBonus) {
		entityMiningBonus = miningBonus;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		PassiveResourceExtractor extractor = (PassiveResourceExtractor) o;
		return UID.equals(extractor.UID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(UID);
	}

	@Override
	public String toString() {
		String last = "NULL";
		String factionName = "ERROR: Unknown Name; Faction Manager Not Present";
		if(GameServerState.instance != null && GameServerState.instance.getFactionManager() != null) factionName = GameServerState.instance.getFactionManager().getFactionName(faction);
		if(lastEmptied > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("kk:mm - EEE, d. MMM");
			last = (lastEmptied > 0) ? sdf.format(new Date(lastEmptied)) : "NULL";
			long since = (System.currentTimeMillis() - lastEmptied) / 1000;
			long h = since / 3600;
			long m = (since % 3600) / 60;
			last += String.format("  +%sh %smin", h, m);
		}
		return "Extractor {" +
				"UID = '" + UID + '\'' +
				", strength = " + strength +
				", capacity = " + capacity +
				", collected = " + collected +
				", sector = " + sector +
				", factionID = " + faction + " (" + factionName + ")" +
				", type = " + sourceTypesExtractable +
				", oreId = " + oreId + "(" + ElementInformation.getKeyId(oreId) + ")" +
				", lastEmptied = " + last +
				", effectMiningBonus = " + entityMiningBonus +
				'}';
	}

	public int getEntityMiningBonus() {
		return entityMiningBonus;
	}
}
