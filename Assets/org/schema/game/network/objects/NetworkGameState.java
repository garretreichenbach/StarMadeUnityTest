package org.schema.game.network.objects;

import org.schema.game.common.data.DebugServerPhysicalObject;
import org.schema.game.common.data.SendableGameState;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.data.simulation.npc.NPCFactionControlCommand;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.*;

public class NetworkGameState extends NetworkEntity implements NetworkInventoryInterface, NTRuleInterface {

	public RemoteLong universeDayDuration = new RemoteLong(this);
	public RemoteLong serverStartTime = new RemoteLong(this);
	public RemoteLong serverModTime = new RemoteLong(this);
	public RemoteLongPrimitive lastFactionPointTurn = new RemoteLongPrimitive(0, this);
	public RemoteIntBuffer frozenSectorRequests = new RemoteIntBuffer(this);

	public RemoteLeaderboardBuffer leaderBoardBuffer = new RemoteLeaderboardBuffer(this);
	public RemoteFactionBuffer factionDel;
	public RemoteFactionBuffer factionAdd;
	public RemoteFactionNewsPostBuffer factionNewsPosts = new RemoteFactionNewsPostBuffer(this);
	public RemoteFactionInvitationBuffer factionInviteAdd = new RemoteFactionInvitationBuffer(this);
	public RemoteFactionInvitationBuffer factionInviteDel = new RemoteFactionInvitationBuffer(this);

	
	public RemoteRaceBuffer raceBuffer = new RemoteRaceBuffer(this); 
	public RemoteRaceModBuffer raceModBuffer = new RemoteRaceModBuffer(this); 
	
	 
	
	public RemoteArrayBuffer<RemoteStringArray> personalElemiesAdd = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);
	public RemoteArrayBuffer<RemoteStringArray> personalElemiesDel = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);

	public RemoteMetaObjectStateLessBuffer metaObjectStateLessBuffer = new RemoteMetaObjectStateLessBuffer(this);

	public RemoteCatalogEntryBuffer catalogBuffer = new RemoteCatalogEntryBuffer(this);
	public RemoteCatalogEntryBuffer catalogDeleteBuffer = new RemoteCatalogEntryBuffer(this);
	public RemoteCatalogEntryBuffer catalogChangeRequestBuffer = new RemoteCatalogEntryBuffer(this);
	public RemoteCatalogEntryBuffer catalogDeleteRequestBuffer = new RemoteCatalogEntryBuffer(this);
	public RemoteArrayBuffer<RemoteStringArray> catalogRatingBuffer = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);

	public RemoteFloat serverShutdown = new RemoteFloat(-1f, this);
	public RemoteFloat serverCountdownTime = new RemoteFloat(-1f, this);
	public RemoteString serverCountdownMessage = new RemoteString("", this);
	public RemoteFloat serverMaxSpeed = new RemoteFloat(50f, this);
	public RemoteFloat sectorSize = new RemoteFloat(1000f, this);
	public RemoteFloat turnSpeedDivisor = new RemoteFloat(1.1f, this);
	public RemoteFloat linearDamping = new RemoteFloat(0.09f, this);
	public RemoteFloat rotationalDamping = new RemoteFloat(0.09f, this);
	public RemoteInteger recipeBlockCost = new RemoteInteger(10000, this);
	public RemoteInteger maxChainDocking = new RemoteInteger(10000, this);
	public RemoteInteger spawnProtection = new RemoteInteger(0, this);
	public RemoteBoolean additiveProjectiles = new RemoteBoolean(false, this);
	public RemoteBoolean dynamicPrices = new RemoteBoolean(false, this);
	public RemoteBoolean buyBBWIthCredits = new RemoteBoolean(false, this);
	public RemoteFloat relativeProjectiles = new RemoteFloat(1.0f, this);
	public RemoteFloat weaponRangeReference = new RemoteFloat(1.0f, this);

	public RemoteArrayBuffer<RemoteStringArray> deployBlockBehaviorChecksum = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);

	public RemoteString serverMessage = new RemoteString("", this);
	public RemoteString gameModeMessage = new RemoteString("", this);

	public RemoteInteger saveSlotsAllowed = new RemoteInteger(0, this);
	public RemoteInteger maxBuildArea = new RemoteInteger(10, this);
	public RemoteString battlemodeInfo = new RemoteString("", this);
	
	public RemoteTradeActiveBuffer tradeActiveBuffer = new RemoteTradeActiveBuffer(this);

	public RemoteShortIntPairBuffer inventoryFillBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteBuffer<RemoteLongIntPair> inventoryProductionLimitBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);
	
	
	public RemoteByteArrayDynBuffer catalogBufferDeflated = new RemoteByteArrayDynBuffer(this);
	@Override
	public RemoteShortIntPairBuffer getInventoryFillBuffer() {
		return inventoryFillBuffer;
	}

	@Override
	public RemoteBuffer<RemoteLongIntPair> getInventoryProductionLimitBuffer() {
		return inventoryProductionLimitBuffer;
	}
	
	public RemoteSerializableObjectBuffer<DebugServerPhysicalObject> debugPhysical = new RemoteSerializableObjectBuffer<DebugServerPhysicalObject>(
			this, DebugServerPhysicalObject::new);

	/**
	 * 0 -> initiator
	 * 1 -> offer ID
	 */
	public RemoteArrayBuffer<RemoteStringArray> factionRelationshipAcceptBuffer = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);

	/**
	 * 0 -> initiator
	 * 1 -> faction id
	 * 2 -> option
	 * 3 -> setting
	 */
	public RemoteArrayBuffer<RemoteStringArray> factionMod = new RemoteArrayBuffer<RemoteStringArray>(4, RemoteStringArray.class, this);

	public RemoteFactionPointUpdateBuffer factionPointMod = new RemoteFactionPointUpdateBuffer(this);

	/**
	 * 0 -> playerName
	 * 1 -> faction id
	 * 2 -> (long)/"r" (permission mask / remove)
	 * 3 -> initiator
	 */
	public RemoteArrayBuffer<RemoteStringArray> factionMemberMod = new RemoteArrayBuffer<RemoteStringArray>(5, RemoteStringArray.class, this);

	/**
	 * 0 -> playerName
	 * 1 -> faction id
	 * 2 -> initiator
	 */
	public RemoteArrayBuffer<RemoteStringArray> factionkickMemberRequests = new RemoteArrayBuffer<RemoteStringArray>(3, RemoteStringArray.class, this);

	/**
	 * 0 -> faction from
	 * 1 -> faction to
	 * 2 -> relationship
	 */
	public RemoteArrayBuffer<RemoteIntegerArray> factionRelationships = new RemoteArrayBuffer<RemoteIntegerArray>(3, RemoteIntegerArray.class, this);
	public RemoteArrayBuffer<RemoteStringArray> factionHomeBaseChangeBuffer = new RemoteArrayBuffer<RemoteStringArray>(7, RemoteStringArray.class, this);
	public RemoteSystemOwnershipChangeBuffer factionClientSystemOwnerChangeBuffer = new RemoteSystemOwnershipChangeBuffer(this);
	public RemoteArrayBuffer<RemoteStringArray> factionRelationshipOffer = new RemoteArrayBuffer<RemoteStringArray>(6, RemoteStringArray.class, this);
	public RemoteFactionRolesBuffer factionRolesBuffer = new RemoteFactionRolesBuffer(this);

	public RemoteArrayBuffer<RemoteStringArray> serverConfig = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);
	public RemoteLongPrimitive seed = new RemoteLongPrimitive(0, this);
	public RemoteIntPrimitive stationCost = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive sectorsToExploreForSystemScan = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive factionKickInactiveTimeLimit = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive aiWeaponSwitchDelay = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive clientUploadBlockSize = new RemoteIntPrimitive(128, this);
	public RemoteFloatPrimitive planetSizeMean = new RemoteFloatPrimitive(0.0F, this);
	public RemoteFloatPrimitive planetSizeDeviation = new RemoteFloatPrimitive(0.0F, this);

	public RemoteFloatPrimitive shopArmorRepairPerSecond = new RemoteFloatPrimitive(0.0F, this);
	public RemoteFloatPrimitive shopRebootCostPerSecond = new RemoteFloatPrimitive(0.0F, this);

	public RemoteBooleanPrimitive weightedCenterOfMass = new RemoteBooleanPrimitive(false, this);
	
	public RemoteBooleanPrimitive lockFactionShips = new RemoteBooleanPrimitive(false, this);
	public RemoteBooleanPrimitive allowPersonalInvOverCap = new RemoteBooleanPrimitive(false, this);
	public RemoteBooleanPrimitive onlyAddFactionToFleet = new RemoteBooleanPrimitive(false, this);
	public RemoteFloatPrimitive massLimitShip = new RemoteFloatPrimitive(-1, this);
	public RemoteFloatPrimitive massLimitPlanet = new RemoteFloatPrimitive(-1, this);
	public RemoteFloatPrimitive massLimitStation = new RemoteFloatPrimitive(-1, this);
	public RemoteIntPrimitive blockLimitShip = new RemoteIntPrimitive(-1, this);
	public RemoteIntPrimitive blockLimitPlanet = new RemoteIntPrimitive(-1, this);
	public RemoteIntPrimitive blockLimitStation = new RemoteIntPrimitive(-1, this);
	
	public RemoteNPCFactionNewsBuffer npcFactionNewsBuffer = new RemoteNPCFactionNewsBuffer(this);
	public RemoteFleetBuffer fleetBuffer = new RemoteFleetBuffer(this, getState());
	public RemoteFleetModBuffer fleetModBuffer = new RemoteFleetModBuffer(this);
	public RemoteFleetCommandBuffer fleetCommandBuffer = new RemoteFleetCommandBuffer(this);
	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);
	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);
	public RemoteInventoryBuffer inventoryChangeBuffer;
	public RemoteNPCSystemBuffer npcSystemBuffer = new RemoteNPCSystemBuffer(this);
	public RemoteBooleanPrimitive npcDebug = new RemoteBooleanPrimitive(false, this);
	public RemoteBooleanPrimitive fow = new RemoteBooleanPrimitive(false, this);

//	public RemoteNPCDiplomacyBuffer npcDiplomacyBuffer;
	public RemoteSimpleCommandBuffer simpleCommandQueue = new RemoteSimpleCommandBuffer(this, NPCFactionControlCommand::new);
	public RemoteFloatPrimitive npcFleetSpeedLoaded = new RemoteFloatPrimitive(1, this);
	public RemoteString npcShopOwnersDebug = new RemoteString(this);
	public RemoteBooleanPrimitive manCalcCancelOn = new RemoteBooleanPrimitive(false, this);
	public RemoteLongBooleanPairBuffer modulesEnabledByDefault = new RemoteLongBooleanPairBuffer(this);
	

	
	public RemoteRuleRulePropertyBuffer rulePropertyBuffer = new RemoteRuleRulePropertyBuffer(this);
	
	public RemoteRuleSetManagerBuffer ruleSetManagerBuffer = new RemoteRuleSetManagerBuffer(this);
	public RemoteBooleanPrimitive allowFactoriesOnShip = new RemoteBooleanPrimitive(false, this);
	public RemoteBooleanPrimitive shipyardIgnoreStructure = new RemoteBooleanPrimitive(false, this);
	public RemoteRuleStateChangeBuffer ruleChangeBuffer = new RemoteRuleStateChangeBuffer(this);
	public RemoteIntBuffer ruleStateRequestBuffer = new RemoteIntBuffer(this);
	public RemoteBuffer<RemoteString> ruleIndividualAddRemoveBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteBooleanPrimitive ignoreDockingArea = new RemoteBooleanPrimitive(false, this);

	public NetworkGameState(SendableGameState sst, StateInterface state) {
		super(state);
		this.factionDel = new RemoteFactionBuffer(this, state);
		this.factionAdd = new RemoteFactionBuffer(this, state);
		inventoryChangeBuffer = new RemoteInventoryBuffer(sst, this);
//		npcDiplomacyBuffer = new RemoteNPCDiplomacyBuffer((FactionState) state, this);
	}

	@Override
	public RemoteInventoryClientActionBuffer getInventoryClientActionBuffer() {
		return inventoryClientActionBuffer;
	}

	@Override
	public RemoteInventoryMultModBuffer getInventoryMultModBuffer() {
		return inventoryMultModBuffer;
	}

	@Override
	public RemoteLongBuffer getInventoryProductionBuffer() {
		return inventoryProductionBuffer;
	}

	@Override
	public RemoteShortIntPairBuffer getInventoryFilterBuffer() {
		return inventoryFilterBuffer;
	}

	@Override
	public RemoteLongStringBuffer getInventoryCustomNameModBuffer() {
		return null;
	}

	@Override
	public RemoteLongStringBuffer getInventoryPasswordModBuffer() {
		return null;
	}

	@Override
	public RemoteLongBooleanPairBuffer getInventoryAutoLockModBuffer() {
		return null;
	}

	@Override
	public void onDelete(StateInterface stateI) {
	}

	@Override
	public void onInit(StateInterface stateI) {
	}

	@Override
	public RemoteInventoryBuffer getInventoriesChangeBuffer() {
		return inventoryChangeBuffer;
	}

	@Override
	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer() {
		return inventorySlotRemoveRequestBuffer;
	}
	@Override
	public RemoteRuleStateChangeBuffer getRuleStateChangeBuffer() {
		//used for factions
		return ruleChangeBuffer;
	}

	@Override
	public RemoteIntBuffer getRuleStateRequestBuffer() {
		//used for factions
		return ruleStateRequestBuffer;
	}

	@Override
	public RemoteBuffer<RemoteString> getRuleIndividualAddRemoveBuffer() {
		//used for factions
		return ruleIndividualAddRemoveBuffer;
	}
}
