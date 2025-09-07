package org.schema.game.client.view.mainmenu.gui;

import java.io.File;
import java.io.IOException;

public class GUIFileEntry implements Comparable<GUIFileEntry>{
	public final File file;
	
	public GUIFileEntry(File f) {
		this.file = f;
	}

	public String getName() {
		try {
			return isUpDir() ? ".." : (file.getName().length() == 0 ? file.getCanonicalPath() : file.getName());
		} catch (IOException e) {
			e.printStackTrace();
			return "IOEXCEPTION";
		}
	}
	
	public boolean isDirectory() {
		return isUpDir() || file.isDirectory();
	}

	public boolean isUpDir() {
		return file == null;
	}

	@Override
	public int compareTo(GUIFileEntry o) {
		if(isUpDir()) {
			return -1;
		}else if(o.isUpDir()) {
			return 1;
		}
		if(file.getName().length() == 0 && o.file.getName().length() > 0) {
			return -1;
		}else if(file.getName().length() > 0 && o.file.getName().length() == 0) {
			return 1;
		}
		if(isDirectory() && !o.isDirectory()) {
			return -1;
		}else if(!isDirectory() && o.isDirectory()) {
			return 1;
		}else {
			return getName().compareTo(o.getName());
		}
	}
}
