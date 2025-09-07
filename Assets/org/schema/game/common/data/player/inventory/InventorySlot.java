package org.schema.game.common.data.player.inventory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import com.bulletphysics.util.ObjectArrayList;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class InventorySlot {
	public static final short MULTI_SLOT = Short.MIN_VALUE;
	private final List<InventorySlot> subSlots = new ObjectArrayList<InventorySlot>(5);
	public int slot;
	public int metaId = -1;
	public String multiSlot;
	private int count;
	private short type;
	private boolean infinite;
	private static final int INFINITE_COUNT = 9999999;
	public static Comparator<InventorySlot> subSlotSorter = (o1, o2) -> {
		if(ElementKeyMap.isValidType(o1.type) && ElementKeyMap.isValidType(o2.type)) {
			return ElementKeyMap.getInfo(o1.type).getBlockStyle().id - ElementKeyMap.getInfo(o2.type).getBlockStyle().id;
		}
		return 0;
	};

	public InventorySlot() {
	}

	public InventorySlot(InventorySlot s, int newSlot) {
		count = s.count;
		type = s.type;
		metaId = s.metaId;
		slot = newSlot;
		multiSlot = s.multiSlot;
		setInfinite(s.infinite);
		for(int i = 0; i < s.subSlots.size(); i++) {
			if(s.subSlots.get(i) != null) {
				InventorySlot inventorySlot = new InventorySlot(s.subSlots.get(i), i);
				inventorySlot.setInfinite(infinite);
				subSlots.add(inventorySlot);
			}
		}
	}

	public static InventorySlot getMultiSlotFromTag(Tag tag) {
		InventorySlot s = new InventorySlot();
		s.fromMultiSlotTag(tag);
		return s;
	}

	public int count() {
		assert (!isMultiSlot() || type == MULTI_SLOT) : "Multi: " + multiSlot + "; type: " + type + "; slot: " + slot;
		if(type == MULTI_SLOT) {
			long c = 0;
			for(InventorySlot s : subSlots) {
				if(infinite) {
					c += INFINITE_COUNT;
				} else {
					c += s.count();
				}
			}

			return c > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) c;
		}
//		assert(getType() == 0 || count > 0):ElementKeyMap.toString(getType())+"; "+count;
		if(infinite) {
			return INFINITE_COUNT;
		}
		return count;
	}

	public Tag getTag(MetaObject o) {
		if(o == null) {
			return new Tag(Type.INT, null, count);
		} else {
			Tag[] str = new Tag[5];
			str[0] = new Tag(Type.INT, null, o.getId());
			str[1] = new Tag(Type.SHORT, null, o.getObjectBlockID());
			str[2] = o.getBytesTag();
			str[3] = new Tag(Type.SHORT, null, o.getSubObjectId());
			str[4] = FinishTag.INST;
			return new Tag(Type.STRUCT, null, str);
		}
	}

	public void fromMultiSlotTag(Tag t) {
		Tag[] str = (Tag[]) t.getValue();
		multiSlot = (String) str[0].getValue();
		type = InventorySlot.MULTI_SLOT;

		for(int i = 1; i < str.length - 1; i++) {
			Tag[] value = (Tag[]) str[i].getValue();

			short type = (Short) value[0].getValue();
			if(!ElementKeyMap.exists(type)) {
				continue;
			}
			short orig = type;
			if(ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}

			boolean filledIn = false;
			for(int c = 0; c < subSlots.size(); c++) {
				InventorySlot cc = subSlots.get(c);
				if(cc.type == type && cc.count != INFINITE_COUNT) {
//					System.err.println("[SEVRER][INVENTORY][READ] INVENTORY slot conversion CONVERTED "+ElementKeyMap.toString(orig)+" -> "+ElementKeyMap.toString(type)+"; Filled into existing subslot;");
					cc.count += (Integer) value[1].getValue();
					filledIn = true;
					break;
				}
			}

			if(!filledIn) {
				InventorySlot inventorySlot = new InventorySlot();
				inventorySlot.type = type;
				inventorySlot.setCount((Integer) value[1].getValue());

				if(ElementKeyMap.isValidType(inventorySlot.type)) {
					subSlots.add(inventorySlot);
				} else {
					System.err.println("[INVENTORY][TAG] error when loading multislot. contained invalid type");
				}
			}
		}
		if(subSlots.size() == 1) {
			multiSlot = null;
			this.type = subSlots.get(0).type;
			this.count = (subSlots.get(0).count);
			subSlots.clear();
		}
		String a = null;
		long count = 0;
		for(int i = 0; i < subSlots.size(); i++) {
			InventorySlot s = subSlots.get(i);
			if(a == null) {
				a = ElementKeyMap.getInfo(s.type).inventoryGroup;
			} else {
				if(!a.equals(ElementKeyMap.getInfo(s.type).inventoryGroup)) {
					System.err.println("[INVENTORY][TAG] error when loading multislot. contained invalid grouping subslot");
					subSlots.remove(i);
					i--;
				}
			}

			count += s.count();
		}
		if(a != null && !a.equals(multiSlot)) {
			System.err.println("[INVENTORY][TAG] error when loading multislot. loaded grouping invalid: laoded: " + multiSlot + "; but contains: " + a);
			multiSlot = a;
		}
		if(isEmpty()) {
			multiSlot = null;
			type = Element.TYPE_NONE;
		}

	}

	public Tag getMultiSlotTag() {
		Tag[] str = new Tag[subSlots.size() + 2];
		str[0] = new Tag(Type.STRING, null, multiSlot);
		for(int i = 0; i < subSlots.size(); i++) {
			InventorySlot sub = subSlots.get(i);
			str[i + 1] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.SHORT, null, sub.type), new Tag(Type.INT, null, sub.count), FinishTag.INST});
		}
		str[str.length - 1] = FinishTag.INST;
		return new Tag(Type.STRUCT, null, str);
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	public void inc(int by) throws InventoryExceededException {
		long newCount = infinite ? INFINITE_COUNT : ((long) count + (long) by);
		if(newCount <= Integer.MAX_VALUE) {
			count = (int) newCount;
			if(count == 0 && type != MULTI_SLOT) {
				type = Element.TYPE_NONE;
			}
		} else {
			throw new InventoryExceededException();
		}
	}

	public void clear() {
		count = 0;
		type = 0;
		multiSlot = null;
		metaId = -1;
		subSlots.clear();
	}

	public void setCount(int i) {
		if(infinite) {
			count = INFINITE_COUNT;
		} else {
			count = i;
		}
	}

	@Override
	public String toString() {
		return "[slot " + slot + "; t " + ElementKeyMap.toString(type) + (isMultiSlot() ? "; Multislot: " + multiSlot : "") + "; c " + count + "; mt " + metaId + "]";
	}

	public boolean isMultiSlotCompatibleTo(InventorySlot other) {
		if((!isMultiSlot() && !other.isMultiSlot() && type != Element.TYPE_NONE && type == other.type)) {
			//they are compatible, but they are the same block, so we dont have to create multislots
			return false;
		}
//		if(ElementKeyMap.isValidType(other.getType())){
//			System.err.println("OTHER: "+ElementKeyMap.getInfo(other.getType()).getInventoryGroup()+"; oMulti: "+multiSlot);
//		}
//		if(ElementKeyMap.isValidType(getType())){
//			System.err.println("THIS: "+ElementKeyMap.getInfo(getType()).getInventoryGroup()+"; oMulti: "+other.multiSlot);
//		}
//		
//		System.err.println("A::: "+(isMultiSlot() &&  ElementKeyMap.isValidType(other.getType()) && ElementKeyMap.getInfo(other.getType()).hasInventoryGroup() && multiSlot.equals(ElementKeyMap.getInfo(other.getType()).inventoryGroup)));
//		System.err.println("B::: "+(other.isMultiSlot() && ElementKeyMap.isValidType(getType()) && ElementKeyMap.getInfo(getType()).hasInventoryGroup() && other.multiSlot.equals(ElementKeyMap.getInfo(getType()).inventoryGroup)));
		return ((isMultiSlot() && other.isMultiSlot() && multiSlot.equals(other.multiSlot)) ||
				(!isMultiSlot() && !other.isMultiSlot() && ElementKeyMap.isValidType(other.type) && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).hasInventoryGroup() && ElementKeyMap.getInfo(type).inventoryGroup.equals(ElementKeyMap.getInfo(other.type).inventoryGroup)) ||
				(isMultiSlot() && ElementKeyMap.isValidType(other.type) && ElementKeyMap.getInfo(other.type).hasInventoryGroup() && multiSlot.equals(ElementKeyMap.getInfo(other.type).inventoryGroup)) ||
				(other.isMultiSlot() && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).hasInventoryGroup() && other.multiSlot.equals(ElementKeyMap.getInfo(type).inventoryGroup)));
	}

	public void mergeMulti(InventorySlot other, int modCount) throws InventoryExceededException {
		boolean emptyBefore = other.subSlots.isEmpty();
		if(!isMultiSlot() && !other.isMultiSlot() && ElementKeyMap.isValidType(other.type) && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).hasInventoryGroup() && ElementKeyMap.getInfo(type).inventoryGroup.equals(ElementKeyMap.getInfo(other.type).inventoryGroup)) {
			assert (this.type != other.type);
			//combine two combinable slots

			if(modCount < 0) {
				//all
				modCount = other.count();
			} else {
				modCount = Math.min(modCount, other.count());
			}

			String multiS = new String(ElementKeyMap.getInfo(type).inventoryGroup);

			System.err.println("[INVENTORY] MERGING TO MULTI SLOT " + other.multiSlot);
			InventorySlot o = new InventorySlot();
			o.setInfinite(infinite);
			o.type = other.type;
			o.setCount(modCount);

			other.inc(-modCount);
			if(other.count() <= 0) {
				other.type = (short) 0;
			}

			InventorySlot oth = new InventorySlot();
			oth.setInfinite(infinite);
			oth.type = type;
			oth.setCount(count());

			assert (subSlots.isEmpty());
			subSlots.clear();
			//other shouldnt have had subslot before
			subSlots.add(o);
			subSlots.add(oth);

			multiSlot = multiS;
			type = MULTI_SLOT;
			setCount(0);
			assert (checkMultiSlots());
		} else if((isMultiSlot() && other.isMultiSlot() && multiSlot.equals(other.multiSlot))) {
			//multi -> multi
			A:
			for(int i = 0; i < other.subSlots.size(); i++) {
				InventorySlot oth = other.subSlots.get(i);
				for(InventorySlot ori : subSlots) {
					if(oth.type == ori.type) {
						//always take all when combining compatible multiSlots
						int count = oth.count;
						ori.inc(count);
						oth.inc(-count);
						if(oth.count() <= 0) {
							other.subSlots.remove(i);
							i--;
						}
						continue A;
					}
				}
				InventorySlot ori = new InventorySlot();
				ori.setInfinite(infinite);
				ori.type = oth.type;
				subSlots.add(ori);
				//always take all when combining compatible multiSlots
				int count = oth.count;
				ori.inc(count);
				oth.inc(-count);
				if(oth.count() <= 0) {
					other.subSlots.remove(i);
					i--;
				}

			}
			assert (checkMultiSlots());
		} else if(isMultiSlot() && ElementKeyMap.isValidType(other.type) && ElementKeyMap.getInfo(other.type).hasInventoryGroup() && multiSlot.equals(ElementKeyMap.getInfo(other.type).inventoryGroup)) {

			if(modCount == -1) {
				//take all
				modCount = other.count;
			} else {
				modCount = Math.min(modCount, other.count);
			}
			//type -> multi
			boolean found = false;
			for(InventorySlot ori : subSlots) {
				if(other.type == ori.type) {
					ori.inc(modCount);
					other.inc(-modCount);
					found = true;
					break;
				}
			}
			if(!found) {
				InventorySlot ori = new InventorySlot();
				ori.setInfinite(infinite);
				ori.type = other.type;
				subSlots.add(ori);
				ori.inc(modCount);
				other.inc(-modCount);
			}
			assert (checkMultiSlots());
		} else if(other.isMultiSlot() && ElementKeyMap.isValidType(type) && ElementKeyMap.getInfo(type).hasInventoryGroup() && other.multiSlot.equals(ElementKeyMap.getInfo(type).inventoryGroup)) {
			//multi -> type
			String multiS = new String(other.multiSlot);

			//should be empty anyway
			assert (subSlots.isEmpty());
			subSlots.clear();

			//put everything together
			for(int i = 0; i < other.subSlots.size(); i++) {
				InventorySlot oth = other.subSlots.get(i);
				InventorySlot n = new InventorySlot();
				n.setInfinite(infinite);
				n.type = oth.type;

				//take everything from multislot
				int count = oth.count;
				n.inc(count);
				oth.inc(-count);
				subSlots.add(n);

				if(oth.count() <= 0) {
					other.subSlots.remove(i);
					i--;
				}

			}

			//put in the type that were already in the slot

			boolean found = false;
			for(InventorySlot subsOfOri : subSlots) {
				if(type == subsOfOri.type) {
					//take all from our own super slot
					subsOfOri.inc(count);
					inc(-count);
					found = true;
					break;
				}
			}
			if(!found) {
				InventorySlot ori = new InventorySlot();
				ori.setInfinite(infinite);
				ori.type = type;
				ori.inc(count);
				subSlots.add(ori);

				setCount(0);
			}
			this.multiSlot = multiS;
			type = MULTI_SLOT;
			assert (checkMultiSlots());
		} else {
			assert (false);
		}
		if(!emptyBefore && other.subSlots.isEmpty()) {
			other.multiSlot = null;
			other.type = (short) 0;
		}
		if(subSlots.isEmpty()) {
			multiSlot = null;
			type = (short) 0;
		}
		if(other.subSlots.size() == 1) {
			other.type = other.subSlots.get(0).type;
			other.setCount(other.subSlots.get(0).count());
			other.multiSlot = null;
			other.subSlots.clear();
		}
		if(subSlots.size() == 1) {
			type = subSlots.get(0).type;
			setCount(subSlots.get(0).count());
			multiSlot = null;
			subSlots.clear();
		}
		assert (checkMultiSlots());
		assert (other.checkMultiSlots());
		Collections.sort(subSlots, subSlotSorter);
	}

	public boolean isMultiSlot() {
		return multiSlot != null;
	}

	public boolean isEmpty() {
		if(isMultiSlot()) {
			if(subSlots.isEmpty()) {
				return true;
			}
			for(int i = 0; i < subSlots.size(); i++) {
				if(!subSlots.get(i).isEmpty()) {
					return false;
				}
			}
			return true;
		}
		if(infinite) {
			return type == 0;
		}
		return count == 0 || type == 0;
	}

	/**
	 * @return the subSlots
	 */
	public List<InventorySlot> getSubSlots() {
		return subSlots;
	}

	public boolean isMetaItem() {
		return type < 0 && type != MULTI_SLOT;
	}

	public int getMultiCount(short type) {

		int c = 0;
		for(InventorySlot s : subSlots) {
			if(s.type == type) {
				if(infinite) {
					c += INFINITE_COUNT;
				} else {
					c += s.count();
				}
			}
		}
		return c;
	}

	public boolean isMultiSlotCompatibleTo(short type) {
		return ElementKeyMap.isValidType(type) && isMultiSlot() && ElementKeyMap.getInfo(type).getInventoryGroup().equals(multiSlot);
	}

	public void setMulti(short type, int value) {
		if(isMultiSlot()) {
			boolean found = false;
			for(int i = 0; i < subSlots.size(); i++) {
				InventorySlot s = subSlots.get(i);
				if(s.type == type) {
					s.setCount(value);
					found = true;
					if(s.count() <= 0) {
						subSlots.remove(i);
						i--;
					}
					break;
				}
			}
			if(!found && value > 0) {
				//no existing sub found
				InventorySlot s = new InventorySlot();
				s.setInfinite(infinite);
				s.type = type;
				s.setCount(value);
				subSlots.add(s);
			}
			Collections.sort(subSlots, subSlotSorter);
			if(subSlots.isEmpty()) {
				multiSlot = null;
				this.type = (short) 0;
			}
			if(subSlots.size() == 1) {
				this.type = subSlots.get(0).type;
				setCount(subSlots.get(0).count());
				multiSlot = null;
				subSlots.clear();
			}
			assert (checkMultiSlots());
		} else {
			if(value > 0) {
				//make multiSlot
				assert (ElementKeyMap.isGroupCompatible(this.type, type));

				//what was in that slot
//				System.err.println("WAS IN: "+count+"; count for incoming "+value);
				InventorySlot s1 = new InventorySlot();
				s1.setInfinite(infinite);
				s1.type = this.type;
				s1.setCount(count);

				InventorySlot s0 = new InventorySlot();
				s0.setInfinite(infinite);
				s0.type = type;
				s0.setCount(value);
				subSlots.add(s0);
				subSlots.add(s1);

				multiSlot = new String(ElementKeyMap.getInfo(type).getInventoryGroup());
				this.type = MULTI_SLOT;
				setCount(0);

				Collections.sort(subSlots, subSlotSorter);

				assert (isMultiSlot());
				assert (checkMultiSlots());
			}
		}

	}

	private boolean checkMultiSlots() {
		if(isMultiSlot()) {
			assert (type == MULTI_SLOT);
			for(int i = 0; i < subSlots.size(); i++) {
				InventorySlot a = subSlots.get(i);
				assert (a.count > 0);
				assert (ElementKeyMap.isValidType(a.type)) : a.type;
				for(int j = 0; j < subSlots.size(); j++) {
					if(i != j) {
						InventorySlot b = subSlots.get(j);
						assert (a.type != b.type) : ElementKeyMap.toString(a.type);
					}
				}
			}
		}
		return true;
	}

	public long count(short type) {
		if(type == this.type) {
			return count();
		}
		if(isMultiSlot()) {
			return getMultiCount(type);
		}
		if(ElementKeyMap.isGroupCompatible(this.type, type) && type != this.type) {
			return 0;
		}
		return 0;
	}

	public void splitMulti(Inventory inventory, IntOpenHashSet changed) {
		assert (inventory.getVolume() == inventory.calcVolume()) : getVolume() + ", " + inventory.calcVolume() + "; " + inventory.inventoryMap;
		for(int i = 1; i < subSlots.size(); i++) {
			InventorySlot g = subSlots.get(i);
			double n = getVolume();
			InventorySlot remove = subSlots.remove(i);
			if(remove != null) {
				inventory.removeFromCountAndSlotMap(remove);
			}

			double nAft = getVolume();
			inventory.setVolume((inventory.getVolume() - n) + nAft);

			assert (inventory.getVolume() == inventory.calcVolume()) : inventory.getVolume() + ", " + inventory.calcVolume() + "; " + inventory.inventoryMap;

			int slot = inventory.putNextFreeSlotWithoutException(g.type, g.count(), g.metaId, this.slot);
			changed.add(slot);

			i--;
		}

		Collections.sort(subSlots, subSlotSorter);
		if(subSlots.isEmpty()) {
			multiSlot = null;
			type = (short) 0;
		}
		if(subSlots.size() == 1) {
			type = subSlots.get(0).type;
			setCount(subSlots.get(0).count());
			multiSlot = null;
			subSlots.clear();
		}
		assert (inventory.getVolume() == inventory.calcVolume()) : getVolume() + ", " + inventory.calcVolume() + "; " + inventory.inventoryMap;
	}

	public void removeSubSlot(short type) {
		for(int i = 0; i < subSlots.size(); i++) {
			if(subSlots.get(i).type == type) {
				subSlots.remove(i);
				i--;
			}
		}
		Collections.sort(subSlots, subSlotSorter);
		if(subSlots.isEmpty()) {
			multiSlot = null;
			this.type = (short) 0;
		}
		if(subSlots.size() == 1) {

			this.type = subSlots.get(0).type;
			setCount(subSlots.get(0).count());
			multiSlot = null;
			subSlots.clear();

			//System.err.println("SLOT REVERTED FROM MULTISLOT: " + slot + "; is now " + ElementKeyMap.toString(getType()) + "; " + count());
		}
	}

	public void setInfinite(boolean infinite) {
		setInfiniteRec(infinite);
	}

	public boolean isInfinite() {
		return infinite;
	}

	private void setInfiniteRec(boolean inf) {
		infinite = inf;

		for(InventorySlot a : subSlots) {
			a.setInfiniteRec(inf);
		}

	}

	public double getVolume() {
		if(infinite) {
			return 0d;
		}
		if(isMultiSlot()) {
			double volume = 0d;
			for(InventorySlot s : subSlots) {
				volume += s.getVolume();
			}
			return volume;
		}
		if(count > 0) {
			if(ElementKeyMap.isValidType(type)) {

				double vol = (double) ElementKeyMap.getInfoFast(type).getVolume() * (double) count;
				return vol;
			} else if(type < 0) {
				return 1d;
			}
		}
		return 0d;
	}

	public void addTo(ElementCountMap m) {
		if(!isMetaItem() && type != 0) {
			if(isMultiSlot()) {
				for(InventorySlot s : subSlots) {
					s.addTo(m);
				}
			} else {
				m.inc(type, count);
			}
		}
	}

	public InventorySlot getCompatible(short in) {
		if(!ElementKeyMap.isValidType(in)) {
			return null;
		}
		short t = in;
		ElementInformation info = ElementKeyMap.getInfoFast(t);
		if(info.getSourceReference() != 0) {
			t = (short) info.getSourceReference();
		}

		short own = type;

		if(ElementKeyMap.isValidType(own)) {
			ElementInformation oinfo = ElementKeyMap.getInfoFast(own);
			if(oinfo.getSourceReference() != 0) {
				own = (short) oinfo.getSourceReference();
			}
		}
		if(own == t) {
			return this;
		}
		for(InventorySlot slot : subSlots) {
			InventorySlot m = slot.getCompatible(in);
			if(m != null) {
				return m;
			}
		}

		return null;
	}

	public void addToCountMap(InventorySlot s, ElementCountMap m) {
		if(isMultiSlot()) {
			for(InventorySlot ss : subSlots) {
				ss.addToCountMap(ss, m);
			}

		} else if(ElementKeyMap.isValidType(type)) {
			m.inc(type, count);
		}
	}

	public void copyTo(InventorySlot otherSlot) {
		otherSlot.setInfinite(infinite);
		otherSlot.setCount(count);
		otherSlot.type = type;
		otherSlot.metaId = metaId;
		otherSlot.multiSlot = multiSlot;
		otherSlot.subSlots.clear();
		for(InventorySlot s : subSlots) {
			InventorySlot n = new InventorySlot();
			s.copyTo(n);
			otherSlot.subSlots.add(n);
		}
	}

	public void deserialize(ObjectInputStream inputStream) {
		try {
			count = inputStream.readInt();
			type = inputStream.readShort();
			metaId = inputStream.readInt();
			if(inputStream.readBoolean()) multiSlot = inputStream.readUTF();
			infinite = inputStream.readBoolean();
			int size = inputStream.readInt();
			subSlots.clear();
			for(int i = 0; i < size; i++) {
				InventorySlot s = new InventorySlot();
				s.deserialize(inputStream);
				subSlots.add(s);
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public void serialize(ObjectOutputStream outputStream) {
		try {
			outputStream.writeInt(count);
			outputStream.writeShort(type);
			outputStream.writeInt(metaId);
			if(multiSlot != null) {
				outputStream.writeBoolean(true);
				outputStream.writeUTF(multiSlot);
			} else outputStream.writeBoolean(false);
			outputStream.writeBoolean(infinite);
			outputStream.writeInt(subSlots.size());
			for(InventorySlot s : subSlots) s.serialize(outputStream);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
