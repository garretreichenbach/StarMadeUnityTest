package org.schema.game.server.data.blueprint;

import java.io.IOException;
import java.sql.SQLException;

import javax.vecmath.Quat4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.ai.Types;
import org.schema.game.common.controller.database.DatabaseEntry;
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

public class SpaceStationOutline extends SegmentControllerOutline<SpaceStation> {
	private int factionId;

	public SpaceStationOutline(GameServerState state, BlueprintInterface en, String uniqueIdentifier,
	                           String realName, float[] mat, int factionID, Vector3i min,
	                           Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super(state, en, uniqueIdentifier, realName, mat, min, max, playerUID, activeAI, sector, stats);
		this.factionId = factionID;
		checkForChilds(factionId);
	}

	@Override
	public SpaceStation spawn(Vector3i sectorId, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException, StateParameterNotFoundException {

		SpaceStation s = null;
		try {
			Sector sector = callback.sector;
			if (sector != null && sector.isActive()) {
				EntityRequest.existsIdentifier(state, uniqueIdentifier);
				s = EntityRequest.getNewSpaceStation(state, uniqueIdentifier, sector.getId(), realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, en.isChunk16());
				s.npcSystem = this.npcSystem;
				s.npcSpec = this.npcSpec;
				if (stats.usedNamed.contains(uniqueIdentifier)) {
					assert (false) : "Already exists: " + uniqueIdentifier + "; USED: " + stats.usedNamed;
					throw new EntityAlreadyExistsException(uniqueIdentifier);
				}
				stats.usedNamed.add(uniqueIdentifier);
				if (en instanceof BlueprintEntry) {
					BlueprintEntry e = (BlueprintEntry) en;
					
					
					if (e.rootRead) {
						stats.rootName = uniqueIdentifier;
					}

					if (e.getManagerTag() != null) {
						s.getManagerContainer().loadInventoriesFromTag = false;

						s.getManagerContainer().fromTagStructure(e.getManagerTag());
						s.getManagerContainer().handleBlueprintWireless(stats.rootName, e.wirelessToOwnRail);
						s.getManagerContainer().getShoppingAddOn().setCredits(0);
						s.getManagerContainer().loadInventoriesFromTag = true;

					}
				}

				
				s.setTouched(true, false);
				ElementCountMap toSpawnWithPart = null;
				if(itemsToSpawnWith != null && en.getCapacitySingle() > 0){
					toSpawnWithPart = new ElementCountMap();
					toSpawnWithPart.transferFrom(itemsToSpawnWith, en.getCapacitySingle());
				}
				s.itemsToSpawnWith = toSpawnWithPart;
				
				if (childs != null) {
					for (SegmentControllerOutline<?> c : childs) {
						if (c.railTag != null) {
							//add to excpected if this is a rail child
//							assert(c.uniqueIdentifier.startsWith(c.en.getType().type.dbPrefix));
//							s.railController.addExpectedToDock(c.uniqueIdentifier);
							
						}
						c.parent = s;
						c.parentOutline = this;
					}
					for (SegmentControllerOutline<?> c : childs) {
						c.scrap = scrap;
						c.dockTo = uniqueIdentifier;
						c.itemsToSpawnWith = itemsToSpawnWith;
						assert (c.dockTo != null);
						c.spawn(sectorId, false, stats, callback);
					}
				}
				s.usedOldPowerFromTag = en.isOldPowerFlag();
				s.oldPowerBlocksFromBlueprint = en.getElementMap().get(ElementKeyMap.POWER_ID_OLD);
				s.setFactionId(factionId);
				s.getControlElementMap().getControllingMap().setFromMap(en.getControllingMap());
				if (factionId != FactionManager.ID_NEUTRAL) {
					s.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", true);
					s.getAiConfiguration().get(Types.TYPE).switchSetting("Ship", true);
					s.getAiConfiguration().get(Types.ACTIVE).switchSetting("true", true);

					s.getAiConfiguration().applyServerSettings();
				}
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
				} else if (dockTo != null) {
					BlueprintEntry e = (BlueprintEntry) en;
					System.err.println("[STATIONOUTLINE] ADDING DELAYED DOCK " + uniqueIdentifier + " to " + dockTo);
					s.getDockingController().getSize().set(e.getDockingSize());
					Quat4f rot = new Quat4f(0, 0, 0, 1);
					s.getDockingController().requestDelayedDock(new String(dockTo), new Vector3i(e.getDockingPos()), rot, e.getDockingOrientation());
					s.setHidden(true);
				}
				if (checkProspectedBlockCounts) {
					s.setProspectedElementCount(en.getElementMap());
				}
				s.getShoppingAddOn().setTradeNodeOn(tradeNode);
				s.blueprintIdentifier = blueprintUID;
				s.blueprintSegmentDataPath = blueprintFolder;
				
				System.err.println("[BLUEPRINT] UID: "+uniqueIdentifier+"; writeUID: "+s.getWriteUniqueIdentifier());
				s.setScrap(this.scrap);
				s.getManagerContainer().getShoppingAddOn().clearInventory(false);
				if (shop) {
					s.getManagerContainer().getShoppingAddOn().getOwnerPlayers().clear();
					s.getManagerContainer().getShoppingAddOn().fillInventory(false, false);
				}
				s.getManagerContainer().getShoppingAddOn().setInfiniteSupply(false);
				callback.onSpawn(s);
//				state.getController().getSynchController().addNewSynchronizedObjectQueued(s);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return s;

	}

	@Override
	public long spawnInDatabase(Vector3i startSector, GameServerState state, int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate) throws SQLException, EntityAlreadyExistsException, StateParameterNotFoundException, IOException {

		Transform t = new Transform();
		t.setFromOpenGLMatrix(mat);

		if(purgeDuplicate){
			try {
				SectorUtil.removeFromDatabaseAndEntityFile(state, EntityType.SPACE_STATION.dbPrefix+DatabaseEntry.removePrefixWOException(uniqueIdentifier));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		members.add(uniqueIdentifier);
		EntityRequest.existsIdentifier(state, uniqueIdentifier);
		SpaceStation s = EntityRequest.getNewSpaceStation(state, uniqueIdentifier, -123, realName, mat, min.x, min.y, min.z, max.x, max.y, max.z, en.isChunk16());
		s.setFactionId(factionId);
		s.getControlElementMap().setFromMap(en.getControllingMap());
		s.getWorldTransform().set(t);
		s.getInitialTransform().set(t);
		s.npcSystem = this.npcSystem;
		s.npcSpec = this.npcSpec;
		s.setSpawnedInDatabaseAsChunk16(en.isChunk16());
		s.usedOldPowerFromTagForcedWrite = en.isOldPowerFlag();
		s.usedOldPowerFromTag = en.isOldPowerFlag();
		s.oldPowerBlocksFromBlueprint = en.getElementMap().get(ElementKeyMap.POWER_ID_OLD);
		if (factionId != FactionManager.ID_NEUTRAL && s.getType() == EntityType.SHIP) {
			s.getAiConfiguration().get(Types.AIM_AT).switchSetting("Any", true);
			s.getAiConfiguration().get(Types.TYPE).switchSetting("Ship", true);
			s.getAiConfiguration().get(Types.ACTIVE).switchSetting("true", true);
			s.getAiConfiguration().applyServerSettings();
		}
		s.blueprintIdentifier = blueprintUID;
		s.blueprintSegmentDataPath = blueprintFolder;
		s.transientSectorPos.set(startSector);
		
		ElementCountMap toSpawnWithPart = null;
		if(itemsToSpawnWith != null && en.getCapacitySingle() > 0){
			toSpawnWithPart = new ElementCountMap();
			toSpawnWithPart.transferFrom(itemsToSpawnWith, en.getCapacitySingle());
		}
		
		s.itemsToSpawnWith = toSpawnWithPart;
		
		if (railTag != null) {
			assert (dockTo != null);
			boolean loadExpectedToDock = false; //don't need expected here since the UIDs of those are not right
			//no shift needed, because its going to shift if needed on actual loading
			s.railController.fromTag(railTag, 0, loadExpectedToDock);
			s.railController.modifyDockingRequestNames(uniqueIdentifier, dockTo);
			
		} else if (dockTo != null) {
			BlueprintEntry e = (BlueprintEntry) en;
			System.err.println("[SHIPOUTLINE] DATABASE ADDING DELAYED DOCK " + uniqueIdentifier + " to " + dockTo);
			s.getDockingController().getSize().set(e.getDockingSize());
			Quat4f orientation = new Quat4f(0, 0, 0, 1);
			s.getDockingController().tagOverwrite = new DockingTagOverwrite(dockTo, new Vector3i(e.getDockingPos()), orientation, e.getDockingOrientation());
		}
		
		s.getShoppingAddOn().setTradeNodeOn(tradeNode);
		
		state.getController().writeEntity(s, false);
		
		s.getDockingController().tagOverwrite = null;
		
		long dbId = state.getDatabaseIndex().getTableManager().getEntityTable().updateOrInsertSegmentController
				(DatabaseEntry.removePrefixWOException(uniqueIdentifier),
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
						creatorThreadId, true, 
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
			for (SegmentControllerOutline<?> c : childs ) {
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
