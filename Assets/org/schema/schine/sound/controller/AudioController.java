package org.schema.schine.sound.controller;

import api.common.GameClient;
import api.common.GameCommon;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.common.DelayedUpdateList;
import org.schema.schine.graphicsengine.camera.Camera;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.core.settings.EngineSettingsChangeListener;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.Identifiable;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.assignment.AudioAssignmentID;
import org.schema.schine.sound.controller.config.AudioConfiguration;
import org.schema.schine.sound.controller.config.AudioEntry;
import org.schema.schine.sound.controller.gui.AudioChangeListener;
import org.schema.schine.sound.controller.gui.AudioManagerFrame;
import org.schema.schine.sound.controller.mixer.AudioMixer;
import org.schema.schine.sound.loaders.AudioLoaderManager;
import org.schema.schine.sound.manager.engine.*;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglAL;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglALC;
import org.schema.schine.sound.manager.engine.lwjgl.LwjglEFX;
import org.schema.schine.sound.manager.engine.openal.ALAudioRenderer;
import org.xml.sax.SAXException;

import javax.sound.sampled.AudioFormat;
import javax.vecmath.Vector3f;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AudioController {
	private static final long AUDIO_SPAM_TIMEOUT = 1;
	private AudioConfiguration config;
	private AudioManagerFrame activeGUI;
	private AudioLoaderManager loadManager;
	private AudioListener defaultListener;
	private IndividualAudio currentTestAudioNode;
	private final DelayedUpdateList<AudioChangeListener> changeListeners = new DelayedUpdateList<>();
	private final List<AudioNode> playQueue = new ObjectArrayList<>();
	private AudioNode currentMusic;
	public final List<MusicControlListener> musicControlListener = new ObjectArrayList<>();
	private final List<AudioNode> playing = new ObjectArrayList<>();
	public static AudioController instance;
	private StateInterface state;
	private static long lastCall;
	private static int lastId;
	private static String lastUID;
	private final EngineSettingsChangeListener musicVolumeListener = setting -> {
		if(currentMusic != null)
			currentMusic.setVolume(Math.min(EngineSettings.AUDIO_MIXER_MUSIC.getFloat(), EngineSettings.AUDIO_MIXER_MASTER.getFloat()));
	};

	/**
	 * Starts playing a looped audio with the specified name and arguments.
	 * If an audio with the same name is already playing, it is stopped.
	 * The volume of the audio node is adjusted based on the assigned mixer and engine settings.
	 * The audio is queued for playback.
	 *
	 * @param name the name of the audio file (without extension)
	 * @param args the arguments to be passed to the audio
	 */
	public static void firedAudioLoopStart(String name, AudioArgument args) {
		instance.fireAudioLoopStart(name, args);
	}

	/**
	 * Stops the currently playing looped audio with the specified name.
	 *
	 * @param name the name of the audio file (without extension)
	 */
	public static void firedAudioLoopStop(String name) {
		instance.fireAudioLoopStop(name);
	}

	/**
	 * Starts playing a looped audio with the specified name and arguments.
	 * If an audio with the same name is already playing, it is stopped.
	 * The volume of the audio node is adjusted based on the assigned mixer and engine settings.
	 * The audio is queued for playback.
	 *
	 * @param name the name of the audio file (without extension)
	 * @param args the arguments to be passed to the audio
	 */
	private void fireAudioLoopStart(String name, AudioArgument args) {
		for(AudioAsset asset : config.assetManager.assets) {
			if(asset.getFile().getName().split("\\.")[0].equals(name)) {
				fireAudioLoopStop(name);
				try {
					AdvancedAudioNode node = getAdvancedNodeFromPool(asset, args); //Make loops quieter so they aren't annoying
					node.setVolume(Math.min(EngineSettings.AUDIO_MIXER_SFX_INGAME.getFloat() / 2.0f, EngineSettings.AUDIO_MIXER_MASTER.getFloat()));
					node.setLooping(true);
					queueAudio(node);
					addLoop(node);
					return;
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	private final ObjectArrayList<AdvancedAudioNode> loopRunners = new ObjectArrayList<>();
	private Thread loopThread;

	private void addLoop(AdvancedAudioNode node) {
		if(loopThread == null) {
			loopThread = new Thread(() -> {
				while(true) {
					for(AdvancedAudioNode loopRunner : loopRunners) {
						if(loopRunner.arg instanceof AudioArgumentEntity) {
							if(loopRunner.removed()) {
								loopRunner.stop();
								return;
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			loopThread.start();
		}
		loopRunners.add(node);
	}

	/**
	 * Stops playing a looped audio with the specified name.
	 *
	 * @param name the name of the audio file (without extension)
	 */
	private void fireAudioLoopStop(String name) {
		for(AudioAsset asset : config.assetManager.assets) {
			if(playing.isEmpty()) return;
			for(AudioNode node : playing) {
				if(node.getAsset().equals(asset)) {
					if(asset.getFile().getName().split("\\.")[0].equals(name)) node.queueStop();
				}
			}
		}
	}

	/**
	 * Executes the audio event with the given ID.
	 * Prevents audio spam by ignoring events with the same ID that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param id the ID of the audio event
	 */
	public static void fireAudioEventID(int id) {
		instance.firedAudioEventID(id);
	}


	/**
	 * Executes the audio event with the given ID and arguments.
	 * Prevents audio spam by ignoring events with the same ID that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param id   the ID of the audio event
	 * @param args the audio arguments for the event
	 */
	public static void fireAudioEventID(int id, AudioArgument args) {
		instance.firedAudioEventID(id, args);
	}

	/**
	 * Executes the audio event with the given ID and network ID.
	 * Prevents audio spam by ignoring events with the same ID that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param id        the ID of the audio event
	 * @param networkId the network ID of the audio event
	 */
	public static void fireAudioEventRemoteID(int id, int networkId) {
		instance.firedAudioEventRemoteID(id, networkId);
	}

	/**
	 * Executes the audio event with the given ID, network ID, and arguments.
	 * Prevents audio spam by ignoring events with the same ID that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param id        the ID of the audio event
	 * @param networkId the network ID of the audio event
	 * @param args      additional arguments for the audio event
	 */
	public static void fireAudioEventRemoteID(int id, int networkId, AudioArgument args) {
		instance.firedAudioEventRemoteID(id, networkId, args);
	}

	/**
	 * Executes the audio event with the given name and network ID.
	 * Prevents audio spam by ignoring events with the same name that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param name      the name of the audio event
	 * @param networkId the network ID of the audio event
	 */
	public static void fireAudioEventRemoteUID(String name, int networkId) {
		instance.firedAudioEventRemoteUID(name, networkId);
	}

	/**
	 * Executes the audio event with the given name and network ID, with additional audio parameter.
	 * Prevents audio spam by ignoring events with the same name that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param name      the name of the audio event
	 * @param networkId the network ID of the audio event
	 * @param param     the additional audio parameter
	 */
	public static void fireAudioEventRemoteUID(String name, int networkId, AudioParam param) {
		instance.firedAudioEventRemoteUID(name, networkId, param);
	}

	/**
	 * Fires the audio event with the given name and network ID, using the assigned audio entry.
	 *
	 * @param name      the name of the audio event
	 * @param networkId the network ID of the audio event
	 */
	private void firedAudioEventRemoteUID(String name, int networkId) {
		for(AudioEntry entry : config.entries.values()) {
			if(entry.assignmnetID.getAssignment() != null) {
				if(entry.assignmnetID.getAssignment().getAssetPrimary() != null) {
					String assetName = entry.assignmnetID.getAssignment().getAssetPrimary().name.split("\\.")[0];
					if(assetName.equals(name.toLowerCase(Locale.ENGLISH))) {
						firedAudioEventRemoteID(entry.id, networkId);
						return;
					}
				}
			}
		}
	}

	/**
	 * Fires the audio event with the given name and network ID, using the specified audio parameter.
	 * This method is called to execute the audio event and performs the following actions:
	 * <p>
	 * 1. Prevents audio spam by ignoring events with the same name that are fired too rapidly.
	 * 2. Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * 3. Handles queuing of the audio entry.
	 *
	 * @param name      the name of the audio event
	 * @param networkId the network ID of the audio event
	 * @param param     the additional audio parameter
	 */
	private void firedAudioEventRemoteUID(String name, int networkId, AudioParam param) {
		for(AudioEntry entry : config.entries.values()) {
			if(entry.assignmnetID.getAssignment() != null) {
				if(entry.assignmnetID.getAssignment().getAssetPrimary() != null) {
					String assetName = entry.assignmnetID.getAssignment().getAssetPrimary().name.split("\\.")[0];
					if(assetName.equals(name.toLowerCase(Locale.ENGLISH))) {
						entry.audioParam = param;
						firedAudioEventRemoteID(entry.id, networkId);
						return;
					}
				}
			}
		}
	}

	public AudioController() {
	}

	/**
	 * Initializes the OpenAL audio renderer and sets the default audio listener.
	 * This method must be called before using any OpenAL audio functionality.
	 * It performs the following actions:
	 * <p>
	 * 1. Creates an instance of the ALAudioRenderer with the specified implementation classes.
	 * 2. Initializes the ALAudioRenderer.
	 * 3. Sets the audio renderer as the active renderer for the AudioContext.
	 * 4. Creates a default audio listener and sets it as the listener for the audio renderer.
	 */
	public void initializeOpenAL() {
		ALAudioRenderer alAudioRenderer = new ALAudioRenderer(new LwjglAL(), new LwjglALC(), new LwjglEFX());
		alAudioRenderer.initialize();
		AudioContext.setAudioRenderer(alAudioRenderer);
		defaultListener = new AudioListenerDefault(); //use default listener when there is no other context (like camera)
		AudioContext.getAudioRenderer().setListener(defaultListener);
	}

	/**
	 * Checks if the given AudioEntry has any associated audio assignment.
	 *
	 * @param r The AudioEntry to check.
	 * @return True if the AudioEntry has an assignment of type other than NONE, false otherwise.
	 */
	public static boolean hasAudio(AudioEntry r) {
		return r.assignmnetID.getAssignment() != null && r.assignmnetID.getAssignment().getType() != AudioAssignmentID.AudioAssignmentType.NONE;
	}

	public static String describeAudioAssignment(AudioEntry entry) {
		return "";
	}

	public static void initialize() throws IOException {
		(new AudioController()).init();
	}

	private void init() throws IOException {
		try {
			loadManager = new AudioLoaderManager();
			config = AudioConfiguration.load();
			config.resolveAllEvents(this);
			config.sortAssets();
		} catch(SAXException | ParserConfigurationException e) {
			throw new IOException(e);
		}
		instance = this;
	}

	public void startGUI() {
		activeGUI = new AudioManagerFrame();
		activeGUI.setupAndShow();
		activeGUI.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				activeGUI = null;
			}
		});
	}

	/**
	 * Executes the audio event with the given ID.
	 * Performs necessary checks to prevent audio spam and adjusts volume based on settings.
	 *
	 * @param id the ID of the audio event
	 */
	private void firedAudioEventID(int id) {
		//Dont fire the same event too many times in a row to prevent audio spam
		long time = System.currentTimeMillis();
		if(id == lastId && time - lastCall < AUDIO_SPAM_TIMEOUT) {
//			System.err.println("[AUDIO] Audio spam detected, ignoring event " + id + "(" + (time - lastCall) + "ms < " + AUDIO_SPAM_TIMEOUT + "ms)");
			return;
		}
		lastId = id;
		lastCall = System.currentTimeMillis();
		handleGUI(id, -1, null);
		AudioAssignmentID aId = config.entries.get(id).assignmnetID;
		if(aId.getType() != AudioAssignmentID.AudioAssignmentType.NONE && aId.getAssignment().getAssetPrimary() != null) {
			assert (aId.getAssignment().getAssetPrimary().isLoaded());
			try {
				AudioNode defaultAudioNode = aId.getAssignment().getAssetPrimary().getDefaultAudioNode();
				if(defaultAudioNode == null) return;
				defaultAudioNode.setVolume(aId.getAssignment().getMixer().getVolume());
				((EngineSettings) aId.getAssignment().getMixer().getVolumeSetting()).addChangeListener(setting -> defaultAudioNode.setVolume(Math.min(setting.getFloat(), aId.getAssignment().getMixer().getVolume())));

				queueAudio(defaultAudioNode);
			} catch(AudioGUIException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Executes the audio event with the given ID and arguments.
	 * Prevents audio spam by ignoring events with the same ID that are fired too rapidly.
	 * Adjusts the volume of the audio node based on the assigned mixer and engine settings.
	 * Handles queuing of the audio entry.
	 *
	 * @param id   the ID of the audio event
	 * @param args the audio arguments for the event
	 */
	private void firedAudioEventID(int id, AudioArgument args) {
		//Dont fire the same event too many times in a row to prevent audio spam
		long time = System.currentTimeMillis();
		if(lastId == id && time - lastCall < AUDIO_SPAM_TIMEOUT) {
//			System.err.println("[AUDIO] Audio spam detected, ignoring event " + id + "(" + (time - lastCall) + "ms < " + AUDIO_SPAM_TIMEOUT + "ms)");
			return;
		}
		lastId = id;
		lastCall = System.currentTimeMillis();
		handleGUI(id, -1, args);
		AudioEntry audioEntry = config.entries.get(id);
		AudioAssignmentID aId = audioEntry.assignmnetID;
		if(aId.getType() != AudioAssignmentID.AudioAssignmentType.NONE && aId.getAssignment().getAssetPrimary() != null) {
			assert (aId.getAssignment().getAssetPrimary().isLoaded());
			AdvancedAudioNode node = getAdvancedNodeFromPool(aId.getAssignment().getAssetPrimary(), args);
			node.setVolume(aId.getAssignment().getMixer().getVolume());
			((EngineSettings) aId.getAssignment().getMixer().getVolumeSetting()).addChangeListener(setting -> node.setVolume(Math.min(setting.getFloat(), aId.getAssignment().getMixer().getVolume())));
			handleQueuing(audioEntry, node);
		}
	}

	/**
	 * EntityID -> EventId -> SubId
	 */
	private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<AudioNode>>> playingMap = new Int2ObjectOpenHashMap<>();

	/**
	 * Handles queuing of the audio entry.
	 *
	 * @param audioEntry the audio entry to be queued
	 * @param node       the audio node associated with the entry
	 */
	public void handleQueuing(AudioEntry audioEntry, AdvancedAudioNode node) {
		try {
			if(audioEntry.audioParam == AudioParam.ONE_TIME) {
				node.setLooping(false);
				queueAudio(node);
			} else if(audioEntry.audioParam == AudioParam.START) {
				int primId = node.arg.getPrimaryId();
				long secId = node.arg.getSubId();
				Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<AudioNode>> map = playingMap.get(primId);
				if(map == null) {
					map = new Int2ObjectOpenHashMap<>();
					playingMap.put(primId, map);
				}
				Long2ObjectOpenHashMap<AudioNode> subIdMap = map.get(audioEntry.id);
				if(subIdMap == null) {
					subIdMap = new Long2ObjectOpenHashMap<>();
					map.put(audioEntry.id, subIdMap);
				}
				AudioNode prev = subIdMap.put(secId, node);
				if(prev != null) prev.queueStop();
				node.setLooping(true);
				queueAudio(node);
			} else {
				assert (audioEntry.audioParam == AudioParam.STOP);
				int primId = node.arg.getPrimaryId();
				long secId = node.arg.getSubId();
				Int2ObjectOpenHashMap<Long2ObjectOpenHashMap<AudioNode>> map = playingMap.get(primId);
				if(map != null) {
					Long2ObjectOpenHashMap<AudioNode> subIdMap = map.get(audioEntry.id);
					if(subIdMap != null) {
						AudioNode audioNode = subIdMap.get(secId);
						if(audioNode != null) {
							audioNode.queueStop();
						}
					}
				}
			}
		} catch(AudioGUIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * AdvancedAudioNode class extends AudioNode and adds additional functionalities to handle audio assets.
	 */
	private class AdvancedAudioNode extends AudioNode {
		private AudioArgument arg;
		private final AudioAsset asset;

		public AdvancedAudioNode(AudioAsset asset) {
			super(asset.getData(), asset.getId());
			this.asset = asset;
		}

		public void assign(AudioArgument arg) {
			this.arg = arg;
			setPositional(this.arg.isPositional());
			setRefDistance(this.arg.getRefDistance());
			setMaxDistance(this.arg.getMaxDistance());
			setReverbEnabled(this.arg.isReverbEnabled());
			setReverbFilter(this.arg.getReverbFilter());
			setVelocityFromTranslation(true);
			this.arg.init();
		}

		@Override
		public AudioAsset getAsset() {
			return asset;
		}

		@Override
		public Vector3f getPosition() {
			return arg.getPos();
		}

		@Override
		public TransformTimed getWorldTransform() {
			return arg.getWorldTransform();
		}

		@Override
		public boolean isPlayInstanced() {
			return false;
		}

		public void reset() {
			arg = null;
		}

		@Override
		public void update(Timer timer) {
			arg.update(timer);
			super.update(timer);
		}

		private boolean removed() {
			if(arg instanceof AudioArgumentEntity argument) {
				if(argument.transformable instanceof SegmentController controller) {
					return !controller.existsInState() || !controller.getSegmentBuffer().existsPointUnsave(argument.absIndex) || (controller.getSegmentBuffer().getPointUnsave(argument.absIndex).getType() != argument.blockID && argument.blockID > 0);
				}
			}
			return false;
		}
	}

	private final Short2ObjectMap<List<AdvancedAudioNode>> advancedAudioNodePool = new Short2ObjectOpenHashMap<List<AudioController.AdvancedAudioNode>>();

	public AdvancedAudioNode getAdvancedNodeFromPool(AudioAsset asset, AudioArgument arg) {
		AdvancedAudioNode a;
		synchronized(advancedAudioNodePool) {
			List<AdvancedAudioNode> list = advancedAudioNodePool.get(asset.transientID);
			if(list == null) {
				list = new ObjectArrayList<>();
				advancedAudioNodePool.put(asset.transientID, list);
			}
			if(list.isEmpty()) {
				a = new AdvancedAudioNode(asset);
			} else {
				a = list.remove(list.size() - 1);
			}
		}
		a.assign(arg);
		return a;
	}

	/**
	 * Releases the given advanced audio node and adds it back to the pool.
	 * The node must be in a stopped state before it can be released.
	 * The released node will be reset and added to the appropriate pool based on its asset's transient ID.
	 *
	 * @param node the advanced audio node to be released
	 */
	public void freeAdvancedAudioNode(AdvancedAudioNode node) {
		synchronized(advancedAudioNodePool) {
			assert (node.getStatus() == AudioSource.Status.STOPPED);
			node.reset();
			List<AdvancedAudioNode> list = advancedAudioNodePool.get(node.asset.transientID);
			if(list == null) {
				list = new ObjectArrayList<>();
				advancedAudioNodePool.put(node.asset.transientID, list);
			}
			list.add(node);
		}
	}

	/**
	 * Adds a change listener to the list of change listeners.
	 * The change listener will be notified when a change occurs in the audio.
	 *
	 * @param c the change listener to be added
	 */
	public void addChangeListener(AudioChangeListener c) {
		changeListeners.addDelayed(c);
	}

	/**
	 * Removes a change listener from the list of change listeners.
	 * The change listener will no longer be notified when a change occurs in the audio.
	 *
	 * @param c the change listener to be removed
	 */
	public void removeChangeListener(AudioChangeListener c) {
		changeListeners.removeDelayed(c);
	}

	private void handleGUI(int id, int networkId, AudioArgument args) {
		if(activeGUI == null) activeGUI = new AudioManagerFrame();
		assert (config.entries.size() > 0) : "no audio events";
		assert (config.entries.containsKey(id)) : "ID NOT FOUND " + id + "\n" + config.printEntries();
		FiredAudioEvent e = new FiredAudioEvent(config.entries.get(id));
		e.stackTraceElements = Thread.currentThread().getStackTrace();
		e.argument = args;
		e.networkId = networkId;
		activeGUI.onFiredEvent(e);
	}

	/**
	 * Internal method that handles firing an audio event with the given ID and network ID.
	 * This method is responsible for notifying the change listeners and sending the audio event.
	 *
	 * @param id        the ID of the audio event
	 * @param networkId the network ID of the audio event
	 */
	private void firedAudioEventRemoteID(int id, int networkId) {
		handleGUI(id, networkId, null);
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(networkId);
		if(sendable instanceof AudioSender) ((AudioSender) sendable).sendAudioEvent(id, networkId, null);
		else assert (false) : id;
	}

	/**
	 * Internal method that handles firing an audio event with the given ID and network ID.
	 * This method is responsible for notifying the change listeners and sending the audio event.
	 *
	 * @param id        the ID of the audio event
	 * @param networkId the network ID of the audio event
	 * @param args      the optional audio argument
	 */
	private void firedAudioEventRemoteID(int id, int networkId, AudioArgument args) {
		handleGUI(id, networkId, args);
		Sendable sendable = getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(networkId);
		if(sendable instanceof AudioSender) ((AudioSender) sendable).sendAudioEvent(id, networkId, args);
		else assert (false) : id;
	}
	//	public static void fireAudioEventRemote(String UID, int toId, AudioTag[] audioTags, AudioParam param, AudioArgument args) {
	//		throw new RuntimeException("should not be called directly. Use preprocessor first");
	//	}
	//
	//	public static void fireAudioEventRemote(int toId, AudioTag... audioTags) {
	//		throw new RuntimeException("should not be called directly. Use preprocessor first");
	//	}
	//
	//	public static void fireAudioEvent(String UID, AudioTag[] audioTags, AudioParam param, AudioArgument args) {
	//		throw new RuntimeException("should not be called directly. Use preprocessor first");
	//	}

	/**
	 * Fires an audio event with the given UID and optional audio argument.
	 * This method searches for an audio asset with a matching UID, handles GUI updates,
	 * creates an advanced audio node from the asset and enqueues it for playback.
	 *
	 * @param UID  the UID of the audio event
	 * @param args the optional audio argument
	 */
	public static void fireAudioEvent(String UID, AudioArgument args) {
		for(AudioAsset asset : instance.config.assetManager.assets) {
			if(asset.getFile().getName().split("\\.")[0].equals(UID)) {
				long t = System.currentTimeMillis();
				if(UID.equals(lastUID) && t - lastCall < AUDIO_SPAM_TIMEOUT) {
					return;
				}
				lastUID = UID;
				lastCall = System.currentTimeMillis();
				instance.handleGUI(asset.transientID, -1, args);
				try {
					AdvancedAudioNode node = instance.getAdvancedNodeFromPool(asset, args);
					instance.queueAudio(node);
					return;
				} catch(Exception exception) {
					System.err.println("failed to play audio event " + UID + "\n" + exception.getMessage());
				}
			}
		}
		/*
		for(Map.Entry<Integer, AudioEntry> entry : instance.config.entries.entrySet()) {
			try {
				if(entry.getValue().assignmnetID.getAssignment().getAssetPrimary() != null) {
					String name = entry.getValue().assignmnetID.getAssignment().getAssetPrimary().getFile().getName().split("\\.")[0];
					if(name.equals(UID)) {
						instance.handleGUI(entry.getKey(), -1, args);
						try {
							entry.getValue().audioParam = param;
							instance.queueAudio(entry.getValue().assignmnetID.getAssignment().getAssetPrimary().getDefaultAudioNode());
							return;
						} catch(Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			} catch(NullPointerException ignored) {}
		}

		 */
	}

	/**
	 * Fires an audio event with the given audio tags.
	 * This method searches for audio entries with matching audio tags,
	 * handles GUI updates, and enqueues a random audio node from the matching entries for playback.
	 *
	 * @param audioTags the audio tags associated with the desired audio event
	 */
	public static void fireAudioEvent(AudioTag... audioTags) {
		//throw new RuntimeException("should not be called directly. Use preprocessor first");
		ObjectArrayList<AudioNode> nodes = new ObjectArrayList<>();
		for(Map.Entry<Integer, AudioEntry> entry : instance.config.entries.entrySet()) {
			if(entry.getValue().tags.containsAll(List.of(audioTags))) {
				AudioAssignmentID aId = entry.getValue().assignmnetID;
				instance.handleGUI(entry.getKey(), -1, null);
				if(aId.getType() != AudioAssignmentID.AudioAssignmentType.NONE && aId.getAssignment().getAssetPrimary() != null) {
					assert (aId.getAssignment().getAssetPrimary().isLoaded());
					try {
						AudioNode defaultAudioNode = aId.getAssignment().getAssetPrimary().getDefaultAudioNode();
						if(defaultAudioNode == null) return;
						defaultAudioNode.setVolume(aId.getAssignment().getMixer().getVolume());
						((EngineSettings) aId.getAssignment().getMixer().getVolumeSetting()).addChangeListener(setting -> defaultAudioNode.setVolume(Math.min(setting.getFloat(), aId.getAssignment().getMixer().getVolume())));
						nodes.add(defaultAudioNode);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(nodes.size() > 0) {
			AudioNode node = nodes.get((int) (Math.random() * nodes.size()));
			try {
				instance.queueAudio(node);
			} catch(AudioGUIException exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t     the transformable associated with the entity
	 * @param pos   the position of the entity
	 * @param subId the secondary ID of the entity
	 * @param min   the minimum range of the entity
	 * @param max   the maximum range of the entity
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, Transform pos, long subId, Vector3f min, Vector3f max) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position = pos;
		e.range.min.set(min);
		e.range.max.set(max);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		e.range.min.sub(halfSegment);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		e.secondaryId = subId;
		return e;
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t      the transformable associated with the entity
	 * @param pos    the position of the entity
	 * @param subId  the secondary ID of the entity
	 * @param radius the radius of the entity's range
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, Transform pos, long subId, float radius) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position.set(pos);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		e.range.min.set(-radius, -radius, -radius);
		e.range.min.sub(halfSegment);
		e.range.max.set(radius, radius, radius);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		e.secondaryId = subId;
		return e;
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t      the transformable associated with the entity
	 * @param pos    the position of the entity
	 * @param subId  the secondary ID of the entity
	 * @param radius the radius of the entity's range
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, Vector3f pos, long subId, float radius) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position.setIdentity();
		e.position.origin.set(pos);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		e.range.min.set(-radius, -radius, -radius);
		e.range.min.sub(halfSegment);
		e.range.max.set(radius, radius, radius);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		e.secondaryId = subId;
		return e;
	}

	public static AudioArgumentEntity ent(Transformable t, Vector3i pos, long subId, float radius) {
		return ent(t, pos.toVector3f(), subId, radius);
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t      the transformable associated with the entity
	 * @param pos    the position of the entity
	 * @param subId  the secondary ID of the entity
	 * @param radius the radius of the entity's range
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, SegmentPiece pos, long subId, float radius) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position.setIdentity();
		e.absIndex = pos.getAbsoluteIndex();
		pos.getAbsolutePos(e.position.origin);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		e.range.min.set(-radius, -radius, -radius);
		e.range.min.sub(halfSegment);
		e.range.max.set(radius, radius, radius);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		e.secondaryId = subId;
		return e;
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t     the transformable associated with the entity
	 * @param pos   the position of the entity
	 * @param subId the secondary ID of the entity
	 * @param min   the minimum range of the entity
	 * @param max   the maximum range of the entity
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, SegmentPiece pos, long subId, Vector3f min, Vector3f max) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position.setIdentity();
		pos.getAbsolutePos(e.position.origin);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		e.range.min.set(min);
		e.range.min.sub(halfSegment);
		e.range.max.set(max);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		e.secondaryId = subId;
		return e;
	}

	/**
	 * Creates an audio argument entity with the specified parameters.
	 *
	 * @param t   the transformable associated with the entity
	 * @param pos the position of the entity
	 * @param ec  the element collection containing the range of the entity
	 * @return the created audio argument entity
	 */
	public static AudioArgumentEntity ent(Transformable t, SegmentPiece pos, ElementCollection<?, ?, ?> ec) {
		AudioArgumentEntity e = new AudioArgumentEntity();
		e.transformable = t;
		e.position.setIdentity();
		pos.getAbsolutePos(e.position.origin);
		Vector3f halfSegment = new Vector3f(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF);
		ec.getMin(e.range.min);
		e.range.min.sub(halfSegment);
		ec.getMax(e.range.max);
		e.range.max.add(halfSegment);
		e.primaryId = t instanceof Identifiable ? ((Identifiable) t).getId() : 0;
		return e;
	}

	public AudioConfiguration getConfig() {
		return config;
	}

	public void playOnce(int eventId, String name, AudioAsset asset, AudioPlaySettings settings, AudioArgument arg) {
	}

	public void playStart(int eventId, String name, AudioAsset asset, AudioPlaySettings settings, AudioArgument arg) {
	}

	public void playStop(int eventId, String name, AudioAsset asset, AudioPlaySettings settings, AudioArgument arg) {
	}

	public void playVolume(int eventId, String name, AudioAsset asset, AudioPlaySettings settings, AudioArgument arg) {
	}

	public void playUpdate(int eventId, String name, AudioAsset asset, AudioPlaySettings settings, AudioArgument arg) {
	}

	public AudioLoaderManager getLoadManager() {
		return loadManager;
	}

	public void onSelectedAsset(AudioAsset a) {
		if(activeGUI != null) {
			activeGUI.onSelectedAsset(a);
		}
	}

	public void update(Timer timer) {
		changeListeners.synch();
		if(!playQueue.isEmpty()) {

			synchronized(playQueue) {
				for(AudioNode an : playQueue) {
					if(an.isPlayInstanced()) {
						an.playInstance();
					} else {
						an.play();
					}
					for(AudioChangeListener l : changeListeners) {
						l.onAudioPlay(an);
					}
					playing.add(an);
				}
				playQueue.clear();
			}
		}
		for(int i = 0; i < playing.size(); i++) {
			AudioNode an = playing.get(i);
			an.update(timer);
			if(an.getStatus() == AudioSource.Status.STOPPED) {
				an.onFinishedAfterStopped();
				for(AudioChangeListener l : changeListeners) l.onAudioStop(an);
				if(an instanceof MusicAudio) {
					for(MusicControlListener l : musicControlListener) l.onMusicStopped(an.getAsset());
				}
				playing.remove(i);
				i--;
			}
		}
		AudioContext.getAudioRenderer().update(timer.getDelta());
	}

	public void stopTestAudio() {
		if(currentTestAudioNode != null && currentTestAudioNode.getStatus() != AudioSource.Status.STOPPED) {
			currentTestAudioNode.queueStop();
		}
		if(isMusicPlaying()) currentMusic.play();
	}

	public AudioNode getMusicPlaying() {
		return currentMusic;
	}

	/**
	 * Plays the specified music audio asset.
	 *
	 * @param asset the audio asset to be played
	 * @return the currently playing music audio node
	 */
	public AudioNode playMusic(AudioAsset asset) {
		if(currentMusic != null) stopMusic();
		assert (currentMusic == null);
		currentMusic = new MusicAudio(asset);
		currentMusic.setVolume(EngineSettings.AUDIO_MIXER_MUSIC.getFloat());
		System.err.println("[AUDIO] [MUSIC] Now playing: " + asset.toString().replace(".ogg", ""));
		try {
			//playQueue.clear();
			queueAudio(currentMusic);
		} catch(AudioGUIException exception) {
			exception.printStackTrace();
			stopMusic();
		}
		return currentMusic;
	}

	public void queueAudio(AudioNode node) throws AudioGUIException {
		synchronized(playQueue) {
			playQueue.add(node);
		}
		//if(node.canPlay()) {
		//	synchronized(playQueue) {
		//		playQueue.add(node);
		//	}
		//}else {
		//throw new AudioGUIException("Can only play mono clips with spatial option!");
		//}
	}

	public AudioNode playTestAudio(AudioAsset asset, boolean loop, boolean spatial, boolean reverb, AudioEnvironment env) throws AudioGUIException {
		if(isMusicPlaying()) stopMusic();
		if(currentTestAudioNode == null) {
			try {
				asset.loadAudioIfNecessary(this);
				assert (asset.getData() != null);
				currentTestAudioNode = new IndividualAudio(asset, loop, spatial, reverb, env);
				queueAudio(currentTestAudioNode);
				return currentTestAudioNode;
			} catch(IOException e) {
				e.printStackTrace();
				currentTestAudioNode = null;
				return null;
			}
		} else {
			return null;
		}
	}

	public class MusicAudio extends AudioNode {
		final AudioAsset asset;
		final TransformTimed t = new TransformTimed();

		public MusicAudio(AudioAsset asset) {
			super(asset.getData(), asset.getId());
			assert (asset.getData() != null);
			this.asset = asset;
			t.setIdentity();
			setPositional(false);
			setVelocityFromTranslation(false);
			setReverbEnabled(false);
			setLooping(false);
		}

		@Override
		public AudioAsset getAsset() {
			return asset;
		}

		@Override
		public Vector3f getPosition() {
			return t.origin;
		}

		@Override
		public TransformTimed getWorldTransform() {
			return t;
		}

		@Override
		public void onFinishedAfterStopped() {
			super.onFinishedAfterStopped();
			stopMusic();
		}

		@Override
		public boolean isPlayInstanced() {
			return false;
		}
	}

	public static class IndividualAudio extends AudioNode {
		final AudioAsset asset;
		private final boolean loop;
		private final boolean spatial;
		private final boolean reverb;
		private final AudioEnvironment env;
		final TransformTimed t = new TransformTimed();
		float d = 0.5f;
		float dd;

		public IndividualAudio(AudioAsset asset, boolean loop, boolean spatial, boolean reverb, AudioEnvironment env) {
			super(asset.getData(), asset.getId());
			assert (asset.getData() != null);
			this.asset = asset;
			t.setIdentity();
			this.loop = loop;
			this.spatial = spatial;
			this.reverb = reverb;
			this.env = env;
			setPositional(spatial);
			setVelocityFromTranslation(spatial);
			setReverbEnabled(reverb); //depends on environment
			setLooping(loop);
		}

		@Override
		public boolean isPlayInstanced() {
			return false;
		}

		@Override
		public void onFinishedAfterStopped() {
			super.onFinishedAfterStopped();
			if(this == instance.currentTestAudioNode) {
				instance.currentTestAudioNode = null;
			}
		}

		public void update(Timer timer) {
			if(reverb) {
				AudioContext.getAudioRenderer().setEnvironment(env);
			}
			if(isPositional()) {
				t.origin.z += d;
				if(Math.abs(t.origin.z) > 30) {
					d = -d;
				}
				//			System.err.println("AudioNodePos: "+getWorldTransform().origin);
			}
			AudioContext.getAudioRenderer().update(0.05f);
			dd += timer.getDelta();
			super.update(timer);
		}

		@Override
		public TransformTimed getWorldTransform() {
			return t;
		}

		@Override
		public Vector3f getPosition() {
			return t.origin;
		}

		@Override
		public AudioAsset getAsset() {
			return asset;
		}
	}

	public void save() {
		try {
			config.save();
		} catch(ParserConfigurationException | IOException | TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops the currently playing music.
	 */
	public void stopMusic() {
		if(currentMusic != null) {
			//if(currentMusic.getStatus() != AudioSource.Status.STOPPED) currentMusic.queueStop();
			currentMusic.queueStop();
			currentMusic = null;
			try {
				if(GameClient.getClientState() != null && GameClient.getClientState().getGlobalGameControlManager() != null)
					GameClient.getClientState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().musicManager.stop();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public boolean isMusicPlaying() {
		return currentMusic != null;
	}

	public static void feedRaw(String name, byte[] samples) {
		throw new RuntimeException("need audio implementation");
		//		AudioContext.getAudioRenderer().playSource(src);
	}

	public static void rawDataStream(AudioFormat decodedFormat, boolean prio, String name, float x, float y, float z, int attModel, float distOrRoll) {
		throw new RuntimeException("need audio implementation");
	}

	public static void closeStream(String name) {
		throw new RuntimeException("need audio implementation");
	}

	public static void setListener(Camera camera) {
		AudioContext.getAudioRenderer().setListener(camera);
	}

	public void onRemoteEntryFired(RemoteAudioEntry remoteAudioEntry) {
		fireAudioEventID(remoteAudioEntry.audioId);
		/*
		if(((ClientState) getState()).canPlayAudioEntry(remoteAudioEntry)) {
			if(remoteAudioEntry.audioArgument != null) {
				fireAudioEventID(remoteAudioEntry.audioId, remoteAudioEntry.audioArgument);
			}else {
				fireAudioEventID(remoteAudioEntry.audioId);
			}
		}
		 */
	}

	public StateInterface getState() {
		if(state == null) state = GameCommon.getGameState().getState();
		return state;
	}

	public void setState(StateInterface state) {
		this.state = state;
	}
}
