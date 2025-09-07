package org.schema.game.common.controller;

import api.config.BlockConfig;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.shorts.Short2FloatOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap.Entry;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.data.element.ElementCategory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.resource.tag.SerializableTagElement;

import javax.vecmath.Vector3f;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ElementCountMap implements SerializableTagElement {

	private int[] counts;
	private int[] oreCounts;
	private long currentPrice;
	private int existingTypeCount;
	private int lodShapes;

	public ElementCountMap() {
		counts = new int[ElementKeyMap.highestType + 1];
		oreCounts = new int[32];
	}

	public ElementCountMap(ElementCountMap c) {
		this();
		int existing = 0;
		for(int i = 0; i < counts.length; i++) {
			// When the copied ElementCountMap is old, do not copy out of bounds
			if(i >= c.counts.length) break;

			counts[i] = c.counts[i];
			if(counts[i] > 0) {
				existing++;
			}
		}
		for(int i = 0; i < oreCounts.length; i++) {
			oreCounts[i] = c.oreCounts[i];
		}
		currentPrice = getPrice();
		existingTypeCount = existing;
	}

	public static void test(String[] args) throws ParserConfigurationException {
		//set up ElementKeyMap
		ElementCategory rootCategory = new ElementCategory("Element", null);
		ElementInformation brick = new ElementInformation((short) 1, "brick", rootCategory, new short[0]);
		ElementInformation stone = new ElementInformation((short) 2, "stone", rootCategory, new short[0]);


		List<ElementInformation> l = Arrays.asList(brick, stone);
		ElementKeyMap.initElements(l, rootCategory);

		//test adding to ecm
		ElementCountMap ecm = new ElementCountMap();
		assert (ecm.getTotalAmount() == 0);
		assert (ecm.get(stone.id) == 0);
		assert (ecm.get(brick.id) == 0);

		ecm.inc(brick.id);
		assert (ecm.get(brick.id) == 1);

		ecm.resetAll();
		assert (ecm.getTotalAmount() == 0);

		ecm.inc(brick.id, 42069);
		assert (ecm.get(brick.id) == 42069);
		assert (ecm.getTotalAmount() == 42069);

		ecm.resetAll();

		brick.consistence.add(new FactoryResource(5, stone.id));
		assert (brick.getConsistence().iterator().next().type == stone.id);
		assert (brick.getConsistence().iterator().next().count == 5);
		assert (brick.getConsistence().size() == 1);

		//test production spiking
		assert (ecm.getTotalAmount() == 0);
		ecm.spikeWithProduction();
		assert (ecm.getTotalAmount() == 0);

		ecm.inc(brick.id, 3);
		ecm.spikeWithProduction();
		assert (ecm.get(brick.id) == 3);
		assert (ecm.get(stone.id) == 3 * 5);

		ecm.resetAll();
		ecm.inc(stone.id, 420);
		ecm.spikeWithProduction();
		assert (ecm.getTotalAmount() == 420);
		assert (ecm.get(stone.id) == 420);
		System.out.println(ElementCountMap.class.desiredAssertionStatus() ? "success" : "NO ASSERTIONS ON");
	}

	/**
	 * rechecks array size. this is necessary if these maps reside in pools
	 * and the player joins a game with a different block config.
	 */
	public void checkArraySize() {
		if(counts.length != ElementKeyMap.highestType + 1) {
			counts = new int[ElementKeyMap.highestType + 1];
		}
	}

	public void add(int[] nCounts, int[] oreCounts) {
		for(int i = 0; i < counts.length; i++) {
			int c = nCounts[i];
			if(c > 0) {
				inc((short) i, c);
			}
		}
		for(int i = 0; i < oreCounts.length; i++) {
			this.oreCounts[i] += oreCounts[i];
		}
	}

	public void add(ElementCountMap elementMap) {
		for(int i = 0; i < counts.length; i++) {
			if(elementMap.counts[i] > 0) {
				inc((short) i, elementMap.counts[i]);
			}
		}
		for(int i = 0; i < oreCounts.length; i++) {
			oreCounts[i] += elementMap.oreCounts[i];
		}
	}

	public void add(ElementCountMap elementMap, double weight) {
		for(int i = 0; i < counts.length; i++) {
			if(elementMap.counts[i] > 0) {
				inc((short) i, (int) (elementMap.counts[i] * weight));
			}
		}
		for(int i = 0; i < oreCounts.length; i++) {
			oreCounts[i] += (int) (elementMap.oreCounts[i] * weight);
		}
	}

	public void mult(int mult) {
		for(int i = 0; i < counts.length; i++) {
			if(this.counts[i] > 0) {
				mult((short) i, mult);
			}
		}
		for(int i = 0; i < oreCounts.length; i++) {
			oreCounts[i] *= mult;
		}
	}

	public void decOre(int ore) {
		oreCounts[ore]--;
	}

	public void addOre(int ore) {
		oreCounts[ore]++;
	}

	public void dec(short type) {
		boolean wasPlus = counts[type] > 0;
		counts[type]--;

		if(wasPlus && counts[type] == 0) {
			existingTypeCount--;
		}

		currentPrice -= ElementKeyMap.getInfo(type).getPrice(false);
	}

	public void dec(short type, int count) {
		boolean wasPlus = counts[type] > 0;

		long newCount = Math.max(Integer.MIN_VALUE, (long) counts[type] - (long) count);
		counts[type] = (int) newCount;

		if(wasPlus && counts[type] <= 0) {
			existingTypeCount--;
		}
		if(!wasPlus && counts[type] > 0) {
			existingTypeCount++;
		}

		currentPrice -= (count * ElementKeyMap.getInfo(type).getPrice(false));

	}

	public void deserialize(DataInput stream) throws IOException {
		resetAll();
		int size = stream.readInt();
		int existing = 0;
		for(int i = 0; i < size; i++) {
			short type = stream.readShort();
			int count = stream.readInt();
			if(ElementKeyMap.exists(type)) {
				counts[type] = count;
				existing++;
			}
		}
		existingTypeCount = existing;
		currentPrice = getPrice();
	}

	public int get(short type) {
		return counts[type];
	}

	/**
	 * @return the currentPrice
	 */
	public long getCurrentPrice() {
		return currentPrice;
	}

	public long getPrice() {
		long price = 0;
		for(short d : ElementKeyMap.keySet) {
			price += ElementKeyMap.getInfo(d).getPrice(false) * counts[d];
		}
		if(price < 0) {
			return Long.MAX_VALUE;
		}
		return price;
	}

	public double getMass() {
		double mass = 0;
		for(short d : ElementKeyMap.keySet) {
			mass += (double) ElementKeyMap.getInfoFast(d).getMass() * (double) counts[d];
		}

		return mass;
	}

	public void inc(short type) {
		assert (type < counts.length) : "ERROR: " + type + "/" + counts.length + "  (" + ElementKeyMap.highestType + ")";

		boolean wasZero = counts[type] <= 0;
		long newCount = Math.min(Integer.MAX_VALUE, counts[type] + 1L);

		counts[type] = (int) newCount;
		if(wasZero && counts[type] > 0) {
			existingTypeCount++;
		}
		currentPrice += ElementKeyMap.getInfo(type).getPrice(false);
		if(ElementKeyMap.isLodShape(type)) {
			lodShapes++;
		}
	}

	public void inc(short type, int count) {
		assert (type < counts.length) : "ERROR: " + type + "/" + counts.length + "  (" + ElementKeyMap.highestType + ")";

		boolean wasZero = counts[type] <= 0;
		long newCount = Math.min(Integer.MAX_VALUE, (long) counts[type] + (long) count);
		counts[type] = (int) newCount;
		if(wasZero && counts[type] > 0) {
			existingTypeCount++;
		}
		if(!wasZero && counts[type] <= 0) {
			existingTypeCount--;
		}

		currentPrice += (count * ElementKeyMap.getInfo(type).getPrice(false));

		if(ElementKeyMap.isLodShape(type)) {
			lodShapes += count;
		}
	}

	public void mult(short type, int mult) {
		if(mult < 1) {
			return;
		}
		int oldCount = counts[type];
		long newCount = Math.min(Integer.MAX_VALUE, (long) counts[type] * (long) mult);
		System.out.println("mult " + ElementKeyMap.getInfo(type).name + " count " + oldCount + " with mult " + mult + " = " + newCount);
		counts[type] = (int) newCount;

		currentPrice += ((newCount - oldCount) * ElementKeyMap.getInfo(type).getPrice(false));
	}

	public void load(Short2IntArrayMap elementMap) {
		int existing = 0;
		for(Entry s : elementMap.short2IntEntrySet()) {
			counts[s.getShortKey()] = s.getIntValue();
			if(counts[s.getShortKey()] > 0) {
				existing++;
			}
		}
		existingTypeCount = existing;
		currentPrice = getPrice();
	}

	public void reset(short type) {
		currentPrice -= ElementKeyMap.getInfoFast(type).getPrice(false) * counts[type];
		int c = counts[type];
		counts[type] = 0;
		if(c > 0) {
			existingTypeCount--;
		}
		if(ElementKeyMap.isLodShape(type)) {
			lodShapes -= c;
		}

	}

	public void resetAll() {
		Arrays.fill(counts, 0);
		Arrays.fill(oreCounts, 0);
		currentPrice = 0;
		existingTypeCount = 0;
		lodShapes = 0;
	}

	public void serialize(DataOutput stream) throws IOException {
		assert (sizeOk());
		stream.writeInt(existingTypeCount);
		for(int i = 0; i < counts.length; i++) {
			if(counts[i] > 0) {
				stream.writeShort(i);
				stream.writeInt(counts[i]);
			}
		}
	}

	private boolean sizeOk() {
		int size = 0;
		for(int i = 0; i < counts.length; i++) {
			if(counts[i] > 0) {
				size++;
			}
		}
		if(size != existingTypeCount) {
			System.err.println("Size not ok: " + size + "; " + existingTypeCount);
		}
		return size == existingTypeCount;
	}

	public int getExistingTypeCount() {
		return this.existingTypeCount;
	}

	public byte[] getByteArray() {
		try {
			//size plus one int and one short per entry
			byte[] b = new byte[4 + existingTypeCount * 6];
			DataOutputStream out = new DataOutputStream(new FastByteArrayOutputStream(b));
			serialize(out);
			out.close();
			return b;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void readByteArray(byte[] b) {
		try {
			DataInputStream in = new DataInputStream(new FastByteArrayInputStream(b));
			deserialize(in);
			in.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ElementCountMap) {
			ElementCountMap o = (ElementCountMap) obj;
			return Arrays.equals(counts, o.counts);
		}
		return true;
	}

	public String printList() {
		StringBuilder b = new StringBuilder();
		for(short type : ElementKeyMap.keySet) {
			int am = get(type);
			if(am > 0) {
				b.append(ElementKeyMap.getInfoFast(type).getName()).append(": ").append(am).append("\n");
			}
		}
		return b.toString();
	}

	public void spawnInSpace(SimpleTransformableSendableObject owner) {

		spawnInSpace(owner, new Vector3i(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF));
	}

	public void spawnInSpace(SimpleTransformableSendableObject owner, Vector3i block) {
		Sector sector = ((GameServerState) owner.getState()).getUniverse().getSector(owner.getSectorId());
		if(sector != null) {
			System.err.println("[ELEMENTCOUNTMAP][SPAWNING] spawning element map at " + owner.getWorldTransform().origin);
			Vector3f pos = new Vector3f(block.x - SegmentData.SEG_HALF, block.y - SegmentData.SEG_HALF, block.z - SegmentData.SEG_HALF);
			owner.getWorldTransform().transform(pos);
			for(int i = 0; i < counts.length; i++) {
				short type = (short) i;
				int count = get(type);
				if(count > 0) {

					Vector3f sPos = new Vector3f(pos);
					sPos.x += Math.random() - 0.5;
					sPos.y += Math.random() - 0.5;
					sPos.z += Math.random() - 0.5;
					System.err.println("[ELEMENTCOUNTMAP][SPAWNING] spawning type at -> " + sPos);
					sector.getRemoteSector().addItem(sPos, type, -1, count);

				}
			}
		} else {
			System.err.println("[ELEMENTCOUNTMAP][SPAWN] sector null of " + owner);
		}

	}

	public long getMaxHP() {
		long maxHP = 0;
		for(short d : ElementKeyMap.keySet) {
			maxHP += (long) ElementKeyMap.getInfo(d).structureHP * (long) counts[d] * (long) VoidElementManager.STRUCTURE_HP_BLOCK_MULTIPLIER;
		}
		return maxHP;
	}


	@Override
	public byte getFactoryId() {
		return SerializableTagElement.ELEMENT_COUNT_MAP;
	}

	@Override
	public void writeToTag(DataOutput dos) throws IOException {
		serialize(dos);
	}

	public long getTotalAmount() {
		long c = 0;
		for(short d : ElementKeyMap.keySet) {
			c += counts[d];
		}
		return c;
	}

	public boolean hasLod() {
		return lodShapes > 0;
	}

	/**
	 * returns a weighted representation of this map with amount/totalAmount per type in here
	 */
	public void getWeights(Short2FloatOpenHashMap out) {
		double total = getTotalAmount();
		if(total > 0) {
			for(short d : ElementKeyMap.keySet) {
				out.put(d, (float) (counts[d] / total));
			}
		}
	}

	public void convertNonPlaceableBlocks() {
		for(short type : ElementKeyMap.sourcedTypes) {
			int count = this.get(type);
			//Block has a source, count is added to the source block instead
			if(count > 0) {
				ElementInformation info = ElementKeyMap.getInfoFast(type);
				inc((short) info.getSourceReference(), count);
				dec(info.id, count);
			}
		}
	}

	/**
	 * @return An ElementCountMap composed of the components needed to make this ElementCountMap's blocks. If a block in this ElementCountMap cannot be formed from Components, it will be included in the output as-is.
	 */
	public ElementCountMap calculateComponents() {
		int[] resultingCounts = new int[ElementKeyMap.highestType + 1];
		for(int i = 0; i < counts.length; i++) {
			if(counts[i] > 0) {
				ElementInformation info = ElementKeyMap.getInfoFast((short) i);
				if(info.producedInFactory != 4 || info.getConsistence().isEmpty()) { //either not crafted in the block assembler, or not craftable at all
					resultingCounts[i] = counts[i];
				} else { //it's a craftable block made (presumably) from Components
					List<FactoryResource> consistence = info.getConsistence();
					for(FactoryResource c : consistence) {
						if(ElementKeyMap.getInfo(c.type).getSourceReference() != 0) resultingCounts[ElementKeyMap.getInfo(c.type).getSourceReference()] += (c.count * counts[i]);
						else resultingCounts[c.type] += (c.count * counts[i]);
					}
				}
			}
		}
		ElementCountMap result = new ElementCountMap();
		result.add(resultingCounts, oreCounts);
		return result;
	}

	private void spikeWithProduction(short type, int amount) {
		ElementInformation info = ElementKeyMap.getInfoFast(type);
		List<FactoryResource> consistence = info.getConsistence();
		for(FactoryResource c : consistence) {
			//INSERTED CODE
			//Do not do recursive loops
			if(type == c.type) break;
			///
			inc(c.type, amount * c.count);
			spikeWithProduction(c.type, amount * c.count);
		}
	}

	/**
	 * increases amounts by the consistence of the type
	 * <p>
	 * e.g. if there are 2 block X in here, and block X is made out of 2 Y and 3 Z
	 * this method adds 4Y and 6Z to its map. If Y and Z also have consistences,
	 * those are also added recusively
	 */
	public void spikeWithProduction() {
		ElementCountMap copy = new ElementCountMap(this);
		for(short d : ElementKeyMap.keySet) {
			spikeWithProduction(d, copy.get(d));
		}
	}

	public void put(short type, int amount) {
		inc(type, amount - get(type));

	}

	public boolean isEmpty() {
		return existingTypeCount == 0;
	}

	public double getVolume() {
		double volume = 0;
		for(short d : ElementKeyMap.keySet) {
			volume += ElementKeyMap.getInfoFast(d).volume * (double) counts[d];
		}
		return volume;
	}

	public void transferFrom(ElementCountMap c, double totalCap) {
		double volume = 0;
		for(short d : ElementKeyMap.keySet) {
			if(c.counts[d] > 0) {
				double toAdd = ElementKeyMap.getInfoFast(d).volume * (double) c.counts[d];
				if(volume + toAdd < totalCap) {

					int a = c.counts[d];


					inc(d, a);
					c.inc(d, -a);
					volume += toAdd;
				} else {
					double left = totalCap - volume;
					int amountPossible = (int) (left / ElementKeyMap.getInfoFast(d).volume);
					int a = Math.min(amountPossible, c.counts[d]);

					if(a > 0) {
						inc(d, a);
						c.inc(d, -a);
					}
					break;
				}
			}
		}

	}

	public int getTotalAdded(ElementCountMap map, short type) {
		return map.get(type) + get(type);
	}

	public boolean restrictedBlocks(ElementCountMap map) {
		return
				getTotalAdded(map, ElementKeyMap.FACTION_BLOCK) > 1 ||
						getTotalAdded(map, ElementKeyMap.AI_ELEMENT) > 1 ||
						getTotalAdded(map, ElementKeyMap.SHOP_BLOCK_ID) > 1 ||
						getTotalAdded(map, ElementKeyMap.CORE_ID) > 1
				;
	}

	public int getTotalAdded(int[] map, short type) {
		return map[type] + get(type);
	}

	public boolean restrictedBlocks(int[] map) {
		boolean hasRestrictedBlock = getTotalAdded(map, ElementKeyMap.FACTION_BLOCK) > 1 ||
				getTotalAdded(map, ElementKeyMap.AI_ELEMENT) > 1 ||
				getTotalAdded(map, ElementKeyMap.SHOP_BLOCK_ID) > 1 ||
				getTotalAdded(map, ElementKeyMap.CORE_ID) > 1;
		if(hasRestrictedBlock) {
			return true;
		}


		for(Short elemId : BlockConfig.restrictedBlocks) {
			if(getTotalAdded(map, elemId) > 1) {
				return true;
			}
		}
		return false;
	}

	public String getResourceString() {
		StringBuilder b = new StringBuilder();
		int a = 0;
		for(int i = 0; i < oreCounts.length; i++) {
			int count = oreCounts[i];
			if(count > 0) {
				if(a > 0) {
					b.append("\n");
				}
				ElementInformation info = ElementKeyMap.getInfoFast(ElementKeyMap.orientationToResIDMapping[i + 1]);
				b.append(count).append(" ").append(info.getName());
				a++;
			}
		}
		if(a == 0) {
			return Lng.str("no resrouces scanned!");
		}
		return b.toString();
	}
}
