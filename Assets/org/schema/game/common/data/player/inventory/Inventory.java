package org.schema.game.common.data.player.inventory;

import api.listener.events.inventory.metaobject.InventoryPutMetaItemEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.RemoteInventoryClientAction;
import org.schema.game.network.objects.remote.RemoteInventoryFilter;
import org.schema.game.network.objects.remote.RemoteInventoryMultMod;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.client.ClientState;
import org.schema.schine.network.client.ClientStateInterface;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public abstract class Inventory implements TagSerializable {
    public static final int FULL_RET = -2;
    public static final int FREE_RET = -1;
    public static final int INVENTORY_TO_CREDITS = 1;
    public static final int PLAYER_INVENTORY = 2;
    public static final int STASH_INVENTORY = 3;
    public static final int SHOP_INVENTORY = 4;
    public static final int PERSONAL_FACTORY_INVENTORY = 5;
    public static final int CREATIVE_MODE_INVENTORY = 6;
    public static final int VIRTUEL_CREATIVE_MODE_INVENTORY = 7;
    public static final int NPC_FACTION_INVENTORY = 8;
    public static final String INFINITE_TEXT = "";
    private final Short2LongOpenHashMap countMap = new Short2LongOpenHashMap();
    private final Short2ObjectOpenHashMap<IntSet> slotMap = new Short2ObjectOpenHashMap<IntSet>();
    protected final Int2ObjectAVLTreeMap<InventorySlot> inventoryMap;
    private final ObjectArrayFIFOQueue<ExecutableInventoryRequest> requests = new ObjectArrayFIFOQueue<ExecutableInventoryRequest>();
    private final Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlotsForUpdates = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();
    private long parameter = Long.MIN_VALUE;
    private InventoryHolder inventoryHolder;
    private double volume;
    protected SegmentPiece cachedInvBlock;
    public boolean clientRequestedDetails = false;

    public Inventory(InventoryHolder state, long parameter) {
        inventoryMap = new Int2ObjectAVLTreeMap<InventorySlot>();
        this.inventoryHolder = state;
        this.parameter = parameter;
    }

    public static InventorySlot getNewSlot(int inventoySlot, short type, int count, int meta) {
        if(type < 0 && type != InventorySlot.MULTI_SLOT) {
            assert (count == 0 || meta >= 0) : "Added invalid meta object " + meta + "; type: " + type;
        }
        InventorySlot slot = new InventorySlot();
        slot.setCount(count);
        slot.setType(type);
        slot.metaId = meta;
        slot.slot = inventoySlot;
        assert (slot.slot == inventoySlot);
        assert (slot.getType() == type) : "SLOT: " + slot.getType() + " TRANSMIT: " + type;
        return slot;
    }

    public static void serializeSlotNT(DataOutputStream buffer, int slot, Inventory inventory) throws IOException {
        buffer.writeShort((short) slot);
        short type = inventory.getType(slot);
        buffer.writeInt(inventory.getMeta(slot));
        buffer.writeShort(type);
        if(type != Element.TYPE_NONE) {
            if(type == InventorySlot.MULTI_SLOT) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(inventory.getCount(slot, type));
            }
        }
        InventorySlot inventorySlot = inventory.inventoryMap.get(slot);
        if(inventorySlot != null && inventorySlot.isMultiSlot()) {
            byte b = (byte) inventorySlot.getSubSlots().size();
            buffer.writeByte(b);
            assert (b >= 0);
            buffer.writeUTF(inventorySlot.multiSlot);
            for(int i = 0; i < inventorySlot.getSubSlots().size(); i++) {
                InventorySlot subSlot = inventorySlot.getSubSlots().get(i);
                buffer.writeShort(subSlot.getType());
                buffer.writeInt(subSlot.count());
            }
        } else {
            buffer.writeByte(-1);
        }
    }

    public static void deserializeSlotNT(DataInputStream buffer, InventorySlot out) throws IOException {
        short slot = buffer.readShort();
        int meta = buffer.readInt();
        short type = buffer.readShort();
        if(type != Element.TYPE_NONE) {
            int count = buffer.readInt();
            out.setCount(count);
        }
        out.slot = slot;
        out.setType(type);
        out.metaId = meta;
        byte subSlots = buffer.readByte();
        if(subSlots >= 0) {
            out.multiSlot = buffer.readUTF();
            out.setType(InventorySlot.MULTI_SLOT);
            for(int i = 0; i < subSlots; i++) {
                InventorySlot s = new InventorySlot();
                s.setType(buffer.readShort());
                s.setCount(buffer.readInt());
                out.getSubSlots().add(s);
            }
            Collections.sort(out.getSubSlots(), InventorySlot.subSlotSorter);
            //			System.err.println("RECEIVED MULTI: "+out.slot+"; "+out.multiSlot+": subSlots: "+out.getSubSlots().size()+"; totalCount: "+out.count());
        } else {
            //not a subslot
        }
    }

    public static void serializeSlot(DataOutput buffer, InventorySlot entry) throws IOException {
        int size = 0;
        buffer.writeInt(entry.slot); // 4
        buffer.writeShort(entry.getType()); // 2
        buffer.writeInt(entry.count());// 4
        buffer.writeInt(entry.metaId);// 4
        if(entry.isMultiSlot()) {
            buffer.writeByte(entry.getSubSlots().size());
            buffer.writeUTF(entry.multiSlot);
            for(int i = 0; i < entry.getSubSlots().size(); i++) {
                //				assert(false):entry.getSubSlots().get(i);
                serializeSlot(buffer, entry.getSubSlots().get(i));
            }
        } else {
            buffer.writeByte(-1);
        }
    }

    private static boolean canMerge(InventorySlot original, InventorySlot other) {
        if(original != null && other != null) {
            if(original.isMultiSlot() && other.isMultiSlot()) {
                return original.isMultiSlotCompatibleTo(other);
            }
            if(original.isMultiSlotCompatibleTo(other)) {
                return true;
            }
            return original.getType() == other.getType() && original.metaId == other.metaId && original != other;
        } else {
            return false;
        }
    }

    public void clear() {
        System.err.println("[INVENTORY] CLEARING INVENTORY (CLEAR)");
        this.setVolume(0);
        countMap.clear();
        slotMap.clear();
        inventoryMap.clear();
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    public void clear(IntOpenHashSet mod) {
        //		System.err.println("LKSJLSJLKSJ "+this.getClass().getSimpleName()+": "+inventoryMap.size()+"; "+inventoryMap.keySet());
        for(int s : inventoryMap.keySet()) {
            mod.add(s);
        }
        clear();
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    private boolean checkCountAndSlots() {
        //		if(isInfinite() || true){
        //			return true;
        //		}
        //		for(short s : ElementKeyMap.typeList()){
        //			if(getOverallQuantity(s) != countMap.get(s)){
        //				assert(false):ElementKeyMap.toString(s);
        //				return false;
        //			}
        //
        //			for(InventorySlot slot : inventoryMap.values()){
        //				if(slot.count(s) > 0){
        //
        //					IntSet intSet = slotMap.get(s);
        //
        //					if(intSet == null){
        //						assert(false):slot.isMultiSlot()+"; "+ElementKeyMap.toString(s)+"; "+ElementKeyMap.toString(slot.getType())+"; "+slot.count();
        //						return false;
        //					}
        //					if(!intSet.contains(slot.slot)){
        //						assert(false):slot.isMultiSlot()+"; "+ElementKeyMap.toString(s);
        //						return false;
        //					}
        //				}
        //			}
        //		}
        //		for(Entry<Short, IntSet> e : slotMap.entrySet()){
        //			for(int sl : e.getValue()){
        //				InventorySlot slot = getSlot(sl);
        //				if(slot == null){
        //					assert(false):ElementKeyMap.toString(e.getKey());
        //					return false;
        //				}
        //				if(slot.count(e.getKey()) <= 0){
        //					assert(false):slot.isMultiSlot()+"; "+ElementKeyMap.toString(e.getKey());
        //					return false;
        //				}
        //			}
        //		}
        return true;
    }

    private void recalcCountAndSlots() {
        countMap.clear();
        slotMap.clear();
        for(InventorySlot slot : inventoryMap.values()) {
            addToCountAndSlotMap(slot);
        }
    }

    public void decreaseBatch(ElementCountMap elementMap, IntOpenHashSet mod) throws NotEnoughBlocksInInventoryException {
        for(short type : ElementKeyMap.keySet) {
            int amount = elementMap.get(type);
            if(amount > 0) {
                int availableAmount = getOverallQuantity(type);
                if(availableAmount < amount) {
                    throw new NotEnoughBlocksInInventoryException(type, amount, availableAmount);
                }
            }
        }
        for(short type : ElementKeyMap.keySet) {
            int amount = elementMap.get(type);
            if(amount > 0) {
                decreaseBatch(type, amount, mod);
            }
        }
    }

    public void decreaseBatchIgnoreAmount(ElementCountMap elementMap, IntOpenHashSet mod) {
        for(short type : ElementKeyMap.keySet) {
            int amount = elementMap.get(type);
            if(amount > 0) {
                int availableAmount = getOverallQuantity(type);
                if(availableAmount < amount) {
                    elementMap.put(type, availableAmount);
                }
            }
        }
        for(short type : ElementKeyMap.keySet) {
            int amount = elementMap.get(type);
            if(amount > 0) {
                decreaseBatch(type, amount, mod);
            }
        }
    }

    private IntSet tmpMap = new IntOpenHashSet();

    private IntSet getAllSlotsWithTmp(short type) {
        tmpMap.clear();
        IntSet intSet = slotMap.get(type);
        if(intSet != null) {
            tmpMap.addAll(intSet);
        }
        return tmpMap;
    }

    public void decreaseBatch(short type, int amount, IntOpenHashSet mod) {
        IntSet intSet = getAllSlotsWithTmp(type);
        int rest = amount;
        for(int i : intSet) {
            if(getType(i) == type || (isMultiSlot(i) && isMultiCompatible(i, type))) {
                if(getCount(i, type) > rest) {
                    inc(i, type, -rest);
                    mod.add(i);
                    return;
                } else if(getCount(i, type) == rest) {
                    if(getSlot(i).isMultiSlotCompatibleTo(type)) {
                        getSlot(i).setMulti(type, 0);
                    } else {
                        put(i, (short) 0, 0, -1);
                    }
                    mod.add(i);
                    setVolume(calcVolume());
                    recalcCountAndSlots();
                    assert (checkCountAndSlots());
                    return;
                } else {
                    int countCurrently = getCount(i, type);
                    rest -= countCurrently;
                    inc(i, type, -countCurrently);
                    mod.add(i);
                    assert (checkCountAndSlots());
                }
            }
        }
    }

    @Deprecated
    public void decreaseBatchOld(short type, int amount, IntOpenHashSet mod) {
        if(amount < 0) {
            throw new IllegalArgumentException("amount cannot be < 0 but was " + amount);
        }
        int rest = amount;
        for(InventorySlot s : inventoryMap.values()) {
            int i = s.slot;
            if(getType(i) == type || (isMultiSlot(i) && isMultiCompatible(i, type))) {
                if(getCount(i, type) > rest) {
                    inc(i, type, -rest);
                    mod.add(i);
                    assert (checkVolumeInt());
                    assert (checkCountAndSlots());
                    return;
                } else if(getCount(i, type) == rest) {
                    if(getSlot(i).isMultiSlotCompatibleTo(type)) {
                        getSlot(i).setMulti(type, 0);
                    } else {
                        put(i, (short) 0, 0, -1);
                    }
                    mod.add(i);
                    setVolume(calcVolume());
                    recalcCountAndSlots();
                    assert (checkCountAndSlots());
                    return;
                } else {
                    int countCurrently = getCount(i, type);
                    rest -= countCurrently;
                    if(getSlot(i).isMultiSlotCompatibleTo(type)) {
                        getSlot(i).setMulti(type, 0);
                    } else {
                        put(i, (short) 0, 0, -1);
                    }
                    mod.add(i);
                    assert (checkCountAndSlots());
                }
            }
        }
        recalcCountAndSlots();
        setVolume(calcVolume());
    }

    public void removeFromCountAndSlotMap(InventorySlot slot) {
        if(slot.isMultiSlot()) {
            List<InventorySlot> subSlots = slot.getSubSlots();
            for(InventorySlot ss : subSlots) {
                removeFromCountAndSlotMap(ss, slot.slot);
            }
        } else {
            removeFromCountAndSlotMap(slot, slot.slot);
        }
    }

    public void removeFromCountAndSlotMap(InventorySlot slot, int slotPos) {
        long now = countMap.get(slot.getType()) - slot.count();
        countMap.put(slot.getType(), now);
        removeFromSlotMap(slot.getType(), slotPos);
    }

    private void addToCountAndSlotMap(InventorySlot slot) {
        if(slot.isMultiSlot()) {
            List<InventorySlot> subSlots = slot.getSubSlots();
            for(InventorySlot ss : subSlots) {
                addToCountAndSlotMap(ss, slot.slot);
            }
        } else {
            addToCountAndSlotMap(slot, slot.slot);
        }
    }

    private void addToCountAndSlotMap(InventorySlot slot, int slotPos) {
        long now = countMap.get(slot.getType()) + slot.count();
        countMap.put(slot.getType(), now);
        //		System.err.println("COUNT NOW: "+ElementKeyMap.toString(slot.getType())+"; "+now+"; "+slot.count());
        if(slot.count() > 0) {
            addToSlotMap(slot.getType(), slotPos);
        }
    }

    private void removeFromSlotMap(short type, int slot) {
        IntSet intList = slotMap.get(type);
        if(intList != null) {
            intList.remove(slot);
        }
    }

    private void addToSlotMap(short type, int slot) {
        IntSet intList = slotMap.get(type);
        if(intList == null) {
            intList = new IntOpenHashSet();
            slotMap.put(type, intList);
        }
        intList.add(slot);
    }

    public void deleteAllSlotsWithType(short type, Collection<Integer> changedOut) {
        synchronized(inventoryMap) {
            int oc = 0;
            int i = 0;
            while((oc = getOverallQuantity(type)) > 0) {
                for(Integer s : inventoryMap.keySet()) {
                    InventorySlot inventorySlot = inventoryMap.get(s);
                    double volume = inventorySlot.getVolume();
                    long countBef = inventorySlot.count(type);
                    if(inventorySlot.isMultiSlot()) {
                        double volBef = inventorySlot.getVolume();
                        inventorySlot.removeSubSlot(type);
                        if(inventorySlot.isEmpty()) {
                            inventoryMap.remove(s);
                            this.setVolume(this.volume - volume);
                        } else {
                            this.setVolume(this.volume - (volBef - inventorySlot.getVolume())); //subtract difference
                        }
                        changedOut.add(s);
                        countMap.put(type, countMap.get(type) - countBef);
                        removeFromSlotMap(type, s.intValue());
                    } else if(type == inventorySlot.getType()) {
                        this.setVolume(this.volume - volume);
                        inventoryMap.remove(s);
                        changedOut.add(s);
                        countMap.put(type, countMap.get(type) - countBef);
                        removeFromSlotMap(type, s.intValue());
                    }
                }
                if(i > 500) {
                    try {
                        throw new Exception("Made 100 passes and couldnt remove all " + type + "");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    recalcCountAndSlots();
                    break;
                }
                i++;
            }
        }
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
        assert (checkCountAndSlots());
    }

    public InventorySlot deserializeSlot(DataInput buffer) throws IOException {
        int slot = buffer.readInt();
        short type = buffer.readShort();
        int count = buffer.readInt();
        int meta = buffer.readInt();
        InventorySlot slotNew = getNewSlot(slot, type, count, meta);
        byte subSlots = buffer.readByte();
        if(subSlots >= 0) {
            String multi = buffer.readUTF();
            slotNew.multiSlot = multi;
            slotNew.setType(InventorySlot.MULTI_SLOT);
            slotNew.setInfinite(isInfinite());
            for(int j = 0; j < subSlots; j++) {
                slotNew.getSubSlots().add(deserializeSlot(buffer));
            }
            Collections.sort(slotNew.getSubSlots(), InventorySlot.subSlotSorter);
        }
        return slotNew;
    }

    public void deserialize(DataInput buffer) throws IOException {
        int size = buffer.readInt();
        for(int i = 0; i < size; i++) {
            InventorySlot desSlot = deserializeSlot(buffer);
            InventorySlot inventorySlot = inventoryMap.get(desSlot.slot);
            if(inventorySlot != null) {
                if(inventorySlot.isMultiSlot()) {
                    List<InventorySlot> subSlots = inventorySlot.getSubSlots();
                    for(InventorySlot ss : subSlots) {
                        long now = countMap.get(ss.getType()) - ss.count();
                        countMap.put(ss.getType(), now);
                        if(now <= 0) {
                            removeFromSlotMap(ss.getType(), desSlot.slot);
                        }
                    }
                } else {
                    long now = countMap.get(inventorySlot.getType()) - inventorySlot.count();
                    countMap.put(inventorySlot.getType(), now);
                    if(now <= 0) {
                        removeFromSlotMap(inventorySlot.getType(), desSlot.slot);
                    }
                }
                setVolume(volume - inventorySlot.getVolume());
            }
            inventoryMap.put(desSlot.slot, desSlot);
            if(desSlot.isMultiSlot()) {
                List<InventorySlot> subSlots = desSlot.getSubSlots();
                for(InventorySlot ss : subSlots) {
                    long now = countMap.get(ss.getType()) + ss.count();
                    countMap.put(ss.getType(), now);
                    if(now > 0) {
                        addToSlotMap(ss.getType(), desSlot.slot);
                    }
                }
            } else {
                long now = countMap.get(desSlot.getType()) + desSlot.count();
                countMap.put(desSlot.getType(), now);
                if(now > 0) {
                    addToSlotMap(desSlot.getType(), desSlot.slot);
                }
            }
            setVolume(volume + desSlot.getVolume());
        }
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    public boolean existsInInventory(short type) {
        return getFirstSlot(type, -1, 0, Integer.MAX_VALUE, true) >= 0;
    }

    @Override
    public void fromTagStructure(Tag tag) {
        // discard any other inventory data
        synchronized(inventoryMap) {
            inventoryMap.clear();
            this.setVolume(0);
            countMap.clear();
            slotMap.clear();
        }
        //TODO: remove "inventory".equals(tag.getName()) after a while to save space
        if("inv1".equals(tag.getName()) || "inv".equals(tag.getName()) || "inventory".equals(tag.getName())) {
            Tag[] sub = (Tag[]) tag.getValue();
            Tag slots = sub[0];
            Tag types = sub[1];
            Tag values = sub[2];
            Tag[] slotTag = (Tag[]) slots.getValue();
            Tag[] typeTag = (Tag[]) types.getValue();
            Tag[] countTag = (Tag[]) values.getValue();
            for(int i = 0; i < slotTag.length; i++) {
                if(countTag[i].getType() == Type.INT) {
                    short type = (Short) typeTag[i].getValue();
                    if(ElementKeyMap.exists(type)) {
                        if(ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
                            type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
                        }
                        put((Integer) slotTag[i].getValue(), type, (Integer) countTag[i].getValue(), -1);
                        assert (checkCountAndSlots());
                    } else {
                        System.err.println("[SERVER][INVENTORY][TAG] Exception: tried to load slot with type " + type + "; but that does not exist in inventory");
                    }
                } else if(countTag[i].getType() == Type.STRUCT) {
                    Tag[] meta = (Tag[]) countTag[i].getValue();
                    if(meta[0].getType() == Type.INT) {
                        int id = (Integer) meta[0].getValue();
                        short type = (Short) meta[1].getValue();
                        if(type == MetaObjectType.RECIPE.type) {
                            System.err.println("[SERVER] not loading old recipes");
                            continue;
                        }
                        if(type >= 0) {
                            throw new IllegalArgumentException();
                        }
                        short subId = -1;
                        if(meta[3].getType() == Type.SHORT) {
                            subId = (Short) meta[3].getValue();
                        }
                        //METADATA
                        try {
                            MetaObject o;
                            if(!"inv1".equals(tag.getName())) {
                                //old inventory version. give new id!
                                o = MetaObjectManager.instantiate(type, subId, true); //asigns new id
                                o.fromTag(meta[2]);
                            } else {
                                //new inventory version. id is unique and can be loaded
                                o = MetaObjectManager.instantiate(type, -1, subId); //asigns new id
                                o.fromTag(meta[2]);
                                o.setId(id);
                            }
                            if(getInventoryHolder() != null) {
                                ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().checkCollisionServer(o);
                            }
                            put((Integer) slotTag[i].getValue(), o);
                        } catch(InvalidMetaItemException e) {
                            System.err.println("Exception catched (not fatal): Can continue");
                            e.printStackTrace();
                        }
                    } else {
                        InventorySlot multiSlotFromTag = InventorySlot.getMultiSlotFromTag(countTag[i]);
                        multiSlotFromTag.setInfinite(isInfinite());
                        if(multiSlotFromTag.getType() != InventorySlot.MULTI_SLOT) {
                            put((Integer) slotTag[i].getValue(), multiSlotFromTag.getType(), multiSlotFromTag.count(), -1);
                        } else if(!multiSlotFromTag.isEmpty()) {
                            multiSlotFromTag.slot = (Integer) slotTag[i].getValue();
                            inventoryMap.put((Integer) slotTag[i].getValue(), multiSlotFromTag);
                            setVolume(volume + multiSlotFromTag.getVolume());
                            addToCountAndSlotMap(multiSlotFromTag);
                            assert (inventoryMap.get(slotTag[i].getValue()).isMultiSlot() && inventoryMap.get(slotTag[i].getValue()).getType() == InventorySlot.MULTI_SLOT) : inventoryMap.get(slotTag[i].getValue());
                        } else {
                            System.err.println("[INVENTORY] Exception: " + getInventoryHolder() + " loaded empty multi slot from tag");
                        }
                    }
                } else {
                    assert false;
                }
            }
        } else {
            assert (false);
        }
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    @Override
    public Tag toTagStructure() {
        long t = System.currentTimeMillis();
        Tag slots = new Tag(null, Type.INT);
        Tag types = new Tag(null, Type.SHORT);
        Tag[] v;
        synchronized(inventoryMap) {
            int c = 0;
            for(Integer i : inventoryMap.keySet()) {
                if(!isSlotEmpty(i)) {
                    c++;
                }
            }
            v = new Tag[c + 1];
            v[c] = FinishTag.INST;
            int count = 0;
            for(Integer i : inventoryMap.keySet()) {
                if(!isSlotEmpty(i)) {
                    InventorySlot inventorySlot = inventoryMap.get(i);
                    assert (i == inventorySlot.slot);
                    slots.addTag(new Tag(Type.INT, null, i));
                    types.addTag(new Tag(Type.SHORT, null, inventorySlot.getType()));
                    if(inventorySlot.isMultiSlot()) {
                        v[count] = (inventorySlot.getMultiSlotTag());
                    } else if(inventorySlot.getType() < 0) {
                        assert (inventorySlot.metaId >= 0);
                        MetaObject object = ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().getObject(inventorySlot.metaId);
                        assert (object != null);
                        v[count] = (inventorySlot.getTag(object));
                    } else {
                        v[count] = (inventorySlot.getTag(null));
                    }
                    count++;
                }
            }
        }
        Tag values = new Tag(Type.STRUCT, null, v);
        return new Tag(Type.STRUCT, "inv1", new Tag[]{slots, types, values, FinishTag.INST});
    }

    public int slotsContaining(short type) {
        synchronized(inventoryMap) {
            int c = 0;
            for(InventorySlot i : inventoryMap.values()) {
                if((i.getType() == type && i.count() > 0) || (isMultiCompatible(i.slot, type) && i.count(type) > 0)) {
                    c++;
                }
            }
            return c;
        }
    }

    public abstract int getActiveSlotsMax();

    public int getCount(int slot, short type) {
        if(!isSlotEmpty(slot)) {
            InventorySlot s = getSlot(slot);
            if(s.isMultiSlot()) {
                return s.getMultiCount(type);
            }
            //			assert(type == Element.TYPE_NONE || type == s.getType()):ElementKeyMap.toString(type)+"; "+ElementKeyMap.toString(s.getType());
            //			assert(type == Element.TYPE_NONE || type == s.getType()):ElementKeyMap.toString(type)+"; "+ElementKeyMap.toString(s.getType());
            return s.count();
        }
        return 0;
    }

    /**
     * @param type
     * @return the first slot, that contains elements of 'type' </br>
     * -1, if inventory doesn't contain elements of 'type'
     */
    public int getFirstSlot(short type, boolean containing) {
        return getFirstSlot(type, (short) -1, -1, 0, Integer.MAX_VALUE, containing);
    }

    public int getFirstSlotMulti(short type, short subType, boolean containing) {
        assert (type == InventorySlot.MULTI_SLOT);
        return getFirstSlot(type, subType, -1, 0, Integer.MAX_VALUE, containing);
    }

    public int getFirstNonActiveSlot(short type, boolean containing) {
        return getFirstSlot(type, (short) -1, -1, getActiveSlotsMax(), Integer.MAX_VALUE, containing);
    }

    public int getFirstActiveSlot(short type, boolean containing) {
        return getFirstSlot(type, (short) -1, -1, 0, getActiveSlotsMax(), containing);
    }

    private int getFirstSlot(short type, int meta, int startSlot, int endSlot, boolean containing) {
        return getFirstSlot(type, (short) -1, meta, startSlot, endSlot, containing);
    }

    /**
     * @param type
     * @return the first slot, that contains elements of 'type' </br>
     * -1, if inventory doesn't contain elements of 'type'
     */
    private int getFirstSlot(short type, short subType, int meta, int startSlot, int endSlot, boolean containing) {
        if(type == InventorySlot.MULTI_SLOT) {
            // Search for compatible multi-slot
            for(int s : inventoryMap.keySet()) {
                if(s >= startSlot && s < endSlot && !isSlotEmpty(s)) {
                    if(isMultiSlot(s)) {
                        if(isMultiCompatible(s, subType) && getMeta(s) == meta) {
                            return s;
                        }
                    }
                }
            }
            try {
                return getFreeSlot(startSlot, endSlot);
            } catch(NoSlotFreeException e) {
                return FULL_RET;
            }
        }
        IntSet intSet = slotMap.get(type);
        if(intSet != null && !intSet.isEmpty()) {
            return intSet.iterator().nextInt();
        }
        int c = 0;
        for(int s : inventoryMap.keySet()) {
            c++;
            if(s >= startSlot && s < endSlot && !isSlotEmpty(s)) {
                if(!isMultiSlot(s)) {
                    if(getType(s) == type && getMeta(s) == meta) {
                        return s;
                    } else if(!containing && isMultiCompatible(s, type)) {
                        //if two types are compatible
                        return s;
                    }
                } else {
                    if(containing) {
                        if((getCount(s, type) > 0) && getMeta(s) == meta) {
                            return s;
                        }
                    } else {
                        if(isMultiCompatible(s, type) && getMeta(s) == meta) {
                            return s;
                        }
                    }
                }
            }
        }
        return FREE_RET;
    }

    public boolean isMultiSlot(int s) {
        InventorySlot inventorySlot = inventoryMap.get(s);
        return inventorySlot != null && inventorySlot.isMultiSlot();
    }

    private boolean isMultiCompatible(int s, short type) {
        InventorySlot inventorySlot = inventoryMap.get(s);
        return inventorySlot != null && (inventorySlot.isMultiSlotCompatibleTo(type) || ElementKeyMap.isGroupCompatible(inventorySlot.getType(), type));
    }

    /**
     * @return the first slot, that contains elements of 'type' </br>
     * -1, if inventory doesn't contain elements of 'type'
     */
    public int getFirstSlotMetatype(short metaType) {
        synchronized(inventoryMap) {
            for(Integer s : inventoryMap.keySet()) {
                if(!isSlotEmpty(s) && getType(s) == metaType) {
                    return s;
                }
            }
        }
        return -1;
    }

    public int getFreeNonActiveSlot() throws NoSlotFreeException {
        return getFreeSlot(getActiveSlotsMax(), Integer.MAX_VALUE);
    }

    public int getFreeSlot() throws NoSlotFreeException {
        return getFreeSlot(0, Integer.MAX_VALUE);
    }

    public int getFreeActiveSlot() throws NoSlotFreeException {
        return getFreeSlot(0, getActiveSlotsMax());
    }

    public boolean hasFreeSlot() {
        return !isOverCapacity();
    }

    public boolean isFull() {
        return isOverCapacity();
    }

    public boolean isEmpty() {
        return inventoryMap.isEmpty();
    }

    public int getFreeSlot(int startSlot, int endSlot) throws NoSlotFreeException {
        for(int i = startSlot; i < Math.min(endSlot, Math.max(startSlot + 10, getLastInvKey() + 10)); i++) {
            if(isSlotEmpty(i)) {
                return i;
            }
        }
        throw new NoSlotFreeException();
    }

    /**
     * @return the getInventoryHolder()
     */
    public InventoryHolder getInventoryHolder() {
        return inventoryHolder;
    }

    public void setInventoryHolder(InventoryHolder dd) {
        this.inventoryHolder = dd;
    }

    public int getLocalInventoryType() {
        return 0;
    }

    public int getMeta(int slot) {
        if(!isSlotEmpty(slot)) {
            return getSlot(slot).metaId;
        }
        return -1;
    }

    public boolean containsAny(short type) {
        return getOverallQuantity(type) > 0;
    }

    public int getOverallQuantity(short type) {
        return (int) countMap.get(type);
    }

    @Deprecated
    public int getOverallQuantityOld(short type) {
        int val = 0;
        for(InventorySlot entry : inventoryMap.values()) {
            if(entry.getType() == type) {
                val += entry.count();
            } else if(entry.isMultiSlot()) {
                val += entry.getMultiCount(type);
            }
        }
        return val;
    }

    /**
     * @return the parameter
     */
    public long getParameter() {
        return parameter;
    }

    public int getParameterX() {
        return ElementCollection.getPosX(parameter);
    }

    public int getParameterY() {
        return ElementCollection.getPosY(parameter);
    }

    public int getParameterZ() {
        return ElementCollection.getPosZ(parameter);
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(long parameter) {
        this.parameter = parameter;
    }

    public InventorySlot getSlot(int innventoySlot) {
        InventorySlot slot = inventoryMap.get(innventoySlot);
        return slot;
    }

    public IntSet getSlots() {
        return inventoryMap.keySet();
    }

    public short getType(int slot) {
        if(!isSlotEmpty(slot)) {
            return getSlot(slot).getType();
        }
        return Element.TYPE_NONE;
    }

    public boolean isSlotLocked(int slot) {
        int meta = getMeta(slot);
        MetaObject object = ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().getObject(meta);
        if(object != null && object.isInventoryLocked(this)) {
            return true;
        }
        return false;
    }

    public void handleReceived(InventoryMultMod a, NetworkInventoryInterface inventoryInterface) {
        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
        IntArrayList slotsChanged = null;
        if(getInventoryHolder().getState() instanceof ServerStateInterface) {
            slotsChanged = new IntArrayList();
        }
        for(int i = 0; i < a.receivedMods.length; i++) {
            InventorySlot inventorySlot = a.receivedMods[i];
            inventorySlot.setInfinite(isInfinite());
            //			System.err.println("######## HANDLE ::: "+inventorySlot+"; "+getInventoryHolder()+"; "+getInventoryHolder().getState());
            if(inventorySlot.getType() == Element.TYPE_NONE) {
                InventorySlot remove = inventoryMap.remove(inventorySlot.slot);
                if(remove != null) {
                    setVolume(volume - remove.getVolume());
                    removeFromCountAndSlotMap(remove);
                    onRemoveSlot(remove);
                }
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
            } else {
                if(inventoryMap.containsKey(inventorySlot.slot)) {
                    InventorySlot sl = inventoryMap.get(inventorySlot.slot);
                    setVolume(volume - sl.getVolume());
                    removeFromCountAndSlotMap(sl);
                }
                inventoryMap.put(inventorySlot.slot, inventorySlot);
                setVolume(volume + inventorySlot.getVolume());
                addToCountAndSlotMap(inventorySlot);
                if(!(getInventoryHolder().getState() instanceof ServerStateInterface) && inventorySlot.isMetaItem()) {
                    //MetaObject
                    ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().checkAvailable(inventorySlot.metaId, (MetaObjectState) getInventoryHolder().getState());
                }
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
            }
            if(getInventoryHolder().getState() instanceof ServerStateInterface) {
                slotsChanged.add(inventorySlot.slot);
            }
        }
        if(getInventoryHolder().getState() instanceof ServerStateInterface) {
            getInventoryHolder().sendInventoryModification(slotsChanged, parameter);
        }
        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
    }

    public void inc(int slot, short type, int count) {
        InventorySlot inventorySlot = getSlot(slot);
        if(inventorySlot != null) {
            long newValueL = inventorySlot.count(type) + count;
            int newValue;
            if(newValueL > Integer.MAX_VALUE) {
                newValue = Integer.MAX_VALUE - 1;
            } else if(newValueL < 0) {
                newValue = 0;
            } else {
                newValue = (int) newValueL;
            }
            if(inventorySlot.getType() != type && ElementKeyMap.isGroupCompatible(inventorySlot.getType(), type)) {
                //				System.err.println("GROUP COMPATIBLE INC");
                setVolume(volume - inventorySlot.getVolume());
                removeFromCountAndSlotMap(inventorySlot);
                inventorySlot.setMulti(type, Math.max(0, newValue));
                setVolume(volume + inventorySlot.getVolume());
                addToCountAndSlotMap(inventorySlot);
                assert (newValue == 0 || inventorySlot.isMultiSlot());
            } else if(inventorySlot.isMultiSlot() && inventorySlot.isMultiSlotCompatibleTo(type)) {
                //				System.err.println("MULTI COMPATIBLE INC");
                setVolume(volume - inventorySlot.getVolume());
                removeFromCountAndSlotMap(inventorySlot);
                inventorySlot.setMulti(type, Math.max(0, newValue));
                setVolume(volume + inventorySlot.getVolume());
                addToCountAndSlotMap(inventorySlot);
                assert (newValue == 0 || inventorySlot.isMultiSlot());
            } else {
                //				System.err.println("NORMAL COMPATIBLE INC");
                int cToSet = Math.max(0, newValue);
                if(cToSet == 0) {
                    type = Element.TYPE_NONE;
                }
                put(slot, type, cToSet, -1);
                assert (getCount(slot, type) == newValue) : "TO SET: " + cToSet + "; type: " + type + ";";
            }
        } else {
            if(count < 0) {
                assert false : "TRYING TO SET INVENTORY TO NEGATIVE VALUE";
            } else {
                //				System.err.println("STANDARD NONEXITING INC");
                put(slot, type, Math.max(0, count), -1);
            }
        }
    }

    public boolean checkVolume() {
        boolean checkVolume = checkVolumeInt();
        assert (checkVolume) : volume + "; " + calcVolume() + "; " + inventoryMap;
        return checkVolume;
    }

    public int incExisting(short type, int count) throws NoSlotFreeException {
        int slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, true);
        if(slot < 0) {
            slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, false);
        }
        if(slot >= 0) {
            inc(slot, type, count);
            return slot;
        }
        throw new NoSlotFreeException();
    }

    public int incExistingAndSend(short type, int count, NetworkInventoryInterface o) throws NoSlotFreeException {
        int slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, true);
        if(slot < 0) {
            slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, false);
        }
        if(slot >= 0) {
            inc(slot, type, count);
            IntArrayList l = new IntArrayList(1);
            l.add(slot);
            o.getInventoryMultModBuffer().add(new RemoteInventoryMultMod(new InventoryMultMod(l, this, parameter), o.isOnServer()));
            //			send(o, o.getInventoryUpdateBuffer(), slot);
            return slot;
        }
        throw new NoSlotFreeException();
    }

    public int incExistingOrNextFreeSlot(short type, int count) {
        return incExistingOrNextFreeSlot(type, count, -1);
    }

    public int incExistingOrNextFreeSlotWithoutException(short type, int count) {
        return incExistingOrNextFreeSlotWithoutException(type, count, -1);
    }

    public int incExistingOrNextFreeSlot(short type, int count, int metaId, int startFromSlot) throws NoSlotFreeException {
        //check if we can put it into a slot that actually contains the item
        int slot = getFirstSlot(type, -1, startFromSlot, Integer.MAX_VALUE, true);
        if(slot >= 0) {
            inc(slot, type, count);
            return slot;
        }
        synchronized(inventoryMap) {
            if(metaId == -1) {
                for(Integer s : inventoryMap.keySet()) {
                    if(s > startFromSlot && !isSlotEmpty(s) && (getType(s) == type || isMultiCompatible(s, type))) {
                        inc(s, type, count);
                        return s;
                    }
                }
            }
        }
        return putNextFreeSlot(type, count, metaId, startFromSlot);
    }

    public int incExistingOrNextFreeSlot(short type, int count, int metaId) {
        //check if we can put it into a slot that actually contains the item
        int slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, true);
        if(slot >= 0) {
            inc(slot, type, count);
            return slot;
        }
        synchronized(inventoryMap) {
            if(metaId == -1) {
                for(Integer s : inventoryMap.keySet()) {
                    if(!isSlotEmpty(s) && (getType(s) == type || isMultiCompatible(s, type))) {
                        inc(s, type, count);
                        return s;
                    }
                }
            }
        }
        return putNextFreeSlot(type, count, metaId);
    }

    public int incExistingOrNextFreeSlotWithoutException(short type, int count, int metaId) {
        if(type < 0 && metaId >= 0) {
            return putNextFreeSlotWithoutException(type, count, metaId);
        }
        //check if we can put it into a slot that actually contains the item
        int slot = getFirstSlot(type, -1, 0, Integer.MAX_VALUE, true);
        if(slot >= 0) {
            inc(slot, type, count);
            return slot;
        }
        synchronized(inventoryMap) {
            if(metaId == -1) {
                for(Integer s : inventoryMap.keySet()) {
                    if(!isSlotEmpty(s) && (getType(s) == type || isMultiCompatible(s, type))) {
                        inc(s, type, count);
                        return s;
                    }
                }
            }
        }
        return putNextFreeSlotWithoutException(type, count, metaId);
    }

    public boolean canPutIn(short type, int count) {
        if(isCapacityException(type)) {
            return true;
        }
        tmp.setType(type);
        tmp.setCount(count);
        tmp.metaId = -1;
        return !isOverCapacity(tmp.getVolume());
    }

    InventorySlot tmp = new InventorySlot();

    public boolean canPutIn(short type, int count, int meta) {
        if(isCapacityException(type)) {
            return true;
        }
        tmp.setType(type);
        tmp.setCount(count);
        tmp.metaId = meta;
        return !isOverCapacity(tmp.getVolume());
    }

    public boolean isCapacityException(short type) {
        return (((GameStateInterface) getInventoryHolder().getState()).getGameState().isAllowPersonalInvOverCap() && getInventoryHolder() instanceof PlayerState);
    }

    public int canPutInHowMuch(short type, int count, int meta) {
        if(isCapacityException(type)) {
            return count;
        }
        if(canPutIn(type, count, meta)) {
            return count;
        }
        if(ElementKeyMap.isValidType(type)) {
            ElementInformation n = ElementKeyMap.getInfoFast(type);
            int max = (int) Math.floor((Math.max(0.0, getCapacity() - volume)) / n.getVolume());
            return Math.min(count, max);
        }
        if(meta >= 0) {
            return isOverCapacity(1) ? 0 : 1;
        }
        return 0;
    }

    public boolean canPutInNonMulti(short type, int count) {
        if(isCapacityException(type)) {
            return true;
        }
        InventorySlot s = new InventorySlot();
        s.setType(type);
        s.setCount(count);
        return !isOverCapacity(s.getVolume());
    }

    public boolean inventoryEmpty() {
        return inventoryMap.isEmpty();
    }

    public boolean isSlotEmpty(int slot) {
        InventorySlot inventorySlot = getSlot(slot);
        return inventorySlot == null || inventorySlot.count() <= 0 || inventorySlot.getType() == Element.TYPE_NONE;
    }

    public void put(int slot, MetaObject metaObject) {
        assert (checkVolumeInt()) : volume + "; " + calcVolume() + "; " + inventoryMap;
        short t = metaObject.getObjectBlockID();
        if(t == Element.TYPE_NONE) {
            synchronized(inventoryMap) {
                if(inventoryMap.get(slot) != null) {
                    setVolume(volume - inventoryMap.get(slot).getVolume());
                    removeFromCountAndSlotMap(inventoryMap.get(slot));
                }
                inventoryMap.remove(slot);
            }
            assert (checkVolumeInt()) : volume + "; " + calcVolume() + "; " + inventoryMap;
        } else {
            InventorySlot inventorySlot = inventoryMap.get(slot);
            if(inventorySlot == null) {
                inventorySlot = new InventorySlot();
                inventorySlot.setType(t);
                inventorySlot.setInfinite(isInfinite());
                inventorySlot.slot = slot;
            } else {
                setVolume(volume - inventorySlot.getVolume());
                removeFromCountAndSlotMap(inventorySlot);
            }
            inventorySlot.setCount(1);
            inventorySlot.metaId = metaObject.getId();
            if(getInventoryHolder() != null) {
                ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().putServer(metaObject);
            }
            inventorySlot.setType(t);
            inventorySlot.setInfinite(isInfinite());
            assert (inventorySlot.slot == slot);
            assert (inventorySlot.getType() == t) : "SLOT: " + inventorySlot.getType() + " TRANSMIT: " + t;
            //INSERTED CODE
            //Meta object event fired after all info on the InventorySlot has been set
            InventoryPutMetaItemEvent event = new InventoryPutMetaItemEvent(slot, metaObject, t, inventorySlot);
            StarLoader.fireEvent(event, true); //TODO Confirm this is server-only
            ///

            inventoryMap.put(slot, inventorySlot);
            setVolume(volume + inventorySlot.getVolume());
            addToCountAndSlotMap(inventorySlot);
            assert (checkVolumeInt()) : volume + "; " + calcVolume() + "; " + inventoryMap;
        }
    }

    public boolean isInfinite() {
        return false;
    }

    public InventorySlot setSlot(int slt, short type, int count, int meta) {
        if(type < 0) {
            assert (count == 0 || meta >= 0) : "Added invalid meta object";
        }
        InventorySlot slot = inventoryMap.get(slt);
        if(slot == null) {
            slot = new InventorySlot();
            slot.setType(type);
            slot.slot = slt;
        }
        slot.setCount(count);
        slot.setType(type);
        slot.setInfinite(isInfinite());
        slot.metaId = meta;
        assert (slot.slot == slt);
        assert (slot.getType() == type) : "SLOT: " + slot.getType() + " TRANSMIT: " + type;
        return slot;
    }

    public InventorySlot put(int slot, short type, int count, int meta) {
        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
        if(count <= 0) {
            type = Element.TYPE_NONE;
        }
        if(type == Element.TYPE_NONE) {
            if(!isInfinite() && inventoryMap.get(slot) != null) {
                setVolume(volume - inventoryMap.get(slot).getVolume());
                removeFromCountAndSlotMap(inventoryMap.get(slot));
            }
            InventorySlot remove = inventoryMap.remove(slot);
            assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
            assert (checkCountAndSlots());
            return remove;
        } else {
            int removedPrev = -1;
            InventorySlot isPrev = inventoryMap.get(slot);
            double volPrev = -1;
            if(isPrev != null) {
                volPrev = isPrev.getVolume();
                setVolume(volume - isPrev.getVolume());
                removeFromCountAndSlotMap(isPrev);
                removedPrev = isPrev.count();
                inventoryMap.remove(slot);
                assert (checkVolumeInt()) : volume + ", " + calcVolume();
            }
            //			System.err.println("PUTTING into "+slot+" "+ElementKeyMap.toString(type)+"; meta "+meta+"; count: "+count+"; INV: "+this);
            InventorySlot inventorySlot = setSlot(slot, type, count, meta);
            inventorySlot.setInfinite(isInfinite());
            //no need to ghandle volume. its already done in setSlot
            inventoryMap.put(slot, inventorySlot);
            setVolume(volume + inventorySlot.getVolume());
            addToCountAndSlotMap(inventorySlot);
            assert (checkVolumeInt()) : (isInfinite() ? "infinite" : "") + "; type " + type + "; metaId " + meta + "; count " + count + ";\n remPrevCount " + isPrev + ";\n " + volume + ", " + calcVolume() + ";\n" + volPrev + ", " + inventorySlot.getVolume();
            return inventorySlot;
        }
    }

    private boolean checkVolumeInt() {
        return Math.abs(volume - calcVolume()) < 0.0001f;
    }

    private int getLastInvKey() {
        return inventoryMap.isEmpty() ? 0 : inventoryMap.lastIntKey();
    }

    public int putNextFreeSlot(short type, int count, int metaId, int startSlot) {
        if(startSlot > 0) {
            //start from slot
            for(int i = startSlot; i < Math.max(startSlot + 2, getLastInvKey() + 2); i++) {
                if(isSlotEmpty(i)) {
                    put(i, type, count, metaId);
                    //				System.err.println("NEXT FREE SLOT: "+i+": "+getSlotsMax());
                    return i;
                }
            }
        }
        if(getLastInvKey() == inventoryMap.size() - 1) {
            //all free slots taken. We can take a shortcut
            int i = getLastInvKey() + 1;
            if(isSlotEmpty(i)) {
                put(i, type, count, metaId);
                return i;
            }
        }
        for(int i = 0; i < getLastInvKey() + 2; i++) {
            if(isSlotEmpty(i)) {
                put(i, type, count, metaId);
                return i;
            }
        }
        return -1;
    }

    public int putNextFreeSlotWithoutException(short type, int count, int metaId) {
        return putNextFreeSlotWithoutException(type, count, metaId, 0);
    }

    public int putNextFreeSlotWithoutException(short type, int count, int metaId, int startSlot) {
        // getSlotsMax smaller then zero indicates an infinite inventory (shops)
        if(startSlot > 0) {
            //start from slot
            for(int i = startSlot; i < getLastInvKey() + 2; i++) {
                if(isSlotEmpty(i)) {
                    put(i, type, count, metaId);
                    //				System.err.println("NEXT FREE SLOT: "+i+": "+getSlotsMax());
                    return i;
                }
            }
            for(int i = 0; i < Math.min(startSlot, getLastInvKey() + 2); i++) {
                if(isSlotEmpty(i)) {
                    put(i, type, count, metaId);
                    //				System.err.println("NEXT FREE SLOT: "+i+": "+getSlotsMax());
                    return i;
                }
            }
        } else {
            for(int i = 0; i < getLastInvKey() + 2; i++) {
                if(isSlotEmpty(i)) {
                    put(i, type, count, metaId);
                    //				System.err.println("NEXT FREE SLOT: "+i+": "+getSlotsMax());
                    return i;
                }
            }
        }
        return -1;
    }

    public int putNextFreeSlot(short type, int count, int metaId) {
        return putNextFreeSlot(type, count, metaId, 0);
    }

    public void requestMissingMetaObjects() {
        for(int i = 0; i < getLastInvKey() + 2; i++) {
            if(!isSlotEmpty(i) && getType(i) < 0 && getCount(i, (short) 0) > 0 && getMeta(i) >= 0) {
                ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().checkAvailable(getMeta(i), (MetaObjectState) getInventoryHolder().getState());
            }
        }
    }

    public void sendAllWithExtraSlots(IntOpenHashSet c) {
        for(InventorySlot entry : inventoryMap.values()) {
            c.add(entry.slot);
        }
        sendInventoryModification(c);
    }

    public Int2ObjectAVLTreeMap<InventorySlot> getMap() {
        return inventoryMap;
    }

    public IntOpenHashSet getAllSlots() {
        IntOpenHashSet c = new IntOpenHashSet(inventoryMap.size());
        for(InventorySlot entry : inventoryMap.values()) {
            c.add(entry.slot);
        }
        return c;
    }

    public void sendAll() {
        sendInventoryModification(getAllSlots());
    }

    public void sendInventoryModification(IntCollection changedSlotsOthers) {
        getInventoryHolder().sendInventoryModification(changedSlotsOthers, parameter);
    }

    public void sendInventoryModification(int slot) {
        getInventoryHolder().sendInventoryModification(slot, parameter);
    }

    public void serialize(DataOutput buffer) throws IOException {
        synchronized(inventoryMap) {
            buffer.writeInt(inventoryMap.size());
            for(InventorySlot entry : inventoryMap.values()) {
                serializeSlot(buffer, entry);
            }
        }
    }

    public void spawnInSpace(SimpleTransformableSendableObject<?> owner) {
        spawnInSpace(owner, getParameterIndex());
    }

    public void spawnInSpace(SimpleTransformableSendableObject<?> owner, long parameter, IntOpenHashSet slots) {
        spawnVolumeInSpace(owner, parameter, -1, slots);
    }

    public void spawnInSpace(SimpleTransformableSendableObject<?> owner, long parameter) {
        spawnVolumeInSpace(owner, parameter, -1, null);
    }

    public void spawnVolumeInSpace(SimpleTransformableSendableObject<?> owner, double volume) {
        spawnVolumeInSpace(owner, getParameterIndex(), volume, null);
    }

    public void spawnVolumeInSpace(SimpleTransformableSendableObject<?> owner, long parameter, double spawnSlotsForVolume, IntOpenHashSet slots) {
        boolean vol = spawnSlotsForVolume > 0; //-1 means spawning all the slots
        double volumeBef = volume;
        Sector sector = ((GameServerState) owner.getState()).getUniverse().getSector(owner.getSectorId());
        if(sector != null) {
            Vector3f pos = ElementCollection.getPosFromIndex(parameter, new Vector3f());
            pos.x -= SegmentData.SEG_HALF;
            pos.y -= SegmentData.SEG_HALF;
            pos.z -= SegmentData.SEG_HALF;
            System.err.println("[INVENTORY][SPAWNING] spawning inventory at " + pos);
            owner.getWorldTransform().transform(pos);
            ObjectIterator<InventorySlot> it = inventoryMap.values().iterator();
            while(it.hasNext()) {
                InventorySlot entry = it.next();
                if(entry.getType() != 0) {
                    spawnSlotsForVolume -= entry.getVolume();
                    setVolume(this.volume - entry.getVolume());
                    removeFromCountAndSlotMap(entry);
                    if(entry.getType() == InventorySlot.MULTI_SLOT) {
                        for(int i = 0; i < entry.getSubSlots().size(); i++) {
                            InventorySlot ee = entry.getSubSlots().get(i);
                            Vector3f sPos = new Vector3f(pos);
                            sPos.x += Math.random() - 0.5;
                            sPos.y += Math.random() - 0.5;
                            sPos.z += Math.random() - 0.5;
                            System.err.println("[INVENTORY][SPAWNING] spawning inventory at " + parameter + " -> " + sPos);
                            sector.getRemoteSector().addItem(sPos, ee.getType(), ee.metaId, ee.count());
                        }
                    } else {
                        Vector3f sPos = new Vector3f(pos);
                        sPos.x += Math.random() - 0.5;
                        sPos.y += Math.random() - 0.5;
                        sPos.z += Math.random() - 0.5;
                        System.err.println("[INVENTORY][SPAWNING] spawning inventory at " + parameter + " -> " + sPos);
                        sector.getRemoteSector().addItem(sPos, entry.getType(), entry.metaId, entry.count());
                    }
                    it.remove();
                    if(vol) {
                        //dont send mod when emptying out for a block delete
                        sendInventoryModification(entry.slot);
                    }
                    if(slots != null) {
                        slots.add(entry.slot);
                    }
                    assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                    if(vol) {
                        break;
                    }
                }
            }
        } else {
            System.err.println("[INVENTORY][SPAWN] sector null of " + owner);
        }
    }

    //	if(subSlotFromOther >= 0 && original != null && original.isMultiSlot() && other != null && !other.isMultiSlot()){
//		InventorySlot sub = original.getSubSlots().get(subSlotFromOther);
//		if(count == -1){
//			//take all
//			count = other.count();
//		}
//		original.setMulti(sub.getType(), sub.count()-count);
//
//		other.inc(count);
//
//	}else
    public void doSwitchSlotsOrCombine(int slot, int otherSlot, int subSlotFromOther, Inventory otherInventory, int count, Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots) throws InventoryExceededException {
        if((isLockedInventory() || otherInventory.isLockedInventory()) && this != otherInventory) {
            //cannot put items into this inventory or insert from other
            return;
        }
        if((isSlotLocked(slot) || otherInventory.isSlotLocked(otherSlot)) && this != otherInventory) {
            //cannot put items into this inventory or insert from other
            return;
        }
        if(isInfinite() && this == otherInventory && slot < getActiveSlotsMax() && otherSlot >= getActiveSlotsMax()) {
            //copy over
            InventorySlot inventorySlot = inventoryMap.get(otherSlot);
            InventorySlot s = new InventorySlot(inventorySlot, slot);
            InventorySlot b = inventoryMap.get(slot);
            inventoryMap.put(slot, s);
            IntOpenHashSet intOpenHashSet = moddedSlots.get(this);
            if(intOpenHashSet == null) {
                intOpenHashSet = new IntOpenHashSet();
                moddedSlots.put(this, intOpenHashSet);
            }
            intOpenHashSet.add(slot);
            this.setVolume(this.volume + s.getVolume());
            this.addToCountAndSlotMap(s);
            assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
            assert (checkCountAndSlots());
            return;
        } else if(isInfinite() && this == otherInventory && otherSlot < 10) {
            //remove from hotbar
            inventoryMap.remove(otherSlot);
            return;
        }
        if((otherInventory != null && otherInventory.isCapacityException((short) 0)) || (isCapacityException((short) 0))) {
        } else if(otherInventory != null && this != otherInventory) {
            double ownVolumeOrig = volume;
            double otherVolumeOrig = otherInventory != null ? otherInventory.volume : volume;
            double ownVolume = volume;
            double otherVolume = otherInventory != null ? otherInventory.volume : volume;
            InventorySlot slotA = getSlot(slot);
            InventorySlot slotB = otherInventory.getSlot(otherSlot);
            if(slotB != null && slotB.isMultiSlot() && subSlotFromOther >= 0) {
                slotB = slotB.getSubSlots().get(subSlotFromOther);
                System.err.println("GET SUB SLOT::: " + slotB);
            }
            double slotVolumeA = 0;
            double slotVolumeB = 0;
            if(slotA != null) {
                slotVolumeA = slotA.getVolume();
            }
            if(slotB != null) {
                if(count >= 0 && ElementKeyMap.isValidType(slotB.getType())) {
                    System.err.println("SLOT B VOL COUNT AND VOL: " + slotB.getVolume());
                    slotVolumeB = ElementKeyMap.getInfoFast(slotB.getType()).getVolume() * count;
                } else {
                    System.err.println("SLOT B VOL FROM SLOT: " + slotB.getVolume());
                    slotVolumeB = slotB.getVolume();
                }
            } else {
                System.err.println("SLOT B NULL!");
            }
            if(slotA != null && slotB != null && !slotA.isMultiSlot() && slotA.getType() == slotB.getType()) {
                System.err.println("A IS MULTI SLOT COMPATIBLE TO B same type ownInvClass: " + this.getClass().getSimpleName() + "; " + ElementKeyMap.toString(slotA.getType()) + "; Btype " + ElementKeyMap.toString(slotB.getType()) + "; AVol " + slotVolumeA + "; BVol " + slotVolumeB + "; " + slot + "; " + otherSlot + "; ownVol " + ownVolume + "; otherVol " + otherVolume);
                ownVolume += slotVolumeB;
                otherVolume -= slotVolumeB;
            } else if(slotB != null && slotA != null && slotB.isMultiSlotCompatibleTo(slotA)) {
                System.err.println("B IS MULTI SLOT COMPATIBLE TO A ATyp " + ElementKeyMap.toString(slotA.getType()) + "; Btype " + ElementKeyMap.toString(slotB.getType()) + "; AVol " + slotVolumeA + "; BVol " + slotVolumeB + "; " + slot + "; " + otherSlot + "; ownVol " + ownVolume + "; otherVol " + otherVolume);
                otherVolume -= slotVolumeB;
                ownVolume += slotVolumeB;
            } else {
                ownVolume -= slotVolumeA;
                ownVolume += slotVolumeB;
                otherVolume -= slotVolumeB;
                otherVolume += slotVolumeA;
            }
            double capacityA = getInventoryHolder().getCapacityFor(this);
            double capacityB = otherInventory.getInventoryHolder().getCapacityFor(otherInventory);
            if(ownVolume > ownVolumeOrig && ownVolume > capacityA) {
                System.err.println("[INVENTORY] NOT Executing action: Orig Inventory too small");
                getInventoryHolder().sendInventoryErrorMessage(Lng.astr("Inventory capacity too small"), this);
                if(otherInventory != null) {
                    otherInventory.getInventoryHolder().sendInventoryErrorMessage(Lng.astr("Inventory capacity too small"), this);
                }
                if(!isInfinite() && !otherInventory.isInfinite()) {
                    return;
                }
            }
            if(otherVolume > otherVolumeOrig && otherVolume > capacityB) {
                getInventoryHolder().sendInventoryErrorMessage(Lng.astr("Inventory capacity too small"), this);
                if(otherInventory != null) {
                    otherInventory.getInventoryHolder().sendInventoryErrorMessage(Lng.astr("Inventory capacity too small"), this);
                }
                System.err.println("[INVENTORY] NOT Executing action: Other Inventory too small " + otherVolume + " / " + capacityB + "; SlotA: " + slotVolumeA + "; SlotB: " + slotVolumeB);
                return;
            }
            System.err.println("[INVENTORY] DO Executing action: Other Inventory is ok " + otherVolume + " / " + capacityB + "; SlotA: " + slotVolumeA + "; SlotB: " + slotVolumeB);
        }
        //		if(getSlot(slot) != null){
        //			volSelfBef = getSlot(slot).getVolume();
        //		}
        //
        //		if(otherInventory.getSlot(otherSlot) != null){
        //			volOtherBef = otherInventory.getSlot(otherSlot).getVolume();
        //		}
        synchronized(inventoryMap) {
            InventorySlot original = getSlot(slot);
            InventorySlot other = otherInventory.getSlot(otherSlot);
            if(subSlotFromOther >= 0 && other != null && other.isMultiSlot() && original != null && !original.isMultiSlot()) {
                otherInventory.setVolume(otherInventory.volume - other.getVolume());
                otherInventory.removeFromCountAndSlotMap(other);
                setVolume(volume - original.getVolume());
                removeFromCountAndSlotMap(original);
                InventorySlot sub = other.getSubSlots().get(subSlotFromOther);
                if(count == -1) {
                    //take all
                    count = (int) other.count(sub.getType());
                } else {
                    count = (int) Math.min(count, other.count(sub.getType()));
                }
                short type = sub.getType();
                if(original.getType() == type) {
                    original.inc(count);
                    other.setMulti(type, sub.count() - count);
                } else if(ElementKeyMap.isGroupCompatible(original.getType(), type)) {
                    original.setMulti(type, count);
                    other.setMulti(type, sub.count() - count);
                } else {
                    //cannot put there
                }
                otherInventory.setVolume(otherInventory.volume + other.getVolume());
                otherInventory.addToCountAndSlotMap(other);
                setVolume(volume + original.getVolume());
                addToCountAndSlotMap(original);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                assert (checkCountAndSlots());
                assert (otherInventory.checkCountAndSlots());
            } else if(subSlotFromOther >= 0 && other != null && other.isMultiSlot() && original != null && original.isMultiSlot() && other.getType() == other.getSubSlots().get(subSlotFromOther).getType()) {
                otherInventory.setVolume(otherInventory.volume - other.getVolume());
                otherInventory.removeFromCountAndSlotMap(other);
                setVolume(volume - original.getVolume());
                removeFromCountAndSlotMap(original);
                InventorySlot sub = other.getSubSlots().get(subSlotFromOther);
                if(count == -1) {
                    //take all
                    count = (int) other.count(sub.getType());
                } else {
                    count = (int) Math.min(count, other.count(sub.getType()));
                }
                short type = sub.getType();
                other.setMulti(type, sub.count() - count);
                original.setMulti(type, original.getMultiCount(type) + count);
                otherInventory.setVolume(otherInventory.volume + other.getVolume());
                otherInventory.addToCountAndSlotMap(other);
                setVolume(volume + original.getVolume());
                addToCountAndSlotMap(original);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                assert (checkCountAndSlots());
                assert (otherInventory.checkCountAndSlots());
            } else if(subSlotFromOther >= 0 && other != null && other.isMultiSlot() && original == null) {
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                otherInventory.setVolume(otherInventory.volume - other.getVolume());
                otherInventory.removeFromCountAndSlotMap(other);
                InventorySlot sub = other.getSubSlots().get(subSlotFromOther);
                if(count == -1) {
                    //take all
                    count = (int) other.count(sub.getType());
                } else {
                    count = (int) Math.min(count, other.count(sub.getType()));
                }
                short type = sub.getType();
                other.setMulti(sub.getType(), sub.count() - count);
                otherInventory.setVolume(otherInventory.volume + other.getVolume());
                otherInventory.addToCountAndSlotMap(other);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                put(slot, type, count, -1);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (checkCountAndSlots());
                assert (otherInventory.checkCountAndSlots());
            } else if((original != null && original.isMultiSlot() && other == null)) {
                //multislot to empty
                //				System.err.println("[INVENTORY] MOVING MULTI TO EMPTY "+slot+" -> "+otherSlot);
                InventorySlot newSlot = new InventorySlot(original, slot);
                if(!isInfinite() || this == otherInventory) {
                    InventorySlot remove = inventoryMap.remove(slot);
                    if(!otherInventory.isInfinite() && remove != null) {
                        setVolume(volume - remove.getVolume());
                        removeFromCountAndSlotMap(remove);
                    }
                }
                newSlot.slot = otherSlot;
                newSlot.setInfinite(otherInventory.isInfinite());
                otherInventory.inventoryMap.put(otherSlot, newSlot);
                otherInventory.setVolume(otherInventory.volume + newSlot.getVolume());
                otherInventory.addToCountAndSlotMap(newSlot);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                assert (checkCountAndSlots());
                assert (otherInventory.checkCountAndSlots());
            } else if(other != null && other.isMultiSlot() && original == null) {
                //multislot from empty
                //				System.err.println("[INVENTORY] MOVING EMPTY TO MULTI "+slot+" -> "+otherSlot);
                InventorySlot newSlot = new InventorySlot(other, otherSlot);
                if(!otherInventory.isInfinite() || this == otherInventory) {
                    InventorySlot remove = otherInventory.inventoryMap.remove(otherSlot);
                    if(!otherInventory.isInfinite() && remove != null) {
                        otherInventory.setVolume(otherInventory.volume - remove.getVolume());
                        otherInventory.removeFromCountAndSlotMap(remove);
                    }
                }
                newSlot.slot = slot;
                newSlot.setInfinite(isInfinite());
                inventoryMap.put(slot, newSlot);
                setVolume(volume + newSlot.getVolume());
                addToCountAndSlotMap(newSlot);
                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                assert (checkCountAndSlots());
                assert (otherInventory.checkCountAndSlots());
            } else if(canMerge(original, other)) {
                if(original.isMultiSlotCompatibleTo(other)) {
                    original.mergeMulti(other, count);
                    other.setInfinite(otherInventory.isInfinite());
                    original.setInfinite(isInfinite());
                    if(other.isEmpty()) {
                        //						System.err.println("[INVENTORY] multislot <other> now empty");
                        InventorySlot remove = otherInventory.inventoryMap.remove(otherSlot);
                        if(remove != null) {
                            otherInventory.setVolume(otherInventory.volume - remove.getVolume());
                            otherInventory.removeFromCountAndSlotMap(remove);
                        }
                    }
                    if(isEmpty()) {
                        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                        //						System.err.println("[INVENTORY] multislot <orig> now empty");
                        InventorySlot remove = inventoryMap.remove(slot);
                        if(remove != null) {
                            setVolume(volume - remove.getVolume());
                            removeFromCountAndSlotMap(remove);
                        }
                        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                    }
                    //better to do a recalc on merge :)
                    setVolume(calcVolume());
                    recalcCountAndSlots();
                    if(otherInventory != this) {
                        otherInventory.setVolume(otherInventory.calcVolume());
                        otherInventory.recalcCountAndSlots();
                    }
                } else {
                    //					System.err.println("[INVENTORY] MERGING");
                    //merge the two stacks
                    if(count == -1) {
                        //take all
                        count = other.count();
                    }
                    setVolume(volume - original.getVolume());
                    removeFromCountAndSlotMap(original);
                    otherInventory.setVolume(otherInventory.volume - other.getVolume());
                    otherInventory.removeFromCountAndSlotMap(other);
                    other.inc(-count);
                    original.inc(count);
                    if(other.count() <= 0) {
                        otherInventory.inventoryMap.remove(otherSlot);
                    } else {
                        otherInventory.setVolume(otherInventory.volume + other.getVolume());
                        otherInventory.addToCountAndSlotMap(other);
                    }
                    setVolume(volume + original.getVolume());
                    addToCountAndSlotMap(original);
                    assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                    assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                    assert (checkCountAndSlots());
                    assert (otherInventory.checkCountAndSlots());
                }
            } else {
                if(original != null && other != null && (((original.getType() != other.getType() || original.metaId != other.metaId) && original.getType() != 0) || (original.isMultiSlot() && other.isMultiSlot() && !original.isMultiSlotCompatibleTo(other)))) {
                    //					System.err.println("[INVENTORY] SWITCHING (both slots filled with different types)");
                    if(!isInfinite() || this == otherInventory) {
                        if(other.count() == count || (original.count() == 0 || other.count() == 0) || (original.isMultiSlot() || other.isMultiSlot())) {
                            original.slot = otherSlot;
                            assert (original.getType() != Element.TYPE_NONE);
                            original.setInfinite(otherInventory.isInfinite());
                            otherInventory.inventoryMap.put(otherSlot, original);
                            inventoryMap.remove(slot);
                            other.slot = slot;
                            other.setInfinite(isInfinite());
                            inventoryMap.put(slot, other);
                            if(otherInventory.inventoryMap.get(otherSlot) != original) {
                                otherInventory.inventoryMap.remove(otherSlot);
                            }
                            setVolume(calcVolume());
                            recalcCountAndSlots();
                            if(otherInventory != this) {
                                otherInventory.setVolume(otherInventory.calcVolume());
                                otherInventory.recalcCountAndSlots();
                            }
                            assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                            assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                            assert (checkCountAndSlots());
                            assert (otherInventory.checkCountAndSlots());
                        }
                    }
                } else {
                    //					System.err.println("[INVENTORY] SWITCHING (One slot empty) "+original+"; "+other);
                    if(original != null) {
                        if(other == null || other.count() == 0) {
                            int modCount = count;
                            if(count < 0) {
                                modCount = original.count();
                            } else {
                                modCount = Math.min(modCount, original.count());
                            }
                            InventorySlot newSlot = new InventorySlot(original, otherSlot);
                            newSlot.setCount(modCount);
                            assert (newSlot.getType() != Element.TYPE_NONE);
                            System.err.println("[INVENTORY] PUT NOW OTHER: " + newSlot + " ON " + otherSlot);
                            newSlot.setInfinite(otherInventory.isInfinite());
                            if(otherInventory.inventoryMap.containsKey(otherSlot)) {
                                InventorySlot ssl = otherInventory.inventoryMap.get(otherSlot);
                                otherInventory.setVolume(otherInventory.volume - ssl.getVolume());
                                otherInventory.removeFromCountAndSlotMap(ssl);
                            }
                            otherInventory.inventoryMap.put(otherSlot, newSlot);
                            otherInventory.setVolume(otherInventory.volume + newSlot.getVolume());
                            otherInventory.addToCountAndSlotMap(newSlot);
                            if(!isInfinite() || this == otherInventory) {
                                InventorySlot inventorySlot = inventoryMap.get(slot);
                                if(inventorySlot != null) {
                                    setVolume(volume - inventorySlot.getVolume());
                                    removeFromCountAndSlotMap(inventorySlot);
                                    inventorySlot.inc(-modCount);
                                    if(inventorySlot.count() <= 0) {
                                        inventoryMap.remove(slot);
                                    } else {
                                        setVolume(volume + inventorySlot.getVolume());
                                        addToCountAndSlotMap(inventorySlot);
                                    }
                                }
                                if(isInfinite()) {
                                    recalcCountAndSlots();
                                    setVolume(calcVolume());
                                    if(otherInventory != this) {
                                        otherInventory.recalcCountAndSlots();
                                        otherInventory.setVolume(otherInventory.calcVolume());
                                    }
                                }
                            }
                            assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                            assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                            assert (checkCountAndSlots());
                            assert (otherInventory.checkCountAndSlots());
                        }
                    }
                    if(other != null) {
                        if(original == null || original.count() == 0) {
                            int modCount = count;
                            if(count < 0) {
                                modCount = other.count();
                            } else {
                                modCount = Math.min(modCount, other.count());
                            }
                            InventorySlot newSlot = new InventorySlot(other, slot);
                            newSlot.setCount(modCount);
                            assert (newSlot.getType() != Element.TYPE_NONE);
                            System.err.println("[INVENTORY] PUT NOW ORIGINAL: " + newSlot + " ON " + slot);
                            newSlot.setInfinite(isInfinite());
                            if(inventoryMap.get(slot) != null) {
                                InventorySlot sll = inventoryMap.get(slot);
                                this.setVolume(this.volume - sll.getVolume());
                                removeFromCountAndSlotMap(sll);
                            }
                            inventoryMap.put(slot, newSlot);
                            this.setVolume(this.volume + newSlot.getVolume());
                            addToCountAndSlotMap(newSlot);
                            InventorySlot inventorySlot = otherInventory.inventoryMap.get(otherSlot);
                            if(!otherInventory.isInfinite() || this == otherInventory) {
                                if(inventorySlot != null) {
                                    if(modCount >= inventorySlot.count()) {
                                        InventorySlot remove = otherInventory.inventoryMap.remove(otherSlot);
                                        if(remove != null) {
                                            otherInventory.setVolume(otherInventory.volume - remove.getVolume());
                                            otherInventory.removeFromCountAndSlotMap(remove);
                                        }
                                    } else {
                                        otherInventory.setVolume(otherInventory.volume - inventorySlot.getVolume());
                                        otherInventory.removeFromCountAndSlotMap(inventorySlot);
                                        inventorySlot.inc(-modCount);
                                        if(inventorySlot.count() <= 0) {
                                            otherInventory.inventoryMap.remove(otherSlot);
                                        } else {
                                            otherInventory.setVolume(otherInventory.volume + inventorySlot.getVolume());
                                            otherInventory.addToCountAndSlotMap(inventorySlot);
                                        }
                                    }
                                }
                                if(isInfinite()) {
                                    recalcCountAndSlots();
                                    setVolume(calcVolume());
                                    if(otherInventory != this) {
                                        otherInventory.setVolume(otherInventory.calcVolume());
                                        otherInventory.recalcCountAndSlots();
                                    }
                                }
                                assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                                assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                                assert (checkCountAndSlots());
                            }
                            assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
                            assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
                            assert (checkCountAndSlots());
                        }
                    }
                }
            }
        }
        assert (checkVolumeInt()) : volume + ", " + calcVolume() + "; " + inventoryMap;
        assert (otherInventory.volume == otherInventory.calcVolume()) : otherInventory.volume + ", " + otherInventory.calcVolume() + "; " + otherInventory.inventoryMap;
        assert (checkCountAndSlots());
        assert (otherInventory.checkCountAndSlots());
        IntOpenHashSet ourSet = moddedSlots.get(this);
        if(ourSet == null) {
            ourSet = new IntOpenHashSet();
            moddedSlots.put(this, ourSet);
        }
        IntOpenHashSet otherSet = moddedSlots.get(otherInventory);
        if(otherSet == null) {
            otherSet = new IntOpenHashSet();
            moddedSlots.put(otherInventory, otherSet);
        }
        ourSet.add(slot);
        otherSet.add(otherSlot);
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
        assert (otherInventory.volume == otherInventory.calcVolume());
        assert (checkCountAndSlots());
        assert (otherInventory.checkCountAndSlots());
        //		sendInventoryModification(slot);
        //		otherInventory.sendInventoryModification(otherSlot);
    }

    public void clientUpdate() {
        if(!requests.isEmpty()) {
            moddedSlotsForUpdates.clear();
            synchronized(requests) {
                while(!requests.isEmpty()) {
                    requests.dequeue().execute(moddedSlotsForUpdates);
                }
            }
            //dont send updates (this is now done as an action on the server. the client just gets a headup (which could be overwritten by the server)
            //			if(!moddedSlotsForUpdates.isEmpty()){
            //				for(Entry<Inventory, IntOpenHashSet> e : moddedSlotsForUpdates.entrySet()){
            //					e.getKey().sendInventoryModification(e.getValue());
            //				}
            //			}
        }
    }

    /**
     * @param slot             to put into
     * @param otherSlot        from same or other inv to take from
     * @param subSlotFromOther sub slot (-1 for no subslot or taking whole stack)
     * @param count            amount to take (-1 for everything)
     */
    public void switchSlotsOrCombineClient(int slot, int otherSlot, int subSlotFromOther, int count) {
        switchSlotsOrCombineClient(slot, otherSlot, subSlotFromOther, this, count);
    }

    /**
     * @param slot           to put into
     * @param count          amount to take (-1 for everything)
     */
    public void switchSlotsOrCombineClient(int slot, int otherSlot, int count) {
        switchSlotsOrCombineClient(slot, otherSlot, -1, this, count);
    }

    /**
     * @param slot           to put into
     * @param otherSlot      from same or other inv to take from
     * @param otherInventory inventory to take from
     * @param count          is the amount to take (-1 for everything)
     */
    public void switchSlotsOrCombineClient(int slot, int otherSlot, Inventory otherInventory, int count) {
        switchSlotsOrCombineClient(slot, otherSlot, -1, otherInventory, count);
    }

    /**
     * @param slot             to put into
     * @param otherSlot        from same or other inv to take from
     * @param subSlotFromOther sub slot (-1 for no subslot or taking whole stack)
     * @param otherInventory   inventory to take from
     * @param count            amount to tak (-1 for everything)
     */
    public void switchSlotsOrCombineClient(int slot, int otherSlot, int subSlotFromOther, Inventory otherInventory, int count) {
        SwitchOrCombineRequest s = new SwitchOrCombineRequest(slot, otherSlot, subSlotFromOther, otherInventory, count);
        synchronized(requests) {
            requests.enqueue(s);
        }
    }

    public void removeRequestClient(int slot) {
        getInventoryHolder().sendInventorySlotRemove(slot, parameter);
    }

    public int getCountFilledSlots() {
        int t = 0;
        for(InventorySlot a : inventoryMap.values()) {
            t += a.getType() != Element.TYPE_NONE ? 1 : 0;
        }
        return t;
    }

    public short getProduction() {
        return 0;
    }

    public InventoryFilter getFilter() {
        return null;
    }

    @Override
    public String toString() {
        return "Inventory: (Type" + getLocalInventoryType() + "; Param " + parameter + "; SlotCount: " + inventoryMap.size() + ")";
    }

    public String toStringLong() {
        return "Inventory: (" + getLocalInventoryType() + "; " + parameter + ")" + inventoryMap.toString();
    }

    public List<InventorySlot> getSubSlots(int slot) {
        return inventoryMap.get(slot).getSubSlots();
    }

    public void splitUpMulti(int slot) {
        IntOpenHashSet changed = new IntOpenHashSet();
        changed.add(slot);
        InventorySlot s = inventoryMap.get(slot);
        if(s != null && s.isMultiSlot()) {
            s.splitMulti(this, changed);
        }
        sendInventoryModification(changed);
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    public void removeMetaItem(MetaObject object) {
        for(Integer s : inventoryMap.keySet()) {
            if(getMeta(s) == object.getId()) {
                InventorySlot remove = inventoryMap.remove(s);
                if(remove != null) {
                    setVolume(volume - remove.getVolume());
                    removeFromCountAndSlotMap(remove);
                    onRemoveSlot(remove);
                }
                sendInventoryModification(s);
            }
        }
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    private void onRemoveSlot(InventorySlot remove) {
        if(remove.getType() < 0) {
            if(getInventoryHolder().getState() instanceof ClientStateInterface) {
                ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().clientRemoveObject(remove.metaId);
            } else {
                ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().serverRemoveObject(remove.metaId);
            }
        }
    }

    public boolean conatainsMetaItem(MetaObject object) {
        return conatainsMetaItem(object.getId());
    }

    public boolean conatainsMetaItem(int metaItemId) {
        synchronized(inventoryMap) {
            for(int s : inventoryMap.keySet()) {
                if(getMeta(s) == metaItemId) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getFirstMetaItemByType(short metaItemType, short metaItemSubType) {
        synchronized(inventoryMap) {
            for(Integer s : inventoryMap.keySet()) {
                if(getMeta(s) > 0) {
                    MetaObject object = ((MetaObjectState) getInventoryHolder().getState()).getMetaObjectManager().getObject(getMeta(s));
                    if(object != null && object.getObjectBlockID() == metaItemType && object.getSubObjectId() == metaItemSubType) {
                        return s;
                    }
                }
            }
        }
        return -1;
    }

    public abstract String getCustomName();

    private interface ExecutableInventoryRequest {
        public void execute(Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots);
    }

    private class SwitchOrCombineRequest implements ExecutableInventoryRequest {
        private final int slot;
        private final int subSlot;
        private final int otherSlot;
        private final Inventory otherInventory;
        private final int count;

        public SwitchOrCombineRequest(int slot, int otherSlot, int subSlot, Inventory otherInventory, int count) {
            super();
            this.slot = slot;
            this.subSlot = subSlot;
            this.otherSlot = otherSlot;
            this.otherInventory = otherInventory;
            this.count = count;
        }

        @Override
        public void execute(Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> moddedSlots) {
            assert (getInventoryHolder().getState() instanceof ClientState);
            //			System.err.println("[CLIENT] Executing inventory slot switch/mod: " + toString());
            try {
                doSwitchSlotsOrCombine(slot, otherSlot, subSlot, otherInventory, count, moddedSlots);
                InventoryClientAction action = new InventoryClientAction(Inventory.this, otherInventory, slot, otherSlot, subSlot, count);
                getInventoryHolder().getInventoryNetworkObject().getInventoryClientActionBuffer().add(new RemoteInventoryClientAction(action, false));
            } catch(InventoryExceededException e) {
                e.printStackTrace();
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "SwitchOrCombineRequest [inv: " + Inventory.this + " slot=" + slot + ", subSlot=" + subSlot + ", otherSlot=" + otherSlot + ", otherInventory=" + otherInventory + ", count=" + count + "]";
        }
    }

    public boolean isLockedInventory() {
        return false;
    }

    public long getParameterIndex() {
        return parameter;
    }

    public int getSlotFromMetaId(int id) {
        for(int slot : getSlots()) {
            if(getMeta(slot) == id) {
                return slot;
            }
        }
        return -1;
    }

    public void removeSlot(int slot, boolean send) {
        InventorySlot remove = inventoryMap.remove(slot);
        if(remove != null) {
            setVolume(volume - remove.getVolume());
            removeFromCountAndSlotMap(remove);
            onRemoveSlot(remove);
        }
        if(send) {
            getInventoryHolder().sendInventorySlotRemove(slot, parameter);
        }
        assert (checkVolumeInt());
        assert (checkCountAndSlots());
    }

    double calcVolume() {
        if(isInfinite()) {
            return 0;
        }
        double v = 0;
        for(InventorySlot s : inventoryMap.values()) {
            v += s.getVolume();
        }
        return v;
    }

    public double getVolume() {
        return volume;
    }

    public boolean isOverCapacity(double d) {
        double capacity = getInventoryHolder().getCapacityFor(this);
        return volume + d > capacity;
    }

    public boolean isOverCapacity() {
        double capacity = getInventoryHolder().getCapacityFor(this);
        return volume > capacity;
    }

    public boolean isAlmostFull() {
        double capacity = getInventoryHolder().getCapacityFor(this);
        return volume > 0.9 * capacity;
    }

    public String getVolumeString() {
        double capacity = getInventoryHolder().getCapacityFor(this);
        double pc = capacity > 0 ? volume / capacity : 1d;
        return Lng.str("Capacity: %s/%s Volume Units (%s%%)", StringTools.massFormat(volume), StringTools.massFormat(capacity), StringTools.formatPointZero(pc * 100d)) + (volume > capacity ? Lng.str("  WARNING: over capacity!") : "");
    }

    public void setVolume(double v) {
        if(getInventoryHolder() != null) {
            getInventoryHolder().volumeChanged(this.volume, v);
        }
        this.volume = v;
    }

    public double getCapacity() {
        return getInventoryHolder().getCapacityFor(this);
    }

    public SegmentPiece getBlockIfPossible() {
        if(cachedInvBlock != null) {
            cachedInvBlock.refresh();
            return cachedInvBlock;
        }
        if(getInventoryHolder() instanceof ManagerContainer<?>) {
            cachedInvBlock = ((ManagerContainer<?>) getInventoryHolder()).getSegmentController().getSegmentBuffer().getPointUnsave(getParameterIndex());
            return cachedInvBlock;
        }
        return null;
    }

    public void fromBlockAmountByteArray(byte[] b) {
        clear();
        DataInputStream in = new DataInputStream(new FastByteArrayInputStream(b));
        try {
            final byte version = in.readByte();
            final int size = in.readInt();
            for(int i = 0; i < size; i++) {
                short type = in.readShort();
                int count = in.readInt();
                int meta = -1;
                put(i, type, count, meta);
            }
            in.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] toBlockAmountByteArray() {
        byte VERSION = 0;
        Short2IntOpenHashMap itemMap = new Short2IntOpenHashMap();
        for(InventorySlot slot : inventoryMap.values()) {
            addBlockItemsRec(slot, itemMap);
        }
        int size = 1 + 4 + (itemMap.size() * (2 + 4)); //total size + all items (type, count)
        byte[] b = new byte[size];
        DataOutputStream o = new DataOutputStream(new FastByteArrayOutputStream(b));
        ObjectSet<Entry<Short, Integer>> entrySet = itemMap.entrySet();
        try {
            o.writeByte(VERSION);
            o.writeInt(itemMap.size());
            for(Entry<Short, Integer> e : entrySet) {
                o.writeShort(e.getKey());
                o.writeInt(e.getValue());
            }
            o.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    private void addBlockItemsRec(InventorySlot slot, Short2IntOpenHashMap itemMap) {
        if(slot.isMultiSlot()) {
            for(InventorySlot s : slot.getSubSlots()) {
                addBlockItemsRec(s, itemMap);
            }
        } else if(!slot.isMetaItem()) {
            itemMap.addTo(slot.getType(), slot.count());
        }
    }

    public void addTo(ElementCountMap m) {
        for(InventorySlot s : inventoryMap.values()) {
            s.addTo(m);
        }
    }

    public void incAndConsume(ElementCountMap c, IntCollection o) {
        for(short type : ElementKeyMap.typeList()) {
            if(c.get(type) > 0) {
                int pp = canPutInHowMuch(type, c.get(type), -1);
                if(pp == 0) {
                    return;
                }
                o.add(incExistingOrNextFreeSlot(type, pp));
                c.inc(type, -pp);
            }
        }
    }

    public void addToCountMap(ElementCountMap cm) {
        for(InventorySlot s : inventoryMap.values()) {
            s.addToCountMap(s, cm);
        }
    }

    public void receivedFilterRequest(NetworkSegmentProvider prov) {
        InventoryFilter filter = getFilter();
        if(filter != null) {
            prov.inventoryDetailAnswers.add(new RemoteInventoryFilter(filter, prov.isOnServer()));
            System.err.println("SENDING FILTER ANSWER");
        }
    }

    public int getProductionLimit() {
        return 0;
    }

    public void requestClient(GameClientState state) {
        clientRequestedDetails = true;
    }

    public void receivedFilterAnswer(InventoryFilter inventoryFilter) {
        if(getFilter() != null) {
            getFilter().received(inventoryFilter);
        }
    }
}
