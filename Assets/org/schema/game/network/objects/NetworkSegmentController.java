package org.schema.game.network.objects;

import javax.vecmath.Vector4f;

import org.schema.game.common.controller.SegmentController.PullPermission;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.config.EffectConfigNetworkObjectInterface;
import org.schema.game.network.objects.remote.RemoteBlockCount;
import org.schema.game.network.objects.remote.RemoteBlockEffectUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteControlModBuffer;
import org.schema.game.network.objects.remote.RemoteInterconnectStructureBuffer;
import org.schema.game.network.objects.remote.RemoteRailMoveRequestBuffer;
import org.schema.game.network.objects.remote.RemoteRailRequestBuffer;
import org.schema.game.network.objects.remote.RemoteRuleStateChangeBuffer;
import org.schema.game.network.objects.remote.RemoteShipKeyConfigBuffer;
import org.schema.game.network.objects.remote.RemoteTextBlockBuffer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkEntity;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteBooleanPrimitive;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteByteBuffer;
import org.schema.schine.network.objects.remote.RemoteBytePrimitive;
import org.schema.schine.network.objects.remote.RemoteFloatBuffer;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteVector3f;
import org.schema.schine.network.objects.remote.RemoteVector3s;
import org.schema.schine.network.objects.remote.RemoteVector4f;
import org.schema.schine.network.objects.remote.RemoteVector4i;

import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;

public class NetworkSegmentController extends NetworkEntity implements EffectConfigNetworkObjectInterface, NTRuleInterface{

	public RemoteVector3s minSize = new RemoteVector3s(this);

	public RemoteVector3s maxSize = new RemoteVector3s(this);

	public RemoteRailMoveRequestBuffer railMoveToPos = new RemoteRailMoveRequestBuffer(this);

	public RemoteIntPrimitive lastModifiedClientId = new RemoteIntPrimitive(0, this);

	public RemoteString uniqueIdentifier = new RemoteString(this);

	public RemoteString realName = new RemoteString(this);

	public RemoteBlockCount relevantBlockCounts = new RemoteBlockCount(new Short2IntOpenHashMap(), this);

	public RemoteLongPrimitive coreDestructionStarted = new RemoteLongPrimitive(-1l, this);
	public RemoteLongPrimitive coreDestructionDuration = new RemoteLongPrimitive(-1l, this);


	public RemoteBooleanPrimitive useHpLong = new RemoteBooleanPrimitive(false, this);
//	public RemoteBooleanPrimitive useArmorLong = new RemoteBooleanPrimitive(false, this);

	public RemoteIntPrimitive hpInt = new RemoteIntPrimitive(0, this);
	public RemoteIntPrimitive hpMaxInt = new RemoteIntPrimitive(0, this);


	public RemoteLongPrimitive hpLong = new RemoteLongPrimitive(0, this);
	public RemoteLongPrimitive hpMaxLong = new RemoteLongPrimitive(0, this);
	
//	public RemoteLongPrimitive armorHpLong = new RemoteLongPrimitive(0, this);
//	public RemoteLongPrimitive armorHpMaxLong = new RemoteLongPrimitive(0, this);
//	public RemoteIntPrimitive armorHpInt = new RemoteIntPrimitive(0, this);
//	public RemoteIntPrimitive armorHpMaxInt = new RemoteIntPrimitive(0, this);

	public RemoteBooleanPrimitive rebootRecover = new RemoteBooleanPrimitive(false, this);
	public RemoteLongPrimitive rebootStartTime = new RemoteLongPrimitive(0, this);
	public RemoteLongPrimitive rebootDuration = new RemoteLongPrimitive(0, this);

	public RemoteLongBuffer initialPower = new RemoteLongBuffer(this);
	public RemoteLongBuffer initialBatteryPower = new RemoteLongBuffer(this);

	public RemoteLongBuffer initialShields = new RemoteLongBuffer(this);

	public RemoteControlModBuffer controlledByBuffer = new RemoteControlModBuffer(this);

	public RemoteIntPrimitive expectedNonEmptySegmentsFromLoad = new RemoteIntPrimitive(0, this);

	public RemoteString dockedTo = new RemoteString("NONE", this);

	public RemoteBuffer<RemoteBoolean> dockClientUndockRequests = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);

	public RemoteVector3f dockingSize = new RemoteVector3f(this);

	public RemoteVector4i dockedElement = new RemoteVector4i(this);

	public RemoteBlockEffectUpdateBuffer effectUpdateBuffer = new RemoteBlockEffectUpdateBuffer(this);

	public RemoteVector4f dockingOrientation = new RemoteVector4f(this);

	public RemoteVector4f dockingTrans = new RemoteVector4f(new Vector4f(0, 0, 0, 1), this);

	public RemoteBuffer<RemoteVector4f> railTurretTransPrimary = new RemoteBuffer<RemoteVector4f>(RemoteVector4f.class, this);
	public RemoteBuffer<RemoteVector4f> railTurretTransSecondary = new RemoteBuffer<RemoteVector4f>(RemoteVector4f.class, this);

	public RemoteTextBlockBuffer textBlockChangeBuffer = new RemoteTextBlockBuffer(this);

	public RemoteBytePrimitive creatorId = new RemoteBytePrimitive((byte) 0, this);
	public RemoteBytePrimitive classification = new RemoteBytePrimitive((byte) 0, this);

	
	public RemoteBooleanPrimitive scrap = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive vulnerable = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive minable = new RemoteBooleanPrimitive(this);

	public RemoteBooleanPrimitive virtualBlueprint = new RemoteBooleanPrimitive(this);

	public RemoteBytePrimitive factionRigths = new RemoteBytePrimitive((byte) -1, this);

	public RemoteRailRequestBuffer railRequestBuffer = new RemoteRailRequestBuffer(this);
	//	public RemoteControlStructure zcontrolStructureBuffer ;

	public RemoteInterconnectStructureBuffer structureInterconnectRequestBuffer = new RemoteInterconnectStructureBuffer(this);

	public RemoteShipKeyConfigBuffer slotKeyBuffer = new RemoteShipKeyConfigBuffer(this);

	public RemoteBuffer<RemoteVector4f> shieldHits = new RemoteBuffer<RemoteVector4f>(RemoteVector4f.class, this);
	public RemoteBuffer<RemoteVector4f> hits = new RemoteBuffer<RemoteVector4f>(RemoteVector4f.class, this);

	public RemoteBooleanPrimitive additionalBlueprintData = new RemoteBooleanPrimitive(this);
	
	public RemoteLongBuffer clientToServerCheckEmptyConnection = new RemoteLongBuffer(this);

	public RemoteString currentOwner = new RemoteString(this); 
	public RemoteBuffer<RemoteString> currentOwnerChangeRequest = new RemoteBuffer<RemoteString>(RemoteString.class, this); 
	public RemoteString lastDockerPlayerServerLowerCase = new RemoteString(this); 

	public RemoteLongPrimitive dbId = new RemoteLongPrimitive(-1, this);

	public RemoteLongPrimitive lastAllowed  = new RemoteLongPrimitive(0, this);
	public RemoteBytePrimitive pullPermission  = new RemoteBytePrimitive((byte)PullPermission.ASK.ordinal(), this);
	public RemoteLongBuffer pullPermissionAskAnswerBuffer = new RemoteLongBuffer(this);
	public RemoteByteBuffer pullPermissionChangeBuffer = new RemoteByteBuffer(this);
	
	public RemoteShortBuffer effectAddBuffer = new RemoteShortBuffer(this, 128);
	public RemoteShortBuffer effectRemoveBuffer = new RemoteShortBuffer(this, 128);

	public RemoteLongBuffer convertRequestBuffer = new RemoteLongBuffer(this);
	public RemoteLongBuffer bootRequestBuffer = new RemoteLongBuffer(this);
	public RemoteLongPrimitive activeReactor = new RemoteLongPrimitive(0L, this);
	public RemoteFloatBuffer blockDelayTimers = new RemoteFloatBuffer(this);
	public RemoteBuffer<RemoteString> ruleIndividualAddRemoveBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	
	public RemoteRuleStateChangeBuffer ruleChangeBuffer = new RemoteRuleStateChangeBuffer(this);

	public RemoteIntBuffer ruleStateRequestBuffer = new RemoteIntBuffer(this);
	
	
	private SendableSegmentController c;
	public NetworkSegmentController(StateInterface state, SendableSegmentController n) {
		super(state);
		this.c = n;
	}

	@Override
	public void onDelete(StateInterface state) {
	}

	@Override
	public void onInit(StateInterface state) {
	}

	@Override
	public RemoteShortBuffer getEffectAddBuffer() {
		return effectAddBuffer;
	}
	@Override
	public RemoteShortBuffer getEffectRemoveBuffer() {
		return effectRemoveBuffer;
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
