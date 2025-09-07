package org.schema.schine.graphicsengine.movie.subtitles;

import java.io.File;
import java.util.Locale;

import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.resource.FileExt;

public class SubtitleFactory {

	
	
	
	public static AbstractSubtitleManager getManagerFromVideoFile(File movieFile){
		File dir = movieFile.getParentFile();
		if(dir != null){
			String nameOnly = movieFile.getName().substring(0, movieFile.getName().lastIndexOf("."));
			String lang = EngineSettings.LANGUAGE_PACK.getString();
			AbstractSubtitleManager sbv = null;
			if(!lang.toLowerCase(Locale.ENGLISH).equals("english")){
				File d = new File("./data/language/");
				
				for(File dd : d.listFiles()){
					if(dd.isDirectory() && dd.getName().toLowerCase(Locale.ENGLISH).equals(lang.toLowerCase(Locale.ENGLISH))){
						sbv = getSBV(dd, nameOnly);
						System.err.println("[CLIENT][MOVIE] Trying to use translated subtitle file from dir: "+dd.getAbsolutePath());
						break;
					}
				}
				
			}
			if(sbv == null){
				System.err.println("[CLIENT][MOVIE] Using default subtitles: "+dir.getAbsolutePath());
				sbv = getSBV(dir, nameOnly);
			}
			
			if(sbv != null){
				return sbv;
			}
			
		}
		return null;
	}
	
	
	private static AbstractSubtitleManager getSBV(File dir, String nameOnly){
		File fSBV = new FileExt(dir, nameOnly+".sbv");
		if(fSBV.exists()){
			try {
				return new SvbSubtitleManager(fSBV);
			} catch (SubtitleParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
