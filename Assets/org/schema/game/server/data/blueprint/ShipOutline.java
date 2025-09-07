package org.schema.game.server.data.blueprint;

import java.io.IOException;
import java.sql.SQLException;

import javax.vecmath.Quat4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.database.DatabaseEntry;
import org.schema.game.common.data.VoidSegmentPiece;
import org.schema.game.common.data.VoidUniqueSegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Chunk16SegmentData;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.Universe;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.SectorUtil;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShipOutline extends SegmentControllerOutline<Ship> {
	
	
	
	public ShipOutline(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                   String realName, float[] mat, int factionId, Vector3i min,
	                   Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super(state, en, uniqueIdentifier, realName, mat, min, max, playerUID, activeAI, sector, stats);
		this.factionId = factionId;
		checkForChilds(this.factionId);

	}

	@Override
	public Ship spawn(Vector3i sectorPos, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException {
		assert (sectorPos != null);
		
		checkOkName();

		Sector sector = callback.sector;
		Ship s = null;
		try {

			if (sector == null || sector.isActive()) {

//				if(railToSpawnOn != null) {
//					assert(false):"spawn on rail"+railToSpawnOn;
//				}
				s = EntityRequest.getNewShip(state, uniqueIdentifier, sector != null ? sector.getId() : -1, realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, playerUID, en.isChunk16());
				s.setFactionId(factionId);
				s.setFactionFromBlueprint(true);
				s.npcSystem = this.npcSystem;
				s.npcSpec = this.npcSpec;
				ElementCountMap toSpawnWithPart = null;
				if(itemsToSpawnWith != null && en.getCapacitySingle() > 0){
					toSpawnWithPart = new ElementCountMap();
					toSpawnWithPart.transferFrom(itemsToSpawnWith, en.getCapacitySingle());
				}
				s.itemsToSpawnWith = toSpawnWithPart;
				if (stats.usedNamed.contains(uniqueIdentifier)) {
					assert (false) : "Already exists: " + uniqueIdentifier + "; USED: " + stats.usedNamed;
					throw new EntityAlreadyExistsException(uniqueIdentifier);
				}
				stats.usedNamed.add(uniqueIdentifier);
				System.err.println("[BB] SETTING FACTION OF " + s + " to " + factionId);
				long t = System.currentTimeMillis();
				s.usedOldPowerFromTag = en.isOldPowerFlag();
				s.oldPowerBlocksFromBlueprint = en.getElementMap().get(ElementKeyMap.POWER_ID_OLD);
				s.getControlElementMap().setFromMap(en.getControllingMap());
				long timeControllingMap = System.currentTimeMillis() - t;
				long timeTag = 0;

				if (en instanceof BlueprintEntry) {
					BlueprintEntry e = (BlueprintEntry) en;
					if (e.rootRead) {
						stats.rootName = uniqueIdentifier;
					}
					if (e.getManagerTag() != null) {
						s.getManagerContainer().loadInventoriesFromTag = false;
												System.err.println("[OUTLINE) BLUEPRINT MANAGER TAG EXISTS");
						t = System.currentTimeMillis();
						s.getManagerContainer().fromTagStructure(e.getManagerTag());
						s.getManagerContainer().handleBlueprintWireless(stats.rootName, e.wirelessToOwnRail);
						timeTag = System.currentTimeMillis() - t;
						s.getManagerContainer().loadInventoriesFromTag = true;

					}
				}
				s.setTouched(true, false);
				if (childs != null) {
					for (SegmentControllerOutline<?> c : childs) {
						c.parent = s;
						c.parentOutline = this;
					}
					for (SegmentControllerOutline<?> c : childs) {
						c.scrap = this.scrap;
						c.dockTo = uniqueIdentifier;
						c.itemsToSpawnWith = itemsToSpawnWith;
						assert (c.dockTo != null);
						c.spawn(sectorPos, checkProspectedBlockCounts, stats, callback);
					}
				}
				if (en.getAiTag() != null) {
					s.getAiConfiguration().fromTagStructure(en.getAiTag());
				}

				if (factionId != FactionManager.ID_NEUTRAL && en.getAiTag() == null) {

					//apply default config if there is none loaded with this blueprint entity
					s.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", false);
					if (dockTo != null && ((BlueprintEntry) en).getDockingStyle() == ElementKeyMap.TURRET_DOCK_ID) {
						s.getAiConfiguration().get(Types.TYPE).switchSetting("Turret", false);
					} else {
						s.getAiConfiguration().get(Types.TYPE).switchSetting("Ship", false);
					}
				}
				if(!s.getAiConfiguration().get(Types.ACTIVE).isOn()){
					s.getAiConfiguration().get(Types.ACTIVE).switchSetting(activeAI ? "true" : "false", false);
				}
				s.getAiConfiguration().applyServerSettings();

				s.blueprintIdentifier = blueprintUID;
				s.blueprintSegmentDataPath = blueprintFolder;
				
				s.setScrap(this.scrap);
				if (railTag != null) {
					assert (dockTo != null);
					boolean loadExpectedToDock = false; //don't need expected here since the UIDs of those are not right
					s.railController.fromTag(railTag, en.isChunk16() ? Chunk16SegmentData.SHIFT_ : 0, loadExpectedToDock);
					s.railController.modifyDockingRequestNames(uniqueIdentifier, dockTo);
					
					assert(parent != null);
					if(parent != null){
						//add to excpected if this is a rail child
						assert(uniqueIdentifier.startsWith(en.getType().type.dbPrefix));
						parent.railController.addExpectedToDock(s.railController.getCurrentRailRequest());
					}
					
					assert (s.railController.railRequestCurrent.docked.uniqueIdentifierSegmentController.equals(s.getUniqueIdentifier()));
					assert (!s.railController.railRequestCurrent.rail.uniqueIdentifierSegmentController.equals(s.getUniqueIdentifier()));

					assert (!dockTo.equals(uniqueIdentifier)) : "WARNING: DOCK TO SELF: " + dockTo;
					assert (!dockTo.equals(s.getUniqueIdentifier())) : "WARNING: DOCK TO SELF: " + dockTo;
					
//					System.err.println("RAIL TAG LOADED FOR "+s);
				} else if (dockTo != null) {
					BlueprintEntry e = (BlueprintEntry) en;
					System.err.println("[SHIPOUTLINE] ADDING DELAYED DOCK " + uniqueIdentifier + " to " + dockTo);
					s.getDockingController().getSize().set(e.getDockingSize());
					Quat4f rot = new Quat4f(0, 0, 0, 1);
					Vector3i cc = new Vector3i(e.getDockingPos());
					s.getDockingController().requestDelayedDock(new String(dockTo), cc, rot, e.getDockingOrientation());
					s.setHidden(true);
				}
				
				if(railToSpawnOn != null) {
					if(en.getDockerPoints() == null || en.getDockerPoints().isEmpty()) {
						callback.onNoDocker();
					}else {
						VoidSegmentPiece docker = en.getDockerPoints().values().iterator().next();
						((VoidUniqueSegmentPiece)docker).setSegmentController(s);
						System.err.println("[SHIPOUTLINE] Connecting docker to rail \n"+railToSpawnOn+" \n-> \n"+railToSpawnOn);
						
						s.railController.connectServer(docker, railToSpawnOn);
					}
				}
				if (checkProspectedBlockCounts) {
					s.setProspectedElementCount(en, playerUID);
				}
				if (sector != null) {
					callback.onSpawn(s);
//					state.getController().getSynchController().addNewSynchronizedObjectQueued(s);
				}else {
					callback.onNullSector(s);
				}
				if (timeControllingMap > 500 || timeTag > 500) {
					System.err.println("[SHIPOUTLINE] WARNING: spawning of " + uniqueIdentifier + " took long -- tControlMap: " + timeControllingMap + "ms; tTag: " + timeTag + "ms");
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (removeAfterSpawn != null) {

			if (en instanceof BlueprintEntry) {
				try {
					removeAfterSpawn.removeBluePrint(((BlueprintEntry) en));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return s;
	}

	@Override
	public long spawnInDatabase(Vector3i startSector, GameServerState state, int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate) throws SQLException, EntityAlreadyExistsException, StateParameterNotFoundException, IOException {
		Transform t = new Transform();
		t.setFromOpenGLMatrix(mat);

		if(purgeDuplicate){
			try {
				SectorUtil.removeFromDatabaseAndEntityFile(state, EntityType.SHIP.dbPrefix+DatabaseEntry.removePrefixWOException(uniqueIdentifier));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		EntityRequest.existsIdentifier(state, uniqueIdentifier);
		Ship s = EntityRequest.getNewShip(state, uniqueIdentifier, -123, realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, playerUID, en.isChunk16());
		s.npcSystem = this.npcSystem;
		s.npcSpec = this.npcSpec;
		s.setFactionId(factionId);
		s.getControlElementMap().setFromMap(en.getControllingMap());
		s.getWorldTransform().set(t);
		s.getInitialTransform().set(t);
		s.blueprintIdentifier = blueprintUID;
		s.blueprintSegmentDataPath = blueprintFolder;
		s.setSpawnedInDatabaseAsChunk16(en.isChunk16());
		s.usedOldPowerFromTagForcedWrite = en.isOldPowerFlag();
		s.usedOldPowerFromTag = en.isOldPowerFlag();
		s.oldPowerBlocksFromBlueprint = en.getElementMap().get(ElementKeyMap.POWER_ID_OLD);

		if (factionId != FactionManager.ID_NEUTRAL) {
			s.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", true);
			s.getAiConfiguration().get(Types.TYPE).switchSetting("Ship", true);
			s.getAiConfiguration().get(Types.ACTIVE).switchSetting("true", true);
			s.getAiConfiguration().applyServerSettings();
		}
		s.transientSectorPos.set(startSector);
		if (railTag != null) {
			assert (dockTo != null);
//			assert(false):en.isChunk16()+"; "+en;
			
			boolean loadExpectedToDock = false; //don't need expected here since the UIDs of those are not right
			//Shift is needed for chunk16 blueprints. no extra shift is done on loading
			s.railController.fromTag(railTag, en.isChunk16() ? Chunk16SegmentData.SHIFT_ : 0, loadExpectedToDock);
			s.railController.modifyDockingRequestNames(uniqueIdentifier, dockTo);
			
		} else if (dockTo != null) {
			BlueprintEntry e = (BlueprintEntry) en;
			System.err.println("[SHIPOUTLINE] DATABASE ADDING DELAYED DOCK " + uniqueIdentifier + " to " + dockTo);
			s.getDockingController().getSize().set(e.getDockingSize());
			Quat4f orientation = new Quat4f(0, 0, 0, 1);
			s.getDockingController().tagOverwrite = new DockingTagOverwrite(dockTo, new Vector3i(e.getDockingPos()), orientation, e.getDockingOrientation());
		}
		ElementCountMap toSpawnWithPart = null;
		if(itemsToSpawnWith != null && en.getCapacitySingle() > 0){
			toSpawnWithPart = new ElementCountMap();
			toSpawnWithPart.transferFrom(itemsToSpawnWith, en.getCapacitySingle());
		}
		s.itemsToSpawnWith = toSpawnWithPart;
		
		state.getController().writeEntity(s, false);
		s.getDockingController().tagOverwrite = null;
		System.err.println("[BLUEPRINT][SPAWNINDB] added to members: " + uniqueIdentifier +" in sector "+startSector);
		members.add(new String(uniqueIdentifier));
		long dbId = state.getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController(
				DatabaseEntry.removePrefixWOException(uniqueIdentifier),
						startSector,
						s.getType().dbTypeId,
						Universe.getRandom().nextLong(),
						"<sim>",
						"<sim>",
						realName,
						false,
						factionId,
						t.origin,
						min,
						max,
						creatorThreadId,
						true,
						parentDbId,
						rootDb,
						false);
		
		if(s.npcSystem != null){
			assert(s.npcSpec != null);
			s.npcSystem.getContingent().spawn(s.npcSpec, dbId);
			s.npcSystem = null;
			s.npcSpec = null;
		}
		if (childs != null) {
			for (SegmentControllerOutline<?> c : childs) {
				c.dockTo = uniqueIdentifier;
				c.parentDbId = dbId;
				
				//if we dont have a parent, set it initially
				//else inherit it from parent
				if(parentDbId <= 0){
					c.rootDb = dbId;
				}else{
					c.rootDb = rootDb;
				}
				c.itemsToSpawnWith = itemsToSpawnWith;
				c.spawnInDatabase(startSector, state, creatorThreadId, members, stats, purgeDuplicate);
			}
		}
		return dbId;
	}

}
