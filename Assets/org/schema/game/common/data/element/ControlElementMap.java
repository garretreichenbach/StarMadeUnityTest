package org.schema.game.common.data.element;

import api.listener.events.block.ClientSegmentPieceConnectionChangeEvent;
import api.listener.events.entity.SegmentControllerControlMapConnectionAddEvent;
import api.listener.events.entity.SegmentControllerControlMapConnectionRemoveEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.ingame.AbstractBuildControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.io.SegmentSerializationBuffersGZIP;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.remote.RemoteControlMod;
import org.schema.game.network.objects.remote.RemoteControlModBuffer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.DataOutputStreamPositional;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.resource.tag.TagSerializable;

import java.io.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ControlElementMap implements TagSerializable {

	public static final boolean USE_SMART_COMPRESSION = true;
	public static final int SERIALIZATION_VERSION = 2;
	public static final int blockSize = ((3 * ByteUtil.SIZEOF_INT) + ByteUtil.SIZEOF_SHORT);
	private final Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<PositionControl>> positionControlMapChache = new Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<PositionControl>>();
	private final Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<PositionControl>> positionControlMapChacheDirect = new Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<PositionControl>>();
	private final ArrayList<ControlledElementContainer> failedDelayedUpdates = new ArrayList<ControlledElementContainer>();
	private final LongOpenHashSet failedControllerBlocks = new LongOpenHashSet();
	private final List<ControlledElementContainer> delayedNTUpdates = new ObjectArrayList<ControlledElementContainer>();
	private ControlElementMapper delayedNTUpdatesMap = new ControlElementMapper();
	private final LongOpenHashSet loopMap = new LongOpenHashSet();
	private final Vector3i p0 = new Vector3i();
	private final Vector3i po0 = new Vector3i();
	private final Vector3i p1 = new Vector3i();
	private final Vector3i po1 = new Vector3i();
	private final SegmentPiece tmpPiece = new SegmentPiece();
	SegmentPiece pTmp = new SegmentPiece();
	SegmentPiece pointUnsaveTmp = new SegmentPiece();

//	private final LongArrayList toRemoveControlled = new LongArrayList();
	private ControlElementMapper controllingMap = new ControlElementMapper();
	private SendableSegmentController sendableSegmentController;
	private ControlledElementContainer addedDouble;
	private boolean needsControllerUpdates;
	private LongIterator controllingOldMapCheck;
	private AbstractBuildControlManager currentBuildController;
	private boolean structureCompleteChangeFromNT;
	private boolean receivedNT;
	private boolean initialStructureReceived;
	private boolean flagRequested;
	private long lastFailed;
	private boolean loadedFromChunk16;
	public boolean receivedInitialClient;

	public static void deserialize(DataInput dataInputStream, ControlElementMapper m) throws IOException {
		m.deserialize(dataInputStream);
	}

	

	public static ControlElementMapper mapFromTag(Tag tag, ControlElementMapper controllingMap, boolean chunk16) {
		int shift = 0;
		if(chunk16){
			shift = 8;
		}
		if ("cs0".equals(tag.getName())) {
			System.err.println("[ControlElementMap] WARNING: OLD TAG NAME (cs0): " + tag.getName());
			Tag[] controllers = (Tag[]) tag.getValue();
			for (int i = 0; i < controllers.length - 1; i++) {
				Tag[] ent = (Tag[]) controllers[i].getValue();
				Vector3i key = (Vector3i) ent[0].getValue();
				key.add(shift, shift, shift);
				byte[] val = (byte[]) ent[1].getValue();
				int size = val.length / blockSize;
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(val));

				try {
					for (int j = 0; j < size; j++) {
						int x = in.readInt()+shift;
						int y = in.readInt()+shift;
						int z = in.readInt()+shift;
						short type = in.readShort();
						if (ElementKeyMap.exists(type)) {
							controllingMap.put(key, ElementCollection.getIndex(x, y, z), type);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return controllingMap;
		} else if ("cs1".equals(tag.getName())) {
			controllingMap = (ControlElementMapper) tag.getValue();
			return controllingMap;
		} else {
			System.err.println("[ControlElementMap] WARNING: OLD TAG NAME: " + tag.getName());
			Tag[] controllers = (Tag[]) tag.getValue();
			for (int i = 0; i < controllers.length; i++) {
				if (controllers[i].getType() == Type.FINISH) {
					break;
				}
				Tag[] ent = (Tag[]) controllers[i].getValue();
				Vector3i key = (Vector3i) ent[0].getValue();
				key.add(shift, shift, shift);
				Tag[] vTags = (Tag[]) ent[1].getValue();
				for (int c = 0; c < vTags.length; c++) {
					if (vTags[c].getType() == Type.FINISH) {
						break;
					}
					Tag[] elemEntry = (Tag[]) vTags[c].getValue();
					//					ControlledElementContainer controlledElementContainer = new ControlledElementContainer(key, (Vector3i) elemEntry[0].getValue(), (Short) elemEntry[1].getValue(), true, false);
					//					addDelayed(controlledElementContainer);
					if (ElementKeyMap.exists((Short) elemEntry[1].getValue())) {
						Vector3i val = (Vector3i) elemEntry[0].getValue();
						val.add(shift, shift, shift);
						controllingMap.put(key, ElementCollection.getIndex(val), (Short) elemEntry[1].getValue());
					}

					//					addControl(controlledElementContainer);
				}
				//			getControllingMap().put(key, set);

			}
			return controllingMap;
		}
	}

	public static Tag mapToTag(ControlElementMapper controllingMap) {
		return new Tag(Type.SERIALIZABLE, "cs1", controllingMap);
	}

	public static void serializeOptimized(DataOutputStreamPositional outputStream, ControlElementMapper controlElementMapper, ControlElementMapOptimizer optimizer) throws IOException {

		//version
		outputStream.writeInt(-SERIALIZATION_VERSION);

		outputStream.writeInt(controlElementMapper.size());
		int en = 0;
		int sen = 0;
		for (Entry<Long, Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> entry : controlElementMapper
				.entrySet()) {
			en++;

			long keyController = entry.getKey();
			ElementCollection.writeIndexAsShortPos(keyController, outputStream);

			int size = entry.getValue().size();
			outputStream.writeInt(size);

			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> value = entry.getValue();

			for (Entry<Short, FastCopyLongOpenHashSet> v : value.entrySet()) {
				sen++;
				outputStream.writeShort(v.getKey());

				int valuesSize = v.getValue().size();
				outputStream.writeInt(valuesSize);

//				System.err.println("SERIALIZING FOR KEY: "+ElementCollection.getPosX(keyController)+", "+ElementCollection.getPosY(keyController)+", "+ElementCollection.getPosZ(keyController));
				optimizer.serialize(controlElementMapper, v, valuesSize, outputStream);

			}

		}

		assert (!ControlElementMapOptimizer.CHECK_SANITY || optimizer.checkSanity(outputStream, controlElementMapper));

		//		System.err.println("[CONTROLELEMENTMAPPER] WRITTEN "+en+" -> "+sen+" -> "+senen);
	}

	public static void serializeForDisk(DataOutput outputStream, ControlElementMapper controlElementMapper) throws IOException {

		outputStream.writeInt(-(1024+SERIALIZATION_VERSION));
		outputStream.writeInt(controlElementMapper.size());
		int en = 0;
		int sen = 0;
		int senen = 0;
		for (Entry<Long, Short2ObjectOpenHashMap<FastCopyLongOpenHashSet>> entry : controlElementMapper
				.entrySet()) {
			en++;
			ElementCollection.writeIndexAsShortPos(entry.getKey(), outputStream);

			int size = entry.getValue().size();
			outputStream.writeInt(size);

			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> value = entry.getValue();

			for (Entry<Short, FastCopyLongOpenHashSet> v : value.entrySet()) {
				sen++;
				outputStream.writeShort(v.getKey());

				outputStream.writeInt(v.getValue().size());


				for (long g : v.getValue()) {
					ElementCollection.writeIndexAsShortPos(g, outputStream);
					senen++;
				}
			}

		}
	}

	private boolean addControl(ControlledElementContainer c) {
		return addControl(c.from, c.to, c.controlledType, c.send);
	}
	SegmentControllerControlMapConnectionAddEvent mapEventTmp = new SegmentControllerControlMapConnectionAddEvent();
	SegmentControllerControlMapConnectionRemoveEvent mapRemoveEventTmp = new SegmentControllerControlMapConnectionRemoveEvent();
	private boolean addControl(long from, long to, short controlledType, boolean send) {
		if (from == to) {
			System.err.println("WARNING: tried to add controlled element that is equal with the controlling");
			if (!sendableSegmentController.isOnServer()) {
				((GameClientState) sendableSegmentController.getState()).getController().popupAlertTextMessage(Lng.str("Error: Cannot connect a block\nto itself!"), 0);
			}
			return false;
		}

		if (controlledType < 0) {
			throw new IllegalArgumentException("tried to send illegal controller: " + controlledType);
		}

		long elementPosition = ElementCollection.getIndex4(to, controlledType);

//		try {
//			throw new Exception("ADDED: "+ElementCollection.getPosFromIndex(from, new Vector3i())+" -> "+ElementCollection.getPosFromIndex(to, new Vector3i())+": "+ElementKeyMap.toString(controlledType));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		boolean add = controllingMap.put(from, to, controlledType);

		//		if(!sendableSegmentController.isOnServer() && sendableSegmentController instanceof Ship){
		//			System.err.println("HANDLING RECEIVED: "+c+"; success: "+add);
		//		}
		//
		if (send && sendableSegmentController != null) {

			send(from, to, controlledType, true);
		}
		clearCache(controlledType);

		if (add) {
			send = false;
			if (needsControllerUpdates) {
				assert (controlledType != 1);
				onAddedConnectionForManagerContainer(from, to, controlledType, send);
			}
		} 
		
		if (!sendableSegmentController.isOnServer() && (ElementKeyMap.getInfo(controlledType)).drawConnection()) {
			if (((GameClientState) sendableSegmentController.getState()).getWorldDrawer() != null) {
				((GameClientState) sendableSegmentController.getState()).getWorldDrawer().getConnectionDrawerManager().onConnectionChanged(sendableSegmentController);
			}
		}
		//INSERTED CODE
		mapEventTmp.set( from,  to,  controlledType, send, this);
		StarLoader.fireEvent(mapEventTmp, sendableSegmentController.isOnServer());
		///
		return add;
	}
	private void onRemovedConnectionForManagerContainer(long from, long to, short controlledType, boolean send){
		if (controlledType != Element.TYPE_ALL) {
			assert(sendableSegmentController.getState().isSynched()):"State should be synched here. Else running conditions might occur";
			ManagedSegmentController<?> mm = (ManagedSegmentController<?>) sendableSegmentController;
			
			SegmentPiece controllerPiece = sendableSegmentController.getSegmentBuffer().getPointUnsave(from, tmpPiece);
			if (controllerPiece != null) {
				if (controllerPiece.getType() != Element.TYPE_NONE) {
					mm.getManagerContainer().onConnectionRemoved(
							ElementCollection.getPosFromIndex(from, p0), controllerPiece.getType(),
							ElementCollection.getPosFromIndex(to, po0),
							controlledType);
				} else {
					System.err.println("[ERROR] Exception: not executing onRemoveConnection for 0 type " + controllerPiece + " (can happen if controller gets shot down with the controlled)");
				}
			} else {
				failedDelayedUpdates.add(new ControlledElementContainer(from, to, controlledType, true, send));
			}
		}
	}
	private void onAddedConnectionForManagerContainer(long from, long to, short controlledType, boolean send){
		if (controlledType != Element.TYPE_ALL) {
			assert(sendableSegmentController.getState().isSynched()):"State should be synched here. Else running conditions might occur";
			ManagedSegmentController<?> mm = (ManagedSegmentController<?>) sendableSegmentController;
			
			SegmentPiece controllerPiece = sendableSegmentController.getSegmentBuffer().getPointUnsave(from, tmpPiece);
			if (controllerPiece != null) {
				if (controllerPiece.getType() != Element.TYPE_NONE) {
					mm.getManagerContainer().onConnectionAdded(
							ElementCollection.getPosFromIndex(from, p1), controllerPiece.getType(),
							ElementCollection.getPosFromIndex(to, po1),
							controlledType);
				} else {
					System.err.println("[ERROR] Exception: removed executing onAddConnection for 0 type " + controllerPiece + " (can happen if controller gets shot down with the controlled)");
				}
			} else {
				failedDelayedUpdates.add(new ControlledElementContainer(from, to, controlledType, true, send));
			}
		}
	}

	//	private void addControlChain(long from, short type,
//			LongArrayList positionControl, boolean recursive) {
//
//
//		Short2ObjectOpenHashMap<LongOpenHashSet> controlledElementsFrom = getControllingMap().get(from);
//
//		if(controlledElementsFrom != null){
//			if(type == Element.TYPE_ALL){
//				LongOpenHashSet longOpenHashSet = getControllingMap().getAll().get(from);
//
//				positionControl.addControlled(longOpenHashSet);
//				//				System.err.println(sendableSegmentController.getState()+" ADDING CONTROL CHAIN FOR ALL "+ElementCollection.getPosFromIndex(from, new Vector3i())+":::: "+positionControl.getControlMap());
//				if(recursive && longOpenHashSet != null){
//					for(long controller : longOpenHashSet){
//						if(!loopMap.contains(controller)){
//							loopMap.add(controller);
//
//							addControlChain(ElementCollection.getPosIndexFrom4(controller), type, positionControl, recursive);
//						}
//					}
//				}else{
//					//					System.err.println("the connected elements are not controllers: "+controllers);
//				}
//			}else{
//				if(controlledElementsFrom.containsKey(type)){
//					LongOpenHashSet cmap = getControllingMap().get(from).get(type);
//					positionControl.addControlled(cmap);
//				}
//			}
//		}
//	}
	private void addControlChain(long from, short type,
	                             PositionControl positionControl, boolean recursive) {

		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> controlledElementsFrom = controllingMap.get(from);

		if (controlledElementsFrom != null) {
			if (type == Element.TYPE_ALL) {
				LongOpenHashSet longOpenHashSet = controllingMap.getAll().get(from);

				positionControl.addControlled(longOpenHashSet);
				//				System.err.println(sendableSegmentController.getState()+" ADDING CONTROL CHAIN FOR ALL "+ElementCollection.getPosFromIndex(from, new Vector3i())+":::: "+positionControl.getControlMap());
				if (recursive && longOpenHashSet != null) {
					for (long controller : longOpenHashSet) {
						if (!loopMap.contains(controller)) {
							loopMap.add(controller);

							addControlChain(ElementCollection.getPosIndexFrom4(controller), type, positionControl, recursive);
						}
					}
				} else {
					//					System.err.println("the connected elements are not controllers: "+controllers);
				}
			} else {
				if (controlledElementsFrom.containsKey(type)) {
					LongOpenHashSet cmap = controllingMap.get(from).get(type);
					positionControl.addControlled(cmap);
				}
			}
		}
	}

	/**
	 * Main Method used by client and server for adding connections
	 * @param from
	 * @param to
	 * @param toType
	 */
	public void addControllerForElement(long from, long to, short toType) {
		SegmentPiece p = sendableSegmentController.getSegmentBuffer().getPointUnsave(from, pointUnsaveTmp);//autorequest true previously
		if (p == null || p.getType() == Element.TYPE_NONE) {
			System.err.println("[" + sendableSegmentController.getState() + "]" + "[ERROR] " + sendableSegmentController + " add controller failed: " + p);
			return;
		}
		p = sendableSegmentController.getSegmentBuffer().getPointUnsave(to, pointUnsaveTmp);//autorequest true previously
		if (p == null || p.getType() == Element.TYPE_NONE) {
			System.err.println("[" + sendableSegmentController.getState() + "]" + "[ERROR] " + sendableSegmentController + " add controller failed: " + p);
			return;
		}
		
		addControl(from, to, toType, true);
		
		if (currentBuildController != null) {
			currentBuildController.notifyElementChanged();
		}
	}

	/**
	 * @param from
	 * @param to
	 * @param toType
	 * @throws IOException
	 */
	public void addControllerForElement(Vector3i from, Vector3i to, short toType) throws IOException {
		addControllerForElement(ElementCollection.getIndex(from), ElementCollection.getIndex(to), toType);
	}



	public void addDelayed(ControlledElementContainer container) {

		synchronized (delayedNTUpdates) {
//			try {
//				throw new Exception("ADDED: "+container);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			assert (checkContainer(container)):container;
			delayedNTUpdates.add(container);
		}
	}

	private boolean checkContainer(ControlledElementContainer container) {
		if(delayedNTUpdates.contains(container)) {
			try {
				throw new Exception(container.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}






	private void cache(Vector3i from, short type,
	                   PositionControl positionControl, boolean recursive) {
		long index = ElementCollection.getIndex(from);
		Long2ObjectOpenHashMap<Short2ObjectOpenHashMap<PositionControl>> cMap;
		if (recursive) {
			cMap = positionControlMapChache;
		} else {
			cMap = positionControlMapChacheDirect;
		}

		Short2ObjectOpenHashMap<PositionControl> map = cMap.get(index);

		if (map == null) {
			map = new Short2ObjectOpenHashMap<PositionControl>();
			cMap.put(index, map);
		}
		map.put(positionControl.getType(),
				positionControl);
	}

	/**
	 * clears all cache entries that refer to that type
	 * <p/>
	 * this is invoked if a contolChain of a type has been changed so that the
	 * cache for that particular type is rebuilt
	 *
	 * @param type : the type of all Element cache entries to remove from cache
	 */
	private void clearCache(short type) {
		for (Entry<Long, Short2ObjectOpenHashMap<PositionControl>> entries : positionControlMapChache
				.entrySet()) {
			if (entries.getValue().containsKey(type)) {
				entries.getValue().get(type).clear();
			}
		}
		positionControlMapChache.clear();

		for (Entry<Long, Short2ObjectOpenHashMap<PositionControl>> entries : positionControlMapChacheDirect
				.entrySet()) {
			if (entries.getValue().containsKey(type)) {
				entries.getValue().get(type).clear();
			}
		}
		positionControlMapChacheDirect.clear();
	}

	public void deserializeNT(byte[] outputStream, int offset, int length) throws IOException {
		FastByteArrayInputStream v = new FastByteArrayInputStream(outputStream, offset, length);
		DataInputStream d = new DataInputStream(v);
		deserializeNT(d);
	}

	public void deserializeNT(DataInputStream dataInputStream) throws IOException {
		
		receivedNT = true;
		if (!USE_SMART_COMPRESSION) {
			int keySize = dataInputStream.readInt();
			controllingMap.deserializeFromDisk(dataInputStream, keySize);
			return;
		}

		long t = System.currentTimeMillis();
		int keySize = dataInputStream.readInt();
		if (keySize > 0) {
			//WILL NOT HAPPEN ON NT
			assert (false);
			return;
		}
		
		int version = -keySize;
		keySize = dataInputStream.readInt();
		assert (!sendableSegmentController.isOnServer());
		synchronized (delayedNTUpdatesMap) {

			for (int i = 0; i < keySize; i++) {
				short xKey = dataInputStream.readShort();
				short yKey = dataInputStream.readShort();
				short zKey = dataInputStream.readShort();
				long key = ElementCollection.getIndex(xKey, yKey, zKey);

				int valueSize = dataInputStream.readInt();

				for (int v = 0; v < valueSize; v++) {

					short type = dataInputStream.readShort();

					int elementSize = dataInputStream.readInt();

					((GameStateInterface) sendableSegmentController.getState()).getControlOptimizer().deserialize(type, key, elementSize, delayedNTUpdatesMap, dataInputStream);
					structureCompleteChangeFromNT = true;

				}
			}
		}

		long took = (System.currentTimeMillis() - t);
		if (took > 20) {
			System.err.println("RECEIVED " + keySize + " CONTROL ELEMENT MAP ENTRIES FOR " + sendableSegmentController + " TOOK " + took);
		}
		
	}

	public void deserializeZipped(DataInputStream inputStream) throws IOException {
		int debugSegConId = inputStream.readInt();
		int controllerBlockSize = inputStream.readInt();
		int deflatedSize = inputStream.readInt();
		
		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		try {
			bm.ensureSegmentBufferSize(controllerBlockSize);
			Inflater inflater = bm.inflater;
			byte[] buffer = bm.SEGMENT_BUFFER;
			byte[] byteArrayStream = bm.getStaticArray(controllerBlockSize);
		


			assert (deflatedSize <= buffer.length) : deflatedSize + "/" + buffer.length;
			int read = inputStream.read(buffer, 0, deflatedSize);

			assert (read == deflatedSize) : read + "/" + deflatedSize;

			if (deflatedSize == 0) {
				System.err.println("[CONTROLSTRUCTURE] WARNING: controlstructure deserializing with 0 data " + sendableSegmentController);
			}
			inflater.reset();

			inflater.setInput(buffer, 0, deflatedSize);

			int inflate;
			try {
				inflate = inflater.inflate(byteArrayStream, 0, controllerBlockSize);

				assert (inflate == controllerBlockSize) : inflate + " / " + controllerBlockSize + "; deflated " + deflatedSize + "; " + debugSegConId + "; " + sendableSegmentController;

				deserializeNT(byteArrayStream, 0, controllerBlockSize);
				if (inflate == 0) {
					System.err.println("[CONTROLSTRUCTURE] WARNING: INFLATED BYTES 0: " + inflater.needsInput() + " " + inflater.needsDictionary());
				}

			} catch (DataFormatException e) {
				System.err.println("Exception in " + sendableSegmentController);
				e.printStackTrace();
			} catch (IOException e) {

				System.err.println("Exception in " + sendableSegmentController + "; DEBUG-ID: " + debugSegConId);
				throw e;
			}

		}finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}
	}

	public void removeFromRemovedBlock(long e) {
		long index = ElementCollection.getPosIndexFrom4(e);

		removeControlledFromAllControllers(index, (short) ElementCollection.getType(e), true, false);

		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> remove = controllingMap.remove(index);
		if (remove != null) {
			Map<Short, PositionControl> rr = positionControlMapChache.remove(index);
			Map<Short, PositionControl> rrD = positionControlMapChacheDirect.remove(index);
			System.err.println("[ControlElementMap] RemoveControlBlock: REMOVED CACHE: " + index + " ---> " + rr + "; " + positionControlMapChache);
		}
		if (currentBuildController != null) {
			currentBuildController.notifyElementChanged();
		}
	}

	//	public void set(Map<Vector3i, HashSet<ElementPosition>> controllingMap){
	//		this.controllingMap.putAll(controllingMap);
	//	}
	@Override
	public void fromTagStructure(Tag tag) {
		if ("cs1".equals(tag.getName())) {
			controllingMap = (ControlElementMapper) tag.getValue();
		} else if ("cs0".equals(tag.getName())) {
			mapFromTag(tag, controllingMap, true);
		} else {
			mapFromTag(tag, controllingMap, true);
			this.controllingOldMapCheck = controllingMap.keySet().iterator();
			System.err.println("[SERVER][CONTROL-ELEMENT-MAP][TAG][OLD] ADDED CONTROLLER FROM TAG. MAP NOW: " + sendableSegmentController + ". CONTROLLER MAP SIZE: " + delayedNTUpdates.size());
		}

	}

	@Override
	public Tag toTagStructure() {
		return mapToTag(controllingMap);
	}

	public LongOpenHashSet getAllControlledElements(short type) {

		LongOpenHashSet set = new LongOpenHashSet();

		for (long v : controllingMap.keySet()) {
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> elementPositions = controllingMap.get(v);
			LongOpenHashSet objectOpenHashSet = elementPositions.get(type);
			if (objectOpenHashSet != null) {
				for (long ep : objectOpenHashSet) {
					if (ElementCollection.getType(ep) == type) {
						set.add(ep);
					}
				}
			}
		}

		return set;
	}

	private PositionControl getChached(Vector3i from, short type, boolean recusive) {
		return getChached(ElementCollection.getIndex(from), type, recusive);
	}

	private PositionControl getChached(long fromIndex, short type, boolean recusive) {
		Short2ObjectOpenHashMap<PositionControl> map;
		if (recusive) {
			map = positionControlMapChache.get(fromIndex);
		} else {
			map = positionControlMapChacheDirect.get(fromIndex);
		}
		return map.get(type);
	}

	public PositionControl getDirectControlledElements(short type, Vector3i from) {
		try {
			if (isCached(from, type, false)) {
				PositionControl chached = getChached(from, type, false);
				return chached;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.err.println("Exception successfully catched. rebuilding cache");
		}
		PositionControl positionControl = new PositionControl();

		positionControl.setType(type);

		loopMap.clear();
		addControlChain(ElementCollection.getIndex(from), type, positionControl, false);

		cache(from, type, positionControl, false);

		return positionControl;
	}

	public PositionControl getControlledElements(short type, Vector3i from) {
		try {
			if (isCached(from, type, true)) {
				PositionControl chached = getChached(from, type, true);
				return chached;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.err.println("Exception successfully catched. rebuilding cache");
		}
		PositionControl positionControl = new PositionControl();

		positionControl.setType(type);

		loopMap.clear();
		addControlChain(ElementCollection.getIndex(from), type, positionControl, true);

		cache(from, type, positionControl, true);

		return positionControl;
	}

	/**
	 * @return the controllingMap
	 */
	public ControlElementMapper getControllingMap() {
		return controllingMap;
	}

	/**
	 * @return the sendableSegmentController
	 */
	public SendableSegmentController getSegmentController() {
		return sendableSegmentController;
	}


	public void handleReceived() {
		if (!sendableSegmentController.isOnServer() && !initialStructureReceived && !flagRequested) {
			//skip this update since initial control structure has not been received
			return;
		}
		RemoteControlModBuffer controlledByBuffer = sendableSegmentController
				.getNetworkObject().controlledByBuffer;
		for (int i = 0; i < controlledByBuffer.getReceiveBuffer().size(); i++) {
			SendableControlMod a = controlledByBuffer.getReceiveBuffer()
					.get(i).get();
			ControlledElementContainer container = new ControlledElementContainer(
					a.from,
					a.to,
					a.controlledType,
					a.add,
					sendableSegmentController.isOnServer());
			addDelayed(container);

		}
		controlledByBuffer.getReceiveBuffer().clear();

	}

	private boolean isCached(Vector3i from, short type, boolean recusive) {
		return isCached(ElementCollection.getIndex(from), type, recusive);
	}

	private boolean isCached(long fromIndex, short type, boolean recusive) {
		Short2ObjectOpenHashMap<PositionControl> map;
		if (recusive) {
			map = positionControlMapChache.get(fromIndex);
		} else {
			map = positionControlMapChacheDirect.get(fromIndex);
		}

		return map != null && map.containsKey(type);
	}

	public boolean isControlling(long controller, final long controlled, short controlledType) {
		if (controllingMap.containsKey(controller)) {
			long elementPosition = ElementCollection.getIndex4(controlled, controlledType);

			return controllingMap.get(controller).containsKey(controlledType)
					&& controllingMap.get(controller).get(controlledType).contains(elementPosition);
		}
		return false;
	}

	public boolean isControlling(Vector3i controller, final Vector3i controlled, short controlledType) {
		return isControlling(ElementCollection.getIndex(controller), controlled, controlledType);
	}

	public boolean isControlling(long index, final Vector3i controlled, short controlledType) {
		if (controllingMap.containsKey(index)) {
			long elementPosition = ElementCollection.getIndex4(ElementCollection.getIndex(controlled), controlledType);

			return controllingMap.get(index).containsKey(controlledType)
					&& controllingMap.get(index).get(controlledType).contains(elementPosition);
		}
		return false;
	}

	public boolean isControlling(Vector3i controller, final Vector3i controlled, ShortArrayList sList) {
		return isControlling(ElementCollection.getIndex(controller), controlled, sList);
	}

	public boolean isControlling(long index, final Vector3i controlled, ShortArrayList sList) {
//		long index = ElementCollection.getIndex(controller);
		if (controllingMap.containsKey(index)) {
			for (short controlledType : sList) {
				long elementPosition = ElementCollection.getIndex4(ElementCollection.getIndex(controlled), controlledType);

				if (controllingMap.get(index).containsKey(controlledType)
						&& controllingMap.get(index).get(controlledType).contains(elementPosition)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the flagRequested
	 */
	public boolean isFlagRequested() {
		return flagRequested;
	}

	/**
	 * @param flagRequested the flagRequested to set
	 */
	public void setFlagRequested(boolean flagRequested) {
		this.flagRequested = flagRequested;
	}

	public void onRemoveElement(long elementIndex, short removedType) {

		removeFromRemovedBlock(ElementCollection.getIndex4(elementIndex, removedType));

		if (!sendableSegmentController.isOnServer() && ElementKeyMap.isValidType(removedType) && ElementKeyMap.getInfo(removedType).drawConnection()) {
			if (((GameClientState) sendableSegmentController.getState()).getWorldDrawer() != null) {
				((GameClientState) sendableSegmentController.getState()).getWorldDrawer().getConnectionDrawerManager().onConnectionChanged(sendableSegmentController);
			}
		}
	}

	private boolean removeControlled(long controller, long controlled,
	                                 short controlledType, boolean send) {
		boolean removeControlled = false;
		if (controllingMap.containsKey(controller)) {
			if (controllingMap.get(controller).containsKey(controlledType)) {

				removeControlled = controllingMap.remove(controller, controlled, controlledType);
				
//				try {
//					throw new Exception(sendableSegmentController.getState()+" REMOVING CONTROLLRT: "+ElementCollection.getPosFromIndex(controller, new Vector3i())+" -> "+ElementCollection.getPosFromIndex(controlled, new Vector3i())+"; "+ElementKeyMap.toString(controlledType)+"; happened: "+removeControlled);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				System.err.println(sendableSegmentController.getState()+" REMOVING CONTROLLRT: "+ElementCollection.getPosFromIndex(controller, new Vector3i())+" -> "+ElementCollection.getPosFromIndex(controlled, new Vector3i())+"; "+ElementKeyMap.toString(controlledType)+"; happened: "+removeControlled);

				if (removeControlled) {
					//					System.err.println("REMOVING CONTROLLED: "+ElementKeyMap.toString(controlledType)+" -> "+ElementKeyMap.getInfo(controlledType).drawConnection());
					if (!sendableSegmentController.isOnServer() && ElementKeyMap.getInfo(controlledType).drawConnection()) {
						if (((GameClientState) sendableSegmentController.getState()).getWorldDrawer() != null) {
							((GameClientState) sendableSegmentController.getState()).getWorldDrawer().getConnectionDrawerManager().onConnectionChanged(sendableSegmentController);
						}
					}
					if (needsControllerUpdates) {
						assert (controlledType != 1);
						onRemovedConnectionForManagerContainer(controller, controlled, controlledType, send);
					}
				} else {
					/*
					 * controlled block was not connected to this controller.
					 * This happens on any switchController, to
					 * make sure the current controlled block is disconnected
					 * from any other controller.
					 *
					 * If there was no connection present,
					 * there is no need to update the bound ElementCollections
					 */
				}
			}
		}

		if (send && removeControlled && sendableSegmentController != null) {
			send(controller, controlled, controlledType, false);
		}

		if (removeControlled) {
			clearCache(controlledType);
		}
		if (controlledType != Element.TYPE_ALL) {
			removeControlled(controller, controlled, Element.TYPE_ALL, false);
		}

		//INSERTED CODE
		mapRemoveEventTmp.set( controller,  controlled,  controlledType, send, this);
		StarLoader.fireEvent(mapRemoveEventTmp, sendableSegmentController.isOnServer());
		///

		return removeControlled;
	}

	public void removeControlledFromAll(long controlled, short toType, boolean send) {
		removeControlledFromAllControllers(controlled, toType, false, send);
		if (currentBuildController != null) {
			currentBuildController.notifyElementChanged();
		}
	}

	private boolean removeControlledFromAllControllers(long controlled,
	                                                   short controlledType, boolean force, boolean clientSend) {

		if (!force && (ElementInformation.allowsMultiConnect(controlledType))) {
			//factories and signal allow multiconnection
			return false;
		} else {
			boolean removeControlled = false;
			if(ElementKeyMap.isValidType(controlledType)){
				ElementInformation infoFast = ElementKeyMap.getInfoFast(controlledType);
				final boolean res = infoFast.isRestrictedMultiControlled();
				for (long controlling : controllingMap.keySet()) {
					if(res){
						SegmentPiece pointUnsave = sendableSegmentController.getSegmentBuffer().getPointUnsave(controlling);
						ShortArrayList resCon = infoFast.getRestrictedMultiControlled();
						if(pointUnsave != null){
							if(resCon.contains(pointUnsave.getType())){
								removeControlled = removeControlled(controlling, controlled,
										controlledType, clientSend || sendableSegmentController.isOnServer()) | removeControlled;
							}
						}
					}else{
						removeControlled = removeControlled(controlling, controlled,
								controlledType, clientSend || sendableSegmentController.isOnServer()) | removeControlled;
					}
	
				}
			}
			return removeControlled;
		}
	}

	public void removeControllerForElement(long from, long to,
	                                       short toType) {
		removeControlled(from, to, toType, true);
		if (currentBuildController != null) {
			currentBuildController.notifyElementChanged();
		}
	}

	private void send(long from, long to, short controlledType,
	                  boolean add) {
		synchronized (sendableSegmentController.getState()) {
			boolean mustSynch = !sendableSegmentController.getState().isSynched();

			if (mustSynch) {
				try {
					throw new Exception("Debug Warning: synch");
				} catch (Exception e) {
					e.printStackTrace();
				}
				sendableSegmentController.getState().setSynched();
			}

			if (controlledType < 0) {
				throw new IllegalArgumentException("tried to send illegal controller: " + controlledType);
			}
//			try {
//				throw new Exception("MODIFICATION :::  SEND: "+from+" -> "+to+"; "+controlledType+": add "+add);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			RemoteControlMod md = new RemoteControlMod(new SendableControlMod(from, to, controlledType, add), sendableSegmentController.isOnServer());
			assert(!controlBufferContains(md)):md;
			sendableSegmentController.getNetworkObject().controlledByBuffer.add(md);

			if (mustSynch) {
				sendableSegmentController.getState().setUnsynched();
			}
		}
	}

	private boolean controlBufferContains(RemoteControlMod md) {
		for(RemoteControlMod mm : sendableSegmentController.getNetworkObject().controlledByBuffer) {
			if(mm.get().equals(md.get())) {
				return true;
			}
		}
		
		return false;
	}



	public long serializeForNT(FastByteArrayOutputStream v) throws IOException {
		DataOutputStreamPositional d = new DataOutputStreamPositional(v);
		long posBefore = v.position();
		if (USE_SMART_COMPRESSION) {
//			System.err.println("OPT FOR: " + getSegmentController());
			serializeOptimized(d, controllingMap, ((GameStateInterface) sendableSegmentController.getState()).getControlOptimizer());
		} else {
			serializeForDisk(d, controllingMap);
		}
		long posAfter = v.position();
		long size = posAfter - posBefore;

//		System.err.println("SERIALIZE____________CMAP"+getControllingMap().size()+"; SIZE: "+StringTools.readableFileSize(size)+"; predicted: "+StringTools.readableFileSize(getByteSize(getControllingMap())));
//		assert(size == controllerBlockSize):size+"/"+controllerBlockSize+" in "+getSegmentController()+"; total "+controllingMap.size()+"; "+controllingMap.getAll().size();
		return size;
	}

	public int serializeZippedForNT(DataOutputStream outputStream) throws IOException {

		int zipSize = 0;
		byte[] buffer;
		Deflater deflater;
		
		outputStream.writeInt(sendableSegmentController.getId()); //DEBUG, remove this later
//		int controllerBlockSize = getByteSize(getControllingMap());
//		if(controllerBlockSize > 1024*1024){
//			System.err.println(getSegmentController().getState()+" WARNING: meta data of "+getSegmentController()+" over 1mb: "+controllerBlockSize+"; mapSize: "+getControllingMap().getAllElementsSize());
//		}
		

		final SegmentSerializationBuffersGZIP bm = SegmentSerializationBuffersGZIP.get();
		try {
			FastByteArrayOutputStream byteArrayStream = bm.getStaticArrayOutput();
			int controllerBlockSize = (int) serializeForNT(byteArrayStream);
			
			
			bm.ensureSegmentBufferSize(controllerBlockSize);
			
			buffer = bm.SEGMENT_BUFFER;
			deflater = bm.deflater;

			outputStream.writeInt(controllerBlockSize);

			deflater.reset();
			deflater.setInput(byteArrayStream.array, 0, controllerBlockSize);
			deflater.finish();
			zipSize = deflater.deflate(buffer);

			outputStream.writeInt(zipSize);

			outputStream.write(buffer, 0, zipSize);

//			assert(checkDeserialize(buffer, byteArrayStream.array, zipSize, controllerBlockSize));

		}finally {
			SegmentSerializationBuffersGZIP.free(bm);
		}
		//		System.err.println("ControlStructure compressed: "+controllerBlockSize+" -> "+zipSize);

		//		System.err.println("FINISHED TO WRITE STRUCTURE FOR "+getSendableSegmentController());
		return 1;
	}

	public void setFromMap(ControlElementMapper controllingMap) {
		this.controllingMap.setFromMap(controllingMap);
	}

	public void setObs(AbstractBuildControlManager segmentBuildController) {
		this.currentBuildController = segmentBuildController;
	}

	/**
	 * @param sendableSegmentController the sendableSegmentController to set
	 */
	public void setSendableSegmentController(
			SendableSegmentController sendableSegmentController) {
		needsControllerUpdates = (sendableSegmentController instanceof ManagedSegmentController);
		this.sendableSegmentController = sendableSegmentController;

	}
	ClientSegmentPieceConnectionChangeEvent eventTmp = new ClientSegmentPieceConnectionChangeEvent();
	public void switchControllerForElement(long controller,
	                                       long controlled, short controlledType) {
		
		if (isControlling(controller, controlled, controlledType)) {
//			System.err.println("REMOVE-> REMOVE");
			//remove only if this element is controlled by the provided controller
			removeControllerForElement(controller, controlled, controlledType);
			//INSERTED CODE (in this method)
			eventTmp.setEvent(this, controller, controlled, controlledType, false);
			StarLoader.fireEvent(eventTmp, false);
		} else {
//			System.err.println("REMOVE-> ADD");
			//remove in case another element is already controlling this
			if(controller != controlled){
//				System.err.println("REMOVE ALL :::: "+ElementKeyMap.toString(controlledType));
				removeControlledFromAll(controlled, controlledType, true);
			}

			//now we can add the control link
			addControllerForElement(controller, controlled, controlledType);

			eventTmp.setEvent(this, controller, controlled, controlledType, true);
			StarLoader.fireEvent(eventTmp, false);
		}
		//		this.setChanged();
		//		this.notifyObservers(SegNotifyType.SHIP_ELEMENT_CHANGED);
		if (currentBuildController != null) {
			currentBuildController.notifyElementChanged();
		}
	}
	
	public void updateLocal(Timer timer) {
		//		if(deserialized){
		//			System.err.println("[SERVER][CONTROL-ELEMENT-MAP][TAG] deserialized "+getSegmentController()+": Controllers: "+controllingMap.getAll().size());
		//			deserialized = false;
		//		}
		long time = System.currentTimeMillis();
		if (!failedControllerBlocks.isEmpty() && time - lastFailed > 600) {
			Vector3i p = new Vector3i();
			LongIterator it = failedControllerBlocks.iterator();
			ManagedSegmentController<?> mm = (ManagedSegmentController<?>) sendableSegmentController;
			while (it.hasNext()) {
				long controllerKey = it.nextLong();

				Vector3i controllerPos = ElementCollection.getPosFromIndex(controllerKey, p);
				SegmentPiece controllerPiece = sendableSegmentController.getSegmentBuffer().getPointUnsave(controllerPos, tmpPiece);
				final short type;
				if (controllerPiece != null && (type = controllerPiece.getType()) > 0) {
					if (!ElementKeyMap.isValidType(type)) {
						System.err.println("Exception: not adding controller block. type unknown:  "+type+"; "+ElementCollection.getPosFromIndex(controllerKey, new Vector3i())+"; "+ sendableSegmentController);
						//remove invalid type
						it.remove();
					} else {
						mm.getManagerContainer().addControllerBlockWithAddingBlock(type, controllerPiece.getAbsoluteIndex(), controllerPiece.getSegment(), false);
						it.remove();
					}
				}else{
					if(controllerPiece != null){
						System.err.println("Exception: not adding controller block. not a type (0); "
					+ElementCollection.getPosFromIndex(controllerKey, new Vector3i())+"; "+ sendableSegmentController);
						it.remove();
					}
				}
			}
			lastFailed = time;
		}
		if(receivedNT){
			//all necessary data received from server. set flag so a player editing connection works
			receivedInitialClient = true;
			receivedNT = false;
		}
		if (structureCompleteChangeFromNT) {
			receivedInitialClient = true;
			synchronized (delayedNTUpdatesMap) {
				assert (!sendableSegmentController.isOnServer());
				controllingMap.clear();
				controllingMap.setFromMap(delayedNTUpdatesMap);

				delayedNTUpdatesMap.clearAndTrim();
				structureCompleteChangeFromNT = false;

				
				if (!sendableSegmentController.isOnServer()) {
					if (((GameClientState) sendableSegmentController.getState()).getWorldDrawer() != null && ((GameClientState) sendableSegmentController.getState()).getWorldDrawer().getConnectionDrawerManager() != null) {
						((GameClientState) sendableSegmentController.getState()).getWorldDrawer().getConnectionDrawerManager().onConnectionChanged(sendableSegmentController);
					}
				}
				if (sendableSegmentController instanceof ManagedSegmentController<?>) {
					ManagedSegmentController<?> mm = (ManagedSegmentController<?>) sendableSegmentController;
					Long2ObjectOpenHashMap<FastCopyLongOpenHashSet> controllers = controllingMap.getAll();
					Vector3i p = new Vector3i();
					Vector3i po = new Vector3i();
					for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<FastCopyLongOpenHashSet> c : controllers.long2ObjectEntrySet()) {


						SegmentPiece controllerPiece = sendableSegmentController.getSegmentBuffer().getPointUnsave(c.getLongKey(), tmpPiece);
						FastCopyLongOpenHashSet cc = controllingMap.getAll().get(c.getLongKey());
						short type;
						if (controllerPiece != null && ElementKeyMap.isValidType((type = controllerPiece.getType()))) {
							mm.getManagerContainer().addControllerBlockWithAddingBlock(type, controllerPiece.getAbsoluteIndex(), controllerPiece.getSegment(), false);
						} else {
							failedControllerBlocks.add(c.getLongKey());
						}
					}
				} else {
					System.err.println("Exception: Cannot update controlstructure of " + sendableSegmentController + ": This object doesn't use connections! Please include this object in any bug report");
				}
				initialStructureReceived = true;
			}
		}

		if (!delayedNTUpdates.isEmpty()) {
			int size = delayedNTUpdates.size();
			long t0 = System.currentTimeMillis();
			synchronized (delayedNTUpdates) {
				for (ControlledElementContainer n : delayedNTUpdates) {
//					System.err.println("RECEIVED MID "+n);
					if (n.add) {
						addControl(n);
					} else {
						removeControlled(n.from, n.to, n.controlledType, n.send);
					}
				}
				delayedNTUpdates.clear();
			}
			long took = System.currentTimeMillis() - t0;
			if (took > 5) {
				System.err.println("[CONTROLELEMENTMAP][" + sendableSegmentController.getState() + "] NTUPDATE " + sendableSegmentController + " took " + took + " for " + size + " elements");
			}
		}

		if (addedDouble != null) {
			System.err.println("[WARNING][CONTROLELEMENTMAP] DOUBLE CONTROL SENT " + sendableSegmentController.getState() + ": " + sendableSegmentController + "; " + addedDouble.from + "; " + addedDouble.to + "; ty " + addedDouble.controlledType + "; add " + addedDouble.add + "; send " + addedDouble.send);
			addedDouble = null;
		}

		if (!failedDelayedUpdates.isEmpty()) {
			long t0 = System.currentTimeMillis();
			if (needsControllerUpdates) {
				final int size = failedDelayedUpdates.size();
				ManagedSegmentController<?> mm = (ManagedSegmentController<?>) sendableSegmentController;

				for (int i = 0; i < size; i++) {
					ControlledElementContainer n = failedDelayedUpdates.get(i);
					if(n.add){
						onAddedConnectionForManagerContainer(n.from, n.to, n.controlledType,n.send);
					}else{
						onRemovedConnectionForManagerContainer(n.from, n.to, n.controlledType,n.send);
					}
				}
			}
			failedDelayedUpdates.clear();
		}

		/**
		 * used to determine if there are invalid states in the controller
		 * (missing blocks that have a connection)
		 */

		if (controllingOldMapCheck != null) {

			try {
				if (controllingOldMapCheck.hasNext()) {
					long v = controllingOldMapCheck.next();
					SegmentPiece pi = sendableSegmentController.getSegmentBuffer().getPointUnsave(ElementCollection.getPosFromIndex(v, new Vector3i()), pointUnsaveTmp);//autorequest true previously
					if(pi == null){
						//not loaded yet. retry fully
						controllingOldMapCheck = controllingMap.keySet().iterator();
					}else{
						if (pi.getType() == Element.TYPE_NONE) {
	
							System.err.println("Exception: REMOVING DUE TO CONTROLLING MAP CHECK: " + v);
							controllingOldMapCheck.remove();
						}
					}
				} else {
					controllingOldMapCheck = null;
				}

			} catch (ConcurrentModificationException e) {
				controllingOldMapCheck = controllingMap.keySet().iterator();
			}
		}
	}

	public void clear() {
		controllingMap.clearAndTrim();
		positionControlMapChache.clear();
		positionControlMapChacheDirect.clear();
		positionControlMapChache.trim();
		positionControlMapChacheDirect.trim();
	}

	public void checkControllerOnServer(long r) {
		SegmentPiece pointUnsave = sendableSegmentController.getSegmentBuffer().getPointUnsave(r);//autorequest true previously
		
		if(pointUnsave != null && pointUnsave.getType() == Element.TYPE_NONE){
			System.err.println("[SERVER] Client marked a controller as faulty -> confirmed on server -> removing controller");
			removeFromRemovedBlock(r);
		}else{
			System.err.println("[SERVER] Client marked a controller as faulty -> exits on server -> not removing controller");
		}
	}



	public boolean isLoadedFromChunk16() {
		return loadedFromChunk16;
	}



	public void setLoadedFromChunk16(boolean loadedFromChunk16) {
		this.loadedFromChunk16 = loadedFromChunk16;
	}



	public void disconnectAllLightBlocks(long fromIndex3, long exceptionIndex4) {
		
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map = controllingMap.get(fromIndex3);
		
		if(map != null){
			for(short s : ElementKeyMap.lightTypes){
				FastCopyLongOpenHashSet typedMap = map.get(s);
				if(typedMap != null){
					LongArrayList list = new LongArrayList(typedMap);
					
					for(long l : list){
						if(l != exceptionIndex4){
							controllingMap.remove(fromIndex3, ElementCollection.getPosIndexFrom4(l), (short)ElementCollection.getType(l));
						}
					}
				}
			}
		}
	}

}
