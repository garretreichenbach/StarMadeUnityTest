package org.schema.game.client.view.mainmenu;

import java.io.File;
import java.util.List;

import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LocalUniverse {

	
	public String name;
	public final long lastChanged;
	
	public LocalUniverse(String name, long created) {
		super();
		this.name = name;
		this.lastChanged = created;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return name.equals(((LocalUniverse)obj).name);
	}
	

	
	public static List<LocalUniverse> readUniverses(){
		List<LocalUniverse> l = new ObjectArrayList<LocalUniverse>();
		File sDir = new FileExt(GameServerState.SERVER_DATABASE);
		
		if(sDir.exists() && sDir.isDirectory()){
			File[] ff = sDir.listFiles();
			for(File f : ff){
				if(f.isDirectory() && !f.getName().startsWith(".") && !f.getName().equals("DATA") && !f.getName().equals("index")){
					l.add(new LocalUniverse(f.getName(), f.lastModified()));
				}
			}
			
		}
		
		return l;
	}
	
}
