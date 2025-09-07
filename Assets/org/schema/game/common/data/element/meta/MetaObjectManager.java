package org.schema.game.common.data.element.meta;

import api.listener.events.inventory.metaobject.MetaObjectPreInstantiateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.element.meta.weapon.Weapon;
import org.schema.game.common.data.element.meta.weapon.Weapon.WeaponSubType;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.player.inventory.InvalidMetaItemException;
import org.schema.game.network.objects.NetworkClientChannel;
import org.schema.game.network.objects.remote.RemoteMetaObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MetaObjectManager {

	
	public enum MetaObjectType{
		
		BLUEPRINT((short)-9),
		RECIPE((short)-10),
		LOG_BOOK((short)-11),
		HELMET((short)-12),
		BUILD_PROHIBITER((short)-13),
		FLASH_LIGHT((short)-14),
		VIRTUAL_BLUEPRINT((short)-15),
		BLOCK_STORAGE((short)-16),
		WEAPON((short)-32), 
		;
		
		public final short type;

		private MetaObjectType(short type){
			this.type = type;
		}

		public static MetaObjectType getById(short objectType) {
			
			for(MetaObjectType t : values()){
				if(t.type == objectType){
					return t;
				}
			}
			throw new NullPointerException("Illegal meta id: "+objectType);
		}
		public static MetaObjectType getByIdWOExcept(short objectType) {
			
			for(MetaObjectType t : values()){
				if(t.type == objectType){
					return t;
				}
			}
			return null;
		}
	}
	
	public static final String FILENAME = GameServerState.ENTITY_DATABASE_PATH + "FLOATING_ITEMS_ARCHIVE.ent";
	public static final IntOpenHashSet subIdTypes = new IntOpenHashSet();
	private static int idGenServer = 100000;
	private static int idGenClient = 100;

	static {
		subIdTypes.add(MetaObjectType.WEAPON.type);
	}
	public static short[] getSubTypes(MetaObjectType type) {
		return switch(type) {
			case WEAPON -> WeaponSubType.getTypes();
			default -> null;
		};
	}
	private final Int2ObjectOpenHashMap<MetaObject> map = new Int2ObjectOpenHashMap<MetaObject>();
	private final StateInterface state;
	private final Object2ObjectOpenHashMap<Vector3i, ObjectArrayList<MetaObject>> archive = new Object2ObjectOpenHashMap<Vector3i, ObjectArrayList<MetaObject>>();
	public static boolean oldLoad;

	public MetaObjectManager(StateInterface state) {
		this.state = state;
	}

	public static MetaObject deserializeStatic(DataInputStream stream) throws IOException {
		int id = stream.readInt();
		short objectId = stream.readShort();
		short subId = -1;
		if (subIdTypes.contains(objectId)) {
			subId = stream.readShort();
		}

		MetaObject metaObject = instantiate(objectId, 0, subId); //init with id 0
		metaObject.setId(id); //set id from received
		metaObject.deserialize(stream);
		return metaObject;
	}

	private static synchronized int getNewId(boolean server) {
		if(server){
			return idGenServer++;
		}else{
			return idGenClient++;
		}
	}

	public static MetaObject instantiate(short objectType, int id, short subId) {
		//INSERTED CODE
		MetaObjectPreInstantiateEvent event = new MetaObjectPreInstantiateEvent(objectType, subId, subId);
		StarLoader.fireEvent(event, subId == idGenServer);
		if(event.isCustomMetaObject()){
			return event.getNewMetaObject();
		}
		///
//		System.err.println("[METAITEM] Creating new meta item: type: "+objectType+"; ID: "+id);
		return switch(MetaObjectType.getById(objectType)) {
			case LOG_BOOK -> new Logbook(id);
			case RECIPE -> new Recipe(id);
			case HELMET -> new Helmet(id);
			case BLUEPRINT -> new BlueprintMetaItem(id);
			case VIRTUAL_BLUEPRINT -> new VirtualBlueprintMetaItem(id);
			case BUILD_PROHIBITER -> new BuildProhibiter(id);
			case FLASH_LIGHT -> new FlashLight(id);
			case BLOCK_STORAGE -> new BlockStorageMetaItem(id);
			case WEAPON -> Weapon.instantiate(id, subId);
		};
//		throw new IllegalArgumentException("UNKNOWN OID: " + objectType);
	}

	public static MetaObject instantiate(MetaObjectType objectId, short subId, boolean onServer) {
		return instantiate(objectId.type, subId, onServer);
	}
	public static MetaObject instantiate(short objectId, short subId, boolean onServer) {
		return instantiate(objectId, getNewId(onServer), subId);
	}

	public static void serialize(DataOutputStream stream, MetaObject mo) throws IOException {
		stream.writeInt(mo.getId());
		stream.writeShort(mo.getObjectBlockID());
		assert (subIdTypes.contains(mo.getObjectBlockID()) || mo.getSubObjectId() == -1);
		if (mo.getSubObjectId() >= 0) {
			assert (subIdTypes.contains(mo.getObjectBlockID()));
			stream.writeShort(mo.getSubObjectId());
		}
		mo.serialize(stream);
	}

	public void archive(Vector3i s, FreeItem i) {
		MetaObject metaObject = map.remove(i.getMetaId());
		archive(s, metaObject);
	}

	public void archive(Vector3i s, MetaObject metaObject) {
		if (metaObject != null) {
			synchronized (archive) {
				ObjectArrayList<MetaObject> objectArrayList = archive.get(s);
				if (objectArrayList == null) {
					objectArrayList = new ObjectArrayList();
					archive.put(s, objectArrayList);
				}
				objectArrayList.add(metaObject);

				System.err.println("[METAITEM] Archived meta item " + s + " -> " + metaObject.getId() + "; LIST: " + objectArrayList);
			}
		}
	}

	public void awnserRequestTo(int reqId,
	                            NetworkClientChannel networkClientChannel) {
		//can only be on server!
		if (map.containsKey(reqId)) {
			MetaObject metaObject = map.get(reqId);
//						System.err.println("[SERVER] Awnsering Client request for metaobject "+reqId+" -> "+metaObject);
			networkClientChannel.metaObjectBuffer.add(new RemoteMetaObject(metaObject, this, true));
		} else {
			//this could happen in a racing condition (removed object at the same time someone requested)
			//a delete request should be sent out
			assert (false) : "requested meta object does not exist " + reqId + ": " + map;
		}
	}

	public void checkAvailable(int id, MetaObjectState state) {
		if (!map.containsKey(id)) {
			//			try{
			//				throw new NullPointerException("Metaobject "+id+" is not available: requesting from server");
			//			}catch (Exception e) {
			//				e.printStackTrace();
			//			}
						System.err.println("Metaobject "+id+" is not available: requesting from server");
			state.requestMetaObject(id);
		}
	}

	public void deserialize(DataInputStream stream) throws IOException {

		int id = stream.readInt();
		short objectId = stream.readShort();
		short subId = -1;
		if (subIdTypes.contains(objectId)) {
			subId = stream.readShort();
		}
		MetaObject metaObject = map.get(id);
		if (metaObject == null) {
			//			System.err.println("READ: "+id+"; "+subId);
			metaObject = instantiate(objectId, 0, subId); //init with id 0
			metaObject.setId(id); //set id from received
			map.put(id, metaObject);
		}
		metaObject.deserialize(stream);

	}

	public void deserializeRequest(DataInputStream stream) throws IOException {
		MetaObject metaObject = deserializeStatic(stream);

		if (metaObject.clientMayModify() && metaObject.isValidObject()) {
			map.put(metaObject.getId(), metaObject);
			((GameServerState) state).getGameState().announceMetaObject(metaObject);
		}

	}

	public void getFromArchive(Vector3i sector, Map<Integer, FreeItem> items) {
		if (items.isEmpty()) {
			return;
		}
		//		System.err.println("[METAITEM] GET METAOBJECTS FROM ARCHIVE FROM TOTAL: "+items.size());
		synchronized (archive) {
			if (!archive.containsKey(sector)) {
				Iterator<FreeItem> iterator = items.values().iterator();
				while (iterator.hasNext()) {
//					System.err.println("[METAITEM] NO ARCHIVE FOUND FOR " + sector);
					//no archive for this sector: remove all meta items
					FreeItem item = iterator.next();
					if (item.getType() < 0) {
						iterator.remove();
					}
				}
				return;
			} else {
				ObjectArrayList<MetaObject> archived = archive.remove(sector);
				Iterator<FreeItem> iterator = items.values().iterator();
				while (iterator.hasNext()) {
					FreeItem item = iterator.next();
					if (item.getType() < 0) {
						boolean foundInArchive = false;
						for (int i = 0; i < archived.size(); i++) {
							MetaObject metaObject = archived.get(i);
							if (metaObject.getId() == item.getMetaId()) {

								if(oldLoad){
									//give new id and add
									metaObject.setId(getNewId(isOnServer()));
								}
								item.setMetaId(metaObject.getId());
								foundInArchive = true;
								putServer(metaObject);
								//								System.err.println("[METAOBJECT] found in archive: "+sector+" -> "+item.getMetaId()+" -> "+metaObject);
							}
						}
						if (!foundInArchive) {
							System.err.println("[METAOBJECT] WARNING: NOT found in archive: " + item.getMetaId());
							iterator.remove();
						}
					}
				}

			}
		}

	}
	public boolean isOnServer(){
		return state instanceof ServerStateInterface;
	}
	public MetaObject getObject(int metaId) {
		return map.get(metaId);
	}

	public void load() throws FileNotFoundException, IOException {
		Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(new FileExt(FILENAME))), true, false);

		synchronized (archive) {
			Tag[] top;
			if("moi".equals(readFrom.getName())){
				Tag[] vals = (Tag[]) readFrom.getValue();
				byte version = (Byte) vals[0].getValue();
				idGenServer = (Integer)vals[1].getValue();
				top = (Tag[])vals[2].getValue();
			}else{
				top = (Tag[]) readFrom.getValue();
				oldLoad = true;
				System.err.println("[SERVER] Warning using old way of loading Meta Items");
			}
			for (int i = 0; i < top.length - 1; i++) {
				Tag[] pair = (Tag[]) top[i].getValue();
				Vector3i pos = (Vector3i) pair[0].getValue();
				Tag[] items = (Tag[]) pair[1].getValue();
				for (int c = 0; c < items.length - 1; c++) {
					Tag[] item = (Tag[]) items[c].getValue();
					int id = (Integer) item[0].getValue();
					short type = (Short) item[1].getValue();
					short subId = -1;
					if (item[3].getType() == Type.SHORT) {
						subId = (Short) item[3].getValue();
					}
					try {
						MetaObject inst = instantiate(type, -1, subId);
						inst.setId(id);
						inst.fromTag(item[2]);
						archive(pos, inst);
					} catch (InvalidMetaItemException e) {
						e.printStackTrace();
					}
				}
				//				System.err.println("[METAOBJECT] loaded "+(items.length-1)+" meta objects for "+pos);
			}
		}
	}
	public final Set<MetaObject> updatableObjects = new ObjectOpenHashSet<MetaObject>();
	private static final byte VERSION = 0;
	public void updateLocal(Timer timer){
		if(updatableObjects.size() > 0){
			Iterator<MetaObject> iterator = updatableObjects.iterator();
			while(iterator.hasNext()){
				MetaObject next = iterator.next();
				if(!next.update(timer)){
					iterator.remove();
				}
			}
		}
	}
	public void modifyRequest(NetworkClientChannel networkClientChannel, MetaObject o) throws MetaItemModifyPermissionException {
		if ((o.getPermission() & MetaObject.MODIFIABLE_CLIENT) == MetaObject.MODIFIABLE_CLIENT) {
			networkClientChannel.metaObjectModifyRequestBuffer.add(new RemoteMetaObject(o, this, true));
		} else {
			throw new MetaItemModifyPermissionException("Cannot modify item " + o + " (Permission denied)");
		}
	}
	public void putServer(MetaObject metaObject) {
		map.put(metaObject.getId(), metaObject);
	}
	public void checkCollisionServer(MetaObject metaObject) {
		MetaObject other = map.get(metaObject.getId());
		if(other != null && !metaObject.equalsObject(other)){
			System.err.println("WARNING: MetaItem ID collision: "+this+" and "+metaObject);
			metaObject.setId(getNewId(true));
		}
	}
	public void receivedAnnoucedMetaObject(MetaObject metaObject) {
		if (map.containsKey(metaObject.getId())) {
			/*
			 * This object has changed only, so
			 * only insert if already known object.
			 * 
			 * If it hasen't been requested yet, there is no need to insert.
			 * 
			 */
			map.put(metaObject.getId(), metaObject);
		}
	}

	public void save() throws FileNotFoundException, IOException {
		synchronized (archive) {
			Tag[] m = new Tag[archive.size() + 1];
			m[m.length - 1] = FinishTag.INST;
			int i = 0;
			for (Entry<Vector3i, ObjectArrayList<MetaObject>> entry : archive.entrySet()) {

				Tag[] sub = new Tag[entry.getValue().size() + 1];
				sub[sub.length - 1] = FinishTag.INST;

				for (int c = 0; c < entry.getValue().size(); c++) {
					MetaObject metaObject = entry.getValue().get(c);
					sub[c] = new Tag(Type.STRUCT, null, new Tag[]{
							new Tag(Type.INT, null, metaObject.getId()),
							new Tag(Type.SHORT, null, metaObject.getObjectBlockID()),
							metaObject.getBytesTag(),
							new Tag(Type.SHORT, null, metaObject.getSubObjectId()),
							FinishTag.INST});

				}
				m[i] = new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.VECTOR3i, null, entry.getKey()), new Tag(Type.STRUCT, null, sub), FinishTag.INST});

				i++;
			}
			Tag tag = new Tag(Type.STRUCT, "moi", 
					new Tag[]{new Tag(Type.BYTE, null, VERSION), 
					new Tag(Type.INT, null, idGenServer+10), 
					new Tag(Type.STRUCT, "floatingItems", m), 
					FinishTag.INST});

			File file = new FileExt(FILENAME + ".tmp");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			tag.writeTo(new BufferedOutputStream(new FileOutputStream(file)), true);

			File newfile = new FileExt(FILENAME);
			File oldfile = new FileExt(FILENAME + ".old");
			if (oldfile.exists()) {
				oldfile.delete();
			}
			if (newfile.exists()) {
				newfile.renameTo(oldfile);
			}
			newfile = new FileExt(FILENAME);
			file.renameTo(newfile);
		}
	}

	public void serverRemoveObject(int metaId) {
		System.err.println("[SERVER][META] removing metaID: " + metaId);
		MetaObject remove = map.remove(metaId);
		if(remove != null){
			remove.onDelete(true, state);
		}
	}

	public double getVolume(int meta) {
		MetaObject object = getObject(meta);
		if(object != null){
			return object.getVolume();
		}
		return 1;
	}

	public void clientRemoveObject(int metaId) {
		System.err.println("[CLIENT][META] removing metaID: " + metaId);
		MetaObject remove = map.remove(metaId);
		if(remove != null){
			remove.onDelete(false, state);
		}
	}

	

	

	

}
