package org.schema.game.server.controller;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.controller.io.SegmentRegionFileOld;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.RemoteSector;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.migration.Migration0061;
import org.schema.game.common.data.world.migration.Migration0078;
import org.schema.game.common.data.world.migration.Migration00898;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.common.util.FolderZipper.ZipCallback;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.data.CannotWriteExeption;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.SegmentControllerBluePrintEntryOld;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.LoadingScreen;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Vector3f;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class BluePrintController {

	public static final int MIGRATION_SERVER_DATABASE = 1;
	public static final int MIGRATION_CATALOG = 1;
	public static final BluePrintController temp = new BluePrintController(GameServerState.ENTITY_TEMP_BLUEPRINT_PATH, GameServerState.SEGMENT_DATA_TEMP_BLUEPRINT_PATH);
	public static final BluePrintController active = new BluePrintController(GameServerState.ENTITY_BLUEPRINT_PATH, GameServerState.SEGMENT_DATA_BLUEPRINT_PATH);
	public static final BluePrintController defaultBB = new BluePrintController(GameServerState.ENTITY_BLUEPRINT_PATH_DEFAULT, GameServerState.SEGMENT_DATA_BLUEPRINT_PATH_DEFAULT);
	public static final BluePrintController stationsNeutral = new BluePrintController(GameServerState.ENTITY_BLUEPRINT_PATH_STATIONS_NEUTRAL, GameServerState.SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_NEUTRAL);
	public static final BluePrintController stationsPirate = new BluePrintController(GameServerState.ENTITY_BLUEPRINT_PATH_STATIONS_PIRATE, GameServerState.SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_PIRATE);
	public static final BluePrintController stationsTradingGuild = new BluePrintController(GameServerState.ENTITY_BLUEPRINT_PATH_STATIONS_TRADING_GUILD, GameServerState.SEGMENT_DATA_BLUEPRINT_PATH_STATIONS_TRADING_GUILD);
	public static final BluePrintController shopsTradingGuild = new BluePrintController("./blueprints-stations/shop/", "./blueprints-stations/shop/DATA");
	private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(25, new ThreadFactory() {
		private int c;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "BlueprintLoaderThread-"+(c++));
		}
	});
	private static final int BLUEPRINT_SYSTEM_VERSION = 1;
	public static boolean migrating;
	public final String entityBluePrintPath;
	public final String dataBluePrintPath;
	public final String BLUE_PRINTS_TMP_PATH = "./blueprints/tmp/";
	
	private List<BlueprintEntry> cache;
	private long lastRetrieve;
	private boolean dirty = true;
	private boolean importedByDefault;
	private Object bkmaxFile;

	//	public static void migrateCatalogVSMD3(int mode, String datapath) throws IOException{
	//
	//		File dataPathF = new FileExt(datapath);
	//		SegmentDataIO.migrate(dataPathF);
	//
	//	}
	public BluePrintController(String entityBlueprintPath,
	                           String segmentDataBlueprintPath) {
		this.entityBluePrintPath = entityBlueprintPath;
		this.dataBluePrintPath = segmentDataBlueprintPath;
	}

	public static void migrateCatalogV00898(int mode, String datapath) throws IOException {

		File dataPathF = new FileExt(datapath);
		Migration00898.migrate(dataPathF);

	}

	public static SegmentControllerOutline<?> getOutline(GameServerState state,
	                                                     BlueprintEntry en, String ident,
	                                                     String newEntityName, float[] mat, int factionId, Vector3i min,
	                                                     Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		//			System.err.println("GETTING ENTITY TYPE: "+en.entityType+": "+Type.values()[en.entityType]);

		return en.getEntityType().iFace.inst(state, en, ident, newEntityName, mat, factionId, min, max, playerUID, activeAI, sector, stats);
	}

	public static SegmentControllerOutline<?> getOutline(GameServerState state,
	                                                     SegmentControllerBluePrintEntryOld en, String ident,
	                                                     String newEntityName, float[] mat, int factionId, Vector3i min,
	                                                     Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats, AIGameConfiguration<?, ?> sConfig) {
		//			System.err.println("GETTING ENTITY TYPE: "+en.entityType+": "+Type.values()[en.entityType]);

		return BlueprintType.values()[en.entityType].iFace.inst(state, en, ident, newEntityName, mat, factionId, min, max, playerUID, activeAI, sector, stats);
	}

	public void convert(String entityBluePrintPath, boolean backup) throws IOException {
		convert(entityBluePrintPath, backup, null);
		dirty = true;
	}

	public void convert(String entityBluePrintPath, boolean backup, String catalog) throws IOException {
		File f;
		if (catalog == null) {
			f = new FileExt(entityBluePrintPath + "/Catalog.txt");
			if (!f.exists()) {
				f = new FileExt(entityBluePrintPath + "/catalog.txt");
			}
		} else {
			f = new FileExt(entityBluePrintPath + "/" + catalog);
		}
		if (f.exists()) {
			System.err.println("[BLUEPRINT][MIGRATION] OLD FILES EXIST: " + f.getAbsolutePath());
			if (backup) {
				FileUtil.copyDirectory(new FileExt(entityBluePrintPath), new FileExt("./blueprints_old"));
			}
			System.err.println("NOW DOING CONVERSION " + entityBluePrintPath);
			if (!ElementKeyMap.initialized) {
				ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
			}

			List<SegmentControllerBluePrintEntryOld> readBluePrints = readBluePrintsOld(entityBluePrintPath, catalog);
			try {
				System.err.println("OLD BLUEPRINT READ: " + readBluePrints);
				for (int i = 0; i < readBluePrints.size(); i++) {
					SegmentControllerBluePrintEntryOld old = readBluePrints.get(i);
					BlueprintEntry newEntry = new BlueprintEntry(old.name);
					newEntry.write(old, entityBluePrintPath);

				}
				System.err.println("[BLUEPRINT][MIGRATION] DELETING OLD FILES");
				File exp = new FileExt(entityBluePrintPath + "/export/");
				File data = new FileExt(entityBluePrintPath + "/DATA/");
				f.delete();
				FileUtil.deleteRecursive(exp);
				FileUtil.deleteRecursive(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("[BLUEPRINT][MIGRATION] NO CONVERSION NEEDED: no old bb file exists: " + f.getAbsolutePath());
		}
	}

	public void editBluePrint(BlueprintEntry e) throws IOException {
		List<BlueprintEntry> readBluePrints = readBluePrints();
		readBluePrints.remove(e);
		readBluePrints.add(e);
		File cat = new FileExt(entityBluePrintPath + "Catalog.txt");
		if (!cat.exists()) {
			cat.createNewFile();
		}
		e.update();
		dirty = true;

	}

	public File export(String name) throws IOException, CatalogEntryNotFoundException {
		List<BlueprintEntry> readBluePrints = readBluePrints();
		for (BlueprintEntry n : readBluePrints) {
			if (n.getName().equals(name)) {
				return n.export();
			}
		}
		throw new CatalogEntryNotFoundException();
	}

	public File exportOnTheFly(SegmentController c, String name) throws Exception {
		c.writeAllBufferedSegmentsToDatabase(true, true, false);
		return export(name);
	}

	/**
	 * @return the lastRetrieve
	 */
	public long getLastRetrieve() {
		return lastRetrieve;
	}

	/**
	 * @param lastRetrieve the lastRetrieve to set
	 */
	public void setLastRetrieve(long lastRetrieve) {
		this.lastRetrieve = lastRetrieve;
	}

	public List<BlueprintEntry> importFile(File file, MayImportCallback callback) throws ImportFailedException, IOException {

		String importName = BlueprintEntry.importFile(file, this);

		File dir = new FileExt(entityBluePrintPath);
		File[] dF = dir.listFiles();
		File[] nw = null;
		for(File f : dF){
			if(f.getName().toLowerCase(Locale.ENGLISH).equals(importName.toLowerCase(Locale.ENGLISH))){
				nw = new File[]{f};
				break;
			}
		}
		if(nw != null){
			ThreadLoader r = new ThreadLoader();
			r.blueprintDirs = nw;
			r.list = new ObjectArrayList<>(nw.length);
			loadThreaded(r);
			r.list.trim();
			cache.addAll(r.list);
		}else{
			dirty = true;
		}

		List<BlueprintEntry> readBluePrints = readBluePrints();

		if (callback != null) {
			for (int i = 0; i < readBluePrints.size(); i++) {
				if (readBluePrints.get(i).getName().equals(importName)) {
					callback.onImport(readBluePrints.get(i));
				}
			}
		}
		return readBluePrints;
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @param dirty the dirty to set
	 */
	public void setDirty(boolean dirty) {
		//		System.err.println("DIRTY SET TO "+dirty);
		this.dirty = dirty;
	}

	public BlueprintEntry getBlueprint(
			final String catalogName
	) throws EntityNotFountException {
		List<BlueprintEntry> bluePrints = readBluePrints();

		return getBlueprint(catalogName, bluePrints);
	}

	public BlueprintEntry getBlueprint(
			final String catalogName,
			List<BlueprintEntry> bluePrints
	) throws EntityNotFountException {
		int index = -1;
		for (int i = 0; i < bluePrints.size(); i++) {
			if (catalogName.equals(bluePrints.get(i).getName())) {
				index = i;
				break;
			}
		}
		if (index >= 0) {

			BlueprintEntry en = bluePrints.get(index);
			return en;
		} else {
			throw new EntityNotFountException(catalogName + "; blueprints: " + bluePrints.size());
		}
	}

	public SegmentControllerOutline<?> loadBluePrint(
			GameServerState state,
			final String catalogName,
			String newEntityName,
			Transform where,
			long creditsToSpend,
			int factionId,
			List<BlueprintEntry> bluePrints,
			Vector3i spawnSector, List<BoundingBox> extraBB,
			String spawner, ByteBuffer buffer, boolean activeAI, SegmentPiece railToDockOn, ChildStats stats) throws EntityNotFountException, IOException, EntityAlreadyExistsException {

		BlueprintEntry en = getBlueprint( catalogName, bluePrints);
		return loadBluePrint(state, newEntityName, where, creditsToSpend, factionId, en, spawnSector, extraBB, spawner, buffer, activeAI, railToDockOn, stats);
	}

	public SegmentControllerOutline<?> loadBluePrint(GameServerState state, String catalogName, String newEntityName,
	                                              Transform where, long creditsToSpend, int factionId, Vector3i spawnSector, String spawner, ByteBuffer buffer, SegmentPiece railToDockOn, boolean activeAI, ChildStats stats ) throws EntityNotFountException, IOException, EntityAlreadyExistsException {
		List<BlueprintEntry> bluePrints = readBluePrints();

		return loadBluePrint(state, catalogName, newEntityName, where, creditsToSpend, factionId, bluePrints, spawnSector, null, spawner, buffer, activeAI, railToDockOn, stats);
	}

	public SegmentControllerOutline<?> loadBluePrintDirect(
			GameServerState state,
			BlueprintEntry en, SegmentPiece railToDockOn, 
			ChildStats stats) throws EntityNotFountException, IOException, EntityAlreadyExistsException {

		long time = System.currentTimeMillis();

		long took = (System.currentTimeMillis() - time);
		boolean okPos = false;

		int tries = 0;
		Vector3f minOut = new Vector3f();
		Vector3f maxOut = new Vector3f();

		BoundingBox bbO = new BoundingBox();
		en.calculateTotalBb(bbO);

		Vector3i min = new Vector3i();
		Vector3i max = new Vector3i();

		if(en.isChunk16()){
			min.x = bbO.min.x >= 0 ? (int) bbO.min.x / Chunk16SegmentData.SEG : (int) bbO.min.x / Chunk16SegmentData.SEG - 1;
			min.y = bbO.min.y >= 0 ? (int) bbO.min.y / Chunk16SegmentData.SEG : (int) bbO.min.y / Chunk16SegmentData.SEG - 1;
			min.z = bbO.min.z >= 0 ? (int) bbO.min.z / Chunk16SegmentData.SEG : (int) bbO.min.z / Chunk16SegmentData.SEG - 1;

			max.x = bbO.max.x >= 0 ? (int) bbO.max.x / Chunk16SegmentData.SEG : (int) bbO.max.x / Chunk16SegmentData.SEG - 1;
			max.y = bbO.max.y >= 0 ? (int) bbO.max.y / Chunk16SegmentData.SEG : (int) bbO.max.y / Chunk16SegmentData.SEG - 1;
			max.z = bbO.max.z >= 0 ? (int) bbO.max.z / Chunk16SegmentData.SEG : (int) bbO.max.z / Chunk16SegmentData.SEG - 1;
		}else{
			min.x = bbO.min.x >= 0 ? (int) bbO.min.x / SegmentData.SEG : (int) bbO.min.x / SegmentData.SEG - 1;
			min.y = bbO.min.y >= 0 ? (int) bbO.min.y / SegmentData.SEG : (int) bbO.min.y / SegmentData.SEG - 1;
			min.z = bbO.min.z >= 0 ? (int) bbO.min.z / SegmentData.SEG : (int) bbO.min.z / SegmentData.SEG - 1;
	
			max.x = bbO.max.x >= 0 ? (int) bbO.max.x / SegmentData.SEG : (int) bbO.max.x / SegmentData.SEG - 1;
			max.y = bbO.max.y >= 0 ? (int) bbO.max.y / SegmentData.SEG : (int) bbO.max.y / SegmentData.SEG - 1;
			max.z = bbO.max.z >= 0 ? (int) bbO.max.z / SegmentData.SEG : (int) bbO.max.z / SegmentData.SEG - 1;
		}
		float[] mat = new float[16];
		Transform t = new Transform();
		t.setIdentity();
		t.getOpenGLMatrix(mat);

		SegmentControllerOutline outline = getOutline(state, en, "DIRECT", "DIRECT", mat, 0, min, max, "DIRECT", false, null, stats);
		outline.railToSpawnOn = railToDockOn;
		return outline;
	}

	public SegmentControllerOutline<?> loadBluePrint(
			GameServerState state,
			String newEntityName,
			Transform where,
			long creditsToSpend,
			int factionId,
			BlueprintEntry en,
			Vector3i spawnSector, List<BoundingBox> extraBB,
			String spawner, ByteBuffer buffer, boolean activeAI, SegmentPiece railToDockOn, ChildStats stats) throws EntityNotFountException, IOException, EntityAlreadyExistsException {
		String railUID = en.getRailUID();
		if (railUID != null) {
			newEntityName += railUID;
			while (stats.railUIDs.contains(newEntityName)) {
				newEntityName += "E";
			}

		}
		
		boolean ok = false;
		final String befName = newEntityName; 
		while(!ok){
			try {
				BlueprintEntry.canSpawn(state, newEntityName, en.getEntityType(), false);
				ok= true;
			} catch (EntityAlreadyExistsException e2) {
				String oldName = new String(newEntityName);
				newEntityName = befName+"d"+stats.addedNum;
				System.err.println("[BLUERPRINT][WARN] Name collision resolved: "+oldName+" -> "+newEntityName);
			}
			stats.addedNum++;
		}
		
		stats.railUIDs.add(newEntityName);

		long time = System.currentTimeMillis();

		if (creditsToSpend >= 0 && en.getPrice() > creditsToSpend) {
			throw new NotEnoughCreditsException();
		}
		en.canSpawn(state, newEntityName);
		String outlineBBFolder = null;
		String outlineBBUID = null;
//		assert(stats.transientSpawn):en.getName()+"; "+en.getRawBlockData()[0].getAbsolutePath();
		if(!stats.transientSpawn){
			if (!en.existRawData()) {
				throw new EntityNotFountException("no raw DATA dir for: " + en.getName());
			}
			
			File[] rawBlockDataTest = en.getRawBlockData();
	
			File[] rawBlockData = null;
			//chck forold (corrupt) format from a prebuild where data was in an extra dir
			for (File rawBlockFile : rawBlockDataTest) {
				if (rawBlockFile.isDirectory()) {
					rawBlockData = rawBlockFile.listFiles();
				}
			}
			if (rawBlockData == null) {
				rawBlockData = rawBlockDataTest;
			}
	
			byte[] tsBuffer = SegmentRegionFileOld.createTimestampHeader(System.currentTimeMillis());
	
			for (File rawBlockFile : rawBlockData) {
	
				if (rawBlockFile.isDirectory()) {
					System.err.println("[BLUEPRINT][LOAD] Exception: catalog file " + en + " contains directory in raw data: " + rawBlockFile.getAbsolutePath());
					continue;
				}
				if(!rawBlockFile.getName().toLowerCase(Locale.ENGLISH).endsWith(SegmentDataFileUtils.BLOCK_FILE_EXT)){
					continue;
				}
				String fileName = "";
				switch(en.getEntityType()) {
					case SHIP -> fileName = EntityRequest.convertShipEntityName(newEntityName);
					case ASTEROID -> fileName = EntityRequest.convertAsteroidEntityName(newEntityName, false);
					case SHOP -> fileName = EntityRequest.convertShopEntityName(newEntityName);
					case SPACE_STATION -> fileName = EntityRequest.convertStationEntityName(newEntityName);
					case PLANET -> fileName = EntityRequest.convertPlanetEntityName(newEntityName);
					case MANAGED_ASTEROID -> fileName = EntityRequest.convertAsteroidEntityName(newEntityName, true);
					default -> {
					}
				}
	
				if (fileName.length() == 0) {
					throw new EntityNotFountException("wrong type: " + en.getEntityType());
				}
				String rawWithoutPoints = BlueprintEntry.removePoints(rawBlockFile.getName());
				assert (rawWithoutPoints.indexOf(".") >= 0) : rawWithoutPoints + " -> " + rawBlockFile.getName() + "; there is a point at: " + rawWithoutPoints.indexOf(".");
				fileName += rawWithoutPoints.substring(rawWithoutPoints.indexOf("."));
	
				File outputFile = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH + fileName);
	
				FileInputStream fin = new FileInputStream(rawBlockFile);
				FileOutputStream fout = new FileOutputStream(outputFile);
	
				FileChannel fcin = fin.getChannel();
				FileChannel fcout = fout.getChannel();
				while (true) {
					buffer.clear();
	
					int r = fcin.read(buffer);
	
					if (r == -1) {
						break;
					}
	
					buffer.flip();
	
					fcout.write(buffer);
				}
				fcin.close();
				fcout.close();
				fin.close();
				fout.close();
//				SegmentRegionFileOld.writeLastChanged(outputFile, tsBuffer);
			}
	
			long took = (System.currentTimeMillis() - time);
		}else{
			File base = new FileExt("./");
			String absBase = base.getAbsolutePath();
			
			File f = null;
			File[] rawBlockData = en.getRawBlockData();
			if(rawBlockData != null){
				
			
				for(File m : rawBlockData){
					if(m.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(SegmentDataFileUtils.BLOCK_FILE_EXT)){
						f = m;
						break;
					}
				}
				if(f != null){
					String oFolder = f.getAbsolutePath();
					System.err.println("[BLUEPRINT] BASE PATH: "+absBase);
					String relPath = oFolder.replace(absBase, ".").replaceAll("\\\\", "/");
					System.err.println("[BLUEPRINT] USING TRANSIENT: REL PATH ::: "+relPath);
					outlineBBFolder = relPath.substring(0, relPath.lastIndexOf("/")+1);
					System.err.println("[BLUEPRINT] USING TRANSIENT: BB FODLER ::: "+outlineBBFolder);
					outlineBBUID = relPath.substring(outlineBBFolder.length());
					System.err.println("[BLUEPRINT] USING TRANSIENT: FILE ISOLATED ::: "+outlineBBUID);
					outlineBBUID = outlineBBUID.substring(0, outlineBBUID.indexOf('.'));
					System.err.println("[BLUEPRINT] USING TRANSIENT: EXTRACTED BB UID ::: "+outlineBBUID);
					assert(outlineBBUID.length() > 0);
				}else{
					System.err.println("[BLUEPRINT] ERROR: "+en.getName()+" has no DATA files");
				}
			}else{
				System.err.println("[BLUEPRINT] ERROR: "+en.getName()+" has no DATA files RAW DIRECTORY");
			}
		}
		boolean okPos = false;

		ArrayList<BoundingBox> sectorElements = new ArrayList<BoundingBox>();
		synchronized(state) {
			for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (s instanceof SimpleTransformableSendableObject) {
					SimpleTransformableSendableObject st = ((SimpleTransformableSendableObject) s);

					Sendable rS = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(st.getSectorId());

					if (rS != null && rS instanceof RemoteSector && ((RemoteSector) rS).getServerSector().pos.equals(spawnSector)
							) {

						Vector3f minOut = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
						Vector3f maxOut = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
						if (st instanceof SegmentController) {
							SegmentController sc = (SegmentController) st;
							Vector3f localMin = new Vector3f(sc.getMinPos().x * SegmentData.SEG, sc.getMinPos().y * SegmentData.SEG, sc.getMinPos().z * SegmentData.SEG);
							Vector3f localMax = new Vector3f(sc.getMaxPos().x * SegmentData.SEG, sc.getMaxPos().y * SegmentData.SEG, sc.getMaxPos().z * SegmentData.SEG);
							Transform trans;
							if (st.getPhysicsDataContainer() != null && st.getPhysicsDataContainer().getShape() != null && st.getPhysicsDataContainer().getCurrentPhysicsTransform() != null) {
								trans = st.getPhysicsDataContainer().getCurrentPhysicsTransform();
							} else {
								trans = sc.getInitialTransform();
							}
							AabbUtil2.transformAabb(localMin, localMax, 40, trans, minOut, maxOut);
						} else if (st.getPhysicsDataContainer() != null && st.getPhysicsDataContainer().getShape() != null && st.getPhysicsDataContainer().getCurrentPhysicsTransform() != null) {
							st.getPhysicsDataContainer().getShape().getAabb(st.getPhysicsDataContainer().getCurrentPhysicsTransform(), minOut, maxOut);
						}
						if (Float.isNaN(minOut.x) || Float.isNaN(maxOut.x)) {
							try {
								throw new IllegalArgumentException("Bounding Box is NaN of " + st);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							sectorElements.add(new BoundingBox(minOut, maxOut));
						}
					}
				}
			}
		}
		if (extraBB != null) {
			sectorElements.addAll(extraBB);
		}
		int tries = 0;
		Vector3f minOut = new Vector3f();
		Vector3f maxOut = new Vector3f();
		BoundingBox bb = new BoundingBox();
		en.calculateTotalBb(bb);
		while (!okPos) {
			okPos = true;
			if (!(bb.min.x <= bb.max.x) ||
					!(bb.min.y <= bb.max.y) ||
					!(bb.min.z <= bb.max.z)) {
				throw new EntityNotFountException("[BLUEPRINT] INVALID AABB: " + en.getName() + ": " + bb.min + "; " + bb.max);
			}

			AabbUtil2.transformAabb(bb.min, bb.max, 1, where, minOut, maxOut);

			for (BoundingBox p : sectorElements) {

				if (AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, p.min, p.max)) {

					where.origin.z += 4;
					if (tries % 30 == 0) {
						System.err.println("[SERVER] FINDING POS TO SPAWN try(" + tries + ") COLLISION WITH: " + p + ": " + where.origin + ": collisionAABB: self " + minOut + " - " + maxOut + ";");
					}
					okPos = false;
					break;
				} else {
					//								System.err.println("NO COLLISION WITH "+p);
				}

			}
			tries++;

		}
		if (extraBB != null) {
			//save position BB so following ships wont spawn inside of this
			extraBB.add(new BoundingBox(minOut, maxOut));
		}

		float[] mat = new float[16];
		where.getOpenGLMatrix(mat);

		String UID = "";
		switch(en.getEntityType()) {
			case SHIP -> UID = EntityRequest.convertShipEntityName(newEntityName);
			case ASTEROID -> UID = EntityRequest.convertAsteroidEntityName(newEntityName, false);
			case SHOP -> UID = EntityRequest.convertShopEntityName(newEntityName);
			case SPACE_STATION -> UID = EntityRequest.convertStationEntityName(newEntityName);
			case PLANET -> throw new NullPointerException("Spawning planets not yet supported");
			default -> {
			}
		}
		if (UID.length() == 0) {
			throw new EntityNotFountException("wrong type: " + en.getEntityType());
		}

		BoundingBox bbO = new BoundingBox();
		en.calculateTotalBb(bbO);

		Vector3i min = new Vector3i();
		Vector3i max = new Vector3i();

		min.x = bbO.min.x >= 0 ? (int) Math.ceil(bbO.min.x / SegmentData.SEG) : (int) Math.ceil(bbO.min.x / SegmentData.SEG) - 1;
		min.y = bbO.min.y >= 0 ? (int) Math.ceil(bbO.min.y / SegmentData.SEG) : (int) Math.ceil(bbO.min.y / SegmentData.SEG) - 1;
		min.z = bbO.min.z >= 0 ? (int) Math.ceil(bbO.min.z / SegmentData.SEG) : (int) Math.ceil(bbO.min.z / SegmentData.SEG) - 1;

		max.x = bbO.max.x >= 0 ? (int) Math.ceil(bbO.max.x / SegmentData.SEG) : (int) Math.ceil(bbO.max.x / SegmentData.SEG) - 1;
		max.y = bbO.max.y >= 0 ? (int) Math.ceil(bbO.max.y / SegmentData.SEG) : (int) Math.ceil(bbO.max.y / SegmentData.SEG) - 1;
		max.z = bbO.max.z >= 0 ? (int) Math.ceil(bbO.max.z / SegmentData.SEG) : (int) Math.ceil(bbO.max.z / SegmentData.SEG) - 1;

		SegmentControllerOutline<?> outline = getOutline(state, en, UID, newEntityName, mat, factionId, min, max, spawner, activeAI, spawnSector, stats);
		outline.railToSpawnOn = railToDockOn;
		outline.blueprintFolder = outlineBBFolder;
		outline.blueprintUID = outlineBBUID;
		return outline;
	}

	/**
	 * @param catalogName
	 * @param newEntityName
	 * @param where
	 * @param creditsToSpend (credits available. use -1 to ignore credits)
	 * @param spawnSector
	 * @param i
	 * @return the entry of the SegmentController Blue Print
	 * @throws EntityNotFountException
	 * @throws IOException
	 * @throws EntityAlreadyExistsException
	 * @
	 */
	public SegmentControllerOutline loadBluePrintOld(
			GameServerState state,
			final String catalogName,
			String newEntityName,
			Transform where,
			int creditsToSpend,
			int factionId,
			List<SegmentControllerBluePrintEntryOld> bluePrints,
			Vector3i spawnSector, ArrayList<BoundingBox> extraBB,
			String spawner, ByteBuffer buffer, boolean activeAI, ChildStats stats) throws EntityNotFountException, IOException, EntityAlreadyExistsException {

		int index = bluePrints.indexOf(new SegmentControllerBluePrintEntryOld(catalogName));
		if (index >= 0) {

			long time = System.currentTimeMillis();
			SegmentControllerBluePrintEntryOld en = bluePrints.get(index);
			if (creditsToSpend >= 0 && en.price > creditsToSpend) {
				throw new NotEnoughCreditsException();
			}
			File dataPathF = new FileExt(dataBluePrintPath);
			assert (dataPathF.isDirectory());
			File[] list = dataPathF.listFiles(arg0 -> arg0.getName().startsWith(catalogName + "."));
			for (File s : list) {
				if (s.getName().startsWith(catalogName + ".")) {
					File inputFile = s;
					String fileName = "";
					BlueprintType t = BlueprintType.values()[en.entityType];
					switch(t) {
						case SHIP -> fileName = EntityRequest.convertShipEntityName(newEntityName);
						case ASTEROID -> fileName = EntityRequest.convertAsteroidEntityName(newEntityName, false);
						case SHOP -> fileName = EntityRequest.convertShopEntityName(newEntityName);
						case SPACE_STATION -> fileName = EntityRequest.convertStationEntityName(newEntityName);
						default -> {
						}
					}
					if (fileName.length() == 0) {
						throw new EntityNotFountException("wrong type: " + en.entityType);
					}

					fileName += s.getName().substring(s.getName().indexOf("."));
					File outputFile = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH + fileName);

					FileInputStream fin = new FileInputStream(inputFile);
					FileOutputStream fout = new FileOutputStream(outputFile);

					FileChannel fcin = fin.getChannel();
					FileChannel fcout = fout.getChannel();

					while (true) {
						buffer.clear();

						int r = fcin.read(buffer);

						if (r == -1) {
							break;
						}

						buffer.flip();

						fcout.write(buffer);
					}
					fcin.close();
					fcout.close();
					fin.close();
					fout.close();
					//					int c;
					//					while ((c = in.read()) != -1){
					//						out.write(c);
					//					}
					//					out.flush();
					//					in.close();
					//					out.close();
				}
			}

			long took = (System.currentTimeMillis() - time);
			if (took > 10) {
				System.err.println("[BLUEPRINT][READ] COPY OF " + catalogName + " TOOK " + took);
			}
			boolean okPos = false;

			ArrayList<BoundingBox> sectorElements = new ArrayList<BoundingBox>();

			synchronized (state.getLocalAndRemoteObjectContainer().getLocalObjects()) {
				for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
					if (s instanceof SimpleTransformableSendableObject) {
						SimpleTransformableSendableObject st = ((SimpleTransformableSendableObject) s);
						Sendable rS = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(st.getSectorId());

						if (rS != null && rS instanceof RemoteSector && ((RemoteSector) rS).getServerSector().pos.equals(spawnSector)) {

							Vector3f minOut = new Vector3f();
							Vector3f maxOut = new Vector3f();
							st.getPhysicsDataContainer().getShape().getAabb(st.getPhysicsDataContainer().getCurrentPhysicsTransform(), minOut, maxOut);
							if (Float.isNaN(minOut.x) || Float.isNaN(maxOut.x)) {
								try {
									throw new IllegalArgumentException("Bounding Box is NaN of " + st);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								sectorElements.add(new BoundingBox(minOut, maxOut));
							}
						}
					}
				}
			}
			if (extraBB != null) {
				sectorElements.addAll(extraBB);
			}
			int tries = 0;
			Vector3f minOut = new Vector3f();
			Vector3f maxOut = new Vector3f();
			while (!okPos) {
				okPos = true;

				AabbUtil2.transformAabb(en.bb.min, en.bb.max, 1, where, minOut, maxOut);

				for (BoundingBox p : sectorElements) {

					if (AabbUtil2.testAabbAgainstAabb2(minOut, maxOut, p.min, p.max)) {

						where.origin.z += 4;
						if (tries % 30 == 0) {
							System.err.println("[SERVER] FINDING POS TO SPAWN try(" + tries + ") COLLISION WITH: " + p + ": " + where.origin + ": collisionAABB: self " + minOut + " - " + maxOut + ";");
						}
						okPos = false;
						break;
					} else {
						//								System.err.println("NO COLLISION WITH "+p);
					}

				}
				tries++;

			}
			if (extraBB != null) {
				//save position BB so following ships wont spawn inside of this
				extraBB.add(new BoundingBox(minOut, maxOut));
			}

			float[] mat = new float[16];
			where.getOpenGLMatrix(mat);

			String ident = "";
			BlueprintType t = BlueprintType.values()[en.entityType];
			switch (t) {
				case SHIP:
					ident = EntityRequest.convertShipEntityName(newEntityName);
					break;
				case ASTEROID:
					ident = EntityRequest.convertAsteroidEntityName(newEntityName, false);
					break;
				case SHOP:
					ident = EntityRequest.convertShopEntityName(newEntityName);
					break;
				case SPACE_STATION:
					ident = EntityRequest.convertStationEntityName(newEntityName);
					break;
				case MANAGED_ASTEROID:
					break;
				case PLANET:
					break;
				default:
					break;
			}
			if (ident.length() == 0) {
				throw new EntityNotFountException("wrong type: " + en.entityType);
			}

			Vector3i min = new Vector3i();
			Vector3i max = new Vector3i();

			min.x = en.bb.min.x >= 0 ? (int) en.bb.min.x / SegmentData.SEG : (int) en.bb.min.x / SegmentData.SEG - 1;
			min.y = en.bb.min.y >= 0 ? (int) en.bb.min.y / SegmentData.SEG : (int) en.bb.min.y / SegmentData.SEG - 1;
			min.z = en.bb.min.z >= 0 ? (int) en.bb.min.z / SegmentData.SEG : (int) en.bb.min.z / SegmentData.SEG - 1;

			max.x = en.bb.max.x >= 0 ? (int) en.bb.max.x / SegmentData.SEG : (int) en.bb.max.x / SegmentData.SEG - 1;
			max.y = en.bb.max.y >= 0 ? (int) en.bb.max.y / SegmentData.SEG : (int) en.bb.max.y / SegmentData.SEG - 1;
			max.z = en.bb.max.z >= 0 ? (int) en.bb.max.z / SegmentData.SEG : (int) en.bb.max.z / SegmentData.SEG - 1;

			SegmentControllerOutline<?> outline = getOutline(state, en, ident, newEntityName, mat, factionId, min, max, spawner, activeAI, spawnSector, stats, null);

			return outline;
		} else {
			throw new EntityNotFountException(catalogName + "; blueprints: " + bluePrints.size());
		}
	}

	/**
	 * @param catalogName
	 * @param newEntityName
	 * @param where
	 * @param creditsToSpend (credits available. use -1 to ignore credits)
	 * @param i
	 * @return the entry of the SegmentController Blue Print
	 * @throws EntityNotFountException
	 * @throws IOException
	 * @throws EntityAlreadyExistsException
	 * @
	 */
	public SegmentControllerOutline loadBluePrintOld(GameServerState state, String catalogName, String newEntityName,
	                                                 Transform where, int creditsToSpend, int factionId, Vector3i spawnSector, String spawner, ByteBuffer buffer, boolean activeAI, ChildStats stats) throws EntityNotFountException, IOException, EntityAlreadyExistsException {
		List<SegmentControllerBluePrintEntryOld> bluePrints = readBluePrintsOld(entityBluePrintPath, null);

		return loadBluePrintOld(state, catalogName, newEntityName, where, creditsToSpend, factionId, bluePrints, spawnSector, null, spawner, buffer, activeAI, stats);
	}

	public void migrateCatalogV0061toV0062(int mode) throws IOException {

		if ((mode & MIGRATION_CATALOG) == MIGRATION_CATALOG) {
			File dataPathF = new FileExt(dataBluePrintPath);
			Migration0061.migrate0061to0062(dataPathF);
		}
		if ((mode & MIGRATION_SERVER_DATABASE) == MIGRATION_SERVER_DATABASE) {
			File serverDataPath = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
			Migration0061.migrate0061to0062(serverDataPath);
		}
		FileUtil.deleteDir(new FileExt("./client-database"));

	}

	public void migrateCatalogV0078toV0079(int mode) throws IOException {

		if ((mode & MIGRATION_CATALOG) == MIGRATION_CATALOG) {
			File dataPathF = new FileExt(dataBluePrintPath);
			Migration0078.migrate0078to0079(dataPathF);
		}
		if ((mode & MIGRATION_SERVER_DATABASE) == MIGRATION_SERVER_DATABASE) {
			File serverDataPath = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
			Migration0078.migrate0078to0079(serverDataPath);
		}

		FileUtil.deleteDir(new FileExt("./client-database"));
	}

	public void migrateCatalogV00898(int mode) throws IOException {

		if ((mode & MIGRATION_CATALOG) == MIGRATION_CATALOG) {
			File dataPathF = new FileExt(dataBluePrintPath);
			Migration00898.migrate(dataPathF);
		}
		if ((mode & MIGRATION_SERVER_DATABASE) == MIGRATION_SERVER_DATABASE) {
			File serverDataPath = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
			Migration00898.migrate(serverDataPath);
		}
		FileUtil.deleteDir(new FileExt("./client-database"));

	}

	public boolean needsMigrationV0061toV0062(List<SegmentControllerBluePrintEntryOld> readBluePrints) {
		for (int i = 0; i < readBluePrints.size(); i++) {
			if (readBluePrints.get(i).needsMigration.contains(SegmentControllerBluePrintEntryOld.MIGRATION_V0061_TO_V0062)) {
				return true;
			}
		}
		return false;
	}

	public boolean needsMigrationV0078toV0079(List<SegmentControllerBluePrintEntryOld> readBluePrints) {
		for (int i = 0; i < readBluePrints.size(); i++) {
			if (readBluePrints.get(i).needsMigration.contains(SegmentControllerBluePrintEntryOld.MIGRATION_V0078_TO_V0079)) {
				return true;
			}
		}
		return false;
	}

	public boolean needsMigrationV00898(List<SegmentControllerBluePrintEntryOld> readBluePrints) {
		for (int i = 0; i < readBluePrints.size(); i++) {
			if (readBluePrints.get(i).needsMigration.contains(SegmentControllerBluePrintEntryOld.MIGRATION_V00897_TO_V00898)) {
				return true;
			}
		}
		return false;
	}

	public List<BlueprintEntry> readBluePrints() {
		if (cache != null && !dirty) {
			return cache;
		}
		long time = System.currentTimeMillis();

		//		try{
		//			throw new DebugMarking("Reading Blueprints");
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}

		System.err.println("[SERVER] ###### READING ALL BLUEPRINTS!");

		File dir = new FileExt(entityBluePrintPath);
		if (!dir.exists()) {
			dir.mkdir();
			return new ObjectArrayList();
		}else{
			File version = new FileExt(entityBluePrintPath+"/"+"bbversion");
			
			int blueprintVersion = 0;
			if(version.exists()){
				try {
					BufferedReader fi = new BufferedReader(new FileReader(version));
					blueprintVersion = Integer.parseInt(fi.readLine());
					fi.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			if(blueprintVersion < 1){
				if(ServerConfig.BACKUP_BLUEPRINTS_ON_MIGRATION.isOn()){
					System.err.println("[BLUEPRINT] Blueptrint migration needed! Backing up all blueprints... (Might take a little)");
					
					//BACKUP
					bkmaxFile = FileUtil.countFilesRecusrively(entityBluePrintPath);
					ZipCallback zipCallback = new FolderZipper.ZipCallback() {
						private int bkfile;
						@Override
						public void update(File f) {
							LoadingScreen.serverMessage = Lng.str("Blueptrint migration needed! Backing up all blueprints... files zipped: %s/%s",bkfile,bkmaxFile);
							bkfile++;
						}
					};
					try {
						
						FolderZipper.zipFolder(dir.getAbsolutePath(), "BLUEPRINTS_BACKUP_CHUNK16_"+dir.getName()+".zip", null, zipCallback, "", null, true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			if(blueprintVersion != BLUEPRINT_SYSTEM_VERSION){
				BufferedWriter fw;
				try {
					version.delete();
					fw = new BufferedWriter(new FileWriter(version));
					fw.append(String.valueOf(BLUEPRINT_SYSTEM_VERSION));
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		File[] blueprintDirs = dir.listFiles();
		ThreadLoader r = new ThreadLoader();
		r.blueprintDirs = blueprintDirs;
		r.list = new ObjectArrayList<BlueprintEntry>(blueprintDirs.length);
		if(cache != null){
			for(BlueprintEntry e : cache){
				r.previousMapLowerCase.put(e.getName().toLowerCase(Locale.ENGLISH), e);
			}
		}
		loadThreaded(r);
		
		r.list.trim();
		cache = r.list;
		dirty = false;
		System.err.println("[SERVER] ###### READING ALL " + r.loaded + " BLUEPRINTS FINISHED! TOOK " + (System.currentTimeMillis() - time) + "ms");
		return r.list;
	}

	private void loadThreaded(final ThreadLoader r) {
		
		for (final File blueprintDir : r.blueprintDirs) {
			threadPool.execute(() -> {
				try {
					if (blueprintDir.getName().equals("exported")) {
						return;
					}
					if (blueprintDir.isDirectory()) {
						long time = System.currentTimeMillis();
						final String name = blueprintDir.getName();
						BlueprintEntry prevBlueprintEntry = r.previousMapLowerCase.get(name.toLowerCase(Locale.ENGLISH));
//							if(prevBlueprintEntry != null){
//								System.err.println("[BLUEPRINT] "+name+" found in previous cache. Modified: "+prevBlueprintEntry.isFilesDirty());
//							}else{
//								System.err.println("[BLUEPRINT] "+name+" NOT found in previous cache ("+r.previousMapLowerCase.size()+")");
//							}
						if(prevBlueprintEntry != null && !prevBlueprintEntry.isFilesDirty()){
							//blueprint hasnt changed. no need to reload
							r.list.add(prevBlueprintEntry);
//								System.err.println("[BLUEPRINT] "+name+" has not been modified. Readding to cache!");
						}else{
							BlueprintEntry e = new BlueprintEntry(name, BluePrintController.this);
							try {
								e.rootRead = true;
								e.read();
								synchronized (r) {
									r.loaded++;
									r.list.add(e);
								}
							} catch (FileNotFoundException e1) {
								System.err.println("[BLUEPRINT] ERROR: invalid blueprint directory: File Not Found: " + e1.getMessage());
							} catch (IOException e1) {
								e1.printStackTrace();

								System.err.println("ERROR READING BLUEPRINT: " + blueprintDir.getAbsolutePath() + "; Blueprint will be moved");
								try {
									File corrupted = new FileExt("./blueprints-fileerror/" + blueprintDir.getName() + "/");
									corrupted.mkdirs();
									FileUtil.copyDirectory(blueprintDir, corrupted);
									FileUtil.deleteRecursive(blueprintDir);
								} catch (IOException e2) {
									e2.printStackTrace();
								}

							}

							long took = System.currentTimeMillis() - time;
							if(took > 1000){
								System.err.println("[BLUEPRINT] WARNING: Blueprint "+e.getName()+" took "+StringTools.formatPointZero((double)took/(double)1000)+" Seconds to load! HeaderMS "+e.getTookHeaderRead()+"; MetaMS "+e.getTookMetaRead()+"; StructMS "+e.getTookStructureRead());
							}
						}
					}

				} finally {
					synchronized (r) {
						r.done++;
						r.notify();
					}
				}
			});

		}
		synchronized (r) {
			while (r.done < r.blueprintDirs.length) {
				if(migrating){
					LoadingScreen.serverMessage = Lng.str("At least one Blueprint Migrating (might take a little)... %s/%s",r.done, r.blueprintDirs.length);
				}
				try {
					r.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			LoadingScreen.serverMessage = "";
		}
	}

	public List<SegmentControllerBluePrintEntryOld> readBluePrintsOld(File cat) throws IOException {
		//		System.err.println("[BLUEPRINT] READING BLUEPRINT: "+cat.getName());
		//		try{
		//			throw new NullPointerException();
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		ArrayList<SegmentControllerBluePrintEntryOld> bb = new ArrayList<SegmentControllerBluePrintEntryOld>();
		if (cat.exists()) {
			try {
				DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(cat)));
				while (in.available() > 0) {
					int size = in.readInt();
					if (size <= 0 || size > cat.length()) {
						System.err.println("[BLUEPRINT] WARNING: blueprint tried to read too much data (possible try to upload hacked old blueprint format): Size read: " + ((size / 1024) / 1024) + "MB");
						break;
					}
					byte b[] = new byte[size];
					in.readFully(b);

					try {
						DataInputStream dd = new DataInputStream(new ByteArrayInputStream(b));
						bb.add(new SegmentControllerBluePrintEntryOld(dd));
						dd.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				in.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		lastRetrieve = System.currentTimeMillis();
		dirty = false;
		return bb;
	}

	public List<SegmentControllerBluePrintEntryOld> readBluePrintsOld(String blueprintPath, String bbFile) {
		System.err.println("REDING BLUEPRINTS: dirty " + dirty + ": Cache " + (cache != null ? cache.size() : "null") + ": " + entityBluePrintPath);

		File cat;
		if (bbFile == null) {
			cat = new FileExt(blueprintPath + "Catalog.txt");
		} else {
			cat = new FileExt(blueprintPath + bbFile);
		}
		try {

			return readBluePrintsOld(cat);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalAccessError("Critical Exception");
		}
	}

	public void removeBluePrint(BlueprintEntry e) throws IOException {
		File dataPathF = new FileExt(entityBluePrintPath);

		File[] list = dataPathF.listFiles();
		for (File s : list) {
			if (s.getName().equals(e.getName())) {
				FileUtil.deleteRecursive(s);
			}
		}

		dirty = true;

	}

	public BlueprintEntry writeBluePrint(SegmentController con, String name, boolean local, BlueprintClassification classification) throws IOException {
		con.writeAllBufferedSegmentsToDatabase(true, local, local);

		BlueprintEntry entry = new BlueprintEntry(name, this);
		
		entry.setClassification(classification);
		entry.write(con, local);
		dirty = true;

		return entry;
	}

	public void writeBluePrints(List<SegmentControllerBluePrintEntryOld> toWrite) throws IOException {
		File cat = new FileExt(entityBluePrintPath + "Catalog.txt");
		writeBluePrints(toWrite, cat);
		dirty = true;
	}

	public void writeBluePrints(List<SegmentControllerBluePrintEntryOld> toWrite, File cat) throws IOException {
		if (!cat.exists()) {
			cat.createNewFile();
		}
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cat)));
		for (int i = 0; i < toWrite.size(); i++) {
			SegmentControllerBluePrintEntryOld s = toWrite.get(i);
			System.err.println("[writeBluePrints] WRITING BLUEPRINT: " + s);
			try {
				s.write(out, null);
			} catch (CannotWriteExeption e) {
				e.printStackTrace();
			}
		}
		out.flush();
		out.close();
		dirty = true;
	}

	/**
	 * @return the importedByDefault
	 */
	public boolean isImportedByDefault() {
		return importedByDefault;
	}

	/**
	 * @param importedByDefault the importedByDefault to set
	 */
	public void setImportedByDefault(boolean importedByDefault) {
		this.importedByDefault = importedByDefault;
	}

	private class ThreadLoader {
		int done = 0;
		int loaded = 0;
		File[] blueprintDirs;
		ObjectArrayList<BlueprintEntry> list;
		Object2ObjectOpenHashMap<String, BlueprintEntry> previousMapLowerCase  = new Object2ObjectOpenHashMap<String, BlueprintEntry>();
	}
}
