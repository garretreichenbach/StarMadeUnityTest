package org.schema.game.common.controller.database;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DatabaseEntry {
	public String uid;
	public Vector3i sectorPos;
	public int type;
	public long seed;
	public String lastModifier;
	public String spawner;
	public String realName;
	public boolean touched;
	public int faction;
	public Vector3f pos;
	public Vector3i minPos;
	public Vector3i maxPos;
	public int creatorID;
	public long dbId;
	public long dockedTo;
	public long dockedRoot;
	public boolean dbspawnOnlyInDb;
	public boolean tracked;

	public DatabaseEntry() {

	}

	public DatabaseEntry(ResultSet res) throws SQLException {

		setFrom(res);
	}

	private static List<String> getDataFilesListOld(String UID) {
		String fil = UID + ".";
		FilenameFilter filter = (dir, name) -> name.startsWith(fil);
		File dir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		String[] list = dir.list(filter);

		List<String> l = new ObjectArrayList<String>();

		Collections.addAll(l, list);
		return l;
	}

	private static List<String> getFileNames(List<String> la) {
		List<String> l = new ObjectArrayList<String>();
		for(String s : la) {
			l.add((new FileExt(s)).getName());
		}
		return l;
	}

	public static String getWithFilePrefix(String uid, int type) throws EntityTypeNotFoundException {

		for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
			if(t.dbTypeId == type) {
				return t.dbPrefix + uid;
			}
		}
		throw new EntityTypeNotFoundException("cannot determine type: " + uid + ", " + type);
	}

	public static int getType(String uid) throws EntityTypeNotFoundException {
		for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
			if(uid.startsWith(t.dbPrefix)) {
				return t.dbTypeId;
			}
		}
		throw new EntityTypeNotFoundException("cannot determine type: " + uid);
	}

	public static boolean hasPrefix(String uid) {
		for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
			if(uid.startsWith(t.dbPrefix)) {
				return true;
			}
		}
		return false;
	}

	public static String removePrefixWOException(String uid) {
		for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
			if(uid.startsWith(t.dbPrefix)) {

				String clearedPrefixString = uid.substring(t.dbPrefix.length());

				return clearedPrefixString;
			}
		}
		return uid;
	}

	public static String removePrefix(String uid) {

		for(SimpleTransformableSendableObject.EntityType t : SimpleTransformableSendableObject.EntityType.values()) {
			if(uid.startsWith(t.dbPrefix)) {

				String clearedPrefixString = uid.substring(t.dbPrefix.length());

				return clearedPrefixString;
			}
		}
		throw new IllegalArgumentException("cannot determine type: " + uid);
	}

	public static SimpleTransformableSendableObject.EntityType getEntityType(String uidWithPrefix) throws EntityTypeNotFoundException {
		assert (hasPrefix(uidWithPrefix));
		return SimpleTransformableSendableObject.EntityType.getByDatabaseId(getType(uidWithPrefix));
	}

	public void setFrom(ResultSet res) throws SQLException {
		uid = res.getString(1).trim();
		sectorPos = new Vector3i(res.getInt(2), res.getInt(3), res.getInt(4));
		type = res.getByte(5);
		realName = res.getString(6).trim();
		faction = res.getInt(7);
		spawner = res.getString(8).trim();
		lastModifier = res.getString(9).trim();
		seed = res.getLong(10);
		touched = res.getBoolean(11);
		Array posArray = res.getArray(12);
		Object[] poses = (Object[]) posArray.getArray();
		double x = (Double) poses[0];
		double y = (Double) poses[1];
		double z = (Double) poses[2];
		pos = new Vector3f((float) x, (float) y, (float) z);

		Array posMinArray = res.getArray(13);
		Object[] dimension = (Object[]) posMinArray.getArray();
		minPos = new Vector3i((Integer) dimension[0], (Integer) dimension[1], (Integer) dimension[2]);
		maxPos = new Vector3i((Integer) dimension[3], (Integer) dimension[4], (Integer) dimension[5]);
		creatorID = res.getInt(14);
		dockedTo = res.getLong(15);
		dockedRoot = res.getLong(16);
		dbspawnOnlyInDb = res.getBoolean(17);
		tracked = res.getBoolean(18);
		dbId = res.getLong(18);
		uid = getWithFilePrefix(uid, type);
	}

	public void destroyAssociatedDatabaseFiles() {
		List<String> allFiles = SegmentDataFileUtils.getAllFiles(minPos, maxPos, uid, null);
		assert (getFileNames(allFiles).containsAll(getDataFilesListOld(uid))) : "\n" + getFileNames(allFiles) + ";\n\n" + getDataFilesListOld(uid);

		File sE = new FileExt(GameServerState.DATABASE_PATH + uid + ".ent");
		sE.delete();
		System.err.println("[DATABASE][REMOVE] removing entity file: " + sE.getAbsolutePath());

		for(String s : allFiles) {
			File f = new FileExt(s);
			if(f.exists()) {
				System.err.println("[DATABASE][REMOVE] removing raw block data file: " + f.getName() + " (exists: " + f.exists() + ")");
				f.delete();
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DatabaseEntry [uid=" + uid + ", sectorPos=" + sectorPos
				+ ", type=" + type + ", seed=" + seed + ", lastModifier="
				+ lastModifier + ", spawner=" + spawner + ", realName="
				+ realName + ", touched=" + touched + ", faction=" + faction
				+ ", pos=" + pos + ", minPos=" + minPos + ", maxPos=" + maxPos
				+ ", creatorID=" + creatorID + "]";
	}

	public SimpleTransformableSendableObject.EntityType getEntityType() {
		return SimpleTransformableSendableObject.EntityType.getByDatabaseId(type);
	}

	public String getFullUid() throws EntityTypeNotFoundException {
		return getWithFilePrefix(uid, type);
	}

	public String getEntityFilePath() throws EntityTypeNotFoundException {
		return GameServerState.ENTITY_DATABASE_PATH + getFullUid() + ".ent";
	}

	public static class EntityTypeNotFoundException extends SQLException {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public EntityTypeNotFoundException() {
		}

		public EntityTypeNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public EntityTypeNotFoundException(String message) {
			super(message);
		}

		public EntityTypeNotFoundException(Throwable cause) {
			super(cause);
		}

	}
}
