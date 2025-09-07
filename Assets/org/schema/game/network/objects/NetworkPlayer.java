package org.schema.game.network.objects;

import org.schema.game.common.data.player.NetworkPlayerInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.*;

public class NetworkPlayer extends NetworkObject implements NetworkInventoryInterface, NetworkPlayerInterface, NTRuleInterface {

	public RemoteBooleanPrimitive upgradedAccount = new RemoteBooleanPrimitive(false, this);

	public RemoteFloatPrimitive health = new RemoteFloatPrimitive(1f, this);
	public RemoteIntPrimitive clientId = new RemoteIntPrimitive(-777777, this);
	public RemoteIntPrimitive currentSectorType = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive sectorId = new RemoteIntPrimitive(-2, this);
	public RemoteVector3i sectorPos = new RemoteVector3i(this);
	public RemoteVector3i waypoint = new RemoteVector3i(this);
	public RemoteLongPrimitive credits = new RemoteLongPrimitive(0, this);
	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);
	public RemoteInteger kills = new RemoteInteger(this);
	public RemoteIntPrimitive helmetSlot = new RemoteIntPrimitive(0, this);
	public RemoteBooleanPrimitive infiniteInventoryVolume = new RemoteBooleanPrimitive(false, this);
	public RemoteLongPrimitiveArray sittingState = new RemoteLongPrimitiveArray(4, this);
	public RemoteSimpleCommandBuffer simpleCommandQueue = new RemoteSimpleCommandBuffer(this, SimplePlayerCommand::new);
	public RemoteInteger deaths = new RemoteInteger(this);
	public RemoteLongPrimitive lastDeathNotSuicide = new RemoteLongPrimitive(0, this);
	public RemoteBooleanPrimitive isAdminClient = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive hasCreativeMode = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive useCreativeMode = new RemoteBooleanPrimitive(this);
	public RemoteIntBuffer requestCargoMode = new RemoteIntBuffer(this);
	public RemoteBooleanPrimitive useCargoMode = new RemoteBooleanPrimitive(this);
	public RemoteInteger aquiredTargetId = new RemoteInteger(-1, this);
	public RemoteBlockCountMapBuffer blockCountMapBuffer = new RemoteBlockCountMapBuffer(this);
	public RemoteSegmentControllerBlockBuffer cargoInventoryChange = new RemoteSegmentControllerBlockBuffer(this);
	public RemoteArrayBuffer<RemoteIntegerArray> factionEntityIdChangeBuffer = new RemoteArrayBuffer<RemoteIntegerArray>(2, RemoteIntegerArray.class, this);
	public RemoteIntPrimitive selectedEntityId = new RemoteIntPrimitive(-1, this);
	public RemoteIntPrimitive selectedAITargetId = new RemoteIntPrimitive(-1, this);
	public RemoteIntPrimitive ping = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive playerFaceId = new RemoteIntPrimitive(1, this);
	public RemoteBytePrimitive shipControllerSlot = new RemoteBytePrimitive((byte) 0, this);
	public RemoteBytePrimitive buildSlot = new RemoteBytePrimitive((byte) 0, this);
	public RemoteBoolean canRotate = new RemoteBoolean(true, this);
	public RemoteString skinName = new RemoteString(this);
	public RemoteBooleanPrimitive invisibility = new RemoteBooleanPrimitive(this);
	public RemoteString playerName = new RemoteString(this);
	public RemoteInteger factionId = new RemoteInteger(this);
	public RemoteFactionBuffer factionCreateBuffer;
	public RemoteIntBuffer factionLeaveBuffer = new RemoteIntBuffer(this);
	public RemoteIntBuffer factionJoinBuffer = new RemoteIntBuffer(this);
	public RemoteVector4f tint = new RemoteVector4f(this);
	public RemoteCrewFleetBuffer crewRequest = new RemoteCrewFleetBuffer(this);
	public RemoteBuffer<RemoteString> factionDescriptionEditRequest = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteBuffer<RemoteString> factionChatRequests = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteArrayBuffer<RemoteIntegerArray> roundEndBuffer = new RemoteArrayBuffer<RemoteIntegerArray>(3, RemoteIntegerArray.class, this);
	public RemoteIntBuffer killedBuffer = new RemoteIntBuffer(this);
	public RemoteControlledFileStreamBuffer shipUploadBuffer;
	public RemoteControlledFileStreamBuffer skinUploadBuffer;
	public RemoteControlledFileStreamBuffer skinDownloadBuffer;
	public RemoteBooleanArray activeControllerMask = new RemoteBooleanArray(4, this);
	public RemoteCreatureSpawnBuffer creatureSpawnBuffer = new RemoteCreatureSpawnBuffer(this);
	public RemoteConversationBuffer converationBuffer = new RemoteConversationBuffer(this);
	public RemoteControllerUnitRequestBuffer controlRequestParameterBuffer = new RemoteControllerUnitRequestBuffer(this);
	public RemoteIntBuffer factionShareFowBuffer = new RemoteIntBuffer(this);
	public RemoteBuffer<RemoteVector3i> resetFowBuffer = new RemoteBuffer<RemoteVector3i>(RemoteVector3i.class, this);
	public RemoteBuffer<RemoteString> creditTransactionBuffer = new RemoteBuffer<>(RemoteString.class, this);
	public RemoteDragDropBuffer dropOrPickupSlots = new RemoteDragDropBuffer(this);
	public RemoteIntBuffer recipeRequests = new RemoteIntBuffer(this);
	public RemoteIntBuffer recipeSellRequests = new RemoteIntBuffer(this);
	public RemoteIntBuffer fixedRecipeBuyRequests = new RemoteIntBuffer(this);
	public RemoteServerMessageBuffer messages = new RemoteServerMessageBuffer(this);
	public RemoteFloatPrimitive frontBackAxis = new RemoteFloatPrimitive(0, this);
	public RemoteFloatPrimitive rightLeftAxis = new RemoteFloatPrimitive(0, this);
	public RemoteFloatPrimitive upDownAxis = new RemoteFloatPrimitive(0, this);
	public RemoteBuffer<RemoteString> skinRequestBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteBlueprintPlayerHandleRequestBuffer catalogPlayerHandleBuffer = new RemoteBlueprintPlayerHandleRequestBuffer(this);
	public RemoteArrayBuffer<RemoteIntegerArray> buyBuffer = new RemoteArrayBuffer<RemoteIntegerArray>(2, RemoteIntegerArray.class, this);
	public RemoteArrayBuffer<RemoteIntegerArray> sellBuffer = new RemoteArrayBuffer<RemoteIntegerArray>(2, RemoteIntegerArray.class, this);
	public RemoteArrayBuffer<RemoteIntegerArray> deleteBuffer = new RemoteArrayBuffer<RemoteIntegerArray>(3, RemoteIntegerArray.class, this);
	public RemoteBuffer<RemoteBoolean> spawnRequest = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteInventoryBuffer inventoryBuffer;
	//	public RemoteIntArrayBuffer inventoryUpdateBuffer = new RemoteIntArrayBuffer(5, this);
	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteProximitySector proximitySector;

	//	public RemoteVector3f spawnPoint = new RemoteVector3f(this);
//	public RemoteBuffer<RemoteVector3f> spawnPointSetBuffer = new RemoteBuffer<RemoteVector3f>(RemoteVector3f.class, this);
	public RemoteProximitySystem proximitySystem;
	public RemoteIntBuffer creditsDropBuffer = new RemoteIntBuffer(this);
	public RemoteCatalogEntryBuffer catalogBuffer = new RemoteCatalogEntryBuffer(this);

	public RemoteShort inputState = new RemoteShort(this);

	public RemoteMatrix3f camOrientation = new RemoteMatrix3f(this);

	public RemoteCockpit cockpit;
	public RemoteArrayBuffer<RemoteLongArray> textureChangedBroadcastBuffer = new RemoteArrayBuffer<RemoteLongArray>(2, RemoteLongArray.class, this);
	public RemoteBuffer<RemoteBoolean> requestFactionOpenToJoin = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteBuffer<RemoteBoolean> requestAttackNeutral = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteBuffer<RemoteBoolean> requestAutoDeclareWar = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteBytePrimitive hitNotifications = new RemoteBytePrimitive((byte) 0, this);
	public RemoteBuffer<RemoteString> tutorialCalls = new RemoteBuffer<RemoteString>(RemoteString.class, this);

	public RemoteVector3i personalSector = new RemoteVector3i(this);
	public RemoteVector3i testSector = new RemoteVector3i(this);

	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);

	public RemoteLongPrimitive dbId = new RemoteLongPrimitive(0, this);

	public RemoteIntPrimitive inputSeed = new RemoteIntPrimitive(0, this);
	public RemoteShortIntPairBuffer inventoryFillBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteBuffer<RemoteLongIntPair> inventoryProductionLimitBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);

	public RemoteIntBuffer mineArmTimerRequests = new RemoteIntBuffer(this);
	public RemoteIntPrimitive mineArmTimer = new RemoteIntPrimitive(0, this);

	public RemoteLongPrimitive lastSpawnedThisSession = new RemoteLongPrimitive(0L, this);

	public RemoteTransformationBuffer buildModePositionBuffer = new RemoteTransformationBuffer(this);
	public RemoteBooleanPrimitive isInBuildMode = new RemoteBooleanPrimitive(false, this);
	public RemoteBooleanPrimitive isBuildModeSpotlight = new RemoteBooleanPrimitive(true, this);
	public RemoteBuffer<RemoteString> ruleIndividualAddRemoveBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);

	public RemoteRuleStateChangeBuffer ruleChangeBuffer = new RemoteRuleStateChangeBuffer(this);

	public RemoteIntBuffer ruleStateRequestBuffer = new RemoteIntBuffer(this);
	public RemoteBooleanPrimitive adjustMode = new RemoteBooleanPrimitive(false, this);

	public RemoteIntPrimitive keyboardPressedMap = new RemoteIntPrimitive(0, this);
	public RemoteByteBuffer keyboardEventQueue = new RemoteByteBuffer(this);
	
	public RemoteBuffer<RemoteString> blueprintMarketItemModBuffer = new RemoteBuffer<>(RemoteString.class, this);
	
	@Override
	public RemoteShortIntPairBuffer getInventoryFillBuffer() {
		return inventoryFillBuffer;
	}

	@Override
	public RemoteBuffer<RemoteLongIntPair> getInventoryProductionLimitBuffer() {
		return inventoryProductionLimitBuffer;
	}

	@Override
	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer() {
		return inventorySlotRemoveRequestBuffer;
	}

	public NetworkPlayer(StateInterface state, PlayerState player) {
		super(state);
		inventoryBuffer = new RemoteInventoryBuffer(player, this);
		proximitySector = new RemoteProximitySector(player.getProximitySector(), this);
		proximitySystem = new RemoteProximitySystem(player.getProximitySystem(), this);

		shipUploadBuffer = new RemoteControlledFileStreamBuffer(this, (int) state.getUploadBlockSize());
		skinUploadBuffer = new RemoteControlledFileStreamBuffer(this, (int) state.getUploadBlockSize());
		skinDownloadBuffer = new RemoteControlledFileStreamBuffer(this, (int) state.getUploadBlockSize());

		factionCreateBuffer = new RemoteFactionBuffer(this, state);
		cockpit = new RemoteCockpit(player.getCockpit(), this);
	}

	/**
	 * @return the inventoriesChangeBuffer
	 */
	@Override
	public RemoteInventoryBuffer getInventoriesChangeBuffer() {
		return inventoryBuffer;
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

	/**
	 * @return the buildSlot
	 */
	@Override
	public RemoteBytePrimitive getBuildSlot() {
		return buildSlot;
	}

	@Override
	public RemoteRuleStateChangeBuffer getRuleStateChangeBuffer() {
		return ruleChangeBuffer;
	}

	@Override
	public RemoteIntBuffer getRuleStateRequestBuffer() {
		return ruleStateRequestBuffer;
	}

	@Override
	public RemoteBuffer<RemoteString> getRuleIndividualAddRemoveBuffer() {
		return ruleIndividualAddRemoveBuffer;
	}

}
