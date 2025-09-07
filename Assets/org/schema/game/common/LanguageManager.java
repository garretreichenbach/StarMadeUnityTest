package org.schema.game.common;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.io.FileUtils;
import org.schema.game.client.controller.tutorial.TutorialMode;
import org.schema.game.client.view.gui.GUIHelpPanel;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.dialog.PlayerConversationManager;
import org.schema.game.common.version.VersionContainer;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translation;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.font.FontPath;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.resource.FileExt;

import java.io.File;
import java.io.IOException;

public class LanguageManager {
	
	public static final String DATA_LANG_PATH = "."+File.separator+"data"+File.separator+"language"+File.separator;
	public static final String USED_LANG_PATH =  "."+File.separator+"language"+File.separator;
	
	private static boolean init = false;
	
	
	public static void copyDefaultLanguages(){
		File defaultUsed = new FileExt(USED_LANG_PATH+"english"+File.separator+"pack.xml");
		File defaultLanguage = new FileExt(DATA_LANG_PATH+"defaultPack.xml");
		
		
		defaultUsed.getParentFile().mkdirs();
		if(defaultUsed.exists() && defaultLanguage.exists()){
			defaultUsed.delete();
		}
		if(defaultLanguage.exists()){
			try {
				FileUtils.copyFile(defaultLanguage, defaultUsed);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File englishInData = new FileExt(DATA_LANG_PATH+"english"+File.separator);
		if(englishInData.exists()){
			//should not exist
			try {
				FileUtils.deleteDirectory(englishInData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		File otherDefaultLanguagesDir = new FileExt(DATA_LANG_PATH);
		
		File[] langFolders = otherDefaultLanguagesDir.listFiles();
		
		for(File lang : langFolders){
			if(lang.isDirectory()){
				
				File otherLang = new FileExt(DATA_LANG_PATH+lang.getName()+File.separator+"pack.xml");
				if(otherLang.exists()){
					try {
						FileUtils.copyDirectory(lang, new FileExt(USED_LANG_PATH+lang.getName()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	private static String checkCurrentLanguage(String language){
		
		
		File langFile = new FileExt(USED_LANG_PATH+language+File.separator+"pack.xml");
		
		File defaultUsed = new FileExt(USED_LANG_PATH+"english"+File.separator+"pack.xml");
		if(defaultUsed.exists() && (language == null || !langFile.exists())){
			System.err.println("[LANGUAGE] Language pack not found. Using default (english)");
			language = "english";
		}
		
		return language;
	}
	private static void loadLanguageFolder(String languageFolderName){
		if(VersionContainer.build.equals("latest")){
			return;
		}
		
		try {
			String path = "."+File.separator+"language"+File.separator+languageFolderName+File.separator+"pack.xml";
			KeyboardMappings.read();
			Object2ObjectOpenHashMap<String, Translation> params = Lng.loadLanguage(path);
			
			
			
			IntOpenHashSet chars = new IntOpenHashSet();
			
			ElementKeyMap.nameTranslations.clear();
			ElementKeyMap.descriptionTranslations.clear();
			TutorialMode.translation.clear();
			GUIHelpPanel.translations.clear();
			PlayerConversationManager.translations.clear();
			//KeyboardMappings.translations.clear();
			
			for(Translation t : params.values()){
				for(int i = 0; i < t.translation.length(); i++){
					chars.add(t.translation.codePointAt(i));
				}
				
				
				/*if(t.var.startsWith("#CONTROL")){
					String[] s = t.var.split("_", 2);
					KeyboardMappings.translations.put(t.original, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
				}else */if(t.var.startsWith("#TUTSETTINGSTITLE")){
					String[] s = t.var.split("_", 2);
					GUIHelpPanel.translations.put(t.original, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
				}else if(t.var.startsWith("#TUTSETTINGSNAME")){
					String[] s = t.var.split("_", 2);
					GUIHelpPanel.translations.put(t.original, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
				}/*else if(t.var.startsWith("#CLIENTSETTING")){
					try{
						String[] s = t.var.split("_", 2);
						EngineSettings c = EngineSettings.valueOf(s[1]);
						if(c != null){
							EngineSettings.translations.put(c, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
						}
					}catch(Exception e){
						System.err.println("Couldn't load translation for "+t.var+"; Enum name probably changed. Retranslation necessary");
					}
				}else if(t.var.startsWith("#SERVERSETTING")){
					try{
						String[] s = t.var.split("_", 2);
						ServerConfig c = ServerConfig.valueOf(s[1]);
						if(c != null){
							ServerConfig.translations.put(c, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
						}
					}catch(Exception e){
						System.err.println("Couldn't load translation for "+t.var+"; Enum name probably changed. Retranslation necessary");
					}
				}*/else if(t.var.startsWith("#BLOCK")){
					String[] s = t.var.split("_", 3);
					short id = Short.parseShort(s[1]);
					if(s[2].equals("BLOCKNAME")){
//						ElementKeyMap.nameTranslations.put(id, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
					}else{
//						ElementKeyMap.descriptionTranslations.put(id, KeyboardMappings.formatText(t.translation).replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
					}
				}else if(t.var.startsWith("#TUTORIAL")){
					
					String[] v = t.var.substring("#TUTORIAL_".length()).split(";");
					String tutName = v[0].trim();
					String state = v[1].trim();
					
					Object2ObjectOpenHashMap<String, String> m = TutorialMode.translation.get(tutName);
					if(m == null){
						m = new Object2ObjectOpenHashMap();
						TutorialMode.translation.put(tutName, m);
					}
					m.put(state, t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
				}else if(t.var.startsWith("#LUA")){
					
					PlayerConversationManager.translations.put(t.original.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%").trim(), t.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%"));
				}
				
			}
			
			FontPath.addChars(chars);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void loadLanguage(String language, boolean server) {
		EngineSettings.LANGUAGE_PACK.setString(language);
		try {
			EngineSettings.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadCurrentLanguage(server);
	}
	public static void loadCurrentLanguage(boolean server) {
		String c = EngineSettings.LANGUAGE_PACK.getString();
		
		if(!init){
			GraphicsContext.IME = false;
			copyDefaultLanguages();
			if(c.equals("english") && !EngineSettings.LANGUAGE_PACK_ASSIGNED.isOn()){
				 
				String defLang = System.getProperty("user.language");
				if(defLang != null){
					System.err.println("[LANGUAGE] detected language: "+defLang);
					if(defLang.startsWith("de")){
						EngineSettings.LANGUAGE_PACK.setString("German");
					}else if(defLang.startsWith("es")){
						EngineSettings.LANGUAGE_PACK.setString("Spanish");
					}else if(defLang.startsWith("jp")){
						EngineSettings.LANGUAGE_PACK.setString("Japanese");
						GraphicsContext.IME = true;
					}else if(defLang.startsWith("fr")){
						EngineSettings.LANGUAGE_PACK.setString("French");
					}else if(defLang.startsWith("ru")){
						EngineSettings.LANGUAGE_PACK.setString("Russian");
					}else if(defLang.startsWith("pl")){
						EngineSettings.LANGUAGE_PACK.setString("Polish");
					}else if(defLang.startsWith("pt_BR")){
						EngineSettings.LANGUAGE_PACK.setString("Czech");
					}else if(defLang.startsWith("cs_CZ")){
						EngineSettings.LANGUAGE_PACK.setString("Portuguese Brazilian");
					}else if(defLang.startsWith("zh_HK") || defLang.startsWith("zh_TW")){
						EngineSettings.LANGUAGE_PACK.setString("Chinese Traditional");
						GraphicsContext.IME = true;
					}else if(defLang.startsWith("zh_CN") || defLang.startsWith("zh_SG")){
						EngineSettings.LANGUAGE_PACK.setString("Chinese Simplified");
						GraphicsContext.IME = true;
					}
					
					
					c = EngineSettings.LANGUAGE_PACK.getString();
				}
				
				EngineSettings.LANGUAGE_PACK_ASSIGNED.setOn(true);
				if(!server){
					try {
						EngineSettings.write();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			init = true;
		}
		
		String language = checkCurrentLanguage(c);
		System.err.println("[LANGUAGE] loading language: "+c+"; Resolved to "+language);
		loadLanguageFolder(language);		
	}
	
}
