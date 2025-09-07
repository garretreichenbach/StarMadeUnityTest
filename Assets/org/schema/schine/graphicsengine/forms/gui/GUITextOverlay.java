package org.schema.schine.graphicsengine.forms.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.font.unicode.Color;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont;
import org.schema.schine.graphicsengine.forms.font.unicode.UnicodeFont.DisplayList;
import org.schema.schine.input.InputState;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

public class GUITextOverlay extends GUIElement {

	private static final Object2ObjectOpenHashMap<UnicodeFont, int[]> fontWidths = new Object2ObjectOpenHashMap<UnicodeFont, int[]>();
	private static final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ?,;+*#'=)({}\\&%$�\"!@-_.:,;|~^";
	public static FontInterface defaultFont;
	private final ObjectArrayList<Object> textCache = new ObjectArrayList<Object>();
	private final ObjectArrayList<String> textCacheCache = new ObjectArrayList<String>();
	public boolean doDepthTest = false;
	public GUIResizableElement autoWrapOn;
	public boolean wrapSimple = true;
	public boolean autoHeight;
	public int limitTextWidth;
	public boolean debug;
	long dirtyTime = System.currentTimeMillis();
	//	private UnicodeFont font;
	private boolean firstDraw = true;
	private List<Object> text;
	private Color color = new Color(Color.white);
	private int limitTextDraw = -1;
	private boolean beginTextAtLast;
	private boolean blend = true;
	private int clipStartPX = -1;
	private int clipEndPX = -1;
	private boolean useUncachedDefaultFont;
	private boolean dirty;
	private int lastWrapWidth;
	private int maxLineWidth;
	private int textHeight;
	private String limstr = "";
	private int limAmount = 3;
	private String origStr;
	private boolean wasCacheDirty;
	private FontInterface fontSize;

	public GUITextOverlay(InputState state) {
		super(state);
	}

	public GUITextOverlay(FontInterface font, InputState state) {
		this(state);
		this.fontSize = font;
	}

	public GUITextOverlay(FontInterface font, Color color, InputState state) {
		this(font, state);
		this.setColor(color);
	}

	private int[] createFontTable(UnicodeFont font) {
		int max = 0;
		for(int i = 0; i < itoa64.length(); i++) {
			max = Math.max(max, itoa64.charAt(i));
		}
		int[] h = new int[max + 1];
		for(int i = 0; i < itoa64.length(); i++) {

			h[itoa64.charAt(i)] = getWidthOfFont(String.valueOf(itoa64.charAt(i)));
		}
		return h;
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public String generateToolTip() {
		if(origStr != null) {
			return origStr;
		}
		return text.get(0).toString();
	}

	@Override
	public void draw() {

		if(isRenderable()) {
			drawText();
		}
		if(limitTextWidth > 0) {
			if(origStr == null) {
				origStr = text.get(0).toString();
			}
			while(maxLineWidth > limitTextWidth) {
				limAmount++;
				limstr = origStr.substring(0, origStr.length() - limAmount) + "...";
				setTextSimple(limstr);
				updateTextSize();
			}
		}

	}

	@Override
	public void onInit() {
		if(!firstDraw) {
			return;
		}
		if(fontSize == null) {
			if(defaultFont == null) {
				defaultFont = FontSize.TINY_12;
				//				defaultFont.getEffects().add(new ColorEffect(java.awt.Color.white));
				//				defaultFont.addAsciiGlyphs();
				//				try {
				//					defaultFont.loadGlyphs();
				//				} catch (SlickException e1) {
				//					e1.printStackTrace();
				//				}
			}
			if(useUncachedDefaultFont) {
				fontSize = FontSize.getUncached(12);
			} else {
				fontSize = defaultFont;
			}
		}
		fontSize.getFont().setDisplayListCaching(true);

		if(text == null) {
			text = new ObjectArrayList<Object>();
		}

		firstDraw = false;

	}

	@Override
	protected void doOrientation() {

	}

	@Override
	public float getHeight() {
		return textHeight;
	}

	@Override
	public float getWidth() {
		return 1;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	private void addCache(final Object a, final ColoredInterface c) {
		wasCacheDirty = true;
		if(c == null) {
			textCache.add(a);
		} else {
			textCache.add(new ColoredInterface() {
				@Override
				public Vector4f getColor() {
					return c.getColor();
				}

				/* (non-Javadoc)
				 * @see java.lang.Object#toString()
				 */
				@Override
				public String toString() {
					return a.toString();
				}

			});
		}
	}

	public void updateCacheForced() {
		createCache(true);
	}

	private void createCache(boolean forced) {
		if(text == null) {
			text = new ObjectArrayList<Object>();
			text.add("NULLOBJECT");
		}
		for(int j = 0; j < text.size(); j++) {
			if(text.get(j) == null) {
				text.set(j, "NULL");
			}
		}

		dirty = false;
		if(forced || (autoWrapOn != null && lastWrapWidth != (int) autoWrapOn.getWidth())) {
			//always dirty when width changed of wrapping dependency
			if(wrapSimple) {
				dirty = true;
			} else {
				dirtyTime = System.currentTimeMillis();
			}
		} else if(text.size() == textCacheCache.size()) {
			for(int j = 0; j < text.size(); j++) {

				if(!text.get(j).toString().equals(textCacheCache.get(j))) {
					dirty = true;
					break;
				}
			}
		} else {
			dirty = true;
		}
		if(dirtyTime > 0 && System.currentTimeMillis() - dirtyTime > 40) {
			dirty = true;
			dirtyTime = 0;
		}
		lastWrapWidth = (int) (autoWrapOn != null ? autoWrapOn.getWidth() : 0);
		if(dirty) {
			textCache.clear();
			boolean containsNL = false;
			textCacheCache.clear();
			for(int j = 0; j < text.size(); j++) {
				textCacheCache.add(text.get(j).toString());
				int i;

				if(beginTextAtLast) {
					i = (text.size() - 1) - j;
				} else {
					i = j;
				}

				String s = text.get(i).toString();
				if(autoWrapOn != null) {
					if(s.contains("\n")) {
						StringBuffer n = new StringBuffer();
						String t = s;
						int index = 0;
						int previousIndex = 0;
						while((index = t.indexOf("\n", index)) >= 0) {
							if(index == previousIndex) {
								n.append("\n");
							} else {
								String toAdd = t.subSequence(previousIndex, index).toString();
								n.append(autoWrap(autoWrapOn, toAdd));
								n.append("\n");
							}
							index++;
							previousIndex = index;

						}
						String toAdd = t.subSequence(previousIndex, s.length()).toString();
						n.append(autoWrap(autoWrapOn, toAdd));
						s = n.toString();

					} else {
						s = autoWrap(autoWrapOn, s);
					}
				}
				assert (s != null);

				if(s.contains("\n")) {
					containsNL = true;
					ColoredInterface c = null;
					if(text.get(i) instanceof ColoredInterface) {
						c = (ColoredInterface) text.get(i);
					}
					String t = s;
					int index = 0;
					int previousIndex = 0;
					while((index = t.indexOf("\n", index)) >= 0) {
						if(index == previousIndex) {
							addCache("", null);
						} else {
							String toAdd = t.subSequence(previousIndex, index).toString();
							addCache(toAdd, c);
						}
						limitTextDraw++;
						index++;
						previousIndex = index;

					}
					assert (previousIndex >= 0);
					assert (t.length() > 0);
					if(previousIndex == t.length()) {
						addCache("", null);
					} else {
						assert (previousIndex >= 0);
						assert (t.length() > 0);
						addCache(t.subSequence(previousIndex, t.length()).toString(), c);
					}
					limitTextDraw += 2;
					assert (!this.text.isEmpty());
				} else {
					addCache(text.get(i), null);
				}
			}
		}
		if(dirty) {
			onDirty();
		}
	}

	private String autoWrap(GUIResizableElement wrap, String s) {
		if(s.length() <= 1) {
			return s;
		}
		int wrapWidth;

		if(wrap instanceof GUIScrollablePanel) {
			wrapWidth = (int) Math.max(1, ((GUIScrollablePanel) wrap).getClipWidth());
		} else {
			wrapWidth = (int) Math.max(1, wrap.getWidth());
		}

		return wrap(s, wrapWidth);
	}

	public int getWidthOfFont(String str) {
		return FontLibrary.getMetrics(fontSize.getFont()).stringWidth(str);
	}

	private int findLenOnWidth(String text, int len) {
		if(getWidthOfFont(text) < len) {
			return text.length() + 1;
		} else {

			int lWidth = getWidthOfFont("l");
			int startFrom = len / lWidth;

			StringBuffer b = new StringBuffer(text.substring(0, Math.min(text.length(), startFrom)));

			int width;
			int start = 0;
			int end = b.length();
			int t = 0;
			while((width = getWidthOfFont(b.toString())) > len) {
				b.deleteCharAt(b.length() - 1);
				t++;
//				System.err.println("REMOVED:::: "+b.length()+": "+b.toString()+"; "+width+" / "+len);
			}
//			System.err.println("REMOVED:::: "+b.length()+": "+b.toString()+"; "+width+" / "+len+": "+t);
			return Math.max(0, b.length() - 1);
		}
	}

	private String wrap(String in, int lenPX) {

//		wrapSimple = !FontLibrary.isDefaultFont();

		int[] is = fontWidths.get(fontSize.getFont());
		if(is == null) {
			is = createFontTable(fontSize.getFont());
			fontWidths.put(fontSize.getFont(), is);
		}

		in = in.trim();
		if(in.isEmpty()) {
			return "";
		}
		int len;
		if(wrapSimple) {
			int m = 0;
			int margin = 14;
			int px = margin;
			len = 0;

			while(px < lenPX && m < in.length()) {
				int val = in.charAt(m);
				int w = -1;
				if(val >= 0 && val < is.length) {
					w = is[val];
				}
				if(w <= 0) {
					w = fontSize.getFont().getSpaceWidth() + 5;
				}
				px += w;
				m++;
			}
			len = Math.max(0, m + 1);
//			len = lenPX / getWidthOfFont("@");//getSpaceWidth();
		} else {
			len = findLenOnWidth(in, lenPX);
			assert (len >= 0) : "len invalid: " + len + ": \"" + in + "\"; PX: " + lenPX;
		}
//		System.err.println("WRAP:: "+in+": "+ lenPX+" -> "+len+"; ok "+(in.length() < len));
		if(in.length() < len) {
			return in;
		}
		assert (len >= 0) : "len invalid: " + len + ": " + in;
		if(in.substring(0, len).contains("\n")) {
			return in.substring(0, in.indexOf("\n")).trim() + "\n\n"
					+ wrap(in.substring(in.indexOf("\n") + 1), len);
		}
		int place;
//		if(WITH_DASH){
//			int dash = in.lastIndexOf("-", len);
//			place = Math.max(
//					Math.max(in.lastIndexOf(" ", len), in.lastIndexOf("\t", len)),
//					dash >= 0 ? dash+1 : -1);
//		}else{
		place = Math.max(in.lastIndexOf(" ", len), in.lastIndexOf("\t", len));
//		}
		if(place < 0) {
			return in;
		}
		return in.substring(0, place).trim() + "\n"
				+ wrap(in.substring(place), lenPX);
	}

	public void onDirty() {
	}

	void drawText() {
		if(firstDraw) {
			onInit();
		}

		if(fontSize == null || fontSize.getFont() == null) {
			try {
				throw new NullPointerException("Font not initialized: " + text);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return;
		}

		GlUtil.glDisable(GL11.GL_LIGHTING);
		if(translateOnlyMode) {
			translate();
		} else {
			GlUtil.glPushMatrix();
			transform();
		}
		try {
			if(blend) {
				GlUtil.glEnable(GL11.GL_BLEND);
				GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				if(!doDepthTest) {
					GlUtil.glDisable(GL11.GL_DEPTH_TEST);
				} else {
				}
			} else {
				GlUtil.glDisable(GL11.GL_BLEND);
			}

			Vector2f offset = FontLibrary.offsetMap.get(getFont().getFontFile() + getFont().getFont().getSize2D());

			float x = offset != null ? offset.x : 0;
			float y = offset != null ? offset.y : 0;

			int limitTextDraw = this.limitTextDraw;
			createCache(false);
			if(dirty) {
				maxLineWidth = 0;
			}

			int i = 0;
			if(clipStartPX >= 0) {
				int skipped = FastMath.fastFloor((float) clipStartPX / (float) getFont().getLineHeight());
				y = skipped * getFont().getLineHeight();
				i = skipped;
			}
			for(; i < textCache.size(); i++) {
				if(wasCacheDirty) {
					try {
						if(textCache.get(i) == null || textCache.get(i).toString() == null) {
							try {
								throw new Exception("TEXT NULL ON GUI OVERLAY " + textCache);
							} catch(Exception e) {
								e.printStackTrace();
							}
							break;
						}

					} catch(Exception e) {
						System.err.println("TEXT NULL ON GUI OVERLAY");
						e.printStackTrace();
					}
				}
				if(clipEndPX >= 0 && y > clipEndPX) {

					break;
				}

				if(textCache.get(i) instanceof ColoredInterface) {
					Vector4f c = (((ColoredInterface) textCache.get(i)).getColor());
					color.r = c.x;
					color.g = c.y;
					color.b = c.z;
					color.a = c.w;
					if(color.a <= 0) {
						continue;
					}
				}
				GlUtil.glColor4f(color.r, color.b, color.g, color.a);
				if(limitTextDraw >= 0 && i >= limitTextDraw) {
					break;
				}
				if(textCache.get(i) == null || textCache.get(i).toString() == null) {
					try {
						throw new Exception("TEXT NULL ON GUI OVERLAY " + textCache);
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				}
				String s = textCache.get(i).toString();
				DisplayList drawDisplayList = fontSize.getFont().drawDisplayList(x, y, s, color, 0, s.length());
				y += getFont().getLineHeight();
				if(dirty) {
					maxLineWidth = Math.max(maxLineWidth, getFont().getWidth(s));
				}
			}

			if(clipStartPX >= 0 && clipEndPX >= 0) {
				int size = textCache.size();
				textHeight = (size * getFont().getLineHeight());
			} else {
				textHeight = (((int) y));
			}

			if(isMouseUpdateEnabled()) {
				checkMouseInside();
			}
			GlUtil.glColor4f(1, 1, 1, 1);
			GlUtil.glDisable(GL11.GL_BLEND);
			GlUtil.glEnable(GL11.GL_LIGHTING);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(translateOnlyMode) {
				translateBack();
			} else {
				GlUtil.glPopMatrix();
			}
		}
		wasCacheDirty = false;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color.r = color.r;
		this.color.g = color.g;
		this.color.b = color.b;
		this.color.a = color.a;
	}

	public void setColor(Vector4f color) {
		this.color.r = color.x;
		this.color.g = color.y;
		this.color.b = color.z;
		this.color.a = color.w;
	}

	public void setColor(java.awt.Color color) {
		this.color.r = color.getRed();
		this.color.g = color.getGreen();
		this.color.b = color.getBlue();
		this.color.a = color.getAlpha();
	}

	/**
	 * @return the font
	 */
	public UnicodeFont getFont() {
		return fontSize.getFont();
	}

	/**
	 * @param fontSize the font to set
	 */
	public void setFont(FontInterface fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * @return the limitTextDraw
	 */
	public int getLimitTextDraw() {
		return limitTextDraw;
	}

	/**
	 * @param limitTextDraw the limitTextDraw to set
	 */
	public void setLimitTextDraw(int limitTextDraw) {
		if(limitTextDraw != this.limitTextDraw) {
			dirty = true;
		}
		this.limitTextDraw = limitTextDraw;

	}

	/**
	 * @return the text
	 */
	public List<Object> getText() {
		return text;
	}

	public void setText(List<Object> arrayList) {
		this.text = arrayList;
	}

	/**
	 * @return the beginTextAtLast
	 */
	public boolean isBeginTextAtLast() {
		return beginTextAtLast;
	}

	/**
	 * @param beginTextAtLast the beginTextAtLast to set
	 */
	public void setBeginTextAtLast(boolean beginTextAtLast) {
		this.beginTextAtLast = beginTextAtLast;
	}

	/**
	 * @return the blend
	 */
	public boolean isBlend() {
		return blend;
	}

	/**
	 * @param blend the blend to set
	 */
	public void setBlend(boolean blend) {
		this.blend = blend;
	}

	/**
	 * @param color the color to set
	 */
	public void setAWTColor(java.awt.Color color) {
		this.color.a = color.getAlpha();
		this.color.r = color.getRed();
		this.color.b = color.getBlue();
		this.color.g = color.getGreen();
	}

	public void setClip(int startPX, int endPX) {
		this.clipStartPX = startPX;
		this.clipEndPX = endPX;
	}

	public void setColor(float r, float g, float b, float a) {
		color.r = r;
		color.g = g;
		color.b = b;
		color.a = a;
	}

	public void setTextSimple(Object simpleString) {
		if(this.text == null) {
			this.text = new ArrayList<Object>();
		} else {
			this.text.clear();
		}
		if(debug) {
			try {
				if(simpleString == null) {
					System.err.println("ERROR: STRING SET TO NULL");
				}
				simpleString.toString();
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
		text.add(simpleString);
	}

	public void useUncachedDefaultFont(boolean b) {
		this.useUncachedDefaultFont = b;
	}

	public void setTextFromArray(String[] split) {

		text = new ArrayList<Object>(split.length);

		for(int i = 0; i < split.length; i++) {
			text.add(split[i]);
		}
	}

//	public void updateSize() {
//		onInit();
//		int y = 0;
//		for(int i = 0; i < text.size(); i++){
//			if(clipStartPX >= 0){
//
//				int skipped = (int) Math.floor((float)clipStartPX / (float)getFont().getLineHeight());
//				y = skipped *getFont().getLineHeight();
//				i = skipped;
//				for(; i < textCache.size() ; i++){
//					if(clipEndPX >= 0 && y > clipEndPX){
//
//						break;
//					}
//
//					if(textCache.get(i) instanceof ColoredInterface){
//						Vector4f c = (((ColoredInterface)textCache.get(i)).getColor());
//						color.r = c.x;
//						color.g = c.y;
//						color.b = c.z;
//						color.a = c.w;
//						if(color.a <= 0){
//							continue;
//						}
//					}
//					GlUtil.glColor4f(color.r,color.b,color.g,color.a);
//					if(limitTextDraw >= 0 && i >= limitTextDraw){
//						break;
//					}
//
//					y+= getFont().getLineHeight();
//
//
//				}
//			}
//			if(clipStartPX >= 0 && clipEndPX >= 0){
//				assert(textCache.size() > 0);
//				int size = textCache.size();
//				setHeight(size * getFont().getLineHeight());
//			}else{
//				setHeight((int) y);
//			}
//		}
//	}

	/**
	 * @return the currentLineHeight
	 */
	public int getCurrentLineHeight(int lineIndex) {
		return lineIndex * getFont().getLineHeight();
//		int c = 0;
//		for(int i = 0; i < lineIndex && i < currentLineHeight.size(); i++){
//			c += currentLineHeight.get(i);
//		}
//		return c;
	}

	/**
	 * @return the maxLineWidth
	 */
	public int getMaxLineWidth() {
		return maxLineWidth;
	}

	public void updateTextSize() {
		createCache(false);
		maxLineWidth = 0;
		for(int i = 0; i < textCache.size(); i++) {
			assert (textCache != null);
			assert (textCache.get(i) != null);
			maxLineWidth = Math.max(maxLineWidth, getFont().getWidth(textCache.get(i).toString()));
		}
		textHeight = (textCache.size() * getFont().getLineHeight());
	}

	/**
	 * @return the textHeight
	 */
	public int getTextHeight() {
		return textHeight;
	}

	public int getCurrentLines() {
		return textCache.size();
	}

	public FontInterface getFontSize() {
		return fontSize;
	}
}
