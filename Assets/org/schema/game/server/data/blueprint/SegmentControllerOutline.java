package org.schema.game.server.data.blueprint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.game.server.data.simulation.npc.geo.NPCSystem;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class SegmentControllerOutline<E extends SegmentController> {
	public String uniqueIdentifier;
	public String realName;
	public BlueprintInterface en;
	public Vector3i min;
	public Vector3i max;
	public Vector3i spawnSectorId;
	public String playerUID;
	public String dockTo;
	public BluePrintController removeAfterSpawn;
	public boolean scrap;
	public boolean shop;
	protected GameServerState state;
	protected float[] mat;
	protected ArrayList<SegmentControllerOutline<?>> childs;
	protected boolean activeAI;
	protected Tag railTag;
	protected int factionId;
	private ChildStats stats;
	public String blueprintFolder;
	public String blueprintUID;
	public NPCSystem npcSystem;
	public NPCEntitySpecification npcSpec;
	public boolean tradeNode;
	public ElementCountMap itemsToSpawnWith;
	public long parentDbId = -1;
	public long rootDb = -1;
	public boolean checkProspectedBlockCount;
	public SegmentController parent;
	public SegmentControllerOutline<?> parentOutline;
	public SegmentPiece railToSpawnOn;
	public static final ConcurrentLinkedQueue<SegmentControllerOutline> shipUidsToConvert = new ConcurrentLinkedQueue<>();

	public SegmentControllerOutline(GameServerState state,
	                                BlueprintInterface en,
	                                String uniqueIdentifier,
	                                String realName, float[] mat, Vector3i min,
	                                Vector3i max, String playerUID, boolean activeAI, Vector3i sector, ChildStats stats) {
		super();
		this.en = en;
		this.activeAI = activeAI;
		this.state = state;
		this.uniqueIdentifier = uniqueIdentifier;
		this.realName = realName;
		this.mat = mat;
		this.min = min;
		this.max = max;
		spawnSectorId = sector;
		this.stats = stats;
		if (playerUID.startsWith("ENTITY_PLAYERSTATE_")) {
			playerUID = playerUID.substring("ENTITY_PLAYERSTATE_".length());
		}
		this.playerUID = playerUID;
		//INSERTED CODE
		//Queue this ship to have its blueprint mod mappings converted on load
		if(this.en instanceof BlueprintEntry) {
			//Only check new blueprints, not SegmentControllerBluePrintEntryOld
			shipUidsToConvert.add(this);
		}
		///
	}

	private String getChildName(ChildStats stats, String tagName) {
		String ret;
		String newName = realName;

		return newName += (stats.childCounter++);
	}

	public void checkForChilds(int factionId) {

		if (en instanceof BlueprintEntry) {
			BlueprintEntry e = (BlueprintEntry) en;

			if (e.rootRead) {
				stats.rootName = realName;
			}

			int siblingCountTurret = 0;
			int siblingCountDock = 0;
			int railCountDock = 0;
			if (e.getChilds() != null) {
				childs = new ArrayList<SegmentControllerOutline<?>>();

				for (BlueprintEntry blueprintEntry : e.getChilds()) {
					if (blueprintEntry.getEntityType() == BlueprintType.SPACE_STATION &&
							ServerConfig.OVERRIDE_INVALID_BLUEPRINT_TYPE.isOn()) {
						// If a child blueprint is a space station, someone has been editing the bp files...
						// Override with ship type unless server has this disabled.
						blueprintEntry.setEntityType(BlueprintType.SHIP);
					}
					if (blueprintEntry.railDock) {
						Transform t = new Transform();
						t.setFromOpenGLMatrix(mat);

						railCountDock++;

						String tName;
						tName = getChildName(stats, "r");
						
						try {
							SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
							SegmentControllerOutline<?> loadBluePrint = e.getBbController().loadBluePrint(
									state,
									e.metaVersionRead >= 2 ? stats.rootName : tName,
									t,
									-1,
									factionId,
									blueprintEntry,
									this.spawnSectorId,
									null,
									playerUID,
									PlayerState.buffer,
									activeAI, toDockOn, stats);
							assert (blueprintEntry.railTag != null);
							loadBluePrint.railTag = blueprintEntry.railTag;
							childs.add(loadBluePrint);
						} catch (EntityNotFountException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (EntityAlreadyExistsException e1) {
							e1.printStackTrace();
						}
						siblingCountTurret++;
					} else {
						Transform t = new Transform();
						t.setFromOpenGLMatrix(mat);
						t.origin.x += blueprintEntry.getDockingPos().x - SegmentData.SEG_HALF;
						t.origin.y += blueprintEntry.getDockingPos().y - SegmentData.SEG_HALF;
						t.origin.z += blueprintEntry.getDockingPos().z - SegmentData.SEG_HALF;
						String tName;
						if (blueprintEntry.getDockingStyle() == ElementKeyMap.TURRET_DOCK_ID) {
							tName = getChildName(stats, "turret");
							siblingCountTurret++;
						} else {
							tName = getChildName(stats, "dock");
							siblingCountDock++;
						}
						try {
							SegmentPiece toDockOn = null; //this is for spawning turrets manually by the player
							SegmentControllerOutline<?> loadBluePrint = e.getBbController().loadBluePrint(
									state,
									tName,
									t,
									-1,
									factionId,
									blueprintEntry,
									this.spawnSectorId,
									null,
									playerUID,
									PlayerState.buffer,
									activeAI, toDockOn, stats);
							childs.add(loadBluePrint);
						} catch (EntityNotFountException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (EntityAlreadyExistsException e1) {
							e1.printStackTrace();
						}
					}

				}
			}
		}

	}

	public abstract E spawn(Vector3i sectorId, boolean checkProspectedBlockCounts, ChildStats stats, SegmentControllerSpawnCallback callback) throws EntityAlreadyExistsException, StateParameterNotFoundException;

	public abstract long spawnInDatabase(Vector3i startSector, GameServerState state, int creatorThreadId, ObjectArrayList<String> members, ChildStats stats, boolean purgeDuplicate) throws SQLException, EntityAlreadyExistsException, StateParameterNotFoundException, IOException;

	public void checkOkName() throws EntityAlreadyExistsException {
		EntityRequest.existsIdentifier(state, uniqueIdentifier);

		if (childs != null) {
			for (SegmentControllerOutline<?> c : childs) {
				c.checkOkName();
			}
		}
	}

	public int getFactionId() {
		return factionId;
	}

	public boolean hasOldDocking() {
		BlueprintEntry e = (BlueprintEntry) en;
		return e.hasOldDocking();
		
	}
}
