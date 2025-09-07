package org.schema.game.network.objects;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.SendableSegmentProvider;
import org.schema.game.network.objects.remote.RemoteBitsetBuffer;
import org.schema.game.network.objects.remote.RemoteBlockBulkBuffer;
import org.schema.game.network.objects.remote.RemoteCompressedCoordBuffer;
import org.schema.game.network.objects.remote.RemoteControlStructureBuffer;
import org.schema.game.network.objects.remote.RemoteInventoryBuffer;
import org.schema.game.network.objects.remote.RemoteInventoryFilterBuffer;
import org.schema.game.network.objects.remote.RemoteManualMouseEventBuffer;
import org.schema.game.network.objects.remote.RemoteSegmentPieceBuffer;
import org.schema.game.network.objects.remote.RemoteSegmentRemoteObjBuffer;
import org.schema.game.network.objects.remote.RemoteServerMessageBuffer;
import org.schema.game.network.objects.remote.RemoteTextBlockBuffer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteAudioEvent;
import org.schema.schine.network.objects.remote.RemoteAudioEventBuffer;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteVector3f;
import org.schema.schine.sound.controller.AudioArgument;
import org.schema.schine.sound.controller.AudioNetworkObject;
import org.schema.schine.sound.controller.RemoteAudioEntry;

public class NetworkSegmentProvider extends NetworkEntityProvider implements AudioNetworkObject {

	public static final int BATCH_SIZE = 512;
	//	public static final int SIGNATURE_BATCH_SIZE = 64;
	public RemoteLongBuffer segmentBufferRequestBuffer = new RemoteLongBuffer(this, BATCH_SIZE);
	public RemoteBitsetBuffer segmentBufferAwnserBuffer = new RemoteBitsetBuffer(this);

	//bytes of one complete fireing request cycle 32 (16 for client) (16 for server) with 64 batch = 1kb @30 updates/sec = 30kb/sec

	public RemoteLongBuffer segmentClientToServerCombinedRequestBuffer = new RemoteLongBuffer(this, BATCH_SIZE);

	public RemoteLongBuffer signatureEmptyBuffer = new RemoteLongBuffer(this, BATCH_SIZE);

	public RemoteLongBuffer signatureOkBuffer = new RemoteLongBuffer(this, BATCH_SIZE);

	public RemoteSegmentRemoteObjBuffer segmentBuffer;

	public RemoteBoolean requestedInitialControlMap = new RemoteBoolean(this);

	public RemoteLongBuffer blockActivationBuffer = new RemoteLongBuffer(this);

	public RemoteSegmentPieceBuffer modificationBuffer = new RemoteSegmentPieceBuffer(this, ((GameStateInterface) this.getState()).getSegmentPieceQueueSize());

	public RemoteBlockBulkBuffer modificationBulkBuffer = new RemoteBlockBulkBuffer(this);

	public RemoteControlStructureBuffer initialControlMap;

	public RemoteInventoryBuffer invetoryBuffer;

	public RemoteTextBlockBuffer textBlockResponsesAndChangeRequests = new RemoteTextBlockBuffer(this);
	;
	public RemoteLongBuffer textBlockRequests = new RemoteLongBuffer(this);

	public RemoteLongBuffer textBlockChangeInLongRange = new RemoteLongBuffer(this); 
	
	public RemoteBoolean signalDelete = new RemoteBoolean(false, this);
	
	public RemoteShortBuffer killBuffer = new RemoteShortBuffer(this, ((GameStateInterface) this.getState()).getSegmentPieceQueueSize()*4);
	
	public RemoteLongBuffer beamLatchBuffer = new RemoteLongBuffer(this, 60); //buffer must be divisible by 3 as they come in batches of 3 
	
	
	public RemoteShortBuffer activeChangedTrueBuffer = new RemoteShortBuffer(this, ((GameStateInterface) this.getState()).getSegmentPieceQueueSize()*3);
	public RemoteShortBuffer activeChangedFalseBuffer = new RemoteShortBuffer(this, ((GameStateInterface) this.getState()).getSegmentPieceQueueSize()*3);
	
	public RemoteCompressedCoordBuffer salvageBuffer = new RemoteCompressedCoordBuffer(this, ((GameStateInterface) this.getState()).getSegmentPieceQueueSize()*3);
	
	public RemoteServerMessageBuffer messagesToBlocks = new RemoteServerMessageBuffer(this);
	public RemoteBuffer<RemoteVector3f> explosions = new RemoteBuffer<RemoteVector3f>(RemoteVector3f.class, this);
	public RemoteLongBuffer inventoryDetailRequests = new RemoteLongBuffer(this);
	public RemoteInventoryFilterBuffer inventoryDetailAnswers = new RemoteInventoryFilterBuffer(this);
	public RemoteManualMouseEventBuffer manualMouseEventBuffer = new RemoteManualMouseEventBuffer(this);
	public RemoteAudioEventBuffer audioBuffer = new RemoteAudioEventBuffer(this);
	
	public NetworkSegmentProvider(StateInterface state, SendableSegmentProvider sendableSegmentProvider) {
		super(state);
		initialControlMap = new RemoteControlStructureBuffer(sendableSegmentProvider, this);
		segmentBuffer = new RemoteSegmentRemoteObjBuffer(this, sendableSegmentProvider.getSegmentController());
		explosions.MAX_BATCH = 16;
	}

	@Override
	public void onDelete(StateInterface state) {
	}

	@Override
	public void onInit(StateInterface state) {
		//		assert(state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(playerId.get())): "player "+playerId+" not in local pbjects for "+state;
		//		PlayerState player = (PlayerState)state.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerId.get());
		//		player.getOwnerObjectsIds().add(this.id.get());
	}

	public void setBatchSize(int size) {
		segmentClientToServerCombinedRequestBuffer.MAX_BATCH = size;
		segmentBufferAwnserBuffer.MAX_BATCH = size;
		signatureEmptyBuffer.MAX_BATCH = size;
		signatureOkBuffer.MAX_BATCH = size;
	}

	@Override
	public void sendAudioEvent(int id, int networkId, AudioArgument args) {
		RemoteAudioEntry a = new RemoteAudioEntry();
		a.audioId = id;
		a.targetId = networkId;
		a.audioArgument = args;
		audioBuffer.add(new RemoteAudioEvent(a, this));		
	}

}
