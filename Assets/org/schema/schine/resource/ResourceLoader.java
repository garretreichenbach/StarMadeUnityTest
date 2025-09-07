package org.schema.schine.resource;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.ParseException;
import org.schema.common.util.data.DataUtil;
import org.schema.common.util.data.ResourceUtil;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.graphicsengine.texture.DDSLoader;
import org.schema.schine.graphicsengine.texture.textureImp.Texture3D;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Loads all game resources given by the information in the mainConfig.xml
 *
 * @author schema
 */
public abstract class ResourceLoader {

	public static final int MODE_DEFAULT = 0;
	public static final int MODE_WITH_SPRITES = 1;
	public static final int MODE_FAST_LOAD = 2;
	public static ResourceUtil resourceUtil = new ResourceUtil();
	public static Texture3D explosionVolume;
	public static boolean dedicatedServer;
	public final List<ResourceLoadEntry> loadQueue = new ObjectArrayList<ResourceLoadEntry>();
	public final MeshLoader meshLoader;
	public final ImageLoader imageLoader;
	private final ResourceMap resourceMap;

	//INSERTED CODE

	public MeshLoader getMeshLoader() {
		return meshLoader;
	}

	///

	/**
	 * The one perc.
	 */
	float onePerc;
	private int mode;
	private int loadCount = 0;
	private String loadString = "";
	private float loadStatus;
	private boolean loaded = false;
	protected Set<ResourceLoadEntry> loadedDataEntries = new ObjectOpenHashSet<ResourceLoadEntry>();

	/**
	 * Instantiates a new resource loader.
	 *
	 * @param mode the mode
	 */
	public ResourceLoader(int mode) {
		this.mode = mode;
		resourceMap = new ResourceMap();
		imageLoader = new ImageLoader();
		meshLoader = new MeshLoader(resourceUtil);
	}



	public void loadModelDirectly(String modelName, String path, String filename) throws ResourceException, IOException{
		ResourceLoadEntry e = new ResourceLoadEntryMesh(modelName, new File(DataUtil.dataPath+path+filename));
		e.load(this);
		this.loadedDataEntries.add(e);
	}
	static void enqueueModel(Node root, List<ResourceLoadEntry> loadQueue) throws ResourceException, IOException {
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				String meshType = item.getNodeName();
				NamedNodeMap attributes = item.getAttributes();

				Node pathItem = attributes.getNamedItem("path");
				String path = pathItem.getNodeValue();

				NodeList modelItem = item.getChildNodes();

				for (int j = 0; j < modelItem.getLength(); j++) {
					Node model = modelItem.item(j);
					if (model.getNodeType() == Node.ELEMENT_NODE) {
						ResourceLoadEntry e = ResourceLoadEntryMesh.parseFromXML(model, path, meshType);
						assert( e != null);
						loadQueue.add(e);
					}
				}
			}
		}

	}

	/**
	 * Load ground.
	 *
	 * @throws ResourceException            the resource exception
	 * @throws ParseException               the parse exception
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws FileNotFoundException
	 */
	public static void loadModelConfig(List<ResourceLoadEntry> loadQueue)  {
		Document root;
		try {
			root = parseXML();


			NodeList childNodes = root.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				enqueueModel(item, loadQueue);
			}
		} catch (SAXException | IOException | ParserConfigurationException e ) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Parses the xml.
	 *
	 * @throws IOException
	 * @throws SAXException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 */
	public static Document parseXML() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(DataUtil.dataPath + File.separator + DataUtil.configPath));
		Document root = db.parse(bufferedInputStream);
		bufferedInputStream.close();
		return root;
	}



	/**
	 * @return the imageLoader
	 */
	public ImageLoader getImageLoader() {
		return imageLoader;
	}


	public float getLoadStatus() {
		return loadStatus;
	}
	public void setLoadStatus(int loadStatus) {
		this.loadStatus = loadStatus;
	}
	public String getLoadString() {
		return loadString;
	}

	public void setLoadString(String loadString) {
		this.loadString = loadString;
	}

	public Mesh getMesh(String spriteName) {
		Mesh i = meshLoader.getMeshMap().get(spriteName);
		//		assert(i != null):("Could not find mesh: " + spriteName+", "+meshMap);
		return i;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getRandomTipp() {
		return "UNDEFINED";
	}

	/**
	 * Gets the sprite.
	 *
	 * @param spriteName the sprite description
	 * @return the sprite
	 */
	public Sprite getSprite(String spriteName) {
		Sprite i = imageLoader.getSpriteMap().get(spriteName);
		if(i == null) i = imageLoader.getSpriteMap().get(UIScale.getUIScale().getGuiPath() + spriteName);
		assert (i != null) : ("Could not find sprites: " + spriteName);
		return i;
	}

	/**
	 * Checks if is loaded.
	 *
	 * @return true, if is loaded
	 */
	public boolean isLoaded() {

		return loaded;
	}

	/**
	 * Sets the loaded.
	 *
	 * @param loaded the loaded to set
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	public void enqueueFont(){
		ResourceLoadEntry font = new ResourceLoadEntryFont();
		synchronized(loadQueue){
			loadQueue.add(font);
			resetLoadCounts();
			loaded = false;
		}
	}
	
	public void enqueueAudio(AudioAsset.AudioGeneralTag tag){
		try {
			queueAudioResources(tag);
			resetLoadCounts();
			loaded = false;
		} catch (ResourceException e) {
			try {
				GLFrame.processErrorDialogException(e,  null);
			} catch (Exception es) {
				es.printStackTrace();
			}
			System.err.println("COULD NOT LOAD AUDIO PATH!!!!!!");
		}
	}

	public void enqueueModels(){
		int size = loadQueue.size();
		synchronized(loadQueue){
			loadModelConfig(loadQueue);
			resetLoadCounts();
			loaded = false;
		}
		System.err.println("[RESOURCE] ENQUEING 3D MODELS "+(size - loadQueue.size()));
	}

	public void enqueueCusom(){
		synchronized(loadQueue){
			try {

				loadQueue.addAll(loadCustom());
			} catch (ResourceException e) {

			}
			resetLoadCounts();
			loaded = false;
		}
	}
	public void loadAll() {
		System.err.println("[RESOURCE] EQUEUING ALL RESOURCES!");

		enqueueFont();
		assert(check(loadQueue));
		enqueueImageResources();
		assert(check(loadQueue));
		enqueueConfigResources("GuiConfig.xml", false);
		assert(check(loadQueue));
		enqueueAudio(AudioAsset.AudioGeneralTag.BASIC);
		enqueueAudio(AudioAsset.AudioGeneralTag.GAME);
		assert(check(loadQueue));
		enqueueModels();
		assert(check(loadQueue));
		enqueueCusom();
		assert(check(loadQueue));
//		resourceMap.init(loadQueue, true);

		resetLoadCounts();

	}
	public abstract void enqueueConfigResources(String guiConfigFileName, boolean reenqueu);
	public void enqueueImageResources(){
		queueImageResources();
		assert(check(loadQueue));
		resetLoadCounts();
		loaded = false;
	}
	public void enqueueWithResetForced(ResourceLoadEntry ... l){
		synchronized(loadQueue){
			for(ResourceLoadEntry r : l){
				loadedDataEntries.remove(r);
				loadQueue.add(r);
			}
			assert(check(loadQueue));
			resetLoadCounts();
			loaded = false;
		}
	}
	public void enqueueWithResetForced(Collection<ResourceLoadEntry> l){
		synchronized(loadQueue){
			for(ResourceLoadEntry r : l){
				loadedDataEntries.remove(r);
				loadQueue.add(r);
			}
			assert(check(loadQueue));
			resetLoadCounts();
			loaded = false;
		}
	}
	public void enqueueWithReset(ResourceLoadEntry ... l){
		synchronized(loadQueue){
			for(ResourceLoadEntry r : l){
				loadQueue.add(r);
			}
			assert(check(loadQueue));
			resetLoadCounts();
			loaded = false;
		}
	}
	private boolean check(Collection<ResourceLoadEntry> l){
		synchronized(l){
			for(ResourceLoadEntry e : l){
				if(e == null){
					return false;
				}
			}
			return true;
		}
	}
	public void enqueueWithReset(List<ResourceLoadEntry> l){
		synchronized(loadQueue){
			assert(check(l));
			loadQueue.addAll(l);
			resetLoadCounts();
			loaded = false;
		}
	}


	private void queueAudioResources(AudioAsset.AudioGeneralTag tag ) throws ResourceException {
		List<AudioAsset> list = AudioController.instance.getConfig().assetManager.assetsByGeneralTag.get(tag);
		System.out.println("[RESOURCE] queued Audio Assets to load for "+tag.name()+": "+list.size()+"; ");
		try {
			loadQueue.addAll(list);
		} catch(IndexOutOfBoundsException exception) {
			exception.printStackTrace();
		}
	}

	public List<? extends ResourceLoadEntry> loadCustom() throws ResourceException {
		List<ResourceLoadEntry> customLoadList = new ArrayList<ResourceLoadEntry>();
		customLoadList.add(new ResourceLoadEntry("Explosion") {


			@Override
			public LoadEntryType getType() {
				return LoadEntryType.OTHER;
			}

			@Override
			protected void loadResource(ResourceLoader resourceLoader) throws IOException {
				explosionVolume = new Texture3D();
				DDSLoader.load(new FileExt(getFilePath()), explosionVolume, false);
			}

			@Override
			public String getFilePath() {
				return DataUtil.dataPath + "effects/explosionMaps/explode.dds";
			}
		});
		return customLoadList;

	}
	public void resetLoadCounts(){
		synchronized(loadQueue){
			onePerc = 100f / Math.max(1, loadQueue.size());
			loadCount = 0;
		}
	}
	/**
	 * Load meshes.
	 *
	 * @throws ResourceException the resource exception
	 * @throws IOException
	 * @
	 */
	public void loadQueuedDataEntry() throws ResourceException, IOException {
		synchronized(loadQueue){
		if (!loadQueue.isEmpty()) {
				ResourceLoadEntry loadEntry;
				do{
					if(loadQueue.isEmpty()){
						loaded = true;
						return;
					}
					loadEntry = loadQueue.remove(0);
					if(loadEntry == null){
						System.err.println("ERROR! NULL load entry detected!");
						continue;
					}
				}while(loadEntry == null || (!loadEntry.canLoad() || 
						this.loadedDataEntries.contains(loadEntry)));
				

				try {
					loadEntry.load(this);
				}catch(IOException e) {
					throw new IOException("Error while loading Resource "+loadEntry.name+"; "+loadEntry.getFilePath(), e);
				}

				
				
				this.loadedDataEntries.add(loadEntry);
	
				loadCount++;
				loadStatus = loadCount * onePerc;
				// System.err.println(loadStatus);
				loadString = Lng.str("...loaded  %d%%", (int) loadStatus);
					
	
			} else {
				loaded = true;
			}
		}
	}

	private void queueImage(File f){

		if (f.getName().endsWith(".png")) {


			ResourceLoadEntryImage e = new ResourceLoadEntryImage(f);
			synchronized(loadQueue){
				loadQueue.add(e);
			}
		}else if(f.isDirectory() && !f.getName().equals("unused")){
			File[] files = f.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				queueImage(files[i]);
			}
		}
	}
	private void queueImageResources() {
		String path = DataUtil.dataPath + "/image-resource/";
		File f = new FileExt(path);
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			queueImage(files[i]);
		}
	}



	/**
	 * @return the resourceMap
	 */
	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	public void removeLoaded(ResourceLoadEntry resourceLoadEntry) {
		loadedDataEntries.remove(resourceLoadEntry);
	}

	public void onStopClient() {
		
	}

	public void forceLoadAll() {
		try {
			while (!loadQueue.isEmpty()) {
				loadQueuedDataEntry();
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadServer() throws FileNotFoundException, ResourceException, ParseException, SAXException, IOException, ParserConfigurationException {
	}

	public void loadClient() {
	}

}
