package org.schema.game.server.controller;

import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.controller.rails.RailController;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.Sector.SectorMode;
import org.schema.game.common.data.world.SectorInformation.GasPlanetType;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.migration.Chunk32Migration;
import org.schema.game.common.util.FolderZipper;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import javax.vecmath.Vector3f;
import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class SectorUtil {

	public static int tutCount = 0;

	public static void bulk(GameServerState state, String fileName, boolean export) throws IOException, SQLException {
		File f = new FileExt("./" + fileName);
		if (f.exists()) {
			BufferedReader r = new BufferedReader(new FileReader(f));
			try {
				String line;
				while ((line = r.readLine()) != null) {
					int index = line.indexOf("//");
					if (index >= 0) {
						line = line.substring(0, index);
					}
					if (line.contains(">>")) {
						String[] split = line.split(">>", 3);
						if (split.length == 3) {
							String fromPosString = split[0].trim();
							String fileNameString = split[1].trim();
							String toPosString = split[2].trim();
							String[] fromSplit = fromPosString.split(",");
							String[] toSplit = fromPosString.split(",");
							if (fromSplit.length == 3) {
								if (toSplit.length == 3) {

									Vector3i from = new Vector3i(Integer.parseInt(fromSplit[0].trim()), Integer.parseInt(fromSplit[1].trim()), Integer.parseInt(fromSplit[2].trim()));
									Vector3i to = new Vector3i(Integer.parseInt(toSplit[0].trim()), Integer.parseInt(toSplit[1].trim()), Integer.parseInt(toSplit[2].trim()));
									try {
										if (export) {
											exportSector(from, fileNameString, state);
										} else {
											importSector(fileNameString, to, state);
										}
									} catch (Exception e) {
										e.printStackTrace();
										state.getController().broadcastMessageAdmin(new Object[]{"ADMIN-MESSAGE [ERROR]\nCannot " + (export ? "export" : "import") + " sector\n" + (export ? ("from " + from) : ("to " + to)) + "\n" + e.getClass().getSimpleName() + "\n" + e.getMessage()}, ServerMessage.MESSAGE_TYPE_ERROR);
										System.err.println("[SERVER][ERROR][BULKEXPORTIMPORT] Cannot " + (export ? "export" : "import") + " sector " + (export ? ("from " + from) : ("to " + to)) + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
										LogUtil.log().warning("[SERVER][ERROR][BULKEXPORTIMPORT] Cannot " + (export ? "export" : "import") + " sector " + (export ? ("from " + from) : ("to " + to)) + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
									}

								} else {
									throw new IllegalArgumentException("Destination is not a coordinate");
								}
							} else {
								throw new IllegalArgumentException("Source is not a coordinate");
							}

						} else {
							throw new IllegalArgumentException("Illegal format (must be: from >> fileName >> to)");
						}
					}
				}
			} finally {
				r.close();
			}
		} else {
			throw new FileNotFoundException(f.getAbsolutePath());
		}
	}

	public static String changeUIDIfExists(File f, Vector3i toSecPos, GameServerState state, int index) throws IOException, SQLException {
		if (f.getName().startsWith(EntityType.SHOP.dbPrefix) || f.getName().startsWith(EntityType.SPACE_STATION.dbPrefix)
				|| f.getName().startsWith(EntityType.ASTEROID.dbPrefix) || f.getName().startsWith(EntityType.ASTEROID_MANAGED.dbPrefix) || f.getName().startsWith(EntityType.PLANET_SEGMENT.dbPrefix)
				|| f.getName().startsWith(EntityType.SHIP.dbPrefix) || f.getName().startsWith(EntityType.PLANET_CORE.dbPrefix)) {

			DataInputStream is = new DataInputStream(new FileInputStream(f));
			Tag readFrom = Tag.readFrom(is, true, false);
			is.close();

			Tag tag;
			if (f.getName().startsWith(EntityType.SHOP.dbPrefix)) {
				tag = ((Tag[]) readFrom.getValue())[0];
			} else if (f.getName().startsWith(EntityType.SPACE_STATION.dbPrefix) || f.getName().startsWith(EntityType.ASTEROID_MANAGED.dbPrefix)) {
				tag = ((Tag[]) readFrom.getValue())[1];
			} else if (f.getName().startsWith(EntityType.PLANET_SEGMENT.dbPrefix)) {
				tag = ((Tag[]) readFrom.getValue())[2];

				System.err.println("[IMPORT] PLANET " + ((Tag[]) readFrom.getValue())[0].getValue() + "; " + ((Tag[]) readFrom.getValue())[1].getValue());

				//adapt planet core
				String planetCoreUID = (String) ((Tag[]) readFrom.getValue())[1].getValue();
				planetCoreUID += toSecPos.x + "_" + toSecPos.y + "_" + toSecPos.z;
				((Tag[]) readFrom.getValue())[1] = new Tag(Type.STRING, null, planetCoreUID);

			} else {
				tag = readFrom;
			}
			int type = 0;
			if (f.getName().startsWith(EntityType.PLANET_CORE.dbPrefix)) {
				//special case since the uid needs to be adapted
				type = -1;

				Tag[] planetCoreValues = ((Tag[]) tag.getValue());

				String uid = (String) ((Tag[]) planetCoreValues[0].getValue())[0].getValue();

				uid += toSecPos.x + "_" + toSecPos.y + "_" + toSecPos.z;

				System.err.println("[IMPORT] CHANGING PLANET UID TO " + uid);

				((Tag[]) planetCoreValues[0].getValue())[0] = new Tag(Type.STRING, null, uid);

				BufferedOutputStream b = new BufferedOutputStream(new FileOutputStream(f));
				readFrom.writeTo(b, true);

				return uid;
			} else {
				//all other cases
				for (EntityType t : EntityType.values()) {
					if (f.getName().startsWith(t.dbPrefix)) {
						type = t.dbTypeId;
						break;
					}
				}

			}
			final String postfix = "_" + toSecPos.x + "_" + toSecPos.y + "_" + toSecPos.z;
			System.err.println("[IMPORT] PARSING: " + f.getName());

			Tag[] segControllerValuesTags = tag.getStruct();

			Tag[] simpleTransformableValues = (Tag[]) segControllerValuesTags[6].getValue();

			String uid = (String) segControllerValuesTags[0].getValue();

			String tmpUID = new String(uid);
			int i = 0;

			tmpUID += postfix;
//			}
			Tag[] docking = (Tag[]) segControllerValuesTags[3].getValue();

			// #RM1994 check for RailController index existence to avoid IndexOutOfBoundsException
			if (segControllerValuesTags.length > 19 && segControllerValuesTags[19].getType() != Type.FINISH) {
				
				int shift = 0;
				if(tag.getName().equals("sc")){
					//NO SHIFT NEEDED BECAUSE ITS GOING TO SHIFT WHEN LOADED
				}
				System.err.println("[IMPORT][DOCK] APPENING POSTFIX "+postfix);
				Tag newRailTag = RailController.addPostfixToTagUIDS(segControllerValuesTags[19], postfix, shift);
				
				if (newRailTag != null) {
					segControllerValuesTags[19] = newRailTag;
				}
			}

			String dockedToUID = (String) docking[0].getValue();
			if (!dockedToUID.equals("NONE")) {
//				if(Sector.isTutorialSector(toSecPos)){
//					Vector3i loc = VoidSystem.getLocalCoordinates(toSecPos, new Vector3i());
//					dockedToUID = "ENTITY_SHIP_tuttf"+"_"+loc.x+"_"+loc.y+"_"+loc.z;
//				}else{
				dockedToUID += "_" + toSecPos.x + "_" + toSecPos.y + "_" + toSecPos.z;
//				}

				docking[0].setValue(new String(dockedToUID));
			}

			uid = new String(tmpUID);

			segControllerValuesTags[0].setValue(uid);
			Vector3i sectorPos = (Vector3i) simpleTransformableValues[3].getValue();

			simpleTransformableValues[3].setValue(new Vector3i(toSecPos));

			BufferedOutputStream b = new BufferedOutputStream(new FileOutputStream(f));
			readFrom.writeTo(b, true);
			return uid;
		}
		assert(false):f.getName()+"; "+toSecPos;
		return null;
	}
	
	private static void cleanSector(Vector3i pos, GameServerState state) throws SQLException {
		List<DatabaseEntry> bySector = state.getDatabaseIndex().getTableManager().getEntityTable().getBySector(pos, -1);

		for (DatabaseEntry e : bySector) {
			final String withFilePrefix = e.uid;
			boolean removed = state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(withFilePrefix, state);
			if (!Sector.isTutorialSector(pos)) {
				if (removed) {
					state.getController().broadcastMessageAdmin(Lng.astr("ADMIN-MESSAGE\nCleaning sector for import\nremoved: \n",  withFilePrefix), ServerMessage.MESSAGE_TYPE_INFO);
				} else {
					state.getController().broadcastMessageAdmin(Lng.astr("ADMIN-MESSAGE ERROR\nObject to clean not found: \n",  withFilePrefix), ServerMessage.MESSAGE_TYPE_ERROR);
				}
			}
			
			
			e.destroyAssociatedDatabaseFiles();
//			File[] list = (new FileExt(GameServerState.DATABASE_PATH)).listFiles(new FilenameFilter() {
//
//				@Override
//				public boolean accept(File dir, String name) {
//					return name.startsWith(withFilePrefix + ".");
//				}
//			});
//			File s = new FileExt(GameServerState.DATABASE_PATH + withFilePrefix + ".ent");
//			s.delete();
//			System.err.println("[SERCTOR][CLEAN] removing entity file: " + s.getAbsolutePath());
//			for (File f : list) {
//				System.err.println("[SERCTOR][CLEAN] removing raw: " + f.getAbsolutePath());
//				f.delete();
//			}
		}

		state.getDatabaseIndex().getTableManager().getSectorTable().removeSector(pos);

	}

	public static synchronized void exportSector(Vector3i pos, String name, GameServerState state) throws SQLException, IOException {
		Sector sector = new Sector(state);
		state.getDatabaseIndex().getTableManager().getSectorTable().loadSector(pos, sector);
		if (sector.pos == null) {
			throw new IOException("Sector " + pos + " not in DB (be sure it has been discovered, and use /force_save)");
		}
		List<DatabaseEntry> bySector = state.getDatabaseIndex().getTableManager().getEntityTable().getBySector(pos, -1);

		File dir = new FileExt("./sector-export/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File dirTmp = new FileExt("./sector-export/sector/");
		if (!dirTmp.exists()) {
			dirTmp.mkdir();
		}
		File dirEntities = new FileExt("./sector-export/sector/ENTITIES/");
		if (!dirEntities.exists()) {
			dirEntities.mkdir();
		}
		File dirData = new FileExt("./sector-export/sector/DATA/");
		if (!dirData.exists()) {
			dirData.mkdir();
		}
		long time = System.currentTimeMillis();
		for (final DatabaseEntry e : bySector) {
			File entity = new FileExt(GameServerState.ENTITY_DATABASE_PATH + e.uid + ".ent");
			File entityTarget = new FileExt("./sector-export/sector/ENTITIES/" + e.uid + ".ent");
			HashSet<String> copied = new HashSet<String>();
			if (!entity.exists() && e.uid.startsWith("ENTITY_FLOATINGROCK")) {
				File sectorFile = new FileExt("./sector-export/sector/ENTITIES/" + e.uid + ".erock");
				FileWriter w = new FileWriter(sectorFile);
				w.append("name = " + e.uid + "\n");
				w.append("seed = " + e.seed + "\n");
				w.append("pos = " + e.pos.x + ", " + e.pos.y + ", " + e.pos.z + "\n");
				w.append("genid = " + e.creatorID + "\n");
				w.flush();
				w.close();
			} else {

				FileUtil.copyFile(entity, entityTarget);
				copied.add(entity.getName());

				if (entity.getName().startsWith(EntityType.PLANET_SEGMENT.dbPrefix)) {

					Tag planetTag = Tag.readFrom(new BufferedInputStream(new FileInputStream(entityTarget)), true, false);
					String planetCoreUID = (String) ((Tag[]) planetTag.getValue())[1].getValue();

					System.err.println("[EXPORT] FOUND PLANET; Checking for core: " + planetCoreUID);
					if (!planetCoreUID.equals("none")) {
						File coreFile = new FileExt(GameServerState.ENTITY_DATABASE_PATH + planetCoreUID + ".ent");

						String planetUIDTarget = EntityType.PLANET_CORE.dbPrefix;
						if (!copied.contains(coreFile.getName())) {

							File planetCoreTarget = new FileExt("./sector-export/sector/ENTITIES/" + planetUIDTarget + ".ent");

							FileUtil.copyFile(coreFile, planetCoreTarget);

							Tag coreTag = Tag.readFrom(new BufferedInputStream(new FileInputStream(planetCoreTarget)), true, false);

							//change core UID
							((Tag[]) ((Tag[]) coreTag.getValue())[0].getValue())[0] = new Tag(Type.STRING, null, planetUIDTarget);

							coreTag.writeTo(new BufferedOutputStream(new FileOutputStream(planetCoreTarget)), true);

							copied.add(coreFile.getName());
						}
						//change where how planet references the core
						((Tag[]) planetTag.getValue())[1] = new Tag(Type.STRING, null, planetUIDTarget);
						planetTag.writeTo(new BufferedOutputStream(new FileOutputStream(entityTarget)), true);
					}
				}

				File dataDir = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
				File[] listFiles = dataDir.listFiles((arg0, name1) -> name1.startsWith(e.uid + "."));

				for (File f : listFiles) {
					File dataTarget = new FileExt("./sector-export/sector/DATA/" + f.getName());
					FileUtil.copyFile(f, dataTarget);
				}
			}
		}
		File sectorFile = new FileExt("./sector-export/sector/sector.cfg");
		BufferedWriter w = new BufferedWriter(new FileWriter(sectorFile));
		assert (sector != null);
		assert (sector.pos != null);
		w.append("pos = " + sector.pos.x + ", " + sector.pos.y + ", " + sector.pos.z + "\n");

		SectorType sectorType = sector.getSectorType();
		w.append("type = " + sectorType.ordinal() + "\n");
		if (sectorType == SectorType.PLANET) {
			w.append("subtype = " + sector.getPlanetType().ordinal() + "\n");
		} else if (sectorType == SectorType.GAS_PLANET) {
			w.append("subtype = " + sector.getGasPlanetType().ordinal() + "\n");
		} else if (sectorType == SectorType.SPACE_STATION) {
			w.append("subtype = " + sector.getStationType().ordinal() + "\n");
		} else {
			w.append("subtype = 0\n");
		}
		w.flush();
		w.close();
		String tipFileName;
		tipFileName = "./sector-export/" + name + ".smsec";
		// zip everything except backups themselves
		FolderZipper.zipFolder("./sector-export/sector/", tipFileName, null, null);

		FileUtil.deleteDir(dirTmp);

	}

	private static void importIntoStellarSystem(Vector3i pos, int type, int subType, GameServerState state) throws IOException, SQLException {

		StellarSystem stellarSystemFromSecPos = state.getUniverse().getStellarSystemFromSecPos(pos);

		SectorType secType = SectorType.values()[type];

		int x = stellarSystemFromSecPos.getLocalCoordinate(pos.x);
		int y = stellarSystemFromSecPos.getLocalCoordinate(pos.y);
		int z = stellarSystemFromSecPos.getLocalCoordinate(pos.z);
		Vector3i positionLocal = new Vector3i(x,y,z);
		int index = stellarSystemFromSecPos.getIndex(x, y, z);

		stellarSystemFromSecPos.setSectorType(index, secType);

		String importMsg = "[IMPORT] SECTOR TYPE: " + secType;
		System.err.println(importMsg);
		stellarSystemFromSecPos.log(importMsg);

		if (secType == SectorType.PLANET) {
			System.err.println("[IMPORT] setting planet type to " + PlanetType.values()[subType]);
			stellarSystemFromSecPos.addPlanet(positionLocal, PlanetType.values()[subType]);
		}
		else if(secType == SectorType.GAS_PLANET){
			System.err.println("[IMPORT] setting planet type to " + GasPlanetType.values()[subType]);
			stellarSystemFromSecPos.addPlanet(positionLocal, GasPlanetType.values()[subType]);
		}
		else if (secType == SectorType.SPACE_STATION) {
			stellarSystemFromSecPos.setStationType(index, SpaceStationType.values()[subType]);
		}

		state.getDatabaseIndex().getTableManager().getSystemTable().updateOrInsertSystemIfChanged(stellarSystemFromSecPos, true);

	}

	public static synchronized void importSectorFullDir(final String dirStr, String fileName, Vector3i posForce, GameServerState state) throws IOException, SQLException {
		try {
			if (!fileName.endsWith(".smsec")) {
				fileName += ".smsec";
			}
			File dir = new FileExt(dirStr);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File dirTmp = new FileExt(dirStr + File.separator + "tmp" + File.separator);
			if (!dirTmp.exists()) {
				dirTmp.mkdir();
			}else{
				FileUtil.deleteDir(dirTmp);
				dirTmp.mkdir();
			}
			String entityDirPath = dirStr + File.separator + "tmp" + File.separator + "sector/ENTITIES/";
			String dataDirPath = dirStr + File.separator + "tmp" + File.separator + "sector/DATA/";
			//			boolean bb1 = dirTmp.delete();//DEBUG
			//			System.err.println("ABLE TO DEL1: "+bb1);

			File zipFile = new FileExt(dirStr + File.separator + fileName);

			//			boolean bb2 = dirTmp.delete();//DEBUG
			//			System.err.println("ABLE TO DEL2: "+bb2);
			File tmpDir = new FileExt(dirStr + File.separator + "tmp" + File.separator);
			FileUtil.extract(zipFile, dirStr + File.separator + "tmp" + File.separator);

			File dataDirTmp = new FileExt(dataDirPath);
			if(dataDirTmp.exists()){
				Chunk32Migration.processFolder(dataDirPath, true, null, false);
			}
			
			//			boolean bb3 = dirTmp.delete();//DEBUG
			//			System.err.println("ABLE TO DEL3: "+bb3);

			File sectorFile = new FileExt(dirStr + File.separator + "tmp" + File.separator + "sector/sector.cfg");
			BufferedReader read = new BufferedReader(new FileReader(sectorFile));

			Vector3i pos = new Vector3i();
			int type = 0;
			int subType = 0;
			try {

				String line;
				while ((line = read.readLine()) != null) {
					try {
						if (line.toLowerCase(Locale.ENGLISH).contains("pos")) {
							String[] split = line.split("=", 2);

							String[] posEl = split[1].split(",");
							pos.x = Integer.parseInt(posEl[0].trim());
							pos.y = Integer.parseInt(posEl[1].trim());
							pos.z = Integer.parseInt(posEl[2].trim());

						} else if (line.toLowerCase(Locale.ENGLISH).contains("subtype")) {
							String[] split = line.split("=", 2);

							subType = Integer.parseInt(split[1].trim());

						} else if (line.toLowerCase(Locale.ENGLISH).contains("type")) {
							//be sure to check subtype before type, because subtype contains "type"
							String[] split = line.split("=", 2);

							type = Integer.parseInt(split[1].trim());

						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new IOException("Invalid sector.cfg format in file");
					}
				}

			} finally {
				read.close();
			}

			if (posForce != null) {
				//replace internal saved position with forced position
				pos.set(posForce);
			}
			if (state.getUniverse().isSectorLoaded(pos)) {
				throw new IOException("cannot import while target sector " + pos + " is loaded, please get all players away from this sector");
			}

			System.err.println("[IMPORTSECTOR] PARSED SECTOR FILE: Pos " + pos + "; Type " + type + "; SubType: " + subType);

			cleanSector(pos, state);

			importIntoStellarSystem(pos, type, subType, state);

			state.getDatabaseIndex().getTableManager().getSectorTable().insertNewSector(pos, SectorMode.PROT_NORMAL.code, new HashMap<Integer, FreeItem>(), subType, false, 0);

			
			
			File entityDir = new FileExt(entityDirPath);
			if (entityDir.exists()) {
				File dataDir = new FileExt(dataDirPath);
				File[] entFiles = entityDir.listFiles();
				int c = 0;
				int i = 0;
				for (File entFile : entFiles) {
					if (entFile.getName().endsWith(".ent")) {

						String UID = changeUIDIfExists(entFile, pos, state, i);

						state.getDatabaseIndex().updateFromFile(entFile);

						String[] prParts = entFile.getName().split("_", 3);
						String prefix = prParts[0] + "_" + prParts[1] + "_";
						assert(UID != null):entFile;
						assert(prefix != null):entFile;
						String fullUID;
						if (!UID.startsWith(prefix)) {
							fullUID = prefix + UID;
						} else {
							fullUID = UID;
						}

						System.err.println("[IMPORTSECTOR] FULL ID '" + fullUID + "' (pre: " + prefix + ", uid " + UID + ")");

						File entityTarget = new FileExt(GameServerState.ENTITY_DATABASE_PATH + fullUID + ".ent");

						FileUtil.copyFile(entFile, entityTarget);

						final String dataNamePattern = entFile.getName().substring(0, (entFile.getName().length() - 4));
						System.err.println("[IMPORTSECTOR] pattern for " + entFile.getName() + " -> " + dataNamePattern);

						File[] dataFiles = dataDir.listFiles((dir1, name) -> {
							String nameP = name.substring(0, name.indexOf("."));

//								System.err.println("CHECKING: "+nameP+" --------- "+dataNamePattern);

							return nameP.equals(dataNamePattern);
						});
						if (dataFiles == null) {
							System.err.println("Couldn't find data files for " + dataNamePattern + " in: " + dataDir.getAbsolutePath() + "; Ok for shops or other transient object");
						} else {
							for (File dataFile : dataFiles) {

								String append = dataFile.getName().substring(dataFile.getName().indexOf("."));

								File dataTarget = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH +
										fullUID + append);

								System.err.println("[IMPORTSECTOR] copy " + dataFile.getAbsolutePath() + " " + dataTarget.getAbsolutePath());
								FileUtil.copyFile(dataFile, dataTarget);
							}
						}
					} else if (entFile.getName().endsWith(".erock")) {
						try {
							BufferedReader readRock = new BufferedReader(new FileReader(entFile));
							String lineRock;
							Vector3f posRock = new Vector3f();
							int genid = 0;
							long seed = 0;
							while ((lineRock = readRock.readLine()) != null) {
								if (lineRock.toLowerCase(Locale.ENGLISH).contains("genid")) {
									String[] split = lineRock.split("=", 2);

									genid = Integer.parseInt(split[1].trim());

								}
								if (lineRock.toLowerCase(Locale.ENGLISH).contains("seed")) {
									String[] split = lineRock.split("=", 2);

									seed = Long.parseLong(split[1].trim());

								}
								if (lineRock.toLowerCase(Locale.ENGLISH).contains("pos")) {
									String[] split = lineRock.split("=", 2);

									String[] posEl = split[1].split(",");
									posRock.x = Float.parseFloat(posEl[0].trim());
									posRock.y = Float.parseFloat(posEl[1].trim());
									posRock.z = Float.parseFloat(posEl[2].trim());

								}
							}
							readRock.close();
							state.getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController(
									System.currentTimeMillis() + "_" + c,
									pos, EntityType.ASTEROID.dbTypeId,
									seed, "", "", "undef", false, 0, posRock,
									new Vector3i(-2, -2, -2), new Vector3i(2, 2, 2), genid, false, false);

							c++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					i++;
				}
			}
			FileUtil.deleteRecursive(tmpDir);
			state.getController().triggerForcedSave();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("EXCEPTION OCCURRED: REMOVING TMP FILES");
			try {
				File dirTmpDel = new FileExt("./sector-export/tmp/");
				FileUtil.deleteRecursive(dirTmpDel);
			} catch (Exception ey) {
				System.err.println("CRITICAL: EXCEPTION WHILE REMOVEING TMP FILES");
				ey.printStackTrace();
			}
			throw new IOException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	public static synchronized void importSector(String fileName, Vector3i posForce, GameServerState state) throws IOException, SQLException {
		importSectorFullDir("./sector-export/", fileName, posForce, state);
	}

	public static synchronized void removeFromDatabaseAndEntityFile(GameServerState state, String withFilePrefix) throws SQLException {
		state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(withFilePrefix, state);
		File ent = new File(GameServerState.ENTITY_DATABASE_PATH+withFilePrefix+".ent");
		ent.delete();
	}
	public static boolean existsEntityPhysical(String withFilePrefix) throws SQLException {
		File ent = new File(GameServerState.ENTITY_DATABASE_PATH+withFilePrefix+".ent");
		return ent.exists();
	}

	public static File getEntityPath(String uidWithPrefix) {
		assert(DatabaseEntry.hasPrefix(uidWithPrefix));
		return new FileExt(GameServerState.ENTITY_DATABASE_PATH+uidWithPrefix+".ent");
	}

}
