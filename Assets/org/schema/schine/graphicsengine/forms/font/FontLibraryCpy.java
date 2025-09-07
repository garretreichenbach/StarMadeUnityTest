package org.schema.schine.graphicsengine.forms.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.schema.common.util.data.DataUtil;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.forms.font.unicode.SlickException;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;
import org.schema.schine.graphicsengine.forms.font.unicode.glyph.effects.ColorEffect;
import org.schema.schine.graphicsengine.forms.font.unicode.glyph.effects.OutlineEffect;
import org.schema.schine.resource.FileExt;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

@SuppressWarnings("unchecked")
public class FontLibraryCpy {

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
	private static UnicodeFont boldArial8White;
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
	private static UnicodeFont boldBlenderProMedium18;
	private static UnicodeFont boldBlenderProMedium19;
	private static UnicodeFont boldBlenderProHeavy13;
	private static UnicodeFont boldBlenderProHeavy14;

	private static Font blenderProMedium;
//	public static final String font_Path_default = "font/Monda-Regular.ttf";
	public static final String font_Path_default = "Tahoma";
	public static String font_Path = font_Path_default;
	public static int offsetStartSize = 10;
	public static int offsetDividedBy = -3;
	public static int offsetFixed = -2;
	
	
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
	private static Font getBlenderProMedium() {
		if (blenderProMedium == null) {
			try {
				File file = new FileExt(DataUtil.dataPath + font_Path);
				if(file.exists()){
					System.err.println("[FONT] created from font file: "+file.getAbsolutePath());
					blenderProMedium = Font.createFont(Font.TRUETYPE_FONT, file);
				}else if(checkExistsFont(font_Path)){
					System.err.println("[FONT] created from font name: "+font_Path);
					blenderProMedium = new Font(font_Path, Font.PLAIN, 20);
				}else{
					System.err.println("[FONT] file or name for font not found from "+font_Path);
					blenderProMedium = Font.createFont(Font.TRUETYPE_FONT, new FileExt(DataUtil.dataPath + font_Path_default));
				}
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return blenderProMedium;
	}

	public static UnicodeFont getBlenderProMedium20() {
		
		return (boldBlenderProMedium20 = deriveFont(boldBlenderProMedium20, 18, new java.awt.Color(250,250,250)));
	}
	public static final int s = 0x4E00;
	public static final int e = 0x9FBF;
	public static UnicodeFont deriveFont(UnicodeFont var, int size, java.awt.Color c){
		if (var == null) {
			try {
				// Returned font is of pt size 1
				Font onePtFont = getBlenderProMedium();

				// Derive and return a 12 pt version:
				// Need to use float otherwise
				// it would be interpreted as style

				Font awtFont = onePtFont.deriveFont((float)size);
				 
				var = new UnicodeFont(awtFont);
				var.getEffects().add(
						new OutlineEffect(1, java.awt.Color.black));
				var.getEffects().add(
						new ColorEffect(c));
				
				
				var.addGlyphs(s, e);
				var.addAsciiGlyphs();

				
				
				int ext = Math.max(0, size - offsetStartSize) / offsetDividedBy;
				
				offsetMap.put(var.getFontFile()+var.getFont().getSize2D(), new Vector2f(0, offsetFixed + ext));
				
				var.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}

		}
		return var;
	}

	public static UnicodeFont getBlenderProMedium18() {
		
		
		return (boldBlenderProMedium18 = deriveFont(boldBlenderProMedium18, 16, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium19() {
		
		return (boldBlenderProMedium19 = deriveFont(boldBlenderProMedium19, 17, new java.awt.Color(250,250,250)));
	}

	public static final Object2ObjectOpenHashMap<String, Vector2f> offsetMap = new Object2ObjectOpenHashMap<String, Vector2f>();
	
	public static UnicodeFont getBlenderProHeavy20() {
		
		return (boldBlenderProHeavy20 = deriveFont(boldBlenderProHeavy20, 18, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProHeavy13() {
		
		return (boldBlenderProHeavy20 = deriveFont(boldBlenderProHeavy13, 11, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProHeavy14() {
		
		return (boldBlenderProHeavy14 = deriveFont(boldBlenderProHeavy14, 12, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProBook16() {
		return (boldBlenderProBook16 = deriveFont(boldBlenderProBook16, 14, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium16() {
		
		return (boldBlenderProMedium16 = deriveFont(boldBlenderProMedium16, 14, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium17() {
		return (boldBlenderProMedium17 = deriveFont(boldBlenderProMedium17, 15, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium14() {
		
		return (boldBlenderProMedium14 = deriveFont(boldBlenderProMedium14, 12, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium13() {
		
		return (boldBlenderProMedium13 = deriveFont(boldBlenderProMedium13, 11, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProMedium15() {
		
		return (boldBlenderProMedium15 = deriveFont(boldBlenderProMedium15, 13, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBlenderProBook14() {
		return (boldBlenderProBook14 = deriveFont(boldBlenderProBook14, 12, new java.awt.Color(250,250,250)));
	}

	public static UnicodeFont getBoldArial12Green() {
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

	public static UnicodeFont getBoldArial12Yellow() {
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

	public static UnicodeFont getBoldArial12White() {
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

	public static UnicodeFont getBoldArial16White() {
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

	public static UnicodeFont getBoldArial16WhiteNoOutline() {
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

	public static UnicodeFont getBoldArial20White() {
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

	public static UnicodeFont getBoldArial20WhiteNoOutline() {
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

	public static UnicodeFont getBoldArial14White() {
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

	public static UnicodeFont getBoldArial18() {
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

	public static UnicodeFont getBoldArial18NoOutline() {
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

	public static UnicodeFont getBoldArial24() {
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

	public static UnicodeFont getBoldArial32() {
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

	public static UnicodeFont getBoldArialGreen15() {
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

	public static UnicodeFont getBoldArialWhite14() {
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

	public static UnicodeFont getCourierNew12White() {
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

	public static UnicodeFont getRegularArial11White() {
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

	public static UnicodeFont getRegularArial8White() {
		if (boldArial8White == null) {
			Font awtFont = new Font("Arial", Font.PLAIN, 8);
			boldArial8White = new UnicodeFont(awtFont);
			boldArial8White.getEffects().add(new OutlineEffect(2, java.awt.Color.black));
			boldArial8White.getEffects().add(new ColorEffect(new java.awt.Color(250,250,250)));
			boldArial8White.addAsciiGlyphs();
			try {
				boldArial8White.loadGlyphs();
			} catch (SlickException e1) {
				e1.printStackTrace();
			}
		}
		return boldArial8White;
	}

	public static UnicodeFont getRegularArial10White() {
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

	public static UnicodeFont getRegularArial12WhiteWithoutOutline() {
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

	public static UnicodeFont getRegularArial12WhiteWithoutOutlineUncached() {
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

	public static UnicodeFont getRegularArial13White() {
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

	public static UnicodeFont getRegularArial15White() {
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
		
		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			System.err.println("VIDEO MEMORY BEFORE LOADING FONTS");
			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
		}
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
		getRegularArial8White();
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
		if (GraphicsContext.current.getCapabilities().GL_NVX_gpu_memory_info) {
			System.err.println("VIDEO MEMORY AFTER LOADING FONTS");
			int CURRENT_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
			System.err.println("CURRENT_AVAILABLE: " + (CURRENT_AVAILABLE / 1024) + "MB");
			int TOTAL_AVAILABLE = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
			System.err.println("TOTAL_AVAILABLE: " + (TOTAL_AVAILABLE / 1024) + "MB");
			int INFO_DEDICATED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX);
			System.err.println("INFO_DEDICATED: " + (INFO_DEDICATED / 1024) + "MB");
			int INFO_EVICTED = GL11.glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_EVICTED_MEMORY_NVX);
			System.err.println("INFO_EVICTED: " + (INFO_EVICTED / 1024) + "MB");
		}
		GlUtil.printGlErrorCritical();
		//		UnicodeFont.info = true;
	}
}
