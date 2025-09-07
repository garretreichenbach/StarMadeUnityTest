package org.schema.schine.resource;

import java.io.IOException;

import org.schema.schine.graphicsengine.core.ResourceException;

public abstract class ResourceLoadEntry {
	public enum LoadEntryType{
		IMAGE,
		MESH,
		AUDIO,
		FONT,
		OTHER,
	}
	
	public abstract LoadEntryType getType();
	public String name;

	

	private long timeToLoad;

	public ResourceLoadEntry(String name){
		this.name = name;
	}
	/**
	 * loads even if it has already been loaded
	 * @param resourceLoader
	 * @throws ResourceException
	 * @throws IOException
	 */
	public final void loadForced(ResourceLoader resourceLoader) throws  IOException {
		resourceLoader.removeLoaded(this);
		if(canLoad()){
			load(resourceLoader);
		}
	}
	protected abstract void loadResource(ResourceLoader resourceLoader) throws  IOException;
		
	protected final void load(ResourceLoader resourceLoader) throws  IOException {
		long t = System.currentTimeMillis();
		
		loadResource(resourceLoader);
		resourceLoader.getResourceMap().resources.put(name, this);
		this.timeToLoad = System.currentTimeMillis()-t;
	}
//	{
//		switch (type) {
//			case ResourceLoadEntry.TYPE_MESH:
//				if(texture != null){
//					if(!ResourceLoader.dedicatedServer) {
//						texture.load(path);
//					}
//				}
//				resourceLoader.queueMesh(this);
//				break;
//			case ResourceLoadEntry.TYPE_SPRITE:
//				resourceLoader.queueSprite(this);
//				break;
//			case ResourceLoadEntry.TYPE_AUDIO:
//				assert(false):"implement";
//				break;
//			case ResourceLoadEntry.TYPE_TEXTURE_ADDITIONAL:
//				
//				break;
//			case ResourceLoadEntry.TYPE_FONT:
//				resourceLoader.getLoadList().add(0, "loading FONTS...");
//				FontLibrary.initialize();
//				break;
//			default:
//				throw new ResourceException("Unspecified resource type: " + type);
//		}
//		if(texture != null){
//			texture.load(path);
//		}
//		
//		resourceLoader.getResourceMap().resources.put(name, this);
//		
//		if (creature != null) {
//			for (PartType p : creature.partType) {
//				resourceLoader.getResourceMap().creatureMap.get(p).add(name);
//			}
//		}
//		
//		
//	}
	public abstract String getFilePath();
	@Override
	public String toString() {
		return "RESOURCE[" + getType() + "; " + name + ";]";
	}

	@Override
	public int hashCode() {
		return getType().ordinal() * 2342 + getUID().hashCode();
	}
	public String getUID(){
		return getFilePath()+";"+name;
	}
	@Override
	public boolean equals(Object obj) {
		return getType() == ((ResourceLoadEntry)obj).getType() && (getUID().equals(((ResourceLoadEntry)obj).getUID()));
	}

	public boolean canLoad() {
		return true;
	}
	public long getTimeToLoad() {
		return timeToLoad;
	}
	
	
}
