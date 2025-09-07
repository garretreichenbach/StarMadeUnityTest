package org.schema.game.server.data.structurepersistence;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.agrona.collections.ObjectHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Manages Persistent Structure Data containers.
 *
 * @author TheDerpGamer
 */
public class PersistentStructureDataManager {

	private static final byte VERSION = 0;

	private static final File file = new File(GameServerState.DATABASE_PATH + "persistent_structure_data.smdat");
	private static final Long2ObjectOpenHashMap<PersistentStructureDataContainer> globalStructureMap = new Long2ObjectOpenHashMap<>();

	public static boolean containerExists(long id) {
		return globalStructureMap.containsKey(id);
	}

	public static void addContainer(PersistentStructureDataContainer container) {
		if(globalStructureMap.containsKey(container.getId()) && !globalStructureMap.get(container.getId()).equals(container)) {
			globalStructureMap.put(container.getId(), container);
			save();
		}
	}

	public static void removeContainer(PersistentStructureDataContainer container) {
		globalStructureMap.remove(container.getId());
		save();
	}

	public static void load() {
		if(!file.exists()) {
			System.out.println("No data to load for PersistentStructureDataManager");
			return;
		}
		globalStructureMap.clear();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			Tag tag = Tag.readFrom(fileInputStream, true, false);
			Tag[] tags = tag.getStruct();
			for(Tag value : tags) {
				if(value != FinishTag.INST) continue;
				Tag[] data = value.getStruct();
				byte version = data[0].getByte();
				long id = data[1].getLong();
				String type = data[2].getString();
				Tag structureData = data[3];
				try {
					Class<?> clazz = Class.forName(type);
					if(!clazz.isAssignableFrom(PersistentStructureDataContainer.class)) throw new ClassNotFoundException("Class " + type + " is not a PersistentStructureDataContainer");
					PersistentStructureDataContainer container = (PersistentStructureDataContainer) clazz.getConstructor(long.class).newInstance(id);
					container.fromTagStructure(structureData);
					globalStructureMap.put(id, container);
				} catch(ClassNotFoundException exception) {
					System.out.println("[PERSISTENT STRUCTURE DATA] Failed to load PersistentStructureDataContainer of type " + type + " due to ClassNotFoundException:\n" + exception.getMessage());
				} catch(Exception exception) {
					exception.printStackTrace();
					System.out.println("[PERSISTENT STRUCTURE DATA] Failed to load PersistentStructureDataContainer of type " + type + " due to Exception:\n" + exception.getMessage());
				}
			}
			System.out.println("Loaded " + globalStructureMap.size() + " PersistentStructureDataContainers from database");
		} catch(IOException exception) {
			exception.printStackTrace();
			System.out.println("[SERVER][EXCEPTION][FATAL]: Failed to load persistent structure data containers due to IOException:\n" + exception.getMessage());
			throw new RuntimeException("[PERSISTENT STRUCTURE DATA] Failed to initialize PersistentStructureDataManager", exception);
		}
	}

	public static void save() {
		try {
			file.delete();
			file.createNewFile();
			ObjectHashSet<Tag> tags = new ObjectHashSet<>();
			for(PersistentStructureDataContainer container : globalStructureMap.values()) {
				Tag[] data = new Tag[5];
				data[0] = new Tag(Tag.Type.BYTE, "version", VERSION);
				data[1] = new Tag(Tag.Type.LONG, "id", container.getId());
				data[2] = new Tag(Tag.Type.STRING, "type", container.getClass().getName());
				data[3] = container.toTagStructure();
				data[4] = FinishTag.INST;
				tags.add(new Tag(Tag.Type.STRUCT, container.getClass().getSimpleName(), data));
			}
			Tag tag = Tag.listToTagStruct(tags, Tag.Type.STRUCT, "persistent_data_containers");
			FileOutputStream outputStream = new FileOutputStream(file);
			tag.writeTo(outputStream, false);
			outputStream.flush();
			outputStream.close();
			System.out.println("Saved " + globalStructureMap.size() + " PersistentStructureDataContainers to database");
		} catch(IOException exception) {
			exception.printStackTrace();
			System.out.println("[SERVER][EXCEPTION][FATAL]: Failed to save persistent structure data containers due to IOException:\n" + exception.getMessage());
			throw new RuntimeException("[PERSISTENT STRUCTURE DATA] Failed to save PersistentStructureDataManager", exception);
		}
	}

	/**
	 * Calculates the unique ID of a structure based on the sector ID, entity ID, and block index.
	 * @param sectorId The sector ID
	 * @param entityId The entity ID
	 * @param blockIndex The block index
	 * @return The unique ID of the structure
	 */
	public static long calculateId(int sectorId, long entityId, long blockIndex) {
		return ((long) sectorId << 32) | (entityId << 16) | blockIndex;
	}

	public static int getSectorId(long id) {
		return (int) (id >> 32);
	}

	public static long getEntityId(long id) {
		return (id >> 16) & 0xFFFF;
	}

	public static long getBlockIndex(long id) {
		return id & 0xFFFF;
	}

	public static ArrayList<PersistentStructureDataContainer> getContainersBySectorId(int sectorId) {
		ArrayList<PersistentStructureDataContainer> containers = new ArrayList<>();
		for(long index : globalStructureMap.keySet()) {
			if(getSectorId(index) == sectorId) containers.add(globalStructureMap.get(index));
		}
		return containers;
	}

	public static ArrayList<PersistentStructureDataContainer> getContainersByEntityId(int entityId) {
		ArrayList<PersistentStructureDataContainer> containers = new ArrayList<>();
		for(long index : globalStructureMap.keySet()) {
			if(getEntityId(index) == entityId) containers.add(globalStructureMap.get(index));
		}
		return containers;
	}

	public static ArrayList<PersistentStructureDataContainer> getContainersByBlockIndex(long blockIndex) {
		ArrayList<PersistentStructureDataContainer> containers = new ArrayList<>();
		for(long index : globalStructureMap.keySet()) {
			if(getBlockIndex(index) == blockIndex) containers.add(globalStructureMap.get(index));
		}
		return containers;
	}
}