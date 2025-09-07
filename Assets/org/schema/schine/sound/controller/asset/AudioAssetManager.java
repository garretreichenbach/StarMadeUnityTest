package org.schema.schine.sound.controller.asset;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.XMLSerializationInterface;
import org.schema.schine.sound.controller.config.AudioEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AudioAssetManager implements XMLSerializationInterface{
	public final List<AudioAsset> assets = new ObjectArrayList<>();
	public final Object2ObjectOpenHashMap<AudioAsset.AudioGeneralTag, List<AudioAsset>> assetsByGeneralTag = new Object2ObjectOpenHashMap<>();
	public final List<AudioAsset> musicAssets = new ObjectArrayList<>();
	public final Object2ObjectOpenHashMap<String, AudioAsset> assetsByPathLowerCase = new Object2ObjectOpenHashMap<>();
	public AudioAssetManager() {
		for(AudioAsset.AudioGeneralTag t : AudioAsset.AudioGeneralTag.values()) {
			assetsByGeneralTag.put(t, new ObjectArrayList<>());
		}
	}
	@Override
	public void parseXML(Node root) {

		for(int i = 0; i < root.getChildNodes().getLength(); i++) {
			Node item = root.getChildNodes().item(i);
			
			if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals(AudioEntry.TAG_ASSET)) {
				AudioAsset asset = new AudioAsset();
				asset.parseXML(item);
				try {
					asset.associateFileFromLoadedConfig();
					
					addAsset(asset);
					
				} catch (AudioAsset.NotASoundFileException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
	}
	
	public void addAsset(AudioAsset asset) {
		assets.add(asset);
		List<AudioAsset> list = assetsByGeneralTag.get(asset.generalTag);
		if(list == null) {
			list = new ObjectArrayList<>();
		}
		list.add(asset);
		
		if(asset.isMusic()) {
			musicAssets.add(asset);
		}
		assetsByPathLowerCase.put(asset.getRelativePath().toLowerCase(Locale.ENGLISH), asset);
	}
	private void removeAsset(AudioAsset asset) {
		assets.remove(asset);
		for(List<AudioAsset> a : assetsByGeneralTag.values()) {
			a.remove(asset);
		}
	}
	public void readAndCombineEntriesFromDir() throws IOException {
		Set<AudioAsset> assetSet = new ObjectOpenHashSet<AudioAsset>(assets);
		Object2ObjectOpenHashMap<String, AudioAsset> assetMap = new Object2ObjectOpenHashMap<>();
		for(AudioAsset s : assets) {
			assetMap.put(s.getHash(), s);
		}
		
		AudioAssetManager man = new AudioAssetManager();
		
		man.readFromFiles(new File(AudioAsset.audioPath));
		
		
		Set<AudioAsset> toRemoveSet = new ObjectOpenHashSet<AudioAsset>();
		for(AudioAsset a : man.assets) {
			//check if there is an existing entry. if so, nothing to do
			if(assetSet.remove(a)) {
				toRemoveSet.add(a);
			}
		}
		
		for(AudioAsset a : toRemoveSet) {
			man.removeAsset(a);
		}
		
		
		for(AudioAsset a : man.assets) {
			//new entries
			if(assetMap.containsKey(a.getHash())) {
				System.err.println("[AUDIOASSET][WARNING] found orphaned asset but has hash entry. updating... existing: "+assetMap.get(a.getHash()).getRelativePath()+"; new "+a.getRelativePath()+" with ");
				
				assetSet.remove(assetMap.get(a.getHash()));
				//entry changed path. update.
				assetMap.get(a.getHash()).updateWith(a);
				
				
				
			}else {
				//new entry
				addAsset(a);
			}
		}
		
		for(AudioAsset a : assetSet) {
			System.err.println("[AUDIOASSET][WARNING] found orphaned asset without a corresponding file. Removing asset entry "+a.getRelativePath());
			removeAsset(a);
		}
	}
	
	

	private void readFromFiles(File c) throws IOException {
		if(c.isDirectory()) {
			File[] listFiles = c.listFiles();
			for(File f : listFiles) {
				readFromFiles(f);
			}
		}else {
			AudioAsset a = new AudioAsset(c);
			addAsset(a);
		}
	}
	
	@Override
	public Node writeXML(Document doc, Node parent) {
		
		for(AudioAsset a : assets) {
			Element assetTag = doc.createElement(AudioEntry.TAG_ASSET);
			
			a.writeXML(doc, assetTag);
			
			parent.appendChild(assetTag);
		}
		
		return parent;
	}
	public class AudioAssetCat{
		public AudioAssetCat(String name) {
			this.name = name;
		}
		public String name;
		public AudioAsset asset;
		public List<AudioAssetCat> children = new ObjectArrayList<>();
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.hashCode();
			result = prime * result + ((asset == null) ? 0 : asset.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			
			return ((AudioAssetCat)obj).asset == asset && ((AudioAssetCat)obj).name.equals(name);
		}
		
		
		
	}
	
	private void insertCat(AudioAssetCat root, AudioAsset a) {
		File c = a.getFile();
		
		List<File> path = new ObjectArrayList<File>();
		
		while(!c.getName().equals("audio-resource")) {
			path.add(0, c);
			c = c.getParentFile();
		}
		
		AudioAssetCat current = root;
		for(File pe : path) {
			AudioAssetCat check = new AudioAssetCat(pe.getName());
			
			int indexOf = current.children.indexOf(check);
			
			AudioAssetCat audioAssetCat;
			if(indexOf >= 0) {
				audioAssetCat = current.children.get(indexOf);
			}else {
				audioAssetCat = check;
				current.children.add(audioAssetCat);
			}
			
			current = audioAssetCat;
			
			if(pe.getName().equals(a.getFile().getName())) {
				current.asset = a;
			}
		}
	}
	public AudioAssetCat buildHirachy() {
		AudioAssetCat root = new AudioAssetCat("Assets");
		for(AudioAsset a : assets) {
			insertCat(root, a);
		}
		return root;
	}
	public void sort() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
