package org.schema.schine.graphicsengine.forms.font;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.unicode.SlickException;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;
import org.schema.schine.graphicsengine.forms.font.unicode.glyph.effects.ColorEffect;
import org.schema.schine.graphicsengine.forms.font.unicode.glyph.effects.OutlineEffect;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.resource.FileExt;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class FontLibrary {
	public static final Object2ObjectOpenHashMap<String, Vector2f> offsetMap = new Object2ObjectOpenHashMap<String, Vector2f>();
	public static final Object2ObjectOpenHashMap<String, FontMetrics> metricsMap = new Object2ObjectOpenHashMap<String, FontMetrics>();
	
	
	private static UnicodeFont boldArial24;
	private static UnicodeFont boldArial12Green;
	private static UnicodeFont boldArial12White;
	private static UnicodeFont boldArial18;
	private static UnicodeFont boldArial18NoOutline;
	private static UnicodeFont arial14White;
	private static UnicodeFont boldArial15;
	private static UnicodeFont boldArial13White;
	private static UnicodeFont boldArial15White;
	private static UnicodeFont boldArial14;
	private static UnicodeFont regArial12White;
	private static UnicodeFont boldArial16White;
	private static UnicodeFont boldArial16WhiteNoOutline;
	private static UnicodeFont boldArial32;
	private static UnicodeFont regArial12WhiteUncached;
	private static UnicodeFont boldCourierNew12White;
	private static UnicodeFont boldArial14White;
	private static UnicodeFont boldArial12Blue;
	private static UnicodeFont boldArial20White;
	private static UnicodeFont boldArial20WhiteNoOutline;
	private static UnicodeFont boldArial10White;
	private static UnicodeFont boldBlenderProBook14;
	private static UnicodeFont boldBlenderProBook16;
	private static UnicodeFont boldBlenderProMedium16;
	private static UnicodeFont boldBlenderProMedium17;
	private static UnicodeFont boldBlenderProMedium15;
	private static UnicodeFont boldBlenderProMedium13;
	private static UnicodeFont boldBlenderProMedium14;
	private static UnicodeFont boldBlenderProHeavy20;
	private static UnicodeFont boldBlenderProMedium20;
	private static UnicodeFont boldBlenderProMedium22;
	private static UnicodeFont boldBlenderProMedium18;
	private static UnicodeFont boldBlenderProMedium19;
	private static UnicodeFont boldBlenderPro20;
	private static UnicodeFont boldBlenderProHeavy13;
	private static UnicodeFont boldBlenderProHeavy14;
	private static UnicodeFont boldBlenderProHeavy30;
	private static UnicodeFont boldBlenderProHeavy31;
	private static Font loadedDefaultFont;

	
	
	public static interface FontInterface{
		public UnicodeFont getFont();
		public FontInterface bigger();
		public FontInterface smaller();
		public int getBarTopDist();
	}
	
	private static interface FontFac{
		public FontInterface get();
	}
	private static interface FontUnicodeFac{
		public UnicodeFont get();
	}
	private static interface FontIntFac{
		public int get();
	}
	
	public enum FontSize implements FontInterface{
		TINY_11(11, 
				new FontFac() { @Override public FontInterface get() { return TINY_11; } },
				new FontFac() { @Override public FontInterface get() { return TINY_12; } }, () -> 8),
		TINY_12(12, 
				new FontFac() { @Override public FontInterface get() { return TINY_11; } },
				new FontFac() { @Override public FontInterface get() { return SMALL_13; } }, () -> 7),
		SMALL_13(13, 
				new FontFac() { @Override public FontInterface get() { return TINY_12; } },
				new FontFac() { @Override public FontInterface get() { return SMALL_14; } }, () -> 6),
		SMALL_14(14, 
				new FontFac() { @Override public FontInterface get() { return SMALL_13; } },
				new FontFac() { @Override public FontInterface get() { return SMALL_15; } }, () -> 5),
		SMALL_15(15, 
				new FontFac() { @Override public FontInterface get() { return SMALL_14; } },
				new FontFac() { @Override public FontInterface get() { return MEDIUM_15; } }, () -> 4),
		MEDIUM_15(16,
				new FontFac() { @Override public FontInterface get() { return SMALL_15; } },
				new FontFac() { @Override public FontInterface get() { return MEDIUM_16; } }, () -> 3),
		MEDIUM_16(17,
				new FontFac() { @Override public FontInterface get() { return MEDIUM_15; } },
				new FontFac() { @Override public FontInterface get() { return MEDIUM_18; } }, () -> 2),
		MEDIUM_18(18, 
				new FontFac() { @Override public FontInterface get() { return MEDIUM_16; } },
				new FontFac() { @Override public FontInterface get() { return MEDIUM_19; } }, () -> 1),
		MEDIUM_19(19, 
				new FontFac() { @Override public FontInterface get() { return MEDIUM_18; } },
				new FontFac() { @Override public FontInterface get() { return BIG_20; } }, () -> 1),
		BIG_20(20, 
				new FontFac() { @Override public FontInterface get() { return MEDIUM_19; } },
				new FontFac() { @Override public FontInterface get() { return BIG_24; } }, () -> 1),
		BIG_24(24, 
				new FontFac() { @Override public FontInterface get() { return BIG_20; } },
				new FontFac() { @Override public FontInterface get() { return BIG_30; } }, () -> 0),
		BIG_30(30, 
				new FontFac() { @Override public FontInterface get() { return BIG_24; } },
				new FontFac() { @Override public FontInterface get() { return BIG_30; } }, () -> 0),
		BIG_40(40,
				new FontFac() { @Override public FontInterface get() { return BIG_30; } },
				new FontFac() { @Override public FontInterface get() { return BIG_40; } }, () -> 0),
		BIG_60(60,
				new FontFac() { @Override public FontInterface get() { return BIG_40; } },
				new FontFac() { @Override public FontInterface get() { return BIG_60; } }, () -> 0),
		BIG_100(100,
				new FontFac() { @Override public FontInterface get() { return BIG_60; } },
				new FontFac() { @Override public FontInterface get() { return BIG_100; } }, () -> 0)
		;
		private final FontFac bigger;
		private final FontFac smaller;
		private final FontIntFac topDist;
		
		private UIScale currentScale;
		
		private UnicodeFont cachedFont;
		
		private final int unscaledSize;
		
		private FontSize(int size, FontFac smaller, FontFac bigger, FontIntFac topDist) {
			this.bigger = bigger;
			this.smaller = smaller;
			this.unscaledSize = size;
			this.topDist = topDist;
		}
		
		private void cache() {
			cachedFont = FontLibrary.deriveFont(cachedFont, UIScale.getUIScale().scale(unscaledSize), Font.PLAIN, 1,  new java.awt.Color(250,250,250));
		}
		public static void clearAll() {
			for(FontSize s : values()) {
				s.clear();
			}
		}
		public void clear() {
			if(cachedFont != null) {
				cachedFont.destroy();
			}
			cachedFont = null;
			for(FontInterface c : cache.values()) {
				c.getFont().destroy();
			}
			cache.clear();
			currentScale = UIScale.getUIScale();
		}
		
		public UnicodeFont getFont(){
			if(UIScale.getUIScale() != currentScale) {
				//UI scale changed. clean up fonts to recreate them
				clear();
				cache();
			}
			return cachedFont;
		}
		public FontInterface smaller() {
			return smaller.get();
		}
		public FontInterface bigger() {
			return bigger.get();
		}


		/**
		 * 
		 * @return distance to the top of bars the text is in (e.g. search bars)
		 */
		public int getBarTopDist() {
			return topDist.get();
		}
		

		public FontInterface getUnscaled() {
			return getStyle(Integer.MIN_VALUE);
		}


		public int getWidth(String string) {
			return getFont().getWidth(string);
		}

		public static void initialize() {
			for(FontSize s : values()) {
				s.currentScale = UIScale.getUIScale();
				s.cache();
			}
		}
		private final Int2ObjectOpenHashMap<FontInterface> cache = new Int2ObjectOpenHashMap<>();
		public FontInterface getStyle(int style) {
			
			FontInterface fontInterface = cache.get(style);
			if(fontInterface == null) {
				fontInterface = new FontInterface() {
					UnicodeFont cache;
					@Override
					public UnicodeFont getFont() {
						return (cache = FontLibrary.deriveFont(cache, 
								style == Integer.MIN_VALUE ? unscaledSize : UIScale.getUIScale().scale(unscaledSize),
									style == Integer.MIN_VALUE ? Font.PLAIN : style, 
									1,  new java.awt.Color(250,250,250)));
					}
		
					@Override
					public FontInterface bigger() {
						return bigger.get();
					}
		
					@Override
					public FontInterface smaller() {
						return smaller.get();
					}
		
					@Override
					public int getBarTopDist() {
						return topDist.get();
					}
				};
				cache.put(style, fontInterface);
			}
			return fontInterface;
		}

		public static FontInterface getUncached(final int size) {
			return new FontInterface() {
				UnicodeFont localCache;
				@Override
				public UnicodeFont getFont() {
					return (localCache = FontLibrary.deriveFont(localCache, UIScale.getUIScale().scale(size), Font.PLAIN, 1,  new java.awt.Color(250,250,250)));
				}
	
				@Override
				public FontInterface bigger() {
					throw new RuntimeException("Unsupported Function");
				}
	
				@Override
				public FontInterface smaller() {
					throw new RuntimeException("Unsupported Function");
				}
	
				@Override
				public int getBarTopDist() {
					return UIScale.getUIScale().scale(Math.max(0, 19 - size));
				}
			};
		}

		
	}
	
//	public static UnicodeFont getFont(FontInterface size){
//		if(smallFont == null){
//			tinyFont = FontLibrary.getRegularArial12WhiteWithoutOutline();
//			smallestFont = FontLibrary.getBlenderProMedium13();
//			smallFont = FontLibrary.getBlenderProMedium14();
//			smallMidFont = FontLibrary.getBlenderProMedium15();
//			mid16Font = FontLibrary.getBlenderProMedium16();
//			midFont = FontLibrary.getBlenderProMedium17();
//			bigFont =FontLibrary.getBlenderProMedium20();
//			
//			fonts = new UnicodeFont[]{
//					smallFont,
//					midFont,
//					bigFont
//				};
//		}
//		return fonts[size.ordinal()];
//	}

	public static boolean checkExistsFont(String s) {
		GraphicsEnvironment g = null;
		g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = g.getAvailableFontFamilyNames();
		for (int i = 0; i < fonts.length; i++) {
			if (fonts[i].equals(s)) {
				System.err.println("[FONT] FONT FOUND: "+s);
				return true;
			}
		}
		try{
			throw new IllegalArgumentException("FONT "+s+" not found!");
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	private static Graphics2D g;
	private static Font loadOrGetFont() {
		if (loadedDefaultFont == null) {
			try {
				File file = new FileExt(DataUtil.dataPath + FontPath.font_Path);
				
				File fontfile = new FileExt(FontPath.font_Path);
				
				System.err.println("[FONT] FONT PATH TO LOAD: "+FontPath.font_Path+"; "+fontfile.exists()+"; "+file.exists());
				
				if(fontfile.exists()){
					loadedDefaultFont = Font.createFont(Font.TRUETYPE_FONT, fontfile);
				}else if(file.exists()){
					System.err.println("[FONT] created from font file: "+file.getAbsolutePath());
					loadedDefaultFont = Font.createFont(Font.TRUETYPE_FONT, file);
				}else if(checkExistsFont(FontPath.font_Path)){
					System.err.println("[FONT] created from font name: "+FontPath.font_Path);
					loadedDefaultFont = new Font(FontPath.font_Path, Font.PLAIN, 20);
				}else{
					System.err.println("[FONT] file or name for font not found from "+FontPath.font_Path);
					loadedDefaultFont = Font.createFont(Font.TRUETYPE_FONT, new FileExt(DataUtil.dataPath + FontPath.font_Path_default));
				}
				
				
				if(g == null){
					BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
					g = image.createGraphics();
					g.create();
				}
				
				
				
				
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return loadedDefaultFont;
	}

	private static UnicodeFont getBlenderProMedium22() {
		
		return (boldBlenderProMedium22 = deriveFont(boldBlenderProMedium22, 20, new java.awt.Color(250,250,250)));
	}
	private static UnicodeFont getBlenderProMedium20() {
		
		return (boldBlenderProMedium20 = deriveFont(boldBlenderProMedium20, 18, new java.awt.Color(250,250,250)));
	}

	

	public static UnicodeFont deriveFont(UnicodeFont var, int size, java.awt.Color c){
		return deriveFont(var, size, Font.PLAIN, 1, c);
	}
	
	public static UnicodeFont deriveFont(UnicodeFont var, int size, int style , java.awt.Color c){
		return deriveFont(var, size, style, 1, c);
	}
	public static UnicodeFont deriveFont(UnicodeFont var, int size, int style, int outline, java.awt.Color c){
		if (var == null) {
			try {
				// Returned font is of pt size 1
				Font onePtFont = loadOrGetFont();

				// Derive and return a 12 pt version:
				// Need to use float otherwise
				// it would be interpreted as style

				Font awtFont = onePtFont.deriveFont(style, size);
				 
				FontMetrics fontMetrics = g.getFontMetrics(awtFont);
				
				
				var = new UnicodeFont(awtFont);
				if(outline > 0){
					var.getEffects().add(
							new OutlineEffect(outline, java.awt.Color.black));
				}
				var.getEffects().add(
						new ColorEffect(c));
				
				
//				var.addGlyphs(s, e);
				
				if(FontPath.chars != null){
					var.addGlyphs(FontPath.chars);
				}
				var.addAsciiGlyphs();

				
				
				int ext = Math.max(0, size - FontPath.offsetStartSize) / FontPath.offsetDividedBy;
				assert(offsetMap != null);
				assert(var != null);
				offsetMap.put(
						var.getFontFile()+
						var.getFont().getSize2D(), 
						new Vector2f(0, FontPath.offsetFixed + ext));
				metricsMap.put(var.getFontFile()+var.getFont().getSize2D(), fontMetrics);
				
				var.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}

		}
		return var;
	}
	public static FontMetrics getMetrics(UnicodeFont f){
		return metricsMap.get(f.getFontFile()+f.getFont().getSize2D());
	}
	private static UnicodeFont getBlenderProMedium18() {
		
		
		return (boldBlenderProMedium18 = deriveFont(boldBlenderProMedium18, 16, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium19() {
		
		return (boldBlenderProMedium19 = deriveFont(boldBlenderProMedium19, 17, new java.awt.Color(250,250,250)));
	}
	private static UnicodeFont getBOLDBlender20() {
		return (boldBlenderPro20 = deriveFont(boldBlenderPro20, 17, Font.BOLD, 0, new java.awt.Color(250,250,250)));
	}


	private static UnicodeFont getBlenderProHeavy20() {
		
		return (boldBlenderProHeavy20 = deriveFont(boldBlenderProHeavy20, 18, new java.awt.Color(250,250,250)));
	}
	private static UnicodeFont getBlenderProHeavy30() {
		
		return (boldBlenderProHeavy30 = deriveFont(boldBlenderProHeavy30, 28, new java.awt.Color(250,250,250)));
	}
	private static UnicodeFont getBlenderProHeavy31() {
		
		return (boldBlenderProHeavy31 = deriveFont(boldBlenderProHeavy31, 28, Font.PLAIN, 5, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProHeavy13() {
		
		return (boldBlenderProHeavy13 = deriveFont(boldBlenderProHeavy13, 11, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProHeavy14() {
		
		return (boldBlenderProHeavy14 = deriveFont(boldBlenderProHeavy14, 12, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProBook16() {
		return (boldBlenderProBook16 = deriveFont(boldBlenderProBook16, 14, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium16() {
		
		return (boldBlenderProMedium16 = deriveFont(boldBlenderProMedium16, 14, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium17() {
		return (boldBlenderProMedium17 = deriveFont(boldBlenderProMedium17, 15, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium14() {
		
		return (boldBlenderProMedium14 = deriveFont(boldBlenderProMedium14, 12, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium13() {
		
		return (boldBlenderProMedium13 = deriveFont(boldBlenderProMedium13, 11, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProMedium15() {
		
		return (boldBlenderProMedium15 = deriveFont(boldBlenderProMedium15, 13, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBlenderProBook14() {
		return (boldBlenderProBook14 = deriveFont(boldBlenderProBook14, 12, new java.awt.Color(250,250,250)));
	}

	private static UnicodeFont getBoldArial12Green() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial12White == null ? 
				(boldArial12White = deriveFont(boldArial12White, 12, Font.BOLD, java.awt.Color.green)) : 
					boldArial12White;
		}
		if (boldArial12Green == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 12);
			boldArial12Green = new UnicodeFont(awtFont);
			boldArial12Green.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial12Green.getEffects().add(new ColorEffect(java.awt.Color.green));
			boldArial12Green.addAsciiGlyphs();
			try {
				boldArial12Green.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial12Green;
	}

	private static UnicodeFont getBoldArial12Yellow() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial12Blue == null ? 
				(boldArial12Blue = deriveFont(boldArial12Blue, 12, Font.BOLD, java.awt.Color.yellow)) : 
					boldArial12Blue;
		}
		if (boldArial12Blue == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 12);
			boldArial12Blue = new UnicodeFont(awtFont);
			boldArial12Blue.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial12Blue.getEffects().add(new ColorEffect(java.awt.Color.yellow));
			boldArial12Blue.addAsciiGlyphs();
			try {
				boldArial12Blue.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial12Blue;
	}

	private static UnicodeFont getBoldArial12White() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial12White == null ? 
				(boldArial12White = deriveFont(boldArial12White, 12, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial12White;
		}
		if (boldArial12White == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 12);
			boldArial12White = new UnicodeFont(awtFont);
			boldArial12White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial12White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial12White.addAsciiGlyphs();
			try {
				boldArial12White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial12White;
	}

	private static UnicodeFont getBoldArial16White() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial16White == null ? 
				(boldArial16White = deriveFont(boldArial16White, 16, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial16White;
		}
		if (boldArial16White == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 16);
			boldArial16White = new UnicodeFont(awtFont);
			boldArial16White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial16White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial16White.addAsciiGlyphs();
			try {
				boldArial16White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial16White;
	}

	private static UnicodeFont getBoldArial16WhiteNoOutline() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial16WhiteNoOutline == null ? 
				(boldArial16WhiteNoOutline = deriveFont(boldArial16WhiteNoOutline, 16, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial16WhiteNoOutline;
		}
		if (boldArial16WhiteNoOutline == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 16);
			boldArial16WhiteNoOutline = new UnicodeFont(awtFont);
			boldArial16WhiteNoOutline.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial16WhiteNoOutline.addAsciiGlyphs();
			try {
				boldArial16WhiteNoOutline.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial16WhiteNoOutline;
	}

	private static UnicodeFont getBoldArial20White() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial20White == null ? 
				(boldArial20White = deriveFont(boldArial20White, 20, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial20White;
		}
		if (boldArial20White == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 20);
			boldArial20White = new UnicodeFont(awtFont);
			boldArial20White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial20White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial20White.addAsciiGlyphs();
			try {
				boldArial20White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial20White;
	}

	private static UnicodeFont getBoldArial20WhiteNoOutline() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial20WhiteNoOutline == null ? 
				(boldArial20WhiteNoOutline = deriveFont(boldArial20WhiteNoOutline, 20, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial20WhiteNoOutline;
		}
		if (boldArial20WhiteNoOutline == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 20);
			boldArial20WhiteNoOutline = new UnicodeFont(awtFont);
			boldArial20WhiteNoOutline.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial20WhiteNoOutline.addAsciiGlyphs();
			try {
				boldArial20WhiteNoOutline.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial20WhiteNoOutline;
	}

	private static UnicodeFont getBoldArial14White() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial14White == null ? 
				(boldArial14White = deriveFont(boldArial14White, 14, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial14White;
		}
		if (boldArial14White == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 14);
			boldArial14White = new UnicodeFont(awtFont);
			boldArial14White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial14White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial14White.addAsciiGlyphs();
			try {
				boldArial14White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial14White;
	}

	private static UnicodeFont getBoldArial18() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial18 == null ? 
				(boldArial18 = deriveFont(boldArial18, 18, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial18;
		}
		if (boldArial18 == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 18);
			boldArial18 = new UnicodeFont(awtFont);
			boldArial18.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial18.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial18.addAsciiGlyphs();
			try {
				boldArial18.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial18;
	}

	private static UnicodeFont getBoldArial18NoOutline() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial18NoOutline == null ? 
				(boldArial18NoOutline = deriveFont(boldArial18NoOutline, 18, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial18NoOutline;
		}
		if (boldArial18NoOutline == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 18);
			boldArial18NoOutline = new UnicodeFont(awtFont);
			boldArial18NoOutline.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial18NoOutline.addAsciiGlyphs();
			try {
				boldArial18NoOutline.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial18NoOutline;
	}

	private static UnicodeFont getBoldArial24() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial24 == null ? 
				(boldArial24 = deriveFont(boldArial24, 24, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial24;
		}
		if (boldArial24 == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 24);
			boldArial24 = new UnicodeFont(awtFont);
			boldArial24.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial24.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial24.addAsciiGlyphs();
			try {
				boldArial24.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial24;
	}

	private static UnicodeFont getBoldArial32() {

		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial32 == null ? 
				(boldArial32 = deriveFont(boldArial32, 32, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial32;
		}
		if (boldArial32 == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 32);
			boldArial32 = new UnicodeFont(awtFont);
			boldArial32.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial32.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial32.addAsciiGlyphs();
			try {
				boldArial32.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial32;
	}

	
	
	
	private static UnicodeFont getBoldArialGreen15() {
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial15 == null ? 
				(boldArial15 = deriveFont(boldArial15, 12, Font.BOLD, java.awt.Color.green.darker())) : 
					boldArial15;
		}
		
		if (boldArial15 == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 15);
			boldArial15 = new UnicodeFont(awtFont);
			boldArial15.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial15.getEffects().add(new ColorEffect(java.awt.Color.green.darker()));
			boldArial15.addAsciiGlyphs();
			try {
				boldArial15.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial15;
	}

	private static UnicodeFont getBoldArialWhite14() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial14 == null ? 
				(boldArial14 = deriveFont(boldArial32, 14, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial14;
		}
		if (boldArial14 == null) {
			Font awtFont = new Font("Arial", Font.BOLD, 14);
			boldArial14 = new UnicodeFont(awtFont);
			boldArial14.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial14.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial14.addAsciiGlyphs();
			try {
				boldArial14.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial14;
	}

//	public static UnicodeFont getBoldDotrice14() {
//
//		if (boldDotrice14 == null) {
//			try {
//				// Returned font is of pt size 1
//				Font onePtFont = Font.createFont(Font.TRUETYPE_FONT, new FileExt(//						DataUtil.dataPath + "/font/dotricebold.ttf"));
//
//				// Derive and return a 12 pt version:
//				// Need to use float otherwise
//				// it would be interpreted as style
//
//				Font awtFont = onePtFont.deriveFont(14f);
//
//				boldDotrice14 = new UnicodeFont(awtFont);
//				boldDotrice14.getEffects().add(
//						new OutlineEffect(4, java.awt.Color.black));
//				boldDotrice14.getEffects().add(
//						new ColorEffect(new java.awt.Color(250,250,250)));
//				boldDotrice14.addAsciiGlyphs();
//
//				boldDotrice14.loadGlyphs();
//			} catch (SlickException e1) {
//				e1.printStackTrace();
//			} catch (FontFormatException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//		return boldDotrice14;
//	}

//	public static UnicodeFont getBoldDotrice18() {
//
//		if (boldDotrice18 == null) {
//			try {
//				// Returned font is of pt size 1
//				Font onePtFont = Font.createFont(Font.TRUETYPE_FONT, new FileExt(//						DataUtil.dataPath + "/font/dotricebold.ttf"));
//
//				// Derive and return a 12 pt version:
//				// Need to use float otherwise
//				// it would be interpreted as style
//
//				Font awtFont = onePtFont.deriveFont(20f);
//
//				boldDotrice18 = new UnicodeFont(awtFont);
//				boldDotrice18.getEffects().add(
//						new OutlineEffect(4, java.awt.Color.black));
//				boldDotrice18.getEffects().add(
//						new ColorEffect(new java.awt.Color(250,250,250)));
//				boldDotrice18.addAsciiGlyphs();
//
//				boldDotrice18.loadGlyphs();
//			} catch (SlickException e1) {
//				e1.printStackTrace();
//			} catch (FontFormatException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//		return boldDotrice18;
//	}

	private static UnicodeFont getCourierNew12White() {
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldCourierNew12White == null ? 
				(boldCourierNew12White = deriveFont(boldCourierNew12White, 12, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldCourierNew12White;
		}
		
		if (boldCourierNew12White == null) {
			Font awtFont = new Font("Courier New", Font.BOLD, 12);
			boldCourierNew12White = new UnicodeFont(awtFont);
			boldCourierNew12White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldCourierNew12White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldCourierNew12White.addAsciiGlyphs();
			try {
				boldCourierNew12White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldCourierNew12White;
	}

	private static UnicodeFont getRegularArial11White() {
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					arial14White == null ? 
				(arial14White = deriveFont(arial14White, 14, Font.BOLD, new java.awt.Color(250,250,250))) : 
					arial14White;
		}
		
		if (arial14White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 11);
			arial14White = new UnicodeFont(awtFont);
			arial14White.getEffects().add(new OutlineEffect(2, java.awt.Color.black));
			arial14White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			arial14White.addAsciiGlyphs();
			try {
				arial14White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return arial14White;
	}



	private static UnicodeFont getRegularArial10White() {
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial10White == null ? 
				(boldArial10White = deriveFont(boldArial10White, 10, Font.BOLD, new java.awt.Color(250,250,250))) : 
					boldArial10White;
		}
		if (boldArial10White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 10);
			boldArial10White = new UnicodeFont(awtFont);
			boldArial10White.getEffects().add(new OutlineEffect(2, java.awt.Color.black));
			boldArial10White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial10White.addAsciiGlyphs();
			try {
				boldArial10White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial10White;
	}

	private static UnicodeFont getRegularArial12WhiteWithoutOutline() {
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
				regArial12White == null ? 
				(regArial12White = deriveFont(regArial12White, 12, Font.BOLD, new java.awt.Color(250,250,250))) : 
				regArial12White;
		}
		
		if (regArial12White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 12);
			regArial12White = new UnicodeFont(awtFont);
			regArial12White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			regArial12White.addAsciiGlyphs();
			try {
				regArial12White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return regArial12White;
	}

	private static UnicodeFont getRegularArial12WhiteWithoutOutlineUncached() {
		
		//for loading screen
		
		if (regArial12WhiteUncached == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 12);
			regArial12WhiteUncached = new UnicodeFont(awtFont);
			regArial12WhiteUncached.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			regArial12WhiteUncached.addAsciiGlyphs();
			try {
				regArial12WhiteUncached.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
			regArial12WhiteUncached.setDisplayListCaching(false);
		}
		return regArial12WhiteUncached;
	}

	private static UnicodeFont getRegularArial13White() {
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial13White == null ? 
				(boldArial13White = deriveFont(boldArial13White, 13, new java.awt.Color(250,250,250))) : 
					boldArial13White;
		}
		
		if (boldArial13White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 13);
			boldArial13White = new UnicodeFont(awtFont);
			boldArial13White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial13White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial13White.addAsciiGlyphs();
			try {
				boldArial13White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial13White;
	}

	private static UnicodeFont getRegularArial15White() {
		
		
		if(!FontPath.font_Path.equals(FontPath.font_Path_default)){
			return 
					boldArial15White == null ? 
				(boldArial15White = deriveFont(boldArial15White, 15, new java.awt.Color(250,250,250))) : 
					boldArial15White;
		}
		
		if (boldArial15White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 15);
			boldArial15White = new UnicodeFont(awtFont);
			boldArial15White.getEffects().add(new OutlineEffect(4, java.awt.Color.black));
			boldArial15White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial15White.addAsciiGlyphs();
			try {
				boldArial15White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial15White;
	}

	public static void initialize() {

		File dir = new FileExt("./data/fonts/");
		
		if(dir.exists()){
			File[] listFiles = dir.listFiles((dir1, name) -> name.toLowerCase(Locale.ENGLISH).contains("blenderpro") ||
					name.toLowerCase(Locale.ENGLISH).contains("dited") ||
					name.toLowerCase(Locale.ENGLISH).contains("dotrice") ||
					name.toLowerCase(Locale.ENGLISH).contains("segment14"));
			
			for(File f : listFiles){
				f.delete();
			}
		}
		
//		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
//			System.err.println("VIDEO MEMORY BEFORE LOADING FONTS");
//			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
//			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
//			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
//			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
//			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
//			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
//			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
//			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
//		}
		getRegularArial12WhiteWithoutOutlineUncached();
		getBoldArial12Green();
		getBoldArial12White();
		getBoldArialWhite14();
		getBoldArial24();
		getBoldArial18();
//		getBoldDotrice18();
//		getBoldDotrice14();
		getBoldArialGreen15();
		getRegularArial13White();
		getRegularArial15White();
		getBoldArial16White();
		getBoldArial32();
		getBoldArial20White();
		getRegularArial10White();
		getRegularArial12WhiteWithoutOutline();
		getRegularArial13White();
		getRegularArial15White();
		getRegularArial11White();
		GlUtil.printGlErrorCritical();
		getBlenderProBook14();
		getBlenderProBook16();
		GlUtil.printGlErrorCritical();
		getBlenderProMedium16();
		getBlenderProMedium15();
		getBlenderProMedium13();
		GlUtil.printGlErrorCritical();
		getBlenderProMedium14();
		getBlenderProMedium17();
		getBlenderProHeavy20();
		getBlenderProHeavy13();
		getBlenderProHeavy14();
		getBlenderProMedium20();
		getBlenderProMedium18();
		getBlenderProMedium19();
		GlUtil.printGlErrorCritical();
//		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
//			System.err.println("VIDEO MEMORY AFTER LOADING FONTS");
//			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
//			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
//			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
//			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
//			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
//			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
//			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
//			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
//		}
		GlUtil.printGlErrorCritical();
		
		FontSize.initialize();
//		getFont(FontSize.MEDIUM_15);
		//		UnicodeFont.info = true;
	}
	public static void purge() {
		System.err.println("[FONT] purging fonts");
		FontPath.chars = null;
		loadedDefaultFont = null;
		FontPath.font_Path = FontPath.font_Path_default;
		GUITextOverlay.defaultFont = null;
		FontSize.clearAll();
		Field[] fields = FontLibrary.class.getDeclaredFields();
		
		for(Field f : fields){
			if(f.getType() == UnicodeFont.class ){
				f.setAccessible(true);
				try {
					UnicodeFont o = (UnicodeFont) f.get(null);
					if(o != null){
						o.destroy();
					}
					f.set(null, null);
					System.err.println("[FONT] purged font: "+f.getName());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		GUITextOverlay.defaultFont = null;
	}
	public static boolean isDefaultFont() {
		return FontPath.font_Path_default.equals(FontPath.font_Path);
	}

	
}
