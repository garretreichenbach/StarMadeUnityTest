package org.schema.schine.common.language;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.parsers.ParserConfigurationException;

import org.schema.schine.resource.FileExt;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * this class is generated. do NOT refactor or reorder any fields please
 * @author schema
 *
 */
public class Lng {
	public static void createMap(){}
	
	public static String getByIndex(int index) {
		return null;
	}
	
	public static final String str(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7){
		try {
			return String.format(s, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6){
		try {
			return String.format(s, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5){
		try {
			return String.format(s, arg0, arg1, arg2, arg3, arg4, arg5);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4){
		try {
			return String.format(s, arg0, arg1, arg2, arg3, arg4);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1, Object arg2, Object arg3){
		try {
			return String.format(s, arg0, arg1, arg2, arg3);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1, Object arg2){
		try {
			return String.format(s, arg0, arg1, arg2);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0, Object arg1){
		try {
			return String.format(s, arg0, arg1);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s, Object arg0){
		try {
			return String.format(s, arg0);
		}catch(Exception e) {
			e.printStackTrace();
			return "TRANSLATION ERROR ON '"+s+"'; PLEASE SEND IN REPORT FOR THAT LINE.";
		}
	}
	public static final String str(String s){
		return s;
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7){
		return new Object[]{s, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6){
		return new Object[]{s, arg0, arg1, arg2, arg3, arg4, arg5, arg6};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5){
		return new Object[]{s, arg0, arg1, arg2, arg3, arg4, arg5};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4){
		return new Object[]{s, arg0, arg1, arg2, arg3, arg4};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2, Object arg3){
		return new Object[]{s, arg0, arg1, arg2, arg3};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1, Object arg2){
		return new Object[]{s, arg0, arg1, arg2};
	}
	public static final Object[] astr(String s, Object arg0, Object arg1){
		return new Object[]{s, arg0, arg1};
	}
	public static final Object[] astr(String s, Object arg0){
		return new Object[]{s, arg0};
	}
	public static final Object[] astr(String s){
		return new Object[]{s};
	}
	
	public static void loadDefaultLanguage(){
		try {
			loadLanguage(null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Object2ObjectOpenHashMap<String, Translation> loadLanguage(String path) throws IOException{
		Object2ObjectOpenHashMap<String, Translation> params = new Object2ObjectOpenHashMap<String, Translation>();
		if(path != null ){
			File file = new FileExt(path);
			if(!file.exists()){
				throw new FileNotFoundException(file.getAbsolutePath());
			}
			try {
				LanguageReader.loadLangFile(file, params);
			} catch (SAXException e1) {
				e1.printStackTrace();
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			}
			
			try {
				setLang(params);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		return params;
	}

	private static void setLang(
			Object2ObjectOpenHashMap<String, Translation> params) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = Lng.class.getDeclaredFields();
		
		for(Field f : fields){
			
			LanguageVar l = f.getAnnotation(LanguageVar.class);
			
			if(l != null){
//				System.err.println("LF: "+f.getName()+" -> "+l.original());
				Translation translation = params.get(f.getName());
				
				String o = l.original().replaceAll("\\\\n", "\n").replaceAll("\\\"", "\"");
				
				if(translation != null){
					translation.original = translation.original.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%");
					translation.translation = translation.translation.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\%%", "%%");
					
					if(o.equals(translation.original)){
						
						f.set(Lng.class, translation.translation);
//						System.err.println("Language: SET: "+f.getName()+" -> "+f.get(Lng.class));
					}else{
						assert(false):"Not equals original\n\n"+o+"\n-------------\n"+translation.original;
						f.set(Lng.class, o);
					}
				}else{
					assert(false);
					f.set(Lng.class, o);
				}
			}
		}
		Lng.createMap();
		
		
		
		
	}
	

	
}
