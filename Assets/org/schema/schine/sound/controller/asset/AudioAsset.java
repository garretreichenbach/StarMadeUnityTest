package org.schema.schine.sound.controller.asset;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.XMLSerializationInterface;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.network.objects.container.TransformTimed;
import org.schema.schine.resource.ResourceLoader;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.MusicTag;
import org.schema.schine.sound.controller.MusicTags;
import org.schema.schine.sound.loaders.AudioLoadEntry;
import org.schema.schine.sound.manager.engine.AudioData;
import org.schema.schine.sound.manager.engine.AudioId;
import org.schema.schine.sound.manager.engine.AudioNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AudioAsset extends AudioLoadEntry implements XMLSerializationInterface{
	
	
	public enum AudioGeneralTag{
		BASIC,
		GAME,
	}
	
	public final Set<MusicTag> musicTags = new ObjectOpenHashSet<>();
	public boolean isMusic() {
		return !musicTags.isEmpty();
	}
	
	
	public AudioGeneralTag generalTag = AudioGeneralTag.GAME;
	
	public enum AudioFileType{
//		MP3(".mp3"),
		WAV(".wav"),
		OGG(".ogg"),
		;
		
		private String ending;

		private AudioFileType(String ending) {
			this.ending = ending;
		}
		
		public boolean isType(File file) {
			return isType(file.getName());
		}
		public boolean isType(String file) {
			return file.toLowerCase(Locale.ENGLISH).endsWith(ending);
		}
	}
	public static class NotASoundFileException extends IOException{
		private static final long serialVersionUID = 1421464176703396908L;
		public NotASoundFileException(String f) {
			super(f);
		}
		
	}
	
	@Override
	public String toString() {
		return file.getName();
	}

	private AudioFileType fileType;
	private static final String audioResourceDirName = "audio-resource";
	private static final String audioPathRel = "/data/"+audioResourceDirName+"/";
	public static final String audioPath = "."+audioPathRel;
	private static final String TAG_HASH = "Hash";
	private static final String TAG_RELPATH = "Relpath";
	private static final String TAG_STREAM = "Stream";
	private static final String TAG_STREAM_CACHE = "StreamCache";
	private static final String TAG_GENERAL_VOLUME = "Volume";
	private static final String TAG_MUSIC_TAGS = "MusicTags";
	private static final String TAG_MUSIC_TAG = "Tag";
	private static final String TAG_ASSET_GENERAL_TAG = "General";
	
	private float volume = 1;
	private File file;
	private String hash;
	private AudioData data;
	private String relativePath;
	private AudioId audioId;
	private float musicPrio;
	private AudioNode defaultAudioNode;
	
	private class DefaultAssetAudioNode extends AudioNode{
		private TransformTimed t = new TransformTimed();
		
		public DefaultAssetAudioNode(AudioData audioData, AudioId audioKey) {
			super(audioData, audioKey);
			t.setIdentity();
			setPositional(false);
			setLooping(false);
		}

		@Override
		public AudioAsset getAsset() {
			return AudioAsset.this;
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
		public boolean isPlayInstanced() {
			return true;
		}

		
	}
	
    /**
     * Set audio parameter.
     * 
     * @param name Name of the asset
     * @param stream If true, the audio will be streamed from harddrive,
     * otherwise it will be buffered entirely and then played.
     * @param streamCache If stream is true, then this specifies if
     * the stream cache is used. When enabled, the audio stream will
     * be read entirely but not decoded, allowing features such as 
     * seeking, determining duration and looping.
     */
	public void setAudioParameters(String name, boolean stream, boolean streamCache) {
		audioId = new AudioId(name, stream, streamCache);
	}
	public void loadAudioIfNecessary(AudioController c) throws IOException {
		if(!isLoaded()) {
			loadAudio(c);
		}
	}
	public void loadAudio(AudioController c) throws IOException {
		if(audioId == null) {
			throw new IOException("Audio parameters not set (use setAudioParameters())");
		}
		data = c.getLoadManager().load(this);
		defaultAudioNode = new DefaultAssetAudioNode(data, audioId);
	}
	
	@Override
	protected void loadResource(ResourceLoader resourceLoader) throws IOException {
		loadAudioIfNecessary(AudioController.instance);
		assert(isLoaded());
	}
	public boolean isLoaded() {
		return data != null;
	}
	public AudioData getData() {
		return data;
	}
	
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
	public AudioAsset(File file) throws IOException {
		super(file.getAbsolutePath());
		associateNewFile(file);
	}
	public AudioAsset() {
		super("UNDEFINED_AUDIO_ASSET");
	}
	private String getRelativePath(File f) {
		File c = f;
		
		List<File> path = new ObjectArrayList<File>();
		
		while(!c.getName().equals("audio-resource")) {
			path.add(0, c);
			c = c.getParentFile();
		}
		
		StringBuffer rel = new StringBuffer();
		for(int i = 0; i < path.size(); i++) {
			rel.append(path.get(i).getName());
			if(i < path.size()-1) {
				rel.append("/");
			}
		}
		
		return rel.toString();
	}

	

	public void associateNewFile(File file) throws IOException {
		this.file = file;
		this.hash = FileUtil.getSha1Checksum(file);
		fileType = determineType(file);
		relativePath = getRelativePath(file);
		
		setAudioParameters(relativePath, false, false);
	}
	public void associateFileFromLoadedConfig() throws NotASoundFileException {
		assert(relativePath != null);
		this.file = new File(audioPath+relativePath);
		fileType = determineType(file);
		name = file.getAbsolutePath();
	}
	
	public boolean checkCurrentHash() throws IOException {
		return FileUtil.getSha1Checksum(file).equals(hash);
	}
	
	public AudioFileType determineType(File file) throws NotASoundFileException {
		return determineType(file.getName());
	}
	public AudioFileType determineType(String file) throws NotASoundFileException {
		
		assert(file != null);
		AudioFileType[] values = AudioFileType.values();
		for(int i = 0; i < values.length; i++) {
			if(values[i].isType(file)) {
				return values[i];
			}
		}
		System.err.println("NOT A SOUND FILE "+file);
		throw new NotASoundFileException(file);
	}

	public AudioFileType getFileType() {
		return fileType;
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public AudioId getId() {
		return audioId;
	}

	@Override
	public InputStream openStream() throws IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public void parseXML(Node root) {
		boolean streamed = false;
		boolean streamCached = false;
		NodeList childNodes = root.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if(item.getNodeType() == Element.ELEMENT_NODE) {
				switch(item.getNodeName()) {
					case TAG_HASH -> this.hash = item.getTextContent();
					case TAG_RELPATH -> this.relativePath = item.getTextContent();
					case TAG_GENERAL_VOLUME -> this.volume = Float.parseFloat(item.getTextContent());
					case TAG_STREAM -> streamed = Boolean.parseBoolean(item.getTextContent());
					case TAG_STREAM_CACHE -> streamCached = Boolean.parseBoolean(item.getTextContent());
					case TAG_ASSET_GENERAL_TAG -> generalTag = AudioGeneralTag.valueOf(item.getTextContent().toUpperCase(Locale.ENGLISH));
					case TAG_MUSIC_TAGS -> parseMusicTags(item);
				}
			}
		}
		assert(relativePath != null);
		setAudioParameters(relativePath, streamed, streamCached);
	}

	private void parseMusicTags(Node root) {
		NodeList cn = root.getChildNodes();
		for(int i = 0; i < cn.getLength(); i++) {
			Node item = cn.item(i);
			MusicTags tags = MusicTags.parseXMLStatic(item);
			if(tags != null) addMusicTag(tags);
		}
	}
	public void addMusicTag(MusicTags t) {
		musicTags.add(t);
		this.musicPrio = calcPrio();
	}
	public void removeMusicTag(MusicTags t) {
		musicTags.remove(t);
		this.musicPrio = calcPrio();
	}
	
	private float calcPrio() {
		if(musicTags.isEmpty()) {
			return 0;
		}
		
		float p = 0;
		for(MusicTag t : musicTags) {
			p += musicPrio;
		}
		p /= (float)musicTags.size();
		
		
		return p;
	}
	@Override
	public Node writeXML(Document doc, Node parent) {
		
		Element hashTag = doc.createElement(TAG_HASH);
		Element relpathTag = doc.createElement(TAG_RELPATH);
		Element generalVolumeTag = doc.createElement(TAG_GENERAL_VOLUME);
		Element streamTag = doc.createElement(TAG_STREAM);
		Element streamCacheTag = doc.createElement(TAG_STREAM_CACHE);
		Element musicTags = writeMusicTags(doc);
		Element generalTypeTag = doc.createElement(TAG_ASSET_GENERAL_TAG);
		
		hashTag.setTextContent(hash);
		relpathTag.setTextContent(relativePath);
		generalVolumeTag.setTextContent(String.valueOf(this.volume));
		streamTag.setTextContent(String.valueOf(this.audioId.isStream()));
		streamCacheTag.setTextContent(String.valueOf(this.audioId.isUseStreamCache()));
		generalTypeTag.setTextContent(generalTag.name());
		
		
		parent.appendChild(hashTag);
		parent.appendChild(relpathTag);
		parent.appendChild(generalTypeTag);
		parent.appendChild(generalVolumeTag);
		parent.appendChild(streamTag);
		parent.appendChild(streamCacheTag);
		parent.appendChild(musicTags);
		
		return parent;
	}

	private Element writeMusicTags(Document doc) {
		
		Element root = doc.createElement(TAG_MUSIC_TAGS);
		
		for(MusicTag t : musicTags) {
			t.writeXML(doc, root); //appended in method
		}
		
		return root;
	}
	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	@Override
	public int hashCode() {
		return hash.hashCode() + (relativePath.hashCode() * 39248092);
	}

	@Override
	public boolean canLoad() {
		return super.canLoad();
	}
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AudioAsset && ((AudioAsset)obj).hash.equals(hash) && ((AudioAsset)obj).relativePath.equals(relativePath);
	}

	public void updateWith(AudioAsset a) {
		this.relativePath = a.relativePath;
		setAudioParameters(relativePath, a.audioId.isStream(), a.audioId.isUseStreamCache());
	}

	public void moveAssetToPath(String path) {
		System.err.println("[AUDIO] MOVING FROM "+relativePath+" -> "+path+file.getName());
		
		File to = new File(audioPath+path+file.getName());
		
		if(file.getParentFile().equals(to.getParentFile())) {
			System.err.println("[AUDIO] Not moving file. Source and Target are the same");
			return;
		}
		try {
			FileUtil.copyFile(file, to);
			file.delete();
			associateNewFile(to);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public float getMusicPrioAvg() {
		return musicPrio;
	}
	@Override
	public String getFilePath() {
		return file.getAbsolutePath();
	}
	public AudioNode getDefaultAudioNode() {
		return defaultAudioNode;
	}
	
	
	
}
