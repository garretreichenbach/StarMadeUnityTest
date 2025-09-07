package org.schema.game.server.data.simulation.resource;

import api.listener.events.world.generation.ExtractorMiningBonusCalculateEvent;
import api.listener.events.world.generation.CustomModResourceProviderInitEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2FloatOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.apache.commons.lang3.SerializationUtils;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import java.io.Serializable;

import static api.mod.StarLoader.fireEvent;
import static java.lang.Math.min;
import static org.schema.common.FastMath.fastFloor;

public class PassiveResourceProvider implements Serializable {
	
	public static final PassiveResourceProvider EMPTY = new PassiveResourceProvider(new Short2IntOpenHashMap(), new Vector3i(), (short) 0);
	private static final float ACTIVE_RESOURCE_REGEN_PER_SECOND = 2;
	private final Short2IntOpenHashMap resources; //resource types to quantities per tick
	private final Short2IntOpenHashMap activeResourceCaps = new Short2IntOpenHashMap();
	private final Short2FloatOpenHashMap activeResourceAmounts = new Short2FloatOpenHashMap();
	private final Vector3i location; //sector where this is located
	private final Object2ObjectOpenHashMap<String, PassiveResourceExtractor> extractors; //uids to extractors
	/**
	 * What kind of resource source this is (atmosphere, planet core etc.)
	 */
	private short sourceType;
	private long lastPassiveUpdate;
	private long lastActiveUpdate;
	private transient ResourceProviderTypedef sourceTypeInfo;
	private final float passivePoolRegenRate = 1;

	public PassiveResourceProvider(Short2IntMap resourcesToRegenRates, Vector3i location, short type) {
		resources = new Short2IntOpenHashMap(resourcesToRegenRates);
		this.location = location;
		extractors = new Object2ObjectOpenHashMap<>();
		sourceType = type;
		onInit();
	}

	//called after creation or deserialization
	private void onInit() {
		if(sourceType < DefaultResourceProviderTypes.values().length) {
			sourceTypeInfo = DefaultResourceProviderTypes.values()[sourceType];
		} else {
			CustomModResourceProviderInitEvent ev = new CustomModResourceProviderInitEvent(this);
			fireEvent(ev, true);
			sourceType = ev.getProviderType().getProviderTypeId();
		}
	}

	public PassiveResourceProvider(Tag tag) {
		this(tag.getStruct());
	}

	public PassiveResourceProvider(Tag[] tag) {
		resources = new Short2IntOpenHashMap();
		location = new Vector3i();
		extractors = new Object2ObjectOpenHashMap<>();
		fromTagStructure(tag);
	}

	@SuppressWarnings("unchecked")
	public void fromTagStructure(Tag[] v) {
		resources.clear();
		clearExtractors();
		activeResourceAmounts.clear(); //presumably not necessary
		activeResourceCaps.clear();
		sourceType = v[0].getShort();
		resources.putAll((Short2IntOpenHashMap) SerializationUtils.deserialize(v[1].getByteArray()));
		extractors.putAll((Object2ObjectOpenHashMap<String, PassiveResourceExtractor>) SerializationUtils.deserialize(v[2].getByteArray()));
		activeResourceAmounts.putAll((Short2FloatOpenHashMap) SerializationUtils.deserialize(v[3].getByteArray()));
		activeResourceCaps.putAll((Short2IntOpenHashMap) SerializationUtils.deserialize(v[4].getByteArray()));
		location.set(v[5].getVector3i());
		lastPassiveUpdate = v[6].getLong();
		lastActiveUpdate = v[7].getLong();
		onLoadFromPersistence();
	}

	private void clearExtractors() {
		for(PassiveResourceExtractor extractor : extractors.values()) {
			//TODO do any finalization here
		}
		extractors.clear();
	}

	public void onLoadFromPersistence() {
		onInit();
		PassiveResourceManager.addProvider(this);

		//rebuild lost (circular) references
		for(PassiveResourceExtractor e : extractors.values()) {
			e.setSupplier(this);
		}
		//force initial update
		updatePassive(System.currentTimeMillis());
		lastActiveUpdate = System.currentTimeMillis();
	}

	public void updatePassive(long currentTime) {
		if(extractors.isEmpty()) {
			lastPassiveUpdate = currentTime;
			return;
		}
		long millisSinceLastUpdate = currentTime - lastPassiveUpdate;
		boolean nullUpdate = (lastPassiveUpdate <= 0);
		boolean onLoaded = (millisSinceLastUpdate > 10000);
		System.out.println("\n################## UPDATING WELL... Last Update: (" + (millisSinceLastUpdate / 1000) + " sec\n" + this);
		lastPassiveUpdate = currentTime;
		if(nullUpdate) //Initial update after server start; don't do anything here. -> Time the server was offline doesn't count into passive generation.
			return;
		//calculated needed numbers like total regen collected over time
		float regenRateTimed = (passivePoolRegenRate * millisSinceLastUpdate) / (1000.0f); //millis -> seconds -> sum of regen rate for x seconds
		float totalExPower = getTotalExtractorPower();
		boolean oversubscribed = (totalExPower > passivePoolRegenRate);
		//increase collected ores in virtual extractors
		for(PassiveResourceExtractor ex : extractors.values()) {
			System.out.println("BEFORE UPDATING extractor: " + ex);
			float changeOre = 0;
			if(!oversubscribed)
				changeOre = (ex.getStrength() * millisSinceLastUpdate) / 1000.0f; //extraction power adjusted for time
			else {
				changeOre = regenRateTimed * (ex.getStrength() / totalExPower); //proportional extraction power adjusted for time
			}
			changeOre *= getSituationMiningBonus(ex.getUID(), ex.oreId, ex.sourceTypesExtractable, ex.faction, ex.sector);
			changeOre *= ex.getEntityMiningBonus();
			ex.incCollected(changeOre);
			System.out.printf("Changed ore quantity for extr. strength=%s: %s%n", ex.getStrength(), changeOre);
			System.out.println("FINISHED UPDATING extractor: " + ex);
		}
		System.out.println("\n");
	}

	private float getTotalExtractorPower() {
		float result = 0;
		for(PassiveResourceExtractor p : extractors.values()) result += p.strength;
		return result;
	}

	public static int getSituationMiningBonus(String entityUID, short resOreId, ShortOpenHashSet availableResourceTypes, int factionId, Vector3i sector) {
		//UID and resource/source type variables just for the benefit of mods subscribing to the event, which might want that for whatever reason
		int result;
		try {
			Universe unv = GameServerState.instance.getUniverse();
			Vector3i tmpSectorPosition = new Vector3i(sector);
			StellarSystem sys = GameServerState.instance.getUniverse().getStellarSystemFromSecPos(tmpSectorPosition); //TODO strip tmps from StellarSystemFromPos method if offsetting bug occurs
			result = unv.getSystemOwnerShipType(sys, factionId).getMiningBonusMult();
		} catch(Exception ex) {
			System.err.println("[SIM][Resources][WARNING] Unable to retrieve faction mining bonus:");
			ex.printStackTrace();
			result = 1;
		}
		result *= ServerConfig.MINING_BONUS.getInt();
		ExtractorMiningBonusCalculateEvent exev = new ExtractorMiningBonusCalculateEvent(entityUID, factionId, sector, resOreId, availableResourceTypes, result);
		fireEvent(exev, true);
		result = exev.getBonus();
		return result;
	}

	public Vector3i getLocation() {
		return location;
	}

	public void updateActivePool() {
		long now = System.currentTimeMillis();
		for(short id : activeResourceCaps.keySet()) {
			int cap = activeResourceCaps.get(id);
			if(activeResourceAmounts.get(id) < cap) {
				int rawNewResources = Math.round(((now - lastActiveUpdate) / 1000.0f) * ACTIVE_RESOURCE_REGEN_PER_SECOND);
				activeResourceAmounts.put(id, min(cap, activeResourceAmounts.get(id) + rawNewResources));
			} else activeResourceAmounts.put(id, cap);
		}
		lastActiveUpdate = now;
	}

	public void onExtractorPowerChanged(PassiveResourceExtractor passiveResourceExtractor, float oldValue) {
	}

	public void addResource(short id, int v) {
		resources.put(id, v);
	}

	/**
	 * Returns a map of resources (by block ID) to the quantity of that resource yielded per resource simulation tick.
	 */
	public Short2IntOpenHashMap getResources() {
		return resources;
	}

	public void setActivePool(Short2IntOpenHashMap caps) {
		activeResourceCaps.putAll(caps);
		for(short id : caps.keySet()) {
			activeResourceAmounts.put(id, activeResourceCaps.get(id)); //fill completely
		}
	}

	public ResourceProviderTypedef getSourceTypeInfo() {
		return sourceTypeInfo;
	}

	public void fromTagStructure(Tag struct) {
		fromTagStructure(struct.getStruct());
	}

	public Tag toTagStructure() {
		return new Tag(Tag.Type.STRUCT, "Resource Extraction Source",
				new Tag[]{
						new Tag(Tag.Type.SHORT, null, sourceType),
						new Tag(Tag.Type.BYTE_ARRAY, null, SerializationUtils.serialize(resources)), //TODO pointless obj. overhead on known types slows down read times for no reason. convert to shorts and ints struct before serialization
						new Tag(Tag.Type.BYTE_ARRAY, null, SerializationUtils.serialize(extractors)),
						new Tag(Tag.Type.BYTE_ARRAY, null, SerializationUtils.serialize(activeResourceAmounts)),
						new Tag(Tag.Type.BYTE_ARRAY, null, SerializationUtils.serialize(activeResourceCaps)),
						new Tag(Tag.Type.VECTOR3i, null, location),
						new Tag(Tag.Type.LONG, null, lastPassiveUpdate),
						new Tag(Tag.Type.LONG, null, lastActiveUpdate),
						FinishTag.INST
				}
		);
	}

	public Object2ObjectOpenHashMap<String, PassiveResourceExtractor> getExtractors() {
		return extractors;
	}

	/**
	 * Removes resources from the active resource harvesting pool according to harvestPower, bounded by volumeMax, and returns an array of quantities removed.
	 *
	 * @param harvestPower Maximum amount of items of a given type to harvest at once
	 * @param volumeMax    Maximum cargo volume to fill with items
	 * @return A 2D array where each column represents a different resource, row 0 is the block ID of the resource, and row 1 is quantities of resources of the given id which have been harvested.
	 */
	public int[][] claimActiveResources(float harvestPower, double volumeMax) {
		int[][] result = new int[2][resources.size()]; //row 0 : resource IDs. row 1 : quantities.
		int largestCount = 0;
		if(!resources.isEmpty()) {
			//we need to get equal amounts based on amount in active harvest pool, scaled to volume
			float volTotal = 0; //volume of everything in the pool
			{
				short id;
				int i = 0;
				int quantity;
				for(Short2FloatOpenHashMap.Entry set : activeResourceAmounts.short2FloatEntrySet()) {
					id = set.getShortKey();
					quantity = min(fastFloor(harvestPower), fastFloor(set.getFloatValue())); //amount available or max amount the system can harvest - whichever is smaller
					result[i][0] = id; //item id
					result[i][1] = quantity; //total quantity of (full) items in active harvesting pool
					volTotal += quantity * ElementKeyMap.getInfo(id).getVolume();
					if(quantity > largestCount) largestCount = quantity;
					i++;
				}
			}

			boolean reduceForVolume = volTotal > volumeMax;
			double volumeFactor = 1;
			if(reduceForVolume && volTotal > 0) volumeFactor = volumeMax / volTotal;
			for(int i = 0; i < result.length; i++) {
				if(reduceForVolume) result[i][1] = fastFloor((float) (volumeFactor * (float) result[i][1]));
				activeResourceAmounts.addTo((short) result[i][0], -result[i][1]); //remove from harvesting pool
			}
		}
		return result;
	}

	/**
	 * Default types. Any {@code short} value can technically be used as a resource provider type
	 */
	public enum DefaultResourceProviderTypes implements ResourceProviderTypedef {
		ATMOSPHERE(0, Lng.str("Atmospheric Traces")),
		PLANET_INSIDE(1, Lng.str("Deep Subsurface Resources")),
		NEBULA(2, Lng.str("Nebula Plasma Traces")),
		STAR(3, Lng.str("Stellar Core"));

		private final short typeId;
		private final String name;

		DefaultResourceProviderTypes(int typeId, String name) { //using ints because Java has no short literals and typing (short) every time is ugly
			this.typeId = (short) typeId;
			this.name = name;
		}

		@Override
		public short getProviderTypeId() {
			return typeId;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean canScanFromEntity(ManagedUsableSegmentController<?> entity) {
			boolean canScanAtmosphere = entity.getConfigManager().apply(StatusEffectType.RESOURCE_SCANNER_ATMOSPHERE, false);
			boolean canScanCore = entity.getConfigManager().apply(StatusEffectType.RESOURCE_SCANNER_CORE, false);
			boolean canScanNebula = entity.getConfigManager().apply(StatusEffectType.RESOURCE_SCANNER_NEBULA, false);
			return switch(this) {
				case ATMOSPHERE -> canScanAtmosphere;
				case PLANET_INSIDE -> canScanCore;
				case NEBULA -> canScanNebula;
				case STAR -> true;
			};
		}
	}

	public interface ResourceProviderTypedef {
		short getProviderTypeId();

		String getName();

		/**
		 * Determines if the specified entity can scan the resource using the long range scanner.
		 * <br/>Useful for situations where a custom resource needs the entity to have a specific chamber in order to be viewable in the scan info log.
		 *
		 * @param entity The entity to check
		 * @return {@code true} if the entity can scan the resource, {@code false} otherwise
		 */
		boolean canScanFromEntity(ManagedUsableSegmentController<?> entity);
	}
}
