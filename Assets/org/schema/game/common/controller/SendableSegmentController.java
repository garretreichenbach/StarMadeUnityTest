package org.schema.game.common.controller;

import api.element.block.Blocks;
import api.listener.events.block.SegmentPieceKillEvent;
import api.listener.events.block.SendableSegmentControllerFireActivationEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.segmentpiece.SegmentPieceKilledListener;
import api.mod.StarLoader;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.LogUtil;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.effects.ShieldDrawer;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.common.Starter;
import org.schema.game.common.controller.ai.SegmentControllerAIInterface;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.*;
import org.schema.game.common.controller.elements.activation.ActivationCollectionManager;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.sensor.SensorCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterCollectionManager;
import org.schema.game.common.controller.elements.transporter.TransporterElementManager;
import org.schema.game.common.controller.elements.transporter.TransporterUnit;
import org.schema.game.common.controller.elements.warpgate.WarpgateCollectionManager;
import org.schema.game.common.controller.io.IOFileManager;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.*;
import org.schema.game.common.data.blockeffects.BlockEffectManager;
import org.schema.game.common.data.blockeffects.config.ConfigProviderSource;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.element.meta.weapon.MarkerBeam;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.player.*;
import org.schema.game.common.data.world.*;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.network.objects.InterconnectStructureRequest;
import org.schema.game.network.objects.NetworkSegmentController;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteVector3f;
import org.schema.schine.network.objects.remote.RemoteVector4f;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;
import org.schema.schine.sound.controller.AudioArgument;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioSender;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class SendableSegmentController extends SegmentController implements Sendable, AudioSender {

	private static final List<BlockActiveReaction> blockActPool = new ObjectArrayList<BlockActiveReaction>();
	private static Long2ObjectOpenHashMap<BlockActiveReaction> blockActivations = new Long2ObjectOpenHashMap<BlockActiveReaction>();
	private static ThreadLocal<SignalTracePool> signalThread = new ThreadLocal<SignalTracePool>() {

		@Override
		protected SignalTracePool initialValue() {
			return new SignalTracePool();
		}
	};
	public final ObjectArrayFIFOQueue<TextBlockPair> receivedTextBlocks = new ObjectArrayFIFOQueue<TextBlockPair>();
	public final ObjectArrayFIFOQueue<InterconnectStructureRequest> receivedInterconnectedStructure = new ObjectArrayFIFOQueue<InterconnectStructureRequest>();
	public final ObjectArrayFIFOQueue<TextBlockPair> receivedTextBlockRequests = new ObjectArrayFIFOQueue<TextBlockPair>();
	public final LongArrayFIFOQueue textBlockChangeInLongRange = new LongArrayFIFOQueue();
	public final ObjectArrayFIFOQueue<ServerMessage> receivedBlockMessages = new ObjectArrayFIFOQueue<ServerMessage>();
	private final LongArrayFIFOQueue blockActivationBuffer = new LongArrayFIFOQueue();
	private final BlockEffectManager blockEffectManager;
	private final BlockProcessor blockProcessor;
	private final ObjectArrayFIFOQueue<Vector4f> remoteHits = new ObjectArrayFIFOQueue<Vector4f>();
	private final ObjectArrayFIFOQueue<Vector4f> remoteShieldHits = new ObjectArrayFIFOQueue<Vector4f>();
	private final SegmentPiece tmpPiece = new SegmentPiece();
	private final Long2ObjectOpenHashMap<DelayedAct> delayActivationBuffer = new Long2ObjectOpenHashMap<DelayedAct>();
	private final Long2ObjectOpenHashMap<DelayedAct> delayActivationBufferNonRepeating = new Long2ObjectOpenHashMap<DelayedAct>();
	private final Long2LongOpenHashMap cooldownBlocks = new Long2LongOpenHashMap();
	private final ObjectArrayList<SendableSegmentProvider> listeners = new ObjectArrayList<SendableSegmentProvider>();
	private final DisplayReplace displayReplace = new DisplayReplace();
	private final ObjectArrayFIFOQueue<SendableSegmentProvider> flagRemoveCachedTextBoxes = new ObjectArrayFIFOQueue<SendableSegmentProvider>();
	public SignalQueue signalQueue = new SignalQueue();
	public SignalTrace currentTrace = null;
	public int signalId;
	private NetworkSegmentController networkEntity;
	private int lastModifierId;
	private boolean lastModifierChanged;
	private SendableSegmentProvider serverSendableSegmentProvider;
	private Vector3i tmpPos = new Vector3i();
	private SegmentRetrieveCallback tmpRtrv = new SegmentRetrieveCallback();
	private int failedActivating;
	private long warnActivations;
	private long logicCooldown;
	private RemoteSector currentConfigSectorProjection;
	private String ownerChangeRequested;
	private int ownerChangeRequestedClientId;
	private long lastSignalStopMsgSent;

	// private final Set<SegmentPiece> segmentModificationsToSend = new HashSet<SegmentPiece>();
	public SendableSegmentController(StateInterface state) {
		super(state);
		this.getControlElementMap().setSendableSegmentController(this);
		blockEffectManager = new BlockEffectManager(this);
		this.blockProcessor = new BlockProcessor(this);
	}

	public static Tag listToTagStruct(Long2ObjectOpenHashMap<DelayedAct> m, String name) {
		Tag[] members = new Tag[m.size() + 1];
		members[m.size()] = FinishTag.INST;
		int i = 0;
		for(it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<DelayedAct> a : m.long2ObjectEntrySet()) {
			Tag[] t = new Tag[3];
			t[0] = new Tag(Type.LONG, null, a.getValue().encode);
			t[1] = new Tag(Type.LONG, null, a.getValue().time);
			t[2] = FinishTag.INST;
			members[i] = (new Tag(Type.STRUCT, null, t));
			i++;
		}
		Tag root = new Tag(Type.STRUCT, name, members);
		return root;
	}

	private static void freeActReaction(BlockActiveReaction o) {
		o.clear();
		blockActPool.add(o);
	}

	private static BlockActiveReaction getActReaction() {
		return blockActPool.size() > 0 ? blockActPool.remove(blockActPool.size() - 1) : new BlockActiveReaction();
	}

	@Override
	public SendableSegmentProvider createNetworkListenEntity() {
		SendableSegmentProvider sendableSegmentProvider = new SendableSegmentProvider(getState());
		sendableSegmentProvider.initialize();
		sendableSegmentProvider.setProvidedObject(this);
		((ClientSegmentProvider) getSegmentProvider()).setSendableSegmentProvider(sendableSegmentProvider);
		return sendableSegmentProvider;
	}

	public boolean isControlHandled(ControllerStateInterface unit) {
		// must be in same sector and must be either person in the right controller mode or remote controlled (e.g. AI)
		return unit.isUnitInPlayerSector() && (!(unit instanceof ControllerStateUnit) || ((ControllerStateUnit) unit).playerState.getNetworkObject().activeControllerMask.get(AbstractControlManager.CONTROLLER_SHIP_EXTERN).get());
	}

	public void backUpAllRawFiles() {
		File path = new FileExt(GameServerState.SEGMENT_DATA_DATABASE_PATH);
		File[] listFiles = path.listFiles(pathname -> pathname.getName().startsWith(getUniqueIdentifier()));
		long time = System.currentTimeMillis();
		for(int i = 0; i < listFiles.length; i++) {
			File pathBk = new FileExt("debugBackupRAW");
			if(!pathBk.exists()) {
				pathBk.mkdir();
			}
			File dest = new FileExt("debugBackupRAW/" + time + "###" + listFiles[i].getName());
			try {
				FileUtil.copyFile(listFiles[i], dest);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void onBlockSinglePlacedOnServer() {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#destroyPersistent()
	 */
	@Override
	public void destroyPersistent() {
		Starter.modManager.onSegmentControllerDestroyedPermanently(this);
		super.destroyPersistent();
		assert (isOnServer());
		// ((GameServerState)getState()).getGameState().getNetworkObject().clearSegmentCacheCommands.add(new RemoteString(getUniqueIdentifier(), true));
	}

	@Override
	protected void fireActivation(ActivationTrigger act) throws IOException {
		//INSERTED CODE
		SendableSegmentControllerFireActivationEvent event = new SendableSegmentControllerFireActivationEvent(this, act);
		StarLoader.fireEvent(event, isOnServer());
		///

		if(act.getType() == ElementKeyMap.SIGNAL_TRIGGER_AREA) {
			// the pos is the controller block in this case
			for(int i = 0; i < 6; i++) {
				Vector3i neightbor = ElementCollection.getPosFromIndex(act.pos, new Vector3i());
				neightbor.add(Element.DIRECTIONSi[i]);
				SegmentPiece segmentPiece = getSegmentBuffer().getPointUnsave(neightbor);
				if(segmentPiece != null && segmentPiece.isValid() && segmentPiece.getInfo().isSignal()) {
					PositionControl gravityElements = getControlElementMap().getControlledElements(ElementKeyMap.GRAVITY_ID, neightbor);
					PositionControl gravityExitElements = getControlElementMap().getControlledElements(ElementKeyMap.GRAVITY_EXIT_ID, neightbor);
					if(!gravityElements.getControlMap().isEmpty() || !gravityExitElements.getControlMap().isEmpty()) {
						if(act.obj instanceof PairCachingGhostObjectAlignable) {
							PairCachingGhostObjectAlignable gs = (PairCachingGhostObjectAlignable) act.obj;
							if(gs.getObj() instanceof PlayerCharacter) {
								PlayerCharacter c = ((PlayerCharacter) gs.getObj());
								long grav = gravityElements.getControlPosMap().iterator().nextLong();
								SegmentPiece pointUnsave = segmentPiece.getSegment().getSegmentController().getSegmentBuffer().getPointUnsave(grav);
								if(pointUnsave != null) {
									c.activateGravity(pointUnsave);
								}
							}
						}
					}
					sendBlockActivation(ElementCollection.getEncodeActivation(segmentPiece, true, !segmentPiece.isActive(), false));
				}
			}
		} else if(act.getType() == ElementKeyMap.SIGNAL_TRIGGER_STEPON) {
			// the pos is the step on block
			SegmentPiece segmentPiece = getSegmentBuffer().getPointUnsave(act.pos);
			if(segmentPiece != null) {
				sendBlockActivation(ElementCollection.getEncodeActivation(segmentPiece, true, !segmentPiece.isActive(), false));
			}
		} else {
			System.err.println("[TRIGGER] Error: Unknown type: " + ElementKeyMap.toString(act.getType()));
		}
	}

	@Override
	public int writeAllBufferedSegmentsToDatabase(boolean includeDocked, boolean forced, boolean forceWriteUnchanged) throws IOException {
		if(this instanceof TransientSegmentController && !((TransientSegmentController) this).isTouched()) {
			if(this instanceof Planet || this instanceof PlanetIco || this instanceof SpaceStation) {
				System.err.println("[SENDABLESEGMENTVONTROLLER][WRITE] " + getState() + " skipping writing transient object " + this);
			}
			return 0;
		}
		int writtenSegments = 0;
		if(includeDocked) {
			for(ElementDocking e : getDockingController().getDockedOnThis()) {
				if(e.from.getSegment().getSegmentController() != this) {
					writtenSegments += e.from.getSegment().getSegmentController().writeAllBufferedSegmentsToDatabase(includeDocked, forced, forceWriteUnchanged);
				}
			}
			for(RailRelation e : railController.next) {
				if(e.docked.getSegmentController() != this) {
					writtenSegments += e.docked.getSegmentController().writeAllBufferedSegmentsToDatabase(includeDocked, forced, forceWriteUnchanged);
				}
			}
		}
		if(isOnServer() && ServerConfig.DEBUG_SEGMENT_WRITING.isOn()) {
			backUpAllRawFiles();
		} else {
		}
		long t = System.currentTimeMillis();
		long tHeader = 0;
		// this MUST be synchronized in order to be ok
		synchronized(getSegmentBuffer()) {
			// write to database if necessary (if server version is newer)
			Writer writer = new Writer(false);
			writer.forcedTimestamp = forceWriteUnchanged;
			if(forced) {
				getSegmentBuffer().iterateOverEveryElement(writer, true);
			} else {
				if(getSegmentBuffer().getLastBufferChanged() > getSegmentBuffer().getLastBufferSaved()) {
					System.err.println("[SENDABLESEGMENTVONTROLLER][WRITE] " + getState() + " WRITING BLOCK DATA " + this + " since it HAS changed: lastChanged: " + getSegmentBuffer().getLastBufferChanged() + " / last written " + getSegmentBuffer().getLastBufferSaved() + "; NonEmpty: " + getSegmentBuffer().getTotalNonEmptySize());
					getSegmentBuffer().iterateOverEveryChangedElement(writer, true);
				} else {
					// System.err.println("[SENDABLESEGMENTVONTROLLER][WRITE] " + getState() + " skipping block data writing of " + this + " since it hasn't changed: lastChanged: " + getSegmentBuffer().getLastBufferChanged() + " / last written " + getSegmentBuffer().getLastBufferSaved());
				}
			}
			getSegmentBuffer().setLastBufferSaved(System.currentTimeMillis());
			writtenSegments += writer.writtenSegments;
			try {
				if(writtenSegments > 0) {
					long tH = System.currentTimeMillis();
					IOFileManager.writeAllHeaders(getSegmentProvider().getSegmentDataIO().getManager());
					tHeader = System.currentTimeMillis() - tH;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		if(isOnServer() && ServerConfig.FORCE_DISK_WRITE_COMPLETION.isOn()) {
			forceAllRawFiles();
		}
		long took = System.currentTimeMillis() - t;
		if(took > 10) {
			System.err.println("[SENDABLESEGMENTVONTROLLER][WRITE] WARNING: segment writing of " + this + " on " + this.getState() + " took: " + took + " ms (file header: " + tHeader + "ms)");
		}
		return writtenSegments;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentController#cleanUpOnEntityDelete()
	 */
	@Override
	public void cleanUpOnEntityDelete() {
		Starter.modManager.onSegmentControllerDelete(this);
		if(!isOnServer()) {
			SendableSegmentProvider sendableSegmentProvider = ((ClientSegmentProvider) getSegmentProvider()).getSendableSegmentProvider();
			if(sendableSegmentProvider != null && sendableSegmentProvider.getNetworkObject() != null) {
				sendableSegmentProvider.getNetworkObject().signalDelete.set(true, true);
			}
		} else {
			if(serverSendableSegmentProvider != null) {
				serverSendableSegmentProvider.markForPermanentDelete(true);
				serverSendableSegmentProvider = null;
			}
		}
		super.cleanUpOnEntityDelete();
	}

	@Override
	public NetworkSegmentController getNetworkObject() {
		return networkEntity;
	}

	public void setNetworkObject(NetworkSegmentController networkEntity) {
		this.networkEntity = networkEntity;
	}

	@Override
	public void updateLocal(Timer timer) {
		getState().getDebugTimer().start(this, "SendableSegmentController");
		super.updateLocal(timer);
		getState().getDebugTimer().start(this, "SendableSegmentController", "ConfigManager");
		getConfigManager().updateLocal(timer, this);
		getState().getDebugTimer().end(this, "SendableSegmentController", "ConfigManager");
		if(currentConfigSectorProjection != getRemoteSector()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "ProjectionAddRemove");
			if(currentConfigSectorProjection != null) {
				currentConfigSectorProjection.onRemovedEntityFromSector(this);
			}
			currentConfigSectorProjection = getRemoteSector();
			if(currentConfigSectorProjection != null) {
				currentConfigSectorProjection.onAddedEntityFromSector(this);
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "ProjectionAddRemove");
		}
		if(currentConfigSectorProjection != null) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "ProjectionUpdate");
			currentConfigSectorProjection.entityUpdateInSector(this);
			getState().getDebugTimer().end(this, "SendableSegmentController", "ProjectionUpdate");
		}
		if(!flagRemoveCachedTextBoxes.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "TextBoxes");
			synchronized(flagRemoveCachedTextBoxes) {
				while(!flagRemoveCachedTextBoxes.isEmpty()) {
					SendableSegmentProvider s = flagRemoveCachedTextBoxes.dequeue();
					s.clearChangedTextBoxLongRangeIndices();
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "TextBoxes");
		}
		if(this.ownerChangeRequested != null) {
			assert (isOnServer());
			RegisteredClientOnServer cl = ((GameServerState) getState()).getClients().get(ownerChangeRequestedClientId);
			if(cl != null && cl.getPlayerObject() instanceof PlayerState) {
				PlayerState p = (PlayerState) cl.getPlayerObject();
				if(getFactionId() == 0 || (getFactionId() == p.getFactionId() && isSufficientFactionRights(p))) {
					if(ownerChangeRequested.length() == 0) {
						currentOwnerLowerCase = "";
					} else if(p.isAdmin() || ownerChangeRequested.toLowerCase(Locale.ENGLISH).equals(p.getName().toLowerCase(Locale.ENGLISH))) {
						currentOwnerLowerCase = ownerChangeRequested.toLowerCase(Locale.ENGLISH);
					}
				} else {
					p.sendServerMessagePlayerError(Lng.astr("Cannot change owner!\nInsufficient rights!"));
				}
			}
			this.ownerChangeRequested = null;
			this.ownerChangeRequestedClientId = 0;
		}
		if(!textBlockChangeInLongRange.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "TextBoxesLR");
			synchronized(textBlockChangeInLongRange) {
				while(!textBlockChangeInLongRange.isEmpty()) {
					if(!isInClientRange()) {
						/*
						 * when a text of a display block is changed on server it sends out
						 * this flag the change is only directly sent to close by entities
						 * so entites far away have to reset their text cache for this
						 * entity since clients request text boxes on demand when close by
						 */
						assert (!isOnServer());
						getTextMap().remove(textBlockChangeInLongRange.dequeueLong());
					}
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "TextBoxesLR");
		}
		if(!remoteHits.isEmpty() && !isOnServer()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "RemoteHits");
			while(!remoteHits.isEmpty()) {
				Vector4f dequeue = remoteHits.dequeue();
				Transform t = new Transform();
				t.setIdentity();
				t.origin.set(dequeue.x, dequeue.y, dequeue.z);
				int damage = (int) Math.abs(dequeue.w);
				HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, String.valueOf(damage), 1, 0, 0, 1));
				// prepare to add some explosions
				GameClientState s = (GameClientState) getState();
				/*AudioController.fireAudioEvent("HIT_HULL", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.HIT, AudioTags.HULL }, AudioParam.ONE_TIME, AudioController.ent(this, t, 0L, dequeue.w))*/
				AudioController.fireAudioEventID(940, AudioController.ent(this, t, 0L, dequeue.w));
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "RemoteHits");
		}
		if(!remoteShieldHits.isEmpty() && !isOnServer()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "RemoteShieldHits");
			while(!remoteShieldHits.isEmpty()) {
				Vector4f dequeue = remoteShieldHits.dequeue();
				Transform t = new Transform();
				t.setIdentity();
				t.origin.set(dequeue.x, dequeue.y, dequeue.z);
				HudIndicatorOverlay.toDrawTexts.add(new RaisingIndication(t, String.valueOf((int) dequeue.w), ShieldAddOn.shieldHitColor.x, ShieldAddOn.shieldHitColor.y, ShieldAddOn.shieldHitColor.z, ShieldAddOn.shieldHitColor.w));
				// prepare to add some explosions
				GameClientState s = (GameClientState) getState();
				/*AudioController.fireAudioEvent("HIT_SHIELD", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.HIT, AudioTags.SHIELD }, AudioParam.ONE_TIME, AudioController.ent(this, t, 0L, dequeue.w))*/
				AudioController.fireAudioEventID(941, AudioController.ent(this, t, 0L, dequeue.w));
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "RemoteShieldHits");
		}
		if(!cooldownBlocks.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "CooldownBlocks");
			long cur = System.currentTimeMillis();
			LongIterator iterator = cooldownBlocks.values().iterator();
			while(iterator.hasNext()) {
				long timeUsed = iterator.nextLong();
				if(cur - timeUsed > AbstractCharacter.MEDICAL_REUSE_MS) {
					iterator.remove();
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "CooldownBlocks");
		}
		if(!receivedInterconnectedStructure.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "Interconnected");
			synchronized(receivedInterconnectedStructure) {
				while(!receivedInterconnectedStructure.isEmpty()) {
					InterconnectStructureRequest s = receivedInterconnectedStructure.dequeue();
					VoidUniqueSegmentPiece selectedBlock = s.fromPiece;
					VoidUniqueSegmentPiece controlledPiece = s.toPiece;
					selectedBlock.setSegmentControllerFromUID(getState());
					controlledPiece.setSegmentControllerFromUID(getState());
					// assert(false):selectedBlock+"; "+controlledPiece;
					if(selectedBlock.getSegmentController() != null && controlledPiece.getSegmentController() != null && selectedBlock.getSegmentController() instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) selectedBlock.getSegmentController()).getManagerContainer() instanceof ActivationManagerInterface) {
						ManagerContainer<?> m = ((ManagedSegmentController<?>) selectedBlock.getSegmentController()).getManagerContainer();
						ActivationManagerInterface man = (ActivationManagerInterface) m;
						ActivationCollectionManager activationCollectionManager = man.getActivation().getCollectionManagersMap().get(selectedBlock.getAbsoluteIndex());
						MarkerBeam b = new MarkerBeam(0);
						b.marking = controlledPiece.getSegmentController().getUniqueIdentifier();
						b.markerLocation = controlledPiece.getAbsoluteIndex();
						b.realName = controlledPiece.getSegmentController().getRealName();
						Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(s.playerId);
						if(activationCollectionManager != null) {
							if(activationCollectionManager.getDestination() == null || !activationCollectionManager.getDestination().equalsBeam(b)) {
								activationCollectionManager.setDestination(b);
								if(sendable != null && sendable instanceof PlayerState) {
									((PlayerState) sendable).sendServerMessagePlayerInfo(Lng.astr("Wireless logic blocks connected."));
								}
							} else {
								activationCollectionManager.setDestination(null);
								if(sendable != null && sendable instanceof PlayerState) {
									((PlayerState) sendable).sendServerMessagePlayerInfo(Lng.astr("Wireless logic blocks \ndisconnected."));
								}
							}
						}
					}
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "Interconnected");
		}
		if(!receivedTextBlockRequests.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "TextBoxRequest");
			synchronized(receivedTextBlockRequests) {
				while(!receivedTextBlockRequests.isEmpty()) {
					// this is a request
					TextBlockPair dequeue = receivedTextBlockRequests.dequeue();
					assert (isOnServer());
					assert (dequeue.provider != null);
					dequeue.text = getTextMap().get(dequeue.block);
					if(dequeue.text == null) {
						dequeue.text = "[no data]";
					}
					dequeue.provider.getNetworkObject().textBlockResponsesAndChangeRequests.add(new RemoteTextBlockPair(dequeue, isOnServer()));
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "TextBoxRequest");
		}
		if(!receivedTextBlocks.isEmpty()) {
			getState().getDebugTimer().start(this, "SendableSegmentController", "TextBoxReceived");
			synchronized(receivedTextBlocks) {
				while(!receivedTextBlocks.isEmpty()) {
					TextBlockPair dequeue = receivedTextBlocks.dequeue();
					// System.err.println(getState() + "; received text block " + dequeue);
					// treat as a response on client, treat as a change request on server
					getTextMap().put(dequeue.block, dequeue.text);
					if(!isOnServer()) {
						synchronized(((ClientSegmentProvider) getSegmentProvider()).getSendableSegmentProvider().getRequestedTextBlocks()) {
							((ClientSegmentProvider) getSegmentProvider()).getSendableSegmentProvider().getRequestedTextBlocks().remove(dequeue.block);
						}
					} else {
						getNetworkObject().textBlockChangeBuffer.add(new RemoteTextBlockPair(dequeue, isOnServer()));
					}
				}
			}
			getState().getDebugTimer().end(this, "SendableSegmentController", "TextBoxReceived");
		}
		if(isOnServer() && lastModifierChanged && lastModifierId != 0) {
			try {
				PlayerState p = ((GameServerState) getState()).getPlayerFromStateId(lastModifierId);
				setLastModifier(p.getUniqueIdentifier());
				// System.err.println("[SERVER][SENSEGMENTCONTROLLER] LAST MODIFIER CHANGED TO "+p);
			} catch(Exception e) {
			}
			lastModifierChanged = false;
		}
		long time = System.currentTimeMillis();
		getState().getDebugTimer().start(this, "SendableSegmentController", "BlockeffectMan");
		if(isOnServer()) {
			blockEffectManager.updateServer(timer);
		} else {
			blockEffectManager.updateClient(timer);
		}
		getState().getDebugTimer().end(this, "SendableSegmentController", "BlockeffectMan");
		getState().getDebugTimer().start(this, "SendableSegmentController", "DelayedMods");
		blockProcessor.handleDelayedMods();
		getState().getDebugTimer().end(this, "SendableSegmentController", "DelayedMods");
		getState().getDebugTimer().start(this, "SendableSegmentController", "DelayedModsBulk");
		blockProcessor.handleDelayedBuklkMods();
		getState().getDebugTimer().end(this, "SendableSegmentController", "DelayedModsBulk");
		// if(!getBlockEffectManager().getActiveEffects().isEmpty()){
		// System.err.println(getState()+" "+this+" Current Effects: "+getBlockEffectManager().getActiveEffects());
		// }
		getState().getDebugTimer().start(this, "SendableSegmentController", "ActivationsAndControl");
		handleActivationsAndControlMap(timer, time);
		getState().getDebugTimer().end(this, "SendableSegmentController", "ActivationsAndControl");
		// long t0 = System.currentTimeMillis();
		// long took = System.currentTimeMillis() - t0;
		// if(took > 5){
		// System.err.println("[SENSEGMENTCONTROLLER]["+getState()+"] WARNING: superUpdate of "+this+" took "+took+" ms");
		// }
		getState().getDebugTimer().end(this, "SendableSegmentController");
	}

	@Override
	public void sendBlockActivation(long encodeActivation) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().blockActivationBuffer.add(encodeActivation);
			}
		}
	}

	@Override
	public void sendBlockMod(RemoteSegmentPiece mod) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				assert (getState().isSynched());
				listeners.get(i).getNetworkObject().modificationBuffer.add(mod);
			}
		}
	}

	public void sendBlockActiveChanged(int x, int y, int z, boolean active) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				RemoteShortBuffer a;
				if(active) {
					a = listeners.get(i).getNetworkObject().activeChangedTrueBuffer;
				} else {
					a = listeners.get(i).getNetworkObject().activeChangedFalseBuffer;
				}
				a.addCoord((short) x, (short) y, (short) z);
				int size = listeners.get(i).getNetworkObject().activeChangedTrueBuffer.size() + listeners.get(i).getNetworkObject().activeChangedFalseBuffer.size();
				// each modification adds 3 to the buffer size
				size /= 3;
				if(size > ServerConfig.MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_STOP.getInt()) {
					assert (isOnServer());
					if(System.currentTimeMillis() - this.logicCooldown > 10000) {
						((GameServerState) getState()).getController().broadcastMessage(Lng.astr("WARNING: Too much logic lag from activations:\nPossible logic bomb by\n%s; in sector: %s\nOBJECT'S LOGIC WILL FREEZE FOR 10 SECONDS TO PREVENT SERVER OVERLOADING", this.getRealName(), this.getSector(new Vector3i())), ServerMessage.MESSAGE_TYPE_ERROR);
						this.logicCooldown = System.currentTimeMillis();
					}
				} else if(size > ServerConfig.MAX_LOGIC_ACTIVATIONS_AT_ONCE_PER_OBJECT_WARN.getInt()) {
					assert (isOnServer());
					if(System.currentTimeMillis() - this.warnActivations > 5000) {
						((GameServerState) getState()).getController().broadcastMessage(Lng.astr("WARNING: Too much logic lag from activations:\nPossible logic bomb by\n%s; in sector: %s", this.getRealName(), this.getSector(new Vector3i())), ServerMessage.MESSAGE_TYPE_ERROR);
						this.warnActivations = System.currentTimeMillis();
					}
				}
			}
		}
	}

	@Override
	public void sendBlockHpByte(int x, int y, int z, short hp) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().killBuffer.addCoord((short) x, (short) y, (short) z, hp);
			}
		}
	}

	@Override
	public void sendBeamLatchOn(long beamId, int id, long block) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				NetworkSegmentProvider nt = listeners.get(i).getNetworkObject();
				nt.beamLatchBuffer.add(beamId);
				nt.beamLatchBuffer.add(id);
				nt.beamLatchBuffer.add(block);
			}
		}
	}

	public void sendExplosionGraphic(Vector3f explosionOrigin) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().explosions.add(new RemoteVector3f(isOnServer(), explosionOrigin));
			}
		}
	}

	private void sendTextBlockServerUpdate(SegmentPiece textBlock, String text) {
		for(int i = 0; i < listeners.size(); i++) {
			SendableSegmentProvider pr = listeners.get(i);
			if(pr.isSendTo()) {
				pr.clearChangedTextBoxLongRangeIndices();
				TextBlockPair p = new TextBlockPair();
				p.block = textBlock.getTextBlockIndex();
				p.text = text;
				p.provider = pr;
				pr.getNetworkObject().textBlockResponsesAndChangeRequests.add(new RemoteTextBlockPair(p, isOnServer()));
			} else {
				pr.sendTextBoxCacheClearIfNotSentYet(textBlock.getTextBlockIndex());
			}
		}
	}

	@Override
	public void sendBlockServerMessage(ServerMessage m) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().messagesToBlocks.add(new RemoteServerMessage(m, isOnServer()));
			}
		}
	}

	@Override
	public void sendBlockKill(SegmentPiece p) {
		sendBlockHpByte(p, (short) 0);
	}

	public void sendBlockKill(long p) {
		sendBlockHpByte(p, (short) 0);
	}

	public void sendBlockHpByte(long p, short hp) {
		sendBlockHpByte(ElementCollection.getPosX(p), ElementCollection.getPosY(p), ElementCollection.getPosZ(p), hp);
	}

	// public Set<SegmentPiece> getSegmentModificationsToSend() {
	// return segmentModificationsToSend;
	// }

	@Override
	public void sendBlockHpByte(SegmentPiece p, short hp) {
		sendBlockHpByte(p.getSegment().pos.x + p.x, p.getSegment().pos.y + p.y, p.getSegment().pos.z + p.z, hp);
	}

	@Override
	public void sendBlockSalvage(SegmentPiece p) {
		sendBlockSalvage(p.getSegment().pos.x + p.x, p.getSegment().pos.y + p.y, p.getSegment().pos.z + p.z);
	}

	@Override
	public void sendBlockSalvage(int x, int y, int z) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().salvageBuffer.addCoord((short) x, (short) y, (short) z);
			}
		}
	}

	public void forceAllRawFiles() {
		IOFileManager.writeAllOpenFiles(getSegmentProvider().getSegmentDataIO().getManager());
	}

	/**
	 * @return the blockActivationBuffer
	 */
	public LongArrayFIFOQueue getBlockActivationBuffer() {
		return blockActivationBuffer;
	}

	/**
	 * @return the blockEffectManager
	 */
	public BlockEffectManager getBlockEffectManager() {
		return blockEffectManager;
	}

	/*
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#modivyGravity(javax.vecmath.Vector3f)
	 */
	@Override
	protected void modivyGravity(Vector3f inout) {
		super.modivyGravity(inout);
		inout.scale(Math.max(0f, 1f - this.blockEffectManager.status.antiGravity));
	}

	@Override
	public void initFromNetworkObject(NetworkObject from) {
		super.initFromNetworkObject(from);
		NetworkSegmentController s = (NetworkSegmentController) from;
		if(!isOnServer()) {
			getMinPos().set(s.minSize.getVector());
			getMaxPos().set(s.maxSize.getVector());
			setScrap(s.scrap.get());
			setFactionRights(s.factionRigths.getByte());
			setVulnerable(s.vulnerable.get());
			setMinable(s.minable.get());
			((SegmentBufferManager) getSegmentBuffer()).setExpectedNonEmptySegmentsFromLoad(getNetworkObject().expectedNonEmptySegmentsFromLoad.getInt());
			if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof PowerManagerInterface) {
				PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getPowerAddOn();
				for(int i = 0; i < getNetworkObject().initialPower.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialPower.getReceiveBuffer().getLong(i);
					powerAddOn.setInitialPower(val);
				}
				for(int i = 0; i < getNetworkObject().initialBatteryPower.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialBatteryPower.getReceiveBuffer().getLong(i);
					powerAddOn.setInitialBatteryPower(val);
				}
			}
			if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldAddOn shields = ((ShieldContainerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getShieldAddOn();
				for(int i = 0; i < getNetworkObject().initialShields.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialShields.getReceiveBuffer().getLong(i);
					shields.setInitialShields(val);
				}
			}
			this.setCreatorId(s.creatorId.getByte());
			railController.updateFromNetworkObject();
			currentOwnerLowerCase = getNetworkObject().currentOwner.get();
			lastDockerPlayerServerLowerCase = getNetworkObject().lastDockerPlayerServerLowerCase.get();
			setVirtualBlueprint(getNetworkObject().virtualBlueprint.getBoolean());
			this.dbId = s.dbId.getLong();
		}
		lastAllowed = s.lastAllowed.getLong();
		pullPermission = PullPermission.values()[s.pullPermission.getByte()];
		setRealName(s.realName.get());
		setUniqueIdentifier(getNetworkObject().uniqueIdentifier.get());
		getDockingController().updateFromNetworkObject(s);
		getHpController().initFromNetwork(s);
		getSlotAssignment().updateFromNetworkObject(s);
		getConfigManager().initFromNetworkObject(getNetworkObject());
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.world.SimpleTransformableSendableObject#toNiceString()
	 */
	@Override
	public String toNiceString() {
		return null;
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		NetworkSegmentController s = (NetworkSegmentController) o;
		handleReceivedControllers(s);
		handleReceivedHarvestConnections(s);
		blockEffectManager.updateFromNetworkObject(s);
		getHpController().updateFromNetworkObject(s);
		railController.updateFromNetworkObject();
		getSlotAssignment().updateFromNetworkObject(s);
		getConfigManager().updateFromNetworkObject(getNetworkObject());
		if(!isOnServer()) {
			lastAllowed = s.lastAllowed.getLong();
			pullPermission = PullPermission.values()[s.pullPermission.getByte()];
			setVirtualBlueprint(getNetworkObject().virtualBlueprint.getBoolean());
			setScrap(getNetworkObject().scrap.get());
			setVulnerable(s.vulnerable.get());
			setMinable(s.minable.get());
			setFactionRights(s.factionRigths.getByte());
			this.dbId = s.dbId.getLong();
			currentOwnerLowerCase = getNetworkObject().currentOwner.get();
			lastDockerPlayerServerLowerCase = getNetworkObject().lastDockerPlayerServerLowerCase.get();
			coreTimerStarted = ((NetworkSegmentController) o).coreDestructionStarted.get();
			coreTimerDuration = ((NetworkSegmentController) o).coreDestructionDuration.get();
			// receive text block changes on client
			for(int i = 0; i < getNetworkObject().textBlockChangeBuffer.getReceiveBuffer().size(); i++) {
				synchronized(receivedTextBlocks) {
					receivedTextBlocks.enqueue(getNetworkObject().textBlockChangeBuffer.getReceiveBuffer().get(i).get());
				}
			}
			for(int i = 0; i < getNetworkObject().pullPermissionAskAnswerBuffer.getReceiveBuffer().size(); i++) {
				askForPullClient = getNetworkObject().pullPermissionAskAnswerBuffer.getReceiveBuffer().getLong(i);
			}
			if(!s.minSize.equalsVector(getMinPos())) {
				s.minSize.getVector(getMinPos());
				((ClientSegmentProvider) getSegmentProvider()).flagDimChange();
			}
			if(!s.maxSize.equalsVector(getMaxPos())) {
				s.maxSize.getVector(getMaxPos());
				((ClientSegmentProvider) getSegmentProvider()).flagDimChange();
			}
			if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof PowerManagerInterface) {
				PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getPowerAddOn();
				for(int i = 0; i < getNetworkObject().initialPower.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialPower.getReceiveBuffer().getLong(i);
					powerAddOn.setInitialPower(val);
				}
				for(int i = 0; i < getNetworkObject().initialBatteryPower.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialBatteryPower.getReceiveBuffer().getLong(i);
					powerAddOn.setInitialBatteryPower(val);
				}
			}
			if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof ShieldContainerInterface) {
				ShieldAddOn shields = ((ShieldContainerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getShieldAddOn();
				for(int i = 0; i < getNetworkObject().initialShields.getReceiveBuffer().size(); i++) {
					long val = getNetworkObject().initialShields.getReceiveBuffer().getLong(i);
					shields.setInitialShields(val);
				}
			}
			for(int i = 0; i < getNetworkObject().shieldHits.getReceiveBuffer().size(); i++) {
				RemoteVector4f r = getNetworkObject().shieldHits.getReceiveBuffer().get(i);
				if(((GameClientState) getState()).getWorldDrawer() != null) {
					ShieldDrawer shieldDrawer = ((GameClientState) getState()).getWorldDrawer().getShieldDrawerManager().get(this);
					Vector4f v = r.getVector();
					if(shieldDrawer != null) {
						shieldDrawer.addHitOld(new Vector3f(v.x, v.y, v.z), v.w);
					}
					this.remoteShieldHits.enqueue(v);
				}
			}
			for(int i = 0; i < getNetworkObject().hits.getReceiveBuffer().size(); i++) {
				RemoteVector4f r = getNetworkObject().hits.getReceiveBuffer().get(i);
				this.remoteHits.enqueue(r.getVector(new Vector4f()));
			}
		} else {
			for(int i = 0; i < getNetworkObject().clientToServerCheckEmptyConnection.getReceiveBuffer().size(); i++) {
				long r = getNetworkObject().clientToServerCheckEmptyConnection.getReceiveBuffer().getLong(i);
				getControlElementMap().checkControllerOnServer(r);
			}
			for(int i = 0; i < getNetworkObject().pullPermissionChangeBuffer.getReceiveBuffer().size(); i++) {
				byte b = getNetworkObject().pullPermissionChangeBuffer.getReceiveBuffer().getByte(i);
				pullPermission = PullPermission.values()[b];
			}
			for(int i = 0; i < getNetworkObject().pullPermissionAskAnswerBuffer.getReceiveBuffer().size(); i++) {
				lastAllowed = getNetworkObject().pullPermissionAskAnswerBuffer.getReceiveBuffer().get(i).longValue();
			}
			ObjectArrayList<RemoteString> ownerChangeReq = s.currentOwnerChangeRequest.getReceiveBuffer();
			for(int i = 0; i < ownerChangeReq.size(); i++) {
				String nameChange = ownerChangeReq.get(i).get();
				this.ownerChangeRequested = nameChange;
				this.ownerChangeRequestedClientId = senderId;
			}
		}
		String nameBefore = null;
		if(isOnServer() && !getRealName().equals(s.realName.get())) {
			System.err.println("[SERVER] received name change from client " + senderId + ": " + getRealName() + " -> " + s.realName.get());
			try {
				LogUtil.log().fine("[RENAME] " + ((GameServerState) getState()).getPlayerFromStateId(senderId).getName() + " changed object name: \"" + getRealName() + "\" to \"" + s.realName.get() + "\"");
			} catch(PlayerNotFountException e) {
				e.printStackTrace();
			}
			getNetworkObject().realName.setChanged(true);
			nameBefore = getRealName();
		}
		setRealName(s.realName.get());
		if(nameBefore != null) {
			onRename(nameBefore, getRealName());
		}
		getDockingController().updateFromNetworkObject(s);
		if(!s.structureInterconnectRequestBuffer.getReceiveBuffer().isEmpty()) {
			synchronized(receivedInterconnectedStructure) {
				for(int i = 0; i < getNetworkObject().structureInterconnectRequestBuffer.getReceiveBuffer().size(); i++) {
					receivedInterconnectedStructure.enqueue(s.structureInterconnectRequestBuffer.getReceiveBuffer().get(i).get());
				}
			}
		}
	}

	@Override
	public void updateToFullNetworkObject() {
		super.updateToFullNetworkObject();
		assert (getUniqueIdentifier() != null);
		getNetworkObject().factionRigths.set(getFactionRights());
		getNetworkObject().currentOwner.set(currentOwnerLowerCase);
		getNetworkObject().lastDockerPlayerServerLowerCase.set(lastDockerPlayerServerLowerCase);
		getNetworkObject().uniqueIdentifier.set(getUniqueIdentifier());
		getNetworkObject().scrap.set(isScrap());
		getNetworkObject().vulnerable.set(isVulnerable());
		getNetworkObject().minable.set(isMinable());
		getNetworkObject().creatorId.set((byte) getCreatorId());
		getNetworkObject().dbId.set(dbId);
		getNetworkObject().expectedNonEmptySegmentsFromLoad.set(((SegmentBufferManager) getSegmentBuffer()).getExpectedNonEmptySegmentsFromLoad());
		if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof PowerManagerInterface) {
			PowerAddOn powerAddOn = ((PowerManagerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getPowerAddOn();
			getNetworkObject().initialPower.add((long) (powerAddOn.getInitialPower() + powerAddOn.getPowerSimple()));
			getNetworkObject().initialBatteryPower.add((long) (powerAddOn.getInitialBatteryPower() + powerAddOn.getBatteryPower()));
		}
		if(this instanceof ManagedSegmentController<?> && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof ShieldContainerInterface) {
			ShieldAddOn shields = ((ShieldContainerInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getShieldAddOn();
			getNetworkObject().initialShields.add((long) (shields.getInitialShields() + shields.getShields()));
		}
		railController.updateToFullNetworkObject();
		getConfigManager().updateToFullNetworkObject(getNetworkObject());
		getNetworkObject().lastAllowed.set(lastAllowed);
		getNetworkObject().pullPermission.set((byte) pullPermission.ordinal());
		getSlotAssignment().sendAll();
		getHpController().updateToFullNetworkObject();
		blockEffectManager.updateToFullNetworkObject(getNetworkObject());
		updateToNetworkObject();
	}

	@Override
	public void updateToNetworkObject() {
		super.updateToNetworkObject();
		assert (getMinPos() != null);
		if(isOnServer()) {
			getNetworkObject().dbId.set(dbId);
			getNetworkObject().virtualBlueprint.set(isVirtualBlueprint());
			railController.updateToNetworkObject();
			getNetworkObject().factionRigths.set(getFactionRights());
			getNetworkObject().currentOwner.set(currentOwnerLowerCase);
			getNetworkObject().lastDockerPlayerServerLowerCase.set(lastDockerPlayerServerLowerCase);
			getNetworkObject().scrap.set(isScrap());
			getNetworkObject().vulnerable.set(isVulnerable());
			getNetworkObject().minable.set(isMinable());
			getNetworkObject().minSize.set(getMinPos());
			getNetworkObject().maxSize.set(getMaxPos());
			getNetworkObject().coreDestructionStarted.set(coreTimerStarted);
			getNetworkObject().coreDestructionDuration.set(coreTimerDuration);
			getNetworkObject().lastAllowed.set(lastAllowed);
			getNetworkObject().pullPermission.set((byte) pullPermission.ordinal());
			if(!getRealName().equals(getNetworkObject().realName.get())) {
				getNetworkObject().realName.set(getRealName());
			}
		}
		getConfigManager().updateToNetworkObject(getNetworkObject());
		getHpController().updateToNetworkObject();
	}

	public void handleNTDockChanged() {
		getDockingController().onDockChanged(getNetworkObject());
	}

	public void handleReceivedBlockActivations(NetworkSegmentProvider s) {
		for(int i = 0; i < s.blockActivationBuffer.getReceiveBuffer().size(); i++) {
			long blockPos = s.blockActivationBuffer.getReceiveBuffer().getLong(i);
			synchronized(blockActivationBuffer) {
				blockActivationBuffer.enqueue(blockPos);
			}
		}
	}

	private void handleReceivedControllers(NetworkSegmentController s) {
		getControlElementMap().handleReceived();
	}

	private void handleReceivedHarvestConnections(NetworkSegmentController s) {
	}

	protected void handleReceivedModifications(NetworkSegmentProvider s) {
		boolean forUs = isOnServer() || ((GameClientState) getState()).getCurrentSectorEntities().containsKey(getId());
		if(forUs) {
			blockProcessor.receivedMods(s);
			for(int i = 0; i < s.explosions.getReceiveBuffer().size(); i += 4) {
				Vector3f exp = s.explosions.getReceiveBuffer().get(i).getVector();
				if(((GameClientState) getState()).getCurrentSectorId() == getSectorId() && ((GameClientState) getState()).getWorldDrawer() != null && ((GameClientState) getState()).getWorldDrawer().getExplosionDrawer() != null) {
					((GameClientState) getState()).getWorldDrawer().getExplosionDrawer().addExplosion(exp, 15);
					Transform t = new Transform();
					t.setIdentity();
					t.origin.set(exp);
					/*AudioController.fireAudioEvent("EXPLOSION", new AudioTag[] { AudioTags.GAME, AudioTags.SHIP, AudioTags.EXPLOSION }, AudioParam.ONE_TIME, AudioController.ent(this, t, 0L, 15))*/
					AudioController.fireAudioEventID(942, AudioController.ent(this, t, 0L, 15));
				}
			}
			for(int i = 0; i < s.beamLatchBuffer.getReceiveBuffer().size(); i += 3) {
				if(((GameClientState) getState()).getCurrentSectorId() == getSectorId()) {
					long beamId = s.beamLatchBuffer.getReceiveBuffer().getLong(i);
					int objId = (int) s.beamLatchBuffer.getReceiveBuffer().getLong(i + 1);
					long blockPos = s.beamLatchBuffer.getReceiveBuffer().getLong(i + 2);
					addReceivedBeamLatch(beamId, objId, blockPos);
				}
			}
		}
	}

	protected void addReceivedBeamLatch(long beamId, int objId, long blockPos) {
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#isVolatile()
	 */
	@Override
	public boolean isVolatile() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.Sendable#newNetworkObject()
	 */
	@Override
	public void newNetworkObject() {
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.util.Collisionable#onCollision(com.bulletphysics.collision.narrowphase.ManifoldPoint, org.schema.schine.network.objects.Sendable)
	 */
	@Override
	public void onCollision(ManifoldPoint pt, Sendable sendableB) {
	}

	public void setServerSendableSegmentController(SendableSegmentProvider sendableSegmentProvider) {
		this.serverSendableSegmentProvider = sendableSegmentProvider;
	}

	public void onBlockAddedHandled() {
	}

	public void sendBlockBulkMod(RemoteBlockBulk mod) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().modificationBulkBuffer.add(mod);
			}
		}
	}

	private void handleActivationsAndControlMap(Timer timer, long time) {
		if(isOnServer()) {
			if(timer.currentTime - this.logicCooldown < 10000) {
				delayActivationBuffer.clear();
				blockActivationBuffer.clear();
				delayActivationBufferNonRepeating.clear();
			}
		}
		if(isOnServer()) {
			handleActivationsServer(timer);
		}
		if(!delayActivationBuffer.isEmpty()) {
			ObjectIterator<Entry<Long, DelayedAct>> iterator = delayActivationBuffer.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Long, DelayedAct> next = iterator.next();
				if(time > next.getValue().time + 500) {
					SegmentPiece block = getSegmentBuffer().getPointUnsave(next.getKey());
					if(block != null) {
						if(ElementKeyMap.isButton(block.getType())) {
							// block will reset into the false state. this will not send a signal
							long da = ElementCollection.getDeactivation(block.getAbsoluteIndex(), true, false);
							blockActivationBuffer.enqueue(da);
						} else {
							// this is a delayed signal
							synchronized(blockActivationBuffer) {
								blockActivationBuffer.enqueue(next.getValue().encode);
							}
						}
						iterator.remove();
					}
				}
			}
		}
		if(!delayActivationBufferNonRepeating.isEmpty()) {
			ObjectIterator<Entry<Long, DelayedAct>> iterator = delayActivationBufferNonRepeating.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Long, DelayedAct> next = iterator.next();
				if(time > next.getValue().time + 500) {
					synchronized(blockActivationBuffer) {
						blockActivationBuffer.enqueue(next.getValue().encode);
					}
					iterator.remove();
				}
			}
		}
		long t0 = System.currentTimeMillis();
		getControlElementMap().updateLocal(timer);
		long took = System.currentTimeMillis() - t0;
		if(took > 20) {
			System.err.println("[SENSEGMENTCONTROLLER][" + getState() + "] WARNING: getControlElementMap().update(timer) of " + this + " took " + took + " ms");
		}
	}

	@Override
	public void onBlockKill(SegmentPiece piece, Damager from) {
		/// INSERTED CODE @...
		for(SegmentPieceKilledListener listener : FastListenerCommon.segmentPieceKilledListeners)
			listener.onBlockKilled(piece, this, from, isOnServer());
		///
		if(piece.getType() == 0) {
			System.err.println(getState() + " WARNING: Killed an air block (should not happen) " + piece);
		} else if(isOnServer()) {
			railController.getRoot().onDamageServerRootObject(piece.getInfo().getMaxHitPointsFull(), from);
		}
		//INSERTED CODE @1251
		SegmentPieceKillEvent event = new SegmentPieceKillEvent(piece, this, from);
		StarLoader.fireEvent(SegmentPieceKillEvent.class, event, this.isOnServer());
		///blockProcessor.onBlockChanged((RemoteSegment) piece.getSegment());
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().onBlockKill(piece.getAbsoluteIndex(), piece.getType(), from);
		}
	}

	private ReentrantReadWriteLock handleActivationServer(Timer timer, SignalTrace sig, ReentrantReadWriteLock currentLock, long currentTime) {
		this.currentTrace = sig;
		long a = sig.a;
		SegmentPiece block = getSegmentBuffer().getPointUnsave(ElementCollection.getPosX(a), ElementCollection.getPosY(a), // autorequest true previously
				ElementCollection.getPosZ(a));
		long posIndex = ElementCollection.getPosIndexFrom4(a);
		if(block == null) {
			// System.err.println("BLOCK NULL");
			failedActivating++;
			delayActivationBuffer.put(posIndex, new DelayedAct(a, currentTime + 1000));
			return currentLock;
		}
		if(!ElementKeyMap.isValidType(block.getType())) {
			System.err.println("[EXCEPTION][SERVER] tried to handle activation of nonexitent block: " + block.getType());
			return currentLock;
		}
		long activationType = ElementCollection.getType(a);
		// System.err.println("RECEIVED ACTIVATION TYPE: "+activationType);
		boolean reactivated = false;
		boolean sentFromWireless = false;
		if(activationType > 100) {
			activationType -= ElementCollection.SENT_FROM_WIRELESS;
			sentFromWireless = true;
			// rewrite a so no abnormally happens
			a = ElementCollection.getIndex4(posIndex, (short) activationType);
		}
		if(activationType > 10) {
			activationType -= ElementCollection.FROM_DELIGATE_ECODE;
			reactivated = true;
		}
		if(sentFromWireless && sig.parent == null && block.getType() == ElementKeyMap.LOGIC_WIRELESS) {
			// the start point is this wireless block and it was activated by another wireless block
			sig.rootPos = posIndex;
		}
		boolean signalActive = (activationType == ElementCollection.ACTIVE || activationType == ElementCollection.ACTIVE_NO_DELEGATE);
		boolean canDelegate = true;
		if(ElementKeyMap.isButton(block.getType())) {
			long l = ElementCollection.getIndex4(posIndex, (short) activationType);
			if(!block.isActive() && !delayActivationBuffer.containsKey(l)) {
				delayActivationBuffer.put(posIndex, new DelayedAct(l, currentTime));
			}
		}
		if(block.getType() == ElementKeyMap.RACE_GATE_CONTROLLER) {
			if(signalActive) {
				((RaceManagerState) getState()).getRaceManager().onActivateRaceController(block);
			}
			System.err.println("[SERVER] ACTIVATED RACE " + signalActive);
		}
		if(block.getType() == ElementKeyMap.WARP_GATE_CONTROLLER && ((ManagedSegmentController<?>) this).getManagerContainer().getWarpGate() != null) {
			WarpgateCollectionManager wcm = ((ManagedSegmentController<?>) this).getManagerContainer().getWarpGate().getCollectionManagersMap().get(block.getAbsoluteIndex());
			if(wcm != null) {
				wcm.setActive(signalActive);
				return currentLock;
			}
		}
		if(block.getType() == ElementKeyMap.SIGNAL_SENSOR) {
			SensorCollectionManager sensor = ((ManagedSegmentController<?>) this).getManagerContainer().getSensor().getCollectionManagersMap().get(block.getAbsoluteIndex());
			if(signalActive && sensor != null) {
				sensor.check();
			}
			return currentLock;
		}
		if(block.getType() == ElementKeyMap.AI_ELEMENT) {
			if(this instanceof SegmentControllerAIInterface) {
				((SegmentControllerAIInterface) this).activateAI(signalActive, true);
			}
			return currentLock;
		}
		if(block.getType() == ElementKeyMap.LOGIC_WIRELESS) {

//			if(block.getAbsoluteIndex() != sig.rootPos) {
			//only reactivate if this is a first class signal
			//don't activate if the signal originated from another wireless block on another structure
			if(this instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) this).getManagerContainer() instanceof ActivationManagerInterface) {
					ActivationManagerInterface ac = ((ActivationManagerInterface) ((ManagedSegmentController<?>) this).getManagerContainer());
					ActivationCollectionManager aco = ac.getActivation().getCollectionManagersMap().get(posIndex);
					if(aco != null) {
						//						System.err.println("[SERVER][ACTIVATION] activated wireless logic on " + this + "; " +
						//								(aco.getDestination() != null ? aco.getDestination().marking + ";" + ElementCollection.getPosFromIndex(aco.getDestination().markerLocation, new Vector3i()) : "null"));

						if(aco.getDestination() != null) {
							Sendable sendable = getState().getLocalAndRemoteObjectContainer().getUidObjectMap().get(aco.getDestination().marking);
							if(sendable instanceof SegmentController) {
								SendableSegmentController d = (SendableSegmentController) sendable;
								SegmentPiece pointUnsave = d.getSegmentBuffer().getPointUnsave(aco.getDestination().markerLocation);
								if(pointUnsave != null) {
									if(pointUnsave.getType() == ElementKeyMap.LOGIC_WIRELESS) {

										long da;
										if(signalActive) {
											da = ElementCollection.getActivationWireless(pointUnsave.getAbsoluteIndex(), true, false);
										} else {
											da = ElementCollection.getDeactivationWireless(pointUnsave.getAbsoluteIndex(), true, false);
										}
										System.err.println("[SERVER][ACTIVATION] sent '" + signalActive + "' signal from " + this + " -> " + aco.getDestination().marking + "; " + ElementCollection.getPosFromIndex(aco.getDestination().markerLocation, new Vector3i()));
										if(pointUnsave.isActive() != !block.isActive()) {
											if(block.getAbsoluteIndex() != sig.rootPos) {
												d.blockActivationBuffer.enqueue(da);
											} else {
												d.delayActivationBuffer.put(pointUnsave.getAbsoluteIndex(), new DelayedAct(da, currentTime + 500));
											}
										}
									}
								} else {
									System.err.println("[SERVER][ACTIVATION][WARNING] wireless logic signal delayed. Recipient block not loaded yet: " + this + " -> " + sendable);
									//other object's block not loaded. requeue with delay
									failedActivating++;
									delayActivationBuffer.put(posIndex, new DelayedAct(a, currentTime + 1000));
									return currentLock;
								}
							} else {
								System.err.println("[SERVER][ACTIVATION][WARNING] " + this + " Destination " + aco.getDestination().marking + " not found for wireless block collectionManager at " + posIndex);
							}
						} else {
							System.err.println("[SERVER][ACTIVATION][WARNING] " + this + " Wireless block collectionManager at " + posIndex + " has no destination");
						}
					} else {
						System.err.println("[SERVER][ACTIVATION][ERROR] " + this + " Wireless block collectionManager not found at " + posIndex);
					}
				}
			}
//			}else {
//				if(getState().getUpdateTime() - lastSignalStopMsgSent > 100) {
//					ServerMessage msg = new ServerMessage(Lng.astr("Logic loop protection: Signal stopped from traveling to another entity!"), ServerMessage.MESSAGE_TYPE_ERROR_BLOCK);
//					msg.block = block.getAbsoluteIndex();
//					sendBlockServerMessage(msg);
//					lastSignalStopMsgSent = getState().getUpdateTime();
//				}
//			}
		}
		if(reactivated) {
			a = ElementCollection.getIndex4(posIndex, (short) activationType);
			// System.err.println(getState()+" RECEIVED REACTIVATED "+block+" -> "+activationType+"/"+ElementCollection.getType(a));
			assert (activationType == ElementCollection.getType(a));
			if(block.getType() == ElementKeyMap.SIGNAL_DELAY_BLOCK_ID) {
				if(!delayActivationBuffer.containsKey(a)) {
					delayActivationBuffer.put(posIndex, new DelayedAct(a, currentTime));
				}
				return currentLock;
			}
			if(block.getType() == ElementKeyMap.SIGNAL_DELAY_NON_REPEATING_ID) {
				if(this instanceof ManagedSegmentController<?>) {
					if(((ManagedSegmentController<?>) this).getManagerContainer() instanceof ActivationManagerInterface) {
						ActivationManagerInterface ac = ((ActivationManagerInterface) ((ManagedSegmentController<?>) this).getManagerContainer());
						ActivationCollectionManager activationCollectionManager = ac.getActivation().getCollectionManagersMap().get(posIndex);
						if(activationCollectionManager != null) {
							if(signalId > activationCollectionManager.currentSignal) {
								activationCollectionManager.currentSignal = signalId;
								if(!delayActivationBufferNonRepeating.containsKey(a)) {
									delayActivationBufferNonRepeating.put(posIndex, new DelayedAct(a, currentTime));
								}
							} else {
							}
						}
					}
				}
				return currentLock;
			}
			if(!ElementKeyMap.isValidType(block.getType()) || !ElementKeyMap.getInfo(block.getType()).isSignal()) {
				// when reactivated, do not deligate for blocks that are not signals
				canDelegate = false;
			}
		} else {
			boolean inc = true;
			if(block.getType() == ElementKeyMap.SIGNAL_DELAY_NON_REPEATING_ID && this instanceof ManagedSegmentController<?>) {
				if(((ManagedSegmentController<?>) this).getManagerContainer() instanceof ActivationManagerInterface ac) {
					ActivationCollectionManager activationCollectionManager = ac.getActivation().getCollectionManagersMap().get(posIndex);
					if(activationCollectionManager != null) {
						signalId = activationCollectionManager.currentSignal;
						// System.err.println("ACTIVATE FROM ACTIVATION SIGNAL: "+signalId);
						inc = false;
					}
				}
			}
			if(inc) {
				signalId++;
			}
		}
		/*
		 *
		 * inactive = 1
		 * inactiveNoDelegate = 2
		 * active = 3
		 * activeNoDelegate = 4
		 *
		 * consequential changes of activation
		 * (like the whole collection for doors)
		 * set the act modifier to -2 for inactive and 2 for active.
		 * This is necessary to prevent an
		 * endless loop with self changing collections
		 */
		boolean delegateToManagedController = canDelegate && ElementCollection.isDeligateFromActivationIndex(a);
		assert (activationType == ElementCollection.getType(a));
		if(delegateToManagedController) {
			if(currentTrace.checkLoop()) {
				return currentLock;
			}
		}
		// remember that this one already fired once
		boolean active = ElementCollection.isActiveFromActivationIndex(a);
		assert (isOnServer());
		if(block.getType() == ElementKeyMap.GRAVITY_ID && (block.getSegmentController() instanceof Planet || block.getSegmentController() instanceof PlanetIco)) {
			return currentLock;
		}
		if(block.getSegment().getSegmentData() == null) {
			throw new NullPointerException(block.getSegment() + " was empty (segment data null) but it's an activation " + block);
		}
		if(currentLock == null || currentLock != block.getSegment().getSegmentData().rwl) {
			if(currentLock != null) {
				currentLock.writeLock().unlock();
			}
			block.getSegment().getSegmentData().rwl.writeLock().lock();
			currentLock = block.getSegment().getSegmentData().rwl;
		}
		/**
		 * This can change the activation of the SegmentPiece (block)
		 * e.g. if a signal is hit
		 */
		boolean oldActive = block.isActive();
		// System.err.println("ACTIVE 0 ::: "+oldActive+" -> "+active);
		ReplacementContainer railReplace = null;
		ReplacementContainer displayRepl = null;
		// System.err.println("BLOCK "+block+" "+oldActive+" -> "+active);
		if(active != oldActive || (block.getType() == ElementKeyMap.SIGNAL_NOT_BLOCK_ID && active == oldActive)) {
			boolean acc = active;
			if(block.getType() == ElementKeyMap.SIGNAL_NOT_BLOCK_ID) {
				acc = !acc;
			}
			railReplace = getReplacementConnected(sig, block, acc, currentTime, railController);
			displayRepl = getReplacementConnected(sig, block, acc, currentTime, displayReplace);
		}
		if((active != oldActive) && (railReplace == null || displayRepl == null)) {
			// connected rail/display block isn't loaded. delay whole process
			failedActivating++;
			delayActivationBuffer.put(posIndex, new DelayedAct(a, currentTime + 1000));
			return currentLock;
		}
		if(delegateToManagedController && this instanceof ManagedSegmentController) {
			// System.err.println("[SERVER] HANDLE BLOCK ACTIVATING: "+block+": "+active+"; deligate: "+delegateToManagedController+"; ("+a+")");
			// deligated blocks can be signals or are direct normal blocks like opening doors
			boolean handleBlockActivate = ((ManagedSegmentController<?>) this).getManagerContainer().handleBlockActivate(block, oldActive, active);
			if(!handleBlockActivate) {
				// System.err.println("[SERVER] FAILED ACTIVATING " + block + "; reenqueueing");
				failedActivating++;
				delayActivationBuffer.put(posIndex, new DelayedAct(a, currentTime + 3000));
				return currentLock;
			}
			if(!ElementKeyMap.getInfo(block.getType()).isSignal() && !ElementKeyMap.getInfo(block.getType()).isRailTrack()) {
				block.setActive(active);
			}
		} else {
			if(ElementKeyMap.isValidType(block.getType()) && !ElementKeyMap.getInfo(block.getType()).isRailTrack()) {
				block.setActive(active);
			}
		}
		// block.setActive(true);
		// System.err.println("ACTIVE 1 ::: "+oldActive+" -> "+active+"; "+block.isActive()+"; "+ElementKeyMap.getInfo(block.getType()).isRailTrack());
		if(active && (block.getType() == ElementKeyMap.RAIL_BLOCK_DOCKER || block.getType() == Blocks.SHIPYARD_CORE_ANCHOR.getId()) && railController.isDockedAndExecuted()) {
			railController.disconnect();
			return currentLock;
		}
		assert (block.getType() != 0);
		int changedRetCode;
		try {
			changedRetCode = block.getSegment().getSegmentData().applySegmentData(block, currentTime);
		} catch(SegmentDataWriteException e) {
			try {
				SegmentDataWriteException.replaceData(block.getSegment());
				changedRetCode = block.getSegment().getSegmentData().applySegmentData(block, currentTime);
			} catch(SegmentDataWriteException e1) {
				throw new RuntimeException(e1);
			}
		}
		if(this instanceof ManagedSegmentController<?>) {
			// execute always for signals (even in fast logic clock)
			if(block.getInfo().isSignal()) {
				long fromActivation = Long.MIN_VALUE;
				if(sig.parent != null) {
					fromActivation = sig.parent.pos;
				}
				boolean handleActivateBlockActivate = ((ManagedSegmentController<?>) this).getManagerContainer().handleActivateBlockActivate(block, fromActivation, oldActive, timer);
				// System.err.println("HAMD: "+handleActivateBlockActivate);
				if(!handleActivateBlockActivate) {
					// System.err.println("[SERVER] FAILED ACTIVATING "+block+"; reenqueueing");
					delayActivationBuffer.put(posIndex, new DelayedAct(a, timer.currentTime + 3000));
				}
			}
		}
		((RemoteSegment) block.getSegment()).setLastChanged(currentTime);
		if(changedRetCode != SegmentData.PIECE_UNCHANGED) {
			block.refresh();
			assert (oldActive != block.isActive()) : block;
			// if(block.getInfo().isLightSource() && !block.getInfo().isSignal() ) {
			// System.err.println("#LIGHT CHANGE::::: "+block.isActive());
			// }
			long fromActivation = Long.MIN_VALUE;
			if(sig.parent != null) {
				fromActivation = sig.parent.pos;
			}
			queueBlockChangeReaction(oldActive, active, block, posIndex, a, fromActivation);
			if(ElementKeyMap.isValidType(block.getType()) && ElementKeyMap.getInfo(block.getType()).isSignal()) {
				// block seActive is done in the handling
				if(railReplace != null) {
					boolean replaced = replaceConnected(railReplace, sig, block, active, currentTime, railController);
					if(active && !replaced) {
						// save to call since all connected blocks have been checked for replace
						railController.disconnectFromRailRailIfContact(block, currentTime);
					}
				}
				if(displayRepl != null) {
					replaceConnected(displayRepl, sig, block, active, currentTime, displayReplace);
				}
			}
		}
		return currentLock;
	}

	private void executeBlockActiveReaction(Timer timer, BlockActiveReaction o) {
		if(this instanceof ManagedSegmentController<?>) {
			if(o.active && !isVirtualBlueprint() && o.block.getType() == ElementKeyMap.EXPLOSIVE_ID && ((ManagedSegmentController<?>) this).getManagerContainer() instanceof ExplosiveManagerContainerInterface) {
				ExplosiveManagerContainerInterface exp = ((ExplosiveManagerContainerInterface) ((ManagedSegmentController<?>) this).getManagerContainer());
				Vector3i absolutePos = o.block.getAbsolutePos(new Vector3i());
				exp.getExplosiveElementManager().addExplosion(absolutePos);
			}
			if(o.active && !isVirtualBlueprint() && o.block.getType() == ElementKeyMap.TRANSPORTER_CONTROLLER) {
				ManagerModuleCollection<TransporterUnit, TransporterCollectionManager, TransporterElementManager> transporter = ((TransporterModuleInterface) ((ManagedSegmentController<?>) this).getManagerContainer()).getTransporter();
				TransporterCollectionManager transporterCollectionManager = transporter.getCollectionManagersMap().get(o.block.getAbsoluteIndex());
				if(transporterCollectionManager != null && transporterCollectionManager.canUse()) {
					transporterCollectionManager.sendTransporterUsage();
				}
			}
			if(!o.block.getInfo().isSignal()) {
				// if(o.block.getInfo().isLightSource() && !o.block.getInfo().isSignal() ) {
				// System.err.println("---> LIGHT CHANGE "+o.counter+" times::::: "+o.block.isActive());
				// }
				// execute once for non signals (signals have already been executed in handleActivationServer)
				boolean handleActivateBlockActivate = ((ManagedSegmentController<?>) this).getManagerContainer().handleActivateBlockActivate(o.block, o.fromActivation, o.oldActive, timer);
				if(!handleActivateBlockActivate) {
					// System.err.println("[SERVER] FAILED ACTIVATING "+block+"; reenqueueing");
					delayActivationBuffer.put(o.posIndex, new DelayedAct(o.a, timer.currentTime + 3000));
				}
			}
		}
		long absIn = o.block.getAbsoluteIndex();
		sendBlockActiveChanged(ElementCollection.getPosX(absIn), ElementCollection.getPosY(absIn), ElementCollection.getPosZ(absIn), o.block.isActive());
		((RemoteSegment) o.block.getSegment()).setLastChanged(timer.currentTime);
	}

	private void queueBlockChangeReaction(boolean oldActive, boolean active, SegmentPiece block, long posIndex, long a, long fromActivation) {
		BlockActiveReaction act = getActReaction();
		act.oldActive = oldActive;
		act.active = active;
		act.block = block;
		act.posIndex = posIndex;
		act.a = a;
		act.fromActivation = fromActivation;
		BlockActiveReaction old = blockActivations.put(block.getAbsoluteIndex(), act);
		if(old != null) {
			act.counter = old.counter + 1;
			freeActReaction(old);
		} else {
			act.counter++;
		}
	}

	private void handleBlockActivationsServerAfter(Timer timer) {
		// if used in threads. make sure that block activations isnt static
		for(BlockActiveReaction o : blockActivations.values()) {
			executeBlockActiveReaction(timer, o);
			freeActReaction(o);
		}
		blockActivations.clear();
	}

	/**
	 * @param sig
	 * @param block
	 * @param active
	 * @param currentTime
	 * @param rp
	 * @return a set of blocks to be replaced by activating the block. null when there is one or more unloaded
	 */
	private ReplacementContainer getReplacementConnected(SignalTrace sig, SegmentPiece block, boolean active, long currentTime, BlockLogicReplaceInterface rp) {
		ReplacementContainer r = new ReplacementContainer();
		Vector3i p = block.getAbsolutePos(tmpPos);
		if(active) {
			FastCopyLongOpenHashSet cMap = getControlElementMap().getControllingMap().getAll().get(block.getAbsoluteIndex());
			if(cMap != null) {
				for(int i = 0; i < 6; i++) {
					Vector3i dir = Element.DIRECTIONSi[i];
					SegmentPiece fromBlockSurround = getSegmentBuffer().getPointUnsave(p.x + dir.x, p.y + dir.y, // autorequest true previously
							p.z + dir.z);
					r.fromBlockSurround = fromBlockSurround;
					if(fromBlockSurround == null) {
						System.err.println("FROM SURROUND NULL " + (p.x + dir.x) + ";" + (p.y + dir.y) + "; " + (p.z + dir.z) + "; " + rp.getClass());
						return null;
					}
					if(rp.fromBlockOk(fromBlockSurround)) {
						for(long l : cMap) {
							// autorequest true previously
							SegmentPiece toReplace = getSegmentBuffer().getPointUnsave(l);
							if(toReplace == null) {
								System.err.println("FROM TO REPLACE NULL " + (p.x + dir.x) + ";" + (p.y + dir.y) + "; " + (p.z + dir.z) + "; " + rp.getClass());
								return null;
							}
							r.toReplaceList.add(toReplace);
						}
						r.ok = true;
						return r;
					}
				}
			}
		}
		return r;
	}

	private boolean replaceConnected(ReplacementContainer r, SignalTrace sig, SegmentPiece triggerBlock, boolean active, long currentTime, BlockLogicReplaceInterface rp) {
		if(!r.ok) {
			return false;
		}
		SegmentPiece fromBlockSurround = r.fromBlockSurround;
		final short newBlockType = fromBlockSurround.getType();
		for(SegmentPiece toReplace : r.toReplaceList) {
			final short toReplaceType = toReplace.getType();
			final long toReplaceIndexWithType4 = toReplace.getAbsoluteIndexWithType4();
			if(rp.isBlockNextToLogicOkTuUse(fromBlockSurround, toReplace)) {
				// both blocks are rails so we can replace the target
				if(!rp.equalsBlockData(fromBlockSurround, toReplace)) {
					Segment seg = toReplace.getSegment();
					boolean differentBlock = fromBlockSurround.getType() != toReplace.getType();
					SegmentPiece originalFromBlockSurround = new SegmentPiece(fromBlockSurround);
					rp.modifyReplacement(fromBlockSurround, toReplace);
					try {
						boolean preserveControl = true;
						// add the replaced one
						seg.getSegmentData().applySegmentData(toReplace.x, toReplace.y, toReplace.z, fromBlockSurround.getData(), 0, false, toReplace.getAbsoluteIndex(), false, false, currentTime, preserveControl);
					} catch(SegmentDataWriteException e) {
						try {
							seg.setSegmentData(SegmentDataWriteException.replaceData(seg));
							seg.getSegmentData().applySegmentData(toReplace.x, toReplace.y, toReplace.z, fromBlockSurround.getData(), 0, false, toReplace.getAbsoluteIndex(), false, false, currentTime);
						} catch(SegmentDataWriteException e1) {
							throw new RuntimeException(e1);
						}
					}
					// remove connection and re-add it with proper type
					((RemoteSegment) toReplace.getSegment()).setLastChanged(System.currentTimeMillis());
					toReplace.refresh();
					rp.afterReplaceBlock(originalFromBlockSurround, toReplace);
					// autorequest true previously
					assert (fromBlockSurround.getType() == getSegmentBuffer().getPointUnsave(fromBlockSurround.getAbsoluteIndex()).getType());
					// autorequest true previously
					assert (fromBlockSurround.getOrientation() == getSegmentBuffer().getPointUnsave(fromBlockSurround.getAbsoluteIndex()).getOrientation());
					RemoteSegmentPiece remoteSegmentPiece = new RemoteSegmentPiece(toReplace, getNetworkObject());
					sendBlockMod(remoteSegmentPiece);
					for(it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<FastCopyLongOpenHashSet> e : getControlElementMap().getControllingMap().getAll().long2ObjectEntrySet()) {
						if(e.getValue().contains(toReplaceIndexWithType4)) {
							getControlElementMap().removeControllerForElement(e.getLongKey(), toReplace.getAbsoluteIndex(), toReplaceType);
							SegmentPiece from = getSegmentBuffer().getPointUnsave(e.getLongKey());
							if(from == null || ElementInformation.canBeControlled(from.getType(), newBlockType)) {
								if(from == null) {
									System.err.println("[SERVER] WARNING: block replacement: connection update couldn't load connection starting point to replaced block (To replaced: " + toReplace.getAbsolutePos(new Vector3i()) + "). From (which failed to load): " + ElementCollection.getPosFromIndex(e.getLongKey(), new Vector3i()));
								}
								getControlElementMap().addControllerForElement(e.getLongKey(), toReplace.getAbsoluteIndex(), newBlockType);
							}
						}
					}
					FastCopyLongOpenHashSet fMap = getControlElementMap().getControllingMap().getAll().get(toReplace.getAbsoluteIndex());
					if(fMap != null) {
						LongArrayList l = new LongArrayList(fMap);
						for(long index : l) {
							// redo connection from the replaced block to be sure
							long pos = ElementCollection.getPosIndexFrom4(index);
							short type = (short) ElementCollection.getType(index);
							getControlElementMap().removeControllerForElement(toReplace.getAbsoluteIndex(), pos, type);
							getControlElementMap().addControllerForElement(toReplace.getAbsoluteIndex(), pos, type);
						}
					}
					((RemoteSegment) toReplace.getSegment()).setLastChanged(currentTime);
				} else {
					toReplace.refresh();
					rp.afterReplaceBlock(fromBlockSurround, toReplace);
				}
			}
		}
		return true;
	}

	public SignalTracePool getSignalPool() {
		return signalThread.get();
	}

	private void handleActivationsServer(Timer timer) {
		failedActivating = 0;
		if(!blockActivationBuffer.isEmpty()) {
			SignalTracePool signalPool = getSignalPool();
			long currentTime = getState().getUpdateTime();
			assert (isOnServer());
			// System.err.println("BLOCK UPDATE --------------------------------------------- START");
			// synchronized (getBlockActivationBuffer()) {
			while(!blockActivationBuffer.isEmpty()) {
				long a = blockActivationBuffer.dequeue();
				SignalTrace currentTrace = signalPool.get();
				currentTrace.set(ElementCollection.getPosIndexFrom4(a), a, null);
				signalQueue.enqueue(currentTrace);
				if(signalQueue.size() > ServerConfig.MAX_LOGIC_SIGNAL_QUEUE_PER_OBJECT.getInt()) {
					((GameServerState) getState()).getController().broadcastMessage(Lng.astr("WARNING: Too much logic lag caused by\n%s; in sector: %s", this.getRealName(), this.getSector(new Vector3i())), ServerMessage.MESSAGE_TYPE_ERROR);
					while(!signalQueue.isEmpty()) {
						signalPool.free(signalQueue.dequeue());
					}
					return;
				}
			}
			// }
			ReentrantReadWriteLock currentLock = null;
			while(!signalQueue.isEmpty()) {
				SignalTrace st = signalQueue.dequeue();
				currentLock = handleActivationServer(timer, st, currentLock, currentTime);
				signalPool.markFree(st);
			}
			signalPool.freeMarked();
			if(currentLock != null) {
				currentLock.writeLock().unlock();
			}
			if(blockActivationBuffer.size() > 0) {
				handleActivationsServer(timer);
			}
			// System.err.println("BLOCK UPDATE --------------------------------------------- END");
		}
		handleBlockActivationsServerAfter(timer);
		if(failedActivating > 0) {
			System.err.println("[SERVER] " + this + " Failed Activating " + failedActivating + " Blocks ");
		}
	}

	public void activateSwitchSingleServer(long absoluteIndex) {
		SegmentPiece segmentPiece = getSegmentBuffer().getPointUnsave(absoluteIndex);
		if(segmentPiece != null) {
			activateSingleServer(!segmentPiece.isActive(), absoluteIndex);
		}
	}

	public void activateSingleServer(boolean activate, long absoluteIndex) {
		assert (isOnServer());
		long d;
		if(activate) {
			d = ElementCollection.getActivation(absoluteIndex, true, false);
		} else {
			d = ElementCollection.getDeactivation(absoluteIndex, true, false);
		}
		blockActivationBuffer.enqueue(d);
	}

	public void activateSurroundServer(boolean activate, Vector3i pos, short... signalTypesToActivate) {
		assert (isOnServer());
		for(int i = 0; i < 6; i++) {
			Vector3i n = new Vector3i(pos);
			n.add(Element.DIRECTIONSi[i]);
			SegmentPiece segmentPiece = getSegmentBuffer().getPointUnsave(n);
			if(segmentPiece != null) {
				for(int s = 0; s < signalTypesToActivate.length; s++) {
					if(segmentPiece.getType() == signalTypesToActivate[s]) {
						long d;
						if(activate) {
							d = ElementCollection.getActivation(segmentPiece.getAbsoluteIndex(), true, false);
						} else {
							d = ElementCollection.getDeactivation(segmentPiece.getAbsoluteIndex(), true, false);
						}
						blockActivationBuffer.enqueue(d);
//						break;
					}
				}
			}
		}
	}

	public boolean acivateConnectedSignalsServer(boolean activate, long controller3) {
		assert (isOnServer());
		Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> map = getControlElementMap().getControllingMap().get(controller3);
		boolean activated = false;
		if(map != null) {
			for(short s : ElementKeyMap.signalArray) {
				FastCopyLongOpenHashSet toMap = map.get(s);
				if(toMap != null) {
					for(long l : toMap) {
						long d;
						if(activate) {
							d = ElementCollection.getActivation(ElementCollection.getPosIndexFrom4(l), true, false);
						} else {
							d = ElementCollection.getDeactivation(ElementCollection.getPosIndexFrom4(l), true, false);
						}
						blockActivationBuffer.enqueue(d);
						activated = true;
					}
				}
			}
		}
		return activated;
	}

	/**
	 * @return the delayActivationBuffer
	 */
	public Long2ObjectOpenHashMap<DelayedAct> getDelayActivationBuffer() {
		return delayActivationBuffer;
	}

	/**
	 * @return the delayActivationBufferNonRepeating
	 */
	public Long2ObjectOpenHashMap<DelayedAct> getDelayActivationBufferNonRepeating() {
		return delayActivationBufferNonRepeating;
	}

	public void fromActivationStateTag(Tag t) {
		boolean loadedSome = false;
		assert ("a".equals(t.getName())) : t.getName();
		Tag[] vs = (Tag[]) t.getValue();
		Tag[] v0 = (Tag[]) vs[0].getValue();
		for(int i = 0; i < v0.length - 1; i++) {
			long pos = (Long) v0[i].getValue();
			if(isLoadedFromChunk16()) {
				pos = ElementCollection.shiftIndex4(pos, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
			}
			blockActivationBuffer.enqueue(pos);
			loadedSome = true;
		}
		Tag[] v1 = (Tag[]) vs[1].getValue();
		for(int i = 0; i < v1.length - 1; i++) {
			Tag[] k0 = (Tag[]) v1[i].getValue();
			long pos = (Long) k0[0].getValue();
			long time = (Long) k0[1].getValue();
			if(isLoadedFromChunk16()) {
				pos = ElementCollection.shiftIndex4(pos, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
			}
			delayActivationBuffer.put(ElementCollection.getPosIndexFrom4(pos), new DelayedAct(pos, time));
			loadedSome = true;
		}
		Tag[] v2 = (Tag[]) vs[2].getValue();
		for(int i = 0; i < v2.length - 1; i++) {
			Tag[] k0 = (Tag[]) v2[i].getValue();
			long pos = (Long) k0[0].getValue();
			long time = (Long) k0[1].getValue();
			if(isLoadedFromChunk16()) {
				pos = ElementCollection.shiftIndex4(pos, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_, Chunk16SegmentData.SHIFT_);
			}
			delayActivationBufferNonRepeating.put(ElementCollection.getPosIndexFrom4(pos), new DelayedAct(pos, time));
			loadedSome = true;
		}
		if(loadedSome) {
			System.err.println("[SERVER] " + this + " loaded activation state: ActBuffer " + blockActivationBuffer.size() + "; Delay " + delayActivationBuffer.size() + "; DelayNR " + delayActivationBufferNonRepeating.size());
		}
	}

	public Tag getActivationStateTag() {
		synchronized(blockActivationBuffer) {
			LongArrayList l = new LongArrayList();
			while(!blockActivationBuffer.isEmpty()) {
				l.add(blockActivationBuffer.dequeue());
			}
			for(int i = 0; i < l.size(); i++) {
				blockActivationBuffer.enqueue(l.get(i));
			}
			Tag bab0 = Tag.listToTagStruct(l, Type.LONG, null);
			Tag bab1 = listToTagStruct(delayActivationBuffer, null);
			Tag bab2 = listToTagStruct(delayActivationBufferNonRepeating, null);
			return new Tag(Type.STRUCT, "a", new Tag[]{bab0, bab1, bab2, FinishTag.INST});
		}
	}

	public Long2LongOpenHashMap getCooldownBlocks() {
		return cooldownBlocks;
	}

	@Override
	public void addListener(SendableSegmentProvider sendableSegmentProvider) {
		this.listeners.add(sendableSegmentProvider);
	}

	@Override
	public List<SendableSegmentProvider> getListeners() {
		return listeners;
	}

	public void onRename(String oldName, String newName) {
	}

	public void addFlagRemoveCachedTextBoxes(SendableSegmentProvider flagRemoveCachedTextBoxes) {
		synchronized(this.flagRemoveCachedTextBoxes) {
			this.flagRemoveCachedTextBoxes.enqueue(flagRemoveCachedTextBoxes);
		}
	}

	public void addSectorConfigProjection(Collection<ConfigProviderSource> to) {
		if(this instanceof ManagedSegmentController<?>) {
			((ManagedSegmentController<?>) this).getManagerContainer().getPowerInterface().addSectorConfigProjection(to);
		}
	}

	public boolean isNewPowerSystemNoReactorOverheatingCondition() {
		return false;
	}

	public boolean isNewPowerSystemNoReactor() {
		return false;
	}

	public long getReactorHpMax() {
		if(this instanceof ManagedSegmentController<?>) {
			return ((ManagedSegmentController<?>) this).getManagerContainer().getPowerInterface().getCurrentMaxHp();
		} else {
			return 1;
		}
	}

	public long getReactorHp() {
		if(this instanceof ManagedSegmentController<?>) {
			return ((ManagedSegmentController<?>) this).getManagerContainer().getPowerInterface().getCurrentHp();
		} else {
			return 1;
		}
	}

	@Override
	public abstract boolean isStatic();

	public int getLastModifierId() {
		return lastModifierId;
	}

	public void setLastModifierId(int lastModifierId) {
		this.lastModifierId = lastModifierId;
	}

	public boolean isLastModifierChanged() {
		return lastModifierChanged;
	}

	public void setLastModifierChanged(boolean lastModifierChanged) {
		this.lastModifierChanged = lastModifierChanged;
	}

	public BlockProcessor getBlockProcessor() {
		return blockProcessor;
	}

	public void onClear() {
		railController.onClear();
		fullyLoadedRailRecChache = false;
	}

	@Override
	public void sendAudioEvent(int id, int targetId, AudioArgument arg) {
		for(int i = 0; i < listeners.size(); i++) {
			if(listeners.get(i).isSendTo()) {
				listeners.get(i).getNetworkObject().sendAudioEvent(id, targetId, arg);
			}
		}
	}

	private static class BlockActiveReaction {

		public long fromActivation;
		boolean oldActive;
		boolean active;
		SegmentPiece block;
		long posIndex;
		long a;
		int counter = 0;

		@Override
		public int hashCode() {
			long v = block.getAbsoluteIndex();
			return (int) (v ^ (v >>> 32));
		}

		@Override
		public boolean equals(Object obj) {
			return this.block.getAbsoluteIndex() == ((BlockActiveReaction) obj).block.getAbsoluteIndex();
		}

		public void clear() {
			counter = 0;
			block = null;
		}
	}

	public static class SignalTracePool {

		private static ObjectArrayList<SignalTrace> pool = new ObjectArrayList<SignalTrace>();

		private static ObjectArrayList<SignalTrace> marked = new ObjectArrayList<SignalTrace>();

		public SignalTrace get() {
			if(pool.isEmpty()) {
				return new SignalTrace();
			} else {
				return pool.remove(pool.size() - 1);
			}
		}

		private void free(SignalTrace t) {
			t.reset();
			pool.add(t);
		}

		public void markFree(SignalTrace st) {
			marked.add(st);
		}

		public void freeMarked() {
			final int size = marked.size();
			for(int i = 0; i < size; i++) {
				free(marked.get(i));
			}
			marked.clear();
		}
	}

	public class SignalQueue extends ObjectArrayFIFOQueue<SignalTrace> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
	}

	private class DisplayReplace implements BlockLogicReplaceInterface {

		@Override
		public boolean isBlockNextToLogicOkTuUse(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
			return toReplace != null && ElementKeyMap.isTextBox(toReplace.getType());
		}

		@Override
		public void afterReplaceBlock(SegmentPiece fromBlockSurroundOriginal, SegmentPiece toReplace) {
			String copyText = getTextMap().get(fromBlockSurroundOriginal.getTextBlockIndex());
			if(copyText == null) {
				copyText = "[no data]";
			} else {
				String bef = getTextMap().get(toReplace.getTextBlockIndex());
				if(bef == null) {
					bef = "";
				}
				String add = "[add]";
				String del = "[del]";
				String replaceFirst = "[replacefirst]";
				String replaceAll = "[replaceall]";
				if(copyText.toLowerCase(Locale.ENGLISH).startsWith(add)) {
					copyText = StringTools.limit(bef + copyText.substring(add.length()), SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT);
				} else if(copyText.toLowerCase(Locale.ENGLISH).startsWith(del)) {
					try {
						int howMany = Integer.parseInt(copyText.substring(add.length()).trim());
						copyText = StringTools.limit(bef.substring(0, Math.max(0, bef.length() - howMany)), SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT);
					} catch(Exception e) {
						e.printStackTrace();
						sendControllingPlayersServerMessage(Lng.astr("Display block error at %s.\nSyntax error!", fromBlockSurroundOriginal.getAbsolutePos(new Vector3f())), ServerMessage.MESSAGE_TYPE_ERROR);
						copyText = bef;
					}
				} else if(copyText.toLowerCase(Locale.ENGLISH).startsWith(replaceFirst)) {
					try {
						String[] split = copyText.substring(replaceFirst.length()).trim().split("\\[[wW][iI][tT][hH]\\]");
						copyText = StringTools.limit(bef.replaceFirst(split[0], split[1]), SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT);
					} catch(Exception e) {
						e.printStackTrace();
						sendControllingPlayersServerMessage(Lng.astr("Display block error at %s.\nSyntax error!", fromBlockSurroundOriginal.getAbsolutePos(new Vector3f())), ServerMessage.MESSAGE_TYPE_ERROR);
						copyText = bef;
					}
				} else if(copyText.toLowerCase(Locale.ENGLISH).startsWith(replaceAll)) {
					try {
						String[] split = copyText.substring(replaceAll.length()).trim().split("\\[[wW][iI][tT][hH]\\]");
						copyText = StringTools.limit(bef.replaceAll(split[0], split[1]), SendableGameState.TEXT_BLOCK_LIMIT, SendableGameState.TEXT_BLOCK_LINE_LIMIT);
					} catch(Exception e) {
						e.printStackTrace();
						sendControllingPlayersServerMessage(Lng.astr("Display block error at %s.\nSyntax error!", fromBlockSurroundOriginal.getAbsolutePos(new Vector3f())), ServerMessage.MESSAGE_TYPE_ERROR);
						copyText = bef;
					}
				}
			}
			getTextMap().put(toReplace.getTextBlockIndex(), copyText);
			sendTextBlockServerUpdate(toReplace, copyText);
		}

		@Override
		public boolean fromBlockOk(SegmentPiece fromBlockSurround) {
			return ElementKeyMap.isTextBox(fromBlockSurround.getType());
		}

		@Override
		public boolean equalsBlockData(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
			return false;
		}

		@Override
		public void modifyReplacement(SegmentPiece fromBlockSurround, SegmentPiece toReplace) {
			fromBlockSurround.setOrientation(toReplace.getOrientation());
		}
	}

	private class ReplacementContainer {

		public boolean ok;
		SegmentPiece fromBlockSurround;
		List<SegmentPiece> toReplaceList = new ObjectArrayList<SegmentPiece>();
	}

	private class DelayedAct {

		long encode;

		long time;

		public DelayedAct(long encode, long time) {
			super();
			this.encode = encode;
			this.time = time;
		}
	}

	private class Writer implements SegmentBufferIteratorEmptyInterface {

		public boolean forcedTimestamp;

		private int writtenSegments;

		private boolean debug;

		public Writer(boolean debug) {
			this.debug = debug;
		}

		@Override
		public boolean handle(Segment s, long lastChanged) {
			try {
				if(forcedTimestamp) {
					// force write by setting last changed to current time
					lastChanged = getState().getUpdateTime();
				}
				boolean written = getSegmentProvider().getSegmentDataIO().write((RemoteSegment) s, lastChanged, false, debug);
				System.err.println("[SEFMENTWRITER] WRITING " + SendableSegmentController.this + " " + s.absPos + ": " + written);
				if(written) {
					writtenSegments++;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
			try {
				getSegmentProvider().getSegmentDataIO().writeEmpty(posX, posY, posZ, SendableSegmentController.this, lastChanged, false);
			} catch(IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}


}
