package org.schema.schine.graphicsengine.forms.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class FontPath {
	public static final String font_Path_default = "font/Monda-Regular.ttf";
	public static String font_Path = font_Path_default;
	public static int offsetStartSize = 10;
	public static int offsetDividedBy = -3;
	public static int offsetFixed = -2;
	public static String chars;
	public static void addChars(IntOpenHashSet chars) {
		
		
		if(chars != null){
			StringBuffer sb = new StringBuffer(chars.size());
			for(int codePoint : chars){
				sb.appendCodePoint(codePoint);
			}
			FontPath.chars = sb.toString();
		}else{
			FontPath.chars = null;
		}
	}
}
