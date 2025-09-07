package org.schema.game.server.data.blueprintnw;

import api.utils.game.BlueprintModMappings;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.ClientStatics;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.cargo.CargoCollectionManager;
import org.schema.game.common.controller.elements.cargo.CargoElementManager;
import org.schema.game.common.controller.elements.cargo.CargoUnit;
import org.schema.game.common.controller.elements.railbeam.RailBeamElementManager;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.element.ControlElementMap;
import org.schema.game.common.data.element.ControlElementMapper;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.migration.Chunk32Migration;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.version.VersionContainer;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.gameConfig.GameConfig;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.SegmentControllerBluePrintEntryOld;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;

import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.InflaterInputStream;

public class BlueprintEntry implements BlueprintInterface {

	public static final int dataVersion = 5;
	public static final int structureVersion = 0;
	public static final int metaVersion = 5;

	private static final byte FINISH_BYTE = 1;
	private static final byte SEG_MANAGER_BYTE = 2;
	private static final byte DOCKING_BYTE = 3;
	private static final byte RAIL_BYTE = 4;
	private static final byte AI_CONFIG_BYTE = 5;
	private static final byte RAIL_DOCKER_BYTE = 6;
	private static final byte CARGO_BYTE = 7;
	private static final int LOCK_BOX_BYTE = 8;
	private static final byte THRUST_CONFIG_BYTE = 9;

	public final List<BBWirelessLogicMarker> wirelessToOwnRail = new ObjectArrayList<BBWirelessLogicMarker>();
	private final BluePrintController bbController;
	private final Vector3f railRootMinTotal = new Vector3f();
	private final Vector3f railRootMaxTotal = new Vector3f();
	public boolean railDock = false;
	public Tag railTag;
	public boolean rootRead;
	public int metaVersionRead;
	private BoundingBox bb;
	private String name;
	private BlueprintType entityType;
	private long price;
	private ElementCountMap elementMap;
	private File header;
	private File structure;
	private File meta;
	//INSERTED CODE
	private FileExt modMappingsFile;
	private BlueprintModMappings modMappings;
	///
	private Tag managerTag;
	private File rawBlockDir;
	private BlueprintClassification classification;
	private ControlElementMapper controlElementMap;
	private List<BlueprintEntry> childsToWrite;
	private List<BlueprintEntry> childs;
	private BlueprintEntry parent;
	private SegmentController delayedWriteSegCon;
	private Vector3i dockingPos;
	private Vector3f dockingSize;
	private short dockingStyle;
	private Tag aiTag;
	private byte dockingOrientation;
	private double mass;
	private String railUID;
	private EntityIndexScore score;
	private boolean loadedChunk16;
	private boolean needsChunkMigration;
	private Long2ObjectOpenHashMap<VoidSegmentPiece> dockerPoints;
	private Long2DoubleOpenHashMap cargoPoints;
	public ElementCountMap countWithChilds;
	private double totalCargo = -1;
	private int headerVersion;
	private boolean hadCargoByte;
	private double singleCargo = -1;
	private boolean oldPowerFlag;
	private long tookHeaderRead;
	private long tookMetaRead;
	private long tookStructureRead;
	private long headerModified = -1;
	private long metaModified = -1;
	private long structModified = -1;
	private String blueprintSavedInGameVersion = "unknown";

	public BlueprintEntry(String name) {
		this(name, BluePrintController.active);
	}

	public BlueprintEntry(String name, BluePrintController bbController) {
		this.bbController = bbController;
		init(name, bbController);
	}

	public double getTotalCapacity() {
		if(totalCargo < 0) {
			totalCargo = 0d;
			if(cargoPoints != null) {
				for(double c : cargoPoints.values()) {
					totalCargo += c;
				}
			}
			if(childs != null) {
				for(int i = 0; i < childs.size(); i++) {
					BlueprintEntry c = childs.get(i);
					totalCargo += c.getTotalCapacity();
				}
			}
		}
		return totalCargo;
	}

	@Override
	public double getCapacitySingle() {
		if(singleCargo < 0) {
			singleCargo = 0d;
			if(cargoPoints != null) {
				for(double c : cargoPoints.values()) {
					singleCargo += c;
				}
			}
		}
		return singleCargo;
	}

	public boolean hadCargoByteRead() {
		return hadCargoByte;
	}

	//INSERTED CODE
	public BlueprintModMappings getModMappings() {
		return modMappings;
	}

	public FileExt getModMappingsFile() {
		return modMappingsFile;
	}

	///
	public static String importFile(File toImport, BluePrintController bluePrintController) throws IOException {
		File tmp = new FileExt("./bbtmp/");
		if(tmp.exists()) {
			FileUtil.deleteRecursive(tmp);
		}
		if(!tmp.exists()) {
			tmp.mkdir();
		}

		long bytes = FileUtil.getExtractedFilesSize(toImport);

		System.err.println("[IMPORT][ZIP] TOTAL FILE SIZE EXTRACTED: " + toImport + ": " + ((bytes / 1024L) / 1024L) + " MB");

		long gbInBytes = 1024L * 1024L * ((long) ServerConfig.ALLOWED_UNPACKED_FILE_UPLOAD_IN_MB.getInt());
		if(bytes > gbInBytes) {
			throw new IOException("Extracted files too big (possible zip bomb through sparse files) for " + toImport.getAbsolutePath() + ": " + ((bytes / 1024L) / 1024L) + " MB");
		}

		FileUtil.extract(toImport, "./bbtmp/");
		boolean found = false;
		File[] topLevel = tmp.listFiles();
		if(topLevel.length != 1) {
			throw new IOException("wrong file format to import. Must be exctly one dir, but found " + Arrays.toString(tmp.list()));
		}
		File catalogAndData = topLevel[0];
		File[] listFiles = catalogAndData.listFiles();
		if(listFiles.length > 0) {
			for(int i = 0; i < listFiles.length; i++) {
				if(listFiles[i].getName().toLowerCase(Locale.ENGLISH).endsWith(".txt")) {
					System.err.println("[BLUEPRINT][IMPORT] Found Old Data " + listFiles[i].getName());
					bluePrintController.convert("./bbtmp/" + catalogAndData.getName() + "/", false, listFiles[i].getName());
					found = true;
					break;
				}
			}
		} else {
			throw new IOException("wrong file format to import. found " + Arrays.toString(catalogAndData.list()));
		}
		if(!found) {
			String[] fList = catalogAndData.list();
			System.err.println("failed to import old method. no Catalog.txt found in " + Arrays.toString(fList) + " trying new!");
			for(int i = 0; i < fList.length; i++) {
				if(!fList[i].equals("header.smbph")) {
					found = true;
					break;
				}
			}
			if(!found) {
				throw new IOException("ERROR: No blueprint data found to import: " + Arrays.toString(fList));
			}
		}

		File[] importFolders = tmp.listFiles();
		if(!importFolders[0].isDirectory()) {
			throw new IOException("not a directory: " + importFolders[0].getAbsolutePath());
		}
		File toDir = new FileExt(bluePrintController.entityBluePrintPath + "/" + importFolders[0].getName());
		System.err.println("IMPORT: " + importFolders[0].getAbsolutePath());

		String t = bluePrintController.entityBluePrintPath + "/" + importFolders[0].getName();
		int i = 0;
		while(toDir.exists()) {
			toDir = new FileExt(t + "_" + i);
			i++;
		}
		FileUtil.copyDirectory(importFolders[0], toDir);

		String dPath = bluePrintController.entityBluePrintPath + "/" + toDir.getName() + "/DATA";
		File dataDir = new FileExt(dPath);
		if(dataDir.exists() && dataDir.isDirectory()) {
			File[] dataFiles = dataDir.listFiles();
			if(dataFiles.length > 0 && dataFiles[0].getName().endsWith(".smd") && !dataFiles[0].getName().endsWith(".smd2")) {
				BluePrintController.migrateCatalogV00898(0, dPath);
			}
			//			if( dataFiles.length > 0 && dataFiles[0].getName().endsWith(".smd2") && !dataFiles[0].getName().endsWith(SegmentDataIO.BLOCK_FILE_EXT)){
			//				BluePrintController.migrateCatalogV00898(0, dPath);
			//			}
		}

		FileUtil.deleteRecursive(tmp);
		return toDir.getName();
	}

	public static String removePoints(String fName) {
		int points = 0;
		int in = -1;
		while((in = fName.indexOf(".", in + 1)) > -1) {
			points++;
		}
		if(points > 4) {
			int replace = points - 4;
			StringBuffer ssb = new StringBuffer(fName);
			for(int i = 0; i < replace; i++) {
				in = -1;
				while(replace > 0 && (in = fName.indexOf(".", in + 1)) > -1) {
					ssb.replace(in, in + 1, "_");
					replace--;
				}
			}
			return ssb.toString();
		} else {
			return fName;
		}
	}

	private void calculatePrice() {
		this.price = elementMap.getPrice();
		if(childs != null) {
			for(BlueprintEntry c : childs) {
				this.price += c.price;
			}
		}
	}

	/**
	 * @return mass of this and all its children
	 */
	public double getMass() {
		return mass;
	}

	private void calculateMass() {
		this.mass = elementMap.getMass();
		if(childs != null) {
			for(BlueprintEntry c : childs) {
				this.mass += c.mass;
			}
		}
	}

	private void addElementCountMap(ElementCountMap m) {
		m.add(this.elementMap);
		if(childs != null) {
			for(BlueprintEntry c : childs) {
				c.addElementCountMap(m);
			}
		}
	}

	private void copyDataFilesToBlueprint(final SegmentController controller) throws IOException {
		File dbDataDir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		File[] rawFiles = dbDataDir.listFiles((arg0, arg1) -> arg1.startsWith(controller.getUniqueIdentifier() + "."));

		for(File rawFile : rawFiles) {
			File dir = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/");
			if(!dir.exists()) {
				dir.mkdirs();
			}
			File rawBlockBlueprintFile;
			String fName = "";
			if(parent == null) {
				rawBlockBlueprintFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/" + rawFile.getName());
			} else {
				fName = rawFile.getName();
				fName = name.substring(name.lastIndexOf("/")) + fName.substring(fName.indexOf("."));
				rawBlockBlueprintFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/" + fName);
			}
			if(!rawBlockBlueprintFile.getParentFile().exists()) {
				rawBlockBlueprintFile.getParentFile().mkdirs();
			}
			System.err.println("[BLUEPRINT] NAME: " + name + " fName(" + fName + ") COPY FILE " + rawFile.getName() + " TO: " + rawBlockBlueprintFile.getAbsolutePath());
			FileUtil.copyFile(rawFile, rawBlockBlueprintFile);
		}
	}

	private void copyLocalDataFilesToBlueptint(final SegmentController controller) throws IOException {
		File dbDataDir = new FileExt(ClientStatics.SEGMENT_DATA_DATABASE_PATH);
		StringBuilder sb = new StringBuilder();
		SegmentDataFileUtils.convertUID(controller.getUniqueIdentifier(), controller.getObfuscationString(), sb);
		final String id = sb.toString();
		File[] rawFiles = dbDataDir.listFiles((arg0, arg1) -> arg1.startsWith(id + "."));
		if(rawFiles.length == 0) {
			System.err.println("[BLUEPRINT] ERROR, NO DATA FOUND FOR: " + controller + "; UID: " + controller.getUniqueIdentifier() + "; Pattern: " + id);
		}
		for(File rawFile : rawFiles) {

			File rawBlockBlueprintFile;

			String fName = removePoints(rawFile.getName());

			if(parent == null) {
				fName = name + fName.substring(fName.indexOf("."));
				System.err.println("[BLUEPRINT] localSave pN: FNAME: " + fName);
				rawBlockBlueprintFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/" + (new FileExt(fName)).getName());
			} else {
				fName = name.substring(name.indexOf("/")) + fName.substring(fName.indexOf("."));
				System.err.println("[BLUEPRINT] localSave: FNAME: " + fName);
				rawBlockBlueprintFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/" + (new FileExt(fName)).getName());
			}

			if(!rawBlockBlueprintFile.getParentFile().exists()) {
				rawBlockBlueprintFile.getParentFile().mkdirs();
			}
			FileUtil.copyFile(rawFile, rawBlockBlueprintFile);
		}
	}

	private void copyOldDataFilesToBlueprint(final SegmentControllerBluePrintEntryOld old, String entityBluePrintPath) throws IOException {
		File dataPathF = new FileExt(entityBluePrintPath + "/DATA/");
		assert (dataPathF.isDirectory()) : dataPathF.getAbsolutePath();
		File[] list = dataPathF.listFiles(arg0 -> arg0.getName().startsWith(old.name + "."));
		for(File s : list) {
			if(s.getName().startsWith(old.name + ".")) {
				File inputFile = s;

				File outputFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/" + inputFile.getName());

				if(!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
				FileInputStream fin = new FileInputStream(inputFile);
				FileOutputStream fout = new FileOutputStream(outputFile);

				FileChannel fcin = fin.getChannel();
				FileChannel fcout = fout.getChannel();

				ByteBuffer buffer = PlayerState.buffer;

				while(true) {
					buffer.clear();

					int r = fcin.read(buffer);

					if(r == -1) {
						break;
					}

					buffer.flip();

					fcout.write(buffer);
				}
				fcin.close();
				fcout.close();
				fin.close();
				fout.close();
				//				int c;
				//				while ((c = in.read()) != -1){
				//					out.write(c);
				//				}
				//				out.flush();
				//				in.close();
				//				out.close();
			}
		}
		String dPath = bbController.entityBluePrintPath + "/" + name + "/DATA/";
		File dataDir = new FileExt(dPath);
		if(dataDir.exists() && dataDir.isDirectory()) {
			File[] listFiles = dataDir.listFiles();
			if(listFiles.length > 0 && listFiles[0].getName().endsWith(".smd") && !listFiles[0].getName().endsWith(".smd2")) {
				BluePrintController.migrateCatalogV00898(0, dPath);
			}

		}
	}

	public boolean existRawData() {
		return rawBlockDir.exists();
	}

	public File export() throws IOException {

		String tipDirName = bbController.entityBluePrintPath + File.separator + name + "/";

		String tipFileName = bbController.entityBluePrintPath + File.separator + "exported" + File.separator + name + ".sment";
		File f = new FileExt(tipFileName);
		if(!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FolderZipper.zipFolder(tipDirName, tipFileName, null, null);

		return new FileExt(tipFileName);

	}

	/**
	 * @return the bb
	 */
	@Override
	public BoundingBox getBb() {
		return bb;
	}

	public void calculateTotalBb(BoundingBox out) {
		calculateTotalBb(out, new Vector3f());
	}

	public boolean isOkWithConfig(GameConfig g) {
		boolean ok = g.isBBOk(this);
		if(!ok) {
			return false;
		} else {
			for(BlueprintEntry e : childs) {
				if(!e.isOkWithConfig(g)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return the bb
	 */
	private void calculateTotalBb(BoundingBox out, Vector3f position) {
		out.expand(railRootMinTotal, railRootMaxTotal);

		Vector3f start = new Vector3f();
		Vector3f end = new Vector3f();
		start.add(position, bb.min);
		end.add(position, bb.max);
		out.expand(start, end);

		if(childs != null) {
			for(int i = 0; i < childs.size(); i++) {
				BlueprintEntry blueprintEntry = childs.get(i);

				if(blueprintEntry.railDock) {

				} else {
					Vector3f loc = new Vector3f(position);
					loc.x += blueprintEntry.dockingPos.x;
					loc.y += blueprintEntry.dockingPos.y;
					loc.z += blueprintEntry.dockingPos.z;

					blueprintEntry.calculateTotalBb(out, loc);
				}

			}

		}

	}

	/**
	 * @return the bbController
	 */
	public BluePrintController getBbController() {
		return bbController;
	}

	/**
	 * @return the chields
	 */
	public List<BlueprintEntry> getChilds() {
		return childs;
	}

	@Override
	public ControlElementMapper getControllingMap() {
		try {
			return readStructure(false);
		} catch(IOException e) {
			e.printStackTrace();
			return new ControlElementMapper();
		}
	}

	/**
	 * @return the elementMap
	 */
	@Override
	public ElementCountMap getElementMap() {
		return elementMap;
	}

	@Override
	public ElementCountMap getElementCountMapWithChilds() {
		if(countWithChilds == null) {
			countWithChilds = new ElementCountMap(this.elementMap);
			if(childs != null) {
				for(BlueprintEntry c : childs) {
					c.addElementCountMap(countWithChilds);
				}
			}
			if(ServerConfig.BLUEPRINTS_USE_COMPONENTS.isOn()) {
				countWithChilds = countWithChilds.calculateComponents();
			}
		}
		return countWithChilds;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getPrice() {
		return price;
	}

	@Override
	public EntityIndexScore getScore() {
		return score;
	}

	@Override
	public BlueprintType getType() {
		return entityType;
	}

	@Override
	public Tag getAiTag() {
		return aiTag;
	}

	/**
	 * @return the dockingOrientation
	 */
	public byte getDockingOrientation() {
		return dockingOrientation;
	}

	/**
	 * @return the dockingPos
	 */
	public Vector3i getDockingPos() {
		return dockingPos;
	}

	/**
	 * @return the dockingSize
	 */
	public Vector3f getDockingSize() {
		return dockingSize;
	}

	/**
	 * @return the dockingStyle
	 */
	public short getDockingStyle() {
		return dockingStyle;
	}

	/**
	 * @return the entityType
	 */
	public BlueprintType getEntityType() {
		return entityType;
	}

	public void setEntityType(BlueprintType entityType) {
		this.entityType = entityType;
	}

	/**
	 * @return the managerTag
	 */
	public Tag getManagerTag() {
		if(managerTag == null) {
			try {
				readManagerTagOnly(meta);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return managerTag;
	}

	public File[] getRawBlockData() {
		return rawBlockDir.listFiles();
	}

	public void init(String name, BluePrintController bbController) {
		if(name.contains(".")) {
			System.err.println("Exception: WARNING: Blueprint name contained irregular characters: \"" + name + "\"! Removing characters...");
		}
		this.name = name.replaceAll("\\.", "");
		header = new FileExt(bbController.entityBluePrintPath + "/" + name + "/" + "header.smbph");
		structure = new FileExt(bbController.entityBluePrintPath + "/" + name + "/" + "logic.smbpl");
		meta = new FileExt(bbController.entityBluePrintPath + "/" + name + "/" + "meta.smbpm");
		//INSERTED CODE
		this.modMappingsFile = new FileExt(bbController.entityBluePrintPath + "/" + name + "/modmappings.smbmm");
		///
		rawBlockDir = new FileExt(bbController.entityBluePrintPath + "/" + name + "/DATA/");
	}

	public void read() throws IOException {
		headerModified = header.lastModified();
		metaModified = meta.lastModified();
		structModified = structure.lastModified();

		long t = System.currentTimeMillis();

		readHeader(header);
		tookHeaderRead = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();

		synchronized(getClass()) {
			//synch on class
			if(needsChunkMigration) {
				BluePrintController.migrating = true;
				String baseDir = bbController.entityBluePrintPath + "/" + name + "/";
				migrateChunk16To32(baseDir);
				System.err.println("[BLUEPRINT] Chunk16 blueprint detected! Doing migration to chunk32...");
				DataOutputStream hOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(header)));
				writeHeader(bb.min, bb.max, entityType, score, classification, elementMap, hOut);
				hOut.close();
			}
		}

		readMeta(meta, false, true);
		//INSERTED CODE
		this.readModMappings(this.modMappingsFile);
		///
		tookMetaRead = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();

		//gets rid of any blocks that use sourceReference
		elementMap.convertNonPlaceableBlocks();
		calculatePrice();
		calculateMass();

		tookStructureRead = System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
	}

	private void migrateChunk16To32(String baseDir) {

		String dataDir = baseDir + "DATA" + "/";
		File dir = new FileExt(dataDir);
		if(dir.exists() && dir.isDirectory()) {
			Chunk32Migration.processFolder(dataDir, true, null, false);
		}
	}

	public void readManagerTagOnly(File meta) throws IOException {
		DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(meta), 1024 * 64));

		int metaVersionRead = stream.readInt();
		boolean loadedChunk16 = false;
		if(metaVersionRead < 4) {
			loadedChunk16 = true;
		}
		//		System.err.println("[BLUEPRINT] META VERSION: "+metaVersion);
		byte dataType = -1;
		boolean end = false;
		while(!end && (dataType = stream.readByte()) != FINISH_BYTE) {
			//			System.err.println("[BLUEPRINT] PARSED: "+ty);
			switch(dataType) {
				case (SEG_MANAGER_BYTE) -> {
					this.managerTag = Tag.readFrom(stream, false, false);
					end = true;
					break;
				}
				case (DOCKING_BYTE) -> {
					int size = stream.readInt();
					for(int i = 0; i < size; i++) {
						String name = stream.readUTF();
						BlueprintEntry child = new BlueprintEntry(name, bbController);
						child.dockingPos = new Vector3i(stream.readInt(), stream.readInt(), stream.readInt());
						if(this.loadedChunk16) {
							child.dockingPos.add(Chunk16SegmentData.SHIFT);
						}
						child.dockingSize = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
						child.dockingStyle = stream.readShort();
						child.dockingOrientation = stream.readByte();
						child.parent = this;
						child.railDock = false;
					}
					break;
				}
				case (AI_CONFIG_BYTE) -> {
					int tagSize = stream.readInt();
					if(tagSize < 0 || tagSize > 1000000000) {
						throw new IOException("Invalid tag size: " + tagSize);
					}
					byte[] tagBytes = new byte[tagSize];
					stream.readFully(tagBytes);
					Tag.readFrom(new FastByteArrayInputStream(tagBytes), true, false);
					break;
				}
				case (RAIL_DOCKER_BYTE) -> {
					boolean exists = stream.readByte() > 0;
					if(exists) {
						if(exists) {
							int size = stream.readInt();
							for(int i = 0; i < size; i++) {
								VoidUniqueSegmentPiece p = VoidUniqueSegmentPiece.deserizalizeWithoutUID(stream);
							}
						}
					}
					break;
				}
				case (CARGO_BYTE) -> {
					boolean exists = stream.readByte() > 0;
					cargoPoints = new Long2DoubleOpenHashMap();
					if(exists) {
						int size = stream.readInt();
						for(int i = 0; i < size; i++) {
							long posIndex = stream.readLong();
							double capacity = stream.readDouble();
							cargoPoints.put(posIndex, capacity);
						}
						if(cargoPoints.size() > 0) {
							cargoPoints.trim();
						}
					}
					this.hadCargoByte = true;
				}
				case LOCK_BOX_BYTE -> {
					boolean exists = stream.readByte() > 0;
					if(exists) {
						int size = stream.readInt();
						for(int i = 0; i < size; i++) {
							long posIndex = stream.readLong();
							double capacity = stream.readDouble();
							cargoPoints.put(posIndex, capacity);
						}
						if(cargoPoints.size() > 0) cargoPoints.trim();
					}
				}
				case THRUST_CONFIG_BYTE -> {
					Tag tag = Tag.readFrom(stream, false, false);
					//Todo: Read thrust config
				}
				case (RAIL_BYTE) -> {
					Vector3f railRootMinTotal = new Vector3f();
					Vector3f railRootMaxTotal = new Vector3f();
					railRootMinTotal.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
					railRootMaxTotal.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
					String railUID = null;
					if(metaVersionRead >= 2) {
						railUID = stream.readUTF();
						int wirelessLogicToOwnSize = stream.readInt();
						for(int i = 0; i < wirelessLogicToOwnSize; i++) {
							BBWirelessLogicMarker m = new BBWirelessLogicMarker();
							m.deserialize(stream, loadedChunk16 ? Chunk16SegmentData.SHIFT_ : 0);
						}
					}
					int size = stream.readInt();
					for(int i = 0; i < size; i++) {
						String name = stream.readUTF();
						BlueprintEntry child = new BlueprintEntry(name, bbController);
						int tagSize = stream.readInt();
						if(tagSize < 0 || tagSize > 1000000000) {
							throw new IOException("Invalid tag size: " + tagSize);
						}
						byte[] tagBytes = new byte[tagSize];
						stream.readFully(tagBytes);
						Tag.readFrom(new FastByteArrayInputStream(tagBytes), true, false);
					}
					break;
				}
				default -> throw new IOException("Unknown data type: " + dataType);
			}
		}
		stream.close();

	}

	public void readMeta(File meta, boolean segManager, boolean children) throws IOException {
		DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(meta), 1024 * 64));

		metaVersionRead = stream.readInt();

		if(metaVersionRead < 4) {
			loadedChunk16 = true;
		}
		//		System.err.println("[BLUEPRINT] META VERSION: "+metaVersion);
		byte dataType = -1;
		boolean end = false;
		while(!end && (dataType = stream.readByte()) != FINISH_BYTE) {
			//			System.err.println("[BLUEPRINT] PARSED: "+ty);
			switch(dataType) {
				case (SEG_MANAGER_BYTE) -> {
					if(segManager) {
						this.managerTag = Tag.readFrom(stream, false, false);
					}
					end = true;
				}
				case (RAIL_DOCKER_BYTE) -> {
					boolean exists = stream.readByte() > 0;
					if(exists) {
						if(exists) {
							dockerPoints = new Long2ObjectOpenHashMap<VoidSegmentPiece>();
							int size = stream.readInt();
							for(int i = 0; i < size; i++) {
								VoidUniqueSegmentPiece p = VoidUniqueSegmentPiece.deserizalizeWithoutUID(stream);
								dockerPoints.put(p.getAbsoluteIndex(), p);
							}
							dockerPoints.trim();
						}
					}
				}
				case (CARGO_BYTE) -> {
					boolean exists = stream.readByte() > 0;
					if(exists) {
						cargoPoints = new Long2DoubleOpenHashMap();
						int size = stream.readInt();
						for(int i = 0; i < size; i++) {
							long posIndex = stream.readLong();
							double capacity = stream.readDouble();
							cargoPoints.put(posIndex, capacity);
						}
						if(cargoPoints.size() > 0) {
							cargoPoints.trim();
						}
					}
					this.hadCargoByte = true;
				}
				case (LOCK_BOX_BYTE) -> {
					boolean exists = stream.readByte() > 0;
					if(exists) {
						int size = stream.readInt();
						for(int i = 0; i < size; i++) {
							long posIndex = stream.readLong();
							double capacity = stream.readDouble();
							cargoPoints.put(posIndex, capacity);
						}
						if(cargoPoints.size() > 0) cargoPoints.trim();
					}
					this.hadCargoByte = true;
				}
				case (DOCKING_BYTE) -> {
					if(childs == null) {
						childs = new ObjectArrayList<>();
					}
					int size = stream.readInt();
					for(int i = 0; i < size; i++) {
						String name = stream.readUTF();
						BlueprintEntry child = new BlueprintEntry(name, bbController);
						child.dockingPos = new Vector3i(stream.readInt(), stream.readInt(), stream.readInt());
						if(loadedChunk16) {
							child.dockingPos.add(Chunk16SegmentData.SHIFT);
						}
						child.dockingSize = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
						child.dockingStyle = stream.readShort();
						child.dockingOrientation = stream.readByte();
						child.parent = this;
						child.railDock = false;
						childs.add(child);
					}
				}
				case (AI_CONFIG_BYTE) -> {
					int tagSize = stream.readInt();
					if(tagSize < 0 || tagSize > 1000000000) {
						throw new IOException("Invalid tag size: " + tagSize);
					}
					//				System.err.println("READING SIZE: "+tagSize);
					byte[] tagBytes = new byte[tagSize];
					stream.readFully(tagBytes);
					aiTag = Tag.readFrom(new FastByteArrayInputStream(tagBytes), true, false);
					break;
				}
				case (RAIL_BYTE) -> {
					railRootMinTotal.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
					railRootMaxTotal.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
					if(childs == null) {
						childs = new ObjectArrayList<>();
					}
					railUID = null;
					if(metaVersionRead >= 2) {
						railUID = stream.readUTF();
						int wirelessLogicToOwnSize = stream.readInt();
						for(int i = 0; i < wirelessLogicToOwnSize; i++) {
							BBWirelessLogicMarker m = new BBWirelessLogicMarker();
							m.deserialize(stream, loadedChunk16 ? Chunk16SegmentData.SHIFT_ : 0);
							wirelessToOwnRail.add(m);
						}
					}
					int size = stream.readInt();
					for(int i = 0; i < size; i++) {
						String name = stream.readUTF();
						BlueprintEntry child = new BlueprintEntry(name, bbController);
						int tagSize = stream.readInt();
						if(tagSize < 0 || tagSize > 1000000000) {
							throw new IOException("Invalid tag size: " + tagSize);
						}
						byte[] tagBytes = new byte[tagSize];
						stream.readFully(tagBytes);
						child.railTag = Tag.readFrom(new FastByteArrayInputStream(tagBytes), true, false);
						child.parent = this;
						child.railDock = true;
						childs.add(child);
					}
				}
				default -> throw new IOException("Unknown data type: " + dataType);
			}
		}
		stream.close();
		if(children) {
			if(childs != null) {
				for(int i = 0; i < childs.size(); i++) {
					childs.get(i).read();
				}
			}
		}
	}

	public ControlElementMapper readStructure(boolean force) throws IOException {
		return readStructure(structure, force);
	}

	public ControlElementMapper readStructure(File structure, boolean force) throws IOException {
		if(this.controlElementMap == null || force) {
			//			System.err.println("[BLUEPRINT] STRUCTURE READING "+structure.getAbsolutePath());
			DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(structure), 1024 * 64));

			int structureVersion = stream.readInt();

			ControlElementMapper map = new ControlElementMapper();
			ControlElementMap.deserialize(stream, map);
			this.controlElementMap = map;
			stream.close();
			//			System.err.println("[BLUEPRINT] OREAD: "+map.getAll().size());
			//			System.err.println("[BLUEPRINT] READ: "+controlElementMap.getAll().size()+" CONTROLLING ELEMENS");
		}

		return this.controlElementMap;
	}

	@Override
	public String toString() {
		return name;
	}

	public void update() {

	}

	public void write(SegmentController controller, boolean local) throws IOException {

		if(controller instanceof FloatingRockManaged) {
			throw new IOException("Asteroids cannot be saved in the catalog currently to prevent duping");
		}

		File fileOld = new FileExt(bbController.entityBluePrintPath + "/" + name + "Tmp/");
		header.getParentFile().renameTo(fileOld);
		if(!header.getParentFile().exists()) {
			header.getParentFile().mkdirs();
		}
		if(classification == null) {
			System.err.println("[BLUEPRINT] use default classification");
			this.classification = controller.getType().getDefaultClassification();
		}

		DataOutputStream hOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(header)));
		writeHeader(controller, hOut);
		hOut.close();

		DataOutputStream sOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(structure)));
		writeStructure(controller, sOut);
		sOut.close();

		FileOutputStream fileOutputStream = new FileOutputStream(meta);

		DataOutputStream mOut = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
		writeMeta(controller, mOut, fileOutputStream);
		//INSERTED CODE
		this.writeModMappings();
		///
		mOut.close();

		if(local) {
			System.err.println("[CLIENT][BLUEPRINT][LOCAL] " + controller + "; childs: " + childsToWrite.size());
			copyLocalDataFilesToBlueptint(controller);
		} else {
			copyDataFilesToBlueprint(controller);
		}
		if(childsToWrite.size() > 0) {
			for(int i = 0; i < childsToWrite.size(); i++) {
				childsToWrite.get(i).write(childsToWrite.get(i).delayedWriteSegCon, local);
				childsToWrite.get(i).delayedWriteSegCon = null;
			}
		}

		childsToWrite = null;
		if(parent == null) {
			FileUtil.deleteRecursive(fileOld);
		}

	}

	public void write(SegmentControllerBluePrintEntryOld old, String entityBluePrintPath) throws IOException {
		File fileOld = new FileExt(bbController.entityBluePrintPath + "/" + name + "Tmp/");
		header.getParentFile().renameTo(fileOld);
		if(!header.getParentFile().exists()) {
			header.getParentFile().mkdirs();
		}
		try {
			copyOldDataFilesToBlueprint(old, entityBluePrintPath);
			DataOutputStream hOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(header)));
			writeHeader(old, hOut);
			hOut.close();

			DataOutputStream sOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(structure)));
			writeStructure(old, sOut);
			sOut.close();

			DataOutputStream mOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(meta)));
			writeMeta(old, mOut);
			mOut.close();

			FileUtil.deleteRecursive(fileOld);
		} catch(Exception e) {
			FileUtil.deleteRecursive(header.getParentFile());
		}
	}

	//INSERTED CODE

	/**
	 * Translates the saved id's into the current ids
	 */
	private void translateElementMap() {
		//[Client elementMap -> Client namespace map -> Server namespace map -> Server elementMap]
		//Old blockId -> Namespaced key

		//Remove mod blocks from array
		HashMap<Short, Integer> oldCount = new HashMap<>();
		for(short sh : modMappings.getNamespacedBlocks().keySet()) {
			int amt = elementMap.get(sh);
			oldCount.put(sh, amt);
		}
		for(Short key : oldCount.keySet()) {
			int count = oldCount.get(key);
			elementMap.reset(key);
			short newId = modMappings.translateId(key);
			System.err.println("TRANSLATE: " + key + " to: " + newId);
			elementMap.inc(newId, count);
		}
		for(BlueprintEntry child : childs) {
			child.translateElementMap();
		}

	}

	private void translateBlocks() {

	}

	private void readModMappings(FileExt file) throws IOException {
		modMappings = new BlueprintModMappings();
		//If mappings does not exist, it has no modded blocks, so no need to do anything.
		if(file.exists()) {
			HashMap<String, Short> revMap = modMappings.getReverseNamespacedBlocks();
			HashMap<Short, String> map = modMappings.getNamespacedBlocks();
			Scanner scanner = null;
			try {
				// Old smbmm files are compressed, read them compressed first and then compressed if that fails
				scanner = new Scanner(new InflaterInputStream(new FileInputStream(file)));
//                System.err.println("[BlueprintEntry] Old MapMappings detected");
			} catch(Exception e) {
				// New modmappings
				scanner = new Scanner(new FileInputStream(file));
			}

			while(scanner.hasNext()) {
				String[] line = scanner.nextLine().split("~");
				if(line.length == 3) {
					String modName = line[0];
					String blockName = line[1];
					short id = Short.parseShort(line[2]);
					revMap.put(modName + "~" + blockName, id);
					map.put(id, modName + "~" + blockName);
				}
			}
			scanner.close();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		BlueprintModMappings bpmm = new BlueprintModMappings();
		File f = new File("modmappings.smbmm");
		HashMap<String, Short> revMap = bpmm.getReverseNamespacedBlocks();
		HashMap<Short, String> map = bpmm.getNamespacedBlocks();
		Scanner scanner = new Scanner(new InflaterInputStream(new FileInputStream(f)));
		while(scanner.hasNext()) {
			String[] line = scanner.nextLine().split("~");
			if(line.length == 3) {
				String modName = line[0];
				String blockName = line[1];
				short id = Short.parseShort(line[2]);
				revMap.put(modName + "~" + blockName, id);
				map.put(id, modName + "~" + blockName);
				String namespace = modName + "~" + blockName;
				namespace = namespace.replace(" ", "\\ ");
				namespace = namespace.replace(":", "\\:");
				System.out.println(namespace + "=" + id);
			}
		}
		scanner.close();
	}

	private void writeModMappings() throws FileNotFoundException {
		//Let gzip to the compressing
		StringBuilder out = new StringBuilder();
		for(Map.Entry<String, Short> stringShortEntry : BlueprintModMappings.getCurrent().getReverseNamespacedBlocks().entrySet()) {
			String namespace = stringShortEntry.getKey();
			short id = stringShortEntry.getValue();
			out.append(namespace).append("~").append(id).append("\n");
			//modname~blockname~id
		}

		FileOutputStream output = new FileOutputStream(modMappingsFile);
		try {
			output.write(out.toString().getBytes());
			output.flush();
			output.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	///

	public static void writeHeader(Vector3f bbMin, Vector3f bbMax, BlueprintType t, EntityIndexScore score, BlueprintClassification classifi, ElementCountMap elementClassCountMap, DataOutputStream stream) throws IOException {
		stream.writeInt(dataVersion);
		String vString = VersionContainer.VERSION + "_" + VersionContainer.build;
//		System.err.println("WRITING HEAD: "+vString);
		stream.writeUTF(vString);
		stream.writeInt(t.ordinal());

		stream.writeInt(classifi.ordinal());

		Vector3f min = new Vector3f(bbMin);
		Vector3f max = new Vector3f(bbMax);
		stream.writeFloat(min.x);
		stream.writeFloat(min.y);
		stream.writeFloat(min.z);
		stream.writeFloat(max.x);
		stream.writeFloat(max.y);
		stream.writeFloat(max.z);

		elementClassCountMap.serialize(stream);

		stream.writeBoolean(score != null);
		if(score != null) {
			score.serialize(stream, true);
		}
	}

	public void readHeader(File header) throws IOException {
		try {
			DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(header), 1024 * 64));

			headerVersion = stream.readInt();

			if(headerVersion >= 5) {
				blueprintSavedInGameVersion = stream.readUTF();
//				System.err.println("READ GAME VERUION "+blueprintSavedInGameVersion+"; "+getName());
			}

			if(headerVersion < 4) {
				oldPowerFlag = true;
			}

			if(headerVersion < 2) {
				needsChunkMigration = true;
			}

			if(headerVersion == 0) {
				entityType = BlueprintType.values()[stream.readInt()];

				Vector3f min = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
				Vector3f max = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
				bb = new BoundingBox(min, max);
				elementMap = new ElementCountMap();
				elementMap.deserialize(stream);
				classification = entityType.type.getDefaultClassification();
			} else {
				entityType = BlueprintType.values()[stream.readInt()];

				if(headerVersion >= 3) {
					classification = BlueprintClassification.values()[stream.readInt()];
				} else {
					classification = entityType.type.getDefaultClassification();
				}

				Vector3f min = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
				Vector3f max = new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());
				bb = new BoundingBox(min, max);
				elementMap = new ElementCountMap();
				elementMap.deserialize(stream);

				boolean hasScore = stream.readBoolean();
				if(hasScore) {

					this.score = new EntityIndexScore();
					this.score.deserialize(stream, 0, true);

				}
			}

			stream.close();
		} catch(RuntimeException e) {
			System.err.println("ERROR: " + headerVersion + "; " + name + "; " + blueprintSavedInGameVersion);
			throw e;
		}
	}

	public void writeHeader(SegmentController controller, DataOutputStream stream) throws IOException {
		ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> cargo = null;
		RailBeamElementManager railDockers = null;
		if(controller instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) controller).getManagerContainer();
			if(managerContainer.getStatisticsManager() != null) {
				try {
					this.score = managerContainer.getStatisticsManager().calculateIndex();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(managerContainer instanceof ShipManagerContainer) {
				railDockers = ((ShipManagerContainer) managerContainer).getRailBeam();
			}
		}

		writeHeader(
				controller.getBoundingBox().min,
				controller.getBoundingBox().max,
				BlueprintType.getType(controller.getClass()),
				this.score,
				this.classification,

				controller.getElementClassCountMap(),
				stream);
	}

	private void writeHeader(SegmentControllerBluePrintEntryOld old,
	                         DataOutputStream stream) throws IOException {
//		assert(false);
		stream.writeInt(dataVersion);
		stream.writeUTF(VersionContainer.VERSION + "_" + VersionContainer.build);
		stream.writeInt(old.entityType);

		if(BlueprintType.values()[old.entityType] == BlueprintType.SPACE_STATION) {
			stream.writeInt(BlueprintClassification.NONE_STATION.ordinal());
		} else {
			stream.writeInt(BlueprintClassification.NONE.ordinal());
		}

		Vector3i min = new Vector3i(old.bb.min);
		Vector3i max = new Vector3i(old.bb.max);
		stream.writeFloat(min.x);
		stream.writeFloat(min.y);
		stream.writeFloat(min.z);
		stream.writeFloat(max.x);
		stream.writeFloat(max.y);
		stream.writeFloat(max.z);
		ElementCountMap map = new ElementCountMap();
		map.load(old.elementMap);

		ElementCountMap elementClassCountMap = map;
		elementClassCountMap.serialize(stream);

	}

	public void writeMeta(SegmentController controller, DataOutputStream stream, FileOutputStream fileOutputStreamForSize) throws IOException {

		ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> cargo = null;
		ManagerModuleCollection<CargoUnit, CargoCollectionManager, CargoElementManager> lockBox = null;
		RailBeamElementManager railDockers = null;
		if(controller instanceof ManagedSegmentController<?>) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) controller).getManagerContainer();
			if(managerContainer instanceof ShipManagerContainer) {
				railDockers = ((ShipManagerContainer) managerContainer).getRailBeam();
			}
			cargo = managerContainer.getCargo();
			lockBox = managerContainer.getLockBox();
		}

		stream.writeInt(metaVersion);

		stream.writeByte(DOCKING_BYTE);

		stream.writeInt(controller.getDockingController().getDockedOnThis().size());
		childsToWrite = new ObjectArrayList<BlueprintEntry>();// BlueprintEntry[controller.getDockingController().getDockedOnThis().size()];
		int i = 0;
		for(ElementDocking e : controller.getDockingController().getDockedOnThis()) {
			SegmentController turretController = e.from.getSegment().getSegmentController();
			Vector3i dockingPos = e.to.getAbsolutePos(new Vector3i());
			String tname = name + "/ATTACHED_" + i;
			stream.writeUTF(tname);
			stream.writeInt(dockingPos.x);
			stream.writeInt(dockingPos.y);
			stream.writeInt(dockingPos.z);
			stream.writeFloat(turretController.getDockingController().getSize().x);
			stream.writeFloat(turretController.getDockingController().getSize().y);
			stream.writeFloat(turretController.getDockingController().getSize().z);
			stream.writeShort(e.to.getType());
			stream.writeByte(-1);
			BlueprintEntry ctw = new BlueprintEntry(tname, bbController);
			ctw.parent = this;
			ctw.delayedWriteSegCon = turretController;
			childsToWrite.add(ctw);
			i++;
			stream.flush();
		}

		stream.writeByte(RAIL_DOCKER_BYTE);
		if(railDockers == null) {
			stream.writeByte(0);
		} else {
			stream.writeByte(1);

			List<VoidUniqueSegmentPiece> f = new ObjectArrayList<VoidUniqueSegmentPiece>();
			for(long s : railDockers.getRailDockers()) {
				SegmentPiece pointUnsave = controller.getSegmentBuffer().getPointUnsave(s);

				if(pointUnsave != null) {
					f.add(new VoidUniqueSegmentPiece(pointUnsave));
				}
			}

			stream.writeInt(f.size());
			for(int c = 0; c < f.size(); c++) {
				f.get(c).serializeWithoutUID(stream);
			}
		}

		stream.writeByte(CARGO_BYTE);
		if(cargo == null) {
			stream.writeByte(0);
		} else {
			stream.writeByte(1);

			stream.writeInt(cargo.getCollectionManagersMap().size());
			double totalCargo = 0;
			for(Map.Entry<Long, CargoCollectionManager> e : cargo.getCollectionManagersMap().entrySet()) {
				stream.writeLong(e.getKey());
				double capacity = e.getValue().getCapacity();
				stream.writeDouble(capacity);
				totalCargo += capacity;
			}

		}

		stream.writeByte(RAIL_BYTE);

		Vector3f totalMin = new Vector3f();
		Vector3f totalMax = new Vector3f();
		if(controller.railController.isRoot() && controller.getPhysicsDataContainer() != null && controller.getPhysicsDataContainer().getShape() != null) {
			Transform t = new Transform();
			t.setIdentity();
			controller.getPhysicsDataContainer().getShape().getAabb(t, totalMin, totalMax);
		}
		stream.writeFloat(totalMin.x);
		stream.writeFloat(totalMin.y);
		stream.writeFloat(totalMin.z);

		stream.writeFloat(totalMax.x);
		stream.writeFloat(totalMax.y);
		stream.writeFloat(totalMax.z);

		stream.writeUTF(controller.railController.getRailUID());

		List<BBWirelessLogicMarker> marksToWrite = new ObjectArrayList<BBWirelessLogicMarker>();

		if(controller instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) controller).getManagerContainer() instanceof ActivationManagerInterface) {
			ActivationManagerInterface a = (ActivationManagerInterface) ((ManagedSegmentController<?>) controller).getManagerContainer();

			for(ActivationCollectionManager m : a.getActivation().getCollectionManagers()) {
				if(m.getDestination() != null) {
					SegmentController chainElementByUID = controller.railController.getRoot().railController.getChainElementByUID(m.getDestination().marking);
					if(chainElementByUID != null) {
						BBWirelessLogicMarker b = new BBWirelessLogicMarker();
						b.fromLocation = ElementCollection.getIndex(m.getControllerPos());
						b.markerLocation = m.getDestination().markerLocation;
						b.marking = chainElementByUID.railController.getRailUID();
						marksToWrite.add(b);
					}
				}

			}
		}

		stream.writeInt(marksToWrite.size());

		for(int j = 0; j < marksToWrite.size(); j++) {
			BBWirelessLogicMarker markerBeam = marksToWrite.get(j);
			markerBeam.serialize(stream);
		}

		stream.writeInt(controller.railController.next.size());

		for(RailRelation e : controller.railController.next) {

			SegmentController c = e.docked.getSegmentController();

			String tname = name + "/ATTACHED_" + i;
			stream.writeUTF(tname);

			Tag tag = c.railController.getTag();

			ByteArrayOutputStream op = new ByteArrayOutputStream(1024);
			tag.writeTo(op, false);

			int tagSize = op.size();
			stream.writeInt(tagSize);

			op.writeTo(stream);
//			System.err.println("WRITTEN RAIL TAG: "+tagSize);
			BlueprintEntry ctw = new BlueprintEntry(tname, bbController);
			ctw.parent = this;
			ctw.delayedWriteSegCon = c;
			ctw.railDock = true;
			childsToWrite.add(ctw);
			i++;
		}
		if(controller instanceof SegmentControllerAIInterface) {
			stream.writeByte(AI_CONFIG_BYTE);

			SegmentControllerAIInterface a = (SegmentControllerAIInterface) controller;

			Tag tag = a.getAiConfiguration().toTagStructure();

			ByteArrayOutputStream op = new ByteArrayOutputStream(1024);
			tag.writeTo(op, false);

			int tagSize = op.size();
			stream.writeInt(tagSize);
			op.writeTo(stream);
		}

		boolean isManaged = controller instanceof ManagedSegmentController<?>;
		if(controller instanceof ManagedSegmentController<?>) {
			stream.writeByte(SEG_MANAGER_BYTE);
			long position = fileOutputStreamForSize.getChannel().position();
			Tag t;
			if(((ManagedSegmentController<?>) controller).getManagerContainer() instanceof StationaryManagerContainer) {
				ShoppingAddOn addOn = ((StationaryManagerContainer) ((ManagedSegmentController<?>) controller).getManagerContainer()).getShoppingAddOn();
				long credits = addOn.getCredits();
				addOn.setCredits(0);
				t = ((ManagedSegmentController<?>) controller).getManagerContainer().toTagStructure();
				addOn.setCredits(credits);
			} else {
				t = ((ManagedSegmentController<?>) controller).getManagerContainer().toTagStructure();
			}
			t.writeTo(stream, false);
			stream.flush();
//			System.err.println("WRITTEN SC TAG: "+(fileOutputStreamForSize.getChannel().position() - position));
//						System.err.println("[BLUEPRINT] WRITTEN SegmentManagerTag");
		} else {
//			System.err.println("[BLUEPRINT] WRITTEN FINISHED TAG");
//			stream.writeByte(FINISH_BYTE);
		}

		//Lockbox
		stream.writeByte(LOCK_BOX_BYTE);
		if(lockBox == null) stream.writeByte(0);
		else {
			stream.writeByte(1);
			stream.writeInt(lockBox.getCollectionManagersMap().size());
			for(Map.Entry<Long, CargoCollectionManager> e : lockBox.getCollectionManagersMap().entrySet()) {
				stream.writeLong(e.getKey());
				stream.writeDouble(e.getValue().getCapacity());
			}
		}

		//Thrust config
		stream.writeByte(THRUST_CONFIG_BYTE);
		if(controller instanceof Ship ship) ship.getManagerContainer().thrustConfiguration.toTag().writeTo(stream, false);
		else stream.writeByte(0);

		if(!isManaged) stream.writeByte(FINISH_BYTE);
		stream.flush();
	}

	private void writeMeta(SegmentControllerBluePrintEntryOld old,
	                       DataOutputStream stream) throws IOException {
		stream.writeInt(metaVersion);

		stream.writeByte(FINISH_BYTE);
	}

	public void writeStructure(SegmentController controller, DataOutputStream stream) throws IOException {
		stream.writeInt(structureVersion);
		//		System.err.println("[BLUEPRINT] serializing structure (ALL "+controller.getControlElementMap().getControllingMap().getAll().size()+")");
		ControlElementMap.serializeForDisk(stream, controller.getControlElementMap().getControllingMap());
	}

	private void writeStructure(SegmentControllerBluePrintEntryOld old,
	                            DataOutputStream stream) throws IOException {

		stream.writeInt(structureVersion);
		ControlElementMap.serializeForDisk(stream, old.getControllingMap());

	}

	public void canSpawn(StateInterface state, String newEntityName) throws EntityAlreadyExistsException {
		canSpawn(state, newEntityName, entityType, false);
	}

	public static void canSpawn(StateInterface state, String newEntityName, BlueprintType type, boolean purge) throws EntityAlreadyExistsException {
		String fileName = null;
		if(type == null) {
			throw new NullPointerException("Entity has no type...");
		}
		switch(type) {
			case SHIP -> fileName = EntityRequest.convertShipEntityName(newEntityName);
			case ASTEROID -> fileName = EntityRequest.convertAsteroidEntityName(newEntityName, false);
			case SHOP -> fileName = EntityRequest.convertShopEntityName(newEntityName);
			case SPACE_STATION -> fileName = EntityRequest.convertStationEntityName(newEntityName);
			case PLANET -> fileName = EntityRequest.convertPlanetEntityName(newEntityName);
			case MANAGED_ASTEROID -> fileName = EntityRequest.convertAsteroidEntityName(newEntityName, false);
			default -> {
			}
		}
		if(fileName == null || EntityRequest.existsIdentifierWOExc(state, fileName)) {
			try {
				int dbCount = 0;

				if(purge) {
					System.err.println("[PURGE] Removing file " + fileName);
					(new FileExt(fileName)).delete();
					long idForFullUID = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getIdForFullUID(type.type.dbPrefix + newEntityName);
					if(idForFullUID >= 0) {
						((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(
								type.type.dbPrefix + newEntityName, (GameServerState) state);
					}
				} else {
					if((!newEntityName.startsWith("MOB_SIM") || (dbCount = ((GameServerState) state).getDatabaseIndex().getTableManager().getEntityTable().getByUIDExact(newEntityName, 1).size()) > 0)) {
						if(dbCount > 0) {
							System.err.println("ENTITY WAS FOUND IN DATABSASE");
						}
						throw new EntityAlreadyExistsException("FILE: " + fileName + "; Name-Only: " + newEntityName);
					} else {
						System.err.println("[MOB] File exists but is not in database: Removing file " + fileName);
						(new FileExt(fileName)).delete();
					}
				}
			} catch(SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getRailUID() {
		return railUID;
	}

	private boolean isOldDockingRec() {
		if(childs != null) {
			for(BlueprintEntry c : childs) {
				if(c.isOldDockingRec()) {
					return true;
				}
			}
		}
		return !railDock;
	}

	public boolean hasOldDocking() {
		if(childs != null) {
			for(BlueprintEntry c : childs) {
				if(c.isOldDockingRec()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isChunk16() {
		return loadedChunk16;
	}

	public BlueprintClassification getClassification() {
		return classification;
	}

	public void setClassification(BlueprintClassification classification) {
		if(classification != null) {
			this.classification = classification;
		} else {
			System.err.println("[BLUEPRINTENTRY] NOT SETTING CLASSIFICATION (provided null)");
		}
	}

	public int getHeaderVersion() {
		return headerVersion;
	}

	@Override
	public boolean isOldPowerFlag() {
		return oldPowerFlag;
	}

	public void setOldPowerFlag(boolean oldPowerFlag) {
		this.oldPowerFlag = oldPowerFlag;
	}

	public long getTookHeaderRead() {
		return tookHeaderRead;
	}

	public void setTookHeaderRead(long tookHeaderRead) {
		this.tookHeaderRead = tookHeaderRead;
	}

	public long getTookMetaRead() {
		return tookMetaRead;
	}

	public void setTookMetaRead(long tookMetaRead) {
		this.tookMetaRead = tookMetaRead;
	}

	public long getTookStructureRead() {
		return tookStructureRead;
	}

	public void setTookStructureRead(long tookStructureRead) {
		this.tookStructureRead = tookStructureRead;
	}

	public boolean isFilesDirty() {
		if(headerModified == header.lastModified() &&
				metaModified == meta.lastModified() &&
				structModified == structure.lastModified()) {
			return false;
		}
		return true;
	}

	public long getLastModified() {
		return Math.max(headerModified, Math.max(metaModified, structModified));
	}

	public Long2ObjectOpenHashMap<VoidSegmentPiece> getDockerPoints() {
		return dockerPoints;
	}
}
