package org.schema.schine.resource;

import java.io.File;
import java.io.IOException;

public class ResourceLoadEntryImage extends ResourceLoadEntry{

	private File file;

	public ResourceLoadEntryImage(String name, File f) {
		super(name);
		this.file = f; 
	}

	public ResourceLoadEntryImage(File f) {
		this(fromFileName(f), f);
	}

	private static String fromFileName(File f) {
		StringBuffer pre = new StringBuffer();
		
		File parent = f.getParentFile();
		while(parent != null && !parent.getName().equals("image-resource")) {
			pre.insert(0, parent.getName()+"/");
			parent = parent.getParentFile();
		}
		
		String name = pre+f.getName().substring(0, f.getName().lastIndexOf(".png"));
		return name;
	}

	@Override
	public LoadEntryType getType() {
		return LoadEntryType.IMAGE;
	}

	@Override
	protected void loadResource(ResourceLoader resourceLoader) throws IOException {
		resourceLoader.imageLoader.loadImage(file.getCanonicalPath(), name);	
	}

	@Override
	public String getFilePath() {
		return file.getAbsolutePath();
	}

}
