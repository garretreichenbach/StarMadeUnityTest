package org.schema.game.network.objects;

import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.network.objects.remote.RemoteChatChannelBuffer;
import org.schema.game.network.objects.remote.RemoteChatMessageBuffer;
import org.schema.game.network.objects.remote.RemoteControlledFileStreamBuffer;
import org.schema.game.network.objects.remote.RemoteCreateDockBuffer;
import org.schema.game.network.objects.remote.RemoteEffectConfigGroupBuffer;
import org.schema.game.network.objects.remote.RemoteFTLConnectionUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteFactionNewsPostBuffer;
import org.schema.game.network.objects.remote.RemoteFowRequestAndAwnserBuffer;
import org.schema.game.network.objects.remote.RemoteGalaxyRequestBuffer;
import org.schema.game.network.objects.remote.RemoteGalaxyZoneRequestBuffer;
import org.schema.game.network.objects.remote.RemoteManualTradeBuffer;
import org.schema.game.network.objects.remote.RemoteManualTradeItemBuffer;
import org.schema.game.network.objects.remote.RemoteMapEntryAnswerBuffer;
import org.schema.game.network.objects.remote.RemoteMapEntryRequestBuffer;
import org.schema.game.network.objects.remote.RemoteMetaObjectBuffer;
import org.schema.game.network.objects.remote.RemoteMetaObjectRequestAwnserBuffer;
import org.schema.game.network.objects.remote.RemoteMetaObjectRequestModifyBuffer;
import org.schema.game.network.objects.remote.RemoteMineUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteMissileUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteNPCDiplomacyBuffer;
import org.schema.game.network.objects.remote.RemoteParticleEntryBuffer;
import org.schema.game.network.objects.remote.RemotePlayerKeyConfigBuffer;
import org.schema.game.network.objects.remote.RemotePlayerMessageBuffer;
import org.schema.game.network.objects.remote.RemoteSavedCoordinatesBuffer;
import org.schema.game.network.objects.remote.RemoteScanDataBuffer;
import org.schema.game.network.objects.remote.RemoteStringLongPairBuffer;
import org.schema.game.network.objects.remote.RemoteTradeNodeUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteTradeOrderBuffer;
import org.schema.game.network.objects.remote.RemoteTradePriceBuffer;
import org.schema.game.network.objects.remote.RemoteTradeTypeRequestBuffer;
import org.schema.game.server.data.FactionState;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.objects.remote.RemoteInteger;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteShort;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;

public class NetworkClientChannel extends NetworkObject {

	public RemoteBoolean connectionReady = new RemoteBoolean(false, this);

	public RemoteMissileUpdateBuffer missileUpdateBuffer = new RemoteMissileUpdateBuffer(this);
	public RemoteBuffer<RemoteShort> missileMissingRequestBuffer = new RemoteBuffer<RemoteShort>(RemoteShort.class, this);

	public RemoteLongBuffer factionNewsRequests = new RemoteLongBuffer(this);

	public RemoteSavedCoordinatesBuffer savedCoordinates = new RemoteSavedCoordinatesBuffer(this);

	public RemotePlayerKeyConfigBuffer controllerKeyAwnserBuffer = new RemotePlayerKeyConfigBuffer(this);
	public RemoteCreateDockBuffer createDockBuffer = new RemoteCreateDockBuffer(this);
	public RemoteBuffer<RemoteString> controllerKeyRequestBuffer = new RemoteBuffer<RemoteString>(RemoteString.class, this);

	public RemoteBuffer<RemoteString> fileRequests = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteIntBuffer playerMessageRequests = new RemoteIntBuffer(this);

	public RemotePlayerMessageBuffer playerMessageBuffer = new RemotePlayerMessageBuffer(this);

	public RemoteBuffer<RemoteString> blockBehaviorUploads = new RemoteBuffer<RemoteString>(RemoteString.class, this);

	public RemoteBuffer<RemoteString> timeStampRequests = new RemoteBuffer<RemoteString>(RemoteString.class, this);

	public RemoteStringLongPairBuffer timeStampResponses = new RemoteStringLongPairBuffer(this);

	
	public RemoteFowRequestAndAwnserBuffer fogOfWarRequestsAndAwnsers = new RemoteFowRequestAndAwnserBuffer(this);

	public RemoteScanDataBuffer scanDataUpdates = new RemoteScanDataBuffer(this);

	public RemoteFTLConnectionUpdateBuffer ftlUpdatesAndRequests = new RemoteFTLConnectionUpdateBuffer(this);
	public RemoteTradeNodeUpdateBuffer tradeNodeUpdatesAndRequests;

	public RemoteMetaObjectBuffer metaObjectBuffer;
	public RemoteMetaObjectBuffer metaObjectModifyRequestBuffer;

	public RemoteInteger playerId = new RemoteInteger(-121212, this);

	public RemoteFactionNewsPostBuffer factionNewsPosts = new RemoteFactionNewsPostBuffer(this);

	public RemoteMapEntryRequestBuffer mapRequests = new RemoteMapEntryRequestBuffer(this);
	public RemoteMapEntryAnswerBuffer mapAnswers = new RemoteMapEntryAnswerBuffer(this);

	public RemoteIntBuffer metaObjectRequests = new RemoteIntBuffer(this);

	public RemoteGalaxyRequestBuffer galaxyRequests = new RemoteGalaxyRequestBuffer(this);
	public RemoteGalaxyZoneRequestBuffer galaxyZoneRequests = new RemoteGalaxyZoneRequestBuffer(this);

	public RemoteGalaxyRequestBuffer galaxyServerMods = new RemoteGalaxyRequestBuffer(this);

	public RemoteChatMessageBuffer chatBuffer = new RemoteChatMessageBuffer(this);
	public RemoteChatChannelBuffer chatChannelBuffer = new RemoteChatChannelBuffer(this);

	public RemoteControlledFileStreamBuffer downloadBuffer;

	public RemoteParticleEntryBuffer particles = new RemoteParticleEntryBuffer(this);

	public RemoteLongBuffer requestPricesOfShop = new RemoteLongBuffer(this);
	
	public RemoteTradePriceBuffer pricesOfShopAwnser = new RemoteTradePriceBuffer(this);
	public RemoteTradeTypeRequestBuffer tradeTypeBuffer = new RemoteTradeTypeRequestBuffer(this);
	public RemoteShortBuffer tradeTypeRequestBuffer = new RemoteShortBuffer(this, 5);
	public RemoteTradeOrderBuffer tradeOrderRequests;

	public RemoteManualTradeItemBuffer manualTradeItemBuffer = new RemoteManualTradeItemBuffer(this);
	public RemoteManualTradeBuffer manualTradeBuffer = new RemoteManualTradeBuffer(this);
	public RemoteNPCDiplomacyBuffer npcDiplomacyBuffer;

	public RemoteBuffer<RemoteString> effectConfigSig = new RemoteBuffer<RemoteString>(RemoteString.class, this);
	public RemoteEffectConfigGroupBuffer effectConfigGroupBuffer = new RemoteEffectConfigGroupBuffer(this);
	public RemoteMineUpdateBuffer mineUpdateBuffer = new RemoteMineUpdateBuffer(this);
	
	public NetworkClientChannel(StateInterface state, MetaObjectManager man) {
		super(state);
		tradeNodeUpdatesAndRequests = new RemoteTradeNodeUpdateBuffer(this, state);
		tradeOrderRequests = new RemoteTradeOrderBuffer(this, state);
		metaObjectBuffer = new RemoteMetaObjectRequestAwnserBuffer(man, this);
		metaObjectModifyRequestBuffer = new RemoteMetaObjectRequestModifyBuffer(man, this);
		downloadBuffer = new RemoteControlledFileStreamBuffer(this, (int) state.getUploadBlockSize());
		npcDiplomacyBuffer = new RemoteNPCDiplomacyBuffer((FactionState)state, this);
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


}
